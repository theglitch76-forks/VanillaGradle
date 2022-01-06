package org.spongepowered.gradle.vanilla.repository.mappings.format;

import net.minecraftforge.srgutils.IMappingFile;
import net.minecraftforge.srgutils.INamedMappingFile;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.gradle.vanilla.repository.MinecraftResolver;
import org.spongepowered.gradle.vanilla.repository.mappings.entry.MappingsEntry;
import org.spongepowered.gradle.vanilla.repository.mappings.entry.NamespacedMappingsEntry;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

public class TinyMappingFormat extends MappingFormat<@NonNull MappingsEntry> {
    public static final String NAME = "tiny";

    public TinyMappingFormat() {
        super(MappingsEntry.class);
    }

    @Override
    public @NonNull String getName() {
        return NAME;
    }

    @Override
    public IMappingFile read(final @NonNull Path file, final @NonNull MappingsEntry entry,
                             MinecraftResolver.Context context) throws IOException {
        try (ZipFile zip = new ZipFile(file.toFile())) {
            ZipEntry zipEntry = zip.getEntry("mappings/mappings.tiny");
            if (zipEntry == null) {
                // TODO: Quilt's hashed mojmap is special and uses it's own folder for some reason
                zipEntry = zip.getEntry("hashed/mappings.tiny");
            }
            if (zipEntry != null) {
               return NamespacedMappingFormat.readNamespaced(zip.getInputStream(zipEntry), entry);
            }
        } catch (ZipException e) {
            // not a zip file
            return NamespacedMappingFormat.readNamespaced(file, entry);
        }

        throw new FileNotFoundException("Could not find mappings/mappings.tiny");
    }
}

