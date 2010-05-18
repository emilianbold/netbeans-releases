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
        assert name != null  &&  type != null  &&  (defaultValueFactory != null  ||  defaultValue != null) : "Name, type, defaultValueFactory or defaultValue could be null in PropertyDescriptor : " + this; // NOI18N
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
