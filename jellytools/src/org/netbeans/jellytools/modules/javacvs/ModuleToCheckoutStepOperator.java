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
package org.netbeans.jellytools.modules.javacvs;

import javax.swing.JTextField;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JFileChooserOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;

/**
 * Class implementing all necessary methods for handling "Module to Checkout" panel of
 * Checkout wizard.
 * <br>
 * Usage:<br>
 * <pre>
 *      CheckoutWizardOperator.invoke();
 *      CVSRootStepOperator cvsRootOper = new CVSRootStepOperator();
 *      cvsRootOper.setPassword("password");
 *      cvsRootOper.setCVSRoot(":pserver:user@host:repository");
 *      cvsRootOper.next();
 *      ModuleToCheckoutStepOperator moduleOper = new ModuleToCheckoutStepOperator();
 *      moduleOper.setModule("module");
 *      moduleOper.setBranch("branch");
 *      moduleOper.setLocalFolder("/tmp");
 *      moduleOper.finish();
 * </pre>
 * 
 * @author Jiri.Skrivanek@sun.com
 * @see CheckoutWizardOperator
 * @see BrowseCVSModuleOperator
 * @see BrowseTagsOperator
 */
public class ModuleToCheckoutStepOperator extends CheckoutWizardOperator {

    /** Waits for "Module to Checkout" panel. */
    public ModuleToCheckoutStepOperator() {
        super();
        stepsWaitSelectedValue(Bundle.getString(
                "org.netbeans.modules.versioning.system.cvss.ui.wizards.Bundle", 
                "BK2009"));
    }

    private JLabelOperator _lblModule;
    private JTextFieldOperator _txtModule;
    private JButtonOperator _btBrowse;
    private JLabelOperator _lblBranch;
    private JTextFieldOperator _txtBranch;
    private JButtonOperator _btBrowse2;
    private JLabelOperator _lblLocalFolder;
    private JTextFieldOperator _txtLocalFolder;
    private JButtonOperator _btBrowse3;

    //******************************
    // Subcomponents definition part
    //******************************

    /** Tries to find "Module:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblModule() {
        if (_lblModule==null) {
            _lblModule = new JLabelOperator(this, Bundle.getStringTrimmed(
                    "org.netbeans.modules.versioning.system.cvss.ui.wizards.Bundle", "BK2002"));
        }
        return _lblModule;
    }

    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtModule() {
        if (_txtModule==null) {
            _txtModule = new JTextFieldOperator(
                    (JTextField)lblModule().getLabelFor());
        }
        return _txtModule;
    }

    /** Tries to find "Browse..." JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btBrowseModule() {
        if (_btBrowse==null) {
            _btBrowse = new JButtonOperator(this, Bundle.getStringTrimmed(
                    "org.netbeans.modules.versioning.system.cvss.ui.wizards.Bundle", "BK2003"));
        }
        return _btBrowse;
    }

    /** Tries to find "Branch:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblBranch() {
        if (_lblBranch==null) {
            _lblBranch = new JLabelOperator(this, Bundle.getStringTrimmed(
                    "org.netbeans.modules.versioning.system.cvss.ui.wizards.Bundle", "BK2005"));
        }
        return _lblBranch;
    }

    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtBranch() {
        if (_txtBranch==null) {
            _txtBranch = new JTextFieldOperator(
                    (JTextField)lblBranch().getLabelFor());
        }
        return _txtBranch;
    }

    /** Tries to find "Browse..." JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btBrowseBranch() {
        if (_btBrowse2==null) {
            _btBrowse2 = new JButtonOperator(this, 
                    Bundle.getStringTrimmed("org.netbeans.modules.versioning.system.cvss.ui.wizards.Bundle", "BK2003"), 
                    1);
        }
        return _btBrowse2;
    }

    /** Tries to find "Local Folder:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblLocalFolder() {
        if (_lblLocalFolder==null) {
            _lblLocalFolder = new JLabelOperator(this, Bundle.getStringTrimmed(
                    "org.netbeans.modules.versioning.system.cvss.ui.wizards.Bundle", "BK2007"));
        }
        return _lblLocalFolder;
    }

    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtLocalFolder() {
        if (_txtLocalFolder==null) {
            _txtLocalFolder = new JTextFieldOperator(
                    (JTextField)lblLocalFolder().getLabelFor());
        }
        return _txtLocalFolder;
    }

    /** Tries to find "Browse..." JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btBrowseLocalFolder() {
        if (_btBrowse3==null) {
            _btBrowse3 = new JButtonOperator(this, 
                    Bundle.getStringTrimmed("org.netbeans.modules.versioning.system.cvss.ui.wizards.Bundle", "BK2003"),
                    2);
        }
        return _btBrowse3;
    }


    //****************************************
    // Low-level functionality definition part
    //****************************************

    /** gets text for txtModule
     * @return String text
     */
    public String getModule() {
        return txtModule().getText();
    }

    /** sets text for txtModule
     * @param text String text
     */
    public void setModule(String text) {
        txtModule().clearText();
        txtModule().typeText(text);
    }

    /**
     * clicks on "Browse..." JButton and returns BrowseCVSModuleOperator
     * 
     * @return instance of BrowseCVSModuleOperator
     */
    public BrowseCVSModuleOperator browseModule() {
        btBrowseModule().pushNoBlock();
        return new BrowseCVSModuleOperator();
    }

    /** gets text for txtBranch
     * @return String text
     */
    public String getBranch() {
        return txtBranch().getText();
    }

    /** sets text for txtBranch
     * @param text String text
     */
    public void setBranch(String text) {
        txtBranch().clearText();
        txtBranch().typeText(text);
    }

    /** clicks on "Browse..." button and returns BrowseTagsOperator
     * @return instance of BrowseTagsOperator
     */
    public BrowseTagsOperator browseBranch() {
        btBrowseBranch().pushNoBlock();
        return new BrowseTagsOperator();
    }

    /** gets text for txtLocalFolder
     * @return String text
     */
    public String getLocalFolder() {
        return txtLocalFolder().getText();
    }

    /** sets text for txtLocalFolder
     * @param text String text
     */
    public void setLocalFolder(String text) {
        txtLocalFolder().clearText();
        txtLocalFolder().typeText(text);
    }

    /** clicks on "Browse..." and returns JFileChooserOperator instance.
     * @return instance of JFileChooserOperator
     */
    public JFileChooserOperator browseLocalFolder() {
        btBrowseLocalFolder().pushNoBlock();
        return new JFileChooserOperator();
    }


    //*****************************************
    // High-level functionality definition part
    //*****************************************

    /**
     * Performs verification of ModuleToCheckoutStepOperator by accessing all its components.
     */
    public void verify() {
        lblModule();
        txtModule();
        btBrowseModule();
        lblBranch();
        txtBranch();
        btBrowseBranch();
        lblLocalFolder();
        txtLocalFolder();
        btBrowseLocalFolder();
    }
}

