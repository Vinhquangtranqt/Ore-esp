package com.kodari.oreoutlines;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.ColorControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.awt.Color;

public final class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return ModMenuIntegration::screen;
    }

    private static Screen screen(Screen parent) {
        OreConfig config = OreConfig.INSTANCE;
        ConfigCategory.Builder general = ConfigCategory.createBuilder().name(Component.literal("General"));
        general.option(Option.<Boolean>createBuilder().name(Component.literal("Enable outlines"))
                .binding(true, () -> config.enabled, value -> config.enabled = value)
                .controller(TickBoxControllerBuilder::create).build());
        general.option(Option.<Integer>createBuilder().name(Component.literal("Scan radius"))
                .binding(16, () -> config.radius, value -> config.radius = value)
                .controller(option -> IntegerSliderControllerBuilder.create(option)
                        .range(OreConfig.MIN_SCAN_RADIUS, OreConfig.MAX_SCAN_RADIUS).step(1)).build());

        ConfigCategory.Builder ores = ConfigCategory.createBuilder().name(Component.literal("Ore"));
        ConfigCategory.Builder extras = ConfigCategory.createBuilder().name(Component.literal("Extra"));
        for (var entry : config.types.entrySet()) {
            String name = entry.getKey();
            OreConfig.Setting setting = entry.getValue();
            ConfigCategory.Builder category = name.equals("Lava") || name.equals("Gravel") || name.equals("Sand") ? extras : ores;
            category.option(Option.<Boolean>createBuilder().name(Component.literal(name))
                    .binding(true, () -> setting.enabled, value -> setting.enabled = value)
                    .controller(TickBoxControllerBuilder::create).build());
            category.option(Option.<Color>createBuilder().name(Component.literal(name + " color"))
                    .binding(new Color(setting.color & 0xffffff), () -> new Color(setting.color & 0xffffff), value -> setting.color = value.getRGB() & 0xffffff)
                    .controller(ColorControllerBuilder::create).build());
        }
        return YetAnotherConfigLib.createBuilder().title(Component.literal("Ore Outlines"))
                .category(general.build()).category(ores.build()).category(extras.build()).save(OreConfig::save).build().generateScreen(parent);
    }
}