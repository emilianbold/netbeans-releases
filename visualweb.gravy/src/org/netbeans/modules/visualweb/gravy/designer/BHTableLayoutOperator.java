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
import org.netbeans.modules.visualweb.gravy.Util;

/**
 * This class implements test functionality for the dialog "Table Layout", 
 * related to the component "Table" from the palette "Basic".
 */
public class BHTableLayoutOperator extends GeneralTableLayoutOperator{
    /**
     * Creates an instance of this class.
     */
    public BHTableLayoutOperator(){
        super();
    }

    /**
     * Creates and returns an instance of this class via using 
     * the appropriate item of a poup menu.
     * @param menu a popup menu, related to a table
     * @return a new created object BHTableLayoutOperator
     */
    public static BHTableLayoutOperator invoke(JPopupMenuOperator menu){
        Util.wait(300);
        menu.pushMenuNoBlock(getBundleString("tblLayoutEllipse"));
        return new BHTableLayoutOperator();
    }

    private JButtonOperator _btBrowseFirst;
    private JButtonOperator _btBrowsePrevious;
    private JButtonOperator _btBrowseNext;
    private JButtonOperator _btBrowseLast;
    private JTextFieldOperator _txtPageSize;
    private JTextFieldOperator _txtFirst;
    private JTextFieldOperator _txtPrevious;
    private JTextFieldOperator _txtNext;
    private JTextFieldOperator _txtLast;
    private JComboBoxOperator _cboPosition;
    private JComboBoxOperator _cboAlignment;
    private JComboBoxOperator _cboNavigation;
    private JCheckBoxOperator _cbEnable;
    private JCheckBoxOperator _cbFirst;
    private JCheckBoxOperator _cbPrevious;
    private JCheckBoxOperator _cbNext;
    private JCheckBoxOperator _cbLast;

    /**
     * Initializes (if necessary) and returns an object JButtonOperator 
     * for the dialog button "<<".
     * @return the appropriate object JButtonOperator
     */
    public JButtonOperator btBrowseFirst() {
        if (_btBrowseFirst==null) {
            _btBrowseFirst = new JButtonOperator(this, getBundleString("browseEllipse"),0);
        }
        return _btBrowseFirst;
    }

    /**
     * Initializes (if necessary) and returns an object JButtonOperator 
     * for the dialog button "<".
     * @return the appropriate object JButtonOperator
     */
    public JButtonOperator btBrowsePrevious() {
        if (_btBrowsePrevious==null) {
            _btBrowsePrevious = new JButtonOperator(this, getBundleString("browseEllipse"),1);
        }
        return _btBrowsePrevious;
    }

    /**
     * Initializes (if necessary) and returns an object JButtonOperator 
     * for the dialog button ">".
     * @return the appropriate object JButtonOperator
     */
    public JButtonOperator btBrowseNext() {
        if (_btBrowseNext==null) {
            _btBrowseNext = new JButtonOperator(this, getBundleString("browseEllipse"),2);
        }
        return _btBrowseNext;
    }

    /**
     * Initializes (if necessary) and returns an object JButtonOperator 
     * for the dialog button ">>".
     * @return the appropriate object JButtonOperator
     */
    public JButtonOperator btBrowseLast() {
        if (_btBrowseLast==null) {
            _btBrowseLast = new JButtonOperator(this, getBundleString("browseEllipse"),3);
        }
        return _btBrowseLast;
    }

    /**
     * Initializes (if necessary) and returns an object JTextFieldOperator 
     * for the dialog text field "Page Size".
     * @return the appropriate object JTextFieldOperator
     */
    public JTextFieldOperator txtPageSize() {
        if (_txtPageSize==null) {
            _txtPageSize = new JTextFieldOperator(this, 0);
        }
        return _txtPageSize;
    }

    /**
     * Initializes (if necessary) and returns an object JTextFieldOperator 
     * for the dialog text field "First".
     * @return the appropriate object JTextFieldOperator
     */
    public JTextFieldOperator txtFirst() {
        if (_txtFirst==null) {
            _txtFirst = new JTextFieldOperator(this, 1);
        }
        return _txtFirst;
    }

    /**
     * Initializes (if necessary) and returns an object JTextFieldOperator 
     * for the dialog text field "Previous".
     * @return the appropriate object JTextFieldOperator
     */
    public JTextFieldOperator txtPrevious() {
        if (_txtPrevious==null) {
            _txtPrevious = new JTextFieldOperator(this, 2);
        }
        return _txtPrevious;
    }

    /**
     * Initializes (if necessary) and returns an object JTextFieldOperator 
     * for the dialog text field "Next".
     * @return the appropriate object JTextFieldOperator
     */
    public JTextFieldOperator txtNext() {
        if (_txtNext==null) {
            _txtNext = new JTextFieldOperator(this, 3);
        }
        return _txtNext;
    }

    /**
     * Initializes (if necessary) and returns an object JTextFieldOperator 
     * for the dialog text field "Last".
     * @return the appropriate object JTextFieldOperator
     */
    public JTextFieldOperator txtLast() {
        if (_txtLast==null) {
            _txtLast = new JTextFieldOperator(this, 4);
        }
        return _txtLast;
    }

    /**
     * Initializes (if necessary) and returns an object JCheckBoxOperator 
     * for the dialog check-box "Enable".
     * @return the appropriate object JCheckBoxOperator
     */
    public JCheckBoxOperator cbEnable() {
        if (_cbEnable==null) {
            _cbEnable = new JCheckBoxOperator(this, getBundleString("enablePaging"));
        }
        return _cbEnable;
    }

    /**
     * Initializes (if necessary) and returns an object JCheckBoxOperator 
     * for the dialog check-box "First".
     * @return the appropriate object JCheckBoxOperator
     */
    public JCheckBoxOperator cbFirst() {
        if (_cbFirst==null) {
            _cbFirst = new JCheckBoxOperator(this, getBundleString("firstPage"));
        }
        return _cbFirst;
    }

    /**
     * Initializes (if necessary) and returns an object JCheckBoxOperator 
     * for the dialog check-box "Previous".
     * @return the appropriate object JCheckBoxOperator
     */
    public JCheckBoxOperator cbPrevious() {
        if (_cbPrevious==null) {
            _cbPrevious = new JCheckBoxOperator(this, getBundleString("prevPage"));
        }
        return _cbPrevious;
    }

    /**
     * Initializes (if necessary) and returns an object JCheckBoxOperator 
     * for the dialog check-box "Next".
     * @return the appropriate object JCheckBoxOperator
     */
    public JCheckBoxOperator cbNext() {
        if (_cbNext==null) {
            _cbNext = new JCheckBoxOperator(this, getBundleString("nextPage"));
        }
        return _cbNext;
    }

    /**
     * Initializes (if necessary) and returns an object JCheckBoxOperator 
     * for the dialog check-box "Last".
     * @return the appropriate object JCheckBoxOperator
     */
    public JCheckBoxOperator cbLast() {
        if (_cbLast==null) {
            _cbLast = new JCheckBoxOperator(this, getBundleString("lastPage"));
        }
        return _cbLast;
    }

    /**
     * Initializes (if necessary) and returns an object JComboBoxOperator 
     * for the dialog drop-down list "Navigation".
     * @return the appropriate object JComboBoxOperator
     */
    public JComboBoxOperator cboNavigation() {
        if (_cboNavigation==null) {
            _cboNavigation = new JComboBoxOperator(this,0);
        }
        return _cboNavigation;
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
            _cboComponentType = new JComboBoxOperator(this,2);
        }
        return _cboComponentType;
    }

    /**
     * Initializes (if necessary) and returns an object JComboBoxOperator 
     * for the dialog drop-down list "Position".
     * @return the appropriate object JComboBoxOperator
     */
    public JComboBoxOperator cboPosition() {
        if (_cboPosition==null) {
            _cboPosition = new JComboBoxOperator(this,1);
        }
        return _cboPosition;
    }

    /**
     * Initializes (if necessary) and returns an object JComboBoxOperator 
     * for the dialog drop-down list "Alignment".
     * @return the appropriate object JComboBoxOperator
     */
    public JComboBoxOperator cboAlignment() {
        if (_cboAlignment==null) {
            _cboAlignment = new JComboBoxOperator(this,3);
        }
        return _cboAlignment;
    }

    /**
     * Select the tab "Columns" in the tabbed pane.
     */
    public void selectColumnsTab(){
        tbpLayout().selectPage(getBundleString("cols"));
    }

    /**
     * Select the tab "Options" in the tabbed pane.
     */
    public void selectOptionsTab(){
        tbpLayout().selectPage(getBundleString("paging"));
    }

    /**
     * Initializes all necessary controls.
     */
    public void verify() {

        super.verify();

        selectOptionsTab();

        btBrowseFirst();
        btBrowsePrevious();
        btBrowseNext();
        btBrowseLast();
        cboNavigation();
        cboPosition();
        cboAlignment();
        cbEnable();
        cbFirst();
        cbPrevious();
        cbNext();
        cbLast();
        txtPageSize();
        txtFirst();
        txtPrevious();
        txtNext();
        txtLast();

        selectColumnsTab();
    }

    /**
     * Finds in a bundle file and returns an actual text of control component.
     * @param p_text string-key corresponding to required control component.
     * @return actual text of control component
     */
    public static String getBundleString(String p_text) {
        System.out.println("Getting bundle for " + p_text);
        try {
            return Bundle.getStringTrimmed("org.netbeans.modules.visualweb.faces.dt.std.table.Bundle", p_text);
        } catch (JemmyException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }
}
