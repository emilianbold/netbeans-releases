/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.cnd.paralleladviser.hints;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;
import javax.swing.text.Position;
import org.netbeans.modules.cnd.api.model.CsmChangeEvent;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmListeners;
import org.netbeans.modules.cnd.api.model.CsmModelListener;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.deep.CsmLoopStatement;
import org.netbeans.modules.cnd.api.model.syntaxerr.CsmErrorInfo;
import org.netbeans.modules.cnd.api.model.syntaxerr.CsmErrorProvider;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.paralleladviser.api.ParallelAdviser;
import org.netbeans.modules.cnd.paralleladviser.paralleladvisermonitor.impl.LoopParallelizationAdvice;
import org.netbeans.modules.cnd.paralleladviser.paralleladviserview.Advice;
import org.netbeans.modules.cnd.paralleladviser.paralleladviserview.ParallelAdviserTopComponent;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.EnhancedFix;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.editor.hints.HintsController;
import org.openide.loaders.DataObject;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.PositionBounds;
import org.openide.text.PositionRef;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * Hints about parallelization.
 *
 * @author Nick Krasilnikov
 */
public class ParallelAdviserHintsProvider extends CsmErrorProvider {

    private static final ParallelAdviserHintsProvider instance = new ParallelAdviserHintsProvider();
    private ModelListenerImpl modelListener = new ModelListenerImpl();
    private List<Advice> outdatedTips = new ArrayList<Advice>();

    public static ParallelAdviserHintsProvider getInstance() {
        return instance;
    }

    /** Creates a new instance of HighlightProvider */
    private ParallelAdviserHintsProvider() {
        CsmListeners.getDefault().addModelListener(modelListener);
    }

    public String getName() {
        return "parallel-adviser-hints"; //NOI18N
    }

    private static abstract class OffsetableErrorInfo implements CsmErrorInfo {

        private int start;
        private int end;
        private CsmErrorInfo.Severity severity;

        public OffsetableErrorInfo(CsmOffsetable offsetable, CsmErrorInfo.Severity severity) {
            start = offsetable.getStartOffset();
            end = offsetable.getEndOffset();
            this.severity = severity;
        }

        public OffsetableErrorInfo(int start, int end, CsmErrorInfo.Severity severity) {
            this.start = start;
            this.end = end;
            this.severity = severity;
        }

        public int getEndOffset() {
            return end;
        }

        public int getStartOffset() {
            return start;
        }

        public Severity getSeverity() {
            return severity;
        }
    }

    private static class LoopParallelizationInfo extends OffsetableErrorInfo implements CsmErrorInfo {

        private String message;

        public LoopParallelizationInfo(PositionBounds loopPosition) {
            super(loopPosition.getBegin().getOffset(), loopPosition.getBegin().getOffset() + 3, Severity.WARNING);
            this.message = NbBundle.getMessage(ParallelAdviserHintsProvider.class, "ParallelAdviser_LoopParallelization");
        }

        public String getMessage() {
            return message;
        }
    }

    @Override
    protected void doGetErrors(CsmErrorProvider.Request request, CsmErrorProvider.Response response) {
        for (LoopParallelizationInfo info : getErrors(request.getFile())) {
            response.addError(info);
        }
    }

    private List<LoopParallelizationInfo> getErrors(CsmFile file) {
        List<LoopParallelizationInfo> res = new ArrayList<LoopParallelizationInfo>();
        Collection<Advice> tips = ParallelAdviser.getTips();
        for (Advice advice : tips) {
            if (!outdatedTips.contains(advice)) {
                if (advice instanceof LoopParallelizationAdvice) {
                    PositionBounds loopPosition = ((LoopParallelizationAdvice) advice).getLoopPosition();
                    if (CsmUtilities.getCsmFile(loopPosition.getBegin().getCloneableEditorSupport().getDocument(), true) == file) {
                        res.add(new LoopParallelizationInfo(loopPosition));
                    }
                }
            }
        }
        List<Advice> newOutdatedTips = new ArrayList<Advice>();
        for (Advice advice : outdatedTips) {
            if(tips.contains(advice)) {
                newOutdatedTips.add(advice);
            }
        }
        outdatedTips.clear();
        outdatedTips.addAll(newOutdatedTips);
        return res;
    }

    /* package */ void update(CsmFile file, Document doc, DataObject dao) {
        if (doc != null) {
            addAnnotations(doc, file, dao);
        }
    }

    /* package */ void clear(Document doc) {
        if (doc != null) {
            removeAnnotations(doc);
        }
    }

    private void addAnnotations(final Document doc, final CsmFile file, final DataObject dao) {
        removeAnnotations(doc);
        final List<ErrorDescription> descriptions = new ArrayList<ErrorDescription>();
        for (LoopParallelizationInfo info : getErrors(file)) {
            PositionBounds pb = createPositionBounds(dao, info.getStartOffset(), info.getEndOffset());
            ErrorDescription desc = null;
            if (pb != null) {
                try {
                    List<Fix> fixes = new ArrayList<Fix>();
                    fixes.add(new ParallelAdviserHintFix());
                    desc = ErrorDescriptionFactory.createErrorDescription(
                            getSeverity(info), info.getMessage(), fixes, doc, pb.getBegin().getPosition(), pb.getEnd().getPosition());
                } catch (IOException ioe) {
                    Exceptions.printStackTrace(ioe);
                }
                descriptions.add(desc);
            }
        }
        HintsController.setErrors(doc, ParallelAdviserHintsProvider.class.getName(), descriptions);
    }

    private static PositionBounds createPositionBounds(DataObject dao, int start, int end) {
        CloneableEditorSupport ces = CsmUtilities.findCloneableEditorSupport(dao);
        if (ces != null) {
            PositionRef posBeg = ces.createPositionRef(start, Position.Bias.Forward);
            PositionRef posEnd = ces.createPositionRef(end, Position.Bias.Backward);
            return new PositionBounds(posBeg, posEnd);
        }
        return null;
    }

    private void removeAnnotations(Document doc) {
        HintsController.setErrors(doc, ParallelAdviserHintsProvider.class.getName(), Collections.<ErrorDescription>emptyList());
    }

    private static org.netbeans.spi.editor.hints.Severity getSeverity(CsmErrorInfo info) {
        switch (info.getSeverity()) {
            case ERROR:
                return org.netbeans.spi.editor.hints.Severity.ERROR;
            case WARNING:
                return org.netbeans.spi.editor.hints.Severity.WARNING;
            default:
                throw new IllegalArgumentException("Unexpected severity: " + info.getSeverity()); //NOI18N
        }
    }

    private static class ParallelAdviserHintFix implements EnhancedFix {

        public ParallelAdviserHintFix() {
        }

        public CharSequence getSortText() {
            return ParallelAdviserHintsProvider.class.getName(); // NOI18N
        }

        public String getText() {
            return NbBundle.getMessage(ParallelAdviserHintsProvider.class, "ParallelAdviser_LoopParallelizationHint"); // NOI18N
        }

        public ChangeInfo implement() throws Exception {
            Runnable openView = new Runnable() {

                public void run() {
                    ParallelAdviserTopComponent view = ParallelAdviserTopComponent.findInstance();
                    if (!view.isOpened()) {
                        view.open();
                    }
                    view.requestActive();
                }
            };
            if (SwingUtilities.isEventDispatchThread()) {
                openView.run();
            } else {
                SwingUtilities.invokeLater(openView);
            }
            return null;
        }
    }

    private class ModelListenerImpl implements CsmModelListener {

        public void projectOpened(CsmProject project) {
        }

        public void projectClosed(CsmProject project) {
        }

        public void modelChanged(CsmChangeEvent e) {
            Collection<CsmFile> changedFiles = e.getChangedFiles();
            Collection<Advice> tips = ParallelAdviser.getTips();
            for (Advice advice : tips) {
                if (advice instanceof LoopParallelizationAdvice) {
                    PositionBounds loopPosition = ((LoopParallelizationAdvice) advice).getLoopPosition();
                    if (changedFiles.contains(CsmUtilities.getCsmFile(loopPosition.getBegin().getCloneableEditorSupport().getDocument(), true))) {
                        outdatedTips.add(advice);
                    }
                }
            }
        }
    }
}
