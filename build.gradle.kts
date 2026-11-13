import org.jetbrains.intellij.platform.gradle.TestFrameworkType
import org.jetbrains.intellij.platform.gradle.tasks.GenerateLexerTask
import org.jetbrains.intellij.platform.gradle.tasks.GenerateParserTask

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.changelog")
    id("org.jetbrains.intellij.platform")
    // Grammar-Kit / JFlex integration shipped with the IntelliJ Platform Gradle Plugin.
    id("org.jetbrains.intellij.platform.grammarkit")
}

// Read more: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin.html
dependencies {
    testImplementation(libs.junit)

    // IntelliJ Platform Gradle Plugin Dependencies Extension - read more: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-dependencies-extension.html
    intellijPlatform {
        intellijIdea("2026.2.4")
        testFramework(TestFrameworkType.Platform)

        // Add plugin dependencies for compilation here, for example:
        // bundledPlugin("com.intellij.java")
    }
}

// Generate the lexer (JFlex) and parser/PSI (Grammar-Kit) from the .flex and .bnf
// definitions. Outputs land under build/generated/sources and are added as source
// roots below so they are compiled together with the hand-written sources.
val generateLexerTask = tasks.named<GenerateLexerTask>("generateLexer") {
    sourceFile.set(file("src/main/java/org/intellij/sdk/language/Varlink.flex"))
    // The target package (org.intellij.sdk.language.lexer) is read from the .flex file.
    purgeOldFiles.set(true)
}

val generateParserTask = tasks.named<GenerateParserTask>("generateParser") {
    sourceFile.set(file("src/main/java/org/intellij/sdk/language/Varlink.bnf"))
    pathToParser.set("org/intellij/sdk/language/parser/VarlinkParser.java")
    pathToPsiRoot.set("org/intellij/sdk/language/psi")
    purgeOldFiles.set(true)
}

sourceSets {
    main {
        java {
            srcDir(generateLexerTask.flatMap { it.targetRootOutputDir })
            srcDir(generateParserTask.flatMap { it.targetRootOutputDir })
        }
    }
}

tasks {
    withType<JavaCompile> {
        dependsOn(generateLexerTask, generateParserTask)
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        dependsOn(generateLexerTask, generateParserTask)
    }
}
