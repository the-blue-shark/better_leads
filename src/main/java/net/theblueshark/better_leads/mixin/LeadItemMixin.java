package net.theblueshark.better_leads.mixin;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.LeadItem;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.theblueshark.better_leads.entity.ModEntities;
import net.theblueshark.better_leads.entity.custom.PolymerLeashKnotEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.rabbit.Rabbit;
import net.minecraft.server.level.ServerLevel;

import java.util.List;

@Mixin(LeadItem.class)
public class LeadItemMixin {

	@Inject(method = "useOn", at = @At("HEAD"), cancellable = true)
	private void onUseOnBlock(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
		Level world = context.getLevel();
		BlockPos pos = context.getClickedPos();
		BlockState state = world.getBlockState(pos);

		if (!state.is(BlockTags.FENCES)) return;

		Player player = context.getPlayer();
		if (player == null || world.isClientSide()) return;

		List<Leashable> heldLeashables = Leashable.leashableInArea(
				world,
				Vec3.atCenterOf(pos),
				leashable -> leashable.getLeashHolder() == player
		);

		List<PolymerLeashKnotEntity> playerConnectedPolymerKnots = heldLeashables.stream()
				.filter(leashable -> leashable instanceof PolymerLeashKnotEntity)
				.map(leashable -> (PolymerLeashKnotEntity) leashable)
				.filter(knot -> knot.blockPosition().equals(pos))
				.toList();

		if (!playerConnectedPolymerKnots.isEmpty()) {
			cir.setReturnValue(InteractionResult.PASS);
			return;
		}

		InteractionResult result = LeadItem.bindPlayerMobs(player, world, pos);

		if (result != InteractionResult.PASS) {
			cir.setReturnValue(result);
			return;
		}

		spawnLeashedPolymerLeashKnot(player, world, pos);
		context.getItemInHand().shrink(1);
		cir.setReturnValue(InteractionResult.SUCCESS);
	}

	@Inject( method = "bindPlayerMobs", at = @At("HEAD"), cancellable = true )
	private static void preventVanillaLeashIfPolymerKnot(Player player, Level world, BlockPos pos, CallbackInfoReturnable<InteractionResult> cir ) {
		List<Leashable> heldLeashables = Leashable.leashableInArea(
				world,
				Vec3.atCenterOf(pos),
				leashable -> leashable.getLeashHolder() == player
		);

		List<PolymerLeashKnotEntity> playerConnectedPolymerKnots = heldLeashables.stream()
				.filter(leashable -> leashable instanceof PolymerLeashKnotEntity)
				.map(leashable -> (PolymerLeashKnotEntity) leashable)
				.filter(knot -> knot.blockPosition().equals(pos))
				.toList();

		if (!playerConnectedPolymerKnots.isEmpty()) {
			cir.setReturnValue(InteractionResult.PASS);
			cir.cancel();
		}

	}

	@Unique
	private void spawnLeashedPolymerLeashKnot(Player player, Level world, BlockPos pos) {
		if (!(world instanceof ServerLevel serverWorld)) return;

		PolymerLeashKnotEntity leashKnot = new PolymerLeashKnotEntity(ModEntities.POLYMER_LEASH_KNOT, world);

		leashKnot.snapTo(pos.getX() + 0.5, pos.getY() + 0.375, pos.getZ() + 0.5, 0, 0);
		leashKnot.onPlace();
		serverWorld.addFreshEntity(leashKnot);

		leashKnot.setLeashedTo(player, true);
	}
}