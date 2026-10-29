package com.trolmastercard.sexmod;

import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

/** The characters carried over from the 1.12.2 mod, with their original asset paths. */
public enum GirlType {
    JENNY("jenny", "jenny/jennydressed", "jenny/jennynude", "jenny/jenny", "jenny/jenny", 0.49F, 1.95F),
    ELLIE("ellie", "ellie/dressed", "ellie/nude", "ellie/ellie", "ellie/ellie", 0.49F, 1.95F),
    BIA("bia", "bia/biadressed", "bia/bianude", "bia/bia", "bia/bia", 0.49F, 1.65F),
    BEE("bee", "bee/bee", null, "bee/bee", "bee/bee", 0.49F, 1.5F),
    ALLIE("allie", "allie/allie", null, "allie/allie", "allie/allie", 0.49F, 1.9F),
    LUNA("luna", "cat/cat", null, "cat/cat", "cat/cat", 0.49F, 1.8F),
    SLIME("slime", "slime/dressed", "slime/nude", "slime/slime", "slime/slime", 0.49F, 1.8F),
    GALATH("galath", "galath/galath", null, "galath/galath", "galath/galath", 0.6F, 1.95F),
    MANGLELIE("manglelie", "manglelie/manglelie", null, "manglelie/manglelie", "manglelie/manglelie", 0.49F, 1.8F);

    public final String id;
    public final Identifier model;
    public final @Nullable Identifier nudeModel;
    public final Identifier animations;
    public final Identifier texture;
    public final float width;
    public final float height;

    GirlType(String id, String model, @Nullable String nudeModel, String animations, String texture, float width, float height) {
        this.id = id;
        this.model = JennyMod.id(model);
        this.nudeModel = nudeModel == null ? null : JennyMod.id(nudeModel);
        this.animations = JennyMod.id(animations);
        this.texture = JennyMod.id("textures/entity/" + texture + ".png");
        this.width = width;
        this.height = height;
    }

    public boolean hasOutfit() {
        return this.nudeModel != null;
    }

    /** Classpath location of the animation JSON, readable on both client and dedicated server. */
    public String animationResource() {
        return "/assets/sexmod/geckolib/animations/" + this.animations.getPath() + ".animation.json";
    }
}
