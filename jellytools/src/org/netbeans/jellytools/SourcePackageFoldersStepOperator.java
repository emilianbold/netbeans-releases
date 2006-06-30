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

import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.Waiter;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JTableOperator;
import javax.swing.JDialog;

/**
 * Handle "Source Packages and Folders" panel of the New Project wizard for
 * J2SE Ant Project.<br>
 * Usage:
 * <pre>
 * SourcePackageFoldersStepOperator spop = new SourcePackageFoldersStepOperator();
 * spop.addFolder();
 * 
 * spop.tblSourcePackageFolders().selectCell(0,0);
 * spop.remove();
 * spop.selectSourceLevel("JDK");
 * System.out.println(spop.lblOnlineError().getText());
 * </pre>
 *
 * @author tb115823
 */
public class SourcePackageFoldersStepOperator extends NewProjectWizardOperator {
    
    private JLabelOperator _lblSpecifyFolders;
    private JLabelOperator _lblSourcePackageFolders;
    private JLabelOperator _lblSourceLevel;
    private JComboBoxOperator _cboSourceLevel;
    private JButtonOperator _btAddFolder;
    private JButtonOperator _btRemove;
    private JLabelOperator _lblOnlineError;
    private JTableOperator _tblSourcePackageFolders;
    
        
    
    /** Tries to find "Specify folders containing source packages." JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblSpecifyFolders() {
        if (_lblSpecifyFolders==null) {
            _lblSpecifyFolders = new JLabelOperator(this, "Specify folders containing source packages.");//TODO I18N
        }
        return _lblSpecifyFolders;
    }

    /** Tries to find "Source Package Folders:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblSourcePackageFolders() {
        if (_lblSourcePackageFolders==null) {
            _lblSourcePackageFolders = new JLabelOperator(this, "Source Package Folders:");//TODO I18N
        }
        return _lblSourcePackageFolders;
    }

    /** Tries to find "Source Level:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblSourceLevel() {
        if (_lblSourceLevel==null) {
            _lblSourceLevel = new JLabelOperator(this, "Source Level:");//TODO I18N
        }
        return _lblSourceLevel;
    }

    /** Tries to find null JComboBox in this dialog.
     * @return JComboBoxOperator
     */
    public JComboBoxOperator cboSourceLevel() {
        if (_cboSourceLevel==null) {
            _cboSourceLevel = new JComboBoxOperator(this);
        }
        return _cboSourceLevel;
    }

    /** Tries to find "Add Folder..." JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btAddFolder() {
        if (_btAddFolder==null) {
            _btAddFolder = new JButtonOperator(this, "Add Folder...");//TODO I18N
        }
        return _btAddFolder;
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

    /** Tries to find "OnlineError string" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblOnlineError() {
        if (_lblOnlineError==null) {
            _lblOnlineError = new JLabelOperator(this, 5);
        }
        return _lblOnlineError;
    }

    
    /** tries to find Source Package Folder table
     * @return JTableOperator
     */
    public JTableOperator tblSourcePackageFolders() {
        if ( _tblSourcePackageFolders==null ) {
            _tblSourcePackageFolders = new JTableOperator(this,0);
        }
        return _tblSourcePackageFolders;
    }

    
    
    /** returns selected item for cboSourceLevel
     * @return String item
     */
    public String getSelectedSourceLevel() {
        return cboSourceLevel().getSelectedItem().toString();
    }

    /** selects item for cboSourceLevel
     * @param item String item
     */
    public void selectSourceLevel(String item) {
        cboSourceLevel().selectItem(item);
    }

    /** clicks on "Add Folder..." JButton
     */
    public void addFolder() {
        btAddFolder().push();
    }

    /** clicks on "Remove" JButton
     */
    public void remove() {
        btRemove().push();
    }


    /** Performs verification of NewJ2SEAntProject by accessing all its components.
     */
    public void verify() {
        lblSpecifyFolders();
        lblSourcePackageFolders();
        lblSourceLevel();
        cboSourceLevel();
        btAddFolder();
        btRemove();
        lblOnlineError();
        tblSourcePackageFolders();
    }
    
}
