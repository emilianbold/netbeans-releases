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
package org.netbeans.jellytools.modules.form.properties.editors;

import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JListOperator;

/**
 *
 * Handles Select Method dialog opened after click on "..." button from
 * {@link ParametersPickerOperator Form Connection panel}. It contains combo
 * box of available components and list of methods for the selected component.
 * OK and Cancel buttons are inhereted from NbDialog.
 * <p>
 * Usage:<br>
 * <pre>
 *      See example in {@link ParametersPickerOperator}.
 * </pre>
 *
 * @see FormCustomEditorOperator
 * @see ParametersPickerOperator
 * @see PropertyPickerOperator
 *
 * @author Jiri.Skrivanek@sun.com
 */
public class MethodPickerOperator extends NbDialogOperator {
    
    /** Components operators. */
    private JLabelOperator _lblComponent;
    private JComboBoxOperator _cboComponent;
    private JLabelOperator _lblMethods;
    private JListOperator _lstMethods;
    
    /** Waits for dialog with title "Select Method". */
    public MethodPickerOperator() {
        super(Bundle.getStringTrimmed("org.netbeans.modules.form.Bundle", 
                                      "CTL_FMT_CW_SelectMethod"));
    }
    
    
    /** Returns operator of "Component:" label.
     * @return  JLabelOperator instance of "Component:" label
     */
    public JLabelOperator lblComponent() {
        if(_lblComponent == null) {
            _lblComponent = new JLabelOperator(this, 
                            Bundle.getStringTrimmed("org.netbeans.modules.form.Bundle", 
                                                    "CTL_CW_Component"));
        }
        return _lblComponent;
    }
    
    /** Returns operator of combo box of available components.
     * @return  JComboBoxOperator instance of components combo box
     */
    public JComboBoxOperator cboComponent() {
        if(_cboComponent == null) {
            _cboComponent = new JComboBoxOperator(this);
        }
        return _cboComponent;
    }
    
    /** Returns operator of "Methods" label.
     * @return  JLabelOperator instance of "Methods" label
     */
    public JLabelOperator lblMethods() {
        if(_lblMethods == null) {
            _lblMethods = new JLabelOperator( this, 
                            Bundle.getStringTrimmed("org.netbeans.modules.form.Bundle",
                                                    "CTL_CW_MethodList"));
        }
        return _lblMethods;
    }
    
    /** Returns operator of list of properties of selected component.
     * @return  JListOperator instance of list of properties
     */
    public JListOperator lstMethods() {
        if(_lstMethods == null) {
            _lstMethods = new JListOperator(this);
        }
        return _lstMethods;
    }
    
    //****************************************
    // Low-level functionality definition part
    //****************************************
    
    /** Selects given item in combo box of available components.
     * @param item item to be selected
     */
    public void setComponent(String item) {
        cboComponent().selectItem(item);
    }
    
    /** Selects given item in list of properties of selected component.
     * @param item item to be selected
     */
    public void setMethods(String item) {
        lstMethods().selectItem(item);
    }

    /** Performs verification by accessing all sub-components */    
    public void verify() {
        lblComponent();
        lblMethods();
        cboComponent();
        lstMethods();
    }

}
