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

import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.JListOperator;

/**
 * This class is used as abstract ancestor of classes ExportDataSourcesOperator 
 * and ImportDataSourcesOperator.
 */
public abstract class ExportImportDataSourcesOperator extends NbDialogOperator{

    /**
     * This constructor is used by inherited classes for 
     * creation of their new instances.
     */
    public ExportImportDataSourcesOperator(String p_title){
        super(p_title);
    }

    private JButtonOperator _btSelectAll;
    private JButtonOperator _btClear;
    private JButtonOperator _btBrowse;
    private JTextFieldOperator _txtDSName;
    private JTextFieldOperator _txtValidationQuery;
    private JTextFieldOperator _txtDriverClass;
    private JTextFieldOperator _txtUser;
    private JTextFieldOperator _txtPassword;
    private JTextFieldOperator _txtURL;
    private JTextFieldOperator _txtFileName;
    private JListOperator _lstDataSources;

    /**
     * Initializes (if necessary) and returns an object JButtonOperator 
     * for the dialog button "Select All".
     * @return the appropriate object JButtonOperator
     */
    public JButtonOperator btSelectAll() {
        if (_btSelectAll==null) {
            _btSelectAll = new JButtonOperator(this, getBundleString("SCHEMA_SELECT_ALL_BTN_LABEL"));
        }
        return _btSelectAll;
    }

    /**
     * Initializes (if necessary) and returns an object JButtonOperator 
     * for the dialog button "Clear".
     * @return the appropriate object JButtonOperator
     */
    public JButtonOperator btClear() {
        if (_btClear==null) {
            _btClear = new JButtonOperator(this, getBundleString("SCHEMA_CLEAR_BTN_LABEL"));
        }
        return _btClear;
    }

    /**
     * Initializes (if necessary) and returns an object JButtonOperator 
     * for the dialog button "Browse".
     * @return the appropriate object JButtonOperator
     */
    public JButtonOperator btBrowse() {
        if (_btBrowse==null) {
            _btBrowse = new JButtonOperator(this, getBundleString("BROWSE_BUTTON_LABEL"));
        }
        return _btBrowse;
    }

    /**
     * Initializes (if necessary) and returns an object JTextFieldOperator 
     * for the dialog text field "Datasource Name".
     * @return the appropriate object JTextFieldOperator
     */
    public JTextFieldOperator txtDSName() {
        if (_txtDSName==null) {
            _txtDSName = new JTextFieldOperator(this, 2);
        }
        return _txtDSName;
    }

    /**
     * Initializes (if necessary) and returns an object JTextFieldOperator 
     * for the dialog text field "Validation Query".
     * @return the appropriate object JTextFieldOperator
     */
    public JTextFieldOperator txtValidationQuery() {
        if (_txtValidationQuery==null) {
            _txtValidationQuery = new JTextFieldOperator(this, 1);
        }
        return _txtValidationQuery;
    }

    /**
     * Initializes (if necessary) and returns an object JTextFieldOperator 
     * for the dialog text field "Driver Class".
     * @return the appropriate object JTextFieldOperator
     */
    public JTextFieldOperator txtDriverClass() {
        if (_txtDriverClass==null) {
            _txtDriverClass = new JTextFieldOperator(this, 0);
        }
        return _txtDriverClass;
    }

    /**
     * Initializes (if necessary) and returns an object JTextFieldOperator 
     * for the dialog text field "User".
     * @return the appropriate object JTextFieldOperator
     */
    public JTextFieldOperator txtUser() {
        if (_txtUser==null) {
            _txtUser = new JTextFieldOperator(this, 3);
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
            _txtPassword = new JTextFieldOperator(this, 4);
        }
        return _txtPassword;
    }

    /**
     * Initializes (if necessary) and returns an object JTextFieldOperator 
     * for the dialog text field "Database URL".
     * @return the appropriate object JTextFieldOperator
     */
    public JTextFieldOperator txtURL() {
        if (_txtURL==null) {
            _txtURL = new JTextFieldOperator(this, 5);
        }
        return _txtURL;
    }

    /**
     * Initializes (if necessary) and returns an object JTextFieldOperator 
     * for the dialog text field "File Name".
     * @return the appropriate object JTextFieldOperator
     */
    public JTextFieldOperator txtFileName() {
        if (_txtFileName==null) {
            _txtFileName = new JTextFieldOperator(this, 2);
        }
        return _txtFileName;
    }

    /**
     * Initializes (if necessary) and returns an object JListOperator 
     * for the dialog list "Data Sources".
     * @return the appropriate object JListOperator
     */
    public JListOperator lstDataSources() {
        if (_lstDataSources==null) {
            _lstDataSources = new JListOperator(this);
        }
        return _lstDataSources;
    }

    /**
     * Initializes all necessary controls.
     */
    public void verify(){
        btOK();
        btCancel();
        btHelp();
        btSelectAll();
        btClear();
        btBrowse();
        txtDSName();
        txtDriverClass();
        txtUser();
        txtPassword();
        txtURL();
        txtValidationQuery();
        txtFileName();
        lstDataSources();
    }

    /**
     * Finds in a bundle file and returns an actual text of control component.
     * @param p_text string-key corresponding to required control component.
     * @return actual text of control component
     */
    public static String getBundleString(String p_text) {
        System.out.println("Getting bundle for " + p_text);
        try {
            return Bundle.getStringTrimmed("org.netbeans.modules.visualweb.dataconnectivity.ui.Bundle", p_text);
        } catch (JemmyException e) {}
        return null;
    }

}
