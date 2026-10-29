package com.trolmastercard.sexmod.client;

import com.trolmastercard.sexmod.AdvancePayload;
import com.trolmastercard.sexmod.DevAutoTest;
import com.trolmastercard.sexmod.JennyMod;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;

/**
 * Client half of {@link DevAutoTest}: takes a screenshot every couple of seconds while the scene runs,
 * presses "next stage" now and then, and quits when done. Only active with -Dsexmod.autotest.
 */
public final class DevAutoTestClient {
    private static final int FIRST_SHOT = 140;
    private static final int INTERVAL = 50;
    private static final int SHOTS = Integer.getInteger("sexmod.autotest.shots", 10);
    private static int ticks;
    private static int shots;

    private DevAutoTestClient() {}

    public static void register() {
        if (DevAutoTest.SPEC == null || DevAutoTest.SPEC.isBlank()) return;
        ClientTickEvents.END_CLIENT_TICK.register(DevAutoTestClient::tick);
    }

    private static void tick(Minecraft client) {
        if (client.player == null || client.level == null) return;
        ticks++;
        client.options.pauseOnLostFocus = false;
        String camera = System.getProperty("sexmod.autotest.camera");
        if (camera != null && JennyModClient.localPlayerInScene()) {
            client.options.setCameraType(net.minecraft.client.CameraType.valueOf(camera));
        }
        if (ticks < FIRST_SHOT || (ticks - FIRST_SHOT) % INTERVAL != 0) return;
        if (shots >= SHOTS) {
            client.stop();
            return;
        }
        Screenshot.grab(client, false);
        if (client.player.getVehicle() instanceof com.trolmastercard.sexmod.GirlEntity girl) {
            JennyMod.LOGGER.info("[autotest] screenshot {} anim='{}' dressed={} girl={} yaw={} player={}", shots, girl.getAnimation(),
                    girl.isDressed(), girl.position(), girl.getYRot(), client.player.position());
        } else {
            JennyMod.LOGGER.info("[autotest] screenshot {} (not in scene)", shots);
        }
        shots++;
        // Step through the scene's looping stages so later shots show later clips.
        if (shots % 3 == 0 && JennyModClient.localPlayerInScene()) ClientPlayNetworking.send(AdvancePayload.INSTANCE);
    }
}
