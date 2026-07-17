package dev.alienstoearth.paramax.mixin;

import dev.alienstoearth.paramax.config.ParaMaxConfig;
import dev.alienstoearth.paramax.governor.PerformanceGovernor;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.block.entity.BlockEntityRenderManager;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockEntityRenderManager.class)
public abstract class BlockEntityCullingMixin {

    @Shadow private Vec3d cameraPos;

    @Inject(method = "getRenderState", at = @At("HEAD"), cancellable = true)
    private void paramax$distanceCull(BlockEntity blockEntity, float tickProgress,
                                      ModelCommandRenderer.CrumblingOverlayCommand crumblingOverlay,
                                      CallbackInfoReturnable<BlockEntityRenderState> cir) {
        ParaMaxConfig cfg = ParaMaxConfig.get();
        if (!cfg.enabled || this.cameraPos == null) {
            return;
        }

        double max = PerformanceGovernor.maxBlockEntityRenderDistance(cfg);
        if (max == Double.MAX_VALUE) {
            return;
        }
        if (!Vec3d.ofCenter(blockEntity.getPos()).isInRange(this.cameraPos, max)) {
            cir.setReturnValue(null);
        }
    }
}
