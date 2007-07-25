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
package org.netbeans.modules.refactoring.java.spi;

import org.netbeans.api.java.source.ModificationResult.Difference;
import com.sun.source.tree.CompilationUnitTree;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.type.TypeKind;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.java.RetoucheUtils;
import org.netbeans.modules.refactoring.java.plugins.FindVisitor;
import org.netbeans.modules.refactoring.java.plugins.RetoucheCommit;
import org.netbeans.modules.refactoring.spi.ProgressProviderAdapter;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Becicka
 */
public abstract class JavaRefactoringPlugin extends ProgressProviderAdapter implements RefactoringPlugin {

    protected enum Phase {PRECHECK, FASTCHECKPARAMETERS, CHECKPARAMETERS, PREPARE};
    protected volatile boolean cancelRequest = false;
    private volatile CancellableTask currentTask;
    private WorkingTask workingTask = new WorkingTask();
    

    protected Problem preCheck(CompilationController javac) throws IOException {
        return null;
    }
    protected Problem checkParameters(CompilationController javac) throws IOException {
        return null;
    }
    protected Problem fastCheckParameters(CompilationController javac) throws IOException {
        return null;
    }
//    protected abstract Problem prepare(WorkingCopy wc, RefactoringElementsBag bag) throws IOException;

    protected abstract JavaSource getJavaSource(Phase p);

    public Problem preCheck() {
        return workingTask.run(Phase.PRECHECK);
    }

    public Problem checkParameters() {
        return workingTask.run(Phase.CHECKPARAMETERS);
    }

    public Problem fastCheckParameters() {
        return workingTask.run(Phase.FASTCHECKPARAMETERS);
    }

//    public Problem prepare(final RefactoringElementsBag bag) {
//        this.whatRun = Switch.PREPARE;
//        this.problem = null;
//        FileObject fo = getFileObject();
//        JavaSource js = JavaSource.forFileObject(fo);
//        try {
//            js.runModificationTask(new CancellableTask<WorkingCopy>() {
//                public void cancel() {
//                }
//
//                public void run(WorkingCopy wc) throws Exception {
//                    prepare(wc, bag);
//                }
//            }).commit();
//        } catch (IOException ex) {
//            throw new RuntimeException(ex);
//        }
//        return problem;
//    }
    
    public void cancelRequest() {
        cancelRequest = true;
        if (currentTask!=null) {
            currentTask.cancel();
        }
    }

    protected ClasspathInfo getClasspathInfo(AbstractRefactoring refactoring) {
        ClasspathInfo cpInfo = refactoring.getContext().lookup(ClasspathInfo.class);
        if (cpInfo==null) {
            Collection<? extends TreePathHandle> handles = refactoring.getRefactoringSource().lookupAll(TreePathHandle.class);
            if (!handles.isEmpty()) {
                cpInfo = RetoucheUtils.getClasspathInfoFor(handles.toArray(new TreePathHandle[handles.size()]));
            } else {
                cpInfo = RetoucheUtils.getClasspathInfoFor((FileObject)null);
            }
            refactoring.getContext().add(cpInfo);
        }
        return cpInfo;
    }
    
    protected static final Problem createProblem(Problem result, boolean isFatal, String message) {
        Problem problem = new Problem(isFatal, message);
        if (result == null) {
            return problem;
        } else if (isFatal) {
            problem.setNext(result);
            return problem;
        } else {
            //problem.setNext(result.getNext());
            //result.setNext(problem);
            
            // [TODO] performance
            Problem p = result;
            while (p.getNext() != null)
                p = p.getNext();
            p.setNext(problem);
            return result;
        }
    }

    /**
     * Checks if the element is still available. Tests if it is still valid.
     * (Was not deleted by matching mechanism.)
     * If element is available, returns null, otherwise it creates problem.
     * (Helper method for refactoring implementation as this problem is
     * general for all refactorings.)
     *
     * @param   e  element to check
     * @param info 
     * @return  problem message or null if the element is valid
     */
    protected static Problem isElementAvail(TreePathHandle e, CompilationInfo info) {
        if (e==null) {
            //element is null or is not valid.
            return new Problem(true, NbBundle.getMessage(FindVisitor.class, "DSC_ElNotAvail")); // NOI18N
        } else {
            Element el = e.resolveElement(info);
            if (el == null || el.asType().getKind() == TypeKind.ERROR) {
                return new Problem(true, NbBundle.getMessage(FindVisitor.class, "DSC_ElementNotResolved"));
            }
            
            if ("this".equals(el.getSimpleName().toString()) || "super".equals(el.getSimpleName().toString())) {
                return new Problem(true, NbBundle.getMessage(FindVisitor.class, "ERR_CannotRefactorThis", el.getSimpleName()));
            }
            
            // element is still available
            return null;
        }
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
    
    protected final Collection<ModificationResult> processFiles(Set<FileObject> files, CancellableTask<WorkingCopy> task) {
        currentTask = task;
        Collection<ModificationResult> results = new LinkedList<ModificationResult>();
        try {
            Iterable<? extends List<FileObject>> work = groupByRoot(files);
            for (List<FileObject> fos : work) {
                final JavaSource javaSource = JavaSource.create(ClasspathInfo.create(fos.get(0)), fos);
                try {
                    results.add(javaSource.runModificationTask(task));
                } catch (IOException ex) {
                    throw (RuntimeException) new RuntimeException().initCause(ex);
                }
            }
        } finally {
            currentTask = null;
        }
        return results;
    }
    
    protected final void createAndAddElements(Set<FileObject> files, CancellableTask<WorkingCopy> task, RefactoringElementsBag elements, AbstractRefactoring refactoring) {
        final Collection<ModificationResult> results = processFiles(files, task);
        elements.registerTransaction(new RetoucheCommit(results));
        for (ModificationResult result:results) {
            for (FileObject jfo : result.getModifiedFileObjects()) {
                for (Difference dif: result.getDifferences(jfo)) {
                        elements.add(refactoring,DiffElement.create(dif, jfo, result));
                }
            }
        }
    }
    
    private class WorkingTask implements Task<CompilationController> {
        
        private Phase whatRun;
        private Problem problem;

        private Problem run(Phase s) {
            this.whatRun = s;
            this.problem = null;
            JavaSource js = getJavaSource(s);
            if (js==null) {
                return null;
            }
            try {
                js.runUserActionTask(this, true);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            return problem;
        }

        public void run(CompilationController javac) throws Exception {
            switch(whatRun) {
            case PRECHECK:
                this.problem = preCheck(javac);
                break;
            case CHECKPARAMETERS:
                this.problem = checkParameters(javac);
                break;
            case FASTCHECKPARAMETERS:
                this.problem = fastCheckParameters(javac);
                break;
            default:
                throw new IllegalStateException();
            }
        }
        
    }
    
    protected class TransformTask implements CancellableTask<WorkingCopy> {
        
        private RefactoringVisitor visitor;
        private TreePathHandle treePathHandle;
        public TransformTask(RefactoringVisitor visitor, TreePathHandle searchedItem) {
            this.visitor = visitor;
            this.treePathHandle = searchedItem;
        }
        
        public void cancel() {
        }
        
        public void run(WorkingCopy compiler) throws IOException {
            try {
                visitor.setWorkingCopy(compiler);
            } catch (ToPhaseException e) {
                return;
            }
            CompilationUnitTree cu = compiler.getCompilationUnit();
            if (cu == null) {
                ErrorManager.getDefault().log(ErrorManager.ERROR, "compiler.getCompilationUnit() is null " + compiler);
                return;
            }
            Element el = null;
            if (treePathHandle!=null) {
                el = treePathHandle.resolveElement(compiler);
                assert el != null;
            }
            
            visitor.scan(compiler.getCompilationUnit(), el);
            
            fireProgressListenerStep();
        }
    }
}
