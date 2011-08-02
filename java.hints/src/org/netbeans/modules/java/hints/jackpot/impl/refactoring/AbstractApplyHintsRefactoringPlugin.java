/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.hints.jackpot.impl.refactoring;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.ModificationResult.Difference;
import org.netbeans.modules.java.hints.jackpot.impl.MessageImpl;
import org.netbeans.modules.java.hints.jackpot.impl.batch.BatchSearch;
import org.netbeans.modules.java.hints.jackpot.impl.batch.BatchSearch.BatchResult;
import org.netbeans.modules.java.hints.jackpot.impl.batch.BatchSearch.Resource;
import org.netbeans.modules.java.hints.jackpot.impl.batch.BatchSearch.Scope;
import org.netbeans.modules.java.hints.jackpot.impl.batch.BatchUtilities;
import org.netbeans.modules.java.hints.jackpot.impl.batch.ProgressHandleWrapper;
import org.netbeans.modules.java.hints.jackpot.impl.batch.ProgressHandleWrapper.ProgressHandleAbstraction;
import org.netbeans.modules.java.hints.jackpot.spi.HintContext.MessageKind;
import org.netbeans.modules.java.hints.jackpot.spi.HintDescription;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.java.spi.DiffElement;
import org.netbeans.modules.refactoring.java.spi.JavaRefactoringPlugin;
import org.netbeans.modules.refactoring.spi.ProgressProviderAdapter;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.openide.filesystems.FileObject;
import org.openide.text.PositionBounds;

/**
 *
 * @author lahvac
 */
public abstract class AbstractApplyHintsRefactoringPlugin extends ProgressProviderAdapter implements RefactoringPlugin, ProgressHandleAbstraction {

    private final AbstractRefactoring refactoring;
    protected final AtomicBoolean cancel = new AtomicBoolean();

    protected AbstractApplyHintsRefactoringPlugin(AbstractRefactoring refactoring) {
        this.refactoring = refactoring;
    }

    public void cancelRequest() {
        cancel.set(true);
    }

    protected final Problem messagesToProblem(Collection<MessageImpl> problems) throws IllegalStateException {
        Problem current = null;

        for (MessageImpl problem : problems) {
            Problem p = new Problem(problem.kind == MessageKind.ERROR, problem.text);

            if (current != null)
                p.setNext(current);
            current = p;
        }

        return current;
    }

    protected Collection<MessageImpl> performApplyPattern(Iterable<? extends HintDescription> pattern, Scope scope, RefactoringElementsBag refactoringElements) {
        ProgressHandleWrapper w = new ProgressHandleWrapper(this, 30, 70);
        BatchResult candidates = BatchSearch.findOccurrences(pattern, scope, w);
        Collection<MessageImpl> problems = new LinkedList<MessageImpl>(candidates.problems);
        Collection<? extends ModificationResult> res = BatchUtilities.applyFixes(candidates, w, /*XXX*/new AtomicBoolean(), problems);

        refactoringElements.registerTransaction(JavaRefactoringPlugin.createTransaction(new LinkedList<ModificationResult>(res)));

        for (ModificationResult mr : res) {
            for (FileObject file : mr.getModifiedFileObjects()) {
                for (Difference d : mr.getDifferences(file)) {
                    refactoringElements.add(refactoring, DiffElement.create(d, file, mr));
                }
            }
        }

        w.finish();

        return problems;
    }

    protected final void prepareElements(BatchResult candidates, ProgressHandleWrapper w, final RefactoringElementsBag refactoringElements, final boolean verify, List<MessageImpl> problems) {
        if (verify) {
            BatchSearch.getVerifiedSpans(candidates, w, new BatchSearch.VerifiedSpansCallBack() {
                public void groupStarted() {}
                public boolean spansVerified(CompilationController wc, Resource r, Collection<? extends ErrorDescription> hints) throws Exception {
                    List<PositionBounds> spans = new LinkedList<PositionBounds>();

                    for (ErrorDescription ed : hints) {
                        spans.add(ed.getRange());
                    }

                    refactoringElements.addAll(refactoring, Utilities.createRefactoringElementImplementation(r.getResolvedFile(), spans, verify));

                    return true;
                }
                public void groupFinished() {}
                public void cannotVerifySpan(Resource r) {
                    refactoringElements.addAll(refactoring, Utilities.createRefactoringElementImplementation(r.getResolvedFile(), prepareSpansFor(r), verify));
                }
            }, problems);
        } else {
            int[] parts = new int[candidates.getResources().size()];
            int   index = 0;

            for (Collection<? extends Resource> resources : candidates.getResources()) {
                parts[index++] = resources.size();
            }

            ProgressHandleWrapper inner = w.startNextPartWithEmbedding(parts);

            for (Collection<? extends Resource> it :candidates.getResources()) {
                inner.startNextPart(it.size());

                for (Resource r : it) {
                    refactoringElements.addAll(refactoring, Utilities.createRefactoringElementImplementation(r.getResolvedFile(), prepareSpansFor(r), verify));
                    inner.tick();
                }
            }
        }
    }

    private static List<PositionBounds> prepareSpansFor(Resource r) {
        return Utilities.prepareSpansFor(r.getResolvedFile(), r.getCandidateSpans());
    }

    public void start(int totalWork) {
        fireProgressListenerStart(-1, totalWork);
        lastWorkDone = 0;
    }

    private int lastWorkDone;
    public void progress(int currentWorkDone) {
        while (lastWorkDone < currentWorkDone) {
            fireProgressListenerStep(currentWorkDone);
            lastWorkDone++;
        }
    }

    public void progress(String message) {
        //ignored
    }

    public void finish() {
        fireProgressListenerStop();
    }

}
