package dev.alienstoearth.paramax.mixin;

import dev.alienstoearth.paramax.config.ParaMaxConfig;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Comparator;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;

@Mixin(PlayerTabOverlay.class)
public abstract class PlayerListCacheMixin {

    @Shadow @Final private Minecraft minecraft;
    @Shadow @Final private static Comparator<PlayerInfo> PLAYER_COMPARATOR;

    @Unique private List<PlayerInfo> paramax$cachedEntries;
    @Unique private long paramax$lastBuildMs;

    @Inject(method = "getPlayerInfos", at = @At("HEAD"), cancellable = true)
    private void paramax$cachedCollect(CallbackInfoReturnable<List<PlayerInfo>> cir) {
        ParaMaxConfig cfg = ParaMaxConfig.get();
        if (!cfg.enabled || !cfg.cacheHudText) {
            return;
        }
        long now = System.currentTimeMillis();
        if (this.paramax$cachedEntries == null || now - this.paramax$lastBuildMs >= cfg.hudCacheIntervalMs) {

            this.paramax$cachedEntries = this.minecraft.player.connection.getListedOnlinePlayers()
                    .stream()
                    .sorted(PLAYER_COMPARATOR)
                    .limit(80L)
                    .toList();
            this.paramax$lastBuildMs = now;
        }
        cir.setReturnValue(this.paramax$cachedEntries);
    }
}
