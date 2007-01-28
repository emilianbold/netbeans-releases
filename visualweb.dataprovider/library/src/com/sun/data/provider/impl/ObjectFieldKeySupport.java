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

package com.sun.data.provider.impl;

import com.sun.data.provider.DataProvider;
import com.sun.data.provider.FieldKey;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import com.sun.data.provider.DataProviderException;

/**
 * <p>Support class for {@link DataProvider} implementations that need to
 * instrospect Java classes to discover properties (and optionally public
 * fields) and return {@link FieldKey} instances for them.</p>
 */
public class ObjectFieldKeySupport {


    // ------------------------------------------------------------- Constructor


    /**
     * <p>Construct a new support instance wrapping the specified class,
     * with the specified flag for including public fields.</p>
     *
     * <p><strong>WARNING</strong> - Instances of this class will not be
     * <code>Serializable</code>, so callers should not attempt to save
     * fields containing such instances.</p>
     *
     * @param clazz Class whose properties should be exposed
     * @param includeFields Flag indicating whether public fields should
     *  also be included
     */
    public ObjectFieldKeySupport(Class clazz, boolean includeFields) {

        this.clazz = clazz;
        this.includeFields = includeFields;
        introspect();

    }


    // -------------------------------------------------------- Static Variables


    /**
     * <p>The empty argument list when calling property getters.</p>
     */
    private static final Object[] EMPTY = new Object[0];


    // ------------------------------------------------------ Instance Variables


    /**
     * <p>The class whose properties (and, optionally, fields) we are
     * exposing.</p>
     */
    private Class clazz = null;


    /**
     * <p>Map of {@link Field}s for fields, keyed by field name.  This
     * is only populated if <code>includeFields</code> is set to <code>true</code>.</p>
     */
    private Map fields = null;


    /**
     * <p>Flag indicating whether we should expose public fields as well as
     * properties as {@link FieldKey}s.</p>
     */
    private boolean includeFields = false;


    /**
     * <p>Map of all {@link FieldKey}s to be returned.</p>
     */
    private Map keys = null;


    /**
     * <p>Map of {@link PropertyDescriptor}s for properties, keyed by property name.</p>
     */
    private Map props = null;


    // ---------------------------------------------------------- Public Methods


    /**
     * <p>Return the {@link FieldKey} associated with the specified canonical
     * identifier, if any; otherwise, return <code>null</code>.</p>
     *
     * @param fieldId Canonical identifier of the required field
     */
    public FieldKey getFieldKey(String fieldId) throws DataProviderException {

        return (FieldKey) keys.get(fieldId);

    }


    /**
     * <p>Return an array of all supported {@link FieldKey}s.</p>
     */
    public FieldKey[] getFieldKeys() throws DataProviderException {

        return ((FieldKey[]) keys.values().toArray(new FieldKey[keys.size()]));

    }


    /**
     * <p>Return the type of the field associated with the specified
     * {@link FieldKey}, if it can be determined; otherwise, return
     * <code>null</code>.</p>
     *
     * @param fieldKey {@link FieldKey} to return the type for
     */
    public Class getType(FieldKey fieldKey) throws DataProviderException {

        PropertyDescriptor pd = (PropertyDescriptor) props.get(fieldKey.getFieldId());
        if (pd != null) {
            return pd.getPropertyType();
        }
        if (includeFields) {
            Field f = (Field) fields.get(fieldKey.getFieldId());
            if (f != null) {
                return f.getType();
            }
        }
        return null;

    }


    /**
     * <p>Return the value for the specified {@link FieldKey}, from the
     * specified base object.</p>
     *
     * @param fieldKey {@link FieldKey} for the requested field
     * @param base Base object to be used
     */
    public Object getValue(FieldKey fieldKey, Object base) throws DataProviderException {

        if (base == null) {
            return null;
        }

        PropertyDescriptor pd = (PropertyDescriptor) props.get(fieldKey.getFieldId());
        if (pd != null && pd.getReadMethod() != null) {
            try {
                return pd.getReadMethod().invoke(base, EMPTY);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        if (includeFields) {
            Field f = (Field) fields.get(fieldKey.getFieldId());
            if (f != null) {
                try {
                    return f.get(base);
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }
        }
        return null;

    }


    /**
     * <p>Return <code>true</code> if the specified value may be
     * successfully assigned to the specified field.</p>
     *
     * @param fieldKey {@link FieldKey} to check assignability for
     * @param value Proposed value
     */
    public boolean isAssignable(FieldKey fieldKey, Object value)
        throws DataProviderException {

        Class type = getType(fieldKey);
        if (value == null) {
            return !type.isPrimitive();
        }
        Class clazz = value.getClass();
        if (type.isAssignableFrom(clazz)) {
            return true;
        }
        if ((type.equals(Boolean.TYPE) && clazz.equals(Boolean.class)) ||
            (type.equals(Integer.TYPE) && clazz.equals(Integer.class)) ||
            (type.equals(Long.TYPE) && clazz.equals(Long.class)) ||
            (type.equals(Double.TYPE) && clazz.equals(Double.class)) ||
            (type.equals(Character.TYPE) && clazz.equals(Character.class)) ||
            (type.equals(Byte.TYPE) && clazz.equals(Byte.class)) ||
            (type.equals(Short.TYPE) && clazz.equals(Short.class)) ||
            (type.equals(Float.TYPE) && clazz.equals(Float.class))) {
            return true;
        } else {
            return false;
        }

    }


    /**
     * <p>Return the read only state of the field associated with the
     * specified {@link FieldKey}, if it can be determined, otherwise,
     * return <code>true</code>.</p>
     *
     * @param fieldKey {@link FieldKey} to return read only state for
     */
    public boolean isReadOnly(FieldKey fieldKey) throws DataProviderException {

        PropertyDescriptor pd =
          (PropertyDescriptor) props.get(fieldKey.getFieldId());
        if (pd != null) {
            return pd.getWriteMethod() == null;
        }
        if (includeFields) {
            Field f = (Field) fields.get(fieldKey.getFieldId());
            if (f != null) {
                return false; // All fields are writeable
            }
        }
        return true;

    }


    /**
     * <p>Set the value for the specified {@link FieldKey}, on the
     * specified base object.</p>
     *
     * @param fieldKey {@link FieldKey} for the requested field
     * @param base Base object to be used
     * @param value Value to be set
     *
     * @exception IllegalArgumentException if a type mismatch occurs
     * @exception IllegalStateException if setting a read only field
     *  is attempted
     */
    public void setValue(FieldKey fieldKey, Object base, Object value) throws DataProviderException {

        PropertyDescriptor pd =
          (PropertyDescriptor) props.get(fieldKey.getFieldId());
        if (pd != null) {
            if (pd.getWriteMethod() == null) {
                throw new IllegalStateException("" + fieldKey);
            }
            try {
                pd.getWriteMethod().invoke(base,
                                           new Object[] { value });
            } catch (IllegalArgumentException e) {
                throw e;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (includeFields) {
            Field f = (Field) fields.get(fieldKey.getFieldId());
            if (f != null) {
                try {
                    f.set(base, value);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }


    // --------------------------------------------------------- Private Methods


    /**
     * <p>Introspect the public properties (and optionally the public fields)
     * of the class we are wrapping.</p>
     */
    private void introspect() {

        props = new HashMap();
        if (includeFields) {
            fields = new HashMap();
        }

        // Introspect the properties and fields of the specified class
        try {
            BeanInfo bi = Introspector.getBeanInfo(clazz);
            PropertyDescriptor[] props = bi.getPropertyDescriptors();
            for (int i = 0; i < props.length; i++) {
                this.props.put(props[i].getName(), props[i]);
            }
            if (includeFields) {
                Field[] fields = clazz.getFields();
                for (int i = 0; i < fields.length; i++) {
                    if (((fields[i].getModifiers() & Modifier.PUBLIC) != 0) &&
                        !this.props.containsKey(fields[i].getName())) {
                        this.fields.put(fields[i].getName(), fields[i]);
                    }
                }
            }
        } catch (IntrospectionException ix) {
            ix.printStackTrace();
        }

        // Accumulate the set of all appropriate FieldKeys
        keys = new TreeMap();
        Iterator names = props.keySet().iterator();
        while (names.hasNext()) {
            String name = (String) names.next();
            keys.put(name, new FieldKey(name));
        }
        if (includeFields) {
            names = fields.keySet().iterator();
            while (names.hasNext()) {
                String name = (String) names.next();
                keys.put(name, new FieldKey(name));
            }
        }

    }

}
