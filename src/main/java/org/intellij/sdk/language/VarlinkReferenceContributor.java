package org.intellij.sdk.language;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.psi.PsiReferenceRegistrar;
import com.intellij.util.ProcessingContext;
import org.intellij.sdk.language.psi.VarlinkBaseType;
import org.intellij.sdk.language.psi.VarlinkTypes;
import org.jetbrains.annotations.NotNull;

/**
 * Contributes a {@link VarlinkTypeReference} for every custom type usage in a
 * Varlink file. A usage is a base type that is a bare {@code IDENTIFIER} (i.e. not
 * a built-in {@code basicType} and not an inline {@code struct}); such an
 * identifier names a {@code type} alias declared elsewhere in the same file.
 */
public class VarlinkReferenceContributor extends PsiReferenceContributor {

    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
        registrar.registerReferenceProvider(
                PlatformPatterns.psiElement(VarlinkBaseType.class),
                new PsiReferenceProvider() {
                    @Override
                    public PsiReference @NotNull [] getReferencesByElement(@NotNull PsiElement element,
                                                                           @NotNull ProcessingContext context) {
                        VarlinkBaseType baseType = (VarlinkBaseType) element;
                        // Built-in types and inline structs are not references to a declaration.
                        if (baseType.getBasicType() != null || baseType.getStruct() != null) {
                            return PsiReference.EMPTY_ARRAY;
                        }
                        ASTNode identifier = baseType.getNode().findChildByType(VarlinkTypes.IDENTIFIER);
                        if (identifier == null) {
                            return PsiReference.EMPTY_ARRAY;
                        }
                        String name = identifier.getText();
                        if (name.isEmpty()) {
                            return PsiReference.EMPTY_ARRAY;
                        }
                        TextRange range = identifier.getTextRange().shiftLeft(baseType.getTextRange().getStartOffset());
                        return new PsiReference[]{new VarlinkTypeReference(baseType, range, name)};
                    }
                });
    }
}
