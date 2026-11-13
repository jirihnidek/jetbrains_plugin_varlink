package org.intellij.sdk.language;

import com.intellij.lexer.FlexAdapter;
import org.intellij.sdk.language.lexer.VarlinkLexer;

public class VarlinkLexerAdapter extends FlexAdapter {

    public VarlinkLexerAdapter() {
        super(new VarlinkLexer(null));
    }
}
