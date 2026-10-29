package com.trolmastercard.sexmod;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Development-only scene check, enabled with -Dsexmod.autotest=character:action (e.g. jenny:blowjob).
 * Spawns the character in front of the first player to join and starts the action, so the client
 * side can capture screenshots. Does nothing in normal play.
 */
public final class DevAutoTest {
    public static final @Nullable String SPEC = System.getProperty("sexmod.autotest");
    private static @Nullable ServerPlayer player;
    private static int delay = -1;

    private DevAutoTest() {}

    public static void register() {
        if (SPEC == null || SPEC.isBlank()) return;
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            player = handler.getPlayer();
            delay = 60;
        });
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (player == null || --delay != 0) return;
            String[] parts = SPEC.split(":");
            GirlType type = GirlType.valueOf(parts[0].toUpperCase());
            ServerLevel level = (ServerLevel)player.level();
            GirlEntity girl = JennyMod.ENTITY_TYPES.get(type).create(level, EntitySpawnReason.COMMAND);
            if (girl == null) return;
            Vec3 look = player.getLookAngle().multiply(1.0, 0.0, 1.0).normalize();
            Vec3 at = player.position().add(look.scale(2.5));
            girl.snapTo(at.x, player.getY(), at.z, player.getYRot() + 180.0F, 0.0F);
            girl.setYHeadRot(player.getYRot() + 180.0F);
            girl.setYBodyRot(player.getYRot() + 180.0F);
            level.addFreshEntity(girl);
            if (parts.length > 1) girl.performAction(player, parts[1]);
            JennyMod.LOGGER.info("[autotest] spawned {} and started '{}'", type, parts.length > 1 ? parts[1] : "-");
        });
    }
}
