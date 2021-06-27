package com.github.rntrp.springcontent.nio2;

import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Nio2PathServiceImpl implements Nio2PathService {
    @Override
    public void mkdirs(Path path) throws IOException {
        Files.createDirectories(path);
    }

    @Override
    public void rmdirs(Path from, Path to) throws IOException {
        if (!Files.isDirectory(from)) {
            throw new IOException("Not a directory");
        }
        for (Path dir = from; dir != null && !dir.equals(to); dir = dir.getParent()) {
            try {
                Files.deleteIfExists(dir);
            } catch (DirectoryNotEmptyException e) {
                break;
            }
        }
    }

    @Override
    public void rmdirs(Path from) throws IOException {
        rmdirs(from, null);
    }
}
