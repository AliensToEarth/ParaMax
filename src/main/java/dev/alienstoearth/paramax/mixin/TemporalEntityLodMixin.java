package dev.alienstoearth.paramax.mixin;

import dev.alienstoearth.paramax.ParaMaxState;
import dev.alienstoearth.paramax.config.ParaMaxConfig;
import dev.alienstoearth.paramax.parallel.TemporalLodCache;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
public abstract class TemporalEntityLodMixin {

    @Shadow @Final protected EntityRenderDispatcher entityRenderDispatcher;

    @Inject(method = "createRenderState(Lnet/minecraft/world/entity/Entity;F)Lnet/minecraft/client/renderer/entity/state/EntityRenderState;", at = @At("HEAD"), cancellable = true)
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

        double distanceSq = this.entityRenderDispatcher.distanceToSqr(entity);
        double near = cfg.lodNearDistance;

        int interval = Mth.clamp((int) (Math.sqrt(distanceSq) / near) + 1, 1, cfg.lodMaxInterval);

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
            state.x = Mth.lerp(tickProgress, entity.xOld, entity.getX());
            state.y = Mth.lerp(tickProgress, entity.yOld, entity.getY());
            state.z = Mth.lerp(tickProgress, entity.zOld, entity.getZ());
            state.distanceToCameraSq = distanceSq;
            state.ageInTicks = entity.tickCount + tickProgress;
        }
        cir.setReturnValue(entry.state);
    }

    @Unique
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void paramax$fullUpdate(EntityRenderer renderer, Entity entity,
                                           EntityRenderState state, float tickProgress) {
        renderer.extractRenderState(entity, state, tickProgress);
        ((EntityRendererInvoker) renderer).paramax$updateShadow(entity, state);
    }
}
