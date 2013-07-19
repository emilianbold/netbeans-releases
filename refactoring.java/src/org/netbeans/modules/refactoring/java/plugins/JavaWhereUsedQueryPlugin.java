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

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.java.source.*;
import org.netbeans.api.java.source.ClassIndex.SearchScopeType;
import org.netbeans.modules.refactoring.api.*;
import org.netbeans.modules.refactoring.java.RefactoringUtils;
import org.netbeans.modules.refactoring.java.SourceUtilsEx;
import org.netbeans.modules.refactoring.java.WhereUsedElement;
import org.netbeans.modules.refactoring.java.api.JavaRefactoringUtils;
import org.netbeans.modules.refactoring.java.api.WhereUsedQueryConstants;
import org.netbeans.modules.refactoring.java.spi.JavaRefactoringPlugin;
import org.netbeans.modules.refactoring.java.spi.JavaWhereUsedFilters;
import org.netbeans.modules.refactoring.java.spi.JavaWhereUsedFilters.ReadWrite;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.ui.FiltersDescription;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 *
 * @author  Jan Becicka
 * @author  Ralph Ruijs
 */
public class JavaWhereUsedQueryPlugin extends JavaRefactoringPlugin implements FiltersDescription.Provider {
    private boolean fromLibrary;
    private WhereUsedQuery refactoring;
    private ClasspathInfo cp;
    
    private volatile CancellableTask queryTask;

    /** Creates a new instance of WhereUsedQuery */
    public JavaWhereUsedQueryPlugin(WhereUsedQuery refactoring) {
        this.refactoring = refactoring;
    }
    
    @Override
    protected JavaSource getJavaSource(Phase p) {
        switch (p) {
        default: 
            return JavaSource.forFileObject(refactoring.getRefactoringSource().lookup(TreePathHandle.class).getFileObject());
        }
    }
    
    @Override
    public Problem preCheck() {
        cancelRequest = false;
        cancelRequested.set(false);
        TreePathHandle handle = refactoring.getRefactoringSource().lookup(TreePathHandle.class);
        if (!handle.getFileObject().isValid()) {
            return new Problem(true, NbBundle.getMessage(FindVisitor.class, "DSC_ElNotAvail")); // NOI18N
        }
        if (handle.getKind() == Tree.Kind.ARRAY_TYPE) {
            return new Problem(true, NbBundle.getMessage(FindVisitor.class, "ERR_FindUsagesArrayType"));
        }
        return super.preCheck();
    }

    @Override
    protected Problem preCheck(CompilationController javac) throws IOException {
        TreePathHandle handle = refactoring.getRefactoringSource().lookup(TreePathHandle.class);
        if (handle.resolveElement(javac) == null) {
            return new Problem(true, NbBundle.getMessage(FindVisitor.class, "DSC_ElNotAvail")); // NOI18N
        }
        return null;
    }
    
    @Override
    protected ClasspathInfo getClasspathInfo(AbstractRefactoring refactoring) {
        ClasspathInfo cpInfo;
        Collection<? extends TreePathHandle> handles = refactoring.getRefactoringSource().lookupAll(TreePathHandle.class);
        if (!handles.isEmpty()) {
            cpInfo = RefactoringUtils.getClasspathInfoFor(handles.toArray(new TreePathHandle[handles.size()]));
        } else {
            cpInfo = JavaRefactoringUtils.getClasspathInfoFor((FileObject)null);
        }
        refactoring.getContext().add(cpInfo);
        return cpInfo;
    }
    
    private Set<FileObject> getRelevantFiles(final TreePathHandle tph) {
        Set<FileObject> fileSet;
        cp = getClasspathInfo(refactoring);
        fromLibrary = tph.getFileObject() == null || tph.getFileObject().getNameExt().endsWith("class"); // NOI18N
        if(isSearchFromBaseClass()) {
            TreePathHandle sourceHandle = refactoring.getContext().lookup(TreePathHandle.class);
            if (fromLibrary && sourceHandle != null) {
                cp = RefactoringUtils.getClasspathInfoFor(sourceHandle, tph);
            } else {
                cp = RefactoringUtils.getClasspathInfoFor(tph);
            }
        }
        
        Scope customScope = refactoring.getContext().lookup(Scope.class);
        ClasspathInfo cpath;
        if (customScope != null) {
            fileSet = new TreeSet<FileObject>(new FileComparator());
            fileSet.addAll(customScope.getFiles());
            FileObject fo = null;
            if(fromLibrary) {
                fo = RefactoringUtils.getFileObject(tph);
                if (fo == null) {
                    fo = tph.getFileObject();
                }
            }
            if (!customScope.getSourceRoots().isEmpty()) {
                if(isSearchFromBaseClass() && fo != null) {
                    HashSet<FileObject> fileobjects = new HashSet<FileObject>(customScope.getSourceRoots());
                    fileobjects.add(fo);
                    cpath = RefactoringUtils.getClasspathInfoFor(false, fileobjects.toArray(new FileObject[0]));
                } else {
                    cpath = RefactoringUtils.getClasspathInfoFor(false, customScope.getSourceRoots().toArray(new FileObject[0]));
                }
                fileSet.addAll(getRelevantFiles(tph,
                        cpath,
                        isFindSubclasses(),
                        isFindDirectSubclassesOnly(),
                        isFindOverridingMethods(),
                        isSearchOverloadedMethods(),
                        isFindUsages(),
                        null, cancelRequested));
            }
            Map<FileObject, Set<NonRecursiveFolder>> folders = new HashMap<FileObject, Set<NonRecursiveFolder>>();
            
            for(NonRecursiveFolder nonRecursiveFolder : customScope.getFolders()) {
                FileObject folder = nonRecursiveFolder.getFolder();
                ClassPath classPath = ClassPath.getClassPath(folder, ClassPath.SOURCE);
                final FileObject sourceRoot = classPath.findOwnerRoot(folder);
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
                    if(isSearchFromBaseClass() && fo != null) {
                        cpath = RefactoringUtils.getClasspathInfoFor(false, sourceRoot, fo);
                    } else {
                        cpath = RefactoringUtils.getClasspathInfoFor(false, sourceRoot);
                    }
                    fileSet.addAll(getRelevantFiles(tph,
                            cpath,
                            isFindSubclasses(),
                            isFindDirectSubclassesOnly(),
                            isFindOverridingMethods(),
                            isSearchOverloadedMethods(),
                            isFindUsages(), packages, cancelRequested));
                }
            }
            return fileSet;
        } else {
            fileSet = getRelevantFiles(
                    tph,
                    cp,
                    isFindSubclasses(),
                    isFindDirectSubclassesOnly(),
                    isFindOverridingMethods(),
                    isSearchOverloadedMethods(),
                    isFindUsages(),
                    null,
                    cancelRequested);
        }
        return fileSet;
    }
    
    public static Set<FileObject> getRelevantFiles(
            final TreePathHandle tph, final ClasspathInfo cpInfo,
            final boolean isFindSubclasses, final boolean isFindDirectSubclassesOnly,
            final boolean isFindOverridingMethods, final boolean isSearchOverloadedMethods,
            final boolean isFindUsages, final Set<NonRecursiveFolder> folders,
            final AtomicBoolean cancel) {
        final ClassIndex idx = cpInfo.getClassIndex();
        final Set<FileObject> set = new TreeSet<FileObject>(new FileComparator());
        final Set<NonRecursiveFolder> packages = (folders == null)? Collections.<NonRecursiveFolder>emptySet() : folders;
        
        final FileObject file = tph.getFileObject();
        JavaSource source;
        source = JavaPluginUtils.createSource(file, cpInfo, tph);
        if(cancel != null && cancel.get()) {
            return Collections.<FileObject>emptySet();
        }
        CancellableTask<CompilationController> task = new CancellableTask<CompilationController>() {
            @Override
            public void cancel() {
            }
            
            @Override
            public void run(CompilationController info) throws Exception {
                info.toPhase(JavaSource.Phase.RESOLVED);
                final Element el = tph.resolveElement(info);
                if (el == null) {
                    throw new NullPointerException(String.format("#145291: Cannot resolve handle: %s\n%s", tph, info.getClasspathInfo())); // NOI18N
                }
                Set<SearchScopeType> searchScopeType = new HashSet<SearchScopeType>(1);
                if (packages.isEmpty()) {
                    searchScopeType.add(ClassIndex.SearchScope.SOURCE);
                } else {
                    final Set<String> packageSet = new HashSet<String>(packages.size());
                    for (NonRecursiveFolder nonRecursiveFolder : packages) {
                        String resourceName = info.getClasspathInfo().getClassPath(ClasspathInfo.PathKind.SOURCE).getResourceName(nonRecursiveFolder.getFolder());
                        packageSet.add(resourceName.replace('/', '.'));
                    }
                    searchScopeType.add(new SearchScopeType() {
                        @Override
                        public Set<? extends String> getPackages() {
                            return packageSet;
                        }

                        @Override
                        public boolean isSources() {
                            return true;
                        }

                        @Override
                        public boolean isDependencies() {
                            return false;
                        }
                    });
                }
                if (cancel != null && cancel.get()) {
                    set.clear();
                    return;
                }
                if (el.getKind().isField()) {
                    //get field references from index
                    set.addAll(idx.getResources(ElementHandle.create((TypeElement) el.getEnclosingElement()), EnumSet.of(ClassIndex.SearchKind.FIELD_REFERENCES), searchScopeType));
                } else if (el.getKind().isClass() || el.getKind().isInterface()) {
                    if (isFindSubclasses || isFindDirectSubclassesOnly) {
                        if (isFindDirectSubclassesOnly) {
                            //get direct implementors from index
                            EnumSet searchKind = EnumSet.of(ClassIndex.SearchKind.IMPLEMENTORS);
                            set.addAll(idx.getResources(ElementHandle.create((TypeElement) el), searchKind, searchScopeType));
                        } else {
                            //itererate implementors recursively
                            set.addAll(getImplementorsRecursive(idx, cpInfo, (TypeElement) el, cancel));
                        }
                    } else {
                        //get type references from index
                        set.addAll(idx.getResources(ElementHandle.create((TypeElement) el), EnumSet.of(ClassIndex.SearchKind.TYPE_REFERENCES, ClassIndex.SearchKind.IMPLEMENTORS), searchScopeType));
                    }
                } else if (el.getKind() == ElementKind.METHOD) {
                    ExecutableElement method = (ExecutableElement) el;
                    List<ExecutableElement> methods = new LinkedList<ExecutableElement>();
                    methods.add(method);
                    TypeElement enclosingTypeElement = info.getElementUtilities().enclosingTypeElement(method);
                    if(isSearchOverloadedMethods) {
                        for (Element overloaded : enclosingTypeElement.getEnclosedElements()) {
                            if(method != overloaded &&
                                    method.getKind() == overloaded.getKind() &&
                                    ((ExecutableElement)overloaded).getSimpleName().contentEquals(method.getSimpleName())) {
                                methods.add((ExecutableElement)overloaded);
                            }
                        }
                    }
                    if (isFindOverridingMethods) {
                        //Find overriding methods
                        set.addAll(getImplementorsRecursive(idx, cpInfo, enclosingTypeElement, cancel));
                    }
                    if (isFindUsages) {
                        //get method references for method and for all it's overriders
                        Set<ElementHandle<TypeElement>> s = RefactoringUtils.getImplementorsAsHandles(idx, cpInfo, (TypeElement) method.getEnclosingElement(), cancel);
                        for (ElementHandle<TypeElement> eh : s) {
                            if (cancel != null && cancel.get()) {
                                set.clear();
                                return;
                            }
                            TypeElement te = eh.resolve(info);
                            if (te == null) {
                                continue;
                            }
                            for (Element e : te.getEnclosedElements()) {
                                if (e.getKind() == ElementKind.METHOD || e.getKind() == ElementKind.CONSTRUCTOR) {
                                    for (ExecutableElement executableElement : methods) {
                                        if (info.getElements().overrides((ExecutableElement) e, executableElement, te)) {
                                            set.addAll(idx.getResources(ElementHandle.create(te), EnumSet.of(ClassIndex.SearchKind.METHOD_REFERENCES), searchScopeType));
                                        }
                                    }
                                }
                            }
                        }
                        set.addAll(idx.getResources(ElementHandle.create((TypeElement) el.getEnclosingElement()), EnumSet.of(ClassIndex.SearchKind.METHOD_REFERENCES), searchScopeType)); //?????
                    }
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
        // filter out files that are not on source path
        Set<FileObject> set2 = new HashSet<FileObject>(set.size());
        ClassPath cp = cpInfo.getClassPath(ClasspathInfo.PathKind.SOURCE);
        for (FileObject fo : set) {
            if (cp.contains(fo)) {
                set2.add(fo);
            }
            if (cancel != null && cancel.get()) {
                return Collections.<FileObject>emptySet();
            }
        }
        return set;
    }
    
    private static Collection<FileObject> getImplementorsRecursive(ClassIndex idx, ClasspathInfo cpInfo, TypeElement el, AtomicBoolean cancel) {
        Set<?> implementorsAsHandles = RefactoringUtils.getImplementorsAsHandles(idx, cpInfo, el, cancel);

        if(cancel != null && cancel.get()) {
            return Collections.<FileObject>emptySet();
        }
        @SuppressWarnings("unchecked")
        Collection<FileObject> set = SourceUtilsEx.getFiles((Collection<ElementHandle<? extends Element>>) implementorsAsHandles, cpInfo, cancel);

        // filter out files that are not on source path
        ClassPath source = cpInfo.getClassPath(ClasspathInfo.PathKind.SOURCE);
        Collection<FileObject> set2 = new ArrayList<FileObject>(set.size());
        for (FileObject fo : set) {
            if (source.contains(fo)) {
                set2.add(fo);
            }
            if(cancel != null && cancel.get()) {
                return Collections.<FileObject>emptySet();
            }
        }
        return set2;
    }
    
    //@Override
    @Override
    public Problem prepare(final RefactoringElementsBag elements) {
        fireProgressListenerStart(ProgressEvent.START, -1);
        Set<FileObject> a = getRelevantFiles(refactoring.getRefactoringSource().lookup(TreePathHandle.class));
        fireProgressListenerStep(a.size());
        Problem problem = null;
        try {
            final FindTask findTask = new FindTask(elements);
            queryFiles(a, findTask);
        } catch (IOException e) {
            problem = createProblemAndLog(null, e);
        }
        fireProgressListenerStop();
        return problem;
    }
    
    @Override
    public void cancelRequest() {
        super.cancelRequest();
        CancellableTask t = queryTask;
        if (t != null) {
            t.cancel();
        }
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
        } else {
            return null;
        }
    }
    
    public static CloneableEditorSupport findCloneableEditorSupport(DataObject dob) {
        Object obj = dob.getLookup().lookup(org.openide.cookies.OpenCookie.class);
        if (obj instanceof CloneableEditorSupport) {
            return (CloneableEditorSupport)obj;
        }
        obj = dob.getLookup().lookup(org.openide.cookies.EditorCookie.class);
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
    private boolean isSearchOverloadedMethods() {
        return refactoring.getBooleanValue(WhereUsedQueryConstants.SEARCH_OVERLOADED);
    }
    private boolean isSearchFromBaseClass() {
        return refactoring.getBooleanValue(WhereUsedQueryConstants.SEARCH_FROM_BASECLASS);
    }

    @Override
    public void addFilters(FiltersDescription filtersDescription) {
        filtersDescription.addFilter(JavaWhereUsedFilters.ReadWrite.READ.getKey(), "Read filter", true,
                ImageUtilities.loadImageIcon("org/netbeans/modules/refactoring/java/resources/found_item_read.png", false));
        filtersDescription.addFilter(JavaWhereUsedFilters.ReadWrite.WRITE.getKey(), "Write filter", true,
                ImageUtilities.loadImageIcon("org/netbeans/modules/refactoring/java/resources/found_item_write.png", false));
        filtersDescription.addFilter(JavaWhereUsedFilters.ReadWrite.READ_WRITE.getKey(), "Read/Write filter", true,
                ImageUtilities.loadImageIcon("org/netbeans/modules/refactoring/java/resources/found_item_readwrite.png", false));
        filtersDescription.addFilter(JavaWhereUsedFilters.IMPORT.getKey(), "Import filter", true,
                ImageUtilities.loadImageIcon("org/netbeans/modules/refactoring/java/resources/found_item_import.png", false));
        filtersDescription.addFilter(JavaWhereUsedFilters.COMMENT.getKey(), "Comment filter", true,
                ImageUtilities.loadImageIcon("org/netbeans/modules/refactoring/java/resources/found_item_comment.png", false));
        filtersDescription.addFilter(JavaWhereUsedFilters.TESTFILE.getKey(), "Test filter", true,
                ImageUtilities.loadImageIcon("org/netbeans/modules/refactoring/java/resources/found_item_test.png", false));
    }

    private EnumSet<JavaWhereUsedFilters.ReadWrite> usedAccessFilters = EnumSet.noneOf(JavaWhereUsedFilters.ReadWrite.class);
    private LinkedList<String> usedFilters = new LinkedList<String>();
    @Override
    public void enableFilters(FiltersDescription filtersDescription) {
        for (JavaWhereUsedFilters.ReadWrite filter : usedAccessFilters) {
            filtersDescription.enable(filter.getKey());
        }
        for (String string : usedFilters) {
            filtersDescription.enable(string);
        }
    }
    
    private class FindTask implements CancellableTask<CompilationController> {

        private RefactoringElementsBag elements;
        private volatile AtomicBoolean cancelled;

        public FindTask(RefactoringElementsBag elements) {
            super();
            this.elements = elements;
            this.cancelled = new AtomicBoolean(false);
        }

        @Override
        public void cancel() {
            cancelled.set(true);
        }

        @Override
        public void run(CompilationController compiler) throws IOException {
            if (cancelled.get()) {
                return ;
            }
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
            
            final boolean fromTestRoot = RefactoringUtils.isFromTestRoot(compiler.getFileObject(), compiler.getClasspathInfo().getClassPath(ClasspathInfo.PathKind.SOURCE));
            AtomicBoolean inImport = new AtomicBoolean();
            if (isFindUsages()) {
                FindUsagesVisitor findVisitor = new FindUsagesVisitor(compiler, cancelled, refactoring.getBooleanValue(WhereUsedQuery.SEARCH_IN_COMMENTS), isSearchOverloadedMethods(), fromTestRoot, inImport);
                findVisitor.scan(compiler.getCompilationUnit(), element);
                Collection<WhereUsedElement> foundElements = findVisitor.getElements();
                for (WhereUsedElement el : foundElements) {
                    final ReadWrite access = el.getAccess();
                    if(access != null) {
                        usedAccessFilters.add(access);
                    }
                    elements.add(refactoring, el);
                }
                if(fromTestRoot && !foundElements.isEmpty()) {
                    usedFilters.add(JavaWhereUsedFilters.TESTFILE.getKey());
                }
                if(!foundElements.isEmpty() && findVisitor.usagesInComments()) {
                    usedFilters.add(JavaWhereUsedFilters.COMMENT.getKey());
                }
            }
            Collection<TreePath> result = new ArrayList<TreePath>();
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
                elements.add(refactoring, WhereUsedElement.create(compiler, tree, fromTestRoot, inImport));
            }
            if(fromTestRoot && !result.isEmpty()) {
                usedFilters.add(JavaWhereUsedFilters.TESTFILE.getKey());
            }
            if(inImport.get()) {
                usedFilters.add(JavaWhereUsedFilters.IMPORT.getKey());
            }
            fireProgressListenerStep();
        }
    }
    
    private static class FileComparator implements Comparator<FileObject> {

        @Override
        public int compare(FileObject o1, FileObject o2) {
            return o1.getPath().compareTo(o2.getPath());
        }
    }

}
