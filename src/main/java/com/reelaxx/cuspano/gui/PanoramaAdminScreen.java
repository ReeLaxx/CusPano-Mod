package com.reelaxx.cuspano.gui;

import com.reelaxx.cuspano.config.ModConfig;
import com.reelaxx.cuspano.util.PanoramaManager;
import com.reelaxx.cuspano.util.PanoramaPackGenerator;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import dev.isxander.yacl3.api.controller.CyclingListControllerBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class PanoramaAdminScreen {

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
            .name(Text.literal("Settings"));

        // --- AREA 1: GENERAL ---
        settingsCategory.group(OptionGroup.createBuilder()
            .name(Text.literal("Mode selection"))
            .option(Option.<Boolean>createBuilder()
                .name(Text.literal("Current mode:"))
                .description(OptionDescription.of(Text.literal("Switch between Single (Fixed) and Multi (Rotating).\n\nNote: Both settings areas below remain visible so you can configure them at any time.")))
                .binding(
                    false,
                    () -> ModConfig.multiModeActive, 
                    newVal -> ModConfig.multiModeActive = newVal
                )
                .controller(opt -> CyclingListControllerBuilder.create(opt)
                    .values(Arrays.asList(false, true))
                    .formatValue(val -> Text.literal(val ? "MULTI (Rotating)" : "SINGLE (Fixed)").formatted(Formatting.GOLD))
                )
                .build())
            .build());
        
        // --- AREA 2: SINGLE MODE ---
        settingsCategory.group(OptionGroup.createBuilder()
            .name(Text.literal("Single Panorama Settings"))
            .option(Option.<String>createBuilder()
                .name(Text.literal("Currently selected:"))
                .description(OptionDescription.of(Text.literal("Click here to cycle through the available panoramas.\n\n(This setting is only active if MULTI Mode is OFF)")))
                .binding(
                    "default",
                    () -> ModConfig.selectedSinglePanorama,
                    newVal -> ModConfig.selectedSinglePanorama = newVal
                )
                .controller(opt -> CyclingListControllerBuilder.create(opt)
                    .values(available)
                    .formatValue(val -> Text.literal(val))
                )
                .build())
            .build());

        // --- AREA 3: MULTI MODE ---
        settingsCategory.group(ListOption.<String>createBuilder()
            .name(Text.literal("Rotating Panoramas:"))
            .description(OptionDescription.of(
                Text.literal("Enter here all folder names that should rotate.\n(This setting is only active if MULTI Mode is ON)\n\nHow to do:\nClick on '+'', then type the name of the folder (example: 'file_name') and confirm the entry with [Enter].\n\n")
                    .append(Text.literal("Available Panoramas:\n").formatted(Formatting.GRAY))
                    .append(Text.literal(availableText).formatted(Formatting.AQUA))
            ))
            .binding(new ArrayList<>(), () -> ModConfig.selectedMultiPanoramas, val -> ModConfig.selectedMultiPanoramas = val)
            .controller(StringControllerBuilder::create)
            .initial("")
            .build());

        // --- Area 4: FOLDER MANAGEMENT ---
        settingsCategory.group(OptionGroup.createBuilder()
            .name(Text.literal("Folder Management"))

            .option(ButtonOption.createBuilder()
                .name(Text.literal("Panoramas Folder"))
                .description(OptionDescription.of(Text.literal("Opens the folder where you can drop your custom panorama folders.")))
                .text(Text.literal("Open Folder").formatted(Formatting.DARK_GREEN))
                .action((screen, opt) -> Util.getOperatingSystem().open(PanoramaManager.PANORAMA_PATH.toFile()))
                .build())

            .option(ButtonOption.createBuilder()
                .name(Text.literal("Refresh List"))
                .description(OptionDescription.of(Text.literal("No new panoramas after adding them? Click here to refresh the menu so they show up.")))
                .text(Text.literal("Refresh").formatted(Formatting.YELLOW))
                .action((screen, opt) -> {
                    net.minecraft.client.MinecraftClient.getInstance().setScreen(PanoramaAdminScreen.create(parent));
                })
                .build())

            .option(ButtonOption.createBuilder()
                .name(Text.literal("Download Examples"))
                .description(OptionDescription.of(Text.literal("Download a pack with example panoramas to see how the folder and files must be structured.\n\nNote:\nThis button contains an external link.\nBy clicking on the button you will direct to a zip file on google drive.")))
                .text(Text.literal("Download").formatted(Formatting.DARK_AQUA))
                .action((screen, opt) -> {
                    Util.getOperatingSystem().open("https://drive.google.com/file/d/18vwBOMzeuQzzdOYYjDQIyTerxqulFxYF/view?usp=sharing");
                })
                .build())
            .build());
        
        // --- BEREICH 5: UI / UX SETTINGS ---
        settingsCategory.group(OptionGroup.createBuilder()
            .name(Text.literal("UI / UX Settings"))
            
            .option(Option.<Boolean>createBuilder()
                .name(Text.literal("Show Main Menu Button"))
                .description(OptionDescription.of(Text.literal("Toggle (on/off) the visibility of the CusPano button on the main menu.")))
                .binding(
                    true,
                    () -> ModConfig.showMainMenuButton,
                    newVal -> ModConfig.showMainMenuButton = newVal
                )
                .controller(TickBoxControllerBuilder::create)
                .build())
                
            .option(Option.<Boolean>createBuilder()
                .name(Text.literal("Change on Disconnect"))
                .description(OptionDescription.of(Text.literal("Automatically switch to a new random panorama when you leave a world or server.\n\nNote: This only works if MULTI Mode is enabled!")))
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
            .name(Text.literal("Credits"))
            .option(ButtonOption.createBuilder()
                .name(Text.literal("Powered by:"))
                .description(OptionDescription.of(Text.literal("ReeLaxx feat. MaReeCRAFT\n\nNote:\nThis button contains an external link.\nBy clicking on the button you will direct to our Instagram page of (MaReeCRAFT).")))
                .text(Text.literal("ReeLaxx feat. MaReeCRAFT").formatted(Formatting.LIGHT_PURPLE))
                .action((screen, opt) -> {
                    Util.getOperatingSystem().open("https://www.instagram.com/mareecraft/");
                })
                .build())
            .build());
        
        // --- DER GLOBALE YACL BUILDER ---
        return YetAnotherConfigLib.createBuilder()
            .title(Text.literal("Panorama Setup"))
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