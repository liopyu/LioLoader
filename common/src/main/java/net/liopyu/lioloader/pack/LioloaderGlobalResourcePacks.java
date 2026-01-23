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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Consumer;

public final class LioloaderGlobalResourcePacks {
    private static final PackSource LIOLOADER_SOURCE = PackSource.create(PackSource.NO_DECORATION, true);
    private static final org.slf4j.Logger LOGGER = com.mojang.logging.LogUtils.getLogger();

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
        return new GlobalDirSource(globalDir);
    }

    private static final class GlobalDirSource implements RepositorySource {
        private final Path globalDir;

        private GlobalDirSource(Path globalDir) {
            this.globalDir = globalDir;
        }

        @Override
        public void loadPacks(Consumer<Pack> consumer) {
            LOGGER.info("[Lioloader] Scanning global resourcepacks dir (recursive): {}", globalDir);

            try {
                Files.createDirectories(globalDir);
            } catch (IOException e) {
                LOGGER.error("[Lioloader] Failed to create global resourcepacks dir: {}", globalDir, e);
                return;
            }

            java.util.HashSet<String> emitted = new java.util.HashSet<>();

            try {
                Files.walkFileTree(globalDir, java.util.EnumSet.noneOf(java.nio.file.FileVisitOption.class), Integer.MAX_VALUE,
                        new java.nio.file.SimpleFileVisitor<>() {
                            @Override
                            public java.nio.file.FileVisitResult preVisitDirectory(Path dir, java.nio.file.attribute.BasicFileAttributes attrs) {
                                if (Files.isRegularFile(dir.resolve("pack.mcmeta"))) {
                                    String id = makeIdForPathPack(globalDir, dir);
                                    if (emitted.add(id)) {
                                        acceptPackWithId(consumer, dir, new PathPackResources.PathResourcesSupplier(dir), id, dir.getFileName().toString());
                                    }
                                }
                                return java.nio.file.FileVisitResult.CONTINUE;
                            }

                            @Override
                            public java.nio.file.FileVisitResult visitFile(Path file, java.nio.file.attribute.BasicFileAttributes attrs) {
                                String n = file.getFileName().toString();
                                if (n.toLowerCase(Locale.ROOT).endsWith(".zip") && Files.isRegularFile(file)) {
                                    if (zipHasRootPackMcmeta(file)) {
                                        String id = makeIdForZipPack(globalDir, file);
                                        if (emitted.add(id)) {
                                            acceptPackWithId(consumer, file, new FilePackResources.FileResourcesSupplier(file), id, file.getFileName().toString());
                                        }
                                    }
                                }
                                return java.nio.file.FileVisitResult.CONTINUE;
                            }
                        });
            } catch (IOException e) {
                LOGGER.error("[Lioloader] Recursive scan failed for {}", globalDir, e);
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof GlobalDirSource other)) return false;
            return globalDir.normalize().toAbsolutePath().equals(other.globalDir.normalize().toAbsolutePath());
        }

        @Override
        public int hashCode() {
            return globalDir.normalize().toAbsolutePath().hashCode();
        }

        @Override
        public String toString() {
            return "LioloaderGlobalResourcePacksSource[" + globalDir + "]";
        }
    }

    private static boolean zipHasRootPackMcmeta(Path zipFile) {
        try (java.util.zip.ZipFile zf = new java.util.zip.ZipFile(zipFile.toFile())) {
            return zf.getEntry("pack.mcmeta") != null;
        } catch (Throwable t) {
            LOGGER.error("[Lioloader] Failed checking zip for pack.mcmeta: {}", zipFile, t);
            return false;
        }
    }

    private static String makeIdForPathPack(Path rootDir, Path packDir) {
        Path relPath = rootDir.relativize(packDir.toAbsolutePath().normalize());
        String packName = packDir.getFileName().toString();

        if (relPath.getNameCount() == 1) {
            return sanitizeIdStable(packName);
        }

        String parentRel = relPath.getParent().toString().replace('\\', '/');
        return sanitizeIdStable(parentRel + "/" + packName);
    }

    private static String makeIdForZipPack(Path rootDir, Path zipFile) {
        Path relPath = rootDir.relativize(zipFile.toAbsolutePath().normalize());
        String name = zipFile.getFileName().toString();
        if (name.toLowerCase(Locale.ROOT).endsWith(".zip")) name = name.substring(0, name.length() - 4);

        if (relPath.getNameCount() == 1) {
            return sanitizeIdStable(name);
        }

        String parentRel = relPath.getParent().toString().replace('\\', '/');
        return sanitizeIdStable(parentRel + "/" + name);
    }

    private static String sanitizeIdStable(String s) {
        String out = s.toLowerCase(Locale.ROOT).replaceAll("\\s+", "_").replaceAll("[^a-z0-9._/!:-]", "_");
        while (out.contains("__")) out = out.replace("__", "_");
        out = out.replace('/', '_').replace('!', '_').replace(':', '_').replace('-', '_');
        return out;
    }

    private static void acceptPackWithId(Consumer<Pack> consumer, Path entry, Pack.ResourcesSupplier supplier, String id, String displayName) {
        Component title = Component.literal("Lioloader: " + displayName);

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

        Pack pack = Pack.readMetaAndCreate(location, supplier, PackType.CLIENT_RESOURCES, selection);
        if (pack == null) {
            LOGGER.warn("[Lioloader] Pack readMetaAndCreate returned null for id='{}' entry='{}'", id, entry);
            return;
        }

        consumer.accept(pack);
    }
}
