package org.intellij.sdk.language;

import com.intellij.navigation.NavigationItem;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.intellij.util.indexing.FindSymbolParameters;
import org.intellij.sdk.language.psi.VarlinkTypeAlias;

import java.util.ArrayList;
import java.util.List;

/**
 * Tests for {@link VarlinkSymbolContributor}: custom {@code type} names are offered
 * to "Go to Symbol" and navigate to their declaration, while methods and errors
 * are not contributed.
 */
public class VarlinkSymbolContributorTest extends BasePlatformTestCase {

    private final VarlinkSymbolContributor contributor = new VarlinkSymbolContributor();

    private List<String> symbolNames() {
        List<String> names = new ArrayList<>();
        contributor.processNames(names::add, GlobalSearchScope.allScope(getProject()), null);
        return names;
    }

    private List<NavigationItem> symbolsNamed(String name) {
        List<NavigationItem> items = new ArrayList<>();
        FindSymbolParameters parameters = new FindSymbolParameters(
                name, name, GlobalSearchScope.allScope(getProject()), null);
        contributor.processElementsWithName(name, item -> {
            items.add(item);
            return true;
        }, parameters);
        return items;
    }

    public void testTypeNamesAreContributed() {
        myFixture.configureByText("test.varlink",
                "interface org.example.test\n" +
                        "type Metadata(a: int)\n" +
                        "type Status(ok: bool)\n");
        List<String> names = symbolNames();
        assertContainsElements(names, "Metadata", "Status");
    }

    public void testMethodsAndErrorsAreNotContributed() {
        myFixture.configureByText("test.varlink",
                "interface org.example.test\n" +
                        "type Metadata(a: int)\n" +
                        "method Ping() -> ()\n" +
                        "error Boom(message: string)\n");
        List<String> names = symbolNames();
        assertContainsElements(names, "Metadata");
        assertDoesntContain(names, "Ping");
        assertDoesntContain(names, "Boom");
    }

    public void testTypeNavigatesToDeclaration() {
        myFixture.configureByText("test.varlink",
                "interface org.example.test\n" +
                        "type Metadata(a: int)\n" +
                        "type Status(ok: bool)\n");
        List<NavigationItem> items = symbolsNamed("Metadata");
        assertSize(1, items);
        assertInstanceOf(items.get(0), VarlinkTypeAlias.class);
        assertEquals("Metadata", items.get(0).getName());
    }

    public void testUnknownSymbolNameYieldsNothing() {
        myFixture.configureByText("test.varlink",
                "interface org.example.test\n" +
                        "type Metadata(a: int)\n");
        assertEmpty(symbolsNamed("DoesNotExist"));
    }
}
