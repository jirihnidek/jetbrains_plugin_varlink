package org.intellij.sdk.language;

import com.intellij.lang.HelpID;
import com.intellij.lang.cacheBuilder.DefaultWordsScanner;
import com.intellij.lang.cacheBuilder.WordsScanner;
import com.intellij.lang.findUsages.FindUsagesProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.TokenSet;
import org.intellij.sdk.language.psi.VarlinkErrorDeclaration;
import org.intellij.sdk.language.psi.VarlinkField;
import org.intellij.sdk.language.psi.VarlinkMethodDeclaration;
import org.intellij.sdk.language.psi.VarlinkNamedElement;
import org.intellij.sdk.language.psi.VarlinkTypeAlias;
import org.intellij.sdk.language.psi.VarlinkTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Enables "Find Usages" for the named elements of a Varlink file. In practice the
 * references that resolve within a file are the custom {@code type} usages, so
 * this is what makes "Find Usages" on a {@code type} declaration list every place
 * it is used.
 */
public class VarlinkFindUsagesProvider implements FindUsagesProvider {

    @Nullable
    @Override
    public WordsScanner getWordsScanner() {
        return new DefaultWordsScanner(
                new VarlinkLexerAdapter(),
                TokenSet.create(VarlinkTypes.IDENTIFIER),
                VarlinkTokenSets.COMMENTS,
                TokenSet.EMPTY);
    }

    @Override
    public boolean canFindUsagesFor(@NotNull PsiElement element) {
        return element instanceof VarlinkNamedElement;
    }

    @Nullable
    @Override
    public String getHelpId(@NotNull PsiElement element) {
        return HelpID.FIND_OTHER_USAGES;
    }

    @NotNull
    @Override
    public String getType(@NotNull PsiElement element) {
        if (element instanceof VarlinkTypeAlias) {
            return "type";
        }
        if (element instanceof VarlinkMethodDeclaration) {
            return "method";
        }
        if (element instanceof VarlinkErrorDeclaration) {
            return "error";
        }
        if (element instanceof VarlinkField) {
            return "field";
        }
        return "";
    }

    @NotNull
    @Override
    public String getDescriptiveName(@NotNull PsiElement element) {
        if (element instanceof VarlinkNamedElement named) {
            String name = named.getName();
            if (name != null) {
                return name;
            }
        }
        return "";
    }

    @NotNull
    @Override
    public String getNodeText(@NotNull PsiElement element, boolean useFullName) {
        return getDescriptiveName(element);
    }
}
