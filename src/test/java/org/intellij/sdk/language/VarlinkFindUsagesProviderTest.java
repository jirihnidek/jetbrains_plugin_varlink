package org.intellij.sdk.language;

import com.intellij.psi.PsiElement;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.intellij.sdk.language.psi.VarlinkTypeAlias;

import java.util.Collection;

/**
 * Tests for {@link VarlinkFindUsagesProvider}: a {@code type} declaration can be
 * searched, and "Find Usages" lists every usage of that type in the file.
 */
public class VarlinkFindUsagesProviderTest extends BasePlatformTestCase {

    private VarlinkTypeAlias configureAndGetType(String source, String typeName) {
        myFixture.configureByText("test.varlink", source);
        for (VarlinkTypeAlias typeAlias : VarlinkUtil.findTypeAliases(myFixture.getFile())) {
            if (typeName.equals(typeAlias.getName())) {
                return typeAlias;
            }
        }
        fail("Type '" + typeName + "' not found in test source");
        return null;
    }

    public void testTypeCanBeFound() {
        VarlinkTypeAlias type = configureAndGetType(
                "interface org.example.test\n" +
                        "type Metadata(a: int)\n", "Metadata");
        VarlinkFindUsagesProvider provider = new VarlinkFindUsagesProvider();
        assertTrue("A type declaration must be searchable", provider.canFindUsagesFor(type));
        assertEquals("type", provider.getType(type));
        assertEquals("Metadata", provider.getDescriptiveName(type));
    }

    public void testFindsAllUsagesOfType() {
        VarlinkTypeAlias type = configureAndGetType(
                "interface org.example.test\n" +
                        "type Metadata(a: int)\n" +
                        "method Ping(meta: Metadata) -> (copy: Metadata)\n" +
                        "error Boom(detail: Metadata)\n", "Metadata");

        Collection<UsageInfoLike> usages = findUsageOffsets(type);
        // Three usages: method input, method output and error parameter.
        assertEquals(3, usages.size());
    }

    public void testUnusedTypeHasNoUsages() {
        VarlinkTypeAlias type = configureAndGetType(
                "interface org.example.test\n" +
                        "type Metadata(a: int)\n" +
                        "type Other(a: int)\n", "Metadata");
        assertEmpty(findUsageOffsets(type));
    }

    /** Thin wrapper so the test does not depend on the UsageInfo API surface. */
    private record UsageInfoLike(int offset) {
    }

    private Collection<UsageInfoLike> findUsageOffsets(PsiElement element) {
        return myFixture.findUsages(element).stream()
                .filter(usage -> usage.getElement() != null)
                .map(usage -> new UsageInfoLike(usage.getElement().getTextOffset()))
                .toList();
    }
}
