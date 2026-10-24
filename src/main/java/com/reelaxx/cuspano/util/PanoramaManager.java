package com.reelaxx.cuspano.util;

import net.minecraft.client.Minecraft;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public class PanoramaManager {
    public static final Path PANORAMA_PATH = Minecraft.getInstance().gameDirectory.toPath().resolve("config/custompanoramas");

    public static List<String> getAvailablePanoramas() {
        List<String> panoramas = new ArrayList<>();
        if (!Files.exists(PANORAMA_PATH)) {
            PANORAMA_PATH.toFile().mkdirs();
            return panoramas;
        }

        try (Stream<Path> paths = Files.list(PANORAMA_PATH)) {
            paths.filter(Files::isDirectory)
                 .forEach(path -> panoramas.add(path.getFileName().toString()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return panoramas;
    }

    public static void deletePanorama(String name) {
        Path toDelete = PANORAMA_PATH.resolve(name);
        try (Stream<Path> walk = Files.walk(toDelete)) {
            walk.sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}