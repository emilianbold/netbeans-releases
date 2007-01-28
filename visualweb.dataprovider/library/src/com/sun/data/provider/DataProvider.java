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

package com.sun.data.provider;

/**
 * <p>DataProvider is an interface that describes a single set of data
 * elements, each identified by a {@link FieldKey}.  Applications may retrieve
 * a list of available {@link FieldKey}s supported by a particular
 * {@link DataProvider} instance, or acquire a {@link FieldKey} for a
 * particular canonical name (whose intrinsic meaning is defined by the
 * particular {@link DataProvider} implementation in use).  For each
 * supported <code>{@link FieldKey}</code>, the corresponding data element's
 * value may be retrieved, and (optionally) modified, or its data type
 * and read-only status may be acquired.</p>
 *
 * <p>The base {@link DataProvider} interface describes access to individual
 * data elements.  In addition, specialized subinterfaces are defined for
 * access to multiple data elements.</p>
 *
 * <p>{@link TableDataProvider} provides access to tabular data, with
 * multiple rows each containing data elements corresponding to the same set
 * of {@link FieldKey}s.</p>
 *
 * <p>{@link DataProvider} imposes no requirements or limitations related to
 * whether it is legal to access the same {@link DataProvider} instance from
 * multiple threads that are operating simultaneously.  Thread safety
 * considerations are a feature of particular implementations.</p>
 *
 * <p>Most methods throw {@link DataProviderException}, which is a generic
 * runtime exception indicating something is amiss in the internal state of the
 * DataProvider implementation.  Because DPE is a runtime exception, method
 * calls are not required to be wrapped in a try...catch block, but it is
 * advised to check the documentation of the particular DataProvider
 * implementation to see what conditions will cause a DataProviderException to
 * be thrown.  It is recommended to always wrap calls to this interface in
 * try...catch blocks in case an unforseen error condition arises at runtime.</p>
 *
 * @author Joe Nuxoll
 */
public interface DataProvider {

    //--------------------------------------------------------- FieldKey Methods

    /**
     * @return An array of all {@link FieldKey}s supported by this
     * {@link DataProvider}.  If the set of valid {@link FieldKey}s cannot
     * be determined, return <code>null</code> instead.
     * @throws DataProviderException Implementations may wish to surface
     *         internal exceptions (nested in DataProviderException) rather
     *         than simply returning null or an empty array.  Consult the
     *         documentation of the specific DataProvider implementation for
     *         details on what exceptions might be wrapped by a DPE.
     */
    public FieldKey[] getFieldKeys() throws DataProviderException;

    /**
     * <p>Returns the {@link FieldKey} associated with the specified data
     * element canonical id, if any; otherwise, return <code>null</code>.</p>
     *
     * @param fieldId Canonical id of the requested {@link FieldKey}
     * @return the {@link FieldKey} associated with the specified data
     *         element canonical id, if any; otherwise, return
     *         <code>null</code>
     * @throws DataProviderException Implementations may wish to surface
     *         internal exceptions (nested in DataProviderException) rather
     *         than simply returning null.  A DPE may also indicate that the
     *         passed fieldId is not valid.  Consult the documentation of the
     *         specific DataProvider implementation for details on what
     *         exceptions might be wrapped by a DPE.
     */
    public FieldKey getFieldKey(String fieldId) throws DataProviderException;

    //------------------------------------------------------ Data Element Access

    /**
     * <p>Returns the data type of the data element referenced by the
     * specified data key.</p>
     *
     * @param fieldKey <code>FieldKey</code> identifying the data element
     *        whose type is to be returned
     * @return the data type of the data element referenced by the
     *         specified data key
     * @throws DataProviderException Implementations may wish to surface
     *         internal exceptions (nested in DataProviderException) rather
     *         than simply returning null.  A DPE may also indicate that the
     *         passed fieldKey is not valid.  Consult the documentation of the
     *         specific DataProvider implementation for details on what
     *         exceptions might be wrapped by a DPE.
     */
    public Class getType(FieldKey fieldKey) throws DataProviderException;

    /**
     * <p>Return a flag indicating whether the value of the data element
     * represented by the specified {@link FieldKey} can be modified via the
     * <code>setValue()</code> method.</p>
     *
     * @param fieldKey <code>FieldKey</code> identifying the data element
     *                whose settable status is to be returned
     * @return a flag indicating whether the value of the data element
     *         represented by the specified {@link FieldKey} can be modified
     *         via the <code>setValue()</code> method
     * @throws DataProviderException Implementations may wish to surface
     *         internal exceptions (nested in DataProviderException) rather
     *         than simply returning true.  A DPE may also indicate that the
     *         passed fieldKey is not valid.  Consult the documentation of the
     *         specific DataProvider implementation for details on what
     *         exceptions might be wrapped by a DPE.
     */
    public boolean isReadOnly(FieldKey fieldKey) throws DataProviderException;

    /**
     * <p>Returns value of the data element referenced by the specified
     * {@link FieldKey}.</p>
     *
     * @param fieldKey <code>FieldKey</code> identifying the data element
     *                whose value is to be returned
     * @return value of the data element referenced by the specified
     *         {@link FieldKey}
     * @throws DataProviderException Implementations may wish to surface
     *         internal exceptions (nested in DataProviderException) rather
     *         than simply returning null.  A DPE may also indicate that the
     *         passed fieldKey is not valid.  Consult the documentation of the
     *         specific DataProvider implementation for details on what
     *         exceptions might be wrapped by a DPE.
     */
    public Object getValue(FieldKey fieldKey) throws DataProviderException;

    /**
     * <p>Set the value of the data element represented by the specified
     * {@link FieldKey} to the specified new value.</p>
     *
     * @param fieldKey <code>FieldKey</code> identifying the data element
     *        whose value is to be modified
     * @param value New value for this data element
     * @throws DataProviderException Implementations may wish to surface
     *         internal exceptions (nested in DataProviderException) rather
     *         than simply returning null.  A DPE may also indicate that the
     *         passed fieldKey is not valid.  Consult the documentation of the
     *         specific DataProvider implementation for details on what
     *         exceptions might be wrapped by a DPE.
     */
    public void setValue(FieldKey fieldKey, Object value)
        throws DataProviderException;

    //----------------------------------------------- Event Registration Methods

    /**
     * <p>Register a new {@link DataListener} to this DataProvider
     * instance.</p>
     *
     * @param listener New {@link DataListener} to register
     */
    public void addDataListener(DataListener listener);

    /**
     * <p>Deregister an existing {@link DataListener} from this
     * DataProvider instance.</p>
     *
     * @param listener Old {@link DataListener} to deregister
     */
    public void removeDataListener(DataListener listener);

    /**
     * @return An array of the {@link DataListener}s currently
     *         registered on this DataProvider.  If there are no registered
     *         listeners, a zero-length array is returned.
     */
    public DataListener[] getDataListeners();
}
