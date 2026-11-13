package org.intellij.sdk.language.psi;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.util.PsiTreeUtil;
import org.intellij.sdk.language.VarlinkFileType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Creates Varlink PSI elements from text. Used, for example, to build a
 * replacement name identifier when an element is renamed.
 */
public final class VarlinkElementFactory {

    private VarlinkElementFactory() {
    }

    @NotNull
    public static VarlinkFile createFile(@NotNull Project project, @NotNull String text) {
        return (VarlinkFile) PsiFileFactory.getInstance(project)
                .createFileFromText("dummy.varlink", VarlinkFileType.INSTANCE, text);
    }

    /**
     * Creates a standalone identifier token carrying the given name, by parsing a
     * minimal valid interface and extracting the name of its single type alias.
     */
    @Nullable
    public static com.intellij.psi.PsiElement createIdentifier(@NotNull Project project, @NotNull String name) {
        VarlinkFile file = createFile(project, "interface a.b\ntype " + name + " ()\n");
        VarlinkTypeAlias typeAlias = PsiTreeUtil.findChildOfType(file, VarlinkTypeAlias.class);
        return typeAlias != null ? typeAlias.getNameIdentifier() : null;
    }
}
