package com.reelaxx.cuspano.util;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.repository.PackRepository;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public class PanoramaPackGenerator {
    public static void applyFilesSilently(String folderName) {
        File gameDir = FabricLoader.getInstance().getGameDir().toFile();
        File packDir = new File(gameDir, "resourcepacks/CusPano_Panorama");
        File assetsDir = new File(packDir, "assets/minecraft/textures/gui/title/background");
        assetsDir.mkdirs();

        try (FileWriter writer = new FileWriter(new File(packDir, "pack.mcmeta"))) {
            writer.write("{\n  \"pack\": {\n    \"pack_format\": 34,\n    \"min_format\": 34,\n    \"max_format\": 999,\n    \"description\": \"CusPano Auto-Generated\"\n  }\n}");
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (folderName != null && !folderName.equals("default")) {
            File sourceFolder = PanoramaManager.PANORAMA_PATH.resolve(folderName).toFile();
            if (sourceFolder.exists() && sourceFolder.isDirectory()) {
                for (int i = 0; i < 6; i++) {
                    File sourceImg = new File(sourceFolder, "panorama_" + i + ".png");
                    File destImg = new File(assetsDir, "panorama_" + i + ".png");
                    if (sourceImg.exists()) {
                        try {
                            Files.copy(sourceImg.toPath(), destImg.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        } else {
            for (int i = 0; i < 6; i++) {
                new File(assetsDir, "panorama_" + i + ".png").delete();
            }
        }
    }

    public static void applyAndReload(String folderName) {
        applyFilesSilently(folderName);
        reloadOnly();
    }

    public static void reloadOnly() {
        Minecraft client = Minecraft.getInstance();
        PackRepository packManager = client.getResourcePackRepository();
        packManager.reload();
        
        List<String> enabledPacks = new ArrayList<>(packManager.getSelectedIds());
        
        String packName = "file/CusPano_Panorama";
        if (!enabledPacks.contains(packName)) {
            enabledPacks.add(packName);
            packManager.setSelected(enabledPacks);
            client.options.updateResourcePacks(packManager);
        } else {
            client.reloadResourcePacks();
        }
    }
}