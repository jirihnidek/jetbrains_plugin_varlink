package org.intellij.sdk.language;

import com.intellij.lang.ASTNode;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import org.intellij.sdk.language.psi.VarlinkBaseType;
import org.intellij.sdk.language.psi.VarlinkErrorDeclaration;
import org.intellij.sdk.language.psi.VarlinkField;
import org.intellij.sdk.language.psi.VarlinkInterfaceDeclaration;
import org.intellij.sdk.language.psi.VarlinkMethodDeclaration;
import org.intellij.sdk.language.psi.VarlinkNamedElement;
import org.intellij.sdk.language.psi.VarlinkStruct;
import org.intellij.sdk.language.psi.VarlinkTypeAlias;
import org.intellij.sdk.language.psi.VarlinkTypes;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

/**
 * Reports semantic errors in the editor: duplicate declarations and references to
 * undefined types.
 *
 * <p>Varlink gives all top-level members (types, methods and errors) a single
 * shared namespace, so any name used by more than one of them is a conflict.
 * Likewise, the field names within a single struct or enum body (a method's
 * input/output parameters, an error's parameters or a type's elements) must be
 * unique. A field whose type is a bare identifier must name a {@code type}
 * declared in the same file; if it resolves to nothing, it is an undefined type.
 * The duplicate detection and type resolution live in {@link VarlinkUtil} and the
 * reference machinery; this annotator turns their results into highlighted errors.
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
        } else if (element instanceof VarlinkBaseType baseType) {
            // A custom type usage must resolve to a type declared in the file.
            annotateUnresolvedType(baseType, holder);
        }
    }

    private static void annotateUnresolvedType(@NotNull VarlinkBaseType baseType, @NotNull AnnotationHolder holder) {
        // Built-in types and inline structs are not references to a declaration.
        if (baseType.getBasicType() != null || baseType.getStruct() != null) {
            return;
        }
        ASTNode identifier = baseType.getNode().findChildByType(VarlinkTypes.IDENTIFIER);
        if (identifier == null) {
            return;
        }
        PsiReference[] references = baseType.getReferences();
        if (references.length == 0) {
            return;
        }
        for (PsiReference reference : references) {
            if (reference.resolve() != null) {
                return;
            }
        }
        holder.newAnnotation(HighlightSeverity.ERROR, "Unresolved type '" + identifier.getText() + "'")
                .range(identifier.getTextRange())
                .highlightType(ProblemHighlightType.LIKE_UNKNOWN_SYMBOL)
                .create();
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
