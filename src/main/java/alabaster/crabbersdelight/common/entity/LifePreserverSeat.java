package alabaster.crabbersdelight.common.entity;

import alabaster.crabbersdelight.common.registry.CDModEntities;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;

// 救生圈的隐形座位实体（对应 1.21 版 LifePreserverSeat）
public class LifePreserverSeat extends Entity {
    private static final double PASSENGER_HEIGHT = 0.25;

    public LifePreserverSeat(EntityType<? extends LifePreserverSeat> type, World world) {
        super(type, world);
        this.noClip = true;
        this.setNoGravity(true);
    }

    public LifePreserverSeat(World world, double x, double y, double z) {
        this(CDModEntities.LIFE_PRESERVER_SEAT, world);
        this.setPosition(x, y, z);
    }

    @Override
    public double getMountedHeightOffset() {
        return PASSENGER_HEIGHT;
    }

    @Override
    protected void initDataTracker() {
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
    }
}
