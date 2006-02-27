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
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JEditorPaneOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JRadioButtonOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;

/**
 * Handles Form Connection panel within {@link FormCustomEditorOperator Form Custom Editor}.
 * It contains radio buttons to select source of parameter and appropriate
 * inputs to specify source.
 *
 * <p>
 * Usage:<br>
 * <pre>
 *      PropertySheetOperator pso = new PropertySheetOperator("Properties of doGarbage");
 *      String propertyName = "text";
 *      Property property = new Property(pso, propertyName);
 *      property.openEditor();
 *      FormCustomEditorOperator fceo = new FormCustomEditorOperator(propertyName);
 *      // ParametersPickerOperator
 *      fceo.setMode("Form Connection");
 *      ParametersPickerOperator paramPicker = new ParametersPickerOperator(propertyName);
 *      paramPicker.value();
 *      paramPicker.setValue("myValue");
 *      paramPicker.userCode();
 *      paramPicker.setUserCode("// my code");
 *
 *      // PropertyPickerOperator
 *      paramPicker.property();
 *      paramPicker.selectProperty();
 *      PropertyPickerOperator propertyPicker = new PropertyPickerOperator();
 *      propertyPicker.setComponent("Form");
 *      propertyPicker.setProperty("title");
 *      propertyPicker.ok();
 *
 *      // MethodPickerOperator
 *      paramPicker.methodCall();
 *      paramPicker.selectMethod();
 *      MethodPickerOperator methodPicker = new MethodPickerOperator();
 *      methodPicker.setComponent("Form");
 *      methodPicker.setMethods("getTitle()");
 *      methodPicker.ok();
 *
 *      paramPicker.ok();
 *      fceo.ok();
 * </pre>
 *
 * @see FormCustomEditorOperator
 * @see PropertyPickerOperator
 * @see MethodPickerOperator
 *
 * @author Jiri.Skrivanek@sun.com
 */
public class ParametersPickerOperator extends FormCustomEditorOperator {
    
    /** Components operators. */
    private JLabelOperator _lblGetParameterFrom;
    private JRadioButtonOperator _rbValue;
    private JTextFieldOperator _txtValue;
    private JRadioButtonOperator _rbBean;
    private JComboBoxOperator _cboBean;
    private JRadioButtonOperator _rbProperty;
    private JTextFieldOperator _txtProperty;
    private JButtonOperator _btSelectProperty;
    private JRadioButtonOperator _rbMethodCall;
    private JTextFieldOperator _txtMethodCall;
    private JButtonOperator _btSelectMethod;
    private JRadioButtonOperator _rbUserCode;
    private JEditorPaneOperator _txtUserCode;
    
    /** Waits for dialog with specified title.
     * @param propertyName name of property used as title of dialog
     */
    public ParametersPickerOperator(String propertyName) {
        super(propertyName);
    }
    
    /** Returns operator of "Get Parameter From:" label.
     * @return  JLabelOperator instance of "Get Parameter From:" label
     */
    public JLabelOperator lblGetParameterFrom() {
        if(_lblGetParameterFrom == null) {
            _lblGetParameterFrom = new JLabelOperator(this, 
                            Bundle.getString("org.netbeans.modules.form.Bundle", 
                                             "CTL_CW_GetParametersFrom"));
        }
        return _lblGetParameterFrom;
    }
    
    /** Returns operator of "Value:" radio button.
     * @return  JRadioButtonOperator instance of "Value:" radio button
     */
    public JRadioButtonOperator rbValue() {
        if(_rbValue == null) {
            _rbValue = new JRadioButtonOperator(this, 
                            Bundle.getStringTrimmed("org.netbeans.modules.form.Bundle",
                                                    "CTL_CW_Value"));
        }
        return _rbValue;
    }
    
    /** Returns operator of "Value:" text field.
     * @return  JTextFieldOperator instance of "Value:" text field
     */
    public JTextFieldOperator txtValue() {
        if(_txtValue == null) {
            _txtValue = new JTextFieldOperator(this, 0);
        }
        return _txtValue;
    }
    
    /** Returns operator of "Bean:" radio button.
     * @return  JRadioButtonOperator instance of "Bean:" radio button
     */
    public JRadioButtonOperator rbBean() {
        if(_rbBean == null) {
            _rbBean = new JRadioButtonOperator(this, 
                            Bundle.getStringTrimmed("org.netbeans.modules.form.Bundle",
                                                    "CTL_CW_Bean"));
        }
        return _rbBean;
    }
    
    /** Returns operator of "Bean:" combo box.
     * @return  JComboBoxOperator instance of "Bean:" combo box
     */
    public JComboBoxOperator cboBean() {
        if(_cboBean == null) {
            _cboBean = new JComboBoxOperator(this, 1);
        }
        return _cboBean;
    }
    
    /** Returns operator of "Property:" radio button.
     * @return  JRadioButtonOperator instance of "Property:" radio button
     */
    public JRadioButtonOperator rbProperty() {
        if(_rbProperty == null) {
            _rbProperty = new JRadioButtonOperator(this, 
                            Bundle.getStringTrimmed("org.netbeans.modules.form.Bundle",
                                                    "CTL_CW_Property"));
        }
        return _rbProperty;
    }
    
    /** Returns operator of "Property:" text field.
     * @return  JTextFieldOperator instance of "Property:" text field
     */
    public JTextFieldOperator txtProperty() {
        if(_txtProperty == null) {
            _txtProperty = new JTextFieldOperator(this, 1);
        }
        return _txtProperty;
    }
    
    /** Returns operator of "..." button for "Property:" field.
     * @return  JButtonOperator instance of "..." button
     */
    public JButtonOperator btSelectProperty() {
        if(_btSelectProperty == null) {
            _btSelectProperty = new JButtonOperator(this, "...", 0); // NOI18N
        }
        return _btSelectProperty;
    }
    
    /** Returns operator of "Method Call:" radio button.
     * @return  JRadioButtonOperator instance of "Method Call:" radio button
     */
    public JRadioButtonOperator rbMethodCall() {
        if(_rbMethodCall == null) {
            _rbMethodCall = new JRadioButtonOperator(this, 
                            Bundle.getStringTrimmed("org.netbeans.modules.form.Bundle",
                                                    "CTL_CW_Method"));
        }
        return _rbMethodCall;
    }
    
    /** Returns operator of "Method Call:" text field.
     * @return  JTextFieldOperator instance of "Method Call:" text field
     */
    public JTextFieldOperator txtMethodCall() {
        if(_txtMethodCall==null) {
            _txtMethodCall = new JTextFieldOperator(this, 2);
        }
        return _txtMethodCall;
    }
    
    /** Returns operator of "..." button for "Method Call:" field.
     * @return  JButtonOperator instance of "..." button
     */
    public JButtonOperator btSelectMethod() {
        if(_btSelectMethod == null) {
            _btSelectMethod = new JButtonOperator(this, "...", 1); // NOI18N
        }
        return _btSelectMethod;
    }
    
    /** Returns operator of "User Code:" radio button.
     * @return  JRadioButtonOperator instance of "User Code:" radio button
     */
    public JRadioButtonOperator rbUserCode() {
        if(_rbUserCode == null) {
            _rbUserCode = new JRadioButtonOperator(this, 
                            Bundle.getStringTrimmed("org.netbeans.modules.form.Bundle",
                                                    "CTL_CW_UserCode"));
        }
        return _rbUserCode;
    }
    
    /** Returns operator of "User Code:" text field.
     * @return  JTextFieldOperator instance of "User Code:" text field
     */
    public JEditorPaneOperator txtUserCode() {
        if(_txtUserCode == null) {
            _txtUserCode = new JEditorPaneOperator(this);
        }
        return _txtUserCode;
    }
    
    //****************************************
    // Low-level functionality definition part
    //****************************************
    
    /** Pushes "Value:" radion button. */
    public void value() {
        rbValue().push();
    }
    
    /** Sets specified text into "Value" text field.
     * @param text text to be set to text field
     */
    public void setValue(String text) {
        txtValue().setText(text);
    }
    
    /** Pushes "Bean:" radio button. */
    public void bean() {
        rbBean().push();
    }
    
    /** Selects specified item from "Bean:" combo box.
     * @param item item to be selected
     */
    public void setBean(String item) {
        cboBean().setSelectedItem(item);
    }
    
    /** Pushes "Property:" radio button. */
    public void property() {
        rbProperty().push();
    }
    
    /** Clicks on ... JButton in Property field. It invokes "Select Property"
     * dialog. Use {@link PropertyPickerOperator} to test the dialog. */
    public void selectProperty() {
        btSelectProperty().pushNoBlock();
    }
    
    /** Pushes "Method Call:" radio button. */
    public void methodCall() {
        rbMethodCall().push();
    }
    
    /** Clicks on ... JButton in Method Call field. It invokes "Select Method"
     * dialog. Use {@link MethodPickerOperator} to test the dialog. */
    public void selectMethod() {
        btSelectMethod().pushNoBlock();
    }
    
    /** Pushes "User Code:" radio button. */
    public void userCode() {
        rbUserCode().push();
    }
    
    /** Sets specified text into "User Code" editor pane.
     * @param text text to set to editor pane
     */
    public void setUserCode( String text ) {
        txtUserCode().setText(text);
    }
    
    
    /** Performs verification by accessing all sub-components */    
    public void verify() {
        lblGetParameterFrom();
        txtMethodCall();
        txtProperty();
        txtUserCode();
        txtValue();
        rbBean();
        rbMethodCall();
        rbProperty();
        rbUserCode();
        rbValue();
        btSelectMethod();
        btSelectProperty();
        cboBean();
        super.verify();
    }

}
