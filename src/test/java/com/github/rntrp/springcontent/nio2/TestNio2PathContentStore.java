package com.github.rntrp.springcontent.nio2;

import com.github.rntrp.springcontent.nio2.spring.TestEntity;
import com.github.rntrp.springcontent.nio2.spring.TestEntityMultiContent;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.content.commons.property.PropertyPath;

import java.io.*;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.util.List;

import static com.github.rntrp.springcontent.nio2.TestNio2PathUtils.initStore;
import static com.github.rntrp.springcontent.nio2.TestNio2PathUtils.newFileSystem;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.*;

class TestNio2PathContentStore {
    @ParameterizedTest
    @ValueSource(strings = {"osx", "windows", "unix"})
    void testGetContent(String configuration) throws IOException {
        try (FileSystem fs = newFileSystem(configuration)) {
            Nio2PathContentStore<TestEntity, Integer> store = initStore(fs, "test");
            Files.createDirectories(fs.getPath("test"));
            Files.write(fs.getPath("test", "42"), singletonList("foo"));
            TestEntity entity = new TestEntity();
            entity.setContent(42);
            entity.setContentLength(Files.size(fs.getPath("test", "42")));
            try (BufferedReader r = new BufferedReader(new InputStreamReader(store.getContent(entity), UTF_8))) {
                assertEquals("foo", r.readLine());
            }
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"osx", "windows", "unix"})
    void testSetContent(String configuration) throws IOException {
        try (FileSystem fs = newFileSystem(configuration)) {
            Nio2PathContentStore<TestEntity, Integer> store = initStore(fs, "test");
            TestEntity entity = new TestEntity();
            entity.setContent(42);
            try (InputStream in = new ByteArrayInputStream("foo".getBytes(UTF_8))) {
                store.setContent(entity, in);
            }
            assertTrue(Files.exists(store.getResource(42).getFilePath()));
            List<String> expected = singletonList("foo");
            List<String> actual = Files.readAllLines(fs.getPath("test", "42"));
            assertEquals(expected, actual);
            assertEquals(3L, entity.getContentLength());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"osx", "windows", "unix"})
    void testUnsetContent(String configuration) throws IOException {
        try (FileSystem fs = newFileSystem(configuration)) {
            Nio2PathContentStore<TestEntity, Integer> store = initStore(fs, "test");
            Files.createDirectories(fs.getPath("test"));
            Files.write(fs.getPath("test", "42"), singletonList("foo"));
            TestEntity entity = new TestEntity();
            entity.setContent(42);
            assertTrue(Files.exists(store.getResource(42).getFilePath()));
            store.unsetContent(entity);
            assertFalse(Files.exists(store.getResource(42).getFilePath()));
            assertEquals(0L, entity.getContentLength());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"osx", "windows", "unix"})
    void testAssociate(String configuration) throws IOException {
        try (FileSystem fs = newFileSystem(configuration)) {
            Nio2PathContentStore<TestEntity, Integer> store = initStore(fs, "test");
            TestEntity entity = new TestEntity();
            store.associate(entity, 42);
            assertEquals(42, entity.getContent());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"osx", "windows", "unix"})
    void testUnassociate(String configuration) throws IOException {
        try (FileSystem fs = newFileSystem(configuration)) {
            Nio2PathContentStore<TestEntity, Integer> store = initStore(fs, "test");
            TestEntity entity = new TestEntity();
            entity.setContent(42);
            store.unassociate(entity);
            assertNull(entity.getContent());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"osx", "windows", "unix"})
    void testGetResource(String configuration) throws IOException {
        try (FileSystem fs = newFileSystem(configuration)) {
            Nio2PathContentStore<TestEntityMultiContent, Integer> store = initStore(fs, "test");
            TestEntityMultiContent entity = new TestEntityMultiContent();
            entity.setContent1(43);
            Nio2PathResource resource1 = store.getResource(entity, PropertyPath.from("content1"));
            assertEquals(fs.getPath("test", "43"), resource1.getFilePath());
            entity.setContent2(44);
            Nio2PathResource resource2 = store.getResource(entity, PropertyPath.from("content2"));
            assertEquals(fs.getPath("test", "44"), resource2.getFilePath());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"osx", "windows", "unix"})
    void testAssociatePropertyPath(String configuration) throws IOException {
        try (FileSystem fs = newFileSystem(configuration)) {
            Nio2PathContentStore<TestEntityMultiContent, Integer> store = initStore(fs, "test");
            TestEntityMultiContent entity = new TestEntityMultiContent();
            store.associate(entity, PropertyPath.from("content1"), 43);
            assertEquals(43, entity.getContent1());
            store.associate(entity, PropertyPath.from("content2"), 44);
            assertEquals(44, entity.getContent2());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"osx", "windows", "unix"})
    void testUnassociatePropertyPath(String configuration) throws IOException {
        try (FileSystem fs = newFileSystem(configuration)) {
            Nio2PathContentStore<TestEntityMultiContent, Integer> store = initStore(fs, "test");
            TestEntityMultiContent entity = new TestEntityMultiContent();
            entity.setContent1(43);
            store.unassociate(entity, PropertyPath.from("content1"));
            assertNull(entity.getContent1());
            entity.setContent2(44);
            store.unassociate(entity, PropertyPath.from("content2"));
            assertNull(entity.getContent2());
        }
    }
}
