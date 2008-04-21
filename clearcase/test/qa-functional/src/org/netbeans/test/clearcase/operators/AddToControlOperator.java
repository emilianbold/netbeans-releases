/*
 * AddToControlOperator.java
 *
 * Created on 05/03/08 17:07
 */
package org.netbeans.test.clearcase.operators;

import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.*;
import org.netbeans.jemmy.util.NameComponentChooser;
import org.netbeans.test.clearcase.operators.actions.AddToControlAction;

/** Class implementing all necessary methods for handling "Add - CCtest" NbDialog.
 *
 * @author peter
 * @version 1.0
 */
public class AddToControlOperator extends NbDialogOperator {

    /** Creates new AddToControlOperator that can handle it.
     */
    public AddToControlOperator() {
        super("Add");
    }

    private JTextAreaOperator _txtJTextArea;
    private JTableOperator _tabFilesToAdd;
    private JButtonOperator _btWindowsScrollBarUI$WindowsArrowButton;
    private JButtonOperator _btWindowsScrollBarUI$WindowsArrowButton2;
    private JCheckBoxOperator _cbCheckinAddedFiles;
    private JLabelOperator _lblDescribingMessage;
    private JButtonOperator _btJButton;
    private JLabelOperator _lblFilesToAdd;
    private JButtonOperator _btAdd;
    private JButtonOperator _btCancel;
    private JButtonOperator _btHelp;
  
    public static AddToControlOperator invoke(Node[] nodes) {
        new AddToControlAction().perform(nodes);
        return new AddToControlOperator();
    }
    
    public static AddToControlOperator invoke(Node node) {
        return invoke(new Node[] {node});
    }

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

    /** Tries to find null JTable in this dialog.
     * @return JTableOperator
     */
    public JTableOperator tabFilesToAdd() {
        if (_tabFilesToAdd==null) {
            _tabFilesToAdd = new JTableOperator(this);
        }
        return _tabFilesToAdd;
    }

    /** Tries to find null WindowsScrollBarUI$WindowsArrowButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btWindowsScrollBarUI$WindowsArrowButton() {
        if (_btWindowsScrollBarUI$WindowsArrowButton==null) {
            _btWindowsScrollBarUI$WindowsArrowButton = new JButtonOperator(this);
        }
        return _btWindowsScrollBarUI$WindowsArrowButton;
    }

    /** Tries to find null WindowsScrollBarUI$WindowsArrowButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btWindowsScrollBarUI$WindowsArrowButton2() {
        if (_btWindowsScrollBarUI$WindowsArrowButton2==null) {
            _btWindowsScrollBarUI$WindowsArrowButton2 = new JButtonOperator(this, 1);
        }
        return _btWindowsScrollBarUI$WindowsArrowButton2;
    }

    /** Tries to find "Checkin Added Files" JCheckBox in this dialog.
     * @return JCheckBoxOperator
     */
    public JCheckBoxOperator cbCheckinAddedFiles() {
        if (_cbCheckinAddedFiles==null) {
            _cbCheckinAddedFiles = new JCheckBoxOperator(this, "Checkin Added Files");
        }
        return _cbCheckinAddedFiles;
    }

    /** Tries to find "Describing Message:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblDescribingMessage() {
        if (_lblDescribingMessage==null) {
            _lblDescribingMessage = new JLabelOperator(this, "Describing Message:");
        }
        return _lblDescribingMessage;
    }

    /** Tries to find null JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btJButton() {
        if (_btJButton==null) {
            _btJButton = new JButtonOperator(this, 2);
        }
        return _btJButton;
    }

    /** Tries to find "Files To Add:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblFilesToAdd() {
        if (_lblFilesToAdd==null) {
            _lblFilesToAdd = new JLabelOperator(this, "Files To Add:");
        }
        return _lblFilesToAdd;
    }

    /** Tries to find "Add" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btAdd() {
        if (_btAdd==null) {
            _btAdd = new JButtonOperator(this, "Add");
        }
        return _btAdd;
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

    /** clicks on null WindowsScrollBarUI$WindowsArrowButton
     */
    public void windowsScrollBarUI$WindowsArrowButton() {
        btWindowsScrollBarUI$WindowsArrowButton().push();
    }

    /** clicks on null WindowsScrollBarUI$WindowsArrowButton
     */
    public void windowsScrollBarUI$WindowsArrowButton2() {
        btWindowsScrollBarUI$WindowsArrowButton2().push();
    }

    /** checks or unchecks given JCheckBox
     * @param state boolean requested state
     */
    public void checkCheckinAddedFiles(boolean state) {
        if (cbCheckinAddedFiles().isSelected()!=state) {
            cbCheckinAddedFiles().push();
        }
    }

    /** clicks on null JButton
     */
    public void jButton() {
        btJButton().push();
    }

    /** clicks on "Add" JButton
     */
    public void add() {
        btAdd().push();
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

    /** Performs verification of AddToControlOperator by accessing all its components.
     */
    public void verify() {
        txtJTextArea();
        tabFilesToAdd();
        btWindowsScrollBarUI$WindowsArrowButton();
        btWindowsScrollBarUI$WindowsArrowButton2();
        cbCheckinAddedFiles();
        lblDescribingMessage();
        btJButton();
        lblFilesToAdd();
        btAdd();
        btCancel();
        btHelp();
    }

    /** Performs simple test of AddToControlOperator
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        new AddToControlOperator().verify();
        System.out.println("AddCCtest verification finished.");
    }
}

