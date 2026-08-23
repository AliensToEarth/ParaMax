package dev.alienstoearth.paramax.mixin;

import dev.alienstoearth.paramax.config.ParaMaxConfig;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.effect.MobEffects;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LightTexture.class)
public abstract class SmartLightmapMixin {

    @Shadow @Final private Minecraft minecraft;
    @Shadow private boolean updateLightTexture;
    @Shadow private float blockLightRedFlicker;

    @Unique private long paramax$lastFingerprint = Long.MIN_VALUE;

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void paramax$smartTick(CallbackInfo ci) {
        ParaMaxConfig cfg = ParaMaxConfig.get();
        if (!cfg.enabled || !cfg.smartLightmap) {
            return;
        }
        ci.cancel();
        this.blockLightRedFlicker = 0.0F;

        ClientLevel world = this.minecraft.level;
        LocalPlayer player = this.minecraft.player;
        if (world == null || player == null) {
            this.updateLightTexture = true;
            this.paramax$lastFingerprint = Long.MIN_VALUE;
            return;
        }

        boolean volatileState = player.hasEffect(MobEffects.NIGHT_VISION)
                || player.hasEffect(MobEffects.DARKNESS)
                || (player.getWaterVision() > 0.0F && player.hasEffect(MobEffects.CONDUIT_POWER))
                || world.endFlashState() != null;
        if (volatileState) {
            this.updateLightTexture = true;
            this.paramax$lastFingerprint = Long.MIN_VALUE;
            return;
        }

        Camera camera = this.minecraft.gameRenderer.getMainCamera();
        int skyColor = camera.attributeProbe()
                .getValue(EnvironmentAttributes.SKY_LIGHT_COLOR, 1.0F);
        float skyFactor = camera.attributeProbe()
                .getValue(EnvironmentAttributes.SKY_LIGHT_FACTOR, 1.0F);

        long fingerprint = skyColor;
        fingerprint = fingerprint * 31 + Math.round(skyFactor * 1024.0F);
        fingerprint = fingerprint * 31 + Float.floatToIntBits(world.dimensionType().ambientLight());
        fingerprint = fingerprint * 31 + Math.round(this.minecraft.options.gamma().get().floatValue() * 1024.0F);
        fingerprint = fingerprint * 31 + Math.round(player.getWaterVision() * 64.0F);

        if (fingerprint != this.paramax$lastFingerprint) {
            this.paramax$lastFingerprint = fingerprint;
            this.updateLightTexture = true;
        }
    }
}
