package dev.alienstoearth.paramax.mixin;

import dev.alienstoearth.paramax.config.ParaMaxConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.network.PlayerListEntry;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Comparator;
import java.util.List;

@Mixin(PlayerListHud.class)
public abstract class PlayerListCacheMixin {

    @Shadow @Final private MinecraftClient client;
    @Shadow @Final private static Comparator<PlayerListEntry> ENTRY_ORDERING;

    @Unique private List<PlayerListEntry> paramax$cachedEntries;
    @Unique private long paramax$lastBuildMs;

    @Inject(method = "collectPlayerEntries", at = @At("HEAD"), cancellable = true)
    private void paramax$cachedCollect(CallbackInfoReturnable<List<PlayerListEntry>> cir) {
        ParaMaxConfig cfg = ParaMaxConfig.get();
        if (!cfg.enabled || !cfg.cacheHudText) {
            return;
        }
        long now = System.currentTimeMillis();
        if (this.paramax$cachedEntries == null || now - this.paramax$lastBuildMs >= cfg.hudCacheIntervalMs) {

            this.paramax$cachedEntries = this.client.player.networkHandler.getListedPlayerListEntries()
                    .stream()
                    .sorted(ENTRY_ORDERING)
                    .limit(80L)
                    .toList();
            this.paramax$lastBuildMs = now;
        }
        cir.setReturnValue(this.paramax$cachedEntries);
    }
}
