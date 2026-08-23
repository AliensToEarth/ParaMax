package dev.alienstoearth.paramax.mixin;

import dev.alienstoearth.paramax.config.ParaMaxConfig;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.numbers.NumberFormat;
import net.minecraft.network.chat.numbers.StyledFormat;
import net.minecraft.util.CommonColors;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerScoreEntry;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;

@Mixin(Hud.class)
public abstract class   ScoreboardSidebarCacheMixin {

    @Shadow @Final private Minecraft minecraft;
    @Shadow @Final private static Comparator<PlayerScoreEntry> SCORE_DISPLAY_ORDER;

    @Shadow public abstract Font getFont();

    @Unique private record ParamaxSidebarLine(Component name, Component score, int scoreWidth) {
    }

    @Unique private Objective paramax$cachedObjective;
    @Unique private long paramax$lastBuildMs;
    @Unique private List<ParamaxSidebarLine> paramax$lines;
    @Unique private Component paramax$title;
    @Unique private int paramax$titleWidth;
    @Unique private int paramax$maxWidth;

    @Inject(method = "displayScoreboardSidebar(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/world/scores/Objective;)V",
            at = @At("HEAD"), cancellable = true)
    private void paramax$cachedSidebar(GuiGraphicsExtractor context, Objective objective, CallbackInfo ci) {
        ParaMaxConfig cfg = ParaMaxConfig.get();
        if (!cfg.enabled || !cfg.cacheHudText) {
            return;
        }
        ci.cancel();

        long now = System.currentTimeMillis();
        if (this.paramax$lines == null
                || this.paramax$cachedObjective != objective
                || now - this.paramax$lastBuildMs >= cfg.hudCacheIntervalMs) {
            this.paramax$rebuild(objective);
            this.paramax$cachedObjective = objective;
            this.paramax$lastBuildMs = now;
        }

        List<ParamaxSidebarLine> lines = this.paramax$lines;
        int maxWidth = this.paramax$maxWidth;
        int count = lines.size();
        int height = count * 9;
        int bottom = context.guiHeight() / 2 + height / 3;
        int left = context.guiWidth() - maxWidth - 3;
        int right = context.guiWidth() - 3 + 2;
        int bodyColor = this.minecraft.options.getBackgroundColor(0.3F);
        int titleColor = this.minecraft.options.getBackgroundColor(0.4F);
        int top = bottom - count * 9;
        context.fill(left - 2, top - 9 - 1, right, top - 1, titleColor);
        context.fill(left - 2, top - 1, right, bottom, bodyColor);
        context.text(this.getFont(), this.paramax$title,
                left + maxWidth / 2 - this.paramax$titleWidth / 2, top - 9, CommonColors.WHITE, false);

        for (int i = 0; i < count; i++) {
            ParamaxSidebarLine line = lines.get(i);
            int y = bottom - (count - i) * 9;
            context.text(this.getFont(), line.name(), left, y, CommonColors.WHITE, false);
            context.text(this.getFont(), line.score(),
                    right - line.scoreWidth(), y, CommonColors.WHITE, false);
        }
    }

    @Unique
    private void paramax$rebuild(Objective objective) {
        Scoreboard scoreboard = objective.getScoreboard();
        NumberFormat numberFormat = objective.numberFormatOrDefault(StyledFormat.SIDEBAR_DEFAULT);

        List<ParamaxSidebarLine> lines = new ArrayList<>(15);
        scoreboard.listPlayerScores(objective)
                .stream()
                .filter(score -> !score.isHidden())
                .sorted(SCORE_DISPLAY_ORDER)
                .limit(15L)
                .forEach(entry -> {
                    PlayerTeam team = scoreboard.getPlayersTeam(entry.owner());
                    Component name = PlayerTeam.formatNameForTeam(team, entry.ownerName());
                    Component score = entry.formatValue(numberFormat);
                    lines.add(new ParamaxSidebarLine(name, score, this.getFont().width(score)));
                });

        Component title = objective.getDisplayName();
        int titleWidth = this.getFont().width(title);
        int maxWidth = titleWidth;
        int separatorWidth = this.getFont().width(": ");
        for (ParamaxSidebarLine line : lines) {
            maxWidth = Math.max(maxWidth, this.getFont().width(line.name())
                    + (line.scoreWidth() > 0 ? separatorWidth + line.scoreWidth() : 0));
        }

        this.paramax$lines = lines;
        this.paramax$title = title;
        this.paramax$titleWidth = titleWidth;
        this.paramax$maxWidth = maxWidth;
    }
}
