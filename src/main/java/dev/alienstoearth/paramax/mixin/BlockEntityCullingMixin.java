package dev.alienstoearth.paramax.mixin;

import dev.alienstoearth.paramax.config.ParaMaxConfig;
import dev.alienstoearth.paramax.governor.PerformanceGovernor;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
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
public abstract class BlockEntityCullingMixin {

    @Shadow private Vec3 cameraPos;

    @Inject(method = "tryExtractRenderState", at = @At("HEAD"), cancellable = true)
    private void paramax$distanceCull(BlockEntity blockEntity, float tickProgress,
                                      ModelFeatureRenderer.CrumblingOverlay crumblingOverlay,
                                      CallbackInfoReturnable<BlockEntityRenderState> cir) {
        ParaMaxConfig cfg = ParaMaxConfig.get();
        if (!cfg.enabled || this.cameraPos == null) {
            return;
        }

        double max = PerformanceGovernor.maxBlockEntityRenderDistance(cfg);
        if (max == Double.MAX_VALUE) {
            return;
        }
        if (!Vec3.atCenterOf(blockEntity.getBlockPos()).closerThan(this.cameraPos, max)) {
            cir.setReturnValue(null);
        }
    }
}
