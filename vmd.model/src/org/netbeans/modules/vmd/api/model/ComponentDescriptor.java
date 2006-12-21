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

import java.util.*;

/**
 * This class represents a component descriptor. It is registered in the component descriptor registry and then available
 * for using in document. Component itself is just a data container and does not know anything about these data.
 * On the other hand a component descriptor does not contain data but describes types of these data.
 *
 * @author David Kaspar
 */
public abstract class ComponentDescriptor {

    private Collection<PropertyDescriptor> propertyDescriptors;
    private ComponentDescriptor superDescriptor;

    /**
     * Returns a super descriptor.
     * <p>
     * Note: This is available when a component is registered by registry.
     * @return the super descriptor.
     */
    public final ComponentDescriptor getSuperDescriptor () {
        return superDescriptor;
    }

    /**
     * Returns a full collection of all property descriptors.
     * <p>
     * Note: This is available when a component is registered by registry.
     * @return the collection of property descriptors.
     */
    public final Collection<PropertyDescriptor> getPropertyDescriptors () {
        return propertyDescriptors;
    }

    /**
     * Returns a property descriptor for a property.
     * @param propertyName the property name
     * @return the property descriptor
     */
    public final PropertyDescriptor getPropertyDescriptor (String propertyName) {
        // HINT - optimize to HashMap
        for (PropertyDescriptor propertyDescriptor : getPropertyDescriptors ()) {
            if (propertyName.equals (propertyDescriptor.getName ()))
                return propertyDescriptor;
        }
        return null;
    }

    final void setSuperComponentDescriptor (ComponentDescriptor superDescriptor) {
        assert Debug.isFriend (GlobalDescriptorRegistry.class, "resolveDescriptor") || Debug.isFriend (GlobalDescriptorRegistry.class, "reloadCore") || Debug.isFriend (DescriptorRegistry.class, "reloadCore"); // NOI18N
        this.superDescriptor = superDescriptor;
    }

    final void setPropertyDescriptors (Collection<PropertyDescriptor> propertyDescriptors) {
        assert Debug.isFriend (GlobalDescriptorRegistry.class, "resolveDescriptor")  ||  Debug.isFriend (GlobalDescriptorRegistry.class, "reloadCore")  ||  Debug.isFriend (DescriptorRegistry.class, "reloadCore"); // NOI18N
        this.propertyDescriptors = propertyDescriptors == null ? null : Collections.unmodifiableCollection (new ArrayList<PropertyDescriptor> (propertyDescriptors));
    }

    /**
     * Returns a debug string.
     * @return the string
     */
    public final String toString () {
        TypeDescriptor typeDescriptor = getTypeDescriptor ();
        if (typeDescriptor == null)
            return super.toString ();
        return typeDescriptor.getThisType ().toString ();
    }

    /**
     * Post-initialize created component.
     * @param component the created component
     */
    public void postInitialize (DesignComponent component) {
    }

    /**
     * Returns a type descriptor of this component descriptor.
     * @return the type descriptor
     */
    public abstract TypeDescriptor getTypeDescriptor ();

    /**
     * Returns a version descriptor of this component descriptor.
     * @return the version descriptor
     */
    public abstract VersionDescriptor getVersionDescriptor ();

    /**
     * Returns a collection of property names that should be masked/hidden from a collection of property descriptors
     * that are inherited from the super component descriptor.
     * @return the collection of property names
     */
    public Collection<String> getExcludedPropertyDescriptorNames () {
        return null;
    }

    /**
     * Returns a list of property descriptors that are newly declared in the component descriptor or override property descriptors from the super component descriptor.
     * @return the list of property descriptors
     */
    public abstract List<PropertyDescriptor> getDeclaredPropertyDescriptors ();

    /**
     * Returns a palette descriptor.
     * @return the palette descriptor; if non-null, then default ComponentProducer will be created
     */
    // TODO - obsolete - remove this method
    public PaletteDescriptor getPaletteDescriptor () {
        return null;
    }

    /**
     * Gather presenters. Override this method when you need to excluded or filter the list of presenter created by super component descriptors.
     * @param presenters a list of presenters created by super component descriptors
     */
    protected void gatherPresenters (ArrayList<Presenter> presenters) {
        List<? extends Presenter> array = createPresenters ();
        if (array != null)
            for (Presenter presenter : array) {
                if (presenter != null)
                    presenters.add (presenter);
                else
                    Debug.warning ("Null presenter", this);
            }
    }

    /**
     * Returns a list of new instances of presenters that are declared in the component descriptor or override presenters
     * from the super component descriptor.
     * @return the list of presenters
     */
    protected abstract List<? extends Presenter> createPresenters ();

}
