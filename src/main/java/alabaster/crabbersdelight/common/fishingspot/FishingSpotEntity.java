package alabaster.crabbersdelight.common.fishingspot;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

// 钓点（对应 1.21 版 FishingSpotEntity）：一片水面上冒泡/鱼苗粒子的隐形标记，钓点内钓鱼有额外幸运。
public class FishingSpotEntity extends Entity {
    private static final TrackedData<Integer> RADIUS =
            DataTracker.registerData(FishingSpotEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> LIFETIME_TICKS =
            DataTracker.registerData(FishingSpotEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Boolean> DISTURBED =
            DataTracker.registerData(FishingSpotEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

    public static final int EXPIRE_FADE_TICKS = 20;
    public static final int FADE_IN_TICKS = 20;

    private int age = 0;

    public FishingSpotEntity(EntityType<?> type, World world) {
        super(type, world);
        this.noClip = true;
    }

    @Override
    public void tick() {
        super.tick();
        age++;

        // 粒子统一由服务端广播（客户端也能看到所有人的钓点），避免只在本机飘
        if (!this.getWorld().isClient) {
            if (this.random.nextFloat() < 0.16f) {
                spawnBubbles(3 + this.random.nextInt(3));
            }
            if (this.random.nextFloat() < 0.05f) {
                spawnFishingParticles(1 + this.random.nextInt(2));
            }
            // 每 20 tick 沿边缘打一圈水花，让"钓点范围"肉眼可见
            if (age % 20 == 0) {
                spawnRing();
            }
            // 每 40 tick 播一次水花音效，远处也能靠声音找到
            if (age % 40 == 0) {
                this.getWorld().playSound(null, getX(), getY(), getZ(),
                        net.minecraft.sound.SoundEvents.ENTITY_FISHING_BOBBER_SPLASH,
                        net.minecraft.sound.SoundCategory.NEUTRAL, 0.8F, 0.9F + this.random.nextFloat() * 0.2F);
            }
        }

        if (age >= getLifetimeTicks()) {
            discard();
        }
    }

    private void spawnBubbles(int count) {
        if (!(this.getWorld() instanceof ServerWorld serverWorld)) {
            return;
        }
        for (int i = 0; i < count; i++) {
            double angle = this.random.nextDouble() * Math.PI * 2;
            double dist = Math.sqrt(this.random.nextDouble()) * getRadius();
            double x = getX() + Math.cos(angle) * dist;
            double z = getZ() + Math.sin(angle) * dist;
            serverWorld.spawnParticles(ParticleTypes.BUBBLE_POP, x, getY() + 0.1, z, 1, 0.0, 0.02, 0.0, 0.0);
        }
    }

    private void spawnFishingParticles(int count) {
        if (!(this.getWorld() instanceof ServerWorld serverWorld)) {
            return;
        }
        for (int i = 0; i < count; i++) {
            double angle = this.random.nextDouble() * Math.PI * 2;
            double dist = Math.sqrt(this.random.nextDouble()) * getRadius();
            double x = getX() + Math.cos(angle) * dist;
            double z = getZ() + Math.sin(angle) * dist;
            serverWorld.spawnParticles(ParticleTypes.FISHING, x, getY() + 0.1, z, 1, 0.0, 0.02, 0.0, 0.0);
        }
    }

    // 边缘水花圈：半径 getRadius() 上均匀 24 个点，用更大的 SPLASH 粒子
    private void spawnRing() {
        if (!(this.getWorld() instanceof ServerWorld serverWorld)) {
            return;
        }
        int points = 24;
        double radius = Math.max(1.0, getRadius());
        for (int i = 0; i < points; i++) {
            double angle = Math.PI * 2 * i / points;
            double x = getX() + Math.cos(angle) * radius;
            double z = getZ() + Math.sin(angle) * radius;
            serverWorld.spawnParticles(ParticleTypes.SPLASH, x, getY() + 0.05, z, 1, 0.0, 0.0, 0.0, 0.05);
        }
    }

    public int getAge() {
        return age;
    }

    public boolean contains(double x, double y, double z) {
        double dx = x - getX();
        double dz = z - getZ();
        return dx * dx + dz * dz <= (double) getRadius() * getRadius() && Math.abs(y - getY()) <= 2;
    }

    public void expireNow() {
        setLifetimeTicks(Math.min(getLifetimeTicks(), age + EXPIRE_FADE_TICKS));
    }

    public void disturb() {
        this.dataTracker.set(DISTURBED, true);
        expireNow();
    }

    public boolean isDisturbed() {
        return this.dataTracker.get(DISTURBED);
    }

    public int getRadius() {
        return this.dataTracker.get(RADIUS);
    }

    public void setRadius(int radius) {
        this.dataTracker.set(RADIUS, radius);
    }

    public int getLifetimeTicks() {
        return this.dataTracker.get(LIFETIME_TICKS);
    }

    public void setLifetimeTicks(int ticks) {
        this.dataTracker.set(LIFETIME_TICKS, ticks);
    }

    @Override
    protected void initDataTracker() {
        this.dataTracker.startTracking(RADIUS, 4);
        this.dataTracker.startTracking(LIFETIME_TICKS, 2400);
        this.dataTracker.startTracking(DISTURBED, false);
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        this.age = nbt.getInt("Age");
        setRadius(nbt.getInt("Radius"));
        setLifetimeTicks(nbt.getInt("LifetimeTicks"));
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        nbt.putInt("Age", age);
        nbt.putInt("Radius", getRadius());
        nbt.putInt("LifetimeTicks", getLifetimeTicks());
    }

    @Override
    public boolean isCollidable() {
        return false;
    }
}
