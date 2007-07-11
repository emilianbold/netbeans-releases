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
package com.sun.sql.rowset;

import javax.sql.rowset.*;
import java.sql.*;
import javax.sql.*;
import javax.naming.*;
import java.io.*;
import java.math.*;
import java.text.MessageFormat;
import java.util.*;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import java.io.Serializable;

import java.text.DateFormat;
import java.text.ParseException;

import javax.sql.rowset.serial.SerialArray;
import javax.sql.rowset.serial.SerialBlob;
import javax.sql.rowset.serial.SerialClob;
import javax.sql.rowset.serial.SerialRef;
import javax.sql.rowset.serial.SerialStruct;
import javax.sql.rowset.serial.SQLInputImpl;

import javax.sql.rowset.spi.SyncFactory;
import javax.sql.rowset.spi.SyncProvider;
import javax.sql.rowset.spi.SyncProviderException;
import javax.sql.rowset.spi.TransactionalWriter;

import com.sun.rowset.internal.BaseRow;
import com.sun.rowset.internal.InsertRow;
import com.sun.rowset.internal.Row;

import com.sun.sql.rowset.internal.CachedRowSetXReader;
import com.sun.sql.rowset.internal.CachedRowSetXWriter;

import com.sun.sql.rowset.providers.RIOptimisticProvider;

import java.beans.Beans;

import java.util.logging.Logger;

/**
 * The reference implementation of the <code>CachedRowSetX</code> interface. See the interface
 * defintion for full behavior and implementation requirements.
 *
 * Note: This implemenation is based on the Sun Microsystems Reference Implemenation of
 * <code>CachedRowSet</code>
 */

public class CachedRowSetXImpl extends BaseRowSetX implements CachedRowSetX, RowSetInternal, Serializable, Cloneable {
    
    private static ResourceBundle rb =
            ResourceBundle.getBundle("com.sun.sql.rowset.Bundle", Locale.getDefault()); // NOI18N
    
    
    /**
     * The <code>PropertyChangeSupport</code> object helps in providing
     * support for property change listeners.
     */
    private PropertyChangeSupport propertyChangeSupport;
    
    
    /**
     * The <code>executed</code> flag tells us whether to throw exceptions
     * when methods are called that require the rowset to have been executed
     */
    private boolean executed;
    
    
    /**
     * The <code>internalUseInstance</code> flag tells to ignore exceptional conditions
     * with regard to the rowset not being executed.  We must do this because this class
     * is also used internally and methods are called that would result in a rowset not
     * executed sqlexception if the user called them.
     */
    protected boolean internalUseInstance;
    
    
    /**
     * Constructs a default <code>CachedRowSetXImpl</code> object instance.
     *
     * Constructs a new default <code>CachedRowSetXImpl</code> object with
     * the capacity to hold 100 rows. This new object has no metadata
     * and has the following default values:
     * <pre>
     *     cursorPos = 0
     *     numRows = 0
     *     showDeleted = false
     *     queryTimeout = 0
     *     maxRows = 0
     *     maxFieldSize = 0
     *     rowSetType = ResultSet.TYPE_SCROLL_INSENSITIVE
     *     concurrency = ResultSet.CONCUR_UPDATABLE
     *     readOnly = false
     *     isolation = Connection.TRANSACTION_READ_COMMITTED
     *     escapeProcessing = true
     *     cursorPos = 0
     *     absolutePos = 0
     *     numRows = 0
     * </pre>
     * A <code>CachedRowSetXImpl</code> object is configured to use the default
     * <code>RIOptimisticProvider</code> implementation to provide connectivity
     * and synchronization capabilities to the set data source.
     */
    public CachedRowSetXImpl() {
        
        /*
        try {
            // set the Reader, this maybe overridden latter
            provider = (SyncProvider)SyncFactory.getInstance(DEFAULT_SYNC_PROVIDER);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
         */
        provider = new RIOptimisticProvider();
        
        if (!(provider instanceof RIOptimisticProvider)) {
            throw new RuntimeException(
                    new SQLException(rb.getString("INVALID_PERSISTENCE_PROV_GEN"))); //NOI18N
        }
        
        rowSetReader = (CachedRowSetXReader)provider.getRowSetReader();
        rowSetWriter = (CachedRowSetXWriter)provider.getRowSetWriter();
        
        // allocate the parameters collection
        initParams();
        
        initContainer();
        
        // set up some default values
        try {
            initProperties();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        
        // set the warninings
        sqlwarn = new SQLWarning();
        rowsetWarning = new RowSetWarning();
    }
    
    /**
     * used internally by readers and writers
     */
    static public CachedRowSetXImpl createInternalUseInstance() {
        CachedRowSetXImpl iucrs = new CachedRowSetXImpl();
        iucrs.internalUseInstance = true;
        return iucrs;
    }
    
    protected void initParams() {
        super.initParams();
        propertyChangeSupport = new PropertyChangeSupport(this);
        insertableColumns = null;
    }
    
    /**
     * Provides a <code>CachedRowSetXImpl</code> instance with the same default properties as
     * as the zero parameter constructor.
     * <pre>
     *     cursorPos = 0
     *     numRows = 0
     *     showDeleted = false
     *     queryTimeout = 0
     *     maxRows = 0
     *     maxFieldSize = 0
     *     rowSetType = ResultSet.TYPE_SCROLL_INSENSITIVE
     *     concurrency = ResultSet.CONCUR_UPDATABLE
     *     readOnly = false
     *     isolation = Connection.TRANSACTION_READ_COMMITTED
     *     escapeProcessing = true
     *     onInsertRow = false
     *     cursorPos = 0
     *     absolutePos = 0
     *     numRows = 0
     * </pre>
     *
     * However, applications will have the means to specify at runtime the
     * desired <code>SyncProvider</code> object.
     * <p>
     * For example, creating a <code>CachedRowSetXImpl</code> object as follows ensures
     * that a it is established with the <code>com.foo.provider.Impl</code> synchronization
     * implementation providing the synchronization mechanism for this disconnected
     * <code>RowSet</code> object.
     * <pre>
     *     Hashtable env = new Hashtable();
     *     env.put(javax.sql.rowset.spi.SyncFactory.ROWSET_PROVIDER_NAME,
     *         "com.foo.provider.Impl");
     *     CachedRowSetXImpl crs = new CachedRowSet(env);
     * </pre>
     * <p>
     * Calling this constructor with a <code>null</code> parameter will
     * cause the <code>SyncFactory</code> to provide the reference
     * optimistic provider <code>com.sun.rowset.providers.RIOptimisticProvider</code>.
     * <p>
     * In addition, the following properties can be associated with the
     * provider to assist in determining the choice of the synchronizaton
     * provider such as:
     * <ul>
     * <li><code>ROWSET_SYNC_PROVIDER</code> - the property specifying the the
     * <code>SyncProvider</code> class name to be instantiated by the
     * <code>SyncFacttory</code>
     * <li><code>ROWSET_SYNC_VENDOR</code> - the property specifying the software
     * vendor associated with a <code>SyncProvider</code> implementation.
     * <li><code>ROWSET_SYNC_PROVIDER_VER</code> - the property specifying the
     * version of the <code>SyncProvider</code> implementation provided by the
     * software vendor.
     * </ul>
     * More specific detailes are available in the <code>SyncFactory</code>
     * and <code>SyncProvider</code> specificiations later in this document.
     * <p>
     * @param env a <code>Hashtable</code> object with a list of desired
     *        synchronization providers
     * @throws SQLException if the requested provider cannot be found by the
     * synchonization factory
     * @see SyncProvider
     */
    public CachedRowSetXImpl(Hashtable env) throws SQLException {
        
        if (env == null) {
            throw new SQLException(rb.getString("NULL_HASHTABLE")); //NOI18N
        }
        
        /*
        String providerName = (String)env.get(
        javax.sql.rowset.spi.SyncFactory.ROWSET_SYNC_PROVIDER);
         
        // set the Reader, this maybe overridden latter
        provider =
        (SyncProvider)SyncFactory.getInstance(providerName);
         */
        provider = new RIOptimisticProvider();
        
        rowSetReader = provider.getRowSetReader();
        rowSetWriter = provider.getRowSetWriter();
        
        initParams(); // allocate the parameters collection
        initContainer();
        initProperties(); // set up some default values
    }
    
    
    /**
     * {@inheritDoc}
     */
    public boolean isExecuted() throws SQLException {
        return executed;
    }
    
    
    void checkExecuted() throws SQLException {
        // Don't throw an exception in the middle of commit or rollback changes
        if (internalUseInstance) {
            return;
        }
        // make sure we have a resultset (methods such as next()
        // prev(), first(), last(), beforeFirst(), beforeLast(),
        // getString() ,setString(), etc, etc., etc., will throw
        // a NPE if there is not resultset internally because
        // the rowset wasn't executed
        if (!isExecuted()) {
            throw new SQLException(rb.getString("EXECUTE_NEVER_CALLED")); //NOI18N
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public void setDataSourceName(String dataSourceName) throws SQLException {
        String oldValue = getDataSourceName();
        super.setDataSourceName(dataSourceName);
        release();
        propertyChangeSupport.firePropertyChange("dataSourceName", oldValue, dataSourceName); // NOI18N
    }
    
    /**
     * {@inheritDoc}
     */
    public void setUrl(String url) throws SQLException {
        String oldValue = getUrl();
        super.setUrl(url);
        release();
        propertyChangeSupport.firePropertyChange("url", oldValue, url); // NOI18N
    }
    
    /**
     * {@inheritDoc}
     */
    public void setUsername(String username) {
        String oldValue = getUsername();
        super.setUsername(username);
        try {
            release();
        } catch (SQLException e) {
        }
        propertyChangeSupport.firePropertyChange("username", oldValue, username); // NOI18N
    }
    
    /**
     * {@inheritDoc}
     */
    public void setPassword(String password) {
        String oldValue = getPassword();
        super.setPassword(password);
        propertyChangeSupport.firePropertyChange("password", oldValue, password); // NOI18N
    }
    
    /**
     * {@inheritDoc}
     */
    public void setType(int type) throws SQLException {
        int oldValue = 0;
        try {
            oldValue = getType();
        } catch (SQLException e) {
        }
        super.setType(type);
        propertyChangeSupport.firePropertyChange("type", oldValue, type); // NOI18N
    }
    
    /**
     * {@inheritDoc}
     */
    public void setConcurrency(int concurrency) throws SQLException {
        int oldValue = 0;
        try {
            oldValue = getConcurrency();
        } catch (SQLException e) {
        }
        super.setConcurrency(concurrency);
        propertyChangeSupport.firePropertyChange("concurrency", oldValue, concurrency); // NOI18N
    }
    
    /**
     * {@inheritDoc}
     */
    public void setTransactionIsolation(int transactionIsolation) throws SQLException {
        int oldValue = getTransactionIsolation();
        super.setTransactionIsolation(transactionIsolation);
        propertyChangeSupport.firePropertyChange("transactionIsolation", oldValue, //NOI18N
                transactionIsolation); // NOI18N
    }
    
    /**
     * {@inheritDoc}
     */
    public void setMaxRows(int maxRows) throws SQLException {
        int oldValue = 0;
        try {
            oldValue = getMaxRows();
        } catch (SQLException e) {
        }
        super.setMaxRows(maxRows);
        propertyChangeSupport.firePropertyChange("maxRows", oldValue, maxRows); // NOI18N
    }
    
    /**
     * {@inheritDoc}
     */
    public void setReadOnly(boolean isReadOnly) {
        if (isReadOnly() != isReadOnly) {
            boolean oldValue = isReadOnly();
            super.setReadOnly(isReadOnly);
            propertyChangeSupport.firePropertyChange("readOnly", oldValue, isReadOnly); // NOI18N
        }
    }
    
    //--------------------------------------------------------------------
    // PropertyChangeEvents
    //--------------------------------------------------------------------
    
    /**
     * {@inheritDoc}
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }
    
    /**
     * {@inheritDoc}
     */
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
    }
    
    /**
     * {@inheritDoc}
     * @deprecated
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    };
    
    /**
     * {@inheritDoc}
     */
    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(propertyName, listener);
    }
    
    /**
     * The <code>SyncProvider</code> used by the CachedRowSet
     */
    private SyncProvider provider;
    
    /**
     * The <code>RowSetReaderImpl</code> object that is the reader
     * for this rowset.  The method <code>execute</code> uses this
     * reader as part of its implementation.
     * @serial
     */
    private RowSetReader rowSetReader;
    
    /**
     * The <code>RowSetWriterImpl</code> object that is the writer
     * for this rowset.  The method <code>acceptChanges</code> uses
     * this writer as part of its implementation.
     * @serial
     */
    private RowSetWriter rowSetWriter;
    
    /**
     * The <code>Connection</code> object that connects with this
     * <code>CachedRowSetXImpl</code> object's current underlying data source.
     */
    private transient Connection conn;
    
    /**
     * The <code>ResultSetMetaData</code> object that contains information
     * about the columns in the <code>ResultSet</code> object that is the
     * current source of data for this <code>CachedRowSetXImpl</code> object.
     */
    private transient ResultSetMetaData rsmd;
    
    /**
     * The <code>RowSetMetaData</code> object that contains information about
     * the columns in this <code>CachedRowSetXImpl</code> object.
     * @serial
     */
    private RowSetMetaDataXImpl rowSetMD;
    
    // Properties of this RowSet
    
    /**
     * An array containing the columns in this <code>CachedRowSetXImpl</code>
     * object that form a unique identifier for a row. This array
     * is used by the writer.
     * @serial
     */
    private int keyCols[];
    
    /**
     * The name of the table in the underlying database to which updates
     * should be written.  This name is needed because most drivers
     * do not return this information in a <code>ResultSetMetaData</code>
     * object.
     * @serial
     */
    private String tableName;
    
    
    /**
     * A <code>Vector</code> object containing the <code>Row</code>
     * objects that comprise  this <code>CachedRowSetXImpl</code> object.
     * @serial
     */
    private Vector rvh;
    /**
     * The current postion of the cursor in this <code>CachedRowSetXImpl</code>
     * object.
     * @serial
     */
    private int cursorPos;
    
    /**
     * The current postion of the cursor in this <code>CachedRowSetXImpl</code>
     * object not counting rows that have been deleted, if any.
     * <P>
     * For example, suppose that the cursor is on the last row of a rowset
     * that started with five rows and subsequently had the second and third
     * rows deleted. The <code>absolutePos</code> would be <code>3</code>,
     * whereas the <code>cursorPos</code> would be <code>5</code>.
     * @serial
     */
    private int absolutePos;
    
    /**
     * The number of deleted rows currently in this <code>CachedRowSetXImpl</code>
     * object.
     * @serial
     */
    private int numDeleted;
    
    /**
     * The total number of rows currently in this <code>CachedRowSetXImpl</code>
     * object.
     * @serial
     */
    private int numRows;
    
    /**
     * A special row used for constructing a new row. A new
     * row is constructed by using <code>ResultSet.updateXXX</code>
     * methods to insert column values into the insert row.
     * @serial
     */
    private InsertRow insertRow;
    
    /**
     * A <code>boolean</code> indicating whether the cursor is
     * currently on the insert row.
     * @serial
     */
    private boolean onInsertRow;
    
    /**
     * The field that temporarily holds the last position of the
     * cursor before it moved to the insert row, thus preserving
     * the number of the current row to which the cursor may return.
     * @serial
     */
    private int currentRow;
    
    /**
     * A <code>boolean</code> indicating whether the last value
     * returned was an SQL <code>NULL</code>.
     * @serial
     */
    private boolean lastValueNull;
    
    /**
     * A <code>SQLWarning</code> which logs on the warnings
     */
    private SQLWarning sqlwarn;
    
    /**
     * Used to track match column for JoinRowSet consumption
     */
    private String strMatchColumn =""; //NOI18N
    
    /**
     * Used to track match column for JoinRowSet consumption
     */
    private int iMatchColumn = -1;
    
    /**
     * A <code>RowSetWarning</code> which logs on the warnings
     */
    private RowSetWarning rowsetWarning;
    
    /**
     * The default SyncProvider for the RI CachedRowSetXImpl
     */
    private String DEFAULT_SYNC_PROVIDER = "com.sun.sql.rowset.providers.RIOptimisticProvider"; //NOI18N
    
    /**
     * The boolean variable indicating locatorsUpdateValue
     */
    private boolean dbmslocatorsUpdateCopy;
    
    /**
     * The <code>ResultSet</code> object that is used to maintain the data when
     * a ResultSet and start position are passed as parameters to the populate function
     */
    private ResultSet resultSet;
    
    /**
     * The integer value indicating the end position in the ResultSetwhere the picking
     * up of rows for populating a CachedRowSet object was left off.
     */
    private int endPos;
    
    /**
     * The integer value indicating the end position in the ResultSetwhere the picking
     * up of rows for populating a CachedRowSet object was left off.
     */
    private int prevEndPos;
    
    /**
     * The integer value indicating the position in the ResultSet, to populate the
     * CachedRowSet object.
     */
    private int startPos;
    
    /**
     * The integer value indicating the positon from where the page prior to this
     * was populated.
     */
    private int startPrev;
    
    /**
     * The integer value indicating size of the page.
     */
    private int pageSize;
    
    /**
     * The integer value indicating number of rows that have been processed so far.
     * Used for checking whether maxRows has been reached or not.
     */
    private int maxRowsreached;
    /**
     * The boolean value when true signifies that pages are still to follow and a
     * false value indicates that this is the last page.
     */
    private boolean pagenotend = true;
    
    /**
     * The boolean value indicating whether this is the first page or not.
     */
    private boolean onFirstPage;
    
    /**
     * The boolean value indicating whether this is the last page or not.
     */
    private boolean onLastPage;
    
    /**
     * The integer value indicating how many times the populate function has been called.
     */
    private int populatecallcount;
    
    /**
     * The integer value indicating the total number of rows to be processed in the
     * ResultSet object passed to the populate function.
     */
    private int totalRows;
    
    /**
     * The boolean value indicating how the CahedRowSet object has been populated for
     * paging purpose. True indicates that connection parameter is passed.
     */
    private boolean callWithCon;
    
    /**
     * CachedRowSet reader object to read the data from the ResultSet when a connection
     * parameter is passed to populate the CachedRowSet object for paging.
     */
    private CachedRowSetXReader crsReader;
    
    /**
     * The Vector holding the Match Columns
     */
    private Vector iMatchColumns;
    
    /**
     * The Vector that will hold the Match Column names.
     */
    private Vector strMatchColumns;
    
    /**
     * Trigger that indicates whether the active SyncProvider is exposes the
     * additional TransactionalWriter method
     */
    private boolean tXWriter = false;
    
    /**
     * The field object for a transactional RowSet writer
     */
    private TransactionalWriter tWriter = null;
    
    /**
     * Sets the <code>rvh</code> field to a new <code>Vector</code>
     * object with a capacity of 100 and sets the
     * <code>cursorPos</code> and <code>numRows</code> fields to zero.
     */
    private void initContainer() {
        
        rvh = new Vector(100);
        cursorPos = 0;
        absolutePos = 0;
        numRows = 0;
        numDeleted = 0;
        rsmd = null;
        rowSetMD = null;
    }
    
    /**
     * Sets the properties for this <code>CachedRowSetXImpl</code> object to
     * their default values. This method is called internally by the
     * default constructor.
     */
    
    private void initProperties() throws SQLException {
        setShowDeleted(false);
        setQueryTimeout(0);
        setMaxRows(0);
        setMaxFieldSize(0);
        setType(ResultSet.TYPE_SCROLL_INSENSITIVE);
        setConcurrency(ResultSet.CONCUR_UPDATABLE);
        setReadOnly(true);
        setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
        setEscapeProcessing(true);
        setTypeMap(null);
        checkTransactionalWriter();
        
        // insert row setup
        onInsertRow = false;
        insertRow = null;
        
        executed = false;
        
        //Instantiating the vector for MatchColumns
        
        iMatchColumns = new Vector(10);
        for(int i = 0; i < 10 ; i++) {
            iMatchColumns.add(i,new Integer(-1));
        }
        
        strMatchColumns = new Vector(10);
        for(int j = 0; j < 10; j++) {
            strMatchColumns.add(j,null);
        }
    }
    
    /**
     * Determine whether the SyncProvider's writer implements the
     * <code>TransactionalWriter<code> interface
     */
    private void checkTransactionalWriter() {
        if (rowSetWriter != null) {
            Class c = rowSetWriter.getClass();
            if (c != null) {
                Class[] theInterfaces = c.getInterfaces();
                for (int i = 0; i < theInterfaces.length; i++) {
                    if ((theInterfaces[i].getName()).indexOf("TransactionalWriter") > 0) { //NOI18N
                        tXWriter = true;
                        establishTransactionalWriter();
                    }
                }
            }
        }
    }
    
    /**
     * Sets an private field to all transaction bounddaries to be set
     */
    private void establishTransactionalWriter() {
        tWriter = (TransactionalWriter)provider.getRowSetWriter();
    }
    
    //-----------------------------------------------------------------------
    // Properties
    //-----------------------------------------------------------------------
    
    /**
     * {@inheritDoc}
     */
    public void setCommand(String command) throws SQLException {
        
        String oldValue = getCommand();
        super.setCommand(command);
        release();
        propertyChangeSupport.firePropertyChange("command", oldValue, command); // NOI18N
    }
    
    
    //---------------------------------------------------------------------
    // Reading and writing data
    //---------------------------------------------------------------------
    
    /**
     * {@inheritDoc}
     */
    public void populate(ResultSet data) throws SQLException {
        int rowsFetched;
        Row currentRow;
        int numCols;
        int i;
        Map map = getTypeMap();
        Object obj;
        int mRows;
        
        if (data == null) {
            throw new SQLException(rb.getString("INVALID_RESULTSET_SUPPLIED")); //NOI18N
        }
        
        // get the meta data for this ResultSet
        rsmd = data.getMetaData();
        
        // set up the metadata
        rowSetMD = new RowSetMetaDataXImpl();
        initMetaData(rowSetMD, rsmd);
        
        // release the meta-data so that aren't tempted to use it.
        rsmd = null;
        numCols = rowSetMD.getColumnCount();
        mRows = this.getMaxRows();
        rowsFetched = 0;
        currentRow = null;
        
        while ( data.next()) {
            
            currentRow = new Row(numCols);
            
            if ( rowsFetched > mRows && mRows > 0) {
                rowsetWarning.setNextWarning(new RowSetWarning(rb.getString("MAX_ROWS_EXCEEDED"))); //NOI18N
            }
            
            for ( i = 1; i <= numCols; i++) {
                /*
                 * check if the user has set a map. If no map
                 * is set then use plain getObject. This lets
                 * us work with drivers that do not support
                 * getObject with a map in fairly sensible way
                 */
                if (map == null) {
                    obj = data.getObject(i);
                } else {
                    obj = data.getObject(i, map);
                }
                /*
                 * the following block checks for the various
                 * types that we have to serialize in order to
                 * store - right now only structs have been tested
                 */
                if (obj instanceof Struct) {
                    obj = new SerialStruct((Struct)obj, map);
                } else if (obj instanceof SQLData) {
                    obj = new SerialStruct((SQLData)obj, map);
                } else if (obj instanceof Blob) {
                    obj = new SerialBlob((Blob)obj);
                } else if (obj instanceof Clob) {
                    obj = new SerialClob((Clob)obj);
                } else if (obj instanceof java.sql.Array) {
                    obj = new SerialArray((java.sql.Array)obj, map);
                }
                
                ((Row)currentRow).initColumnObject(i, obj);
            }
            rowsFetched++;
            rvh.add(currentRow);
        }
        executed = true;
        
        numRows = rowsFetched ;
        // Also rowsFetched should be equal to rvh.size()
        
        // notify any listeners that the rowset has changed
        notifyRowSetChanged();
    }
    
    /**
     * Initializes the given <code>RowSetMetaData</code> object with the values
     * in the given <code>ResultSetMetaData</code> object.
     *
     * @param md the <code>RowSetMetaData</code> object for this
     *           <code>CachedRowSetXImpl</code> object, which will be set with
     *           values from rsmd
     * @param rsmd the <code>ResultSetMetaData</code> object from which new
     *             values for md will be read
     * @throws SQLException if an error occurs
     */
    private void initMetaData(RowSetMetaDataXImpl md, ResultSetMetaData rsmd) throws SQLException {

        Log.getLogger().entering(getClass().getName(), "initMetaData()");
        int numCols = rsmd.getColumnCount();
        
        md.setColumnCount(numCols);
        for (int col=1; col <= numCols; col++) {
            md.setAutoIncrement(col, rsmd.isAutoIncrement(col));
            md.setCaseSensitive(col, rsmd.isCaseSensitive(col));
            md.setCurrency(col, rsmd.isCurrency(col));
            md.setNullable(col, rsmd.isNullable(col));
            md.setSigned(col, rsmd.isSigned(col));
            md.setSearchable(col, rsmd.isSearchable(col));

            /*
             * The PostgreSQL drivers sometimes return negative columnDisplaySize,
             * which causes an exception to be thrown.  Check for it.
             */
            int size = rsmd.getColumnDisplaySize(col);
            if (size < 0) {
                size = 0;
            }
            md.setColumnDisplaySize(col, size);

            md.setColumnLabel(col, rsmd.getColumnLabel(col));
            md.setColumnName(col, rsmd.getColumnName(col));
            md.setSchemaName(col, rsmd.getSchemaName(col));

            /*
             * Drivers return some strange values for precision, for non-numeric data, including reports of
             * non-integer values; maybe we should check type, & set to 0 for non-numeric types.
             */
	    int precision = rsmd.getPrecision(col);
            if (precision < 0) {
                precision = 0;
            }
            md.setPrecision(col, precision);

            /*
             * It seems, from a bug report, that a driver can sometimes return a negative
             * value for scale.  javax.sql.rowset.RowSetMetaDataImpl will throw an exception
             * if we attempt to set a negative value.  As such, we'll check for this case.
             */
            int scale = rsmd.getScale(col);
            if (scale < 0) {
                scale = 0;
            }
            md.setScale(col, scale);

            md.setTableName(col, rsmd.getTableName(col));
            md.setCatalogName(col, rsmd.getCatalogName(col));
            md.setColumnType(col, rsmd.getColumnType(col));
            md.setColumnTypeName(col, rsmd.getColumnTypeName(col));
            
            md.setColumnClassName(col, rsmd.getColumnClassName(col));
            /*
             * We face a dilemna here.  The RI always says columns are writable.
             * We'd like to give a better answer; but since we allow joins that
             * would result in the database saying all columns are readonly, we
             * can't just hand back the information the driver gives about the
             * resultset.  As such, we would really like to compute if the column is
             * writable in much the same way that CachedRowSetXWriter computes this
             * information.  For now, we'll return true unless the user has provided
             * information otherwise in the updatable columns on the rowset.
             *
             * Actually, even this will cause problems as there is a difference between
             * insertable and updatable.  We want to allow new rows to specify insertable
             * columns that are not updatable.  So we will actually honor insertable if it
             * is present.  If not, we'll use updatable if present.  If not, we'll say the
             * column is writable.
             */
            if (insertableColumns != null && insertableColumns.length >= col) {
                md.setWritable(col, insertableColumns[col-1]);
                md.setDefinitelyWritable(col, insertableColumns[col-1]);
                md.setReadOnly(col, !insertableColumns[col-1]);
            } else if (updatableColumns != null && updatableColumns.length >= col) {
                md.setWritable(col, updatableColumns[col-1]);
                md.setDefinitelyWritable(col, updatableColumns[col-1]);
                md.setReadOnly(col, !updatableColumns[col-1]);
            } else {
                md.setWritable(col, true);
                md.setDefinitelyWritable(col, false); // We don't know for sure
                md.setReadOnly(col, false);
            }
            
            dbmslocatorsUpdateCopy = false;
            if( conn != null){
                try {
                    dbmslocatorsUpdateCopy = conn.getMetaData().locatorsUpdateCopy();
                } catch (SQLException e) {
                    // It seems, some databases throw an exception here (e.g., Pointbase)
                    // assume false (so updateClob/Blob will not work)
                }
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public void execute(Connection conn) throws SQLException {

        Log.getLogger().entering(getClass().getName(), "execute()");

        // store the connection so the reader can find it.
        setConnection(conn);
        
        if(getPageSize() != 0){
            crsReader = (CachedRowSetXReader)provider.getRowSetReader();
            crsReader.setStartPosition(1);
            callWithCon = true;
            crsReader.readData((RowSetInternal)this);
        }
        
        // Now call the current reader's readData method
        else {
            rowSetReader.readData((RowSetInternal)this);
        }
        
        executed = true;
        
        rowSetMD = (RowSetMetaDataXImpl)this.getMetaData();
        
        if (conn != null) {
            try {
                dbmslocatorsUpdateCopy = conn.getMetaData().locatorsUpdateCopy();
            } catch (SQLException e) {
                // It seems, some databases throw an exception here (e.g., Pointbase)
                // assume false (so updateClob/Blob will not work)
                dbmslocatorsUpdateCopy = false;
            }
        } else {
            Connection tempConn = null;
            try {
                tempConn = getConnection(getDataSourceName(),
                        getUrl(),
                        getUsername(),
                        getPassword());
                dbmslocatorsUpdateCopy = tempConn.getMetaData().locatorsUpdateCopy();
            } catch (SQLException e) {
                // It seems, some databases throw an exception here (e.g., Pointbase)
                // assume false (so updateClob/Blob will not work)
                dbmslocatorsUpdateCopy = false;
            } catch (AbstractMethodError e) {
                // Some drivers (e.g., Oracle 9) don't implement the locatorsUpdateCopy() method.
                // Assume false (so updateClob/Blob will not work)
                dbmslocatorsUpdateCopy = false;
            } finally {
                if (tempConn != null) {
                    try {
                        if (!tempConn.getAutoCommit()) {
                            tempConn.rollback();
                        }
                    } catch (Exception dummy) {
                        /*
                         * not an error condition, we're closing anyway, but
                         * we'd like to clean up any locks if we can since
                         * it is not clear the connection pool will clean
                         * these connections in a timely manner
                         */
                    }
                    tempConn.close();
                }
            }
        }
    }
    
    /**
     * Sets this <code>CachedRowSetXImpl</code> object's connection property
     * to the given <code>Connection</code> object.  This method is called
     * internally by the version of the method <code>execute</code> that takes a
     * <code>Connection</code> object as an argument. The reader for this
     * <code>CachedRowSetXImpl</code> object can retrieve the connection stored
     * in the rowset's connection property by calling its
     * <code>getConnection</code> method.
     *
     * @param connection the <code>Connection</code> object that was passed in
     *                   to the method <code>execute</code> and is to be stored
     *                   in this <code>CachedRowSetXImpl</code> object's connection
     *                   property
     */
    private void setConnection(Connection connection) {
        conn = connection;
    }
    
    
    /**
     * {@inheritDoc}
     */
    public void acceptChanges() throws SyncProviderException {
        try {
            checkExecuted();
        } catch (SQLException e) {
            throw new SyncProviderException(e.getMessage());
        }
        if (onInsertRow == true) {
            throw new SyncProviderException(rb.getString("INVALID_INSERT_ROW_OP")); //NOI18N
        }
        
        int saveCursorPos = cursorPos;
        boolean success = false;
        boolean conflict = false;
        
        try {
            if (rowSetWriter != null) {
                saveCursorPos = cursorPos;
                conflict = rowSetWriter.writeData((RowSetInternal)this);
                cursorPos = saveCursorPos;
            }
            
            if ((tXWriter) && this.COMMIT_ON_ACCEPT_CHANGES) {
                // do commit/rollback's here
                if (!conflict) {
                    tWriter = (TransactionalWriter)rowSetWriter;
                    tWriter.rollback();
                    success = false;
                } else {
                    tWriter = (TransactionalWriter)rowSetWriter;
                    tWriter.commit();
                    success = true;
                }
            }
            
            if (success == true) {
                setOriginal();
            } else if (!(success) && !(this.COMMIT_ON_ACCEPT_CHANGES)) {
                throw new SyncProviderException(rb.getString("ACCEPT_CHANGES_FAILED")); //NOI18N
            }
            
        } catch (SyncProviderException spe) {
            throw spe;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SyncProviderException(e.getMessage());
        } catch (SecurityException e) {
            throw new SyncProviderException(e.getMessage());
        } finally {
            try {
                if (tWriter instanceof CachedRowSetXWriter) {
                    ((CachedRowSetXWriter)tWriter).closeConnection();
                }
            } catch (SQLException e) {
                // well, we tried
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public void acceptChanges(Connection con) throws SyncProviderException{
        
        try{
            checkExecuted();
            setConnection(con);
            acceptChanges();
        } catch (SyncProviderException spe) {
            throw spe;
        } catch(SQLException sqle){
            throw new SyncProviderException(sqle.getMessage());
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public void restoreOriginal() throws SQLException {
        checkExecuted();
        Row currentRow;
        for (Iterator i = rvh.iterator(); i.hasNext();) {
            currentRow = (Row)i.next();
            if (currentRow.getInserted() == true) {
                i.remove();
                --numRows;
            } else {
                if (currentRow.getDeleted() == true) {
                    currentRow.clearDeleted();
                    //!JK shouldn't we be doing a ++numRows here?
                }
                if (currentRow.getUpdated() == true) {
                    currentRow.clearUpdated();
                }
            }
        }
        // move to before the first
        cursorPos = 0;
        absolutePos = 0; //!JK: added this
        
        // notify any listeners
        notifyRowSetChanged();
    }
    
    /**
     * {@inheritDoc}
     */
    public void release() throws SQLException {
        initContainer();
        executed = false;
        notifyRowSetChanged();
    }
    
    /**
     * {@inheritDoc}
     */
    public void undoDelete() throws SQLException {
        checkExecuted();
        if (getShowDeleted() == false) {
            return;
        }
        // make sure we are on a row
        checkCursor();
        
        // don't want this to happen...
        if (onInsertRow == true) {
            throw new SQLException(rb.getString("INVALID_CURSOR_POS")); //NOI18N
        }
        
        Row currentRow = (Row)getCurrentRow();
        if (currentRow.getDeleted() == true) {
            currentRow.clearDeleted();
            --numDeleted;
            notifyRowChanged();
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public void undoInsert() throws SQLException {
        checkExecuted();
        // make sure we are on a row
        checkCursor();
        
        // don't want this to happen...
        if (onInsertRow == true) {
            throw new SQLException(rb.getString("INVALID_CURSOR_POS")); //NOI18N
        }
        
        Row currentRow = (Row)getCurrentRow();
        if (currentRow.getInserted() == true) {
            rvh.remove(cursorPos);
            --numRows;
            notifyRowChanged();
        } else {
            throw new SQLException(rb.getString("INVALID_INSERT_ROW_OP")); //NOI18N
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public void undoUpdate() throws SQLException {
        checkExecuted();
        // if on insert row, cancel the insert row
        // make the insert row flag,
        // cursorPos back to the current row
        moveToCurrentRow();
        
        // else if not on insert row
        // call undoUpdate or undoInsert
        undoDelete();
        
        undoInsert();
        
    }
    
    //--------------------------------------------------------------------
    // Views
    //--------------------------------------------------------------------
    
    /**
     * {@inheritDoc}
     */
    public RowSet createShared() throws SQLException {
        checkExecuted(); //!JK ?
        RowSet clone;
        try {
            clone = (RowSet)clone();
        } catch (CloneNotSupportedException ex) {
            throw new SQLException(ex.getMessage());
        }
        return clone;
    }
    
    /**
     * Returns a new <code>RowSet</code> object containing by the same data
     * as this <code>CachedRowSetXImpl</code> object.  This method
     * differs from the method <code>createCopy</code> in that it throws a
     * <code>CloneNotSupportedException</code> object instead of an
     * <code>SQLException</code> object, as the method <code>createShared</code>
     * does.  This <code>clone</code>
     * method is called internally by the method <code>createShared</code>,
     * which catches the <code>CloneNotSupportedException</code> object
     * and in turn throws a new <code>SQLException</code> object.
     *
     * @return a copy of this <code>CachedRowSetXImpl</code> object
     * @throws CloneNotSupportedException if an error occurs when
     * attempting to clone this <code>CachedRowSetXImpl</code> object
     * @see #createShared
     */
    protected Object clone() throws CloneNotSupportedException  {
        return (super.clone());
    }
    
    /**
     * {@inheritDoc}
     */
    public CachedRowSet createCopy() throws SQLException {
        checkExecuted(); //!JK ?
        ObjectOutputStream out;
        ByteArrayOutputStream bOut = new ByteArrayOutputStream();
        try {
            out = new ObjectOutputStream(bOut);
            out.writeObject(this);
        } catch (IOException ex) {
            throw new SQLException(rb.getString("CLONE_FAILED" + ex.getMessage())); //NOI18N
        }
        
        ObjectInputStream in;
        
        try {
            ByteArrayInputStream bIn = new ByteArrayInputStream(bOut.toByteArray());
            in = new ObjectInputStream(bIn);
        } catch (StreamCorruptedException ex) {
            throw new SQLException(rb.getString("CLONE_FAILED" + ex.getMessage())); //NOI18N
        } catch (IOException ex) {
            throw new SQLException(rb.getString("CLONE_FAILED" + ex.getMessage())); //NOI18N
        }
        
        try {
            return ((CachedRowSet)(in.readObject()));
        } catch (ClassNotFoundException ex) {
            throw new SQLException(rb.getString("CLONE_FAILED" + ex.getMessage())); //NOI18N
        } catch (OptionalDataException ex) {
            throw new SQLException(rb.getString("CLONE_FAILED" + ex.getMessage())); //NOI18N
        } catch (IOException ex) {
            throw new SQLException(rb.getString("CLONE_FAILED" + ex.getMessage())); //NOI18N
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public CachedRowSet createCopySchema() throws SQLException {
        checkExecuted(); //!JK ?
        // Copy everything except data i.e all constraints
        
        // Store the number of rows of "this"
        // and make numRows equals zero.
        // and make data also zero.
        int nRows = numRows;
        numRows = 0;
        
        CachedRowSet crs = this.createCopy();
        
        // reset this object back to number of rows.
        numRows = nRows;
        
        return crs;
    }
    
    /**
     * {@inheritDoc}
     */
    public CachedRowSet createCopyNoConstraints() throws SQLException {
        checkExecuted(); //!JK ?
        // Copy the whole data ONLY without any constraints.
        CachedRowSetXImpl crs;
        crs = (CachedRowSetXImpl)this.createCopy();
        
        crs.initProperties();
        try {
            crs.unsetMatchColumn(crs.getMatchColumnIndexes());
        } catch(SQLException sqle) {
            //do nothing, if the setMatchColumn is not set.
        }
        
        try {
            crs.unsetMatchColumn(crs.getMatchColumnNames());
        } catch(SQLException sqle) {
            //do nothing, if the setMatchColumn is not set.
        }
        
        return crs;
    }
    
    /**
     * {@inheritDoc}
     */
    public Collection toCollection() throws SQLException {
        checkExecuted(); //!JK ?
        
        TreeMap tMap;
        int count = 0;
        Row origRow;
        Vector newRow;
        
        int colCount = rowSetMD.getColumnCount();
        tMap = new TreeMap();
        
        for (int i = 0; i<numRows; i++) {
            tMap.put(new Integer(i), rvh.get(i));
        }
        
        return (tMap.values());
    }
    
    /**
     * {@inheritDoc}
     */
    public Collection toCollection(int column) throws SQLException {
        checkExecuted(); //!JK ?
        
        Vector vec;
        Row origRow;
        int nRows = numRows;
        vec = new Vector(nRows);
        
        // create a copy
        CachedRowSetXImpl crsTemp;
        crsTemp = (CachedRowSetXImpl) this.createCopy();
        
        while(nRows!=0) {
            crsTemp.next();
            vec.add(crsTemp.getObject(column));
            nRows--;
        }
        
        return (Collection)vec;
    }
    
    /**
     * {@inheritDoc}
     */
    public Collection toCollection(String column) throws SQLException {
        checkExecuted(); //!JK ?
        return toCollection(getColIdxByName(column));
    }
    
    //--------------------------------------------------------------------
    // Advanced features
    //--------------------------------------------------------------------
    
    
    /**
     * {@inheritDoc}
     */
    public SyncProvider getSyncProvider() throws SQLException {
        return provider;
    }
    
    /**
     * {@inheritDoc}
     */
    public void setSyncProvider(String providerStr) throws SQLException {
        provider =
                (SyncProvider)SyncFactory.getInstance(providerStr);
        
        rowSetReader = provider.getRowSetReader();
        rowSetWriter = (TransactionalWriter)provider.getRowSetWriter();
    }
    
    
    //-----------------
    // methods inherited from RowSet
    //-----------------
    
    
    
    
    
    
    //---------------------------------------------------------------------
    // Reading and writing data
    //---------------------------------------------------------------------
    
    /**
     * {@inheritDoc}
     */
    public void execute() throws SQLException {
        execute(null);
    }
    
    
    
    //-----------------------------------
    // Methods inherited from ResultSet
    //-----------------------------------
    
    /**
     * {@inheritDoc}
     */
    public boolean next() throws SQLException {
        checkExecuted();
        /*
         * make sure things look sane. The cursor must be
         * positioned in the rowset or before first (0) or
         * after last (numRows + 1)
         */
        if (cursorPos < 0 || cursorPos >= numRows + 1) {
            throw new SQLException(rb.getString("INVALID_CURSOR_POS")); //NOI18N
        }
        // now move and notify
        boolean ret = this.internalNext();
        notifyCursorMoved();
        
        return ret;
    }
    
    /**
     * Moves this <code>CachedRowSetXImpl</code> object's cursor to the next
     * row and returns <code>true</code> if the cursor is still in the rowset;
     * returns <code>false</code> if the cursor has moved to the position after
     * the last row.
     * <P>
     * This method handles the cases where the cursor moves to a row that
     * has been deleted.
     * If this rowset shows deleted rows and the cursor moves to a row
     * that has been deleted, this method moves the cursor to the next
     * row until the cursor is on a row that has not been deleted.
     * <P>
     * The method <code>internalNext</code> is called by methods such as
     * <code>next</code>, <code>absolute</code>, and <code>relative</code>,
     * and, as its name implies, is only called internally.
     * <p>
     * This is a implementation only method and is not required as a standard
     * implementation of the <code>CachedRowSet</code> interface.
     *
     * @return <code>true</code> if the cursor is on a valid row in this
     *         rowset; <code>false</code> if it is after the last row
     * @throws SQLException if an error occurs
     */
    protected boolean internalNext() throws SQLException {
        boolean ret = false;
        //System.out.println("internalNext(): start");
        
        do {
            //System.out.println("internalNext(): do top: cursorPos: " + cursorPos + ", numRows: " + numRows);
            if (cursorPos < numRows) {
                ++cursorPos;
                ret = true;
                //System.out.println("internalNext(): do: case was cursorPos < numRows: cursorPos: " + cursorPos + ", ret: " + ret);
            } else if (cursorPos == numRows) {
                // increment to after last
                ++cursorPos;
                ret = false;
                //System.out.println("internalNext(): do: case was cursorPos == numRows: cursorPos: " + cursorPos + ", ret: " + ret);
                break;
            }
        } while ((getShowDeleted() == false) && (rowDeleted() == true));
        //System.out.println("internalNext(): fell through the while");
        
        /* each call to internalNext may increment cursorPos multiple
         * times however, the absolutePos only increments once per call.
         */
        if (ret == true)
            absolutePos++;
        else
            absolutePos = 0;
        
        //System.out.println("internalNext(): returning: cursorPos: " + cursorPos + ", absolutePos: " + absolutePos + ", numRows: " + numRows + ", ret: " + ret);
        return ret;
    }
    
    /**
     * {@inheritDoc}
     */
    public void close() {
        
        // close all data structures holding
        // the disconnected rowset
        
        cursorPos = 0;
        absolutePos = 0;
        numRows = 0;
        numDeleted = 0;
        
        // set all insert(s), update(s) & delete(s),
        // if at all, to their initial values.
        try {
            initProperties();
        } catch (SQLException e) {
        }
        
        // clear the vector of it's present contents
        rvh.clear();
        
        // this will make it eligible for gc
        // rvh = null;
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean wasNull() throws SQLException {
        checkExecuted();
        return lastValueNull;
    }
    
    /**
     * Sets the field <code>lastValueNull</code> to the given
     * <code>boolean</code> value.
     *
     * @param value <code>true</code> to indicate that the value of
     *        the last column read was SQL <code>NULL</code>;
     *        <code>false</code> to indicate that it was not
     */
    private void setLastValueNull(boolean value) {
        lastValueNull = value;
    }
    
    //======================================================================
    // Methods for accessing results by column index
    //======================================================================
    
    /**
     * Checks to see whether the given index is a valid column number
     * in this <code>CachedRowSetXImpl</code> object and throws
     * an <code>SQLException</code> if it is not. The index is out of bounds
     * if it is less than <code>1</code> or greater than the number of
     * columns in this rowset.
     * <P>
     * This method is called internally by the <code>getXXX</code> and
     * <code>updateXXX</code> methods.
     *
     * @param idx the number of a column in this <code>CachedRowSetXImpl</code>
     *            object; must be between <code>1</code> and the number of
     *            rows in this rowset
     * @throws SQLException if the given index is out of bounds
     */
    private void checkIndex(int idx) throws SQLException {
        if (idx < 1 || idx > rowSetMD.getColumnCount()) {
            throw new SQLException(MessageFormat.format(rb.getString("INVALID_COLUMN_INDEX"), //NOI18N
                    new Object[] { new Integer(idx) }));
        }
    }
    
    /**
     * Checks to see whether the cursor for this <code>CachedRowSetXImpl</code>
     * object is on a row in the rowset and throws an
     * <code>SQLException</code> if it is not.
     * <P>
     * This method is called internally by <code>getXXX</code> methods, by
     * <code>updateXXX</code> methods, and by methods that update, insert,
     * or delete a row or that cancel a row update, insert, or delete.
     *
     * @throws SQLException if the cursor for this <code>CachedRowSetXImpl</code>
     *         object is not on a valid row
     */
    private void checkCursor() throws SQLException {
        if (isAfterLast() == true || isBeforeFirst() == true) {
            throw new SQLException(rb.getString("INVALID_CURSOR_POS")); //NOI18N
        }
    }
    
    /**
     * Returns the column number of the column with the given name in this
     * <code>CachedRowSetXImpl</code> object.  This method throws an
     * <code>SQLException</code> if the given name is not the name of
     * one of the columns in this rowset.
     *
     * @param name a <code>String</code> object that is the name of a column in
     *              this <code>CachedRowSetXImpl</code> object
     * @throws SQLException if the given name does not match the name of one of
     *         the columns in this rowset
     */
    private int getColIdxByName(String name) throws SQLException {
        int cols = rowSetMD.getColumnCount();
        
        for (int i=1; i <= cols; ++i) {
            String colName = rowSetMD.getColumnName(i);
            if (colName != null)
                if (name.equalsIgnoreCase(colName))
                    return (i);
                else
                    continue;
        }
        throw new SQLException(rb.getString("INVALID_COLUMN_NAME")); //NOI18N
        
    }
    
    /**
     * Returns the insert row or the current row of this
     * <code>CachedRowSetXImpl</code>object.
     *
     * @return the <code>Row</code> object on which this <code>CachedRowSetXImpl</code>
     * objects's cursor is positioned
     */
    protected BaseRow getCurrentRow() throws SQLException {
        if (onInsertRow == true) {
            return (BaseRow)insertRow;
        } else {
            if (cursorPos < 1 || cursorPos > rvh.size()) {
                throw new SQLException(rb.getString("INVALID_CURSOR_POS")); //NOI18N
            }
            return (BaseRow)(rvh.get(cursorPos - 1));
        }
    }
    
    /**
     * Removes the row on which the cursor is positioned.
     * <p>
     * This is a implementation only method and is not required as a standard
     * implementation of the <code>CachedRowSet</code> interface.
     *
     * @throws SQLException if the cursor is positioned on the insert
     *            row
     */
    protected void removeCurrentRow() throws SQLException {
        ((Row)getCurrentRow()).setDeleted();
        rvh.remove(cursorPos);
        --numRows;
    }
    
    
    /**
     * {@inheritDoc}
     */
    public String getString(int columnIndex) throws SQLException {
        checkExecuted();
        Object value;
        
        // sanity check.
        checkIndex(columnIndex);
        // make sure the cursor is on a valid row
        checkCursor();
        
        setLastValueNull(false);
        value = getCurrentRow().getColumnObject(columnIndex);
        
        // check for SQL NULL
        if (value == null) {
            setLastValueNull(true);
            return null;
        }
        
        return value.toString();
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean getBoolean(int columnIndex) throws SQLException {
        checkExecuted();
        Object value;
        
        // sanity check.
        checkIndex(columnIndex);
        // make sure the cursor is on a valid row
        checkCursor();
        
        setLastValueNull(false);
        value = getCurrentRow().getColumnObject(columnIndex);
        
        // check for SQL NULL
        if (value == null) {
            setLastValueNull(true);
            return false;
        }
        
        // check for Boolean...
        if (value instanceof Boolean) {
            return ((Boolean)value).booleanValue();
        }
        
        // convert to a Double and compare to zero
        try {
            Double d = new Double(value.toString());
            if (d.compareTo(new Double((double)0)) == 0) {
                return false;
            } else {
                return true;
            }
        } catch (NumberFormatException ex) {
            throw new SQLException(MessageFormat.format(rb.getString("GET_BOOLEAN_FAILED"), //NOI18N
                    new Object[] { value.toString().trim(), new Integer(columnIndex) }));
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public byte getByte(int columnIndex) throws SQLException {
        checkExecuted();
        Object value;
        
        // sanity check.
        checkIndex(columnIndex);
        // make sure the cursor is on a valid row
        checkCursor();
        
        setLastValueNull(false);
        value = getCurrentRow().getColumnObject(columnIndex);
        
        // check for SQL NULL
        if (value == null) {
            setLastValueNull(true);
            return (byte)0;
        }
        try {
            return ((new Byte(value.toString())).byteValue());
        } catch (NumberFormatException ex) {
            throw new SQLException(MessageFormat.format(rb.getString("GET_BYTE_FAILED"), //NOI18N
                    new Object[] { value.toString().trim(), new Integer(columnIndex) }));
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public short getShort(int columnIndex) throws SQLException {
        checkExecuted();
        Object value;
        
        // sanity check.
        checkIndex(columnIndex);
        // make sure the cursor is on a valid row
        checkCursor();
        
        setLastValueNull(false);
        value = getCurrentRow().getColumnObject(columnIndex);
        
        // check for SQL NULL
        if (value == null) {
            setLastValueNull(true);
            return (short)0;
        }
        
        try {
            return ((new Short(value.toString().trim())).shortValue());
        } catch (NumberFormatException ex) {
            throw new SQLException(MessageFormat.format(rb.getString("GET_SHORT_FAILED"), //NOI18N
                    new Object[] { value.toString().trim(), new Integer(columnIndex) }));
        }
    }
    
    public int getInt(int columnIndex) throws SQLException {
        checkExecuted();
        Object value;
        
        // sanity check.
        checkIndex(columnIndex);
        // make sure the cursor is on a valid row
        checkCursor();
        
        setLastValueNull(false);
        value = getCurrentRow().getColumnObject(columnIndex);
        
        // check for SQL NULL
        if (value == null) {
            setLastValueNull(true);
            return (int)0;
        }
        
        try {
            return ((new Integer(value.toString().trim())).intValue());
        } catch (NumberFormatException ex) {
            throw new SQLException(MessageFormat.format(rb.getString("GET_INT_FAILED"), //NOI18N
                    new Object[] { value.toString().trim(), new Integer(columnIndex) }));
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public long getLong(int columnIndex) throws SQLException {
        checkExecuted();
        Object value;
        
        // sanity check.
        checkIndex(columnIndex);
        // make sure the cursor is on a valid row
        checkCursor();
        
        setLastValueNull(false);
        value = getCurrentRow().getColumnObject(columnIndex);
        
        // check for SQL NULL
        if (value == null) {
            setLastValueNull(true);
            return (long)0;
        }
        try {
            return ((new Long(value.toString().trim())).longValue());
        } catch (NumberFormatException ex) {
            throw new SQLException(MessageFormat.format(rb.getString("GET_LONG_FAILED"), //NOI18N
                    new Object[] { value.toString().trim(), new Integer(columnIndex) }));
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public float getFloat(int columnIndex) throws SQLException {
        checkExecuted();
        Object value;
        
        // sanity check.
        checkIndex(columnIndex);
        // make sure the cursor is on a valid row
        checkCursor();
        
        setLastValueNull(false);
        value = getCurrentRow().getColumnObject(columnIndex);
        
        // check for SQL NULL
        if (value == null) {
            setLastValueNull(true);
            return (float)0;
        }
        try {
            return ((new Float(value.toString())).floatValue());
        } catch (NumberFormatException ex) {
            throw new SQLException(MessageFormat.format(rb.getString("GET_FLOAT_FAILED"), //NOI18N
                    new Object[] { value.toString().trim(), new Integer(columnIndex) }));
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public double getDouble(int columnIndex) throws SQLException {
        checkExecuted();
        Object value;
        
        // sanity check.
        checkIndex(columnIndex);
        // make sure the cursor is on a valid row
        checkCursor();
        
        setLastValueNull(false);
        value = getCurrentRow().getColumnObject(columnIndex);
        
        // check for SQL NULL
        if (value == null) {
            setLastValueNull(true);
            return (double)0;
        }
        try {
            return ((new Double(value.toString().trim())).doubleValue());
        } catch (NumberFormatException ex) {
            throw new SQLException(MessageFormat.format(rb.getString("GET_DOUBLE_FAILED"), //NOI18N
                    new Object[] { value.toString().trim(), new Integer(columnIndex) }));
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException {
        checkExecuted();
        return getBigDecimal(columnIndex, scale);
    }
    
    /**
     * {@inheritDoc}
     */
    public byte[] getBytes(int columnIndex) throws SQLException {
        checkExecuted();
        // sanity check.
        checkIndex(columnIndex);
        // make sure the cursor is on a valid row
        checkCursor();
        
        if (isBinary(rowSetMD.getColumnType(columnIndex)) == false) {
            throw new SQLException(rb.getString("DATATYPE_MISMATCH")); //NOI18N
        }
        
        return (byte[])(getCurrentRow().getColumnObject(columnIndex));
    }
    
    /**
     * {@inheritDoc}
     */
    public java.sql.Date getDate(int columnIndex) throws SQLException {
        checkExecuted();
        Object value;
        
        // sanity check.
        checkIndex(columnIndex);
        // make sure the cursor is on a valid row
        checkCursor();
        
        setLastValueNull(false);
        value = getCurrentRow().getColumnObject(columnIndex);
        
        // check for SQL NULL
        if (value == null) {
            setLastValueNull(true);
            return null;
        }
        
        /*
         * The object coming back from the db could be
         * a date, a timestamp, or a char field variety.
         * If it's a date type return it, a timestamp
         * we turn into a long and then into a date,
         * char strings we try to parse. Yuck.
         */
        switch (rowSetMD.getColumnType(columnIndex)) {
            case java.sql.Types.DATE: {
                long sec = ((java.sql.Date)value).getTime();
                return new java.sql.Date(sec);
            }
            case java.sql.Types.TIMESTAMP: {
                long sec = ((java.sql.Timestamp)value).getTime();
                return new java.sql.Date(sec);
            }
            case java.sql.Types.CHAR:
            case java.sql.Types.VARCHAR:
            case java.sql.Types.LONGVARCHAR: {
                try {
                    DateFormat df = DateFormat.getDateInstance();
                    return ((java.sql.Date)(df.parse(value.toString())));
                } catch (ParseException ex) {
                    throw new SQLException(MessageFormat.format(rb.getString("GET_TIMESTAMP_FAILED"), //NOI18N
                            new Object[] { value.toString().trim(), new Integer(columnIndex) }));
                }
            }
            default: {
                throw new SQLException(MessageFormat.format(rb.getString("GET_TIMESTAMP2_FAILED"), //NOI18N
                        new Object[] { value.toString().trim(), new Integer(columnIndex) }));
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public java.sql.Time getTime(int columnIndex) throws SQLException {
        checkExecuted();
        Object value;
        
        // sanity check.
        checkIndex(columnIndex);
        // make sure the cursor is on a valid row
        checkCursor();
        
        setLastValueNull(false);
        value = getCurrentRow().getColumnObject(columnIndex);
        
        // check for SQL NULL
        if (value == null) {
            setLastValueNull(true);
            return null;
        }
        
        /*
         * The object coming back from the db could be
         * a date, a timestamp, or a char field variety.
         * If it's a date type return it, a timestamp
         * we turn into a long and then into a date,
         * char strings we try to parse. Yuck.
         */
        switch (rowSetMD.getColumnType(columnIndex)) {
            case java.sql.Types.TIME: {
                return (java.sql.Time)value;
            }
            case java.sql.Types.TIMESTAMP: {
                long sec = ((java.sql.Timestamp)value).getTime();
                return new java.sql.Time(sec);
            }
            case java.sql.Types.CHAR:
            case java.sql.Types.VARCHAR:
            case java.sql.Types.LONGVARCHAR: {
                try {
                    DateFormat tf = DateFormat.getTimeInstance();
                    return ((java.sql.Time)(tf.parse(value.toString())));
                } catch (ParseException ex) {
                    throw new SQLException(MessageFormat.format(rb.getString("GET_TIME_FAILED"), //NOI18N
                            new Object[] { value.toString().trim(), new Integer(columnIndex) }));
                }
            }
            default: {
                throw new SQLException(MessageFormat.format(rb.getString("GET_TIME2_FAILED"), //NOI18N
                        new Object[] { value.toString().trim(), new Integer(columnIndex) }));
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public java.sql.Timestamp getTimestamp(int columnIndex) throws SQLException {
        // System.out.println("Entering CRSXI.getTimestamp");
        checkExecuted();
        Object value;
        
        // sanity check.
        checkIndex(columnIndex);
        // make sure the cursor is on a valid row
        checkCursor();
        
        setLastValueNull(false);
        value = getCurrentRow().getColumnObject(columnIndex);
        // System.out.println("CRSXI.getTimestamp/1, value: " + value);
        
        // check for SQL NULL
        if (value == null) {
            setLastValueNull(true);
            return null;
        }
        
        /*
         * The object coming back from the db could be
         * a date, a timestamp, or a char field variety.
         * If it's a date type return it; a timestamp
         * we turn into a long and then into a date;
         * char strings we try to parse. Yuck.
         */
        // System.out.println("CRSXI.getTimestamp/2, columnType: " + rowSetMD.getColumnType(columnIndex));

        switch (rowSetMD.getColumnType(columnIndex)) {
            case java.sql.Types.TIMESTAMP: {
                return (java.sql.Timestamp)value;
            }
            case java.sql.Types.TIME: {
                long sec = ((java.sql.Time)value).getTime();
                return new java.sql.Timestamp(sec);
            }
            case java.sql.Types.DATE: {
                long sec = ((java.sql.Date)value).getTime();
                return new java.sql.Timestamp(sec);
            }
            case java.sql.Types.CHAR:
            case java.sql.Types.VARCHAR:
            case java.sql.Types.LONGVARCHAR: {
                try {
                    DateFormat tf = DateFormat.getTimeInstance();
                    return ((java.sql.Timestamp)(tf.parse(value.toString())));
                } catch (ParseException ex) {
                    throw new SQLException(MessageFormat.format(rb.getString("GET_TIMESTAMP_FAILED"), //NOI18N
                            new Object[] { value.toString().trim(), new Integer(columnIndex) }));
                }
            }
            default: {
                throw new SQLException(MessageFormat.format(rb.getString("GET_TIMESTAMP2_FAILED"), //NOI18N
                        new Object[] { value.toString().trim(), new Integer(columnIndex) }));
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public java.io.InputStream getAsciiStream(int columnIndex) throws SQLException {
        checkExecuted();
        Object value;
        
        // always free an old stream
        asciiStream = null;
        
        // sanity check
        checkIndex(columnIndex);
        //make sure the cursor is on a vlid row
        checkCursor();
        
        value =  getCurrentRow().getColumnObject(columnIndex);
        if (value == null) {
            lastValueNull = true;
            return null;
        }
        
        try {
            if (isString(rowSetMD.getColumnType(columnIndex))) {
                asciiStream = new ByteArrayInputStream(((String)value).getBytes("ASCII")); //NOI18N
            } else {
                throw new SQLException(rb.getString("DATATYPE_MISMATCH")); //NOI18N
            }
        } catch (java.io.UnsupportedEncodingException ex) {
            throw new SQLException(ex.getMessage());
        }
        
        return (java.io.InputStream)asciiStream;
    }
    
    /**
     * {@inheritDoc}
     */
    public java.io.InputStream getUnicodeStream(int columnIndex) throws SQLException {
        checkExecuted();
        // always free an old stream
        unicodeStream = null;
        
        // sanity check.
        checkIndex(columnIndex);
        // make sure the cursor is on a valid row
        checkCursor();
        
        if (isBinary(rowSetMD.getColumnType(columnIndex)) == false &&
                isString(rowSetMD.getColumnType(columnIndex)) == false) {
            throw new SQLException(rb.getString("DATATYPE_MISMATCH")); //NOI18N
        }
        
        Object value = getCurrentRow().getColumnObject(columnIndex);
        if (value == null) {
            lastValueNull = true;
            return null;
        }
        
        unicodeStream = new StringBufferInputStream(value.toString());
        
        return (java.io.InputStream)unicodeStream;
    }
    
    /**
     * {@inheritDoc}
     */
    public java.io.InputStream getBinaryStream(int columnIndex) throws SQLException {
        checkExecuted();
        
        // always free an old stream
        binaryStream = null;
        
        // sanity check.
        checkIndex(columnIndex);
        // make sure the cursor is on a valid row
        checkCursor();
        
        if (isBinary(rowSetMD.getColumnType(columnIndex)) == false) {
            throw new SQLException(rb.getString("DATATYPE_MISMATCH")); //NOI18N
        }
        
        Object value = getCurrentRow().getColumnObject(columnIndex);
        if (value == null) {
            lastValueNull = true;
            return null;
        }
        
        binaryStream = new ByteArrayInputStream((byte[])value);
        
        return (java.io.InputStream)binaryStream;
        
    }
    
    
    //======================================================================
    // Methods for accessing results by column name
    //======================================================================
    
    /**
     * {@inheritDoc}
     */
    public String getString(String columnName) throws SQLException {
        checkExecuted();
        return getString(getColIdxByName(columnName));
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean getBoolean(String columnName) throws SQLException {
        checkExecuted();
        return getBoolean(getColIdxByName(columnName));
    }
    
    /**
     * {@inheritDoc}
     */
    public byte getByte(String columnName) throws SQLException {
        checkExecuted();
        return getByte(getColIdxByName(columnName));
    }
    
    /**
     * {@inheritDoc}
     */
    public short getShort(String columnName) throws SQLException {
        checkExecuted();
        return getShort(getColIdxByName(columnName));
    }
    
    /**
     * {@inheritDoc}
     */
    public int getInt(String columnName) throws SQLException {
        checkExecuted();
        return getInt(getColIdxByName(columnName));
    }
    
    /**
     * {@inheritDoc}
     */
    public long getLong(String columnName) throws SQLException {
        checkExecuted();
        return getLong(getColIdxByName(columnName));
    }
    
    /**
     * {@inheritDoc}
     */
    public float getFloat(String columnName) throws SQLException {
        checkExecuted();
        return getFloat(getColIdxByName(columnName));
    }
    
    /**
     * {@inheritDoc}
     */
    public double getDouble(String columnName) throws SQLException {
        checkExecuted();
        return getDouble(getColIdxByName(columnName));
    }
    
    /**
     * {@inheritDoc}
     */
    public BigDecimal getBigDecimal(String columnName, int scale) throws SQLException {
        checkExecuted();
        return getBigDecimal(getColIdxByName(columnName), scale);
    }
    
    /**
     * {@inheritDoc}
     */
    public byte[] getBytes(String columnName) throws SQLException {
        checkExecuted();
        return getBytes(getColIdxByName(columnName));
    }
    
    /**
     * {@inheritDoc}
     */
    public java.sql.Date getDate(String columnName) throws SQLException {
        checkExecuted();
        return getDate(getColIdxByName(columnName));
    }
    
    /**
     * {@inheritDoc}
     */
    public java.sql.Time getTime(String columnName) throws SQLException {
        checkExecuted();
        return getTime(getColIdxByName(columnName));
    }
    
    /**
     * {@inheritDoc}
     */
    public java.sql.Timestamp getTimestamp(String columnName) throws SQLException {
        checkExecuted();
        return getTimestamp(getColIdxByName(columnName));
    }
    
    /**
     * {@inheritDoc}
     */
    public java.io.InputStream getAsciiStream(String columnName) throws SQLException {
        checkExecuted();
        return getAsciiStream(getColIdxByName(columnName));
        
    }
    
    /**
     * {@inheritDoc}
     */
    public java.io.InputStream getUnicodeStream(String columnName) throws SQLException {
        checkExecuted();
        return getUnicodeStream(getColIdxByName(columnName));
    }
    
    /**
     * {@inheritDoc}
     */
    public java.io.InputStream getBinaryStream(String columnName) throws SQLException {
        checkExecuted();
        return getBinaryStream(getColIdxByName(columnName));
    }
    
    
    //=====================================================================
    // Advanced features:
    //=====================================================================
    
    /**
     * {@inheritDoc}
     */
    public SQLWarning getWarnings() {
        return sqlwarn;
    }
    
    /**
     * {@inheritDoc}
     */
    public void clearWarnings() {
        sqlwarn = null;
    }
    
    /**
     * {@inheritDoc}
     */
    public String getCursorName() throws SQLException {
        throw new SQLException(rb.getString("POSITIONED_UPDATES_NOT_SUPPORTED")); //NOI18N
    }
    
    /**
     * {@inheritDoc}
     */
    public ResultSetMetaData getMetaData() throws SQLException {

        Log.getLogger().entering(getClass().getName(), "getMetaData()");

        // if we haven't yet executed, rowSetMD will be null.
        // we don't want to execute here, so let's get a connection
        // and manufacture the rowsetmd
        if (rowSetMD == null) {

            Connection        tempConn = null;
            PreparedStatement tempPS   = null;
            try {
                tempConn = getConnection(getDataSourceName(), getUrl(),
                                         getUsername(), getPassword());
                Log.getLogger().finest("Connection: " + tempConn +
                                       "  DataSourceName: " + getDataSourceName() +
                                       "  URL: " + getUrl());
                tempPS = tempConn.prepareStatement(getCommand());
                // System.out.println("Prepared Statement: " + tempPS);

                rowSetMD = new RowSetMetaDataXImpl();
                ResultSetMetaData rsmd;
                try {
		    rsmd = tempPS.getMetaData();
                    Log.getLogger().finest("After calling tempPS.getMetaData(), rsmd: " + rsmd);

		    if (rsmd==null && "Oracle".equals(tempConn.getMetaData().getDatabaseProductName()))
		    {
			// Oracle 9 driver problem -- silently returns null rsmd
			rsmd = oracleFixup(tempConn, getCommand());
		    }
		} catch (SQLException e) {

                    Log.getLogger().finest("Exception caught: " + e.getErrorCode() + " : " + e.getMessage());
		    if (e.getErrorCode()==17144) {
			// Oracle 10 driver problem -- throws SQLException, "statement handle not executed"
			rsmd = oracleFixup(tempConn, getCommand());

                    } else {
                        // Some other problem.  Re-throw.
                        throw (e);
                    }
                }

                initMetaData(rowSetMD, rsmd);
                // System.out.println("After calling initMetaData, rsmd: " + rsmd);
            }
            // catch (SQLException e) {
            // }
            finally {
                if (tempPS != null) {
                    tempPS.close();
                }
                if (tempConn != null) {
                    try {
                        if (!tempConn.getAutoCommit()) {
                            tempConn.rollback();
                        }
                    } catch (Exception dummy) {
                        /*
                         * not an error condition, we're closing anyway, but
                         * we'd like to clean up any locks if we can since
                         * it is not clear the connection pool will clean
                         * these connections in a timely manner
                         */
                    }
                    tempConn.close();
                }
            }
        }
        Log.getLogger().exiting(getClass().getName(), "getMetaData()");
        return rowSetMD;
    }
    

    // Work around Oracle driver problem by executing modified command, to populate metadata
    // Strip off unnecessary stuff, and add where clause that is always false
    private ResultSetMetaData oracleFixup(Connection tempConn, String command)
	throws SQLException
    {
	Log.getLogger().entering("CachedRowSetXImpl", "oracleFixup()", command);
	String cmd = command;

	// Extract the GROUP BY, to use later
	String groupBy="";
	int pos = cmd.toUpperCase().indexOf("GROUP BY");
	if (pos != -1) {
	    groupBy = cmd.substring(pos);
	    cmd = cmd.substring(0,pos);
	}

	// Retain SELECT/FROM, remove WHERE/HAVING/ORDER BY
	String[] parts = cmd.split("(?i:where|having|order by)");

	// Attach new WHERE clause, previous GROUP BY
	String newCommand = parts[0] + " WHERE 1=0 " + groupBy;

	Log.getLogger().finest("Executing modified statement: " + newCommand);
	PreparedStatement tempPS = tempConn.prepareStatement(newCommand);
	tempPS.execute();
	return tempPS.getMetaData();
    }
    
	
    /**
     * {@inheritDoc}
     */
    public Object getObject(int columnIndex) throws SQLException {
        checkExecuted();
        Object value;
        java.util.Map map;
        
        // sanity check.
        checkIndex(columnIndex);
        // make sure the cursor is on a valid row
        checkCursor();
        
        setLastValueNull(false);
        value = getCurrentRow().getColumnObject(columnIndex);
        
        // check for SQL NULL
        if (value == null) {
            setLastValueNull(true);
            return null;
        }
        if (value instanceof Struct) {
            Struct s = (Struct)value;
            map = getTypeMap();
            // look up the class in the map
            Class c = (Class)map.get(s.getSQLTypeName());
            if (c != null) {
                // create new instance of the class
                SQLData obj = null;
                try {
                    obj = (SQLData)c.newInstance();
                } catch (java.lang.InstantiationException ex) {
                    throw new SQLException(rb.getString("UNABLE_TO_INSTANTIATE") + ex.getMessage()); //NOI18N
                } catch (java.lang.IllegalAccessException ex) {
                    throw new SQLException(rb.getString("UNABLE_TO_INSTANTIATE") + ex.getMessage()); //NOI18N
                }
                // get the attributes from the struct
                Object attribs[] = s.getAttributes(map);
                // create the SQLInput "stream"
                SQLInputImpl sqlInput = new SQLInputImpl(attribs, map);
                // read the values...
                obj.readSQL(sqlInput, s.getSQLTypeName());
                return (Object)obj;
            }
        }
        return value;
    }
    
    /**
     * {@inheritDoc}
     */
    public Object getObject(String columnName) throws SQLException {
        checkExecuted();
        return getObject(getColIdxByName(columnName));
    }
    
    //----------------------------------------------------------------
    
    /**
     * {@inheritDoc}
     */
    public int findColumn(String columnName) throws SQLException {
        return getColIdxByName(columnName);
    }
    
    
    //--------------------------JDBC 2.0-----------------------------------
    
    //---------------------------------------------------------------------
    // Getter's and Setter's
    //---------------------------------------------------------------------
    
    /**
     * {@inheritDoc}
     */
    public java.io.Reader getCharacterStream(int columnIndex) throws SQLException{
        checkExecuted();
        
        // sanity check.
        checkIndex(columnIndex);
        // make sure the cursor is on a valid row
        checkCursor();
        
        if (isBinary(rowSetMD.getColumnType(columnIndex))) {
            Object value = getCurrentRow().getColumnObject(columnIndex);
            if (value == null) {
                lastValueNull = true;
                return null;
            }
            charStream = new InputStreamReader
                    (new ByteArrayInputStream((byte[])value));
        } else if (isString(rowSetMD.getColumnType(columnIndex))) {
            Object value = getCurrentRow().getColumnObject(columnIndex);
            if (value == null) {
                lastValueNull = true;
                return null;
            }
            charStream = new StringReader(value.toString());
        } else {
            throw new SQLException(rb.getString("DATATYPE_MISMATCH")); //NOI18N
        }
        
        return (java.io.Reader)charStream;
    }
    
    /**
     * {@inheritDoc}
     */
    public java.io.Reader getCharacterStream(String columnName) throws SQLException {
        checkExecuted();
        return getCharacterStream(getColIdxByName(columnName));
    }
    
    /**
     * {@inheritDoc}
     */
    public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
        checkExecuted();
        Object value;
        
        // sanity check.
        checkIndex(columnIndex);
        // make sure the cursor is on a valid row
        checkCursor();
        
        setLastValueNull(false);
        value = getCurrentRow().getColumnObject(columnIndex);
        
        // check for SQL NULL
        if (value == null) {
            setLastValueNull(true);
            return null;
        }
        try {
            return (new BigDecimal(value.toString().trim()));
        } catch (NumberFormatException ex) {
            throw new SQLException(MessageFormat.format(rb.getString("GET_DOUBLE_FAILED"), //NOI18N
                    new Object[] { value.toString().trim(), new Integer(columnIndex) }));
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public BigDecimal getBigDecimal(String columnName) throws SQLException {
        checkExecuted();
        return getBigDecimal(getColIdxByName(columnName));
    }
    
    //---------------------------------------------------------------------
    // Traversal/Positioning
    //---------------------------------------------------------------------
    
    /**
     * {@inheritDoc}
     */
    public int size() {
        // checkExecuted(); !JK Doesn't allow SQLException!
        return numRows;
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean isBeforeFirst() throws SQLException {
        checkExecuted();
        if (cursorPos == 0 && numRows > 0) {
            return true;
        } else {
            return false;
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean isAfterLast() throws SQLException {
        checkExecuted();
        if (cursorPos == numRows+1 && numRows > 0) {
            return true;
        } else {
            return false;
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean isFirst() throws SQLException {
        checkExecuted();
        // this becomes nasty because of deletes.
        int saveCursorPos = cursorPos;
        int saveAbsoluteCursorPos = absolutePos;
        internalFirst();
        if (cursorPos == saveCursorPos) {
            return true;
        } else {
            cursorPos = saveCursorPos;
            absolutePos = saveAbsoluteCursorPos;
            return false;
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean isLast() throws SQLException {
        checkExecuted();
        int saveCursorPos = cursorPos;
        int saveAbsoluteCursorPos = absolutePos;
        boolean saveShowDeleted = getShowDeleted();
        setShowDeleted(true);
        internalLast();
        if (cursorPos == saveCursorPos) {
            setShowDeleted(saveShowDeleted);
            return true;
        } else {
            setShowDeleted(saveShowDeleted);
            cursorPos = saveCursorPos;
            absolutePos = saveAbsoluteCursorPos;
            return false;
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public void beforeFirst() throws SQLException {
        checkExecuted();
        if (getType() == ResultSet.TYPE_FORWARD_ONLY) {
            throw new SQLException(rb.getString("RESULTSET_TYPE_FORWARD_ONLY")); //NOI18N
        }
        cursorPos = 0;
        absolutePos = 0;
        notifyCursorMoved();
    }
    
    /**
     * {@inheritDoc}
     */
    public void afterLast() throws SQLException {
        checkExecuted();
        if (numRows > 0) {
            cursorPos = numRows + 1;
            absolutePos = 0;
            notifyCursorMoved();
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean first() throws SQLException {
        checkExecuted();
        if (getType() == ResultSet.TYPE_FORWARD_ONLY) {
            throw new SQLException(rb.getString("RESULTSET_TYPE_FORWARD_ONLY")); //NOI18N
        }
        
        // move and notify
        boolean ret = this.internalFirst();
        notifyCursorMoved();
        
        return ret;
    }
    
    protected boolean internalFirst() throws SQLException {
        boolean ret = false;
        
        if (numRows > 0) {
            cursorPos = 1;
            if ((getShowDeleted() == false) && (rowDeleted() == true)) {
                ret = internalNext();
            } else {
                ret = true;
            }
        }
        
        if (ret == true)
            absolutePos = 1;
        else
            absolutePos = 0;
        
        return ret;
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean last() throws SQLException {
        checkExecuted();
        if (getType() == ResultSet.TYPE_FORWARD_ONLY) {
            throw new SQLException(rb.getString("RESULTSET_TYPE_FORWARD_ONLY")); //NOI18N
        }
        
        // move and notify
        boolean ret = this.internalLast();
        notifyCursorMoved();
        
        return ret;
    }
    
    protected boolean internalLast() throws SQLException {
        boolean ret = false;
        
        if (numRows > 0) {
            cursorPos = numRows;
            if ((getShowDeleted() == false) && (rowDeleted() == true)) {
                ret = internalPrevious();
            } else {
                ret = true;
            }
        }
        if (ret == true)
            absolutePos = numRows - numDeleted;
        else
            absolutePos = 0;
        return ret;
    }
    
    /**
     * {@inheritDoc}
     */
    public int getRow() throws SQLException {
        checkExecuted();
        // are we on a valid row? Valid rows are between first and last
        if (numRows > 0 &&
                cursorPos > 0 &&
                cursorPos < (numRows + 1) &&
                (getShowDeleted() == false && rowDeleted() == false)) {
            return absolutePos;
        } else if (getShowDeleted() == true) {
            return cursorPos;
        } else {
            return 0;
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean absolute( int row ) throws SQLException {
        checkExecuted();
        if (row == 0 || getType() == ResultSet.TYPE_FORWARD_ONLY) {
            throw new SQLException(rb.getString("RESULTSET_TYPE_FORWARD_ONLY")); //NOI18N
        }
        
        if (row > 0) { // we are moving foward
            if (row > numRows) {
                // fell off the end
                afterLast();
                return false;
            } else {
                if (absolutePos <= 0)
                    internalFirst();
            }
        } else { // we are moving backward
            if (cursorPos + row < 0) {
                // fell off the front
                beforeFirst();
            } else {
                if (absolutePos >= 0)
                    internalLast();
            }
        }
        
        // Now move towards the absolute row that we're looking for
        while (absolutePos != row) {
            if (absolutePos < row) {
                if (!internalNext())
                    break;
            } else {
                if (!internalPrevious())
                    break;
            }
        }
        
        notifyCursorMoved();
        
        if (isAfterLast() || isBeforeFirst()) {
            return false;
        } else {
            return true;
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean relative(int rows) throws SQLException {
        checkExecuted();
        if (numRows == 0 || isBeforeFirst() ||
                isAfterLast() || getType() == ResultSet.TYPE_FORWARD_ONLY) {
            throw new SQLException(rb.getString("RESULTSET_TYPE_FORWARD_ONLY")); //NOI18N
        }
        
        if (rows == 0) {
            return true;
        }
        
        if (rows > 0) { // we are moving forward
            if (cursorPos + rows > numRows) {
                // fell off the end
                afterLast();
            } else {
                for (int i=0; i < rows; i++) {
                    if (!internalNext())
                        break;
                }
            }
        } else { // we are moving backward
            if (cursorPos + rows < 0) {
                // fell off the front
                beforeFirst();
            } else {
                for (int i=rows; i < 0; i++) {
                    if (!internalPrevious())
                        break;
                }
            }
        }
        notifyCursorMoved();
        
        if (isAfterLast() || isBeforeFirst()) {
            return false;
        } else {
            return true;
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean previous() throws SQLException {
        checkExecuted();
        if (getType() == ResultSet.TYPE_FORWARD_ONLY) {
            throw new SQLException(rb.getString("RESULTSET_TYPE_FORWARD_ONLY")); //NOI18N
        }
        /*
         * make sure things look sane. The cursor must be
         * positioned in the rowset or before first (0) or
         * after last (numRows + 1)
         */
        if (cursorPos < 0 || cursorPos > numRows + 1) {
            throw new SQLException(rb.getString("INVALID_CURSOR_POS")); //NOI18N
        }
        // move and notify
        boolean ret = this.internalPrevious();
        notifyCursorMoved();
        
        return ret;
    }
    
    protected boolean internalPrevious() throws SQLException {
        boolean ret = false;
        
        do {
            if (cursorPos > 1) {
                --cursorPos;
                ret = true;
            } else if (cursorPos == 1) {
                // decrement to before first
                --cursorPos;
                ret = false;
                break;
            }
        } while ((getShowDeleted() == false) && (rowDeleted() == true));
        
        /*
         * Each call to internalPrevious may move the cursor
         * over multiple rows, the absolute postion moves one one row
         */
        if (ret == true)
            --absolutePos;
        else
            absolutePos = 0;
        
        return ret;
    }
    
    
    //---------------------------------------------------------------------
    // Updates
    //---------------------------------------------------------------------
    
    /**
     * {@inheritDoc}
     */
    public boolean rowUpdated() throws SQLException {
        checkExecuted();
        // make sure the cursor is on a valid row
        checkCursor();
        if (onInsertRow == true) {
            throw new SQLException(rb.getString("INVALID_INSERT_ROW_OP")); // NOI18N
        }
        return(((Row)getCurrentRow()).getUpdated());
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean columnUpdated(int idx) throws SQLException {
        checkExecuted();
        // make sure the cursor is on a valid row
        checkCursor();
        if (onInsertRow == true) {
            throw new SQLException(rb.getString("INVALID_INSERT_ROW_OP")); // NOI18N
        }
        return (((Row)getCurrentRow()).getColUpdated(idx - 1));
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean columnUpdated(String columnName) throws SQLException {
        checkExecuted();
        return columnUpdated(getColIdxByName(columnName));
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean rowInserted() throws SQLException {
        checkExecuted();
        // make sure the cursor is on a valid row
        checkCursor();
        if (onInsertRow == true) {
            throw new SQLException(rb.getString("INVALID_INSERT_ROW_OP")); // NOI18N
        }
        return(((Row)getCurrentRow()).getInserted());
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean rowDeleted() throws SQLException {
        checkExecuted();
        // make sure the cursor is on a valid row
        
        if (isAfterLast() == true ||
                isBeforeFirst() == true ||
                onInsertRow == true) {
            
            throw new SQLException(rb.getString("INVALID_CURSOR_POS")); //NOI18N
        }
        return(((Row)getCurrentRow()).getDeleted());
    }
    
    /**
     * Indicates whether the given SQL data type is a numberic type.
     *
     * @param type one of the constants from <code>java.sql.Types</code>
     * @return <code>true</code> if the given type is <code>NUMERIC</code>,'
     *         <code>DECIMAL</code>, <code>BIT</code>, <code>TINYINT</code>,
     *         <code>SMALLINT</code>, <code>INTEGER</code>, <code>BIGINT</code>,
     *         <code>REAL</code>, <code>DOUBLE</code>, or <code>FLOAT</code>;
     *         <code>false</code> otherwise
     */
    private boolean isNumeric(int type) {
        switch (type) {
            case java.sql.Types.NUMERIC:
            case java.sql.Types.DECIMAL:
            case java.sql.Types.BIT:
            case java.sql.Types.TINYINT:
            case java.sql.Types.SMALLINT:
            case java.sql.Types.INTEGER:
            case java.sql.Types.BIGINT:
            case java.sql.Types.REAL:
            case java.sql.Types.DOUBLE:
            case java.sql.Types.FLOAT:
                return true;
            default:
                return false;
        }
    }
    
    /**
     * Indicates whether the given SQL data type is a string type.
     *
     * @param type one of the constants from <code>java.sql.Types</code>
     * @return <code>true</code> if the given type is <code>CHAR</code>,'
     *         <code>VARCHAR</code>, or <code>LONGVARCHAR</code>;
     *         <code>false</code> otherwise
     */
    private boolean isString(int type) {
        switch (type) {
            case java.sql.Types.CHAR:
            case java.sql.Types.VARCHAR:
            case java.sql.Types.LONGVARCHAR:
                return true;
            default:
                return false;
        }
    }
    
    /**
     * Indicates whether the given SQL data type is a binary type.
     *
     * @param type one of the constants from <code>java.sql.Types</code>
     * @return <code>true</code> if the given type is <code>BINARY</code>,'
     *         <code>VARBINARY</code>, or <code>LONGVARBINARY</code>;
     *         <code>false</code> otherwise
     */
    private boolean isBinary(int type) {
        switch (type) {
            case java.sql.Types.BINARY:
            case java.sql.Types.VARBINARY:
            case java.sql.Types.LONGVARBINARY:
                return true;
            default:
                return false;
        }
    }
    
    /**
     * Indicates whether the given SQL data type is a temporal type.
     * This method is called internally by the conversion methods
     * <code>convertNumeric</code> and <code>convertTemporal</code>.
     *
     * @param type one of the constants from <code>java.sql.Types</code>
     * @return <code>true</code> if the given type is <code>DATE</code>,
     *         <code>TIME</code>, or <code>TIMESTAMP</code>;
     *         <code>false</code> otherwise
     */
    private boolean isTemporal(int type) {
        switch (type) {
            case java.sql.Types.DATE:
            case java.sql.Types.TIME:
            case java.sql.Types.TIMESTAMP:
                return true;
            default:
                return false;
        }
    }
    
    /**
     * Converts the given <code>Object</code> in the Java programming language
     * to the standard mapping for the specified SQL target data type.
     * The conversion must be to a string or numeric type, but there are no
     * restrictions on the type to be converted.  If the source type and target
     * type are the same, the given object is simply returned.
     *
     * @param srcObj the <code>Object</code> in the Java programming language
     *               that is to be converted to the target type
     * @param srcType the data type that is the standard mapping in SQL of the
     *                object to be converted; must be one of the constants in
     *                <code>java.sql.Types</code>
     * @param trgType the SQL data type to which to convert the given object;
     *                must be one of the following constants in
     *                <code>java.sql.Types</code>: <code>NUMERIC</code>,
     *         <code>DECIMAL</code>, <code>BIT</code>, <code>TINYINT</code>,
     *         <code>SMALLINT</code>, <code>INTEGER</code>, <code>BIGINT</code>,
     *         <code>REAL</code>, <code>DOUBLE</code>, <code>FLOAT</code>,
     *         <code>VARCHAR</code>, <code>LONGVARCHAR</code>, or <code>CHAR</code>
     * @return an <code>Object</code> value.that is
     *         the standard object mapping for the target SQL type
     * @throws SQLException if the given target type is not one of the string or
     *         numeric types in <code>java.sql.Types</code>
     */
    private Object convertNumeric(Object srcObj, int srcType,
            int trgType) throws SQLException {
        
        if (srcType == trgType) {
            return srcObj;
        }
        
        if (isNumeric(trgType) == false && isString(trgType) == false) {
            throw new SQLException(rb.getString("DATATYPE_MISMATCH") + trgType); //NOI18N
        }
        
        try {
            switch (trgType) {
                case java.sql.Types.BIT:
                    Integer i = new Integer(srcObj.toString().trim());
                    return i.equals(new Integer((int)0)) ?
                        new Boolean(false) :
                        new Boolean(true);
                case java.sql.Types.TINYINT:
                    return new Byte(srcObj.toString().trim());
                case java.sql.Types.SMALLINT:
                    return new Short(srcObj.toString().trim());
                case java.sql.Types.INTEGER:
                    return new Integer(srcObj.toString().trim());
                case java.sql.Types.BIGINT:
                    return new Long(srcObj.toString().trim());
                case java.sql.Types.NUMERIC:
                case java.sql.Types.DECIMAL:
                    return new BigDecimal(srcObj.toString().trim());
                case java.sql.Types.REAL:
                case java.sql.Types.FLOAT:
                    return new Float(srcObj.toString().trim());
                case java.sql.Types.DOUBLE:
                    return new Double(srcObj.toString().trim());
                case java.sql.Types.CHAR:
                case java.sql.Types.VARCHAR:
                case java.sql.Types.LONGVARCHAR:
                    return new String(srcObj.toString());
                default:
                    throw new SQLException(rb.getString("DATATYPE_MISMATCH") + trgType); //NOI18N
            }
        } catch (NumberFormatException ex) {
            throw new SQLException(rb.getString("DATATYPE_MISMATCH") + trgType); //NOI18N
        }
    }
    
    /**
     * Converts the given <code>Object</code> in the Java programming language
     * to the standard object mapping for the specified SQL target data type.
     * The conversion must be to a string or temporal type, and there are also
     * restrictions on the type to be converted.
     * <P>
     * <TABLE ALIGN="CENTER" BORDER CELLPADDING=10 BORDERCOLOR="#0000FF"
     * <CAPTION ALIGN="CENTER"><B>Parameters and Return Values</B></CAPTION>
     * <TR>
     *   <TD><B>Source SQL Type</B>
     *   <TD><B>Target SQL Type</B>
     *   <TD><B>Object Returned</B>
     * </TR>
     * <TR>
     *   <TD><code>TIMESTAMP</code>
     *   <TD><code>DATE</code>
     *   <TD><code>java.sql.Date</code>
     * </TR>
     * <TR>
     *   <TD><code>TIMESTAMP</code>
     *   <TD><code>TIME</code>
     *   <TD><code>java.sql.Time</code>
     * </TR>
     * <TR>
     *   <TD><code>TIME</code>
     *   <TD><code>TIMESTAMP</code>
     *   <TD><code>java.sql.Timestamp</code>
     * </TR>
     * <TR>
     *   <TD><code>DATE</code>, <code>TIME</code>, or <code>TIMESTAMP</code>
     *   <TD><code>CHAR</code>, <code>VARCHAR</code>, or <code>LONGVARCHAR</code>
     *   <TD><code>java.lang.String</code>
     * </TR>
     * </TABLE>
     * <P>
     * If the source type and target type are the same,
     * the given object is simply returned.
     *
     * @param srcObj the <code>Object</code> in the Java programming language
     *               that is to be converted to the target type
     * @param srcType the data type that is the standard mapping in SQL of the
     *                object to be converted; must be one of the constants in
     *                <code>java.sql.Types</code>
     * @param trgType the SQL data type to which to convert the given object;
     *                must be one of the following constants in
     *                <code>java.sql.Types</code>: <code>DATE</code>,
     *         <code>TIME</code>, <code>TIMESTAMP</code>, <code>CHAR</code>,
     *         <code>VARCHAR</code>, or <code>LONGVARCHAR</code>
     * @return an <code>Object</code> value.that is
     *         the standard object mapping for the target SQL type
     * @throws SQLException if the given target type is not one of the string or
     *         temporal types in <code>java.sql.Types</code>
     */
    private Object convertTemporal(Object srcObj,
            int srcType, int trgType) throws SQLException {
        
        if (srcType == trgType) {
            return srcObj;
        }
        
        if (isNumeric(trgType) == true ||
                (isString(trgType) == false && isTemporal(trgType) == false)) {
            throw new SQLException(rb.getString("DATATYPE_MISMATCH") + trgType); //NOI18N
        }
        
        try {
            switch (trgType) {
                case java.sql.Types.DATE:
                    if (srcType == java.sql.Types.TIMESTAMP) {
                        return new java.sql.Date(((java.sql.Timestamp)srcObj).getTime());
                    } else {
                        throw new SQLException(rb.getString("DATATYPE_MISMATCH")); //NOI18N
                    }
                case java.sql.Types.TIMESTAMP:
                    if (srcType == java.sql.Types.TIME) {
                        return new Timestamp(((java.sql.Time)srcObj).getTime());
                    } else {
                        return new Timestamp(((java.sql.Date)srcObj).getTime());
                    }
                case java.sql.Types.TIME:
                    if (srcType == java.sql.Types.TIMESTAMP) {
                        return new Time(((java.sql.Timestamp)srcObj).getTime());
                    } else {
                        throw new SQLException(rb.getString("DATATYPE_MISMATCH")); //NOI18N
                    }
                case java.sql.Types.CHAR:
                case java.sql.Types.VARCHAR:
                case java.sql.Types.LONGVARCHAR:
                    return new String(srcObj.toString());
                default:
                    throw new SQLException(rb.getString("DATATYPE_MISMATCH")); //NOI18N
            }
        } catch (NumberFormatException ex) {
            throw new SQLException(rb.getString("DATATYPE_MISMATCH")); //NOI18N
        }
        
    }
    
    /**
     * {@inheritDoc}
     */
    public void updateNull(int columnIndex) throws SQLException {
        checkExecuted();
        // sanity check.
        checkIndex(columnIndex);
        // make sure the cursor is on a valid row
        checkCursor();
        
        BaseRow row = getCurrentRow();
        row.setColumnObject(columnIndex, null);
    }
    
    /**
     * {@inheritDoc}
     */
    public void updateBoolean(int columnIndex, boolean x) throws SQLException {
        checkExecuted();
        // sanity check.
        checkIndex(columnIndex);
        // make sure the cursor is on a valid row
        checkCursor();
        Object obj = convertNumeric(new Boolean(x),
                java.sql.Types.BIT,
                rowSetMD.getColumnType(columnIndex));
        
        getCurrentRow().setColumnObject(columnIndex, obj);
    }
    
    /**
     * {@inheritDoc}
     */
    public void updateByte(int columnIndex, byte x) throws SQLException {
        checkExecuted();
        // sanity check.
        checkIndex(columnIndex);
        // make sure the cursor is on a valid row
        checkCursor();
        
        Object obj = convertNumeric(new Byte(x),
                java.sql.Types.TINYINT,
                rowSetMD.getColumnType(columnIndex));
        
        getCurrentRow().setColumnObject(columnIndex, obj);
    }
    
    /**
     * {@inheritDoc}
     */
    public void updateShort(int columnIndex, short x) throws SQLException {
        checkExecuted();
        // sanity check.
        checkIndex(columnIndex);
        // make sure the cursor is on a valid row
        checkCursor();
        
        Object obj = convertNumeric(new Short(x),
                java.sql.Types.SMALLINT,
                rowSetMD.getColumnType(columnIndex));
        
        getCurrentRow().setColumnObject(columnIndex, obj);
    }
    
    /**
     * {@inheritDoc}
     */
    public void updateInt(int columnIndex, int x) throws SQLException {
        checkExecuted();
        // sanity check.
        checkIndex(columnIndex);
        // make sure the cursor is on a valid row
        checkCursor();
        Object obj = convertNumeric(new Integer(x),
                java.sql.Types.INTEGER,
                rowSetMD.getColumnType(columnIndex));
        
        getCurrentRow().setColumnObject(columnIndex, obj);
    }
    
    /**
     * {@inheritDoc}
     */
    public void updateLong(int columnIndex, long x) throws SQLException {
        checkExecuted();
        // sanity check.
        checkIndex(columnIndex);
        // make sure the cursor is on a valid row
        checkCursor();
        
        Object obj = convertNumeric(new Long(x),
                java.sql.Types.BIGINT,
                rowSetMD.getColumnType(columnIndex));
        
        getCurrentRow().setColumnObject(columnIndex, obj);
        
    }
    
    /**
     * {@inheritDoc}
     */
    public void updateFloat(int columnIndex, float x) throws SQLException {
        checkExecuted();
        // sanity check.
        checkIndex(columnIndex);
        // make sure the cursor is on a valid row
        checkCursor();
        
        Object obj = convertNumeric(new Float(x),
                java.sql.Types.REAL,
                rowSetMD.getColumnType(columnIndex));
        
        getCurrentRow().setColumnObject(columnIndex, obj);
    }
    
    /**
     * {@inheritDoc}
     */
    public void updateDouble(int columnIndex, double x) throws SQLException {
        checkExecuted();
        // sanity check.
        checkIndex(columnIndex);
        // make sure the cursor is on a valid row
        checkCursor();
        Object obj = convertNumeric(new Double(x),
                java.sql.Types.DOUBLE,
                rowSetMD.getColumnType(columnIndex));
        
        getCurrentRow().setColumnObject(columnIndex, obj);
    }
    
    /**
     * {@inheritDoc}
     */
    public void updateBigDecimal(int columnIndex, BigDecimal x) throws SQLException {
        checkExecuted();
        // sanity check.
        checkIndex(columnIndex);
        // make sure the cursor is on a valid row
        checkCursor();
        
        Object obj = convertNumeric(x,
                java.sql.Types.NUMERIC,
                rowSetMD.getColumnType(columnIndex));
        
        getCurrentRow().setColumnObject(columnIndex, obj);
    }
    
    /**
     * {@inheritDoc}
     */
    public void updateString(int columnIndex, String x) throws SQLException {
        checkExecuted();
        // sanity check.
        checkIndex(columnIndex);
        // make sure the cursor is on a valid row
        checkCursor();
        
        getCurrentRow().setColumnObject(columnIndex, x);
    }
    
    /**
     * {@inheritDoc}
     */
    public void updateBytes(int columnIndex, byte x[]) throws SQLException {
        checkExecuted();
        // sanity check.
        checkIndex(columnIndex);
        // make sure the cursor is on a valid row
        checkCursor();
        
        if (isBinary(rowSetMD.getColumnType(columnIndex)) == false) {
            throw new SQLException(rb.getString("DATATYPE_MISMATCH")); //NOI18N
        }
        
        getCurrentRow().setColumnObject(columnIndex, x);
    }
    
    /**
     * {@inheritDoc}
     */
    public void updateDate(int columnIndex, java.sql.Date x) throws SQLException {
        checkExecuted();
        // sanity check.
        checkIndex(columnIndex);
        // make sure the cursor is on a valid row
        checkCursor();
        
        Object obj = convertTemporal(x,
                java.sql.Types.DATE,
                rowSetMD.getColumnType(columnIndex));
        
        getCurrentRow().setColumnObject(columnIndex, obj);
    }
    
    /**
     * {@inheritDoc}
     */
    public void updateTime(int columnIndex, java.sql.Time x) throws SQLException {
        checkExecuted();
        // sanity check.
        checkIndex(columnIndex);
        // make sure the cursor is on a valid row
        checkCursor();
        
        Object obj = convertTemporal(x,
                java.sql.Types.TIME,
                rowSetMD.getColumnType(columnIndex));
        
        getCurrentRow().setColumnObject(columnIndex, obj);
    }
    
    /**
     * {@inheritDoc}
     */
    public void updateTimestamp(int columnIndex, java.sql.Timestamp x) throws SQLException {
        checkExecuted();
        // sanity check.
        checkIndex(columnIndex);
        // make sure the cursor is on a valid row
        checkCursor();
        
        Object obj = convertTemporal(x,
                java.sql.Types.TIMESTAMP,
                rowSetMD.getColumnType(columnIndex));
        
        getCurrentRow().setColumnObject(columnIndex, obj);
    }
    
    /**
     * {@inheritDoc}
     */
    public void updateAsciiStream(int columnIndex, java.io.InputStream x, int length) throws SQLException {
        checkExecuted();
        // sanity Check
        checkIndex(columnIndex);
        // make sure the cursor is on a valid row
        checkCursor();
        
        
        if (isString(rowSetMD.getColumnType(columnIndex)) == false &&
                isBinary(rowSetMD.getColumnType(columnIndex)) == false) {
            throw new SQLException(rb.getString("DATATYPE_MISMATCH")); //NOI18N
        }
        
        byte buf[] = new byte[length];
        try {
            int charsRead = 0;
            do {
                charsRead += x.read(buf, charsRead, length - charsRead);
            } while (charsRead != length);
            //Changed the condition check to check for length instead of -1
        } catch (java.io.IOException ex) {
            throw new SQLException(rb.getString("READ_FAILED_ASCII_STREAM")); //NOI18N
        }
        String str = new String(buf);
        
        getCurrentRow().setColumnObject(columnIndex, str);
        
    }
    
    /**
     * {@inheritDoc}
     */
    public void updateBinaryStream(int columnIndex, java.io.InputStream x,int length) throws SQLException {
        checkExecuted();
        // sanity Check
        checkIndex(columnIndex);
        // make sure the cursor is on a valid row
        checkCursor();
        
        if (isBinary(rowSetMD.getColumnType(columnIndex)) == false) {
            throw new SQLException(rb.getString("DATATYPE_MISMATCH")); //NOI18N
        }
        
        byte buf[] = new byte[length];
        try {
            int bytesRead = 0;
            do {
                bytesRead += x.read(buf, bytesRead, length - bytesRead);
            } while (bytesRead != -1);
        } catch (java.io.IOException ex) {
            throw new SQLException(rb.getString("READ_FAILED_BINARY_STREAM")); //NOI18N
        }
        
        getCurrentRow().setColumnObject(columnIndex, buf);
    }
    
    /**
     * {@inheritDoc}
     */
    public void updateCharacterStream(int columnIndex, java.io.Reader x, int length) throws SQLException {
        checkExecuted();
        // sanity Check
        checkIndex(columnIndex);
        // make sure the cursor is on a valid row
        checkCursor();
        
        if (isString(rowSetMD.getColumnType(columnIndex)) == false &&
                isBinary(rowSetMD.getColumnType(columnIndex)) == false) {
            throw new SQLException(rb.getString("DATATYPE_MISMATCH")); //NOI18N
        }
        
        char buf[] = new char[length];
        try {
            int charsRead = 0;
            do {
                charsRead += x.read(buf, charsRead, length - charsRead);
            } while (charsRead != length);
            //Changed the condition checking to check for length instead of -1
        } catch (java.io.IOException ex) {
            throw new SQLException(rb.getString("READ_FAILED_BINARY_STREAM")); //NOI18N
        }
        String str = new String(buf);
        
        getCurrentRow().setColumnObject(columnIndex, str);
    }
    
    /**
     * {@inheritDoc}
     */
    public void updateObject(int columnIndex, Object x, int scale) throws SQLException {
        checkExecuted();
        // sanity check.
        checkIndex(columnIndex);
        // make sure the cursor is on a valid row
        checkCursor();
        
        int type = rowSetMD.getColumnType(columnIndex);
        if (type == Types.DECIMAL || type == Types.NUMERIC) {
            ((java.math.BigDecimal)x).setScale(scale);
        }
        getCurrentRow().setColumnObject(columnIndex, x);
    }
    
    /**
     * {@inheritDoc}
     */
    public void updateObject(int columnIndex, Object x) throws SQLException {
        checkExecuted();
        // sanity check.
        checkIndex(columnIndex);
        // make sure the cursor is on a valid row
        checkCursor();
        
        getCurrentRow().setColumnObject(columnIndex, x);
    }
    
    /**
     * {@inheritDoc}
     */
    public void updateNull(String columnName) throws SQLException {
        checkExecuted();
        updateNull(getColIdxByName(columnName));
    }
    
    /**
     * {@inheritDoc}
     */
    public void updateBoolean(String columnName, boolean x) throws SQLException {
        checkExecuted();
        updateBoolean(getColIdxByName(columnName), x);
    }
    
    public void updateByte(String columnName, byte x) throws SQLException {
        checkExecuted();
        updateByte(getColIdxByName(columnName), x);
    }
    
    public void updateShort(String columnName, short x) throws SQLException {
        checkExecuted();
        updateShort(getColIdxByName(columnName), x);
    }
    
    public void updateInt(String columnName, int x) throws SQLException {
        checkExecuted();
        updateInt(getColIdxByName(columnName), x);
    }
    
    public void updateLong(String columnName, long x) throws SQLException {
        checkExecuted();
        updateLong(getColIdxByName(columnName), x);
    }
    
    public void updateFloat(String columnName, float x) throws SQLException {
        checkExecuted();
        updateFloat(getColIdxByName(columnName), x);
    }
    
    public void updateDouble(String columnName, double x) throws SQLException {
        checkExecuted();
        updateDouble(getColIdxByName(columnName), x);
    }
    
    public void updateBigDecimal(String columnName, BigDecimal x) throws SQLException {
        checkExecuted();
        updateBigDecimal(getColIdxByName(columnName), x);
    }
    
    public void updateString(String columnName, String x) throws SQLException {
        checkExecuted();
        updateString(getColIdxByName(columnName), x);
    }
    
    public void updateBytes(String columnName, byte x[]) throws SQLException {
        checkExecuted();
        updateBytes(getColIdxByName(columnName), x);
    }
    
    public void updateDate(String columnName, java.sql.Date x) throws SQLException {
        checkExecuted();
        updateDate(getColIdxByName(columnName), x);
    }
    
    public void updateTime(String columnName, java.sql.Time x) throws SQLException {
        checkExecuted();
        updateTime(getColIdxByName(columnName), x);
    }
    
    public void updateTimestamp(String columnName, java.sql.Timestamp x) throws SQLException {
        checkExecuted();
        updateTimestamp(getColIdxByName(columnName), x);
    }
    
    public void updateAsciiStream(String columnName,
            java.io.InputStream x,
            int length) throws SQLException {
        checkExecuted();
        updateAsciiStream(getColIdxByName(columnName), x, length);
    }
    
    public void updateBinaryStream(String columnName, java.io.InputStream x, int length) throws SQLException {
        checkExecuted();
        updateBinaryStream(getColIdxByName(columnName), x, length);
    }
    
    public void updateCharacterStream(String columnName,
            java.io.Reader reader,
            int length) throws SQLException {
        checkExecuted();
        updateCharacterStream(getColIdxByName(columnName), reader, length);
    }
    
    public void updateObject(String columnName, Object x, int scale) throws SQLException {
        checkExecuted();
        updateObject(getColIdxByName(columnName), x, scale);
    }
    
    public void updateObject(String columnName, Object x) throws SQLException {
        checkExecuted();
        updateObject(getColIdxByName(columnName), x);
    }
    
    public void insertRow() throws SQLException {
        checkExecuted();
        int pos;
        
        if (onInsertRow == false) {
            throw new SQLException(rb.getString("INSERT_ROW_FAILED")); //NOI18N
        }
        /* We cannot reliably compute whether or not this is a complete row without
         * obtaining a connection to see what is and isn't an insertable column.  For
         * now, we will relax this requirement.  In the future, perhaps we will obtain this
         * information when the rowset is populated -- and then hand it off to the writer
         * (which is currently computing this type of information.
         *
        if (insertRow.isCompleteRow(rowSetMD) == false) {
            throw new SQLException(rb.getString("INSERT_ROW_FAILED")); //NOI18N
        }
         */
        // Added the setting of parameters that are passed
        // to setXXX methods after an empty CRS Object is
        // created through RowSetMetaData object
        
        // removed what came from the RI version - bug 6345804
        
        Row insRow = new Row(rowSetMD.getColumnCount(),
                insertRow.getOrigRow());
        insRow.setInserted();
        /*
         * The new row is inserted into the RowSet
         * immediately following the current row.
         *
         * If we are afterlast then the rows are
         * inserted at the end.
         */
        if (currentRow >= numRows || currentRow < 0) {
            pos = numRows;
        } else {
            pos = currentRow;
        }
        
        rvh.add(pos, insRow);
        ++numRows;
        onInsertRow = false;
        cursorPos = currentRow; /*!JK: aded this */
        // notify the listeners that the row changed.
        notifyRowChanged();
    }
    
    public void updateRow() throws SQLException {
        checkExecuted();
        // make sure we aren't on the insert row
        if (onInsertRow == true) {
            throw new SQLException(rb.getString("UPDATE_ROW_CALLED_ON_INSERT_ROW")); //NOI18N
        }
        
        ((Row)getCurrentRow()).setUpdated();
        
        // notify the listeners that the row changed.
        notifyRowChanged();
    }
    
    public void deleteRow() throws SQLException {
        checkExecuted();
        // make sure the cursor is on a valid row
        checkCursor();
        
        ((Row)getCurrentRow()).setDeleted();
        ++numDeleted;
        
        // notify the listeners that the row changed.
        notifyRowChanged();
    }
    
    public void refreshRow() throws SQLException {
        checkExecuted();
        // make sure we are on a row
        checkCursor();
        
        // don't want this to happen...
        if (onInsertRow == true) {
            throw new SQLException(rb.getString("INVALID_CURSOR_POS")); //NOI18N
        }
        
        Row currentRow = (Row)getCurrentRow();
        // just undo any changes made to this row.
        currentRow.clearUpdated();
        
    }
    
    public void cancelRowUpdates() throws SQLException {
        checkExecuted();
        // make sure we are on a row
        checkCursor();
        
        // don't want this to happen...
        if (onInsertRow == true) {
            throw new SQLException(rb.getString("INVALID_CURSOR_POS")); //NOI18N
        }
        
        Row currentRow = (Row)getCurrentRow();
        if (currentRow.getUpdated() == true) {
            currentRow.clearUpdated();
            notifyRowChanged();
        }
    }
    
    public void moveToInsertRow() throws SQLException {
        checkExecuted();
        if (getConcurrency() == ResultSet.CONCUR_READ_ONLY) {
            throw new SQLException(rb.getString("RESULTSET_IS_READONLY")); //NOI18N
        }
        if (insertRow == null) {
            if (rowSetMD == null)
                throw new SQLException(rb.getString("MOVE_TO_INSERT_ROW_NO_MD")); //NOI18N
            int numCols = rowSetMD.getColumnCount();
            if (numCols > 0) {
                insertRow = new InsertRow(numCols);
            } else {
                throw new SQLException(rb.getString("MOVE_TO_INSERT_ROW_INVALID_NO_OF_COLS")); //NOI18N
            }
        }
        onInsertRow = true;
        // %%% setCurrentRow called in BaseRow
        
        currentRow = cursorPos;
        cursorPos = -1;
        
        insertRow.initInsertRow();
    }
    
    public void moveToCurrentRow() throws SQLException {
        checkExecuted();
        if (onInsertRow == false) {
            return;
        } else {
            cursorPos = currentRow;
            onInsertRow = false;
        }
    }
    
    /**
     * Returns <code>null</code>.
     *
     * @return <code>null</code>
     * @throws SQLException if an error occurs
     */
    public Statement getStatement() throws SQLException {
        return null;
    }
    
    public Object getObject(int columnIndex, java.util.Map map) throws SQLException {
        checkExecuted();
        Object value;
        
        // sanity check.
        checkIndex(columnIndex);
        // make sure the cursor is on a valid row
        checkCursor();
        
        setLastValueNull(false);
        value = getCurrentRow().getColumnObject(columnIndex);
        
        // check for SQL NULL
        if (value == null) {
            setLastValueNull(true);
            return null;
        }
        if (value instanceof Struct) {
            Struct s = (Struct)value;
            
            // look up the class in the map
            Class c = (Class)map.get(s.getSQLTypeName());
            if (c != null) {
                // create new instance of the class
                SQLData obj = null;
                try {
                    obj = (SQLData)c.newInstance();
                } catch (java.lang.InstantiationException ex) {
                    throw new SQLException(rb.getString("UNABLE_TO_INSTANTIATE") + ex.getMessage()); //NOI18N
                } catch (java.lang.IllegalAccessException ex) {
                    throw new SQLException(rb.getString("UNABLE_TO_INSTANTIATE") + ex.getMessage()); //NOI18N
                }
                // get the attributes from the struct
                Object attribs[] = s.getAttributes(map);
                // create the SQLInput "stream"
                SQLInputImpl sqlInput = new SQLInputImpl(attribs, map);
                // read the values...
                obj.readSQL(sqlInput, s.getSQLTypeName());
                return (Object)obj;
            }
        }
        return value;
    }
    
    public Ref getRef(int columnIndex) throws SQLException {
        checkExecuted();
        Ref value;
        
        // sanity check.
        checkIndex(columnIndex);
        // make sure the cursor is on a valid row
        checkCursor();
        
        if (rowSetMD.getColumnType(columnIndex) != java.sql.Types.REF) {
            throw new SQLException(rb.getString("DATATYPE_MISMATCH")); //NOI18N
        }
        
        setLastValueNull(false);
        value = (Ref)(getCurrentRow().getColumnObject(columnIndex));
        
        // check for SQL NULL
        if (value == null) {
            setLastValueNull(true);
            return null;
        }
        
        return value;
    }
    
    public Blob getBlob(int columnIndex) throws SQLException {
        checkExecuted();
        Blob value;
        
        // sanity check.
        checkIndex(columnIndex);
        // make sure the cursor is on a valid row
        checkCursor();
        
        if (rowSetMD.getColumnType(columnIndex) != java.sql.Types.BLOB) {
            //System.out.println("Type is: "+rowSetMD.getColumnType(columnIndex));
            throw new SQLException(rb.getString("DATATYPE_MISMATCH")); //NOI18N
        }
        
        setLastValueNull(false);
        value = (Blob)(getCurrentRow().getColumnObject(columnIndex));
        
        // check for SQL NULL
        if (value == null) {
            setLastValueNull(true);
            return null;
        }
        
        return value;
    }
    
    public Clob getClob(int columnIndex) throws SQLException {
        checkExecuted();
        Clob value;
        
        // sanity check.
        checkIndex(columnIndex);
        // make sure the cursor is on a valid row
        checkCursor();
        
        if (rowSetMD.getColumnType(columnIndex) != java.sql.Types.CLOB) {
            //System.out.println("Type is: "+rowSetMD.getColumnType(columnIndex));
            throw new SQLException(rb.getString("DATATYPE_MISMATCH")); //NOI18N
        }
        
        setLastValueNull(false);
        value = (Clob)(getCurrentRow().getColumnObject(columnIndex));
        
        // check for SQL NULL
        if (value == null) {
            setLastValueNull(true);
            return null;
        }
        
        return value;
    }
    
    public Array getArray(int columnIndex) throws SQLException {
        checkExecuted();
        java.sql.Array value;
        
        // sanity check.
        checkIndex(columnIndex);
        // make sure the cursor is on a valid row
        checkCursor();
        
        if (rowSetMD.getColumnType(columnIndex) != java.sql.Types.ARRAY) {
            throw new SQLException(rb.getString("DATATYPE_MISMATCH")); //NOI18N
        }
        
        setLastValueNull(false);
        value = (java.sql.Array)(getCurrentRow().getColumnObject(columnIndex));
        
        // check for SQL NULL
        if (value == null) {
            setLastValueNull(true);
            return null;
        }
        
        return value;
    }
    
    public Object getObject(String columnName, java.util.Map map)
    throws SQLException {
        checkExecuted();
        return getObject(getColIdxByName(columnName), map);
    }
    
    public Ref getRef(String colName) throws SQLException {
        checkExecuted();
        return getRef(getColIdxByName(colName));
    }
    
    public Blob getBlob(String colName) throws SQLException {
        checkExecuted();
        return getBlob(getColIdxByName(colName));
    }
    
    public Clob getClob(String colName) throws SQLException {
        checkExecuted();
        return getClob(getColIdxByName(colName));
    }
    
    public Array getArray(String colName) throws SQLException {
        checkExecuted();
        return getArray(getColIdxByName(colName));
    }
    
    public java.sql.Date getDate(int columnIndex, Calendar cal) throws SQLException {
        checkExecuted();
        Object value;
        
        // sanity check.
        checkIndex(columnIndex);
        // make sure the cursor is on a valid row
        checkCursor();
        
        setLastValueNull(false);
        value = getCurrentRow().getColumnObject(columnIndex);
        
        // check for SQL NULL
        if (value == null) {
            setLastValueNull(true);
            return null;
        }
        
        value = convertTemporal(value,
                rowSetMD.getColumnType(columnIndex),
                java.sql.Types.DATE);
        
        // create a default calendar
        Calendar defaultCal = Calendar.getInstance();
        // set this Calendar to the time we have
        defaultCal.setTime((java.util.Date)value);
        
        /*
         * Now we can pull the pieces of the date out
         * of the default calendar and put them into
         * the user provided calendar
         */
        cal.set(Calendar.YEAR, defaultCal.get(Calendar.YEAR));
        cal.set(Calendar.MONTH, defaultCal.get(Calendar.MONTH));
        cal.set(Calendar.DAY_OF_MONTH, defaultCal.get(Calendar.DAY_OF_MONTH));
        
        /*
         * This looks a little odd but it is correct -
         * Calendar.getTime() returns a Date...
         */
        return new java.sql.Date(cal.getTime().getTime());
    }
    
    public java.sql.Date getDate(String columnName, Calendar cal) throws SQLException {
        checkExecuted();
        return getDate(getColIdxByName(columnName), cal);
    }
    
    public java.sql.Time getTime(int columnIndex, Calendar cal) throws SQLException {
        checkExecuted();
        Object value;
        
        // sanity check.
        checkIndex(columnIndex);
        // make sure the cursor is on a valid row
        checkCursor();
        
        setLastValueNull(false);
        value = getCurrentRow().getColumnObject(columnIndex);
        
        // check for SQL NULL
        if (value == null) {
            setLastValueNull(true);
            return null;
        }
        
        value = convertTemporal(value,
                rowSetMD.getColumnType(columnIndex),
                java.sql.Types.TIME);
        
        // create a default calendar
        Calendar defaultCal = Calendar.getInstance();
        // set the time in the default calendar
        defaultCal.setTime((java.util.Date)value);
        
        /*
         * Now we can pull the pieces of the date out
         * of the default calendar and put them into
         * the user provided calendar
         */
        cal.set(Calendar.HOUR_OF_DAY, defaultCal.get(Calendar.HOUR_OF_DAY));
        cal.set(Calendar.MINUTE, defaultCal.get(Calendar.MINUTE));
        cal.set(Calendar.SECOND, defaultCal.get(Calendar.SECOND));
        
        return new java.sql.Time(cal.getTime().getTime());
    }
    
    public java.sql.Time getTime(String columnName, Calendar cal) throws SQLException {
        checkExecuted();
        return getTime(getColIdxByName(columnName), cal);
    }
    
    public java.sql.Timestamp getTimestamp(int columnIndex, Calendar cal) throws SQLException {
        checkExecuted();
        Object value;
        
        // sanity check.
        checkIndex(columnIndex);
        // make sure the cursor is on a valid row
        checkCursor();
        
        setLastValueNull(false);
        value = getCurrentRow().getColumnObject(columnIndex);
        
        // check for SQL NULL
        if (value == null) {
            setLastValueNull(true);
            return null;
        }
        
        value = convertTemporal(value,
                rowSetMD.getColumnType(columnIndex),
                java.sql.Types.TIMESTAMP);
        
        // create a default calendar
        Calendar defaultCal = Calendar.getInstance();
        // set the time in the default calendar
        defaultCal.setTime((java.util.Date)value);
        
        /*
         * Now we can pull the pieces of the date out
         * of the default calendar and put them into
         * the user provided calendar
         */
        cal.set(Calendar.YEAR, defaultCal.get(Calendar.YEAR));
        cal.set(Calendar.MONTH, defaultCal.get(Calendar.MONTH));
        cal.set(Calendar.DAY_OF_MONTH, defaultCal.get(Calendar.DAY_OF_MONTH));
        cal.set(Calendar.HOUR_OF_DAY, defaultCal.get(Calendar.HOUR_OF_DAY));
        cal.set(Calendar.MINUTE, defaultCal.get(Calendar.MINUTE));
        cal.set(Calendar.SECOND, defaultCal.get(Calendar.SECOND));
        
        return new java.sql.Timestamp(cal.getTime().getTime());
    }
    
    public java.sql.Timestamp getTimestamp(String columnName, Calendar cal) throws SQLException {
        checkExecuted();
        return getTimestamp(getColIdxByName(columnName), cal);
    }
    
    /*
     * RowSetInternal Interface
     */
    
    /**
     * Retrieves the <code>Connection</code> object passed to this
     * <code>CachedRowSetXImpl</code> object.  This connection may be
     * used to populate this rowset with data or to write data back
     * to its underlying data source.
     *
     * @return the <code>Connection</code> object passed to this rowset;
     *         may be <code>null</code> if there is no connection
     * @throws SQLException if an error occurs
     */
    public Connection getConnection() throws SQLException{
        
        return conn;
    }
    
    /**
     * Given a dataSourceName, url, username and password, create a connection
     * If dataSourceName is null, assume a Driver Manager connection
     * If username is null, don't pass them to the lookup method
     *
     * @return the <code>Connection</code> object
     *
     * @throws SQLException if an error occurs
     */
    static private Connection getConnection(String dataSourceName,
            String url,
            String username,
            String password) throws SQLException {

        Log.getLogger().entering("CachedRowSetXImpl", "getConnection()",
                                 new Object[] {dataSourceName, url, username, password});

        Connection conn = null;
        if (dataSourceName != null) {
            // Connect using JNDI.
            try {
                Context ctx = new InitialContext();
                Log.getLogger().finest("About to get DataSource, ctx: " + ctx);

                DataSource ds = (DataSource)ctx.lookup(dataSourceName);
                
                Log.getLogger().finest("About to get connection, DataSource: " + ds);

                if(username == null || username.equals("")) { //NOI18N
                    conn = ds.getConnection();
                } else {
                    conn = ds.getConnection(username, password);
                }
            } catch (javax.naming.NamingException ex) {
                Log.getLogger().finest("Caught exception: " + ex + " " + ex.getMessage());
                throw new SQLException(rb.getString("UNABLE_TO_CONNECT")+ex.getMessage()); //NOI18N
            }
        } else if (url != null) {
            // Check only for url != null because
            // user, passwd can be null
            // Connect using the driver manager.
            conn = DriverManager.getConnection(url, username, password);
        } else {
            throw new SQLException(rb.getString("BOTH_DS_AND_URL_CANNOT_BE_NULL")); //NOI18N
        }
        return conn;
    }
    
    public void setMetaData(RowSetMetaData md) throws SQLException {
        rowSetMD =(RowSetMetaDataXImpl) md;
    }
    
    public ResultSet getOriginal() throws SQLException {
        checkExecuted();
        CachedRowSetXImpl crs = CachedRowSetXImpl.createInternalUseInstance();
        crs.rowSetMD = rowSetMD;
        crs.numRows = numRows;
        crs.cursorPos = 0;
        
        // make sure we don't get someone playing with these
        // %%% is this now necessary ???
        //crs.setReader(null);
        //crs.setWriter(null);
        int colCount = rowSetMD.getColumnCount();
        Row orig;
        
        for (Iterator i = rvh.iterator(); i.hasNext();) {
            orig = new Row(colCount, ((Row)i.next()).getOrigRow());
            crs.rvh.add(orig);
        }
        return (ResultSet)crs;
    }
    
    public ResultSet getOriginalRow() throws SQLException {
        checkExecuted();
        CachedRowSetXImpl crs = CachedRowSetXImpl.createInternalUseInstance();
        crs.rowSetMD = rowSetMD;
        crs.numRows = 1;
        crs.cursorPos = 0;
        
        // make sure we don't get someone playing with these
        // %%% is this now necessary ???
        //crs.setReader(null);
        //crs.setWriter(null);
        
        Row orig = new Row(rowSetMD.getColumnCount(),
                getCurrentRow().getOrigRow());
        
        crs.rvh.add(orig);
        
        return (ResultSet)crs;
        
    }
    
    public void setOriginalRow() throws SQLException {
        checkExecuted();
        if (onInsertRow == true) {
            throw new SQLException(rb.getString("INVALID_INSERT_ROW_OP")); //NOI18N
        }
        
        Row row = (Row)getCurrentRow();
        makeRowOriginal(row);
        
        // this can happen if deleted rows are being shown
        if (row.getDeleted() == true) {
            removeCurrentRow();
            --numRows;
        }
    }
    
    private void makeRowOriginal(Row row) {
        if (row.getInserted() == true) {
            row.clearInserted();
        }
        
        if (row.getUpdated() == true) {
            row.moveCurrentToOrig();
        }
    }
    
    public void setOriginal() throws SQLException {
        checkExecuted();
        for (Iterator i = rvh.iterator(); i.hasNext();) {
            Row row = (Row)i.next();
            makeRowOriginal(row);
            // remove deleted rows from the collection.
            if (row.getDeleted() == true) {
                i.remove();
                --numRows;
            }
        }
        numDeleted = 0;
        
        // notify any listeners that the rowset has changed
        notifyRowSetChanged();
    }
    
    public String getTableName() throws SQLException {
        return tableName;
    }
    
    public void setTableName(String tabName) throws SQLException {
        if (tabName == null)
            throw new SQLException(rb.getString("TABLENAME_CANNOT_BE_NULL")); //NOI18N
        else
            tableName = new String(tabName);
    }
    
    public int[] getKeyColumns() throws SQLException {
        return keyCols;
    }
    
    
    public void setKeyColumns(int [] keys) throws SQLException {
        int numCols = 0;
        if (rowSetMD != null) {
            numCols = rowSetMD.getColumnCount();
            if (keys.length > numCols)
                throw new SQLException(rb.getString("INVALID_KEY_COLS")); //NOI18N
        }
        keyCols = new int[keys.length];
        for (int i = 0; i < keys.length; i++) {
            if (rowSetMD != null && (keys[i] <= 0 ||
                    keys[i] > numCols)) {
                throw new SQLException(MessageFormat.format(rb.getString("INVALID_COLUMN_INDEX"), //NOI18N
                        new Object[] { new Integer(keys[i]) }));
            }
            keyCols[i] = keys[i];
        }
    }
    
    public void updateRef(int columnIndex, java.sql.Ref ref) throws SQLException {
        checkExecuted();
        // sanity check.
        checkIndex(columnIndex);
        // make sure the cursor is on a valid row
        checkCursor();
        
        // SerialClob will help in getting the byte array and storing it.
        // We need to be checking DatabaseMetaData.locatorsUpdatorCopy()
        // or through RowSetMetaData.locatorsUpdatorCopy()
        getCurrentRow().setColumnObject(columnIndex, new SerialRef(ref));
    }
    
    public void updateRef(String columnName, java.sql.Ref ref) throws SQLException {
        checkExecuted();
        updateRef(getColIdxByName(columnName), ref);
    }
    
    public void updateClob(int columnIndex, Clob c) throws SQLException {
        checkExecuted();
        // sanity check.
        checkIndex(columnIndex);
        // make sure the cursor is on a valid row
        checkCursor();
        
        // SerialClob will help in getting the byte array and storing it.
        // We need to be checking DatabaseMetaData.locatorsUpdatorCopy()
        // or through RowSetMetaData.locatorsUpdatorCopy()
        
        if(dbmslocatorsUpdateCopy){
            getCurrentRow().setColumnObject(columnIndex, new SerialClob(c));
        } else{
            throw new SQLException(rb.getString("OP_NOT_SUPPORTED_BY_DB")); //NOI18N
        }
    }
    
    public void updateClob(String columnName, Clob c) throws SQLException {
        checkExecuted();
        updateClob(getColIdxByName(columnName), c);
    }
    
    public void updateBlob(int columnIndex, Blob b) throws SQLException {
        checkExecuted();
        // sanity check.
        checkIndex(columnIndex);
        // make sure the cursor is on a valid row
        checkCursor();
        
        // SerialBlob will help in getting the byte array and storing it.
        // We need to be checking DatabaseMetaData.locatorsUpdatorCopy()
        // or through RowSetMetaData.locatorsUpdatorCopy()
        
        if(dbmslocatorsUpdateCopy){
            getCurrentRow().setColumnObject(columnIndex, new SerialBlob(b));
        } else{
            throw new SQLException(rb.getString("OP_NOT_SUPPORTED_BY_DB")); //NOI18N
        }
    }
    
    public void updateBlob(String columnName, Blob b) throws SQLException {
        checkExecuted();
        updateBlob(getColIdxByName(columnName), b);
    }
    
    public void updateArray(int columnIndex, Array a) throws SQLException {
        checkExecuted();
        // sanity check.
        checkIndex(columnIndex);
        // make sure the cursor is on a valid row
        checkCursor();
        
        // SerialArray will help in getting the byte array and storing it.
        // We need to be checking DatabaseMetaData.locatorsUpdatorCopy()
        // or through RowSetMetaData.locatorsUpdatorCopy()
        getCurrentRow().setColumnObject(columnIndex, new SerialArray(a));
    }
    
    public void updateArray(String columnName, Array a) throws SQLException {
        checkExecuted();
        updateArray(getColIdxByName(columnName), a);
    }
    
    
    public java.net.URL getURL(int columnIndex) throws SQLException {
        checkExecuted();
        
        java.net.URL value;
        
        // sanity check.
        checkIndex(columnIndex);
        // make sure the cursor is on a valid row
        checkCursor();
        
        if (rowSetMD.getColumnType(columnIndex) != java.sql.Types.DATALINK) {
            throw new SQLException(rb.getString("DATATYPE_MISMATCH")); //NOI18N
        }
        
        setLastValueNull(false);
        value = (java.net.URL)(getCurrentRow().getColumnObject(columnIndex));
        
        // check for SQL NULL
        if (value == null) {
            setLastValueNull(true);
            return null;
        }
        
        return value;
    }
    
    public java.net.URL getURL(String columnName) throws SQLException {
        checkExecuted();
        return getURL(getColIdxByName(columnName));
        
    }
    
    public RowSetWarning getRowSetWarnings() {
        try {
            notifyCursorMoved();
        } catch (SQLException e) {} // mask exception
        return rowsetWarning;
    }
    
    
    public void commit() throws SQLException {
        checkExecuted();
        if (conn != null) {
            conn.commit();
        }
    }
    
    public void rollback() throws SQLException {
        checkExecuted();
        if (conn != null) {
            conn.rollback();
        }
    }
    
    public void rollback(Savepoint s) throws SQLException {
        checkExecuted();
        if (conn != null) {
            conn.rollback(s);
        }
    }
    
    public void unsetMatchColumn(int[] columnIdxes) throws SQLException {
        
        int i_val;
        for( int j= 0 ;j < columnIdxes.length; j++) {
            i_val = (Integer.parseInt(iMatchColumns.get(j).toString()));
            if(columnIdxes[j] != i_val) {
                throw new SQLException(rb.getString("MATCH_COLUMNS_NOT_THE_SAME")); //NOI18N
            }
        }
        
        for( int i = 0;i < columnIdxes.length ;i++) {
            iMatchColumns.set(i,new Integer(-1));
        }
    }
    
    public void unsetMatchColumn(String[] columnIdxes) throws SQLException {
        
        for(int j = 0 ;j < columnIdxes.length; j++) {
            if( !columnIdxes[j].equals(strMatchColumns.get(j)) ){
                throw new SQLException(rb.getString("MATCH_COLUMNS_NOT_THE_SAME")); //NOI18N
            }
        }
        
        for(int i = 0 ; i < columnIdxes.length; i++) {
            strMatchColumns.set(i,null);
        }
    }
    
    public String[] getMatchColumnNames() throws SQLException {
        
        String []str_temp = new String[strMatchColumns.size()];
        
        if( strMatchColumns.get(0) == null) {
            throw new SQLException(rb.getString("SET_MATCH_COLUMNS_BEFORE_GETTING")); //NOI18N
        }
        
        strMatchColumns.copyInto(str_temp);
        return str_temp;
    }
    
    public int[] getMatchColumnIndexes() throws SQLException {
        
        Integer []int_temp = new Integer[iMatchColumns.size()];
        int [] i_temp = new int[iMatchColumns.size()];
        int i_val;
        
        i_val = ((Integer)iMatchColumns.get(0)).intValue();
        
        if( i_val == -1 ) {
            throw new SQLException(rb.getString("SET_MATCH_COLUMNS_BEFORE_GETTING")); //NOI18N
        }
        
        
        iMatchColumns.copyInto(int_temp);
        
        for(int i = 0; i < int_temp.length; i++) {
            i_temp[i] = (int_temp[i]).intValue();
        }
        
        return i_temp;
    }
    
    public void setMatchColumn(int[] columnIdxes) throws SQLException {
        
        for(int j = 0 ; j < columnIdxes.length; j++) {
            if( columnIdxes[j] < 0 ) {
                throw new SQLException(rb.getString("MATCH_COL_MUST_BE_GT_0")); //NOI18N
            }
        }
        for(int i = 0 ;i < columnIdxes.length; i++) {
            iMatchColumns.add(i,new Integer(columnIdxes[i]));
        }
    }
    
    public void setMatchColumn(String[] columnNames) throws SQLException {
        
        for(int j = 0; j < columnNames.length; j++) {
            if( columnNames[j] == null || columnNames[j].equals("")) { //NOI18N
                throw new SQLException(rb.getString("MATCH_COL_CANNOT_BE_NULL_OR_EMPTY")); //NOI18N
            }
        }
        for( int i = 0; i < columnNames.length; i++) {
            strMatchColumns.add(i,columnNames[i]);
        }
    }
    
    
    public void setMatchColumn(int columnIdx) throws SQLException {
        // validate, if col is ok to be set
        if(columnIdx < 0) {
            throw new SQLException(rb.getString("COL_ID_HAS_TO_BE_GT_0")); //NOI18N
        } else {
            // set iMatchColumn
            iMatchColumns.set(0, new Integer(columnIdx));
            //strMatchColumn = null;
        }
    }
    
    public void setMatchColumn(String columnName) throws SQLException {
        // validate, if col is ok to be set
        columnName = columnName.trim();
        if( columnName == "" || columnName.equals(null)) { //NOI18N
            throw new SQLException(rb.getString("COL_ID_HAS_TO_BE_NON_NULL")); //NOI18N
        } else {
            // set strMatchColumn
            strMatchColumns.set(0, columnName);
            //iMatchColumn = -1;
        }
    }
    
    public void unsetMatchColumn(int columnIdx) throws SQLException {
        // check if we are unsetting the SAME column
        if(! iMatchColumns.get(0).equals(new Integer(columnIdx) )  ) {
            throw new SQLException(rb.getString("COL_UNSET_NOT_SAME_AS_SET")); //NOI18N
        } else if(strMatchColumns.get(0) != null) {
            throw new SQLException(rb.getString("USE_COL_NAME_AS_ARG_TO_UNSETMATCHCOL")); //NOI18N
        } else {
            // that is, we are unsetting it.
            iMatchColumns.set(0, new Integer(-1));
        }
    }
    
    public void unsetMatchColumn(String columnName) throws SQLException {
        // check if we are unsetting the same column
        columnName = columnName.trim();
        
        if(!((strMatchColumns.get(0)).equals(columnName))) {
            throw new SQLException(rb.getString("COL_UNSET_NOT_SAME_AS_SET")); //NOI18N
        } else if( ((Integer)(iMatchColumns.get(0))).intValue() > 0) {
            throw new SQLException(rb.getString("USE_COL_ID_AS_ARG_TO_UNSETMATCHCOL")); //NOI18N
        } else {
            strMatchColumns.set(0, null);   // that is, we are unsetting it.
        }
    }
    
    public void rowSetPopulated(RowSetEvent event, int numRows) throws SQLException {
        
        if( numRows < 0 || numRows < getFetchSize()) {
            throw new SQLException(rb.getString("NUM_ROWS_CANNOT_BE_LT_0_OR_LT_FETCHSIZE")); //NOI18N
        }
        
        if(size() % numRows == 0) {
            RowSetEvent event_temp = new RowSetEvent(this);
            event = event_temp;
            notifyRowSetChanged();
        }
    }
    
    public void populate(ResultSet data, int start) throws SQLException{
        
        int rowsFetched;
        Row currentRow;
        int numCols;
        int i;
        Map map = getTypeMap();
        Object obj;
        int mRows;
        
        cursorPos = 0;
        if(populatecallcount == 0){
            if(start < 0){
                throw new SQLException(rb.getString("START_POS_CANNOT_BE_NEGATIVE")); //NOI18N
            }
            if(getMaxRows() == 0){
                data.absolute(start);
                while(data.next()){
                    totalRows++;
                }
                totalRows++;
            }
            startPos = start;
        }
        populatecallcount = populatecallcount +1;
        resultSet = data;
        if((endPos - startPos) >= getMaxRows() && (getMaxRows() > 0)){
            endPos = prevEndPos;
            pagenotend = false;
            return;
        }
        
        if((maxRowsreached != getMaxRows() || maxRowsreached != totalRows) && pagenotend) {
            startPrev = start - getPageSize();
        }
        
        if( pageSize == 0){
            prevEndPos = endPos;
            endPos = start + getMaxRows() ;
        } else{
            prevEndPos = endPos;
            endPos = start + getPageSize();
        }
        
        
        if (start == 1){
            resultSet.beforeFirst();
        } else {
            resultSet.absolute(start -1);
        }
        if( pageSize == 0) {
            rvh = new Vector(getMaxRows());
            
        } else{
            rvh = new Vector(getPageSize());
        }
        
        if (data == null) {
            throw new SQLException(rb.getString("INVALID_RESULTSET_SUPPLIED")); //NOI18N
        }
        
        // get the meta data for this ResultSet
        rsmd = data.getMetaData();
        
        // set up the metadata
        rowSetMD = new RowSetMetaDataXImpl();
        initMetaData(rowSetMD, rsmd);
        
        // release the meta-data so that aren't tempted to use it.
        rsmd = null;
        numCols = rowSetMD.getColumnCount();
        mRows = this.getMaxRows();
        rowsFetched = 0;
        currentRow = null;
        
        if(!data.next() && mRows == 0){
            endPos = prevEndPos;
            pagenotend = false;
            return;
        }
        
        data.previous();
        
        while ( data.next()) {
            
            currentRow = new Row(numCols);
            if(pageSize == 0){
                if ( rowsFetched >= mRows && mRows > 0) {
                    rowsetWarning.setNextException(new SQLException(rb.getString("MAX_ROWS_EXCEEDED"))); //NOI18N
                    break;
                }
            } else {
                if ( (rowsFetched >= pageSize) ||( maxRowsreached >= mRows && mRows > 0)) {
                    rowsetWarning.setNextException(new SQLException(rb.getString("MAX_ROWS_EXCEEDED"))); //NOI18N
                    break;
                }
            }
            
            for ( i = 1; i <= numCols; i++) {
                /*
                 * check if the user has set a map. If no map
                 * is set then use plain getObject. This lets
                 * us work with drivers that do not support
                 * getObject with a map in fairly sensible way
                 */
                if (map == null) {
                    obj = data.getObject(i);
                } else {
                    obj = data.getObject(i, map);
                }
                /*
                 * the following block checks for the various
                 * types that we have to serialize in order to
                 * store - right now only structs have been tested
                 */
                if (obj instanceof Struct) {
                    obj = new SerialStruct((Struct)obj, map);
                } else if (obj instanceof SQLData) {
                    obj = new SerialStruct((SQLData)obj, map);
                } else if (obj instanceof Blob) {
                    obj = new SerialBlob((Blob)obj);
                } else if (obj instanceof Clob) {
                    obj = new SerialClob((Clob)obj);
                } else if (obj instanceof java.sql.Array) {
                    obj = new SerialArray((java.sql.Array)obj, map);
                }
                
                ((Row)currentRow).initColumnObject(i, obj);
            }
            rowsFetched++;
            maxRowsreached++;
            rvh.add(currentRow);
        }
        numRows = rowsFetched ;
        executed = true;
        // Also rowsFetched should be equal to rvh.size()
        // notify any listeners that the rowset has changed
        notifyRowSetChanged();
        
    }
    
    public boolean nextPage() throws SQLException {
        // checkExecuted(); //!JK: ??
        
        if (populatecallcount == 0){
            throw new SQLException(rb.getString("INVALID_OP_ROWSET_NOT_POPULATED")); //NOI18N
        }
        if (populatecallcount == 1){
            populatecallcount++;
            return pagenotend;
        } else{
            onFirstPage = false;
            if(callWithCon){
                crsReader.setStartPosition(endPos);
                crsReader.readData((RowSetInternal)this);
                resultSet = null;
            } else {
                populate(resultSet,endPos);
            }
            return pagenotend;
        }
    }
    
    public void setPageSize(int size) throws SQLException {
        if (size < 0) {
            throw new SQLException(rb.getString("PAGESIZE_MUST_BE_GE_0")); //NOI18N
        }
        if (size > getMaxRows() && getMaxRows() != 0) {
            throw new SQLException(rb.getString("PAGESIZE_MUST_BE_LE_MAXROWS")); //NOI18N
        }
        pageSize = size;
    }
    
    public int getPageSize() {
        return pageSize;
    }
    
    
    public boolean previousPage() throws SQLException {
        // checkExecuted(); //!JK: ??
        int pS;
        int mR;
        int rem;
        
        pS = getPageSize();
        mR = maxRowsreached;
        
        if (populatecallcount == 0){
            throw new SQLException(rb.getString("INVALID_OP_ROWSET_NOT_POPULATED")); //NOI18N
        }
        
        if( !callWithCon){
            if(resultSet.getType() == ResultSet.TYPE_FORWARD_ONLY){
                throw new SQLException(rb.getString("RESULTSET_TYPE_FORWARD_ONLY")); //NOI18N
            }
        }
        
        pagenotend = true;
        
        if(startPrev < startPos ){
            onFirstPage = true;
            return false;
        }
        
        if(onFirstPage){
            return false;
        }
        
        rem = mR % pS;
        
        if(rem == 0){
            maxRowsreached -= (2 * pS);
            if(callWithCon){
                crsReader.setStartPosition(startPrev);
                crsReader.readData((RowSetInternal)this);
                resultSet = null;
            } else {
                populate(resultSet,startPrev);
            }
            return true;
        } else {
            maxRowsreached -= (pS + rem);
            if(callWithCon){
                crsReader.setStartPosition(startPrev);
                crsReader.readData((RowSetInternal)this);
                resultSet = null;
            } else {
                populate(resultSet,startPrev);
            }
            return true;
        }
    }
    
    private String catalogName = null;
    /**
     * {@inheritDoc}
     */
    public String getCatalogName() {
        return catalogName;
    }
    
    
    /**
     * {@inheritDoc}
     */
    public void setCatalogName(String catalogName) {
        this.catalogName = catalogName;
    }
    
    
    private String schemaName = null;
    /**
     * {@inheritDoc}
     */
    public String getSchemaName() {
        return schemaName;
    }
    
    
    /**
     * {@inheritDoc}
     */
    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }
    
    
    private String[] columnCatalogNames = null;
    /**
     * {@inheritDoc}
     */
    public String[] getColumnCatalogNames() {
        return columnCatalogNames;
    }
    
    
    /**
     * {@inheritDoc}
     */
    public String getColumnCatalogNames(int index) {
        return columnCatalogNames[index];
    }
    
    
    /**
     * {@inheritDoc}
     */
    public void setColumnCatalogNames(String[] columnCatalogNames) {
        this.columnCatalogNames = columnCatalogNames;
    }
    
    
    /**
     * {@inheritDoc}
     */
    public void setColumnCatalogNames(int index, String columnCatalogName) {
        this.columnCatalogNames[index] = columnCatalogName;
    }
    
    
    private String[] columnSchemaNames = null;
    /**
     * {@inheritDoc}
     */
    public String[] getColumnSchemaNames() {
        return columnSchemaNames;
    }
    
    
    /**
     * {@inheritDoc}
     */
    public String getColumnSchemaNames(int index) {
        return columnSchemaNames[index];
    }
    
    
    /**
     * {@inheritDoc}
     */
    public void setColumnSchemaNames(String[] columnSchemaNames) {
        this.columnSchemaNames = columnSchemaNames;
    }
    
    
    /**
     * {@inheritDoc}
     */
    public void setColumnSchemaNames(int index, String columnSchemaName) {
        this.columnSchemaNames[index] = columnSchemaName;
    }
    
    
    private String[] columnTableNames = null;
    /**
     * {@inheritDoc}
     */
    public String[] getColumnTableNames() {
        return columnTableNames;
    }
    
    
    /**
     * {@inheritDoc}
     */
    public String getColumnTableNames(int index) {
        return columnTableNames[index];
    }
    
    
    /**
     * {@inheritDoc}
     */
    public void setColumnTableNames(String[] columnTableNames) {
        this.columnTableNames = columnTableNames;
    }
    
    
    /**
     * {@inheritDoc}
     */
    public void setColumnTableNames(int index, String columnTableName) {
        this.columnTableNames[index] = columnTableName;
    }
    
    
    private String[] columnNames = null;
    /**
     * {@inheritDoc}
     */
    public String[] getColumnNames() {
        return columnNames;
    }
    
    
    /**
     * {@inheritDoc}
     */
    public String getColumnNames(int index) {
        return columnNames[index];
    }
    
    
    /**
     * {@inheritDoc}
     */
    public void setColumnNames(String[] columnNames) {
        this.columnNames = columnNames;
    }
    
    
    /**
     * {@inheritDoc}
     */
    public void setColumnNames(int index, String columnName) {
        this.columnNames[index] = columnName;
    }
    
    
    private boolean[] insertableColumns = null;
    /**
     * {@inheritDoc}
     */
    public boolean[] getInsertableColumns() {
        return insertableColumns;
    }
    
    
    /**
     * {@inheritDoc}
     */
    public boolean getInsertableColumns(int index) {
        return insertableColumns[index];
    }
    
    
    /**
     * {@inheritDoc}
     */
    public void setInsertableColumns(boolean[] insertableColumns) {
        this.insertableColumns = insertableColumns;
    }
    
    
    /**
     * {@inheritDoc}
     */
    public void setInsertableColumns(int index, boolean insertableColumn) {
        this.insertableColumns[index] = insertableColumn;
    }
    
    
    private boolean[] updatableColumns = null;
    /**
     * {@inheritDoc}
     */
    public boolean[] getUpdatableColumns() {
        return updatableColumns;
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean getUpdatableColumns(int index) {
        return updatableColumns[index];
    }
    
    
    /**
     * {@inheritDoc}
     */
    public void setUpdatableColumns(boolean[] updatableColumns) {
        this.updatableColumns = updatableColumns;
    }
    
    
    /**
     * {@inheritDoc}
     */
    public void setUpdatableColumns(int index, boolean updatableColumn) {
        this.updatableColumns[index] = updatableColumn;
    }
    
    
    private boolean printStatements;
    /**
     * {@inheritDoc}
     */
    public boolean getPrintStatements() {
        return printStatements;
    }
    
    
    /**
     * {@inheritDoc}
     */
    public void setPrintStatements(boolean printStatements) {
        this.printStatements = printStatements;
    }
    
}
