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
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;

/**
 * This class implements test functionality for "Edit Server Types" dialog
 */
public class EditServerTypesOperator extends NbDialogOperator{
    /**
     * Creates new instance of this class.
     */
    public EditServerTypesOperator(){
        super(getBundleString("New JDBC Driver"));
    }

    private JButtonOperator _btAdd;
    private JButtonOperator _btRemove;
    private JButtonOperator _btNew;
    private JButtonOperator _btDelete;
    private JButtonOperator _btFind;
    private JListOperator _lstServerTypes;
    private JListOperator _lstJars;
    private JTextFieldOperator _txtName;
    private JTextFieldOperator _txtTemplate;
    private JComboBoxOperator _cboDriver;

    /**
     * Initializes (if necessary) and returns an object JButtonOperator 
     * for the dialog button "Add".
     * @return the appropriate object JButtonOperator
     */
    public JButtonOperator btAdd() {
        if (_btAdd==null) {
            _btAdd = new JButtonOperator(this, getBundleString("Add..."));
        }
        return _btAdd;
    }

    /**
     * Initializes (if necessary) and returns an object JButtonOperator 
     * for the dialog button "Remove".
     * @return the appropriate object JButtonOperator
     */
    public JButtonOperator btRemove() {
        if (_btRemove==null) {
            _btRemove = new JButtonOperator(this, getBundleString("Remove"));
        }
        return _btRemove;
    }

    /**
     * Initializes (if necessary) and returns an object JButtonOperator 
     * for the dialog button "New".
     * @return the appropriate object JButtonOperator
     */
    public JButtonOperator btNew() {
        if (_btNew==null) {
            _btNew = new JButtonOperator(this, getBundleString("Add..."));
        }
        return _btNew;
    }

    /**
     * Initializes (if necessary) and returns an object JButtonOperator 
     * for the dialog button "Delete".
     * @return the appropriate object JButtonOperator
     */
    public JButtonOperator btDelete() {
        if (_btDelete==null) {
            _btDelete = new JButtonOperator(this, getBundleString("LBL_REMOVE_DRIVER"));
        }
        return _btDelete;
    }

    /**
     * Initializes (if necessary) and returns an object JButtonOperator 
     * for the dialog button "Find".
     * @return the appropriate object JButtonOperator
     */
    public JButtonOperator btFind() {
        if (_btFind==null) {
            _btFind = new JButtonOperator(this, getBundleString("Find"));
        }
        return _btFind;
    }

    /**
     * Initializes (if necessary) and returns an object JTextFieldOperator 
     * for the dialog text field "Name".
     * @return the appropriate object JTextFieldOperator
     */
    public JTextFieldOperator txtName() {
        if (_txtName==null) {
            _txtName = new JTextFieldOperator(this, 0);
        }
        return _txtName;
    }

    /**
     * Initializes (if necessary) and returns an object JTextFieldOperator 
     * for the dialog text field "Template".
     * @return the appropriate object JTextFieldOperator
     */
    public JTextFieldOperator txtTemplate() {
        if (_txtTemplate==null) {
            _txtTemplate = new JTextFieldOperator(this, 1);
        }
        return _txtTemplate;
    }

    /**
     * Initializes (if necessary) and returns an object JComboBoxOperator 
     * for the dialog drop-down list "Driver".
     * @return the appropriate object JComboBoxOperator
     */
    public JComboBoxOperator cboDriver() {
        if (_cboDriver==null) {
            _cboDriver = new JComboBoxOperator(this);
        }
        return _cboDriver;
    }

    /**
     * Initializes (if necessary) and returns an object JListOperator 
     * for the dialog list "Server Types".
     * @return the appropriate object JListOperator
     */
    public JListOperator lstServerTypes() {
        if (_lstServerTypes==null) {
            _lstServerTypes = new JListOperator(this,0);
        }
        return _lstServerTypes;
    }

    /**
     * Initializes (if necessary) and returns an object JListOperator 
     * for the dialog list "Jars".
     * @return the appropriate object JListOperator
     */
    public JListOperator lstJars() {
        if (_lstJars==null) {
            _lstJars = new JListOperator(this,1);
        }
        return _lstJars;
    }

    /**
     * Initializes all necessary controls.
     */
    public void verify() {
        btAdd();
        btRemove();
        btNew();
//        btDelete();
        btFind();
//        btClose();
//        btHelp();
        txtName();
        txtTemplate();
        cboDriver();
//        lstServerTypes();
//        lstJars();
        btOK();
        btCancel();
    }

    /**
     * Finds in a bundle file and returns an actual text of control component.
     * @param p_text string-key corresponding to required control component.
     * @return actual text of control component
     */
    public static String getBundleString(String p_text) {
        System.out.println("Getting bundle for " + p_text);
/*        try {
            return Bundle.getStringTrimmed("org.netbeans.modules.visualweb.dataconnectivity.actions.Bundle", p_text);
        } catch (JemmyException e) {}
        try {
            return Bundle.getStringTrimmed("org.netbeans.modules.visualweb.dataconnectivity.ui.Bundle", p_text);
        } catch (JemmyException e) {}
*/
        return p_text;
    }

}
