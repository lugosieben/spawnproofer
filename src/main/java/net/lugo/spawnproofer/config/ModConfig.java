package net.lugo.spawnproofer.config;

import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.*;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.lugo.spawnproofer.Spawnproofer;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;

import java.awt.*;

public class ModConfig {
    @SerialEntry
    public static int lightLevelThreshold = 1;

    @SerialEntry
    public static int chunkScanRange = 4;

    @SerialEntry
    public static boolean hideGreen = false;

    public static Screen makeScreen(Screen parent) {
        return YetAnotherConfigLib.createBuilder()
                .title(Component.translatable("text.spawnproofer.config.title"))
                .category(ConfigCategory.createBuilder()
                        .name(Component.translatable("text.spawnproofer.config.category.main"))
                        .group(OptionGroup.createBuilder()
                                .name(Component.translatable("text.spawnproofer.config.group.general"))
                                .option(Option.<Integer>createBuilder()
                                        .name(Component.translatable("text.spawnproofer.config.option.light_level_threshold.name"))
                                        .description(OptionDescription.of(Component.translatable("text.spawnproofer.config.option.light_level_threshold.description")))
                                        .binding(
                                                1,
                                                () -> lightLevelThreshold,
                                                newVal -> lightLevelThreshold = newVal)
                                        .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                                                .range(1, 15)
                                                .step(1))
                                        .build())
                                .option(Option.<Integer>createBuilder()
                                        .name(Component.translatable("text.spawnproofer.config.option.render_distance.name"))
                                        .description(OptionDescription.of(Component.translatable("text.spawnproofer.config.option.render_distance.description")))
                                        .binding(
                                                4,
                                                () -> chunkScanRange,
                                                newVal -> chunkScanRange = newVal)
                                        .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                                                .range(1, 24)
                                                .step(1))
                                        .build())
                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.translatable("text.spawnproofer.config.option.hide_green.name"))
                                        .binding(
                                                false,
                                                () -> hideGreen,
                                                newVal -> hideGreen = newVal)
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())
                                .build())
                        .build())
                .category(ConfigCategory.createBuilder()
                        .name(Component.translatable("text.spawnproofer.config.category.report"))
                        .option(ButtonOption.createBuilder()
                                .name(Component.translatable("text.spawnproofer.config.button.report"))
                                .description(OptionDescription.of(Component.translatable("text.spawnproofer.config.button.report.description")))
                                .text(Component.literal(""))
                                .action((yaclScreen, buttonOption) -> {
                                    var modContainerOpt = FabricLoader.getInstance().getModContainer(Spawnproofer.MOD_ID);
                                    modContainerOpt.ifPresent(modContainer -> {
                                        var issuesUrlOpt = modContainer.getMetadata().getContact().get("issues");
                                        issuesUrlOpt.ifPresent(url -> Util.getPlatform().openUri(url));
                                    });
                                })
                                .build())
                        .build())
                .save(HANDLER::save)
                .build()
                .generateScreen(parent);
    }

    public static final ConfigClassHandler<ModConfig> HANDLER = ConfigClassHandler.createBuilder(ModConfig.class)
            .id(Identifier.fromNamespaceAndPath(Spawnproofer.MOD_ID, "config"))
            .serializer(config -> GsonConfigSerializerBuilder.create(config)
                    .setPath(FabricLoader.getInstance().getConfigDir().resolve("spawnproofer.json5"))
                    .setJson5(true)
                    .build())
            .build();
}
