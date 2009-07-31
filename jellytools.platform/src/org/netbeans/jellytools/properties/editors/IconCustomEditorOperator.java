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

//TODO: Rewrite this class!! It's waaaaaay out of date!

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
            //_rbURL = new JRadioButtonOperator(this, Bundle.getString("org.openide.explorer.propertysheet.editors.Bundle", "CTL_URL"));

        }
        return _rbURL;
    }

    /** Tries to find "File" JRadioButton in this dialog.
     * @return JRadioButtonOperator instance
     */
    public JRadioButtonOperator rbFile() {
        if (_rbFile==null) {
            //_rbFile = new JRadioButtonOperator(this, Bundle.getString("org.openide.explorer.propertysheet.editors.Bundle", "CTL_File"));
        }
        return _rbFile;
    }

    /** Tries to find "Classpath" JRadioButton in this dialog.
     * @return JRadioButtonOperator instance
     */
    public JRadioButtonOperator rbClasspath() {        
        if (_rbClasspath==null) {
           // _rbClasspath = new JRadioButtonOperator(this, Bundle.getString("org.openide.explorer.propertysheet.editors.Bundle", "CTL_Classpath"));
        }
        return _rbClasspath;

    }

    /** Tries to find "No picture" JRadioButton in this dialog.
     * @return JRadioButtonOperator instance
     */
    public JRadioButtonOperator rbNoPicture() {
        if (_rbNoPicture==null) {
            //_rbNoPicture = new JRadioButtonOperator(this, Bundle.getString("org.openide.explorer.propertysheet.editors.Bundle", "CTL_NoPicture"));
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
            //_btSelectFile = new JButtonOperator(this, Bundle.getString("org.openide.explorer.propertysheet.editors.Bundle", "CTL_ButtonSelect"));
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
        btSelectFile().pushNoBlock();
    }

    //*****************************************
    // High-level functionality definition part
    //*****************************************

    /** Performs verification of this operator by accessing all its components.
     */
    public void verify() {

        /*rbURL();
        rbFile();
        rbClasspath();
        rbNoPicture();
        txtName();
        btSelectFile();
        btOK();
        btCancel();*/
    }
}
