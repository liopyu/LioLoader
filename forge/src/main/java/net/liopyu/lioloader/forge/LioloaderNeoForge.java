package net.liopyu.lioloader.util;

import net.liopyu.lioloader.Lioloader;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;

@Mod(Lioloader.MOD_ID)
public final class LioloaderNeoForge {
    public LioloaderNeoForge() {
        // Run our common setup.
        Lioloader.init(FMLPaths.GAMEDIR.get());
    }
}
