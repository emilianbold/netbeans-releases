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

package org.netbeans.modules.visualweb.gravy.dataconnectivity;

import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.NbDialogOperator;

/**
 * This class implements test functionality for "Add Data Source" dialog
 */
public class AddDataSourceOperator extends NbDialogOperator{
    private JButtonOperator _btEdit, _btTestConnection, _btAdd,_btSelectTable;
    private JComboBoxOperator _cboServerType, _cboURL;
    private JTextFieldOperator _txtDSName, _txtDBName, _txtHostName, 
                               _txtUser, _txtPassword, _txtValidationTable;

    /**
     * Creates new instance of this class.
     */
    public AddDataSourceOperator(){
        super(getBundleString("New Database Connection"));
    }

    /**
     * Initializes (if necessary) and returns an object JButtonOperator 
     * for the dialog button "Configure".
     * @return the appropriate object JButtonOperator
     */
    public JButtonOperator btEdit() {
        if (_btEdit==null) {
            _btEdit = new JButtonOperator(this, getBundleString("LBL_CONFIGURE"));
        }
        return _btEdit;
    }

    /**
     * Initializes (if necessary) and returns an object JButtonOperator 
     * for the dialog button "Connection".
     * @return the appropriate object JButtonOperator
     */
    public JButtonOperator btTestConnection() {
        if (_btTestConnection==null) {
            _btTestConnection = new JButtonOperator(this, getBundleString("TEST_CONNECTION"));
        }
        return _btTestConnection;
    }

    /**
     * Initializes (if necessary) and returns an object JButtonOperator 
     * for the dialog button "Add".
     * @return the appropriate object JButtonOperator
     */
    public JButtonOperator btAdd() {
        if (_btAdd==null) {
            _btAdd = new JButtonOperator(this, getBundleString("ADD"));
        }
        return _btAdd;
    }
    
    /**
     * Initializes (if necessary) and returns an object JButtonOperator 
     * for the dialog button "Select".
     * @return the appropriate object JButtonOperator
     */
    public JButtonOperator btSelectTable() {
        if (_btSelectTable==null) {
            _btSelectTable = new JButtonOperator(this, getBundleString("SelectValTable_SelectBtn_label"));
        }
        return _btSelectTable;
    }    

    /**
     * Initializes (if necessary) and returns an object JComboBoxOperator 
     * for the dialog drop-down list "Server Type".
     * @return the appropriate object JComboBoxOperator
     */
    public JComboBoxOperator cboServerType() {
        if (_cboServerType==null) {
            _cboServerType = new JComboBoxOperator(this);
        }
        return _cboServerType;
    }

    /**
     * Initializes (if necessary) and returns an object JComboBoxOperator 
     * for the dialog drop-down list "URL".
     * @return the appropriate object JComboBoxOperator
     */
    public JComboBoxOperator cboURL() {
        if (_cboURL==null) {
            _cboURL = new JComboBoxOperator(this,1);
        }
        return _cboURL;
    }

    /**
     * Initializes (if necessary) and returns an object JTextFieldOperator 
     * for the dialog text field "Datasource Name".
     * @return the appropriate object JTextFieldOperator
     */
    public JTextFieldOperator txtDSName() {
        if (_txtDSName==null) {
            _txtDSName = new JTextFieldOperator(this, 4);
        }
        return _txtDSName;
    }

    /**
     * Initializes (if necessary) and returns an object JTextFieldOperator 
     * for the dialog text field "Database Name".
     * @return the appropriate object JTextFieldOperator
     */
    public JTextFieldOperator txtDBName() {
        if (_txtDBName==null) {
            _txtDBName = new JTextFieldOperator(this, 0);
        }
        return _txtDBName;
    }

    /**
     * Initializes (if necessary) and returns an object JTextFieldOperator 
     * for the dialog text field "Host Name".
     * @return the appropriate object JTextFieldOperator
     */
    public JTextFieldOperator txtHostName() {
        if (_txtHostName==null) {
            _txtHostName = new JTextFieldOperator(this, 1);
        }
        return _txtHostName;
    }

    /**
     * Initializes (if necessary) and returns an object JTextFieldOperator 
     * for the dialog text field "User".
     * @return the appropriate object JTextFieldOperator
     */
    public JTextFieldOperator txtUser() {
        if (_txtUser==null) {
            _txtUser = new JTextFieldOperator(this, 2);
        }
        return _txtUser;
    }

    /**
     * Initializes (if necessary) and returns an object JTextFieldOperator 
     * for the dialog text field "Password".
     * @return the appropriate object JTextFieldOperator
     */
    public JTextFieldOperator txtPassword() {
        if (_txtPassword==null) {
            _txtPassword = new JTextFieldOperator(this, 3);
        }
        return _txtPassword;
    }

    /**
     * Initializes (if necessary) and returns an object JTextFieldOperator 
     * for the dialog text field "Validation Table".
     * @return the appropriate object JTextFieldOperator
     */
    public JTextFieldOperator txtValidationTable() {
        if (_txtValidationTable==null) {
            _txtValidationTable = new JTextFieldOperator(this, 7);
        }
        return _txtValidationTable;
    }
    
    /**
     * Initializes all necessary controls.
     */
    public void verify() {
        btOK();
        btCancel();
//        btHelp();
//        btEdit();
//        btTestConnection();
//        btSelectTable();
//        txtDSName();
//        txtDBName();
//        txtHostName();
        txtPassword();
        cboURL();
        txtUser();
//        txtValidationTable();
        cboServerType();
    }

    /**
     * Finds in a bundle file and returns an actual text of control component.
     * @param p_text string-key corresponding to required control component.
     * @return actual text of control component
     */
    public static String getBundleString(String p_text) {
/*        System.out.println("Getting bundle for " + p_text);
        try {
            return Bundle.getStringTrimmed("org.netbeans.modules.visualweb.dataconnectivity.ui.Bundle", p_text);
        } catch (JemmyException e) {}
        return null;
*/
        //stub
        return p_text;
    }
}
