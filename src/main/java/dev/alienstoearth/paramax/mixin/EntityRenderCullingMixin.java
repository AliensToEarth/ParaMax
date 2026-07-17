package dev.alienstoearth.paramax.mixin;

import dev.alienstoearth.paramax.config.ParaMaxConfig;
import dev.alienstoearth.paramax.governor.PerformanceGovernor;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
public class EntityRenderCullingMixin {

    @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
    private void paramax$distanceCull(Entity entity, Frustum frustum,
                                      double cameraX, double cameraY, double cameraZ,
                                      CallbackInfoReturnable<Boolean> cir) {
        ParaMaxConfig cfg = ParaMaxConfig.get();
        if (!cfg.enabled) {
            return;
        }

        double max = PerformanceGovernor.maxEntityRenderDistance(cfg);
        if (max == Double.MAX_VALUE) {
            return;
        }
        if (entity.squaredDistanceTo(cameraX, cameraY, cameraZ) > max * max) {
            cir.setReturnValue(false);
        }
    }
}
