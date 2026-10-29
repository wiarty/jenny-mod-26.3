package com.trolmastercard.sexmod.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.trolmastercard.sexmod.AdvancePayload;
import com.trolmastercard.sexmod.GirlEntity;
import com.trolmastercard.sexmod.GirlType;
import com.trolmastercard.sexmod.JennyMod;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.CameraType;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import org.jspecify.annotations.Nullable;

public class JennyModClient implements ClientModInitializer {
    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(JennyMod.id("main"));
    private static final KeyMapping ADVANCE = KeyMappingHelper.registerKeyMapping(
            new KeyMapping("key.sexmod.advance", InputConstants.Type.KEYBOARD, InputConstants.KEY_G, CATEGORY));

    /** Camera mode to restore once the player leaves a scene; null while not in one. */
    private static @Nullable CameraType cameraBeforeScene;
    private static int lastClipSeq = -1;

    @Override
    public void onInitializeClient() {
        for (GirlType type : GirlType.values()) {
            EntityRenderers.register(JennyMod.ENTITY_TYPES.get(type), context -> new GirlRenderer<>(context, type));
        }
        GirlEntity.openMenu = girl -> Minecraft.getInstance().setScreenAndShow(new ActionScreen(girl));
        GirlEntity.playCue = (girl, cue) -> CueSounds.play(girl, girl.girlType().id, cue);

        ClientTickEvents.END_CLIENT_TICK.register(JennyModClient::tick);
        DevAutoTestClient.register();
        ScreenEvents.AFTER_INIT.register((client, screen, width, height) -> {
            if (screen instanceof TitleScreen && !AgeGateScreen.isConfirmed()) client.setScreenAndShow(new AgeGateScreen(screen));
        });
    }

    private static void tick(Minecraft client) {
        boolean inScene = client.player != null && client.player.getVehicle() instanceof GirlEntity;
        if (inScene && cameraBeforeScene == null) {
            // The partner is drawn by the character's rig. First person looks out of the rig's eyes
            // (SceneCamera); third person (F5) watches from beside her, where the hidden player is parked.
            cameraBeforeScene = client.options.getCameraType();
            client.options.setCameraType(CameraType.FIRST_PERSON);
            GirlEntity girl = (GirlEntity)client.player.getVehicle();
            client.player.lookAt(EntityAnchorArgument.Anchor.EYES, girl.position().add(0.0, girl.getBbHeight() * 0.55, 0.0));
            SceneCamera.aim();
        } else if (!inScene && cameraBeforeScene != null) {
            client.options.setCameraType(cameraBeforeScene);
            cameraBeforeScene = null;
        }
        if (inScene && client.player.getVehicle() instanceof GirlEntity girl && girl.getAnimationSeq() != lastClipSeq) {
            lastClipSeq = girl.getAnimationSeq();
            SceneCamera.aim();
        }
        while (ADVANCE.consumeClick()) {
            if (inScene) ClientPlayNetworking.send(AdvancePayload.INSTANCE);
        }
    }

    /** True while the local player is a scene partner (used to hide the first-person hand). */
    public static boolean localPlayerInScene() {
        Minecraft client = Minecraft.getInstance();
        return client.player != null && client.player.getVehicle() instanceof GirlEntity;
    }

    /** Players in a scene are hidden; the character model draws them with their skin. */
    public static boolean hidesPlayer(net.minecraft.world.entity.Entity entity) {
        return entity instanceof net.minecraft.world.entity.player.Player && entity.getVehicle() instanceof GirlEntity;
    }
}
