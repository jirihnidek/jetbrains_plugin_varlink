package org.intellij.sdk.language;

import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

import java.util.List;

/**
 * Tests for {@link VarlinkAnnotator}: duplicate declarations (types, methods,
 * errors, cross-category collisions) and duplicate fields (parameters and enum
 * elements) must be reported as errors, while a valid interface must not.
 *
 * <p>The tests drive the real highlighting pass through the test fixture, so they
 * exercise the annotator exactly as the IDE would.
 */
public class VarlinkAnnotatorTest extends BasePlatformTestCase {

    /** All ERROR-severity highlights produced for the given interface source. */
    private List<HighlightInfo> errorsFor(String source) {
        myFixture.configureByText("test.varlink", source);
        return myFixture.doHighlighting(HighlightSeverity.ERROR);
    }

    private void assertAllContain(List<HighlightInfo> infos, String expectedSubstring) {
        for (HighlightInfo info : infos) {
            assertTrue(
                    "Expected description to contain \"" + expectedSubstring + "\" but was: " + info.getDescription(),
                    info.getDescription() != null && info.getDescription().contains(expectedSubstring));
        }
    }

    public void testDuplicateMethods() {
        List<HighlightInfo> errors = errorsFor(
                "interface org.example.test\n" +
                        "method Ping() -> ()\n" +
                        "method Ping() -> ()\n");
        // Both colliding declarations are highlighted.
        assertSize(2, errors);
        assertAllContain(errors, "Duplicate method 'Ping'");
    }

    public void testDuplicateErrors() {
        List<HighlightInfo> errors = errorsFor(
                "interface org.example.test\n" +
                        "error Boom(message: string)\n" +
                        "error Boom(message: string)\n");
        assertSize(2, errors);
        assertAllContain(errors, "Duplicate error 'Boom'");
    }

    public void testDuplicateTypes() {
        List<HighlightInfo> errors = errorsFor(
                "interface org.example.test\n" +
                        "type Meta(a: int)\n" +
                        "type Meta(a: int)\n");
        assertSize(2, errors);
        assertAllContain(errors, "Duplicate type 'Meta'");
    }

    public void testCrossCategoryDuplicate() {
        // Types, methods and errors share a single namespace in Varlink.
        List<HighlightInfo> errors = errorsFor(
                "interface org.example.test\n" +
                        "type Foo(a: int)\n" +
                        "error Foo(message: string)\n");
        assertSize(2, errors);
        // One collision reported on the type, one on the error.
        assertAllContain(errors, "'Foo'");
    }

    public void testDuplicateParameters() {
        List<HighlightInfo> errors = errorsFor(
                "interface org.example.test\n" +
                        "method Do(value: int, value: int) -> ()\n");
        assertSize(2, errors);
        assertAllContain(errors, "Duplicate field 'value'");
    }

    public void testDuplicateEnumElements() {
        List<HighlightInfo> errors = errorsFor(
                "interface org.example.test\n" +
                        "type Color(red, green, red)\n");
        assertSize(2, errors);
        assertAllContain(errors, "Duplicate field 'red'");
    }

    public void testDuplicatesInNestedStruct() {
        // The field-level check must also reach anonymous nested struct bodies.
        List<HighlightInfo> errors = errorsFor(
                "interface org.example.test\n" +
                        "method Do() -> (nested: (x: int, x: int))\n");
        assertSize(2, errors);
        assertAllContain(errors, "Duplicate field 'x'");
    }

    public void testValidInterfaceHasNoErrors() {
        List<HighlightInfo> errors = errorsFor(
                "interface org.example.test\n" +
                        "type Meta(a: int, b: string)\n" +
                        "error Boom(message: string)\n" +
                        "method Ping(meta: ?Meta) -> (ok: bool)\n" +
                        "method IsRegistered() -> (registered: bool)\n");
        assertEmpty(errors);
    }
}
