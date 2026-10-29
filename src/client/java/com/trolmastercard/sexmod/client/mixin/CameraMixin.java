package com.trolmastercard.sexmod.client.mixin;

import com.trolmastercard.sexmod.client.SceneCamera;
import net.minecraft.client.Camera;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {
    @Shadow
    protected abstract void setPosition(Vec3 position);

    /** In a first-person scene, look out of the partner rig's eyes instead of the hidden player's. */
    @Inject(method = "alignWithEntity", at = @At("TAIL"))
    private void sexmod$sceneCamera(float partialTick, CallbackInfo ci) {
        Vec3 eye = SceneCamera.eye();
        if (eye != null) this.setPosition(eye);
    }
}
