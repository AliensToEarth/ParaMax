package dev.alienstoearth.paramax.mixin;

import dev.alienstoearth.paramax.config.ParaMaxConfig;
import dev.alienstoearth.paramax.parallel.ParallelEngine;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRenderManager;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.client.render.state.WorldRenderState;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.BlockBreakingInfo;
import net.minecraft.util.Util;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

@Mixin(WorldRenderer.class)
public abstract class ParallelBlockEntityStateMixin {

    @Shadow @Final private ObjectArrayList<ChunkBuilder.BuiltChunk> builtChunks;
    @Shadow @Final private Long2ObjectMap<SortedSet<BlockBreakingInfo>> blockBreakingProgressions;
    @Shadow @Final private BlockEntityRenderManager blockEntityRenderManager;
    @Shadow private ClientWorld world;

    @Inject(method = "fillBlockEntityRenderStates", at = @At("HEAD"), cancellable = true)
    private void paramax$parallelFill(Camera camera, float tickProgress, WorldRenderState renderStates,
                                      CallbackInfo ci) {
        ParaMaxConfig cfg = ParaMaxConfig.get();
        if (!cfg.enabled || !cfg.parallelBlockEntityStates) {
            return;
        }
        if (!this.blockBreakingProgressions.isEmpty()) {
            return;
        }

        long now = Util.getMeasuringTimeMs();
        List<BlockEntity> candidates = new ArrayList<>(256);
        for (ChunkBuilder.BuiltChunk chunk : this.builtChunks) {
            List<BlockEntity> chunkBlockEntities = chunk.getCurrentRenderData().getBlockEntities();
            if (!chunkBlockEntities.isEmpty() && !(chunk.method_76298(now) < 0.3F)) {
                candidates.addAll(chunkBlockEntities);
            }
        }
        Iterator<BlockEntity> it = this.world.getBlockEntities().iterator();
        while (it.hasNext()) {
            BlockEntity blockEntity = it.next();
            if (blockEntity.isRemoved()) {
                it.remove();
            } else {
                candidates.add(blockEntity);
            }
        }

        int n = candidates.size();
        if (n < cfg.parallelBlockEntityThreshold) {
            return;
        }
        ci.cancel();

        BlockEntityRenderState[] out = new BlockEntityRenderState[n];
        int workers = Math.max(1, ParallelEngine.workerCount());
        int stripe = (n + workers - 1) / workers;
        AtomicReference<Throwable> failure = new AtomicReference<>();
        List<CompletableFuture<Void>> futures = new ArrayList<>(workers);

        for (int start = 0; start < n; start += stripe) {
            final int lo = start;
            final int hi = Math.min(start + stripe, n);
            futures.add(CompletableFuture.runAsync(() -> {
                for (int i = lo; i < hi; i++) {
                    if (failure.get() != null) {
                        return;
                    }
                    try {
                        out[i] = this.blockEntityRenderManager.getRenderState(candidates.get(i), tickProgress, null);
                    } catch (Throwable t) {
                        failure.compareAndSet(null, t);
                        return;
                    }
                }
            }, ParallelEngine::execute));
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        Throwable crash = failure.get();
        if (crash != null) {
            throw new CrashException(
                    CrashReport.create(crash, "Building block entity render state (ParaMax parallel)"));
        }

        for (BlockEntityRenderState state : out) {
            if (state != null) {
                renderStates.blockEntityRenderStates.add(state);
            }
        }
    }
}
