/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.refactoring.java.plugins;

import org.netbeans.modules.refactoring.java.spi.JavaRefactoringPlugin;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.*;
import java.util.HashSet;
import javax.lang.model.element.*;
import org.netbeans.api.java.source.*;
import org.netbeans.modules.refactoring.api.WhereUsedQuery;
import org.netbeans.modules.refactoring.java.WhereUsedElement;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.ProgressEvent;
import org.netbeans.modules.refactoring.api.WhereUsedQuery;
import org.netbeans.modules.refactoring.java.api.WhereUsedQueryConstants;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.NbBundle;

/**
 *
 * @author  Jan Becicka
 */
public class JavaWhereUsedQueryPlugin extends JavaRefactoringPlugin {
    private WhereUsedQuery refactoring;
    
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
        if (!refactoring.getRefactoringSource().lookup(TreePathHandle.class).getFileObject().isValid()) {
            return new Problem(true, NbBundle.getMessage(FindVisitor.class, "DSC_ElNotAvail")); // NOI18N
        }
        return null;
    }
    
    private Set<FileObject> getRelevantFiles(final TreePathHandle tph) {
        final ClasspathInfo cpInfo = getClasspathInfo(refactoring);
        final ClassIndex idx = cpInfo.getClassIndex();
        final Set<FileObject> set = new HashSet<FileObject>();
                
        final FileObject file = tph.getFileObject();
        JavaSource source;
        if (file!=null) {
           set.add(file);
            source = JavaSource.create(cpInfo, new FileObject[]{tph.getFileObject()});
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
                if (el.getKind().isField()) {
                    //get field references from index
                    set.addAll(idx.getResources(ElementHandle.create((TypeElement)el.getEnclosingElement()), EnumSet.of(ClassIndex.SearchKind.FIELD_REFERENCES), EnumSet.of(ClassIndex.SearchScope.SOURCE)));
                } else if (el.getKind().isClass() || el.getKind().isInterface()) {
                    if (isFindSubclasses()||isFindDirectSubclassesOnly()) {
                        if (isFindDirectSubclassesOnly()) {
                            //get direct implementors from index
                            EnumSet searchKind = EnumSet.of(ClassIndex.SearchKind.IMPLEMENTORS);
                            set.addAll(idx.getResources(ElementHandle.create((TypeElement)el), searchKind,EnumSet.of(ClassIndex.SearchScope.SOURCE)));
                        } else {
                            //itererate implementors recursively
                            set.addAll(getImplementorsRecursive(idx, cpInfo, (TypeElement)el));
                        }
                    } else {
                        //get type references from index
                        set.addAll(idx.getResources(ElementHandle.create((TypeElement) el), EnumSet.of(ClassIndex.SearchKind.TYPE_REFERENCES, ClassIndex.SearchKind.IMPLEMENTORS),EnumSet.of(ClassIndex.SearchScope.SOURCE)));
                    }
                } else if (el.getKind() == ElementKind.METHOD && isFindOverridingMethods()) {
                    //Find overriding methods
                    TypeElement type = (TypeElement) el.getEnclosingElement();
                    set.addAll(getImplementorsRecursive(idx, cpInfo, type));
                } 
                if (el.getKind() == ElementKind.METHOD && isFindUsages()) {
                    //get method references for method and for all it's overriders
                    Set<ElementHandle<TypeElement>> s = idx.getElements(ElementHandle.create((TypeElement) el.getEnclosingElement()), EnumSet.of(ClassIndex.SearchKind.IMPLEMENTORS),EnumSet.of(ClassIndex.SearchScope.SOURCE));
                    for (ElementHandle<TypeElement> eh:s) {
                        TypeElement te = eh.resolve(info);
                        if (te==null) {
                            continue;
                        }
                        for (Element e:te.getEnclosedElements()) {
                            if (e instanceof ExecutableElement) {
                                if (info.getElements().overrides((ExecutableElement)e, (ExecutableElement)el, te)) {
                                    set.addAll(idx.getResources(ElementHandle.create(te), EnumSet.of(ClassIndex.SearchKind.METHOD_REFERENCES),EnumSet.of(ClassIndex.SearchScope.SOURCE)));
                                }
                            }
                        }
                    }
                    set.addAll(idx.getResources(ElementHandle.create((TypeElement) el.getEnclosingElement()), EnumSet.of(ClassIndex.SearchKind.METHOD_REFERENCES),EnumSet.of(ClassIndex.SearchScope.SOURCE))); //?????
                } else if (el.getKind() == ElementKind.CONSTRUCTOR) {
                        set.addAll(idx.getResources(ElementHandle.create((TypeElement) el.getEnclosingElement()), EnumSet.of(ClassIndex.SearchKind.TYPE_REFERENCES, ClassIndex.SearchKind.IMPLEMENTORS),EnumSet.of(ClassIndex.SearchScope.SOURCE)));
                }
                    
            }
        };
        try {
            source.runUserActionTask(task, true);
        } catch (IOException ioe) {
            throw (RuntimeException) new RuntimeException().initCause(ioe);
        }
        return set;
    }
    
    private Set<FileObject> getImplementorsRecursive(ClassIndex idx, ClasspathInfo cpInfo, TypeElement el) {
        Set<FileObject> set = new HashSet<FileObject>();
        LinkedList<ElementHandle<TypeElement>> elements = new LinkedList<ElementHandle<TypeElement>>(idx.getElements(ElementHandle.create(el),
                EnumSet.of(ClassIndex.SearchKind.IMPLEMENTORS),
                EnumSet.of(ClassIndex.SearchScope.SOURCE)));
        HashSet<ElementHandle<TypeElement>> result = new HashSet<ElementHandle<TypeElement>>();
        while(!elements.isEmpty()) {
            ElementHandle<TypeElement> next = elements.removeFirst();
            result.add(next);
            elements.addAll(idx.getElements(next,
                    EnumSet.of(ClassIndex.SearchKind.IMPLEMENTORS),
                    EnumSet.of(ClassIndex.SearchScope.SOURCE)));
        }
        for (ElementHandle<TypeElement> e : result) {
            FileObject fo = SourceUtils.getFile(e, cpInfo);
            assert fo != null: "issue 90196, Cannot find file for " + e + ". cpInfo=" + cpInfo ;
            set.add(fo);
        }
        return set;
    }
    
    //@Override
    public Problem prepare(final RefactoringElementsBag elements) {
        Set<FileObject> a = getRelevantFiles(refactoring.getRefactoringSource().lookup(TreePathHandle.class));
        fireProgressListenerStart(ProgressEvent.START, a.size());
        processFiles(a, new FindTask(elements));
        fireProgressListenerStop();
        return null;
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
        return false;
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
                ErrorManager.getDefault().log(ErrorManager.ERROR, "compiler.getCompilationUnit() is null " + compiler);
                return;
            }
            Element element = refactoring.getRefactoringSource().lookup(TreePathHandle.class).resolveElement(compiler);
            assert element != null;
            Collection<TreePath> result = new ArrayList<TreePath>();
            if (isFindUsages()) {
                FindUsagesVisitor findVisitor = new FindUsagesVisitor(compiler);
                findVisitor.scan(compiler.getCompilationUnit(), element);
                result.addAll(findVisitor.getUsages());
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
