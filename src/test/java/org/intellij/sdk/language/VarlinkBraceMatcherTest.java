package org.intellij.sdk.language;

import com.intellij.codeInsight.highlighting.BraceMatchingUtil;
import com.intellij.lang.BracePair;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.intellij.sdk.language.psi.VarlinkTypes;

/**
 * Tests for {@link VarlinkBraceMatcher}: the '(' and ')' of a method, type or
 * error body must be recognised as a matching pair, and matching must work for
 * nested parentheses too.
 */
public class VarlinkBraceMatcherTest extends BasePlatformTestCase {

    public void testPairsCoverParentheses() {
        BracePair[] pairs = new VarlinkBraceMatcher().getPairs();
        assertSize(1, pairs);
        assertEquals(VarlinkTypes.LPAREN, pairs[0].getLeftBraceType());
        assertEquals(VarlinkTypes.RPAREN, pairs[0].getRightBraceType());
        assertTrue("Parentheses should be structural braces", pairs[0].isStructural());
    }

    /** With the caret on an opening '(', the matcher finds the matching ')'. */
    public void testMatchesOpeningToClosing() {
        myFixture.configureByText("test.varlink",
                "interface org.example.test\n" +
                        "type Meta<caret>(a: int, b: string)\n");
        int matched = BraceMatchingUtil.getMatchedBraceOffset(myFixture.getEditor(), true, myFixture.getFile());

        String text = myFixture.getFile().getText();
        assertEquals("Matched brace should be the ')' of the struct",
                text.lastIndexOf(')'), matched);
    }

    /** Matching respects nesting: the outer '(' pairs with the outer ')'. */
    public void testMatchesAcrossNestedParentheses() {
        myFixture.configureByText("test.varlink",
                "interface org.example.test\n" +
                        "method Do() -> <caret>(nested: (x: int))\n");
        int matched = BraceMatchingUtil.getMatchedBraceOffset(myFixture.getEditor(), true, myFixture.getFile());

        String text = myFixture.getFile().getText();
        // The outer opening paren must match the final ')', not the inner one.
        assertEquals("Outer '(' should match the outermost ')'",
                text.lastIndexOf(')'), matched);
    }
}
