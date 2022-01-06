package org.spongepowered.gradle.vanilla.repository.mappings.entry;

import net.minecraftforge.srgutils.IMappingFile;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.gradle.api.Project;
import org.spongepowered.gradle.vanilla.MinecraftExtension;
import org.spongepowered.gradle.vanilla.internal.repository.modifier.ArtifactModifier;
import org.spongepowered.gradle.vanilla.repository.MinecraftPlatform;
import org.spongepowered.gradle.vanilla.repository.MinecraftResolver;

import javax.annotation.Nullable;
import java.util.Set;

public class ObfMappingsEntry extends ImmutableMappingsEntry {
    public static final String NAME = "obfuscated";

    public ObfMappingsEntry(Project project, MinecraftExtension extension) {
        super(project, extension, NAME, "none");
    }

    @Override
    public @NonNull String computeStateKey(boolean isFrom) {
        return isFrom ? "" : super.computeStateKey(false);
    }

    @Override
    @Nullable
    protected <T extends MappingsEntry> IMappingFile doResolve(
            MinecraftResolver.@NonNull Context context,
            MinecraftResolver.@NonNull MinecraftEnvironment environment,
            @NonNull MinecraftPlatform platform,
            ArtifactModifier.@NonNull SharedArtifactSupplier sharedArtifactSupplier,
            @NonNull Set<String> alreadySeen
    ) {
        return null;
    }
}
