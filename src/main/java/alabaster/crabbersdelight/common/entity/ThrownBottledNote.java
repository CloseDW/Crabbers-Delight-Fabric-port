package alabaster.crabbersdelight.common.entity;

import alabaster.crabbersdelight.common.item.NoteData;
import alabaster.crabbersdelight.common.registry.CDModEntities;
import alabaster.crabbersdelight.common.registry.CDModItems;
import alabaster.crabbersdelight.common.world.BottledNotesState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;

// 扔出去的瓶中便笺（对应 1.21 版 ThrownBottledNote）：
// 落进水里 → 记进世界存档（之后别人开瓶中信有几率抽到）；落在地上 → 变回签名便笺掉落。
public class ThrownBottledNote extends ThrownItemEntity {
    private String title = "";
    private String text = "";
    private String author = "";
    private int generation = 0;
    private boolean resolved = false;

    public ThrownBottledNote(EntityType<? extends ThrownBottledNote> type, World world) {
        super(type, world);
    }

    public ThrownBottledNote(World world, LivingEntity owner, String title, String text, String author, int generation) {
        super(CDModEntities.THROWN_BOTTLED_NOTE, owner, world);
        this.title = title == null ? "" : title;
        this.text = text == null ? "" : text;
        this.author = author == null ? "" : author;
        this.generation = generation;
    }

    @Override
    protected Item getDefaultItem() {
        return CDModItems.BOTTLED_NOTE;
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.resolved && !this.getWorld().isClient && this.isTouchingWater()) {
            this.resolved = true;
            this.landInWater((ServerWorld) this.getWorld());
        }
    }

    @Override
    protected void onCollision(HitResult hitResult) {
        super.onCollision(hitResult);
        if (this.resolved || this.getWorld().isClient) {
            return;
        }
        this.resolved = true;

        if (this.isTouchingWater()) {
            this.landInWater((ServerWorld) this.getWorld());
            return;
        }

        ItemStack fallback = new ItemStack(CDModItems.SIGNED_NOTE);
        NoteData.sign(fallback, this.title, this.text, this.author, this.generation);
        if (!this.title.isBlank()) {
            fallback.setCustomName(Text.literal(this.title));
        }
        ServerWorld world = (ServerWorld) this.getWorld();
        world.spawnEntity(new ItemEntity(world, this.getX(), this.getY(), this.getZ(), fallback));
        this.discard();
    }

    private void landInWater(ServerWorld world) {
        BottledNotesState.get(world).addNote(this.title, this.text, this.author, this.generation);
        world.spawnParticles(ParticleTypes.SPLASH, this.getX(), this.getY(), this.getZ(), 8, 0.2, 0.1, 0.2, 0.05);
        this.discard();
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putString("NoteTitle", this.title);
        nbt.putString("NoteText", this.text);
        nbt.putString("NoteAuthor", this.author);
        nbt.putInt("NoteGeneration", this.generation);
        nbt.putBoolean("Resolved", this.resolved);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.title = nbt.getString("NoteTitle");
        this.text = nbt.getString("NoteText");
        this.author = nbt.getString("NoteAuthor");
        this.generation = nbt.getInt("NoteGeneration");
        this.resolved = nbt.getBoolean("Resolved");
    }
}
