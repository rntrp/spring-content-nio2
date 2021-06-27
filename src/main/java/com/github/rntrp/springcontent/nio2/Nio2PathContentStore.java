package com.github.rntrp.springcontent.nio2;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.content.commons.annotations.ContentId;
import org.springframework.content.commons.annotations.ContentLength;
import org.springframework.content.commons.property.PropertyPath;
import org.springframework.content.commons.repository.ContentStore;
import org.springframework.content.commons.repository.StoreAccessException;
import org.springframework.content.commons.utils.BeanUtils;
import org.springframework.content.commons.utils.Condition;
import org.springframework.content.commons.utils.PlacementService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.io.Resource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.UUID;

@Transactional(readOnly = true)
public class Nio2PathContentStore<S, SID extends Serializable> implements ContentStore<S, SID> {
    private static final Condition NOT_PERSISTENCE_ID = field -> {
        for (Annotation annotation : field.getAnnotations()) {
            switch(annotation.annotationType().getCanonicalName()) {
                case "javax.persistence.Id":
                case "org.springframework.data.annotation.Id":
                    return false;
                default:
                    break;
            }
        }
        return true;
    };
    private final Nio2PathResourceLoader loader;
    private final PlacementService placer;

    public Nio2PathContentStore(Nio2PathResourceLoader loader, PlacementService placer) {
        this.loader = loader;
        this.placer = placer;
    }

    @Transactional
    @Override
    public S setContent(S property, InputStream content) {
        Object contentId = BeanUtils.getFieldWithAnnotation(property, ContentId.class);
        if (contentId == null) {
            Serializable newId = UUID.randomUUID().toString();
            Object convertedId = convertToExternalContentIdType(property, newId);
            BeanUtils.setFieldWithAnnotation(property, ContentId.class, convertedId);
        }
        Nio2PathResource resource = this.getResource(property);
        if (resource == null) {
            return property;
        }
        try {
            resource.write(content);
        } catch (IOException e) {
            String msg = String.format("Setting content for entity %s", property);
            throw new StoreAccessException(msg, e);
        }
        long contentLength;
        try {
            contentLength = resource.contentLength();
        } catch (IOException e) {
            String msg = String.format("Setting content length for entity %s", property);
            throw new StoreAccessException(msg, e);
        }
        BeanUtils.setFieldWithAnnotation(property, ContentLength.class, contentLength);
        return property;
    }

    @Transactional
    @Override
    public S setContent(S property, Resource resourceContent) {
        try (InputStream in = resourceContent.getInputStream()) {
            return setContent(property, in);
        } catch (IOException e) {
            String msg = String.format("Setting content for entity %s", property);
            throw new StoreAccessException(msg, e);
        }
    }

    @Transactional
    @Override
    public S unsetContent(S property) {
        if (property == null) {
            return null;
        }
        Nio2PathResource resource = getResource(property);
        if (resource != null && resource.exists()) {
            try {
                resource.delete();
            } catch (IOException e) {
                String msg = String.format("Unsetting content for entity %s", property);
                throw new StoreAccessException(msg, e);
            }
        }
        unassociate(property);
        BeanUtils.setFieldWithAnnotation(property, ContentLength.class, 0);
        return property;
    }

    @Override
    public InputStream getContent(S property) {
        if (property == null) {
            return null;
        }
        Resource resource = getResource(property);
        try {
            if (resource != null && resource.exists()) {
                return resource.getInputStream();
            }
        } catch (IOException e) {
            String msg = String.format("Getting content for entity %s", property);
            throw new StoreAccessException(msg, e);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Nio2PathResource getResource(S entity) {
        if (placer.canConvert(entity.getClass(), String.class)) {
            String location = placer.convert(entity, String.class);
            if (location != null) {
                return loader.getResource(location);
            }
        }
        SID contentId = (SID) BeanUtils.getFieldWithAnnotation(entity, ContentId.class);
        return contentId == null ? null : getResource(contentId);
    }

    @Override
    public Nio2PathResource getResource(S entity, PropertyPath propertyPath) {
        SID contentId = getContentId(entity, propertyPath);
        if (contentId == null) {
            UUID newId = UUID.randomUUID();
            Object convertedId = convertToExternalContentIdType(entity, newId);
            BeanUtils.setFieldWithAnnotation(entity, ContentId.class, convertedId);
        }
        return getResource(contentId);
    }

    @Override
    public void associate(S entity, SID id) {
        BeanUtils.setFieldWithAnnotation(entity, ContentId.class, id.toString());
    }

    @Override
    public void associate(S entity, PropertyPath propertyPath, SID id) {
        setContentId(entity, propertyPath, id, null);
    }

    @Override
    public void unassociate(S entity) {
        BeanUtils.setFieldWithAnnotationConditionally(entity, ContentId.class, null, NOT_PERSISTENCE_ID);
    }

    @Override
    public void unassociate(S entity, PropertyPath propertyPath) {
        setContentId(entity, propertyPath, null, NOT_PERSISTENCE_ID);
    }

    @Override
    public Nio2PathResource getResource(SID id) {
        String location = placer.convert(id, String.class);
        return location == null ? null : loader.getResource(location);
    }

    private Object convertToExternalContentIdType(S property, Object contentId) {
        if (placer.canConvert(TypeDescriptor.forObject(contentId),
                TypeDescriptor.valueOf(BeanUtils.getFieldWithAnnotationType(property,
                        ContentId.class)))) {
            contentId = placer.convert(contentId, TypeDescriptor.forObject(contentId),
                    TypeDescriptor.valueOf(BeanUtils.getFieldWithAnnotationType(property,
                            ContentId.class)));
            return contentId;
        }
        return contentId.toString();
    }

    @SuppressWarnings("unchecked")
    private SID getContentId(S entity, PropertyPath propertyPath) {
        Assert.notNull(entity, "entity must not be null");
        Assert.notNull(propertyPath, "propertyPath must not be null");

        BeanWrapper wrapper = new BeanWrapperImpl(entity);
        Field[] contentIdFields = BeanUtils.findFieldsWithAnnotation(entity.getClass(), ContentId.class, wrapper);
        for (Field contentIdField : contentIdFields) {
            if (contentIdField.getName().startsWith(propertyPath.getName())) {
                return (SID) wrapper.getPropertyValue(contentIdField.getName());
            }
        }
        String msg = String.format("Invalid property path '%s'", propertyPath.getName());
        throw new IllegalArgumentException(msg);
    }

    private void setContentId(S entity, PropertyPath propertyPath, SID contentId, Condition condition) {
        Assert.notNull(entity, "entity must not be null");
        Assert.notNull(propertyPath, "propertyPath must not be null");

        BeanWrapper wrapper = new BeanWrapperImpl(entity);
        Field[] contentIdFields = BeanUtils.findFieldsWithAnnotation(entity.getClass(), ContentId.class, wrapper);
        for (Field contentIdField : contentIdFields) {
            if (contentIdField.getName().startsWith(propertyPath.getName())) {
                if (condition == null || condition.matches(contentIdField)) {
                    wrapper.setPropertyValue(contentIdField.getName(), contentId);
                }
                return;
            }
        }
        String msg = String.format("Invalid property path '%s'", propertyPath.getName());
        throw new IllegalArgumentException(msg);
    }

    public Nio2PathResourceLoader getLoader() {
        return loader;
    }

    public PlacementService getPlacer() {
        return placer;
    }
}
