/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.jellytools.modules.form.properties.editors;

import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JListOperator;

/**
 * Handles Select Property dialog opened after click on "..." button from
 * {@link ParametersPickerOperator Form Connection panel}. It contains combo
 * box of available components and list of properties for the selected component.
 * OK and Cancel buttons are inhereted from NbDialog.
 * <p>
 * Usage:<br>
 * <pre>
 *      See example in {@link ParametersPickerOperator}.
 * </pre>
 *
 * @see FormCustomEditorOperator
 * @see ParametersPickerOperator
 * @see MethodPickerOperator
 *
 * @author Jiri.Skrivanek@sun.com
 */
public class PropertyPickerOperator extends NbDialogOperator {
    
    /** Components operators. */
    private JLabelOperator _lblComponent;
    private JComboBoxOperator _cboComponent;
    private JLabelOperator _lblProperties;
    private JListOperator _lstProperties;
    
    /** Waits for dialog with title "Select Property". */
    public PropertyPickerOperator() {
        super(Bundle.getStringTrimmed("org.netbeans.modules.form.Bundle", 
                                      "CTL_FMT_CW_SelectProperty"));
        
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
    
    /** Returns operator of "Properties:" label.
     * @return  JLabelOperator instance of "Properties:" label
     */
    public JLabelOperator lblProperties() {
        if(_lblProperties == null) {
            _lblProperties = new JLabelOperator( this, 
                            Bundle.getStringTrimmed("org.netbeans.modules.form.Bundle",
                                                    "CTL_CW_PropertyList"));
        }
        return _lblProperties;
    }
    
    /** Returns operator of list of properties of selected component.
     * @return  JListOperator instance of list of properties
     */
    public JListOperator lstProperties() {
        if(_lstProperties == null) {
            _lstProperties = new JListOperator(this);
        }
        return _lstProperties;
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
    public void setProperty(String item) {
        lstProperties().selectItem(item);
    }

    /** Performs verification by accessing all sub-components */    
    public void verify() {
        lblComponent();
        lblProperties();
        cboComponent();
        lstProperties();
    }
    
}
