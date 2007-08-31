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

import java.awt.BorderLayout;
import java.awt.Component;
import java.lang.ref.WeakReference;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import org.netbeans.modules.vmd.api.model.Debug;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.properties.DesignPropertyEditor;
import org.netbeans.modules.vmd.midp.propertyeditors.api.resource.element.PropertyEditorResourceElement.DesignComponentWrapper;
import org.netbeans.modules.vmd.midp.propertyeditors.api.resource.element.PropertyEditorResourceElementEvent;
import org.netbeans.modules.vmd.midp.propertyeditors.api.resource.element.PropertyEditorResourceElementListener;
import org.netbeans.modules.vmd.midpnb.components.resources.SimpleTableModelCD;
import org.openide.util.NbBundle;

/**
 *
 * @author Anton Chechel
 */
public class PropertyEditorTableModel extends DesignPropertyEditor implements PropertyEditorResourceElementListener {

    private WeakReference<DesignComponent> component;
    private JPanel customEditorPanel;
    private TableModelEditorElement customEditor;
    private PropertyValue values;
    private PropertyValue headers;

    private PropertyEditorTableModel() {
        initComponents();
    }
    
    public static PropertyEditorTableModel createInstance() {
        return new PropertyEditorTableModel();
    }
    
     private void initComponents() {
        customEditor = new TableModelEditorElement();
        customEditor.addPropertyEditorResourceElementListener(this);
        customEditorPanel = new JPanel(new BorderLayout());
        customEditorPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        customEditorPanel.add(customEditor, BorderLayout.CENTER);
    }

    @Override
    public void init(DesignComponent component) {
        if (component != null) {
            this.component = new WeakReference<DesignComponent>(component);
        }
    }
        
    @Override
    public Component getCustomEditor() {
        //if (customEditorPanel.isShowing()) {
            if (component != null && component.get() != null) {
                customEditor.setDesignComponentWrapper(new DesignComponentWrapper(component.get()));
            }
            customEditor.setAllEnabled(true);
        //}
        return customEditorPanel;
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
    
    public void elementChanged(PropertyEditorResourceElementEvent event) {
        PropertyValue propertyValue = event.getPropertyValue();
        String propertyName = event.getPropertyName();
        if (SimpleTableModelCD.PROP_COLUMN_NAMES.equals(propertyName)) {
            headers = propertyValue;
        } else if (SimpleTableModelCD.PROP_VALUES.equals(propertyName)) {
            values = propertyValue;
        } else {
            throw Debug.illegalArgument("Illegal property value has been passed"); // NOI18N
        }
    }
}
