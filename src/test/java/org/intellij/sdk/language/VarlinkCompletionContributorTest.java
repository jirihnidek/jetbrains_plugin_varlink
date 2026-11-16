package org.intellij.sdk.language;

import com.intellij.testFramework.fixtures.BasePlatformTestCase;

import java.util.List;

/**
 * Tests for {@link VarlinkCompletionContributor}: custom type names are offered
 * in a method's input and output parameter type positions, and nowhere they
 * should not be (field names, type alias bodies, error bodies).
 */
public class VarlinkCompletionContributorTest extends BasePlatformTestCase {

    /** Completion variants offered at the {@code <caret>} marker in the given source. */
    private List<String> completionsAt(String source) {
        myFixture.configureByText("test.varlink", source);
        myFixture.complete(com.intellij.codeInsight.completion.CompletionType.BASIC);
        return myFixture.getLookupElementStrings();
    }

    private static final String TYPES =
            "interface org.example.test\n" +
                    "type Metadata(a: int)\n" +
                    "type Status(ok: bool)\n";

    public void testCompletesCustomTypeInInputParameter() {
        List<String> variants = completionsAt(TYPES +
                "method Ping(meta: <caret>) -> ()\n");
        assertNotNull(variants);
        assertContainsElements(variants, "Metadata", "Status");
    }

    public void testCompletesCustomTypeInOutputParameter() {
        List<String> variants = completionsAt(TYPES +
                "method Ping() -> (result: <caret>)\n");
        assertNotNull(variants);
        assertContainsElements(variants, "Metadata", "Status");
    }

    public void testCompletesAfterOptionalAndArrayMarkers() {
        List<String> variants = completionsAt(TYPES +
                "method Ping(meta: ?[]<caret>) -> ()\n");
        assertNotNull(variants);
        assertContainsElements(variants, "Metadata", "Status");
    }

    public void testCompletesInNestedParameterStruct() {
        List<String> variants = completionsAt(TYPES +
                "method Ping() -> (nested: (inner: <caret>))\n");
        assertNotNull(variants);
        assertContainsElements(variants, "Metadata", "Status");
    }

    public void testDoesNotCompleteTypesForParameterName() {
        // The caret is in a field-name position, so custom types must not be offered.
        List<String> variants = completionsAt(TYPES +
                "method Ping(<caret>) -> ()\n");
        if (variants != null) {
            assertDoesntContain(variants, "Metadata");
            assertDoesntContain(variants, "Status");
        }
    }

    public void testDoesNotCompleteTypesInTypeAliasBody() {
        // Scoped to method parameters: a type alias body must not get completions.
        List<String> variants = completionsAt(TYPES +
                "type Other(field: <caret>)\n");
        if (variants != null) {
            assertDoesntContain(variants, "Metadata");
            assertDoesntContain(variants, "Status");
        }
    }

    public void testDoesNotCompleteTypesInErrorBody() {
        List<String> variants = completionsAt(TYPES +
                "error Boom(field: <caret>)\n");
        if (variants != null) {
            assertDoesntContain(variants, "Metadata");
            assertDoesntContain(variants, "Status");
        }
    }
}
