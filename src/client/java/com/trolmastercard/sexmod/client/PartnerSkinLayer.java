package com.trolmastercard.sexmod.client;

import com.geckolib.cache.model.GeoBone;
import com.geckolib.cache.model.cuboid.CuboidGeoBone;
import com.geckolib.cache.model.cuboid.GeoCube;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.GeoRenderer;
import com.geckolib.renderer.base.PerBoneRender;
import com.geckolib.renderer.base.RenderPassInfo;
import com.geckolib.renderer.layer.GeoRenderLayer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.trolmastercard.sexmod.GirlEntity;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;

/**
 * Draws the character model's built-in player rig ("steve" and its children) with the scene
 * partner's own skin. The rig's UVs follow the 64x64 player skin layout, so the skin maps 1:1.
 */
public class PartnerSkinLayer<R extends GeoRenderState> extends GeoRenderLayer<GirlEntity, Void, R> {
    public static final DataTicket<Identifier> SKIN = DataTicket.create("sexmod_partner_skin", Identifier.class);
    public static final DataTicket<Boolean> SLIM = DataTicket.create("sexmod_partner_slim", Boolean.class);
    /** Set when the partner is the local player watching in first person: their own head stays hidden. */
    public static final DataTicket<Boolean> FIRST_PERSON = DataTicket.create("sexmod_partner_first_person", Boolean.class);
    private static final String RIG_ROOT = "steve";
    /** Genital bones: the original painted their colors over a copy of the skin, see partner_parts.png. */
    private static final java.util.Set<String> PARTS_ROOTS = java.util.Set.of("cock", "ballL", "ballR");
    private static final Identifier PARTS_TEXTURE = com.trolmastercard.sexmod.JennyMod.id("textures/entity/partner_parts.png");
    private static final String HEAD = "Head2";

    private static final boolean DEBUG_DISABLED = Boolean.getBoolean("sexmod.debug.nopartner");

    public PartnerSkinLayer(GeoRenderer<GirlEntity, Void, R> renderer) {
        super(renderer);
    }

    @Override
    public void preRender(RenderPassInfo<R> renderPassInfo, SubmitNodeCollector renderTasks) {
        if (DEBUG_DISABLED || !renderPassInfo.willRender() || !renderPassInfo.renderState().hasGeckolibData(SKIN)) return;
        // Keep the rig out of the main (character texture) pass; renderBone draws it again below.
        renderPassInfo.addBoneUpdater((passInfo, snapshots) -> forEachRigBone(passInfo,
                bone -> snapshots.get(bone).skipRender(true)));
    }

    @Override
    public void addPerBoneRender(RenderPassInfo<R> renderPassInfo, BiConsumer<GeoBone, PerBoneRender<R>> consumer) {
        if (DEBUG_DISABLED || !renderPassInfo.willRender() || !renderPassInfo.renderState().hasGeckolibData(SKIN)) return;
        boolean slim = renderPassInfo.renderState().getOrDefaultGeckolibData(SLIM, false);
        boolean firstPerson = renderPassInfo.renderState().getOrDefaultGeckolibData(FIRST_PERSON, false);
        forEachRigBone(renderPassInfo, bone -> {
            if (!(bone instanceof CuboidGeoBone) || !isArmVariant(bone.name(), slim)) return;
            if (firstPerson && HEAD.equals(bone.name())) return;
            boolean part = isGenital(bone);
            consumer.accept(bone, (passInfo, posed, tasks) -> this.renderBone(passInfo, posed, tasks, part));
        });
    }

    /** The rig carries both arm shapes: "...Alex" for slim skins and "...Steve" for wide ones. */
    private static boolean isArmVariant(String name, boolean slim) {
        if (name.endsWith("Alex")) return slim;
        if (name.endsWith("Steve")) return !slim;
        return true;
    }

    private static boolean isGenital(GeoBone bone) {
        for (GeoBone b = bone; b != null; b = b.parent()) {
            if (PARTS_ROOTS.contains(b.name())) return true;
        }
        return false;
    }

    private static void forEachRigBone(RenderPassInfo<?> renderPassInfo, Consumer<GeoBone> action) {
        renderPassInfo.model().getBone(RIG_ROOT).ifPresent(root -> visit(root, action));
    }

    private static void visit(GeoBone bone, Consumer<GeoBone> action) {
        action.accept(bone);
        for (GeoBone child : bone.children()) visit(child, action);
    }

    private void renderBone(RenderPassInfo<R> renderPassInfo, GeoBone bone, SubmitNodeCollector renderTasks, boolean part) {
        Identifier skin = part ? PARTS_TEXTURE : renderPassInfo.renderState().getGeckolibData(SKIN);
        if (skin == null) return;
        RenderType renderType = this.renderer.getRenderType(renderPassInfo.renderState(), skin);
        if (renderType == null) return;
        int packedLight = renderPassInfo.packedLight();
        int packedOverlay = renderPassInfo.packedOverlay();
        int renderColor = renderPassInfo.renderColor();

        renderTasks.submitCustomGeometry(renderPassInfo.poseStack(), renderType, (pose, buffer) -> {
            PoseStack poseStack = renderPassInfo.poseStack();
            poseStack.pushPose();
            poseStack.last().set(pose);
            bone.translateAwayFromPivotPoint(poseStack);
            for (GeoCube cube : ((CuboidGeoBone)bone).cubes) {
                poseStack.pushPose();
                cube.render(poseStack, buffer, packedLight, packedOverlay, renderColor);
                poseStack.popPose();
            }
            poseStack.popPose();
        });
    }
}
