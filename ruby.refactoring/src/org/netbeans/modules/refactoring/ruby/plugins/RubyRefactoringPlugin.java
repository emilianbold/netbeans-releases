/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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
package org.netbeans.modules.refactoring.ruby.plugins;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.csl.spi.support.ModificationResult;
import org.netbeans.modules.parsing.api.Embedding;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.refactoring.spi.*;
import org.netbeans.modules.refactoring.api.*;
import org.netbeans.modules.ruby.AstUtilities;
import org.netbeans.modules.ruby.RubyUtils;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Jan Becicka
 */
public abstract class RubyRefactoringPlugin extends ProgressProviderAdapter implements RefactoringPlugin {

//    protected enum Phase {PRECHECK, FASTCHECKPARAMETERS, CHECKPARAMETERS, PREPARE, DEFAULT};
//    private Phase whatRun = Phase.DEFAULT;
//    private Problem problem;
//    protected volatile boolean cancelRequest;
//    private volatile CancellableTask currentTask;
    protected boolean cancelled;

//    protected abstract Problem preCheck(CompilationController javac) throws IOException;
//    protected abstract Problem checkParameters(CompilationController javac) throws IOException;
//    protected abstract Problem fastCheckParameters(CompilationController javac) throws IOException;
//
//    protected abstract Source getRubySource(Phase p);
//
//    public void cancel() {
//    }
//
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
    
    protected final boolean isCancelled() {
        synchronized (this) {
            return cancelled;
        }
    }

    public final void cancelRequest() {
        synchronized (this) {
            cancelled = true;
        }
    }

//    protected ClasspathInfo getClasspathInfo(AbstractRefactoring refactoring) {
//        ClasspathInfo cpInfo = refactoring.getContext().lookup(ClasspathInfo.class);
//        if (cpInfo==null) {
//            Logger.getLogger(getClass().getName()).log(Level.INFO, "Missing scope (ClasspathInfo), using default scope (all open projects)");
//            cpInfo = RetoucheUtils.getClasspathInfoFor((FileObject)null);
//            if (cpInfo != null) {
//                refactoring.getContext().add(cpInfo);
//            }
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
    
//    private Iterable<? extends List<FileObject>> groupByRoot (Iterable<? extends FileObject> data) {
//        Map<FileObject,List<FileObject>> result = new HashMap<FileObject,List<FileObject>> ();
//        for (FileObject file : data) {
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
//        }
//        return result.values();
//    }        

    protected final Collection<ModificationResult> processFiles(Set<FileObject> files, TransformTask task) {
        // Process Ruby files and RHTML files separately - and OTHER files separately
        // TODO - now that I don't need separate RHTML models any more, can
        // I just do a single pass?
        Set<Source> rubyFiles = new HashSet<Source>(2 * files.size());
        Set<Source> rhtmlFiles = new HashSet<Source>(2 * files.size());
        for (FileObject file : files) {
            if (RubyUtils.isRubyFile(file)) {
                rubyFiles.add(Source.create(file));
            } else if (RubyUtils.isRhtmlOrYamlFile(file)) {
                // Avoid opening HUGE Yaml files - they may be containing primarily data
                if (file.getSize() > 512 * 1024) {
                    continue;
                }
                rhtmlFiles.add(Source.create(file));
            }
        }

        Set<Source> sources = new HashSet<Source>(rubyFiles.size() + rhtmlFiles.size());
        sources.addAll(rubyFiles);
        sources.addAll(rhtmlFiles);

        try {
            ParserManager.parse(sources, task);
            return task.results;
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

////
////        Iterable<? extends List<FileObject>> work = groupByRoot(rubyFiles);
////        for (List<FileObject> fos : work) {
////            final Source source = Source.create(ClasspathInfo.create(fos.get(0)), fos);
////            try {
////                results.add(source.runModificationTask(task));
////            } catch (IOException ex) {
////                throw (RuntimeException) new RuntimeException().initCause(ex);
////            }
////        }
////        work = groupByRoot(rhtmlFiles);
////        for (List<FileObject> fos : work) {
////            final Source source = Source.create(ClasspathInfo.create(fos.get(0)), fos);
////            try {
////                results.add(source.runModificationTask(task));
////            } catch (IOException ex) {
////                throw (RuntimeException) new RuntimeException().initCause(ex);
////            }
////        }
//        return results;
    }
    
    protected abstract class TransformTask extends UserTask {

        private final Collection<ModificationResult> results = new ArrayList<ModificationResult>();

        public final void run(ResultIterator resultIterator) throws ParseException {
            visit(resultIterator);
            fireProgressListenerStep();
        }

        protected abstract Collection<ModificationResult> process(ParserResult jspr);

        private void visit(ResultIterator resultIterator) throws ParseException {
            if (resultIterator.getSnapshot().getMimeType().equals(RubyUtils.RUBY_MIME_TYPE)) {
                ParserResult pr = AstUtilities.getParseResult(resultIterator.getParserResult());
                if (pr != null) {
                    Collection<ModificationResult> r = process(pr);
                    results.addAll(r);
                }
            }

            for (Embedding e : resultIterator.getEmbeddings()) {
                visit(resultIterator.getResultIterator(e));
            }
        }
    }

}
