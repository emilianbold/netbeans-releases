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


package com.sun.data.provider.impl;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import com.sun.data.provider.DataListener;
import com.sun.data.provider.DataProvider;
import com.sun.data.provider.DataProviderException;
import com.sun.data.provider.FieldKey;

/**
 * Abstract base implementation of {@link DataProvider}.  This class is a
 * convenient base class to use when creating a new {@link DataProvider}
 * implementation.
 *
 * @author Joe Nuxoll
 */
public abstract class AbstractDataProvider implements DataProvider, Serializable {

    // -------------------------------------------------------- Abstract Methods

    /** {@inheritDoc} */
    abstract public Class getType(FieldKey fieldKey) throws DataProviderException;

    /** {@inheritDoc} */
    abstract public boolean isReadOnly(FieldKey fieldKey) throws DataProviderException;

    /** {@inheritDoc} */
    abstract public Object getValue(FieldKey fieldKey) throws DataProviderException;

    /** {@inheritDoc} */
    abstract public void setValue(FieldKey fieldKey, Object value) throws DataProviderException;

    // ----------------------------------------------------- Convenience Methods

    public Class getType(String fieldId) throws DataProviderException {
        return getType(getFieldKey(fieldId));
    }

    public boolean isReadOnly(String fieldId) throws DataProviderException {
        return isReadOnly(getFieldKey(fieldId));
    }

    public Object getValue(String fieldId) throws DataProviderException {
        return getValue(getFieldKey(fieldId));
    }

    public void setValue(String fieldId, Object value) throws DataProviderException {
        setValue(getFieldKey(fieldId), value);
    }

    // -------------------------------------------------------- FieldKey Methods

    /**
     * storage for the list of {@link FieldKey}s in this {@link DataProvider}
     */
    protected FieldKey[] fieldKeys = FieldKey.EMPTY_ARRAY;

    /**
     * Adds a {@link FieldKey} to the list of keys
     * @param fieldKey FieldKey to add to the list of keys
     */
    protected void addFieldKey(FieldKey fieldKey) {
        if (fieldKeys == null) {
            fieldKeys = new FieldKey[1];
            fieldKeys[0] = fieldKey;
            return;
        }
        FieldKey[] results = new FieldKey[fieldKeys.length + 1];
        System.arraycopy(fieldKeys, 0, results, 0, fieldKeys.length);
        results[results.length - 1] = fieldKey;
        fieldKeys = results;
        fireProviderChanged();
    }

    /**
     * Adds a list of {@link FieldKey}s to the list of keys
     * @param fieldKeys FieldKey[] to add to the list of keys
     */
    protected void addFieldKeys(FieldKey[] fieldKeys) {
        for (int i = 0; i < fieldKeys.length; i++) {
            addFieldKey(fieldKeys[i]);
        }
        fireProviderChanged();
    }

    /**
     * Sorts the {@link FieldKey}s (using Arrays.sort)
     */
    protected void sortFieldKeys() {
        Arrays.sort(fieldKeys);
    }

    /**
     * Removes a {@link FieldKey} from the list of keys
     * @param fieldKey FieldKey to remove from the list
     */
    protected void removeFieldKey(FieldKey fieldKey) {
        if (fieldKeys == null) {
            return;
        }
        ArrayList list = new ArrayList(fieldKeys.length - 1);
        for (int i = 0; i < fieldKeys.length; i++) {
            if (fieldKeys[i] != fieldKey && !fieldKeys[i].equals(fieldKey)) {
                list.add(fieldKeys[i]);
            }
        }
        fieldKeys = (FieldKey[])list.toArray(new FieldKey[list.size() - 1]);
        fireProviderChanged();
    }

    /**
     * Removes an array of {@link FieldKey}s from the list
     * @param fieldKeys FieldKey[] to remove from the list
     */
    protected void removeFieldKeys(FieldKey[] fieldKeys) {
        for (int i = 0; i < fieldKeys.length; i++) {
            removeFieldKey(fieldKeys[i]);
        }
        fireProviderChanged();
    }

    /**
     * Empties the list of {@link FieldKey}s
     */
    protected void clearFieldKeys() {
        fieldKeys = FieldKey.EMPTY_ARRAY;
        fireProviderChanged();
    }

    /** {@inheritDoc} */
    public FieldKey[] getFieldKeys() throws DataProviderException {
        return fieldKeys;
    }

    /** {@inheritDoc} */
    public FieldKey getFieldKey(String fieldId) throws DataProviderException {
        for (int i = 0; fieldKeys != null && i < fieldKeys.length; i++) {
            FieldKey fk = fieldKeys[i];
            if (fieldId == fk.getFieldId() || fieldId.equals(fk.getFieldId())) {
                return fk;
            }
        }
        return null;
    }

    // ------------------------------------------------------- Fake Data Methods

    /**
     * This helper method defers to {@link #getFakeData(Class, Class)} passing
     * null as the collectionElementType.  This is used for design-time only
     * rendering of fake data.
     *
     * @param dataType The Class representing the type of data to fake
     * @return The fake data object
     */
    public static Object getFakeData(Class dataType) {
        return getFakeData(dataType, null);
    }

    /**
     * <p>This helper method produces fake data for use during design-time.
     * This allows a DataProvider implementation class to avoid a costly call
     * to a database, web service, or EJB method at design-time.  This will
     * handle array and collection types as well as object types.</p>
     *
     * <p>If the underlying type is other than a primitive type or java.lang
     * common type, it is constructed by attempting to use the default
     * constructor for the class, if it exists.  The method will then recurse
     * to populate the properties and fields of the nested object (avoiding
     * recursion).  If no default constructor exists, this method will return
     * null for the value.</p>
     *
     * @param dataType The Class representing the type of data to fake.  This
     *        might be a native array, Collection, or any Object subtype
     * @param collectionElementType If the 'dataType' is a Collection type, this
     *        parameter specifies the type of element stored in the Collection
     * @return The fake data object
     */
    public static Object getFakeData(Class dataType, Class collectionElementType) {
        return getFakeData(dataType, collectionElementType, new ArrayList());
    }

    private static Object getFakeData(
        Class dataType, Class collectionElementType, ArrayList recurseCheckClassList) {

        if (dataType == null || Void.TYPE == dataType) {
            return null;

        } else if (dataType.isArray()) {

            collectionElementType = dataType.getComponentType();
            Object[] result = new Object[3];
            for (int i = 0; i < result.length; i++) {
                if (!recurseCheckClassList.contains(collectionElementType)) {
                    recurseCheckClassList.add(collectionElementType);
                    result[i] = getFakeData(collectionElementType, null, recurseCheckClassList);
                    recurseCheckClassList.remove(collectionElementType);
                }
            }
            return result;

        } else if (Collection.class.isAssignableFrom(dataType)) {

            ArrayList result = new ArrayList();
            for (int i = 0; i < 3; i++) {
                if (!recurseCheckClassList.contains(collectionElementType)) {
                    recurseCheckClassList.add(collectionElementType);
                    result.add(getFakeData(collectionElementType, null, recurseCheckClassList));
                    recurseCheckClassList.remove(collectionElementType);
                } else {
                    result.add(null);
                }
            }
            return result;

        } else if (dataType.isPrimitive()) {
            if (Boolean.TYPE == dataType) {
                return Boolean.TRUE;
            } else if (Character.TYPE == dataType) {
                return new Character('a'); // NOI18N
            } else if (Byte.TYPE == dataType) {
                return new Byte((byte)123);
            } else if (Short.TYPE == dataType) {
                return new Short((short)123);
            } else if (Integer.TYPE == dataType) {
                return new Integer(123);
            } else if (Long.TYPE == dataType) {
                return new Long(123);
            } else if (Float.TYPE == dataType) {
                return new Float(123.45);
            } else if (Double.TYPE == dataType) {
                return new Double(123.45);
            }

        } else if (Boolean.class == dataType) {
            return Boolean.TRUE;

        } else if (java.util.Date.class == dataType) {
            return new java.util.Date();

        } else if (java.sql.Date.class == dataType) {
            return new java.sql.Date(System.currentTimeMillis());
            
        } else if (Calendar.class == dataType) {
            return new java.util.GregorianCalendar();
            
        } else if (BigDecimal.class == dataType) {
            return new BigDecimal(123);

        } else if (BigInteger.class == dataType) {
            return BigInteger.valueOf(123);

        } else if (Character.class == dataType) {
            return new Character('a'); // NOI18N

        } else if (Byte.class == dataType) {
            return new Byte((byte)123);

        } else if (Short.class == dataType) {
            return new Short((short)123);

        } else if (Integer.class == dataType) {
            return new Integer(123);

        } else if (Long.class == dataType) {
            return new Long(123);

        } else if (Float.class == dataType) {
            return new Float(123.45);

        } else if (Double.class == dataType) {
            return new Double(123.45);

        } else if (String.class == dataType) {
            return "abc"; // NOI18N

        } else {

            try {
                Constructor con = dataType.getConstructor(new Class[0]);
                if (con != null) {
                    Object o = con.newInstance(new Object[0]);
                    if (o != null) {
                        // attempt to populate the properties of the object
                        try {
                            BeanInfo bi = Introspector.getBeanInfo(o.getClass(),
                                Introspector.IGNORE_ALL_BEANINFO);
                            PropertyDescriptor[] props = bi.getPropertyDescriptors();
                            for (int i = 0; props != null && i < props.length; i++) {
                                if (props[i].getWriteMethod() != null && props[i].getReadMethod() != null) {
                                    Class propType = props[i].getPropertyType();
                                    if (!recurseCheckClassList.contains(propType)) {
                                        recurseCheckClassList.add(propType);
                                        Object data = getFakeData(propType, null, recurseCheckClassList);
                                        recurseCheckClassList.remove(propType);
                                        try {
                                            props[i].getWriteMethod().invoke(o, new Object[] { data });
                                        } catch (Exception x) {}
                                    }
                                }
                            }
                        } catch (Exception x) {}
                        // attempt to populate the public fields of the object
                        Field[] fields = o.getClass().getFields();
                        for (int i = 0; fields != null && i < fields.length; i++) {
                            if (Modifier.isPublic(fields[i].getModifiers())) {
                                Class fieldType = fields[i].getType();
                                if (!recurseCheckClassList.contains(fieldType)) {
                                    recurseCheckClassList.add(fieldType);
                                    Object data = getFakeData(fieldType, null,
                                        recurseCheckClassList);
                                    recurseCheckClassList.remove(fieldType);
                                    try {
                                        fields[i].set(o, data);
                                    } catch (Exception x) {}
                                }
                            }
                        }
                    }
                    return o;
                }
            } catch (Exception x) {
                return null;
            }
        }
        return null;
    }

    // ----------------------------------------------------------- Event Methods

    /**
     * <p>Array of {@link DataListener} instances registered for
     * this {@link DataProvider}.</p>
     */
    protected DataListener dpListeners[] = null;

    /** {@inheritDoc} */
    public void addDataListener(DataListener listener) {
        if (dpListeners == null) {
            dpListeners = new DataListener[1];
            dpListeners[0] = listener;
            return;
        }
        DataListener results[] = new DataListener[dpListeners.length + 1];
        System.arraycopy(dpListeners, 0, results, 0, dpListeners.length);
        results[results.length - 1] = listener;
        dpListeners = results;
    }

    /** {@inheritDoc} */
    public void removeDataListener(DataListener listener) {
        if (dpListeners == null) {
            return;
        }
        ArrayList list = new ArrayList(dpListeners.length - 1);
        for (int i = 0; i < dpListeners.length; i++) {
            if (dpListeners[i] != listener) {
                list.add(dpListeners[i]);
            }
        }
        if (list.size() < 1) {
            dpListeners = null;
        } else {
            dpListeners =
              (DataListener[]) list.toArray(new DataListener[list.size()]);
        }
    }

    /** {@inheritDoc} */
    public DataListener[] getDataListeners() {
        if (dpListeners == null) {
            return new DataListener[0];
        } else {
            return dpListeners;
        }
    }

    /**
     * Fires a valueChanged event to each registered {@link DataListener}
     *
     * @param fieldKey FieldKey identifying the changed value
     * @param oldValue The old value (before the change)
     * @param newValue The new value (after the change)
     * @see DataListener#valueChanged(DataProvider, FieldKey, Object, Object)
     */
    protected void fireValueChanged(FieldKey fieldKey, Object oldValue, Object newValue) {
        DataListener[] dpls = getDataListeners();
        for (int i = 0; i < dpls.length; i++) {
            dpls[i].valueChanged(this, fieldKey, oldValue, newValue);
        }
    }

    /**
     * Fires a providerChanged event to each registered {@link DataListener}
     *
     * @see DataListener#providerChanged(DataProvider)
     */
    protected void fireProviderChanged() {
        DataListener[] dpls = getDataListeners();
        for (int i = 0; i < dpls.length; i++) {
            dpls[i].providerChanged(this);
        }
    }
}
