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

package org.netbeans.jellytools;

import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.JellyVersion;
import org.netbeans.jellytools.actions.DocumentsAction;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.jemmy.operators.JTextAreaOperator;

/**
 * Handle the dialog which is open from main menu "Window|Documents...".
 * This dialog shows list of all opened documents and it enables to close
 * or save selected documents. You can also activate one of documents.
 * <p>
 * Usage:<br>
 * <pre>
        DocumentsDialogOperator ddo = DocumentsDialogOperator.invoke();
        ddo.selectDocument("MyDocument");
        ddo.activate();
        ddo = DocumentsDialogOperator.invoke();
        ddo.selectDocuments(new int[] {0, 3, 4});
        ddo.closeDocuments();
        ddo.close();
 * </pre>
 *
 * @author Jiri.Skrivanek@sun.com
 * @see org.netbeans.jellytools.actions.DocumentsAction
 */
public class DocumentsDialogOperator extends NbDialogOperator {
    
    private JButtonOperator _btClose;
    private JButtonOperator _btActivate;
    private JButtonOperator _btCloseDocuments;
    private JButtonOperator _btSaveDocuments;
    private JTextAreaOperator _txtDescription;
    private JListOperator _lstDocuments;
    
    private static final String title = Bundle.getString("org.netbeans.core.windows.actions.Bundle", 
                                                         "CTL_Documents");
    
    /** Waits until dialog with title Documents is found. */
    public DocumentsDialogOperator() {
        super(title);
    }
    
    /** Invokes dialog from main menu "Windows|Documents...".
     * @return instance of DocumentsDialogOperator
     */
    public static DocumentsDialogOperator invoke() {
        new DocumentsAction().perform();
        return new DocumentsDialogOperator();
    }
    
    /** Returns operator of "Switch to Document" button.
     * @return  JButtonOperator instance of "Switch to Document" button
     */
    public JButtonOperator btSwitchToDocument() {
        if (_btActivate == null) {
            String switchCaption = Bundle.getString("org.netbeans.core.windows.view.ui.Bundle", 
                                                      "LBL_Activate");
            _btActivate = new JButtonOperator(this, switchCaption);
        }
        return _btActivate;
    }

    /** Returns operator of "Close Document(s)" button.
     * @return  JButtonOperator instance of "Close Document(s)" button
     */
    public JButtonOperator btCloseDocuments() {
        if (_btCloseDocuments == null) {
            String closeDocumentsCaption = Bundle.getString("org.netbeans.core.windows.view.ui.Bundle", 
                                                            "LBL_CloseDocuments");
            _btCloseDocuments = new JButtonOperator(this, closeDocumentsCaption);
        }
        return _btCloseDocuments;
    }

    /** Returns operator of "Save Document(s)" button.
     * @return  JButtonOperator instance of "Save Document(s)" button
     */
    public JButtonOperator btSaveDocuments() {
        if (_btSaveDocuments == null) {
            String saveDocumentsCaption = Bundle.getString("org.netbeans.core.windows.view.ui.Bundle", 
                                                           "LBL_SaveDocuments");
            _btSaveDocuments = new JButtonOperator(this, saveDocumentsCaption);
        }
        return _btSaveDocuments;
    }
    
    /** Returns operator of "Close" button.
     * @return  JButtonOperator instance of "Close" button
     */
    public JButtonOperator btClose() {
        if (_btClose == null) {
            String closeCaption = Bundle.getString("org.netbeans.core.windows.services.Bundle", 
                                                   "CLOSED_OPTION_CAPTION");
            // need to set exact matching comparator becouse Close button can 
            // be mistaken for Close Document(s) button
            StringComparator oldComparator = this.getComparator();
            this.setComparator(new DefaultStringComparator(true, true));
            _btClose = new JButtonOperator(this, closeCaption);
            this.setComparator(oldComparator);
        }
        return _btClose;
    }

    /** Returns operator of "Description" text area.
     * @return  JTextAreaOperator instance of "Description" text area
     */
    public JTextAreaOperator txtDescription() {
        if (_txtDescription == null) {
            _txtDescription = new JTextAreaOperator(this);
        }
        return _txtDescription;
    }

    /** Returns operator of list of documents.
     * @return  JListOperator instance of list of documents
     */
    public JListOperator lstDocuments() {
        if (_lstDocuments == null) {
            _lstDocuments = new JListOperator(this);
        }
        return _lstDocuments;
    }

    /** Pushes "Switch to Document" button. It closes the dialog and focuses selected
     * document in the editor area.
     */
    public void switchToDocument() {
        btSwitchToDocument().push();
    }
    
    /** Pushes "Close Document(s)" button. */
    public void closeDocuments() {
        btCloseDocuments().push();
    }
    
    /** Pushes "Save Document(s)" button. */
    public void saveDocuments() {
        btSaveDocuments().push();
    }
    
    /** Returns description text.
     * @return description text.
     */
    public String getDescription() {
        return txtDescription().getText();
    }
    
    /** Selects document with given name in the list.
     * @param name name of document to be selected
     */    
    public void selectDocument(String name) {
        lstDocuments().selectItem(name);
    }
    
    /** Selects index-th document in the list.
     * @param index index of document to be selected
     */    
    public void selectDocument(int index) {
        lstDocuments().selectItem(index);
    }

    /** Selects documents with given names in the list.
     * @param names names of document to be selected
     */    
    public void selectDocuments(String[] names) {
        lstDocuments().selectItem(names);
    }

    /** Selects documents with given indexes in the list.
     * @param indexes indexes of documents to be selected
     */    
    public void selectDocuments(int[] indexes) {
        lstDocuments().selectItems(indexes);
    }

    /** Performs verification by accessing all sub-components */    
    public void verify() {
        btSwitchToDocument();
        btCloseDocuments();
        btSaveDocuments();
        btClose();
        txtDescription();
        lstDocuments();
    }
}
