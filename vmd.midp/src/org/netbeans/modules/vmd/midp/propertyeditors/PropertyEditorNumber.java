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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.vmd.midp.propertyeditors;

import java.awt.BorderLayout;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Pattern;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.propertyeditors.usercode.PropertyEditorUserCode;
import org.netbeans.modules.vmd.midp.propertyeditors.usercode.PropertyEditorElement;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;

/**
 *
 * @author Anton Chechel
 */
public class PropertyEditorNumber extends PropertyEditorUserCode implements PropertyEditorElement {
    
    /**
     * The text to be shown as a warning if user inputs incorrect characters
     */
    public static final String NON_DIGITS_TEXT = NbBundle.getMessage(PropertyEditorGaugeMaxValue.class, "MSG_NON_DIGIT_CHARS"); // NOI18N
    
    private CustomEditor customEditor;
    private JRadioButton radioButton;
    
    private PropertyEditorNumber() {
        super();
        initComponents();
        
        Collection<PropertyEditorElement> elements = new ArrayList<PropertyEditorElement>(1);
        elements.add(this);
        initElements(elements);
    }
    
    /**
     * Creates instance of property editor for integer type
     *
     * @return propertyEditor
     */
    public static final PropertyEditorNumber createIntegerInstance() {
        return new PropertyEditorNumber();
    }
    
    /**
     * Creates instance of property editor for long type
     *
     * @return propertyEditor
     */
    public static final PropertyEditorNumber createLongInstance() {
        return new PropertyEditorNumber(){
            @Override
            protected String getLocalizedRadioButtonLabel() {
                return NbBundle.getMessage(PropertyEditorNumber.class, "LBL_LONG_STR"); // NOI18N
            }
            
            @Override
            protected void saveValue(String text) {
                if (text.length() > 0) {
                    long longValue = 0;
                    try {
                        text = prepareText(text);
                        longValue = Long.parseLong(text);
                    } catch (NumberFormatException e) {
                    }
                    super.setValue(MidpTypes.createLongValue(longValue));
                }
            }
        };
    }
    
    /**
     * Creates instance of property editor for byte type
     *
     * @return propertyEditor
     */
    public static final PropertyEditorNumber createByteInstance() {
        return new PropertyEditorNumber(){
            @Override
            protected String getLocalizedRadioButtonLabel() {
                return NbBundle.getMessage(PropertyEditorNumber.class, "LBL_BYTE_STR"); // NOI18N
            }
            
            @Override
            protected void saveValue(String text) {
                if (text.length() > 0) {
                    byte byteValue = 0;
                    try {
                        text = prepareText(text);
                        byteValue = Byte.parseByte(text);
                    } catch (NumberFormatException e) {
                    }
                    super.setValue(MidpTypes.createByteValue(byteValue));
                }
            }
        };
    }
    
    /**
     * Creates instance of property editor for short type
     *
     * @return propertyEditor
     */
    public static final PropertyEditorNumber createShortInstance() {
        return new PropertyEditorNumber(){
            @Override
            protected String getLocalizedRadioButtonLabel() {
                return NbBundle.getMessage(PropertyEditorNumber.class, "LBL_SHORT_STR"); // NOI18N
            }
            
            @Override
            protected void saveValue(String text) {
                if (text.length() > 0) {
                    short shortValue = 0;
                    try {
                        text = prepareText(text);
                        shortValue = Short.parseShort(text);
                    } catch (NumberFormatException e) {
                    }
                    super.setValue(MidpTypes.createShortValue(shortValue));
                }
            }
        };
    }
    
    /**
     * Creates instance of property editor for float type
     *
     * @return propertyEditor
     */
    public static final PropertyEditorNumber createFloatInstance() {
        return new PropertyEditorNumber(){
            @Override
            protected boolean isTextCorrect(String text) {
                return Pattern.matches("[\\d\\-\\.]+", text); // NOI18N
            }
            
            @Override
            protected String prepareText(String text) {
                return text.replaceAll("[^0-9\\-\\.]+", ""); // NOI18N
            }
            
            @Override
            protected String getLocalizedRadioButtonLabel() {
                return NbBundle.getMessage(PropertyEditorNumber.class, "LBL_FLOAT_STR"); // NOI18N
            }
            
            @Override
            protected void saveValue(String text) {
                if (text.length() > 0) {
                    float floatValue = 0;
                    try {
                        text = prepareText(text);
                        floatValue = Float.parseFloat(text);
                    } catch (NumberFormatException e) {
                    }
                    super.setValue(MidpTypes.createFloatValue(floatValue));
                }
            }
        };
    }
    
    /**
     * Creates instance of property editor for double type
     *
     * @return propertyEditor
     */
    public static final PropertyEditorNumber createDoubleInstance() {
        return new PropertyEditorNumber(){
            @Override
            protected boolean isTextCorrect(String text) {
                return Pattern.matches("[\\d\\-\\.]+", text); // NOI18N
            }
            
            @Override
            protected String prepareText(String text) {
                return text.replaceAll("[^0-9\\-\\.]+", ""); // NOI18N
            }
            
            @Override
            protected String getLocalizedRadioButtonLabel() {
                return NbBundle.getMessage(PropertyEditorNumber.class, "LBL_DOUBLE_STR"); // NOI18N
            }
            
            @Override
            protected void saveValue(String text) {
                if (text.length() > 0) {
                    double doubleValue = 0;
                    try {
                        text = prepareText(text);
                        doubleValue = Double.parseDouble(text);
                    } catch (NumberFormatException e) {
                    }
                    super.setValue(MidpTypes.createDoubleValue(doubleValue));
                }
            }
        };
    }
    
    /**
     * Creates instance of property editor for char type
     *
     * @return propertyEditor
     */
    public static final PropertyEditorNumber createCharInstance() {
        return new PropertyEditorNumber(){
            @Override
            protected boolean isTextCorrect(String text) {
                return Pattern.matches("[\\d\\-]+", text); // NOI18N
            }
            
            @Override
            protected String prepareText(String text) {
                return text.replaceAll("[^0-9\\-]+", ""); // NOI18N
            }
            
            @Override
            protected String getLocalizedRadioButtonLabel() {
                return NbBundle.getMessage(PropertyEditorNumber.class, "LBL_CHAR_STR"); // NOI18N
            }
            
            @Override
            protected void saveValue(String text) {
                if (text.length() > 0) {
                    char charValue = 0;
                    try {
                        text = prepareText(text);
                        charValue = text.charAt(0);
                    } catch (NumberFormatException e) {
                    }
                    super.setValue(MidpTypes.createCharValue(charValue));
                }
            }
        };
    }
    
    private void initComponents() {
        radioButton = new JRadioButton();
        Mnemonics.setLocalizedText(radioButton, getLocalizedRadioButtonLabel());
        customEditor = new CustomEditor();
    }
    
    /**
     * Checks whether text is in correct format for given property editor.
     * For example for integer property editor text must match [\d\-]+ regex
     *
     * @param text to be checked
     * @return result
     */
    protected boolean isTextCorrect(String text) {
        return Pattern.matches("[\\d\\-]+", text) || // NOI18N
                // hexadecimal support
                isHexFormat(text); // NOI18N
    }
    
    /**
     * Removes all incorrect characters for for given property editor.
     * For example for integer property editor removes all chars except [\d\-]+ regex
     *
     * @param text to be checked
     * @return result text
     */
    protected String prepareText(String text) {
        // hex
        if (isHexFormat(text)) { // NOI18N
            text = text.replaceAll("[^0-9\\-0xabcdefABCDEF]+", ""); // NOI18N
            return text.replace("0x", ""); // NOI18N
        }
        return text.replaceAll("[^0-9\\-]+", ""); // NOI18N
    }
    
    /**
     * Returns localized label for radio button for given format.
     *
     * @return localized label
     */
    protected String getLocalizedRadioButtonLabel() {
        return NbBundle.getMessage(PropertyEditorNumber.class, "LBL_INTEGER_STR"); // NOI18N
    }
    
    /**
     * Saves text as a proper property value
     *
     * @param text to be parsed and saved
     */
    protected void saveValue(String text) {
        if (text.length() > 0) {
            int intValue = 0;
            try {
                if (isHexFormat(text)) { // NOI18N
                    text = prepareText(text);
                    intValue = Integer.parseInt(text, 16);
                } else {
                    text = prepareText(text);
                    intValue = Integer.parseInt(text);
                }
            } catch (NumberFormatException e) {
            }
            super.setValue(MidpTypes.createIntegerValue(intValue));
        }
    }
    
    private boolean isHexFormat(String text) {
        return text.matches("-?0x[\\d\\abcdefABCDEF]+"); //NOI18N
    }
    
    /**
     * Returns component to represent custom editor in propertyEditorUserCode
     *
     * @return component
     */
    public JComponent getCustomEditorComponent() {
        return customEditor;
    }
    
    /**
     * Returns radio button for propertyEditorUserCode
     *
     * @return radioButton
     */
    public JRadioButton getRadioButton() {
        return radioButton;
    }
    
    /**
     * Determines whether radioButton should be selected by default in the propertyEditorUserCode
     *
     * @return isSelected
     */
    public boolean isInitiallySelected() {
        return true;
    }
    
    /**
     * Determines whether custom component resizable vertically in the propertyEditorUserCode
     *
     * @return isResizable
     */
    public boolean isVerticallyResizable() {
        return false;
    }
    
    @Override
    public String getAsText() {
        String superText = super.getAsText();
        if (superText != null) {
            return superText;
        }
        
        PropertyValue value = (PropertyValue) super.getValue();
        return String.valueOf(value.getPrimitiveValue());
    }
    
    /**
     * Sets propertyValue from text
     *
     * @param text
     * @see java.beans.PropertyEditor#setAsText
     */
    public void setTextForPropertyValue (String text) {
        saveValue(text);
    }
    
    /**
     * Returns text for additional operations with propertyValue, not used here
     *
     * @return null
     */
    public String getTextForPropertyValue () {
        return null;
    }
    
    /**
     * Sets initial propertyValue before displaying customPropertyEditor
     *
     * @param value
     */
    public void updateState(PropertyValue value) {
        if (isCurrentValueANull() || value == null) {
            customEditor.setText(null);
        } else {
            customEditor.setText(String.valueOf(value.getPrimitiveValue()));
        }
        radioButton.setSelected(!isCurrentValueAUserCodeType());
    }
    
    @Override
    public void customEditorOKButtonPressed() {
        super.customEditorOKButtonPressed();
        if (radioButton.isSelected()) {
            saveValue(customEditor.getText());
        }
    }
    
    private class CustomEditor extends JPanel implements DocumentListener, FocusListener {
        private JTextField textField;
        
        public CustomEditor() {
            radioButton.addFocusListener(this);
            initComponents();
        }
        
        private void initComponents() {
            setLayout(new BorderLayout());
            textField = new JTextField();
            textField.getDocument().addDocumentListener(this);
            textField.addFocusListener(this);
            add(textField, BorderLayout.CENTER);
        }
        
        public void setText(String text) {
            textField.setText(text);
        }
        
        public String getText() {
            return textField.getText();
        }
        
        private void checkNumberStatus() {
            if (!isTextCorrect(textField.getText())) {
                displayWarning(NON_DIGITS_TEXT);
            } else {
                clearErrorStatus();
            }
        }
        
        public void insertUpdate(DocumentEvent evt) {
            radioButton.setSelected(true);
            checkNumberStatus();
        }
        
        public void removeUpdate(DocumentEvent evt) {
            radioButton.setSelected(true);
            checkNumberStatus();
        }
        
        public void changedUpdate(DocumentEvent evt) {
        }
        
        public void focusGained(FocusEvent e) {
            if (e.getSource() == radioButton || e.getSource() == textField) {
                checkNumberStatus();
            }
        }
        
        public void focusLost(FocusEvent e) {
            clearErrorStatus();
        }
    }
}
