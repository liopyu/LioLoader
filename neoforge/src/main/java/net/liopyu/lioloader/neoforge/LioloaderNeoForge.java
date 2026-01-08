package net.liopyu.lioloader.neoforge;

import net.liopyu.lioloader.Lioloader;
import net.neoforged.fml.common.Mod;

@Mod(Lioloader.MOD_ID)
public final class LioloaderNeoForge {
    public LioloaderNeoForge() {
        // Run our common setup.
        Lioloader.init();
    }
}
