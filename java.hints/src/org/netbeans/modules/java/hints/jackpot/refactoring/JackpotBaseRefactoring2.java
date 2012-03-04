/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.hints.jackpot.refactoring;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.ModificationResult.Difference;
import org.netbeans.api.java.source.TypeMirrorHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.java.hints.jackpot.spi.PatternConvertor;
import org.netbeans.modules.java.hints.providers.spi.HintDescription;
import org.netbeans.modules.java.hints.providers.spi.HintDescription.Worker;
import org.netbeans.modules.java.hints.providers.spi.HintDescriptionFactory;
import org.netbeans.modules.java.hints.providers.spi.Trigger;
import org.netbeans.modules.java.hints.spiimpl.MessageImpl;
import org.netbeans.modules.java.hints.spiimpl.batch.BatchSearch;
import org.netbeans.modules.java.hints.spiimpl.batch.BatchSearch.BatchResult;
import org.netbeans.modules.java.hints.spiimpl.batch.BatchUtilities;
import org.netbeans.modules.java.hints.spiimpl.batch.ProgressHandleWrapper;
import org.netbeans.modules.java.hints.spiimpl.batch.Scopes;
import org.netbeans.modules.java.hints.spiimpl.pm.PatternCompiler;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.java.spi.DiffElement;
import org.netbeans.modules.refactoring.java.spi.JavaRefactoringPlugin;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.editor.hints.Severity;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.JavaFix;
import org.netbeans.api.java.source.matching.Matcher;
import org.netbeans.api.java.source.matching.Occurrence;
import org.netbeans.api.java.source.matching.Pattern;
import org.openide.filesystems.FileObject;

/**
 * TODO: needs to be merged with JackpotBaseRefactoring
 * @author lahvac
 * 
 */
public class JackpotBaseRefactoring2 {

    private JackpotBaseRefactoring2() {
    }

    /**
     * XXX: cancelability
     * @param inputJackpotPattern
     * @param transformer
     * @return
     */
    public static Collection<? extends ModificationResult> performTransformation(final String inputJackpotPattern, final Transform transformer) {
        List<HintDescription> descriptions = new ArrayList<HintDescription>();

        for (HintDescription hd : PatternConvertor.create(inputJackpotPattern)) {
            final String triggerPattern = ((Trigger.PatternDescription) hd.getTrigger()).getPattern();
            descriptions.add(HintDescriptionFactory.create().setTrigger(hd.getTrigger()).setWorker(new Worker() {
                @Override public Collection<? extends ErrorDescription> createErrors(HintContext ctx) {
                    final Map<String, TypeMirrorHandle<?>> constraintsHandles = new HashMap<String, TypeMirrorHandle<?>>();

                    for (Entry<String, TypeMirror> c : ctx.getConstraints().entrySet()) {
                        constraintsHandles.put(c.getKey(), TypeMirrorHandle.create(c.getValue()));
                    }

                    Fix fix = new JavaFix(ctx.getInfo(), ctx.getPath()) {
                        @Override protected String getText() {
                            return "";
                        }
                        @Override protected void performRewrite(TransformationContext ctx) {
                            WorkingCopy wc = ctx.getWorkingCopy();
                            Map<String, TypeMirror> constraints = new HashMap<String, TypeMirror>();

                            for (Entry<String, TypeMirrorHandle<?>> c : constraintsHandles.entrySet()) {
                                constraints.put(c.getKey(), c.getValue().resolve(wc));
                            }

                            Pattern pattern = PatternCompiler.compile(wc, triggerPattern, constraints, Collections.<String>emptyList());
                            Collection<? extends Occurrence> occurrence = Matcher.create(wc).setTreeTopSearch().setSearchRoot(ctx.getPath()).match(pattern);

                            assert occurrence.size() == 1;

                            transformer.transform(wc, occurrence.iterator().next());
                        }
                    }.toEditorFix();
                    
                    return Collections.singletonList(ErrorDescriptionFactory.createErrorDescription(Severity.WARNING, "", Collections.singletonList(fix), ctx.getInfo().getFileObject(), 0, 0));
                }
            }).produce());

        }
        
        BatchResult batchResult = BatchSearch.findOccurrences(descriptions, Scopes.allOpenedProjectsScope());
        return BatchUtilities.applyFixes(batchResult, new ProgressHandleWrapper(1, 1), new AtomicBoolean(), new ArrayList<MessageImpl>());
    }

    public interface Transform {
        public void transform(WorkingCopy copy, Occurrence occurrence);
    }

    public static void createAndAddElements(AbstractRefactoring refactoring, RefactoringElementsBag elements, Collection<ModificationResult> results) {
        elements.registerTransaction(JavaRefactoringPlugin.createTransaction(results));
        for (ModificationResult result:results) {
            for (FileObject jfo : result.getModifiedFileObjects()) {
                for (Difference diff: result.getDifferences(jfo)) {
                    elements.add(refactoring, DiffElement.create(diff, jfo, result));
                }
            }
        }
    }
}
