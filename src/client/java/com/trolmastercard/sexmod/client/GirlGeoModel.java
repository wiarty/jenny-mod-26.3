package com.trolmastercard.sexmod.client;

import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import com.trolmastercard.sexmod.GirlEntity;
import com.trolmastercard.sexmod.GirlType;
import net.minecraft.resources.Identifier;

/** Picks the dressed or nude geometry from the render state; textures and animations come from the type. */
public class GirlGeoModel extends GeoModel<GirlEntity> {
    public static final DataTicket<GirlType> TYPE = DataTicket.create("sexmod_type", GirlType.class);
    public static final DataTicket<Boolean> DRESSED = DataTicket.create("sexmod_dressed", Boolean.class);

    private final GirlType type;

    public GirlGeoModel(GirlType type) {
        this.type = type;
    }

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        boolean dressed = renderState.getOrDefaultGeckolibData(DRESSED, true);
        return dressed || this.type.nudeModel == null ? this.type.model : this.type.nudeModel;
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return this.type.texture;
    }

    @Override
    public Identifier getAnimationResource(GirlEntity animatable) {
        return this.type.animations;
    }
}
