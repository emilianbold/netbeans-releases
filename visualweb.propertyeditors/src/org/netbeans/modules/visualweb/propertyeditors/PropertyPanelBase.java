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
package org.netbeans.modules.visualweb.propertyeditors;

import com.sun.rave.designtime.DesignProperty;
import com.sun.rave.propertyeditors.resolver.PropertyEditorResolver;
import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyDescriptor;
import java.beans.PropertyEditor;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.util.Collection;
import javax.swing.JPanel;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.util.Lookup;

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
            // Ask each property editor resolver service that was registered with the
            // IDE for an editor appropriate for the property descriptor
            
            PropertyEditor editor = null;
            
            for (PropertyEditorResolver resolver : getPropertyEditorResolvers()) {
                editor = resolver.getEditor(descriptor);
                if (editor != null)
                    break;
            }
            if (editor == null){
                editor = (PropertyEditor) editorClass.newInstance();
            }
            if (PropertyEditorBase.class.isAssignableFrom(editor.getClass()))
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
    
    private static Lookup.Result propertyEditorResolverLookupResult;
    
    /**
     * Look up all property editor resolvers registered with the current IDE session.
     */
    private static PropertyEditorResolver[] getPropertyEditorResolvers() {
        if (propertyEditorResolverLookupResult == null) {
            Lookup.Template template = new Lookup.Template(PropertyEditorResolver.class);
            Lookup lookup = Lookup.getDefault();
            propertyEditorResolverLookupResult = lookup.lookup(template);
        }
        Collection instances = propertyEditorResolverLookupResult.allInstances();
        return (PropertyEditorResolver[]) instances.toArray(
                new PropertyEditorResolver[instances.size()]);
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
