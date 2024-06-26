// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.gradle.service.syncContributor

import com.intellij.openapi.externalSystem.util.Order
import com.intellij.platform.backend.workspace.workspaceModel
import com.intellij.platform.workspace.jps.entities.ContentRootEntity
import com.intellij.platform.workspace.jps.entities.ModuleEntity
import com.intellij.platform.workspace.storage.MutableEntityStorage
import com.intellij.platform.workspace.storage.entities
import com.intellij.platform.workspace.storage.impl.url.toVirtualFileUrl
import org.jetbrains.annotations.ApiStatus
import org.jetbrains.plugins.gradle.service.project.ProjectResolverContext
import org.jetbrains.plugins.gradle.service.syncAction.GradleSyncContributor
import org.jetbrains.plugins.gradle.service.syncAction.GradleSyncProjectConfigurator.project
import org.jetbrains.plugins.gradle.service.syncContributor.entitites.GradleLinkedProjectEntitySource
import java.nio.file.Path

@ApiStatus.Internal
@Order(GradleSyncContributor.Order.PROJECT_ROOT_CONTRIBUTOR)
class GradleProjectRootSyncContributor : GradleSyncContributor {

  override suspend fun onResolveProjectInfoStarted(context: ProjectResolverContext, storage: MutableEntityStorage) {
    if (context.isPhasedSyncEnabled) {
      configureProjectRoot(context, storage)
    }
  }

  private suspend fun configureProjectRoot(context: ProjectResolverContext, storage: MutableEntityStorage) {
    val project = context.project()
    val virtualFileUrlManager = project.workspaceModel.getVirtualFileUrlManager()

    val linkedProjectRootPath = Path.of(context.projectPath)
    val linkedProjectRootUrl = linkedProjectRootPath.toVirtualFileUrl(virtualFileUrlManager)
    val linkedProjectEntitySource = GradleLinkedProjectEntitySource(linkedProjectRootUrl)

    val contentRootEntities = storage.entities<ContentRootEntity>()
    if (contentRootEntities.all { !isConflictedContentRootEntity(it, linkedProjectEntitySource) }) {
      configureProjectRoot(storage, linkedProjectEntitySource)
    }
  }

  private fun isConflictedContentRootEntity(
    contentRootEntity: ContentRootEntity,
    entitySource: GradleLinkedProjectEntitySource,
  ): Boolean {
    return contentRootEntity.entitySource == entitySource ||
           contentRootEntity.url == entitySource.projectRootUrl
  }

  private fun configureProjectRoot(
    storage: MutableEntityStorage,
    entitySource: GradleLinkedProjectEntitySource,
  ) {
    val moduleEntity = addModuleEntity(storage, entitySource)
    addContentRootEntity(storage, entitySource, moduleEntity)
  }

  private fun addModuleEntity(
    storage: MutableEntityStorage,
    entitySource: GradleLinkedProjectEntitySource,
  ): ModuleEntity.Builder {
    val moduleEntity = ModuleEntity(
      name = entitySource.projectRootUrl.fileName,
      entitySource = entitySource,
      dependencies = emptyList()
    )
    storage addEntity moduleEntity
    return moduleEntity
  }

  private fun addContentRootEntity(
    storage: MutableEntityStorage,
    entitySource: GradleLinkedProjectEntitySource,
    moduleEntity: ModuleEntity.Builder,
  ) {
    storage addEntity ContentRootEntity(
      url = entitySource.projectRootUrl,
      entitySource = entitySource,
      excludedPatterns = emptyList()
    ) {
      module = moduleEntity
    }
  }
}