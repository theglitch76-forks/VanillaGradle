package org.spongepowered.gradle.vanilla.repository.mappings.format;

import net.minecraftforge.srgutils.IMappingFile;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.gradle.api.Named;
import org.spongepowered.gradle.vanilla.repository.MinecraftResolver;
import org.spongepowered.gradle.vanilla.repository.mappings.entry.MappingsEntry;

import java.io.IOException;
import java.nio.file.Path;

public abstract class MappingFormat<T extends MappingsEntry> implements Named {
    private final Class<T> entryType;

    protected MappingFormat(Class<T> entryType) {
        this.entryType = entryType;
    }

    public Class<T> entryType() {
        return entryType;
    }

    public abstract IMappingFile read(final Path file, final T entry, MinecraftResolver.Context context) throws IOException;

    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof MappingFormat && ((MappingFormat<@NonNull ?>) other).getName().equals(getName());
    }
}