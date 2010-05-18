/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.test.web;

import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jemmy.operators.*;

/** Class implementing all necessary methods for handling "New Web Application" NbDialog.
 *
 * @author dkolar
 * @version 1.0
 */
public class NewWebProjectVWJSFFrameworkStepOperator extends NewProjectWizardOperator {

    /** Creates new NewWebProjectVWJSFFrameworkStepOperator that can handle it.
     */
    public NewWebProjectVWJSFFrameworkStepOperator() {
        super("New Web Application");
    }

    private JCheckBoxOperator _cbValidateXML;
    private JCheckBoxOperator _cbVerifyObjects;
    private JTextFieldOperator _txtJSFServletName;
    private JTextFieldOperator _txtServletURLMapping;
    private JTextFieldOperator _txtDefaultJavaPackage;
    private JLabelOperator _lblDefaultJavaPackageNameIsInvalid;
    private JLabelOperator _lblTheURLPatternHasToBeEntered;    
    private JLabelOperator _lblTheURLPatternIsNotValid;//Struts as well    
    private JTableOperator _tabSelectTheFrameworksYouWantToUseInYourWebApplication;

    //******************************
    // Subcomponents definition part
    //******************************
     /*
     * Selects a Visual Web Framework to be added
     */

    public boolean setVWJSFFrameworkCheckbox()
    {
        Integer vwjsfRow = tabSelectTheFrameworksYouWantToUseInYourWebApplication().findCellRow("org.netbeans.modules.visualweb.project.jsf");
        if(vwjsfRow != -1)
        {
            tabSelectTheFrameworksYouWantToUseInYourWebApplication().clickOnCell(vwjsfRow, 0);
            return true;
        }
        else
        {
            System.err.println("No Visual Web framework found!");
            return false;
        }

    }

    /** Tries to find null JTable in this dialog.
     * @return JTableOperator
     */
    public JTableOperator tabSelectTheFrameworksYouWantToUseInYourWebApplication() {
        if (_tabSelectTheFrameworksYouWantToUseInYourWebApplication==null) {
            _tabSelectTheFrameworksYouWantToUseInYourWebApplication = new JTableOperator(this);
        }
        return _tabSelectTheFrameworksYouWantToUseInYourWebApplication;
    }
    
    /** Tries to find "Validate XML" JCheckBox in this dialog.
     * @return JCheckBoxOperator
     */
    public JCheckBoxOperator cbValidateXML() {
        if (_cbValidateXML==null) {
            _cbValidateXML = new JCheckBoxOperator(this,org.netbeans.jellytools.Bundle.getString("org.netbeans.modules.xml.tools.actions.Bundle", "NAME_Validate_XML"));
        }
        return _cbValidateXML;
    }

    /** Tries to find "Verify Objects" JCheckBox in this dialog.
     * @return JCheckBoxOperator
     */
    public JCheckBoxOperator cbVerifyObjects() {
        if (_cbVerifyObjects==null) {
            _cbVerifyObjects = new JCheckBoxOperator(this, "Verify Objects");
        }
        return _cbVerifyObjects;
    }

    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtJSFServletName() {
        if (_txtJSFServletName==null) {
            _txtJSFServletName = new JTextFieldOperator(this);
        }
        return _txtJSFServletName;
    }

    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtServletURLMapping() {
        if (_txtServletURLMapping==null) {
            _txtServletURLMapping = new JTextFieldOperator(this, 1);
        }
        return _txtServletURLMapping;
    }

    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtDefaultJavaPackage() {
        if (_txtDefaultJavaPackage==null) {
            _txtDefaultJavaPackage = new JTextFieldOperator(this, 2);
        }
        return _txtDefaultJavaPackage;
    }

    /** Tries to find "Default java package name is invalid" WizardDescriptor$FixedHeightLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblDefaultJavaPackageNameIsInvalid() {
        if (_lblDefaultJavaPackageNameIsInvalid==null) {
            _lblDefaultJavaPackageNameIsInvalid = new JLabelOperator(this, "Default java package name is invalid");
        }
        return _lblDefaultJavaPackageNameIsInvalid;
    }

    /** Tries to find "The URL Pattern has to be entered." WizardDescriptor$FixedHeightLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblTheURLPatternHasToBeEntered() {
        if (_lblTheURLPatternHasToBeEntered==null) {
            _lblTheURLPatternHasToBeEntered = new JLabelOperator(this, "The URL Pattern has to be entered.");
        }
        return _lblTheURLPatternHasToBeEntered;
    }

            /** Tries to find "The URL Pattern is not valid." WizardDescriptor$FixedHeightLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblTheURLPatternIsNotValid() {
        if (_lblTheURLPatternIsNotValid==null) {
            _lblTheURLPatternIsNotValid = new JLabelOperator(this, "The URL Pattern is not valid.");
        }
        return _lblTheURLPatternIsNotValid;
    }

    //****************************************
    // Low-level functionality definition part
    //****************************************

    /** checks or unchecks given JCheckBox
     * @param state boolean requested state
     */
    public void checkValidateXML(boolean state) {
        if (cbValidateXML().isSelected()!=state) {
            cbValidateXML().push();
        }
    }

    /** checks or unchecks given JCheckBox
     * @param state boolean requested state
     */
    public void checkVerifyObjects(boolean state) {
        if (cbVerifyObjects().isSelected()!=state) {
            cbVerifyObjects().push();
        }
    }

    /** gets text for txtJSFServletName
     * @return String text
     */
    public String getJSFServletName() {
        return txtJSFServletName().getText();
    }

    /** sets text for txtJSFServletName
     * @param text String text
     */
    public void setJSFServletName(String text) {
        txtJSFServletName().setText(text);
    }

    /** types text for txtJSFServletName
     * @param text String text
     */
    public void typeJSFServletName(String text) {
        txtJSFServletName().typeText(text);
    }

    /** gets text for txtServletURLMapping
     * @return String text
     */
    public String getServletURLMapping() {
        return txtServletURLMapping().getText();
    }

    /** sets text for txtServletURLMapping
     * @param text String text
     */
    public void setServletURLMapping(String text) {
        txtServletURLMapping().setText(text);
    }

    /** types text for txtServletURLMapping
     * @param text String text
     */
    public void typeServletURLMapping(String text) {
        txtServletURLMapping().typeText(text);
    }

    /** gets text for txtDefaultJavaPackage
     * @return String text
     */
    public String getDefaultJavaPackage() {
        return txtDefaultJavaPackage().getText();
    }

    /** sets text for txtDefaultJavaPackage
     * @param text String text
     */
    public void setDefaultJavaPackage(String text) {
        txtDefaultJavaPackage().setText(text);
    }

    /** types text for txtDefaultJavaPackage
     * @param text String text
     */
    public void typeDefaultJavaPackage(String text) {
        txtDefaultJavaPackage().typeText(text);
    }


    //*****************************************
    // High-level functionality definition part
    //*****************************************

    /** Performs verification of NewWebProjectVWJSFFrameworkStepOperator by accessing all its components.
     */
    public void verify() {
        cbValidateXML();
        cbVerifyObjects();
        txtJSFServletName();
        txtServletURLMapping();
        txtDefaultJavaPackage();
        lblDefaultJavaPackageNameIsInvalid();
    }

    /** Performs simple test of NewWebProjectVWJSFFrameworkStepOperator
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        new NewWebProjectVWJSFFrameworkStepOperator().verify();
        System.out.println("NewWebProjectVWJSFFrameworkStepOperator verification finished.");
    }
}

