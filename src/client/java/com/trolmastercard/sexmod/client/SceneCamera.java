package com.trolmastercard.sexmod.client;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * First-person scene camera: the original mod put the camera at the partner rig's "boyCam" bone, so the
 * player sees the scene through the eyes of their own (animated) body. Positions come from the last
 * rendered frame of the character model.
 */
public final class SceneCamera {
    private static final long STALE_AFTER_MS = 250;
    private static @Nullable Vec3 eye;
    private static @Nullable Vec3 head;
    private static @Nullable Vec3 body;
    private static long updatedAt;
    private static boolean aimPending;
    private static long aimAfter;

    private SceneCamera() {}

    static void update(Vec3 eyePos) {
        eye = eyePos;
        updatedAt = System.currentTimeMillis();
        if (aimPending && head != null && updatedAt >= aimAfter) {
            aimPending = false;
            // Between her head and torso keeps both oral and penetrative poses in frame.
            Vec3 target = body == null ? head : head.add(body).scale(0.5);
            Vec3 d = target.subtract(eyePos);
            var player = Minecraft.getInstance().player;
            if (player != null) {
                player.setYRot((float)(Mth.atan2(d.z, d.x) * Mth.RAD_TO_DEG) - 90.0F);
                player.setXRot((float)(-(Mth.atan2(d.y, Math.sqrt(d.x * d.x + d.z * d.z)) * Mth.RAD_TO_DEG)));
            }
        }
    }

    static void updateHead(Vec3 pos) {
        head = pos;
    }

    static void updateBody(Vec3 pos) {
        body = pos;
    }

    /** Called when a scene starts or moves to its next clip: aim at the character once, then look freely. */
    static void aim() {
        aimPending = true;
        // Let the controller's blend into the new clip finish before aiming.
        aimAfter = System.currentTimeMillis() + 300;
    }

    /** Camera position for this frame, or null to keep the vanilla camera. */
    public static @Nullable Vec3 eye() {
        if (eye == null || System.currentTimeMillis() - updatedAt > STALE_AFTER_MS) return null;
        if (!JennyModClient.localPlayerInScene()) return null;
        return Minecraft.getInstance().options.getCameraType().isFirstPerson() ? eye : null;
    }
}
