package org.intellij.sdk.language;

import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import org.intellij.sdk.language.psi.VarlinkErrorDeclaration;
import org.intellij.sdk.language.psi.VarlinkField;
import org.intellij.sdk.language.psi.VarlinkInterfaceDeclaration;
import org.intellij.sdk.language.psi.VarlinkMethodDeclaration;
import org.intellij.sdk.language.psi.VarlinkNamedElement;
import org.intellij.sdk.language.psi.VarlinkStruct;
import org.intellij.sdk.language.psi.VarlinkTypeAlias;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * File-level helpers for querying the structure of a Varlink interface: listing
 * the declared types, methods and errors, resolving the interface name and
 * detecting duplicate declarations.
 *
 * <p>These are intended as building blocks for features such as an annotator,
 * completion, structure view or reference resolution.
 */
public final class VarlinkUtil {

    private VarlinkUtil() {
    }

    // -- interface ------------------------------------------------------------

    /** The single interface declaration of the file, or {@code null} if absent. */
    @Nullable
    public static VarlinkInterfaceDeclaration getInterfaceDeclaration(@Nullable PsiFile file) {
        return file == null ? null : PsiTreeUtil.getChildOfType(file, VarlinkInterfaceDeclaration.class);
    }

    /** The reverse-domain interface name (e.g. {@code org.example.more}), or {@code null}. */
    @Nullable
    public static String getInterfaceName(@Nullable PsiFile file) {
        VarlinkInterfaceDeclaration declaration = getInterfaceDeclaration(file);
        if (declaration == null) {
            return null;
        }
        var node = declaration.getNode().findChildByType(org.intellij.sdk.language.psi.VarlinkTypes.IDENTIFIER);
        return node != null ? node.getText() : null;
    }

    // -- members --------------------------------------------------------------

    /** All type aliases declared in the file, in source order. */
    @NotNull
    public static List<VarlinkTypeAlias> findTypeAliases(@Nullable PsiFile file) {
        VarlinkInterfaceDeclaration declaration = getInterfaceDeclaration(file);
        return declaration == null ? Collections.emptyList() : declaration.getTypeAliasList();
    }

    /** All methods declared in the file, in source order. */
    @NotNull
    public static List<VarlinkMethodDeclaration> findMethods(@Nullable PsiFile file) {
        VarlinkInterfaceDeclaration declaration = getInterfaceDeclaration(file);
        return declaration == null ? Collections.emptyList() : declaration.getMethodDeclarationList();
    }

    /** All errors declared in the file, in source order. */
    @NotNull
    public static List<VarlinkErrorDeclaration> findErrors(@Nullable PsiFile file) {
        VarlinkInterfaceDeclaration declaration = getInterfaceDeclaration(file);
        return declaration == null ? Collections.emptyList() : declaration.getErrorDeclarationList();
    }

    /**
     * All named members (types, methods and errors) of the file, in source order.
     * In Varlink these names share a single namespace, so this is the right list
     * for checking overall name uniqueness within an interface.
     */
    @NotNull
    public static List<VarlinkNamedElement> findMembers(@Nullable PsiFile file) {
        VarlinkInterfaceDeclaration declaration = getInterfaceDeclaration(file);
        if (declaration == null) {
            return Collections.emptyList();
        }
        List<VarlinkNamedElement> members = new ArrayList<>();
        members.addAll(declaration.getTypeAliasList());
        members.addAll(declaration.getMethodDeclarationList());
        members.addAll(declaration.getErrorDeclarationList());
        return members;
    }

    /** Members whose name equals {@code name} (normally zero or one). */
    @NotNull
    public static List<VarlinkNamedElement> findMembersByName(@Nullable PsiFile file, @Nullable String name) {
        if (name == null) {
            return Collections.emptyList();
        }
        List<VarlinkNamedElement> result = new ArrayList<>();
        for (VarlinkNamedElement member : findMembers(file)) {
            if (name.equals(member.getName())) {
                result.add(member);
            }
        }
        return result;
    }

    // -- duplicate detection --------------------------------------------------

    /**
     * Groups the given named elements by name, keeping only the names that occur
     * more than once. The returned map preserves source order, and each value
     * lists every colliding declaration (so all of them can be highlighted).
     */
    @NotNull
    public static <T extends VarlinkNamedElement> Map<String, List<T>> findDuplicates(@NotNull List<T> elements) {
        Map<String, List<T>> byName = new LinkedHashMap<>();
        for (T element : elements) {
            String name = element.getName();
            if (name != null && !name.isEmpty()) {
                byName.computeIfAbsent(name, key -> new ArrayList<>()).add(element);
            }
        }
        byName.values().removeIf(group -> group.size() < 2);
        return byName;
    }

    /** Duplicate type names within the file. */
    @NotNull
    public static Map<String, List<VarlinkTypeAlias>> findDuplicateTypeAliases(@Nullable PsiFile file) {
        return findDuplicates(findTypeAliases(file));
    }

    /** Duplicate method names within the file. */
    @NotNull
    public static Map<String, List<VarlinkMethodDeclaration>> findDuplicateMethods(@Nullable PsiFile file) {
        return findDuplicates(findMethods(file));
    }

    /** Duplicate error names within the file. */
    @NotNull
    public static Map<String, List<VarlinkErrorDeclaration>> findDuplicateErrors(@Nullable PsiFile file) {
        return findDuplicates(findErrors(file));
    }

    /**
     * Duplicate member names across all types, methods and errors. This catches
     * collisions that the per-category checks miss, e.g. a type and an error that
     * share the same name.
     */
    @NotNull
    public static Map<String, List<VarlinkNamedElement>> findDuplicateMembers(@Nullable PsiFile file) {
        return findDuplicates(findMembers(file));
    }

    // -- fields ---------------------------------------------------------------

    /** The fields of a struct or enum body, in source order. */
    @NotNull
    public static List<VarlinkField> getFields(@Nullable VarlinkStruct struct) {
        return struct == null ? Collections.emptyList() : struct.getFieldList();
    }

    /** Duplicate field names within a single struct or enum body. */
    @NotNull
    public static Map<String, List<VarlinkField>> findDuplicateFields(@Nullable VarlinkStruct struct) {
        return findDuplicates(getFields(struct));
    }
}
