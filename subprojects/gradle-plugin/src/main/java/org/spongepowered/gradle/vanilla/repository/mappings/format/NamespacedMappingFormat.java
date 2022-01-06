package org.spongepowered.gradle.vanilla.repository.mappings.format;


import net.minecraftforge.srgutils.IMappingBuilder;
import net.minecraftforge.srgutils.IMappingFile;
import net.minecraftforge.srgutils.INamedMappingFile;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.gradle.internal.impldep.org.bouncycastle.pqc.jcajce.provider.NH;
import org.spongepowered.gradle.vanilla.repository.MinecraftResolver;
import org.spongepowered.gradle.vanilla.repository.mappings.entry.MappingsEntry;
import org.spongepowered.gradle.vanilla.repository.mappings.entry.NamespacedMappingsEntry;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class NamespacedMappingFormat extends MappingFormat<@NonNull MappingsEntry> {
    public static final String NAME = "namespaced";

    public NamespacedMappingFormat() {
        super(MappingsEntry.class);
    }

    @Override
    public @NonNull String getName() {
        return NAME;
    }

    @Override
    public IMappingFile read(final @NonNull Path file, final @NonNull MappingsEntry entry,
                             MinecraftResolver.Context context) throws IOException {
        return readNamespaced(file, entry);
    }

    public static IMappingFile readNamespaced(final @NonNull Path file, final @NonNull MappingsEntry entry) throws IOException {
        return readNamespaced(Files.newInputStream(file), entry);
    }
    public static IMappingFile readNamespaced(final @NonNull InputStream stream, final @NonNull MappingsEntry entry) throws IOException {
        INamedMappingFile mappings = INamedMappingFile.load(stream);
        if (mappings.getNames().size() == 2) {
            return mappings.getMap(mappings.getNames().get(0), mappings.getNames().get(1));
        } else {
            if (entry instanceof NamespacedMappingsEntry) {
                NamespacedMappingsEntry cast = (NamespacedMappingsEntry) entry;
                return mappings.getMap(cast.from().get(), cast.to().get());
            } else {
                throw new IllegalArgumentException("The provided mappings file has more than two namespaces. " +
                        "Use a NamespacedMappingsEntry to declare which namespaces should be used.");
            }
        }
    }
}
