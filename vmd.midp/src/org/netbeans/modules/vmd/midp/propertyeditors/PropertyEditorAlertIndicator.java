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

import org.netbeans.modules.vmd.api.model.ComponentProducer;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.model.common.DocumentSupport;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.displayables.AlertCD;
import org.netbeans.modules.vmd.midp.components.items.GaugeCD;
import org.netbeans.modules.vmd.midp.propertyeditors.api.usercode.PropertyEditorElement;
import org.netbeans.modules.vmd.midp.propertyeditors.api.usercode.PropertyEditorUserCode;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collections;
import org.netbeans.modules.vmd.midp.codegen.InstanceNameResolver;
import org.netbeans.modules.vmd.midp.components.general.ClassCD;
import org.netbeans.modules.vmd.midp.components.items.ItemCD;
import org.openide.explorer.propertysheet.InplaceEditor;

/**
 *
 * @author Karol Harezlak
 * @author Anton Chechel
 */
public final class PropertyEditorAlertIndicator extends PropertyEditorUserCode implements PropertyEditorElement {

    private CustomEditor customEditor;
    private JRadioButton radioButton;
    private BooleanInplaceEditor inplaceEditor;
    private Boolean valueState;
    private boolean executeInsideWriteTransactionUsed = true;
    private String rbLabel;

    private PropertyEditorAlertIndicator(String rbLabel) {
        super(NbBundle.getMessage(PropertyEditorAlertIndicator.class, "LBL_VALUE_ALERT_INDICATOR_UCLABEL")); // NOI18N
        this.rbLabel = rbLabel;
        initComponents();

        initElements(Collections.<PropertyEditorElement>singleton(this));
    }

    public static PropertyEditorAlertIndicator createInstance(String rbLabel) {
        return new PropertyEditorAlertIndicator(rbLabel);
    }

    private void initComponents() {
        radioButton = new JRadioButton();
        Mnemonics.setLocalizedText(radioButton, NbBundle.getMessage(PropertyEditorAlertIndicator.class, "LBL_VALUE_ALERT_INDICATOR_STR")); // NOI18N
        
        radioButton.getAccessibleContext().setAccessibleName(
                NbBundle.getMessage(PropertyEditorAlertIndicator.class,
                "ACSN_VALUE_ALERT_INDICATOR_STR")); // NOI18N
        radioButton.getAccessibleContext().setAccessibleDescription(
                NbBundle.getMessage(PropertyEditorAlertIndicator.class,
                "ACSD_VALUE_ALERT_INDICATOR_STR")); // NOI18N
        
        customEditor = new CustomEditor();
    }

    @Override
    public void cleanUp(DesignComponent component) {
        super.cleanUp(component);
        if (customEditor != null) {
            customEditor.removeAll();
            customEditor = null;
        }
        radioButton = null;
        valueState = null;
        inplaceEditor.cleanUp();
        inplaceEditor = null;
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
        } else {
            PropertyValue propertyValue = (PropertyValue) getValue();
            DesignComponent value = propertyValue.getComponent();
            JCheckBox checkBox = (JCheckBox) inplaceEditor.getComponent();
            if (value != null) {
                checkBox.setSelected(true);
            } else {
                checkBox.setSelected(false);
            }
        }
        return inplaceEditor;
    }

    @Override
    public void paintValue(Graphics gfx, Rectangle box) {
        if(inplaceEditor == null) {
            return;
        }
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
            gauge.writeProperty(ClassCD.PROP_INSTANCE_NAME, InstanceNameResolver.createFromSuggested (gauge, "indicator")); //NOI18N
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

        private JCheckBox checkBox;

        public CustomEditor() {
            initComponents();
        }

        void cleanUp() {
            checkBox.removeActionListener(this);
            checkBox = null;
            this.removeAll();
        }

        private void initComponents() {
            setLayout(new BorderLayout());
            checkBox = new JCheckBox();
            if (rbLabel != null) {
                Mnemonics.setLocalizedText(checkBox, rbLabel);
            }
            
            checkBox.getAccessibleContext().setAccessibleName( checkBox.getText());
            checkBox.getAccessibleContext().setAccessibleDescription(
                    checkBox.getText());
            
            checkBox.addActionListener(this);
            add(checkBox, BorderLayout.CENTER);
        }

        public void setValue(PropertyValue value) {
            checkBox.setSelected(value != null && value.getPrimitiveValue() != null && MidpTypes.getBoolean(value));
        }

        public String getText() {
            return checkBox.isSelected() ? "true" : "false"; // NOI18N
        }

        public void actionPerformed(ActionEvent evt) {
            radioButton.setSelected(true);
        }
    }
}
