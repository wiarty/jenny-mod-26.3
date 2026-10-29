package com.trolmastercard.sexmod.client;

import com.trolmastercard.sexmod.ActionPayload;
import com.trolmastercard.sexmod.GirlActions;
import com.trolmastercard.sexmod.GirlEntity;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/** Right-click menu for a character; the chosen action is validated and paid for on the server. */
public class ActionScreen extends Screen {
    private static final int PAGE_SIZE = 8;
    private record Option(String action, Component label) {}

    private final GirlEntity girl;
    private final List<Option> options = new ArrayList<>();
    private int page;

    public ActionScreen(GirlEntity girl) {
        super(Component.translatable("gui.sexmod.title", girl.getDisplayName()));
        this.girl = girl;
        for (GirlActions.Action action : GirlActions.forType(girl.girlType())) {
            Component name = Component.translatable(action.translationKey());
            this.options.add(new Option(action.id(), action.cost() == null ? name
                    : Component.translatable("gui.sexmod.cost", name, action.count(), new ItemStack(action.cost()).getHoverName())));
        }
        if (girl.girlType().hasOutfit()) {
            this.options.add(girl.isDressed()
                    ? new Option(GirlActions.STRIP, Component.translatable("action.sexmod.strip"))
                    : new Option(GirlActions.DRESS, Component.translatable("action.sexmod.dress")));
        }
    }

    @Override
    protected void init() {
        int x = this.width / 2 - 120;
        int y = 48;
        int start = this.page * PAGE_SIZE;
        for (int i = start; i < Math.min(start + PAGE_SIZE, this.options.size()); i++) {
            Option option = this.options.get(i);
            this.addRenderableWidget(Button.builder(option.label(), button -> this.choose(option)).bounds(x, y, 240, 20).build());
            y += 22;
        }
        int footerY = this.height - 28;
        if (this.page > 0) {
            this.addRenderableWidget(Button.builder(Component.translatable("gui.sexmod.prev"), button -> this.turnPage(-1)).bounds(x, footerY, 75, 20).build());
        }
        if (start + PAGE_SIZE < this.options.size()) {
            this.addRenderableWidget(Button.builder(Component.translatable("gui.sexmod.next"), button -> this.turnPage(1)).bounds(x + 82, footerY, 75, 20).build());
        }
        this.addRenderableWidget(Button.builder(Component.translatable("gui.sexmod.close"), button -> this.onClose()).bounds(x + 165, footerY, 75, 20).build());
    }

    private void turnPage(int delta) {
        this.page += delta;
        this.clearWidgets();
        this.init();
    }

    private void choose(Option option) {
        ClientPlayNetworking.send(new ActionPayload(this.girl.getId(), option.action()));
        this.onClose();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        graphics.centeredText(this.font, this.title, this.width / 2, 20, 0xFFFFFFFF);
        graphics.centeredText(this.font, Component.translatable("gui.sexmod.hint"), this.width / 2, 33, 0xFFD0D0D0);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
