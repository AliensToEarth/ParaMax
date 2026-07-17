package dev.alienstoearth.paramax.mixin;

import dev.alienstoearth.paramax.config.ParaMaxConfig;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Util.class)
public class WorkerThreadCountMixin {

    @Inject(method = "getMaxBackgroundThreads", at = @At("HEAD"), cancellable = true)
    private static void paramax$override(CallbackInfoReturnable<Integer> cir) {
        ParaMaxConfig cfg = ParaMaxConfig.get();
        if (!cfg.enabled || !cfg.tuneWorkerThreads || cfg.workerThreadCount <= 0) {
            return;
        }
        cir.setReturnValue(cfg.workerThreadCount);
    }
}
