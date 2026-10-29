package com.trolmastercard.sexmod.client.mixin;

import com.trolmastercard.sexmod.client.JennyModClient;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    /** During a scene the player is only a camera, so no first-person hand or held item. */
    @Inject(method = "renderItemInHand", at = @At("HEAD"), cancellable = true)
    private void sexmod$hideHandInScene(CallbackInfo ci) {
        if (JennyModClient.localPlayerInScene()) ci.cancel();
    }
}
