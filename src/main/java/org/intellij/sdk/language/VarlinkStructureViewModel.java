package org.intellij.sdk.language;

import com.intellij.ide.structureView.StructureViewModel;
import com.intellij.ide.structureView.StructureViewModelBase;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.util.treeView.smartTree.Sorter;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiFile;
import org.intellij.sdk.language.psi.VarlinkField;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static org.intellij.sdk.language.VarlinkStructureViewElement.nestedStruct;

/**
 * Structure view model for a Varlink file. Members (types, methods, errors) and
 * their fields can be sorted alphabetically; fields are always leaves.
 */
public class VarlinkStructureViewModel extends StructureViewModelBase implements StructureViewModel.ElementInfoProvider {

    public VarlinkStructureViewModel(@Nullable Editor editor, @NotNull PsiFile psiFile) {
        super(psiFile, editor, new VarlinkStructureViewElement(psiFile));
    }

    @Override
    public Sorter @NotNull [] getSorters() {
        return new Sorter[]{Sorter.ALPHA_SORTER};
    }

    @Override
    public boolean isAlwaysShowsPlus(StructureViewTreeElement element) {
        return false;
    }

    @Override
    public boolean isAlwaysLeaf(StructureViewTreeElement element) {
        // A field is a leaf unless its type is an inline object, which expands further.
        return element.getValue() instanceof VarlinkField field && nestedStruct(field) == null;
    }
}
