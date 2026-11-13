package org.intellij.sdk.language.psi;

import com.intellij.psi.tree.IElementType;
import org.intellij.sdk.language.VarlinkLanguage;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class VarlinkElementType extends IElementType {

    public VarlinkElementType(@NotNull @NonNls String debugName) {
        super(debugName, VarlinkLanguage.INSTANCE);
    }
}
