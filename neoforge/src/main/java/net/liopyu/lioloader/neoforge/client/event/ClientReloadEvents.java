package net.liopyu.lioloader.neoforge.client.event;

import com.mojang.logging.LogUtils;
import net.liopyu.lioloader.Lioloader;
import net.liopyu.lioloader.pack.LioloaderPackLoadOrder;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.nio.file.Path;
import java.util.List;

@EventBusSubscriber(modid = Lioloader.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public final class ClientReloadEvents {
    private static boolean done;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Pre event) {
        if (done)
            return;

        Minecraft client = Minecraft.getInstance();
        if (client == null)
            return;

        done = true;

        client.execute(() -> {
            try {
                Path gameDir = Lioloader.gameDir();

                if (gameDir != null) {
                    List<String> order = LioloaderPackLoadOrder.readOrder(
                            LioloaderPackLoadOrder.resourcepackOrderFile(gameDir));
                    boolean orderChanged = LioloaderPackLoadOrder.applyOrder(client.getResourcePackRepository(), order);

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
    }
}
