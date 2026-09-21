package dev.alienstoearth.paramax.mixin;

import dev.alienstoearth.paramax.ParaMaxState;
import dev.alienstoearth.paramax.config.ParaMaxConfig;
import dev.alienstoearth.paramax.parallel.BlockEntityStateCache;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.block.entity.BlockEntityRenderManager;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockEntityRenderManager.class)
public abstract class BlockEntityStateReuseMixin {

    @Shadow private Vec3d cameraPos;

    @Shadow
    public abstract <E extends BlockEntity, S extends BlockEntityRenderState> BlockEntityRenderer<E, S> get(E blockEntity);

    @Inject(method = "getRenderState", at = @At("HEAD"), cancellable = true)
    private <E extends BlockEntity, S extends BlockEntityRenderState> void paramax$getRenderState(
            E blockEntity, float tickProgress, ModelCommandRenderer.CrumblingOverlayCommand crumblingOverlay,
            CallbackInfoReturnable<S> cir) {
        ParaMaxConfig cfg = ParaMaxConfig.get();
        if (!cfg.enabled || !cfg.reuseBlockEntityStates) {
            if (!BlockEntityStateCache.isEmpty()) {
                BlockEntityStateCache.clear();
            }
            return;
        }

        BlockEntityRenderer<E, S> renderer = this.get(blockEntity);
        if (renderer == null) {
            return;
        }
        if (!blockEntity.hasWorld() || !blockEntity.getType().supports(blockEntity.getCachedState())) {
            return;
        }
        if (!renderer.isInRenderDistance(blockEntity, this.cameraPos)) {
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

        renderer.updateRenderState(blockEntity, state, tickProgress, this.cameraPos, crumblingOverlay);
        cir.setReturnValue(state);
    }
}
