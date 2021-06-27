package com.github.rntrp.springcontent.nio2;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.github.rntrp.springcontent.nio2.TestNio2PathUtils.NIO2_PATH_SERVICE;
import static com.github.rntrp.springcontent.nio2.TestNio2PathUtils.newFileSystem;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TestNio2PathServiceImpl {
    @ParameterizedTest
    @ValueSource(strings = {"osx", "windows", "unix"})
    void testMkdirs(String configuration) throws IOException {
        try (FileSystem fs = newFileSystem(configuration)) {
            Path path = fs.getPath("a/b/c");
            assertFalse(Files.exists(path));
            NIO2_PATH_SERVICE.mkdirs(path);
            assertTrue(Files.exists(path));
            assertTrue(Files.isDirectory(path));
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"osx", "windows", "unix"})
    void testRmdirs(String configuration) throws IOException {
        try (FileSystem fs = newFileSystem(configuration)) {
            Path path = fs.getPath("a/b/c");
            NIO2_PATH_SERVICE.mkdirs(path);
            assertTrue(Files.exists(path));
            NIO2_PATH_SERVICE.rmdirs(path);
            assertFalse(Files.exists(path));
            assertFalse(Files.exists(path.getParent()));
            assertFalse(Files.exists(path.getParent().getParent()));
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"osx", "windows", "unix"})
    void testRmdirsTo(String configuration) throws IOException {
        try (FileSystem fs = newFileSystem(configuration)) {
            Path path = fs.getPath("a/b/c");
            NIO2_PATH_SERVICE.mkdirs(path);
            assertTrue(Files.exists(path));
            Path to = fs.getPath("a");
            NIO2_PATH_SERVICE.rmdirs(path, to);
            assertFalse(Files.exists(path));
            assertFalse(Files.exists(path.getParent()));
            assertTrue(Files.exists(path.getParent().getParent()));
        }
    }
}
