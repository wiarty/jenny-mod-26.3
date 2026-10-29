package com.trolmastercard.sexmod;

import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.PlayState;
import com.geckolib.util.GeckoLibUtil;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

/** One of the mod's characters: wanders around, offers an action menu and plays scenes with a player. */
public class GirlEntity extends PathfinderMob implements GeoEntity {
    private static final EntityDataAccessor<String> ANIMATION = SynchedEntityData.defineId(GirlEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Integer> ANIMATION_SEQ = SynchedEntityData.defineId(GirlEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DRESSED = SynchedEntityData.defineId(GirlEntity.class, EntityDataSerializers.BOOLEAN);
    private static final double MAX_ACTION_DISTANCE_SQ = 8.0 * 8.0;

    /** Set by the client entrypoint; opens the action menu. */
    public static Consumer<GirlEntity> openMenu = girl -> {};
    /** Set by the client entrypoint; plays a sound keyframe from the original animation files. */
    public static BiConsumer<GirlEntity, String> playCue = (girl, cue) -> {};

    private final GirlType girlType;
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    private GirlActions.@Nullable Action scene;
    private int stage;
    private int stageTicks;
    private boolean stripping;
    private float sceneYaw;

    public GirlEntity(EntityType<? extends GirlEntity> entityType, Level level, GirlType girlType) {
        super(entityType, level);
        this.girlType = girlType;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 20.0).add(Attributes.MOVEMENT_SPEED, 0.25);
    }

    public GirlType girlType() {
        return this.girlType;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new WaterAvoidingRandomStrollGoal(this, 0.7));
        this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ANIMATION, "");
        builder.define(ANIMATION_SEQ, 0);
        builder.define(DRESSED, true);
    }

    // --- synced state -------------------------------------------------------------------------

    /** Short clip name currently forced by a scene, or "" for automatic idle/walk. */
    public String getAnimation() {
        return this.entityData.get(ANIMATION);
    }

    public int getAnimationSeq() {
        return this.entityData.get(ANIMATION_SEQ);
    }

    private void setAnimation(String clip) {
        if (DevAutoTest.SPEC != null) JennyMod.LOGGER.info("[autotest] {} clip '{}' -> '{}' stage={} riders={}", this.girlType, this.getAnimation(), clip, this.stage, this.getPassengers().size());
        this.entityData.set(ANIMATION, clip);
        this.entityData.set(ANIMATION_SEQ, this.getAnimationSeq() + 1);
        this.stageTicks = 0;
    }

    public boolean isDressed() {
        return !this.girlType.hasOutfit() || this.entityData.get(DRESSED);
    }

    public void setDressed(boolean dressed) {
        this.entityData.set(DRESSED, dressed);
    }

    public boolean inScene() {
        return !this.getAnimation().isEmpty();
    }

    // --- interaction --------------------------------------------------------------------------

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND || player.isShiftKeyDown()) return super.mobInteract(player, hand);
        if (this.level().isClientSide() && !this.inScene()) openMenu.accept(this);
        return InteractionResult.SUCCESS;
    }

    /** Server handler for an action chosen in the client menu. */
    public void performAction(ServerPlayer player, String actionId) {
        if (player.distanceToSqr(this) > MAX_ACTION_DISTANCE_SQ) {
            player.sendOverlayMessage(Component.translatable("msg.sexmod.too_far"));
            return;
        }
        if (this.inScene()) {
            player.sendOverlayMessage(Component.translatable("msg.sexmod.busy", this.getDisplayName()));
            return;
        }
        AnimationIndex index = AnimationIndex.get(this.girlType);
        if (GirlActions.DRESS.equals(actionId)) {
            if (!this.girlType.hasOutfit()) return;
            this.setDressed(true);
            return;
        }
        if (GirlActions.STRIP.equals(actionId)) {
            if (!this.girlType.hasOutfit() || !this.isDressed()) {
                player.sendOverlayMessage(Component.translatable("msg.sexmod.no_outfit"));
                return;
            }
            if (this.girlType == GirlType.JENNY && !charge(player, new ItemStack(net.minecraft.world.item.Items.GOLD_INGOT), 1)) return;
            if (index.has("strip")) {
                this.scene = new GirlActions.Action(GirlActions.STRIP, null, 0, false, false, java.util.List.of());
                this.stage = -1;
                this.stripping = true;
                this.lockRotation();
                this.setAnimation("strip");
            } else {
                this.setDressed(false);
            }
            return;
        }

        GirlActions.Action action = GirlActions.find(this.girlType, actionId);
        if (action == null) return;
        if (action.partner() && (player.isPassenger() || this.isVehicle())) {
            player.sendOverlayMessage(Component.translatable("msg.sexmod.busy", this.getDisplayName()));
            return;
        }
        if (action.cost() != null && !charge(player, new ItemStack(action.cost()), action.count())) return;

        this.scene = action;
        this.lockRotation();
        if (action.partner()) {
            player.startRiding(this, true, false);
            player.sendOverlayMessage(Component.translatable("msg.sexmod.scene",
                    Component.keybind("key.sexmod.advance"), Component.keybind("key.sneak")));
        }
        if (action.stripFirst() && this.girlType.hasOutfit() && this.isDressed() && index.has("strip")) {
            this.stage = -1;
            this.stripping = true;
            this.setAnimation("strip");
        } else {
            this.stage = 0;
            this.stripping = false;
            this.setAnimation(action.stages().get(0));
        }
    }

    /** Called when the partner presses the "next stage" key: leaves a looping clip. */
    public void advanceByPlayer(ServerPlayer player) {
        if (this.scene == null || !this.hasPassenger(player) || this.stripping) return;
        AnimationIndex.Clip clip = AnimationIndex.get(this.girlType).clip(this.getAnimation());
        if (clip != null && clip.loops()) this.nextStage();
    }

    private static boolean charge(ServerPlayer player, ItemStack price, int count) {
        if (player.isCreative()) return true;
        var inventory = player.getInventory();
        int found = inventory.clearOrCountMatchingItems(stack -> ItemStack.isSameItem(stack, price), false, 0, player.inventoryMenu.getCraftSlots());
        if (found < count) {
            player.sendOverlayMessage(Component.translatable("msg.sexmod.need", count, price.getHoverName()));
            return false;
        }
        inventory.clearOrCountMatchingItems(stack -> ItemStack.isSameItem(stack, price), true, count, player.inventoryMenu.getCraftSlots());
        return true;
    }

    // --- scene progression (server) ------------------------------------------------------------

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) return;
        if (this.scene == null) {
            // Scenes are not saved, but riding is: after a reload the partner would stay mounted
            // on a character that walks around. Drop them, and clear any stale clip.
            if (this.isVehicle()) this.ejectPassengers();
            if (this.inScene()) this.setAnimation("");
            return;
        }

        if (this.scene.partner() && !this.stripping && !this.isVehicle() && this.stage >= 0) {
            this.endScene();
            return;
        }
        this.getNavigation().stop();
        this.setYRot(this.sceneYaw);
        this.setYBodyRot(this.sceneYaw);
        this.setYHeadRot(this.sceneYaw);

        this.stageTicks++;
        AnimationIndex.Clip clip = AnimationIndex.get(this.girlType).clip(this.getAnimation());
        if (clip == null) {
            this.endScene();
        } else if (!clip.loops() && this.stageTicks >= Math.max(1, Math.round(clip.seconds() * 20.0F))) {
            this.nextStage();
        }
    }

    private void nextStage() {
        if (this.scene == null) return;
        if (this.stripping) {
            this.stripping = false;
            this.setDressed(false);
        }
        this.stage++;
        if (this.stage >= this.scene.stages().size()) {
            this.endScene();
        } else {
            this.setAnimation(this.scene.stages().get(this.stage));
        }
    }

    private void endScene() {
        this.scene = null;
        this.stripping = false;
        this.stage = 0;
        this.ejectPassengers();
        this.setAnimation("");
    }

    private void lockRotation() {
        this.sceneYaw = this.getYRot();
        this.getNavigation().stop();
    }

    @Override
    protected boolean isImmobile() {
        return super.isImmobile() || this.inScene();
    }

    // --- riding: the partner is hidden and drawn through the character's own player rig ---------

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return passenger instanceof Player && this.getPassengers().isEmpty();
    }

    /** Sideways distance of the (hidden) partner's eyes from the character during a scene. */
    private static final double VIEW_DISTANCE = 2.6;

    /**
     * The partner is drawn by the character's own player rig, so the real player only serves as the
     * camera: park their eyes beside the character at chest height, where nothing blocks the view.
     */
    @Override
    protected void positionRider(Entity passenger, Entity.MoveFunction moveFunction) {
        double yaw = Math.toRadians(this.getYRot());
        double eyeY = this.getY() + this.getBbHeight() * 0.6;
        moveFunction.accept(passenger,
                this.getX() + Math.cos(yaw) * VIEW_DISTANCE,
                eyeY - passenger.getEyeHeight(),
                this.getZ() + Math.sin(yaw) * VIEW_DISTANCE);
    }

    @Override
    protected void removePassenger(Entity passenger) {
        super.removePassenger(passenger);
        if (!this.level().isClientSide() && this.scene != null && this.scene.partner()) this.endScene();
    }

    @Override
    public boolean isPushable() {
        return !this.inScene() && super.isPushable();
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        // The original characters enter their own states instead of dying to player hits.
        if (source.getEntity() instanceof Player) return false;
        return super.hurtServer(level, source, amount);
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putBoolean("Dressed", this.entityData.get(DRESSED));
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setDressed(input.getBooleanOr("Dressed", true));
    }

    // --- GeckoLib -------------------------------------------------------------------------------

    private int observedSeq = -1;

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        AnimationIndex index = AnimationIndex.get(this.girlType);
        AnimationController<GirlEntity> controller = new AnimationController<>("main", 4, test -> {
            String clipName = this.getAnimation();
            if (clipName.isEmpty()) {
                clipName = test.isMoving() && index.has("walk") ? "walk" : index.has("idle") ? "idle" : "null";
            } else if (this.observedSeq != this.getAnimationSeq()) {
                this.observedSeq = this.getAnimationSeq();
                test.controller().reset();
            }
            AnimationIndex.Clip clip = index.clip(clipName);
            if (clip == null) return PlayState.STOP;
            return test.setAndContinue(RawAnimation.begin().thenPlay(clip.fullName()));
        });
        controller.setSoundKeyframeHandler(event -> playCue.accept(this, event.keyframeData().getSound()));
        controllers.add(controller);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }
}
