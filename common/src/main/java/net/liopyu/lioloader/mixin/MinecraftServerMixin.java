package net.liopyu.lioloader.mixin;

import net.liopyu.lioloader.Lioloader;
import net.liopyu.lioloader.pack.LioloaderGlobalDatapacks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.RepositorySource;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.WorldDataConfiguration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.nio.file.Path;
import java.util.Collection;

@Mixin(value = MinecraftServer.class, priority = 1001)
public final class MinecraftServerMixin {
    @Inject(
            method = "configurePackRepository(Lnet/minecraft/server/packs/repository/PackRepository;Lnet/minecraft/world/level/WorldDataConfiguration;ZZ)Lnet/minecraft/world/level/WorldDataConfiguration;",
            at = @At("HEAD")
    )
    private static void lioloader$configurePackRepositoryHead(
            PackRepository repo,
            WorldDataConfiguration config,
            boolean safeMode,
            boolean initMode,
            CallbackInfoReturnable<WorldDataConfiguration> cir
    ) {
        Path gameDir = Lioloader.gameDir();
        if (gameDir == null) return;

        Path globalDir = LioloaderGlobalDatapacks.globalDatapacksDir(gameDir);
        var source = LioloaderGlobalDatapacks.repositorySource(gameDir);

        PackRepositoryAccessor acc = (PackRepositoryAccessor) repo;
        java.util.Set<RepositorySource> old = acc.lioloader$getSources();

        if (old != null && old.contains(source)) {
            return;
        }

        java.util.LinkedHashSet<RepositorySource> next = new java.util.LinkedHashSet<>();
        if (old != null) next.addAll(old);
        next.add(source);

        acc.lioloader$setSources(next);

        com.mojang.logging.LogUtils.getLogger().info(
                "[Lioloader] configurePackRepository(HEAD): injected source dir={} safeMode={} initMode={}",
                globalDir, safeMode, initMode
        );
    }

}
