package org.spongepowered.gradle.vanilla.repository.mappings.entry;

import net.minecraftforge.srgutils.IMappingFile;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.gradle.api.Project;
import org.spongepowered.gradle.vanilla.MinecraftExtension;
import org.spongepowered.gradle.vanilla.internal.model.Download;
import org.spongepowered.gradle.vanilla.internal.repository.modifier.ArtifactModifier;
import org.spongepowered.gradle.vanilla.repository.MinecraftPlatform;
import org.spongepowered.gradle.vanilla.repository.MinecraftResolver;
import org.spongepowered.gradle.vanilla.repository.mappings.format.FartMappingFormat;
import org.spongepowered.gradle.vanilla.resolver.HashAlgorithm;

import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class OfficialMappingsEntry extends ImmutableMappingsEntry {
    public static final String NAME = "official";

    public OfficialMappingsEntry(Project project, MinecraftExtension extension) {
        super(project, extension, NAME, FartMappingFormat.NAME);
    }

    @Override
    protected <T extends MappingsEntry> IMappingFile doResolve(
            MinecraftResolver.@NonNull Context context,
            MinecraftResolver.@NonNull MinecraftEnvironment environment,
            @NonNull MinecraftPlatform platform,
            ArtifactModifier.@NonNull SharedArtifactSupplier sharedArtifactSupplier,
            @NonNull Set<String> alreadySeen) {
        @SuppressWarnings("unchecked")
        CompletableFuture<IMappingFile>[] mappingsFutures = platform.activeSides().stream().map(side -> {
            Download mappingsDownload = environment.metadata().requireDownload(side.mappingsArtifact());
            return context.downloader().downloadAndValidate(
                    mappingsDownload.url(),
                    sharedArtifactSupplier.supply(side.name().toLowerCase(Locale.ROOT) + "_m-obf", "mappings", "txt"),
                    HashAlgorithm.SHA1,
                    mappingsDownload.sha1()
            ).thenApplyAsync(downloadResult -> {
                if (!downloadResult.isPresent()) {
                    throw new IllegalArgumentException("No mappings were available for Minecraft " + environment.metadata().id() + "side " + side.name()
                            + "! Official mappings are only available for releases 1.14.4 and newer.");
                }
                IMappingFile mappings;
                try {
                    mappings = IMappingFile.load(downloadResult.get().toFile());
                } catch (IOException e) {
                    throw new CompletionException(e);
                }
                return mappings.reverse(); // proguard mappings are backwards
            }, context.executor());
        }).toArray(CompletableFuture[]::new);
        CompletableFuture<IMappingFile> mappingFuture = CompletableFuture.allOf(mappingsFutures)
                .thenApplyAsync($ -> Arrays.stream(mappingsFutures).map(CompletableFuture::join).reduce(MappingsEntry::goodChain).orElseThrow(() -> new CompletionException(new RuntimeException("TODO what is going on here"))));


        return mappingFuture.join();
    }

    @Override
    public @NonNull String computeStateKey(boolean isFrom) {
        return isFrom ? super.computeStateKey(true) : "";
    }
}
