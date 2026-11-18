package org.intellij.sdk.language;

import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import com.intellij.openapi.options.colors.ColorDescriptor;
import com.intellij.openapi.options.colors.ColorSettingsPage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;
import java.util.Map;

/**
 * Exposes the Varlink text attributes under <em>Settings | Editor | Color Scheme |
 * Varlink</em> so each colour can be customised. The preview text shows the three
 * kinds of type reference side by side: the built-in types the lexer colours, and
 * the foreign (custom) type reference coloured semantically by {@link VarlinkAnnotator}.
 */
public class VarlinkColorSettingsPage implements ColorSettingsPage {

    private static final AttributesDescriptor[] DESCRIPTORS = new AttributesDescriptor[]{
            new AttributesDescriptor("Keyword", VarlinkSyntaxHighlighter.KEYWORD),
            new AttributesDescriptor("Built-in type", VarlinkSyntaxHighlighter.BUILTIN_TYPE),
            new AttributesDescriptor("Custom type reference", VarlinkSyntaxHighlighter.TYPE_REFERENCE),
            new AttributesDescriptor("Identifier", VarlinkSyntaxHighlighter.IDENTIFIER),
            new AttributesDescriptor("Comment", VarlinkSyntaxHighlighter.COMMENT),
            new AttributesDescriptor("Operator", VarlinkSyntaxHighlighter.OPERATOR),
            new AttributesDescriptor("Parentheses", VarlinkSyntaxHighlighter.PARENTHESES),
            new AttributesDescriptor("Comma", VarlinkSyntaxHighlighter.COMMA),
            new AttributesDescriptor("Bad character", VarlinkSyntaxHighlighter.BAD_CHARACTER),
    };

    /** Highlights the foreign type references in the preview; the lexer cannot tag them. */
    private static final Map<String, TextAttributesKey> TAGS =
            Map.of("type_ref", VarlinkSyntaxHighlighter.TYPE_REFERENCE);

    @Nullable
    @Override
    public Icon getIcon() {
        return VarlinkIcons.VARLINK_FILE;
    }

    @NotNull
    @Override
    public SyntaxHighlighter getHighlighter() {
        return new VarlinkSyntaxHighlighter();
    }

    @NotNull
    @Override
    public String getDemoText() {
        return "# Example Varlink interface\n" +
                "interface org.example.store\n" +
                "\n" +
                "type Address (\n" +
                "    street: string,\n" +
                "    zip: int\n" +
                ")\n" +
                "\n" +
                "type Customer (\n" +
                "    name: string,\n" +
                "    address: <type_ref>Address</type_ref>,\n" +
                "    tags: []string\n" +
                ")\n" +
                "\n" +
                "method GetCustomer(id: string) -> (customer: <type_ref>Customer</type_ref>)\n" +
                "\n" +
                "error NotFound(id: string)\n";
    }

    @Nullable
    @Override
    public Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap() {
        return TAGS;
    }

    @Override
    public AttributesDescriptor @NotNull [] getAttributeDescriptors() {
        return DESCRIPTORS;
    }

    @Override
    public ColorDescriptor @NotNull [] getColorDescriptors() {
        return ColorDescriptor.EMPTY_ARRAY;
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Varlink";
    }
}
