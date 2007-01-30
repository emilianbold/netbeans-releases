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

/**
 * This immutable class represents a property descriptor used in component descriptor.
 * <p>
 * This class holds information about name, type id, default value or factory for default values, serializable and readonly ability.
 *
 * @author David Kaspar
 */
public final class PropertyDescriptor {

    private final String name;
    private final TypeID type;
    private final DefaultValueFactory defaultValueFactory;
    private final PropertyValue defaultValue;
    private final boolean allowNull;
    private final boolean allowUserCode;
    private final Versionable versionable;
    private final boolean useForSerialization;
    private final boolean readOnly;

    /**
     * Creates a new property descriptor using name, type id, and default value. It will be serializable and read-write by default.
     * @param name the property name
     * @param type the property type id
     * @param defaultValue the default property value
     * @param allowNull the property allow null values
     * @param allowUserCode the property allow user code values
     * @param versionable the property version
     */
    public PropertyDescriptor (String name, TypeID type, PropertyValue defaultValue, boolean allowNull, boolean allowUserCode, Versionable versionable) {
        this (name, type, defaultValue, allowNull, allowUserCode, versionable, true, false);
    }

    /**
     * Creates a new property descriptor using name, type id, default value, serializable and read-only ability.
     * @param name the property name
     * @param type the property type id
     * @param defaultValue the default property value
     * @param allowNull the property allow null values
     * @param allowUserCode the property allow user code values
     * @param versionable the property version
     * @param useForSerialization true if serializable
     * @param readonly true if the property is read-only
     */
    public PropertyDescriptor (String name, TypeID type, PropertyValue defaultValue, boolean allowNull, boolean allowUserCode, Versionable versionable, boolean useForSerialization, boolean readonly) {
        this (name, type, null, defaultValue, allowNull, allowUserCode, versionable, useForSerialization, readonly);
    }

    /**
     * Creates a new property descriptor using name, type id, factory for default values, serializable and read-only ability.
     * @param name the property name
     * @param type the property type id
     * @param defaultValueFactory the factory for default property values
     * @param allowNull the property allow null values
     * @param allowUserCode the property allow user code values
     * @param versionable the property version
     * @param useForSerialization true if serializable
     * @param readonly true if the property is read-only
     */
    public PropertyDescriptor (String name, TypeID type, DefaultValueFactory defaultValueFactory, boolean allowNull, boolean allowUserCode, Versionable versionable, boolean useForSerialization, boolean readonly) {
        this (name, type, defaultValueFactory, null, allowNull, allowUserCode, versionable, useForSerialization, readonly);
    }

    private PropertyDescriptor (String name, TypeID type, DefaultValueFactory defaultValueFactory, PropertyValue defaultValue, boolean allowNull, boolean allowUserCode, Versionable versionable, boolean useForSerialization, boolean readonly) {
        assert name != null  &&  type != null  &&  (defaultValueFactory != null  ||  defaultValue != null) : "Name, type, defaultValueFactory or defaultValue could be null in PropertyDescriptor : " + this; //NOI18N
        this.name = name;
        this.type = type;
        this.defaultValueFactory = defaultValueFactory;
        this.defaultValue = defaultValue;
        this.allowNull = allowNull;
        this.allowUserCode = allowUserCode;
        this.versionable = versionable;
        this.useForSerialization = useForSerialization;
        this.readOnly = readonly;
    }

    /**
     * Returns a property name.
     * @return the property name
     */
    public String getName () {
        return name;
    }

    /**
     * Returns a property type id.
     * @return the property type id
     */
    public TypeID getType () {
        return type;
    }

    /**
     * Returns a default value.
     * @return the default value, null if a default-value-factory is used
     */
    public PropertyValue getDefaultValue () {
        return defaultValue;
    }

    /**
     * Returns whether the property allows null values.
     * @return true, if allows null
     */
    public boolean isAllowNull () {
        return allowNull;
    }

    /**
     * Returns whether the property allows user code values.
     * @return true, if allows null
     */
    public boolean isAllowUserCode () {
        return allowUserCode;
    }

    /**
     * Returns a property version.
     * @return the property version
     */
    public Versionable getVersionable () {
        return versionable;
    }

    /**
     * Returns a state of serialization ability.
     * @return true if a property is serializable
     */
    public boolean isUseForSerialization () {
        return useForSerialization;
    }

    /**
     * Returns a state of read-only ability.
     * @return true if a property is read-only, false if a property is read-write
     */
    public boolean isReadOnly () {
        return readOnly;
    }

    /**
     * Creates a new default property value.
     * @param component the component where the default value should be stored
     * @param propertyName the property name of a property where the default value should be stored
     * @return the property value
     */
    public PropertyValue createDefaultValue (DesignComponent component, String propertyName) {
        return defaultValueFactory != null ? defaultValueFactory.createDefaultValue (component, propertyName) : defaultValue;
    }

    /**
     * This interface describes a factory of default values. Use this if you would like to create default values dynamically
     * based on the component and property name where the return default property value should be stored.
     */
    public interface DefaultValueFactory {

        /**
         * Create a property value that will be taken as a default value of a specified property name in a specified component.
         * <p>
         * Note: This method is called when a component is not fully initialized, therefore use only
         * component.getComponentID (), component.getType () and component.getComponentDescriptor () methods there.
         * @param component the component
         * @param propertyName the property name
         * @return the default property value
         */
        public PropertyValue createDefaultValue (DesignComponent component, String propertyName);

    }

}
