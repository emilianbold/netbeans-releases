/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.refactoring.java.plugins;

import java.net.URL;
import org.netbeans.api.java.source.ClassIndex.SearchScopeType;
import org.netbeans.modules.refactoring.java.spi.JavaRefactoringPlugin;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.*;
import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.*;
import org.netbeans.modules.refactoring.java.WhereUsedElement;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.ProgressEvent;
import org.netbeans.modules.refactoring.api.WhereUsedQuery;
import org.netbeans.modules.refactoring.java.RetoucheUtils;
import org.netbeans.modules.refactoring.java.SourceUtilsEx;
import org.netbeans.modules.refactoring.java.api.WhereUsedQueryConstants;
import org.netbeans.modules.refactoring.api.Scope;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author  Jan Becicka
 * @author  Ralph Ruijs
 */
public class JavaWhereUsedQueryPlugin extends JavaRefactoringPlugin {
    private WhereUsedQuery refactoring;
    private ClasspathInfo cp;
    private TreePathHandle basem;
    
    /** Creates a new instance of WhereUsedQuery */
    public JavaWhereUsedQueryPlugin(WhereUsedQuery refactoring) {
        this.refactoring = refactoring;
    }
    
    protected JavaSource getJavaSource(Phase p) {
        switch (p) {
        default: 
            return JavaSource.forFileObject(refactoring.getRefactoringSource().lookup(TreePathHandle.class).getFileObject());
        }
    }
    
    @Override
    public Problem preCheck() {
        cancelRequest = false;
        TreePathHandle handle = refactoring.getRefactoringSource().lookup(TreePathHandle.class);
        if (!handle.getFileObject().isValid() || RetoucheUtils.getElementKind(handle) == null) {
            return new Problem(true, NbBundle.getMessage(FindVisitor.class, "DSC_ElNotAvail")); // NOI18N
        }
        if (handle.getKind() == Tree.Kind.ARRAY_TYPE) {
            return new Problem(true, NbBundle.getMessage(FindVisitor.class, "ERR_FindUsagesArrayType"));
        }
        return null;
    }
    
    private Set<FileObject> getRelevantFiles(final TreePathHandle tph) {
        Set<FileObject> fileSet;
        Scope customScope = refactoring.getContext().lookup(Scope.class);
        if (customScope != null) {
            fileSet = new HashSet<FileObject>();
            fileSet.addAll(customScope.getFiles());
            if (!customScope.getSourceRoots().isEmpty()) {
                ClassPath cpath = ClassPathSupport.createClassPath(customScope.getSourceRoots().toArray(new FileObject[0]));
                fileSet.addAll(getRelevantFiles(tph,
                        ClasspathInfo.create(ClassPath.EMPTY, ClassPath.EMPTY, cpath),
                        isFindSubclasses(),
                        isFindDirectSubclassesOnly(),
                        isFindOverridingMethods(),
                        isFindUsages(),
                        null));
            }
            Map<FileObject, Set<NonRecursiveFolder>> folders = new HashMap<FileObject, Set<NonRecursiveFolder>>();
            
            for(NonRecursiveFolder nonRecursiveFolder : customScope.getFolders()) {
                FileObject folder = nonRecursiveFolder.getFolder();
                ClassPath cp = ClassPath.getClassPath(folder, ClassPath.SOURCE);
                final FileObject sourceRoot = cp.findOwnerRoot(folder);
                Set<NonRecursiveFolder> packages = folders.get(sourceRoot);
                if(packages == null) {
                    packages = new HashSet<NonRecursiveFolder>();
                    folders.put(sourceRoot, packages);
                }
                packages.add(nonRecursiveFolder);
            }
            
            for (FileObject sourceRoot : folders.keySet()) {
                Set<NonRecursiveFolder> packages = folders.get(sourceRoot);
                if (packages != null && !packages.isEmpty()) {
                    ClassPath cpath = ClassPathSupport.createClassPath(sourceRoot);
                    fileSet.addAll(getRelevantFiles(tph,
                            ClasspathInfo.create(ClassPath.EMPTY, ClassPath.EMPTY, cpath),
                            isFindSubclasses(),
                            isFindDirectSubclassesOnly(),
                            isFindOverridingMethods(),
                            isFindUsages(), packages));
                }
            }
            return fileSet;
        } else {
            JavaSource source = JavaSource.forFileObject(tph.getFileObject());
            try {
                source.runWhenScanFinished(new Task<CompilationController>() {

                    @Override
                    public void run(CompilationController info) throws Exception {
                        Element element = tph.resolveElement(info);
                        ElementKind kind = element.getKind();
                        if (kind == ElementKind.METHOD && isSearchFromBaseClass()) {
                            Collection<ExecutableElement> overridens = RetoucheUtils.getOverridenMethods((ExecutableElement)element, info);
                            if(!overridens.isEmpty()) {
                                ExecutableElement el = (ExecutableElement) overridens.iterator().next();                        
                                assert el!=null;
                                basem = TreePathHandle.create(el, info);
                                refactoring.setRefactoringSource(Lookups.fixed(basem));
                            }
                            if (basem != null && (basem.getFileObject() == null || basem.getFileObject().getNameExt().endsWith("class"))) { //NOI18N
                                cp = RetoucheUtils.getClasspathInfoFor(tph, basem);
                            } else {
                                cp = RetoucheUtils.getClasspathInfoFor(basem!=null?basem:tph);
                            }
                        }
                    }
                }, true);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            if(cp == null) {
                cp = getClasspathInfo(refactoring);
            }
            fileSet = getRelevantFiles(
                    basem!=null?basem:tph,
                    cp,
                    isFindSubclasses(),
                    isFindDirectSubclassesOnly(),
                    isFindOverridingMethods(),
                    isFindUsages(),
                    null);
        }
        return fileSet;
    }
    
    public static Set<FileObject> getRelevantFiles(
            final TreePathHandle tph, final ClasspathInfo cpInfo,
            final boolean isFindSubclasses, final boolean isFindDirectSubclassesOnly,
            final boolean isFindOverridingMethods, final boolean isFindUsages,
            final Set<NonRecursiveFolder> folders) {
        final ClassIndex idx = cpInfo.getClassIndex();
        final Set<FileObject> set = new HashSet<FileObject>();
        final Set<NonRecursiveFolder> packages = (folders == null)? Collections.EMPTY_SET : folders;
        
        final FileObject file = tph.getFileObject();
        JavaSource source;
        if (file!=null) {
           final ClassPath mergedPlatformPath = merge(cpInfo.getClassPath(ClasspathInfo.PathKind.BOOT),ClassPath.getClassPath(file, ClassPath.BOOT));
           final ClassPath mergedCompilePath  = merge(cpInfo.getClassPath(ClasspathInfo.PathKind.COMPILE),ClassPath.getClassPath(file, ClassPath.COMPILE));
           final ClassPath mergedSourcePath   = merge(cpInfo.getClassPath(ClasspathInfo.PathKind.SOURCE),ClassPath.getClassPath(file, ClassPath.SOURCE));
           final ClasspathInfo mergedInfo = ClasspathInfo.create(mergedPlatformPath, mergedCompilePath, mergedSourcePath);
           source = JavaSource.create(mergedInfo, new FileObject[]{tph.getFileObject()});
        } else {
            source = JavaSource.create(cpInfo);
        }
        //XXX: This is slow!
        CancellableTask<CompilationController> task = new CancellableTask<CompilationController>() {
            public void cancel() {
            }
            
            public void run(CompilationController info) throws Exception {
                info.toPhase(JavaSource.Phase.RESOLVED);
                final Element el = tph.resolveElement(info);
                if (el == null) {
                    throw new NullPointerException(String.format("#145291: Cannot resolve handle: %s\n%s", tph, info.getClasspathInfo())); // NOI18N
                }
                Set<SearchScopeType> searchScopeType = new HashSet<SearchScopeType>(1);
                if(packages.isEmpty()) {
                    searchScopeType.add(ClassIndex.SearchScope.SOURCE);
                } else {
                    final Set<String> packageSet = new HashSet<String>(packages.size());
                    for (NonRecursiveFolder nonRecursiveFolder : packages) {
                        String resourceName = info.getClasspathInfo().getClassPath(ClasspathInfo.PathKind.SOURCE).getResourceName(nonRecursiveFolder.getFolder());
                        packageSet.add(resourceName);
                    }
                    searchScopeType.add(new SearchScopeType() {
                        public Set<? extends String> getPackages() {
                            return packageSet;
                        }
                        public boolean isSources() {
                            return true;
                        }
                        public boolean isDependencies() {
                            return false;
                        }
                    });
                }                
                if (el.getKind().isField()) {
                    //get field references from index
                    set.addAll(idx.getResources(ElementHandle.create((TypeElement)el.getEnclosingElement()), EnumSet.of(ClassIndex.SearchKind.FIELD_REFERENCES), searchScopeType));
                } else if (el.getKind().isClass() || el.getKind().isInterface()) {
                    if (isFindSubclasses || isFindDirectSubclassesOnly) {
                        if (isFindDirectSubclassesOnly) {
                            //get direct implementors from index
                            EnumSet searchKind = EnumSet.of(ClassIndex.SearchKind.IMPLEMENTORS);
                            set.addAll(idx.getResources(ElementHandle.create((TypeElement)el), searchKind, searchScopeType));
                        } else {
                            //itererate implementors recursively
                            set.addAll(getImplementorsRecursive(idx, cpInfo, (TypeElement)el));
                        }
                    } else {
                        //get type references from index
                        set.addAll(idx.getResources(ElementHandle.create((TypeElement) el), EnumSet.of(ClassIndex.SearchKind.TYPE_REFERENCES, ClassIndex.SearchKind.IMPLEMENTORS), searchScopeType));
                    }
                } else if (el.getKind() == ElementKind.METHOD && isFindOverridingMethods) {
                    //Find overriding methods
                    TypeElement type = (TypeElement) el.getEnclosingElement();
                    set.addAll(getImplementorsRecursive(idx, cpInfo, type));
                } 
                if (el.getKind() == ElementKind.METHOD && isFindUsages) {
                    //get method references for method and for all it's overriders
                    Set<ElementHandle<TypeElement>> s = RetoucheUtils.getImplementorsAsHandles(idx, cpInfo, (TypeElement)el.getEnclosingElement());
                    for (ElementHandle<TypeElement> eh:s) {
                        TypeElement te = eh.resolve(info);
                        if (te==null) {
                            continue;
                        }
                        for (Element e:te.getEnclosedElements()) {
                            if (e instanceof ExecutableElement) {
                                if (info.getElements().overrides((ExecutableElement)e, (ExecutableElement)el, te)) {
                                    set.addAll(idx.getResources(ElementHandle.create(te), EnumSet.of(ClassIndex.SearchKind.METHOD_REFERENCES), searchScopeType));
                                }
                            }
                        }
                    }
                    set.addAll(idx.getResources(ElementHandle.create((TypeElement) el.getEnclosingElement()), EnumSet.of(ClassIndex.SearchKind.METHOD_REFERENCES), searchScopeType)); //?????
                } else if (el.getKind() == ElementKind.CONSTRUCTOR) {
                        set.addAll(idx.getResources(ElementHandle.create((TypeElement) el.getEnclosingElement()), EnumSet.of(ClassIndex.SearchKind.TYPE_REFERENCES, ClassIndex.SearchKind.IMPLEMENTORS), searchScopeType));
                }
                    
            }
        };
        try {
            source.runUserActionTask(task, true);
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
        return set;
    }
    
    private static Collection<FileObject> getImplementorsRecursive(ClassIndex idx, ClasspathInfo cpInfo, TypeElement el) {
        Set<?> implementorsAsHandles = RetoucheUtils.getImplementorsAsHandles(idx, cpInfo, el);

        @SuppressWarnings("unchecked")
        Collection<FileObject> set = SourceUtilsEx.getFiles((Collection<ElementHandle<? extends Element>>) implementorsAsHandles, cpInfo);

        // filter out files that are not on source path
        ClassPath source = cpInfo.getClassPath(ClasspathInfo.PathKind.SOURCE);
        Collection<FileObject> set2 = new ArrayList<FileObject>(set.size());
        for (FileObject fo : set) {
            if (source.contains(fo)) {
                set2.add(fo);
            }
        }
        return set2;
    }
    
    //@Override
    public Problem prepare(final RefactoringElementsBag elements) {
        fireProgressListenerStart(ProgressEvent.START, -1);
        Set<FileObject> a = getRelevantFiles(refactoring.getRefactoringSource().lookup(TreePathHandle.class));
        fireProgressListenerStep(a.size());
        Problem problem = null;
        try {
            processFiles(a, new FindTask(elements));
        } catch (IOException e) {
            problem = createProblemAndLog(null, e);
        }
        fireProgressListenerStop();
        return problem;
    }
    
    @Override
    public Problem fastCheckParameters() {
        if (refactoring.getRefactoringSource().lookup(TreePathHandle.class).getKind() == Tree.Kind.METHOD) {
            return checkParametersForMethod(isFindOverridingMethods(), isFindUsages());
        } 
        return null;
    }
    
    @Override
    public Problem checkParameters() {
        return null;
    }
    
    private Problem checkParametersForMethod(boolean overriders, boolean usages) {
        if (!(usages || overriders)) {
            return new Problem(true, NbBundle.getMessage(JavaWhereUsedQueryPlugin.class, "MSG_NothingToFind"));
        } else
            return null;
    }
    
    public static CloneableEditorSupport findCloneableEditorSupport(DataObject dob) {
        Object obj = dob.getCookie(org.openide.cookies.OpenCookie.class);
        if (obj instanceof CloneableEditorSupport) {
            return (CloneableEditorSupport)obj;
        }
        obj = dob.getCookie(org.openide.cookies.EditorCookie.class);
        if (obj instanceof CloneableEditorSupport) {
            return (CloneableEditorSupport)obj;
        }
        return null;
    }
    
    private boolean isFindSubclasses() {
        return refactoring.getBooleanValue(WhereUsedQueryConstants.FIND_SUBCLASSES);
    }
    private boolean isFindUsages() {
        return refactoring.getBooleanValue(WhereUsedQuery.FIND_REFERENCES);
    }
    private boolean isFindDirectSubclassesOnly() {
        return refactoring.getBooleanValue(WhereUsedQueryConstants.FIND_DIRECT_SUBCLASSES);
    }
    
    private boolean isFindOverridingMethods() {
        return refactoring.getBooleanValue(WhereUsedQueryConstants.FIND_OVERRIDING_METHODS);
    }
    private boolean isSearchFromBaseClass() {
        return refactoring.getBooleanValue(WhereUsedQueryConstants.SEARCH_FROM_BASECLASS);
    }

    private static ClassPath merge (final ClassPath... cps) {
        final Set<URL> roots = new LinkedHashSet<URL>();
        for (final ClassPath cp : cps) {
            if (cp != null) {
                for (final ClassPath.Entry entry : cp.entries()) {
                    final URL root = entry.getURL();
                    if (!roots.contains(root)) {
                        roots.add(root);
                    }
                }
            }
        }
        return ClassPathSupport.createClassPath(roots.toArray(new URL[roots.size()]));
    }
    
    private class FindTask implements CancellableTask<WorkingCopy> {

        private RefactoringElementsBag elements;
        private volatile boolean cancelled;

        public FindTask(RefactoringElementsBag elements) {
            super();
            this.elements = elements;
        }

        public void cancel() {
            cancelled=true;
        }

        public void run(WorkingCopy compiler) throws IOException {
            if (cancelled)
                return ;
            if (compiler.toPhase(JavaSource.Phase.RESOLVED)!=JavaSource.Phase.RESOLVED) {
                return;
            }
            CompilationUnitTree cu = compiler.getCompilationUnit();
            if (cu == null) {
                ErrorManager.getDefault().log(ErrorManager.ERROR, "compiler.getCompilationUnit() is null " + compiler); // NOI18N
                return;
            }
            TreePathHandle handle = refactoring.getRefactoringSource().lookup(TreePathHandle.class);
            Element element = handle.resolveElement(compiler);
            if (element==null) {
                ErrorManager.getDefault().log(ErrorManager.ERROR, "element is null for handle " + handle); // NOI18N
                return;
            }
            
            Collection<TreePath> result = new ArrayList<TreePath>();
            if (isFindUsages()) {
                FindUsagesVisitor findVisitor = new FindUsagesVisitor(compiler, refactoring.getBooleanValue(WhereUsedQuery.SEARCH_IN_COMMENTS));
                findVisitor.scan(compiler.getCompilationUnit(), element);
                result.addAll(findVisitor.getUsages());
                for (FindUsagesVisitor.UsageInComment usageInComment : findVisitor.getUsagesInComments()) {
                    elements.add(refactoring, WhereUsedElement.create(usageInComment.from, usageInComment.to, compiler));
                }
            }
            if (element.getKind() == ElementKind.METHOD && isFindOverridingMethods()) {
                FindOverridingVisitor override = new FindOverridingVisitor(compiler);
                override.scan(compiler.getCompilationUnit(), element);
                result.addAll(override.getUsages());
            } else if ((element.getKind().isClass() || element.getKind().isInterface()) &&
                    (isFindSubclasses()||isFindDirectSubclassesOnly())) {
                FindSubtypesVisitor subtypes = new FindSubtypesVisitor(!isFindDirectSubclassesOnly(), compiler);
                subtypes.scan(compiler.getCompilationUnit(), element);
                result.addAll(subtypes.getUsages());
            }
            for (TreePath tree : result) {
                elements.add(refactoring, WhereUsedElement.create(compiler, tree));
            }
            fireProgressListenerStep();
        }
    }

}
