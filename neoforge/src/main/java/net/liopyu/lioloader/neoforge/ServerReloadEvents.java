package net.liopyu.lioloader.neoforge;

import com.mojang.logging.LogUtils;
import net.liopyu.lioloader.Lioloader;
import net.liopyu.lioloader.pack.LioloaderPackLoadOrder;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

@EventBusSubscriber(modid = Lioloader.MOD_ID)
public final class ServerReloadEvents {
    private static final org.slf4j.Logger LOGGER = LogUtils.getLogger();

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        var server = event.getServer();
        server.execute(() -> {
            try {
                Path gameDir = Lioloader.gameDir();
                if (gameDir != null) {
                    List<String> order = LioloaderPackLoadOrder.readOrder(
                            LioloaderPackLoadOrder.datapackOrderFile(gameDir)
                    );
                    LioloaderPackLoadOrder.applyOrder(server.getPackRepository(), order);
                }

                Collection<String> selected = server.getPackRepository().getSelectedIds();
                LOGGER.info("[Lioloader] Forcing server datapack reload. selected={}", selected);

                server.reloadResources(selected).thenRun(() -> {
                    LOGGER.info("[Lioloader] Server datapack reload complete");
                }).exceptionally(err -> {
                    LOGGER.error("[Lioloader] Server datapack reload failed", err);
                    return null;
                });
            } catch (Throwable t) {
                LOGGER.error("[Lioloader] Server reloadResources() call failed", t);
            }
        });
    }
}
