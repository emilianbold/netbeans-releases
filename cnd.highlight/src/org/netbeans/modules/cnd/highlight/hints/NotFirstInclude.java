/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.highlight.hints;

import java.util.Collections;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.cnd.analysis.api.AnalyzerResponse;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmInclude;
import org.netbeans.modules.cnd.api.model.CsmMethod;
import org.netbeans.modules.cnd.api.model.syntaxerr.AbstractCodeAudit;
import static org.netbeans.modules.cnd.api.model.syntaxerr.AbstractCodeAudit.toSeverity;
import org.netbeans.modules.cnd.api.model.syntaxerr.AuditPreferences;
import org.netbeans.modules.cnd.api.model.syntaxerr.CodeAuditFactory;
import org.netbeans.modules.cnd.api.model.syntaxerr.CsmErrorInfo;
import org.netbeans.modules.cnd.api.model.syntaxerr.CsmErrorInfoHintProvider;
import org.netbeans.modules.cnd.api.model.syntaxerr.CsmErrorProvider;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 */
public class NotFirstInclude extends AbstractCodeAudit {
    private final String message;

    private NotFirstInclude(String id, String name, String description, String defaultSeverity, boolean defaultEnabled, AuditPreferences myPreferences, String message) {
        super(id, name, description, defaultSeverity, defaultEnabled, myPreferences);
        this.message = message;
    }

    @Override
    public boolean isSupportedEvent(CsmErrorProvider.EditorEvent kind) {
        return kind == CsmErrorProvider.EditorEvent.FileBased;
    }

    @Override
    public void doGetErrors(CsmErrorProvider.Request request, CsmErrorProvider.Response response) {
        CsmFile file = request.getFile();
        if (file.isSourceFile()) {
            String name = file.getFileObject().getName();
            int i = 0;
            int insertionPoint = -1;
            for (CsmInclude incl : file.getIncludes()) {
                if (request.isCancelled()) {
                    return;
                }
                if (i == 0) {
                    insertionPoint = incl.getStartOffset();
                }
                CsmFile inc = incl.getIncludeFile();
                if (inc != null) {
                    String headerName = inc.getFileObject().getName();
                    if (name.equals(headerName)) {
                        if (i > 0) {
                            if (response instanceof AnalyzerResponse) {
                                String decoratedText = getID()+"\n"+NbBundle.getMessage(NotFirstInclude.class, message, getIncludeText(incl)); // NOI18N
                                ((AnalyzerResponse) response).addError(AnalyzerResponse.AnalyzerSeverity.DetectedError, null, file.getFileObject(),
                                        new MoveIncludeErrorInfoImpl(request.getDocument(), CsmHintProvider.NAME, getID(), decoratedText, toSeverity(minimalSeverity()),
                                                incl.getStartOffset(), incl.getEndOffset(), insertionPoint));
                            } else {
                                String decoratedText = NbBundle.getMessage(NotFirstInclude.class, message, getIncludeText(incl));
                                response.addError(
                                        new MoveIncludeErrorInfoImpl(request.getDocument(), CsmHintProvider.NAME, getID(), decoratedText, toSeverity(minimalSeverity()),
                                                incl.getStartOffset(), incl.getEndOffset(), insertionPoint));
                            }
                        }
                        break;
                    }
                }
                i++;
            }
        }
    }

    private static String getIncludeText(CsmInclude incl){
        if (incl.isSystem()){
            return "<"+incl.getIncludeName()+">"; // NOI18N
        }
        return "\""+incl.getIncludeName()+"\""; // NOI18N
    }

    @ServiceProvider(path = CodeAuditFactory.REGISTRATION_PATH+CsmHintProvider.NAME, service = CodeAuditFactory.class, position = 1300)
    public static final class Factory implements CodeAuditFactory {
        @Override
        public AbstractCodeAudit create(AuditPreferences preferences) {
            String id = NbBundle.getMessage(MissingGuardBlock.class, "NotFirstInclude.name");  // NOI18N
            String description = NbBundle.getMessage(MissingGuardBlock.class, "NotFirstInclude.description");  // NOI18N
            String message = "NotFirstInclude.message"; // NOI18N
            return new NotFirstInclude(id, id, description, "warning", false, preferences, message);  // NOI18N
        }
    }

    private static final class MoveIncludeErrorInfoImpl extends ErrorInfoImpl {
        private final BaseDocument doc;
        private final int insertionPoint;
        public MoveIncludeErrorInfoImpl(Document doc, String providerName, String audutName, String message, CsmErrorInfo.Severity severity,
                int startOffset, int endOffset, int insertionPoint) {
            super(providerName, audutName, message, severity, startOffset, endOffset);
            this.doc = (BaseDocument) doc;
            this.insertionPoint = insertionPoint;
        }
    }

    @ServiceProvider(service = CsmErrorInfoHintProvider.class, position = 1700)
    public static final class MoveIncludeFixProvider extends CsmErrorInfoHintProvider {

        @Override
        protected List<Fix> doGetFixes(CsmErrorInfo info, List<Fix> alreadyFound) {
            if (info instanceof MoveIncludeErrorInfoImpl) {
                alreadyFound.addAll(createFixes((MoveIncludeErrorInfoImpl) info));
            }
            return alreadyFound;
        }

        private List<? extends Fix> createFixes(MoveIncludeErrorInfoImpl info) {
            try {
                return Collections.singletonList(new MoveIncludeFix(info.doc, info.getStartOffset(), info.getEndOffset(), info.insertionPoint));
            } catch (BadLocationException ex) {
                return Collections.emptyList();
            }
        }
    }

    private static final class MoveIncludeFix extends SafeFix {
        private final BaseDocument doc;
        private final Position start;
        private final Position end;
        private final Position isertionPoint;

        public MoveIncludeFix(BaseDocument doc, int startOffset, int endOffset, int insertionPoint) throws BadLocationException {
            this.doc = doc;
            this.start = NbDocument.createPosition(doc, startOffset-1, Position.Bias.Forward);
            this.end = NbDocument.createPosition(doc, endOffset, Position.Bias.Backward);
            this.isertionPoint = NbDocument.createPosition(doc, insertionPoint, Position.Bias.Backward);
        }

        @Override
        public String getText() {
            return NbBundle.getMessage(NonVirtualDestructor.class, "NotFirstInclude.fix"); // NOI18N
        }

        @Override
        public ChangeInfo performFix() throws BadLocationException, Exception {
            doc.runAtomicAsUser(new Runnable() {
                @Override
                public void run() {
                    try {
                        String text = doc.getText(start.getOffset()+1, end.getOffset() - start.getOffset() - 1)+"\n"; // NOI18N
                        doc.remove(start.getOffset(), end.getOffset() - start.getOffset());
                        doc.insertString(isertionPoint.getOffset(), text, null);
                    } catch (BadLocationException ex) {
                        ex.printStackTrace(System.err);
                    }
                }
            });
            return null;
        }
    }
}
