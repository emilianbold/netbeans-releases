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
package org.netbeans.modules.refactoring.java.spi;

import java.util.Collections;
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
import org.netbeans.api.annotations.common.NonNull;
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
import org.netbeans.modules.refactoring.spi.Transaction;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
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
    

    /**
     * Create Java specific implementation of {@link Transaction}.
     * 
     * @param modifications collection of {@link ModificationResult}
     * @return Java specific Transaction
     * @see RefactoringElementsBag#registerTransaction(Transaction) 
     * @since 1.24.0
     * 
     * @author Jan Becicka
     */
    public static Transaction createTransaction(@NonNull Collection<ModificationResult> modifications) {
        return new RetoucheCommit(modifications);
    }

    
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
        cancelRequest = false;
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
        RetoucheUtils.cancel = true;
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
            String elName = el != null ? el.getSimpleName().toString() : null;
            if (el == null || el.asType().getKind() == TypeKind.ERROR || "<error>".equals(elName)) { // NOI18N
                return new Problem(true, NbBundle.getMessage(FindVisitor.class, "DSC_ElementNotResolved"));
            }
            
            if ("this".equals(elName) || "super".equals(elName)) { // NOI18N
                return new Problem(true, NbBundle.getMessage(FindVisitor.class, "ERR_CannotRefactorThis", el.getSimpleName()));
            }
            
            // element is still available
            return null;
        }
    }
    
    private Iterable<? extends List<FileObject>> groupByRoot (Iterable<? extends FileObject> data) {
        Map<FileObject,List<FileObject>> result = new HashMap<FileObject,List<FileObject>> ();
        for (FileObject file : data) {
            if (cancelRequest) {
                return Collections.emptyList();
            }
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
    
    protected final Collection<ModificationResult> processFiles(Set<FileObject> files, CancellableTask<WorkingCopy> task) throws IOException {
        return processFiles(files, task, null);
    }

    protected final Collection<ModificationResult> processFiles(Set<FileObject> files, CancellableTask<WorkingCopy> task, ClasspathInfo info) throws IOException {
        currentTask = task;
        Collection<ModificationResult> results = new LinkedList<ModificationResult>();
        try {
            Iterable<? extends List<FileObject>> work = groupByRoot(files);
            for (List<FileObject> fos : work) {
                if (cancelRequest) {
                    return Collections.<ModificationResult>emptyList();
                }
                final JavaSource javaSource = JavaSource.create(info==null?ClasspathInfo.create(fos.get(0)):info, fos);
                results.add(javaSource.runModificationTask(task)); // can throw IOException
            }
        } finally {
            currentTask = null;
        }
        return results;
    }
    
    protected final Problem createAndAddElements(Set<FileObject> files, CancellableTask<WorkingCopy> task, RefactoringElementsBag elements, AbstractRefactoring refactoring, ClasspathInfo info) {
        try {
            final Collection<ModificationResult> results = processFiles(files, task, info);
            elements.registerTransaction(createTransaction(results));
            for (ModificationResult result:results) {
                for (FileObject jfo : result.getModifiedFileObjects()) {
                    for (Difference dif: result.getDifferences(jfo)) {
                            elements.add(refactoring,DiffElement.create(dif, jfo, result));
                    }
                }
            }
        } catch (IOException e) {
            return createProblemAndLog(null, e);
        }
        return null;
    }

    protected final Problem createAndAddElements(Set<FileObject> files, CancellableTask<WorkingCopy> task, RefactoringElementsBag elements, AbstractRefactoring refactoring) {
        return createAndAddElements(files, task, elements, refactoring, null);
    }
    
    protected final Problem createProblemAndLog(Problem p, Throwable t) {
        Throwable cause = t.getCause();
        Problem newProblem;
        if (cause != null && (cause.getClass().getName().equals("org.netbeans.api.java.source.JavaSource$InsufficientMemoryException") ||  //NOI18N
            (cause.getCause()!=null ) && cause.getCause().getClass().getName().equals("org.netbeans.api.java.source.JavaSource$InsufficientMemoryException"))) { //NOI18N
            newProblem = new Problem(true, NbBundle.getMessage(JavaRefactoringPlugin.class, "ERR_OutOfMemory"));
        } else {
            String msg = NbBundle.getMessage(JavaRefactoringPlugin.class, "ERR_ExceptionThrown", t.toString());
            newProblem = new Problem(true, msg);
        }
        Exceptions.printStackTrace(t);
        
        Problem problem;
        if (p == null) return newProblem;
        problem = p;
        while(problem.getNext() != null) {
            problem = problem.getNext();
        }
        problem.setNext(newProblem);
        return p;
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
                try {
                    visitor.setWorkingCopy(compiler);
                } catch (ToPhaseException e) {
                    return;
                }
                CompilationUnitTree cu = compiler.getCompilationUnit();
                if (cu == null) {
                    ErrorManager.getDefault().log(ErrorManager.ERROR, "compiler.getCompilationUnit() is null " + compiler); // NOI18N
                    return;
                }
                Element el = null;
                if (treePathHandle != null) {
                    el = treePathHandle.resolveElement(compiler);
                    if (el == null) {
                        ErrorManager.getDefault().log(ErrorManager.WARNING, "Cannot resolve " + treePathHandle + "in " + compiler.getFileObject().getPath());
                        return;
                    }
                }

                visitor.scan(compiler.getCompilationUnit(), el);
            } finally {
                fireProgressListenerStep();
            }
        }
    }
}
