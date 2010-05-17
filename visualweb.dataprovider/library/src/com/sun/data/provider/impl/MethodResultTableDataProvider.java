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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import com.sun.data.provider.DataListener;
import com.sun.data.provider.FieldKey;
import com.sun.data.provider.RefreshableDataListener;
import com.sun.data.provider.RefreshableDataProvider;
import com.sun.data.provider.RowKey;
import com.sun.data.provider.TableCursorListener;
import com.sun.data.provider.TableCursorVetoException;
import com.sun.data.provider.TableDataListener;
import com.sun.data.provider.TableDataProvider;
import com.sun.data.provider.DataProviderException;

/**
 * <p>A TableDataProvider implementation to wrap the return value from a method.
 * Set the <code>dataClassInstance</code>, <code>dataMethod</code>, and
 * <code>dataMethodArguments</code> properties to point to a method on a class
 * instance.  The result from that method call will be wrapped as a
 * TableDataProvider, with a row for each element in the returned array or
 * collection.  If there is only a single return value, it will be a single row
 * table.</p>
 *
 * @author cao, Joe Nuxoll
 */
public class MethodResultTableDataProvider
    implements TableDataProvider, RefreshableDataProvider, Serializable {

    /**
     * Constructs a new MethodResultTableDataProvider with no dataClassInstance
     * or dataMethod specified.
     */
    public MethodResultTableDataProvider() {}

    /**
     * Constructs a new MethodResultTableDataProvider using the specified
     * dataClassInstance and dataMethod.
     *
     * @param dataClassInstance The class instance where the method is invoked
     * @param dataMethod The method where the data is from
     */
    public MethodResultTableDataProvider(Object dataClassInstance, Method dataMethod) {
        this();
        setDataClassInstance(dataClassInstance);
        setDataMethod(dataMethod);
    }

    /**
     * Returns the dataClassInstance that contains the dataMethod to be invoked.
     *
     * @return Object
     */
    public Object getDataClassInstance() {
        return dataClassInstance;
    }

    /**
     * Sets the dataClassInstance that contains the dataMethod to be invoked.
     *
     * @param instance Object
     */
    public void setDataClassInstance(Object instance) {
        this.dataClassInstance = instance;
        resultProvider.clearObjectList();
    }

    /**
     * Returns the currently set dataMethod
     *
     * @return Method
     */
    public Method getDataMethod() {
        return dataMethod;
    }

    /**
     * Sets the dataMethod that will be invoked
     *
     * @param method Method
     */
    public void setDataMethod(Method method) {
        this.dataMethod = method;
        resultProvider.clearObjectList();
        refreshFieldKeys();
    }

    /**
     * If the dataMethod returns a <code>Collection</code> type, this property
     * will be used to determine the appropriate FieldKeys for the elements in
     * the collection type.
     *
     * @param elementType Class
     */
    public void setCollectionElementType(Class elementType) {
        this.collectionElementType = elementType;
        refreshFieldKeys();
    }

    public Class getCollectionElementType() {
        return collectionElementType;
    }

    /**
     * Read-only access to the result object from the invocation of the
     * dataMethod
     *
     * @return Object
     */
    public Object getResultObject() throws DataProviderException {
        testInvokeDataMethod();
        return resultObject;
    }

    /**
     * Read-only access to the result object array from the invocation of the
     * dataMethod
     *
     * @return Object[]
     */
    public Object[] getResultObjects() throws DataProviderException {
        testInvokeDataMethod();
        if (resultObject instanceof Object[]) {
            return (Object[])resultObject;
        }
        else if (resultObject instanceof Collection) {
            return ((Collection)resultObject).toArray();
        }
        else if (resultObject != null) {
            return new Object[] { resultObject };
        }
        return null;
    }

    /**
     * <p>Sets the includeFields property.  This affects the set of {@link
     * FieldKey}s that this {@link com.sun.data.provider.DataProvider} emits.
     * If includeFields is set to true (the default), then public fields will
     * be included in the list of available keys (intermixed with the public
     * properties).  If it is set to false, then only the public properties
     * will be available.</p>
     *
     * @param includeFields <code>true</code> to include the public fields, or
     *        <code>false</code> to exclude them (and only show public
     *        properties)
     */
    public void setIncludeFields(boolean includeFields) {
        resultProvider.setIncludeFields(includeFields);
    }

    /**
     * @return The boolean state of the includeFields property
     */
    public boolean isIncludeFields() {
        return resultProvider.isIncludeFields();
    }

    /**
     * Refreshes the list of available fieldKeys (based on the return type of
     * the dataMethod)
     */
    protected void refreshFieldKeys() {
        resultProvider.clearFieldKeys();
        if (dataMethod != null) {
            Class returnType = dataMethod.getReturnType();
            if (returnType.isArray()) {
                returnType = returnType.getComponentType();
            } else if (Collection.class.isAssignableFrom(returnType)) {
                returnType = collectionElementType;
            }
            resultProvider.setObjectType(returnType);
        }
    }

    /**
     * Sets the dataMethodArguments, which will be passed to the dataMethod
     * when it is invoked.
     *
     * @param methodArgs Object[]
     */
    public void setDataMethodArguments(Object[] methodArgs) {
        this.dataMethodArgs = methodArgs;
        resultObject = null;
        resultProvider.clearObjectList();
    }

    /**
     * Returns the dataMethodArguments
     *
     * @return Object[]
     */
    public Object[] getDataMethodArguments() {
        return dataMethodArgs;
    }

    //-------------------------------------------------------- Method Invocation

    /**
     * Invokes the dataMethod using the arguments specified by the
     * dataMethodArguments property.
     */
    public void invokeDataMethod() throws DataProviderException {
        invokeDataMethod(getDataMethodArguments());
    }

    /**
     * Invokes the dataMethod using the specified arguments.
     *
     * @param args Object[]
     */
    public void invokeDataMethod(Object[] args) throws DataProviderException {
        this.dataMethodArgs = args;
        this.resultObject = null;
        resultProvider.clearObjectList();
        if (dataMethod == null || dataClassInstance == null) {
            return;
        }
        try {
            if (java.beans.Beans.isDesignTime()) {
                // fake a call to the underlying data method for design-time
                resultObject = AbstractDataProvider.getFakeData(
                    dataMethod.getReturnType(), collectionElementType);
            } else {
                resultObject = dataMethod.invoke(dataClassInstance, args);
            }
            if (resultObject instanceof Object[]) {
                Object[] oa = (Object[])resultObject;
                for (int i = 0; i < oa.length; i++) {
                    resultProvider.addObject(oa[i]);
                }
            } else if (resultObject instanceof Collection) {
                Object[] oa = ((Collection)resultObject).toArray();
                for (int i = 0; i < oa.length; i++) {
                    resultProvider.addObject(oa[i]);
                }
            } else if (resultObject != null) {
                resultProvider.addObject(resultObject);
            }
            //!FIXME - this is a temporary hack to workaround an issue with
            // ObjectListDataProvider - this should be removed when OLDP is fixed
            resultProvider.commitChanges();
            fireRefreshed();
        }
        catch (Exception e) {
            throw new DataProviderException(e);
        }
    }

    /**
     * Tests to see if the dataMethod has been invoked, and invokes it if it
     * has not.
     */
    protected void testInvokeDataMethod() throws DataProviderException {
        if (resultObject == null || resultProvider.getRowCount() == 0) {
            invokeDataMethod();
        }
    }

    // ---------------------------------- RefreshableDataProvider Implementation

    /**
     * Invokes the dataMethod on the dataClassInstance to refresh the data
     * provider's contets
     */
    public void refresh() throws DataProviderException {
        invokeDataMethod();
    }

    /** {@inheritDoc} */
    public void addRefreshableDataListener(RefreshableDataListener l) {
        resultProvider.addDataListener(l);
    }

    /** {@inheritDoc} */
    public void removeRefreshableDataListener(RefreshableDataListener l) {
        resultProvider.removeDataListener(l);
    }

    /** {@inheritDoc} */
    public RefreshableDataListener[] getRefreshableDataListeners() {
        DataListener[] dpListeners = resultProvider.getDataListeners();
        if (dpListeners == null) {
            return new RefreshableDataListener[0];
        } else {
            ArrayList rdlList = new ArrayList();
            for (int i = 0; i < dpListeners.length; i++) {
                if (dpListeners[i] instanceof RefreshableDataListener) {
                    rdlList.add(dpListeners[i]);
                }
            }
            return (RefreshableDataListener[])rdlList.toArray(
                new RefreshableDataListener[rdlList.size()]);
        }
    }

    /**
     * Fires a refreshed event to each registered {@link RefreshableDataListener}
     *
     * @see RefreshableDataListener#refreshed(RefreshableDataProvider)
     */
    protected void fireRefreshed() {
        RefreshableDataListener[] rdls = getRefreshableDataListeners();
        for (int i = 0; i < rdls.length; i++) {
            rdls[i].refreshed(this);
        }
    }

    private Object dataClassInstance;
    private transient Method dataMethod;
    private Class collectionElementType;
    private Object[] dataMethodArgs;
    private Object resultObject;
    private ObjectListDataProvider resultProvider = new ObjectListDataProvider();

    private void writeObject(ObjectOutputStream out) throws IOException {
        if (dataMethod != null) {
            HashMap sig = new HashMap();
            sig.put("class", dataMethod.getDeclaringClass()); // NOI18N
            sig.put("name", dataMethod.getName()); // NOI18N
            sig.put("params", dataMethod.getParameterTypes()); // NOI18N
            out.writeObject(sig);
        }
    }

    private void readObject(ObjectInputStream in)
        throws IOException, ClassNotFoundException {
        Object o = in.readObject();
        if (o instanceof HashMap) {
            HashMap sig = (HashMap)o;
            Class clazz = (Class)sig.get("class"); // NOI18N
            String name = (String)sig.get("name"); // NOI18N
            Class[] params = (Class[])sig.get("params"); // NOI18N
            try {
                this.dataMethod = clazz.getMethod(name, params);
            } catch (NoSuchMethodException nsmx) {}
        }
    }

    //---------------------------------------------- DataProvider Implementation

    /** {@inheritDoc} */
    public FieldKey[] getFieldKeys() throws DataProviderException {
        return resultProvider.getFieldKeys();
    }
    /** {@inheritDoc} */
    public FieldKey getFieldKey(String fieldId) throws DataProviderException {
        return resultProvider.getFieldKey(fieldId);
    }
    /** {@inheritDoc} */
    public Class getType(FieldKey fieldKey) throws DataProviderException {
        return resultProvider.getType(fieldKey);
    }
    /** {@inheritDoc} */
    public Object getValue(FieldKey fieldKey) throws DataProviderException {
        testInvokeDataMethod();
        return resultProvider.getValue(fieldKey);
    }
    /** {@inheritDoc} */
    public boolean isReadOnly(FieldKey fieldKey) throws DataProviderException {
        testInvokeDataMethod();
        return resultProvider.isReadOnly(fieldKey);
    }
    /** {@inheritDoc} */
    public void setValue(FieldKey fieldKey, Object value) throws DataProviderException {
        testInvokeDataMethod();
        resultProvider.setValue(fieldKey, value);
    }
    /** {@inheritDoc} */
    public void addDataListener(DataListener listener) {
        resultProvider.addDataListener(listener);
    }
    /** {@inheritDoc} */
    public void removeDataListener(DataListener listener) {
        resultProvider.removeDataListener(listener);
    }
    /** {@inheritDoc} */
    public DataListener[] getDataListeners() {
        return resultProvider.getDataListeners();
    }

    //----------------------------------------- TableDataProvider Implementation

    /** {@inheritDoc} */
    public int getRowCount() throws DataProviderException {
        testInvokeDataMethod();
        return resultProvider.getRowCount();
    }
    /** {@inheritDoc} */
    public boolean isRowAvailable(RowKey row) throws DataProviderException {
        testInvokeDataMethod();
        return resultProvider.isRowAvailable(row);
    }
    /** {@inheritDoc} */
    public RowKey[] getRowKeys(int count, RowKey afterRow) throws DataProviderException {
        testInvokeDataMethod();
        return resultProvider.getRowKeys(count, afterRow);
    }
    /** {@inheritDoc} */
    public RowKey getRowKey(String rowId) throws DataProviderException {
        testInvokeDataMethod();
        return resultProvider.getRowKey(rowId);
    }
    /** {@inheritDoc} */
    public Object getValue(FieldKey fieldKey, RowKey row) throws DataProviderException {
        testInvokeDataMethod();
        return resultProvider.getValue(fieldKey, row);
    }
    /** {@inheritDoc} */
    public void setValue(FieldKey fieldKey, RowKey row, Object value) throws DataProviderException {
        testInvokeDataMethod();
        resultProvider.setValue(fieldKey, row, value);
    }
    /** {@inheritDoc} */
    public boolean canInsertRow(RowKey beforeRow) throws DataProviderException {
        testInvokeDataMethod();
        return resultProvider.canInsertRow(beforeRow);
    }
    /** {@inheritDoc} */
    public RowKey insertRow(RowKey beforeRow) throws DataProviderException {
        testInvokeDataMethod();
        return resultProvider.insertRow(beforeRow);
    }
    /** {@inheritDoc} */
    public boolean canAppendRow() throws DataProviderException {
        testInvokeDataMethod();
        return resultProvider.canAppendRow();
    }
    /** {@inheritDoc} */
    public RowKey appendRow() throws DataProviderException {
        testInvokeDataMethod();
        return resultProvider.appendRow();
    }
    /** {@inheritDoc} */
    public boolean canRemoveRow(RowKey row) throws DataProviderException {
        testInvokeDataMethod();
        return resultProvider.canRemoveRow(row);
    }
    /** {@inheritDoc} */
    public void removeRow(RowKey row) throws DataProviderException {
        testInvokeDataMethod();
        resultProvider.removeRow(row);
    }
    /** {@inheritDoc} */
    public void addTableDataListener(TableDataListener l) {
        resultProvider.addTableDataListener(l);
    }
    /** {@inheritDoc} */
    public void removeTableDataListener(TableDataListener l) {
        resultProvider.removeTableDataListener(l);
    }
    /** {@inheritDoc} */
    public TableDataListener[] getTableDataListeners() {
        return resultProvider.getTableDataListeners();
    }
    /** {@inheritDoc} */
    public RowKey getCursorRow() throws DataProviderException {
        testInvokeDataMethod();
        return resultProvider.getCursorRow();
    }
    /** {@inheritDoc} */
    public void setCursorRow(RowKey row) throws TableCursorVetoException {
        testInvokeDataMethod();
        resultProvider.setCursorRow(row);
    }
    /** {@inheritDoc} */
    public boolean cursorFirst() throws DataProviderException {
        testInvokeDataMethod();
        return resultProvider.cursorFirst();
    }
    /** {@inheritDoc} */
    public boolean cursorPrevious() throws DataProviderException {
        testInvokeDataMethod();
        return resultProvider.cursorPrevious();
    }
    /** {@inheritDoc} */
    public boolean cursorNext() throws DataProviderException {
        testInvokeDataMethod();
        return resultProvider.cursorNext();
    }
    /** {@inheritDoc} */
    public boolean cursorLast() throws DataProviderException {
        testInvokeDataMethod();
        return resultProvider.cursorLast();
    }
    /** {@inheritDoc} */
    public void addTableCursorListener(TableCursorListener l) {
        resultProvider.addTableCursorListener(l);
    }
    /** {@inheritDoc} */
    public void removeTableCursorListener(TableCursorListener l) {
        resultProvider.removeTableCursorListener(l);
    }
    /** {@inheritDoc} */
    public TableCursorListener[] getTableCursorListeners() {
        return resultProvider.getTableCursorListeners();
    }
}
