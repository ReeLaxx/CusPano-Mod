package com.reelaxx.cuspano;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.reelaxx.cuspano.config.ModConfig;
import com.reelaxx.cuspano.util.PanoramaPackGenerator;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

import java.util.Random;

public class CusPano implements ModInitializer {
    public static final String MOD_ID = "cuspano";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ModConfig.load();

        if (ModConfig.multiModeActive && ModConfig.selectedMultiPanoramas != null && !ModConfig.selectedMultiPanoramas.isEmpty()) {
            LOGGER.info("CusPano: Multi-mode active! Create a new panorama for this launch...");
            
            int idx = new Random().nextInt(ModConfig.selectedMultiPanoramas.size());
            String randomFolder = ModConfig.selectedMultiPanoramas.get(idx);
            
            PanoramaPackGenerator.applyFilesSilently(randomFolder);
        }

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            if (ModConfig.multiModeActive && ModConfig.changeOnDisconnect) {
                if (ModConfig.selectedMultiPanoramas != null && !ModConfig.selectedMultiPanoramas.isEmpty()) {
                    
                    int idx = new Random().nextInt(ModConfig.selectedMultiPanoramas.size());
                    String nextPanorama = ModConfig.selectedMultiPanoramas.get(idx);
                    
                    client.execute(() -> {
                        PanoramaPackGenerator.applyAndReload(nextPanorama);
                    });
                }
            }
        });

        LOGGER.info("Custom Panorama Mod loaded successfully!");
    }
}