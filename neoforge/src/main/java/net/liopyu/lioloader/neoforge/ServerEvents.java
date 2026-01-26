package net.liopyu.lioloader.neoforge;

import net.liopyu.lioloader.Lioloader;
import net.liopyu.lioloader.pack.LioloaderGlobalDatapacks;
import net.minecraft.server.packs.PackType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.event.AddPackFindersEvent;

import java.nio.file.Path;

@EventBusSubscriber(modid = Lioloader.MOD_ID)
public final class ServerEvents {
    @SubscribeEvent
    public static void addPackFinders(AddPackFindersEvent event) {
        if (event.getPackType() != PackType.SERVER_DATA) return;

        Path gameDir = FMLPaths.GAMEDIR.get();
        Path globalDir = LioloaderGlobalDatapacks.globalDatapacksDir(gameDir);

        event.addRepositorySource(LioloaderGlobalDatapacks.repositorySource(globalDir));
    }
}
