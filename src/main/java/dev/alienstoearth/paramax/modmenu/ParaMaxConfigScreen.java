package dev.alienstoearth.paramax.modmenu;

import dev.alienstoearth.paramax.config.ParaMaxConfig;
import dev.alienstoearth.paramax.config.ParaMaxPreset;
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

    private record Toggle(String key, Function<ParaMaxConfig, Boolean> getter,
                          Consumer<ParaMaxConfig> flipper) {
        static Toggle of(String key, Function<ParaMaxConfig, Boolean> getter,
                         Consumer<ParaMaxConfig> flipper) {
            return new Toggle(key, getter, flipper);
        }
    }

    private static final List<Toggle> TOGGLES = List.of(
            Toggle.of("paramax.toggle.enabled", c -> c.enabled, c -> c.enabled = !c.enabled),
            Toggle.of("paramax.toggle.worker_threads", c -> c.tuneWorkerThreads, c -> c.tuneWorkerThreads = !c.tuneWorkerThreads),
            Toggle.of("paramax.toggle.dynamic_fps", c -> c.dynamicFps, c -> c.dynamicFps = !c.dynamicFps),
            Toggle.of("paramax.toggle.menu_fps_cap", c -> c.throttleMenus, c -> c.throttleMenus = !c.throttleMenus),
            Toggle.of("paramax.toggle.parallel_particles", c -> c.parallelParticles, c -> c.parallelParticles = !c.parallelParticles),
            Toggle.of("paramax.toggle.parallel_block_entity_states", c -> c.parallelBlockEntityStates, c -> c.parallelBlockEntityStates = !c.parallelBlockEntityStates),
            Toggle.of("paramax.toggle.entity_distance_culling", c -> c.entityDistanceCulling, c -> c.entityDistanceCulling = !c.entityDistanceCulling),
            Toggle.of("paramax.toggle.block_entity_distance_culling", c -> c.blockEntityDistanceCulling, c -> c.blockEntityDistanceCulling = !c.blockEntityDistanceCulling),
            Toggle.of("paramax.toggle.particle_throttling", c -> c.throttleParticles, c -> c.throttleParticles = !c.throttleParticles),
            Toggle.of("paramax.toggle.skip_weather_rendering", c -> c.skipWeatherRendering, c -> c.skipWeatherRendering = !c.skipWeatherRendering),
            Toggle.of("paramax.toggle.cache_debug_text", c -> c.throttleDebugHud, c -> c.throttleDebugHud = !c.throttleDebugHud),
            Toggle.of("paramax.toggle.half_rate_texture_animations", c -> c.halfRateTextureAnimations, c -> c.halfRateTextureAnimations = !c.halfRateTextureAnimations),
            Toggle.of("paramax.toggle.reduce_cosmetic_entity_ticks", c -> c.reduceCosmeticEntityTicks, c -> c.reduceCosmeticEntityTicks = !c.reduceCosmeticEntityTicks),
            Toggle.of("paramax.toggle.parallel_entity_visibility", c -> c.parallelEntityVisibility, c -> c.parallelEntityVisibility = !c.parallelEntityVisibility),
            Toggle.of("paramax.toggle.smart_lightmap", c -> c.smartLightmap, c -> c.smartLightmap = !c.smartLightmap),
            Toggle.of("paramax.toggle.cache_hud_text", c -> c.cacheHudText, c -> c.cacheHudText = !c.cacheHudText),
            Toggle.of("paramax.toggle.pool_block_entity_states", c -> c.poolBlockEntityStates, c -> c.poolBlockEntityStates = !c.poolBlockEntityStates),
            Toggle.of("paramax.toggle.adaptive_governor", c -> c.adaptivePerformance, c -> c.adaptivePerformance = !c.adaptivePerformance),
            Toggle.of("paramax.toggle.frame_pacing", c -> c.framePacing, c -> c.framePacing = !c.framePacing),
            Toggle.of("paramax.toggle.particle_distance_culling", c -> c.particleCulling, c -> c.particleCulling = !c.particleCulling),
            Toggle.of("paramax.toggle.pool_entity_states", c -> c.poolEntityStates, c -> c.poolEntityStates = !c.poolEntityStates),
            Toggle.of("paramax.toggle.temporal_entity_lod", c -> c.temporalEntityLod, c -> c.temporalEntityLod = !c.temporalEntityLod),
            Toggle.of("paramax.toggle.governor_anticipation", c -> c.anticipateSpikes, c -> c.anticipateSpikes = !c.anticipateSpikes),
            Toggle.of("paramax.toggle.particle_spawn_budget", c -> c.budgetParticleSpawns, c -> c.budgetParticleSpawns = !c.budgetParticleSpawns)
    );

    private static final int PAGE_TOGGLES = 0;
    private static final int PAGE_NUMBERS = 1;

    private final Screen parent;
    private int page = PAGE_TOGGLES;

    public ParaMaxConfigScreen(Screen parent) {
        super(Text.translatable("paramax.options.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int buttonHeight = 20;
        int gap = 4;
        int gridTop = 56;

        this.addPresetRow(gap);

        int itemCount = this.page == PAGE_TOGGLES ? TOGGLES.size() : 18;
        int availableRows = Math.max(1, (this.height - gridTop - 40) / (buttonHeight + gap));
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
            widget.setY(gridTop + (i / columns) * (buttonHeight + gap));
            this.addDrawableChild(widget);
        }

        Text otherPage = Text.translatable(
                this.page == PAGE_TOGGLES ? "paramax.nav.numbers" : "paramax.nav.toggles");
        this.addDrawableChild(ButtonWidget.builder(otherPage, button -> {
                    this.page = this.page == PAGE_TOGGLES ? PAGE_NUMBERS : PAGE_TOGGLES;
                    this.clearAndInit();
                })
                .dimensions(this.width / 2 - 154, this.height - 27, 150, 20)
                .build());
        this.addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, button -> this.close())
                .dimensions(this.width / 2 + 4, this.height - 27, 150, 20)
                .build());
    }

    private void addPresetRow(int gap) {
        int presetWidth = Math.min(110, (this.width - 20 - gap * 3) / 4);
        int rowWidth = presetWidth * 4 + gap * 3;
        int x = this.width / 2 - rowWidth / 2;
        int y = 30;

        this.addPresetButton("paramax.preset.potato", ParaMaxPreset.POTATO, x, y, presetWidth);
        this.addPresetButton("paramax.preset.balanced", ParaMaxPreset.BALANCED, x + (presetWidth + gap), y, presetWidth);
        this.addPresetButton("paramax.preset.quality", ParaMaxPreset.QUALITY, x + (presetWidth + gap) * 2, y, presetWidth);
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("paramax.preset.defaults"), button -> {
                    ParaMaxConfig cfg = ParaMaxConfig.get();
                    cfg.resetToDefaults();
                    cfg.clamp();
                    this.clearAndInit();
                })
                .dimensions(x + (presetWidth + gap) * 3, y, presetWidth, 20)
                .build());
    }

    private void addPresetButton(String key, ParaMaxPreset preset, int x, int y, int w) {
        this.addDrawableChild(ButtonWidget.builder(Text.translatable(key), button -> {
                    preset.apply(ParaMaxConfig.get());
                    this.clearAndInit();
                })
                .dimensions(x, y, w, 20)
                .build());
    }

    private List<ClickableWidget> buildToggles(int w, int h) {
        ParaMaxConfig cfg = ParaMaxConfig.get();
        return TOGGLES.stream()
                .<ClickableWidget>map(toggle -> CyclingButtonWidget.onOffBuilder(toggle.getter().apply(cfg))
                        .build(0, 0, w, h, Text.translatable(toggle.key()),
                                (button, value) -> toggle.flipper().accept(ParaMaxConfig.get())))
                .toList();
    }

    private List<ClickableWidget> buildSliders(int w, int h) {
        ParaMaxConfig cfg = ParaMaxConfig.get();
        return List.of(
                new ParaMaxSlider(0, 0, w, h, "paramax.slider.target_fps", 20, 240, 5, ParaMaxSlider.Format.FPS,
                        () -> cfg.targetFps, v -> cfg.targetFps = (int) v),
                new ParaMaxSlider(0, 0, w, h, "paramax.slider.unfocused_fps", 1, 60, 1, ParaMaxSlider.Format.FPS,
                        () -> cfg.unfocusedFps, v -> cfg.unfocusedFps = (int) v),
                new ParaMaxSlider(0, 0, w, h, "paramax.slider.menu_fps", 5, 120, 5, ParaMaxSlider.Format.FPS,
                        () -> cfg.menuFps, v -> cfg.menuFps = (int) v),
                new ParaMaxSlider(0, 0, w, h, "paramax.slider.particles_kept", 0.0, 1.0, 0.05, ParaMaxSlider.Format.PERCENT,
                        () -> cfg.particleMultiplier, v -> cfg.particleMultiplier = v),
                new ParaMaxSlider(0, 0, w, h, "paramax.slider.entity_cull_distance", 8, 192, 8, ParaMaxSlider.Format.BLOCKS,
                        () -> cfg.maxEntityRenderDistance, v -> cfg.maxEntityRenderDistance = v),
                new ParaMaxSlider(0, 0, w, h, "paramax.slider.block_entity_cull_distance", 8, 192, 8, ParaMaxSlider.Format.BLOCKS,
                        () -> cfg.maxBlockEntityRenderDistance, v -> cfg.maxBlockEntityRenderDistance = v),
                new ParaMaxSlider(0, 0, w, h, "paramax.slider.parallel_particle_threshold", 64, 4096, 64, ParaMaxSlider.Format.INT,
                        () -> cfg.parallelParticleThreshold, v -> cfg.parallelParticleThreshold = (int) v),
                new ParaMaxSlider(0, 0, w, h, "paramax.slider.parallel_block_entity_threshold", 16, 1024, 16, ParaMaxSlider.Format.INT,
                        () -> cfg.parallelBlockEntityThreshold, v -> cfg.parallelBlockEntityThreshold = (int) v),
                new ParaMaxSlider(0, 0, w, h, "paramax.slider.parallel_entity_threshold", 32, 1024, 32, ParaMaxSlider.Format.INT,
                        () -> cfg.parallelEntityThreshold, v -> cfg.parallelEntityThreshold = (int) v),
                new ParaMaxSlider(0, 0, w, h, "paramax.slider.debug_hud_interval", 16, 1000, 16, ParaMaxSlider.Format.MILLISECONDS,
                        () -> cfg.debugHudIntervalMs, v -> cfg.debugHudIntervalMs = (int) v),
                new ParaMaxSlider(0, 0, w, h, "paramax.slider.hud_interval", 50, 2000, 50, ParaMaxSlider.Format.MILLISECONDS,
                        () -> cfg.hudCacheIntervalMs, v -> cfg.hudCacheIntervalMs = (int) v),
                new ParaMaxSlider(0, 0, w, h, "paramax.slider.worker_threads", 0, 32, 1, ParaMaxSlider.Format.INT_OR_AUTO,
                        () -> cfg.workerThreadCount, v -> cfg.workerThreadCount = (int) v),
                new ParaMaxSlider(0, 0, w, h, "paramax.slider.governor_base_pressure", 0, 4, 1, ParaMaxSlider.Format.INT,
                        () -> cfg.governorBasePressure, v -> cfg.governorBasePressure = (int) v),
                new ParaMaxSlider(0, 0, w, h, "paramax.slider.pacing_min_fps", 10, 120, 5, ParaMaxSlider.Format.FPS,
                        () -> cfg.pacingMinFps, v -> cfg.pacingMinFps = (int) v),
                new ParaMaxSlider(0, 0, w, h, "paramax.slider.particle_cull_distance", 8, 256, 8, ParaMaxSlider.Format.BLOCKS,
                        () -> cfg.maxParticleDistance, v -> cfg.maxParticleDistance = v),
                new ParaMaxSlider(0, 0, w, h, "paramax.slider.lod_near_distance", 8, 64, 4, ParaMaxSlider.Format.BLOCKS,
                        () -> cfg.lodNearDistance, v -> cfg.lodNearDistance = v),
                new ParaMaxSlider(0, 0, w, h, "paramax.slider.lod_max_interval", 1, 8, 1, ParaMaxSlider.Format.INT,
                        () -> cfg.lodMaxInterval, v -> cfg.lodMaxInterval = (int) v),
                new ParaMaxSlider(0, 0, w, h, "paramax.slider.particle_spawn_budget", 100, 8000, 100, ParaMaxSlider.Format.INT,
                        () -> cfg.particleSpawnBudget, v -> cfg.particleSpawnBudget = (int) v)
        );
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        super.render(context, mouseX, mouseY, deltaTicks);
        Text pageName = Text.translatable(
                this.page == PAGE_TOGGLES ? "paramax.page.toggles" : "paramax.page.numbers");
        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.translatable("paramax.options.header", pageName), this.width / 2, 15, 0xFFFFFFFF);
    }

    @Override
    public void close() {
        ParaMaxConfig.get().save();
        this.client.setScreen(this.parent);
    }
}
