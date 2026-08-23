package dev.alienstoearth.paramax.mixin;

import dev.alienstoearth.paramax.config.ParaMaxConfig;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientLevel.class)
public abstract class CosmeticEntityTickMixin {

    @Inject(method = "tickNonPassenger", at = @At("HEAD"), cancellable = true)
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

        ClientLevel world = (ClientLevel) (Object) this;
        if (((world.getGameTime() + entity.getId()) & 1L) == 1L) {
            ci.cancel();
        }
    }
}
