/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.jellytools.properties.editors;

import javax.swing.JDialog;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jemmy.operators.*;

/** Class implementing all necessary methods for handling Icon Custom Editor.
 * @author <a href="mailto:Marian.Mirilovic@sun.com">Marian Mirilovic</a>
 * @version 1.0 */
public class IconCustomEditorOperator extends NbDialogOperator {

    /** Creates new IconCustomEditorOperator that can handle it.
     * Throws TimeoutExpiredException when NbDialog not found.
     * @param title title of custom editor 
     */
    public IconCustomEditorOperator(String title) {
        super(title);
    }

    /** Creates new IconCustomEditorOperator.
     * @param wrapper JDialogOperator wrapper for custom editor 
     */    
    public IconCustomEditorOperator(JDialogOperator wrapper) {
        super((JDialog)wrapper.getSource());
    }
    
    private JRadioButtonOperator _rbURL;
    private JRadioButtonOperator _rbFile;
    private JRadioButtonOperator _rbClasspath;
    private JRadioButtonOperator _rbNoPicture;
    private JTextFieldOperator _txtName;
    private JButtonOperator _btSelectFile;

    //******************************
    // Subcomponents definition part
    //******************************

    /** Tries to find "URL" JRadioButton in this dialog.
     * @return JRadioButtonOperator instance
     */
    public JRadioButtonOperator rbURL() {
        if (_rbURL==null) {
            _rbURL = new JRadioButtonOperator(this, Bundle.getString("org.openide.explorer.propertysheet.editors.Bundle", "CTL_URL"));
        }
        return _rbURL;
    }

    /** Tries to find "File" JRadioButton in this dialog.
     * @return JRadioButtonOperator instance
     */
    public JRadioButtonOperator rbFile() {
        if (_rbFile==null) {
            _rbFile = new JRadioButtonOperator(this, Bundle.getString("org.openide.explorer.propertysheet.editors.Bundle", "CTL_File"));
        }
        return _rbFile;
    }

    /** Tries to find "Classpath" JRadioButton in this dialog.
     * @return JRadioButtonOperator instance
     */
    public JRadioButtonOperator rbClasspath() {
        if (_rbClasspath==null) {
            _rbClasspath = new JRadioButtonOperator(this, Bundle.getString("org.openide.explorer.propertysheet.editors.Bundle", "CTL_Classpath"));
        }
        return _rbClasspath;
    }

    /** Tries to find "No picture" JRadioButton in this dialog.
     * @return JRadioButtonOperator instance
     */
    public JRadioButtonOperator rbNoPicture() {
        if (_rbNoPicture==null) {
            _rbNoPicture = new JRadioButtonOperator(this, Bundle.getString("org.openide.explorer.propertysheet.editors.Bundle", "CTL_NoPicture"));
        }
        return _rbNoPicture;
    }

    /** Tries to find Name JTextField in this dialog.
     * @return JTextFieldOperator instance
     */
    public JTextFieldOperator txtName() {
        if (_txtName==null) {
            _txtName = new JTextFieldOperator(this);
        }
        return _txtName;
    }

    /** Tries to find "Select File" JButton in this dialog.
     * @return JButtonOperator instance
     */
    public JButtonOperator btSelectFile() {
        if (_btSelectFile==null) {
            _btSelectFile = new JButtonOperator(this, Bundle.getString("org.openide.explorer.propertysheet.editors.Bundle", "CTL_ButtonSelect"));
        }
        return _btSelectFile;
    }

    //****************************************
    // Low-level functionality definition part
    //****************************************

    /** Clicks on "URL" JRadioButton. */
    public void uRL() {
        rbURL().push();
    }

    /** Clicks on "File" JRadioButton.  */
    public void file() {
        rbFile().push();
    }

    /** Clicks on "Classpath" JRadioButton. */
    public void classpath() {
        rbClasspath().push();
    }

    /** Clicks on "No picture" JRadioButton. */
    public void noPicture() {
        rbNoPicture().push();
    }

    /** Gets text from Name text field.
     * @return text from Name text field.
     */
    public String getName() {
        return txtName().getText();
    }

    /** Sets text in Name text field.
     * @param text text to be written to Name text field
     */
    public void setName(String text) {
        txtName().setText(text);
    }

    /** Types text in Name text field.
     * @param text text to be written to Name text field
     */
    public void typeName(String text) {
        txtName().typeText(text);
    }

    /** Clicks on "Select File" JButton. */
    public void selectFile() {
        btSelectFile().push();
    }

    //*****************************************
    // High-level functionality definition part
    //*****************************************

    /** Performs verification of this operator by accessing all its components.
     */
    public void verify() {
        rbURL();
        rbFile();
        rbClasspath();
        rbNoPicture();
        txtName();
        btSelectFile();
        btOK();
        btCancel();
    }
}
