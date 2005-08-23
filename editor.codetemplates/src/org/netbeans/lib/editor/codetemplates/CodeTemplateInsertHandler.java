/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.lib.editor.codetemplates;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.DrawLayer;
import org.netbeans.editor.EditorUI;
import org.netbeans.editor.Formatter;
import org.netbeans.editor.Utilities;
import org.netbeans.lib.editor.codetemplates.api.CodeTemplate;
import org.netbeans.lib.editor.codetemplates.spi.CodeTemplateInsertRequest;
import org.netbeans.lib.editor.codetemplates.spi.CodeTemplateParameter;
import org.netbeans.lib.editor.codetemplates.spi.CodeTemplateProcessor;
import org.netbeans.lib.editor.codetemplates.spi.CodeTemplateProcessorFactory;
import org.netbeans.lib.editor.util.swing.MutablePositionRegion;
import org.netbeans.lib.editor.util.swing.PositionRegion;
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
     * Maximum number of notifications about the parameter value changes
     * to the processors to prevent infinite loops caused by changing
     * parameter's value in reaction to other's parameter change
     * in a cycle.
     * <br/>
     * After reaching this count no more notifications will be done.
     */
    private static final int MAX_PARAMETER_CHANGE_NOTIFICATION_COUNT = 10000;
    
    private final CodeTemplate codeTemplate;
    
    private final JTextComponent component;
    
    private final List/*<CodeTemplateProcessor>*/ processors;
    
    private String parametrizedText;
    
    private ParametrizedTextParser parametrizedTextParser;

    private String insertText;
    
    private List allParameters;
    
    private List allParametersUnmodifiable;
    
    private List masters;
    
    private List mastersUnmodifiable;
    
    private List editableMasters;
    
    private CodeTemplateInsertRequest request;
    
    private boolean inserted;
    
    private boolean released;
    
    private Position caretPosition;
    
    private int activeMasterIndex;
    
    private ActionMap componentOrigActionMap;
    
    private List/*<DrawLayer>*/ drawLayers;
    
    private Document doc;
    
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
    
    public CodeTemplateInsertHandler(CodeTemplate codeTemplate,
    JTextComponent component, Collection/*<CodeTemplateProcessorFactory>*/ processorFactories) {
        this.codeTemplate = codeTemplate;
        this.component = component;
        
        // Ensure that the SPI package accessor gets registered
        CodeTemplateInsertRequest.class.getClass().getName();

        this.request = CodeTemplateSpiPackageAccessor.get().
                createInsertRequest(this);

        processors = new ArrayList();
        for (Iterator it = processorFactories.iterator(); it.hasNext();) {
            CodeTemplateProcessorFactory factory = (CodeTemplateProcessorFactory)it.next();
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
    }
    
    public synchronized boolean isReleased() {
        return released;
    }
    
    public String getParametrizedText() {
        return parametrizedText;
    }
    
    public void setParametrizedText(String parametrizedText) {
        this.parametrizedText = parametrizedText;
        parseParametrizedText();
    }

    public String getInsertText() {
        checkInsertTextBuilt();
        return insertText;
    }
    
    public List/*<CodeTemplateParameter>*/ getAllParameters() {
        return allParametersUnmodifiable;
    }

    public List/*<CodeTemplateParameter>*/ getMasterParameters() {
        return mastersUnmodifiable;
    }
    
    public void processTemplate() {
        // Update default values by all processors
        for (Iterator it = processors.iterator(); it.hasNext();) {
            CodeTemplateProcessor processor = (CodeTemplateProcessor)it.next();
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
        return (activeMasterIndex < editableMasters.size())
            ? (CodeTemplateParameter)editableMasters.get(activeMasterIndex)
            : null;
    }
    
    CodeTemplateParameterImpl getActiveMasterImpl() {
        CodeTemplateParameter master = getActiveMaster();
        return (master != null) ? paramImpl(master) : null;
    }
    
    public void insertTemplate() {
        doc = component.getDocument();
        
        String completeInsertString = getInsertText();

        if (doc instanceof BaseDocument) {
            ((BaseDocument)doc).atomicLock();
        }
        try {
            // First check if there is a caret selection and if so remove it
            Caret caret = component.getCaret();
            if (caret.isSelectionVisible()) {
                int removeOffset = component.getSelectionStart();
                int removeLength = component.getSelectionEnd() - removeOffset;
                doc.remove(removeOffset, removeLength);
            }

            // insert the complete text
            int insertOffset = component.getCaretPosition();
            doc.insertString(insertOffset, completeInsertString, null);
            
            // Go through all master parameters and create region infos for them
            for (Iterator it = request.getMasterParameters().iterator(); it.hasNext();) {
                CodeTemplateParameter parameter = (CodeTemplateParameter)it.next();

                if (CodeTemplateParameter.CURSOR_PARAMETER_NAME.equals(parameter.getName())) {
                    caretPosition = doc.createPosition(insertOffset + parameter.getInsertTextOffset());
                } else {
                    List parameterRegions = new ArrayList(4);
                    addParameterRegion(parameterRegions, parameter, doc, insertOffset);
                    for (Iterator slaveIt = parameter.getSlaves().iterator(); slaveIt.hasNext();) {
                        CodeTemplateParameter slaveParameter = (CodeTemplateParameter)slaveIt.next();
                        addParameterRegion(parameterRegions, slaveParameter, doc, insertOffset);
                    }
                    
                    SyncDocumentRegion region = new SyncDocumentRegion(doc, parameterRegions);
                    paramImpl(parameter).setRegion(region);
                }
            }
            
            if (caretPosition == null) { // no specific ${cursor} parameter
                caretPosition = doc.createPosition(insertOffset + completeInsertString.length());
            }
            
            if (parametrizedText.indexOf('\n') != -1 && doc instanceof BaseDocument) {
                BaseDocument bdoc = (BaseDocument)doc;
                Formatter formatter = bdoc.getFormatter();
                if (formatter != null) {
                    formatter.reformat(bdoc, insertOffset,
                            insertOffset + completeInsertString.length());
                }
            }
            
            // Install the post modification document listener to sync regions
            if (doc instanceof BaseDocument) {
                ((BaseDocument)doc).setPostModificationDocumentListener(this);
                updateLastRegionBounds();
            }

        } catch (BadLocationException e) {
            ErrorManager.getDefault().notify(e);
        } finally {
            if (doc instanceof BaseDocument) {
                ((BaseDocument)doc).atomicUnlock();
            }
            
            markInserted();
        }
    }
    
    public void installActions() {
        if (editableMasters.size() > 0) {
            componentOrigActionMap = CodeTemplateOverrideAction.installOverrideActionMap(
                    component, this);

            EditorUI editorUI = Utilities.getEditorUI(component);
            drawLayers = new ArrayList(editableMasters.size());
            for (Iterator it = editableMasters.iterator(); it.hasNext();) {
                CodeTemplateParameterImpl paramImpl = paramImpl(((CodeTemplateParameter)it.next()));
                CodeTemplateDrawLayer drawLayer = new CodeTemplateDrawLayer(paramImpl);
                drawLayers.add(drawLayer);
                editorUI.addLayer(drawLayer, CodeTemplateDrawLayer.VISIBILITY);
            }
            
            component.addKeyListener(this);
        }

        tabUpdate();
    }
    
    public void defaultKeyTypedAction(ActionEvent evt, Action origAction) {
        origAction.actionPerformed(evt);
    }
    
    public void tabAction(ActionEvent evt, Action origAction) {
        checkNotifyParameterUpdate();

        if (activeMasterIndex < editableMasters.size()) {
            activeMasterIndex++;
            
        } else { // release
            activeMasterIndex = 0;
        }
        tabUpdate();
    }
    
    public void shiftTabAction(ActionEvent evt) {
        checkNotifyParameterUpdate();

        if (activeMasterIndex > 0) {
            activeMasterIndex--;
        } else {
            activeMasterIndex = editableMasters.size();
        }
        tabUpdate();
    }
    
    public void enterAction(ActionEvent evt) {
        checkNotifyParameterUpdate();

        getComponent().setCaretPosition(caretPosition.getOffset());
        release();
    }
    
    void undoAction(ActionEvent evt) {
        // Disable undo until release
    }
    
    void redoAction(ActionEvent evt) {
        // Disable redo until release
    }
    
    public String getDocParameterValue(CodeTemplateParameterImpl paramImpl) {
        assert (!paramImpl.isSlave()); // assert master parameter
        SyncDocumentRegion region = paramImpl.getRegion();
        assert (region != null);
        int offset = region.getFirstRegionStartOffset();
        int length = region.getFirstRegionLength();
        String parameterText;
        try {
            parameterText = doc.getText(offset, length);
        } catch (BadLocationException e) {
            ErrorManager.getDefault().notify(e);
            parameterText = null;
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
            String parameterText = doc.getText(offset, length);
            if (!parameterText.equals(newValue)) {
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
    }
    
    public void removeUpdate(DocumentEvent evt) {
        if (!syncingDocModification) {
            syncingDocModification = true;
            syncRemove(evt);
            syncingDocModification = false;
        }
    }
    
    public void changedUpdate(DocumentEvent evt) {
    }
    
    public void keyPressed(KeyEvent e) {
        if (KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0).equals(
                KeyStroke.getKeyStrokeForEvent(e))
        ) {
            release();
            e.consume();
        }
    }

    public void keyReleased(KeyEvent e) {
    }
    
    public void keyTyped(KeyEvent e) {
    }
    
    private void notifyParameterUpdate(CodeTemplateParameter parameter, boolean typingChange) {
        // Notify all processors about parameter's change
        for (Iterator it = processors.iterator(); it.hasNext();) {
            CodeTemplateProcessor processor = (CodeTemplateProcessor)it.next();
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
        allParameters = new ArrayList(2);
        allParametersUnmodifiable = Collections.unmodifiableList(allParameters);
        masters = new ArrayList(2);
        mastersUnmodifiable = Collections.unmodifiableList(masters);
        editableMasters = new ArrayList(2);
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
        if (activeMasterIndex == editableMasters.size()) {
            // Goto caret position
            component.setCaretPosition(caretPosition.getOffset());
        } else {
            updateLastRegionBounds();
            SyncDocumentRegion active = getActiveMasterImpl().getRegion();
            component.select(active.getFirstRegionStartOffset(),
                active.getFirstRegionEndOffset());
        }
        
        // Repaint the selected blocks according to the current master
        requestRepaint();
    }
    
    private void requestRepaint() {
        int startOffset = Integer.MAX_VALUE;
        int endOffset = 0;
        for (Iterator it = editableMasters.iterator(); it.hasNext();) {
            SyncDocumentRegion region = paramImpl(((CodeTemplateParameter)it.next())).getRegion();
            startOffset = Math.min(startOffset,
                    region.getSortedRegion(0).getStartOffset());
            endOffset = Math.max(endOffset, region.getSortedRegion(
                    region.getRegionCount() - 1).getEndOffset());
        }
        JTextComponent component = getComponent();
        if (endOffset != 0) {
            component.getUI().damageRange(component, startOffset, endOffset);
        }
    }

    public void release() {
        synchronized (this) {
            if (released) {
                return;
            }
            this.released = true;
        }
        if (doc instanceof BaseDocument) {
            ((BaseDocument)doc).setPostModificationDocumentListener(null);
        }

        if (editableMasters.size() > 0) {
            component.removeKeyListener(this);

            // Restore original action map
            JTextComponent component = getComponent();
            component.setActionMap(componentOrigActionMap);

            // Free the draw layers
            EditorUI editorUI = Utilities.getEditorUI(component);
            if (editorUI != null) {
                for (Iterator it = drawLayers.iterator(); it.hasNext();) {
                    DrawLayer drawLayer = (DrawLayer)it.next();
                    editorUI.removeLayer(drawLayer.getName());
                }
            }
            component.putClientProperty(DrawLayer.TEXT_FRAME_START_POSITION_COMPONENT_PROPERTY, null);
            component.putClientProperty(DrawLayer.TEXT_FRAME_END_POSITION_COMPONENT_PROPERTY, null);
        }

        // Notify processors
        for (Iterator it = processors.iterator(); it.hasNext();) {
            CodeTemplateProcessor processor = (CodeTemplateProcessor)it.next();
            processor.release();
        }
        
        requestRepaint();
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
                region.sync((offset == lastActiveRegionStartOffset) ? insertLength : 0);
                activeMasterImpl.setValue(getDocParameterValue(activeMasterImpl), false);
                activeMasterImpl.markUserModified();
                notifyParameterUpdate(activeMasterImpl.getParameter(), true);
            } else { // the insert is not managed => release
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
                region.sync(0);
                activeMasterImpl.setValue(getDocParameterValue(activeMasterImpl), false);
                activeMasterImpl.markUserModified();
                notifyParameterUpdate(activeMasterImpl.getParameter(), true);
            } else { // the insert is not managed => release
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
    
    private void addParameterRegion(List parameterRegions, CodeTemplateParameter parameter,
    Document doc, int insertOffset) throws BadLocationException {
        int startOffset = insertOffset + parameter.getInsertTextOffset();
        BaseDocument bdoc = (BaseDocument)doc;
        Position startPos = bdoc.createPosition(startOffset);
        Position endPos = doc.createPosition(startOffset + parameter.getValue().length());
        MutablePositionRegion region = new MutablePositionRegion(startPos, endPos);
        parameterRegions.add(region);
    }

    private String buildInsertText() {
        return parametrizedTextParser.buildInsertText(allParameters);
    }
    
}
