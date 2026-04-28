package net.liopyu.lioloader.neoforge;

import com.mojang.logging.LogUtils;
import net.liopyu.lioloader.Lioloader;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;

@Mod(Lioloader.MOD_ID)
public final class LioloaderNeoForge {
    public LioloaderNeoForge() {
        Lioloader.init(FMLPaths.GAMEDIR.get());
    }
}
