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

package org.netbeans.modules.form;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyEditor;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.jdesktop.layout.GroupLayout;
import org.jdesktop.layout.LayoutStyle;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;

/**
 * A property editor that can wrap another property editor and make it use
 * a ResourcePanel allowing to define the property value as a resource
 * (represented by ResourceValue). E.g. StringEditor in o.n.m.f.editors2 package
 * wraps a plain property editor for strings. FormPropertyEditorManager and
 * FormPropertyEditor support this wrapping.
 * 
 * @author Tomas Pavek
 */
public class ResourceWrapperEditor implements ExPropertyEditor, FormAwareEditor,
        PropertyChangeListener, ChangeListener, VetoableChangeListener {

    protected PropertyEditor delegateEditor;
    protected FormProperty property;
    private boolean ignoreChange;
    private PropertyChangeSupport changeSupport;

    private Object propertyValue;

    // not very nice, but we suppose it's ok to have just one valid custom editor at a moment
    protected ResourcePanel resourcePanel;

    public ResourceWrapperEditor(PropertyEditor wrappedPropEd) {
        delegateEditor = wrappedPropEd;
        delegateEditor.addPropertyChangeListener(this);
    }

    public PropertyEditor getDelegatedPropertyEditor() {
        return delegateEditor;
    }

    void setDelegatedPropertyEditor(PropertyEditor delegate) {
        delegateEditor.removePropertyChangeListener(this);
        delegateEditor = delegate;
        delegateEditor.addPropertyChangeListener(this);
        propertyValue = delegateEditor.getValue();
    }

    // -----

    // FormAwareEditor implementation
    public void setContext(FormModel formModel, FormProperty prop) {
        property = prop;
        if (delegateEditor instanceof FormAwareEditor)
            ((FormAwareEditor)delegateEditor).setContext(formModel, prop);
    }

    // ExPropertyEditor implementation
    public void attachEnv(PropertyEnv env) {
        if (property != null) {
            env.removeVetoableChangeListener(this);
            env.setState(PropertyEnv.STATE_NEEDS_VALIDATION);
            env.addVetoableChangeListener(this);
        }
        if (delegateEditor instanceof ExPropertyEditor)
            ((ExPropertyEditor)delegateEditor).attachEnv(env);
    }

    public void setValue(Object value) {
        propertyValue = value;
        ignoreChange = true;
        setValueToDelegate(value);
        ignoreChange = false;
        firePropertyChange();
    }

    public Object getValue() {
        return propertyValue;
    }

    public Object getUnwrappedValue() {
        return delegateEditor.getValue();
    }

    public void setAsText(String text) {
        if (text.equals(delegateEditor.getAsText()))
            return;

        ignoreChange = true;
        delegateEditor.setAsText(text);
        ignoreChange = false;
        propertyValue = delegateEditor.getValue();
        firePropertyChange();
    }

    public String getAsText() {
        return delegateEditor.getAsText();
    }

    public boolean isPaintable() {
        return delegateEditor.isPaintable();
    }

    public void paintValue(Graphics g, Rectangle box) {
        delegateEditor.paintValue(g, box);
    }

    public String getJavaInitializationString() {
        if (propertyValue instanceof ResourceValue)
            return ((ResourceValue)propertyValue).getJavaInitializationCode();
        else
            return delegateEditor.getJavaInitializationString();
    }

    public String[] getTags() {
        return delegateEditor.getTags();
    }

    public Component getCustomEditor() {
        if (resourcePanel == null) {
            createResourcePanel();
        }

        Component resGUI;
        if (resourcePanel != null) {
            String key;
            boolean enable;
            if (propertyValue instanceof ResourceValue) {
                key = ((ResourceValue)propertyValue).getKey();
                enable = true;
            }
            else {
                key = ResourceSupport.getDefaultKey(property, ResourceSupport.AUTO_RESOURCING);
                enable = ResourceSupport.isPropertyForResourcing(property);
            }
            resourcePanel.init(key, enable);
            resGUI = resourcePanel.getComponent();
        }
        else {
            resGUI = null;
        }

        return createCustomEditorGUI(resGUI);
    }

    public boolean supportsCustomEditor() {
        return true;
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        synchronized (this) {
            if (changeSupport == null)
                changeSupport = new PropertyChangeSupport(this);
        }
        changeSupport.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        if (changeSupport != null)
            changeSupport.removePropertyChangeListener(l);
    }

    // -----

    protected void setValueToDelegate(Object value) {
        if (value instanceof ResourceValue)
            value = ((ResourceValue)value).getValue();
        delegateEditor.setValue(value);
    }

    protected void setValueToResourcePanel() {
        resourcePanel.setValue(delegateEditor.getValue(), null, null);
    }

    // called from the delegated editor
    public void propertyChange(PropertyChangeEvent evt) {
        if (!ignoreChange) { // change initiated through custom editor of the delegate
            if (propertyValue instanceof ResourceValue)
                setValueToResourcePanel();
            else
                propertyValue = delegateEditor.getValue();
            firePropertyChange();
        }
    }

    // called from ResourcePanel when the key or current value has changed
    public void stateChanged(ChangeEvent e) {
        ResourceValue resValue = resourcePanel.getResource();
        if (resValue != null) {
            if (resValue.getValue() == null) {
                // resource panel just enabled or set with a new key
                setValueToResourcePanel();
                resValue = resourcePanel.getResource();
            }
            setValue(resValue);
        }
        else { // resource panel disabled
            propertyValue = delegateEditor.getValue();
            firePropertyChange();
        }
    }

    // called when OK button is pressed in the custom editor dialog
    // TODO: this should only be done if this property editor is the selected one
    public void vetoableChange(PropertyChangeEvent ev) throws PropertyVetoException {
        if (PropertyEnv.PROP_STATE.equals(ev.getPropertyName())) {
            boolean excludeRes = false;
            if (resourcePanel != null) {
                ResourceValue resValue;
                if (propertyValue instanceof ResourceValue
                    && (resValue = resourcePanel.getResource()) != null)
                {   // make sure we have the latest resource value
                    if (resValue.getValue() == ResourceValue.IGNORED_VALUE) {
                        throw new PropertyVetoException("Invalid resource value", ev);
                        // TODO: message dialog about invalid value
                    }
                    if (resValue != propertyValue) {
                        setValue(resValue);
                    }
                } else {
                    excludeRes = true; // have resource panel, but just using plain value
                }
            }
            ResourceSupport.setExcludedProperty(property, excludeRes);
        }
    }

    private void firePropertyChange() {
        if (changeSupport != null)
            changeSupport.firePropertyChange("", null, null); // NOI18N
    }

    // -----

    private void createResourcePanel() {
        resourcePanel = ResourceSupport.createResourcePanel(
                property, propertyValue instanceof ResourceValue);
        if (resourcePanel != null)
            resourcePanel.addChangeListener(this);
    }

    protected Component createCustomEditorGUI(Component resourcePanelGUI) {
        if (resourcePanelGUI == null)
            return delegateEditor.getCustomEditor();

        JPanel panel = new JPanel();
        Component delComp = delegateEditor.getCustomEditor();
        GroupLayout layout = new GroupLayout(panel);
        panel.setLayout(layout);
        layout.setAutocreateGaps(true);
        layout.setHorizontalGroup(layout.createParallelGroup()
                .add(delComp)
                .add(layout.createSequentialGroup()
                    .addPreferredGap(LayoutStyle.RELATED)
                    .add(resourcePanelGUI)
                    .addPreferredGap(LayoutStyle.RELATED)));
        layout.setVerticalGroup(layout.createSequentialGroup()
                .add(delComp).addPreferredGap(LayoutStyle.UNRELATED).add(resourcePanelGUI));

        return panel;
    }
}
