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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.vmd.midpnb.propertyeditors.table;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.JComponent;
import javax.swing.JRadioButton;
import org.netbeans.modules.vmd.api.model.Debug;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.midp.propertyeditors.resource.elements.PropertyEditorResourceElement.DesignComponentWrapper;
import org.netbeans.modules.vmd.midp.propertyeditors.resource.elements.PropertyEditorResourceElementEvent;
import org.netbeans.modules.vmd.midp.propertyeditors.resource.elements.PropertyEditorResourceElementListener;
import org.netbeans.modules.vmd.midp.propertyeditors.usercode.PropertyEditorElement;
import org.netbeans.modules.vmd.midp.propertyeditors.usercode.PropertyEditorUserCode;
import org.netbeans.modules.vmd.midpnb.components.resources.SimpleTableModelCD;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;

/**
 *
 * @author Anton Chechel
 */
public class PropertyEditorTableModel extends PropertyEditorUserCode implements PropertyEditorElement, PropertyEditorResourceElementListener {

    private JRadioButton radioButton;
    private TableModelEditorElement customEditor;
    private PropertyValue values;
    private PropertyValue headers;
    private WeakReference<DesignComponent> component;

    private PropertyEditorTableModel() {
        super();
        initComponents();
        
        Collection<PropertyEditorElement> elements = new ArrayList<PropertyEditorElement>(1);
        elements.add(this);
        initElements(elements);
    }
    
    public static PropertyEditorTableModel createInstance() {
        return new PropertyEditorTableModel();
    }
    
        private void initComponents() {
        radioButton = new JRadioButton();
        Mnemonics.setLocalizedText(radioButton, NbBundle.getMessage(PropertyEditorTableModel.class, "LBL_TABLE_MODEL_STR")); // NOI18N;
        
        customEditor = new TableModelEditorElement();
        customEditor.addPropertyEditorResourceElementListener(this);
    }

    @Override
    public void init(DesignComponent component) {
        super.init(component);
        this.component = new WeakReference<DesignComponent>(component);
    }
    
    public void updateState(PropertyValue value) {
        if (customEditor.isShowing()) {
            if (value == null) {
                customEditor.setDesignComponentWrapper(null);
            } else if (component != null && component.get() != null) {
                customEditor.setDesignComponentWrapper(new DesignComponentWrapper(component.get()));
            }
            customEditor.setAllEnabled(true);
        }
    }

    public void setTextForPropertyValue(String text) {
    }

    @Override
    public Boolean canEditAsText() {
        return Boolean.FALSE;
    }
    
    @Override
    public String getAsText() {
        return NbBundle.getMessage(PropertyEditorTableModel.class, "DISP_PE_TableModel_GetAsText"); //NOI18N
    }

    @Override
    public boolean executeInsideWriteTransaction() {
        if (component == null || component.get() == null) {
            return false;
        }
        
        DesignComponent _component = component.get();
        if (headers != null) {
            _component.writeProperty(SimpleTableModelCD.PROP_COLUMN_NAMES, headers);
        }
        if (values != null) {
            _component.writeProperty(SimpleTableModelCD.PROP_VALUES, values);
        }
        return false;
    }
    
    @Override
    public boolean isExecuteInsideWriteTransactionUsed() {
        return true;
    }
    
    @Override
    public boolean supportsDefaultValue() {
        return false;
    }
    
    public String getTextForPropertyValue() {
        return null;
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
        return true;
    }

    public void elementChanged(PropertyEditorResourceElementEvent event) {
        PropertyValue propertyValue = event.getPropertyValue();
        String propertyName = event.getPropertyName();
        if (SimpleTableModelCD.PROP_COLUMN_NAMES.equals(propertyName)) {
            headers = propertyValue;
        } else if (SimpleTableModelCD.PROP_VALUES.equals(propertyName)) {
            values = propertyValue;
        } else {
            Debug.illegalArgument("Illegal property value has been passed"); // NOI18N
        }
        radioButton.setSelected(true);
    }
}
