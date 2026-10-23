package com.reelaxx.cuspano;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.reelaxx.cuspano.config.ModConfig;
import com.reelaxx.cuspano.util.PanoramaPackGenerator;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CusPano implements ModInitializer {
    public static final String MOD_ID = "cuspano";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    // Speichert das zuletzt geladene Panorama, um doppelte Ziehungen zu vermeiden
    public static String currentLoadedPanorama = "default";

    @Override
    public void onInitialize() {
        ModConfig.load();

        if (ModConfig.multiModeActive && ModConfig.selectedMultiPanoramas != null && !ModConfig.selectedMultiPanoramas.isEmpty()) {
            LOGGER.info("CusPano: Multi-mode active! Create a new panorama for launch...");
            
            int idx = new Random().nextInt(ModConfig.selectedMultiPanoramas.size());
            String randomFolder = ModConfig.selectedMultiPanoramas.get(idx);
            currentLoadedPanorama = randomFolder;
            
            PanoramaPackGenerator.applyFilesSilently(randomFolder);
        }

        // --- Disconnect Hook ---
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            if (ModConfig.multiModeActive && ModConfig.changeOnDisconnect) {
                if (ModConfig.selectedMultiPanoramas != null && !ModConfig.selectedMultiPanoramas.isEmpty()) {
                    
                    List<String> pool = new ArrayList<>(ModConfig.selectedMultiPanoramas);

                    // Wenn mehr als 1 Panorama in der Liste ist: Vorheriges ausschließen
                    if (pool.size() > 1 && pool.contains(currentLoadedPanorama)) {
                        pool.remove(currentLoadedPanorama);
                    }

                    int idx = new Random().nextInt(pool.size());
                    String nextPanorama = pool.get(idx);
                    currentLoadedPanorama = nextPanorama;

                    // Ausführung sicher in den Render-Thread einreihen
                    client.execute(() -> {
                        PanoramaPackGenerator.applyAndReload(nextPanorama);
                    });
                }
            }
        });

        LOGGER.info("Custom Panorama Mod loaded successfully!");
    }
}