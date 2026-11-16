package org.intellij.sdk.language;

import com.intellij.lang.BracePair;
import com.intellij.lang.PairedBraceMatcher;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import org.intellij.sdk.language.psi.VarlinkTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Highlights matching parentheses in the editor: when the caret is next to a '('
 * or ')', its partner is highlighted, and an unmatched bracket is marked.
 *
 * <p>Parentheses are the only paired brackets in Varlink. The '[]' and '[string]'
 * tokens are single lexer tokens (array / map markers), not a pair, so they are
 * not handled here.
 */
public class VarlinkBraceMatcher implements PairedBraceMatcher {

    private static final BracePair[] PAIRS = {
            // Structural: the parentheses delimit a method / type / error body, so the
            // IDE may use them for scope-aware features such as "highlight matched braces".
            new BracePair(VarlinkTypes.LPAREN, VarlinkTypes.RPAREN, true),
    };

    @Override
    public BracePair @NotNull [] getPairs() {
        return PAIRS;
    }

    @Override
    public boolean isPairedBracesAllowedBeforeType(@NotNull IElementType lbraceType, @Nullable IElementType contextType) {
        // A newly typed '(' may be auto-paired regardless of what follows it.
        return true;
    }

    @Override
    public int getCodeConstructStart(@NotNull PsiFile file, int openingBraceOffset) {
        // No special adjustment: the construct starts at the opening brace itself.
        return openingBraceOffset;
    }
}
