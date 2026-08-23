package dev.alienstoearth.paramax.mixin;

import dev.alienstoearth.paramax.governor.PerformanceGovernor;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class SpikeAnticipationMixin {

    @Unique
    private static final int BURST_PARTICLE_COUNT = 500;

    @Inject(method = "handleExplosion", at = @At("HEAD"))
    private void paramax$anticipateExplosion(ClientboundExplodePacket packet, CallbackInfo ci) {
        PerformanceGovernor.anticipate();
    }

    @Inject(method = "handleParticleEvent", at = @At("HEAD"))
    private void paramax$anticipateParticleBurst(ClientboundLevelParticlesPacket packet, CallbackInfo ci) {
        if (packet.getCount() >= BURST_PARTICLE_COUNT) {
            PerformanceGovernor.anticipate();
        }
    }
}
