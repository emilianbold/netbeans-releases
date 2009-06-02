/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.refactoring.javascript.plugins;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.csl.spi.support.ModificationResult;
import org.netbeans.modules.javascript.editing.AstUtilities;
import org.netbeans.modules.javascript.editing.JsParseResult;
import org.netbeans.modules.javascript.editing.JsUtils;
import org.netbeans.modules.javascript.editing.lexer.JsTokenId;
import org.netbeans.modules.parsing.api.Embedding;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.refactoring.spi.*;
import org.netbeans.modules.refactoring.api.*;
import org.netbeans.modules.refactoring.javascript.RetoucheUtils;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Jan Becicka
 */
public abstract class JsRefactoringPlugin extends ProgressProviderAdapter implements RefactoringPlugin {

// XXX: parsingapi
//    protected enum Phase {PRECHECK, FASTCHECKPARAMETERS, CHECKPARAMETERS, PREPARE, DEFAULT};
    private boolean cancelled;
    

// XXX: parsingapi
//    protected abstract Problem preCheck(ResultIterator resultIterator) throws IOException;
//    protected abstract Problem checkParameters(ResultIterator resultIterator) throws IOException;
//    protected abstract Problem fastCheckParameters(ResultIterator resultIterator) throws IOException;
//
//    protected abstract Source getJsSource(Phase p);
//
//    public final Problem preCheck() {
//        return run(Phase.PRECHECK);
//    }
//
//    public final Problem checkParameters() {
//        return run(Phase.CHECKPARAMETERS);
//    }
//
//    public final Problem fastCheckParameters() {
//        return run(Phase.FASTCHECKPARAMETERS);
//    }
//
//    private Problem run(final Phase refactorinPhase) {
//        Source js = getJsSource(refactorinPhase);
//        if (js == null) {
//            return null;
//        }
//
//        try {
//            final Problem [] problem = new Problem [] { null };
//
//            ParserManager.parse(Collections.singleton(js), new UserTask() {
//                public @Override void run(ResultIterator resultIterator) throws Exception {
//                    switch(refactorinPhase) {
//                    case PRECHECK:
//                        problem[0] = JsRefactoringPlugin.this.preCheck(resultIterator);
//                        break;
//                    case CHECKPARAMETERS:
//                        problem[0] = JsRefactoringPlugin.this.checkParameters(resultIterator);
//                        break;
//                    case FASTCHECKPARAMETERS:
//                        problem[0] = JsRefactoringPlugin.this.fastCheckParameters(resultIterator);
//                        break;
//                    default:
//                        throw new IllegalStateException();
//                    }
//                }
//            });
//
//            return problem[0];
//        } catch (ParseException ex) {
//            throw new RuntimeException(ex);
//        }
//    }
    
    public final void cancelRequest() {
        synchronized (this) {
            cancelled = true;
        }
    }

    protected final boolean isCancelled() {
        synchronized (this) {
            return cancelled;
        }
    }

// XXX: parsingapi
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

// XXX: parsingapi
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
        Set<Source> sources = new HashSet<Source>(2*files.size());
        for (FileObject file : files) {
            if (RetoucheUtils.isJsFile(file)) {
                // RetoucheUtils.isJsFile includes HTML files, PHP files, etc.
                // where as JsUtils.isJsFile includes ONLY pure JavaScript files
                if ((!JsUtils.isJsFile(file)) && file.getSize() >= 1024*1024) {
                    // Skip really large HTML files
                    continue;
                }
                sources.add(Source.create(file));
            }
        }

        try {
            for(Source s : sources) {
                ParserManager.parse(Collections.singletonList(s), task);
            }
            return task.results;
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        
//        Iterable<? extends List<FileObject>> work = groupByRoot(jsFiles);
//        for (List<FileObject> fos : work) {
//            final Source source = Source.create(ClasspathInfo.create(fos.get(0)), fos);
//            try {
//                results.add(source.runModificationTask(task));
//            } catch (IOException ex) {
//                throw (RuntimeException) new RuntimeException().initCause(ex);
//            }
//        }
    }
    
    protected abstract class TransformTask extends UserTask {
        private final Collection<ModificationResult> results = new ArrayList<ModificationResult>();
        
        public final void run(ResultIterator resultIterator) throws ParseException {
            visit(resultIterator);
            fireProgressListenerStep();
        }

        protected abstract Collection<ModificationResult> process(JsParseResult jspr);

        private void visit(ResultIterator resultIterator) throws ParseException {
            if (resultIterator.getSnapshot().getMimeType().equals(JsTokenId.JAVASCRIPT_MIME_TYPE)) {
                JsParseResult jspr = AstUtilities.getParseResult(resultIterator.getParserResult());
                Collection<ModificationResult> r = process(jspr);
                results.addAll(r);
            }

            for(Embedding e : resultIterator.getEmbeddings()) {
                visit(resultIterator.getResultIterator(e));
            }
        }
    } // End of TransformTask class
}
