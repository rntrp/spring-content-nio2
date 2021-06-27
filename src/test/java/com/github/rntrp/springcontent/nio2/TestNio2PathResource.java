package com.github.rntrp.springcontent.nio2;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;

import static com.github.rntrp.springcontent.nio2.TestNio2PathUtils.NIO2_PATH_SERVICE;
import static com.github.rntrp.springcontent.nio2.TestNio2PathUtils.newFileSystem;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.*;

class TestNio2PathResource {
    @ParameterizedTest
    @ValueSource(strings = {"osx", "windows", "unix"})
    void testGetFilePath(String configuration) throws IOException {
        try (FileSystem fs = newFileSystem(configuration)) {
            Path path = fs.getPath("test");
            Nio2PathResource resource = new Nio2PathResource(path, NIO2_PATH_SERVICE);
            assertEquals(path, resource.getFilePath());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"osx", "windows", "unix"})
    void testCreateParentDirectory(String configuration) throws IOException {
        try (FileSystem fs = newFileSystem(configuration)) {
            Path root = fs.getPath("a/b/");
            Nio2PathResource resource = new Nio2PathResource(root, NIO2_PATH_SERVICE);
            assertTrue(resource.createParentDirectory());
            assertTrue(Files.exists(resource.getFilePath().getParent()));
            assertTrue(Files.isDirectory(resource.getFilePath().getParent()));
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"osx", "windows", "unix"})
    void testCreateRelative(String configuration) throws IOException {
        try (FileSystem fs = newFileSystem(configuration)) {
            Path root = fs.getPath("a/b/");
            Nio2PathResource resource = new Nio2PathResource(root, NIO2_PATH_SERVICE);
            resource.createParentDirectory();
            Nio2PathResource relative = resource.createRelative("c/d");
            relative.write();
            assertTrue(Files.exists(relative.getFilePath()));
            assertTrue(Files.isRegularFile(relative.getFilePath()));
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"osx", "windows", "unix"})
    void testGetOutputStream(String configuration) throws IOException {
        try (FileSystem fs = newFileSystem(configuration)) {
            Path path = fs.getPath("test");
            Nio2PathResource resource = new Nio2PathResource(path, NIO2_PATH_SERVICE);
            try (OutputStream out = resource.getOutputStream()) {
                out.write("foo".getBytes(UTF_8));
            }
            assertEquals("foo", Files.readAllLines(path).iterator().next());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"osx", "windows", "unix"})
    void testDelete(String configuration) throws IOException {
        try (FileSystem fs = newFileSystem(configuration)) {
            Path path = fs.getPath("a/b/c");
            Files.createDirectories(path.getParent());
            Files.write(path, "foo".getBytes(UTF_8));
            Nio2PathResource resource = new Nio2PathResource(path, NIO2_PATH_SERVICE);
            resource.delete();
            assertFalse(resource.exists());
            assertFalse(Files.exists(path));
            assertFalse(Files.exists(path.getParent()));
            assertFalse(Files.exists(path.getParent().getParent()));
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"osx", "windows", "unix"})
    void testWriteInputStream(String configuration) throws IOException {
        try (FileSystem fs = newFileSystem(configuration)) {
            Path path = fs.getPath("test");
            Nio2PathResource resource = new Nio2PathResource(path, NIO2_PATH_SERVICE);
            try (InputStream in = new ByteArrayInputStream("foo".getBytes(UTF_8))) {
                resource.write(in);
            }
            assertEquals("foo", Files.readAllLines(path).iterator().next());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"osx", "windows", "unix"})
    void testWriteStrings(String configuration) throws IOException {
        try (FileSystem fs = newFileSystem(configuration)) {
            Path path = fs.getPath("test");
            Nio2PathResource resource = new Nio2PathResource(path, NIO2_PATH_SERVICE);
            resource.write("foo", "bar");
            Iterator<String> lines = Files.readAllLines(path).iterator();
            assertEquals("foo", lines.next());
            assertEquals("bar", lines.next());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"osx", "windows", "unix"})
    void testWriteBytes(String configuration) throws IOException {
        try (FileSystem fs = newFileSystem(configuration)) {
            Path path = fs.getPath("test");
            Nio2PathResource resource = new Nio2PathResource(path, NIO2_PATH_SERVICE);
            resource.write("foo".getBytes(UTF_8));
            assertEquals("foo", Files.readAllLines(path).iterator().next());
        }
    }
}
