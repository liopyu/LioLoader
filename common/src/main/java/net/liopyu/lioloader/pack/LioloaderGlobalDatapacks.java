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
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

public final class LioloaderGlobalDatapacks {
    private static final PackSource LIOLOADER_SOURCE = PackSource.create(PackSource.NO_DECORATION, true);
    private static final org.slf4j.Logger LOGGER = com.mojang.logging.LogUtils.getLogger();

    private LioloaderGlobalDatapacks() {
    }

    public static Path globalDatapacksDir(Path gameDir) {
        return gameDir.resolve("lioloader").resolve("data");
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
            LOGGER.info("[Lioloader] Scanning global datapacks dir (recursive): {}", globalDir);

            try {
                Files.createDirectories(globalDir);
            } catch (IOException e) {
                LOGGER.error("[Lioloader] Failed to create global datapacks dir: {}", globalDir, e);
                return;
            }

            Path cacheDir = globalDir.resolve(".lioloader_cache").resolve("extracted_datapacks");
            try {
                Files.createDirectories(cacheDir);
            } catch (IOException e) {
                LOGGER.error("[Lioloader] Failed to create cache dir: {}", cacheDir, e);
                return;
            }

            java.util.HashSet<String> emitted = new java.util.HashSet<>();

            try {
                Files.walkFileTree(globalDir, java.util.EnumSet.noneOf(java.nio.file.FileVisitOption.class), Integer.MAX_VALUE,
                        new java.nio.file.SimpleFileVisitor<>() {
                            @Override
                            public java.nio.file.FileVisitResult preVisitDirectory(Path dir, java.nio.file.attribute.BasicFileAttributes attrs) {
                                if (dir.equals(cacheDir) || dir.startsWith(cacheDir))
                                    return java.nio.file.FileVisitResult.SKIP_SUBTREE;
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
                                    scanZipForPacks(consumer, globalDir, file, cacheDir, emitted);
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
            return "LioloaderGlobalDatapacksSource[" + globalDir + "]";
        }
    }

    private static void scanZipForPacks(
            Consumer<Pack> consumer,
            Path rootDir,
            Path zipFile,
            Path cacheDir,
            java.util.Set<String> emitted
    ) {
        java.util.LinkedHashSet<String> packRootPrefixes = new java.util.LinkedHashSet<>();

        try (java.util.zip.ZipFile zf = new java.util.zip.ZipFile(zipFile.toFile())) {
            if (zf.getEntry("pack.mcmeta") != null) packRootPrefixes.add("");

            java.util.Enumeration<? extends java.util.zip.ZipEntry> it = zf.entries();
            while (it.hasMoreElements()) {
                java.util.zip.ZipEntry ze = it.nextElement();
                if (ze.isDirectory()) continue;
                String name = ze.getName();
                if (!name.endsWith("pack.mcmeta")) continue;

                String prefix = zipPackRootPrefix(name);
                if (prefix != null) packRootPrefixes.add(prefix);
            }

            for (String prefix : packRootPrefixes) {
                if (prefix.isEmpty()) {
                    String id = makeIdForZipRootPack(rootDir, zipFile);
                    if (emitted.add(id)) {
                        acceptPackWithId(consumer, zipFile, new FilePackResources.FileResourcesSupplier(zipFile), id, zipFile.getFileName().toString());
                    }
                } else {
                    Path extracted = extractZipPrefix(zipFile, zf, prefix, cacheDir);
                    if (extracted != null && Files.isRegularFile(extracted.resolve("pack.mcmeta"))) {
                        String id = makeIdForZipSubPack(rootDir, zipFile, prefix);
                        if (emitted.add(id)) {
                            String display = zipFile.getFileName().toString() + ":" + prefix;
                            acceptPackWithId(consumer, extracted, new PathPackResources.PathResourcesSupplier(extracted), id, display);
                        }
                    }
                }
            }
        } catch (Throwable t) {
            LOGGER.error("[Lioloader] Failed scanning zip for packs: {}", zipFile, t);
        }
    }

    private static String zipPackRootPrefix(String packMcmetaPath) {
        String p = packMcmetaPath;
        while (p.startsWith("/")) p = p.substring(1);
        if (!p.endsWith("pack.mcmeta")) return null;
        int idx = p.lastIndexOf('/');
        if (idx < 0) return "";
        return p.substring(0, idx + 1);
    }

    private static Path extractZipPrefix(Path zipFile, java.util.zip.ZipFile zf, String prefix, Path cacheDir) {
        try {
            long stamp = Files.getLastModifiedTime(zipFile).toMillis();
            String key = zipFile.toAbsolutePath().normalize().toString() + "|" + stamp + "|" + prefix;
            String folder = Integer.toHexString(key.hashCode());
            Path outDir = cacheDir.resolve(folder);

            Path marker = outDir.resolve(".lioloader_stamp");
            if (Files.isDirectory(outDir) && Files.isRegularFile(marker)) {
                String prev = Files.readString(marker);
                if (prev.equals(Long.toString(stamp))) return outDir;
            }

            if (Files.exists(outDir)) deleteRecursively(outDir);
            Files.createDirectories(outDir);

            java.util.Enumeration<? extends java.util.zip.ZipEntry> it = zf.entries();
            while (it.hasMoreElements()) {
                java.util.zip.ZipEntry ze = it.nextElement();
                if (ze.isDirectory()) continue;

                String name = ze.getName();
                if (!name.startsWith(prefix)) continue;

                String rel = name.substring(prefix.length());
                if (rel.isEmpty()) continue;

                Path out = outDir.resolve(rel);
                Path parent = out.getParent();
                if (parent != null) Files.createDirectories(parent);

                try (java.io.InputStream in = zf.getInputStream(ze)) {
                    Files.copy(in, out, StandardCopyOption.REPLACE_EXISTING);
                }
            }

            Files.writeString(marker, Long.toString(stamp));
            return outDir;
        } catch (Throwable t) {
            LOGGER.error("[Lioloader] Failed extracting zip prefix. zip={} prefix={}", zipFile, prefix, t);
            return null;
        }
    }

    private static void deleteRecursively(Path root) throws IOException {
        if (Files.notExists(root)) return;
        try (Stream<Path> s = Files.walk(root)) {
            s.sorted(java.util.Comparator.reverseOrder()).forEach(p -> {
                try {
                    Files.deleteIfExists(p);
                } catch (IOException ignored) {
                }
            });
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

    private static String makeIdForZipRootPack(Path rootDir, Path zipFile) {
        Path relPath = rootDir.relativize(zipFile.toAbsolutePath().normalize());
        String name = zipFile.getFileName().toString();
        if (name.toLowerCase(Locale.ROOT).endsWith(".zip")) name = name.substring(0, name.length() - 4);

        if (relPath.getNameCount() == 1) {
            return sanitizeIdStable(name);
        }

        String parentRel = relPath.getParent().toString().replace('\\', '/');
        return sanitizeIdStable(parentRel + "/" + name);
    }

    private static String makeIdForZipSubPack(Path rootDir, Path zipFile, String prefix) {
        Path relPath = rootDir.relativize(zipFile.toAbsolutePath().normalize());
        String zipName = zipFile.getFileName().toString();
        if (zipName.toLowerCase(Locale.ROOT).endsWith(".zip")) zipName = zipName.substring(0, zipName.length() - 4);

        String p = prefix;
        while (p.startsWith("/")) p = p.substring(1);
        while (p.endsWith("/")) p = p.substring(0, p.length() - 1);

        String leaf = p;
        int idx = leaf.lastIndexOf('/');
        if (idx >= 0) leaf = leaf.substring(idx + 1);
        if (leaf.isEmpty()) leaf = "inner_pack";

        if (relPath.getNameCount() == 1) {
            return sanitizeIdStable(zipName + "/" + leaf);
        }

        String parentRel = relPath.getParent().toString().replace('\\', '/');
        return sanitizeIdStable(parentRel + "/" + zipName + "/" + leaf);
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

        Pack pack = Pack.readMetaAndCreate(location, supplier, PackType.SERVER_DATA, selection);
        if (pack == null) {
            LOGGER.warn("[Lioloader] Pack readMetaAndCreate returned null for id='{}' entry='{}'", id, entry);
            return;
        }

        consumer.accept(pack);
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
