package com.trolmastercard.sexmod;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Client → server: the player picked an action from a character's menu. */
public record ActionPayload(int entityId, String action) implements CustomPacketPayload {
    public static final Type<ActionPayload> TYPE = new Type<>(JennyMod.id("action"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ActionPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, ActionPayload::entityId,
            ByteBufCodecs.stringUtf8(64), ActionPayload::action,
            ActionPayload::new);

    @Override
    public Type<ActionPayload> type() {
        return TYPE;
    }
}
