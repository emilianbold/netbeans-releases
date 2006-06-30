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

