package net.liopyu.lioloader.neoforge;

import com.mojang.logging.LogUtils;
import net.liopyu.lioloader.Lioloader;
import net.liopyu.lioloader.pack.LioloaderGlobalDatapacks;
import net.liopyu.lioloader.pack.LioloaderPackLoadOrder;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.PackType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.neoforge.event.GameShuttingDownEvent;

import java.nio.file.Path;
import java.util.List;

@EventBusSubscriber(modid = Lioloader.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class LioloaderNeoForgeClientReload {
    @SubscribeEvent
    public static void addPackFinders(AddPackFindersEvent event) {
        if (event.getPackType() != PackType.SERVER_DATA) return;

        Path gameDir = FMLPaths.GAMEDIR.get();
        Path globalDir = LioloaderGlobalDatapacks.globalDatapacksDir(gameDir);

        event.addRepositorySource(LioloaderGlobalDatapacks.repositorySource(globalDir));
    }
}
