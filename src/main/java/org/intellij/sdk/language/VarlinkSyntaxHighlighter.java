package org.intellij.sdk.language;

import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.HighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.tree.IElementType;
import org.intellij.sdk.language.psi.VarlinkTypes;
import org.jetbrains.annotations.NotNull;

import static com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey;

public class VarlinkSyntaxHighlighter extends SyntaxHighlighterBase {

    public static final TextAttributesKey KEYWORD =
            createTextAttributesKey("VARLINK_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD);
    public static final TextAttributesKey BUILTIN_TYPE =
            createTextAttributesKey("VARLINK_BUILTIN_TYPE", DefaultLanguageHighlighterColors.KEYWORD);
    // Applied semantically by VarlinkAnnotator: a field whose type is a reference to
    // a custom type declaration (e.g. the 'Bar' in 'type MyType ( bar: Bar )').
    // The lexer cannot set it because every name is a single IDENTIFIER token.
    public static final TextAttributesKey TYPE_REFERENCE =
            createTextAttributesKey("VARLINK_TYPE_REFERENCE", DefaultLanguageHighlighterColors.CLASS_REFERENCE);
    public static final TextAttributesKey IDENTIFIER =
            createTextAttributesKey("VARLINK_IDENTIFIER", DefaultLanguageHighlighterColors.IDENTIFIER);
    public static final TextAttributesKey COMMENT =
            createTextAttributesKey("VARLINK_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT);
    public static final TextAttributesKey OPERATOR =
            createTextAttributesKey("VARLINK_OPERATOR", DefaultLanguageHighlighterColors.OPERATION_SIGN);
    public static final TextAttributesKey PARENTHESES =
            createTextAttributesKey("VARLINK_PARENTHESES", DefaultLanguageHighlighterColors.PARENTHESES);
    public static final TextAttributesKey COMMA =
            createTextAttributesKey("VARLINK_COMMA", DefaultLanguageHighlighterColors.COMMA);
    public static final TextAttributesKey BAD_CHARACTER =
            createTextAttributesKey("VARLINK_BAD_CHARACTER", HighlighterColors.BAD_CHARACTER);

    private static final TextAttributesKey[] KEYWORD_KEYS = {KEYWORD};
    private static final TextAttributesKey[] BUILTIN_TYPE_KEYS = {BUILTIN_TYPE};
    private static final TextAttributesKey[] IDENTIFIER_KEYS = {IDENTIFIER};
    private static final TextAttributesKey[] COMMENT_KEYS = {COMMENT};
    private static final TextAttributesKey[] OPERATOR_KEYS = {OPERATOR};
    private static final TextAttributesKey[] PARENTHESES_KEYS = {PARENTHESES};
    private static final TextAttributesKey[] COMMA_KEYS = {COMMA};
    private static final TextAttributesKey[] BAD_CHARACTER_KEYS = {BAD_CHARACTER};
    private static final TextAttributesKey[] EMPTY_KEYS = new TextAttributesKey[0];

    @NotNull
    @Override
    public Lexer getHighlightingLexer() {
        return new VarlinkLexerAdapter();
    }

    @NotNull
    @Override
    public TextAttributesKey[] getTokenHighlights(IElementType tokenType) {
        if (tokenType.equals(VarlinkTypes.INTERFACE_KEYWORD)
                || tokenType.equals(VarlinkTypes.TYPE_KEYWORD)
                || tokenType.equals(VarlinkTypes.METHOD_KEYWORD)
                || tokenType.equals(VarlinkTypes.ERROR_KEYWORD)) {
            return KEYWORD_KEYS;
        }
        if (tokenType.equals(VarlinkTypes.BOOL_KEYWORD)
                || tokenType.equals(VarlinkTypes.INT_KEYWORD)
                || tokenType.equals(VarlinkTypes.FLOAT_KEYWORD)
                || tokenType.equals(VarlinkTypes.STRING_KEYWORD)
                || tokenType.equals(VarlinkTypes.OBJECT_KEYWORD)
                || tokenType.equals(VarlinkTypes.ANY_KEYWORD)) {
            return BUILTIN_TYPE_KEYS;
        }
        if (tokenType.equals(VarlinkTypes.IDENTIFIER)) {
            return IDENTIFIER_KEYS;
        }
        if (tokenType.equals(VarlinkTypes.COMMENT)) {
            return COMMENT_KEYS;
        }
        if (tokenType.equals(VarlinkTypes.ARROW)
                || tokenType.equals(VarlinkTypes.ARRAY)
                || tokenType.equals(VarlinkTypes.MAP)
                || tokenType.equals(VarlinkTypes.QUESTION)
                || tokenType.equals(VarlinkTypes.COLON)) {
            return OPERATOR_KEYS;
        }
        if (tokenType.equals(VarlinkTypes.LPAREN)
                || tokenType.equals(VarlinkTypes.RPAREN)) {
            return PARENTHESES_KEYS;
        }
        if (tokenType.equals(VarlinkTypes.COMMA)) {
            return COMMA_KEYS;
        }
        if (tokenType.equals(com.intellij.psi.TokenType.BAD_CHARACTER)) {
            return BAD_CHARACTER_KEYS;
        }
        return EMPTY_KEYS;
    }
}
