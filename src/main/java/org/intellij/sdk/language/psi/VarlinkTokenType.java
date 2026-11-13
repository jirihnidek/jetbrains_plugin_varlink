package org.intellij.sdk.language.psi;

import com.intellij.psi.tree.IElementType;
import org.intellij.sdk.language.VarlinkLanguage;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class VarlinkTokenType extends IElementType {

    public VarlinkTokenType(@NotNull @NonNls String debugName) {
        super(debugName, VarlinkLanguage.INSTANCE);
    }

    @Override
    public String toString() {
        return "VarlinkTokenType." + super.toString();
    }
}
