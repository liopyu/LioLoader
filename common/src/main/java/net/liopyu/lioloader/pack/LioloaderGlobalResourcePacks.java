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

public final class LioloaderGlobalResourcePacks {
    private static final PackSource LIOLOADER_SOURCE = PackSource.create(PackSource.NO_DECORATION, true);

    private LioloaderGlobalResourcePacks() {
    }

    public static Path globalResourcePacksDir(Path gameDir) {
        return gameDir.resolve("lioloader").resolve("resourcepacks");
    }

    public static void ensureDirs(Path gameDir) {
        try {
            Files.createDirectories(globalResourcePacksDir(gameDir));
        } catch (IOException ignored) {
        }
    }

    public static RepositorySource repositorySource(Path globalDir) {
        return new RepositorySource() {
            @Override
            public void loadPacks(Consumer<Pack> consumer) {
                try {
                    Files.createDirectories(globalDir);
                } catch (IOException ignored) {
                    return;
                }

                try (DirectoryStream<Path> stream = Files.newDirectoryStream(globalDir)) {
                    for (Path entry : stream) {
                        if (Files.isDirectory(entry)) {
                            acceptPack(consumer, entry, new PathPackResources.PathResourcesSupplier(entry));
                            continue;
                        }

                        String name = entry.getFileName().toString();
                        if (name.toLowerCase(Locale.ROOT).endsWith(".zip") && Files.isRegularFile(entry)) {
                            acceptPack(consumer, entry, new FilePackResources.FileResourcesSupplier(entry));
                        }
                    }
                } catch (IOException ignored) {
                }
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
        if (pack != null) consumer.accept(pack);
    }

    private static String sanitizeId(String s) {
        String out = s.toLowerCase(Locale.ROOT).replaceAll("\\s+", "_").replaceAll("[^a-z0-9._-]", "_");
        while (out.contains("__")) out = out.replace("__", "_");
        return out;
    }
}
