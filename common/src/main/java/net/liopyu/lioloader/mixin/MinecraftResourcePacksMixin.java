package net.liopyu.lioloader.mixin;

import net.liopyu.lioloader.Lioloader;
import net.liopyu.lioloader.pack.LioloaderGlobalResourcePacks;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.repository.PackRepository;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.file.Path;

@Mixin(Minecraft.class)
public final class MinecraftResourcePacksMixin {
    @Shadow
    @Final
    private PackRepository resourcePackRepository;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void lioloader$injectGlobalResourcePacks(CallbackInfo ci) {
        Path gameDir = Lioloader.gameDir();
        if (gameDir == null) return;

        Path globalDir = LioloaderGlobalResourcePacks.globalResourcePacksDir(gameDir);

        ((PackRepositoryAccessor) this.resourcePackRepository)
                .lioloader$getSources()
                .add(LioloaderGlobalResourcePacks.repositorySource(globalDir));

        this.resourcePackRepository.reload();

        com.mojang.logging.LogUtils.getLogger().info(
                "[Lioloader] Injected global resourcepack source into client PackRepository. dir={} available={} selected={}",
                globalDir,
                this.resourcePackRepository.getAvailableIds(),
                this.resourcePackRepository.getSelectedIds()
        );
    }
}
