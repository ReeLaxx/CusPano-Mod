package com.reelaxx.cuspano.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.text.Text;

import com.reelaxx.cuspano.config.ModConfig;
import com.reelaxx.cuspano.gui.PanoramaAdminScreen;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;

@Mixin(TitleScreen.class)
public abstract class TitleScreenButtonMixin extends Screen {

    protected TitleScreenButtonMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void addAdminButton(CallbackInfo ci) {
        if (ModConfig.showMainMenuButton) {
            this.addDrawableChild(ButtonWidget.builder(Text.literal("CusPano"), (button) -> {
                this.client.setScreen(PanoramaAdminScreen.create(this));
            }).dimensions(10, 10, 60, 20).build());
        }
    }
}
