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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.visualweb.propertyeditors;

import com.sun.rave.designtime.DesignProperty;
import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyDescriptor;
import java.beans.PropertyEditor;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import javax.swing.JPanel;
import org.openide.explorer.propertysheet.PropertyEnv;

/**
 * A abstract base class for custom property editors. Editor panels that extend
 * this class are intended for use either as custom editors returned by {@link
 * java.beans.PropertyEditor#getCustomEditor()}, or as panels used within a
 * design-time customizer. The initial state of the property and its context
 * is made available to the editor in the design property passed to its
 * constructor. When the user has clicked "ok" in the containing panel, the
 * editor method <code>getPropertyValue()</code> is called and should return
 * the current value.
 *
 * @author gjmurphy
 */
public abstract class PropertyPanelBase extends JPanel {

    /**
     * A utility factory method for creating a property panel, such as for use in a
     * component customizer. If this method is unable to create a property panel
     * for the given design property, it returns null. This may happen if the
     * property is value bound, if the property does not specify a property editor
     * class, or if the component returned by PropertyEditor.getCustomEditor() is
     * not an instance of this base class.
     */
    public static PropertyPanelBase createPropertyPanel(DesignProperty designProperty) {
        PropertyDescriptor descriptor = designProperty.getPropertyDescriptor();
        Class editorClass = descriptor.getPropertyEditorClass();
        if (editorClass == null)
            return null;
        try {
            PropertyEditor editor = (PropertyEditor) editorClass.newInstance();
            if (PropertyEditorBase.class.isAssignableFrom(editorClass))
                ((PropertyEditorBase) editor).setDesignProperty(designProperty);
            editor.setValue(designProperty.getValue());
            Component customEditor = editor.getCustomEditor();
            if (PropertyPanelBase.class.isAssignableFrom(customEditor.getClass()))
                return (PropertyPanelBase) customEditor;
        } catch (InstantiationException e ) {
            e.printStackTrace();
        } catch (IllegalAccessException e ) {
            e.printStackTrace();
        }
        return null;
    }

    PropertyEditorBase propertyEditor;
    PanelSubmissionListener panelSubmissionListener;

    /**
     * Create a new instance of PropertyPanelBase, for the property editor specified.
     * Property editors that extend {@link PropertyEditorBase} should pass themselves
     * as an argument to the constructor of their property panel when the panel is
     * created, in response to a call to {@link java.beans.PropertyEditor#getCustomEditor()}.
     */
    public PropertyPanelBase(PropertyEditorBase propertyEditor) {
        PropertyEnv propertyEnv = propertyEditor.getEnv();
        if (propertyEnv != null) {
            propertyEnv.setState(PropertyEnv.STATE_NEEDS_VALIDATION);
            panelSubmissionListener = new PanelSubmissionListener(propertyEditor);
            propertyEnv.addVetoableChangeListener(panelSubmissionListener);
            this.propertyEditor = propertyEditor;
        }
    }

    /**
     * This method is called just after the user has clicked "ok". Sub-classes
     * should use this method to calculate and return the property value.
     */
    public abstract Object getPropertyValue();

    protected void finalize() throws Throwable {
        if (panelSubmissionListener != null)
            this.propertyEditor.getEnv().removeVetoableChangeListener(panelSubmissionListener);
        super.finalize();
    }
    
    
    class PanelSubmissionListener implements VetoableChangeListener {
        
        PropertyEditorBase propertyEditor;
        
        PanelSubmissionListener(PropertyEditorBase propertyEditor) {
            this.propertyEditor = propertyEditor;
        }
        
        public final void vetoableChange(PropertyChangeEvent event) throws PropertyVetoException {
            if (PropertyEnv.PROP_STATE.equals(event.getPropertyName())) {
                propertyEditor.setValue(getPropertyValue());
                // In theory, it should not be necessary to fire a property change event
                // at this point, but NetBeans will not otherwise pick up the new value
                propertyEditor.firePropertyChange();
                propertyEditor.getEnv().setState(PropertyEnv.STATE_VALID);
            }
        }
    }
    
}
