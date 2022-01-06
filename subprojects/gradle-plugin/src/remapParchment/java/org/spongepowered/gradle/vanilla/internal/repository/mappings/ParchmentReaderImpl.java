package org.spongepowered.gradle.vanilla.internal.repository.mappings;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.minecraftforge.srgutils.IMappingBuilder;
import net.minecraftforge.srgutils.IMappingFile;
import org.parchmentmc.feather.io.gson.MDCGsonAdapterFactory;
import org.parchmentmc.feather.io.gson.OffsetDateTimeAdapter;
import org.parchmentmc.feather.io.gson.SimpleVersionAdapter;
import org.parchmentmc.feather.io.gson.metadata.MetadataAdapterFactory;
import org.parchmentmc.feather.mapping.MappingDataContainer;
import org.parchmentmc.feather.mapping.VersionedMappingDataContainer;
import org.parchmentmc.feather.util.SimpleVersion;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class ParchmentReaderImpl {
    // Parchment javadoc is different from tinyv2 comments, so we use a different meta key
    private static final String JAVADOC = "parchment_javadoc";
    private static final Gson GSON = new GsonBuilder()
            // Required for `MappingDataContainer` and inner data classes
            .registerTypeAdapterFactory(new MDCGsonAdapterFactory())
            // Required for `MappingDataContainer`s and `SourceMetadata`
            .registerTypeAdapter(SimpleVersion.class, new SimpleVersionAdapter())
            // Required for the metadata classes (`SourceMetadata`, `MethodReference`, etc.) and `Named`
            .registerTypeAdapterFactory(new MetadataAdapterFactory())
            // Required for parsing manifests: `LauncherManifest`, `VersionManifest`, and their inner data classes
            .registerTypeAdapter(OffsetDateTime.class, new OffsetDateTimeAdapter())
            .create();

    public static IMappingFile read(Path file) throws IOException {
        final MappingDataContainer parchmentData;

        try (BufferedReader reader = Files.newBufferedReader(file)) {
            parchmentData = GSON.fromJson(reader, VersionedMappingDataContainer.class);
        }

        IMappingBuilder builder = IMappingBuilder.create();

        for (final MappingDataContainer.ClassData parchmentClass : parchmentData.getClasses()) {
            final IMappingBuilder.IClass classMapping = builder.addClass(parchmentClass.getName(), parchmentClass.getName());
            final String classJavadoc = convertList(parchmentClass.getJavadoc());

            if (classJavadoc != null) {
                classMapping.meta(JAVADOC, classJavadoc);
            }

            for (final MappingDataContainer.FieldData parchmentField : parchmentClass.getFields()) {
                final IMappingBuilder.IField fieldMapping = classMapping.field(parchmentField.getName(), parchmentField.getName());
                fieldMapping.descriptor(parchmentField.getDescriptor());
                final String javadoc = convertList(parchmentField.getJavadoc());

                if (javadoc != null) {
                    fieldMapping.meta(JAVADOC, javadoc);
                }
            }

            for (final MappingDataContainer.MethodData parchmentMethod : parchmentClass.getMethods()) {
                final IMappingBuilder.IMethod methodMapping = classMapping.method(parchmentMethod.getDescriptor(), parchmentMethod.getName(), parchmentMethod.getName());
                final String methodJavadoc = convertList(parchmentMethod.getJavadoc());

                if (methodJavadoc != null) {
                    methodMapping.meta(JAVADOC, methodJavadoc);
                }

                for (final MappingDataContainer.ParameterData parchmentParameter : parchmentMethod.getParameters()) {
                    final String name = parchmentParameter.getName();
                    final IMappingBuilder.IParameter parameterMapping;

                    if (name == null) {
                        parameterMapping = methodMapping.parameter(parchmentParameter.getIndex());
                    } else {
                        parameterMapping = methodMapping.parameter(parchmentParameter.getIndex(), null, name);
                    }

                    final String javadoc = parchmentParameter.getJavadoc();

                    if (javadoc != null) {
                        parameterMapping.meta(JAVADOC, methodJavadoc);
                    }
                }
            }
        }

        for (final MappingDataContainer.PackageData parchmentPackage : parchmentData.getPackages()) {
            final IMappingBuilder.IPackage packageMapping = builder.addPackage(parchmentPackage.getName(), parchmentPackage.getName());
            final String javadoc = convertList(parchmentPackage.getJavadoc());

            if (javadoc != null) {
                packageMapping.meta(JAVADOC, javadoc);
            }
        }

        return builder.build().getMap("left", "right"); // TODO
    }

    private static @Nullable String convertList(List<String> list) {
        if (list.isEmpty()) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        Iterator<String> iter = list.iterator();
        while (true) {
            sb.append(iter.next());
            if (iter.hasNext()) {
                sb.append("\n");
            } else {
                return sb.toString();
            }
        }
    }
}
