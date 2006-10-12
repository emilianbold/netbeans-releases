/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/*
 * ConfirmChanges.java
 *
 * Created on 9/3/02 8:12 PM
 */
package org.netbeans.jellytools.modules.xml.saxwizard;

import org.netbeans.jemmy.operators.*;

/** Class implementing all necessary methods for handling "Confirm Changes" NbDialog.
 *
 * @author ms113234
 * @version 1.0
 */
public class ConfirmChangesDialog extends JDialogOperator {

    /** Creates new ConfirmChanges that can handle it.
     */
    public ConfirmChangesDialog() {
        super("Confirm Changes");
    }

    private JTextAreaOperator _txtJTextArea;
    private JLabelOperator _lblChangesList;
    private JListOperator _lstChangesList;
    public static final String ITEM_ADDMETHODPUBLICABSTRACTVOIDCHARACTERSCHARINTINTTHROWSSAXEXCEPTIONFROMORGXMLSAXDOCUMENTHANDLER = "Add method public abstract void characters(char[], int, int) throws SAXException [from org.xml.sax.DocumentHandler]"; 
    public static final String ITEM_ADDMETHODPUBLICABSTRACTVOIDENDDOCUMENTTHROWSSAXEXCEPTIONFROMORGXMLSAXDOCUMENTHANDLER = "Add method public abstract void endDocument() throws SAXException [from org.xml.sax.DocumentHandler]"; 
    public static final String ITEM_ADDMETHODPUBLICABSTRACTVOIDENDELEMENTSTRINGTHROWSSAXEXCEPTIONFROMORGXMLSAXDOCUMENTHANDLER = "Add method public abstract void endElement(String) throws SAXException [from org.xml.sax.DocumentHandler]"; 
    public static final String ITEM_ADDMETHODPUBLICABSTRACTVOIDIGNORABLEWHITESPACECHARINTINTTHROWSSAXEXCEPTIONFROMORGXMLSAXDOCUMENTHANDLER = "Add method public abstract void ignorableWhitespace(char[], int, int) throws SAXException [from org.xml.sax.DocumentHandler]"; 
    public static final String ITEM_ADDMETHODPUBLICABSTRACTVOIDPROCESSINGINSTRUCTIONSTRINGSTRINGTHROWSSAXEXCEPTIONFROMORGXMLSAXDOCUMENTHANDLER = "Add method public abstract void processingInstruction(String, String) throws SAXException [from org.xml.sax.DocumentHandler]"; 
    public static final String ITEM_ADDMETHODPUBLICABSTRACTVOIDSETDOCUMENTLOCATORLOCATORFROMORGXMLSAXDOCUMENTHANDLER = "Add method public abstract void setDocumentLocator(Locator) [from org.xml.sax.DocumentHandler]"; 
    public static final String ITEM_ADDMETHODPUBLICABSTRACTVOIDSTARTDOCUMENTTHROWSSAXEXCEPTIONFROMORGXMLSAXDOCUMENTHANDLER = "Add method public abstract void startDocument() throws SAXException [from org.xml.sax.DocumentHandler]"; 
    public static final String ITEM_ADDMETHODPUBLICABSTRACTVOIDSTARTELEMENTSTRINGATTRIBUTELISTTHROWSSAXEXCEPTIONFROMORGXMLSAXDOCUMENTHANDLER = "Add method public abstract void startElement(String, AttributeList) throws SAXException [from org.xml.sax.DocumentHandler]"; 
    private JRadioButtonOperator _rbDoNotSynchronizeThisObjectNowToDoSoExplicitlyUseToolsSynchronize;
    private JRadioButtonOperator _rbConfirmAllChangesDuringSynchronization;
    private JRadioButtonOperator _rbPerformSynchronizationWithoutConfirmation;
    private JButtonOperator _btProcess;
    private JButtonOperator _btProcessAll;
    private JButtonOperator _btHelp;
    private JButtonOperator _btClose;


    //******************************
    // Subcomponents definition part
    //******************************

    /** Tries to find null JTextArea in this dialog.
     * @return JTextAreaOperator
     */
    public JTextAreaOperator txtJTextArea() {
        if (_txtJTextArea==null) {
            _txtJTextArea = new JTextAreaOperator(this);
        }
        return _txtJTextArea;
    }

    /** Tries to find "Changes List" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblChangesList() {
        if (_lblChangesList==null) {
            _lblChangesList = new JLabelOperator(this, "Changes List");
        }
        return _lblChangesList;
    }

    /** Tries to find null JList in this dialog.
     * @return JListOperator
     */
    public JListOperator lstChangesList() {
        if (_lstChangesList==null) {
            _lstChangesList = new JListOperator(this);
        }
        return _lstChangesList;
    }

    /** Tries to find "Do not synchronize this object now (to do so explicitly, use Tools | Synchronize)" JRadioButton in this dialog.
     * @return JRadioButtonOperator
     */
    public JRadioButtonOperator rbDoNotSynchronizeThisObjectNowToDoSoExplicitlyUseToolsSynchronize() {
        if (_rbDoNotSynchronizeThisObjectNowToDoSoExplicitlyUseToolsSynchronize==null) {
            _rbDoNotSynchronizeThisObjectNowToDoSoExplicitlyUseToolsSynchronize = new JRadioButtonOperator(this, "Do not synchronize this object now (to do so explicitly, use Tools | Synchronize)");
        }
        return _rbDoNotSynchronizeThisObjectNowToDoSoExplicitlyUseToolsSynchronize;
    }

    /** Tries to find "Confirm all changes during synchronization" JRadioButton in this dialog.
     * @return JRadioButtonOperator
     */
    public JRadioButtonOperator rbConfirmAllChangesDuringSynchronization() {
        if (_rbConfirmAllChangesDuringSynchronization==null) {
            _rbConfirmAllChangesDuringSynchronization = new JRadioButtonOperator(this, "Confirm all changes during synchronization");
        }
        return _rbConfirmAllChangesDuringSynchronization;
    }

    /** Tries to find "Perform synchronization without confirmation" JRadioButton in this dialog.
     * @return JRadioButtonOperator
     */
    public JRadioButtonOperator rbPerformSynchronizationWithoutConfirmation() {
        if (_rbPerformSynchronizationWithoutConfirmation==null) {
            _rbPerformSynchronizationWithoutConfirmation = new JRadioButtonOperator(this, "Perform synchronization without confirmation");
        }
        return _rbPerformSynchronizationWithoutConfirmation;
    }

    /** Tries to find "Process" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btProcess() {
        if (_btProcess==null) {
            _btProcess = new JButtonOperator(this, "Process");
        }
        return _btProcess;
    }

    /** Tries to find "Process All" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btProcessAll() {
        if (_btProcessAll==null) {
            _btProcessAll = new JButtonOperator(this, "Process All");
        }
        return _btProcessAll;
    }

    /** Tries to find "Help" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btHelp() {
        if (_btHelp==null) {
            _btHelp = new JButtonOperator(this, "Help");
        }
        return _btHelp;
    }

    /** Tries to find "Close" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btClose() {
        if (_btClose==null) {
            _btClose = new JButtonOperator(this, "Close");
        }
        return _btClose;
    }


    //****************************************
    // Low-level functionality definition part
    //****************************************

    /** gets text for txtJTextArea
     * @return String text
     */
    public String getJTextArea() {
        return txtJTextArea().getText();
    }

    /** sets text for txtJTextArea
     * @param text String text
     */
    public void setJTextArea(String text) {
        txtJTextArea().setText(text);
    }

    /** types text for txtJTextArea
     * @param text String text
     */
    public void typeJTextArea(String text) {
        txtJTextArea().typeText(text);
    }

    /** clicks on "Do not synchronize this object now (to do so explicitly, use Tools | Synchronize)" JRadioButton
     */
    public void doNotSynchronizeThisObjectNowToDoSoExplicitlyUseToolsSynchronize() {
        rbDoNotSynchronizeThisObjectNowToDoSoExplicitlyUseToolsSynchronize().push();
    }

    /** clicks on "Confirm all changes during synchronization" JRadioButton
     */
    public void confirmAllChangesDuringSynchronization() {
        rbConfirmAllChangesDuringSynchronization().push();
    }

    /** clicks on "Perform synchronization without confirmation" JRadioButton
     */
    public void performSynchronizationWithoutConfirmation() {
        rbPerformSynchronizationWithoutConfirmation().push();
    }

    /** clicks on "Process" JButton
     */
    public void process() {
        btProcess().push();
    }

    /** clicks on "Process All" JButton
     */
    public void processAll() {
        btProcessAll().push();
    }

    /** clicks on "Help" JButton
     */
    public void help() {
        btHelp().push();
    }

    /** clicks on "Close" JButton
     */
    public void close() {
        btClose().push();
    }


    //*****************************************
    // High-level functionality definition part
    //*****************************************

    /** Performs verification of ConfirmChanges by accessing all its components.
     */
    public void verify() {
        txtJTextArea();
        lblChangesList();
        lstChangesList();
        rbDoNotSynchronizeThisObjectNowToDoSoExplicitlyUseToolsSynchronize();
        rbConfirmAllChangesDuringSynchronization();
        rbPerformSynchronizationWithoutConfirmation();
        btProcess();
        btProcessAll();
        btHelp();
        btClose();
    }

    /** Performs simple test of ConfirmChanges
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        new ConfirmChangesDialog().verify();
        System.out.println("ConfirmChanges verification finished.");
    }
}

