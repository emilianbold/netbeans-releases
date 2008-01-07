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

package org.netbeans.modules.visualweb.gravy.designer;

import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.operators.*;

/**
 * This abstract class implements test functionality for the dialog "Table Layout".
 */
public abstract class GeneralTableLayoutOperator extends NbDialogOperator{
    /**
     * Constructor of this class.
     */
    public GeneralTableLayoutOperator(){
        super(getBundleString("tblLayout"));
    }

    private JTabbedPaneOperator _tbpLayout;

    private JButtonOperator _btApply;
    private JButtonOperator _btNew;
    private JButtonOperator _btUp;
    private JButtonOperator _btDown;
    private JButtonOperator _btAdd;
    private JButtonOperator _btRemove;
    private JButtonOperator _btRemoveAll;
    private JListOperator _lstAvailable;
    private JListOperator _lstDisplayed;
    private JTextFieldOperator _txtHeader;
    private JTextFieldOperator _txtFooter;
    private JTextFieldOperator _txtValue;
    protected JComboBoxOperator _cboSource;
    protected JComboBoxOperator _cboComponentType;

    /**
     * Initializes (if necessary) and returns an object JTabbedPaneOperator 
     * for the dialog tabbed pane.
     * @return the appropriate object JTabbedPaneOperator
     */
    public JTabbedPaneOperator tbpLayout() {
        if (_tbpLayout==null) {
            _tbpLayout = new JTabbedPaneOperator(this);
        }
        return _tbpLayout;
    }

    /**
     * Initializes (if necessary) and returns an object JButtonOperator 
     * for the dialog button "Apply".
     * @return the appropriate object JButtonOperator
     */
    public JButtonOperator btApply() {
        if (_btApply==null) {
            _btApply = new JButtonOperator(this, Bundle.getStringTrimmed("com.sun.jsfcl.binding.Bundle", "apply"));
        }
        return _btApply;
    }

    /**
     * Initializes (if necessary) and returns an object JButtonOperator 
     * for the dialog button "New".
     * @return the appropriate object JButtonOperator
     */
    public JButtonOperator btNew() {
        if (_btNew==null) {
            _btNew = new JButtonOperator(this, getBundleString("new"));
        }
        return _btNew;
    }

    /**
     * Initializes (if necessary) and returns an object JButtonOperator 
     * for the dialog button "Up".
     * @return the appropriate object JButtonOperator
     */
    public JButtonOperator btUp() {
        if (_btUp==null) {
            _btUp = new JButtonOperator(this, getBundleString("up"));
        }
        return _btUp;
    }

    /**
     * Initializes (if necessary) and returns an object JButtonOperator 
     * for the dialog button "Down".
     * @return the appropriate object JButtonOperator
     */
    public JButtonOperator btDown() {
        if (_btDown==null) {
            _btDown = new JButtonOperator(this, getBundleString("down"));
        }
        return _btDown;
    }

    /**
     * Initializes (if necessary) and returns an object JButtonOperator 
     * for the dialog button "Add".
     * @return the appropriate object JButtonOperator
     */
    public JButtonOperator btAdd() {
        if (_btAdd==null) {
            _btAdd = new JButtonOperator(this, ">");
        }
        return _btAdd;
    }

    /**
     * Initializes (if necessary) and returns an object JButtonOperator 
     * for the dialog button "<".
     * @return the appropriate object JButtonOperator
     */
    public JButtonOperator btRemove() {
        if (_btRemove==null) {
            _btRemove = new JButtonOperator(this, "<");
        }
        return _btRemove;
    }

    /**
     * Initializes (if necessary) and returns an object JButtonOperator 
     * for the dialog button "<<".
     * @return the appropriate object JButtonOperator
     */
    public JButtonOperator btRemoveAll() {
        if (_btRemoveAll==null) {
            _btRemoveAll = new JButtonOperator(this, "<<");
        }
        return _btRemoveAll;
    }

    /**
     * Initializes (if necessary) and returns an object JListOperator 
     * for the dialog list "Available".
     * @return the appropriate object JListOperator
     */
    public JListOperator lstAvailable() {
        if (_lstAvailable==null) {
            _lstAvailable = new JListOperator(this,0);
        }
        return _lstAvailable;
    }

    /**
     * Initializes (if necessary) and returns an object JListOperator 
     * for the dialog list "Displayed".
     * @return the appropriate object JListOperator
     */
    public JListOperator lstDisplayed() {
        if (_lstDisplayed==null) {
            _lstDisplayed = new JListOperator(this,1);
        }
        return _lstDisplayed;
    }

    /**
     * Initializes (if necessary) and returns an object JTextFieldOperator 
     * for the dialog text field "Header".
     * @return the appropriate object JTextFieldOperator
     */
    public JTextFieldOperator txtHeader() {
        if (_txtHeader==null) {
            _txtHeader = new JTextFieldOperator(this, 0);
        }
        return _txtHeader;
    }

    /**
     * Initializes (if necessary) and returns an object JTextFieldOperator 
     * for the dialog text field "Footer".
     * @return the appropriate object JTextFieldOperator
     */
    public JTextFieldOperator txtFooter() {
        if (_txtFooter==null) {
            _txtFooter = new JTextFieldOperator(this, 1);
        }
        return _txtFooter;
    }

    /**
     * Initializes (if necessary) and returns an object JTextFieldOperator 
     * for the dialog text field "Value".
     * @return the appropriate object JTextFieldOperator
     */
    public JTextFieldOperator txtValue() {
        if (_txtValue==null) {
            _txtValue = new JTextFieldOperator(this, 2);
        }
        return _txtValue;
    }

    /**
     * Initializes (if necessary) and returns an object JComboBoxOperator 
     * for the dialog drop-down list "Source".
     * @return the appropriate object JComboBoxOperator
     */
    public JComboBoxOperator cboSource() {
        if (_cboSource==null) {
            _cboSource = new JComboBoxOperator(this,0);
        }
        return _cboSource;
    }

    /**
     * Initializes (if necessary) and returns an object JComboBoxOperator 
     * for the dialog drop-down list "Component Type".
     * @return the appropriate object JComboBoxOperator
     */
    public JComboBoxOperator cboComponentType() {
        if (_cboComponentType==null) {
            _cboComponentType = new JComboBoxOperator(this,1);
        }
        return _cboComponentType;
    }

    /**
     * Select the tab "Columns" in the tabbed pane.
     */
    public void selectColumnsTab(){
        tbpLayout().selectPage(getBundleString("cols"));
    }

    /**
     * Finds in a bundle file and returns an actual text of control component.
     * @param p_text string-key corresponding to required control component.
     * @return actual text of control component
     */
    public static String getBundleString(String p_text) {
        System.out.println("Getting bundle for " + p_text);
        try {
            return Bundle.getStringTrimmed("com.sun.jsfcl.std.table.Bundle", p_text);
        } catch (JemmyException e) {}
        return null;
    }

    /**
     * Initializes all necessary controls.
     */
    public void verify() {
        btOK();
        btApply();
        btCancel();
        btHelp();

        selectColumnsTab();

        btUp();
        btDown();
        btNew();
        btAdd();
        btRemove();
        btRemoveAll();
        lstAvailable();
        lstDisplayed();
        txtFooter();
        txtValue();
        cboSource();
        cboComponentType();
    }

    /**
     * Adds new column to the table.
     * @param column a column name
     */
    public void addColumn(String column){
        lstAvailable().selectItem(column);
        btAdd().push();
    }

    /**
     * Removes a column from the table.
     * @param column a column name
     */
    public void removeColumn(String column){
        lstDisplayed().selectItem(column);
        btRemove().push();
    }

    /**
     * Removes all columns from the table.
     */
    public void removeAllColumns(){
        btRemoveAll().push();
    }
}
