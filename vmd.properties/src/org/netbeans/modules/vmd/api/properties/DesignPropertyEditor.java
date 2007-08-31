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
 * Microsystems, Inc. All Rights Reserved.OOOO
 */

package org.netbeans.modules.vmd.api.properties;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyEditorSupport;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;

import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.netbeans.modules.vmd.api.model.DesignEvent;
import org.netbeans.modules.vmd.api.model.PropertyDescriptor;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.model.common.ActiveDocumentSupport;
import org.netbeans.modules.vmd.api.model.presenters.InfoPresenter;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.InplaceEditor;
import org.openide.explorer.propertysheet.InplaceEditor.Factory;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.nodes.PropertySupport;

/**
 *
 * @author Karol Harezlak
 */

 public abstract class DesignPropertyEditor extends PropertyEditorSupport implements ExPropertyEditor, Factory {
    
    private List<String> propertyNames;
    private WeakReference<DesignComponent> component;
    private Object tempValue;
    private PropertyValue propertyValue;
    private PropertySupport propertySupport;
    private InplaceEditor inplaceEditor;
    private String propertyDisplayName;
    private String customEditorTitle;
    
    /**
     * Useful especially when passing value Boolean.FALSE - in this case disable
     * the possibility of editing the the value as text but allows getAsText to
     * return non null. Also used by passing Boolean.TRUE to indicate
     * that a editor should allow the user to type their own text into the combo box.
     * @return the state of property in-place editor in the advanced property sheet
     */
    public Boolean canEditAsText() {
        return null;
    }
    /**
     * Test whether the property is writable.
     * @return true if the write of the value is supported
     */
    public boolean canWrite() {
        return true;
    }
    
    /**
     * This method indicates whether the current value is the same as the value
     * that would otherwise be restored by calling restoreDefaultValue()
     * (if supportsDefaultValue()  returns true). The default implementation
     * returns true and it is recommended to also return true when supportsDefaultValue()
     * returns false (if we do not support default value any value can be considered
     * as the default). If supportsDefaultValue() returns false this method will
     * not be called by the default implementation of property sheet.
     * @return on default checks
     */
    public boolean isDefaultValue() {
        if (propertyNames == null || propertyNames.isEmpty()) {
            return true;
        }
        
        final boolean[] isDefaultValue = new boolean[] { true };
        component.get().getDocument().getTransactionManager().readAccess(new Runnable() {
            public void run() {
                for (String propertyName : propertyNames) {
                    if (! component.get().isDefaultValue(propertyName)) {
                        isDefaultValue[0] = false;
                        break;
                    }
                }
            }
        });
        
        return isDefaultValue[0];
    }
    
    @Override
    public boolean supportsCustomEditor() {
        Collection components = ActiveDocumentSupport.getDefault().getActiveComponents();
        if (components != null && components.size() == 1)
            return true;
        
        return false;
    }
    
    /**
     * Test whether the property had a default value.
     * @return true if it does (true by default)
     */
    public boolean supportsDefaultValue() {
        if (propertyNames == null || propertyNames.isEmpty()) {
            return false;
        }
        
        final boolean[] supportsDefaultValue = new boolean[] { true };
        component.get().getDocument().getTransactionManager().readAccess(new Runnable() {
            public void run() {
                for (String propertyName : propertyNames) {
                    PropertyDescriptor propertyDescriptor = component.get().getComponentDescriptor().getPropertyDescriptor(propertyName);
                    if(! (propertyDescriptor.getDefaultValue().getKind() != PropertyValue.Kind.NULL || propertyDescriptor.isAllowNull())) {
                        supportsDefaultValue[0] = false;
                        break;
                    }
                }
            }
        });
        
        return supportsDefaultValue[0];
    }
    
    /**
     * Returns value used to restore this property to its default value, if supported. In the default
     * implementation, returns defaultValue of PropertyValue.
     * @return default value specifed in the model for this property
     */
    public Object getDefaultValue() {
        if (propertyNames == null || propertyNames.isEmpty())
            throw new IllegalStateException("Unable to obtain default value for this property without property name"); //NOI18N
        
        if (! (tempValue instanceof GroupValue))
            return readDefaultPropertyValue(propertyNames.iterator().next());
        
        GroupValue newAdvancedValue = ((GroupValue) tempValue);
        for (String propertyName : newAdvancedValue.getPropertyNames()) {
            ((GroupValue) tempValue).putValue(propertyName, readDefaultPropertyValue(propertyName));
        }
        
        return  newAdvancedValue;
    }
    
    /**
     * This method is called by the property sheet to pass the environment to the property editor.
     * The typical use case is for the ExPropertyEditor to call env.getFeatureDescriptor().getValue
     * (String key) to retrieve any hints the Property object may supply regarding how
     * the property editor should behave(such as providing alternate text representations of
     * "true" and "false" for a Boolean property editor).
     * @param allows an object (suchDef as a Node.Property instance) to communicate hints to
     * property editor instances
     */
    public void attachEnv(PropertyEnv env) {
        env.setState(PropertyEnv.STATE_NEEDS_VALIDATION);
        env.addVetoableChangeListener( new VetoableChangeListener() {
            public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
                customEditorOKButtonPressed();
            }
        });
    }
    
    public final void resolve(DesignComponent component,
                              List<String> propertyNames,
                              Object value,
                              PropertySupport propertySupport,
                              String propertyDisplayName) {
        
        this.component = new WeakReference<DesignComponent>(component);
        this.propertyNames = propertyNames;
        this.tempValue = value;
        super.setValue(value);
        this.propertySupport = propertySupport;
        this.propertyDisplayName = propertyDisplayName;
    }
    
    public final void resolveInplaceEditor(InplaceEditor inplaceEditor) {
        this.inplaceEditor = inplaceEditor;
    }
    
    public InplaceEditor getInplaceEditor() {
        return inplaceEditor;
    }
    
    /**
     * Title of the custom property editor dialog.
     * @return custom property editor title
     */
    public String getCustomEditorTitle() {
        if (component == null)
            return null;
        
        component.get().getDocument().getTransactionManager().readAccess((new Runnable() {
            public void run() {
                DesignDocument document = component.get().getDocument();
                if (component.get().getParentComponent() == null && document.getRootComponent() != component.get()) {
                    customEditorTitle = null;
                    return;
                }
                customEditorTitle = InfoPresenter.getDisplayName(component.get()) + " - " + propertyDisplayName; //NOI18N
            }
        }));
        return customEditorTitle;
    }
    
    /**
     * It's called when PropertyEditor is attached to the DesignComponent using PropertiesPresenter (notifyAttached)
     * @param DesignComponent attached to this PropertyEditor
     */
    public void init(DesignComponent component) {
    }
    /**
     * Its called according to the DesignEventFilterResolver(DesignEventFilter) passed into the PropertiesPresenter
     *
     */
    public void notifyDesignChanged(DesignEvent event) {
    }
    
    /**
     * Describes if executeInsideWriteTransaction() will be executed inside write
     * transaction responsible for saving data from property editor into the model.
     * @return Boolean.TRUE will be executed.
     */
    public boolean isExecuteInsideWriteTransactionUsed() {
        return true;
    }
    
    /**
     * This method is executed at the beginning of write transaction when data from property
     * editor is writes into the model
     * @return Boolean.FALSE ONLY this method will be executed without any other 
     * support it's means that value of property editor is not saved 
     */
    public boolean executeInsideWriteTransaction() {
        return true;
    }
    
    /**
     * Method is invoked after OK button is pressed in custom editor.
     */
    public void customEditorOKButtonPressed() {
    }
    
    @SuppressWarnings("unchecked") // NOI18N
    public final void invokeSaveToModel() {
        try {
            propertySupport.setValue(getValue());
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        } catch (InvocationTargetException ex) {
            ex.printStackTrace();
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
        }
    }
    
    private PropertyValue readDefaultPropertyValue(final String propertyName) {
        component.get().getDocument().getTransactionManager().readAccess(new Runnable() {
            public void run() {
                propertyValue = component.get().getComponentDescriptor().getPropertyDescriptor(propertyName).getDefaultValue();
            }
        });
        
        return propertyValue;
    }
    
    public String getPropertyDisplayName() {
        return propertyDisplayName;
    }
}
