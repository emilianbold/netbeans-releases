/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.refactoring.hints;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.text.Document;
import javax.swing.text.Position;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.editor.mimelookup.MimeRegistrations;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.cnd.api.lexer.CppTokenId;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFunctionDefinition;
import org.netbeans.modules.cnd.api.model.CsmInstantiation;
import org.netbeans.modules.cnd.api.model.CsmNamespaceDefinition;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmType;
import org.netbeans.modules.cnd.api.model.deep.CsmCompoundStatement;
import org.netbeans.modules.cnd.api.model.deep.CsmExpressionStatement;
import org.netbeans.modules.cnd.api.model.deep.CsmStatement;
import org.netbeans.modules.cnd.api.model.services.CsmTypeResolver;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.CursorMovedSchedulerEvent;
import org.netbeans.modules.parsing.spi.ParserResultTask;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.netbeans.modules.parsing.spi.SchedulerTask;
import org.netbeans.modules.parsing.spi.TaskFactory;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.editor.hints.HintsController;
import org.netbeans.spi.editor.hints.Severity;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 * @author Alexander Simon
 */
public class LineFactoryTask extends ParserResultTask<CndParserResult> {
    private final AtomicBoolean canceled = new AtomicBoolean(false);
    
    public LineFactoryTask() {
    }

    @Override
    public void run(CndParserResult result, SchedulerEvent event) {
        final Document doc = result.getSnapshot().getSource().getDocument(false);
        final FileObject fileObject = result.getSnapshot().getSource().getFileObject();
        Collection<CsmFile> csmFiles = result.getCsmFiles();
        if (csmFiles.size() == 1) {
            if (event instanceof CursorMovedSchedulerEvent) {
                CursorMovedSchedulerEvent cursorEvent = (CursorMovedSchedulerEvent) event;
                int caretOffset = cursorEvent.getCaretOffset();
                CsmFile file = csmFiles.iterator().next();
                CsmExpressionStatement expression = findExpressionStatement(file.getDeclarations(), caretOffset, doc);
                if (expression != null) {
                    createHint(expression, doc, fileObject);
                } else {
                    clearHint(doc, fileObject);
                }
            }
        }
    }
    
    private CsmExpressionStatement findExpressionStatement(Collection<? extends CsmOffsetableDeclaration> decls, int offset, Document doc) {
        for(CsmOffsetableDeclaration decl : decls) {
            if (decl.getStartOffset() < offset && offset < decl.getEndOffset()) {
                if (CsmKindUtilities.isFunctionDefinition(decl)) {
                    CsmFunctionDefinition def = (CsmFunctionDefinition) decl;
                    return findExpressionStatement(def.getBody(), offset, doc);
                } else if (CsmKindUtilities.isNamespaceDefinition(decl)) {
                    CsmNamespaceDefinition def = (CsmNamespaceDefinition) decl;
                    return findExpressionStatement(def.getDeclarations(), offset, doc);
                } else if (CsmKindUtilities.isClass(decl)) {
                    CsmClass cls = (CsmClass) decl;
                    return findExpressionStatement(cls.getMembers(), offset, doc);
                }
            }
        }
        return null;
    }    
    
    private CsmExpressionStatement findExpressionStatement(CsmCompoundStatement body, int offset, final Document doc) {
        if (body != null) {
            final List<CsmStatement> statements = body.getStatements();
            for(int i = 0; i < statements.size(); i++) {
                final CsmStatement st = statements.get(i);
                final int startOffset = st.getStartOffset();
                final int endOffset = st.getEndOffset();
                if (startOffset > offset) {
                    break;
                }
                if (st.getKind() == CsmStatement.Kind.COMPOUND) {
                    if (startOffset <= offset && offset < endOffset) {
                        findExpressionStatement((CsmCompoundStatement)st, offset, doc);
                    }
                } else if (st.getKind() == CsmStatement.Kind.EXPRESSION){
                    final int nexStartOffset;
                    if(i+1 < statements.size()) {
                        nexStartOffset = statements.get(i+1).getStartOffset();
                    } else {
                        nexStartOffset = body.getEndOffset();
                    }
                    if (startOffset <= offset && offset < nexStartOffset) {
                        final AtomicInteger trueEndOffset = new AtomicInteger(endOffset);
                        doc.render(new Runnable() {

                            @Override
                            public void run() {
                                TokenHierarchy<Document> hi = TokenHierarchy.get(doc);
                                TokenSequence<?> ts = hi.tokenSequence();
                                ts.move(endOffset);
                                while (ts.moveNext()) {
                                    Token<?> token = ts.token();
                                    if (ts.offset() >= nexStartOffset) {
                                        break;
                                    }
                                    if (CppTokenId.SEMICOLON.equals(token.id())) {
                                        trueEndOffset.set(ts.offset()+1);
                                        break;
                                    }
                                }
                            }
                        });
                        if (startOffset <= offset && offset <= trueEndOffset.get()) {
                            if(isApplicable((CsmExpressionStatement) st)) {
                                return (CsmExpressionStatement) st;
                            } else {
                                return null;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }
    
    private boolean isApplicable(CsmExpressionStatement st) {
        CsmType resolveType = CsmTypeResolver.resolveType(st.getExpression(), null);
        if (resolveType == null) {
            return false;
        }
        final String typeText = resolveType.getCanonicalText().toString();
        if ("void".equals(typeText)) { //NOI18N
            return false;
        }
        return true;
    }
    
    private void createHint(CsmExpressionStatement expression, Document doc, FileObject fo) {
        List<Fix> fixes = Collections.<Fix>singletonList(new FixImpl(expression, doc, fo));
        String description = NbBundle.getMessage(LineFactoryTask.class, "HINT_AssignResultToVariable"); //NOI18N
        List<ErrorDescription> hints = Collections.singletonList(
                ErrorDescriptionFactory.createErrorDescription(Severity.HINT, description, fixes, fo,
                        expression.getStartOffset(), expression.getStartOffset()));
        HintsController.setErrors(doc, LineFactoryTask.class.getName(), hints);
        
    }

    private void clearHint(Document doc, FileObject fo) {
        HintsController.setErrors(doc, LineFactoryTask.class.getName(), Collections.<ErrorDescription>emptyList());
        
    }
    
    @Override
    public int getPriority() {
        return 100;
    }

    @Override
    public Class<? extends Scheduler> getSchedulerClass() {
        return Scheduler.CURSOR_SENSITIVE_TASK_SCHEDULER;
    }

    @Override
    public final synchronized void cancel() {
        canceled.set(true);
    }
    
    @MimeRegistrations({
        @MimeRegistration(mimeType = MIMENames.C_MIME_TYPE, service = TaskFactory.class),
        @MimeRegistration(mimeType = MIMENames.CPLUSPLUS_MIME_TYPE, service = TaskFactory.class),
        @MimeRegistration(mimeType = MIMENames.HEADER_MIME_TYPE, service = TaskFactory.class)
    })
    public static class NavigatorSourceFactory extends TaskFactory {
        @Override
        public Collection<? extends SchedulerTask> create(Snapshot snapshot) {
            return Collections.singletonList(new LineFactoryTask());
        }
    }
    
    private static final class FixImpl implements Fix{
        private final CsmExpressionStatement expression;
        private final Document doc;
        private final FileObject fo;
        private String name;

        private FixImpl(CsmExpressionStatement expression, Document doc, FileObject fo) {
            this.expression = expression;
            this.doc = doc;
            this.fo = fo;
        }

        @Override
        public String getText() {
            return NbBundle.getMessage(LineFactoryTask.class, "FIX_AssignResultToVariable"); //NOI18N
        }
        
        private String suggestName() {
            doc.render(new Runnable() {

                @Override
                public void run() {
                    TokenHierarchy<Document> hi = TokenHierarchy.get(doc);
                    TokenSequence<?> ts = hi.tokenSequence();
                    ts.move(expression.getStartOffset());
                    while (ts.moveNext()) {
                        Token<?> token = ts.token();
                        if (ts.offset() > expression.getEndOffset()) {
                            break;
                        }
                        if (CppTokenId.IDENTIFIER.equals(token.id())) {
                            name = token.text().toString();
                        } else if (CppTokenId.LPAREN.equals(token.id())) {
                            if (name != null) {
                                break;
                            }
                        }
                    }
                }
            });
            if (name == null) {
                name = "variable"; //NOI18N
            } else {
                if (name.toLowerCase().startsWith("get") && name.length() > 3) { //NOI18N
                    name = name.substring(3);
                } else if (name.toLowerCase().startsWith("is") && name.length() > 2) { //NOI18N
                    name = name.substring(2);
                }
            }
            return name;
        }

        @Override
        public ChangeInfo implement() throws Exception {
            CsmType resolveType = CsmTypeResolver.resolveType(expression.getExpression(), null);
            if (resolveType == null) {
                return null;
            }
            final String typeText = resolveType.getCanonicalText().toString();
            if ("void".equals(typeText)) { //NOI18N
                return null;
            }
            final String aName = suggestName();
            final String text = typeText+" "+aName+" = "; //NOI18N
            doc.insertString(expression.getStartOffset(), text, null);
            Position startPosition = new Position() {
                
                @Override
                public int getOffset() {
                    return expression.getStartOffset()+typeText.length()+1;
                }
            };
            Position endPosition = new Position() {
                
                @Override
                public int getOffset() {
                    return expression.getStartOffset()+text.length() - 3;
                }
            };
            ChangeInfo changeInfo = new ChangeInfo(fo, startPosition, endPosition);
            return changeInfo;
        }
        
    }
}
