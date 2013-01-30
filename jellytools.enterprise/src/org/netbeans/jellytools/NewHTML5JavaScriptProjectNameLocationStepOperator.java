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
package org.netbeans.jellytools;

import javax.swing.JTextField;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;

/**
 *
 * @author cyhelsky
 */
public class NewHTML5JavaScriptProjectNameLocationStepOperator extends NewProjectWizardOperator {
    
    private JButtonOperator _browseButton;
    private JLabelOperator _createdFolderLabel;
    private JTextFieldOperator _createdFolderTextField;
    private JLabelOperator _projectLocationLabel;
    private JTextFieldOperator _projectLocationTextField;
    private JLabelOperator _projectNameLabel;
    private JTextFieldOperator _projectNameTextField; 
    
    
    
    
    /** Sets given location in text field Project Location.
     * @param location Created folder location
     */
    public void setCreatedFolder(String location) {
        createdFolderTextField().setText(location);
    }
    
    /** Sets given location in text field Location.
     * @param location Project location
     */
    public void setProjectLocation(String location) {
        projectLocationTextField().setText(location);
    }
    
    /** Sets given name in text field Project Name.
     * @param name project name
     */
    public void setProjectName(String name) {
        projectNameTextField().setText(name);
    }
    
    /**
     * 
     * @return JButtonOperator
     */
    public JButtonOperator browseButton() {
        if(_browseButton == null) {
            _browseButton = new JButtonOperator(this,
                    Bundle.getStringTrimmed(
                    "org.netbeans.modules.web.clientproject.ui.wizard",
                    "NewClientSideProject.browseButton.text"));
        }
        return _browseButton;
    }
    
    /**
     * 
     * @return JLabelOperator
     */
    public JLabelOperator createdFolderLabel() {
        if(_createdFolderLabel == null) {
            _createdFolderLabel = new JLabelOperator(this,
                    Bundle.getStringTrimmed(
                    "org.netbeans.modules.web.clientproject.ui.wizard",
                    "NewClientSideProject.projectNameLabel.text"));
        }
        return _createdFolderLabel;
    }
    
    /**
     * 
     * @return JTextFieldOperator
     */
    public JTextFieldOperator createdFolderTextField() {
        if(_createdFolderTextField == null) {
            if (createdFolderLabel().getLabelFor()!=null) {
                _createdFolderTextField = new JTextFieldOperator(
                        (JTextField)createdFolderLabel().getLabelFor());
            }
        }
        return _createdFolderTextField;
    }
    
    /**
     * 
     * @return JLabelOperator
     */
    public JLabelOperator projectLocationLabel() {
        if(_projectLocationLabel == null) {
            _projectLocationLabel = new JLabelOperator(this,
                    Bundle.getStringTrimmed(
                    "org.netbeans.modules.web.clientproject.ui.wizard",
                    "NewClientSideProject.projectLocationLabel.text"));
        }
        return _projectLocationLabel;
    }
    
    /**
     * 
     * @return JTextFieldOperator
     */
    public JTextFieldOperator projectLocationTextField() {
        if(_projectLocationTextField == null) {
            if (projectLocationLabel().getLabelFor()!=null) {
                _projectLocationTextField = new JTextFieldOperator(
                        (JTextField)projectLocationLabel().getLabelFor());
            }
        }
        return _projectLocationTextField;
    }
    
    /**
     * 
     * @return JLabelOperator
     */
    public JLabelOperator projectNameLabel() {
        if(_projectNameLabel == null) {
            _projectNameLabel = new JLabelOperator(this,
                    Bundle.getStringTrimmed(
                    "org.netbeans.modules.web.clientproject.ui.wizard",
                    "NewClientSideProject.projectNameLabel.text"));
        }
        return _projectNameLabel;
    }
    
    /**
     * 
     * @return JTextFieldOperator
     */
    public JTextFieldOperator projectNameTextField() {
        if(_projectNameTextField == null) {
            if (projectNameLabel().getLabelFor()!=null) {
                _projectNameTextField = new JTextFieldOperator(
                        (JTextField)projectNameLabel().getLabelFor());
            }
        }
        return _projectNameTextField;
    }
}
