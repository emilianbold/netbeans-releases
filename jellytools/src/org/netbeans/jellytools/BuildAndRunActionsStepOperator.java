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
import javax.swing.JDialog;

/**
 * Handles Build and Run Actions panel of New Project wizard
 * for J2SE Ant Project.<br>
 *
 * Usage:
 * <pre>
 * BuildAndRunActionsStepOperator brop = new BuildAndRunActionsStepOperator();
 * brop.selectBuild("clean");
 * brop.selectClean("clean");
 * brop.selectRun("clean");
 * brop.selectGenerateJavadoc("clean");
 * brop.selectTest("clean");
 * </pre>
 *
 * @author tb115823
 */
public class BuildAndRunActionsStepOperator extends NewProjectWizardOperator {
    
    private JLabelOperator _lblBuild;
    private JLabelOperator _lblClean;
    private JLabelOperator _lblRun;
    private JLabelOperator _lblGenerateJavadoc;
    private JLabelOperator _lblTest;
    private JLabelOperator _lblOnlineError;
    private JComboBoxOperator _cboBuild;
    private JComboBoxOperator _cboClean;
    private JComboBoxOperator _cboRun;
    private JComboBoxOperator _cboGenerateJavadoc;
    private JComboBoxOperator _cboTest;
    
    
    
    /** Tries to find "Build:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblBuild() {
        if (_lblBuild==null) {
            _lblBuild = new JLabelOperator(this, "Build:");//NOI18N
        }
        return _lblBuild;
    }

    /** Tries to find "Clean:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblClean() {
        if (_lblClean==null) {
            _lblClean = new JLabelOperator(this, "Clean:");//NOI18N
        }
        return _lblClean;
    }

    /** Tries to find "Run:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblRun() {
        if (_lblRun==null) {
            _lblRun = new JLabelOperator(this, "Run:");//NOI18N
        }
        return _lblRun;
    }

    /** Tries to find "Generate Javadoc:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblGenerateJavadoc() {
        if (_lblGenerateJavadoc==null) {
            _lblGenerateJavadoc = new JLabelOperator(this, "Generate Javadoc:");//NOI18N
        }
        return _lblGenerateJavadoc;
    }

    /** Tries to find "Test:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblTest() {
        if (_lblTest==null) {
            _lblTest = new JLabelOperator(this, "Test:");//NOI18N
        }
        return _lblTest;
    }

    /** Tries to find null JComboBox in this dialog.
     * @return JComboBoxOperator
     */
    public JComboBoxOperator cboBuild() {
        if (_cboBuild==null) {
            _cboBuild = new JComboBoxOperator(this);
        }
        return _cboBuild;
    }

    /** Tries to find null JComboBox in this dialog.
     * @return JComboBoxOperator
     */
    public JComboBoxOperator cboClean() {
        if (_cboClean==null) {
            _cboClean = new JComboBoxOperator(this, 1);
        }
        return _cboClean;
    }

    /** Tries to find null JComboBox in this dialog.
     * @return JComboBoxOperator
     */
    public JComboBoxOperator cboRun() {
        if (_cboRun==null) {
            _cboRun = new JComboBoxOperator(this, 2);
        }
        return _cboRun;
    }

    /** Tries to find null JComboBox in this dialog.
     * @return JComboBoxOperator
     */
    public JComboBoxOperator cboGenerateJavadoc() {
        if (_cboGenerateJavadoc==null) {
            _cboGenerateJavadoc = new JComboBoxOperator(this, 3);
        }
        return _cboGenerateJavadoc;
    }

    /** Tries to find null JComboBox in this dialog.
     * @return JComboBoxOperator
     */
    public JComboBoxOperator cboTest() {
        if (_cboTest==null) {
            _cboTest = new JComboBoxOperator(this, 4);
        }
        return _cboTest;
    }

    /** Tries to find " " JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblOnlineError() {
        if (_lblOnlineError==null) {
            _lblOnlineError = new JLabelOperator(this, " ", 3);
        }
        return _lblOnlineError;
    }


    
    /** returns selected item for cboBuild
     * @return String item
     */
    public String getSelectedBuild() {
        return cboBuild().getSelectedItem().toString();
    }

    /** selects item for cboBuild
     * @param item String item
     */
    public void selectBuild(String item) {
        cboBuild().selectItem(item);
    }

    /** returns selected item for cboClean
     * @return String item
     */
    public String getSelectedClean() {
        return cboClean().getSelectedItem().toString();
    }

    /** selects item for cboClean
     * @param item String item
     */
    public void selectClean(String item) {
        cboClean().selectItem(item);
    }

    /** returns selected item for cboRun
     * @return String item
     */
    public String getSelectedRun() {
        return cboRun().getSelectedItem().toString();
    }

    /** selects item for cboRun
     * @param item String item
     */
    public void selectRun(String item) {
        cboRun().selectItem(item);
    }

    /** returns selected item for cboGenerateJavadoc
     * @return String item
     */
    public String getSelectedGenerateJavadoc() {
        return cboGenerateJavadoc().getSelectedItem().toString();
    }

    /** selects item for cboGenerateJavadoc
     * @param item String item
     */
    public void selectGenerateJavadoc(String item) {
        cboGenerateJavadoc().selectItem(item);
    }

    /** returns selected item for cboTest
     * @return String item
     */
    public String getSelectedTest() {
        return cboTest().getSelectedItem().toString();
    }

    /** selects item for cboTest
     * @param item String item
     */
    public void selectTest(String item) {
        cboTest().selectItem(item);
    }


    /** Performs verification of NewJ2SEAntProject by accessing all its components.
     */
    public void verify() {
        lblBuild();
        lblClean();
        lblRun();
        lblGenerateJavadoc();
        lblTest();
        cboBuild();
        cboClean();
        cboRun();
        cboGenerateJavadoc();
        cboTest();
        lblOnlineError();
    }

}
