package dev.alienstoearth.paramax.mixin;

import dev.alienstoearth.paramax.config.ParaMaxConfig;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientWorld.class)
public abstract class CosmeticEntityTickMixin {

    @Inject(method = "tickEntity", at = @At("HEAD"), cancellable = true)
    private void paramax$halfTickCosmetics(Entity entity, CallbackInfo ci) {
        ParaMaxConfig cfg = ParaMaxConfig.get();
        if (!cfg.enabled || !cfg.reduceCosmeticEntityTicks) {
            return;
        }

        EntityType<?> type = entity.getType();
        if (type != EntityType.PAINTING
                && type != EntityType.ITEM_FRAME
                && type != EntityType.GLOW_ITEM_FRAME
                && type != EntityType.LEASH_KNOT) {
            return;
        }

        ClientWorld world = (ClientWorld) (Object) this;
        if (((world.getTime() + entity.getId()) & 1L) == 1L) {
            ci.cancel();
        }
    }
}
