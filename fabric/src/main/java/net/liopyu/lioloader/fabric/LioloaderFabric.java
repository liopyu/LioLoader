package net.liopyu.lioloader.fabric;

import com.mojang.logging.LogUtils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.liopyu.lioloader.Lioloader;
import net.liopyu.lioloader.pack.LioloaderPackLoadOrder;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

public final class LioloaderFabric implements ModInitializer {
    private static final org.slf4j.Logger LOGGER = LogUtils.getLogger();

    @Override
    public void onInitialize() {
        LogUtils.getLogger().info("I'm in your jars. - Liopyu");
        Path gameDir = FabricLoader.getInstance().getGameDir();
        Lioloader.init(gameDir);
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            server.execute(() -> {
                try {
                    Path gd = Lioloader.gameDir();
                    boolean changed = false;

                    if (gd != null) {
                        List<String> order = LioloaderPackLoadOrder.readOrder(
                                LioloaderPackLoadOrder.datapackOrderFile(gd)
                        );
                        changed = LioloaderPackLoadOrder.applyOrder(server.getPackRepository(), order);
                    }

                    if (!changed) {
                        LogUtils.getLogger().info("[Lioloader] Server pack order unchanged; skipping datapack reload");
                        return;
                    }

                    Collection<String> selected = server.getPackRepository().getSelectedIds();
                    LogUtils.getLogger().info("[Lioloader] Forcing server datapack reload. selected={}", selected);

                    server.reloadResources(selected).thenRun(() -> {
                        LogUtils.getLogger().info("[Lioloader] Server datapack reload complete");
                    }).exceptionally(err -> {
                        LogUtils.getLogger().error("[Lioloader] Server datapack reload failed", err);
                        return null;
                    });
                } catch (Throwable t) {
                    LogUtils.getLogger().error("[Lioloader] Server reloadResources() call failed", t);
                }
            });
        });

    }
}
