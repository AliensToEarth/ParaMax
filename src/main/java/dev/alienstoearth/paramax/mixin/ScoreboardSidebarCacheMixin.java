package dev.alienstoearth.paramax.mixin;

import dev.alienstoearth.paramax.config.ParaMaxConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardEntry;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.Team;
import net.minecraft.scoreboard.number.NumberFormat;
import net.minecraft.scoreboard.number.StyledNumberFormat;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
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

@Mixin(InGameHud.class)
public abstract class   ScoreboardSidebarCacheMixin {

    @Shadow @Final private MinecraftClient client;
    @Shadow @Final private static Comparator<ScoreboardEntry> SCOREBOARD_ENTRY_COMPARATOR;

    @Shadow public abstract TextRenderer getTextRenderer();

    @Unique private record ParamaxSidebarLine(Text name, Text score, int scoreWidth) {
    }

    @Unique private ScoreboardObjective paramax$cachedObjective;
    @Unique private long paramax$lastBuildMs;
    @Unique private List<ParamaxSidebarLine> paramax$lines;
    @Unique private Text paramax$title;
    @Unique private int paramax$titleWidth;
    @Unique private int paramax$maxWidth;

    @Inject(method = "renderScoreboardSidebar(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/scoreboard/ScoreboardObjective;)V",
            at = @At("HEAD"), cancellable = true)
    private void paramax$cachedSidebar(DrawContext context, ScoreboardObjective objective, CallbackInfo ci) {
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
        int bottom = context.getScaledWindowHeight() / 2 + height / 3;
        int left = context.getScaledWindowWidth() - maxWidth - 3;
        int right = context.getScaledWindowWidth() - 3 + 2;
        int bodyColor = this.client.options.getTextBackgroundColor(0.3F);
        int titleColor = this.client.options.getTextBackgroundColor(0.4F);
        int top = bottom - count * 9;
        context.fill(left - 2, top - 9 - 1, right, top - 1, titleColor);
        context.fill(left - 2, top - 1, right, bottom, bodyColor);
        context.drawText(this.getTextRenderer(), this.paramax$title,
                left + maxWidth / 2 - this.paramax$titleWidth / 2, top - 9, Colors.WHITE, false);

        for (int i = 0; i < count; i++) {
            ParamaxSidebarLine line = lines.get(i);
            int y = bottom - (count - i) * 9;
            context.drawText(this.getTextRenderer(), line.name(), left, y, Colors.WHITE, false);
            context.drawText(this.getTextRenderer(), line.score(),
                    right - line.scoreWidth(), y, Colors.WHITE, false);
        }
    }

    @Unique
    private void paramax$rebuild(ScoreboardObjective objective) {
        Scoreboard scoreboard = objective.getScoreboard();
        NumberFormat numberFormat = objective.getNumberFormatOr(StyledNumberFormat.RED);

        List<ParamaxSidebarLine> lines = new ArrayList<>(15);
        scoreboard.getScoreboardEntries(objective)
                .stream()
                .filter(score -> !score.hidden())
                .sorted(SCOREBOARD_ENTRY_COMPARATOR)
                .limit(15L)
                .forEach(entry -> {
                    Team team = scoreboard.getScoreHolderTeam(entry.owner());
                    Text name = Team.decorateName(team, entry.name());
                    Text score = entry.formatted(numberFormat);
                    lines.add(new ParamaxSidebarLine(name, score, this.getTextRenderer().getWidth(score)));
                });

        Text title = objective.getDisplayName();
        int titleWidth = this.getTextRenderer().getWidth(title);
        int maxWidth = titleWidth;
        int separatorWidth = this.getTextRenderer().getWidth(": ");
        for (ParamaxSidebarLine line : lines) {
            maxWidth = Math.max(maxWidth, this.getTextRenderer().getWidth(line.name())
                    + (line.scoreWidth() > 0 ? separatorWidth + line.scoreWidth() : 0));
        }

        this.paramax$lines = lines;
        this.paramax$title = title;
        this.paramax$titleWidth = titleWidth;
        this.paramax$maxWidth = maxWidth;
    }
}
