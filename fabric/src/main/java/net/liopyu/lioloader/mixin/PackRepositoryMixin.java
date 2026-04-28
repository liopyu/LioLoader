package net.liopyu.lioloader.mixin;

import net.liopyu.lioloader.Lioloader;
import net.liopyu.lioloader.pack.LioloaderAssetsFolder;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.RepositorySource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;

@Mixin(PackRepository.class)
public class PackRepositoryMixin {
    @Inject(method = "reload", at = @At("HEAD"))
    private void lioloader$injectAssetsSource(CallbackInfo ci) {
        Path gameDir = Lioloader.gameDir();
        if (gameDir == null) return;

        RepositorySource source = LioloaderAssetsFolder.repositorySource(LioloaderAssetsFolder.packRootDir(gameDir));
        PackRepositoryAccessor self = (PackRepositoryAccessor) (Object) this;
        Set<RepositorySource> current = self.lioloader$getSources();
        if (current.contains(source)) return;

        LinkedHashSet<RepositorySource> updated = new LinkedHashSet<>(current);
        updated.add(source);
        self.lioloader$setSources(updated);
    }
}
