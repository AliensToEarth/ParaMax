package dev.alienstoearth.paramax.mixin;

import dev.alienstoearth.paramax.ParaMaxState;
import dev.alienstoearth.paramax.config.ParaMaxConfig;
import dev.alienstoearth.paramax.parallel.TemporalLodCache;
import net.minecraft.client.render.entity.EntityRenderManager;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
public abstract class TemporalEntityLodMixin {

    @Shadow @Final protected EntityRenderManager dispatcher;

    @Inject(method = "getAndUpdateRenderState", at = @At("HEAD"), cancellable = true)
    private void paramax$temporalLod(Entity entity, float tickProgress,
                                     CallbackInfoReturnable<EntityRenderState> cir) {
        ParaMaxConfig cfg = ParaMaxConfig.get();
        if (!cfg.enabled || !cfg.temporalEntityLod) {
            if (!TemporalLodCache.isEmpty()) {
                TemporalLodCache.clear();
            }
            return;
        }

        long frame = ParaMaxState.frames;
        EntityRenderer<?, ?> self = (EntityRenderer<?, ?>) (Object) this;
        TemporalLodCache.Entry entry = TemporalLodCache.get(entity, frame);

        double distanceSq = this.dispatcher.getSquaredDistanceToCamera(entity);
        double near = cfg.lodNearDistance;

        int interval = MathHelper.clamp((int) (Math.sqrt(distanceSq) / near) + 1, 1, cfg.lodMaxInterval);

        if (entry == null) {

            EntityRenderState state = self.createRenderState();
            paramax$fullUpdate(self, entity, state, tickProgress);
            TemporalLodCache.put(entity, state, frame);
            cir.setReturnValue(state);
            return;
        }

        if (frame - entry.lastUpdateFrame >= interval) {
            paramax$fullUpdate(self, entity, entry.state, tickProgress);
            entry.lastUpdateFrame = frame;
        } else {

            EntityRenderState state = entry.state;
            state.x = MathHelper.lerp(tickProgress, entity.lastRenderX, entity.getX());
            state.y = MathHelper.lerp(tickProgress, entity.lastRenderY, entity.getY());
            state.z = MathHelper.lerp(tickProgress, entity.lastRenderZ, entity.getZ());
            state.squaredDistanceToCamera = distanceSq;
            state.age = entity.age + tickProgress;
        }
        cir.setReturnValue(entry.state);
    }

    @Unique
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void paramax$fullUpdate(EntityRenderer renderer, Entity entity,
                                           EntityRenderState state, float tickProgress) {
        renderer.updateRenderState(entity, state, tickProgress);
        ((EntityRendererInvoker) renderer).paramax$updateShadow(entity, state);
    }
}
