package org.intellij.sdk.language;

import com.intellij.psi.PsiReference;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.intellij.sdk.language.psi.VarlinkTypeAlias;

/**
 * Tests for {@link VarlinkReferenceContributor} / {@link VarlinkTypeReference}:
 * a custom type usage resolves to its {@code type} declaration wherever it
 * appears, built-in types do not, and rename propagates to every usage.
 */
public class VarlinkReferenceContributorTest extends BasePlatformTestCase {

    private PsiReference referenceAtCaret(String source) {
        myFixture.configureByText("test.varlink", source);
        return myFixture.getFile().findReferenceAt(myFixture.getCaretOffset());
    }

    private void assertResolvesToType(PsiReference reference, String expectedName) {
        assertNotNull("Expected a reference at the caret", reference);
        var target = reference.resolve();
        assertInstanceOf(target, VarlinkTypeAlias.class);
        assertEquals(expectedName, ((VarlinkTypeAlias) target).getName());
    }

    public void testResolvesTypeUsageInMethodParameter() {
        PsiReference reference = referenceAtCaret(
                "interface org.example.test\n" +
                        "type Metadata(a: int)\n" +
                        "method Ping(meta: Met<caret>adata) -> ()\n");
        assertResolvesToType(reference, "Metadata");
    }

    public void testResolvesTypeUsageInTypeBody() {
        PsiReference reference = referenceAtCaret(
                "interface org.example.test\n" +
                        "type Inner(a: int)\n" +
                        "type Outer(inner: Inn<caret>er)\n");
        assertResolvesToType(reference, "Inner");
    }

    public void testResolvesTypeUsageInErrorBody() {
        PsiReference reference = referenceAtCaret(
                "interface org.example.test\n" +
                        "type Detail(a: int)\n" +
                        "error Boom(detail: Det<caret>ail)\n");
        assertResolvesToType(reference, "Detail");
    }

    public void testResolvesThroughOptionalAndArrayMarkers() {
        PsiReference reference = referenceAtCaret(
                "interface org.example.test\n" +
                        "type Metadata(a: int)\n" +
                        "method Ping(meta: ?[]Met<caret>adata) -> ()\n");
        assertResolvesToType(reference, "Metadata");
    }

    public void testBuiltInTypeHasNoReference() {
        PsiReference reference = referenceAtCaret(
                "interface org.example.test\n" +
                        "method Ping(flag: bo<caret>ol) -> ()\n");
        assertNull("Built-in types must not produce a type reference", reference);
    }

    public void testUnknownTypeDoesNotResolve() {
        PsiReference reference = referenceAtCaret(
                "interface org.example.test\n" +
                        "method Ping(meta: Unkno<caret>wn) -> ()\n");
        assertNotNull("Unknown type should still carry a reference", reference);
        assertNull("Unknown type must not resolve", reference.resolve());
    }

    public void testRenameUpdatesDeclarationAndAllUsages() {
        myFixture.configureByText("test.varlink",
                "interface org.example.test\n" +
                        "type Metadata(a: int)\n" +
                        "method Ping(meta: Met<caret>adata) -> (copy: Metadata)\n");
        myFixture.renameElementAtCaret("Meta");
        String text = myFixture.getFile().getText();
        assertTrue("Declaration should be renamed", text.contains("type Meta("));
        assertFalse("No old name should remain", text.contains("Metadata"));
        // Both the input and output usages must be updated.
        assertTrue("Input usage should be renamed", text.contains("meta: Meta"));
        assertTrue("Output usage should be renamed", text.contains("copy: Meta"));
    }
}
