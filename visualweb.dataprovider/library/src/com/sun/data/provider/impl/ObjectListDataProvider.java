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

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;
import com.sun.data.provider.DataProviderException;
import com.sun.data.provider.FieldKey;
import com.sun.data.provider.RowKey;
import com.sun.data.provider.TransactionalDataListener;
import com.sun.data.provider.TransactionalDataProvider;

/**
 * <p>This {@link com.sun.data.provider.TableDataProvider} wraps access to a
 * list of Java Objects.  The {@link FieldKey}s correspond to the JavaBean
 * properties and optionally the public member fields of the Java Object.</p>
 *
 * <p>This class implements {@link TransactionalDataProvider} semantics,
 * meaning that all updates to existing fields, as well as inserted and
 * deleted rows, are cached until <code>commitChanges()</code> is called.
 * Once that call is made, any <code>RowKey</code> you have retrieved from
 * this instance is invalid, and must be reacquired.</p>
 *
 * <p><strong>WARNING</strong> - Until you call <code>setList()</code> or
 * <code>setObjectType()</code> with a non-null parameter, or use a constructor
 * variant that accepts an non-null non-empty list, no information about field keys will
 * be available.  Therefore, any attempt to reference a <code>FieldKey</code>
 * or field identifier in a method call will throw
 * <code>IllegalArgumentException</code>.</p>
 *
 * <p>NOTE about Serializable:  This class wraps access to a list of any Java
 * Objects. The Objects can be swapped out using the <code>setObject(Object)</code>
 * method.  For this class to remain Serializable, the contained Objects must
 * also be Serializable.</p>
 *
 * @author Joe Nuxoll
 *         Winston Prakash (bug fixes)
 */
public class ObjectListDataProvider extends AbstractTableDataProvider
        implements Serializable, TransactionalDataProvider {
    
    // ------------------------------------------------------------ Constructors
    
    
    /**
     * <p>Construct a new ObjectListDataProvider with no known object type.  The
     * <code>setObjectType()</code> method can be used to set the object type.
     * If not set, the first added object will automatically define the object
     * type.</p>
     */
    public ObjectListDataProvider() {
        
        setObjectType(null);
        
    }
    
    
    /**
     * <p>Constructs a new ObjectListDataProvider wrapping the specified list.
     * </p>
     *
     * @param list List to be wrapped
     */
    public ObjectListDataProvider(List list) {
        
        setList(list);
        
    }
    
    
    /**
     * <p>Constructs a new ObjectListDataProvider wrapping the specified list
     * with the specified include fields flag.</p>
     *
     * @param list List to be wrapped
     * @param includeFields Desired include fields property setting
     */
    public ObjectListDataProvider(List list, boolean includeFields) {
        
        setList(list);
        setIncludeFields(includeFields);
        
    }
    
    
    /**
     * <p>Constructs a new ObjectListDataProvider for the specified object type.
     * </p>
     *
     * @param objectType Desired object type Class
     */
    public ObjectListDataProvider(Class objectType) {
        
        setObjectType(objectType);
        
    }
    
    
    /**
     * <p>Constructs a new ObjectListDataProvider for the specified object type
     * and <code>includeFields</code> property value.</p>
     *
     * @param objectType Desired object type Class
     * @param includeFields Desired include fields property setting
     */
    public ObjectListDataProvider(Class objectType, boolean includeFields) {
        
        setObjectType(objectType);
        setIncludeFields(includeFields);
        
    }
    
    
    // ------------------------------------------------------ Instance Variables
    
    
    /**
     * <p>List of object instances to be appended to the underlying
     * list when changes are committed.  The {@link RowKey} values
     * that correspond to these rows will have row index values starting
     * with the number of rows in the underlying list.  That is, if there
     * are five rows in the list already, the first appended row will have
     * an index of five, the second will have a row index of six, and
     * so on.</p>
     */
    private List appends = new ArrayList();
    
    
    /**
     * <p>Resource bundle containing our localized messages.</p>
     */
    private transient ResourceBundle bundle = null;
    
    
    /**
     * <p>Set of {@link RowKey}s of rows that have been marked to be
     * deleted.  An <code>Iterator</code> over this set will return
     * the corresponding {@link RowKey}s in ascending order.</p>
     */
    private Set deletes = new TreeSet();
    
    
    /**
     * <p>Storage for the <code>includeFields</code> property.  By default,
     * this is true.</p>
     */
    private boolean includeFields = true;
    
    
    /**
     * <p>Storage for the internal list of objects wrapped by this
     * data provider.</p>
     */
    private List list = new ArrayList();
    
    
    /**
     * <p>Storage for the object type contained in this data provider.</p>
     */
    private Class objectType;
    
    
    /**
     * <p>Map keyed by {@link RowKey}, whose elements are themselves
     * Maps keyed by {@link FieldKey} of each field for which an
     * updated value has been cached.</p>
     */
    private Map updates = new HashMap();
    
    
    /**
     * <p>Storage for the userResizable property (default value is
     * <code>true</true>).</p>
     */
    private boolean userResizable = true;
    
    // -------------------------------------------------------------- Private Static Variables
    
    /**
     * <p>The maximum number of rows that can be displayed at designtime.</p>
     */
    private static final int MAX_DESIGNTIME_ROWCOUNT = 25;
    
    /**
     * <p>When showing fake data, the number of rows to show.</p>
     */
    private static final int FAKE_DATA_ROWCOUNT = 3;
    
    
    // -------------------------------------------------------------- Properties
    
    
    /**
     * <p>Return the state of the <code>includeFields</code> property.</p>
     */
    public boolean isIncludeFields() {
        
        return this.includeFields;
        
    }
    
    
    /**
     * <p>Set the <code>includeFields</code> property.  This affects the set of
     * {@link FieldKey}s that this {@link com.sun.data.provider.DataProvider}
     * emits.  If the property is set to <code>true</code> (the default), then
     * public fields will be included in the list of available keys (intermixed
     * with the public properties).  Otherwise, only the public properties will
     * be available.</p>
     *
     * @param includeFields The new include fields value
     */
    public void setIncludeFields(boolean includeFields) {
        
        this.includeFields = includeFields;
        this.support = null;
        
    }
    
    
    /**
     * <code>Return the <code>List</code> that we are wrapping.</p>
     */
    public List getList() {
        
        return this.list;
        
    }
    
    
    /**
     * <p>Replace the <code>List</code> that we are wrapping.  In addition,
     * the <code>objectType</code> property will be reset based on the
     * class of the first element in the list (if any).  If the list is
     * empty, <code>objectType</code> will be set to <code>null</code>.
     *
     * @param list The new list to be wrapped
     */
    public void setList(List list) {
        this.list = list;
        if (list != null && list.size() > 0) {
            setObjectType(list.get(0).getClass());
        }
        else {
            setObjectType(null);
        }
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
        this.objectTypeConstructor = null;
        this.support = null;
        fireProviderChanged();
        
    }
    
    
    /**
     * <p>Return the current state of the userResizable property.  Note that
     * the wrapped list will not be actually resizable unless there is a
     * public no-args constructor on the <code>objectType</code> class.</p>
     */
    public boolean isUserResizable() {
        
        return this.userResizable;
        
    }
    
    
    /**
     * <p>Set the user resizable property.  If set to <code>true</code> (the
     * default), the resizability of this ObjectListDataProvider is based on
     * wether or not a public default constructor exists in the object type.
     * If the userResizable propert is set to <code>false</code>, then this
     * ObjectListDataProvider will not be resizable, regardless of the existence
     * of a public default constructor on the object type.</p>
     *
     * @param resizable <code>true</code> to make this ObjectListDataProvider
     *        resizable, pending the existence of a public default constructor
     *        on the contained object type, or <code>false</code> to make it
     *        non-resizable.
     * @see com.sun.data.provider.TableDataProvider#canInsertRow(RowKey beforeRow)
     */
    public void setUserResizable(boolean resizable) {
        
        this.userResizable = resizable;
        
    }
    
    
    // ---------------------------------------------------------- Public Methods
    
    
    /**
     * <p>Append the specified object to the list of contained objects.</p>
     *
     * @param object The Object to store in the list
     */
    public void addObject(Object object) {
        
        if (objectType == null) {
            setObjectType(object.getClass());
        }
        appendRow(object);
        
    }
    
    
    /**
     * <p>Add the specified object to the list of contained objects
     * at the specified row.</p>
     *
     * @param row The desired index for the new object
     * @param object The Object to store in the list
     */
    /* FIXME - inserts not currently supported
    public void addObject(RowKey row, Object object) {
     
        if (objectType == null) {
            setObjectType(object.getClass());
        }
        int index = ((IndexRowKey) row).getIndex();
        list.add(index, object);
        fireRowAdded(row);
        if (getCursorIndex() == index) {
            fireValueChanged(null, null, object);
        }
     
    }
     */
    
    
    /**
     * <p>Clear the list of contained objects.</p>
     */
    // FIXME - probably remove as being redundant
    public void clearObjectList() {
        
        list.clear();
        fireProviderChanged();
        
    }
    
    
    /**
     * <p>Returns the object stored at the specified row.</p>
     *
     * @param row The desired row to retrieve the contained object from
     */
    public Object getObject(RowKey row) {
        
        return list.get(getRowIndex(row));
        
    }
    
    
    /**
     * <p>Return the contained objects as an array.</p>
     */
    public Object[] getObjects() {
        
        return list.toArray(new Object[list.size()]);
        
    }
    
    
    /**
     * <p>Return <code>true</code> if the specified row has been
     * marked for removal on the next call to <code>commitChanges()</code>.</p>
     *
     * @param row The {@link RowKey} of the row to check
     */
    public boolean isRemoved(RowKey row) {
        
        return deletes.contains(row);
        
    }
    
    
    /**
     * <p>Remove the specified object from the list of contained objects.</p>
     *
     * @param object The Object to remove from the list
     */
    public void removeObject(Object object) {
        
        int index = list.indexOf(object);
        if (index > -1) {
            removeObject(getRowKey(index));
        }
        
    }
    
    
    /**
     * <p>Remove the object at the specified row from the list
     * of contained objects.</p>
     *
     * @param row The desired Object row to remove from the list
     */
    public void removeObject(RowKey row) {
        
        removeRow(row);
        
    }
    
    
    /**
     * <p>Replace the object at the specified row.</p>
     *
     * @param row The desired row to set the contained object
     * @param object The new object to set at the specified row
     */
    public void setObject(RowKey row, Object object) {
        
        Object previous = getObject(row);
        list.set(getRowIndex(row), object);
        fireValueChanged(null, row, previous, object);
        if (getCursorRow() == row) {
            fireValueChanged(null, previous, object);
        }
        
    }
    
    
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
        //at designtime, if there are no field keys
        //prevent ELExceptions from being thrown by showing zero rows
        if (java.beans.Beans.isDesignTime() && getFieldKeys().length < 1) {
            return 0;
        }
        
        //calculate how many rows currently exist in the wrapped data
        int currentRowCount = calculateRowCount();
        
        if (java.beans.Beans.isDesignTime()) {
            if (currentRowCount < 1) {
                //we have no rows to show
                //so show FAKE_DATA_ROWCOUNT rows of fake data
                return FAKE_DATA_ROWCOUNT;
            }
            else if (currentRowCount > MAX_DESIGNTIME_ROWCOUNT) {
                //we have too many rows to show
                //only show the maximum permitted
                return MAX_DESIGNTIME_ROWCOUNT;
            }
            else {
                return currentRowCount;
            }
        }
        else {
            return currentRowCount;
        }        
    }
    
    
    /** {@inheritDoc} */
    public Object getValue(FieldKey fieldKey, RowKey rowKey) throws DataProviderException {
        if(java.beans.Beans.isDesignTime()) {
            //calculate how many rows currently exist in the wrapped data
            int currentRowCount = calculateRowCount();
            if (currentRowCount < 1) {
                //we have no actual rows
                //so show fake data
                return AbstractDataProvider.getFakeData(getType(fieldKey));
            }
        }
        
        if ((getSupport() == null) || (getSupport().getFieldKey(fieldKey.getFieldId()) == null)) {
            throw new IllegalArgumentException(fieldKey.toString());
        }
        
        // Return pending update value (if any)
        Map fieldUpdates = (Map) updates.get(rowKey);
        if ((fieldUpdates != null) && (fieldUpdates.containsKey(fieldKey))) {
            return fieldUpdates.get(fieldKey);
        }
        
        // Otherwise, return the value from the underlying list
        if (!isRowAvailable(rowKey) && !deletes.contains(rowKey)) {
            throw new IndexOutOfBoundsException("" + rowKey);
        }
        
        int index = getRowIndex(rowKey);
        // getRowCount()  returns list.size()-deletes.size()
        // So index could be list.size()-1 (last index) - Winston
        //if (index < getRowCount()) {
        if (index < list.size()) {
            return getSupport().getValue(fieldKey, list.get(index));
        } else {
            return getSupport().getValue(fieldKey, appends.get(index - getRowCount()));
        }
    }
    
    
    /**
     * <p>Return <code>true</code> if the specified {@link RowKey} represents
     * a row in the original list, or a row that has been appended.  FIXME -
     * deal with {@link RowKey}s for inserted rows too, when inserts are
     * supported.</p>
     *
     * @param row {@link RowKey} to test for availability
     */
    public boolean isRowAvailable(RowKey row) throws DataProviderException {
        
        if (deletes.contains(row)) {
            return false;
        }
        if (row instanceof IndexRowKey) {
            //return (getRowCount() + appends.size()) > ((IndexRowKey) row).getIndex();
            // Bug Fix: 6348255 - Delete two rows including the last row, exception thrown
            return (list.size() + appends.size()) > ((IndexRowKey) row).getIndex();
        }
        return false;
    }
    
    
    /** {@inheritDoc} */
    public void setValue(FieldKey fieldKey, RowKey rowKey, Object value) throws DataProviderException {
        
        if ((getSupport() == null) || (getSupport().getFieldKey(fieldKey.getFieldId()) == null)) {
            throw new IllegalArgumentException(fieldKey.toString());
        }
        if (getSupport().isReadOnly(fieldKey)) {
            throw new IllegalStateException(fieldKey.toString() + " " + getBundle().getString("IS_READ_ONLY"));
        }
        if (!isRowAvailable(rowKey) && !deletes.contains(rowKey)) {
            throw new IndexOutOfBoundsException(rowKey.toString());
        }
        
        // Retrieve the previous value and determine if it has changed
        Object previous = getValue(fieldKey, rowKey);
        if (((previous == null) && (value == null)) ||
                ((previous != null) && (value != null) && previous.equals(value))) {
            return; // No change
        }
        
        // Verify type compatibility of the proposed new value
        if (!getSupport().isAssignable(fieldKey, value)) {
            throw new IllegalArgumentException(fieldKey + " = " + value); // NOI18N
        }
        
        // Record a pending change for this row and field
        Map fieldUpdates = (Map) updates.get(rowKey);
        if (fieldUpdates == null) {
            fieldUpdates = new HashMap();
            updates.put(rowKey, fieldUpdates);
        }
        fieldUpdates.put(fieldKey, value);
        fireValueChanged(fieldKey, rowKey, previous, value);
        fireValueChanged(fieldKey, previous, value);
        
    }
    
    
    
    // -------------------------------------- TableDataProvider Methods (Cursor)
    
    
    // Base class definitions are sufficient
    
    
    // ------------------------ TableDataProvider Methods (Append/Insert/Delete)
    
    
    /**
     * <p>Return true if the <code>userResizable</code> property is set to
     * <code>true</code>, and there is a public zero-args constructor for the
     * class specified by the <code>objectType</code> property.</p>
     *
     * {@inheritDoc}
     */
    public boolean canAppendRow() throws DataProviderException {
        
        if (!userResizable) {
            return false;
        }
        if (objectType != null) {
            return getObjectTypeConstructor() != null;
        }
        return false;
        
    }
    
    
    /**
     * <p>Construct a new instance of the specified object type and append it
     * to the end of the list.</p>
     *
     * {@inheritDoc}
     */
    public RowKey appendRow() throws DataProviderException {
        
        if (!canAppendRow()) {
            throw new IllegalStateException(getBundle().getString("OLDP_NOT_RESIZABLE"));
        }
        try {
            Constructor con = getObjectTypeConstructor();
            appends.add(con.newInstance(new Object[0]));
            RowKey rowKey = getRowKey(list.size() + appends.size() - 1);
            fireRowAdded(rowKey);
            return rowKey;
        } catch (Exception x) {
            throw new IllegalStateException(getBundle().getString("OLDP_NOT_RESIZABLE") + ":" + x.getMessage());
        }
        
    }
    
    
    /**
     * <p>Append the specified object to the end of the list.</p>
     *
     * @param object Object to be appended
     */
    public RowKey appendRow(Object object) throws DataProviderException {
        
        if (!userResizable) {
            throw new IllegalStateException(getBundle().getString("OLDP_NOT_RESIZABLE"));
        }
        appends.add(object);
        RowKey rowKey = getRowKey(list.size() + appends.size() - 1);
        fireRowAdded(rowKey);
        return rowKey;
        
    }
    
    
    /**
     * <p>Return true if the <code>userResizable</code> property is set to
     * <code>true</code>, and there is a public zero-args constructor for the
     * class specified by the <code>objectType</code> property.</p>
     *
     * {@inheritDoc}
     */
    public boolean canInsertRow(RowKey beforeRow) throws DataProviderException {
        
        // FIXME - inserts are not currently supported
        return false;
        
    }
    
    
    /**
     * <p>Construct a new instance of the specified object type and insert it
     * at the specified position in the list.</p>
     *
     * @param beforeRow Row before which to insert the new row
     *
     * {@inheritDoc}
     */
    public RowKey insertRow(RowKey beforeRow) throws DataProviderException {
        
        throw new UnsupportedOperationException();
        
        /*
         * FIXME - inserts are not currently supported, and when they
         * are supported will need to be cached until commit or revert.
        if (!canInsertRow(beforeRow)) {
            throw new IllegalStateException("This ObjectListDataProvider is not resizable.");
        }
        try {
            Constructor con = getObjectTypeConstructor();
            if (con != null) {
                Object o = con.newInstance(new Object[0]);
                if (o != null) {
                    addObject(beforeRow, o);
                    return beforeRow;
                }
            }
        } catch (Exception x) {
            throw new IllegalStateException("This ObjectListDataProvider is not resizable: " + x.getMessage());
        }
        return null;
         */
        
    }
    
    
    /**
     * <p>Return <code>true</code> if the <code>userResizable</code>
     * property is set to <code>true</code>.</p>
     *
     * {@inheritDoc}
     */
    public boolean canRemoveRow(RowKey row) throws DataProviderException {
        
        return userResizable;
        
    }
    
    
    /**
     * <p>Remove the object at the specified row from the list.</p>
     *
     * {@inheritDoc}
     */
    public void removeRow(RowKey row) throws DataProviderException {
        
        // Verify that we can actually remove this row
        if (!canRemoveRow(row)) {
            throw new IllegalStateException(getBundle().getString("OLDP_NOT_RESIZABLE")); // NOI18N
        }
        if (!isRowAvailable(row)) {
            throw new IllegalArgumentException(getBundle().getString("CAN_NOT_DELETE_ROW_KEY") + row); // NOI18N
        }
        
        // Record the fact that we are going to delete this row
        deletes.add(row);
        
        // Fire appropriate events regarding this deletion
        fireRowRemoved(row);
        if (getCursorRow() == row) {
            fireValueChanged(null, list.get(getRowIndex(row)), null);
        }
        
    }
    
    
    // --------------------------------------- TransactionalDataProvider Methods
    
    
    /**
     * <p>Cause any cached updates to existing field values, as well as
     * inserted and deleted rows, to be flowed through to the underlying
     * <code>List</code> wrapped by this
     * {@link com.sun.data.provider.DataProvider}.</p>
     */
    public void commitChanges() throws DataProviderException {
        
        // Commit all pending updates to the underlying list
        Iterator rowUpdates = updates.entrySet().iterator();
        while (rowUpdates.hasNext()) {
            Map.Entry rowUpdate = (Map.Entry) rowUpdates.next();
            RowKey rowKey = (RowKey) rowUpdate.getKey();
            int index = getRowIndex(rowKey);
            Object row = null;
            if (index < list.size()) {
                row = list.get(index);
            } else {
                row = appends.get(index - list.size());
            }
            Iterator fieldUpdates = ((Map) rowUpdate.getValue()).entrySet().iterator();
            while (fieldUpdates.hasNext()) {
                Map.Entry fieldUpdate = (Map.Entry) fieldUpdates.next();
                getSupport().setValue((FieldKey) fieldUpdate.getKey(), row, fieldUpdate.getValue());
            }
        }
        updates.clear();
        
        // Commit all pending deletes to the underlying list
        RowKey deletes[] = (RowKey[])
        this.deletes.toArray(new RowKey[this.deletes.size()]);
        for (int i = (deletes.length - 1); i >= 0; i--) {
            list.remove(getRowIndex(deletes[i]));
        }
        this.deletes.clear();
        
        // FIXME - inserts will need to be interwoven to avoid indexing errors
        
        // Commit all pending appends to the underlying list
        Iterator appendInstances = appends.iterator();
        while (appendInstances.hasNext()) {
            list.add(appendInstances.next());
        }
        appends.clear();
        
        // Notify interested listeners that we have committed
        fireChangesCommitted();
        
    }
    
    
    /** {@inheritDoc} */
    public void revertChanges() throws DataProviderException {
        
        // Erase any cached information about pending changes
        updates.clear();
        deletes.clear();
        appends.clear();
        
        // Notify interested listeners that we are reverting
        fireChangesReverted();
        
    }
    
    
    /** {@inheritDoc} */
    public void addTransactionalDataListener(TransactionalDataListener listener) {
        
        super.addDataListener(listener);
        
    }
    
    
    /** {@inheritDoc} */
    public TransactionalDataListener[] getTransactionalDataListeners() {
        
        if (dpListeners == null) {
            return new TransactionalDataListener[0];
        } else {
            ArrayList tdpList = new ArrayList();
            for (int i = 0; i < dpListeners.length; i++) {
                if (dpListeners[i] instanceof TransactionalDataListener) {
                    tdpList.add(dpListeners[i]);
                }
            }
            return (TransactionalDataListener[]) tdpList.toArray
                    (new TransactionalDataListener[tdpList.size()]);
        }
        
    }
    
    
    /** {@inheritDoc} */
    public void removeTransactionalDataListener(TransactionalDataListener listener) {
        
        super.removeDataListener(listener);
        
    }
    
    
    // --------------------------------------------------------- Private Methods
    
    
    /**
     * <p>Fire a <code>changesCommitted</code> method to all registered
     * listeners.</p>
     */
    private void fireChangesCommitted() {
        
        TransactionalDataListener listeners[] = getTransactionalDataListeners();
        for (int i = 0; i < listeners.length; i++) {
            listeners[i].changesCommitted(this);
        }
        
    }
    
    
    /**
     * <p>Fire a <code>changesReverted</code> method to all registered
     * listeners.</p>
     */
    private void fireChangesReverted() {
        
        TransactionalDataListener listeners[] = getTransactionalDataListeners();
        for (int i = 0; i < listeners.length; i++) {
            listeners[i].changesReverted(this);
        }
        
    }
    
    
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
     * <p>The zero-args public constructor for the <code>objectType</code>
     * class (lazily instantiated by getObjectTypeConstructor(), marked
     * as transient because Constructor instances are not Serializable).</p>
     */
    private transient Constructor objectTypeConstructor = null;
    
    
    /**
     * <p>Return the zero-arguments public constructor for the class
     * specified by the <code>objectType</code> property, if there is one.</p>
     */
    private Constructor getObjectTypeConstructor() {
        
        if (objectTypeConstructor != null) {
            return objectTypeConstructor;
        }
        try {
            Constructor con = objectType.getConstructor(new Class[0]);
            if ((con != null) && Modifier.isPublic(con.getModifiers())) {
                objectTypeConstructor = con;
            }
        } catch (NoSuchMethodException e) {
            objectTypeConstructor = null;
        }
        return objectTypeConstructor;
        
    }
    
    
    /**
     * <p>Return the row index corresponding to the specified row key.</p>
     *
     * @param rowKey Row key for which to extract an index
     */
    private int getRowIndex(RowKey rowKey) {
        
        return ((IndexRowKey) rowKey).getIndex();
        
    }
    
    
    /**
     * <p>Return a suitable {@link RowKey} for the specified row index.</p>
     */
    private RowKey getRowKey(int index) {
        
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
    
    /**
     * <p>Calculate how many rows exist in the wrapped data.</p>
     */ 
    private int calculateRowCount() {
        int currentRowCount = 0;
        if (list != null) {
            currentRowCount = list.size() - deletes.size();
        }
        return currentRowCount;
    }
}
