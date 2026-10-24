package com.reelaxx.cuspano.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

public class ModConfig {
    private static final File CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve("cuspano.json").toFile();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static boolean showMainMenuButton = true;
    public static boolean multiModeActive = false;
    public static boolean changeOnDisconnect = false;
    public static String selectedSinglePanorama = "default";
    public static List<String> selectedMultiPanoramas = new ArrayList<>();

    public static void save() {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(new ConfigData(showMainMenuButton, multiModeActive, selectedSinglePanorama, selectedMultiPanoramas, changeOnDisconnect), writer);
        } catch (Exception e) {
            System.err.println("Error saving CusPano Config!");
            e.printStackTrace();
        }
    }

    public static void load() {
        if (!CONFIG_FILE.exists()) {
            save();
            return;
        }
        try (FileReader reader = new FileReader(CONFIG_FILE)) {
            ConfigData data = GSON.fromJson(reader, ConfigData.class);
            if (data != null) {
                showMainMenuButton = data.showMainMenuButton;
                multiModeActive = data.multiModeActive;
                changeOnDisconnect = data.changeOnDisconnect;
                if (data.selectedSinglePanorama != null) {
                    selectedSinglePanorama = data.selectedSinglePanorama;
                }
                if (data.selectedMultiPanoramas != null) {
                    selectedMultiPanoramas = data.selectedMultiPanoramas;
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading CusPano Config!");
            e.printStackTrace();
        }
    }

    private static class ConfigData {
        boolean showMainMenuButton;
        boolean multiModeActive;
        String selectedSinglePanorama;
        List<String> selectedMultiPanoramas;
        boolean changeOnDisconnect;

        public ConfigData(boolean showBtn, boolean m, String ssp, List<String> smp, boolean changeOnDisconnect) {
            this.showMainMenuButton = showBtn;
            this.multiModeActive = m;
            this.selectedSinglePanorama = ssp;
            this.selectedMultiPanoramas = smp;
            this.changeOnDisconnect = changeOnDisconnect;
        }
    }
}