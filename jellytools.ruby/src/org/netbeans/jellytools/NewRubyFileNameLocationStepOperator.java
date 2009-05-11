/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.jellytools;

import java.awt.Component;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JLabelOperator;

//TODO: write a test for this class

/**
 *
 * @author Ivan Sidorkin <ivansidorkin@netbeans.org>
 */
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
public class NewRubyFileNameLocationStepOperator extends NewFileWizardOperator {

    /** Components operators. */
    private JLabelOperator      _lblObjectName;
    private JTextFieldOperator  _txtObjectName;
    private JLabelOperator      _lblProject;
    private JTextFieldOperator  _txtProject;    
    private JLabelOperator      _lblLocation;
    private JComboBoxOperator   _cboLocation;
    private JLabelOperator      _lblFolder;
    private JTextFieldOperator  _txtFolder;
    private JButtonOperator     _btnBrowse;
    private JLabelOperator      _lblCreatedFile;
    private JTextFieldOperator  _txtCreatedFile;    


     /** Waits for wizard with New title.  */
    public NewRubyFileNameLocationStepOperator() {
        super();
    }

    /** Waits for wizard with given title.
     * @param title title of wizard
     */
    public NewRubyFileNameLocationStepOperator(String title) {
        super(title);
    }

    /** Returns operator for first label with "Name"
     * @return JLabelOperator
     */    
    public JLabelOperator lblObjectName() {
        if (_lblObjectName == null) {
            final String nameLabel = Bundle.getString("org.netbeans.modules.properties.Bundle", "PROP_name");
           // final String nameAndLocationLabel = Bundle.getStringTrimmed("org.netbeans.modules.java.project.Bundle", "LBL_JavaTargetChooserPanelGUI_Name");
            final String nameAndLocationLabel = Bundle.getString("org.netbeans.modules.ruby.rubyproject.templates.Bundle", "LBL_RubyTargetChooserPanelGUI_Name");
            _lblObjectName = new JLabelOperator(this, new JLabelOperator.JLabelFinder(new ComponentChooser() {

                public boolean checkComponent(Component comp) {
                    JLabel jLabel = (JLabel) comp;
                    String text = jLabel.getText();
                    if (text == null || nameAndLocationLabel.equals(text)) {
                        return false;
                    } else if (text.indexOf(nameLabel) > -1 && (jLabel.getLabelFor() == null || jLabel.getLabelFor() instanceof JTextField)) {
                        return true;
                    }
                    return false;
                }

                public String getDescription() {
                    return "JLabel containing Name and associated with text field";
                }
            }));
        }
        return _lblObjectName;
    }

    /** Returns operator of text field bind to lblObjectName
     * @return JTextOperator
     */
    public JTextFieldOperator txtObjectName() {
        if( _txtObjectName==null ) {
            if ( lblObjectName().getLabelFor()!=null ) {
                _txtObjectName = new JTextFieldOperator((JTextField)lblObjectName().getLabelFor());
            } else {
                _txtObjectName = new JTextFieldOperator(this,0);
            }
        }
        return _txtObjectName;
    }

    /** Returns operator for first label with "Project"
     * @return JLabelOperator
     */
    @Override
    public JLabelOperator lblProject() {
        if(_lblProject == null) {
            _lblProject = new JLabelOperator(this, Bundle.getStringTrimmed("org.netbeans.modules.ruby.rubyproject.templates.Bundle",
                    "LBL_RubyTargetChooserPanelGUI_jLabel5"));
        }
        return _lblProject;
    }


    /** Returns operator of text field bind to lblProject
     * @return JTextOperator
     */
    public JTextFieldOperator txtProject() {
        if( _txtProject==null ) {
            if ( lblProject().getLabelFor()!=null ) {
                _txtProject = new JTextFieldOperator((JTextField)lblProject().getLabelFor());
            } else {
                _txtProject = new JTextFieldOperator(this,1);
            }
        }
        return _txtProject;
    }

    /** Returns operator for label with "Created File:"
     * @return JLabelOperator
     */
    public JLabelOperator lblCreatedFile() {
        if(_lblCreatedFile == null) {
            _lblCreatedFile = new JLabelOperator(this, Bundle.getStringTrimmed("org.netbeans.modules.ruby.rubyproject.templates.Bundle",
                    "LBL_RubyTargetChooserPanelGUI_CreatedFile_Label"));
        }
        return _lblCreatedFile;
    }

    /** Returns operator of text field bind to lblCreatedFile
     * @return JTextOperator
     */
    public JTextFieldOperator txtCreatedFile() {
        if( _txtCreatedFile==null ) {
            if ( lblCreatedFile().getLabelFor()!=null ) {
                _txtCreatedFile = new JTextFieldOperator((JTextField)lblCreatedFile().getLabelFor());
            } else {
                _txtCreatedFile = new JTextFieldOperator(this,3);
            }
        }
        return _txtCreatedFile;
    }

    /** Returns operator of label "Location:"
     * @return JLabelOperator
     */
    public JLabelOperator lblLocation() {
        if(_lblLocation == null) {
            _lblLocation = new JLabelOperator(this, Bundle.getStringTrimmed("org.netbeans.modules.ruby.rubyproject.templates.Bundle",
                    "LBL_RubyTargetChooserPanelGUI_jLabel1"));
        }
        return _lblLocation;
    }

    /** Returns operator for combo box Location:
     * @return JComboBoxOperator
     */
    public JComboBoxOperator cboLocation() {
        if ( _cboLocation==null ) {
            _cboLocation = new JComboBoxOperator((JComboBox)lblLocation().getLabelFor());
        }
        return _cboLocation;
    }

     /** Returns operator for the Browse... button
     * @return JComboBoxOperator
     */
    public JButtonOperator btnBrowse()
    {
        if(_btnBrowse == null) {
            _btnBrowse = new JButtonOperator(this, Bundle.getStringTrimmed("org.netbeans.modules.ruby.rubyproject.templates.Bundle",
                    "LBL_RubyTargetChooserPanelGUI_Browse"));
        }
        return _btnBrowse;
    }

    /** Returns operator of label "Folder:"
     * @return JLabelOperator
     */
    public JLabelOperator lblFolder() {
        if(_lblFolder == null) {
            _lblFolder = new JLabelOperator(this, Bundle.getStringTrimmed("org.netbeans.modules.ruby.rubyproject.templates.Bundle",
                    "LBL_RubyTargetChooserPanelGUI_Folder"));
        }
        return _lblFolder;
    }

    /** returns operator for combo box Folder:
     * @return JComboBoxOperator
     */
    public JTextFieldOperator txtFolder() {
        if ( _txtFolder==null ) {
            _txtFolder = new JTextFieldOperator((JTextField)lblFolder().getLabelFor());
        }
        return _txtFolder;
    }

    /** Pushes the Browse... button
     */
    public void browseFolder() {
        btnBrowse().press();
    }

    /** Type given package in combo box Package.
     * @param packageName name of package
     */
    public void setFolder(String packageName) {
        new EventTool().waitNoEvent(500);
        txtFolder().clearText();
        txtFolder().typeText(packageName);
    }

    /** Sets given object name in the text field.
     * @param objectName name of object
     */
    public void setObjectName(String objectName) {
        txtObjectName().setText(objectName);
    }

    /** Selects Source Files in combo box Location:.
     * Cannot set location directly by string because combo box has a model
     * with objects and not visible strings.
     */
    public void selectSourceFilesLocation() {
        cboLocation().selectItem(0);
    }

    /** Selects Test Files in combo box Location:
     * Cannot set location directly by string because combo box has a model
     * with objects and not visible strings.
     */
    public void selectTestFilesLocation() {
        cboLocation().selectItem(1);
    }

    /** Performs verification by accessing all sub-components */
    public void verify() {
        lblObjectName();
        txtObjectName();
        lblCreatedFile();
        txtCreatedFile();
        cboLocation();
        lblFolder();
        txtFolder();
    }
}
