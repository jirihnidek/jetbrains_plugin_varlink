package org.intellij.sdk.language;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.util.treeView.smartTree.SortableTreeElement;
import com.intellij.ide.util.treeView.smartTree.TreeElement;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.NavigatablePsiElement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.intellij.sdk.language.psi.VarlinkBaseType;
import org.intellij.sdk.language.psi.VarlinkErrorDeclaration;
import org.intellij.sdk.language.psi.VarlinkField;
import org.intellij.sdk.language.psi.VarlinkFile;
import org.intellij.sdk.language.psi.VarlinkInterfaceDeclaration;
import org.intellij.sdk.language.psi.VarlinkMethodDeclaration;
import org.intellij.sdk.language.psi.VarlinkNamedElement;
import org.intellij.sdk.language.psi.VarlinkStruct;
import org.intellij.sdk.language.psi.VarlinkType;
import org.intellij.sdk.language.psi.VarlinkTypeAlias;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * A node in the Varlink structure view. The root shows the interface name and its
 * children are the interface members (types, methods and errors) in source order.
 * Each member's children are its fields (a type's elements, an error's parameters,
 * a method's input and output parameters), and a field whose type is an inline
 * object expands to show that object's fields, recursively.
 *
 * <p>On the right of each field its type is shown (e.g. {@code int}, {@code ?string});
 * members and inline-object fields show nothing there.
 */
public class VarlinkStructureViewElement implements StructureViewTreeElement, SortableTreeElement {

    private final NavigatablePsiElement element;

    public VarlinkStructureViewElement(@NotNull NavigatablePsiElement element) {
        this.element = element;
    }

    @Override
    public Object getValue() {
        return element;
    }

    @Override
    public void navigate(boolean requestFocus) {
        element.navigate(requestFocus);
    }

    @Override
    public boolean canNavigate() {
        return element.canNavigate();
    }

    @Override
    public boolean canNavigateToSource() {
        return element.canNavigateToSource();
    }

    @NotNull
    @Override
    public String getAlphaSortKey() {
        String name = element.getName();
        return name != null ? name : "";
    }

    @NotNull
    @Override
    public ItemPresentation getPresentation() {
        if (element instanceof VarlinkFile file) {
            String interfaceName = VarlinkUtil.getInterfaceName(file);
            return new PresentationData(interfaceName != null ? interfaceName : file.getName(),
                    null, VarlinkIcons.VARLINK_INTERFACE, null);
        }
        String location = element instanceof VarlinkField field ? fieldTypeText(field) : null;
        return new PresentationData(element.getName(), location, iconFor(element), null);
    }

    @NotNull
    private static javax.swing.Icon iconFor(@NotNull PsiElement element) {
        if (element instanceof VarlinkMethodDeclaration) {
            return VarlinkIcons.VARLINK_METHOD;
        }
        if (element instanceof VarlinkErrorDeclaration) {
            return VarlinkIcons.VARLINK_ERROR;
        }
        if (element instanceof VarlinkField) {
            return VarlinkIcons.VARLINK_FIELD;
        }
        return VarlinkIcons.VARLINK_TYPE;
    }

    @Override
    public TreeElement @NotNull [] getChildren() {
        List<TreeElement> children = new ArrayList<>();
        if (element instanceof VarlinkFile file) {
            VarlinkInterfaceDeclaration declaration = VarlinkUtil.getInterfaceDeclaration(file);
            if (declaration != null) {
                // Direct children in document order: types, methods and errors as written.
                for (VarlinkNamedElement member : PsiTreeUtil.getChildrenOfTypeAsList(declaration, VarlinkNamedElement.class)) {
                    addElement(children, member);
                }
            }
        } else if (element instanceof VarlinkField field) {
            VarlinkStruct nested = nestedStruct(field);
            if (nested != null) {
                for (VarlinkField nestedField : nested.getFieldList()) {
                    addElement(children, nestedField);
                }
            }
        } else if (isMemberWithFields(element)) {
            for (VarlinkStruct struct : PsiTreeUtil.getChildrenOfTypeAsList(element, VarlinkStruct.class)) {
                for (VarlinkField field : struct.getFieldList()) {
                    addElement(children, field);
                }
            }
        }
        return children.toArray(TreeElement.EMPTY_ARRAY);
    }

    /** The inline object body of a field's type, or {@code null} if the type is not an inline object. */
    @Nullable
    static VarlinkStruct nestedStruct(@NotNull VarlinkField field) {
        VarlinkType type = field.getType();
        if (type == null) {
            return null;
        }
        VarlinkBaseType baseType = type.getBaseType();
        return baseType != null ? baseType.getStruct() : null;
    }

    /** The text shown on the right of a field: its type, or {@code null} for enum elements and inline objects. */
    @Nullable
    private static String fieldTypeText(@NotNull VarlinkField field) {
        VarlinkType type = field.getType();
        if (type == null || nestedStruct(field) != null) {
            return null;
        }
        return type.getText();
    }

    private static void addElement(@NotNull List<TreeElement> children, @NotNull PsiElement element) {
        if (element instanceof NavigatablePsiElement navigatable) {
            children.add(new VarlinkStructureViewElement(navigatable));
        }
    }

    private static boolean isMemberWithFields(@NotNull PsiElement element) {
        return element instanceof VarlinkTypeAlias
                || element instanceof VarlinkMethodDeclaration
                || element instanceof VarlinkErrorDeclaration;
    }
}
