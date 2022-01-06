package org.spongepowered.gradle.vanilla.internal.repository.modifier;

import net.minecraftforge.fart.api.SignatureStripperConfig;
import net.minecraftforge.fart.api.SourceFixerConfig;
import net.minecraftforge.fart.api.Transformer;
import net.minecraftforge.srgutils.IMappingFile;
import net.minecraftforge.srgutils.INamedMappingFile;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.gradle.vanilla.internal.resolver.AsyncUtils;
import org.spongepowered.gradle.vanilla.internal.transformer.Transformers;
import org.spongepowered.gradle.vanilla.repository.MinecraftResolver;
import org.spongepowered.gradle.vanilla.repository.mappings.MappingsContainer;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class MappingsModifier implements ArtifactModifier {
    private static final String KEY = "map"; // custom mapped

    private final MappingsContainer mappings;
    private final String from;
    private final String to;
    private @Nullable String stateKey;

    public MappingsModifier(final MappingsContainer mappings, final String from, final String to) {
        this.mappings = mappings;
        this.from = from;
        this.to = to;
    }

    @Override
    public String key() {
        return KEY;
    }

    @Override
    public String stateKey() {
        if (stateKey == null) {
            this.stateKey = mappings.getByName(from).computeStateKey(true);
            if (!this.stateKey.isEmpty()) {
                this.stateKey += "-";
            }
            this.stateKey += mappings.getByName(to).computeStateKey(false);
        }
        return stateKey;
    }

    @Override
    public CompletableFuture<TransformerProvider> providePopulator(MinecraftResolver.Context context) {
        return AsyncUtils.failableFuture(() -> (result, side, sharedArtifactProvider) -> {
            @Nullable final IMappingFile mappings;
            try {
                mappings = this.mappings.getByName(to).convertFrom(from, context, result, side, sharedArtifactProvider);
            } catch (IOException e) {
                throw new UncheckedIOException("An exception occurred while trying to read mappings", e);
            }
            if (mappings == null) {
                return null;
            }

            return Transformer.renamerFactory(mappings);
        }, context.executor());
    }

    @Override
    public boolean requiresLocalStorage() {
        return false;
    }
}
