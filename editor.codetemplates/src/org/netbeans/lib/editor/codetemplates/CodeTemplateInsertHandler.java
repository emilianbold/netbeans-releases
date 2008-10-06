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

package org.netbeans.lib.editor.codetemplates;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Formatter;
import org.netbeans.editor.Utilities;
import org.netbeans.lib.editor.codetemplates.api.CodeTemplate;
import org.netbeans.lib.editor.codetemplates.spi.CodeTemplateInsertRequest;
import org.netbeans.lib.editor.codetemplates.spi.CodeTemplateParameter;
import org.netbeans.lib.editor.codetemplates.spi.CodeTemplateProcessor;
import org.netbeans.lib.editor.codetemplates.spi.CodeTemplateProcessorFactory;
import org.netbeans.lib.editor.codetemplates.textsync.TextRegion;
import org.netbeans.lib.editor.codetemplates.textsync.TextRegionEditing;
import org.netbeans.lib.editor.codetemplates.textsync.TextRegionManager;
import org.netbeans.lib.editor.codetemplates.textsync.TextSync;
import org.netbeans.lib.editor.codetemplates.textsync.TextSyncGroup;
import org.netbeans.lib.editor.codetemplates.textsync.TextSyncGroupEditingNotify;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.lib.editor.util.CharacterConversions;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;

/**
 * Code template allows the client to paste itself into the given
 * text component.
 *
 * @author Miloslav Metelka
 */
public final class CodeTemplateInsertHandler implements TextSyncGroupEditingNotify, Runnable {

    // -J-Dorg.netbeans.lib.editor.codetemplates.CodeTemplateInsertHandler.level=FINE
    private static final Logger LOG = Logger.getLogger(CodeTemplateInsertHandler.class.getName());
    /** logger for timers/counters */
    private static final Logger TIMERS = Logger.getLogger("TIMER"); // NOI18N

    /**
     * Property used while nested template expanding.
     */
    private static final Object CT_HANDLER_DOC_PROPERTY = "code-template-insert-handler"; // NOI18N
    
    private final CodeTemplate codeTemplate;
    
    private final JTextComponent component;
    
    private final List<CodeTemplateProcessor> processors;
    
    private String parametrizedText;
    
    private ParametrizedTextParser parametrizedTextParser;

    private String insertText;
    
    private List<CodeTemplateParameter> allParameters;
    
    private List<CodeTemplateParameter> masterParameters;
    
    private CodeTemplateInsertRequest request;
    
    private boolean inserted;
    
    private boolean released;
    
    private boolean completionInvoke;
    
    private TextRegion completeTextRegion;

    private String completeInsertString;

    private Formatter formatter;
    
    private TextSyncGroup textSyncGroup;
    
    private CodeTemplateParameterImpl lastActiveMasterImpl;
    
    /**
     * When expanding of a template was requested when still editing parameters
     * of an outer template remember the outer template's handler
     */
    private CodeTemplateInsertHandler outerHandler;
    
    public CodeTemplateInsertHandler(
        CodeTemplate codeTemplate,
        JTextComponent component, 
        Collection<? extends CodeTemplateProcessorFactory> processorFactories
    ) {
        this.codeTemplate = codeTemplate;
        this.component = component;

        completeTextRegion = new TextRegion();
        TextSync completeTextSync = new TextSync(completeTextRegion);
        textSyncGroup = new TextSyncGroup(completeTextSync);

        this.request = CodeTemplateSpiPackageAccessor.get().createInsertRequest(this);

        processors = new ArrayList<CodeTemplateProcessor>();
        for (CodeTemplateProcessorFactory factory : processorFactories) {
            processors.add(factory.createProcessor(this.request));
        }

        setParametrizedText(codeTemplate.getParametrizedText());
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Created " + super.toString() + "\n"); // NOI18N
        }
        if (TIMERS.isLoggable(Level.FINE)) {
            LogRecord rec = new LogRecord(Level.FINE, "CodeTemplateInsertHandler"); // NOI18N
            rec.setParameters(new Object[] { this });
            TIMERS.log(rec);
        }
    }
    
    public CodeTemplate getCodeTemplate() {
        return codeTemplate;
    }
    
    public JTextComponent getComponent() {
        return component;
    }
    
    public CodeTemplateInsertRequest getRequest() {
        return request;
    }
    
    public synchronized boolean isInserted() {
        return inserted;
    }
    
    public synchronized boolean isReleased() {
        return released;
    }
    
    public boolean isSuspended() {
        return (textRegionEditing().activeTextSyncGroup() == textSyncGroup);
    }
    
    public String getParametrizedText() {
        return parametrizedText;
    }
    
    public void setParametrizedText(String parametrizedText) {
        this.parametrizedText = CharacterConversions.lineSeparatorToLineFeed(parametrizedText);
        parseParametrizedText();
    }

    public int getInsertOffset() {
        return completeTextRegion.startOffset();
    }

    public String getInsertText() {
        if (inserted) {
            try {
                int startOffset = getInsertOffset();
                Document doc = component.getDocument();
                return doc.getText(startOffset, completeTextRegion.endOffset() - startOffset);
            } catch (BadLocationException e) {
                LOG.log(Level.WARNING, "Invalid offset", e); // NOI18N
                return "";
            }

        } else { // not inserted yet
            checkInsertTextBuilt();
            return insertText;
        }
    }
    
    public List<? extends CodeTemplateParameter> getAllParameters() {
        return Collections.unmodifiableList(allParameters);
    }

    public List<? extends CodeTemplateParameter> getMasterParameters() {
        return Collections.unmodifiableList(masterParameters);
    }
    
    public void processTemplate() {
        // Update default values by all processors
        for (CodeTemplateProcessor processor : processors) {
            processor.updateDefaultValues();
        }

        // Insert the template into document
        insertTemplate();

        // For nested template expanding or when without parameters
        // just update the caret position and release
        textRegionEditing().startGroupEditing(textSyncGroup, this);
        checkInvokeCompletion();
    }

    void checkInsertTextBuilt() {
        if (insertText == null) {
            insertText = buildInsertText();
        }
    }
    
    void resetCachedInsertText() {
        insertText = null;
    }
    
    CodeTemplateParameterImpl lastActiveMasterImpl() {
        return lastActiveMasterImpl;
    }

    public void insertTemplate() {
        Document doc = component.getDocument();
        outerHandler = (CodeTemplateInsertHandler)doc.getProperty(CT_HANDLER_DOC_PROPERTY);
        doc.putProperty(CT_HANDLER_DOC_PROPERTY, this);
        // Build insert string outside of the atomic lock
        completeInsertString = getInsertText();


        BaseDocument bdoc = (doc instanceof BaseDocument)
                ? (BaseDocument)doc
                : null;
        // Need to lock formatter first because CT's multiline text will be reformatted
        formatter = null;
        if (bdoc != null) {
            formatter = bdoc.getFormatter();
            if (formatter != null) {
                formatter.reformatLock();
            }
        }
        try {
            if (bdoc != null) {
                bdoc.runAtomicAsUser(this);
            } else { // Otherwise run without atomic locking
                this.run();
            }
        } finally {
            if (bdoc != null) {
                if (formatter != null) {
                    formatter.reformatUnlock();
                }
                formatter = null;
            }
            completeInsertString = null;
        }
    }

    public void run() {
        try {
            Document doc = component.getDocument();
            BaseDocument bdoc = (doc instanceof BaseDocument)
                    ? (BaseDocument) doc
                    : null;

            // First check if there is a caret selection and if so remove it
            Caret caret = component.getCaret();
            if (Utilities.isSelectionShowing(caret)) {
                int removeOffset = component.getSelectionStart();
                int removeLength = component.getSelectionEnd() - removeOffset;
                doc.remove(removeOffset, removeLength);
            }

            // insert the complete text
            int insertOffset = component.getCaretPosition();
            completeTextRegion.updateBounds(null, 
                    TextRegion.createFixedPosition(completeInsertString.length()));

            doc.insertString(insertOffset, completeInsertString, null);
            // #132615
            // Insert a special undoable-edit marker that - once undone will release CT editing.
            if (bdoc != null) {
                bdoc.addUndoableEdit(new TemplateInsertUndoEdit(doc));
            }
            
            TextRegion<?> caretTextRegion = null;
            // Go through all master parameters and create region infos for them
            for (CodeTemplateParameter master : masterParameters) {
                CodeTemplateParameterImpl masterImpl = CodeTemplateParameterImpl.get(master);
                if (CodeTemplateParameter.CURSOR_PARAMETER_NAME.equals(master.getName())) {
                    caretTextRegion = masterImpl.textRegion();
                    completionInvoke = master.getHints().get(CodeTemplateParameter.COMPLETION_INVOKE_HINT_NAME) != null;
                }
                textSyncGroup.addTextSync(masterImpl.textRegion().textSync());
            }
            
            if (caretTextRegion == null) { // no specific ${cursor} parameter
                Position caretFixedPos = TextRegion.createFixedPosition(completeInsertString.length());
                TextSync caretTextSync = new TextSync(new TextRegion(caretFixedPos, caretFixedPos));
                caretTextSync.setCaretMarker(true);
                textSyncGroup.addTextSync(caretTextSync);
            }
            
            textRegionManager().addTextSyncGroup(textSyncGroup, insertOffset);
            
            if (bdoc != null) {
                formatter.reformat(bdoc, insertOffset,
                        insertOffset + completeInsertString.length());
            }
            
        } catch (BadLocationException e) {
            LOG.log(Level.WARNING, "Invalid offset", e); // NOI18N
        } finally {
            // Mark inserted
            this.inserted = true;
            resetCachedInsertText();
        }
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("CodeTemplateInsertHandler.insertTemplate()\n"); // NOI18N
            LOG.fine(toString());
            if (LOG.isLoggable(Level.FINER)) {
                LOG.finer(textRegionManager().toString() + "\n");
            }
        }
    }
    
    private TextRegionEditing textRegionEditing() {
        return TextRegionEditing.get(component);
    }
    
    private TextRegionManager textRegionManager() {
        return TextRegionManager.get(component.getDocument());
    }
    
    public String getDocParameterValue(CodeTemplateParameterImpl paramImpl) {
        TextRegion textRegion = paramImpl.textRegion();
        int offset = textRegion.startOffset();
        int len = textRegion.endOffset() - offset;
        String parameterText;
        try {
            parameterText = component.getDocument().getText(offset, len);
        } catch (BadLocationException e) {
            LOG.log(Level.WARNING, "Invalid offset", e); // NOI18N
            parameterText = ""; //NOI18N
        }
        return parameterText;
    }
    
    public void setDocMasterParameterValue(CodeTemplateParameterImpl paramImpl, String newValue) {
        assert (!paramImpl.isSlave()); // assert master parameter
        TextRegion textRegion = paramImpl.textRegion();
        int offset = textRegion.startOffset();
        int length = textRegion.endOffset() - offset;
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("CodeTemplateInsertHandler.setMasterParameterValue(): parameter-name=" + paramImpl.getName() + // NOI18N
                    ", offset=" + offset + // NOI18N
                    ", length=" + length + ", newValue=\"" + newValue + "\"\n"); // NOI18N
        }
        try {
            Document doc = component.getDocument();
            CharSequence parameterText = DocumentUtilities.getText(doc, offset, length);
            if (!CharSequenceUtilities.textEquals(parameterText, newValue)) {
                textRegion.textSync().setText(newValue);
                notifyParameterUpdate(paramImpl.getParameter(), false);
            }
        } catch (BadLocationException e) {
            LOG.log(Level.WARNING, "Invalid offset", e); // NOI18N
        }
    }
    
    private void notifyParameterUpdate(CodeTemplateParameter parameter, boolean typingChange) {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine(super.toString() + "notifyParameterUpdate() CALLED for " + parameter.getName() + "\n"); // NOI18N
            LOG.fine(toString());
            if (LOG.isLoggable(Level.FINER)) {
                LOG.finer(textRegionManager().toString() + "\n");
            }
        }
        // Notify all processors about parameter's change
        for (CodeTemplateProcessor processor : processors) {
            processor.parameterValueChanged(parameter, typingChange);
        }
    }
    
    private void parseParametrizedText() {
        allParameters = new ArrayList<CodeTemplateParameter>(2);
        masterParameters = new ArrayList<CodeTemplateParameter>(2);
        parametrizedTextParser = new ParametrizedTextParser(this, parametrizedText);
        parametrizedTextParser.parse();
    }
    
    void notifyParameterParsed(CodeTemplateParameterImpl paramImpl) {
        allParameters.add(paramImpl.getParameter());
        // Check whether a corresponding master parameter already exists
        for (CodeTemplateParameter master : masterParameters) {
            if (master.getName().equals(paramImpl.getName())) {
                paramImpl.markSlave(master);
                CodeTemplateParameterImpl masterImpl = CodeTemplateParameterImpl.get(master);
                TextSync textSync = masterImpl.textRegion().textSync();
                textSync.addRegion(paramImpl.textRegion());
                return;
            }
        }
        // Make it master
        masterParameters.add(paramImpl.getParameter());
        TextSync textSync = new TextSync(paramImpl.textRegion());
        if (paramImpl.isEditable())
            textSync.setEditable(true);
        if (CodeTemplateParameter.CURSOR_PARAMETER_NAME.equals(paramImpl.getName()))
            textSync.setCaretMarker(true);
    }
    
    void release() {
        synchronized (this) {
            if (released) {
                return;
            }
            this.released = true;
        }

        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine(super.toString() + "release() CALLED\n");
            LOG.fine(toString());
        }
        if (outerHandler != null) {
            CodeTemplateParameterImpl activeMasterImpl = outerHandler.lastActiveMasterImpl();
            TextSync textSync = activeMasterImpl.textRegion().textSync();
            textSync.syncByMaster();
            activeMasterImpl.markUserModified();
            outerHandler.notifyParameterUpdate(activeMasterImpl.getParameter(), true);
        }
        Document doc = component.getDocument();
        doc.putProperty(CT_HANDLER_DOC_PROPERTY, outerHandler);

        // Notify processors
        for (CodeTemplateProcessor processor : processors) {
            processor.release();
        }
        textRegionEditing().stopGroupEditing(textSyncGroup);
        textRegionManager().removeTextSyncGroup(textSyncGroup);
    }

    private String buildInsertText() {
        return parametrizedTextParser.buildInsertText(allParameters);
    }

    public void deactivated(TextRegionEditing textRegionEditing, TextSync lastActiveTextSync) {
        if (lastActiveTextSync != null) {
            lastActiveMasterImpl = lastActiveTextSync.<CodeTemplateParameterImpl>masterRegion().clientInfo();
        }
    }

    public void released(TextRegionEditing textRegionEditing) {
        release();
        checkInvokeCompletion();
    }

    public void textSyncActivated(TextRegionEditing textRegionEditing, int origTextSyncIndex) {
    }

    public void textSyncModified(TextRegionEditing textRegionEditing) {
        TextSync activeTextSync = textRegionEditing.activeTextSync();
        CodeTemplateParameterImpl activeMasterImpl = activeTextSync.<CodeTemplateParameterImpl>masterRegion().clientInfo();
        if (activeMasterImpl != null) {
            activeMasterImpl.markUserModified();
            notifyParameterUpdate(activeMasterImpl.getParameter(), true);
        }
    }
    
    private void checkInvokeCompletion() {
        if (completionInvoke) {
            completionInvoke = false;
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    Completion.get().showCompletion();
                }
            });
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (CodeTemplateParameter param : allParameters) {
            CodeTemplateParameterImpl paramImpl = CodeTemplateParameterImpl.get(param);
            sb.append("  ").append(paramImpl.getName()).append(":");
            sb.append(paramImpl.textRegion());
            if (!paramImpl.isSlave()) {
                sb.append(" Master");
            }
            sb.append('\n');
        }
        return sb.toString();
    }

    private static final class TemplateInsertUndoEdit extends AbstractUndoableEdit {
        
        private Document doc;
        
        TemplateInsertUndoEdit(Document doc) {
            assert (doc != null);
            this.doc = doc;
        }

        @Override
        public void undo() throws CannotUndoException {
            super.undo();
            CodeTemplateInsertHandler handler = (CodeTemplateInsertHandler) doc.getProperty(CT_HANDLER_DOC_PROPERTY);
            if (handler != null) {
                handler.release();
            }
        }

    }

}
