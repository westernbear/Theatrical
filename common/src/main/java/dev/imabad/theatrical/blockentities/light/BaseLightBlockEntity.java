package dev.imabad.theatrical.blockentities.light;

import dev.imabad.theatrical.Theatrical;
import dev.imabad.theatrical.api.DynamicLightProvider;
import dev.imabad.theatrical.api.FixtureProvider;
import dev.imabad.theatrical.api.Support;
import dev.imabad.theatrical.blockentities.ClientSyncBlockEntity;
import dev.imabad.theatrical.blockentities.SupportedBlockEntity;
import dev.imabad.theatrical.blocks.HangableBlock;
import dev.imabad.theatrical.blocks.light.BaseLightBlock;
import dev.imabad.theatrical.config.TheatricalConfig;
import dev.imabad.theatrical.lighting.LightManager;
import io.github.westernbear.lumina.api.LuminaLights;
import io.github.westernbear.lumina.light.LightCaster;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.AxisCycle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.Optional;

public abstract class BaseLightBlockEntity extends ClientSyncBlockEntity implements FixtureProvider, DynamicLightProvider, SupportedBlockEntity {
    AABB INFINITE_EXTENT_AABB = new AABB(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
    private double distance = 0;
    protected int pan, tilt, focus, intensity, red, green, blue = 0;
    protected int prevTilt, prevPan, prevFocus, prevIntensity, prevRed, prevGreen, prevBlue, prevColour = 0;
    protected float prevSpread = 0;
    private BlockPos emissionBlock, prevEmissionBlock;
    private int prevLuminance;
    private LongOpenHashSet trackedLitChunkPos = new LongOpenHashSet();
    private LightCaster luminaLight;
    private BlockState luminaBlockState;
    private boolean luminaDirty = true;

    public BaseLightBlockEntity(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    @Override
    public void write(ValueOutput output) {
        output.putInt("pan", this.pan);
        output.putInt("tilt", this.tilt);
        output.putInt("focus", this.focus);
        output.putDouble("distance", distance);
        output.putInt("intensity", intensity);
        output.putInt("prevIntensity", prevIntensity);
        output.putInt("red", red);
        output.putInt("green", green);
        output.putInt("blue", blue);
        output.putInt("prevRed", prevRed);
        output.putInt("prevGreen", prevGreen);
        output.putInt("prevBlue", prevBlue);
    }

    @Override
    public void read(ValueInput input) {
        pan = input.getIntOr("pan", 0);
        tilt = input.getIntOr("tilt", 0);
        focus = input.getIntOr("focus", 0);
        prevPan = pan;
        prevTilt = tilt;
        prevFocus = focus;
        distance = input.getDoubleOr("distance", 0);
        intensity = input.getIntOr("intensity", 0);
        prevIntensity = input.getIntOr("prevIntensity", 0);
        red = input.getIntOr("red", 0);
        green = input.getIntOr("green", 0);
        blue = input.getIntOr("blue", 0);
        prevRed = input.getIntOr("prevRed", 0);
        prevGreen = input.getIntOr("prevGreen", 0);
        prevBlue = input.getIntOr("prevBlue", 0);
    }

    public double getDistance() {
        return distance;
    }

    public AABB getRenderBoundingBox(){
        return INFINITE_EXTENT_AABB;
    }

    public BlockPos getEmissionBlock(){
        return emissionBlock;
    }

    public BlockPos getPrevEmissionBlock() {
        return prevEmissionBlock;
    }

    public void setPrevEmissionBlock(BlockPos prevEmissionBlock) {
        this.prevEmissionBlock = prevEmissionBlock;
    }

    public int getPrevLuminance() {
        return prevLuminance;
    }

    public void setPrevLuminance(int prevLuminance) {
        this.prevLuminance = prevLuminance;
    }

    public LongOpenHashSet getTrackedLitChunkPos() {
        return trackedLitChunkPos;
    }

    public void setTrackedLitChunkPos(LongOpenHashSet trackedLitChunkPos) {
        this.trackedLitChunkPos = trackedLitChunkPos;
    }

    protected boolean storePrev(){
        boolean hasChanged = false;
        if(tilt != prevTilt){
            prevTilt = tilt;
            hasChanged = true;
        }
        if(pan != prevPan){
            prevPan = pan;
            hasChanged = true;
        }
        if(focus != prevFocus){
            prevFocus = focus;
            hasChanged = true;
        }
        if(intensity != prevIntensity){
            prevIntensity =  intensity;
            hasChanged = true;
        }
        if(red != prevRed){
            prevRed = red;
            hasChanged = true;
        }
        if(green != prevGreen){
            prevGreen = green;
            hasChanged = true;
        }
        if(blue != prevBlue){
            prevBlue = blue;
            hasChanged = true;
        }
        return hasChanged;
    }

    @Override
    public float getIntensity() {
        return intensity;
    }

    @Override
    public float getMaxLightDistance() {
        return TheatricalConfig.INSTANCE.COMMON.defaultMaxLightDist;
    }

    @Override
    public boolean shouldTrace() {
        return getIntensity() > 0;
    }

    @Override
    public boolean emitsLight() {
        return !getBlockState().getValue(HangableBlock.BROKEN) && TheatricalConfig.INSTANCE.COMMON.shouldEmitLight;
    }

    @Override
    public boolean isUpsideDown() {
        return false;
    }

    public static <T extends BlockEntity> void tick(Level level, BlockPos pos, BlockState state, T be) {
        BaseLightBlockEntity tile = (BaseLightBlockEntity) be;
        if (state != tile.luminaBlockState) {
            tile.luminaDirty = true;
        }
        if(tile.shouldTrace()){
            double distance = tile.doRayTrace();
            if (distance != tile.distance) {
                tile.distance = distance;
                tile.luminaDirty = true;
            }
        }
        tile.tick();
        if (level instanceof ServerLevel serverLevel) {
            tile.syncLumina(serverLevel);
        }
        if (level.isClientSide() && LightManager.shouldUpdateDynamicLight()) {
            if (tile.isRemoved()) {
                tile.setLightEnabled(false);
            } else {
                LightManager.updateTracking(tile);
            }
        }
    }

    public void tick() {}

    @Override
    public void setChanged() {
        super.setChanged();
        luminaDirty = true;
    }

    private void syncLumina(ServerLevel level) {
        if (!luminaDirty) {
            return;
        }
        luminaBlockState = getBlockState();
        if (!emitsLight() || !getFixture().hasBeam() || intensity <= 0 || getColour() == 0) {
            if (luminaLight != null) {
                LuminaLights.remove(level, luminaLight.getId());
                luminaLight = null;
            }
            luminaDirty = false;
            return;
        }

        Vec3 direction = rayTraceDir(this).normalize();
        float outerAngle = (float) Math.toDegrees(Math.atan2(getLightSpread(), getMaxLightDistance()));
        LightCaster next = (luminaLight == null ? LightCaster.block(getBlockPos()) : luminaLight.copy())
                .pos((float) (0.5 + direction.x * 0.25),
                        (float) (0.5 + direction.y * 0.25),
                        (float) (0.5 + direction.z * 0.25))
                .direction((float) direction.x, (float) direction.y, (float) direction.z)
                .color(red, green, blue, 255)
                .intensity(intensity)
                .distance(Mth.clamp((float) distance, 0.0F, 200.0F))
                .angle(outerAngle * 0.75F, outerAngle)
                .setShadow(LightCaster.ShadowConfig.DEFAULT.withEnabled(false))
                .setFlare(LightCaster.FlareConfig.DEFAULT.withEnabled(false));

        if (luminaLight == null) {
            try {
                luminaLight = LuminaLights.addTemporary(level, next);
            } catch (IllegalStateException exception) {
                Theatrical.LOGGER.warn("Could not register Lumina light at {}: {}", getBlockPos(), exception.getMessage());
            }
        } else if (!sameLuminaLight(luminaLight, next)) {
            luminaLight = LuminaLights.update(level, next);
        }
        luminaDirty = false;
    }

    private static boolean sameLuminaLight(LightCaster first, LightCaster second) {
        return first.getPosition().equals(second.getPosition())
                && first.getDirection().equals(second.getDirection())
                && first.getColor().equals(second.getColor())
                && first.getIntensity() == second.getIntensity()
                && first.getDistance() == second.getDistance()
                && first.getInnerAngle() == second.getInnerAngle()
                && first.getOuterAngle() == second.getOuterAngle();
    }

    public int getPan() {
        return pan;
    }

    public int getTilt() {
        return tilt;
    }

    public int getFocus() {
        return focus;
    }

    public int getPrevTilt() {
        return prevTilt;
    }

    public int getPrevPan() {
        return prevPan;
    }

    public int getPrevFocus() {
        return prevFocus;
    }

    public int getRed() {
        return red;
    }

    public int getGreen() {
        return green;
    }

    public int getBlue() {
        return blue;
    }

    public int getPrevIntensity() {
        return prevIntensity;
    }

    public int getPrevRed() {
        return prevRed;
    }

    public int getPrevGreen() {
        return prevGreen;
    }

    public int getPrevBlue() {
        return prevBlue;
    }

    public int getColorHex(){
        return (getRed() << 16) | (getGreen() << 8) | getBlue();
    }

    public int getPrevColor(){
        return (getPrevRed() << 16) | (getPrevGreen() << 8) | getPrevBlue();
    }

    public int getPrevColour() {
        return prevColour;
    }

    public void setPrevColour(int prevColour) {
        this.prevColour = prevColour;
    }

    public Optional<BlockState> getSupportingStructure(){
        if(getLevel() != null){
            BlockState blockState = getLevel().getBlockState(getBlockPos()
                    .relative(getBlockState().getValue(HangableBlock.HANG_DIRECTION)));
            if(blockState.getBlock() instanceof Support) {
                return Optional.of(blockState);
            }
        }
        return Optional.empty();
    }

    public int getBasePan(){
        if(isHangingNonVertically(getBlockState())){
            Direction facing = getBlockState().getValue(BaseLightBlock.FACING);
            return switch (facing) {
                case NORTH, SOUTH -> 90;
                case WEST -> 180;
                default -> 0;
            };
        }
        return 0;
    }

    public int calculatePartialColour(float partialTicks){
        int r = (int) (getPrevRed() + ((getRed()) - getPrevRed()) * partialTicks);
        int g = (int) (getPrevGreen() + ((getGreen()) - getPrevGreen()) * partialTicks);
        int b = (int) (getPrevBlue() + ((getBlue()) - getPrevBlue()) * partialTicks);
        return (r << 16) | (g << 8) | b;
    }

    public int getColour(){
        return (getRed() << 16) | (getGreen() << 8) | getBlue();
    }

    public static final Vec3 calculateViewVector(float xRot, float yRot) {
        float f = xRot * 0.017453292F;
        float g = -yRot * 0.017453292F;
        float h = Mth.cos(g);
        float i = Mth.sin(g);
        float j = Mth.cos(f);
        float k = Mth.sin(f);
        return new Vec3(i * j, -k, h * j);
    }

    public static boolean isHangingNonVertically(BlockState blockState){
        return isHangingNonVertically(blockState.getValue(BaseLightBlock.HANG_DIRECTION),
                blockState.getValue(BaseLightBlock.HANGING));
    }
    public static boolean isHangingNonVertically(Direction hangDirection, boolean isHanging){
        return (hangDirection != Direction.DOWN && hangDirection != Direction.UP) && isHanging;
    }

    public static Vec3 rayTraceDir(BaseLightBlockEntity be){
        BlockState blockState = be.getBlockState();
        Direction hangDirection = blockState.getValue(BaseLightBlock.HANG_DIRECTION);
        Direction direction = blockState.getValue(BaseLightBlock.FACING);
        boolean isHangingNonVertically = isHangingNonVertically(hangDirection, blockState.getValue(BaseLightBlock.HANGING));
        // TODO: Come back and try make this use the same code for both.
        if(!isHangingNonVertically) {
            float tilt = be.getTilt();
            if (be.isUpsideDown() || be.getFixture().invertTilt()) {
                tilt = -tilt;
            }
            if(be instanceof LEDPanelBlockEntity){
                if(blockState.getValue(BaseLightBlock.HANG_DIRECTION) == Direction.DOWN){
                    tilt = -90;
                } else if (blockState.getValue(BaseLightBlock.HANG_DIRECTION) == Direction.UP){
                    tilt = 90;
                }
            }
            float pan = (direction.toYRot() - be.getPan());
            if(direction.getAxis() == Direction.WEST.getAxis()){
                pan -= 180;
            }
            if(be.getFixture().invertPan()){
                pan *= -1;
            }
            if (be.isUpsideDown()) {
                if (direction.getAxis() == Direction.Axis.X) {
                    pan = (direction.getOpposite().toYRot() + be.getPan());
                } else {
                    pan = (direction.toYRot() + be.getPan());
                }
            }
            return BaseLightBlockEntity.calculateViewVector(tilt, pan);
        } else {
            // Kindly put together with help from @Hekera & @Mikey
            Direction opposite = hangDirection.getOpposite();
            int step = opposite.getAxisDirection().getStep();
            float offset = 0;
            float toRad = 3.14159F / 180;
            float pan = be.getBasePan() + be.getPan();
            float tilt = be.getTilt();
            pan *= step * toRad;
            tilt *= -step * toRad;
            float sinPan = Mth.sin(pan + offset);
            float cosPan = Mth.cos(pan + offset);
            float cosTilt = Mth.cos(tilt);
            float x = sinPan * cosTilt;
            float y = Mth.sin(tilt);
            float z = cosPan * cosTilt;
            AxisCycle cycle = AxisCycle.VALUES[(opposite.getAxis().ordinal() + 2) % AxisCycle.VALUES.length];
            return new Vec3(cycle.cycle(x, y, z, Direction.Axis.X), cycle.cycle(x, y, z, Direction.Axis.Y), cycle.cycle(x, y, z, Direction.Axis.Z));
        }
    }

    public double doRayTrace() {
        Vec3 viewVector = BaseLightBlockEntity.rayTraceDir(this);
        double distance = getMaxLightDistance();
        Vec3 vec3 = Vec3.atCenterOf(getBlockPos());
        Vec3 vec33 = vec3.add(viewVector.x * distance, viewVector.y * distance, viewVector.z * distance);
        ClipContext context = new ClipContext(vec3, vec33, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE,
                new LightCollisionContext(getBlockPos()));
        BlockHitResult result = this.level.clip(context);
        BlockPos lightPos = result.getBlockPos();
        if (result.getType() != HitResult.Type.MISS && !result.isInside()) {
            distance = result.getLocation().distanceTo(vec3);
            if (!result.getBlockPos().equals(getBlockPos())) {
                lightPos = result.getBlockPos().relative(result.getDirection(), 1);
            }
        }
        distance = new Vec3(lightPos.getX(), lightPos.getY(), lightPos.getZ()).distanceTo(new Vec3(getBlockPos().getX(), getBlockPos().getY(), getBlockPos().getZ()));
        emissionBlock = lightPos;
        return distance;
    }

    @Override
    public void setRemoved() {
        if (level instanceof ServerLevel serverLevel && luminaLight != null) {
            LuminaLights.remove(serverLevel, luminaLight.getId());
            luminaLight = null;
        }
        if(emissionBlock != null){
            this.setLightEnabled(false);
            emissionBlock = null;
        }
        super.setRemoved();
    }

    public void setTilt(int tilt){
        this.prevTilt = this.tilt;
        this.tilt = tilt;
    }

    public void setPan(int pan){
        this.prevPan = this.pan;
        this.pan = pan;
    }

    public void markAsDirty(){
        setChanged();
        level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
    }

    @Override
    public int getLightLuminance() {
        if (getFixture().hasBeam()) {
            return 0;
        }
        float newVal = intensity / 255f;
        return (int) (newVal * 15f);
    }

    @Override
    public Vector3f getLightPos() {
        return Vec3.atCenterOf(emissionBlock).toVector3f();
    }

    @Override
    public boolean shouldUpdateLight() {
        return LightManager.shouldUpdateDynamicLight() && emitsLight() && emissionBlock != null;
    }

    @Override
    public boolean updateDynamicLight(LevelRenderer renderer) {
        if (!this.shouldUpdateLight())
            return false;
        return LightManager.updateDynamicLight(this, renderer);
    }

    @Override
    public void scheduleTrackedChunksRebuild(LevelRenderer renderer) {
        if (Minecraft.getInstance().level == this.level) {
            for (long pos : this.trackedLitChunkPos) {
                LightManager.scheduleChunkRebuild(renderer, pos);
            }
        }
    }

    @Override
    public BlockPos getOwnerPos() {
        return getBlockPos();
    }

    @Override
    public int getLightColour() {
        return ((int)getIntensity() << 24) | getColour();
    }

    public float getPrevSpread() {
        return prevSpread;
    }

    public void setPrevSpread(float prevSpread) {
        this.prevSpread = prevSpread;
    }

    @Override
    public float getLightSpread() {
        float focus = (getFocus() / 255f);
        float minRadius = 1;
        float maxRadius = (float) getFixture().getLightRadius();
        float clampedSpread = Mth.clamp(focus, 0.05f, 1.0f);
        return Mth.lerp(clampedSpread, minRadius, maxRadius);
    }

    @Override
    public Level getLightWorld() {
        return getLevel();
    }
}
