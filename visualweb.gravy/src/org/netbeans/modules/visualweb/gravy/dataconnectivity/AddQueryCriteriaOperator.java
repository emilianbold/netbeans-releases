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
import org.netbeans.jemmy.operators.JRadioButtonOperator;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.NbDialogOperator;

/**
 * This class implements test functionality for "Add Query Criteria" dialog
 */
public class AddQueryCriteriaOperator extends NbDialogOperator{
    private JComboBoxOperator _cboCompareType;
    private JTextFieldOperator _txtValue, _txtParameter;
    private JRadioButtonOperator _rbtValue, _rbtParameter;

    /**
     * Creates new instance of this class.
     */
    public AddQueryCriteriaOperator(){
        super(getBundleString("ADD_QUERY_CRITERIA_TITLE"));
    }
    
    /**
     * Initializes (if necessary) and returns an object JComboBoxOperator 
     * for the dialog drop-down list "Compare Type".
     * @return the appropriate object JComboBoxOperator
     */
    public JComboBoxOperator cboCompareType() {
        if (_cboCompareType==null) {
            _cboCompareType = new JComboBoxOperator(this);
        }
        return _cboCompareType;
    }

    /**
     * Initializes (if necessary) and returns an object JTextFieldOperator 
     * for the dialog text field "Value".
     * @return the appropriate object JTextFieldOperator
     */
    public JTextFieldOperator txtValue() {
        if (_txtValue==null) {
            _txtValue = new JTextFieldOperator(this, 0);
        }
        return _txtValue;
    }

    /**
     * Initializes (if necessary) and returns an object JTextFieldOperator 
     * for the dialog text field "Parameter".
     * @return the appropriate object JTextFieldOperator
     */
    public JTextFieldOperator txtParameter() {
        if (_txtParameter==null) {
            _txtParameter = new JTextFieldOperator(this, 1);
        }
        return _txtParameter;
    }

    /**
     * Initializes (if necessary) and returns an object JRadioButtonOperator
     * for the dialog radio-button "Value".
     * @return the appropriate object JRadioButtonOperator
     */
    public JRadioButtonOperator rbtValue() {
        if (_rbtValue == null) {
            _rbtValue = new JRadioButtonOperator(this, 0);
        }
        return _rbtValue;
    }

    /**
     * Initializes (if necessary) and returns an object JRadioButtonOperator
     * for the dialog radio-button "Parameter".
     * @return the appropriate object JRadioButtonOperator
     */
    public JRadioButtonOperator rbtParameter() {
        if (_rbtParameter == null) {
            _rbtParameter = new JRadioButtonOperator(this, 1);
        }
        return _rbtParameter;
    }

    /**
     * Initializes all necessary controls.
     */
    public void verify() {
        btOK();
        btCancel();
        btHelp();
        txtValue();
        txtParameter();
        cboCompareType();
        rbtValue();
        rbtParameter();
    }

    /**
     * Finds in a bundle file and returns an actual text of control component.
     * @param p_text string-key corresponding to required control component.
     * @return actual text of control component
     */
    public static String getBundleString(String p_text) {
        return QueryBuilderOperator.getBundleString(p_text);
    }

}
