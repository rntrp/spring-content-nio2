package com.github.rntrp.springcontent.nio2;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import org.springframework.content.commons.utils.PlacementService;
import org.springframework.content.commons.utils.PlacementServiceImpl;

import java.io.Serializable;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.Locale;

class TestNio2PathUtils {
    static final Nio2PathService NIO2_PATH_SERVICE = new Nio2PathServiceImpl();
    static final PlacementService PLACEMENT_SERVICE = new PlacementServiceImpl();

    /**
     * Suppresses default constructor, ensuring non-instantiability.
     */
    private TestNio2PathUtils() {
        // Suppresses default constructor, ensuring non-instantiability.
    }

    static Configuration getConfiguration(String configuration) {
        switch (configuration.toLowerCase(Locale.ROOT)) {
            case "osx":
                return Configuration.osX();
            case "windows":
                return Configuration.windows();
            case "unix":
                return Configuration.unix();
            default:
                throw new IllegalArgumentException("Unsupported configuration: " + configuration);
        }
    }

    static FileSystem newFileSystem(String configuration) {
        return Jimfs.newFileSystem(getConfiguration(configuration));
    }

    static <S, SID extends Serializable> Nio2PathContentStore<S, SID> initStore(FileSystem fileSystem, String root) {
        Path path = fileSystem.getPath(root);
        Nio2PathResourceLoader loader = new Nio2PathResourceLoader(path, NIO2_PATH_SERVICE);
        return new Nio2PathContentStore<>(loader, PLACEMENT_SERVICE);
    }
}
