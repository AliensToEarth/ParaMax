package dev.alienstoearth.paramax.mixin;

import dev.alienstoearth.paramax.config.ParaMaxConfig;
import dev.alienstoearth.paramax.parallel.ParallelEngine;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.entity.EntityRenderManager;
import net.minecraft.client.render.state.WorldRenderState;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
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

@Mixin(WorldRenderer.class)
public abstract class ParallelEntityVisibilityMixin {

    @Shadow @Final private MinecraftClient client;
    @Shadow @Final private EntityRenderManager entityRenderManager;
    @Shadow private ClientWorld world;

    @Unique
    private final Map<Entity, Boolean> paramax$visibility = new IdentityHashMap<>();

    @Inject(method = "fillEntityRenderStates", at = @At("HEAD"))
    private void paramax$prepass(Camera camera, Frustum frustum, RenderTickCounter tickCounter,
                                 WorldRenderState renderStates, CallbackInfo ci) {
        this.paramax$visibility.clear();

        ParaMaxConfig cfg = ParaMaxConfig.get();
        if (!cfg.enabled || !cfg.parallelEntityVisibility) {
            return;
        }

        List<Entity> entities = new ArrayList<>(256);
        for (Entity entity : this.world.getEntities()) {
            entities.add(entity);
        }
        int n = entities.size();
        if (n < cfg.parallelEntityThreshold) {
            return;
        }

        Entity.setRenderDistanceMultiplier(
                MathHelper.clamp(this.client.options.getClampedViewDistance() / 8.0, 1.0, 2.5)
                        * this.client.options.getEntityDistanceScaling().getValue());

        Vec3d cameraPos = camera.getCameraPos();
        double x = cameraPos.getX();
        double y = cameraPos.getY();
        double z = cameraPos.getZ();

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
                        visible[i] = this.entityRenderManager.shouldRender(entities.get(i), frustum, x, y, z);
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

    @Redirect(method = "fillEntityRenderStates",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/render/entity/EntityRenderManager;shouldRender(Lnet/minecraft/entity/Entity;Lnet/minecraft/client/render/Frustum;DDD)Z"))
    private boolean paramax$lookup(EntityRenderManager manager, Entity entity, Frustum frustum,
                                   double x, double y, double z) {
        Boolean cached = this.paramax$visibility.get(entity);
        return cached != null ? cached : manager.shouldRender(entity, frustum, x, y, z);
    }
}
