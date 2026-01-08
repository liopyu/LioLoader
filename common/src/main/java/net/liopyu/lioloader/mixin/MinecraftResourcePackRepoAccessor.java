package net.liopyu.lioloader.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.repository.PackRepository;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Minecraft.class)
public interface MinecraftResourcePackRepoAccessor {
    @Accessor("resourcePackRepository")
    PackRepository lioloader$getResourcePackRepository();
}
