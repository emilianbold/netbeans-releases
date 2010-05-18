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

package com.sun.data.provider;

/**
 * <p>TableDataProvider is a specialized subinterface of {@link DataProvider}
 * that provides access to a scrollable set of "rows" of data elements, with
 * each row being identified by a {@link RowKey}.  Access to the underlying
 * data elements may be done in a random fashion by using the methods defined
 * in this interface, or you can set the cursor row and use the
 * <code>getType()</code>, <code>getValue()</code>, <code>isReadOnly()</code>,
 * and <code>setValue()</code> methods from the base {@link DataProvider}
 * interface to access data elements for the cursor row specified by calling
 * <code>setCursorRow(RowKey)</code>.</p>
 *
 * <p>The set of {@link FieldKey}s returned by <code>getFieldKeys()</code>
 * <strong>MUST</strong> be available on every row supported by this
 * {@link TableDataProvider}.  This implies a rectangular (matrix) dataset.</p>
 *
 * <p>The set of {@link RowKey}s returned by <code>getRowKeys(...)</code> are
 * expected to uniquely represent the rows contained in the underlying data
 * source.  The RowKey objects should remain valid as long as the set of rows
 * in the TableDataProvider has not been re-fetched.  Once a re-fetch has
 * happened (by whatever means are supplied by specific TableDataProvider
 * implementations), the previously fetched RowKeys may become invalid.  Consult
 * the documentation of the specific TableDataProvider implementation for
 * details on the expected valid lifespan of a RowKey.</p>
 *
 * <p>Most methods throw {@link DataProviderException}, which is a generic
 * runtime exception indicating something is amiss in the internal state of the
 * TableDataProvider implementation.  Because DPE is a runtime exception, method
 * calls are not required to be wrapped in a try...catch block, but it is
 * advised to check the documentation of the particular TableDataProvider
 * implementation to see what conditions will cause a DataProviderException to
 * be thrown.  It is recommended to always wrap calls to this interface in
 * try...catch blocks in case an unforseen error condition arises at runtime.</p>
 *
 * @author Joe Nuxoll
 * @author Matthew Bohm
 */
public interface TableDataProvider extends DataProvider {

    // ---------------------------------------------------------- RowKey Methods

    /**
     * @return the number of rows represented by this
     * {@link TableDataProvider} if this information is available;
     * otherwise, return -1 (indicating unknown row count)
     * @throws DataProviderException Implementations may wish to surface
     *         internal exceptions (nested in DataProviderException) rather
     *         than simply returning -1.  Consult the documentation of the
     *         specific TableDataProvider implementation for details on what
     *         exceptions might be wrapped by a DPE.
     */
    public int getRowCount() throws DataProviderException;

    /**
     * Returns an array of {@link RowKey} objects representing the requested
     * batch of RowKeys.  If <code>null</code> is passed as the afterRow
     * parameter, the returned batch of RowKeys will start with the first one.
     *
     * @param count The desired count of RowKeys to fetch.  If this number
     *        exceeds the total number of rows available, the return array
     *        will contain the available number, and no exception will be
     *        thrown.  Consider this an optimistic request of the
     *        TableDataProvider.
     * @param afterRow The RowKey that represents the last row before the set
     *        of desired RowKeys to be returned.  Typically, this is the last
     *        RowKey from the previously fetched set of RowKeys.  If
     *        <code>null</code> is passed, the returned set will begin with the
     *        first row.
     * @return An array of RowKey objects representing the requested batch of
     *         rows.
     * @throws DataProviderException Implementations may wish to surface
     *         internal exceptions (nested in DataProviderException) rather
     *         than simply returning null or an empty array.  Consult the
     *         documentation of the specific TableDataProvider implementation
     *         for details on what exceptions might be wrapped by a DPE.
     */
    public RowKey[] getRowKeys(int count, RowKey afterRow)
        throws DataProviderException;

    /**
     * Returns a RowKey for the specified rowId.  This allows a RowKey to be
     * stored off as a simple string, which can be resolved into an instance
     * of a RowKey at a later date.
     *
     * @param rowId The cannoncial string ID of the desired RowKey
     * @return A RowKey object representing the desired row, or
     *         <code>null</code> if the specified rowId does not correspond to
     *         a row in this TableDataProvider
     * @throws DataProviderException Implementations may wish to surface
     *         internal exceptions (nested in DataProviderException) rather
     *         than simply returning null.  Consult the documentation of the
     *         specific TableDataProvider implementation for details on what
     *         exceptions might be wrapped by a DPE.
     */
    public RowKey getRowKey(String rowId) throws DataProviderException;

    /**
     * Returns <code>true</code> if the specified {@link RowKey} represents
     * data elements that are supported by this {@link TableDataProvider};
     * otherwise, return <code>false</code>
     *
     * @param rowKey RowKey specifying row to be tested
     * @return <code>true</code> if the row is available, <code>false</code> if
     *         not
     * @throws DataProviderException Implementations may wish to surface
     *         internal exceptions (nested in DataProviderException) rather
     *         than simply returning false.  Consult the documentation of the
     *         specific TableDataProvider implementation for details on what
     *         exceptions might be wrapped by a DPE.
     */
    public boolean isRowAvailable(RowKey rowKey) throws DataProviderException;

    // --------------------------------------------------- Random Access Methods

    /**
     * <p>Return value of the data element referenced by the specified
     * {@link FieldKey} and {@link RowKey}.</p>
     *
     * @param fieldKey <code>FieldKey</code> identifying the data element
     *        whose value is to be returned
     * @param rowKey <code>RowKey</code> identifying the data row whose value
     *        is to be returned
     * @return value of the data element referenced by the specified
     *         {@link FieldKey} and {@link RowKey}
     * @throws DataProviderException Implementations may wish to surface
     *         internal exceptions (nested in DataProviderException) rather
     *         than simply returning null.  A DPE may also indicate that this
     *         <code>FieldKey</code> or <code>RowKey</code> does not represent
     *         a data element provided by this TableDataProvider.  Consult the
     *         documentation of the specific TableDataProvider implementation
     *         for details on what exceptions might be wrapped by a DPE.
     */
    public Object getValue(FieldKey fieldKey, RowKey rowKey)
        throws DataProviderException;

    /**
     * <p>Sets the value of the data element represented by the specified
     * {@link FieldKey} and {@link RowKey} to the specified new value.</p>
     *
     * @param fieldKey <code>FieldKey</code> identifying the data element
     *        whose value is to be modified
     * @param rowKey <code>RowKey</code> indentifying the data row whose value
     *        is to be modified
     * @param value New value for this data element
     * @throws DataProviderException Implementations may wish to surface
     *         internal exceptions (nested in DataProviderException).  A DPE
     *         may also indicate that this <code>FieldKey</code> or
     *         <code>RowKey</code> does not represent a data element provided
     *         by this TableDataProvider.  Consult the documentation of the
     *         specific TableDataProvider implementation for details on what
     *         exceptions might be wrapped by a DPE.
     */
    public void setValue(FieldKey fieldKey, RowKey rowKey, Object value)
        throws DataProviderException;

    // -------------------------------------------------------- Resizing Methods

    /**
     * <p>This method is called to test if this TableDataProvider supports
     * resizability.  If objects can be inserted and removed from the list,
     * this method should return <code>true</code>.  If the data provider is
     * not resizable, this method should return <code>false</code>.</p>
     *
     * <p>The following methods will only be called if this method returns
     * <code>true</code>:
     * <ul><li><code>insertRow(RowKey beforeRow)</code>
     * <li><code>appendRow()</code>
     * <li><code>removeRow(RowKey rowKey)</code>
     * </ul>
     *
     * @param beforeRow The desired location to insert the new row in front of
     * @return <code>true</code> if the data provider is resizable, or
     *         <code>false</code> if not.
     * @throws DataProviderException Implementations may wish to surface
     *         internal exceptions (nested in DataProviderException) rather
     *         than simply returning false.  A DPE may also indicate that this
     *         <code>RowKey</code> does not represent a row provided by this
     *         TableDataProvider.  Consult the documentation of the specific
     *         TableDataProvider implementation for details on what exceptions
     *         might be wrapped by a DPE.
     * @see #insertRow(RowKey)
     */
    public boolean canInsertRow(RowKey beforeRow) throws DataProviderException;

    /**
     * <p>Inserts a new row at the specified row.</p>
     *
     * <p>NOTE: The method should only be called after testing the
     * <code>canInsertRow(RowKey beforeRow)</code> to see if this
     * TableDataProvider supports resizing.</p>
     *
     * @param beforeRow The desired location to insert the new row in front of
     * @return A RowKey representing the address of the newly inserted row
     * @throws DataProviderException Implementations may wish to surface
     *         internal exceptions (nested in DataProviderException).  A DPE
     *         may also indicate that this <code>RowKey</code> does not
     *         represent a row provided by this TableDataProvider.  Consult the
     *         documentation of the specific TableDataProvider implementation
     *         for details on what exceptions might be wrapped by a DPE.
     * @see #canInsertRow(RowKey)
     */
    public RowKey insertRow(RowKey beforeRow) throws DataProviderException;

    /**
     * <p>This method is called to test if this TableDataProvider supports
     * the append operation.  If rows can be appended to the list, this method
     * should return <code>true</code>.  If the data provider is not resizable,
     * or cannot support an append operation, this method should return
     * <code>false</code>.</p>
     *
     * @return <code>true</code> if the data provider supports the append
     *         operation, or <code>false</code> if not.
     * @throws DataProviderException Implementations may wish to surface
     *         internal exceptions (nested in DataProviderException).  A DPE
     *         may also indicate that this <code>RowKey</code> does not
     *         represent a row provided by this TableDataProvider.  Consult the
     *         documentation of the specific TableDataProvider implementation
     *         for details on what exceptions might be wrapped by a DPE.
     * @see #appendRow()
     */
    public boolean canAppendRow() throws DataProviderException;

    /**
     * <p>Appends a new row at the end of the list and returns the row key for
     * the newly appended row.</p>
     *
     * <p>NOTE: The method should only be called after testing the
     * <code>canAppendRow()</code> method to see if this TableDataProvider
     * supports the append operation.</p>
     *
     * @return The row key for the newly appended row
     * @throws DataProviderException Implementations may wish to surface
     *         internal exceptions (nested in DataProviderException).  Consult
     *         the documentation of the specific TableDataProvider
     *         implementation for details on what exceptions might be wrapped
     *         by a DPE.
     * @see #canAppendRow()
     */
    public RowKey appendRow() throws DataProviderException;

    /**
     * <p>This method is called to test if this TableDataProvider supports
     * the removeRow operation.  If rows can be removed from the table, this
     * method should return <code>true</code>.  If the data provider is does
     * not support removing rows, this method should return <code>false</code>.
     * </p>
     *
     * @param rowKey The desired row to remove
     * @return <code>true</code> if the data provider supports removing rows,
     *         or <code>false</code> if not.
     * @throws DataProviderException Implementations may wish to surface
     *         internal exceptions (nested in DataProviderException).  A DPE
     *         may also indicate that this <code>RowKey</code> does not
     *         represent a row provided by this TableDataProvider.  Consult
     *         the documentation of the specific TableDataProvider
     *         implementation for details on what exceptions might be wrapped
     *         by a DPE.
     * @see #removeRow(RowKey)
     */
    public boolean canRemoveRow(RowKey rowKey) throws DataProviderException;

    /**
     * <p>Removes the specified row.</p>
     *
     * <p>NOTE: The method should only be called after testing the
     * <code>canRemoveRow(RowKey)</code> method to see if this TableDataProvider
     * supports removing rows.</p>
     *
     * @param rowKey The desired row key to remove
     * @throws DataProviderException Implementations may wish to surface
     *         internal exceptions (nested in DataProviderException).  A DPE
     *         may also indicate that this <code>RowKey</code> does not
     *         represent a row provided by this TableDataProvider.  Consult
     *         the documentation of the specific TableDataProvider
     *         implementation for details on what exceptions might be wrapped
     *         by a DPE.
     * @see #canRemoveRow(RowKey)
     */
    public void removeRow(RowKey rowKey) throws DataProviderException;

    // ----------------------------------- Table Data Event Registration Methods

    /**
     * <p>Register a new {@link TableDataListener} to this TableDataProvider
     * instance.</p>
     *
     * @param listener New {@link TableDataListener} to register
     */
    public void addTableDataListener(TableDataListener listener);

    /**
     * <p>Deregister an existing {@link TableDataListener} from this
     * TableDataProvider instance.</p>
     *
     * @param listener Old {@link TableDataListener} to deregister
     */
    public void removeTableDataListener(TableDataListener listener);

    /**
     * @return An array of the {@link TableDataListener}s currently registered
     *         on this TableDataProvider.  If there are no registered listeners,
     *         a zero-length array is returned.
     */
    public TableDataListener[] getTableDataListeners();

    // ----------------------------------------------------------- Cursor Access

    /**
     * @return the {@link RowKey} of the current cursor row position
     * @throws DataProviderException Implementations may wish to surface
     *         internal exceptions (nested in DataProviderException).  Consult
     *         the documentation of the specific TableDataProvider
     *         implementation for details on what exceptions might be wrapped
     *         by a DPE.
     */
    public RowKey getCursorRow() throws DataProviderException;

    /**
     * <p>Sets the cursor to the row represented by the passed {@link RowKey}.
     * </p>
     *
     * @param rowKey New {@link RowKey} to move the cursor to.
     * @throws TableCursorVetoException if a TableCursorListener decides to
     *         veto the cursor navigation
     * @throws DataProviderException Implementations may wish to surface
     *         internal exceptions (nested in DataProviderException).  A DPE
     *         may also indicate that this <code>RowKey</code> does not
     *         represent a row provided by this TableDataProvider.  Consult
     *         the documentation of the specific TableDataProvider
     *         implementation for details on what exceptions might be wrapped
     *         by a DPE.
     */
    public void setCursorRow(RowKey rowKey) throws TableCursorVetoException;

    /**
     * <p>Move the cursor to the first row in this TableDataProvider.</p>
     *
     * @return <code>true</code> if the cursor row was successfully changed;
     *         else <code>false</code>
     * @throws DataProviderException Implementations may wish to surface
     *         internal exceptions (nested in DataProviderException) rather
     *         than simply returning false.  Consult the documentation of the
     *         specific TableDataProvider implementation for details on what
     *         exceptions might be wrapped by a DPE.
     */
    public boolean cursorFirst() throws DataProviderException;

    /**
     * <p>Move the cursor to the row before the current cursor row, unless
     * the cursor is currently at the first row.</p>
     *
     * @return <code>true</code> if the cursor row was successfully changed;
     *         else <code>false</code>
     * @throws DataProviderException Implementations may wish to surface
     *         internal exceptions (nested in DataProviderException) rather
     *         than simply returning false.  Consult the documentation of the
     *         specific TableDataProvider implementation for details on what
     *         exceptions might be wrapped by a DPE.
     */
    public boolean cursorPrevious() throws DataProviderException;

    /**
     * <p>Move the cursor to the row after the current cursor row, unless the
     * cursor is currently at the last row {@link TableDataProvider}.</p>
     *
     * @return <code>true</code> if the cursor row was successfully changed;
     *         else <code>false</code>
     * @throws DataProviderException Implementations may wish to surface
     *         internal exceptions (nested in DataProviderException) rather
     *         than simply returning false.  Consult the documentation of the
     *         specific TableDataProvider implementation for details on what
     *         exceptions might be wrapped by a DPE.
     */
    public boolean cursorNext() throws DataProviderException;

    /**
     * <p>Move the cursor to the last row in this TableDataProvider.</p>
     *
     * @return <code>true</code> if the cursor row was successfully changed;
     *         else <code>false</code>
     * @throws DataProviderException Implementations may wish to surface
     *         internal exceptions (nested in DataProviderException) rather
     *         than simply returning false.  Consult the documentation of the
     *         specific TableDataProvider implementation for details on what
     *         exceptions might be wrapped by a DPE.
     */
    public boolean cursorLast() throws DataProviderException;

    // --------------------------------- Table Cursor Event Registration Methods

    /**
     * <p>Register a new {@link TableCursorListener} to this TableDataProvider
     * instance.</p>
     *
     * @param listener New {@link TableCursorListener} to register
     */
    public void addTableCursorListener(TableCursorListener listener);

    /**
     * <p>Deregister an existing {@link TableCursorListener} from this
     * TableDataProvider instance.</p>
     *
     * @param listener Old {@link TableCursorListener} to deregister
     */
    public void removeTableCursorListener(TableCursorListener listener);

    /**
     * @return An array of the {@link TableCursorListener}s currently registered
     *         on this TableDataProvider.  If there are no registered listeners,
     *         a zero-length array is returned.
     */
    public TableCursorListener[] getTableCursorListeners();
}
