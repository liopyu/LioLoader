package net.liopyu.lioloader.neoforge;

import net.liopyu.lioloader.Lioloader;
import net.liopyu.lioloader.pack.LioloaderGlobalResourcePacks;
import net.minecraft.server.packs.PackType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.event.AddPackFindersEvent;

import java.nio.file.Path;

@EventBusSubscriber(modid = Lioloader.MOD_ID, value = Dist.CLIENT)
public final class LioloaderNeoForgeClientPacks {
    @SubscribeEvent
    public static void addPackFinders(AddPackFindersEvent event) {
        if (event.getPackType() != PackType.CLIENT_RESOURCES) return;

        Path gameDir = FMLPaths.GAMEDIR.get();
        Path globalDir = LioloaderGlobalResourcePacks.globalResourcePacksDir(gameDir);

        event.addRepositorySource(LioloaderGlobalResourcePacks.repositorySource(globalDir));
    }
}
