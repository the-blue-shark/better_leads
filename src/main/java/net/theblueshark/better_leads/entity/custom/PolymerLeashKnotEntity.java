package net.theblueshark.better_leads.entity.custom;

import eu.pb4.polymer.core.api.entity.PolymerEntity;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.EntityAttachment;
import eu.pb4.polymer.virtualentity.api.data.EntityData;
import eu.pb4.polymer.virtualentity.api.elements.GenericEntityElement;
import eu.pb4.polymer.virtualentity.api.elements.MobAnchorElement;
import eu.pb4.polymer.virtualentity.api.elements.VirtualElement;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.Leashable;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.BlockAttachedEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.tags.BlockTags;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PolymerLeashKnotEntity extends BlockAttachedEntity implements PolymerEntity, Leashable {
    private final ElementHolder holder;
    private final EntityAttachment attachment;
    private final GenericEntityElement leashKnot = new GenericEntityElement() {
        @Override
        protected EntityType<? extends Entity> getEntityType() {
            return EntityTypes.LEASH_KNOT;
        }
    };
    @Nullable
    private Leashable.LeashData leashData;

    private final MobAnchorElement rideAnchor = new MobAnchorElement();

    public PolymerLeashKnotEntity(EntityType<? extends BlockAttachedEntity> entityType, Level world) {
        super(entityType, world);

        this.holder = new ElementHolder() {
            @Override
            protected void notifyElementsOfPositionUpdate(Vec3 newPos, Vec3 delta) {
                rideAnchor.notifyMove(this.getPos(), newPos, delta);
            }

            @Override
            public Vec3 getPos() {
                return this.getAttachment().getPos();
            }
        };

        this.holder.addPassengerElement(leashKnot);
        this.holder.addElement(rideAnchor);
        this.attachment = new EntityAttachment(this.holder, this, false);
        this.leashKnot.setInteractionHandler(VirtualElement.InteractionHandler.redirect(this));
    }



    @Override
    public EntityType<?> getPolymerEntityType(PacketContext packetContext) {
        return EntityTypes.SILVERFISH;
    }

    @Nullable
    public Leashable.LeashData getLeashData() {
        return this.leashData;
    }

    public void setLeashData(@Nullable Leashable.LeashData leashData) {
        this.leashData = leashData;
    }


    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 1024.0;
    }


    public void dropItem(ServerLevel world, @Nullable Entity breaker) {
        this.dropLeash();
        this.playSound(SoundEvents.LEAD_UNTIED, 1.0F, 1.0F);
    }

    protected void addAdditionalSaveData(ValueOutput view) {
        this.writeLeashData(view, this.leashData);
    }

    protected void readAdditionalSaveData(ValueInput view) {
        this.readLeashData(view);
    }


    public void notifyLeasheeRemoved(Leashable heldLeashable) {
        if (Leashable.leashableLeashedTo(this).isEmpty()) {
            this.discard();
        }

    }

    public void onPlace() {
        this.playSound(SoundEvents.LEAD_TIED, 1.0F, 1.0F);
    }

    @Override
    protected void recalculateBoundingBox() {
        this.setPosRaw((double)this.pos.getX() + 0.5, (double)this.pos.getY() + 0.375, (double)this.pos.getZ() + 0.5);
        double d = (double)this.getType().getWidth() / 2.0;
        double e = (double)this.getType().getHeight();
        this.setBoundingBox(new AABB(this.getX() - d, this.getY(), this.getZ() - d, this.getX() + d, this.getY() + e, this.getZ() + d));
    }

    public boolean survives() {
        return this.level().getBlockState(this.pos).is(BlockTags.FENCES);
    }

    public Vec3 getRopeHoldPosition(float tickProgress) {
        return this.getPosition(tickProgress).add(0.0, 0.2, 0.0);
    }

    public ItemStack getPickResult() {
        return new ItemStack(Items.LEAD);
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide() && this.level() instanceof ServerLevel serverWorld) {
            Leashable.tickLeash(serverWorld, this);

            if (!this.mayBeLeashed()) {
                this.discard();
            }
        }
    }

    @Override
    public void modifyRawTrackedData(List<SynchedEntityData.DataValue<?>> data, ServerPlayer player, boolean initial) {
            data.add(SynchedEntityData.DataValue.create(EntityData.SILENT, true));
        data.add(SynchedEntityData.DataValue.create(EntityData.SILENT, true));
        data.add(SynchedEntityData.DataValue.create(EntityData.FLAGS, (byte) (1 << EntityData.INVISIBLE_FLAG_INDEX)));
            data.add(SynchedEntityData.DataValue.create(EntityData.NO_GRAVITY, true));
            data.add(SynchedEntityData.DataValue.create(EntityData.NAME_VISIBLE, false));
    }
}

