package com.github.rntrp.springcontent.nio2;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Path;

import static com.github.rntrp.springcontent.nio2.TestNio2PathUtils.NIO2_PATH_SERVICE;
import static com.github.rntrp.springcontent.nio2.TestNio2PathUtils.newFileSystem;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TestNio2PathResourceLoader {
    @ParameterizedTest
    @ValueSource(strings = {"osx", "windows", "unix"})
    void testGetRootResource(String configuration) throws IOException {
        try (FileSystem fs = newFileSystem(configuration)) {
            Path root = fs.getPath("root/");
            Nio2PathResourceLoader loader = new Nio2PathResourceLoader(root, NIO2_PATH_SERVICE);
            assertEquals(root, loader.getRootResource().getFilePath());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"osx", "windows", "unix"})
    void testGetResource(String configuration) throws IOException {
        try (FileSystem fs = newFileSystem(configuration)) {
            Path root = fs.getPath("root/");
            Nio2PathResourceLoader loader = new Nio2PathResourceLoader(root, NIO2_PATH_SERVICE);
            Nio2PathResource resource = loader.getResource("foo/bar");
            Path path = fs.getPath("root/foo/bar");
            assertEquals(path, resource.getFilePath());
        }
    }
}
