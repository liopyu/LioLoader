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

                    if (gd != null) {
                        List<String> order = LioloaderPackLoadOrder.readOrder(
                                LioloaderPackLoadOrder.resourcepackOrderFile(gd));
                        boolean orderChanged = LioloaderPackLoadOrder.applyOrder(client.getResourcePackRepository(),
                                order);

                        if (orderChanged) {
                            LogUtils.getLogger().info("[Lioloader] Applied client resource pack load order");
                        } else {
                            LogUtils.getLogger().info("[Lioloader] Client pack order unchanged");
                        }
                    }
                } catch (Throwable t) {
                    LogUtils.getLogger().error("[Lioloader] Client pack order application failed", t);
                }
            });
        });
    }
}
