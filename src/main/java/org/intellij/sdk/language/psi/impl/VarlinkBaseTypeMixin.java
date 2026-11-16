package org.intellij.sdk.language.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.ContributedReferenceHost;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistry;
import org.jetbrains.annotations.NotNull;

/**
 * Base implementation for the {@code baseType} rule. A base type can be a bare
 * identifier naming a custom {@code type}; such a usage is a reference to the
 * declaration. Making the element a {@link ContributedReferenceHost} and routing
 * {@link #getReferences()} through the registry is what lets a
 * {@code PsiReferenceContributor} attach references to it — plain
 * {@link ASTWrapperPsiElement}s never consult the registry.
 */
public abstract class VarlinkBaseTypeMixin extends ASTWrapperPsiElement implements ContributedReferenceHost {

    public VarlinkBaseTypeMixin(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public PsiReference @NotNull [] getReferences() {
        return ReferenceProvidersRegistry.getReferencesFromProviders(this);
    }
}
