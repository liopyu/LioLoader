package net.liopyu.lioloader;

import net.liopyu.lioloader.pack.LioloaderGlobalDatapacks;
import net.liopyu.lioloader.pack.LioloaderGlobalResourcePacks;
import net.liopyu.lioloader.pack.LioloaderPackLoadOrder;

import java.nio.file.Path;

public final class Lioloader {
    public static final String MOD_ID = "lioloader";
    private static Path gameDir;

    public static void init(Path gameDir) {
        Lioloader.gameDir = gameDir;
        LioloaderGlobalDatapacks.ensureDirs(gameDir);
        LioloaderGlobalResourcePacks.ensureDirs(gameDir);
        LioloaderPackLoadOrder.ensureOrderFiles(gameDir);
    }

    public static Path gameDir() {
        return gameDir;
    }
}
