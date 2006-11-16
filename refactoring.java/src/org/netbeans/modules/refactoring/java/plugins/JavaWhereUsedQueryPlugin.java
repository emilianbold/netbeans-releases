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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.refactoring.java.plugins;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.*;
import javax.lang.model.element.*;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.*;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.modules.refactoring.java.RetoucheUtils;
import org.netbeans.modules.refactoring.java.WhereUsedElement;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.ProgressEvent;
import org.netbeans.modules.refactoring.api.WhereUsedQuery;
import org.netbeans.modules.refactoring.java.classpath.RefactoringClassPathImplementation;
import org.netbeans.modules.refactoring.java.plugins.FindOverridingVisitor;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.java.api.JavaWhereUsedQuery;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author  Jan Becicka
 */
public class JavaWhereUsedQueryPlugin extends JavaRefactoringPlugin {
    private TreePathHandle searchHandle;
    private boolean baseClass;
    private boolean overriders;
    private boolean subclasses;
    private boolean directOnly;
    private boolean findUsages=true;
    private JavaWhereUsedQuery refactoring;
    private ClasspathInfo classPathInfo=null;
    
    
    /** Creates a new instance of WhereUsedQuery */
    public JavaWhereUsedQueryPlugin(JavaWhereUsedQuery refactoring) {
        this.refactoring = refactoring;
        this.searchHandle = refactoring.getRefactoredObject();
    }
    
    private ClasspathInfo getClasspathInfo(ClasspathInfo info) {
        if (classPathInfo == null) {
            ClassPath boot = info.getClassPath(ClasspathInfo.PathKind.BOOT);
            FileObject fo = searchHandle.getFileObject();
            ClassPath rcp = RefactoringClassPathImplementation.getCustom(Collections.singleton(fo));
            classPathInfo = ClasspathInfo.create(boot, rcp, rcp);
        }
        return classPathInfo; 
    }
    
    public Problem preCheck() {
        Problem p = isElementAvail(searchHandle, refactoring.getContext().lookup(CompilationInfo.class));
        if (p != null)
            return p;
        
//        if (!((jmiObject instanceof Feature) || (jmiObject instanceof Variable) || (jmiObject instanceof JavaPackage) || (jmiObject instanceof TypeParameter)) ) {
//            return new Problem(true, NbBundle.getMessage(WhereUsedQuery.class, "ERR_WhereUsedWrongType"));
//        }
        
        return p;
    }
    
    private Set<FileObject> getRelevantFiles(final TreePathHandle tph) {
        final ClasspathInfo cpInfo = refactoring.getContext().lookup(ClasspathInfo.class);
        final ClassIndex idx = cpInfo.getClassIndex();
        final Set<FileObject> set = new HashSet<FileObject>();
                
        JavaSource source = JavaSource.create(cpInfo, new FileObject[]{tph.getFileObject()});
        //XXX: This is slow!
        CancellableTask<CompilationController> task = new CancellableTask<CompilationController>() {
            public void cancel() {
            }
            
            public void run(CompilationController info) throws Exception {
                info.toPhase(Phase.RESOLVED);
                set.add(tph.getFileObject());
                final Element el = tph.resolveElement(info);
                if (el.getKind().isField()) {
                    //get field references from index
                    set.addAll(idx.getResources(ElementHandle.create((TypeElement)el.getEnclosingElement()), EnumSet.of(ClassIndex.SearchKind.FIELD_REFERENCES), EnumSet.of(ClassIndex.SearchScope.SOURCE)));
                } else if (el.getKind().isClass() || el.getKind().isInterface()) {
                    if (refactoring.isFindSubclasses()||refactoring.isFindDirectSubclassesOnly()) {
                        if (refactoring.isFindDirectSubclassesOnly()) {
                            //get direct implementors from index
                            EnumSet searchKind = EnumSet.of(ClassIndex.SearchKind.IMPLEMENTORS);
                            set.addAll(idx.getResources(ElementHandle.create((TypeElement)el), searchKind,EnumSet.of(ClassIndex.SearchScope.SOURCE)));
                        } else {
                            //itererate implementors recursively
                            LinkedList<ElementHandle<TypeElement>> elements = new LinkedList(idx.getElements(ElementHandle.create((TypeElement) el),
                                    EnumSet.of(ClassIndex.SearchKind.IMPLEMENTORS),
                                    EnumSet.of(ClassIndex.SearchScope.SOURCE)));
                            HashSet<ElementHandle> result = new HashSet();
                            while(!elements.isEmpty()) {
                                ElementHandle<TypeElement> next = elements.removeFirst();
                                result.add(next);
                                elements.addAll(idx.getElements(next,
                                        EnumSet.of(ClassIndex.SearchKind.IMPLEMENTORS),
                                        EnumSet.of(ClassIndex.SearchScope.SOURCE)));
                            }
                            for (ElementHandle<TypeElement> e : result) {
                                set.add(SourceUtils.getFile(e.resolve(info),
                                        cpInfo));
                            }
                        }
                    } else {
                        //get type references from index
                        set.addAll(idx.getResources(ElementHandle.create((TypeElement) el), EnumSet.of(ClassIndex.SearchKind.TYPE_REFERENCES),EnumSet.of(ClassIndex.SearchScope.SOURCE)));
                    }
                } else if (el.getKind() == ElementKind.METHOD && refactoring.isFindOverridingMethods()) {
                    //Find overriding methods
                    TypeElement type = (TypeElement) el.getEnclosingElement();
                    set.addAll(idx.getResources(ElementHandle.create((TypeElement)el.getEnclosingElement()), EnumSet.of(ClassIndex.SearchKind.IMPLEMENTORS),EnumSet.of(ClassIndex.SearchScope.SOURCE)));
                } 
                if (el.getKind() == ElementKind.METHOD && refactoring.isFindUsages()) {
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
                        set.addAll(idx.getResources(ElementHandle.create((TypeElement) el.getEnclosingElement()), EnumSet.of(ClassIndex.SearchKind.TYPE_REFERENCES),EnumSet.of(ClassIndex.SearchScope.SOURCE)));
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
    
    //@Override
    public Problem prepare(final RefactoringElementsBag elements) {
        ClasspathInfo cpInfo = refactoring.getContext().lookup(ClasspathInfo.class);
        
        refactoring.getContext().add(getClasspathInfo(cpInfo));
        
        Set<FileObject> a = getRelevantFiles(searchHandle);
        fireProgressListenerStart(ProgressEvent.START, a.size());
        processFiles(a, new FindTask(elements));
        fireProgressListenerStop();
        return null;
    }
    
    //@Override
    public Problem fastCheckParameters() {
        if (searchHandle.getKind() == Tree.Kind.METHOD) {
            return checkParametersForMethod(refactoring.isSearchFromBaseClass(), refactoring.isFindOverridingMethods(), refactoring.isFindUsages());
        } else if (searchHandle.getKind() == Tree.Kind.CLASS) {
            return checkParametersForClass(refactoring.isFindSubclasses(), refactoring.isFindDirectSubclassesOnly());
        }
        return null;
    }
    
    //@Override
    public Problem checkParameters() {
        if (searchHandle.getKind() == Tree.Kind.METHOD) {
            return setParametersForMethod(refactoring.isSearchFromBaseClass(), refactoring.isFindOverridingMethods(), refactoring.isFindUsages());
        } else if (searchHandle.getKind() == Tree.Kind.CLASS) {
            return setParametersForClass(refactoring.isFindSubclasses(), refactoring.isFindDirectSubclassesOnly());
        }
        return null;
    }
    
    private Problem checkParametersForMethod(boolean baseClass, boolean overriders, boolean usages) {
        if (!(usages || overriders)) {
            return new Problem(true, NbBundle.getMessage(WhereUsedQuery.class, "MSG_NothingToFind"));
        } else
            return null;
    }
    
    private Problem setParametersForMethod(boolean baseClass, boolean overriders, boolean usages) {
        this.baseClass = baseClass;
        this.overriders = overriders;
        this.findUsages = usages;
        if (baseClass) {
            final ClasspathInfo info = refactoring.getContext().lookup(ClasspathInfo.class);
            JavaSource source = JavaSource.create(info, new FileObject[]{searchHandle.getFileObject()});
            CancellableTask<CompilationController> task = new CancellableTask<CompilationController>() {
                public void cancel() {
                }
                public void run(CompilationController parameter) throws Exception {
                    parameter.toPhase(Phase.ELEMENTS_RESOLVED);
                    final Collection<ExecutableElement>  c = RetoucheUtils.getOverridenMethods((ExecutableElement)searchHandle.resolveElement(parameter),parameter);
                    if (!c.isEmpty()) {
                        final ElementHandle eh = ElementHandle.create(c.iterator().next());
                        TreePath tp = SourceUtils.pathFor(parameter, eh.resolve(parameter));
                        searchHandle = TreePathHandle.create(tp, parameter);
                    }
                    
                }
            };
            try {
                source.runUserActionTask(task, true);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return null;
    }
    
    private Problem checkParametersForClass(boolean subclasses, boolean transitively) {
        return null;
    }
    
    private Problem setParametersForClass(boolean subclasses, boolean directOnly) {
        this.subclasses = subclasses;
        this.directOnly = directOnly;
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
    
    
    //    public void start(ProgressEvent event) {
    //        fireProgressListenerStart(event.getOperationType(), event.getCount());
    //    }
    //
    //    public void step(ProgressEvent event) {
    //        fireProgressListenerStep();
    //    }
    //
    //    public void stop(ProgressEvent event) {
    //        fireProgressListenerStop();
    //    }

    private class FindTask implements CancellableTask<WorkingCopy> {

        private RefactoringElementsBag elements;

        public FindTask(RefactoringElementsBag elements) {
            super();
            this.elements = elements;
        }

        public void cancel() {
        }

        public void run(WorkingCopy compiler) throws IOException {
            compiler.toPhase(Phase.RESOLVED);
            CompilationUnitTree cu = compiler.getCompilationUnit();
            if (cu == null) {
                ErrorManager.getDefault().log(ErrorManager.ERROR, "compiler.getCompilationUnit() is null " + compiler);
                return;
            }
            Element element = searchHandle.resolveElement(compiler);
            assert element != null;
            Collection<TreePath> result = new ArrayList();
            if (refactoring.isFindUsages()) {
                FindUsagesVisitor findVisitor = new FindUsagesVisitor(compiler);
                findVisitor.scan(compiler.getCompilationUnit(), element);
                result.addAll(findVisitor.getUsages());
            }
            if (element.getKind() == ElementKind.METHOD && refactoring.isFindOverridingMethods()) {
                FindOverridingVisitor override = new FindOverridingVisitor(compiler);
                override.scan(compiler.getCompilationUnit(), element);
                result.addAll(override.getUsages());
            } else if ((element.getKind().isClass() || element.getKind().isInterface()) &&
                    (refactoring.isFindSubclasses()||refactoring.isFindDirectSubclassesOnly())) {
                FindSubtypesVisitor subtypes = new FindSubtypesVisitor(!refactoring.isFindDirectSubclassesOnly(), compiler);
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
