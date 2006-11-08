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
    private TreePathHandle jmiObject;
    private boolean baseClass;
    private boolean overriders;
    private boolean subclasses;
    private boolean directOnly;
    private boolean findUsages=true;
    private JavaWhereUsedQuery refactoring;
    
    
    /** Creates a new instance of WhereUsedQuery */
    public JavaWhereUsedQueryPlugin(JavaWhereUsedQuery refactoring) {
        this.refactoring = refactoring;
        this.jmiObject = refactoring.getRefactoredObject();
    }
    
    private ClasspathInfo getClasspathInfo(CompilationInfo info) {
        ClassPath boot = info.getClasspathInfo().getClassPath(ClasspathInfo.PathKind.BOOT);
        FileObject fo = jmiObject.getFileObject();
        ClassPath rcp = RefactoringClassPathImplementation.getCustom(Collections.singleton(fo));
        ClasspathInfo cpi = ClasspathInfo.create(boot, rcp, rcp);
        return cpi;
    }
    
    public Problem preCheck() {
        Problem p = isElementAvail(jmiObject, refactoring.getContext().lookup(CompilationInfo.class));
        if (p != null)
            return p;
        
//        if (!((jmiObject instanceof Feature) || (jmiObject instanceof Variable) || (jmiObject instanceof JavaPackage) || (jmiObject instanceof TypeParameter)) ) {
//            return new Problem(true, NbBundle.getMessage(WhereUsedQuery.class, "ERR_WhereUsedWrongType"));
//        }
        
        return p;
    }
    
    private Set<FileObject> getRelevantFiles(CompilationInfo info, TreePathHandle tph) {
        final ClasspathInfo cpInfo = refactoring.getContext().lookup(ClasspathInfo.class);
        final ClassIndex idx = cpInfo.getClassIndex();
        final Set<FileObject> set = new HashSet<FileObject>();
                
        set.add(tph.getFileObject());
        final Element el = tph.resolveElement(info);
        if (el.getKind().isField()) {
            set.addAll(idx.getResources(ElementHandle.create((TypeElement)el.getEnclosingElement()), EnumSet.of(ClassIndex.SearchKind.FIELD_REFERENCES), EnumSet.of(ClassIndex.SearchScope.SOURCE)));
        } else if (el.getKind().isClass() || el.getKind().isInterface()) {
            if (refactoring.isFindSubclasses()||refactoring.isFindDirectSubclassesOnly()) {
                //XXX: IMPLEMENTORS_RECURSIVE was removed
                if (refactoring.isFindDirectSubclassesOnly()) {
                    EnumSet searchKind = EnumSet.of(ClassIndex.SearchKind.IMPLEMENTORS);
                    set.addAll(idx.getResources(ElementHandle.create((TypeElement)el), searchKind,EnumSet.of(ClassIndex.SearchScope.SOURCE)));
                } else {
                    try {
                        JavaSource source = JavaSource.create(cpInfo, new FileObject[0]);
                        source.runUserActionTask(new CancellableTask<CompilationController>() {
                            public void cancel() {
                            }
                            
                            public void run(CompilationController parameter) throws Exception {
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
                                    set.add(SourceUtils.getFile(e.resolve(parameter),
                                            cpInfo));
                                }
                            }
                        }, true);
                    } catch (IOException ex) {
                        java.util.logging.Logger.getLogger("global").log(java.util.logging.Level.SEVERE,ex.getMessage(),ex);
                    };
                }
            } else {
                set.addAll(idx.getResources(ElementHandle.create((TypeElement) el), EnumSet.of(ClassIndex.SearchKind.TYPE_REFERENCES, ClassIndex.SearchKind.IMPLEMENTORS),EnumSet.of(ClassIndex.SearchScope.SOURCE)));
            }
        } else if (el.getKind() == ElementKind.METHOD && refactoring.isFindOverridingMethods()) {
            TypeElement type = (TypeElement) el.getEnclosingElement();
            //XXX: IMPLEMENTORS_RECURSIVE was removed
            set.addAll(idx.getResources(ElementHandle.create((TypeElement)el.getEnclosingElement()), EnumSet.of(ClassIndex.SearchKind.IMPLEMENTORS),EnumSet.of(ClassIndex.SearchScope.SOURCE)));
        } else if (el.getKind() == ElementKind.METHOD && refactoring.isFindUsages()) {
            //XXX: IMPLEMENTORS_RECURSIVE was removed
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
        }
        return set;
    }
    
    //@Override
    public Problem prepare(final RefactoringElementsBag elements) {
        ClasspathInfo cpInfo = refactoring.getContext().lookup(ClasspathInfo.class);
        CompilationInfo mainInfo = refactoring.getContext().lookup(CompilationInfo.class);
        Element element = jmiObject.resolveElement(mainInfo);
        
        if (cpInfo==null) {
            cpInfo = getClasspathInfo(mainInfo);
            refactoring.getContext().add(cpInfo);
        }
        
        Set<FileObject> a = getRelevantFiles(mainInfo, jmiObject);
        fireProgressListenerStart(ProgressEvent.START, a.size());
        Iterable<? extends List<FileObject>> work = groupByRoot (a);        
        for (List<FileObject> fos : work) {
            final JavaSource javaSource = JavaSource.create(ClasspathInfo.create(fos.get(0)), fos);
            try {
                javaSource.runModificationTask(new FindTask(elements, element));
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }                                   
        fireProgressListenerStop();
        return null;
    }
    
    private Iterable<? extends List<FileObject>> groupByRoot (Iterable<? extends FileObject> data) {
        Map<FileObject,List<FileObject>> result = new HashMap<FileObject,List<FileObject>> ();
        for (FileObject file : data) {
            ClassPath cp = ClassPath.getClassPath(file, ClassPath.SOURCE);
            if (cp != null) {
                FileObject root = cp.findOwnerRoot(file);
                if (root != null) {
                    List<FileObject> subr = result.get (root);
                    if (subr == null) {
                        subr = new LinkedList<FileObject>();
                        result.put (root,subr);
                    }
                    subr.add (file);
                }
            }
        }
        return result.values();
    }
    
    //@Override
    public Problem fastCheckParameters() {
        CompilationInfo info = refactoring.getContext().lookup(CompilationInfo.class);
        Element element = jmiObject.resolveElement(info);
        if (element.getKind() == ElementKind.METHOD) {
            return checkParametersForMethod(refactoring.isSearchFromBaseClass(), refactoring.isFindOverridingMethods(), refactoring.isFindUsages());
        } else if (element instanceof TypeElement) {
            return checkParametersForClass(refactoring.isFindSubclasses(), refactoring.isFindDirectSubclassesOnly());
        }
        return null;
    }
    
    //@Override
    public Problem checkParameters() {
        CompilationInfo info = refactoring.getContext().lookup(CompilationInfo.class);
        Element element = jmiObject.resolveElement(info);
        if (element.getKind() == ElementKind.METHOD) {
            return setParametersForMethod(refactoring.isSearchFromBaseClass(), refactoring.isFindOverridingMethods(), refactoring.isFindUsages());
        } else if (element instanceof TypeElement) {
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
        CompilationInfo info = refactoring.getContext().lookup(CompilationInfo.class);
        if (baseClass) {
            //TODO: this is strange
            final Collection<ExecutableElement>  c = RetoucheUtils.getOverridenMethods((ExecutableElement)jmiObject.resolveElement(info),info);
            if (!c.isEmpty()) {
                JavaSource source = JavaSource.forFileObject(SourceUtils.getFile(c.iterator().next(), info.getClasspathInfo()));
                final ElementHandle eh = ElementHandle.create(c.iterator().next());
                try {
                    source.runUserActionTask(new CancellableTask<CompilationController>() {
                        public void cancel() {
                        }
                        public void run(CompilationController parameter) throws Exception {
                            parameter.toPhase(Phase.ELEMENTS_RESOLVED);
                            TreePath tp = parameter.getTrees().getPath(eh.resolve(parameter));
                            jmiObject = TreePathHandle.create(tp, parameter);
                        }
                    },true);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
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
        private Element element;

        public FindTask(RefactoringElementsBag elements, Element element) {
            super();
            this.elements = elements;
            this.element = element;
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
            Element el = jmiObject.resolveElement(compiler);
            assert el != null;
            Collection<TreePath> result = new ArrayList();
            if (refactoring.isFindUsages()) {
                FindUsagesVisitor findVisitor = new FindUsagesVisitor(compiler);
                findVisitor.scan(compiler.getCompilationUnit(), el);
                result.addAll(findVisitor.getUsages());
            }
            if (element.getKind() == ElementKind.METHOD && refactoring.isFindOverridingMethods()) {
                FindOverridingVisitor override = new FindOverridingVisitor(compiler);
                override.scan(compiler.getCompilationUnit(), el);
                result.addAll(override.getUsages());
            } else if ((element.getKind().isClass() || element.getKind().isInterface()) &&
                    (refactoring.isFindSubclasses()||refactoring.isFindDirectSubclassesOnly())) {
                FindSubtypesVisitor subtypes = new FindSubtypesVisitor(!refactoring.isFindDirectSubclassesOnly(), compiler);
                subtypes.scan(compiler.getCompilationUnit(), el);
                result.addAll(subtypes.getUsages());
            }
            for (TreePath tree : result) {
                elements.add(refactoring, WhereUsedElement.create(compiler, tree));
            }
            fireProgressListenerStep();
        }
    }

}
