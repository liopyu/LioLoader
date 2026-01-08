package net.liopyu.lioloader.fabric.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.liopyu.lioloader.mixin.MinecraftResourcePackRepoAccessor;
import net.liopyu.lioloader.pack.LioloaderGlobalResourcePacks;

import java.nio.file.Path;

public final class LioloaderFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            Path gameDir = FabricLoader.getInstance().getGameDir();
            Path globalDir = LioloaderGlobalResourcePacks.globalResourcePacksDir(gameDir);

            net.minecraft.server.packs.repository.PackRepository repo =
                    ((MinecraftResourcePackRepoAccessor) client).lioloader$getResourcePackRepository();

            ((net.liopyu.lioloader.mixin.PackRepositoryAccessor) repo)
                    .lioloader$getSources()
                    .add(LioloaderGlobalResourcePacks.repositorySource(globalDir));

            repo.reload();
            client.reloadResourcePacks();
        });
    }
}
