package dev.alienstoearth.paramax.mixin;

import dev.alienstoearth.paramax.config.ParaMaxConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.world.attribute.EnvironmentAttributes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LightmapTextureManager.class)
public abstract class SmartLightmapMixin {

    @Shadow @Final private MinecraftClient client;
    @Shadow private boolean dirty;
    @Shadow private float flickerIntensity;

    @Unique private long paramax$lastFingerprint = Long.MIN_VALUE;

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void paramax$smartTick(CallbackInfo ci) {
        ParaMaxConfig cfg = ParaMaxConfig.get();
        if (!cfg.enabled || !cfg.smartLightmap) {
            return;
        }
        ci.cancel();
        this.flickerIntensity = 0.0F;

        ClientWorld world = this.client.world;
        ClientPlayerEntity player = this.client.player;
        if (world == null || player == null) {
            this.dirty = true;
            this.paramax$lastFingerprint = Long.MIN_VALUE;
            return;
        }

        boolean volatileState = player.hasStatusEffect(StatusEffects.NIGHT_VISION)
                || player.hasStatusEffect(StatusEffects.DARKNESS)
                || (player.getUnderwaterVisibility() > 0.0F && player.hasStatusEffect(StatusEffects.CONDUIT_POWER))
                || world.getEndLightFlashManager() != null;
        if (volatileState) {
            this.dirty = true;
            this.paramax$lastFingerprint = Long.MIN_VALUE;
            return;
        }

        Camera camera = this.client.gameRenderer.getCamera();
        int skyColor = camera.getEnvironmentAttributeInterpolator()
                .get(EnvironmentAttributes.SKY_LIGHT_COLOR_VISUAL, 1.0F);
        float skyFactor = camera.getEnvironmentAttributeInterpolator()
                .get(EnvironmentAttributes.SKY_LIGHT_FACTOR_VISUAL, 1.0F);

        long fingerprint = skyColor;
        fingerprint = fingerprint * 31 + Math.round(skyFactor * 1024.0F);
        fingerprint = fingerprint * 31 + Float.floatToIntBits(world.getDimension().ambientLight());
        fingerprint = fingerprint * 31 + Math.round(this.client.options.getGamma().getValue().floatValue() * 1024.0F);
        fingerprint = fingerprint * 31 + Math.round(player.getUnderwaterVisibility() * 64.0F);

        if (fingerprint != this.paramax$lastFingerprint) {
            this.paramax$lastFingerprint = fingerprint;
            this.dirty = true;
        }
    }
}
