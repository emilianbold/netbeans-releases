/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.test.subversion.operators;

import org.netbeans.jemmy.operators.*;

/** Class implementing all necessary methods for handling "Merge AnagramGame to..." NbDialog.
 *
 * @version 1.0
 */
public class MergeOriginOperator extends JDialogOperator {

    /**
     * Creates new MergeOriginOperator that can handle it.
     */
    public MergeOriginOperator() {
        super("Merge");
    }

    private JLabelOperator _lblRepositoryFolder;
    private JLabelOperator _lblEndingWithRevision;
    private JLabelOperator _lblEmptyMeansRepositoryHEAD;
    private JLabelOperator _lblMergeIntoLocalFolderChangesFromOneRepositoryFolderSinceItsOrigin;
    private JLabelOperator _lblMergeFrom;
    private JLabelOperator _lblJLabel;
    private JLabelOperator _lblPreview;
    private JButtonOperator _btSearch;
    private JTextFieldOperator _txtEndRevision;
    private JComboBoxOperator _cboMergeFrom;
    private JButtonOperator _btBrowse;
    private JComboBoxOperator _cboRepository;
    private JTextFieldOperator _txtRepostiryFolder;
    private JTextFieldOperator _txtCurrentFolder;
    private JButtonOperator _btMerge;
    private JButtonOperator _btCancel;
    private JButtonOperator _btHelp;

    //******************************
    // Subcomponents definition part
    //******************************

    /** Tries to find "Repository Folder:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblRepositoryFolder() {
        if (_lblRepositoryFolder==null) {
            _lblRepositoryFolder = new JLabelOperator(this, "Repository Folder");
        }
        return _lblRepositoryFolder;
    }

    /** Tries to find "Ending with Revision:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblEndingWithRevision() {
        if (_lblEndingWithRevision==null) {
            _lblEndingWithRevision = new JLabelOperator(this, "Ending");
        }
        return _lblEndingWithRevision;
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


    /** Tries to find "Merge into local folder changes from one repository folder since its origin." JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblMergeIntoLocalFolderChangesFromOneRepositoryFolderSinceItsOrigin() {
        if (_lblMergeIntoLocalFolderChangesFromOneRepositoryFolderSinceItsOrigin==null) {
            _lblMergeIntoLocalFolderChangesFromOneRepositoryFolderSinceItsOrigin = new JLabelOperator(this, "Merge into local folder changes from one repository folder since its origin.");
        }
        return _lblMergeIntoLocalFolderChangesFromOneRepositoryFolderSinceItsOrigin;
    }

    /** Tries to find "Merge from:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblMergeFrom() {
        if (_lblMergeFrom==null) {
            _lblMergeFrom = new JLabelOperator(this, "Merge from:");
        }
        return _lblMergeFrom;
    }

    /** Tries to find null JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblJLabel() {
        if (_lblJLabel==null) {
            _lblJLabel = new JLabelOperator(this, 5);
        }
        return _lblJLabel;
    }

    /** Tries to find "Preview:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblPreview() {
        if (_lblPreview==null) {
            _lblPreview = new JLabelOperator(this, "Preview:");
        }
        return _lblPreview;
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
    
    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtEndRevision() {
        if (_txtEndRevision==null) {
            _txtEndRevision = new JTextFieldOperator(this);
        }
        return _txtEndRevision;
    }
    
    /** Tries to find null JComboBox in this dialog.
     * @return JComboBoxOperator
     */
    public JComboBoxOperator cboJComboBox() {
        if (_cboMergeFrom==null) {
            _cboMergeFrom = new JComboBoxOperator(this, 1);
        }
        return _cboMergeFrom;
    }

    /** Tries to find "Browse..." JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btBrowse() {
        if (_btBrowse==null) {
            _btBrowse = new JButtonOperator(this, "Browse");
        }
        return _btBrowse;
    }
        
    /** Tries to find null JComboBox in this dialog.
     * @return JComboBoxOperator
     */
    public JComboBoxOperator cboRepository() {
        if (_cboRepository==null) {
            _cboRepository = new JComboBoxOperator(this);
        }
        return _cboRepository;
    }
    
    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtRepostiryFolder() {
        if (_txtRepostiryFolder==null) {
            _txtRepostiryFolder = new JTextFieldOperator(this, 2);
        }
        return _txtRepostiryFolder;
    }

    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtCurrentFolder() {
        if (_txtCurrentFolder==null) {
            _txtCurrentFolder = new JTextFieldOperator(this, 3);
        }
        return _txtCurrentFolder;
    }
    
    /** Tries to find "Merge" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btMerge() {
        if (_btMerge==null) {
            _btMerge = new JButtonOperator(this, "Merge");
        }
        return _btMerge;
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
    
    /**
     * gets text for txtEndRevision
     * 
     * @return String text
     */
    public String getEndRevision() {
        return txtEndRevision().getText();
    }

    /**
     * sets text for txtEndRevision
     * 
     * @param text String text
     */
    public void setEndRevision(String text) {
        txtEndRevision().clearText();
        txtEndRevision().typeText(text);
    }
    
    /** selects item for cboJComboBox
     * @param item String item
     */
    public void selectMergeFrom(String item) {
        cboJComboBox().selectItem(item);
    }

    /** types text for cboJComboBox
     * @param text String text
     */
    public void setMergeFrom(String text) {
        cboJComboBox().clearText();
        cboJComboBox().typeText(text);
    }

    /** clicks on "Browse..." JButton
     */
    public RepositoryBrowserOperator browseRepositoryFolder() {
        btBrowse().pushNoBlock();
        return new RepositoryBrowserOperator();
    }

    /**
     * returns selected item for cboRepository
     * 
     * @return String item
     */
    public String getSelectedRepository() {
        return cboRepository().getSelectedItem().toString();
    }
    
    public String getRepositoryFolder() {
        return cboRepository().getEditor().getItem().toString();
    }

    /**
     * selects item for cboRepository
     * 
     * @param item String item
     */
    public void selectRepository(String item) {
        cboRepository().selectItem(item);
    }
    
    public void setRepository(String text) {
        cboRepository().clearText();
        cboRepository().typeText(text);
    }

    /**
     * gets text for txtRepostiryFolder
     * 
     * @return String text
     */
    public String getTxtRepositoryFolder() {
        return txtRepostiryFolder().getText();
    }

    /**
     * gets text for txtCurrentFolder
     * 
     * @return String text
     */
    public String getTxtCurrentFolder() {
        return txtCurrentFolder().getText();
    }
    
    /** clicks on "Merge" JButton
     */
    public void merge() {
        btMerge().push();
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
     * Performs verification of MergeOriginOperator by accessing all its components.
     */
    public void verify() {
        lblRepositoryFolder();
        lblEndingWithRevision();
        lblEmptyMeansRepositoryHEAD();
        lblMergeIntoLocalFolderChangesFromOneRepositoryFolderSinceItsOrigin();
        lblMergeFrom();
        lblJLabel();
        lblPreview();
        btSearch();
        txtEndRevision();
        cboJComboBox();
        btBrowse();
        cboRepository();
    }
}

