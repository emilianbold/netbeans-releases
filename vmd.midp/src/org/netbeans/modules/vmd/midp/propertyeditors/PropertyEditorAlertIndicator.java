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

import org.netbeans.modules.vmd.api.model.ComponentProducer;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.model.common.DocumentSupport;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.displayables.AlertCD;
import org.netbeans.modules.vmd.midp.components.items.GaugeCD;
import org.netbeans.modules.vmd.midp.propertyeditors.usercode.PropertyEditorElement;
import org.netbeans.modules.vmd.midp.propertyeditors.usercode.PropertyEditorUserCode;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collections;
import org.netbeans.modules.vmd.api.model.presenters.InfoPresenter;
import org.netbeans.modules.vmd.midp.codegen.InstanceNameResolver;
import org.netbeans.modules.vmd.midp.components.general.ClassCD;
import org.netbeans.modules.vmd.midp.components.general.ClassCode;
import org.netbeans.modules.vmd.midp.components.items.ItemCD;
import org.openide.explorer.propertysheet.InplaceEditor;

/**
 *
 * @author Karol Harezlak
 * @author Anton Chechel
 */
public final class PropertyEditorAlertIndicator extends PropertyEditorUserCode implements PropertyEditorElement {

    private String[] tags = {"true", "false"}; // NOI18N
    private CustomEditor customEditor;
    private JRadioButton radioButton;
    private BooleanInplaceEditor inplaceEditor;
    private Boolean valueState;
    private boolean executeInsideWriteTransactionUsed = true;

    private PropertyEditorAlertIndicator() {
        super(NbBundle.getMessage(PropertyEditorAlertIndicator.class, "LBL_VALUE_ALERT_INDICATOR_UCLABEL")); // NOI18N
        initComponents();

        initElements(Collections.<PropertyEditorElement>singleton(this));
    }

    public static PropertyEditorAlertIndicator createInstance() {
        return new PropertyEditorAlertIndicator();
    }

    private void initComponents() {
        radioButton = new JRadioButton();
        Mnemonics.setLocalizedText(radioButton, NbBundle.getMessage(PropertyEditorAlertIndicator.class, "LBL_VALUE_ALERT_INDICATOR_STR")); // NOI18N
        customEditor = new CustomEditor();
    }

    public JComponent getCustomEditorComponent() {
        return customEditor;
    }

    public JRadioButton getRadioButton() {
        return radioButton;
    }

    public boolean isInitiallySelected() {
        return true;
    }

    public boolean isVerticallyResizable() {
        return false;
    }

    @Override
    public boolean isPaintable() {
        PropertyValue propertyValue = (PropertyValue) getValue();
        return propertyValue.getKind() != PropertyValue.Kind.USERCODE;
    }

    @Override
    public InplaceEditor getInplaceEditor() {
        if (inplaceEditor == null) {
            inplaceEditor = new BooleanInplaceEditor(this);
            PropertyValue propertyValue = (PropertyValue) getValue();
            DesignComponent value = propertyValue.getComponent();
            JCheckBox checkBox = (JCheckBox) inplaceEditor.getComponent();
            if (value != null) {
                checkBox.setSelected(true);
            }
            checkBox.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    executeInsideWriteTransactionUsed = true;
                    JCheckBox checkBox = (JCheckBox) inplaceEditor.getComponent();
                    valueState = checkBox.isSelected();
                    setValue(NULL_VALUE);
                    invokeSaveToModel();
                }
            });
        }
        return inplaceEditor;
    }

    @Override
    public void paintValue(Graphics gfx, Rectangle box) {
        JComponent _component = inplaceEditor.getComponent();
        _component.setSize(box.width, box.height);
        _component.doLayout();
        _component.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        Graphics g = gfx.create(box.x, box.y, box.width, box.height);
        _component.setOpaque(false);
        _component.paint(g);
        g.dispose();
    }

    @Override
    public Boolean canEditAsText() {
        if (isCurrentValueAUserCodeType()) {
            return super.canEditAsText();
        }
        return true;
    }

    @Override
    public String getAsText() {
        if (isCurrentValueAUserCodeType()) {
            return USER_CODE_TEXT;
        } else if (isCurrentValueANull()) {
            return "false"; // NOI18N
        }
        return "true"; // NOI18N
    }

    public void setTextForPropertyValue(String text) {
    }

    public String getTextForPropertyValue() {
        return null;
    }

    public void updateState(PropertyValue value) {
        customEditor.setValue(value);
        radioButton.setSelected(!isCurrentValueAUserCodeType());
    }

    @Override
    public boolean isExecuteInsideWriteTransactionUsed() {
        return executeInsideWriteTransactionUsed;
    }

    @Override
    public Object getDefaultValue() {
        executeInsideWriteTransactionUsed = false;
        return super.getDefaultValue();
    }

    @Override
    public boolean executeInsideWriteTransaction() {
        if (component == null || component.get() == null) {
            return false;
        }

        DesignComponent alertComponent = component.get();
        if (isCurrentValueAUserCodeType()) {
            removeGauge(alertComponent);
            return true;
        }

        if (valueState) {
            if (alertComponent.readProperty(AlertCD.PROP_INDICATOR).getComponent() != null) {
                return false;
            }
            ComponentProducer producer = DocumentSupport.getComponentProducer(alertComponent.getDocument(), GaugeCD.TYPEID.toString());
            if (producer == null) {
                throw new IllegalStateException("No producer for TypeID : " + GaugeCD.TYPEID); //NOI18N
            }
            DesignComponent gauge = producer.createComponent(alertComponent.getDocument()).getMainComponent();
            gauge.writeProperty(GaugeCD.PROP_INTERACTIVE, MidpTypes.createBooleanValue(false));
            gauge.writeProperty(GaugeCD.PROP_USED_BY_ALERT, MidpTypes.createBooleanValue(true));
            gauge.writeProperty(ItemCD.PROP_LABEL, PropertyValue.createNull());
            gauge.writeProperty(ClassCD.PROP_INSTANCE_NAME, InstanceNameResolver.createFromSuggested (gauge, "indicator"));
            PropertyValue newGauge = PropertyValue.createComponentReference(gauge);
            PropertyEditorAlertIndicator.super.setValue(newGauge);
            alertComponent.addComponent(gauge);
            alertComponent.writeProperty(AlertCD.PROP_INDICATOR, newGauge);
        } else {
            removeGauge(alertComponent);
        }
        return false;
    }

    private boolean removeGauge(DesignComponent ac) {
        DesignComponent gauge = ac.readProperty(AlertCD.PROP_INDICATOR).getComponent();
        if (gauge == null) {
            ac.writeProperty(AlertCD.PROP_INDICATOR, NULL_VALUE);
            PropertyEditorAlertIndicator.super.setValue(NULL_VALUE);
            return false;
        }
        ac.writeProperty(AlertCD.PROP_INDICATOR, PropertyValue.createNull());
        ac.getDocument().deleteComponent(gauge);
        PropertyEditorAlertIndicator.super.setValue(NULL_VALUE);
        return false;
    }

    @Override
    public void customEditorOKButtonPressed() {
        super.customEditorOKButtonPressed();
        executeInsideWriteTransactionUsed = true;
        if (radioButton.isSelected()) {
            if ("true".equals(customEditor.getText())) { // NOI18N
                valueState = true;
            } else {
                valueState = false;
            }
            JCheckBox checkBox = (JCheckBox) inplaceEditor.getComponent();
            checkBox.setSelected(valueState);
            //this is necessary to reset property value from USERCODE state
            setValue(NULL_VALUE);
            invokeSaveToModel();
        }
    }

    @Override
    public boolean canWrite() {
        return MidpPropertyEditorSupport.singleSelectionEditAsTextOnly();
    }

    private class CustomEditor extends JPanel implements ActionListener {

        private JComboBox combobox;

        public CustomEditor() {
            initComponents();
        }

        private void initComponents() {
            setLayout(new BorderLayout());
            combobox = new JComboBox(tags);
            combobox.addActionListener(this);
            add(combobox, BorderLayout.CENTER);
        }

        public void setValue(PropertyValue value) {
            combobox.setSelectedItem(Boolean.toString(value != null && value.getComponent() != null));
        }

        public String getText() {
            return (String) combobox.getSelectedItem();
        }

        public void actionPerformed(ActionEvent evt) {
            radioButton.setSelected(true);
        }
    }
}
