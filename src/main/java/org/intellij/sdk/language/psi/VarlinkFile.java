package org.intellij.sdk.language.psi;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import org.intellij.sdk.language.VarlinkFileType;
import org.intellij.sdk.language.VarlinkLanguage;
import org.jetbrains.annotations.NotNull;

public class VarlinkFile extends PsiFileBase {

    public VarlinkFile(@NotNull FileViewProvider viewProvider) {
        super(viewProvider, VarlinkLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public FileType getFileType() {
        return VarlinkFileType.INSTANCE;
    }

    @Override
    public String toString() {
        return "Varlink File";
    }
}
