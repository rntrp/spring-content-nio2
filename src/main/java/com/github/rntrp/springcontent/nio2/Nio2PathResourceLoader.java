package com.github.rntrp.springcontent.nio2;

import org.springframework.core.io.FileSystemResourceLoader;

import java.nio.file.Path;
import java.util.Objects;

public class Nio2PathResourceLoader extends FileSystemResourceLoader {
    private final Nio2PathResource root;

    public Nio2PathResourceLoader(Path root, Nio2PathService fileService) {
        Objects.requireNonNull(root);
        Objects.requireNonNull(fileService);
        this.root = new Nio2PathResource(root, fileService);
    }

    public Nio2PathResource getRootResource() {
        return root;
    }

    @Override
    public Nio2PathResource getResource(String location) {
        return root.createRelative(location);
    }
}
