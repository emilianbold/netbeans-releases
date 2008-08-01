/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.jellytools.properties.editors;

import javax.swing.JDialog;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jemmy.operators.*;
import org.netbeans.jemmy.operators.JDialogOperator;

/** Class implementing all necessary methods for handling FileSystem Custom Editor.
 * It is editor for bean org.openide.filesystems.FileSystem.
 * @author <a href="mailto:Marian.Mirilovic@sun.com">Marian Mirilovic</a>
 * @version 1.0
 */
public class FilesystemCustomEditorOperator extends NbDialogOperator {

    /** Creates new FilesystemCustomEditorOperator that can handle it.
     * Throws TimeoutExpiredException when NbDialog not found
     * @param title title of custom editor */
    public FilesystemCustomEditorOperator(String title) {
        super(title);
    }

    /** Creates new FilesytemCustomEditorOperator
     * @param wrapper JDialogOperator wrapper for custom editor */    
    public FilesystemCustomEditorOperator(JDialogOperator wrapper) {
        super((JDialog)wrapper.getSource());
    }
    
    private JRadioButtonOperator _rbAddLocalDirectory;
    private JTextFieldOperator _txtDirectory;
    private JButtonOperator _btBrowse;
    private JRadioButtonOperator _rbAddJARFile;
    private JTextFieldOperator _txtJARFile;
    private JButtonOperator _btBrowse2;
    private JRadioButtonOperator _rbAddOtherFileSystemType;
    private JComboBoxOperator _cboType;


    //******************************
    // Subcomponents definition part
    //******************************

    /** Tries to find "Add Local Directory" JRadioButton in this dialog.
     * @return JRadioButtonOperator instance
     */
    public JRadioButtonOperator rbAddLocalDirectory() {
        if (_rbAddLocalDirectory==null) {
            _rbAddLocalDirectory = new JRadioButtonOperator(this, 
                        Bundle.getString("org.netbeans.beaninfo.editors.Bundle",
                                         "CTL_FileSystemPanel.dirRadioButton.text"));
        }
        return _rbAddLocalDirectory;
    }

    /** Tries to find JTextField for Directory in this dialog.
     * @return JTextFieldOperator instance
     */
    public JTextFieldOperator txtDirectory() {
        if (_txtDirectory==null) {
            _txtDirectory = new JTextFieldOperator(this);
        }
        return _txtDirectory;
    }

    /** Tries to find "Browse" JButton to select Directory in this dialog.
     * @return JButtonOperator instance
     */
    public JButtonOperator btBrowse() {
        if (_btBrowse==null) {
            _btBrowse = new JButtonOperator(this, 
                        Bundle.getString("org.netbeans.beaninfo.editors.Bundle",
                                         "CTL_FileSystemPanel.browseDirButton.text"));
        }
        return _btBrowse;
    }

    /** Tries to find "Add JAR File" JRadioButton in this dialog.
     * @return JRadioButtonOperator instance
     */
    public JRadioButtonOperator rbAddJARFile() {
        if (_rbAddJARFile==null) {
            _rbAddJARFile = new JRadioButtonOperator(this, 
                        Bundle.getString("org.netbeans.beaninfo.editors.Bundle", 
                                         "CTL_FileSystemPanel.jarRadioButton.text"));
        }
        return _rbAddJARFile;
    }

    /** Tries to find JTextField for JAR File in this dialog.
     * @return JTextFieldOperator instance
     */
    public JTextFieldOperator txtJARFile() {
        if (_txtJARFile==null) {
            _txtJARFile = new JTextFieldOperator(this, 1);
        }
        return _txtJARFile;
    }

    /** Tries to find "Browse" JButton to select JAR File in this dialog.
     * @return JButtonOperator instance
     */
    public JButtonOperator btBrowse2() {
        if (_btBrowse2==null) {
            _btBrowse2 = new JButtonOperator(this, 
                         Bundle.getString("org.netbeans.beaninfo.editors.Bundle", 
                                          "CTL_FileSystemPanel.browseJarButton.text"),
                                          1);
        }
        return _btBrowse2;
    }

    /** Tries to find "Add (other file system type)" JRadioButton in this dialog.
     * @return JRadioButtonOperator instance
     */
    public JRadioButtonOperator rbAddOtherFileSystemType() {
        if (_rbAddOtherFileSystemType==null) {
            _rbAddOtherFileSystemType = new JRadioButtonOperator(this, 
                        Bundle.getString("org.netbeans.beaninfo.editors.Bundle",
                                         "CTL_FileSystemPanel.otherRadioButton.text"));
        }
        return _rbAddOtherFileSystemType;
    }

    /** Tries to find JComboBox for Type of filesystem in this dialog.
     * @return JComboBoxOperator instance
     */
    public JComboBoxOperator cboType() {
        if (_cboType==null) {
            _cboType = new JComboBoxOperator(this);
        }
        return _cboType;
    }


    //****************************************
    // Low-level functionality definition part
    //****************************************

    /** clicks on "Add Local Directory" JRadioButton.
     */
    public void addLocalDirectory() {
        rbAddLocalDirectory().push();
    }

    /** Gets text from Directory text field.
     * @return text from Directory text field.
     */
    public String getDirectory() {
        return txtDirectory().getText();
    }

    /** Sets text in Directory text field.
     * @param directory file path to directory
     */
    public void setDirectory(String directory) {
        txtDirectory().setText(directory);
    }

    /** Types text in Directory text field.
     * @param directory file path to directory
     */
    public void typeDirectory(String directory) {
        txtDirectory().typeText(directory);
    }

    /** Clicks on "Browse" JButton to set Directory. */
    public void browse() {
        btBrowse().pushNoBlock();
    }

    /** Clicks on "Add JAR File" JRadioButton. */
    public void addJARFile() {
        rbAddJARFile().push();
    }

    /** Gets text from JAR File text field.
     * @return text from JAR File text field
     */
    public String getJARFile() {
        return txtJARFile().getText();
    }

    /** Sets text in JAR File text field.
     * @param jarFile file path to JAR file
     */
    public void setJARFile(String jarFile) {
        txtJARFile().setText(jarFile);
    }

    /** Types text in JAR File text field.
     * @param jarFile file path to JAR file
     */
    public void typeJARFile(String jarFile) {
        txtJARFile().typeText(jarFile);
    }

    /** Clicks on "Browse" JButton to select JAR File */
    public void browse2() {
        btBrowse2().pushNoBlock();
    }

    /** Clicks on "Add (other file system type)" JRadioButton. */
    public void addOtherFileSystemType() {
        rbAddOtherFileSystemType().push();
    }

    /** Returns selected item from combo box of filesystem type.
     * @return selected item from combo box of filesystem type
     */
    public String getSelectedType() {
        return cboType().getSelectedItem().toString();
    }

    /** Selects item in combo box of filesystem type.
     * @param item item to be selected
     */
    public void selectType(String item) {
        cboType().selectItem(item);
    }

    /** Types text in combo box of filesystem type.
     * @param filesystemType type of filesystem
     */
    public void typeType(String filesystemType) {
        cboType().typeText(filesystemType);
    }


    //*****************************************
    // High-level functionality definition part
    //*****************************************

    /** Performs verification of this operator by accessing all its components.
     */
    public void verify() {
        rbAddLocalDirectory();
        txtDirectory();
        btBrowse();
        rbAddJARFile();
        txtJARFile();
        btBrowse2();
        rbAddOtherFileSystemType();
        cboType();
    }

}

