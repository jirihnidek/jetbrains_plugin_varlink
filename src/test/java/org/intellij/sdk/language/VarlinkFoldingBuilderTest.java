package org.intellij.sdk.language;

import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.openapi.editor.Document;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

/**
 * Tests for {@link VarlinkFoldingBuilder}: multi-line bodies of methods, types,
 * errors and nested structs are foldable, single-line bodies are not, and the
 * placeholder is the expected ellipsis.
 */
public class VarlinkFoldingBuilderTest extends BasePlatformTestCase {

    private FoldingDescriptor[] foldRegions(String source) {
        myFixture.configureByText("test.varlink", source);
        PsiFile file = myFixture.getFile();
        Document document = myFixture.getDocument(file);
        return new VarlinkFoldingBuilder().buildFoldRegions(file, document, false);
    }

    public void testMultiLineTypeIsFoldable() {
        FoldingDescriptor[] regions = foldRegions(
                "interface org.example.test\n" +
                        "type Metadata(\n" +
                        "    a: int,\n" +
                        "    b: string\n" +
                        ")\n");
        assertEquals(1, regions.length);
    }

    public void testMultiLineErrorIsFoldable() {
        FoldingDescriptor[] regions = foldRegions(
                "interface org.example.test\n" +
                        "error Boom(\n" +
                        "    message: string\n" +
                        ")\n");
        assertEquals(1, regions.length);
    }

    public void testMultiLineMethodFoldsAsWholeDefinition() {
        // A method with multi-line input and output folds as a single region
        // spanning the whole signature, not one region per body.
        FoldingDescriptor[] regions = foldRegions(
                "interface org.example.test\n" +
                        "method Ping(\n" +
                        "    meta: ?string\n" +
                        ") -> (\n" +
                        "    status: object\n" +
                        ")\n");
        assertEquals(1, regions.length);
        assertEquals("(...) -> (...)", new VarlinkFoldingBuilder().getPlaceholderText(regions[0].getElement()));
    }

    public void testMethodWithMultiLineOutputOnlyIsFoldable() {
        // Empty single-line input, multi-line output: the whole method still folds.
        FoldingDescriptor[] regions = foldRegions(
                "interface org.example.test\n" +
                        "method IsRegistered() -> (\n" +
                        "    registered: bool,\n" +
                        "    reason: string\n" +
                        ")\n");
        assertEquals(1, regions.length);
    }

    public void testNestedOutputObjectFoldsIndependently() {
        FoldingDescriptor[] regions = foldRegions(
                "interface org.example.test\n" +
                        "method Do() -> (\n" +
                        "    nested: (\n" +
                        "        x: int\n" +
                        "    )\n" +
                        ")\n");
        // One region for the whole method, one for the nested object inside the output.
        assertEquals(2, regions.length);
    }

    public void testSingleLineDefinitionIsNotFoldable() {
        FoldingDescriptor[] regions = foldRegions(
                "interface org.example.test\n" +
                        "type Metadata(a: int, b: string)\n" +
                        "method Ping() -> (ok: bool)\n");
        assertEquals(0, regions.length);
    }

    public void testPlaceholderTextIsEllipsis() {
        FoldingDescriptor[] regions = foldRegions(
                "interface org.example.test\n" +
                        "type Metadata(\n" +
                        "    a: int\n" +
                        ")\n");
        assertEquals(1, regions.length);
        assertEquals("(...)", new VarlinkFoldingBuilder().getPlaceholderText(regions[0].getElement()));
    }
}
