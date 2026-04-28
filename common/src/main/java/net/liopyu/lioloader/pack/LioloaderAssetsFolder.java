package net.liopyu.lioloader.pack;

import com.mojang.logging.LogUtils;
import net.liopyu.lioloader.util.PackMetadataCompat;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.RepositorySource;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Consumer;

public final class LioloaderAssetsFolder {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final PackSource SOURCE = PackSource.create(PackSource.NO_DECORATION, true);
    private static final String PACK_ID = "lioloader_assets";

    private LioloaderAssetsFolder() {
    }

    public static Path packRootDir(Path gameDir) {
        return gameDir.resolve("lioloader");
    }

    public static void ensureDir(Path gameDir) {
        Path root = packRootDir(gameDir);
        try {
            Files.createDirectories(root.resolve("assets"));
            writePackMeta(root);
        } catch (IOException e) {
            LOGGER.error("[Lioloader] Failed to prepare assets folder at {}", root, e);
        }
    }

    public static RepositorySource repositorySource(Path packRoot) {
        return new AssetsSource(packRoot.toAbsolutePath().normalize());
    }

    private static void writePackMeta(Path packRoot) throws IOException {
        Path mcmeta = packRoot.resolve("pack.mcmeta");
        String content = PackMetadataCompat.buildPackMetadataJson(PackMetadataCompat.CLIENT_RESOURCES, "LioLoader Assets");
        if (Files.exists(mcmeta) && Files.readString(mcmeta, StandardCharsets.UTF_8).equals(content)) return;
        Files.writeString(mcmeta, content, StandardCharsets.UTF_8);
    }

    private static final class AssetsSource implements RepositorySource {
        private final Path packRoot;

        private AssetsSource(Path packRoot) {
            this.packRoot = packRoot;
        }

        @Override
        public void loadPacks(Consumer<Pack> consumer) {
            try {
                Files.createDirectories(packRoot.resolve("assets"));
                writePackMeta(packRoot);
            } catch (IOException e) {
                LOGGER.error("[Lioloader] Failed to prepare assets folder at {}", packRoot, e);
                return;
            }

            Pack pack = Pack.readMetaAndCreate(
                    new PackLocationInfo(PACK_ID, Component.literal("LioLoader Assets"), SOURCE, Optional.empty()),
                    new PathPackResources.PathResourcesSupplier(packRoot),
                    PackType.CLIENT_RESOURCES,
                    new PackSelectionConfig(true, Pack.Position.TOP, true)
            );

            if (pack != null) consumer.accept(pack);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof AssetsSource other)) return false;
            return packRoot.equals(other.packRoot);
        }

        @Override
        public int hashCode() {
            return packRoot.hashCode();
        }
    }
}
