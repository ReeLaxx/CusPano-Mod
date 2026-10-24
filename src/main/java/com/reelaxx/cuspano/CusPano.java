package com.reelaxx.cuspano;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.reelaxx.cuspano.config.ModConfig;
import com.reelaxx.cuspano.util.PanoramaPackGenerator;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CusPano implements ClientModInitializer {
    public static final String MOD_ID = "cuspano";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static String currentLoadedPanorama = "default";
    public static boolean needsPanoramaReload = false;

    @Override
    public void onInitializeClient() {
        ModConfig.load();

        // Beim Spielstart ein zufälliges Panorama vorbereiten
        if (ModConfig.multiModeActive && ModConfig.selectedMultiPanoramas != null && !ModConfig.selectedMultiPanoramas.isEmpty()) {
            String nextPanorama = pickNextPanorama();
            PanoramaPackGenerator.applyFilesSilently(nextPanorama);
            LOGGER.info("CusPano: Startup-Panorama vorbereitet: {}", nextPanorama);
        }

        // Sofort beim Verlassen der Welt die neuen Dateien im Hintergrund kopieren!
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            if (ModConfig.multiModeActive && ModConfig.changeOnDisconnect) {
                if (ModConfig.selectedMultiPanoramas != null && !ModConfig.selectedMultiPanoramas.isEmpty()) {
                    String nextPanorama = pickNextPanorama();
                    PanoramaPackGenerator.applyFilesSilently(nextPanorama);
                    needsPanoramaReload = true;
                    LOGGER.info("CusPano: Neues Panorama im Hintergrund vorbereitet: {}", nextPanorama);
                }
            }
        });

        LOGGER.info("Custom Panorama Mod loaded successfully on Client!");
    }

    private static String pickNextPanorama() {
        List<String> pool = new ArrayList<>(ModConfig.selectedMultiPanoramas);
        if (pool.size() > 1 && pool.contains(currentLoadedPanorama)) {
            pool.remove(currentLoadedPanorama);
        }
        int idx = new Random().nextInt(pool.size());
        String nextPanorama = pool.get(idx);
        currentLoadedPanorama = nextPanorama;
        return nextPanorama;
    }
}