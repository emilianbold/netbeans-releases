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
package org.netbeans.modules.vmd.midp.propertyeditors.api.resource.element;

import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.model.TypeID;
import org.netbeans.modules.vmd.midp.components.general.ClassCode;
import org.netbeans.modules.vmd.midp.components.resources.ResourceCD;
import javax.swing.*;
import java.util.*;

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

    public static boolean isPropertyValueAUserCodeType(PropertyValue propertyValue) {
        return propertyValue != null && propertyValue.getKind() == PropertyValue.Kind.USERCODE;
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
