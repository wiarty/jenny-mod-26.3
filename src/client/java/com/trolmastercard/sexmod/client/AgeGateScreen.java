package com.trolmastercard.sexmod.client;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** The original mod's adult-content warning, shown on the title screen until confirmed once. */
public class AgeGateScreen extends Screen {
    private static final Path CONFIRMED = FabricLoader.getInstance().getConfigDir().resolve("sexmod-age-confirmed");
    private final Screen parent;
    private MultiLineLabel text = MultiLineLabel.EMPTY;

    public AgeGateScreen(Screen parent) {
        super(Component.translatable("gui.sexmod.age.title"));
        this.parent = parent;
    }

    public static boolean isConfirmed() {
        return Files.exists(CONFIRMED);
    }

    @Override
    protected void init() {
        this.text = MultiLineLabel.create(this.font, Component.translatable("gui.sexmod.age.text"), this.width - 80);
        int y = this.height / 2 + 30;
        this.addRenderableWidget(Button.builder(Component.translatable("gui.sexmod.age.yes"), button -> {
            try {
                Files.writeString(CONFIRMED, "confirmed\n");
            } catch (IOException ignored) {
                // Without the marker file the warning simply shows again next launch.
            }
            this.minecraft.setScreenAndShow(this.parent);
        }).bounds(this.width / 2 - 155, y, 150, 20).build());
        this.addRenderableWidget(Button.builder(Component.translatable("gui.sexmod.age.no"), button -> this.minecraft.stop())
                .bounds(this.width / 2 + 5, y, 150, 20).build());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        graphics.centeredText(this.font, this.title, this.width / 2, this.height / 2 - 50, 0xFFFF5555);
        this.text.visitLines(net.minecraft.client.gui.TextAlignment.CENTER, this.width / 2, this.height / 2 - 30, 10, graphics.textRenderer());
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
}
