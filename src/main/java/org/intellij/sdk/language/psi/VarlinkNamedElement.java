package org.intellij.sdk.language.psi;

import com.intellij.psi.PsiNameIdentifierOwner;

/**
 * Common interface for the named Varlink elements: type aliases, methods,
 * errors and struct/enum fields. Extending {@link PsiNameIdentifierOwner} gives
 * these elements a name and a name-identifier, which is what an annotator,
 * navigation, find-usages and rename all build upon.
 */
public interface VarlinkNamedElement extends PsiNameIdentifierOwner {
}
