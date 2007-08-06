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

package org.netbeans.modules.vmd.midp.propertyeditors;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.displayables.TextBoxCD;
import org.netbeans.modules.vmd.midp.components.items.TextFieldCD;
import org.netbeans.modules.vmd.midp.propertyeditors.usercode.PropertyEditorUserCode;
import org.netbeans.modules.vmd.midp.propertyeditors.usercode.PropertyEditorElement;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;

/**
 * This class provides property editor for common text properties such as label,
 * text, etc. This is also an example how to use PropertyEditorUserCode API
 * with one PropertyEditorElement.
 *
 * @author Karol Harezlak
 * @author Anton Chechel
 */
public class PropertyEditorString extends PropertyEditorUserCode implements PropertyEditorElement {

    public static final int DEPENDENCE_NONE = 0;
    public static final int DEPENDENCE_TEXT_BOX = 1;
    public static final int DEPENDENCE_TEXT_FIELD = 2;

    private CustomEditor customEditor;
    private JRadioButton radioButton;
    private int dependence;
    private String comment;
    private String defaultValue;
    private boolean isDefaultValueUsed;

    /**
     * Creates instance of PropertyEditorString.
     *
     * @param String comment to be displayed underneath of text area in custom
     * property editor. Can be null.
     * @param int dependence of particular DesignComponent type. Possible values
     * are DEPENDENCE_NONE, DEPENDENCE_TEXT_BOX, DEPENDENCE_TEXT_FIELD. This value
     * will affect for that components after property value will be changed. For
     * example is given text length is more than TextBoxCD.PROP_MAX_SIZE then
     * this property will be automatically increased to be equal of text length.
     */
    public PropertyEditorString(String comment, int dependence) {
        this.comment = comment;
        this.dependence = dependence;
        isDefaultValueUsed = false;
        initComponents();

        Collection<PropertyEditorElement> elements = new ArrayList<PropertyEditorElement>(1);
        elements.add(this);
        initElements(elements);
    }

    /**
     * Creates instance of PropertyEditorString.
     *
     * @param String comment to be displayed underneath of text area in custom
     * property editor. Can be null.
     * @param int dependence of particular DesignComponent type. Possible values
     * are DEPENDENCE_NONE, DEPENDENCE_TEXT_BOX, DEPENDENCE_TEXT_FIELD. This value
     * will affect for that components after property value will be changed. For
     * example is given text length is more than TextBoxCD.PROP_MAX_SIZE then
     * this property will be automatically increased to be equal of text length.
     * @param String default value of the property editor, could be different from default
     * value specified in the component descriptor
     */
    public PropertyEditorString(String comment, int dependence, String defaultValue) {
        this(comment, dependence);
        this.defaultValue = defaultValue;
        isDefaultValueUsed = true;
    }

    /**
     * Creates instance of PropertyEditorString without dependences.
     */
    public static final PropertyEditorString createInstance() {
        return new PropertyEditorString(null, DEPENDENCE_NONE);
    }

    /**
     * Creates instance of PropertyEditorString without dependences with default value.
     */
    public static final PropertyEditorString createInstance(String defaultValue) {
        return new PropertyEditorString(null, DEPENDENCE_NONE, defaultValue);
    }

    /**
     * Creates instance of PropertyEditorString with particular dependences.
     * @param int dependence
     * @see PropertyEditorString(String comment, int dependence)
     */
    public static final PropertyEditorString createInstance(int dependence) {
        return new PropertyEditorString(null, dependence);
    }

    /**
     * Creates instance of PropertyEditorString which can not change PropertyValue.
     */
    public static final PropertyEditorString createInstanceReadOnly() {
        return new PropertyEditorString(null, DEPENDENCE_NONE) {

            @Override
            public boolean canWrite() {
                return false;
            }
        };
    }

    private void initComponents() {
        radioButton = new JRadioButton();
        Mnemonics.setLocalizedText(radioButton, NbBundle.getMessage(PropertyEditorString.class, "LBL_STRING_STR")); // NOI18N
        customEditor = new CustomEditor(comment);
    }

    @Override
    public Object getDefaultValue() {
        if (!isDefaultValueUsed) {
            return super.getDefaultValue();
        }
        if (defaultValue == null) {
            return NULL_VALUE;
        }
        return MidpTypes.createStringValue(defaultValue);
    }

    /*
     * Custom editor
     */
    public JComponent getCustomEditorComponent() {
        return customEditor.getComponent();
    }

    /*
     * Radio button
     */
    public JRadioButton getRadioButton() {
        return radioButton;
    }

    /*
     * This element should be selected by default
     */
    public boolean isInitiallySelected() {
        return true;
    }

    /*
     * This element should be vertically resizable
     */
    public boolean isVerticallyResizable() {
        return true;
    }

    /*
     * Returns text from PropertyValue to be displayed in the inplace editor
     */
    @Override
    public String getAsText() {
        String superText = super.getAsText();
        if (superText != null) {
            return superText;
        }

        PropertyValue value = (PropertyValue) super.getValue();
        return (String) value.getPrimitiveValue();
    }

    /*
     * Sets PropertyValue according to given text. This method invoked when user
     * sets new value in the inplace editor.
     */
    public void setTextForPropertyValue(String text) {
        saveValue(text);
    }

    /*
     * This method used when PropertyEditorUserCode has more than one element
     * incapsulated. In that case particular element returns text to be saved
     * to PropertyValue.
     */
    public String getTextForPropertyValue() {
        return null;
    }

    /*
     * This method updates state of custom property editor.
     */
    public void updateState(PropertyValue value) {
        if (isCurrentValueANull() || value == null) {
            customEditor.setText(null);
        } else {
            customEditor.setText((String) value.getPrimitiveValue());
        }
        radioButton.setSelected(!isCurrentValueAUserCodeType());
    }

    private void saveValue(String text) {
        super.setValue(MidpTypes.createStringValue(text));
        if (component == null || component.get() == null) {
            return;
        }

        final DesignComponent _component = component.get();
        final int length = text.length();
        switch (dependence) {
            case DEPENDENCE_TEXT_BOX:
                _component.getDocument().getTransactionManager().writeAccess(new Runnable() {

                    public void run() {
                        PropertyValue value = _component.readProperty(TextBoxCD.PROP_MAX_SIZE);
                        if (MidpTypes.getInteger(value) < length) {
                            _component.writeProperty(TextBoxCD.PROP_MAX_SIZE, MidpTypes.createIntegerValue(length));
                        }
                    }
                });
                break;
            case DEPENDENCE_TEXT_FIELD:
                _component.getDocument().getTransactionManager().writeAccess(new Runnable() {

                    public void run() {
                        PropertyValue value = _component.readProperty(TextFieldCD.PROP_MAX_SIZE);
                        if (MidpTypes.getInteger(value) < length) {
                            _component.writeProperty(TextFieldCD.PROP_MAX_SIZE, MidpTypes.createIntegerValue(length));
                        }
                    }
                });
        }
    }

    /*
     * Saves PropertyValue
     */
    @Override
    public void customEditorOKButtonPressed() {
        super.customEditorOKButtonPressed();
        if (radioButton.isSelected()) {
            saveValue(customEditor.getText());
        }
    }

/*
     * Custom property editor. JEditorPane plus possible JLabels with comments.
     */
    private class CustomEditor {

        private JPanel panel;
        private JEditorPane editorPane;
        private String comment;

        public CustomEditor(String comment) {
            this.comment = comment;
            initComponents();
        }

        private void initComponents() {
            panel = new JPanel(new GridBagLayout());
            editorPane = new JEditorPane();
            JScrollPane scrollPane = new JScrollPane();
            scrollPane.setViewportView(editorPane);
            scrollPane.setPreferredSize(new Dimension(400, 100));

            GridBagConstraints gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 0;
            gridBagConstraints.fill = GridBagConstraints.BOTH;
            gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.weighty = 1.0;
            panel.add(scrollPane, gridBagConstraints);

            if (comment != null) {
                JLabel label = new JLabel(comment);
                gridBagConstraints = new GridBagConstraints();
                gridBagConstraints.gridx = 0;
                gridBagConstraints.gridy = 1;
                gridBagConstraints.fill = GridBagConstraints.BOTH;
                gridBagConstraints.ipadx = 1;
                gridBagConstraints.ipady = 10;
                gridBagConstraints.anchor = GridBagConstraints.WEST;
                gridBagConstraints.weightx = 1.0;
                gridBagConstraints.weighty = 1.0;
                panel.add(label, gridBagConstraints);
            }
        }

        public JComponent getComponent() {
            return panel;
        }

        public void setText(String text) {
            editorPane.setText(text);
        }

        public String getText() {
            return editorPane.getText();
        }
    }
}
