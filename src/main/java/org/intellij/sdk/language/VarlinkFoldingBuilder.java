package org.intellij.sdk.language;

import com.intellij.lang.ASTNode;
import com.intellij.lang.folding.FoldingBuilderEx;
import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.intellij.sdk.language.psi.VarlinkMethodDeclaration;
import org.intellij.sdk.language.psi.VarlinkStruct;
import org.intellij.sdk.language.psi.VarlinkTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Folds multi-line Varlink definitions.
 *
 * <p>For a type or an error the single parenthesised body is folded. A method,
 * however, has two bodies (input and output) joined by {@code ->}, so folding one
 * of them would only ever collapse half of the definition. Instead the whole
 * method signature — from its input {@code (} to its output {@code )} — is folded
 * as a single region, giving methods the same "fold the definition" behaviour that
 * types and errors already have.
 *
 * <p>Nested struct and enum bodies used inline as field types are folded on their
 * own, so an object inside a method's output can still be collapsed independently.
 * Only bodies that actually span more than one line are foldable.
 */
public class VarlinkFoldingBuilder extends FoldingBuilderEx implements DumbAware {

    @Override
    public FoldingDescriptor @NotNull [] buildFoldRegions(@NotNull PsiElement root,
                                                          @NotNull Document document,
                                                          boolean quick) {
        List<FoldingDescriptor> descriptors = new ArrayList<>();

        // A method folds as a whole: from the input body's '(' to the output body's ')'.
        for (VarlinkMethodDeclaration method : PsiTreeUtil.findChildrenOfType(root, VarlinkMethodDeclaration.class)) {
            TextRange range = methodSignatureRange(method);
            if (range != null && spansMultipleLines(document, range)) {
                descriptors.add(new FoldingDescriptor(method.getNode(), range));
            }
        }

        // Every other body (type / error body, and any nested struct or enum) folds
        // on its own. A method's own input/output bodies are covered by the whole-
        // method region above, so they are skipped here to avoid overlapping regions.
        for (VarlinkStruct struct : PsiTreeUtil.findChildrenOfType(root, VarlinkStruct.class)) {
            if (struct.getParent() instanceof VarlinkMethodDeclaration) {
                continue;
            }
            TextRange range = struct.getTextRange();
            if (spansMultipleLines(document, range)) {
                descriptors.add(new FoldingDescriptor(struct.getNode(), range));
            }
        }

        return descriptors.toArray(FoldingDescriptor.EMPTY_ARRAY);
    }

    @Nullable
    @Override
    public String getPlaceholderText(@NotNull ASTNode node) {
        return node.getElementType() == VarlinkTypes.METHOD_DECLARATION ? "(...) -> (...)" : "(...)";
    }

    @Override
    public boolean isCollapsedByDefault(@NotNull ASTNode node) {
        return false;
    }

    /** The span covering a method's input and output bodies, or {@code null} if absent. */
    @Nullable
    private static TextRange methodSignatureRange(@NotNull VarlinkMethodDeclaration method) {
        List<VarlinkStruct> bodies = PsiTreeUtil.getChildrenOfTypeAsList(method, VarlinkStruct.class);
        if (bodies.isEmpty()) {
            return null;
        }
        int start = bodies.get(0).getTextRange().getStartOffset();
        int end = bodies.get(bodies.size() - 1).getTextRange().getEndOffset();
        return new TextRange(start, end);
    }

    private static boolean spansMultipleLines(@NotNull Document document, @NotNull TextRange range) {
        return document.getLineNumber(range.getStartOffset()) < document.getLineNumber(range.getEndOffset());
    }
}
