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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.editor.mimelookup.MimeRegistrations;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.deep.CsmExpressionStatement;
import org.netbeans.modules.cnd.api.model.deep.CsmStatement;
import org.netbeans.modules.cnd.api.model.services.CsmFileInfoQuery;
import org.netbeans.modules.cnd.api.model.services.CsmMacroExpansion;
import org.netbeans.modules.cnd.api.model.syntaxerr.AbstractCodeAudit;
import org.netbeans.modules.cnd.api.model.syntaxerr.AuditPreferences;
import org.netbeans.modules.cnd.api.model.syntaxerr.CodeAudit;
import org.netbeans.modules.cnd.api.model.syntaxerr.CodeAuditFactory;
import org.netbeans.modules.cnd.api.model.syntaxerr.CsmErrorProvider;
import org.netbeans.modules.cnd.refactoring.hints.ExpressionFinder.AssignmentFixImpl;
import org.netbeans.modules.cnd.refactoring.hints.ExpressionFinder.IntroduceFixImpl;
import org.netbeans.modules.cnd.refactoring.hints.ExpressionFinder.StatementResult;
import org.netbeans.modules.cnd.refactoring.hints.StatementFinder.AddMissingCasesFixImpl;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.CursorMovedSchedulerEvent;
import org.netbeans.modules.parsing.spi.IndexingAwareParserResultTask;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.netbeans.modules.parsing.spi.SchedulerTask;
import org.netbeans.modules.parsing.spi.TaskFactory;
import org.netbeans.modules.parsing.spi.TaskIndexingMode;
import org.netbeans.modules.parsing.spi.support.CancelSupport;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.editor.hints.HintsController;
import org.netbeans.spi.editor.hints.Severity;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Alexander Simon
 */
public class SuggestionFactoryTask extends IndexingAwareParserResultTask<Parser.Result> {
    private static final Logger LOG = Logger.getLogger("org.netbeans.modules.cnd.model.tasks"); //NOI18N
    private final CancelSupport cancel = CancelSupport.create(this);
    private AtomicBoolean canceled = new AtomicBoolean(false);
    
    public SuggestionFactoryTask() {
        super(TaskIndexingMode.ALLOWED_DURING_SCAN);
    }

    @Override
    public void run(Parser.Result result, SchedulerEvent event) {
        synchronized (this) {
            canceled.set(true);
            canceled = new AtomicBoolean(false);
        }
        if (cancel.isCancelled()) {
            return;
        }
        Collection<CodeAudit> audits = SuggestionProvider.getInstance().getAudits();
        boolean enabled = false;
        for(CodeAudit audit : audits) {
            if (audit.isEnabled()) {
                enabled = true;
                break;
            }
        }
        if (!enabled) {
            return;
        }
        long time = 0;
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "LineFactoryTask started"); //NOI18N
            time = System.currentTimeMillis();
        }
        final Document doc = result.getSnapshot().getSource().getDocument(false);
        final FileObject fileObject = result.getSnapshot().getSource().getFileObject();
        final CsmFile file = CsmFileInfoQuery.getDefault().getCsmFile(result);
        if (file != null && doc != null && doc.getProperty(CsmMacroExpansion.MACRO_EXPANSION_VIEW_DOCUMENT) == null) {
            if (event instanceof CursorMovedSchedulerEvent) {
                process(audits, doc, fileObject, (CursorMovedSchedulerEvent)event, file, canceled);
            }
        }
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "LineFactoryTask finished for {0}ms", System.currentTimeMillis()-time); //NOI18N
        }
    }

    private void process(Collection<CodeAudit> audits, final Document doc, final FileObject fileObject, CursorMovedSchedulerEvent cursorEvent, final CsmFile file, final AtomicBoolean canceled) {
        clearHint(doc, fileObject);
        int caretOffset = cursorEvent.getCaretOffset();
        JTextComponent comp = EditorRegistry.lastFocusedComponent();
        int selectionStart = caretOffset;
        int selectionEnd = caretOffset;
        if (comp != null) {
            selectionStart = Math.min(cursorEvent.getCaretOffset(),cursorEvent.getMarkOffset());//comp.getSelectionStart();
            selectionEnd = Math.max(cursorEvent.getCaretOffset(),cursorEvent.getMarkOffset());//comp.getSelectionEnd();
        }
        if (canceled.get())  {
            return;
        }
        boolean introduce = false;
        boolean assign = false;
        boolean cases = false;
        for(CodeAudit audit : audits) {
            if (IntroduceVariable.ID.equals(audit.getID()) && audit.isEnabled()) {
                introduce = true;
            } else if (AssignVariable.ID.equals(audit.getID()) && audit.isEnabled()) {
                assign = true;
            } else if (AddMissingCases.ID.equals(audit.getID()) && audit.isEnabled()) {
                cases = true;
            } 
        }
        if (assign || introduce) {
            detectIntroduceVariable(file, caretOffset, selectionStart, selectionEnd, doc, canceled, assign, fileObject, introduce, comp);
        }
        if (cases) {
            if (caretOffset > 0) {
                try {
                    String text = doc.getText(caretOffset-1, 1);
                    if (text.startsWith("{")) { //NOI18N
                        // probably it is switch
                        StatementFinder finder = new StatementFinder(doc, file, caretOffset, selectionStart, selectionEnd, canceled);
                        CsmStatement findStatement = finder.findStatement();
                        if (findStatement != null && findStatement.getKind() == CsmStatement.Kind.SWITCH) {
                            createSwitchHint(findStatement, doc, comp, fileObject, caretOffset);
                        }
                    }
                } catch (BadLocationException ex) {
                }
            }
        }
    }

    private void detectIntroduceVariable(final CsmFile file, int caretOffset, int selectionStart, int selectionEnd, final Document doc, final AtomicBoolean canceled, boolean assign, final FileObject fileObject, boolean introduce, JTextComponent comp) {
        ExpressionFinder expressionFinder = new ExpressionFinder(doc, file, caretOffset, selectionStart, selectionEnd, canceled);
         StatementResult res = expressionFinder.findExpressionStatement();
        if (res == null) {
            return;
        }
        if (canceled.get()) {
            return;
        }
        if (assign) {
            CsmExpressionStatement expression = res.expression;
            if (expression != null) {
                createStatementHint(expression, doc, fileObject);
            }
        }
        if (introduce) {
            if (res.container != null && res.statementInBody != null && comp != null && selectionStart < selectionEnd) {
                if (CsmFileInfoQuery.getDefault().getLineColumnByOffset(file, selectionStart)[0] ==
                        CsmFileInfoQuery.getDefault().getLineColumnByOffset(file, selectionEnd)[0] &&
                        expressionFinder.isExpressionSelection()) {
                    if (!(res.container.getStartOffset() == selectionStart &&
                            res.container.getEndOffset() == selectionEnd)) {
                        CsmOffsetable applicableTextExpression = expressionFinder.applicableTextExpression();
                        if (applicableTextExpression != null) {
                            createExpressionHint(res.statementInBody, applicableTextExpression, doc, comp, fileObject);
                        }
                    }
                }
            }
        }
    }
    
    private void createStatementHint(CsmExpressionStatement expression, Document doc, FileObject fo) {
        List<Fix> fixes = Collections.<Fix>singletonList(new AssignmentFixImpl(expression.getExpression(), doc, fo));
        String description = NbBundle.getMessage(SuggestionFactoryTask.class, "AssignVariable.name"); //NOI18N
        List<ErrorDescription> hints = Collections.singletonList(
                ErrorDescriptionFactory.createErrorDescription(Severity.HINT, description, fixes, fo,
                        expression.getStartOffset(), expression.getStartOffset()));
        HintsController.setErrors(doc, SuggestionFactoryTask.class.getName(), hints);
        
    }

    private void createExpressionHint(CsmStatement st, CsmOffsetable expression, Document doc, JTextComponent comp, FileObject fo) {
        List<Fix> fixes = Collections.<Fix>singletonList(new IntroduceFixImpl(st, expression, doc, comp, fo));
        String description = NbBundle.getMessage(SuggestionFactoryTask.class, "IntroduceVariable.name"); //NOI18N
        List<ErrorDescription> hints = Collections.singletonList(
                ErrorDescriptionFactory.createErrorDescription(Severity.HINT, description, fixes, fo,
                        expression.getStartOffset(), expression.getStartOffset()));
        HintsController.setErrors(doc, SuggestionFactoryTask.class.getName(), hints);
    }

    private void createSwitchHint(CsmStatement st, Document doc, JTextComponent comp, FileObject fo, int caretOffset) {
        List<Fix> fixes = Collections.<Fix>singletonList(new AddMissingCasesFixImpl(st, doc, comp, fo, caretOffset));
        String description = NbBundle.getMessage(SuggestionFactoryTask.class, "AddMissingCases.name"); //NOI18N
        List<ErrorDescription> hints = Collections.singletonList(
                ErrorDescriptionFactory.createErrorDescription(Severity.HINT, description, fixes, fo,
                        st.getStartOffset(), st.getStartOffset()));
        HintsController.setErrors(doc, SuggestionFactoryTask.class.getName(), hints);
    }
    
    private void clearHint(Document doc, FileObject fo) {
        HintsController.setErrors(doc, SuggestionFactoryTask.class.getName(), Collections.<ErrorDescription>emptyList());
    }
    
    @Override
    public int getPriority() {return 500;}

    @Override
    public Class<? extends Scheduler> getSchedulerClass() {
        return Scheduler.CURSOR_SENSITIVE_TASK_SCHEDULER;
    }

    @Override
    public final void cancel() {
        synchronized(this) {
            canceled.set(true);
        }
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "LineFactoryTask cancelled"); //NOI18N
        }
    }
    
    @MimeRegistrations({
        @MimeRegistration(mimeType = MIMENames.C_MIME_TYPE, service = TaskFactory.class),
        @MimeRegistration(mimeType = MIMENames.CPLUSPLUS_MIME_TYPE, service = TaskFactory.class),
        @MimeRegistration(mimeType = MIMENames.HEADER_MIME_TYPE, service = TaskFactory.class)
    })
    public static class SuggestionSourceFactory extends TaskFactory {
        @Override
        public Collection<? extends SchedulerTask> create(Snapshot snapshot) {
            return Collections.singletonList(new SuggestionFactoryTask());
        }
    }
    
    @ServiceProvider(path = CodeAuditFactory.REGISTRATION_PATH+SuggestionProvider.NAME, service = CodeAuditFactory.class, position = 1000)
    public static final class IntroduceVariable implements CodeAuditFactory {
        private static final String ID = "IntroduceVariable"; //NOI18N
        @Override
        public AbstractCodeAudit create(AuditPreferences preferences) {
            String name = NbBundle.getMessage(SuggestionFactoryTask.class, "IntroduceVariable.name"); // NOI18N
            String description = NbBundle.getMessage(SuggestionFactoryTask.class, "IntroduceVariable.description"); // NOI18N
            return new AbstractCodeAudit(ID, name, description, "warning", true, preferences) { // NOI18N

                @Override
                public boolean isSupportedEvent(CsmErrorProvider.EditorEvent kind) {
                    return true;
                }

                @Override
                public String getKind() {
                    return "action"; //NOI18N
                }

                @Override
                public void doGetErrors(CsmErrorProvider.Request request, CsmErrorProvider.Response response) {
                    throw new UnsupportedOperationException();
                }
            };
        }
    }
    
    @ServiceProvider(path = CodeAuditFactory.REGISTRATION_PATH+SuggestionProvider.NAME, service = CodeAuditFactory.class, position = 1000)
    public static final class AssignVariable implements CodeAuditFactory {
        private static final String ID = "AssignVariable"; //NOI18N
        @Override
        public AbstractCodeAudit create(AuditPreferences preferences) {
            String name = NbBundle.getMessage(SuggestionFactoryTask.class, "AssignVariable.name"); // NOI18N
            String description = NbBundle.getMessage(SuggestionFactoryTask.class, "AssignVariable.description"); // NOI18N
            return new AbstractCodeAudit(ID, name, description, "warning", true, preferences) { // NOI18N

                @Override
                public boolean isSupportedEvent(CsmErrorProvider.EditorEvent kind) {
                    return true;
                }

                @Override
                public String getKind() {
                    return "action"; //NOI18N
                }

                @Override
                public void doGetErrors(CsmErrorProvider.Request request, CsmErrorProvider.Response response) {
                    throw new UnsupportedOperationException();
                }
            };
        }
    }

    @ServiceProvider(path = CodeAuditFactory.REGISTRATION_PATH+SuggestionProvider.NAME, service = CodeAuditFactory.class, position = 1000)
    public static final class AddMissingCases implements CodeAuditFactory {
        private static final String ID = "AddMissingCases"; //NOI18N
        @Override
        public AbstractCodeAudit create(AuditPreferences preferences) {
            String name = NbBundle.getMessage(SuggestionFactoryTask.class, "AddMissingCases.name"); // NOI18N
            String description = NbBundle.getMessage(SuggestionFactoryTask.class, "AddMissingCases.description"); // NOI18N
            return new AbstractCodeAudit(ID, name, description, "warning", true, preferences) { // NOI18N

                @Override
                public boolean isSupportedEvent(CsmErrorProvider.EditorEvent kind) {
                    return true;
                }

                @Override
                public String getKind() {
                    return "action"; //NOI18N
                }

                @Override
                public void doGetErrors(CsmErrorProvider.Request request, CsmErrorProvider.Response response) {
                    throw new UnsupportedOperationException();
                }
            };
        }
    }
}
