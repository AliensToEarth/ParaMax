package dev.alienstoearth.paramax.mixin;

import dev.alienstoearth.paramax.governor.PerformanceGovernor;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class SpikeAnticipationMixin {

    @Unique
    private static final int BURST_PARTICLE_COUNT = 500;

    @Inject(method = "onExplosion", at = @At("HEAD"))
    private void paramax$anticipateExplosion(ExplosionS2CPacket packet, CallbackInfo ci) {
        PerformanceGovernor.anticipate();
    }

    @Inject(method = "onParticle", at = @At("HEAD"))
    private void paramax$anticipateParticleBurst(ParticleS2CPacket packet, CallbackInfo ci) {
        if (packet.getCount() >= BURST_PARTICLE_COUNT) {
            PerformanceGovernor.anticipate();
        }
    }
}
