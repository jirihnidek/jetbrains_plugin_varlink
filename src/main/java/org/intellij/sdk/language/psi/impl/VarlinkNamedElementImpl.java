package org.intellij.sdk.language.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import org.intellij.sdk.language.VarlinkIcons;
import org.intellij.sdk.language.psi.VarlinkElementFactory;
import org.intellij.sdk.language.psi.VarlinkNamedElement;
import org.intellij.sdk.language.psi.VarlinkTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;

/**
 * Base implementation for every {@link VarlinkNamedElement} (type alias, method,
 * error and field). In all of these the name is the first {@code IDENTIFIER}
 * child, so the name handling is implemented once here and shared by every
 * generated {@code *Impl} that extends this class.
 */
public abstract class VarlinkNamedElementImpl extends ASTWrapperPsiElement implements VarlinkNamedElement {

    public VarlinkNamedElementImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Nullable
    @Override
    public PsiElement getNameIdentifier() {
        ASTNode node = getNode().findChildByType(VarlinkTypes.IDENTIFIER);
        return node != null ? node.getPsi() : null;
    }

    @Nullable
    @Override
    public String getName() {
        PsiElement identifier = getNameIdentifier();
        return identifier != null ? identifier.getText() : null;
    }

    @NotNull
    @Override
    public PsiElement setName(@NotNull String name) throws IncorrectOperationException {
        PsiElement identifier = getNameIdentifier();
        if (identifier != null) {
            PsiElement newIdentifier = VarlinkElementFactory.createIdentifier(getProject(), name);
            if (newIdentifier == null) {
                throw new IncorrectOperationException("Invalid Varlink name: " + name);
            }
            identifier.replace(newIdentifier);
        }
        return this;
    }

    /** Navigation and error-highlighting target the name rather than the keyword. */
    @Override
    public int getTextOffset() {
        PsiElement identifier = getNameIdentifier();
        return identifier != null ? identifier.getTextOffset() : super.getTextOffset();
    }

    @NotNull
    @Override
    public ItemPresentation getPresentation() {
        return new ItemPresentation() {
            @Nullable
            @Override
            public String getPresentableText() {
                return getName();
            }

            @Nullable
            @Override
            public String getLocationString() {
                return getContainingFile() != null ? getContainingFile().getName() : null;
            }

            @Nullable
            @Override
            public Icon getIcon(boolean unused) {
                return VarlinkIcons.VARLINK_FILE;
            }
        };
    }
}
