/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */
package org.jetbrains.kotlin.idea.debugger.evaluate.compilation

import org.jetbrains.kotlin.analysis.api.KaExperimentalApi
import org.jetbrains.kotlin.analysis.api.components.KaCompiledFile
import org.jetbrains.kotlin.backend.common.output.OutputFile
import org.jetbrains.kotlin.codegen.state.GenerationState
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.idea.debugger.base.util.evaluate.ExecutionContext
import org.jetbrains.kotlin.idea.debugger.evaluate.classLoading.GENERATED_CLASS_NAME
import org.jetbrains.kotlin.psi.KtCodeFragment
import org.jetbrains.kotlin.resolve.BindingContext


interface FragmentCompilerCodegen {
    fun initCodegen(classDescriptor: ClassDescriptor, methodDescriptor: FunctionDescriptor, parameterInfo: K1CodeFragmentParameterInfo)
    fun cleanupCodegen()

    fun configureCompiler(compilerConfiguration: CompilerConfiguration)
    fun configureGenerationState(
        builder: GenerationState.Builder,
        bindingContext: BindingContext,
        compilerConfiguration: CompilerConfiguration,
        classDescriptor: ClassDescriptor,
        methodDescriptor: FunctionDescriptor,
        parameterInfo: K1CodeFragmentParameterInfo
    )

    fun computeFragmentParameters(
        executionContext: ExecutionContext,
        codeFragment: KtCodeFragment,
        bindingContext: BindingContext
    ): K1CodeFragmentParameterInfo

    fun extractResult(
      methodDescriptor: FunctionDescriptor,
      parameterInfo: K1CodeFragmentParameterInfo,
      generationState: GenerationState
    ): CodeFragmentCompiler.CompilationResult
}

private fun isCodeFragmentClassPath(path: String): Boolean {
    return path == "$GENERATED_CLASS_NAME.class"
            || (path.startsWith("$GENERATED_CLASS_NAME\$") && path.endsWith(".class"))
}

fun List<OutputFile>.filterCodeFragmentClassFiles(): List<OutputFile> {
    return filter { isCodeFragmentClassPath(it.relativePath) }
}

@KaExperimentalApi
val KaCompiledFile.isCodeFragmentClassFile: Boolean
    get() = isCodeFragmentClassPath(path)