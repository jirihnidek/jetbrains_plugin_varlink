package org.intellij.sdk.language;

import com.intellij.openapi.fileTypes.LanguageFileType;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class VarlinkFileType extends LanguageFileType{
    public static final VarlinkFileType INSTANCE = new VarlinkFileType();

    private VarlinkFileType() {
        super(VarlinkLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public String getName() {
        return "Varlink File";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Varlink language file";
    }

    @NotNull
    @Override
    public String getDefaultExtension() {
        return "varlink";
    }

    @Override
    public Icon getIcon() {
        return VarlinkIcons.VARLINK_FILE;
    }
}
