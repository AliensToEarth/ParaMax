package dev.alienstoearth.paramax.mixin;

import dev.alienstoearth.paramax.config.ParaMaxConfig;
import dev.alienstoearth.paramax.parallel.EntityStatePool;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EntityRenderer.class)
public abstract class EntityStatePoolMixin {

    @Redirect(method = "getAndUpdateRenderState",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/render/entity/EntityRenderer;createRenderState()Lnet/minecraft/client/render/entity/state/EntityRenderState;"))
    private EntityRenderState paramax$pooledCreate(EntityRenderer<?, ?> renderer) {
        ParaMaxConfig cfg = ParaMaxConfig.get();

        if (!cfg.enabled || !cfg.poolEntityStates || cfg.temporalEntityLod) {
            return renderer.createRenderState();
        }
        EntityRenderState state = EntityStatePool.acquire(renderer);
        if (state == null) {
            state = renderer.createRenderState();
            EntityStatePool.track(renderer, state);
        }
        return state;
    }
}
