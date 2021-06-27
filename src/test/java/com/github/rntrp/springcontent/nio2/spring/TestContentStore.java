package com.github.rntrp.springcontent.nio2.spring;

import com.github.rntrp.springcontent.nio2.Nio2PathContentStore;
import com.github.rntrp.springcontent.nio2.Nio2PathResourceLoader;
import com.github.rntrp.springcontent.nio2.Nio2PathServiceImpl;
import com.google.common.jimfs.Jimfs;
import org.apache.commons.io.file.PathUtils;
import org.springframework.content.commons.utils.PlacementServiceImpl;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Path;

@Component
public class TestContentStore extends Nio2PathContentStore<TestEntity, Integer> implements AutoCloseable {
    private final FileSystem fileSystem;

    public TestContentStore() {
        super(loader(), new PlacementServiceImpl());
        fileSystem = getLoader().getRootResource().getFilePath().getFileSystem();
    }

    private static Nio2PathResourceLoader loader() {
        FileSystem fs = Jimfs.newFileSystem(com.google.common.jimfs.Configuration.unix());
        return new Nio2PathResourceLoader(fs.getPath("/"), new Nio2PathServiceImpl());
    }

    public void clean() throws IOException {
        for (Path root : fileSystem.getRootDirectories()) {
            PathUtils.cleanDirectory(root);
        }
    }

    @PreDestroy
    @Override
    public void close() throws IOException {
        try {
            clean();
        } finally {
            if (fileSystem != null && fileSystem.isOpen()) {
                fileSystem.close();
            }
        }
    }
}
