package dev.alienstoearth.paramax.mixin;

import dev.alienstoearth.paramax.config.ParaMaxConfig;
import dev.alienstoearth.paramax.parallel.ParallelEngine;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.state.LevelRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

@Mixin(LevelRenderer.class)
public abstract class ParallelEntityVisibilityMixin {

    @Shadow @Final private Minecraft minecraft;
    @Shadow @Final private EntityRenderDispatcher entityRenderDispatcher;
    @Shadow private ClientLevel level;

    @Unique
    private final Map<Entity, Boolean> paramax$visibility = new IdentityHashMap<>();

    @Inject(method = "extractVisibleEntities", at = @At("HEAD"))
    private void paramax$prepass(Camera camera, Frustum frustum, DeltaTracker tickCounter,
                                 LevelRenderState renderStates, CallbackInfo ci) {
        this.paramax$visibility.clear();

        ParaMaxConfig cfg = ParaMaxConfig.get();
        if (!cfg.enabled || !cfg.parallelEntityVisibility) {
            return;
        }

        List<Entity> entities = new ArrayList<>(256);
        for (Entity entity : this.level.entitiesForRendering()) {
            entities.add(entity);
        }
        int n = entities.size();
        if (n < cfg.parallelEntityThreshold) {
            return;
        }

        Entity.setViewScale(
                Mth.clamp(this.minecraft.options.getEffectiveRenderDistance() / 8.0, 1.0, 2.5)
                        * this.minecraft.options.entityDistanceScaling().get());

        Vec3 cameraPos = camera.position();
        double x = cameraPos.x();
        double y = cameraPos.y();
        double z = cameraPos.z();

        boolean[] visible = new boolean[n];
        int workers = Math.max(1, ParallelEngine.workerCount());
        int stripe = (n + workers - 1) / workers;
        List<CompletableFuture<Void>> futures = new ArrayList<>(workers);

        for (int start = 0; start < n; start += stripe) {
            final int lo = start;
            final int hi = Math.min(start + stripe, n);
            futures.add(CompletableFuture.runAsync(() -> {
                for (int i = lo; i < hi; i++) {
                    try {
                        visible[i] = this.entityRenderDispatcher.shouldRender(entities.get(i), frustum, x, y, z);
                    } catch (Throwable t) {
                        visible[i] = true;
                    }
                }
            }, ParallelEngine::execute));
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        for (int i = 0; i < n; i++) {
            this.paramax$visibility.put(entities.get(i), visible[i]);
        }
    }

    @Redirect(method = "extractVisibleEntities",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/entity/EntityRenderDispatcher;shouldRender(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/client/renderer/culling/Frustum;DDD)Z"))
    private boolean paramax$lookup(EntityRenderDispatcher manager, Entity entity, Frustum frustum,
                                   double x, double y, double z) {
        Boolean cached = this.paramax$visibility.get(entity);
        return cached != null ? cached : manager.shouldRender(entity, frustum, x, y, z);
    }
}
