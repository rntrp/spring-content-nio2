package com.github.rntrp.springcontent.nio2;

import org.springframework.content.commons.io.DeletableResource;
import org.springframework.content.commons.io.IdentifiableResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.lang.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;

public class Nio2PathResource extends FileSystemResource implements DeletableResource, IdentifiableResource {
    private final Nio2PathService pathService;
    private final Path path;
    private Serializable id;

    public Nio2PathResource(Path path, Nio2PathService pathService) {
        super(path);
        this.path = path;
        this.pathService = pathService;
    }

    public Path getFilePath() {
        return path;
    }

    @NonNull
    @Override
    public Nio2PathResource createRelative(@NonNull String relativePath) {
        return new Nio2PathResource(path.resolve(relativePath), pathService);
    }

    @Override
    public void delete() throws IOException {
        try {
            Files.deleteIfExists(path);
        } finally {
            Path parent = path.getParent();
            if (parent != null) {
                pathService.rmdirs(parent);
            }
        }
    }

    @NonNull
    @Override
    public OutputStream getOutputStream() throws IOException {
        if (!Files.exists(path)) {
            Path parent = path.getParent();
            if (parent != null) {
                pathService.mkdirs(parent);
            }
            Files.createFile(path);
        }
        return Files.newOutputStream(path);
    }

    @Override
    public Serializable getId() {
        return id;
    }

    @Override
    public void setId(Serializable id) {
        this.id = id;
    }

    public void write(InputStream in) throws IOException {
        createParentDirectory();
        Files.copy(in, path, StandardCopyOption.REPLACE_EXISTING);
    }

    public void write(String... lines) throws IOException {
        write(Arrays.asList(lines));
    }

    public void write(Iterable<String> lines) throws IOException {
        createParentDirectory();
        Files.write(path, lines);
    }

    public void write(byte[] bytes) throws IOException {
        createParentDirectory();
        Files.write(path, bytes);
    }

    public boolean createParentDirectory() throws IOException {
        if (!Files.exists(path)) {
            Path parent = path.getParent();
            if (parent != null) {
                pathService.mkdirs(parent);
            }
            return true;
        }
        return false;
    }
}
