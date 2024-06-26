// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.psi.impl;

import com.intellij.lang.jvm.JvmMethod;
import com.intellij.lang.jvm.JvmTypeDeclaration;
import com.intellij.lang.jvm.JvmTypeParameter;
import com.intellij.lang.jvm.types.JvmSubstitutor;
import com.intellij.lang.jvm.types.JvmType;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class JvmPsiConversionHelperImpl implements JvmPsiConversionHelper {

  @Override
  public PsiClass convertTypeDeclaration(@Nullable JvmTypeDeclaration typeDeclaration) {
    if (typeDeclaration instanceof PsiClass) return (PsiClass)typeDeclaration;
    throw new RuntimeException("TODO");
  }

  @Override
  public @NotNull PsiTypeParameter convertTypeParameter(@NotNull JvmTypeParameter typeParameter) {
    if (typeParameter instanceof PsiTypeParameter) return (PsiTypeParameter)typeParameter;
    throw new RuntimeException("TODO");
  }

  @Override
  public @NotNull PsiType convertType(@NotNull JvmType type) {
    if (type instanceof PsiType) return (PsiType)type;
    throw new RuntimeException("TODO");
  }

  @Override
  public @NotNull PsiSubstitutor convertSubstitutor(@NotNull JvmSubstitutor substitutor) {
    if (substitutor instanceof PsiJvmSubstitutor) return ((PsiJvmSubstitutor)substitutor).getPsiSubstitutor();
    PsiSubstitutor result = PsiSubstitutor.EMPTY;
    for (JvmTypeParameter parameter : substitutor.getTypeParameters()) {
      final PsiTypeParameter psiTypeParameter = convertTypeParameter(parameter);
      final JvmType substitution = substitutor.substitute(parameter);
      final PsiType psiType = substitution == null ? null : convertType(substitution);
      result = result.put(psiTypeParameter, psiType);
    }
    return result;
  }

  @Override
  public @NotNull PsiMethod convertMethod(@NotNull JvmMethod method) {
    if (method instanceof PsiMethod) return (PsiMethod)method;
    throw new RuntimeException("TODO");
  }
}
