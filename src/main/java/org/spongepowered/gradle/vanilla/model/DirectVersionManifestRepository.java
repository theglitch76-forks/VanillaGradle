/*
 * This file is part of VanillaGradle, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spongepowered.gradle.vanilla.model;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.gradle.vanilla.Constants;
import org.spongepowered.gradle.vanilla.util.GsonUtils;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

final class DirectVersionManifestRepository implements VersionManifestRepository {
    private static final Logger LOGGER = LoggerFactory.getLogger(DirectVersionManifestRepository.class);

    private volatile @Nullable VersionManifestV2 manifest;
    private final Map<String, VersionDescriptor.Full> injectedVersions = new ConcurrentHashMap<>();

    @Override
    public VersionManifestV2 manifest() throws IOException {
        @Nullable VersionManifestV2 manifest = this.manifest;
        if (manifest == null) {
            final URL url = new URL(Constants.Manifests.API_V2_ENDPOINT);
            this.manifest = manifest = GsonUtils.parseFromJson(url, VersionManifestV2.class);
        }
        return manifest;
    }

    @Override
    public List<? extends VersionDescriptor> availableVersions() {
        try {
            final List<VersionDescriptor.Reference> manifestVersions = this.manifest().versions();
            if (this.injectedVersions.isEmpty()) {
                return manifestVersions;
            } else {
                final List<VersionDescriptor> versions = new ArrayList<>(manifestVersions);
                versions.addAll(this.injectedVersions.values());
                return versions;
            }
        } catch (final IOException ex) {
            DirectVersionManifestRepository.LOGGER.error("Failed to query Minecraft version manifest: ", ex);
            return Collections.emptyList();
        }
    }

    @Override
    public Optional<String> latestVersion(final VersionClassifier classifier) {
        try {
            return Optional.ofNullable(this.manifest().latest().get(classifier));
        } catch (final IOException ex) {
            DirectVersionManifestRepository.LOGGER.error("Failed to query Minecraft version manifest: ", ex);
            return Optional.empty();
        }
    }

    @Override
    public Optional<VersionDescriptor.Full> fullVersion(final String versionId) throws IOException {
        if (this.injectedVersions.containsKey(versionId)) {
            return Optional.of(this.injectedVersions.get(versionId));
        }

        final VersionDescriptor.@Nullable Reference result = this.manifest()
            .findDescriptor(versionId).orElse(null);
        if (result == null) { // no such version
            return Optional.empty();
        }

        return Optional.of(GsonUtils.parseFromJson(result.url(), VersionDescriptor.Full.class));
    }

    @Override
    public String inject(final Path localDescriptor) throws IOException {
        final VersionDescriptor.Full descriptor = GsonUtils.parseFromJson(localDescriptor, VersionDescriptor.Full.class);
        this.injectedVersions.put(descriptor.id(), descriptor);
        return descriptor.id();
    }
}
