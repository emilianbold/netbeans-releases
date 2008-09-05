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
package org.netbeans.modules.vmd.midp.propertyeditors.api.resource.element;

import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.model.TypeID;
import org.netbeans.modules.vmd.midp.components.general.ClassCode;
import org.netbeans.modules.vmd.midp.components.resources.ResourceCD;
import javax.swing.*;
import java.util.*;
import org.netbeans.modules.vmd.midp.propertyeditors.api.usercode.PropertyEditorMessageAwareness;

/**
 *
 * @author Anton Chechel
 */
public abstract class PropertyEditorResourceElement extends JPanel {

    private List<PropertyEditorResourceElementListener> listeners = new ArrayList<PropertyEditorResourceElementListener>(1);

    // component to represent view
    public abstract JComponent getJComponent();

    // component to represent model
    // can be null
    public abstract void setDesignComponentWrapper(DesignComponentWrapper component);

    // TypeID
    public abstract TypeID getTypeID();

    // List of Property Values
    public abstract List<String> getPropertyValueNames();
    
    // support for post setValue() action
    public boolean isPostSetValueSupported(DesignComponent component) {
        return false;
    }

    // support for post setValue() action
    public void postSetValue(DesignComponent parentComponent, DesignComponent childComponent) {
    }
    /**
     * Look ad DesignPropertyEditor for more ifnormation
     */
    public boolean isResetToDefaultAutomatically(DesignComponent component) {
        return true;
    }

    // messageAwareness allows to show warning/error message in custom PropertyEditor
    public void setPropertyEditorMessageAwareness(PropertyEditorMessageAwareness messageAwareness) {
    }
    
    //executed in the write transaction on the end of the saving of changes
    public void postSaveValue(DesignComponent parentComponent) {
    }
    
    // icon path
    public String getIconPath() {
        return ResourceCD.ICON_PATH;
    }

    public String getResourceNameSuggestion() {
        return ClassCode.getSuggestedMainName (getTypeID ());
    }

    public void addPropertyEditorResourceElementListener(PropertyEditorResourceElementListener listener) {
        listeners.add(listener);
    }

    public void removePropertyEditorResourceElementListener(PropertyEditorResourceElementListener listener) {
        listeners.remove(listener);
    }

    protected void fireElementChanged(long componentID, String propertyName, PropertyValue propertyValue) {
        for (PropertyEditorResourceElementListener listener : listeners) {
            listener.elementChanged(new PropertyEditorResourceElementEvent(componentID, propertyName, propertyValue));
        }
    }
    
    /**
     * When property editor sets to null
     * @param component Design component of the property Editor
     */
    public void nullValueSet(DesignComponent component) {
    }
    
    /**
     *It is invokes at the end of the customEditorResetToDefaultValue of the DesignpropertyEditor
     * @param component Design component of the property Editor
     */
    public void preResetToDefaultValue(DesignComponent component) {
    }

    public static boolean isPropertyValueAUserCodeType(PropertyValue propertyValue) {
        return propertyValue != null && propertyValue.getKind() == PropertyValue.Kind.USERCODE;
    }
    
    public void listSelectionHappened() {  
    }
    
    public void getCustomEdiotrNotification() {  
    }
    
    
    
    /** This method should help to get references to the DesignComponent. User should
     * take care of passing DesignComponent references to this class by overiding and
     * invoking this method where appropriate.
     * 
     * @param component - DesignComponent combined with this resource element.
     */
    public void setDesignComponent(DesignComponent component) {
    }
    

    public static class DesignComponentWrapper {

        private DesignComponent component;
        private boolean isDeleted;
        private long componentID;
        private TypeID typeID;
        private Map<String, PropertyValue> changesMap;

        public DesignComponentWrapper(DesignComponent component) {
            this(component.getComponentID(), component.getType());
            this.component = component;
        }

        public DesignComponentWrapper(long componentID, TypeID typeID) {
            this.componentID = componentID;
            this.typeID = typeID;
            changesMap = new HashMap<String, PropertyValue>();
        }

        public DesignComponent getComponent() {
            return component;
        }

        public void deleteComponent() {
            component = null;
            isDeleted = true;
        }

        public long getComponentID() {
            return componentID;
        }

        public TypeID getTypeID() {
            return typeID;
        }

        public void setChangeRecord(String propertyName, PropertyValue value) {
            changesMap.put(propertyName, value);
        }

        public Map<String, PropertyValue> getChanges() {
            return Collections.unmodifiableMap(changesMap);
        }

        public boolean hasChanges() {
            return changesMap.size() > 0 || component == null;
        }

        public boolean isDeleted() {
            return isDeleted;
        }
    }
    
    public static class StringComparator implements Comparator {
        public static final StringComparator instance = new StringComparator();
        
        private StringComparator() {
        }
        
        @SuppressWarnings("unchecked") // NOI18N
        public int compare(Object o1, Object o2) {
            if (o1 instanceof String && o2 instanceof String) {
                return ((String) o1).toLowerCase().compareTo(((String) o2).toLowerCase());
            }
            return ((Comparable) o1).compareTo((Comparable) o2);
        }
    }
}
