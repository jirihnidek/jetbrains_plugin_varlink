package org.intellij.sdk.language;

import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.util.treeView.smartTree.TreeElement;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

import java.util.ArrayList;
import java.util.List;

/**
 * Tests for {@link VarlinkStructureViewFactory} and its model/elements: the file
 * root lists the interface members (types, methods, errors), and each member lists
 * its fields.
 */
public class VarlinkStructureViewFactoryTest extends BasePlatformTestCase {

    private StructureViewTreeElement root(String source) {
        myFixture.configureByText("test.varlink", source);
        return (StructureViewTreeElement) new VarlinkStructureViewModel(null, myFixture.getFile()).getRoot();
    }

    private static List<String> names(TreeElement[] elements) {
        List<String> names = new ArrayList<>();
        for (TreeElement element : elements) {
            names.add(element.getPresentation().getPresentableText());
        }
        return names;
    }

    private static TreeElement childNamed(TreeElement[] elements, String name) {
        for (TreeElement element : elements) {
            if (name.equals(element.getPresentation().getPresentableText())) {
                return element;
            }
        }
        return null;
    }

    public void testRootShowsInterfaceName() {
        StructureViewTreeElement root = root(
                "interface org.example.test\n" +
                        "type Metadata(a: int)\n");
        assertEquals("org.example.test", root.getPresentation().getPresentableText());
    }

    public void testTopLevelMembersAreListed() {
        TreeElement[] members = root(
                "interface org.example.test\n" +
                        "type Metadata(a: int)\n" +
                        "error Boom(message: string)\n" +
                        "method Ping() -> ()\n").getChildren();
        assertSameElements(names(members), "Metadata", "Boom", "Ping");
    }

    public void testMembersAreInSourceOrder() {
        // Written method, then type, then error: the tree must preserve that order
        // rather than grouping by category.
        TreeElement[] members = root(
                "interface org.example.test\n" +
                        "method Ping() -> ()\n" +
                        "type Metadata(a: int)\n" +
                        "error Boom(message: string)\n").getChildren();
        assertOrderedEquals(names(members), "Ping", "Metadata", "Boom");
    }

    public void testMembersHaveNoLocationString() {
        TreeElement[] members = root(
                "interface org.example.test\n" +
                        "type Metadata(a: int)\n").getChildren();
        TreeElement metadata = childNamed(members, "Metadata");
        assertNotNull(metadata);
        assertNull(metadata.getPresentation().getLocationString());
    }

    public void testFieldShowsTypeAsLocationString() {
        TreeElement[] members = root(
                "interface org.example.test\n" +
                        "type Metadata(count: int, name: ?string)\n").getChildren();
        TreeElement[] fields = childNamed(members, "Metadata").getChildren();
        assertEquals("int", childNamed(fields, "count").getPresentation().getLocationString());
        assertEquals("?string", childNamed(fields, "name").getPresentation().getLocationString());
    }

    public void testNestedObjectFieldsAreShown() {
        TreeElement[] members = root(
                "interface org.example.test\n" +
                        "type Config(\n" +
                        "    foo: (\n" +
                        "        bar: int,\n" +
                        "        pub: bool\n" +
                        "    )\n" +
                        ")\n").getChildren();
        TreeElement[] configFields = childNamed(members, "Config").getChildren();
        TreeElement foo = childNamed(configFields, "foo");
        assertNotNull(foo);
        // The inline object itself has no type text on the right, but expands.
        assertNull(foo.getPresentation().getLocationString());
        TreeElement[] nested = foo.getChildren();
        assertSameElements(names(nested), "bar", "pub");
        assertEquals("int", childNamed(nested, "bar").getPresentation().getLocationString());
        assertEquals("bool", childNamed(nested, "pub").getPresentation().getLocationString());
    }

    public void testTypeFieldsAreListed() {
        TreeElement[] members = root(
                "interface org.example.test\n" +
                        "type Metadata(user_agent: string, locale: string)\n").getChildren();
        TreeElement metadata = childNamed(members, "Metadata");
        assertNotNull(metadata);
        assertSameElements(names(metadata.getChildren()), "user_agent", "locale");
    }

    public void testMethodInputAndOutputFieldsAreListed() {
        TreeElement[] members = root(
                "interface org.example.test\n" +
                        "method Ping(meta: string) -> (status: object)\n").getChildren();
        TreeElement ping = childNamed(members, "Ping");
        assertNotNull(ping);
        // Both input and output parameters appear under the method.
        assertSameElements(names(ping.getChildren()), "meta", "status");
    }

    public void testEmptyBodyHasNoChildren() {
        TreeElement[] members = root(
                "interface org.example.test\n" +
                        "method Ping() -> ()\n").getChildren();
        TreeElement ping = childNamed(members, "Ping");
        assertNotNull(ping);
        assertEmpty(ping.getChildren());
    }

    public void testFactoryProducesABuilder() {
        myFixture.configureByText("test.varlink",
                "interface org.example.test\n" +
                        "type Metadata(a: int)\n");
        assertNotNull(new VarlinkStructureViewFactory().getStructureViewBuilder(myFixture.getFile()));
    }
}
