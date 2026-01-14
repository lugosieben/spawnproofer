package net.lugo.spawnproofer.config;

import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.lugo.spawnproofer.Spawnproofer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import java.awt.*;


public class ModConfig {
    @SerialEntry
    public static int chunkScanRange = 4;

    public static Screen makeScreen(Screen parent) {
        return YetAnotherConfigLib.createBuilder()
                .title(Text.translatable("text.spawnproofer.config.title"))
                .category(ConfigCategory.createBuilder()
                        .name(Text.translatable("text.spawnproofer.config.category.main"))
                        .group(OptionGroup.createBuilder()
                                .name(Text.translatable("text.spawnproofer.config.group.general"))
                                .option(Option.<Integer>createBuilder()
                                        .name(Text.translatable("text.spawnproofer.config.option.chunk_scan_range.name"))
                                        .description(OptionDescription.of(Text.translatable("text.spawnproofer.config.option.chunk_scan_range.description")))
                                        .binding(
                                                4,
                                                () -> chunkScanRange,
                                                newVal -> chunkScanRange = newVal)
                                        .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                                                .range(1, 24)
                                                .step(1))
                                        .build())
                                .build())
                        .build())
                .category(ConfigCategory.createBuilder()
                        .name(Text.translatable("text.spawnproofer.config.category.report"))
                        .option(ButtonOption.createBuilder()
                                .name(Text.translatable("text.spawnproofer.config.button.report"))
                                .text(Text.literal(""))
                                .action((yaclScreen, buttonOption) -> {
                                    var modContainerOpt = FabricLoader.getInstance().getModContainer(Spawnproofer.MOD_ID);
                                    modContainerOpt.ifPresent(modContainer -> {
                                        var issuesUrlOpt = modContainer.getMetadata().getContact().get("issues");
                                        issuesUrlOpt.ifPresent(url -> Util.getOperatingSystem().open(url));
                                    });
                                })
                                .build())
                        .build())
                .save(HANDLER::save)
                .build()
                .generateScreen(parent);
    }

    public static final ConfigClassHandler<ModConfig> HANDLER = ConfigClassHandler.createBuilder(ModConfig.class)
            .id(Identifier.of(Spawnproofer.MOD_ID, "config"))
            .serializer(config -> GsonConfigSerializerBuilder.create(config)
                    .setPath(FabricLoader.getInstance().getConfigDir().resolve("spawnproofer.json5"))
                    .setJson5(true)
                    .build())
            .build();
}
