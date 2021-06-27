package com.github.rntrp.springcontent.nio2;

import java.io.IOException;
import java.nio.file.Path;

public interface Nio2PathService {

    void mkdirs(Path path) throws IOException;

    /**
     * Removes directories.
     *
     * Starting at the leaf of {@code from} and working upwards removes directories if, and only if, empty
     * until {@code to} is reached.

     * @param from
     * 			the directory path to be removed
     * @param to
     * 			the sub-directory to preserve.  Maybe null
     * @throws IOException
     *          when the directories cannot be removed
     */
    void rmdirs(Path from, Path to) throws IOException;

    /**
     * Removes directories.
     *
     * Starting at the leaf of {@code from} and working upwards removes directories if, and only if, empty.

     * @param from
     * 			the directory path to be removed
     * @throws IOException
     *          when the directories cannot be removed
     */
    void rmdirs(Path from) throws IOException;
}
