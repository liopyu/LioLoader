package net.liopyu.lioloader.mixin;

import net.liopyu.lioloader.Lioloader;
import net.liopyu.lioloader.pack.LioloaderGlobalResourcePacks;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.RepositorySource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.Set;

@Mixin(PackRepository.class)
public final class PackRepositoryReloadMixin {
    @Inject(method = "reload", at = @At("HEAD"))
    private void lioloader$onReload(CallbackInfo ci) {
        Path gameDir = Lioloader.gameDir();
        if (gameDir == null)
            return;

        Path globalDir = LioloaderGlobalResourcePacks.globalResourcePacksDir(gameDir);
        RepositorySource globalSource = LioloaderGlobalResourcePacks.repositorySource(globalDir);

        PackRepository self = (PackRepository) (Object) this;

        try {
            // This is for Fabric compat as it does not come shipped with unobfuscated bytecode like Neoforge does
            Field sourcesField = null;
            String[] possibleNames = { "sources", "field_14227", "a" };

            for (String name : possibleNames) {
                try {
                    sourcesField = PackRepository.class.getDeclaredField(name);
                    break;
                } catch (NoSuchFieldException e) {
                }
            }

            if (sourcesField == null) {
                com.mojang.logging.LogUtils.getLogger().error(
                        "[Lioloader] Could not find sources field in PackRepository. Tried: java.util.Set sources, field_14227, a");
                return;
            }

            sourcesField.setAccessible(true);
            @SuppressWarnings("unchecked")
            Set<RepositorySource> sources = (Set<RepositorySource>) sourcesField.get(self);

            if (!sources.contains(globalSource)) {
                sources.add(globalSource);
                com.mojang.logging.LogUtils.getLogger().info(
                        "[Lioloader] Injecting global resourcepack source before PackRepository.reload(). dir={}",
                        globalDir);
            }
        } catch (Exception e) {
            com.mojang.logging.LogUtils.getLogger().error("[Lioloader] Failed to inject global resourcepack source", e);
        }
    }
}
