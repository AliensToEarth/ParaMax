package dev.alienstoearth.paramax.mixin;

import dev.alienstoearth.paramax.config.ParaMaxConfig;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.block.entity.BlockEntityRenderManager;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Mixin(BlockEntityRenderManager.class)
public abstract class BlockEntityStatePoolMixin {

    @Unique
    private final Map<BlockEntityRenderer<?, ?>, Queue<BlockEntityRenderState>> paramax$available =
            new ConcurrentHashMap<>();
    @Unique
    private final Queue<PooledState> paramax$inFlight = new ConcurrentLinkedQueue<>();

    @Unique
    private record PooledState(BlockEntityRenderer<?, ?> renderer, BlockEntityRenderState state) {
    }

    @Inject(method = "configure", at = @At("TAIL"))
    private void paramax$recycle(Camera camera, CallbackInfo ci) {
        ParaMaxConfig cfg = ParaMaxConfig.get();
        if (!cfg.enabled || !cfg.poolBlockEntityStates) {
            if (!this.paramax$inFlight.isEmpty() || !this.paramax$available.isEmpty()) {
                this.paramax$inFlight.clear();
                this.paramax$available.clear();
            }
            return;
        }
        PooledState pooled;
        while ((pooled = this.paramax$inFlight.poll()) != null) {
            this.paramax$available
                    .computeIfAbsent(pooled.renderer(), r -> new ConcurrentLinkedQueue<>())
                    .add(pooled.state());
        }
    }

    @Redirect(method = "getRenderState",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/render/block/entity/BlockEntityRenderer;createRenderState()Lnet/minecraft/client/render/block/entity/state/BlockEntityRenderState;"))
    private BlockEntityRenderState paramax$pooledCreate(BlockEntityRenderer<?, ?> renderer) {
        ParaMaxConfig cfg = ParaMaxConfig.get();
        if (!cfg.enabled || !cfg.poolBlockEntityStates) {
            return renderer.createRenderState();
        }

        Queue<BlockEntityRenderState> pool = this.paramax$available.get(renderer);
        BlockEntityRenderState state = pool != null ? pool.poll() : null;
        if (state == null) {
            state = renderer.createRenderState();
        }
        this.paramax$inFlight.add(new PooledState(renderer, state));
        return state;
    }
}
