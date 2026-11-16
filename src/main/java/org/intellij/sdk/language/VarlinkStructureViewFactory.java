package org.intellij.sdk.language;

import com.intellij.ide.structureView.StructureViewBuilder;
import com.intellij.ide.structureView.StructureViewModel;
import com.intellij.ide.structureView.TreeBasedStructureViewBuilder;
import com.intellij.lang.PsiStructureViewFactory;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Provides the "Structure" tool-window outline for Varlink files, listing the
 * interface's types, methods and errors and their fields.
 */
public class VarlinkStructureViewFactory implements PsiStructureViewFactory {

    @Nullable
    @Override
    public StructureViewBuilder getStructureViewBuilder(@NotNull PsiFile psiFile) {
        return new TreeBasedStructureViewBuilder() {
            @NotNull
            @Override
            public StructureViewModel createStructureViewModel(@Nullable Editor editor) {
                return new VarlinkStructureViewModel(editor, psiFile);
            }
        };
    }
}
