package org.intellij.sdk.language;

import com.intellij.lang.Language;
public class VarlinkLanguage extends Language {

    public static final VarlinkLanguage INSTANCE = new VarlinkLanguage();

    private VarlinkLanguage() {
        super("Varlink");
    }
}
