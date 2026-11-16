package org.intellij.sdk.language;

import com.intellij.openapi.actionSystem.IdeActions;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

/**
 * Tests for {@link VarlinkCommenter}: the "Comment with Line Comment" action
 * (Ctrl-/) adds and removes a {@code #} line comment.
 */
public class VarlinkCommenterTest extends BasePlatformTestCase {

    private void commentLine() {
        myFixture.performEditorAction(IdeActions.ACTION_COMMENT_LINE);
    }

    public void testLineIsCommented() {
        myFixture.configureByText("test.varlink",
                "interface org.example.test\n" +
                        "type Met<caret>adata(a: int)\n");
        commentLine();
        myFixture.checkResult(
                "interface org.example.test\n" +
                        "#type Metadata(a: int)\n");
    }

    public void testCommentedLineIsUncommented() {
        myFixture.configureByText("test.varlink",
                "interface org.example.test\n" +
                        "#type Met<caret>adata(a: int)\n");
        commentLine();
        myFixture.checkResult(
                "interface org.example.test\n" +
                        "type Metadata(a: int)\n");
    }

    public void testCommentRoundTrip() {
        // A selection keeps both toggles on the same line (commenting otherwise moves
        // the caret to the next line).
        myFixture.configureByText("test.varlink",
                "interface org.example.test\n" +
                        "<selection>method Ping() -> ()</selection>\n");
        commentLine();
        commentLine();
        myFixture.checkResult(
                "interface org.example.test\n" +
                        "method Ping() -> ()\n");
    }
}
