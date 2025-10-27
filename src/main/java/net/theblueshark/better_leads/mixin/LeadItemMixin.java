package net.theblueshark.better_leads.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.Leashable;
import net.minecraft.entity.decoration.LeashKnotEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.LeadItem;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.theblueshark.better_leads.entity.ModEntities;
import net.theblueshark.better_leads.entity.custom.PolymerLeashKnotEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.RabbitEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.List;

@Mixin(LeadItem.class)
public class LeadItemMixin {

	@Inject(method = "useOnBlock", at = @At("HEAD"), cancellable = true)
	private void onUseOnBlock(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir) {
		World world = context.getWorld();
		BlockPos pos = context.getBlockPos();
		BlockState state = world.getBlockState(pos);

		if (!state.isIn(BlockTags.FENCES)) return;

		PlayerEntity player = context.getPlayer();
		if (player == null || world.isClient()) return;

		List<Leashable> heldLeashables = Leashable.collectLeashablesAround(
				world,
				Vec3d.ofCenter(pos),
				leashable -> leashable.getLeashHolder() == player
		);

		List<PolymerLeashKnotEntity> playerConnectedPolymerKnots = heldLeashables.stream()
				.filter(leashable -> leashable instanceof PolymerLeashKnotEntity)
				.map(leashable -> (PolymerLeashKnotEntity) leashable)
				.filter(knot -> knot.getBlockPos().equals(pos))
				.toList();

		if (!playerConnectedPolymerKnots.isEmpty()) {
			cir.setReturnValue(ActionResult.PASS);
			return;
		}

		ActionResult result = LeadItem.attachHeldMobsToBlock(player, world, pos);

		if (result != ActionResult.PASS) {
			cir.setReturnValue(result);
			return;
		}

		spawnLeashedPolymerLeashKnot(player, world, pos);
		context.getStack().decrement(1);
		cir.setReturnValue(ActionResult.SUCCESS);
	}

	@Inject( method = "attachHeldMobsToBlock", at = @At("HEAD"), cancellable = true )
	private static void preventVanillaLeashIfPolymerKnot(PlayerEntity player, World world, BlockPos pos, CallbackInfoReturnable<ActionResult> cir ) {
		List<Leashable> heldLeashables = Leashable.collectLeashablesAround(
				world,
				Vec3d.ofCenter(pos),
				leashable -> leashable.getLeashHolder() == player
		);

		List<PolymerLeashKnotEntity> playerConnectedPolymerKnots = heldLeashables.stream()
				.filter(leashable -> leashable instanceof PolymerLeashKnotEntity)
				.map(leashable -> (PolymerLeashKnotEntity) leashable)
				.filter(knot -> knot.getBlockPos().equals(pos))
				.toList();

		if (!playerConnectedPolymerKnots.isEmpty()) {
			cir.setReturnValue(ActionResult.PASS);
			cir.cancel();
		}

	}

	@Unique
	private void spawnLeashedPolymerLeashKnot(PlayerEntity player, World world, BlockPos pos) {
		if (!(world instanceof ServerWorld serverWorld)) return;

		PolymerLeashKnotEntity leashKnot = new PolymerLeashKnotEntity(ModEntities.POLYMER_LEASH_KNOT, world);

		leashKnot.refreshPositionAndAngles(pos.getX() + 0.5, pos.getY() + 0.375, pos.getZ() + 0.5, 0, 0);
		leashKnot.onPlace();
		serverWorld.spawnEntity(leashKnot);

		leashKnot.attachLeash(player, true);
	}
}