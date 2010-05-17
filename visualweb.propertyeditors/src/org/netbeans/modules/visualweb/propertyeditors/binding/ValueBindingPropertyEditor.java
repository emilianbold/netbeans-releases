/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.visualweb.propertyeditors.binding;

import org.netbeans.modules.visualweb.propertyeditors.binding.data.DataBindingHelper;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditor;
import java.util.Vector;

import javax.faces.application.Application;
import javax.faces.el.ValueBinding;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.explorer.propertysheet.editors.EnhancedCustomPropertyEditor;

import com.sun.rave.designtime.DesignProperty;
import com.sun.rave.designtime.faces.FacesBindingPropertyEditor;
import com.sun.rave.designtime.faces.FacesDesignContext;
import com.sun.rave.designtime.faces.FacesDesignProperty;
import org.netbeans.modules.visualweb.propertyeditors.binding.data.TabbedDataBindingPanel;
import org.netbeans.modules.visualweb.propertyeditors.util.Bundle;
import org.openide.awt.Mnemonics;

public class ValueBindingPropertyEditor implements PropertyEditor, ExPropertyEditor, FacesBindingPropertyEditor, BindingTargetCallback,
        com.sun.rave.propertyeditors.binding.ValueBindingPropertyEditor {
    /**
     * Key used to get names of panel classes from the property descriptor.
     */
    public static final String BINDING_PANEL_CLASS_NAMES = "bindingPanelClassNames"; // NOI18N
    
    private static final Bundle bundle = Bundle.getBundle(ValueBindingPropertyEditor.class);
    
    protected PropertyEditor delegatePropertyEditor;
    
    private Object value;
    
    protected Component customEditor;
    private Vector listeners;
    
    private boolean useDelegatePropertyEditor;
    
    private class TabbedDataBindingPanelAdapter extends JPanel implements EnhancedCustomPropertyEditor {
        private TabbedDataBindingPanel tabbedDataBindingPanel;
        
        public TabbedDataBindingPanelAdapter(BindingTargetCallback callback, DesignProperty prop, Class[] bindingPanelClasses, boolean showExpr) {
            setLayout(new BorderLayout(8, 8));
            
            tabbedDataBindingPanel = new TabbedDataBindingPanel(callback, prop, bindingPanelClasses, showExpr);
            add(tabbedDataBindingPanel, BorderLayout.CENTER);
        }
        
                /* (non-Javadoc)
                 * @see org.openide.explorer.propertysheet.editors.EnhancedCustomPropertyEditor#getPropertyValue()
                 */
        public Object getPropertyValue() throws IllegalStateException {
            return ValueBindingPropertyEditor.this.getValue();
        }
    }
    
    private class TabbedDataBindingPanelWrapper extends JPanel implements EnhancedCustomPropertyEditor {
        private JRadioButton useValueButton;
        private JRadioButton useBindingButton;
        private ButtonGroup  useValueUseBindingGroup;
        
        private static final String BINDING_CARD = "bindingCard"; // NOI18N
        private static final String VALUE_CARD   = "valueCard";   // NOI18N
        
        private CardLayout cardLayout;
        private JPanel cardPanel;
        
        private TabbedDataBindingPanel tabbedDataBindingPanel;
        
        private Component delegateCustomEditorComponent;
        
        public TabbedDataBindingPanelWrapper(BindingTargetCallback callback, DesignProperty prop, Class[] bindingPanelClasses, boolean showExpr, Component delegateCustomEditor) {
            delegateCustomEditorComponent = delegateCustomEditor;
            
            setLayout(new GridBagLayout());
            GridBagConstraints gridBagConstraints;
            
            useValueUseBindingGroup = new ButtonGroup();
            
            useBindingButton = new JRadioButton(); // NOI18N
            Mnemonics.setLocalizedText(useBindingButton, bundle.getMessage("LBL_UseBindingRadionButton"));
            useBindingButton.getAccessibleContext().setAccessibleName(bundle.getMessage("LBL_UseBindingRadionButton")); // NOI18N
            useBindingButton.getAccessibleContext().setAccessibleDescription(bundle.getMessage("LBL_UseBindingRadionButton_A11YDescription")); // NOI18N
            useValueUseBindingGroup.add(useBindingButton);
            
            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.insets = new Insets(12, 12, 0, 0);
            gridBagConstraints.anchor = GridBagConstraints.WEST;
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 0;
            add(useBindingButton, gridBagConstraints);
            
            useValueButton = new JRadioButton(); // NOI18N
            Mnemonics.setLocalizedText(useValueButton, bundle.getMessage("LBL_UseValueRadionButton"));
            useValueButton.getAccessibleContext().setAccessibleName(bundle.getMessage("LBL_UseValueRadionButton")); // NOI18N
            useValueButton.getAccessibleContext().setAccessibleDescription(bundle.getMessage("LBL_UseValueRadionButton_A11YDescription")); // NOI18N
            useValueUseBindingGroup.add(useValueButton);
            
            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.insets = new Insets(12, 12, 0, 11);
            gridBagConstraints.anchor = GridBagConstraints.WEST;
            gridBagConstraints.gridx = 1;
            gridBagConstraints.gridy = 0;
            add(useValueButton, gridBagConstraints);
            
            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 1;
            gridBagConstraints.gridwidth = 2;
            gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
            gridBagConstraints.anchor = GridBagConstraints.WEST;
            gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 11);
            add(new JSeparator(), gridBagConstraints);
            
            cardLayout = new CardLayout();
            cardPanel = new JPanel(cardLayout);
            //Bug Fix 6335113
            if((bindingPanelClasses == null) || (bindingPanelClasses.length == 1)){
                bindingPanelClasses = new Class[]{DataBindingHelper.BIND_VALUE_TO_DATAPROVIDER, DataBindingHelper.BIND_VALUE_TO_OBJECT};
            }
            tabbedDataBindingPanel = new TabbedDataBindingPanel(callback, prop, bindingPanelClasses, showExpr);
            cardPanel.add(tabbedDataBindingPanel, BINDING_CARD);
            
            if (delegateCustomEditor == null) {
                JTextArea message = new JTextArea(bundle.getMessage("MSG_EditValueInPropertySheet")); // NOI18N
                message.setEditable(false);
                message.setLineWrap(true);
                message.setWrapStyleWord(true);
                message.setBorder(null);
                message.setBackground(UIManager.getColor("Label.background")); // NOI18N
                message.getAccessibleContext().setAccessibleName(bundle.getMessage("MSG_EditValueInPropertySheet")); // NOI18N
                message.getAccessibleContext().setAccessibleDescription(bundle.getMessage("MSG_EditValueInPropertySheet"));  // NOI18N
                cardPanel.add(message, VALUE_CARD);
            } else {
                cardPanel.add(delegateCustomEditor, VALUE_CARD);
            }
            
            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.insets = new Insets(12, 12, 12, 11);
            gridBagConstraints.anchor = GridBagConstraints.WEST;
            gridBagConstraints.fill = GridBagConstraints.BOTH;
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 2;
            gridBagConstraints.gridwidth = 2;
            gridBagConstraints.weightx = 1;
            gridBagConstraints.weighty = 1;
            add(cardPanel, gridBagConstraints);
            
            // set the state of radio buttons and the card panel
            if (isPropertyBound()) {
                useBindingButton.setSelected(true);
                cardLayout.show(cardPanel, BINDING_CARD);
            } else {
                useValueButton.setSelected(true);
                cardLayout.show(cardPanel, VALUE_CARD);
            }
            
            // add listeners
            useBindingButton.addActionListener(
                    new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    adjustUseValueUseBinding();
                }
            }
            );
            useValueButton.addActionListener(
                    new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    adjustUseValueUseBinding();
                }
            }
            );
        }
        
        public void addNotify() {
            super.addNotify();
            // now adjust initial focus...
            cardPanel.requestFocus();
            SwingUtilities.invokeLater(
                    new Runnable() {
                public void run() {
                    cardPanel.transferFocus();
                }
            }
            );
        }
        
        /**
         *
         */
        void adjustUseValueUseBinding() {
            if (useBindingButton.isSelected()) {
                setUseDelegatePropertyEditor(false);
                showBindingCard();
            } else if (useValueButton.isSelected()) {
                setUseDelegatePropertyEditor(true);
                showValueCard();
            }
        }
        
        void showBindingCard() {
            cardLayout.show(cardPanel, BINDING_CARD);
        }
        
        void showValueCard() {
            cardLayout.show(cardPanel, VALUE_CARD);
        }
        
                /* (non-Javadoc)
                 * @see org.openide.explorer.propertysheet.editors.EnhancedCustomPropertyEditor#getPropertyValue()
                 */
        public Object getPropertyValue() throws IllegalStateException {
            if (isUseDelegatePropertyEditor()) {
                if (delegateCustomEditorComponent instanceof EnhancedCustomPropertyEditor) {
                    Object propertyValue = ((EnhancedCustomPropertyEditor)delegateCustomEditorComponent).getPropertyValue();
                    setValueInternal(propertyValue);
                    setUseDelegatePropertyEditor(!isValueBindingValue());
                    return getValueInternal();
                }
            }
            return ValueBindingPropertyEditor.this.getValue();
        }
    }
    
    public ValueBindingPropertyEditor() {
        this(null);
    }
    
    public ValueBindingPropertyEditor(PropertyEditor propertyEditor) {
        delegatePropertyEditor = propertyEditor;
    }
    
    // Return if the property is bound
    protected boolean isPropertyBound() {
        return (facesDesignProperty != null && facesDesignProperty.isBound());
    }
    
    // Return if the value is bound
    protected boolean isValueBindingValue() {
        return (value instanceof ValueBinding);
    }
    
    // Return the value of value binding
    protected Object getValueBindingValue() {
        if (isValueBindingValue()) {
            FacesDesignContext fctx = (FacesDesignContext) facesDesignProperty.getDesignBean().getDesignContext();
            ValueBinding valueBinding = ((ValueBinding) value);
            return valueBinding.getValue(fctx.getFacesContext());
        }
        return null;
    }
    
    // Return the mode of custom edior panel
    protected boolean isUseDelegatePropertyEditor() {
        return useDelegatePropertyEditor;
    }
    
    // Set the mode of custom edior panel
    protected void setUseDelegatePropertyEditor(boolean newUseValue) {
        if (delegatePropertyEditor == null) {
            useDelegatePropertyEditor = false;
        } else {
            if (useDelegatePropertyEditor == newUseValue) {
                return;
            }
            // set state
            useDelegatePropertyEditor = newUseValue;
        }
    }
    
    protected void setValueInternal(Object newValue) {
        value = newValue;
    }
    
    protected Object getValueInternal() {
        return value;
    }
    
    // PropertyEditor2 implementation
    
    protected DesignProperty designProperty;
    protected FacesDesignProperty facesDesignProperty;
    
    public void setDesignProperty(DesignProperty prop) {
        designProperty = prop;
        if (prop instanceof FacesDesignProperty) {
            facesDesignProperty = (FacesDesignProperty) prop;
        } else {
            facesDesignProperty = null;
        }
    }
    
    // PropertyEditor implementation
    
        /* (non-Javadoc)
         * @see java.beans.PropertyEditor#supportsCustomEditor()
         */
    public boolean supportsCustomEditor() {
        return true;
    }
    
        /* (non-Javadoc)
         * @see java.beans.PropertyEditor#getCustomEditor()
         */
    public Component getCustomEditor() {
        // initialize the value
        setValue(designProperty.getValue());
        
        // No delegate - most likely this editor was configured as the property editor
        if (delegatePropertyEditor == null) {
            customEditor = new TabbedDataBindingPanelAdapter(this,
                    designProperty,
                    (Class[]) designProperty.getPropertyDescriptor().getValue(BINDING_PANEL_CLASS_NAMES),
                    true);
        } else {
            // Delegate present - create custom editor component using binding panel and custom editor component supplied delegate property editor
            Component delegateCustomEditorComponent = null;
            if (delegatePropertyEditor.supportsCustomEditor()) {
                delegateCustomEditorComponent = delegatePropertyEditor.getCustomEditor();
            }
            customEditor = new TabbedDataBindingPanelWrapper(this,
                    designProperty,
                    (Class[]) designProperty.getPropertyDescriptor().getValue(BINDING_PANEL_CLASS_NAMES),
                    true,
                    delegateCustomEditorComponent);
        }
        
        return customEditor;
    }
    
        /* (non-Javadoc)
         * @see java.beans.PropertyEditor#addPropertyChangeListener(java.beans.PropertyChangeListener)
         */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        // record the listener
        if (listeners == null) {
            listeners = new java.util.Vector();
        }
        listeners.addElement(listener);
        
        // add to the delegate when going to/from bound state
        if (delegatePropertyEditor != null) {
            delegatePropertyEditor.addPropertyChangeListener(listener);
        }
    }
    
        /* (non-Javadoc)
         * @see java.beans.PropertyEditor#removePropertyChangeListener(java.beans.PropertyChangeListener)
         */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        // remove the listener
        if (listeners == null) {
            return;
        }
        listeners.removeElement(listener);
        
        // remove from the delegate when going to/from bound state
        if (delegatePropertyEditor != null) {
            delegatePropertyEditor.removePropertyChangeListener(listener);
        }
    }
    
    /**
     * Report that we have been modified to any interested listeners.
     */
    void firePropertyChange() {
        Vector targets;
        synchronized (this) {
            if (listeners == null) {
                return;
            }
            targets = (Vector) listeners.clone();
        }
        
        // Tell our listeners that "everything" has changed.
        PropertyChangeEvent evt = new PropertyChangeEvent(this, null, null, null);
        
        for (int i = 0; i < targets.size(); i++) {
            PropertyChangeListener target = (PropertyChangeListener)targets.elementAt(i);
            target.propertyChange(evt);
        }
    }
    
        /* (non-Javadoc)
         * @see java.beans.PropertyEditor#getValue()
         */
    public Object getValue() {
        if (isUseDelegatePropertyEditor()) {
            if (delegatePropertyEditor != null) {
                setValueInternal(delegatePropertyEditor.getValue());
                setUseDelegatePropertyEditor(!isValueBindingValue());
            }
        }
        return getValueInternal();
    }
    
        /* (non-Javadoc)
         * @see java.beans.PropertyEditor#setValue(java.lang.Object)
         */
    public void setValue(Object newValue) {
        if (isPropertyBound()) {
            // the property is bound so use the value binding as the value
            Object valueBinding = facesDesignProperty.getValueBinding();
            // set private value
            setValueInternal(valueBinding);
            setUseDelegatePropertyEditor(false);
        } else {
            setValueInternal(newValue);
            setUseDelegatePropertyEditor(true);
        }
        if (delegatePropertyEditor != null) {
            delegatePropertyEditor.setValue(newValue);
        }
    }
    
        /* (non-Javadoc)
         * @see java.beans.PropertyEditor#getJavaInitializationString()
         */
    public String getJavaInitializationString() {
        if (isUseDelegatePropertyEditor()) {
            if (delegatePropertyEditor != null) {
                return delegatePropertyEditor.getJavaInitializationString();
            }
        }
        
        if (isValueBindingValue()) {
            // the value is bound - get the value of the value binding
            if (delegatePropertyEditor != null) {
                // set the value on delegate and then call getAsText() on it.
                delegatePropertyEditor.setValue(getValueBindingValue());
                return delegatePropertyEditor.getJavaInitializationString();
            }
        } else {
            if (delegatePropertyEditor != null) {
                // set the value on delegate and then call getAsText() on it.
                delegatePropertyEditor.setValue(getValueInternal());
                return delegatePropertyEditor.getJavaInitializationString();
            }
        }
        
        return "";
    }
    
        /* (non-Javadoc)
         * @see java.beans.PropertyEditor#getAsText()
         */
    public String getAsText() {
        if (isUseDelegatePropertyEditor()) {
            if (delegatePropertyEditor != null) {
                return delegatePropertyEditor.getAsText();
            }
        }
        
        if (isValueBindingValue()) {
            return ((ValueBinding) getValueInternal()).getExpressionString();
        } else {
            if (delegatePropertyEditor != null) {
                // set the value on delegate and then call getAsText() on it.
                delegatePropertyEditor.setValue(getValueInternal());
                return delegatePropertyEditor.getAsText();
            }
        }
        
        if (getValueInternal() == null) {
            return "";
        }
        
        return String.valueOf(getValueInternal());
    }
    
        /* (non-Javadoc)
         * @see java.beans.PropertyEditor#setAsText(java.lang.String)
         */
    public void setAsText(String text) throws IllegalArgumentException {
        if (isUseDelegatePropertyEditor()) {
            if (delegatePropertyEditor != null) {
                delegatePropertyEditor.setAsText(text);
            }
        } else {
            text = text.trim();
            if (text.startsWith("#{") && text.endsWith("}")) { //NOI18N
                FacesDesignContext fctx = (FacesDesignContext)designProperty.getDesignBean().getDesignContext();
                Application app = fctx.getFacesContext().getApplication();
                setValueInternal(app.createValueBinding(text));
            } else if (text.length() > 0) {
                if (delegatePropertyEditor != null) {
                    delegatePropertyEditor.setAsText(text);
                    setValueInternal(delegatePropertyEditor.getValue());
                }
            } else {
                setValueInternal(facesDesignProperty.getUnsetValue());
            }
        }
    }
    
        /* (non-Javadoc)
         * @see java.beans.PropertyEditor#getTags()
         */
    public String[] getTags() {
        if (isUseDelegatePropertyEditor()) {
            if (delegatePropertyEditor != null) {
                return delegatePropertyEditor.getTags();
            }
        }
        
        if (isValueBindingValue()) {
            return null;
        } else {
            if (delegatePropertyEditor != null) {
                return delegatePropertyEditor.getTags();
            }
        }
        return null;
    }
    
        /* (non-Javadoc)
         * @see java.beans.PropertyEditor#isPaintable()
         */
    public boolean isPaintable() {
        if (isUseDelegatePropertyEditor()) {
            if (delegatePropertyEditor != null) {
                return delegatePropertyEditor.isPaintable();
            }
        }
        
        if (isValueBindingValue()) {
            // special handling of Boolean or boolean typed property to avoid ClassCastException in NetBeans propertysheet code
            Class c = designProperty.getPropertyDescriptor().getPropertyType();
            if ((c == Boolean.class) || (c == boolean.class)) {
                return true;
            }
        }
        return false;
    }
    
    private static final ImageIcon BOUND_ICON = new ImageIcon(ValueBindingPropertyEditor.class.getResource("img/bound.png"));
    
    
        /* (non-Javadoc)
         * @see java.beans.PropertyEditor#paintValue(java.awt.Graphics, java.awt.Rectangle)
         */
    public void paintValue(Graphics gfx, Rectangle box) {
        if (!isPaintable()) return;
        
        if (isUseDelegatePropertyEditor()) {
            if (delegatePropertyEditor != null) {
                delegatePropertyEditor.paintValue(gfx, box);
            }
            return;
        }
        
        if (isValueBindingValue()) {
            // special handling of Boolean or boolean typed property to avoid ClassCastException in NetBeans propertysheet code
            Class c = designProperty.getPropertyDescriptor().getPropertyType();
            if ((c == Boolean.class) || (c == boolean.class)) {
                JLabel lbl = new JLabel(BOUND_ICON, JLabel.LEFT);
                String text = getAsText();
                lbl.setText(text == null ? "" : text); //NOI18N
                lbl.setBounds(box);
                // get the forground color from the graphics object as this may be a select row of the
                // property sheet in which the foreground needs to be selected foreground
                lbl.setForeground(gfx.getColor());
                lbl.paint(gfx);
            }
        }
    }
    
    // BindingTargetCallback implementation
    
        /* (non-Javadoc)
         * @see com.sun.rave.propertyeditors.binding.BindingTargetCallback#refresh()
         */
    public void refresh() {
        // not a gui - do nothing
    }
    
        /* (non-Javadoc)
         * @see com.sun.rave.propertyeditors.binding.BindingTargetCallback#setNewExpressionText(java.lang.String)
         */
    public void setNewExpressionText(String newExpr) {
        // convert to value binding
        if (newExpr.startsWith("#{") && newExpr.endsWith("}")) { //NOI18N
            FacesDesignContext fctx = (FacesDesignContext)designProperty.getDesignBean().getDesignContext();
            Application app = fctx.getFacesContext().getApplication();
            setValueInternal(app.createValueBinding(newExpr));
            // switch the mode immidiately to use the Use Binding panels so that correct value i.e.
            // value binding is returned
            setUseDelegatePropertyEditor(false);
            if (delegatePropertyEditor != null) {
                delegatePropertyEditor.setValue(getValueBindingValue());
            }
        } else {
            setValueInternal(facesDesignProperty.getUnsetValue());
            // switch the mode immidiately to use the Use Value panels
            setUseDelegatePropertyEditor(true);
            if (delegatePropertyEditor != null) {
                delegatePropertyEditor.setValue(facesDesignProperty.getUnsetValue());
            }
        }
    }
    
        /* (non-Javadoc)
         * @see org.openide.explorer.propertysheet.ExPropertyEditor#attachEnv(org.openide.explorer.propertysheet.PropertyEnv)
         */
    public void attachEnv(PropertyEnv env) {
        if (delegatePropertyEditor instanceof ExPropertyEditor) {
            ((ExPropertyEditor)delegatePropertyEditor).attachEnv(env);
        }
    }
}
