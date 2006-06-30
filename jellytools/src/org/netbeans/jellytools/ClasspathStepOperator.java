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
package org.netbeans.jellytools;

import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.Waiter;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import javax.swing.JDialog;
import javax.swing.JTextField;

/**
 * Handle "Classpath" panel of the New Project wizard
 * for J2SE Ant Project.<br>
 * Usage:
 * <pre>
 * </pre>
 * 
 * @author tb115823
 */
public class ClasspathStepOperator extends NewProjectWizardOperator {
    
    private JLabelOperator      _lblSourcePackageFolder;
    private JLabelOperator      _lblClasspath;
    private JButtonOperator     _btAddJARFolder;
    private JButtonOperator     _btRemove;
    private JLabelOperator      _lblOutputFolderOrJAR;
    private JTextFieldOperator  _txtOutputFolder;
    private JButtonOperator     _btBrowse;
    private JListOperator       _lstClasspath;
    private JComboBoxOperator   _cboSourcePackageFolder;
    private JButtonOperator     _btMoveUp;
    private JButtonOperator     _btMoveDown;
    private JLabelOperator      _lblOnlineError;


    

    /** Tries to find "Source Package Folder:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblSourcePackageFolder() {
        if (_lblSourcePackageFolder==null) {
            _lblSourcePackageFolder = new JLabelOperator(this, "Source Package Folder:");//TODO I18N
        }
        return _lblSourcePackageFolder;
    }

    /** Tries to find "Classpath:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblClasspath() {
        if (_lblClasspath==null) {
            _lblClasspath = new JLabelOperator(this, "Classpath:");//TODO I18N
        }
        return _lblClasspath;
    }

    /** Tries to find "Add JAR/Folder..." JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btAddJARFolder() {
        if (_btAddJARFolder==null) {
            _btAddJARFolder = new JButtonOperator(this, "Add JAR/Folder...");//TODO I18N
        }
        return _btAddJARFolder;
    }

    /** Tries to find "Remove" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btRemove() {
        if (_btRemove==null) {
            _btRemove = new JButtonOperator(this, "Remove");//TODO I18N
        }
        return _btRemove;
    }

    /** Tries to find "Output Folder or JAR:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblOutputFolderOrJAR() {
        if (_lblOutputFolderOrJAR==null) {
            _lblOutputFolderOrJAR = new JLabelOperator(this, "Output Folder or JAR:");//TODO I18N
        }
        return _lblOutputFolderOrJAR;
    }

    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtOutputFolder() {
        if (_txtOutputFolder==null) {
            _txtOutputFolder = new JTextFieldOperator((JTextField)lblOutputFolderOrJAR().getLabelFor());
        }
        return _txtOutputFolder;
    }

    /** Tries to find "Browse..." JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btBrowse() {
        if (_btBrowse==null) {
            _btBrowse = new JButtonOperator(this, "Browse...");//TODO I18N
        }
        return _btBrowse;
    }

    /** Tries to find null JList in this dialog.
     * @return JListOperator
     */
    public JListOperator lstClasspath() {
        if (_lstClasspath==null) {
            _lstClasspath = new JListOperator(this, 1);
        }
        return _lstClasspath;
    }

    /** Tries to find null JComboBox in this dialog.
     * @return JComboBoxOperator
     */
    public JComboBoxOperator cboSourcePackageFolder() {
        if (_cboSourcePackageFolder==null) {
            _cboSourcePackageFolder = new JComboBoxOperator(this);
        }
        return _cboSourcePackageFolder;
    }

    /** Tries to find "Move Up" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btMoveUp() {
        if (_btMoveUp==null) {
            _btMoveUp = new JButtonOperator(this, "Move Up");//TODO I18N
        }
        return _btMoveUp;
    }

    /** Tries to find "Move Down" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btMoveDown() {
        if (_btMoveDown==null) {
            _btMoveDown = new JButtonOperator(this, "Move Down");//TODO I18N
        }
        return _btMoveDown;
    }

    /** Tries to find " " JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblOnlineError() {
        if (_lblOnlineError==null) {
            _lblOnlineError = new JLabelOperator(this, 3);
        }
        return _lblOnlineError;
    }


    
    /** clicks on "Add JAR/Folder..." JButton
     */
    public void addJARFolder() {
        btAddJARFolder().push();
    }

    /** clicks on "Remove" JButton
     */
    public void remove() {
        btRemove().push();
    }

    /** gets text for txtOutputFolder
     * @return String text
     */
    public String getOutputFolder() {
        return txtOutputFolder().getText();
    }

    /** sets text for txtOutputFolder
     * @param text String text
     */
    public void setOutputFolder(String text) {
        txtOutputFolder().setText(text);
    }

    
    /** clicks on "Browse..." JButton
     */
    public void browse() {
        btBrowse().push();
    }

    /** returns selected item for cboSourcePackageFolder
     * @return String item
     */
    public String getSelectedSourcePackageFolder() {
        return cboSourcePackageFolder().getSelectedItem().toString();
    }

    /** selects item for cboSourcePackageFolder
     * @param item String item
     */
    public void selectSourcePackageFolder(String item) {
        cboSourcePackageFolder().selectItem(item);
    }

    /** clicks on "Move Up" JButton
     */
    public void moveUp() {
        btMoveUp().push();
    }

    /** clicks on "Move Down" JButton
     */
    public void moveDown() {
        btMoveDown().push();
    }

    /** selects classpath from the list Classpath:
     */
    public void selectClasspath(String classpath) { 
        lstClasspath().selectItem(classpath); 
    }

    
    /** Performs verification of ClasspathStepOperator by accessing all its components.
     */
    public void verify() {
        lblSourcePackageFolder();
        lblClasspath();
        btAddJARFolder();
        btRemove();
        lblOutputFolderOrJAR();
        txtOutputFolder();
        btBrowse();
        lstClasspath();
        cboSourcePackageFolder();
        btMoveUp();
        btMoveDown();
        lblOnlineError();
    }
    
}
