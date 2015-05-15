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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.editor.document.LineDocument;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.editor.BaseDocument;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.cnd.api.lexer.CndLexerUtilities;
import org.netbeans.cnd.api.lexer.CppTokenId;
import org.netbeans.modules.cnd.analysis.api.AnalyzerResponse;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.services.CsmFileInfoQuery;
import org.netbeans.modules.cnd.api.model.syntaxerr.AbstractCodeAudit;
import org.netbeans.modules.cnd.api.model.syntaxerr.AuditPreferences;
import org.netbeans.modules.cnd.api.model.syntaxerr.CodeAuditFactory;
import org.netbeans.modules.cnd.api.model.syntaxerr.CsmErrorInfo;
import org.netbeans.modules.cnd.api.model.syntaxerr.CsmErrorInfoHintProvider;
import org.netbeans.modules.cnd.api.model.syntaxerr.CsmErrorProvider;
import org.netbeans.modules.cnd.highlight.error.CodeAssistanceHintProvider;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.text.NbDocument;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
import org.netbeans.modules.cnd.refactoring.api.ui.CsmRefactoringActionsFactory;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Danila Sergeyev
 */

public class MissedGuardBlock extends AbstractCodeAudit {
    
    private MissedGuardBlock(String id, String name, String description, String defaultSeverity, boolean defaultEnabled, AuditPreferences myPreferences) {
        super(id, name, description, defaultSeverity, defaultEnabled, myPreferences);
    }

    @Override
    public boolean isSupportedEvent(CsmErrorProvider.EditorEvent kind) {
        return kind == CsmErrorProvider.EditorEvent.FileBased;
    }

    @Override
    public void doGetErrors(CsmErrorProvider.Request request, CsmErrorProvider.Response response) {
        CsmFile file = request.getFile();
        if (file.isHeaderFile()) {            
            if (!CsmFileInfoQuery.getDefault().hasGuardBlock(file)) {
                final Document doc = request.getDocument();
                final AtomicInteger startOffset = new AtomicInteger(0);
                
                Runnable runnable = new Runnable () {
                    @Override
                    public void run() {
                        TokenSequence<TokenId> docTokenSequence = CndLexerUtilities.getCppTokenSequence(doc, doc.getLength(), false, true);
                        if (docTokenSequence == null) {
                            return;
                        }
                        docTokenSequence.moveStart();
                        
                        while (docTokenSequence.moveNext()) {
                            if (docTokenSequence.token().id() instanceof CppTokenId) {
                                CppTokenId tokenId = (CppTokenId) docTokenSequence.token().id();
                                switch (tokenId) {
                                    case LINE_COMMENT:
                                    case NEW_LINE:
                                    case DOXYGEN_LINE_COMMENT:
                                    case BLOCK_COMMENT:
                                    case DOXYGEN_COMMENT:
                                    case WHITESPACE:
                                    case ESCAPED_WHITESPACE:
                                    case ESCAPED_LINE:
                                        continue;
                                    default:
                                        startOffset.set(docTokenSequence.offset());
                                        return;
                                }
                            }
                        }
                        docTokenSequence.moveEnd();
                        docTokenSequence.movePrevious();
                        startOffset.set(docTokenSequence.offset());
                    }
                };
                
                FutureTask<AtomicInteger> moveBellowCommentsTask = new FutureTask<>(runnable, startOffset);
                doc.render(moveBellowCommentsTask);
                
                try {
                    String message = NbBundle.getMessage(MissedGuardBlock.class, "MissedGuardBlock.description"); // NOI18N
                    int start = moveBellowCommentsTask.get().get();
                    int end = start;
                    if (doc instanceof LineDocument) {
                        end = LineDocumentUtils.getLineEnd((LineDocument) doc, start);
                    }
                    CsmErrorInfo.Severity severity = toSeverity(minimalSeverity());
                    if (response instanceof AnalyzerResponse) {
                        ((AnalyzerResponse) response).addError(AnalyzerResponse.AnalyzerSeverity.DetectedError, null, null,
                            new MissedGuardBlock.MissedGuardBlockErrorInfoImpl(doc, file, CodeAssistanceHintProvider.NAME, getID(), getName()+"\n"+message, severity, start, end));  // NOI18N
                    } else {
                        response.addError(new MissedGuardBlock.MissedGuardBlockErrorInfoImpl(doc, file, CodeAssistanceHintProvider.NAME, getID(), message, severity, start, end));
                    }
                } catch (InterruptedException ex) {
                    ex.printStackTrace(System.err);
                } catch (CancellationException ex) {
                    ex.printStackTrace(System.err);
                } catch (ExecutionException ex) {
                    ex.printStackTrace(System.err);
                } catch (BadLocationException ex) {
                    ex.printStackTrace(System.err);
                }
            }
        }
    }
    
    @ServiceProvider(path = CodeAuditFactory.REGISTRATION_PATH+CodeAssistanceHintProvider.NAME, service = CodeAuditFactory.class, position = 1300)
    public static final class Factory implements CodeAuditFactory {
        @Override
        public AbstractCodeAudit create(AuditPreferences preferences) {
            String id = NbBundle.getMessage(MissedGuardBlock.class, "MissedGuardBlock.name");  // NOI18N
            String description = NbBundle.getMessage(MissedGuardBlock.class, "MissedGuardBlock.description");  // NOI18N
            return new MissedGuardBlock(id, id, description, "warning", true, preferences);  // NOI18N
        }
    }
    
    private static final class MissedGuardBlockErrorInfoImpl extends ErrorInfoImpl {
        private final BaseDocument doc;
        private final CsmFile file;
        
        public MissedGuardBlockErrorInfoImpl(Document doc, CsmFile file, String providerName, String audutName, String message, CsmErrorInfo.Severity severity, int startOffset, int endOffset) {
            super(providerName, audutName, message, severity, startOffset, endOffset);
            this.doc = (BaseDocument) doc;
            this.file = file;
        }
    }
    
    @ServiceProvider(service = CsmErrorInfoHintProvider.class, position = 1100)
    public static final class MissedGuardBlockFixProvider extends CsmErrorInfoHintProvider {

        @Override
        protected List<Fix> doGetFixes(CsmErrorInfo info, List<Fix> alreadyFound) {
            if (info instanceof MissedGuardBlock.MissedGuardBlockErrorInfoImpl) {
                alreadyFound.addAll(createFixes((MissedGuardBlock.MissedGuardBlockErrorInfoImpl) info));
            }
            return alreadyFound;
        }
        
        private List<? extends Fix> createFixes(MissedGuardBlock.MissedGuardBlockErrorInfoImpl info) {
            try {
                List<Fix> fixes = new ArrayList<>();
                if (info.getStartOffset() == info.getEndOffset()) {
                    fixes.add(new MissedGuardBlock.AddGuardBlock(info.doc, info.file, info.getStartOffset(), info.getStartOffset()));
                } else {
                    fixes.add(new MissedGuardBlock.AddGuardBlock(info.doc, info.file, info.getStartOffset(), info.file.getText().length()-1));
                }
                fixes.add(new MissedGuardBlock.AddPragmaOnce(info.doc, info.file, info.getStartOffset()));
                return fixes;
            } catch (BadLocationException ex) {
                return Collections.emptyList();
            }
        }
    }
    
    private static final class AddGuardBlock implements Fix {
        private final BaseDocument doc;
        private final CsmFile file;
        private final int startOffset;
        private final int endOffset;
        
        public AddGuardBlock (BaseDocument doc, CsmFile file, int startOffset, int endOffset) throws BadLocationException {
            this.doc = doc;
            this.file = file;
            this.startOffset = startOffset;
            this.endOffset = endOffset;
        }
        
        @Override
        public String getText() {
            return NbBundle.getMessage(MissedGuardBlock.class, "MissedGuardBlock.fix.block"); // NOI18N
        }
        
        @Override
        public ChangeInfo implement () throws Exception {
            Position ifndefPosition = NbDocument.createPosition(doc, startOffset, Position.Bias.Forward);
            Position endifPossition = NbDocument.createPosition(doc, endOffset, Position.Bias.Forward);
            
            // Strings to build guard block
            final String defName = file.getFileObject().getName().toUpperCase() + "_H\n";  // NOI18N
            final String ifndefMacro = "#ifndef ";  // NOI18N
            final String defineMacro = "#define ";  // NOI18N
            final String endifMacro = "#endif\n";  // NOI18N
            final String openGuardBlockText = ifndefMacro + defName + defineMacro + defName + "\n";  // NOI18N
            
            // offsets
            final int ifndefStartPos = startOffset + ifndefMacro.length();
            final int ifndefEndPos = ifndefStartPos + defName.length();
            final int defStartPos = ifndefEndPos + defineMacro.length();
            final int defEndPos = defStartPos + defName.length();
            
            doc.insertString(ifndefPosition.getOffset(), openGuardBlockText, null);
            doc.insertString(endifPossition.getOffset(), "\n\n"+endifMacro, null); // NOI18N
            
            Position ifndefStart = NbDocument.createPosition(doc, ifndefStartPos, Position.Bias.Forward);
            Position ifndefEnd = NbDocument.createPosition(doc, ifndefEndPos-1, Position.Bias.Backward); // substracts 1 because of new line symols
            Position defineStart = NbDocument.createPosition(doc, defStartPos, Position.Bias.Forward);
            Position defineEnd = NbDocument.createPosition(doc, defEndPos-1, Position.Bias.Backward); // substracts 1 because of new line symols
            
            final ChangeInfo changeInfo = new ChangeInfo();
            final FileObject fo = file.getFileObject();
            changeInfo.add(fo, ifndefStart, ifndefEnd);
            changeInfo.add(fo, defineStart, defineEnd);
            CsmRefactoringActionsFactory.performInstantRenameAction(EditorRegistry.lastFocusedComponent(), changeInfo);
            return null;
        }
    }
    
    private static final class AddPragmaOnce implements Fix {
        private final BaseDocument doc;
        private final CsmFile file;
        private final int offset;
        
        public AddPragmaOnce (BaseDocument doc, CsmFile file, int offset) throws BadLocationException {
            this.doc = doc;
            this.file = file;
            this.offset = offset;
        }
        
        @Override
        public String getText() {
            return NbBundle.getMessage(MissedGuardBlock.class, "MissedGuardBlock.fix.pragma"); // NOI18N
        }
        
        @Override
        public ChangeInfo implement () throws Exception {
            Position ifndefPosition = NbDocument.createPosition(doc, offset, Position.Bias.Forward);
            doc.insertString(ifndefPosition.getOffset(), "#pragma once\n\n", null); // NOI18N
            return null;
        }
    }
    
}
