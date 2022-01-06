package org.spongepowered.gradle.vanilla.repository.mappings.format;


import net.minecraftforge.srgutils.IMappingFile;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.gradle.vanilla.repository.MinecraftResolver;
import org.spongepowered.gradle.vanilla.repository.mappings.entry.MappingsEntry;

import java.io.IOException;
import java.nio.file.Path;

public class FartMappingFormat extends MappingFormat<@NonNull MappingsEntry> {
    public static final String NAME = "fart";

    public FartMappingFormat() {
        super(MappingsEntry.class);
    }

    @Override
    public @NonNull String getName() {
        return NAME;
    }

    @Override
    public IMappingFile read(final @NonNull Path file, final @NonNull MappingsEntry entry,
                             MinecraftResolver.Context context) throws IOException {
        return IMappingFile.load(file.toFile());
    }
}
