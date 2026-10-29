package com.trolmastercard.sexmod;

import java.util.EnumMap;
import java.util.Map;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JennyMod implements ModInitializer {
    public static final String MOD_ID = "sexmod";
    public static final Logger LOGGER = LoggerFactory.getLogger("Jenny Mod");
    public static final Map<GirlType, EntityType<GirlEntity>> ENTITY_TYPES = new EnumMap<>(GirlType.class);
    public static final Map<GirlType, Item> SPAWN_EGGS = new EnumMap<>(GirlType.class);

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        for (GirlType type : GirlType.values()) {
            ResourceKey<EntityType<?>> entityKey = ResourceKey.create(Registries.ENTITY_TYPE, id(type.id));
            EntityType<GirlEntity> entityType = EntityType.Builder
                    .<GirlEntity>of((entityTypeRef, level) -> new GirlEntity(entityTypeRef, level, type), MobCategory.CREATURE)
                    .sized(type.width, type.height)
                    .eyeHeight(type.height * 0.9F)
                    .build(entityKey);
            Registry.register(BuiltInRegistries.ENTITY_TYPE, entityKey, entityType);
            FabricDefaultAttributeRegistry.register(entityType, GirlEntity.createAttributes());
            ENTITY_TYPES.put(type, entityType);

            ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, id(type.id + "_spawn_egg"));
            Item egg = new SpawnEggItem(new Item.Properties().setId(itemKey).spawnEgg(entityType));
            Registry.register(BuiltInRegistries.ITEM, itemKey, egg);
            SPAWN_EGGS.put(type, egg);
        }
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.SPAWN_EGGS)
                .register(output -> SPAWN_EGGS.values().forEach(output::accept));

        PayloadTypeRegistry.serverboundPlay().register(ActionPayload.TYPE, ActionPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(AdvancePayload.TYPE, AdvancePayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(ActionPayload.TYPE, (payload, context) -> {
            if (context.player().level().getEntity(payload.entityId()) instanceof GirlEntity girl) {
                girl.performAction(context.player(), payload.action());
            }
        });
        ServerPlayNetworking.registerGlobalReceiver(AdvancePayload.TYPE, (payload, context) -> {
            if (context.player().getVehicle() instanceof GirlEntity girl) girl.advanceByPlayer(context.player());
        });
        DevAutoTest.register();
    }
}
