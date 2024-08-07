// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.jps.builders.impl;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.util.containers.CollectionFactory;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.builders.BuildOutputConsumer;
import org.jetbrains.jps.builders.BuildTarget;
import org.jetbrains.jps.builders.storage.SourceToOutputMapping;
import org.jetbrains.jps.incremental.CompileContext;
import org.jetbrains.jps.incremental.ModuleBuildTarget;
import org.jetbrains.jps.incremental.artifacts.ArtifactBuildTarget;
import org.jetbrains.jps.incremental.messages.FileGeneratedEvent;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;

@ApiStatus.Internal
public final class BuildOutputConsumerImpl implements BuildOutputConsumer {
  private static final Logger LOG = Logger.getInstance(BuildOutputConsumerImpl.class);
  private final BuildTarget<?> myTarget;
  private final CompileContext myContext;
  private final FileGeneratedEvent myFileGeneratedEvent;
  private final Collection<File> myOutputs;
  private final Set<String> myRegisteredSources = CollectionFactory.createFilePathSet();

  public BuildOutputConsumerImpl(BuildTarget<?> target, CompileContext context) {
    myTarget = target;
    myContext = context;
    myFileGeneratedEvent = new FileGeneratedEvent(target);
    myOutputs = myTarget.getOutputRoots(context);
  }

  private void registerOutput(final File output, boolean isDirectory, Collection<String> sourcePaths) throws IOException {
    final String outputPath = FileUtil.toSystemIndependentName(output.getPath());
    for (File outputRoot : myOutputs) {
      final String outputRootPath = FileUtil.toSystemIndependentName(outputRoot.getPath());
      final String relativePath = FileUtil.getRelativePath(outputRootPath, outputPath, '/');
      if (relativePath != null && !relativePath.startsWith("../")) {
        // the relative path must be under the root or equal to it
        if (isDirectory) {
          addEventsRecursively(output, outputRootPath, relativePath);
        }
        else {
          myFileGeneratedEvent.add(outputRootPath, relativePath);
        }
      }
    }
    final SourceToOutputMapping mapping = myContext.getProjectDescriptor().dataManager.getSourceToOutputMap(myTarget);
    for (String sourcePath : sourcePaths) {
      myRegisteredSources.add(FileUtil.toSystemIndependentName(sourcePath));
      mapping.appendOutput(sourcePath, outputPath);
    }
  }

  private void addEventsRecursively(File output, String outputRootPath, String relativePath) {
    File[] children = output.listFiles();
    if (children == null) {
      myFileGeneratedEvent.add(outputRootPath, relativePath);
    }
    else {
      String prefix = relativePath.isEmpty() || relativePath.equals(".") ? "" : relativePath + "/";
      for (File child : children) {
        addEventsRecursively(child, outputRootPath, prefix + child.getName());
      }
    }
  }

  @Override
  public void registerOutputFile(final @NotNull File outputFile, @NotNull Collection<String> sourcePaths) throws IOException {
    registerOutput(outputFile, false, sourcePaths);
  }

  @Override
  public void registerOutputDirectory(@NotNull File outputDir, @NotNull Collection<String> sourcePaths) throws IOException {
    LOG.assertTrue(!(myTarget instanceof ModuleBuildTarget) && !(myTarget instanceof ArtifactBuildTarget),
                   "'registerOutputDirectory' method cannot be used for target " + myTarget + ", it will break incremental compilation");
    registerOutput(outputDir, true, sourcePaths);
  }

  public int getNumberOfProcessedSources() {
    return myRegisteredSources.size();
  }

  public void fireFileGeneratedEvent() {
    if (!myFileGeneratedEvent.getPaths().isEmpty()) {
      myContext.processMessage(myFileGeneratedEvent);
    }
  }
}
