package com.reelaxx.cuspano.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.network.chat.Component;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.GenericMessageScreen;
import net.minecraft.client.gui.screens.ProgressScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.components.Renderable;

import com.reelaxx.cuspano.CusPano;
import com.reelaxx.cuspano.config.ModConfig;
import com.reelaxx.cuspano.gui.PanoramaAdminScreen;
import com.reelaxx.cuspano.util.PanoramaPackGenerator;

@Mixin(Screen.class)
public abstract class TitleScreenButtonMixin {

    @Shadow
    protected Minecraft minecraft;

    @Shadow
    protected abstract <T extends GuiEventListener & Renderable & NarratableEntry> T addRenderableWidget(T widget);

    // Greift in JEDEM Menü nach dem Verlassen (Serverliste, DisconnectScreen & Hauptmenü)
    @Inject(method = "init", at = @At("HEAD"))
    private void onAnyMenuScreenInit(CallbackInfo ci) {
        Screen self = (Screen) (Object) this;

        // Zwischen-Ladebildschirme beim Speichern/Trennen überspringen
        if (self instanceof GenericMessageScreen || self instanceof ProgressScreen) {
            return;
        }

        // Sobald keine Welt mehr geladen ist und ein neues Panorama bereitliegt -> sofort neu laden!
        if (CusPano.needsPanoramaReload && this.minecraft != null && this.minecraft.level == null) {
            CusPano.needsPanoramaReload = false;
            PanoramaPackGenerator.reloadOnly();
        }
    }

    // Zeichnet den CusPano-Button weiterhin nur im echten Hauptmenü (TitleScreen)
    @Inject(method = "init", at = @At("RETURN"))
    private void addAdminButton(CallbackInfo ci) {
        Screen self = (Screen) (Object) this;
        if (self instanceof TitleScreen && ModConfig.showMainMenuButton) {
            this.addRenderableWidget(Button.builder(Component.literal("CusPano"), (button) -> {
                this.minecraft.gui.setScreen(PanoramaAdminScreen.create(self));
            }).bounds(10, 10, 60, 20).build());
        }
    }
}