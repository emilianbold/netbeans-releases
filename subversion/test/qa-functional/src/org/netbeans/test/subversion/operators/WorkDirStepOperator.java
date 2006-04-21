/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/*
 * WorkDirStepOperator.java
 *
 * Created on 19/04/06 13:25
 */
package org.netbeans.test.subversion.operators;

import org.netbeans.jellytools.WizardOperator;
import org.netbeans.jemmy.operators.*;
import org.netbeans.jemmy.util.NameComponentChooser;

/**
 * Class implementing all necessary methods for handling "WorkDirStepOperator" NbDialog.
 * 
 * 
 * @author peter
 * @version 1.0
 */
public class WorkDirStepOperator extends WizardOperator {

    /**
     * Creates new WorkDirStepOperator that can handle it.
     */
    public WorkDirStepOperator() {
        super("");
        stepsWaitSelectedValue("Workdir");
    }

    private JLabelOperator _lblSteps;
    private JListOperator _lstSteps;
    private JLabelOperator _lblWorkdir;
    private JLabelOperator _lblSpecifyTheFoldersToCheckoutFromSubversionRepository;
    private JLabelOperator _lblRepositoryRevision;
    private JLabelOperator _lblRepositoryFolders;
    private JTextFieldOperator _txtJTextField;
    private JButtonOperator _btSearch;
    private JLabelOperator _lblLocalSubversionWorkingCopy;
    private JTextFieldOperator _txtJTextField2;
    private JLabelOperator _lblEmptyMeansRepositoryHEAD;
    private JTextFieldOperator _txtJTextField3;
    private JButtonOperator _btBrowse;
    private JButtonOperator _btBrowse2;
    private JLabelOperator _lblLocalFolder;
    private JLabelOperator _lblSpecifyTheLocalFolderToCheckoutFoldersInto;
    private JLabelOperator _lblWizardDescriptor$FixedHeightLabel;
    private JButtonOperator _btBack;
    private JButtonOperator _btNext;
    private JButtonOperator _btFinish;
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

    /** Tries to find null JList in this dialog.
     * @return JListOperator
     */
    public JListOperator lstSteps() {
        if (_lstSteps==null) {
            _lstSteps = new JListOperator(this);
        }
        return _lstSteps;
    }

    /** Tries to find "Workdir" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblWorkdir() {
        if (_lblWorkdir==null) {
            _lblWorkdir = new JLabelOperator(this, "Workdir");
        }
        return _lblWorkdir;
    }

    /** Tries to find "Specify the folder(s) to checkout from Subversion repository." JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblSpecifyTheFoldersToCheckoutFromSubversionRepository() {
        if (_lblSpecifyTheFoldersToCheckoutFromSubversionRepository==null) {
            _lblSpecifyTheFoldersToCheckoutFromSubversionRepository = new JLabelOperator(this, "Specify the folder(s) to checkout from Subversion repository.");
        }
        return _lblSpecifyTheFoldersToCheckoutFromSubversionRepository;
    }

    /** Tries to find "Repository Revision:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblRepositoryRevision() {
        if (_lblRepositoryRevision==null) {
            _lblRepositoryRevision = new JLabelOperator(this, "Repository Revision:");
        }
        return _lblRepositoryRevision;
    }

    /** Tries to find "Repository Folder(s):" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblRepositoryFolders() {
        if (_lblRepositoryFolders==null) {
            _lblRepositoryFolders = new JLabelOperator(this, "Repository Folder(s):");
        }
        return _lblRepositoryFolders;
    }

    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtJTextField() {
        if (_txtJTextField==null) {
            _txtJTextField = new JTextFieldOperator(this);
        }
        return _txtJTextField;
    }

    /** Tries to find "Search..." JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btSearch() {
        if (_btSearch==null) {
            _btSearch = new JButtonOperator(this, "Search...");
        }
        return _btSearch;
    }

    /** Tries to find "(local Subversion working copy) " JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblLocalSubversionWorkingCopy() {
        if (_lblLocalSubversionWorkingCopy==null) {
            _lblLocalSubversionWorkingCopy = new JLabelOperator(this, "(local Subversion working copy) ");
        }
        return _lblLocalSubversionWorkingCopy;
    }

    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtJTextField2() {
        if (_txtJTextField2==null) {
            _txtJTextField2 = new JTextFieldOperator(this, 1);
        }
        return _txtJTextField2;
    }

    /** Tries to find "(empty means repository HEAD)" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblEmptyMeansRepositoryHEAD() {
        if (_lblEmptyMeansRepositoryHEAD==null) {
            _lblEmptyMeansRepositoryHEAD = new JLabelOperator(this, "(empty means repository HEAD)");
        }
        return _lblEmptyMeansRepositoryHEAD;
    }

    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtJTextField3() {
        if (_txtJTextField3==null) {
            _txtJTextField3 = new JTextFieldOperator(this, 2);
        }
        return _txtJTextField3;
    }

    /** Tries to find "Browse..." JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btBrowse() {
        if (_btBrowse==null) {
            _btBrowse = new JButtonOperator(this, "Browse...");
        }
        return _btBrowse;
    }

    /** Tries to find "Browse..." JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btBrowse2() {
        if (_btBrowse2==null) {
            _btBrowse2 = new JButtonOperator(this, "Browse...", 1);
        }
        return _btBrowse2;
    }

    /** Tries to find "Local Folder:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblLocalFolder() {
        if (_lblLocalFolder==null) {
            _lblLocalFolder = new JLabelOperator(this, "Local Folder:");
        }
        return _lblLocalFolder;
    }

    /** Tries to find "Specify the local folder to checkout folders into." JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblSpecifyTheLocalFolderToCheckoutFoldersInto() {
        if (_lblSpecifyTheLocalFolderToCheckoutFoldersInto==null) {
            _lblSpecifyTheLocalFolderToCheckoutFoldersInto = new JLabelOperator(this, "Specify the local folder to checkout folders into.");
        }
        return _lblSpecifyTheLocalFolderToCheckoutFoldersInto;
    }

    /** Tries to find " " WizardDescriptor$FixedHeightLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblWizardDescriptor$FixedHeightLabel() {
        if (_lblWizardDescriptor$FixedHeightLabel==null) {
            _lblWizardDescriptor$FixedHeightLabel = new JLabelOperator(this, " ", 7);
        }
        return _lblWizardDescriptor$FixedHeightLabel;
    }

    /** Tries to find "< Back" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btBack() {
        if (_btBack==null) {
            _btBack = new JButtonOperator(this, "< Back");
        }
        return _btBack;
    }

    /** Tries to find "Next >" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btNext() {
        if (_btNext==null) {
            _btNext = new JButtonOperator(this, "Next >");
        }
        return _btNext;
    }

    /** Tries to find "Finish" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btFinish() {
        if (_btFinish==null) {
            _btFinish = new JButtonOperator(this, "Finish");
        }
        return _btFinish;
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

    /** gets text for txtJTextField
     * @return String text
     */
    public String getJTextField() {
        return txtJTextField().getText();
    }

    /** sets text for txtJTextField
     * @param text String text
     */
    public void setJTextField(String text) {
        txtJTextField().setText(text);
    }

    /** types text for txtJTextField
     * @param text String text
     */
    public void typeJTextField(String text) {
        txtJTextField().typeText(text);
    }

    /** clicks on "Search..." JButton
     */
    public void search() {
        btSearch().push();
    }

    /** gets text for txtJTextField2
     * @return String text
     */
    public String getJTextField2() {
        return txtJTextField2().getText();
    }

    /** sets text for txtJTextField2
     * @param text String text
     */
    public void setJTextField2(String text) {
        txtJTextField2().setText(text);
    }

    /** types text for txtJTextField2
     * @param text String text
     */
    public void typeJTextField2(String text) {
        txtJTextField2().typeText(text);
    }

    /** gets text for txtJTextField3
     * @return String text
     */
    public String getJTextField3() {
        return txtJTextField3().getText();
    }

    /** sets text for txtJTextField3
     * @param text String text
     */
    public void setJTextField3(String text) {
        txtJTextField3().setText(text);
    }

    /** types text for txtJTextField3
     * @param text String text
     */
    public void typeJTextField3(String text) {
        txtJTextField3().typeText(text);
    }

    /** clicks on "Browse..." JButton
     */
    public void browse() {
        btBrowse().push();
    }

    /** clicks on "Browse..." JButton
     */
    public void browse2() {
        btBrowse2().push();
    }

    /** clicks on "< Back" JButton
     */
    public void back() {
        btBack().push();
    }

    /** clicks on "Next >" JButton
     */
    public void next() {
        btNext().push();
    }

    /** clicks on "Finish" JButton
     */
    public void finish() {
        btFinish().push();
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

    /**
     * Performs verification of WorkDirStepOperator by accessing all its components.
     */
    public void verify() {
        lblSteps();
        lstSteps();
        lblWorkdir();
        lblSpecifyTheFoldersToCheckoutFromSubversionRepository();
        lblRepositoryRevision();
        lblRepositoryFolders();
        txtJTextField();
        btSearch();
        lblLocalSubversionWorkingCopy();
        txtJTextField2();
        lblEmptyMeansRepositoryHEAD();
        txtJTextField3();
        btBrowse();
        btBrowse2();
        lblLocalFolder();
        lblSpecifyTheLocalFolderToCheckoutFoldersInto();
        lblWizardDescriptor$FixedHeightLabel();
        btBack();
        btNext();
        btFinish();
        btCancel();
        btHelp();
    }

    /**
     * Performs simple test of WorkDirStepOperator
     * 
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        new WorkDirStepOperator().verify();
        System.out.println("Checkout2StepOperator verification finished.");
    }
}

