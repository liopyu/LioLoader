package net.liopyu.lioloader.pack;

import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.FilePackResources;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.RepositorySource;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Consumer;

public final class LioloaderGlobalDatapacks {
    private static final PackSource LIOLOADER_SOURCE = PackSource.create(PackSource.NO_DECORATION, true);
    private static final org.slf4j.Logger LOGGER = com.mojang.logging.LogUtils.getLogger();

    private LioloaderGlobalDatapacks() {
    }

    public static Path globalDatapacksDir(Path gameDir) {
        return gameDir.resolve("lioloader").resolve("data");
    }
    public static RepositorySource repositorySource(Path globalDir) {
        return consumer -> {
            try {
                Files.createDirectories(globalDir);
            } catch (IOException e) {
                LOGGER.error("[Lioloader] Failed to create datapack dir: {}", globalDir, e);
                return;
            }

            LOGGER.info("[Lioloader] Scanning global datapacks dir: {}", globalDir);

            try (DirectoryStream<Path> stream = Files.newDirectoryStream(globalDir)) {
                for (Path entry : stream) {
                    if (Files.isDirectory(entry)) {
                        LOGGER.info("[Lioloader] Found datapack folder: {}", entry.getFileName());
                        acceptPack(consumer, entry, new PathPackResources.PathResourcesSupplier(entry));
                        continue;
                    }

                    String name = entry.getFileName().toString();
                    if (name.toLowerCase(Locale.ROOT).endsWith(".zip") && Files.isRegularFile(entry)) {
                        LOGGER.info("[Lioloader] Found datapack zip: {}", entry.getFileName());
                        acceptPack(consumer, entry, new FilePackResources.FileResourcesSupplier(entry));
                    }
                }
            } catch (IOException e) {
                LOGGER.error("[Lioloader] Failed scanning global datapacks dir: {}", globalDir, e);
            }
        };
    }
    private static void acceptPack(Consumer<Pack> consumer, Path entry, Pack.ResourcesSupplier supplier) {
        String baseName = entry.getFileName().toString();
        if (baseName.toLowerCase(Locale.ROOT).endsWith(".zip")) {
            baseName = baseName.substring(0, baseName.length() - 4);
        }

        String id = sanitizeId(baseName);
        Component title = Component.literal("Lioloader: " + baseName);

        LOGGER.info("[Lioloader] Creating pack id='{}' from entry='{}'", id, entry.getFileName());

        PackLocationInfo location = new PackLocationInfo(
                id,
                title,
                LIOLOADER_SOURCE,
                Optional.empty()
        );

        PackSelectionConfig selection = new PackSelectionConfig(
                true,
                Pack.Position.TOP,
                true
        );

        Pack pack = Pack.readMetaAndCreate(location, supplier, PackType.SERVER_DATA, selection);
        if (pack == null) {
            LOGGER.warn("[Lioloader] Pack readMetaAndCreate returned null for id='{}' entry='{}' (likely invalid pack.mcmeta or unreadable)", id, entry.getFileName());
            return;
        }

        LOGGER.info("[Lioloader] Registered pack id='{}' required={} position={}", pack.getId(), pack.isRequired(), pack.getDefaultPosition());
        consumer.accept(pack);
    }


    private static String sanitizeId(String s) {
        String out = s.toLowerCase(Locale.ROOT).replaceAll("\\s+", "_").replaceAll("[^a-z0-9._-]", "_");
        while (out.contains("__")) out = out.replace("__", "_");
        return out;
    }
    public static void ensureDirs(Path gameDir) {
        try {
            java.nio.file.Files.createDirectories(globalDatapacksDir(gameDir));
        } catch (java.io.IOException ignored) {
        }
    }

}
