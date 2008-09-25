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
 * made subject to such option by the copyright holder.OOOO
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
/**
 * 
 * This class represents custom property editor in the Properties Window.
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
     * This method is invokes when Property Editor needs to be destroy.
     */
    public void cleanUp(DesignComponent component) {
        propertyNames = null;
        tempValue = null;
        propertyValue = null;
        propertySupport = null;
        inplaceEditor = null;
    }

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

        final boolean[] isDefaultValue = new boolean[]{true};
        component.get().getDocument().getTransactionManager().readAccess(new Runnable() {

            public void run() {
                for (String propertyName : propertyNames) {
                    if (!component.get().isDefaultValue(propertyName)) {
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
        if (components != null && components.size() == 1) {
            return true;
        }

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

        final boolean[] supportsDefaultValue = new boolean[]{true};
        component.get().getDocument().getTransactionManager().readAccess(new Runnable() {

            public void run() {
                for (String propertyName : propertyNames) {
                    PropertyDescriptor propertyDescriptor = component.get().getComponentDescriptor().getPropertyDescriptor(propertyName);
                    if (!(propertyDescriptor.getDefaultValue().getKind() != PropertyValue.Kind.NULL || propertyDescriptor.isAllowNull())) {
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
        if (propertyNames == null || propertyNames.isEmpty()) {
            throw new IllegalStateException("Unable to obtain default value for this property without property name"); //NOI18N
        }
        if (!(tempValue instanceof GroupValue)) {
            return readDefaultPropertyValue(propertyNames.iterator().next());
        }

        GroupValue newAdvancedValue = ((GroupValue) tempValue);
        for (String propertyName : newAdvancedValue.getPropertyNames()) {
            ((GroupValue) tempValue).putValue(propertyName, readDefaultPropertyValue(propertyName));
        }

        return newAdvancedValue;
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
        env.addVetoableChangeListener(new VetoableChangeListener() {

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
        if (component == null) {
            return null;
        }

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
     * Method is invoked after OK button is pressed in the custom editor.
     */
    public void customEditorOKButtonPressed() {
    }

    /**
     * This method gives support to chose how to handle reset To Defualt event inside of custom property editor.
     * When returns Boolean.True (on default) reset to default is handle automatically
     * by the method getDefaultValue and all values connected with this
     * property editor are restored to the default based on the getDefaultValue method.
     * When return Boolean.False, restoring values to the default state
     * has to be resolve completely inside of method customEditorResetToDefaultButtonPressed
     * @return boolean value
     */
    public boolean isResetToDefaultAutomatically() {
        return true;
    }

    /**
     * Method is invoked after Reset To Defaulat button is pressed in the custom property editor.
     * This method is executed only when isResetToDefaultAutomatically return Boolean.False.
     * NOTE: This method is executed inside of write transaction.
     */
    public void customEditorResetToDefaultButtonPressed() {
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

    public List<String> getPropertyNames() {
        return propertyNames;
    }
}
