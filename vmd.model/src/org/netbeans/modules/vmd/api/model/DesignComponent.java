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
package org.netbeans.modules.vmd.api.model;

import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

import java.util.*;

/**
 * This class represents a component in a document. A component holds mainly its children, property values, and presenters.
 * <p>
 * A component can be created by a DesignDocument.createComponent method only. The component can be used only in the document
 * that created the component. Each component has its component id that is unique inside the document. The component
 * is a type id with component kind.
 * <p>
 * A component could be placed into tree hierarchy - each component has its parent component and its children.
 * <p>
 * A component is holding a map of property name/value pairs. When a component descriptor is assigned to the component
 * it automatically loads up a default property values. In case that the component already contains values
 * that are compatible with a new property descriptors, these values we stay unchanged. Otherwise they are overriden
 * by its default value.
 * <p>
 * A component has a set of presenter. Presenters are a little bit smarter listeners. They also can be assigned/held
 * by a component directly. Presenters are created each time a new component descriptor is assigned.
 *
 * @author David Kaspar
 */
public final class DesignComponent {
    
    // TODO - addComponent and removeComponent does not pass index of affected component into transaction/undoableedit
    // TODO - children could be implemented as a DesignComponent[] and the undoableedit could held old and new array.
    // HINT - no undo/redo while setComponentDescriptor - requires to check writeProperty while doing PropertyChangedUndoableEdit
    
    private final DesignDocument document;
    private final long componentID;
    private final TypeID type;
    private DesignComponent parentComponent;
    private final ArrayList<DesignComponent> children;
    private final HashMap<String, PropertyValue> properties;
    private final HashMap<String, PropertyValue> defaultProperties;
    
    private ComponentDescriptor componentDescriptor;
    private Lookup presenters;
    private PropertyValue referencePropertyValue;
    
    DesignComponent(DesignDocument document, long componentID, ComponentDescriptor componentDescriptor) {
        assert Debug.isFriend(DesignDocument.class, "createRawComponent"); // NOI18N
        this.document = document;
        this.componentID = componentID;
        this.type = componentDescriptor.getTypeDescriptor().getThisType();
        assert type.getKind() == TypeID.Kind.COMPONENT && type.getDimension() == 0;
        children = new ArrayList<DesignComponent> ();
        properties = new HashMap<String, PropertyValue> ();
        defaultProperties = new HashMap<String, PropertyValue> ();
        referencePropertyValue = PropertyValue.createComponentReferenceCore(this);
        presenters = Lookup.EMPTY;
        setComponentDescriptor(componentDescriptor, false);
    }
    
    /**
     * Returns a component descriptor.
     * @return the compatible descriptor, null if not assigned
     */
    public ComponentDescriptor getComponentDescriptor() {
        assert document.getTransactionManager().isAccess();
        return componentDescriptor;
    }
    
    void setComponentDescriptor(ComponentDescriptor componentDescriptor, boolean useUndo) {
        assert Debug.isFriend(DesignDocument.class, "updateDescriptorReferences")  ||  Debug.isFriend(DesignComponent.class, "<init>")  ||  Debug.isFriend (DesignDocument.class, "updateDescriptorReferencesCore"); // NOI18N
        if (this.componentDescriptor == componentDescriptor)
            return;
        
        this.componentDescriptor = componentDescriptor;
        
        if (componentDescriptor != null) {
            for (PropertyDescriptor propertyDescriptor : componentDescriptor.getPropertyDescriptors()) {
                String propertyName = propertyDescriptor.getName();
                PropertyValue propertyValue = properties.get(propertyName);
                if (propertyValue == null  ||  ! propertyValue.isCompatible(propertyDescriptor.getType())) {
                    propertyValue = propertyDescriptor.createDefaultValue(this, propertyName);
                    if (! propertyValue.isCompatible(propertyDescriptor))
                        Debug.warning("Default property value is not compatible", componentID, propertyName, propertyValue); // NOI18N
                    defaultProperties.put(propertyName, propertyValue);
                    if (properties.get(propertyName) != null)
                        writeProperty(propertyName, propertyValue);
                    else
                        properties.put(propertyName, propertyValue);
                }
            }
            
            Collection<? extends Presenter> presentersToRemove = this.presenters.lookupAll(Presenter.class);
            
            ArrayList<Presenter> temp = new ArrayList<Presenter> ();
            gatherPresenters(temp, componentDescriptor);
            PresentersProcessor.postProcessDescriptor (document.getDocumentInterface ().getProjectType (), document, componentDescriptor, temp);
            presenters = Lookups.fixed(temp.toArray());
            
            document.getTransactionManager().componentDescriptorChangeHappened(this, presentersToRemove, temp, useUndo);
        } else
            document.getTransactionManager().componentDescriptorChangeHappened(this, null, null, useUndo);
    }
    
    private void gatherPresenters(ArrayList<Presenter> tempPresenters, ComponentDescriptor componentDescriptor) {
        if (componentDescriptor == null)
            return;
        gatherPresenters(tempPresenters, componentDescriptor.getSuperDescriptor());
        componentDescriptor.gatherPresenters(tempPresenters);
    }
    
    /**
     * Returns a document where the component is living.
     * @return the document
     */
    public DesignDocument getDocument() {
        return document;
    }
    
    /**
     * Returns a component id.
     * @return the component id
     */
    public long getComponentID() {
        return componentID;
    }
    
    /**
     * Returns a component type id
     * @return the component type id
     */
    public TypeID getType() {
        return type;
    }
    
    /**
     * Returns a parent component of this component.
     * @return the parent component
     */
    public DesignComponent getParentComponent() {
        assert document.getTransactionManager().isAccess();
        return parentComponent;
    }
    
    /**
     * Adds a child component into this component.
     * @param component the child component
     */
    public void addComponent(DesignComponent component) {
        assert document.getTransactionManager().isWriteAccess();
        assert document == component.document;
        assert component.parentComponent == null;
        assert addComponentAssert (component);
        children.add(component);
        component.parentComponent = this;
        document.getTransactionManager().parentChangeHappened(null, this, component);
    }

    private boolean addComponentAssert (DesignComponent component) {
        DesignComponent parent = this;
        while (parent != null) {
            assert parent != component;
            parent = parent.getParentComponent();
        }
        return true;
    }

    /**
     * Removes a child component from this component.
     * @param component the child component
     */
    public void removeComponent(DesignComponent component) {
        assert document.getTransactionManager().isWriteAccess();
        assert document == component.document;
        assert component.parentComponent == this;
        if (! children.remove(component))
            throw Debug.error ("Component is not a child of its parent", "parent", this, "Child", component, "Children", children); // NOI18N
        component.parentComponent = null;
        document.getTransactionManager().parentChangeHappened(this, null, component);
    }

    /**
     * Removes the component from its parent component.
     */
    public void removeFromParentComponent () {
        assert document.getTransactionManager ().isWriteAccess ();
        if (parentComponent != null)
            parentComponent.removeComponent (this);
    }

    /**
     * Returns a collection of children components.
     * @return the collection of children
     */
    public Collection<DesignComponent> getComponents() {
        assert document.getTransactionManager().isAccess();
        return Collections.unmodifiableCollection(children);
    }
    
    /**
     * Returns a property value of a specified property
     * @param propertyName the property name
     * @return the property value
     */
    public PropertyValue readProperty(String propertyName) {
        assert document.getTransactionManager().isAccess();
        PropertyValue value = properties.get(propertyName);
        assert properties.containsKey(propertyName) : toString () + "." + propertyName + " property is missing"; //NOI18N
        assert value != null;
        return value;
    }
    
    /**
     * Writes a property value.
     * @param propertyName the property name
     * @param propertyValue the property value
     */
    public void writeProperty(String propertyName, PropertyValue propertyValue) {
        assert document.getTransactionManager().isWriteAccess();
        assert propertyValue != null : "Null property value"; // NOI18N
        assert componentDescriptor != null;
        
        PropertyValue oldValue = properties.get(propertyName);
        assert oldValue != null : "Missing old value in " + this + "." + propertyName; // NOI18N
        if (oldValue == propertyValue)
            return;
        
        PropertyDescriptor propertyDescriptor = componentDescriptor.getPropertyDescriptor(propertyName);
        assert propertyDescriptor != null : "Missing property descriptor in " + this + "." + propertyName; // NOI18N
        assert ! propertyDescriptor.isReadOnly() : "Cannot write read-only property " + this + "." + propertyName; // NOI18N // TODO - allow writing during deserialization
        assert propertyValue.isCompatible(propertyDescriptor);
        
        properties.put(propertyName, propertyValue);
        
        document.getTransactionManager().writePropertyHappened(this, propertyName, oldValue, propertyValue);
    }
    
    /**
     * Resolves whether a specified property has a default value as current value. 
     * Check is done on the PropertyValue object level. This method returns true only when DesignComponent
     * default PropertyValue == current PropertyValue for given propertyName. 
     * @param propertyName the property name
     * @return true if a current PropertyValue is the same PropertyValue object which has been created on the creation of the DesignComponent
     */
    public boolean isDefaultValue(String propertyName) {
        assert document.getTransactionManager().isAccess();
        assert propertyName != null;
        PropertyValue defaultValue = defaultProperties.get(propertyName);
        PropertyValue currentValue = properties.get(propertyName);
        assert defaultValue != null;
        assert currentValue != null;
        //This has to be changed in previous version, it should be resolved on the "equals" method level.
        return defaultValue == currentValue;
    }
    
    /**
     * Reset a specific property to its default value.
     * @param propertyName the property name
     */
    public void resetToDefault(String propertyName) {
        assert document.getTransactionManager().isWriteAccess ();
        assert propertyName != null;
        PropertyValue defaultValue = defaultProperties.get(propertyName);
        assert defaultValue != null;
        writeProperty(propertyName, defaultValue);
    }
    
    PropertyValue getReferenceValue() {
        return referencePropertyValue;
    }
    
    /**
     * Returns a presenter for a specific presenter id.
     * @param presenterClass the presenter class
     * @return the presenter
     */
    public <T extends Presenter> T getPresenter(Class<T> presenterClass) {
        assert document.getTransactionManager().isAccess();
        assert presenterClass != null;
        return presenters.lookup(presenterClass);
    }
    
    /**
     * Returns a list of presenters in a specific presenter category.
     * A presenter matches when the specified presenter category is a prefix of the id of the presenter.
     * @param presenterClass the presenter category class
     * @return a collection of presenters, non-null value
     */
    public <T> Collection<? extends T> getPresenters(Class<T> presenterClass) {
        assert document.getTransactionManager().isAccess();
        assert presenterClass != null;
        return presenters.lookupAll(presenterClass);
    }
    
    /**
     * Returns a component debug string.
     * @return the string
     */
    @Override
    public String toString() {
        return componentID + ":" + type; // NOI18N
    }
    
    void dumpComponent(String indent) {
        assert document.getTransactionManager().isAccess();
        
        System.out.println(indent + componentID + " : " + componentDescriptor); // NOI18N
        indent += "    "; // NOI18N
        
        HashSet<String> undefinedProperties = new HashSet<String> (properties.keySet());
        Collection<PropertyDescriptor> propertyDescriptors = componentDescriptor.getPropertyDescriptors();
        for (PropertyDescriptor property : propertyDescriptors) {
            String name = property.getName();
            System.out.println(indent + (properties.get(name) == defaultProperties.get(name) ? "## " : ":: ") + name + " = " + properties.get(name)); // NOI18N
            undefinedProperties.remove(name);
        }
        for (String name : undefinedProperties)
            System.out.println(indent + "?? " + name + " = " + properties.get(name)); // NOI18N
        for (Object presenter : presenters.lookupAll(Object.class))
            System.out.println(indent + ">>" + presenter); // NOI18N
        
        for (DesignComponent child : children)
            child.dumpComponent(indent);
    }

}
