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

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Collections;
import java.util.LinkedHashMap;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.model.TypeID;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.databinding.MidpDatabindingSupport;
import org.netbeans.modules.vmd.midp.components.displayables.TextBoxCD;
import org.netbeans.modules.vmd.midp.components.items.TextFieldCD;
import org.netbeans.modules.vmd.midp.propertyeditors.api.usercode.PropertyEditorUserCode;
import org.netbeans.modules.vmd.midp.propertyeditors.api.usercode.PropertyEditorElement;
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
    private boolean useTextArea;
    private TypeID parentTypeID;
    private String label;
    private DatabindingElement databindingElement;

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
    protected PropertyEditorString(String comment,
            int dependence,
            boolean useTextArea,
            String label,
            TypeID parentTypeID,
            boolean databinding) {

        super(NbBundle.getMessage(PropertyEditorString.class, "LBL_STRING_STR")); // NOI18N
        this.comment = comment;
        this.dependence = dependence;
        this.useTextArea = useTextArea;
        this.label = label;
        this.parentTypeID = parentTypeID;
        initComponents();

        if (databinding) {
            LinkedHashMap<PropertyEditorElement, Integer> elements = new LinkedHashMap<PropertyEditorElement, Integer>(2);
            databindingElement = new DatabindingElement(this);
            elements.put(this, null);
            elements.put(databindingElement, new Integer(-1));
            initElements(elements);
        } else {
            initElements(Collections.<PropertyEditorElement>singleton(this));
        }

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
    private PropertyEditorString(String comment,
            int dependence,
            String defaultValue,
            String label,
            boolean databinding) {

        this(comment, dependence, true, label, null, databinding);
        this.defaultValue = defaultValue;
    }

    public static final PropertyEditorString createInstanceWithDatabinding(String comment,
            int dependence,
            String defaultValue,
            String label) {

        return new PropertyEditorString(comment, dependence, defaultValue, label, true);
    }

    public static final PropertyEditorString createInstanceWithDatabinding(String comment,
            int dependence,
            boolean useTextArea,
            String label,
            TypeID parentTypeID) {

        return new PropertyEditorString(comment, dependence, useTextArea, label, parentTypeID, true);
    }

    /**
     * Creates instance of PropertyEditorString without dependences.
     */
    public static final PropertyEditorString createInstance(String label) {
        return new PropertyEditorString(null, DEPENDENCE_NONE, true, label, null, false);
    }

    public static final PropertyEditorString createInstanceWithDatabinding(String label) {
        return new PropertyEditorString(null, DEPENDENCE_NONE, true, label, null, true);
    }

    /**
     * Creates instance of PropertyEditorString with particular dependences.
     * @param int dependence
     * @see PropertyEditorString(String comment, int dependence)
     */
    public static final PropertyEditorString createInstance(int dependence, String label) {
        return new PropertyEditorString(null, dependence, true, label, null, false);
    }

    public static final PropertyEditorString createInstanceWithDatabinding(int dependence,
            String label) {

        return new PropertyEditorString(null, dependence, true, label, null, true);
    }

    /**
     * Creates instance of PropertyEditorString with particular dependences and NOT editable for given parent TypeID.
     * @param int dependence
     * @param parentTypeID parentComponent TypeID
     * @see PropertyEditorString(String comment, int dependence)
     */
    public static final PropertyEditorString createInstance(String label, TypeID parentTypeID) {
        return new PropertyEditorString(null, DEPENDENCE_NONE, true, label, parentTypeID, false);
    }

    /**
     * Creates instance of PropertyEditorString using JTExtField.
     */
    public static final PropertyEditorString createTextFieldInstance(String label) {
        return new PropertyEditorString(null, DEPENDENCE_NONE, false, label, null, false);
    }

    /**
     * Creates instance of PropertyEditorString without dependences with default value.
     */
    public static final PropertyEditorString createInstanceWithDefaultValue(String defaultValue, String label) {
        return new PropertyEditorString(null, DEPENDENCE_NONE, defaultValue, label, false);
    }

    /**
     * Creates instance of PropertyEditorString without dependences with default value.
     */
    public static final PropertyEditorString createInstanceWithComment(String comment, String label) {
        return new PropertyEditorString(comment, DEPENDENCE_NONE, null, label, false);
    }

    @Override
    public void cleanUp(DesignComponent component) {
        if (customEditor != null) {
            customEditor.cleanUp();
            customEditor = null;
        }
        radioButton = null;
        parentTypeID = null;
        if (databindingElement != null) {
            databindingElement.clean(component);
            databindingElement = null;
        }
    }

    private void initComponents() {
        radioButton = new JRadioButton();

        Mnemonics.setLocalizedText(radioButton, label);

        radioButton.getAccessibleContext().setAccessibleName(radioButton.getText());
        radioButton.getAccessibleContext().setAccessibleDescription(radioButton.getText());

        customEditor = new CustomEditor(comment);
    }

    @Override
    public Object getDefaultValue() {
        if (defaultValue == null) {
            return super.getDefaultValue();
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
        return databindingElement == null;
    }

    /*
     * This element should be vertically resizable
     */
    public boolean isVerticallyResizable() {
        return useTextArea;
    }

    /*
     * Returns text from PropertyValue to be displayed in the inplace editor
     */
    @Override
    public String getAsText() {
        if (component == null || getPropertyNames() == null) {
            return null;
        }
        String databinding = MidpDatabindingSupport.getDatabaindingAsText(component.get(), getPropertyNames().get(0));
        if (databinding != null) {
            return databinding;
        }

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

    @Override
    public boolean canWrite() {
        if (!isWriteableByParentType()) {
            return false;
        }

        return super.canWrite();
    }

    @Override
    public Boolean canEditAsText() {
        if (MidpDatabindingSupport.getDatabaindingAsText(component.get(), getPropertyNames().get(0)) != null) {
            return false;
        }
        return super.canEditAsText();
    }

    @Override
    public boolean supportsCustomEditor() {
        if (!isWriteableByParentType()) {
            return false;
        }

        return super.supportsCustomEditor();
    }

    /*
     * This method updates state of custom property editor.
     */
    public void updateState(PropertyValue value) {

        final DesignComponent c = component.get();
        if (databindingElement != null) {
            databindingElement.updateDesignComponent(c);
        }
        if (isCurrentValueANull() || value == null) {
            customEditor.setText(null);
        } else if (MidpDatabindingSupport.getDatabaindingAsText(component.get(), getPropertyNames().get(0)) != null) {
            ((DatabindingElementUI) databindingElement.getCustomEditorComponent()).updateComponent(c);
        } else {
            customEditor.setText((String) value.getPrimitiveValue());
        }
        if (!isCurrentValueAUserCodeType()) {
            radioButton.setSelected(true);
            radioButton.requestFocus();
        }
    }

    @Override
    public boolean executeInsideWriteTransaction() {
        if (component == null || component.get() == null) {
            return true;
        }
        final DesignComponent component_ = component.get();
        DesignComponent connector = MidpDatabindingSupport.getConnector(component_, getPropertyNames().get(0));
        if (databindingElement != null && connector != null) {
            return false;
        }
        return super.isExecuteInsideWriteTransactionUsed();
    }

    @Override
    public boolean isExecuteInsideWriteTransactionUsed() {
        if (component == null || component.get() == null) {
            return false;
        }
        final DesignComponent component_ = component.get();
        DesignComponent connector = MidpDatabindingSupport.getConnector(component_, getPropertyNames().get(0));
        if (databindingElement != null && connector != null) {
            return true;
        }
        return super.isExecuteInsideWriteTransactionUsed();
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
        final DesignComponent _component = component.get();
        if (databindingElement != null && databindingElement.getRadioButton().isSelected()) {
            ((DatabindingElementUI) databindingElement.getCustomEditorComponent()).saveToModel(_component);
        } else if (databindingElement != null) {
            ((DatabindingElementUI) databindingElement.getCustomEditorComponent()).resetValuesInModel(_component);
        }
    }

    private boolean isWriteableByParentType() {
        if (component == null || component.get() == null) {
            return false;
        }

        if (parentTypeID != null) {
            final DesignComponent _component = component.get();
            final DesignComponent[] parent = new DesignComponent[1];
            _component.getDocument().getTransactionManager().readAccess(new Runnable() {

                public void run() {
                    parent[0] = _component.getParentComponent();
                }
            });

            if (parent[0] != null && parentTypeID.equals(parent[0].getType())) {
                return false;
            }
        }
        return true;
    }


    /*
     * Custom property editor. JEditorPane plus possible JLabels with comments.
     */
    private class CustomEditor implements DocumentListener {

        private JPanel panel;
        private JTextComponent editorPane;
        private String comment;

        public CustomEditor(String comment) {
            this.comment = comment;
            initComponents();
        }

        void cleanUp() {
            if (editorPane != null && editorPane.getDocument() != null) {
                editorPane.getDocument().removeDocumentListener(this);
            }
            editorPane = null;
            panel.removeAll();
            panel = null;
        }

        private void initComponents() {
            panel = new JPanel(new GridBagLayout());

            JComponent textComponent;
            if (useTextArea) {
                editorPane = new JTextArea();
                textComponent = new JScrollPane();
                ((JScrollPane) textComponent).setViewportView(editorPane);
                ((JScrollPane) textComponent).setPreferredSize(new Dimension(400, 100));
            } else {
                textComponent = editorPane = new JTextField();
            }

            JLabel tempLabel = new JLabel();
            Mnemonics.setLocalizedText(tempLabel, label);
            editorPane.getAccessibleContext().setAccessibleName(tempLabel.getText());
            editorPane.getAccessibleContext().setAccessibleDescription(tempLabel.getText());
            tempLabel = null;

            editorPane.getDocument().addDocumentListener(this);

            GridBagConstraints gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 0;
            gridBagConstraints.fill = GridBagConstraints.BOTH;
            gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.weighty = 1.0;
            panel.add(textComponent, gridBagConstraints);

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

        public void insertUpdate(DocumentEvent e) {
            if (editorPane.hasFocus()) {
                radioButton.setSelected(true);
            }
        }

        public void removeUpdate(DocumentEvent e) {
            if (editorPane.hasFocus()) {
                radioButton.setSelected(true);
            }
        }

        public void changedUpdate(DocumentEvent e) {
        }
    }
}