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

package org.netbeans.modules.vmd.midp.propertyeditors.date;

import java.awt.BorderLayout;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.properties.DesignPropertyEditor;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.items.DateFieldCD;
import org.netbeans.modules.vmd.midp.propertyeditors.api.usercode.PropertyEditorElement;
import org.netbeans.modules.vmd.midp.propertyeditors.api.usercode.PropertyEditorUserCode;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;

/**
 *
 * @author Anton Chechel
 */
public final class PropertyEditorDate extends PropertyEditorUserCode implements PropertyEditorElement {

    private static final DateFormat FORMAT_DATE_TIME = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss"); // NOI18N
    private static final DateFormat FORMAT_DATE = new SimpleDateFormat("dd.MM.yyyy"); // NOI18N
    private static final DateFormat FORMAT_TIME = new SimpleDateFormat("HH:mm:ss"); // NOI18N
    
    private static final String NON_DATE_TIME_TEXT = NbBundle.getMessage(PropertyEditorDate.class, "MSG_NON_DATE_TIME"); // NOI18N
    private static final String NON_DATE_TEXT = NbBundle.getMessage(PropertyEditorDate.class, "MSG_NON_DATE"); // NOI18N
    private static final String NON_TIME_TEXT = NbBundle.getMessage(PropertyEditorDate.class, "MSG_NON_TIME"); // NOI18N
    
    private CustomEditor customEditor;
    private JRadioButton radioButton;

    private PropertyEditorDate() {
        super(NbBundle.getMessage(PropertyEditorDate.class, "LBL_DATE_UCLABEL")); // NOI18N
        initComponents();

        initElements(Collections.<PropertyEditorElement>singleton(this));
    }

    public static final DesignPropertyEditor createInstance() {
        return new PropertyEditorDate();
    }

    private void initComponents() {
        radioButton = new JRadioButton();
        Mnemonics.setLocalizedText(radioButton, NbBundle.getMessage(PropertyEditorDate.class, "LBL_DATE_STR")); // NOI18N
        customEditor = new CustomEditor();
    }

    public JComponent getCustomEditorComponent() {
        return customEditor;
    }

    public JRadioButton getRadioButton() {
        return radioButton;
    }

    public boolean isInitiallySelected() {
        return false;
    }

    public boolean isVerticallyResizable() {
        return false;
    }

    @Override
    public String getAsText() {
        String superText = super.getAsText();
        if (superText != null) {
            return superText;
        }
        return getValueAsText((PropertyValue) super.getValue());
    }

    public void setTextForPropertyValue(String text) {
        saveValue(text);
    }

    public String getTextForPropertyValue() {
        return null;
    }

    public void updateState(PropertyValue value) {
        if (isCurrentValueANull() || value == null) {
            customEditor.setText(null);
        } else {
            customEditor.setText(getValueAsText(value));
        }
        radioButton.setSelected(!isCurrentValueAUserCodeType());
    }

    private void saveValue(String text) {
        int inputMode = getInputMode();
        try {
            Date date = getFormatter(inputMode).parse(text);
            super.setValue(MidpTypes.createLongValue(date.getTime()));
        } catch (ParseException ex) {
        }
    }

    @Override
    public void customEditorOKButtonPressed() {
        super.customEditorOKButtonPressed();
        if (radioButton.isSelected()) {
            saveValue(customEditor.getText());
        }
    }

    private String getValueAsText(PropertyValue value) {
        Date date = new Date();
        Object valueValue = value.getPrimitiveValue();
        date.setTime((Long) valueValue);
        int inputMode = getInputMode();
        return getFormatter(inputMode).format(date);
    }

    private DateFormat getFormatter(int inputMode) {
        if (inputMode == DateFieldCD.VALUE_DATE) {
            return FORMAT_DATE;
        } else if (inputMode == DateFieldCD.VALUE_TIME) {
            return FORMAT_TIME;
        }
        return FORMAT_DATE_TIME;
    }

    private int getInputMode() {
        final int[] inputMode = new int[]{DateFieldCD.VALUE_DATE_TIME};
        if (component != null && component.get() != null) {
            final DesignComponent _component = component.get();
            _component.getDocument().getTransactionManager().readAccess(new Runnable() {

                public void run() {
                    PropertyValue pv = _component.readProperty(DateFieldCD.PROP_INPUT_MODE);
                    if (pv.getKind() == PropertyValue.Kind.VALUE) {
                        inputMode[0] = MidpTypes.getInteger(pv);
                    }
                }
            });
        }
        return inputMode[0];
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

        public void checkDateStatus() {
            int inputMode = getInputMode();
            try {
                getFormatter(inputMode).parse(textField.getText());
                clearErrorStatus();
            } catch (ParseException e) {
                displayWarning(getMessage(inputMode));
            }
        }

        private String getMessage(int inputMode) {
            if (inputMode == DateFieldCD.VALUE_DATE) {
                return NON_DATE_TEXT;
            } else if (inputMode == DateFieldCD.VALUE_TIME) {
                return NON_TIME_TEXT;
            }
            return NON_DATE_TIME_TEXT;
        }

        public void insertUpdate(DocumentEvent evt) {
            radioButton.setSelected(true);
            checkDateStatus();
        }

        public void removeUpdate(DocumentEvent evt) {
            radioButton.setSelected(true);
            checkDateStatus();
        }

        public void changedUpdate(DocumentEvent evt) {
        }

        public void focusGained(FocusEvent e) {
            if (e.getSource() == radioButton || e.getSource() == textField) {
                checkDateStatus();
            }
        }

        public void focusLost(FocusEvent e) {
            clearErrorStatus();
        }
    }
}
