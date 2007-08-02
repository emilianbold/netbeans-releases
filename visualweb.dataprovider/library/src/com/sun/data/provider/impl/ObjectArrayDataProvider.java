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
import com.sun.data.provider.DataProviderException;
import com.sun.data.provider.FieldKey;
import com.sun.data.provider.RowKey;
import com.sun.data.provider.TableDataProvider;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TreeMap;

/**
 * <p>This {@link TableDataProvider} wraps access to an
 * array of Java Objects.  The {@link FieldKey}s correspond to the JavaBean
 * properties and optionally the public member fields of the Java Objects.</p>
 *
 * <p>Note that this {@link TableDataProvider} determines which fields are
 * available by examining the underlying component data type of the array.
 * If you pass in an array that is of type <code>Object[]</code>, then, perhaps
 * with initialization code like this:</p>
 * <pre>
 *   Map map = ...;
 *   return new ObjectArrayDataProvider(map.values().toArray());
 * </pre>
 * <p>the fields of your actual object type will not be available.  If you
 * know that your data is all of type Foo, do this instead:</p>
 * <pre>
 *   Map map = ...;
 *   return new ObjectArrayDataProvider
 *     ((Foo[]) map.values().toArray(new Foo[0]));
 * </pre>
 *
 * <p>Since this {@link TableDataProvider} wraps an array, and arrays in Java
 * are not intrinsically resizeable, this implementation will return
 * <code>false</code> for any call to <code>canAppendRow()</code>,
 * <code>canInsertRow()</code>, or <code>canRemoveRow()</code>.  It will
 * throw <code>UnsupportedOperationException</code> if you attempt to call
 * <code>appendRow()</code>, <code>insertRow()</code>, or
 * <code>removeRow()</code>.</p>
 *
 * <p><strong>WARNING</strong> - Until you call <code>setArray()</code> or
 * <code>setObjectType()</code> with a non-null parameter, or use a constructor
 * variant that accepts an non-null array, no information about field keys will
 * be available.  Therefore, any attempt to reference a <code>FieldKey</code>
 * or field identifier in a method call will throw
 * <code>IllegalArgumentException</code>.</p>
 *
 * <p>NOTE about Serializable:  This class wraps access to an array of any Java
 * Objects.  For this class to remain Serializable, the contained Objects must
 * also be Serializable.</p>
 *
 * @author Joe Nuxoll
 *         Winston Prakash (bug fixes)
 */
public class ObjectArrayDataProvider extends AbstractTableDataProvider
        implements Serializable {
    
    
    // ------------------------------------------------------------ Constructors
    
    
    /**
     * <p>Construct a new ObjectArrayDataProvider with no known data.  The
     * <code>setArray()</code> method can be used to set the contained array.</p>
     */
    public ObjectArrayDataProvider() {
        
        setArray(null);
        
    }
    
    
    /**
     * <p>Constructs a new ObjectArraytDataProvider wrapping the specified
     * array.</p>
     *
     * @param array Array to be wrapped
     */
    public ObjectArrayDataProvider(Object array[]) {
        
        setArray(array);
        
    }
    
    
    /**
     * <p>Constructs a new ObjectArraytDataProvider wrapping the specified
     * array and value of the <code>includeFields</code> property.</p>
     *
     * @param array Array to be wrapped
     * @param includeFields Desired includeFields property
     */
    public ObjectArrayDataProvider(Object array[], boolean includeFields) {
        
        setArray(array);
        setIncludeFields(includeFields);
        
    }
    
    
    
    // ------------------------------------------------------ Instance Variables
    
    
    /**
     * <p>Storage for the array being wrapped by this data provider.</p>
     */
    private Object array[] = null;
    
    
    /**
     * <p>Resource bundle containing our localized messages.</p>
     */
    private transient ResourceBundle bundle = null;
    
    
    /**
     * <p>Storage for the <code>includeFields</code> property.  By default,
     * this is true.</p>
     */
    private boolean includeFields = true;
    
    
    /**
     * <p>Storage for the object type contained in this data provider.</p>
     */
    private Class objectType;
    
    
    // -------------------------------------------------------------- Properties
    
    
    /**
     * <code>Return the array that we are wrapping.</p>
     */
    public Object[] getArray() {
        
        return this.array;
        
    }
    
    
    /**
     * <p>Replace the array that we are wrapping.  In addition,
     * the <code>objectType</code> property will be reset based on the
     * class of the underlying element type.</p>
     *
     * @param array The new array to be wrapped
     */
    public void setArray(Object array[]) {
        
        this.array = array;
        if (array != null) {
            objectType = array.getClass().getComponentType();
        } else {
            objectType = null;
        }
        this.support = null;
        fireProviderChanged();
        
    }
    
    /**
     * <p>Return the object type that this data provider contains.  This
     * determines the list of {@link FieldKey}s that this provider supplies.</p>
     */
    public Class getObjectType() {
        
        return objectType;
        
    }
    
    
    /**
     * <p>Set the object type contained in this ObjectListDataProvider.  This
     * type determines the list of public properties and fields to expose as
     * {@link FieldKey}s.  If no object type is specified, the first added
     * object's class will be used as the object type.</p>
     *
     * @param objectType The desired Class type to be contained in this
     *        ObjectDataProvider
     */
    public void setObjectType(Class objectType) {
        
        this.objectType = objectType;
        this.support = null;
        fireProviderChanged();
        
    }
    
    
    /**
     * <p>Return the state of the <code>includeFields</code> property.</p>
     */
    public boolean isIncludeFields() {
        
        return this.includeFields;
        
    }
    
    
    /**
     * <p>Set the <code>includeFields</code> property.  This affects the set of
     * {@link FieldKey}s that this {@link DataProvider} emits.  If the
     * property is set to <code>true</code> (the default), then public fields
     * will be included in the list of available keys (intermixed with the
     * public properties).  Otherwise, only the public properties will be
     * available.</p>
     *
     * @param includeFields The new include fields value
     */
    public void setIncludeFields(boolean includeFields) {
        
        this.includeFields = includeFields;
        this.support = null;
        
    }
    
    
    // ---------------------------------------------------------- Public Methods
    
    
    // ---------------------------------------------------- DataProvider Methods
    
    
    /** {@inheritDoc} */
    public FieldKey getFieldKey(String fieldId) throws DataProviderException {
        FieldKey fieldKey = null;
        if (getSupport() != null) {
            fieldKey = getSupport().getFieldKey(fieldId);
        }
        if (fieldKey != null){
            return fieldKey;
        } else{
            throw new IllegalArgumentException(fieldId);
        }
    }
    
    
    /** {@inheritDoc} */
    public FieldKey[] getFieldKeys() throws DataProviderException {
        
        if (getSupport() != null) {
            return getSupport().getFieldKeys();
        }
        return FieldKey.EMPTY_ARRAY;
        
    }
    
    
    /** {@inheritDoc} */
    public Class getType(FieldKey fieldKey) throws DataProviderException {
        if ((getSupport() == null) || (getSupport().getFieldKey(fieldKey.getFieldId()) == null)) {
            throw new IllegalArgumentException(fieldKey.toString());
        }else{
            return getSupport().getType(fieldKey);
        }
    }
    
    
    /** {@inheritDoc} */
    public Object getValue(FieldKey fieldKey) throws DataProviderException {
        
        return getValue(fieldKey, getCursorRow());
        
    }
    
    
    /** {@inheritDoc} */
    public void setValue(FieldKey fieldKey, Object value) throws DataProviderException {
        
        setValue(fieldKey, getCursorRow(), value);
        
    }
    
    
    /** {@inheritDoc} */
    public boolean isReadOnly(FieldKey fieldKey) throws DataProviderException {
        if ((getSupport() == null) || (getSupport().getFieldKey(fieldKey.getFieldId()) == null)) {
            throw new IllegalArgumentException(fieldKey.toString());
        }else{
            return getSupport().isReadOnly(fieldKey);
        }
    }
    
    
    // --------------------------------------- TableDataProvider Methods (Basic)
    
    
    /** {@inheritDoc} */
    public int getRowCount() throws DataProviderException {
        if( java.beans.Beans.isDesignTime()) {
            return 3;
        }
        
        if (array == null) {
            return 0;
        } else {
            return array.length;
        }
        
    }
    
    
    /** {@inheritDoc} */
    public Object getValue(FieldKey fieldKey, RowKey rowKey) throws DataProviderException {
        
        if( java.beans.Beans.isDesignTime()) {
            return AbstractDataProvider.getFakeData(getType(fieldKey));
        }
        
        if ((getSupport() == null) || (getSupport().getFieldKey(fieldKey.getFieldId()) == null)) {
            throw new IllegalArgumentException(fieldKey.toString());
        }
        
        if (!isRowAvailable(rowKey)) {
            throw new IndexOutOfBoundsException("" + rowKey);
        }
        
        return getSupport().getValue(fieldKey, array[getRowIndex(rowKey)]);
    }
    
    
    /** {@inheritDoc} */
    public void setValue(FieldKey fieldKey, RowKey rowKey, Object value) throws DataProviderException {
        
        if ((getSupport() == null) || (getSupport().getFieldKey(fieldKey.getFieldId()) == null)) {
            throw new IllegalArgumentException(fieldKey.toString());
        }
        if (getSupport().isReadOnly(fieldKey)) {
            throw new IllegalStateException(fieldKey.toString() + " " + getBundle().getString("IS_READ_ONLY"));
        }
        if (!isRowAvailable(rowKey)) {
            throw new IndexOutOfBoundsException(rowKey.toString());
        }
        Object previous = getSupport().getValue(fieldKey, array[getRowIndex(rowKey)]);
        getSupport().setValue(fieldKey, array[getRowIndex(rowKey)], value);
        if (((previous == null) && (value != null)) ||
                ((previous != null) && (value == null)) ||
                ((previous != null) && (value != null) && !previous.equals(value))) {
            fireValueChanged(fieldKey, rowKey, previous, value);
            fireValueChanged(fieldKey, previous, value);
        }
    }
    
    // -------------------------------------- TableDataProvider Methods (Cursor)
    
    
    // Base class definitions are sufficient
    
    
    // ------------------------ TableDataProvider Methods (Append/Insert/Delete)
    
    
    /**
     * {@inheritDoc}
     */
    public boolean canAppendRow() throws DataProviderException {
        
        return false;
        
    }
    
    
    /**
     * {@inheritDoc}
     */
    public RowKey appendRow() throws DataProviderException {
        
        throw new UnsupportedOperationException();
        
    }
    
    
    /**
     * {@inheritDoc}
     */
    public boolean canInsertRow(RowKey beforeRow) throws DataProviderException {
        
        return false;
        
    }
    
    
    /**
     * {@inheritDoc}
     */
    public RowKey insertRow(RowKey beforeRow) throws DataProviderException {
        
        throw new UnsupportedOperationException();
        
    }
    
    
    /**
     * {@inheritDoc}
     */
    public boolean canRemoveRow(RowKey row) throws DataProviderException {
        
        return false;
        
    }
    
    
    /**
     * <p>Remove the object at the specified row from the list.</p>
     *
     * {@inheritDoc}
     */
    public void removeRow(RowKey row) throws DataProviderException {
        
        throw new UnsupportedOperationException();
        
    }
    
    
    // --------------------------------------------------------- Private Methods
    
    
    /**
     * <p>Return the resource bundle containing our localized messages.</p>
     */
    private ResourceBundle getBundle() {
        
        if (bundle == null) {
            bundle = ResourceBundle.getBundle("com/sun/data/provider/impl/Bundle");
        }
        return bundle;
        
    }
    
    
    /**
     * <p>Return the row index corresponding to the specified row key.</p>
     *
     * @param rowKey Row key for which to extract an index
     */
    private int getRowIndex(RowKey rowKey) throws DataProviderException {
        
        return ((IndexRowKey) rowKey).getIndex();
        
    }
    
    
    /**
     * <p>Return a suitable {@link RowKey} for the specified row index.</p>
     */
    private RowKey getRowKey(int index) throws DataProviderException {
        
        return new IndexRowKey(index);
        
    }
    
    
    /**
     * <p>The cached support object for field key manipulation.  Must be
     * transient because its content is not Serializable.</p>
     */
    private transient ObjectFieldKeySupport support = null;
    
    
    /**
     * <p>Return the {@link ObjectFieldKeySupport} instance for the
     * object class we are wrapping.</p>
     */
    private ObjectFieldKeySupport getSupport() {
        
        if ((support == null) && (objectType != null)) {
            support = new ObjectFieldKeySupport(objectType, includeFields);
        }
        return support;
        
    }
    
    
}
