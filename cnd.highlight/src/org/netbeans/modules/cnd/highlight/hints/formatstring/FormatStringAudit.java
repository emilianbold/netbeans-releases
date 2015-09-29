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
package org.netbeans.modules.cnd.highlight.hints.formatstring;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.cnd.api.lexer.CndLexerUtilities;
import org.netbeans.cnd.api.lexer.CppTokenId;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.cnd.analysis.api.AnalyzerResponse;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.syntaxerr.AbstractCodeAudit;
import static org.netbeans.modules.cnd.api.model.syntaxerr.AbstractCodeAudit.toSeverity;
import org.netbeans.modules.cnd.api.model.syntaxerr.AuditPreferences;
import org.netbeans.modules.cnd.api.model.syntaxerr.CodeAuditFactory;
import org.netbeans.modules.cnd.api.model.syntaxerr.CsmErrorInfo;
import org.netbeans.modules.cnd.api.model.syntaxerr.CsmErrorInfoHintProvider;
import org.netbeans.modules.cnd.api.model.syntaxerr.CsmErrorProvider;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceResolver;
import org.netbeans.modules.cnd.highlight.hints.CsmHintProvider;
import org.netbeans.modules.cnd.highlight.hints.ErrorInfoImpl;
import org.netbeans.modules.cnd.highlight.hints.SafeFix;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Danila Sergeyev
 */
public class FormatStringAudit extends AbstractCodeAudit {
    
    private static final boolean CHECKS_ENABLED;
    
    static {
        String checksEnabled = System.getProperty("printf.check.enable"); //NOI18N
        if (checksEnabled != null) {
            CHECKS_ENABLED = Boolean.parseBoolean(checksEnabled);
        } else {
            CHECKS_ENABLED = true;
        }
    }
    
    private FormatStringAudit(String id, String name, String description, String defaultSeverity, boolean defaultEnabled, AuditPreferences myPreferences) {
        super(id, name, description, defaultSeverity, defaultEnabled, myPreferences);
    }
    
    @Override
    public boolean isSupportedEvent(CsmErrorProvider.EditorEvent kind) {
        return kind == CsmErrorProvider.EditorEvent.FileBased;
    }
    
    @Override
    public void doGetErrors(CsmErrorProvider.Request request, CsmErrorProvider.Response response) {
        final CsmFile file = request.getFile();
        if (file != null) {
            if (request.isCancelled()) {
                return;
            }
            
            Document doc_ = request.getDocument();
            if (doc_ == null) {
                CloneableEditorSupport ces = CsmUtilities.findCloneableEditorSupport(file);
                doc_ = CsmUtilities.openDocument(ces);
            }
            final Document doc = doc_;
            final List<FormattedPrintFunction> result = new LinkedList<>();
            
            Collection<CsmReference> references = CsmReferenceResolver.getDefault().getReferences(file);
            for (CsmReference reference : references) {
                CsmObject object = reference.getReferencedObject();
                if (Utilities.isFormattedPrintFunction(object)) {
                    final int startOffset = reference.getStartOffset();
                    
                    doc.render(new Runnable() {
                        @Override
                        public void run() {
                            TokenSequence<TokenId> docTokenSequence = CndLexerUtilities.getCppTokenSequence(doc, startOffset, false, true);
                            if (docTokenSequence == null) {
                                return;
                            }
                            
                            final CsmReferenceResolver rr = CsmReferenceResolver.getDefault();
                            StringBuilder paramBuf = new StringBuilder();
                            ArrayList<Parameter> params = new ArrayList<>();
                            
                            State state = State.DEFAULT;
                            boolean formatFlag = false;  // detect was format string already processed
                            StringBuilder formatString = null;
                            int paramOffset = -1;
                            int bracketsCounter = 0;
                            int formatStringOffset = -1;
                            boolean doNotResolveType = false;
                            
                            while (docTokenSequence.moveNext()) {
                                Token<TokenId> token = docTokenSequence.token();
                                TokenId tokenId = token.id();
                                if (tokenId.equals(CppTokenId.IDENTIFIER) && state == State.DEFAULT) {
                                    CsmReference reference = rr.findReference(file, doc, docTokenSequence.offset());
                                    CsmObject object = reference.getReferencedObject();
                                    state = State.START;
                                } else if (tokenId.equals(CppTokenId.LPAREN) && state == State.START) {
                                    state = State.IN_PARAM;
                                } else if (tokenId.equals(CppTokenId.LPAREN) && state == State.IN_PARAM) {
                                    state = State.IN_PARAM_BRACKET;
                                    bracketsCounter++;
                                    if (formatFlag) {
                                        paramBuf.append(token.text());
                                        if (paramOffset == -1) {
                                            paramOffset = docTokenSequence.offset();
                                        }
                                    }
                                } else if (tokenId.equals(CppTokenId.LPAREN) && state == State.IN_PARAM_BRACKET) {
                                    bracketsCounter++;
                                    if (formatFlag) {
                                        paramBuf.append(token.text());
                                        if (paramOffset == -1) {
                                            paramOffset = docTokenSequence.offset();
                                        }
                                    }
                                } else if (tokenId.equals(CppTokenId.RPAREN) && state == State.IN_PARAM_BRACKET) {
                                    bracketsCounter--;
                                    if (bracketsCounter == 0) {
                                        state = State.IN_PARAM;
                                    }
                                    if (formatFlag) {
                                        paramBuf.append(token.text());
                                        if (paramOffset == -1) {
                                            paramOffset = docTokenSequence.offset();
                                        }
                                    }
                                } else if (tokenId.equals(CppTokenId.RPAREN) && state == State.IN_PARAM) {
                                    if (paramBuf.length() > 0) {
                                        params.add(new Parameter(paramBuf.toString(), paramOffset, !doNotResolveType));
                                        paramOffset = -1;
                                    }
                                    result.add(new FormattedPrintFunction(file
                                                                         ,formatStringOffset
                                                                         ,(formatString == null) ? "" : formatString.toString()
                                                                         ,params));
                                    return;
                                } else if (state == State.IN_PARAM && tokenId.equals(CppTokenId.STRING_LITERAL) && !formatFlag) {
                                    params = new ArrayList<>();
                                    if (formatString == null) {
                                        formatString = new StringBuilder();
                                    }
                                    formatString.append(token.text().toString());
                                    formatStringOffset = docTokenSequence.offset();
                                } else if (state == State.IN_PARAM && !formatFlag && formatString != null && tokenId.equals(CppTokenId.COMMA)) {
                                    formatFlag = true;
                                } else if (state == State.IN_PARAM && formatFlag && tokenId.equals(CppTokenId.COMMA)) {
                                    if (paramBuf.length() > 0) {
                                        params.add(new Parameter(paramBuf.toString(), paramOffset, !doNotResolveType));
                                        paramOffset = -1;
                                    }
                                    doNotResolveType = false;
                                    paramBuf = new StringBuilder();
                                } else if ((state == State.IN_PARAM || state == State.IN_PARAM_BRACKET) 
                                        && !tokenId.primaryCategory().equals(CppTokenId.COMMENT_CATEGORY)
                                        && formatFlag) {
                                    if (paramBuf.length() == 0 && tokenId.primaryCategory().equals(CppTokenId.WHITESPACE_CATEGORY)) {
                                        // skip whitespoaces before the parameter
                                        continue;
                                    }
                                    if (paramOffset == -1) {
                                        // save start offset of the parameter
                                        paramOffset = docTokenSequence.offset();
                                    }
                                    // skip macro parameters
                                    CsmReference ref = rr.findReference(file, doc, docTokenSequence.offset());
                                    if (ref != null && CsmKindUtilities.isMacro(ref.getReferencedObject())) {
                                        doNotResolveType = true;
                                    }
                                    paramBuf.append(token.text());
                                } else if ((state == State.IN_PARAM || state == State.IN_PARAM_BRACKET) 
                                        && !tokenId.primaryCategory().equals(CppTokenId.COMMENT_CATEGORY)) {
                                    // skip check in case of string concatenation with macros
                                    CsmReference ref = rr.findReference(file, doc, docTokenSequence.offset());
                                    if (ref != null && CsmKindUtilities.isMacro(ref.getReferencedObject())) {
                                        break;
                                    }
                                }
                            }
                        }
                    });
                }
            }
            
            List<FormatError> errors = new LinkedList<>();
            for (FormattedPrintFunction function : result) {
                errors.addAll(function.validate());
            }
            for (FormatError error : errors) {
                CsmErrorInfo.Severity severity = toSeverity(minimalSeverity());
                try {
                    if (response instanceof AnalyzerResponse) {
                        ((AnalyzerResponse) response).addError(AnalyzerResponse.AnalyzerSeverity.DetectedError, null, file.getFileObject(),
                            new FormatStringErrorInfoImpl(doc
                                                         ,CsmHintProvider.NAME, getID()
                                                         ,getName()+"\n"+Utilities.getMessageForError(error)
                                                         ,severity
                                                         ,error));
                    } else {
                        response.addError(new FormatStringErrorInfoImpl(doc
                                                                       ,CsmHintProvider.NAME
                                                                       ,getID()
                                                                       ,Utilities.getMessageForError(error)
                                                                       ,severity
                                                                       ,error));
                    }
                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }
    
    private static enum State {
        DEFAULT,
        START,
        IN_PARAM,
        IN_PARAM_BRACKET
    }
    
    @ServiceProvider(path = CodeAuditFactory.REGISTRATION_PATH+CsmHintProvider.NAME, service = CodeAuditFactory.class, position = 4000)
    public static final class Factory implements CodeAuditFactory {
        @Override
        public AbstractCodeAudit create(AuditPreferences preferences) {
            String id = NbBundle.getMessage(FormatStringAudit.class, "FormatStringAudit.name");  // NOI18N
            String description = NbBundle.getMessage(FormatStringAudit.class, "FormatStringAudit.description");  // NOI18N
            return new FormatStringAudit(id, id, description, "error", CHECKS_ENABLED, preferences);  // NOI18N
        }
    }
    
    private static final class FormatStringErrorInfoImpl extends ErrorInfoImpl {
        private final BaseDocument doc;
        private final Position startPosition;
        private final Position endPosition;
        private final FormatError error;
        
        public FormatStringErrorInfoImpl(Document doc
                                        ,String providerName
                                        ,String audutName
                                        ,String message
                                        ,CsmErrorInfo.Severity severity
                                        ,FormatError error) throws BadLocationException {
            super(providerName, audutName, message, severity, error.startOffset(), error.endOffset());
            this.doc = (BaseDocument) doc;
            this.error = error;
            startPosition = NbDocument.createPosition(doc, this.error.startOffset(), Position.Bias.Forward);
            endPosition = NbDocument.createPosition(doc, this.error.endOffset(), Position.Bias.Backward);
        }
    }
    
    @ServiceProvider(service = CsmErrorInfoHintProvider.class, position = 1600)
    public static final class FormatStringFixProvider extends CsmErrorInfoHintProvider {
        
        @Override
        protected List<Fix> doGetFixes(CsmErrorInfo info, List<Fix> alreadyFound) {
            if (info instanceof FormatStringErrorInfoImpl) {
                alreadyFound.addAll(createFixes((FormatStringErrorInfoImpl) info));
            }
            return alreadyFound;
        }
        
        private List<? extends Fix> createFixes(FormatStringErrorInfoImpl info) {
            try {
                List<Fix> fixes = new ArrayList<>();
                switch (info.error.getType()) {
                    case FLAG:
                    case LENGTH:
                    case TYPE_MISMATCH:
                        fixes.add(new FixFormat(info.doc, info.error, info.startPosition, info.endPosition));
                        break;
                    default:
                        break;
                }
                return fixes;
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
                return Collections.emptyList();
            }
        }
    }
    
    private static final class FixFormat extends SafeFix {
        private final BaseDocument doc;
        private final FormatError error;
        private final Position start;
        private final Position end;
        private final String oldText;
        private final String newText;
        
        public FixFormat(BaseDocument doc, FormatError error, Position start, Position end) throws BadLocationException {
            this.doc = doc;
            this.error = error;
            this.start = start;
            this.end = end;
            int length = end.getOffset() - start.getOffset();
            oldText = doc.getText(start.getOffset(), length);
            if (error.getType().equals(FormatError.FormatErrorType.TYPE_MISMATCH)) {
                newText = getAppropriateFormat(error.getFlag());
            } else {
                newText = oldText.replace(error.getFlag(), ""); // NOI18N
            }
        }
        
        @Override
        public String getText() {
            return NbBundle.getMessage(FormatStringAudit.class, "FormatStringAudit.fix.flag", oldText, newText); // NOI18N
        }
        
        @Override
        public ChangeInfo performFix() throws BadLocationException, Exception {
            int length = end.getOffset() - start.getOffset();
            doc.replace(start.getOffset(), length, newText, null);
            return null;
        }
        
        private String getAppropriateFormat(String type) {
            List<String> formats = Utilities.typeToFormat(type);
            String specifier = error.getSpecifier(); 
            if (specifier.startsWith("l")) {         // NOI18N
                specifier.replace("l", "");          // NOI18N
            } else if (specifier.startsWith("h")) {  // NOI18N
                specifier.replace("h", "");          // NOI18N
            }
            for (String format : formats) {
                if (format.contains(specifier)) {
                    return format;
                }
            }
            if (formats.isEmpty()) {
                return "p";  // NOI18N
            }
            return formats.get(0);
        }
    }
    
}
