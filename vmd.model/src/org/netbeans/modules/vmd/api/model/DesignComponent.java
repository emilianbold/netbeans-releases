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
    
    private DesignDocument document;
    private long componentID;
    private TypeID type;
    private DesignComponent parentComponent;
    private ArrayList<DesignComponent> children;
    private HashMap<String, PropertyValue> properties;
    private HashMap<String, PropertyValue> defaultProperties;
    
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
        assert Debug.isFriend(DesignDocument.class, "updateDescriptorReferences")  ||  Debug.isFriend(DesignComponent.class, "<init>"); // NOI18N
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
                        Debug.warning("Default property value is not compatible", componentID, propertyName, propertyValue);
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
        DesignComponent parent = this;
        while (parent != null) {
            assert parent != component;
            parent = parent.getParentComponent();
        }
        children.add(component);
        component.parentComponent = this;
        document.getTransactionManager().parentChangeHappened(null, this, component);
    }
    
    /**
     * Removes a child component from this component.
     * @param component the child component
     */
    public void removeComponent(DesignComponent component) {
        assert document.getTransactionManager().isWriteAccess();
        assert document == component.document;
        assert component.parentComponent == this;
        assert children.remove(component);
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
        if (! properties.containsKey(propertyName))
            throw new IllegalArgumentException ("DesignComponent (" + this + ") does not contain property: " + propertyName); //NOI18N
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
        assert propertyValue != null : "Null property value";
        assert componentDescriptor != null;
        
        PropertyValue oldValue = properties.get(propertyName);
        assert oldValue != null;
        if (oldValue == propertyValue)
            return;
        
        PropertyDescriptor propertyDescriptor = componentDescriptor.getPropertyDescriptor(propertyName);
        assert propertyDescriptor != null : "Missing property descriptor in " + this + " for " + propertyName;
        assert ! propertyDescriptor.isReadOnly() : "Read-only property"; // TODO - allow writing during deserialization
        assert propertyValue.isCompatible(propertyDescriptor);
        
        properties.put(propertyName, propertyValue);
        
        document.getTransactionManager().writePropertyHappened(this, propertyName, oldValue, propertyValue);
    }
    
    /**
     * Resolves whether a specified property has a default value as current value.
     * @param propertyName the property name
     * @return true if a current value is a default value
     */
    public boolean isDefaultValue(String propertyName) {
        assert document.getTransactionManager().isAccess();
        assert propertyName != null;
        PropertyValue defaultValue = defaultProperties.get(propertyName);
        PropertyValue currentValue = properties.get(propertyName);
        assert defaultValue != null;
        assert currentValue != null;
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
    public String toString() {
        return componentID + ":" + type;
    }
    
    void dumpComponent(String indent) {
        assert document.getTransactionManager().isAccess();
        
        System.out.println(indent + componentID + " : " + componentDescriptor);
        indent += "    ";
        
        HashSet<String> undefinedProperties = new HashSet<String> (properties.keySet());
        Collection<PropertyDescriptor> propertyDescriptors = componentDescriptor.getPropertyDescriptors();
        for (PropertyDescriptor property : propertyDescriptors) {
            String name = property.getName();
            System.out.println(indent + (properties.get(name) == defaultProperties.get(name) ? "## " : ":: ") + name + " = " + properties.get(name));
            undefinedProperties.remove(name);
        }
        for (String name : undefinedProperties)
            System.out.println(indent + "?? " + name + " = " + properties.get(name));
        for (Object presenter : presenters.lookupAll(Object.class))
            System.out.println(indent + ">>" + presenter);
        
        for (DesignComponent child : children)
            child.dumpComponent(indent);
    }

}
