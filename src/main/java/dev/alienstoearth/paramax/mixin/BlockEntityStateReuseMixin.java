package dev.alienstoearth.paramax.mixin;

import dev.alienstoearth.paramax.ParaMaxState;
import dev.alienstoearth.paramax.config.ParaMaxConfig;
import dev.alienstoearth.paramax.parallel.BlockEntityStateCache;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockEntityRenderDispatcher.class)
public abstract class BlockEntityStateReuseMixin {

    @Shadow private Vec3 cameraPos;

    @Shadow
    public abstract <E extends BlockEntity, S extends BlockEntityRenderState> BlockEntityRenderer<E, S> getRenderer(E blockEntity);

    @Inject(method = "tryExtractRenderState", at = @At("HEAD"), cancellable = true)
    private <E extends BlockEntity, S extends BlockEntityRenderState> void paramax$tryExtractRenderState(
            E blockEntity, float tickProgress, ModelFeatureRenderer.CrumblingOverlay crumblingOverlay,
            boolean isGloballyRendered, CallbackInfoReturnable<S> cir) {
        ParaMaxConfig cfg = ParaMaxConfig.get();
        if (!cfg.enabled || !cfg.reuseBlockEntityStates) {
            if (!BlockEntityStateCache.isEmpty()) {
                BlockEntityStateCache.clear();
            }
            return;
        }

        BlockEntityRenderer<E, S> renderer = this.getRenderer(blockEntity);
        if (renderer == null) {
            return;
        }
        if (!blockEntity.hasLevel() || !blockEntity.getType().isValid(blockEntity.getBlockState())) {
            return;
        }
        if (isGloballyRendered != renderer.shouldRenderOffScreen()) {
            return;
        }
        if (!renderer.shouldRender(blockEntity, this.cameraPos)) {
            return;
        }

        long frame = ParaMaxState.frames;
        @SuppressWarnings("unchecked")
        S cached = (S) BlockEntityStateCache.get(blockEntity, frame);
        S state = cached;
        if (state == null) {
            state = renderer.createRenderState();
            BlockEntityStateCache.put(blockEntity, state, frame);
        }

        renderer.extractRenderState(blockEntity, state, tickProgress, this.cameraPos, crumblingOverlay);
        cir.setReturnValue(state);
    }
}
