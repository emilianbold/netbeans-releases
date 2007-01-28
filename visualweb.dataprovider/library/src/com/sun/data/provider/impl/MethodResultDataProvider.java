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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import com.sun.data.provider.DataListener;
import com.sun.data.provider.DataProviderException;
import com.sun.data.provider.FieldKey;
import com.sun.data.provider.RefreshableDataListener;
import com.sun.data.provider.RefreshableDataProvider;

/**
 * <p>A DataProvider implementation to wrap the singleton (non-array) return
 * value from a method.  Set the <code>dataClassInstance</code>,
 * <code>dataMethod</code>, and <code>dataMethodArguments</code> properties to
 * point to a method on a class instance.  The result from that method call
 * will be wrapped as a DataProvider.</p>
 *
 * @author cao, Joe Nuxoll
 */
public class MethodResultDataProvider
    implements RefreshableDataProvider, Serializable {

    /**
     * Constructs a new MethodResultDataProvider with no dataClassInstance
     * or dataMethod specified.
     */
    public MethodResultDataProvider() {}

    /**
     * Constructs a new MethodResultDataProvider using the specified
     * dataClassInstance and dataMethod.
     *
     * @param dataClassInstance The class instance where the method is invoked
     * @param dataMethod The method where the data is from
     */
    public MethodResultDataProvider(Object dataClassInstance, Method dataMethod) {
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
        resultProvider.setObject(null);
        refreshFieldKeys();
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
        resultProvider.setObject(null);
        refreshFieldKeys();
    }

    /**
     * Read-only access to the result object from the invocation of the
     * dataMethod
     *
     * @return Object
     */
    public Object getResultObject() throws DataProviderException {
        testInvokeDataMethod();
        return resultProvider.getObject();
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
            /* FIXME - use refactored stuff
            resultProvider.addFieldKeys(
                ObjectDataProvider.generateFieldKeys(returnType, true));
            */
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
        resultProvider.setObject(null);
        refreshFieldKeys();
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
        resultProvider.setObject(null);
        if (dataMethod == null || dataClassInstance == null) {
            return;
        }
        try {
            Object o = null;
            if (java.beans.Beans.isDesignTime()) {
                o = AbstractDataProvider.getFakeData(dataMethod.getReturnType());
            } else {
                o = dataMethod.invoke(dataClassInstance, args);
            }
            resultProvider.setObject(o);
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
        if (resultProvider.getObject() == null) {
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
    private Object[] dataMethodArgs;
    private ObjectDataProvider resultProvider = new ObjectDataProvider();

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
}
