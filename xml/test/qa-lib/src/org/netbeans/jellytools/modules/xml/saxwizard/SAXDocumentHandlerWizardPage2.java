/*
 * SAXDocumentHandlerWizard2.java
 *
 * Created on 8/5/02 4:15 PM
 */
package org.netbeans.jellytools.modules.xml.saxwizard;

import org.netbeans.jellytools.WizardOperator;
import org.netbeans.jemmy.operators.*;

/** Class implementing all necessary methods for handling "SAX Document Handler Wizard" NbDialog.
 *
 * @author ms113234
 * @version 1.0
 */
public class SAXDocumentHandlerWizardPage2 extends WizardOperator {

    /** Creates new SAXDocumentHandlerWizard2 that can handle it.
     */
    public SAXDocumentHandlerWizardPage2() {
        super("SAX Document Handler Wizard");
    }

    private JLabelOperator _lblSteps;
    private JLabelOperator _lbl2Of4ElementMappings;
    private JTextAreaOperator _txtJTextArea;
    private JTableOperator _tabElementMappings;
    private JButtonOperator _btCancel;
    private JButtonOperator _btHelp;


    //******************************
    // Subcomponents definition part
    //******************************

    /** Tries to find "Steps" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblSteps() {
        if (_lblSteps==null) {
            _lblSteps = new JLabelOperator(this, "Steps");
        }
        return _lblSteps;
    }

    /** Tries to find "2 of 4 - Element Mappings" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lbl2Of4ElementMappings() {
        if (_lbl2Of4ElementMappings==null) {
            _lbl2Of4ElementMappings = new JLabelOperator(this, "2 of 4 - Element Mappings");
        }
        return _lbl2Of4ElementMappings;
    }

    /** Tries to find null JTextArea in this dialog.
     * @return JTextAreaOperator
     */
    public JTextAreaOperator txtJTextArea() {
        if (_txtJTextArea==null) {
            _txtJTextArea = new JTextAreaOperator(this);
        }
        return _txtJTextArea;
    }

    /** Tries to find null SAXGeneratorMethodPanel$MethodsTable in this dialog.
     * @return JTableOperator
     */
    public JTableOperator tabElementMappings() {
        if (_tabElementMappings==null) {
            _tabElementMappings = new JTableOperator(this);
        }
        return _tabElementMappings;
    }

    /** Tries to find "Cancel" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btCancel() {
        if (_btCancel==null) {
            _btCancel = new JButtonOperator(this, "Cancel");
        }
        return _btCancel;
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

    /** clicks on "Cancel" JButton
     */
    public void cancel() {
        btCancel().push();
    }

    /** clicks on "Help" JButton
     */
    public void help() {
        btHelp().push();
    }


    //*****************************************
    // High-level functionality definition part
    //*****************************************

    /** Performs verification of SAXDocumentHandlerWizard2 by accessing all its components.
     */
    public void verify() {
        lblSteps();
        lbl2Of4ElementMappings();
        txtJTextArea();
        tabElementMappings();
        btCancel();
        btHelp();
    }

    /** Performs simple test of SAXDocumentHandlerWizard2
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        new SAXDocumentHandlerWizardPage2().verify();
        System.out.println("SAXDocumentHandlerWizardPage2 verification finished.");
    }
}

