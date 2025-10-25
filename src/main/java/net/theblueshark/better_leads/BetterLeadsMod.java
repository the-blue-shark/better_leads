package net.theblueshark.better_leads;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.FenceBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.Leashable;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.theblueshark.better_leads.entity.ModEntities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class BetterLeadsMod implements ModInitializer {

	public static final String MOD_ID = "better_leads";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModEntities.registerModEntities();
		ServerTickEvents.END_SERVER_TICK.register(this::onServerTick);
	}

	private void onServerTick(MinecraftServer server) {
		for (ServerWorld world : server.getWorlds()) {
			for (ServerPlayerEntity player : world.getPlayers()) {
				double radius = 15.0;

				List<Entity> nearbyEntities = world.getEntitiesByClass(Entity.class, player.getBoundingBox().expand(radius), entity -> entity.getCommandTags().contains("leash_knot"));

				for (Entity entity : nearbyEntities) {
					BlockPos pos = entity.getBlockPos();
					BlockState blockState = world.getBlockState(pos);

					if (!(blockState.getBlock() instanceof FenceBlock)) {
						if(entity instanceof Leashable leashable && leashable.isLeashed()) {
							ItemEntity itemEntity = new ItemEntity(world, entity.getX(), entity.getY(), entity.getZ(), new ItemStack(Items.LEAD));
							world.spawnEntity(itemEntity);
						}
						entity.discard();
					}
				}
			}
		}
	}
}