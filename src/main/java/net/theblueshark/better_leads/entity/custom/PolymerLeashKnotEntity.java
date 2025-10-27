package net.theblueshark.better_leads.entity.custom;

import eu.pb4.polymer.core.api.entity.PolymerEntity;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.EntityAttachment;
import eu.pb4.polymer.virtualentity.api.elements.EntityElement;
import eu.pb4.polymer.virtualentity.api.elements.GenericEntityElement;
import eu.pb4.polymer.virtualentity.api.elements.MobAnchorElement;
import eu.pb4.polymer.virtualentity.api.tracker.EntityTrackedData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.Leashable;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.decoration.BlockAttachedEntity;
import net.minecraft.entity.decoration.LeashKnotEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.List;

public class PolymerLeashKnotEntity extends BlockAttachedEntity implements PolymerEntity, Leashable {
    private Leashable.LeashData leashData;
    private final ElementHolder holder;
    private final EntityAttachment attachment;
    private final GenericEntityElement leashKnot = new GenericEntityElement() {
        @Override
        protected EntityType<? extends Entity> getEntityType() {
            return EntityType.LEASH_KNOT;
        }
    };

    private final MobAnchorElement rideAnchor = new MobAnchorElement();

    public PolymerLeashKnotEntity(EntityType<? extends BlockAttachedEntity> entityType, World world) {
        super(entityType, world);

        this.holder = new ElementHolder() {
            @Override
            protected void notifyElementsOfPositionUpdate(Vec3d newPos, Vec3d delta) {
                rideAnchor.notifyMove(this.getPos(), newPos, delta);
            }

            @Override
            public Vec3d getPos() {
                return this.getAttachment().getPos();
            }
        };

        this.holder.addPassengerElement(leashKnot);
        this.holder.addElement(rideAnchor);
        this.attachment = new EntityAttachment(this.holder, this, false);
    }



    @Override
    public EntityType<?> getPolymerEntityType(PacketContext packetContext) {
        return EntityType.RABBIT;
    }

    @Nullable
    public Leashable.LeashData getLeashData() {
        return this.leashData;
    }

    public void setLeashData(@Nullable Leashable.LeashData leashData) {
        this.leashData = leashData;
    }

    protected void initDataTracker(DataTracker.Builder builder) {
    }

    public boolean shouldRender(double distance) {
        return distance < 1024.0;
    }


    public void onBreak(ServerWorld world, @Nullable Entity breaker) {
        this.detachLeash();
        this.playSound(SoundEvents.ITEM_LEAD_UNTIED, 1.0F, 1.0F);
    }

    protected void writeCustomData(WriteView view) {
    }

    protected void readCustomData(ReadView view) {
    }


    public void onHeldLeashUpdate(Leashable heldLeashable) {
        if (Leashable.collectLeashablesHeldBy(this).isEmpty()) {
            this.discard();
        }

    }

    public void onPlace() {
        this.playSound(SoundEvents.ITEM_LEAD_TIED, 1.0F, 1.0F);
    }

    @Override
    protected void updateAttachmentPosition() {
        this.setPos((double)this.attachedBlockPos.getX() + 0.5, (double)this.attachedBlockPos.getY() + 0.375, (double)this.attachedBlockPos.getZ() + 0.5);
        double d = (double)this.getType().getWidth() / 2.0;
        double e = (double)this.getType().getHeight();
        this.setBoundingBox(new Box(this.getX() - d, this.getY(), this.getZ() - d, this.getX() + d, this.getY() + e, this.getZ() + d));
    }

    public boolean canStayAttached() {
        return this.getEntityWorld().getBlockState(this.attachedBlockPos).isIn(BlockTags.FENCES);
    }

    public Vec3d getLeashPos(float tickProgress) {
        return this.getLerpedPos(tickProgress).add(0.0, 0.2, 0.0);
    }

    public ItemStack getPickBlockStack() {
        return new ItemStack(Items.LEAD);
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.getEntityWorld().isClient() && this.getEntityWorld() instanceof ServerWorld serverWorld) {
            Leashable.tickLeash(serverWorld, this);
            if(this.getLeashHolder() == null) {
                this.discard();
            }
        }
    }

    @Override
    public void modifyRawTrackedData(List<DataTracker.SerializedEntry<?>> data, ServerPlayerEntity player, boolean initial) {
        data.add(DataTracker.SerializedEntry.of(EntityTrackedData.FLAGS, (byte) (1 << EntityTrackedData.INVISIBLE_FLAG_INDEX)));
    }

}

