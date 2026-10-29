package com.trolmastercard.sexmod;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Client → server: the scene partner pressed the "next stage" key. */
public record AdvancePayload() implements CustomPacketPayload {
    public static final AdvancePayload INSTANCE = new AdvancePayload();
    public static final Type<AdvancePayload> TYPE = new Type<>(JennyMod.id("advance"));
    public static final StreamCodec<RegistryFriendlyByteBuf, AdvancePayload> CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public Type<AdvancePayload> type() {
        return TYPE;
    }
}
