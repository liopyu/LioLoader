package net.liopyu.lioloader.fabric.client;

import com.mojang.logging.LogUtils;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.liopyu.lioloader.Lioloader;
import net.liopyu.lioloader.pack.LioloaderPackLoadOrder;

import java.nio.file.Path;
import java.util.List;

public final class LioloaderFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            client.execute(() -> {
                try {
                    Path gd = Lioloader.gameDir();
                    boolean orderChanged = false;

                    if (gd != null) {
                        client.getResourcePackRepository().reload();
                        List<String> order = LioloaderPackLoadOrder.readOrder(
                                LioloaderPackLoadOrder.resourcepackOrderFile(gd)
                        );
                        orderChanged = LioloaderPackLoadOrder.applyOrder(client.getResourcePackRepository(), order);
                    }

                    if (!orderChanged) {
                        LogUtils.getLogger().info("[Lioloader] Client pack order unchanged; running initial resource reload anyway");
                    }

                    client.reloadResourcePacks().thenRun(() -> {
                        LogUtils.getLogger().info("[Lioloader] Client resource reload complete");
                    }).exceptionally(err -> {
                        LogUtils.getLogger().error("[Lioloader] Client resource reload failed", err);
                        return null;
                    });
                } catch (Throwable t) {
                    LogUtils.getLogger().error("[Lioloader] Client reloadResourcePacks() call failed", t);
                }
            });
        });
    }

}
