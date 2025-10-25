package net.theblueshark.better_leads.entity;

import eu.pb4.polymer.core.api.entity.PolymerEntityUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.theblueshark.better_leads.BetterLeadsMod;
import net.theblueshark.better_leads.entity.custom.PolymerLeashKnotEntity;

public class ModEntities {

    public static final EntityType<PolymerLeashKnotEntity> POLYMER_LEASH_KNOT = register(
            Identifier.of(BetterLeadsMod.MOD_ID, "polymer_leash_knot"),
            EntityType.Builder.create(PolymerLeashKnotEntity::new, SpawnGroup.MISC).dimensions(0.5f, 0.5f)
    );


    private static <T extends Entity> EntityType<T> register(Identifier provoker, EntityType.Builder<T> build) {
        var type = Registry.register(Registries.ENTITY_TYPE, provoker, build.build(RegistryKey.of(RegistryKeys.ENTITY_TYPE, provoker)));
        PolymerEntityUtils.registerType(type);
        return type;
    }

    public static void registerModEntities() {
        BetterLeadsMod.LOGGER.info("Registering Mod Entities for " + BetterLeadsMod.MOD_ID);
    }
}
