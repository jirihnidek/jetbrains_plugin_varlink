package org.intellij.sdk.language;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import org.intellij.sdk.language.psi.VarlinkBaseType;
import org.intellij.sdk.language.psi.VarlinkMethodDeclaration;
import org.intellij.sdk.language.psi.VarlinkTypeAlias;
import org.intellij.sdk.language.psi.VarlinkTypes;
import org.jetbrains.annotations.NotNull;

/**
 * Completes references to custom types (the {@code type} aliases declared in the
 * file) when a type is expected inside a method's input or output parameters,
 * e.g. {@code method Ping(meta: <caret>) -> (status: <caret>)}.
 *
 * <p>Only the base-type position of a field is completed, so field names are not
 * suggested, and only inside a method declaration, so type alias and error bodies
 * are left alone.
 */
public class VarlinkCompletionContributor extends CompletionContributor {

    public VarlinkCompletionContributor() {
        extend(CompletionType.BASIC,
                PlatformPatterns.psiElement(VarlinkTypes.IDENTIFIER),
                new CompletionProvider<>() {
                    @Override
                    protected void addCompletions(@NotNull CompletionParameters parameters,
                                                  @NotNull ProcessingContext context,
                                                  @NotNull CompletionResultSet result) {
                        PsiElement position = parameters.getPosition();

                        // Must be the base-type slot of a field (after the colon), not a field name.
                        if (!(position.getParent() instanceof VarlinkBaseType)) {
                            return;
                        }
                        // Must be inside a method's input/output struct (possibly nested),
                        // not a type alias or error body.
                        if (PsiTreeUtil.getParentOfType(position, VarlinkMethodDeclaration.class) == null) {
                            return;
                        }

                        // Resolve type aliases from the real file (free of the completion dummy).
                        PsiFile file = parameters.getOriginalFile();
                        for (VarlinkTypeAlias typeAlias : VarlinkUtil.findTypeAliases(file)) {
                            String name = typeAlias.getName();
                            if (name != null && !name.isEmpty()) {
                                result.addElement(LookupElementBuilder.create(name)
                                        .withIcon(VarlinkIcons.VARLINK_FILE)
                                        .withTypeText("varlink type"));
                            }
                        }
                    }
                });
    }
}
