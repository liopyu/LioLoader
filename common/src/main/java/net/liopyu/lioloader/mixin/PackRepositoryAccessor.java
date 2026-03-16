package net.liopyu.lioloader.mixin;

import net.minecraft.server.packs.repository.RepositorySource;
import net.minecraft.server.packs.repository.PackRepository;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;

@Mixin(PackRepository.class)
public interface PackRepositoryAccessor {
    @Accessor("sources")
    Set<RepositorySource> lioloader$getSources();

    @Accessor("sources")
    void lioloader$setSources(Set<RepositorySource> sources);
}
