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
package org.netbeans.modules.cnd.refactoring.plugins;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.refactoring.spi.*;
import org.netbeans.modules.refactoring.api.*;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Jan Becicka
 * @author Vladimir Voskresensky
 */
public abstract class CsmRefactoringPlugin extends ProgressProviderAdapter implements RefactoringPlugin {

    protected enum Phase {PRECHECK, FASTCHECKPARAMETERS, CHECKPARAMETERS, PREPARE, DEFAULT};
    private Phase whatRun = Phase.DEFAULT;
    private Problem problem;
    protected volatile boolean cancelRequest = false;
//    private volatile CancellableTask currentTask;
    

//    protected abstract Problem preCheck(CompilationController javac) throws IOException;
//    protected abstract Problem checkParameters(CompilationController javac) throws IOException;
//    protected abstract Problem fastCheckParameters(CompilationController javac) throws IOException;

//    protected abstract Source getRubySource(Phase p);

    public void cancel() {
    }

//    public final void run(CompilationController javac) throws Exception {
//        switch(whatRun) {
//        case PRECHECK:
//            this.problem = preCheck(javac);
//            break;
//        case CHECKPARAMETERS:
//            this.problem = checkParameters(javac);
//            break;
//        case FASTCHECKPARAMETERS:
//            this.problem = fastCheckParameters(javac);
//            break;
//        default:
//            throw new IllegalStateException();
//        }
//    }
    
    public Problem preCheck() {
        return run(Phase.PRECHECK);
    }

    public Problem checkParameters() {
        return run(Phase.CHECKPARAMETERS);
    }

    public Problem fastCheckParameters() {
        return run(Phase.FASTCHECKPARAMETERS);
    }

    private Problem run(Phase s) {
        this.whatRun = s;
        this.problem = null;
//        Source js = getRubySource(s);
//        if (js==null) {
//            return null;
//        }
//        try {
//            js.runUserActionTask(this, true);
//        } catch (IOException ex) {
//            throw new RuntimeException(ex);
//        }
        return problem;
    }
    
    public void cancelRequest() {
        cancelRequest = true;
//        if (currentTask!=null) {
//            currentTask.cancel();
//        }
    }

//    protected ClasspathInfo getClasspathInfo(AbstractRefactoring refactoring) {
//        ClasspathInfo cpInfo = refactoring.getContext().lookup(ClasspathInfo.class);
//        if (cpInfo==null) {
//            Logger.getLogger(getClass().getName()).log(Level.INFO, "Missing scope (ClasspathInfo), using default scope (all open projects)");
//            cpInfo = RetoucheUtils.getClasspathInfoFor((FileObject)null);
//            refactoring.getContext().add(cpInfo);
//        }
//        return cpInfo;
//    }
    
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
    
    private Iterable<? extends List<FileObject>> groupByRoot (Iterable<? extends FileObject> data) {
        Map<FileObject,List<FileObject>> result = new HashMap<FileObject,List<FileObject>> ();
        for (FileObject file : data) {
//            ClassPath cp = ClassPath.getClassPath(file, ClassPath.SOURCE);
//            if (cp != null) {
//                FileObject root = cp.findOwnerRoot(file);
//                if (root != null) {
//                    List<FileObject> subr = result.get (root);
//                    if (subr == null) {
//                        subr = new LinkedList<FileObject>();
//                        result.put (root,subr);
//                    }
//                    subr.add (file);
//                }
//            }
        }
        return result.values();
    }        

//    protected final Collection<ModificationResult> processFiles(Set<FileObject> files, CancellableTask<WorkingCopy> task) {
//        currentTask = task;
//        Collection<ModificationResult> results = new LinkedList<ModificationResult>();
//        try {
//            // Process Ruby files and RHTML files separately - and OTHER files separately
//            Set<FileObject> rubyFiles = new HashSet<FileObject>(2*files.size());
//            Set<FileObject> rhtmlFiles = new HashSet<FileObject>(2*files.size());
//            for (FileObject file : files) {
//                if (RubyUtils.isRubyFile(file)) {
//                    rubyFiles.add(file);
//                } else if (RubyUtils.isRhtmlFile(file)) {
//                    rhtmlFiles.add(file);
//                }
//            }
//
//            Iterable<? extends List<FileObject>> work = groupByRoot(rubyFiles);
//            for (List<FileObject> fos : work) {
//                final Source source = Source.create(ClasspathInfo.create(fos.get(0)), fos);
//                try {
//                    results.add(source.runModificationTask(task));
//                } catch (IOException ex) {
//                    throw (RuntimeException) new RuntimeException().initCause(ex);
//                }
//            }
//            work = groupByRoot(rhtmlFiles);
//            for (List<FileObject> fos : work) {
//                final Source source = Source.createFromModel(ClasspathInfo.create(fos.get(0)), fos, new RhtmlEmbeddingModel());
//                try {// todo - make sure I have a source in case I add rhtml files here!
//                    results.add(source.runModificationTask(task));
//                } catch (IOException ex) {
//                    throw (RuntimeException) new RuntimeException().initCause(ex);
//                }
//            }
//        } finally {
//            currentTask = null;
//        }
//        return results;
//    }

//    protected class TransformTask implements CancellableTask<WorkingCopy> {
//        private SearchVisitor visitor;
//        private RubyElementCtx treePathHandle;
//        public TransformTask(SearchVisitor visitor, RubyElementCtx searchedItem) {
//            this.visitor = visitor;
//            this.treePathHandle = searchedItem;
//        }
//        
//        public void cancel() {
//        }
//        
//        public void run(WorkingCopy compiler) throws IOException {
//            visitor.setWorkingCopy(compiler);
//            visitor.scan();
//            
//            //for (RubyElementCtx tree : visitor.getUsages()) {
//            //    ElementGripFactory.getDefault().put(compiler.getFileObject(), tree, compiler);
//            //}
//
//            fireProgressListenerStep();
//        }
//    }
}
