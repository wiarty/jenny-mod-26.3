package com.trolmastercard.sexmod.client;

import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.BoneSnapshots;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;
import com.trolmastercard.sexmod.GirlEntity;
import com.trolmastercard.sexmod.GirlType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.player.PlayerModelType;
import org.jspecify.annotations.Nullable;

public class GirlRenderer<R extends LivingEntityRenderState & GeoRenderState> extends GeoEntityRenderer<GirlEntity, R> {
    /** Prop rigs that the original mod only showed during specific actions. */
    private static final String[] HIDDEN_BONES = {"items", "coin", "bedSlimeLayer"};

    public GirlRenderer(EntityRendererProvider.Context context, GirlType type) {
        super(context, new GirlGeoModel(type));
        this.withRenderLayer(PartnerSkinLayer::new);
    }

    @Override
    public void addRenderData(GirlEntity girl, @Nullable Void relatedObject, R renderState, float partialTick) {
        renderState.addGeckolibData(GirlGeoModel.TYPE, girl.girlType());
        renderState.addGeckolibData(GirlGeoModel.DRESSED, girl.isDressed());
        if (girl.getFirstPassenger() instanceof AbstractClientPlayer partner) {
            renderState.addGeckolibData(PartnerSkinLayer.SKIN, partner.getSkin().body().texturePath());
            renderState.addGeckolibData(PartnerSkinLayer.SLIM, partner.getSkin().model() == PlayerModelType.SLIM);
            Minecraft client = Minecraft.getInstance();
            if (partner == client.player && client.options.getCameraType().isFirstPerson()) {
                renderState.addGeckolibData(PartnerSkinLayer.FIRST_PERSON, true);
            }
        }
    }

    @Override
    public void preRenderPass(RenderPassInfo<R> renderPassInfo, SubmitNodeCollector renderTasks) {
        super.preRenderPass(renderPassInfo, renderTasks);
        // First-person scenes look out of the partner rig's eyes, like the original's "boyCam".
        if (renderPassInfo.renderState().getOrDefaultGeckolibData(PartnerSkinLayer.FIRST_PERSON, false)) {
            renderPassInfo.addBonePositionListener("boyCam", (worldPos, modelPos, localPos) -> {
                if (worldPos != null) SceneCamera.update(worldPos);
            });
            renderPassInfo.addBonePositionListener("girlCam", (worldPos, modelPos, localPos) -> {
                if (worldPos != null) SceneCamera.updateHead(worldPos);
            });
            renderPassInfo.addBonePositionListener("torso", (worldPos, modelPos, localPos) -> {
                if (worldPos != null) SceneCamera.updateBody(worldPos);
            });
        }
    }

    @Override
    public void adjustModelBonesForRender(RenderPassInfo<R> renderPassInfo, BoneSnapshots snapshots) {
        for (String bone : HIDDEN_BONES) {
            snapshots.ifPresent(bone, snapshot -> snapshot.skipRender(true).skipChildrenRender(true));
        }
        // Armor overlays: the original only drew these when the character wore armor.
        for (String bone : renderPassInfo.model().boneLookup().get().keySet()) {
            if (bone.startsWith("armor")) snapshots.ifPresent(bone, snapshot -> snapshot.skipRender(true).skipChildrenRender(true));
        }
        // Without a partner the built-in player rig stays hidden; with one, PartnerSkinLayer draws it.
        if (!renderPassInfo.renderState().hasGeckolibData(PartnerSkinLayer.SKIN)) {
            snapshots.ifPresent("steve", snapshot -> snapshot.skipRender(true).skipChildrenRender(true));
        }
    }
}
