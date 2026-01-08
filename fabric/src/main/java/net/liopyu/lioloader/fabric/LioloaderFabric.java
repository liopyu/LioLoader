package net.liopyu.lioloader.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.liopyu.lioloader.Lioloader;
import net.liopyu.lioloader.mixin.PackRepositoryAccessor;
import net.liopyu.lioloader.pack.LioloaderGlobalDatapacks;

import java.nio.file.Path;

public final class LioloaderFabric implements ModInitializer {
    private static final org.slf4j.Logger LOGGER = com.mojang.logging.LogUtils.getLogger();

    @Override
    public void onInitialize() {
        Path gameDir = FabricLoader.getInstance().getGameDir();
        Lioloader.init(gameDir);

        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            Path globalDir = LioloaderGlobalDatapacks.globalDatapacksDir(gameDir);
            LOGGER.info("[Lioloader] SERVER_STARTING: injecting datapack source dir={}", globalDir);

            ((PackRepositoryAccessor) server.getPackRepository())
                    .lioloader$getSources()
                    .add(LioloaderGlobalDatapacks.repositorySource(globalDir));

            server.getPackRepository().reload();

            LOGGER.info("[Lioloader] SERVER_STARTING: available={} selected={}",
                    server.getPackRepository().getAvailableIds(),
                    server.getPackRepository().getSelectedIds());
        });

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            LOGGER.info("[Lioloader] SERVER_STARTED: forcing resource reload; selected={}", server.getPackRepository().getSelectedIds());
            server.reloadResources(server.getPackRepository().getSelectedIds()).join();
            LOGGER.info("[Lioloader] SERVER_STARTED: reload complete");
        });
    }
}
