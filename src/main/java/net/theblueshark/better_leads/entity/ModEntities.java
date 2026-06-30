package net.theblueshark.better_leads.entity;

import eu.pb4.polymer.core.api.entity.PolymerEntityUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.theblueshark.better_leads.BetterLeadsMod;
import net.theblueshark.better_leads.entity.custom.PolymerLeashKnotEntity;

public class ModEntities {

    public static final EntityType<PolymerLeashKnotEntity> POLYMER_LEASH_KNOT = register(
            Identifier.fromNamespaceAndPath(BetterLeadsMod.MOD_ID, "polymer_leash_knot"),
            EntityType.Builder.of(PolymerLeashKnotEntity::new, MobCategory.MISC).sized(0.5f, 0.5f)
    );


    private static <T extends Entity> EntityType<T> register(Identifier provoker, EntityType.Builder<T> build) {
        var type = Registry.register(BuiltInRegistries.ENTITY_TYPE, provoker, build.build(ResourceKey.create(Registries.ENTITY_TYPE, provoker)));
        PolymerEntityUtils.registerType(type);
        return type;
    }

    public static void registerModEntities() {
        BetterLeadsMod.LOGGER.info("Registering Mod Entities for " + BetterLeadsMod.MOD_ID);
    }
}
