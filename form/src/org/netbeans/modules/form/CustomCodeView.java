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

package org.netbeans.modules.form;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager2;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import javax.swing.text.StyledDocument;
import javax.swing.undo.UndoManager;
import org.netbeans.editor.BaseDocument;

import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.text.NbDocument;
import org.openide.util.NbBundle;

import static org.netbeans.modules.form.CustomCodeData.*;

/**
 * GUI panel of the code customizer.
 * 
 * @author Tomas Pavek
 */

class CustomCodeView extends javax.swing.JPanel {

    private CustomCodeData codeData;
    private int lastLocalModifiers = -1;
    private int lastFieldModifiers = -1;

    private boolean changed;

    interface Listener {
        void componentExchanged(String compName);
        void renameInvoked();
        void declarationChanged();
    }

    private Listener controller;

    private DocumentL docListener;

    // flag to recognize user action on JComboBox from our calls
    private boolean ignoreComboAction;

    private static class EditBlockInfo {
        Position position;
        List<EditableLine> lines;
    }

    private static class GuardBlockInfo {
        Position position;
        String customizedCode;
        boolean customized;
    }

    private Map<CodeCategory, EditBlockInfo[]> editBlockInfos;
    private Map<CodeCategory, GuardBlockInfo[]> guardBlockInfos;

    // -----

    CustomCodeView(Listener controller) {
        this.controller = controller;

        initComponents();

        variableCombo.setModel(new DefaultComboBoxModel(variableStrings));
        accessCombo.setModel(new DefaultComboBoxModel(accessStrings));

        // create gutter panels - let their layout share the component map so
        // they have the same width
        Map<Component, Position> positions = new HashMap<Component, Position>();
        initGutter = new JPanel();
        initGutter.setLayout(new GutterLayout(initCodeEditor, positions));
        declareGutter = new JPanel();
        declareGutter.setLayout(new GutterLayout(declareCodeEditor, positions));
        jScrollPane1.setRowHeaderView(initGutter);
        jScrollPane2.setRowHeaderView(declareGutter);
//        jScrollPane1.setBorder(null);
//        jScrollPane2.setBorder(null);
    }

    @Override
    public void addNotify() {
        super.addNotify();
        initCodeEditor.requestFocusInWindow();
    }

    boolean isChanged() {
        return changed;
    }

    void setComponentNames(String[] compNames) {
        componentCombo.setModel(new DefaultComboBoxModel(compNames));
    }

    void setCodeData(String componentName, CustomCodeData codeData, FileObject srcFile, int[] positions) {
        if (this.codeData != null) { // clean up
            initCodeEditor.getDocument().removeDocumentListener(docListener);
            declareCodeEditor.getDocument().removeDocumentListener(docListener);

            initGutter.removeAll();
            declareGutter.removeAll();

            revalidate();
            repaint();
        }

        if (editBlockInfos != null) {
            editBlockInfos.clear();
            guardBlockInfos.clear();
        }
        else {
            editBlockInfos = new HashMap<CodeCategory, EditBlockInfo[]>();
            guardBlockInfos = new HashMap<CodeCategory, GuardBlockInfo[]>();
        }

        FormUtils.setupEditorPane(initCodeEditor, srcFile, positions[0]);
        FormUtils.setupEditorPane(declareCodeEditor, srcFile, positions[1]);

        this.codeData = codeData;
        selectInComboBox(componentCombo, componentName);

        buildCodeView(CodeCategory.CREATE_AND_INIT);
        buildCodeView(CodeCategory.DECLARATION);

        Object um = initCodeEditor.getDocument().getProperty(BaseDocument.UNDO_MANAGER_PROP);
        if (um instanceof UndoManager) {
            ((UndoManager)um).discardAllEdits();
        }
        um = declareCodeEditor.getDocument().getProperty(BaseDocument.UNDO_MANAGER_PROP);
        if (um instanceof UndoManager) {
            ((UndoManager)um).discardAllEdits();
        }

        VariableDeclaration decl = codeData.getDeclarationData();
        boolean local = decl.local;
        for (int i=0; i < variableValues.length; i++) {
            if (variableValues[i] == local) {
                selectInComboBox(variableCombo, variableStrings[i]);
                break;
            }
        }
        int modifiers = decl.modifiers;
        int access = modifiers & (Modifier.PRIVATE|Modifier.PROTECTED|Modifier.PUBLIC);
        for (int i=0; i < accessValues.length; i++) {
            if (accessValues[i] == access) {
                selectInComboBox(accessCombo, accessStrings[i]);
                break;
            }
        }
        staticCheckBox.setSelected((modifiers & Modifier.STATIC) == Modifier.STATIC);
        finalCheckBox.setSelected((modifiers & Modifier.FINAL) == Modifier.FINAL);
        transientCheckBox.setSelected((modifiers & Modifier.TRANSIENT) == Modifier.TRANSIENT);
        volatileCheckBox.setSelected((modifiers & Modifier.VOLATILE) == Modifier.VOLATILE);
        accessCombo.setEnabled(!local);
        staticCheckBox.setEnabled(!local);
        transientCheckBox.setEnabled(!local);
        volatileCheckBox.setEnabled(!local);

        if (local)
            lastLocalModifiers = modifiers;
        else
            lastFieldModifiers = modifiers;

        changed = false;

        if (docListener == null)
            docListener = new DocumentL();
        initCodeEditor.getDocument().addDocumentListener(docListener);
        declareCodeEditor.getDocument().addDocumentListener(docListener);

        initCodeEditor.setCaretPosition(0);
        declareCodeEditor.setCaretPosition(0);
    }

    private void buildCodeView(CodeCategory category) {
        editBlockInfos.put(category, new EditBlockInfo[codeData.getEditableBlockCount(category)]);
        int gCount = codeData.getGuardedBlockCount(category);
        guardBlockInfos.put(category, new GuardBlockInfo[gCount]);

        try {
            for (int i=0; i < gCount; i++) {
                addEditableCode(category, i);
                addGuardedCode(category, i);
            }
            if (gCount > 0)
                addEditableCode(category, gCount);
        }
        catch (BadLocationException ex) { // should not happen
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }

        getEditor(category).setEnabled(gCount > 0);
    }

    private void addEditableCode(CodeCategory category, int blockIndex)
        throws BadLocationException
    {
        Document doc = getDocument(category);
        EditableBlock eBlock = codeData.getEditableBlock(category, blockIndex);
        boolean lastBlock = blockIndex+1 == codeData.getEditableBlockCount(category);
        List<EditableLine> lineList = new LinkedList<EditableLine>();
        int startIndex = doc.getLength();
        boolean needLineEnd = false;

        CodeEntry[] entries = eBlock.getEntries();
        for (int i=0; i < entries.length; i++) {
            CodeEntry e = entries[i];
            String code = e.getCode();
            if (code == null)
                continue;

            // process lines of the code entry
            int lineStart = 0;
            int codeLength = code.length();
            for (int j=0; j < codeLength; j++) {
                char c = code.charAt(j);
                // TODO: filter out subsequent empty lines?
                if (c == '\n' || j+1 == codeLength) { // end of line
                    if (needLineEnd) // previous line not ended by '\n'
                        doc.insertString(doc.getLength(), "\n", null); // NOI18N

                    boolean lastLine = lastBlock && i+1 == entries.length && j+1 == codeLength;
                    needLineEnd = c != '\n' && !lastLine; // missing '\n' - will add it later when needed
                    int lineEnd = c == '\n' && lastLine ? j : j+1; // skip '\n' for very last line
                    int index = doc.getLength();
                    doc.insertString(index, code.substring(lineStart, lineEnd), null);
                    Position pos = NbDocument.createPosition(doc, index, Position.Bias.Backward);
                    lineList.add(new EditableLine(pos, eBlock, i, lineList));

                    lineStart = j + 1;
                }
            }
        }

        if (lineList.size() > 0) {
            if (needLineEnd) // last line of the block not ended by '\n'
                doc.insertString(doc.getLength(), "\n", null); // NOI18N
        }
        else { // no code in whole block - add one empty line
            int index = doc.getLength();
            if (!lastBlock)
                doc.insertString(index, "\n", null); // NOI18N
            Position pos = NbDocument.createPosition(doc, index, Position.Bias.Backward);
            lineList.add(new EditableLine(pos, eBlock, eBlock.getPreferredEntryIndex(), lineList));
        }

        updateGutterComponents(lineList, doc, startIndex, doc.getLength());

        EditBlockInfo eInfo = new EditBlockInfo();
        eInfo.position = lineList.get(0).getPosition();
        eInfo.lines = lineList;
        getEditInfos(category)[blockIndex] = eInfo;
    }

    private void addGuardedCode(CodeCategory category, int blockIndex)
        throws BadLocationException
    {
        StyledDocument doc = (StyledDocument) getDocument(category);
        GuardedBlock gBlock = codeData.getGuardedBlock(category, blockIndex);
        GuardBlockInfo gInfo = new GuardBlockInfo();
        int index = doc.getLength();
        if (gBlock.isCustomized()) {
            String code = gBlock.getCustomCode();
            doc.insertString(index, code, null);
            if (!code.endsWith("\n")) { // NOI18N
                doc.insertString(doc.getLength(), "\n", null); // NOI18N
            }
            int header = gBlock.getHeaderLength();
            int footer = gBlock.getFooterLength();
            NbDocument.markGuarded(doc, index, header);
            NbDocument.markGuarded(doc, doc.getLength() - footer, footer);
            gInfo.customized = true;
        }
        else {
            String code = gBlock.getDefaultCode();
            doc.insertString(index, code, null);
            if (!code.endsWith("\n")) { // NOI18N
                doc.insertString(doc.getLength(), "\n", null); // NOI18N
            }
            NbDocument.markGuarded(doc, index, doc.getLength()-index);
        }

        Position pos = NbDocument.createPosition(doc, index, Position.Bias.Forward);
        gInfo.position = pos;
        getGuardInfos(category)[blockIndex] = gInfo;

        if (gBlock.isCustomizable()) {
            String[] items = new String[] { NbBundle.getMessage(CustomCodeView.class, "CTL_GuardCombo_Default"), // NOI18N
                                            gBlock.getCustomEntry().getDisplayName() };
            JComboBox combo = new JComboBox(items);
//            combo.setBorder(null);
            if (gBlock.isCustomized()) {
                selectInComboBox(combo, items[1]);
                combo.setToolTipText(gBlock.getCustomEntry().getToolTipText());
            }
            else {
                selectInComboBox(combo, items[0]);
                combo.setToolTipText(NbBundle.getMessage(CustomCodeView.class, "CTL_GuardCombo_Default_Hint")); // NOI18N
            }
            combo.getAccessibleContext().setAccessibleName(gBlock.getCustomEntry().getName());
            combo.addActionListener(new GuardSwitchL(category, blockIndex));
            getGutter(doc).add(combo, pos);
        }
    }

    /**
     * Writes edited code back to the CustomCodeData structure.
     */
    CustomCodeData retreiveCodeData() {
        retreiveCodeData(CodeCategory.CREATE_AND_INIT);
        retreiveCodeData(CodeCategory.DECLARATION);

        VariableDeclaration decl = codeData.getDeclarationData();
        boolean local = variableValues[variableCombo.getSelectedIndex()];
        int modifiers;
        if (local != decl.local) {
            modifiers = local ? lastLocalModifiers : lastFieldModifiers;
            if (finalCheckBox.isSelected()) // only final makes sense for both local and field scope
                modifiers |= Modifier.FINAL;
            else
                modifiers &= ~Modifier.FINAL;
        }
        else {
            modifiers = accessValues[accessCombo.getSelectedIndex()];
            if (staticCheckBox.isSelected())
                modifiers |= Modifier.STATIC;
            if (finalCheckBox.isSelected())
                modifiers |= Modifier.FINAL;
            if (transientCheckBox.isSelected())
                modifiers |= Modifier.TRANSIENT;
            if (volatileCheckBox.isSelected())
                modifiers |= Modifier.VOLATILE;
            if (local)
                modifiers &= ~(Modifier.STATIC | Modifier.TRANSIENT | Modifier.VOLATILE);
        }
        decl.local = local;
        decl.modifiers = modifiers;

        return codeData;
    }

    private void retreiveCodeData(CodeCategory category) {
        int gCount = codeData.getGuardedBlockCount(category);
        for (int i=0; i < gCount; i++) {
            retreiveEditableBlock(category, i);
            retreiveGuardedBlock(category, i);
        }
        if (gCount > 0)
            retreiveEditableBlock(category, gCount);
    }

    private void retreiveEditableBlock(CodeCategory category, int index) {
        CodeEntry[] entries = codeData.getEditableBlock(category, index).getEntries();
        for (CodeEntry e : entries) {
            e.setCode(null);
        }

        int[] blockBounds = getEditBlockBounds(category, index);
        Document doc = getDocument(category);

        try {
            String allCode = doc.getText(blockBounds[0], blockBounds[1]-blockBounds[0]);
            if (allCode.trim().equals("")) // NOI18N
                return;

            StringBuilder buf = new StringBuilder();
            int selIndex = -1;
            EditableLine nextLine = null;
            Iterator<EditableLine> it = getEditInfos(category)[index].lines.iterator();
            while (it.hasNext() || nextLine != null) {
                EditableLine l = nextLine != null ? nextLine : it.next();
                int startPos = l.getPosition().getOffset();
                int endPos;
                if (it.hasNext()) {
                    nextLine = it.next();
                    endPos = nextLine.getPosition().getOffset();
                }
                else {
                    nextLine = null;
                    endPos = blockBounds[1];
                }
                buf.append(doc.getText(startPos, endPos-startPos));
                if (nextLine == null || nextLine.getSelectedIndex() != l.getSelectedIndex()) {
                    String code = buf.toString().trim();
                    if (!code.equals("")) // NOI18N
                        entries[l.getSelectedIndex()].setCode(code);
                    buf.delete(0, buf.length());
                }
            }
        }
        catch (BadLocationException ex) { // should not happen
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
    }

    private void retreiveGuardedBlock(CodeCategory category, int index) {
        GuardedBlock gBlock = codeData.getGuardedBlock(category, index);
        if (!gBlock.isCustomizable())
            return;

        if (getGuardInfos(category)[index].customized) {
            Document doc = getDocument(category);
            int[] blockBounds = getGuardBlockBounds(category, index);
            try {
                int startPos = blockBounds[0] + gBlock.getHeaderLength();
                String code = doc.getText(startPos,
                                          blockBounds[1] - gBlock.getFooterLength() - startPos);
                gBlock.setCustomizedCode(code);
            }
            catch (BadLocationException ex) { // should not happen
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            }
        }
        else { // reset to default code
            gBlock.setCustomizedCode(null);
        }
    }

    private void selectInComboBox(JComboBox combo, Object item) {
        ignoreComboAction = true;
        combo.setSelectedItem(item);
        ignoreComboAction = false;
    }

    // -----
    // mapping methods

    private JTextComponent getEditor(CodeCategory category) {
        switch (category) {
            case CREATE_AND_INIT: return initCodeEditor;
            case DECLARATION: return declareCodeEditor;
        }
        return null;
    }

    private Document getDocument(CodeCategory category) {
        return getEditor(category).getDocument();
    }

    private EditBlockInfo[] getEditInfos(CodeCategory category) {
        return editBlockInfos.get(category);
    }

    private GuardBlockInfo[] getGuardInfos(CodeCategory category) {
        return guardBlockInfos.get(category);
    }

    private JPanel getGutter(Document doc) {
        if (doc == initCodeEditor.getDocument())
            return initGutter;
        if (doc == declareCodeEditor.getDocument())
            return declareGutter;
        return null;
    }

    private CodeCategory getCategoryForDocument(Document doc) {
        if (doc == initCodeEditor.getDocument())
            return CodeCategory.CREATE_AND_INIT;
        if (doc == declareCodeEditor.getDocument())
            return CodeCategory.DECLARATION;
        return null;
    }

    private static Element getRootElement(Document doc) {
        return doc.getRootElements()[0];
    }

    // -----

    private class EditableLine {
        private Position position;
        private JComboBox targetCombo;
        private List<EditableLine> linesInBlock;
        private CodeEntry[] codeEntries;

        EditableLine(Position pos, EditableBlock eBlock, int selIndex, List<EditableLine> lines) {
            position = pos;
            linesInBlock = lines;
            codeEntries = eBlock.getEntries();
            targetCombo = new JComboBox(codeEntries);
            setSelectedIndex(selIndex);
            targetCombo.getAccessibleContext().setAccessibleName(codeEntries[selIndex].getName());
            targetCombo.setToolTipText(codeEntries[selIndex].getToolTipText());
            targetCombo.addActionListener(new EditSwitchL());
        }

        Position getPosition() {
            return position;
        }

        Component getGutterComponent() {
            return targetCombo;
        }

        // if having visible combobox in gutter
        boolean isVisible() {
            return targetCombo.getParent() != null && targetCombo.isVisible();
        }

        int getSelectedIndex() {
            return targetCombo.getSelectedIndex();
        }

        void setSelectedIndex(int index) {
            selectInComboBox(targetCombo, targetCombo.getItemAt(index));
        }

        class EditSwitchL implements ActionListener {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (ignoreComboAction)
                    return; // not invoked by user, ignore

                changed = true;
                // go through all comboboxes in the group and correct selected index
                // according to the selection in this combobox (preceding comboboxes
                // can't have bigger index and subsequent can't have smaller index)
                int selectedIndex = targetCombo.getSelectedIndex();
                boolean preceding = true;
                for (EditableLine l : linesInBlock) {
                    if (l != EditableLine.this) {
                        if ((preceding && l.getSelectedIndex() > selectedIndex)
                            || (!preceding && l.getSelectedIndex() < selectedIndex))
                        {   // correct selected index
                            l.setSelectedIndex(selectedIndex);
                        }
                    }
                    else preceding = false;
                }
                targetCombo.setToolTipText(codeEntries[selectedIndex].getToolTipText());
            }
        }
    }

    private boolean updateGutterComponents(List<EditableLine> lines, Document doc,
                                           int startIndex, int endIndex)
    {
        String text;
        try {
            text = doc.getText(startIndex, endIndex-startIndex);
        }
        catch (BadLocationException ex) { // should not happen
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            return false;
        }

        boolean visibility = !text.trim().equals(""); // NOI18N
        int prevSelectedIndex = 0;
        boolean changed = false;
        Container gutter = getGutter(doc);
        for (EditableLine l : lines) {
            // make sure the selected index is correct (ascending in the group)
            if (l.getSelectedIndex() < prevSelectedIndex)
                l.setSelectedIndex(prevSelectedIndex);
            else
                prevSelectedIndex = l.getSelectedIndex();
            // add the component to the gutter if not there yet
            Component comp = l.getGutterComponent();
            if (comp.getParent() == null)
                gutter.add(comp, l.getPosition());
            // show/hide the component
            if (visibility != l.isVisible()) {
                comp.setVisible(visibility);
                changed = true;
            }
        }
        return changed;
    }

    // -----
    // document changes

    private class DocumentL implements DocumentListener {
        boolean active = true;

        @Override
        public void insertUpdate(DocumentEvent e) {
            if (active)
                contentChange(e);
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            if (active)
                contentChange(e);
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
        }

        private void contentChange(DocumentEvent e) {
            changed = true;

            Document doc = e.getDocument();
            CodeCategory category = getCategoryForDocument(doc);
            int eBlockIndex = getEditBlockIndex(category, e.getOffset());
            if (eBlockIndex < 0)
                return;

            List<EditableLine> lines = getEditInfos(category)[eBlockIndex].lines;
            int[] blockBounds = getEditBlockBounds(category, eBlockIndex);

            boolean repaint = false;
            DocumentEvent.ElementChange change = e.getChange(getRootElement(doc));
            if (change != null) {
                Element[] added = change.getChildrenAdded();
                Element[] removed = change.getChildrenRemoved();
                if (added.length != removed.length) {
                    Element rootEl = getRootElement(doc);
                    int elIndex = rootEl.getElementIndex(e.getOffset());
                    if (added.length > removed.length) { // lines added
                        processAddedLines(rootEl, elIndex, lines, blockBounds,
                                          codeData.getEditableBlock(category, eBlockIndex));
                        repaint = true;
                    }
                    else if (added.length < removed.length) { // lines removed
                        processRemovedLines(rootEl.getElement(elIndex), lines, blockBounds);
                        if (blockBounds[0] == blockBounds[1]) { // whole block's text deleted
                            try { // keep one empty line
                                doc.insertString(blockBounds[0], "\n", null); // NOI18N
                                getEditor(category).setCaretPosition(blockBounds[0]);
                            }
                            catch (BadLocationException ex) { // should not happen
                                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                            }
                            return; // is updated with adding the new line
                        }
                        repaint = true;
                    }
                }
            }

            repaint |= updateGutterComponents(lines, doc, blockBounds[0], blockBounds[1]);

            if (repaint) {
                JPanel gutter = getGutter(doc);
                gutter.revalidate();
                gutter.repaint();
            }
            ((BaseDocument)doc).resetUndoMerge();
        }
    }

    private void processAddedLines(Element rootEl, int elIndex,
                                   List<EditableLine> lines,
                                   int[] blockBounds,
                                   EditableBlock eBlock)
    {
        Document doc = rootEl.getDocument();
        int elPos = rootEl.getElement(elIndex).getStartOffset();
        
        // determine where to insert new lines (and which target should they have selected)
        int endPos = -1;
        int selIndex = -1;
        ListIterator<EditableLine> lineIt = lines.listIterator();
        while (lineIt.hasNext()) {
            EditableLine l = lineIt.next();
            int pos = l.getPosition().getOffset();
            if (pos > elPos) {
                endPos = pos;
                lineIt.previous();
                break;
            }
            else {
                selIndex = l.getSelectedIndex();
                if (pos == elPos) { // already have line for this element (Enter on empty line)
                    elIndex++;
                    elPos = rootEl.getElement(elIndex).getStartOffset();
                }
            }
        }
        if (endPos < 0) { // adding at the end of the block, don't have boundary line
            endPos = blockBounds[1];
        }
        if (selIndex < 0) {
            selIndex = eBlock.getPreferredEntryIndex();
        }

        // now create the missing lines
        try {
            do {
                Position pos = NbDocument.createPosition(doc, elPos, Position.Bias.Backward);
                lineIt.add(new EditableLine(pos, eBlock, selIndex, lines));
                if (++elIndex >= rootEl.getElementCount())
                    break;
                elPos = rootEl.getElement(elIndex).getStartOffset();
            }
            while (elPos < endPos);
        }
        catch (BadLocationException ex) { // should not happen
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
    }

    private void processRemovedLines(Element changeEl,
                                     List<EditableLine> lines,
                                     int[] blockBounds)
    {
        Document doc = changeEl.getDocument();
        int startPos = changeEl.getStartOffset();
        int endPos = changeEl.getEndOffset();

        // determine the lines to remove (their positons lie in the affected element)
        EditableLine firstLine = null;
        EditableLine lastLine = null;
        Iterator<EditableLine> it = lines.iterator();
        while (it.hasNext()) {
            EditableLine l = it.next();
            int pos = l.getPosition().getOffset();
            if (pos >= startPos) {
                if (pos >= endPos)
                    break;
                if (firstLine == null)
                    firstLine = l;
                lastLine = l;
            }
        }
        if (firstLine == null)
            return; // no lines affected

        boolean wholeFirstLine = lastLine.getPosition().getOffset() == startPos;
        boolean mergedToGuarded = startPos == blockBounds[1] && startPos != doc.getLength();

        // remove the lines
        it = lines.iterator();
        while (it.hasNext() && lastLine != null) {
            EditableLine l = it.next();
            boolean remove;
            if (l == firstLine) {
                remove =  mergedToGuarded || wholeFirstLine;
                firstLine = null;
            }
            else if (l == lastLine) {
                remove = mergedToGuarded || !wholeFirstLine;
                lastLine = null;
            }
            else {
                remove = firstLine == null; // all lines in between
            }
            if (remove) {
                it.remove();
                Component comp = l.getGutterComponent();
                comp.getParent().remove(comp);
            }
        }
    }

    private int[] getEditBlockBounds(CodeCategory category, int index) {
        int startIndex = getEditInfos(category)[index].position.getOffset();
        GuardBlockInfo[] gInfos = getGuardInfos(category);
        int endIndex = index < gInfos.length ?
                gInfos[index].position.getOffset() :
                getDocument(category).getLength();
        return new int[] { startIndex, endIndex };
    }

    private int getEditBlockIndex(CodeCategory category, int offset) {
        return getBlockIndex(category, offset, true);
    }

    private int getGuardBlockIndex(CodeCategory category, int offset) {
        return getBlockIndex(category, offset, false);
    }

    private int getBlockIndex(CodeCategory category, int offset, boolean editable) {
        EditBlockInfo[] editInfos = getEditInfos(category);
        GuardBlockInfo[] guardInfos = getGuardInfos(category);
        // assuming editInfo.length == guardInfos.length + 1
        for (int i=0; i < guardInfos.length; i++) {
            int editPos = editInfos[i].position.getOffset();
            if (editPos > offset) // the offset lies in preceding guarded block
                return editable ? -1 : i-1;
            if (editPos == offset || guardInfos[i].position.getOffset() >= offset)
                return editable ? i : -1; // the offset lies in this editable block
        }
        // otherwise the offset is in the last editable block
        return editable ? editInfos.length-1 : -1;
    }

    // -----

    private class GuardSwitchL implements ActionListener {
        CodeCategory category;
        int blockIndex;

        GuardSwitchL(CodeCategory cat, int index) {
            category = cat;
            blockIndex = index;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (ignoreComboAction)
                return; // not invoked by user, ignore

            GuardedBlock gBlock = codeData.getGuardedBlock(category, blockIndex);
            GuardBlockInfo gInfo = getGuardInfos(category)[blockIndex];
            int[] blockBounds = getGuardBlockBounds(category, blockIndex);
            int startOffset = blockBounds[0];
            int endOffset = blockBounds[1];
            int gHead = gBlock.getHeaderLength();
            int gFoot = gBlock.getFooterLength();
            JTextComponent editor = getEditor(category);
            StyledDocument doc = (StyledDocument) editor.getDocument();

            changed = true;

            JComboBox combo = (JComboBox) e.getSource();
            try {
                docListener.active = false;
                if (combo.getSelectedIndex() == 1) { // changing from default to custom
                    NbDocument.unmarkGuarded(doc, startOffset, endOffset - startOffset);
                    // keep last '\n' so we don't destroy next editable block's position
                    doc.remove(startOffset, endOffset - startOffset - 1);
                    // insert the custom code into the document
                    String customCode = gBlock.getCustomCode();
                    int customLength = customCode.length();
                    if (gInfo.customizedCode != null) { // already was edited before
                        customCode = customCode.substring(0, gHead)
                                     + gInfo.customizedCode
                                     + customCode.substring(customLength - gFoot);
                        customLength = customCode.length();
                    }
                    if (customCode.endsWith("\n")) // NOI18N
                        customCode = customCode.substring(0, customLength-1);
                    doc.insertString(startOffset, customCode, null);
                    gInfo.customized = true;
                    // make guarded "header" and "footer", select the text in between
                    NbDocument.markGuarded(doc, startOffset, gHead);
                    NbDocument.markGuarded(doc, startOffset + customLength - gFoot, gFoot);
                    editor.setSelectionStart(startOffset + gHead);
                    editor.setSelectionEnd(startOffset + customLength - gFoot);
                    editor.requestFocus();
                    combo.setToolTipText(gBlock.getCustomEntry().getToolTipText());
                }
                else { // changing from custom to default
                    // remember the customized code
                    gInfo.customizedCode = doc.getText(startOffset + gHead,
                                                       endOffset - gFoot - gHead - startOffset);
                    NbDocument.unmarkGuarded(doc, endOffset - gFoot, gFoot);
                    NbDocument.unmarkGuarded(doc, startOffset, gHead);
                    // keep last '\n' so we don't destroy next editable block's position
                    doc.remove(startOffset, endOffset - startOffset - 1);
                    String defaultCode = gBlock.getDefaultCode();
                    if (defaultCode.endsWith("\n")) // NOI18N
                        defaultCode = defaultCode.substring(0, defaultCode.length()-1);
                    doc.insertString(startOffset, defaultCode, null);
                    gInfo.customized = false;
                    // make the whole text guarded, cancel selection
                    NbDocument.markGuarded(doc, startOffset, defaultCode.length()+1); // including '\n'
                    if (editor.getSelectionStart() >= startOffset && editor.getSelectionEnd() <= endOffset)
                        editor.setCaretPosition(startOffset);
                    combo.setToolTipText(NbBundle.getMessage(CustomCodeData.class, "CTL_GuardCombo_Default_Hint")); // NOI18N
                }
                // we must create a new Position - current was moved away by inserting new string on it
                gInfo.position = NbDocument.createPosition(doc, startOffset, Position.Bias.Forward);

                docListener.active = true;
            }
            catch (BadLocationException ex) { // should not happen
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            }
        }
    }

    private int[] getGuardBlockBounds(CodeCategory category, int index) {
        int startIndex = getGuardInfos(category)[index].position.getOffset();
        int endIndex = getEditInfos(category)[index+1].position.getOffset();
        return new int[] { startIndex, endIndex };
    }

    // -----

    private static class GutterLayout implements LayoutManager2 {

        private JTextComponent editor;
        private Map<Component, Position> positions;
        private int lineHeight = -1;

        private static final int LEFT_GAP = 2;
        private static final int RIGHT_GAP = 4;

        GutterLayout(JTextComponent editor, Map<Component, Position> positionMap) {
            this.editor = editor;
            this.positions = positionMap;
        }

        @Override
        public void addLayoutComponent(Component comp, Object constraints) {
            positions.put(comp, (Position)constraints);
        }

        @Override
        public void layoutContainer(Container parent) {
            StyledDocument doc = (StyledDocument)editor.getDocument();
            for (Component comp : parent.getComponents()) {
                Position pos = positions.get(comp);
                int line = NbDocument.findLineNumber(doc, pos.getOffset());
                Dimension prefSize = comp.getPreferredSize();
                int dy = lineHeight() - prefSize.height;
                dy = dy > 0 ? dy / 2 + 1 : 0;
                comp.setBounds(LEFT_GAP,
                               line * lineHeight() + dy,
                               parent.getWidth() - LEFT_GAP - RIGHT_GAP,
                               Math.min(prefSize.height, lineHeight()));
            }
        }

        @Override
        public void removeLayoutComponent(Component comp) {
            positions.remove(comp);
        }

        @Override
        public Dimension preferredLayoutSize(Container parent) {
            int prefWidth = 0;
            for (Component comp : positions.keySet()) {
                Dimension prefSize = comp.getPreferredSize();
                if (prefSize.width > prefWidth)
                    prefWidth = prefSize.width;
            }
            return new Dimension(prefWidth + LEFT_GAP + RIGHT_GAP,
                                 editor.getPreferredSize().height);
        }

        @Override
        public Dimension minimumLayoutSize(Container parent) {
            return preferredLayoutSize(parent);
        }

        @Override
        public Dimension maximumLayoutSize(Container parent) {
            return preferredLayoutSize(parent);
        }

        @Override
        public float getLayoutAlignmentX(Container target) {
            return .5f;
        }

        @Override
        public float getLayoutAlignmentY(Container target) {
            return .5f;
        }

        @Override
        public void invalidateLayout(Container target) {
        }

        @Override
        public void addLayoutComponent(String name, Component comp) {
        }

        // -----

        private int lineHeight() {
            if (lineHeight < 0) {
                Element root = editor.getDocument().getDefaultRootElement();
                if (root.getElementCount()>0) {
                    Element elem = root.getElement(0);
                    try {
                        int y1 = editor.modelToView(elem.getStartOffset()).y;
                        int y2 = editor.modelToView(elem.getEndOffset()).y;
                        lineHeight = y2-y1;
                    } catch (BadLocationException blex) {
                        Logger.getLogger(CustomCodeView.class.getName()).log(Level.INFO, blex.getMessage(), blex);
                    }
                }
                if (lineHeight <= 0) {
                    // fallback
                    lineHeight = editor.getFontMetrics(editor.getFont()).getHeight();
                }
            }
            return lineHeight;
        }
    }

    private static int getLineCount(Document doc) {
        return getRootElement(doc).getElementCount();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.JLabel initCodeLabel = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        initCodeEditor = new javax.swing.JEditorPane();
        javax.swing.JLabel declarationCodeLabel = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        declareCodeEditor = new javax.swing.JEditorPane();
        javax.swing.JLabel selectComponentLabel = new javax.swing.JLabel();
        componentCombo = new javax.swing.JComboBox();
        renameButton = new javax.swing.JButton();
        javax.swing.JLabel variableScopeLabel = new javax.swing.JLabel();
        variableCombo = new javax.swing.JComboBox();
        javax.swing.JLabel variableAccessLabel = new javax.swing.JLabel();
        accessCombo = new javax.swing.JComboBox();
        staticCheckBox = new javax.swing.JCheckBox();
        finalCheckBox = new javax.swing.JCheckBox();
        transientCheckBox = new javax.swing.JCheckBox();
        volatileCheckBox = new javax.swing.JCheckBox();

        FormListener formListener = new FormListener();

        initCodeLabel.setFont(initCodeLabel.getFont().deriveFont(initCodeLabel.getFont().getStyle() | java.awt.Font.BOLD));
        initCodeLabel.setLabelFor(initCodeEditor);
        org.openide.awt.Mnemonics.setLocalizedText(initCodeLabel, org.openide.util.NbBundle.getMessage(CustomCodeView.class, "CustomCodeView.initCodeLabel.text")); // NOI18N

        jScrollPane1.setViewportView(initCodeEditor);

        declarationCodeLabel.setFont(declarationCodeLabel.getFont().deriveFont(declarationCodeLabel.getFont().getStyle() | java.awt.Font.BOLD));
        declarationCodeLabel.setLabelFor(declareCodeEditor);
        org.openide.awt.Mnemonics.setLocalizedText(declarationCodeLabel, org.openide.util.NbBundle.getMessage(CustomCodeView.class, "CustomCodeView.declarationCodeLabel.text")); // NOI18N

        jScrollPane2.setViewportView(declareCodeEditor);

        selectComponentLabel.setLabelFor(componentCombo);
        org.openide.awt.Mnemonics.setLocalizedText(selectComponentLabel, org.openide.util.NbBundle.getMessage(CustomCodeView.class, "CustomCodeView.selectComponentLabel.text")); // NOI18N

        componentCombo.setToolTipText(org.openide.util.NbBundle.getMessage(CustomCodeView.class, "CustomCodeView.componentCombo.toolTipText")); // NOI18N
        componentCombo.addActionListener(formListener);

        org.openide.awt.Mnemonics.setLocalizedText(renameButton, org.openide.util.NbBundle.getMessage(CustomCodeView.class, "CustomCodeView.renameButton.text")); // NOI18N
        renameButton.setToolTipText(org.openide.util.NbBundle.getMessage(CustomCodeView.class, "CustomCodeView.renameButton.toolTipText")); // NOI18N
        renameButton.addActionListener(formListener);

        variableScopeLabel.setLabelFor(variableCombo);
        org.openide.awt.Mnemonics.setLocalizedText(variableScopeLabel, org.openide.util.NbBundle.getMessage(CustomCodeView.class, "CustomCodeView.variableScopeLabel.text")); // NOI18N

        variableCombo.addActionListener(formListener);

        variableAccessLabel.setLabelFor(accessCombo);
        org.openide.awt.Mnemonics.setLocalizedText(variableAccessLabel, org.openide.util.NbBundle.getMessage(CustomCodeView.class, "CustomCodeView.variableAccessLabel.text")); // NOI18N

        accessCombo.addActionListener(formListener);

        org.openide.awt.Mnemonics.setLocalizedText(staticCheckBox, "&static"); // NOI18N
        staticCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        staticCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        staticCheckBox.addActionListener(formListener);

        org.openide.awt.Mnemonics.setLocalizedText(finalCheckBox, "&final"); // NOI18N
        finalCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        finalCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        finalCheckBox.addActionListener(formListener);

        org.openide.awt.Mnemonics.setLocalizedText(transientCheckBox, "&transient"); // NOI18N
        transientCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        transientCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        transientCheckBox.addActionListener(formListener);

        org.openide.awt.Mnemonics.setLocalizedText(volatileCheckBox, "v&olatile"); // NOI18N
        volatileCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        volatileCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        volatileCheckBox.addActionListener(formListener);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 680, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .add(variableScopeLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(variableCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(variableAccessLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(accessCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(finalCheckBox)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(staticCheckBox)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(transientCheckBox)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(volatileCheckBox))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 680, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, declarationCodeLabel)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .add(selectComponentLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(componentCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(renameButton))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, initCodeLabel))
                .addContainerGap())
        );

        layout.linkSize(new java.awt.Component[] {accessCombo, variableCombo}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(selectComponentLabel)
                    .add(componentCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(renameButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(initCodeLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 348, Short.MAX_VALUE)
                .add(11, 11, 11)
                .add(declarationCodeLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 58, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(variableScopeLabel)
                    .add(variableCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(variableAccessLabel)
                    .add(accessCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(staticCheckBox)
                    .add(finalCheckBox)
                    .add(transientCheckBox)
                    .add(volatileCheckBox))
                .addContainerGap())
        );

        staticCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomCodeView.class, "CustomCodeView.staticCheckBox.accessibleDescription")); // NOI18N
        finalCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomCodeView.class, "CustomCodeView.finalCheckBox.accessibleDescription")); // NOI18N
        transientCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomCodeView.class, "CustomCodeView.transientCheckBox.accessibleDescription")); // NOI18N
        volatileCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomCodeView.class, "CustomCodeView.volatileCheckBox.accessibleDescription")); // NOI18N

        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomCodeView.class, "CustomCodeView.accessibleDescription")); // NOI18N
    }

    // Code for dispatching events from components to event handlers.

    private class FormListener implements java.awt.event.ActionListener {
        FormListener() {}
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            if (evt.getSource() == componentCombo) {
                CustomCodeView.this.componentComboActionPerformed(evt);
            }
            else if (evt.getSource() == renameButton) {
                CustomCodeView.this.renameButtonActionPerformed(evt);
            }
            else if (evt.getSource() == variableCombo) {
                CustomCodeView.this.declControlActionPerformed(evt);
            }
            else if (evt.getSource() == accessCombo) {
                CustomCodeView.this.declControlActionPerformed(evt);
            }
            else if (evt.getSource() == staticCheckBox) {
                CustomCodeView.this.declControlActionPerformed(evt);
            }
            else if (evt.getSource() == finalCheckBox) {
                CustomCodeView.this.declControlActionPerformed(evt);
            }
            else if (evt.getSource() == transientCheckBox) {
                CustomCodeView.this.declControlActionPerformed(evt);
            }
            else if (evt.getSource() == volatileCheckBox) {
                CustomCodeView.this.declControlActionPerformed(evt);
            }
        }
    }// </editor-fold>//GEN-END:initComponents

    private void declControlActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_declControlActionPerformed
        if (ignoreComboAction)
            return; // not invoked by user, ignore

        changed = true;
        controller.declarationChanged();
    }//GEN-LAST:event_declControlActionPerformed

    private void renameButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_renameButtonActionPerformed
        controller.renameInvoked();
    }//GEN-LAST:event_renameButtonActionPerformed

    private void componentComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_componentComboActionPerformed
        if (ignoreComboAction)
            return; // not invoked by user, ignore

        controller.componentExchanged((String)componentCombo.getSelectedItem());
    }//GEN-LAST:event_componentComboActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox accessCombo;
    private javax.swing.JComboBox componentCombo;
    private javax.swing.JEditorPane declareCodeEditor;
    private javax.swing.JCheckBox finalCheckBox;
    private javax.swing.JEditorPane initCodeEditor;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JButton renameButton;
    private javax.swing.JCheckBox staticCheckBox;
    private javax.swing.JCheckBox transientCheckBox;
    private javax.swing.JComboBox variableCombo;
    private javax.swing.JCheckBox volatileCheckBox;
    // End of variables declaration//GEN-END:variables
    private JPanel initGutter;
    private JPanel declareGutter;

    private static final boolean[] variableValues = { false, true };
    private static final String[] variableStrings = {
        NbBundle.getMessage(CustomCodeView.class, "CTL_VariableCombo_Field"), // NOI18N
        NbBundle.getMessage(CustomCodeView.class, "CTL_VariableCombo_Local") }; // NOI18N
    private static final int[] accessValues = { Modifier.PRIVATE, 0, Modifier.PROTECTED, Modifier.PUBLIC };
    private static final String[] accessStrings = {
        "private", // NOI18N
        NbBundle.getMessage(CustomCodeView.class, "CTL_AccessCombo_package_private"), // NOI18N
        "protected", // NOI18N
        "public" }; // NOI18N
}
