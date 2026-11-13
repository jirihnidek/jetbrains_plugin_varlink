package org.intellij.sdk.language;

import com.intellij.psi.tree.TokenSet;
import org.intellij.sdk.language.psi.VarlinkTypes;

public interface VarlinkTokenSets {

    TokenSet KEYWORDS = TokenSet.create(
            VarlinkTypes.INTERFACE_KEYWORD,
            VarlinkTypes.TYPE_KEYWORD,
            VarlinkTypes.METHOD_KEYWORD,
            VarlinkTypes.ERROR_KEYWORD
    );

    TokenSet TYPES = TokenSet.create(
            VarlinkTypes.BOOL_KEYWORD,
            VarlinkTypes.INT_KEYWORD,
            VarlinkTypes.FLOAT_KEYWORD,
            VarlinkTypes.STRING_KEYWORD,
            VarlinkTypes.OBJECT_KEYWORD,
            VarlinkTypes.ANY_KEYWORD
    );

    TokenSet COMMENTS = TokenSet.create(VarlinkTypes.COMMENT);
}
