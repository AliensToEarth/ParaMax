package dev.alienstoearth.paramax.mixin;

import dev.alienstoearth.paramax.config.ParaMaxConfig;
import dev.alienstoearth.paramax.parallel.EntityStatePool;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.entity.EntityRenderManager;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderManager.class)
public abstract class EntityStatePoolRecycleMixin {

    @Inject(method = "configure", at = @At("TAIL"))
    private void paramax$recycle(Camera camera, Entity targetedEntity, CallbackInfo ci) {
        ParaMaxConfig cfg = ParaMaxConfig.get();
        if (!cfg.enabled || !cfg.poolEntityStates || cfg.temporalEntityLod) {
            if (!EntityStatePool.isEmpty()) {
                EntityStatePool.clear();
            }
            return;
        }
        EntityStatePool.recycle();
    }
}
