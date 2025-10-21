package net.theblueshark.better_leads.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.entity.decoration.LeashKnotEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.LeadItem;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.RabbitEntity;
import net.minecraft.server.world.ServerWorld;

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

		ActionResult result = LeadItem.attachHeldMobsToBlock(player, world, pos);

		if (result != ActionResult.PASS) {
			cir.setReturnValue(result);
			return;
		}

		spawnLeashedRabbit(player, world, pos);
		context.getStack().decrement(1);
		cir.setReturnValue(ActionResult.SUCCESS);
	}

	@Unique
	private void spawnLeashedRabbit(PlayerEntity player, World world, BlockPos pos) {
		if (!(world instanceof ServerWorld serverWorld)) return;

		LeashKnotEntity leashKnot = LeashKnotEntity.getOrCreate(world, pos);
		leashKnot.setInvulnerable(true);
		leashKnot.addCommandTag("leash_knot");
		leashKnot.onPlace();

		RabbitEntity rabbit = new RabbitEntity(EntityType.RABBIT, world);

		rabbit.refreshPositionAndAngles(pos.getX() + 0.5, pos.getY() + 0.375, pos.getZ() + 0.5, 0, 0);
		rabbit.addStatusEffect(new StatusEffectInstance(StatusEffects.INVISIBILITY, Integer.MAX_VALUE, 0, false, false));
		rabbit.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, Integer.MAX_VALUE, 255, false, false));
		rabbit.addCommandTag("leash_knot");
		rabbit.setInvisible(true);
		rabbit.setInvulnerable(true);
		rabbit.setAiDisabled(true);
		rabbit.setSilent(true);

		serverWorld.spawnEntity(rabbit);

		rabbit.attachLeash(player, true);
	}
}