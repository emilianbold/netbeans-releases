/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.editor.java;

import java.awt.event.ActionEvent;
import java.util.MissingResourceException;
import java.util.Stack;
import javax.swing.Action;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.Caret;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.mdr.MDRepository;
import org.netbeans.editor.BaseAction;
import org.netbeans.jmi.javamodel.Element;
import org.netbeans.jmi.javamodel.Resource;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.javacore.internalapi.JavaMetamodel;
import org.openide.loaders.DataObject;

/**
* Java editor kit with appropriate document
*
* @author Miloslav Metelka
* @version 1.00
*/

class SelectCodeElementAction extends BaseAction {

    private boolean selectNext;

    /**
     * Construct new action that selects next/previous code elements
     * according to the language model.
     * <br>
     *
     * @param name name of the action (should be one of
     *  <br>
     *  <code>JavaKit.selectNextElementAction</code>
     *  <code>JavaKit.selectPreviousElementAction</code>
     * @param selectNext <code>true</code> if the next element should be selected.
     *  <code>False</code> if the previous element should be selected.
     */
    public SelectCodeElementAction(String name, boolean selectNext) {
        super(name);
        this.selectNext = selectNext;
        String desc = getShortDescription();
        if (desc != null) {
            putValue(SHORT_DESCRIPTION, desc);
        }
    }
        
    public String getShortDescription(){
        String name = (String)getValue(Action.NAME);
        if (name == null) return null;
        String shortDesc;
        try {
            shortDesc = org.openide.util.NbBundle.getBundle (JavaKit.class).getString(name); // NOI18N
        }catch (MissingResourceException mre){
            shortDesc = name;
        }
        return shortDesc;
    }
    
    public void actionPerformed(ActionEvent evt, JTextComponent target) {
        if (target != null) {
            int selectionStartOffset = target.getSelectionStart();
            int selectionEndOffset = target.getSelectionEnd();
            if (selectionEndOffset > selectionStartOffset || selectNext) {
                SelectionHandler handler = (SelectionHandler)target.getClientProperty(SelectionHandler.class);
                if (handler == null) {
                    handler = new SelectionHandler(target);
                    target.addCaretListener(handler);
                    // No need to remove the listener above as the handler
                    // is stored is the client-property of the component itself
                    target.putClientProperty(SelectionHandler.class, handler);
                }
                
                if (!selectNext) { // select previous
                    if (!handler.isEmpty()) {
                        handler.popSelectionInfo(); // the last pushed - want to skip this one
                        handler.selectTop(); // select top if exists
                    }

                } else { // select next element
                    DataObject dob = NbEditorUtilities.getDataObject(target.getDocument());
                    if (dob != null) {
                        JavaMetamodel manager = JavaMetamodel.getManager();
                        MDRepository repository = JavaMetamodel.getDefaultRepository();
                        repository.beginTrans(true); // write access to enforce reparsing if necessary
                        try {
                            //Resource resource = manager.getResource(dob.getPrimaryFile());
                            Element elem;
                            if (handler.isEmpty()) {
                                // Push the initial selection (or non-selection)
                                handler.pushSelectionInfo(
                                    new SelectionInfo(selectionStartOffset, selectionEndOffset, null));
                            }

                            elem = handler.peekSelectionInfo().getElement();
                            if (!(elem instanceof Resource)) {
                                if (elem != null) {
                                    elem = (Element)elem.refImmediateComposite();
                                } else { // initial state
                                    elem = manager.getElementByOffset(dob.getPrimaryFile(), selectionStartOffset);
                                }

                                if (elem != null) {
                                    selectionStartOffset = elem.getStartOffset();
                                    selectionEndOffset = elem.getEndOffset();
                                    
                                    // Additional patching of selection bounds
                                    if (elem instanceof Resource) {
                                        // should extend the bounds to full source (including initial comment etc.)
                                        selectionStartOffset = 0;
                                        selectionEndOffset = target.getDocument().getLength();
                                    }

                                    handler.pushSelectionInfo(
                                        new SelectionInfo(selectionStartOffset, selectionEndOffset, elem));
                                    handler.selectTop();

                                }
                            }
                        } finally {
                            repository.endTrans();
                        }
                    }
                }
            }
        }
    }

    private static final class SelectionHandler implements CaretListener {
        
        private JTextComponent target;

        private Stack selectionInfoStack;
        
        private boolean ignoreNextCaretUpdate;
        
        SelectionHandler(JTextComponent target) {
            this.target = target;
            selectionInfoStack = new Stack();
        }

        public void pushSelectionInfo(SelectionInfo selectionInfo) {
            selectionInfoStack.push(selectionInfo);
        }
        
        public SelectionInfo popSelectionInfo() {
            return (SelectionInfo)selectionInfoStack.pop();
        }
        
        public SelectionInfo peekSelectionInfo() {
            return (SelectionInfo)selectionInfoStack.peek();
        }
        
        public boolean isEmpty() {
            return selectionInfoStack.empty();
        }

        public void selectTop() {
            if (!selectionInfoStack.empty()) {
                SelectionInfo top = peekSelectionInfo();
                Caret caret = target.getCaret();
                markIgnoreNextCaretUpdate();
                caret.setDot(top.getStartOffset());
                markIgnoreNextCaretUpdate();
                caret.moveDot(top.getEndOffset());
            }
        }
        
        private void markIgnoreNextCaretUpdate() {
            ignoreNextCaretUpdate = true;
        }
        
        public void caretUpdate(CaretEvent e) {
            if (!ignoreNextCaretUpdate) {
                selectionInfoStack.clear();
            }
            ignoreNextCaretUpdate = false;
        }
        
    }
    
    private static final class SelectionInfo {
        
        private int startOffset;
        private int endOffset;
        private Element element;
        
        SelectionInfo(int startOffset, int endOffset, Element element) {
            this.startOffset = startOffset;
            this.endOffset = endOffset;
            this.element = element;
        }
        
        public int getStartOffset() {
            return startOffset;
        }
        
        public int getEndOffset() {
            return endOffset;
        }

        public Element getElement() {
            return element;
        }
        
    }
}
