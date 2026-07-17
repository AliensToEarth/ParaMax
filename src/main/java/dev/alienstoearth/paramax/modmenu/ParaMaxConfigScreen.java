package dev.alienstoearth.paramax.modmenu;

import dev.alienstoearth.paramax.config.ParaMaxConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public final class ParaMaxConfigScreen extends Screen {

    private record Toggle(String label, Function<ParaMaxConfig, Boolean> getter,
                          Consumer<ParaMaxConfig> flipper) {
        static Toggle of(String label, Function<ParaMaxConfig, Boolean> getter,
                         Consumer<ParaMaxConfig> flipper) {
            return new Toggle(label, getter, flipper);
        }
    }

    private static final List<Toggle> TOGGLES = List.of(
            Toggle.of("ParaMax Enabled", c -> c.enabled, c -> c.enabled = !c.enabled),
            Toggle.of("Worker Thread Override", c -> c.tuneWorkerThreads, c -> c.tuneWorkerThreads = !c.tuneWorkerThreads),
            Toggle.of("Dynamic FPS (Unfocused)", c -> c.dynamicFps, c -> c.dynamicFps = !c.dynamicFps),
            Toggle.of("Menu FPS Cap", c -> c.throttleMenus, c -> c.throttleMenus = !c.throttleMenus),
            Toggle.of("Parallel Particles", c -> c.parallelParticles, c -> c.parallelParticles = !c.parallelParticles),
            Toggle.of("Parallel Block Entity States", c -> c.parallelBlockEntityStates, c -> c.parallelBlockEntityStates = !c.parallelBlockEntityStates),
            Toggle.of("Entity Distance Culling", c -> c.entityDistanceCulling, c -> c.entityDistanceCulling = !c.entityDistanceCulling),
            Toggle.of("Block Entity Distance Culling", c -> c.blockEntityDistanceCulling, c -> c.blockEntityDistanceCulling = !c.blockEntityDistanceCulling),
            Toggle.of("Particle Throttling", c -> c.throttleParticles, c -> c.throttleParticles = !c.throttleParticles),
            Toggle.of("Skip Weather Rendering", c -> c.skipWeatherRendering, c -> c.skipWeatherRendering = !c.skipWeatherRendering),
            Toggle.of("Cache F3 Debug Text", c -> c.throttleDebugHud, c -> c.throttleDebugHud = !c.throttleDebugHud),
            Toggle.of("Half-Rate Texture Animations", c -> c.halfRateTextureAnimations, c -> c.halfRateTextureAnimations = !c.halfRateTextureAnimations),
            Toggle.of("Reduce Cosmetic Entity Ticks", c -> c.reduceCosmeticEntityTicks, c -> c.reduceCosmeticEntityTicks = !c.reduceCosmeticEntityTicks),
            Toggle.of("Parallel Entity Visibility", c -> c.parallelEntityVisibility, c -> c.parallelEntityVisibility = !c.parallelEntityVisibility),
            Toggle.of("Smart Lightmap (No Flicker)", c -> c.smartLightmap, c -> c.smartLightmap = !c.smartLightmap),
            Toggle.of("Cache HUD Text", c -> c.cacheHudText, c -> c.cacheHudText = !c.cacheHudText),
            Toggle.of("Pool Block Entity States", c -> c.poolBlockEntityStates, c -> c.poolBlockEntityStates = !c.poolBlockEntityStates),
            Toggle.of("Adaptive FPS Governor", c -> c.adaptivePerformance, c -> c.adaptivePerformance = !c.adaptivePerformance),
            Toggle.of("Frame Pacing (Smoothing)", c -> c.framePacing, c -> c.framePacing = !c.framePacing),
            Toggle.of("Particle Distance Culling", c -> c.particleCulling, c -> c.particleCulling = !c.particleCulling),
            Toggle.of("Pool Entity States", c -> c.poolEntityStates, c -> c.poolEntityStates = !c.poolEntityStates),
            Toggle.of("Temporal Entity LOD", c -> c.temporalEntityLod, c -> c.temporalEntityLod = !c.temporalEntityLod),
            Toggle.of("Governor Anticipation", c -> c.anticipateSpikes, c -> c.anticipateSpikes = !c.anticipateSpikes),
            Toggle.of("Particle Spawn Budget", c -> c.budgetParticleSpawns, c -> c.budgetParticleSpawns = !c.budgetParticleSpawns)
    );

    private static final int PAGE_TOGGLES = 0;
    private static final int PAGE_NUMBERS = 1;

    private final Screen parent;
    private int page = PAGE_TOGGLES;

    public ParaMaxConfigScreen(Screen parent) {
        super(Text.literal("ParaMax Options"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int buttonHeight = 20;
        int gap = 4;

        int itemCount = this.page == PAGE_TOGGLES ? TOGGLES.size() : 18;
        int availableRows = Math.max(1, (this.height - 36 - 40) / (buttonHeight + gap));
        int columns = Math.max(2, (itemCount + availableRows - 1) / availableRows);
        int buttonWidth = Math.min(180, (this.width - 20 - gap * (columns - 1)) / columns);

        List<ClickableWidget> widgets = this.page == PAGE_TOGGLES
                ? this.buildToggles(buttonWidth, buttonHeight)
                : this.buildSliders(buttonWidth, buttonHeight);

        int gridWidth = columns * buttonWidth + (columns - 1) * gap;
        int startX = this.width / 2 - gridWidth / 2;

        for (int i = 0; i < widgets.size(); i++) {
            ClickableWidget widget = widgets.get(i);
            widget.setX(startX + (i % columns) * (buttonWidth + gap));
            widget.setY(36 + (i / columns) * (buttonHeight + gap));
            this.addDrawableChild(widget);
        }

        String otherPage = this.page == PAGE_TOGGLES ? "Numeric Tuning >" : "< Feature Toggles";
        this.addDrawableChild(ButtonWidget.builder(Text.literal(otherPage), button -> {
                    this.page = this.page == PAGE_TOGGLES ? PAGE_NUMBERS : PAGE_TOGGLES;
                    this.clearAndInit();
                })
                .dimensions(this.width / 2 - 154, this.height - 27, 150, 20)
                .build());
        this.addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, button -> this.close())
                .dimensions(this.width / 2 + 4, this.height - 27, 150, 20)
                .build());
    }

    private List<ClickableWidget> buildToggles(int w, int h) {
        ParaMaxConfig cfg = ParaMaxConfig.get();
        return TOGGLES.stream()
                .<ClickableWidget>map(toggle -> CyclingButtonWidget.onOffBuilder(toggle.getter().apply(cfg))
                        .build(0, 0, w, h, Text.literal(toggle.label()),
                                (button, value) -> toggle.flipper().accept(ParaMaxConfig.get())))
                .toList();
    }

    private List<ClickableWidget> buildSliders(int w, int h) {
        ParaMaxConfig cfg = ParaMaxConfig.get();
        return List.of(
                new ParaMaxSlider(0, 0, w, h, "Target FPS (Governor)", 20, 240, 5, ParaMaxSlider.Format.FPS,
                        () -> cfg.targetFps, v -> cfg.targetFps = (int) v),
                new ParaMaxSlider(0, 0, w, h, "Unfocused FPS", 1, 60, 1, ParaMaxSlider.Format.FPS,
                        () -> cfg.unfocusedFps, v -> cfg.unfocusedFps = (int) v),
                new ParaMaxSlider(0, 0, w, h, "Menu FPS", 5, 120, 5, ParaMaxSlider.Format.FPS,
                        () -> cfg.menuFps, v -> cfg.menuFps = (int) v),
                new ParaMaxSlider(0, 0, w, h, "Particles Kept", 0.0, 1.0, 0.05, ParaMaxSlider.Format.PERCENT,
                        () -> cfg.particleMultiplier, v -> cfg.particleMultiplier = v),
                new ParaMaxSlider(0, 0, w, h, "Entity Cull Distance", 8, 192, 8, ParaMaxSlider.Format.BLOCKS,
                        () -> cfg.maxEntityRenderDistance, v -> cfg.maxEntityRenderDistance = v),
                new ParaMaxSlider(0, 0, w, h, "Block Entity Cull Distance", 8, 192, 8, ParaMaxSlider.Format.BLOCKS,
                        () -> cfg.maxBlockEntityRenderDistance, v -> cfg.maxBlockEntityRenderDistance = v),
                new ParaMaxSlider(0, 0, w, h, "Parallel Particle Threshold", 64, 4096, 64, ParaMaxSlider.Format.INT,
                        () -> cfg.parallelParticleThreshold, v -> cfg.parallelParticleThreshold = (int) v),
                new ParaMaxSlider(0, 0, w, h, "Parallel Block Entity Threshold", 16, 1024, 16, ParaMaxSlider.Format.INT,
                        () -> cfg.parallelBlockEntityThreshold, v -> cfg.parallelBlockEntityThreshold = (int) v),
                new ParaMaxSlider(0, 0, w, h, "Parallel Entity Threshold", 32, 1024, 32, ParaMaxSlider.Format.INT,
                        () -> cfg.parallelEntityThreshold, v -> cfg.parallelEntityThreshold = (int) v),
                new ParaMaxSlider(0, 0, w, h, "F3 Rebuild Interval", 16, 1000, 16, ParaMaxSlider.Format.MILLISECONDS,
                        () -> cfg.debugHudIntervalMs, v -> cfg.debugHudIntervalMs = (int) v),
                new ParaMaxSlider(0, 0, w, h, "HUD Rebuild Interval", 50, 2000, 50, ParaMaxSlider.Format.MILLISECONDS,
                        () -> cfg.hudCacheIntervalMs, v -> cfg.hudCacheIntervalMs = (int) v),
                new ParaMaxSlider(0, 0, w, h, "Worker Threads", 0, 32, 1, ParaMaxSlider.Format.INT_OR_AUTO,
                        () -> cfg.workerThreadCount, v -> cfg.workerThreadCount = (int) v),
                new ParaMaxSlider(0, 0, w, h, "Governor Base Pressure", 0, 4, 1, ParaMaxSlider.Format.INT,
                        () -> cfg.governorBasePressure, v -> cfg.governorBasePressure = (int) v),
                new ParaMaxSlider(0, 0, w, h, "Pacing Min FPS", 10, 120, 5, ParaMaxSlider.Format.FPS,
                        () -> cfg.pacingMinFps, v -> cfg.pacingMinFps = (int) v),
                new ParaMaxSlider(0, 0, w, h, "Particle Cull Distance", 8, 256, 8, ParaMaxSlider.Format.BLOCKS,
                        () -> cfg.maxParticleDistance, v -> cfg.maxParticleDistance = v),
                new ParaMaxSlider(0, 0, w, h, "LOD Near Distance", 8, 64, 4, ParaMaxSlider.Format.BLOCKS,
                        () -> cfg.lodNearDistance, v -> cfg.lodNearDistance = v),
                new ParaMaxSlider(0, 0, w, h, "LOD Max Interval", 1, 8, 1, ParaMaxSlider.Format.INT,
                        () -> cfg.lodMaxInterval, v -> cfg.lodMaxInterval = (int) v),
                new ParaMaxSlider(0, 0, w, h, "Particle Spawn Budget", 250, 8000, 250, ParaMaxSlider.Format.INT,
                        () -> cfg.particleSpawnBudget, v -> cfg.particleSpawnBudget = (int) v)
        );
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        super.render(context, mouseX, mouseY, deltaTicks);
        String pageName = this.page == PAGE_TOGGLES ? "Feature Toggles" : "Numeric Tuning";
        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("ParaMax Options - " + pageName), this.width / 2, 15, 0xFFFFFFFF);
    }

    @Override
    public void close() {
        ParaMaxConfig.get().save();
        this.client.setScreen(this.parent);
    }
}
