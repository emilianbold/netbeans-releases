/*
 * SAXDocumentHandlerWizardPage3.java
 *
 * Created on 8/5/02 4:17 PM
 */
package org.netbeans.jellytools.modules.xml.saxwizard;

import org.netbeans.jellytools.WizardOperator;
import org.netbeans.jemmy.operators.*;

/** Class implementing all necessary methods for handling "SAX Document Handler Wizard" NbDialog.
 *
 * @author ms113234
 * @version 1.0
 */
public class SAXDocumentHandlerWizardPage3 extends WizardOperator {

    /** Creates new SAXDocumentHandlerWizardPage3 that can handle it.
     */
    public SAXDocumentHandlerWizardPage3() {
        super("SAX Document Handler Wizard");
    }

    private JLabelOperator _lblSteps;
    private JLabelOperator _lbl3Of4DataConvertorsOptional;
    private JTextAreaOperator _txtJTextArea;
    private JTableOperator _tabDataConvertors;
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

    /** Tries to find "3 of 4 - Data Convertors (Optional)" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lbl3Of4DataConvertorsOptional() {
        if (_lbl3Of4DataConvertorsOptional==null) {
            _lbl3Of4DataConvertorsOptional = new JLabelOperator(this, "3 of 4 - Data Convertors (Optional)");
        }
        return _lbl3Of4DataConvertorsOptional;
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

    /** Tries to find null SAXGeneratorParsletPanel$ParsletsTable in this dialog.
     * @return JTableOperator
     */
    public JTableOperator tabDataConvertors() {
        if (_tabDataConvertors==null) {
            _tabDataConvertors = new JTableOperator(this);
        }
        return _tabDataConvertors;
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

    /** Performs verification of SAXDocumentHandlerWizardPage3 by accessing all its components.
     */
    public void verify() {
        lblSteps();
        lbl3Of4DataConvertorsOptional();
        txtJTextArea();
        tabDataConvertors();
        btCancel();
        btHelp();
    }

    /** Performs simple test of SAXDocumentHandlerWizardPage3
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        new SAXDocumentHandlerWizardPage3().verify();
        System.out.println("SAXDocumentHandlerWizardPage3 verification finished.");
    }
}

