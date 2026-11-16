package org.intellij.sdk.language;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.util.IncorrectOperationException;
import org.intellij.sdk.language.psi.VarlinkElementFactory;
import org.intellij.sdk.language.psi.VarlinkTypeAlias;
import org.intellij.sdk.language.psi.VarlinkTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A reference from a custom type usage (a bare {@code IDENTIFIER} in a base-type
 * position) to the {@link VarlinkTypeAlias} that declares it. This powers
 * Ctrl-click navigation, find-usages and rename for types within a Varlink file.
 *
 * <p>Only types declared in the same file can be referenced, so resolution is
 * scoped to the containing file.
 */
public class VarlinkTypeReference extends PsiReferenceBase<PsiElement> {

    private final String name;

    public VarlinkTypeReference(@NotNull PsiElement element, @NotNull TextRange rangeInElement, @NotNull String name) {
        super(element, rangeInElement);
        this.name = name;
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        PsiFile file = myElement.getContainingFile();
        for (VarlinkTypeAlias typeAlias : VarlinkUtil.findTypeAliases(file)) {
            if (name.equals(typeAlias.getName())) {
                return typeAlias;
            }
        }
        return null;
    }

    @Override
    public PsiElement handleElementRename(@NotNull String newName) throws IncorrectOperationException {
        ASTNode idNode = myElement.getNode().findChildByType(VarlinkTypes.IDENTIFIER);
        if (idNode != null) {
            PsiElement newIdentifier = VarlinkElementFactory.createIdentifier(myElement.getProject(), newName);
            if (newIdentifier == null) {
                throw new IncorrectOperationException("Invalid Varlink type name: " + newName);
            }
            idNode.getPsi().replace(newIdentifier);
        }
        return myElement;
    }
}
