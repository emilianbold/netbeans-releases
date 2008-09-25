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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.vmd.midp.propertyeditors;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.util.Collections;
import java.util.regex.Pattern;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.propertyeditors.api.usercode.PropertyEditorUserCode;
import org.netbeans.modules.vmd.midp.propertyeditors.api.usercode.PropertyEditorElement;
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
    private String label;
    private boolean positiveNumersOnly;

    private PropertyEditorNumber(boolean useSpinner, String label, String userCodeLabel) {
        super(userCodeLabel);
        this.label = label;
        initComponents(useSpinner);

        initElements(Collections.<PropertyEditorElement>singleton(this));
    }

    private PropertyEditorNumber(boolean useSpinner, String label, String userCodeLabel, boolean positiveNumbersOnly) {
        super(userCodeLabel);
        this.label = label;
        initComponents(useSpinner);
        this.positiveNumersOnly = positiveNumbersOnly;
        initElements(Collections.<PropertyEditorElement>singleton(this));
    }

    @Override
    public void cleanUp(DesignComponent component) {
        super.cleanUp(component);
        if (customEditor != null) {
            customEditor.cleanUp();
            customEditor = null;
        }
        radioButton = null;
    }

    /**
     * Creates instance of property editor for integer type
     *
     * @param label localized label with mnemonics for radio button
     * @return propertyEditor
     */
    public static final PropertyEditorNumber createIntegerInstance(boolean useSpinner, String label) {
        return new PropertyEditorNumber(useSpinner, label, NbBundle.getMessage(PropertyEditorNumber.class, "LBL_INTEGER_UCLABEL")); // NOI18N
    }

    /**
     * Creates instance of property editor for positive integer type
     *
     * @param label localized label with mnemonics for radio button
     * @return propertyEditor
     */
    public static final PropertyEditorNumber createPositiveIntegerInstance(boolean useSpinner, String label) {
        return new PropertyEditorNumber(useSpinner, label, NbBundle.getMessage(PropertyEditorNumber.class, "LBL_INTEGER_UCLABEL"), true); // NOI18N
    }

    /**
     * Creates instance of property editor for long type
     *
     * @param label localized label with mnemonics for radio button
     * @return propertyEditor
     */
    public static final PropertyEditorNumber createLongInstance(boolean useSpinner, String label) {
        return new PropertyEditorNumber(useSpinner, label, NbBundle.getMessage(PropertyEditorNumber.class, "LBL_LONG_UCLABEL")) { // NOI18N

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
     * @param label localized label with mnemonics for radio button
     * @return propertyEditor
     */
    public static final PropertyEditorNumber createByteInstance(boolean useSpinner, String label) {
        return new PropertyEditorNumber(useSpinner, label, NbBundle.getMessage(PropertyEditorNumber.class, "LBL_BYTE_UCLABEL")) { // NOI18N

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
     * @param label localized label with mnemonics for radio button
     * @return propertyEditor
     */
    public static final PropertyEditorNumber createShortInstance(boolean useSpinner, String label) {
        return new PropertyEditorNumber(useSpinner, label, NbBundle.getMessage(PropertyEditorNumber.class, "LBL_SHORT_UCLABEL")) { // NOI18N

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
     * @param label localized label with mnemonics for radio button
     * @return propertyEditor
     */
    public static final PropertyEditorNumber createFloatInstance(String label) {
        return new PropertyEditorNumber(false, label, NbBundle.getMessage(PropertyEditorNumber.class, "LBL_FLOAT_UCLABEL")) { // NOI18N

            @Override
            protected boolean isTextCorrect(String text) {
                return Pattern.matches("[\\d\\-\\.]+", text); // NOI18N
            }

            @Override
            protected String prepareText(String text) {
                return text.replaceAll("[^0-9\\-\\.]+", ""); // NOI18N
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
     * @param label localized label with mnemonics for radio button
     * @return propertyEditor
     */
    public static final PropertyEditorNumber createDoubleInstance(String label) {
        return new PropertyEditorNumber(false, label, NbBundle.getMessage(PropertyEditorNumber.class, "LBL_DOUBLE_UCLABEL")) { // NOI18N

            @Override
            protected boolean isTextCorrect(String text) {
                return Pattern.matches("[\\d\\-\\.]+", text); // NOI18N
            }

            @Override
            protected String prepareText(String text) {
                return text.replaceAll("[^0-9\\-\\.]+", ""); // NOI18N
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
     * @param label localized label with mnemonics for radio button
     * @return propertyEditor
     */
    public static final PropertyEditorNumber createCharInstance(boolean useSpinner, String label) {
        return new PropertyEditorNumber(useSpinner, label, NbBundle.getMessage(PropertyEditorNumber.class, "LBL_CHAR_UCLABEL")) { // NOI18N

            @Override
            protected boolean isTextCorrect(String text) {
                return Pattern.matches("[\\d\\-]+", text); // NOI18N
            }

            @Override
            protected String prepareText(String text) {
                return text.replaceAll("[^0-9\\-]+", ""); // NOI18N
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

    private void initComponents(boolean useSpinner) {
        radioButton = new JRadioButton();
        Mnemonics.setLocalizedText(radioButton, label);
        
        radioButton.getAccessibleContext().setAccessibleName( 
                radioButton.getText());
        radioButton.getAccessibleContext().setAccessibleDescription( 
                radioButton.getText());
        
        customEditor = new CustomEditor(useSpinner);
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
                isHexFormat(text);
    }

    /**
     * Removes all incorrect characters for for given property editor.
     * For example for integer property editor removes all chars except [\d\-]+ regex
     *
     * @param text to be checked
     * @return result text
     */
    protected String prepareText(String text) {
        if (text == null) {
            return text;
        }

        // hex
        if (isHexFormat(text)) {
            text = text.replaceAll("[^0-9\\-0xabcdefABCDEF]+", ""); // NOI18N
            return text.replace("0x", ""); // NOI18N
        }
        return text.replaceAll("[^0-9\\-]+", ""); // NOI18N
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
                if (isHexFormat(text)) {
                    text = prepareText(text);
                    intValue = Integer.parseInt(text, 16);
                } else {
                    text = prepareText(text);
                    intValue = Integer.parseInt(text);
                }
            } catch (NumberFormatException e) {
            }
            if (positiveNumersOnly && intValue < 0) {
                intValue = 0;
            }
            super.setValue(MidpTypes.createIntegerValue(intValue));
        }
    }

    private boolean isHexFormat(String text) {
        return text != null && text.matches("-?0x[\\d\\abcdefABCDEF]+"); //NOI18N
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
    public void setTextForPropertyValue(String text) {
        saveValue(text);
    }

    /**
     * Returns text for additional operations with propertyValue, not used here
     *
     * @return null
     */
    public String getTextForPropertyValue() {
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

    private class CustomEditor extends JPanel implements DocumentListener, ChangeListener, FocusListener {

        private JTextField textField;
        private JSpinner spinner;
        private boolean useSpinner;

        public CustomEditor(boolean useSpinner) {
            this.useSpinner = useSpinner;
            radioButton.addFocusListener(this);
            initComponents();
            
            /* 
             * Fix for #140843. I don't know why but
             * setting A11Y properties to <code>this</code> component
             * doesn't propagated to NbDialog.
             * It seems this is due presence on more JPanel between
             * Dialog content pane and this component.
             * Mentioned JPanel doesn't use A11Y from here.   
             */
            addHierarchyListener( new HierarchyListener(){

                public void hierarchyChanged( HierarchyEvent evt ) {
                    JDialog dialog = getDialog();
                    if( dialog == null ){
                        return;
                    }
                    else {
                        dialog.getAccessibleContext().setAccessibleName(
                                radioButton.getAccessibleContext().
                                getAccessibleName());
                        dialog.getAccessibleContext().setAccessibleDescription(
                                radioButton.getAccessibleContext().
                                getAccessibleDescription());
                    }
                }
                
            });
        }
        
        void cleanUp() {
            if (textField != null && textField.getDocument() != null) {
                textField.getDocument().removeDocumentListener(this);
            }
            textField = null;
            spinner = null;
            this.removeAll();
        }

        private JDialog getDialog(){
            Component comp = this;
            while ( true ) {
                comp = comp.getParent();
                if ( comp == null ){
                    break;
                }
                if ( comp instanceof JDialog ){
                    return (JDialog)comp;
                }
            }
            return null;
        }

        private void initComponents() {
            setLayout(new BorderLayout());
            if (useSpinner) {
                spinner = new JSpinner();
                spinner.getModel().addChangeListener(this);
                spinner.addFocusListener(this);
                add(spinner, BorderLayout.CENTER);
                
                spinner.getAccessibleContext().setAccessibleName( 
                        radioButton.getAccessibleContext().getAccessibleName());
                spinner.getAccessibleContext().setAccessibleDescription( 
                        radioButton.getAccessibleContext().getAccessibleDescription());
            } else {
                textField = new JTextField();
                textField.getDocument().addDocumentListener(this);
                textField.addFocusListener(this);
                add(textField, BorderLayout.CENTER);
                
                textField.getAccessibleContext().setAccessibleName( 
                        radioButton.getAccessibleContext().getAccessibleName());
                textField.getAccessibleContext().setAccessibleDescription( 
                        radioButton.getAccessibleContext().getAccessibleDescription());
            }
        }

        public void setText(String text) {
            if (useSpinner) {
                Integer intValue = 0;
                try {
                    if (isHexFormat(text)) {
                        text = prepareText(text);
                        intValue = Integer.parseInt(text, 16);
                    } else {
                        text = prepareText(text);
                        intValue = Integer.parseInt(text);
                    }
                } catch (NumberFormatException e) {
                }

                spinner.setValue(intValue);
            } else {
                textField.setText(text);
            }
        }

        public String getText() {
            return useSpinner ? spinner.getValue().toString() : textField.getText();
        }

        private void checkNumberStatus() {
            if (!isTextCorrect(getText())) {
                displayWarning(NON_DIGITS_TEXT);
                return;
            }
            if (positiveNumersOnly) {
                try {
                    int number = Integer.valueOf(textField.getText());
                    if (number < 0) {
                        displayWarning(NbBundle.getMessage(PropertyEditorPreferredSize.class, "MSG_POSITIVE_CHARS")); //NOI18N
                        return;
                    }
                } catch (NumberFormatException ex) {
                    displayWarning(PropertyEditorNumber.NON_DIGITS_TEXT);
                    return;
                }
            }
            clearErrorStatus();
        }

        public void focusGained(FocusEvent e) {
            if (e.getSource() == textField || e.getSource() == spinner) {
                radioButton.setSelected(true);
                checkNumberStatus();
            }
        //if (e.getSource() == radioButton ) {
        //    checkNumberStatus();
        //}
        }

        public void focusLost(FocusEvent e) {
            clearErrorStatus();
        }

        public void stateChanged(ChangeEvent e) {
            if (spinner.hasFocus()) {
                radioButton.setSelected(true);
                checkNumberStatus();
            }
        }

        public void insertUpdate(DocumentEvent e) {
            if (textField.hasFocus()) {
                radioButton.setSelected(true);
                checkNumberStatus();
            }
        }

        public void removeUpdate(DocumentEvent e) {
            if (textField.hasFocus()) {
                radioButton.setSelected(true);
                checkNumberStatus();
            }
        }

        public void changedUpdate(DocumentEvent e) {
        }
    }
}
