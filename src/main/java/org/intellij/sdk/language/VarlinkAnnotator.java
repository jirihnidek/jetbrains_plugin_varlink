package org.intellij.sdk.language;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.psi.PsiElement;
import org.intellij.sdk.language.psi.VarlinkErrorDeclaration;
import org.intellij.sdk.language.psi.VarlinkField;
import org.intellij.sdk.language.psi.VarlinkInterfaceDeclaration;
import org.intellij.sdk.language.psi.VarlinkMethodDeclaration;
import org.intellij.sdk.language.psi.VarlinkNamedElement;
import org.intellij.sdk.language.psi.VarlinkStruct;
import org.intellij.sdk.language.psi.VarlinkTypeAlias;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

/**
 * Reports duplicate declarations as errors in the editor.
 *
 * <p>Varlink gives all top-level members (types, methods and errors) a single
 * shared namespace, so any name used by more than one of them is a conflict.
 * Likewise, the field names within a single struct or enum body (a method's
 * input/output parameters, an error's parameters or a type's elements) must be
 * unique. The duplicate detection itself lives in {@link VarlinkUtil}; this
 * annotator only turns the detected collisions into highlighted errors.
 */
public class VarlinkAnnotator implements Annotator {

    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        if (element instanceof VarlinkInterfaceDeclaration) {
            // Checked once per file: types, methods and errors share one namespace.
            annotateDuplicates(VarlinkUtil.findDuplicateMembers(element.getContainingFile()), holder);
        } else if (element instanceof VarlinkStruct struct) {
            // Field names must be unique within each struct / enum body.
            annotateDuplicates(VarlinkUtil.findDuplicateFields(struct), holder);
        }
    }

    private static void annotateDuplicates(@NotNull Map<String, ? extends List<? extends VarlinkNamedElement>> duplicates,
                                           @NotNull AnnotationHolder holder) {
        for (Map.Entry<String, ? extends List<? extends VarlinkNamedElement>> entry : duplicates.entrySet()) {
            String name = entry.getKey();
            List<? extends VarlinkNamedElement> group = entry.getValue();
            for (VarlinkNamedElement element : group) {
                PsiElement identifier = element.getNameIdentifier();
                PsiElement range = identifier != null ? identifier : element;
                holder.newAnnotation(HighlightSeverity.ERROR, buildMessage(element, name, group.size()))
                        .range(range)
                        .create();
            }
        }
    }

    @NotNull
    private static String buildMessage(@NotNull VarlinkNamedElement element, @NotNull String name, int count) {
        return "Duplicate " + describe(element) + " '" + name + "' (declared " + count + " times)";
    }

    @NotNull
    private static String describe(@NotNull VarlinkNamedElement element) {
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
        return "declaration";
    }
}
