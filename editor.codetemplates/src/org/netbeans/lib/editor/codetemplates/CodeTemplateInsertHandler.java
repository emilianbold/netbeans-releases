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

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.AttributesUtilities;
import org.netbeans.api.editor.settings.EditorStyleConstants;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.Formatter;
import org.netbeans.editor.Utilities;
import org.netbeans.lib.editor.codetemplates.api.CodeTemplate;
import org.netbeans.lib.editor.codetemplates.spi.CodeTemplateInsertRequest;
import org.netbeans.lib.editor.codetemplates.spi.CodeTemplateParameter;
import org.netbeans.lib.editor.codetemplates.spi.CodeTemplateProcessor;
import org.netbeans.lib.editor.codetemplates.spi.CodeTemplateProcessorFactory;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.lib.editor.util.CharacterConversions;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.lib.editor.util.swing.MutablePositionRegion;
import org.netbeans.lib.editor.util.swing.PositionRegion;
import org.netbeans.spi.editor.highlighting.HighlightsLayer;
import org.netbeans.spi.editor.highlighting.HighlightsLayerFactory;
import org.netbeans.spi.editor.highlighting.ZOrder;
import org.netbeans.spi.editor.highlighting.support.OffsetsBag;
import org.openide.ErrorManager;

/**
 * Code template allows the client to paste itself into the given
 * text component.
 *
 * @author Miloslav Metelka
 */
public final class CodeTemplateInsertHandler
implements DocumentListener, KeyListener {
    
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
    
    private List<CodeTemplateParameter> allParametersUnmodifiable;
    
    private List<CodeTemplateParameter> masters;
    
    private List<CodeTemplateParameter> mastersUnmodifiable;
    
    private List<CodeTemplateParameter> editableMasters;
    
    private CodeTemplateInsertRequest request;
    
    private boolean inserted;
    
    private boolean released;
    
    private boolean suspended;
    
    private Position caretPosition;
    
    private boolean completionInvoke;
    
    private int activeMasterIndex;
    
    private ActionMap componentOrigActionMap;
    
    private Document doc;

    private MutablePositionRegion positionRegion;
    
    /**
     * When expanding of a template was requested when still editing parameters
     * of an outer template remember the outer template's handler
     */
    private CodeTemplateInsertHandler outerHandler;
    
    /**
     * Parameter implementation for which the value is being explicitly
     * set through the API. It prevents the release() call due to not editing
     * of the active master parameter.
     */
    private CodeTemplateParameterImpl apiSetValueParamImpl;

    /**
     * Used to check whether the just performed modification affected active region.
     */
    private int lastActiveRegionStartOffset;
    
    /**
     * Used to check whether the just performed modification affected active region.
     */
    private int lastActiveRegionEndOffset;
    
    /**
     * Determines whether the present active parameter's change
     * should be fired upon the active parameter change.
     */
    private boolean activeMasterModified;
    
    /**
     * Whether currently synchronizing the document changes.
     */
    private boolean syncingDocModification;

    private AttributeSet attribs = null;
    private AttributeSet attribsLeft = null;
    private AttributeSet attribsRight = null;
    private AttributeSet attribsMiddle = null;
    private AttributeSet attribsAll = null;
    
    public CodeTemplateInsertHandler(
        CodeTemplate codeTemplate,
        JTextComponent component, 
        Collection<? extends CodeTemplateProcessorFactory> processorFactories
    ) {
        this.codeTemplate = codeTemplate;
        this.component = component;

        Position zeroPos = PositionRegion.createFixedPosition(0);
        this.positionRegion = new MutablePositionRegion(zeroPos, zeroPos);
        this.request = CodeTemplateSpiPackageAccessor.get().createInsertRequest(this);

        processors = new ArrayList<CodeTemplateProcessor>();
        for (CodeTemplateProcessorFactory factory : processorFactories) {
            processors.add(factory.createProcessor(this.request));
        }

        setParametrizedText(codeTemplate.getParametrizedText());
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
    
    private synchronized void markInserted() {
        this.inserted = true;
        resetCachedInsertText();
    }
    
    public synchronized boolean isReleased() {
        return released;
    }
    
    public String getParametrizedText() {
        return parametrizedText;
    }
    
    public void setParametrizedText(String parametrizedText) {
        this.parametrizedText = CharacterConversions.lineSeparatorToLineFeed(parametrizedText);
        parseParametrizedText();
    }

    public int getInsertOffset() {
        if (allParameters.isEmpty())
            return positionRegion.getStartOffset();
        return Math.min(paramImpl(allParameters.get(0)).getPositionRegion().getStartOffset(), positionRegion.getStartOffset());
    }

    public String getInsertText() {
        if (inserted) {
            try {
                int startOffset = getInsertOffset();
                return doc.getText(startOffset, positionRegion.getEndOffset() - startOffset);
            } catch (BadLocationException e) {
                ErrorManager.getDefault().notify(e);
                return "";
            }

        } else { // not inserted yet
            checkInsertTextBuilt();
            return insertText;
        }
    }
    
    public List<? extends CodeTemplateParameter> getAllParameters() {
        return allParametersUnmodifiable;
    }

    public List<? extends CodeTemplateParameter> getMasterParameters() {
        return mastersUnmodifiable;
    }
    
    public void processTemplate() {
        // Update default values by all processors
        for (CodeTemplateProcessor processor : processors) {
            processor.updateDefaultValues();
        }

        // Insert the template into document
        insertTemplate();

        // Install overriding actions for template post-processing
        installActions();
    }

    void checkInsertTextBuilt() {
        if (insertText == null) {
            insertText = buildInsertText();
        }
    }
    
    void resetCachedInsertText() {
        insertText = null;
    }
    
    CodeTemplateParameter getActiveMaster() {
        return (!suspended && activeMasterIndex < editableMasters.size())
            ? editableMasters.get(activeMasterIndex)
            : null;
    }
    
    CodeTemplateParameterImpl getActiveMasterImpl() {
        CodeTemplateParameter master = getActiveMaster();
        return (master != null) ? paramImpl(master) : null;
    }
    
    public void insertTemplate() {
        doc = component.getDocument();
        outerHandler = (CodeTemplateInsertHandler)doc.getProperty(CT_HANDLER_DOC_PROPERTY);
        doc.putProperty(CT_HANDLER_DOC_PROPERTY, this);
        
        String completeInsertString = getInsertText();

        BaseDocument bdoc = (doc instanceof BaseDocument)
                ? (BaseDocument)doc
                : null;
        // Need to lock formatter first because CT's multiline text will be reformatted
        Formatter formatter = null;
        if (bdoc != null) {
             formatter = bdoc.getFormatter();
            if (formatter != null) {
                formatter.reformatLock();
            }
            ((BaseDocument)doc).atomicLock();
        }
        try {
            // First check if there is a caret selection and if so remove it
            Caret caret = component.getCaret();
            if (Utilities.isSelectionShowing(caret)) {
                int removeOffset = component.getSelectionStart();
                int removeLength = component.getSelectionEnd() - removeOffset;
                doc.remove(removeOffset, removeLength);
            }

            // insert the complete text
            int insertOffset = component.getCaretPosition();
            int docLen = doc.getLength();
            doc.insertString(insertOffset, completeInsertString, null);
            
            // Go through all master parameters and create region infos for them
            for (Iterator it = request.getMasterParameters().iterator(); it.hasNext();) {
                CodeTemplateParameter parameter = (CodeTemplateParameter)it.next();

                if (CodeTemplateParameter.CURSOR_PARAMETER_NAME.equals(parameter.getName())) {
                    caretPosition = doc.createPosition(insertOffset + parameter.getInsertTextOffset());
                    CodeTemplateParameterImpl paramImpl = CodeTemplateParameterImpl.get(parameter);
                    paramImpl.resetPositions(caretPosition, caretPosition);
                    completionInvoke = parameter.getHints().get(CodeTemplateParameter.COMPLETION_INVOKE_HINT_NAME) != null;
                } else { // Not a CURSOR parameter
                    List<MutablePositionRegion> parameterRegions = new ArrayList<MutablePositionRegion>(4);
                    addParameterRegion(parameterRegions, parameter, doc, insertOffset);
                    for (Iterator slaveIt = parameter.getSlaves().iterator(); slaveIt.hasNext();) {
                        CodeTemplateParameter slaveParameter = (CodeTemplateParameter)slaveIt.next();
                        addParameterRegion(parameterRegions, slaveParameter, doc, insertOffset);
                    }
                    
                    SyncDocumentRegion region = new SyncDocumentRegion(doc, parameterRegions);
                    paramImpl(parameter).setRegion(region);
                }
            }
            
            positionRegion.reset(doc.createPosition(insertOffset),
                    doc.createPosition(insertOffset + (doc.getLength() - docLen)));

            if (caretPosition == null) { // no specific ${cursor} parameter
                caretPosition = doc.createPosition(insertOffset + completeInsertString.length());
            }
            
            if (doc instanceof BaseDocument) {
                formatter.reformat(bdoc, insertOffset,
                        insertOffset + completeInsertString.length());
            }
            
        } catch (BadLocationException e) {
            ErrorManager.getDefault().notify(e);
        } finally {
            if (bdoc != null) {
                bdoc.atomicUnlock();
                if (formatter != null) {
                    formatter.reformatUnlock();
                }
            }
            
            markInserted();
        }
    }
    
    public void installActions() {
        if (editableMasters.size() > 0) {
            if (outerHandler != null)
                outerHandler.suspended = true;
            
            // Install the post modification document listener to sync regions
            if (doc instanceof BaseDocument) {
                ((BaseDocument)doc).setPostModificationDocumentListener(this);
                updateLastRegionBounds();
            }

            componentOrigActionMap = CodeTemplateOverrideAction.installOverrideActionMap(
                    component, this);

            component.addKeyListener(this);
            tabUpdate();

        } else {
            // For nested template expanding or when without parameters
            // just update the caret position and release
            forceCaretPosition();
            release();
            if (completionInvoke) {
                //Temporary ugly solution
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                Completion.get().showCompletion();
                            }
                        });
                    }
                });
            }
        }
    }
    
    public void defaultKeyTypedAction(ActionEvent evt, Action origAction) {
        origAction.actionPerformed(evt);
    }
    
    public void tabAction(ActionEvent evt, Action origAction) {
        checkNotifyParameterUpdate();
        if (!isManagedInsert(component.getCaretPosition())) {
            origAction.actionPerformed(evt);
        } else if (editableMasters.size() > 1) {
            activeMasterIndex++;
            activeMasterIndex %= editableMasters.size();
            tabUpdate();
        }
    }
    
    public void shiftTabAction(ActionEvent evt) {
        checkNotifyParameterUpdate();

        if (editableMasters.size() > 1) {
            if (activeMasterIndex-- == 0)
                activeMasterIndex = editableMasters.size() - 1;
            tabUpdate();
        }
    }
    
    public void enterAction(ActionEvent evt) {
        checkNotifyParameterUpdate();

        activeMasterIndex++;
        if (activeMasterIndex >= editableMasters.size()) { 
            forceCaretPosition();
            release();
            if (completionInvoke) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        Completion.get().showCompletion();
                    }
                });
            }
        } else {
            tabUpdate();
        }
    }
    
    void undoAction(ActionEvent evt) {
        // Disable undo until release
    }
    
    void redoAction(ActionEvent evt) {
        // Disable redo until release
    }
    
    public String getDocParameterValue(CodeTemplateParameterImpl paramImpl) {
        MutablePositionRegion preg = paramImpl.getPositionRegion();
        int offset = preg.getStartOffset();
        String parameterText;
        try {
            parameterText = doc.getText(offset, preg.getEndOffset() - offset);
        } catch (BadLocationException e) {
            ErrorManager.getDefault().notify(e);
            parameterText = ""; //NOI18N
        }
        return parameterText;
    }
    
    public void setDocParameterValue(CodeTemplateParameterImpl paramImpl, String newValue) {
        assert (paramImpl != getActiveMasterImpl()); // active master should not be modified
        assert (!paramImpl.isSlave()); // assert master parameter
        SyncDocumentRegion region = paramImpl.getRegion();
        assert (region != null);
        int offset = region.getFirstRegionStartOffset();
        int length = region.getFirstRegionLength();
        apiSetValueParamImpl = paramImpl;
        try {
            CharSequence parameterText = DocumentUtilities.getText(doc, offset, length);
            if (!CharSequenceUtilities.textEquals(parameterText, newValue)) {
                doc.remove(offset, length);
                doc.insertString(offset, newValue, null);
                // Explicitly synchronize the other regions with the first
                region.sync(newValue.length());
                
                paramImpl.setValue(newValue, false);
            }
        } catch (BadLocationException e) {
            ErrorManager.getDefault().notify(e);
        } finally {
            apiSetValueParamImpl = null;
        }
    }
    
    public void insertUpdate(DocumentEvent evt) {
        int offset = evt.getOffset();
        int insertLength = evt.getLength();
        if (offset + insertLength == caretPosition.getOffset()) { // move the caret back
            try {
                caretPosition = doc.createPosition(offset);
            } catch (BadLocationException e) {
                ErrorManager.getDefault().notify(e);
            }
        }
        
        if (!syncingDocModification) {
            syncingDocModification = true;
            syncInsert(evt);
            syncingDocModification = false;
        }
        
        requestRepaint();
    }
    
    public void removeUpdate(DocumentEvent evt) {
        if (!syncingDocModification) {
            syncingDocModification = true;
            syncRemove(evt);
            syncingDocModification = false;
        }
        
        requestRepaint();
    }
    
    public void changedUpdate(DocumentEvent evt) {
    }
    
    public void keyPressed(KeyEvent e) {
        if (suspended)
            return;
        if (KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0).equals(KeyStroke.getKeyStrokeForEvent(e))) {
            release();
            e.consume();
        } else if (KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0).equals(KeyStroke.getKeyStrokeForEvent(e))) {
            if (getActiveMaster() == null || !isManagedInsert(component.getCaretPosition())) {
                checkNotifyParameterUpdate();
                release();
            }
        }
    }

    public void keyReleased(KeyEvent e) {
    }
    
    public void keyTyped(KeyEvent e) {
    }
    
    private void forceCaretPosition() {
        component.setCaretPosition(caretPosition.getOffset());
    }
    
    private void notifyParameterUpdate(CodeTemplateParameter parameter, boolean typingChange) {
        // Notify all processors about parameter's change
        for (CodeTemplateProcessor processor : processors) {
            processor.parameterValueChanged(parameter, typingChange);
        }
    }
    
    private void checkNotifyParameterUpdate() {
        if (activeMasterModified) {
            activeMasterModified = false;
            notifyParameterUpdate(getActiveMaster(), false);
        }
    }
    
    private void parseParametrizedText() {
        allParameters = new ArrayList<CodeTemplateParameter>(2);
        allParametersUnmodifiable = Collections.unmodifiableList(allParameters);
        masters = new ArrayList<CodeTemplateParameter>(2);
        mastersUnmodifiable = Collections.unmodifiableList(masters);
        editableMasters = new ArrayList<CodeTemplateParameter>(2);
        parametrizedTextParser = new ParametrizedTextParser(this, parametrizedText);
        parametrizedTextParser.parse();
    }
    
    void notifyParameterParsed(CodeTemplateParameterImpl paramImpl) {
        allParameters.add(paramImpl.getParameter());
        checkSlave(paramImpl.getParameter());
    }
        

    /**
     * Check whether the given parameter is slave of an existing
     * master parameter.
     * <br/>
     * If so the parameter is turned into the slave of its master
     * otherwise it's added to the list of masters.
     */ 
    private void checkSlave(CodeTemplateParameter parameter) {
        for (Iterator it = getMasterParameters().iterator(); it.hasNext();) {
            CodeTemplateParameter master = (CodeTemplateParameter)it.next();
            if (master.getName().equals(parameter.getName())) {
                paramImpl(parameter).markSlave(master);
                return;
            }
        }
        // Make it master
        masters.add(parameter);
        if (parameter.isEditable()) {
            editableMasters.add(parameter);
        }
    }
    
    private static CodeTemplateParameterImpl paramImpl(CodeTemplateParameter param) {
        return CodeTemplateSpiPackageAccessor.get().getImpl(param);
    }
    
    private void tabUpdate() {
        updateLastRegionBounds();
        CodeTemplateParameterImpl activeMasterImpl = getActiveMasterImpl();
        if (activeMasterImpl != null) {
            SyncDocumentRegion active = activeMasterImpl.getRegion();
            component.select(active.getFirstRegionStartOffset(),
                    active.getFirstRegionEndOffset());
        }
        // Repaint the selected blocks according to the current master
        requestRepaint();
    }
    
    private void requestRepaint() {
        if (released) {
            OffsetsBag bag = getBag(doc);
            bag.clear();
            attribs = null;
        } else {
            CodeTemplateParameterImpl ctpi = getActiveMasterImpl();
            if (ctpi != null) {
                // Compute attributes
                if (attribs == null) {
                    attribs = getSyncedTextBlocksHighlight();
                    Color foreground = (Color) attribs.getAttribute(StyleConstants.Foreground);
                    Color background = (Color) attribs.getAttribute(StyleConstants.Background);
                    attribsLeft = AttributesUtilities.createImmutable(
                            StyleConstants.Background, background,
                            EditorStyleConstants.LeftBorderLineColor, foreground, 
                            EditorStyleConstants.TopBorderLineColor, foreground, 
                            EditorStyleConstants.BottomBorderLineColor, foreground
                    );
                    attribsRight = AttributesUtilities.createImmutable(
                            StyleConstants.Background, background,
                            EditorStyleConstants.RightBorderLineColor, foreground, 
                            EditorStyleConstants.TopBorderLineColor, foreground, 
                            EditorStyleConstants.BottomBorderLineColor, foreground
                    );
                    attribsMiddle = AttributesUtilities.createImmutable(
                            StyleConstants.Background, background,
                            EditorStyleConstants.TopBorderLineColor, foreground, 
                            EditorStyleConstants.BottomBorderLineColor, foreground
                    );
                    attribsAll = AttributesUtilities.createImmutable(
                            StyleConstants.Background, background,
                            EditorStyleConstants.LeftBorderLineColor, foreground, 
                            EditorStyleConstants.RightBorderLineColor, foreground,
                            EditorStyleConstants.TopBorderLineColor, foreground, 
                            EditorStyleConstants.BottomBorderLineColor, foreground
                    );
                }
                
                OffsetsBag nue = new OffsetsBag(doc);
                PositionRegion region = ctpi.getPositionRegion();
                int size = region.getEndOffset() - region.getStartOffset();
                if (size == 1) {
                    nue.addHighlight(region.getStartOffset(), region.getEndOffset(), attribsAll);
                } else if (size > 1) {
                    nue.addHighlight(region.getStartOffset(), region.getStartOffset() + 1, attribsLeft);
                    nue.addHighlight(region.getEndOffset() - 1, region.getEndOffset(), attribsRight);
                    if (size > 2) {
                        nue.addHighlight(region.getStartOffset() + 1, region.getEndOffset() - 1, attribsMiddle);
                    }
                }

                OffsetsBag bag = getBag(doc);
                bag.setHighlights(nue);
            }
        }
    }

    private void release() {
        synchronized (this) {
            if (released) {
                return;
            }
            this.released = true;
        }

        if (editableMasters.size() > 0) {
            if (doc instanceof BaseDocument) {
                ((BaseDocument)doc).setPostModificationDocumentListener(null);
            }

            component.removeKeyListener(this);

            // Restore original action map
            JTextComponent c = getComponent();
            c.setActionMap(componentOrigActionMap);

            if (outerHandler != null) {
                outerHandler.suspended = false;
                if (doc instanceof BaseDocument)
                    ((BaseDocument)doc).setPostModificationDocumentListener(outerHandler);
                CodeTemplateParameterImpl activeMasterImpl = outerHandler.getActiveMasterImpl();
                doc.putProperty("abbrev-ignore-modification", Boolean.TRUE); // NOI18N
                try {
                    activeMasterImpl.getRegion().sync(0);
                } finally {
                    doc.putProperty("abbrev-ignore-modification", Boolean.FALSE); // NOI18N
                }
                activeMasterImpl.setValue(outerHandler.getDocParameterValue(activeMasterImpl), false);
                activeMasterImpl.markUserModified();
                outerHandler.notifyParameterUpdate(activeMasterImpl.getParameter(), true);
                outerHandler.updateLastRegionBounds();
                outerHandler.requestRepaint();
            } else {
                requestRepaint();
            }
        }
        doc.putProperty(CT_HANDLER_DOC_PROPERTY, outerHandler);

        // Notify processors
        for (CodeTemplateProcessor processor : processors) {
            processor.release();
        }
        
    }

    void syncInsert(DocumentEvent evt) {
        // Ensure that the caret position will stay logically where it is
        int offset = evt.getOffset();
        int insertLength = evt.getLength();

        CodeTemplateParameterImpl activeMasterImpl = getActiveMasterImpl();
        if (apiSetValueParamImpl == null // not setting value through API (explicit sync would be done)
            && activeMasterImpl != null
        ) {
            SyncDocumentRegion region = activeMasterImpl.getRegion();
            if (isManagedInsert(offset)) {
                doc.putProperty("abbrev-ignore-modification", Boolean.TRUE); // NOI18N
                try {
                    region.sync((offset == lastActiveRegionStartOffset) ? insertLength : 0);
                } finally {
                    doc.putProperty("abbrev-ignore-modification", Boolean.FALSE); // NOI18N
                }
                activeMasterImpl.setValue(getDocParameterValue(activeMasterImpl), false);
                activeMasterImpl.markUserModified();
                notifyParameterUpdate(activeMasterImpl.getParameter(), true);
            } else { // the insert is not managed => release
                if (DocumentUtilities.isTypingModification(evt))
                    release();
            }
        }
        updateLastRegionBounds();
    }
    
    void syncRemove(DocumentEvent evt) {
        CodeTemplateParameterImpl activeMasterImpl = getActiveMasterImpl();
        if (apiSetValueParamImpl == null // not setting value through API (explicit sync would be done)
            && activeMasterImpl != null
        ) {
            SyncDocumentRegion region = activeMasterImpl.getRegion();
            if (isManagedRemove(evt.getOffset(), evt.getLength())) {
                doc.putProperty("abbrev-ignore-modification", Boolean.TRUE); // NOI18N
                try {
                    region.sync(0);
                } finally {
                    doc.putProperty("abbrev-ignore-modification", Boolean.FALSE); // NOI18N
                }
                activeMasterImpl.setValue(getDocParameterValue(activeMasterImpl), false);
                activeMasterImpl.markUserModified();
                if (doc.getProperty(BaseKit.DOC_REPLACE_SELECTION_PROPERTY) == null)
                    notifyParameterUpdate(activeMasterImpl.getParameter(), true);
            } else { // the insert is not managed => release
                if (DocumentUtilities.isTypingModification(evt) || 
                        evt.getLength() >= evt.getDocument().getLength()) //HACK! - see issue #128600
                    release();
            }
        }
        updateLastRegionBounds();
    }
    
    private void updateLastRegionBounds() {
        CodeTemplateParameterImpl masterImpl = getActiveMasterImpl();
        if (masterImpl != null) {
            SyncDocumentRegion region = masterImpl.getRegion();
            lastActiveRegionStartOffset = region.getFirstRegionStartOffset();
            lastActiveRegionEndOffset = region.getFirstRegionEndOffset();
        } else {
            lastActiveRegionStartOffset = -1;
            lastActiveRegionEndOffset = -1;
        }
    }
    
    private boolean isManagedInsert(int offset) {
        return (offset >= lastActiveRegionStartOffset
            && offset <= lastActiveRegionEndOffset);
    }
    
    private boolean isManagedRemove(int offset, int length) {
        return (offset >= lastActiveRegionStartOffset
            && offset + length <= lastActiveRegionEndOffset);
    }
    
    private void addParameterRegion(List<MutablePositionRegion> parameterRegions, CodeTemplateParameter parameter,
    Document doc, int insertOffset) throws BadLocationException {
        int startOffset = insertOffset + parameter.getInsertTextOffset();
        BaseDocument bdoc = (BaseDocument)doc;
        Position startPos = bdoc.createPosition(startOffset);
        Position endPos = doc.createPosition(startOffset + parameter.getValue().length());
        CodeTemplateParameterImpl paramImpl = CodeTemplateParameterImpl.get(parameter);
        paramImpl.resetPositions(startPos, endPos);
        parameterRegions.add(paramImpl.getPositionRegion());
    }

    private String buildInsertText() {
        return parametrizedTextParser.buildInsertText(allParameters);
    }

    private static synchronized OffsetsBag getBag(Document document) {
        String propName = CodeTemplateInsertHandler.class.getName() + "-OffsetsBag"; //NOI18N
        OffsetsBag bag = (OffsetsBag) document.getProperty(propName);
        if (bag == null) {
            bag = new OffsetsBag(document);
            document.putProperty(propName, bag);
        }
        return bag;
    }

    private static AttributeSet getSyncedTextBlocksHighlight() {
        FontColorSettings fcs = MimeLookup.getLookup(MimePath.EMPTY).lookup(FontColorSettings.class);
        AttributeSet as = fcs.getFontColors("synchronized-text-blocks-ext"); //NOI18N
        return as == null ? SimpleAttributeSet.EMPTY : as;
    }
    
    public static final class HLFactory implements HighlightsLayerFactory {
        public HighlightsLayer[] createLayers(Context context) {
            return new HighlightsLayer[] {
                HighlightsLayer.create(
                    "org.netbeans.lib.editor.codetemplates.CodeTemplateParametersHighlights", //NOI18N
                    ZOrder.SHOW_OFF_RACK.forPosition(490), 
                    true, 
                    getBag(context.getDocument())
                )
            };
        }
    } // End of HLFactory class
}
