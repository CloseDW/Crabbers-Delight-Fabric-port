package alabaster.crabbersdelight.common.entity.crab;

import alabaster.crabbersdelight.common.registry.CDModEntities;
import alabaster.crabbersdelight.common.registry.CDModItems;
import alabaster.crabbersdelight.common.tags.CDModTags;
import net.minecraft.block.BlockState;
import net.minecraft.entity.AnimationState;
import net.minecraft.entity.Bucketable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.AnimalMateGoal;
import net.minecraft.entity.ai.goal.EscapeDangerGoal;
import net.minecraft.entity.ai.goal.FollowParentGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.TemptGoal;
import net.minecraft.entity.ai.goal.WanderAroundGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.DyeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.ShapelessRecipe;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;

import java.util.List;
import java.util.Optional;

public class CrabEntity extends AnimalEntity implements Bucketable {
    private static final TrackedData<Boolean> FROM_BUCKET =
            DataTracker.registerData(CrabEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private Ingredient temptationItems;
    public final AnimationState idleAnimationState = new AnimationState();
    private int idleAnimationTimeout = 0;

    private static final TrackedData<Integer> VARIANT =
            DataTracker.registerData(CrabEntity.class, TrackedDataHandlerRegistry.INTEGER);

    // 攀爬标志位（对应 1.21 版的 DATA_FLAGS_ID，1bit 表示"贴墙攀爬中"）
    private static final TrackedData<Byte> CLIMBING_FLAGS =
            DataTracker.registerData(CrabEntity.class, TrackedDataHandlerRegistry.BYTE);

    public CrabEntity(EntityType<? extends AnimalEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(0, new LookAroundGoal(this));
        this.goalSelector.add(1, new EscapeDangerGoal(this, 2.0));
        this.goalSelector.add(2, new AnimalMateGoal(this, 1.0));
        this.goalSelector.add(3, new TemptGoal(this, 0.75, getTemptationItems(), false));
        this.goalSelector.add(4, new FollowParentGoal(this, 1.1));
        this.goalSelector.add(5, new LookAtEntityGoal(this, PlayerEntity.class, 6.0F));
        this.goalSelector.add(6, new WanderAroundGoal(this, 1.0));
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 10)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.2f)
                .add(EntityAttributes.GENERIC_ATTACK_KNOCKBACK, 1.5f)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 0.5)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 3.0f)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 24D);
    }

    @Override
    public int getMaxAir() {
        return 100;
    }

    // 水下呼吸：始终保持空气满值（螃蟹不会溺水）
    @Override
    protected int getNextAirUnderwater(int air) {
        return this.getMaxAir();
    }

    @Override
    protected boolean shouldSwimInFluids() {
        return false;
    }

    // 水流 / 气泡柱不推动螃蟹
    @Override
    public boolean isPushedByFluids() {
        return false;
    }

    // 攀爬：贴墙即进入攀爬态（与蜘蛛同语义，由原版 LivingEntity 物理消费）
    @Override
    public boolean isClimbing() {
        return this.isCrabClimbing();
    }

    public boolean isCrabClimbing() {
        return (this.dataTracker.get(CLIMBING_FLAGS) & 1) != 0;
    }

    public void setCrabClimbing(boolean climbing) {
        byte flags = this.dataTracker.get(CLIMBING_FLAGS);
        if (climbing) {
            flags = (byte) (flags | 1);
        } else {
            flags = (byte) (flags & -2);
        }
        this.dataTracker.set(CLIMBING_FLAGS, flags);
    }

    // 每 tick 扫水平四向，贴着实心方块就进入攀爬态
    @Override
    public void tickMovement() {
        super.tickMovement();
        if (!this.getWorld().isClient) {
            boolean nextToWall = false;
            BlockPos pos = this.getBlockPos();
            for (Direction direction : Direction.Type.HORIZONTAL) {
                BlockPos neighbour = pos.offset(direction);
                if (this.getWorld().getBlockState(neighbour).isSolidBlock(this.getWorld(), neighbour)) {
                    nextToWall = true;
                    break;
                }
            }
            this.setCrabClimbing(nextToWall);
        }
    }

    @Override
    protected void pushAway(Entity entity) {
        super.pushAway(entity);
        if (entity instanceof CrabEntity) {
            this.setCrabClimbing(this.horizontalCollision);
        }
    }

    // 螃蟹壳体坚硬：不吃摔落伤害
    @Override
    public boolean handleFallDamage(float fallDistance, float damageMultiplier, DamageSource damageSource) {
        return false;
    }

    @Override
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason,
                                 EntityData entityData, NbtCompound entityNbt) {
        RegistryEntry<Biome> holder = world.getBiome(this.getBlockPos());
        if (holder.matchesKey(BiomeKeys.MANGROVE_SWAMP)) {
            this.setVariant(CrabVariant.BLUE);
        } else if (holder.matchesKey(BiomeKeys.BEACH)) {
            this.setVariant(CrabVariant.RED);
        } else if (holder.matchesKey(BiomeKeys.SWAMP)) {
            this.setVariant(CrabVariant.GREEN);
        } else if (holder.matchesKey(BiomeKeys.STONY_SHORE)) {
            this.setVariant(CrabVariant.LIGHT_GRAY);
        } else if (holder.matchesKey(BiomeKeys.SNOWY_BEACH)) {
            this.setVariant(CrabVariant.WHITE);
        } else {
            this.setVariant(CrabVariant.BLUE);
        }
        return super.initialize(world, difficulty, spawnReason, entityData, entityNbt);
    }

    public static boolean checkCrabSpawnRules(EntityType<CrabEntity> type, ServerWorldAccess world,
                                              SpawnReason spawnReason, BlockPos pos, Random random) {
        BlockState stateBelow = world.getBlockState(pos.down());
        return stateBelow.isIn(CDModTags.CRAB_SPAWN_ON);
    }

    public static boolean canCrabSpawn(EntityType<CrabEntity> type, WorldAccess world,
                                       SpawnReason spawnReason, BlockPos pos, Random random) {
        return world.getBlockState(pos.down()).isIn(CDModTags.CRAB_SPAWN_ON);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(VARIANT, 0);
        this.dataTracker.startTracking(FROM_BUCKET, false);
        this.dataTracker.startTracking(CLIMBING_FLAGS, (byte) 0);
    }

    private int getTypeVariant() {
        return this.dataTracker.get(VARIANT);
    }

    public CrabVariant getVariant() {
        return CrabVariant.byId(this.getTypeVariant() & 255);
    }

    private void setVariant(CrabVariant variant) {
        this.dataTracker.set(VARIANT, variant.getId() & 255);
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putInt("Variant", this.getTypeVariant());
        nbt.putBoolean("FromBucket", this.isFromBucket());
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.dataTracker.set(VARIANT, nbt.getInt("Variant"));
        this.dataTracker.set(FROM_BUCKET, nbt.getBoolean("FromBucket"));
    }

    @Override
    public PassiveEntity createChild(ServerWorld world, PassiveEntity otherParent) {
        CrabEntity baby = CDModEntities.CRAB.create(world);
        if (!(otherParent instanceof CrabEntity otherCrab)) return baby;

        DyeColor color1 = this.getVariant().getDyeColor();
        DyeColor color2 = otherCrab.getVariant().getDyeColor();

        DyeColor mixedColor = getMixedDyeColor(world, color1, color2);
        baby.setVariant(CrabVariant.fromDyeColor(mixedColor != null ? mixedColor : color1));

        return baby;
    }

    // 有趣机制：通过搜索"两个染料合成新染料"的配方来决定繁殖颜色
    private static DyeColor getMixedDyeColor(ServerWorld world, DyeColor color1, DyeColor color2) {
        if (color1 == color2) return color1;

        for (Recipe<?> recipe : world.getRecipeManager().listAllOfType(RecipeType.CRAFTING)) {
            if (recipe instanceof ShapelessRecipe shapeless
                    && shapeless.getOutput(world.getRegistryManager()).getItem() instanceof DyeItem resultDye
                    && shapeless.getIngredients().size() == 2) {

                List<DyeColor> inputColors = shapeless.getIngredients().stream()
                        .map(ingredient -> {
                            ItemStack[] stacks = ingredient.getMatchingStacks();
                            if (stacks.length > 0 && stacks[0].getItem() instanceof DyeItem dyeItem) {
                                return dyeItem.getColor();
                            }
                            return null;
                        })
                        .filter(c -> c != null)
                        .toList();

                if (inputColors.size() == 2 &&
                        ((inputColors.get(0) == color1 && inputColors.get(1) == color2) ||
                                (inputColors.get(0) == color2 && inputColors.get(1) == color1))) {
                    return resultDye.getColor();
                }
            }
        }
        return null;
    }

    private Ingredient getTemptationItems() {
        if (temptationItems == null)
            temptationItems = Ingredient.fromTag(CDModTags.CRAB_TEMPT_ITEM);

        return temptationItems;
    }

    @Override
    public boolean isFromBucket() {
        return this.dataTracker.get(FROM_BUCKET);
    }

    @Override
    public void setFromBucket(boolean fromBucket) {
        this.dataTracker.set(FROM_BUCKET, true);
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return !stack.isEmpty() && getTemptationItems().test(stack);
    }

    @Override
    public void copyDataToStack(ItemStack stack) {
        NbtCompound tag = stack.getOrCreateNbt();
        tag.putInt("Age", this.getBreedingAge());
        tag.putInt("Variant", this.getTypeVariant());
    }

    @Override
    public void copyDataFromNbt(NbtCompound tag) {
        if (tag.contains("Age")) {
            this.setBreedingAge(tag.getInt("Age"));
        }
        if (tag.contains("Variant")) {
            this.setVariant(CrabVariant.byId(tag.getInt("Variant")));
        }
    }

    @Override
    public ItemStack getBucketItem() {
        return new ItemStack(CDModItems.CRAB_BUCKET);
    }

    @Override
    public SoundEvent getBucketFillSound() {
        return SoundEvents.ITEM_BUCKET_FILL_AXOLOTL;
    }

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        Optional<ActionResult> result = Bucketable.tryBucket(player, hand, this);
        if (result.isPresent()) {
            return result.get();
        }

        ItemStack itemStack = player.getStackInHand(hand);
        Item item = itemStack.getItem();

        if (item instanceof DyeItem dyeItem) {
            DyeColor dyeColor = dyeItem.getColor();
            CrabVariant newVariant = CrabVariant.fromDyeColor(dyeColor);

            if (newVariant != this.getVariant()) {
                this.setVariant(newVariant);
                if (!player.getAbilities().creativeMode) {
                    itemStack.decrement(1);
                }
                return ActionResult.success(this.getWorld().isClient);
            }
        }

        return super.interactMob(player, hand);
    }

    private void setupAnimationStates() {
        if (this.idleAnimationTimeout <= 0) {
            this.idleAnimationTimeout = 60;
            this.idleAnimationState.start(this.age);
        } else {
            --this.idleAnimationTimeout;
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (this.getWorld().isClient) {
            this.setupAnimationStates();
        }
    }
}
