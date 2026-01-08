package net.liopyu.lioloader;

import net.liopyu.lioloader.pack.LioloaderGlobalDatapacks;
import net.liopyu.lioloader.pack.LioloaderGlobalResourcePacks;

import java.nio.file.Path;

public final class Lioloader {
    public static final String MOD_ID = "lioloader";

    public static void init(Path gameDir) {
      LioloaderGlobalDatapacks.ensureDirs(gameDir);
       LioloaderGlobalResourcePacks.ensureDirs(gameDir);
    }
}
