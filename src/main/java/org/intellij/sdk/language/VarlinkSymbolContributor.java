package org.intellij.sdk.language;

import com.intellij.navigation.ChooseByNameContributorEx;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.Processor;
import com.intellij.util.indexing.FindSymbolParameters;
import com.intellij.util.indexing.IdFilter;
import org.intellij.sdk.language.psi.VarlinkTypeAlias;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Makes the custom types of every {@code .varlink} file available to the IDE's
 * "Navigate | Symbol" (Go to Symbol) action. Only {@code type} declarations are
 * contributed, since those are the named entities users navigate to by symbol.
 */
public class VarlinkSymbolContributor implements ChooseByNameContributorEx {

    @Override
    public void processNames(@NotNull Processor<? super String> processor,
                             @NotNull GlobalSearchScope scope,
                             @Nullable IdFilter filter) {
        Project project = scope.getProject();
        if (project == null) {
            return;
        }
        for (VirtualFile file : FileTypeIndex.getFiles(VarlinkFileType.INSTANCE, scope)) {
            PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
            for (VarlinkTypeAlias typeAlias : VarlinkUtil.findTypeAliases(psiFile)) {
                String name = typeAlias.getName();
                if (name != null && !name.isEmpty() && !processor.process(name)) {
                    return;
                }
            }
        }
    }

    @Override
    public void processElementsWithName(@NotNull String name,
                                        @NotNull Processor<? super NavigationItem> processor,
                                        @NotNull FindSymbolParameters parameters) {
        Project project = parameters.getProject();
        for (VirtualFile file : FileTypeIndex.getFiles(VarlinkFileType.INSTANCE, parameters.getSearchScope())) {
            PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
            for (VarlinkTypeAlias typeAlias : VarlinkUtil.findTypeAliases(psiFile)) {
                if (name.equals(typeAlias.getName()) && !processor.process((NavigationItem) typeAlias)) {
                    return;
                }
            }
        }
    }
}
