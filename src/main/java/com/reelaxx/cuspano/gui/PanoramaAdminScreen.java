package com.reelaxx.cuspano.gui;

import com.reelaxx.cuspano.config.ModConfig;
import com.reelaxx.cuspano.util.PanoramaManager;
import com.reelaxx.cuspano.util.PanoramaPackGenerator;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import dev.isxander.yacl3.api.controller.CyclingListControllerBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import java.awt.Desktop;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class PanoramaAdminScreen {

    // Eigene, versionsunabhängige Methode zum Öffnen von Ordnern und Links
    private static void openTarget(String target, boolean isFile) {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("win")) {
                if (isFile) {
                    new ProcessBuilder("explorer.exe", target).start();
                } else {
                    new ProcessBuilder("rundll32", "url.dll,FileProtocolHandler", target).start();
                }
            } else if (Desktop.isDesktopSupported()) {
                if (isFile) {
                    Desktop.getDesktop().open(new File(target));
                } else {
                    Desktop.getDesktop().browse(URI.create(target));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Screen create(Screen parent) {
        List<String> rawPanoramas = PanoramaManager.getAvailablePanoramas();
        List<String> available = new ArrayList<>();
        if (rawPanoramas != null) {
            available.addAll(rawPanoramas);
        }
        String availableText = available.isEmpty() ? "None found" : String.join(", ", available);

        if (available.isEmpty()) {
            available.add("default");
        }
        if (ModConfig.selectedSinglePanorama == null || !available.contains(ModConfig.selectedSinglePanorama)) {
            ModConfig.selectedSinglePanorama = available.get(0);
        }
        
        if (ModConfig.selectedMultiPanoramas == null) {
            ModConfig.selectedMultiPanoramas = new ArrayList<>();
        }

        ConfigCategory.Builder settingsCategory = ConfigCategory.createBuilder()
            .name(Component.literal("Settings"));

        // --- AREA 1: GENERAL ---
        settingsCategory.group(OptionGroup.createBuilder()
            .name(Component.literal("Mode selection"))
            .option(Option.<Boolean>createBuilder()
                .name(Component.literal("Current mode:"))
                .description(OptionDescription.of(Component.literal("Switch between Single (Fixed) and Multi (Rotating).\n\nNote: Both settings areas below remain visible so you can configure them at any time.")))
                .binding(
                    false,
                    () -> ModConfig.multiModeActive, 
                    newVal -> ModConfig.multiModeActive = newVal
                )
                .controller(opt -> CyclingListControllerBuilder.create(opt)
                    .values(Arrays.asList(false, true))
                    .formatValue(val -> Component.literal(val ? "MULTI (Rotating)" : "SINGLE (Fixed)").withStyle(ChatFormatting.GOLD))
                )
                .build())
            .build());
        
        // --- AREA 2: SINGLE MODE ---
        settingsCategory.group(OptionGroup.createBuilder()
            .name(Component.literal("Single Panorama Settings"))
            .option(Option.<String>createBuilder()
                .name(Component.literal("Currently selected:"))
                .description(OptionDescription.of(Component.literal("Click here to cycle through the available panoramas.\n\n(This setting is only active if MULTI Mode is OFF)")))
                .binding(
                    "default",
                    () -> ModConfig.selectedSinglePanorama,
                    newVal -> ModConfig.selectedSinglePanorama = newVal
                )
                .controller(opt -> CyclingListControllerBuilder.create(opt)
                    .values(available)
                    .formatValue(val -> Component.literal(val))
                )
                .build())
            .build());

        // --- AREA 3: MULTI MODE ---
        settingsCategory.group(ListOption.<String>createBuilder()
            .name(Component.literal("Rotating Panoramas:"))
            .description(OptionDescription.of(
                Component.literal("Enter here all folder names that should rotate.\n(This setting is only active if MULTI Mode is ON)\n\nHow to do:\nClick on '+', then type the name of the folder (example: 'file_name') and confirm the entry with [Enter].\n\n")
                    .append(Component.literal("Available Panoramas:\n").withStyle(ChatFormatting.GRAY))
                    .append(Component.literal(availableText).withStyle(ChatFormatting.AQUA))
            ))
            .binding(new ArrayList<>(), () -> ModConfig.selectedMultiPanoramas, val -> ModConfig.selectedMultiPanoramas = val)
            .controller(StringControllerBuilder::create)
            .initial("")
            .build());

        // --- Area 4: FOLDER MANAGEMENT ---
        settingsCategory.group(OptionGroup.createBuilder()
            .name(Component.literal("Folder Management"))
            .option(ButtonOption.createBuilder()
                .name(Component.literal("Panoramas Folder"))
                .description(OptionDescription.of(Component.literal("Opens the folder where you can drop your custom panorama folders.")))
                .text(Component.literal("Open Folder").withStyle(ChatFormatting.DARK_GREEN))
                .action((screen, opt) -> openTarget(PanoramaManager.PANORAMA_PATH.toFile().getAbsolutePath(), true))
                .build())
            .option(ButtonOption.createBuilder()
                .name(Component.literal("Refresh List"))
                .description(OptionDescription.of(Component.literal("No new panoramas after adding them? Click here to refresh the menu so they show up.")))
                .text(Component.literal("Refresh").withStyle(ChatFormatting.YELLOW))
                .action((screen, opt) -> {
                    net.minecraft.client.Minecraft.getInstance().gui.setScreen(PanoramaAdminScreen.create(parent));
                })
                .build())
            .option(ButtonOption.createBuilder()
                .name(Component.literal("Download Examples"))
                .description(OptionDescription.of(Component.literal("Download a pack with example panoramas to see how the folder and files must be structured.\n\nNote:\nThis button contains an external link.\nBy clicking on the button you will direct to a zip file on google drive.")))
                .text(Component.literal("Download").withStyle(ChatFormatting.DARK_AQUA))
                .action((screen, opt) -> {
                    openTarget("https://drive.google.com/file/d/18vwBOMzeuQzzdOYYjDQIyTerxqulFxYF/view?usp=sharing", false);
                })
                .build())
            .build());
        
        // --- BEREICH 5: UI / UX SETTINGS ---
        settingsCategory.group(OptionGroup.createBuilder()
            .name(Component.literal("UI / UX Settings"))
            
            .option(Option.<Boolean>createBuilder()
                .name(Component.literal("Show Main Menu Button"))
                .description(OptionDescription.of(Component.literal("Toggle (on/off) the visibility of the CusPano button on the main menu.")))
                .binding(
                    true,
                    () -> ModConfig.showMainMenuButton,
                    newVal -> ModConfig.showMainMenuButton = newVal
                )
                .controller(TickBoxControllerBuilder::create)
                .build())
                
            .option(Option.<Boolean>createBuilder()
                .name(Component.literal("Change on Disconnect"))
                .description(OptionDescription.of(Component.literal("Automatically switch to a new random panorama when you leave a world or server.\n\nNote: This only works if MULTI Mode is enabled!")))
                .binding(
                    false,
                    () -> ModConfig.changeOnDisconnect,
                    newVal -> ModConfig.changeOnDisconnect = newVal
                )
                .controller(TickBoxControllerBuilder::create)
                .build())
                
            .build());

        // --- BEREICH 6: CREDITS ---
        settingsCategory.group(OptionGroup.createBuilder()
            .name(Component.literal("Credits"))
            .option(ButtonOption.createBuilder()
                .name(Component.literal("Powered by:"))
                .description(OptionDescription.of(Component.literal("ReeLaxx feat. MaReeCRAFT\n\nNote:\nThis button contains an external link.\nBy clicking on the button you will direct to our Instagram page of (MaReeCRAFT).")))
                .text(Component.literal("ReeLaxx feat. MaReeCRAFT").withStyle(ChatFormatting.LIGHT_PURPLE))
                .action((screen, opt) -> {
                    openTarget("https://www.instagram.com/mareecraft/", false);
                })
                .build())
            .build());
        
        // --- DER GLOBALE YACL BUILDER ---
        return YetAnotherConfigLib.createBuilder()
            .title(Component.literal("Panorama Setup"))
            .save(() -> {
                ModConfig.save();
                
                String folderToApply = "default";
                if (!ModConfig.multiModeActive) {
                    folderToApply = ModConfig.selectedSinglePanorama;
                } else if (ModConfig.selectedMultiPanoramas != null && !ModConfig.selectedMultiPanoramas.isEmpty()) {
                    int idx = new Random().nextInt(ModConfig.selectedMultiPanoramas.size());
                    folderToApply = ModConfig.selectedMultiPanoramas.get(idx);
                }
                
                PanoramaPackGenerator.applyAndReload(folderToApply);
            })
            .category(settingsCategory.build())
            .build()
            .generateScreen(parent);
    }
}