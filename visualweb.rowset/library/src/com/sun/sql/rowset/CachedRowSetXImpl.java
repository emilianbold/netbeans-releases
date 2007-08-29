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
import java.util.*;

import com.sun.rowset.internal.Row;

import com.sun.sql.rowset.internal.CachedRowSetXReader;
import com.sun.sql.rowset.internal.CachedRowSetXWriter;

import com.sun.sql.rowset.providers.RIOptimisticProvider;

/**
 * The reference implementation of the <code>CachedRowSetX</code> interface. See the interface
 * defintion for full behavior and implementation requirements.
 *
 * Note: This implemenation is based on the Sun Microsystems Reference Implemenation of
 * <code>CachedRowSet</code>
 */

// public class CachedRowSetXImpl extends BaseRowSetX implements CachedRowSetX, RowSetInternal, Serializable, Cloneable {
public class CachedRowSetXImpl extends CachedRowSetXImpl5 {
    
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
    
     /*
      * used internally by readers and writer
      */
     static public CachedRowSetXImpl createInternalUseInstance() {
         CachedRowSetXImpl iucrs = new CachedRowSetXImpl();
         iucrs.internalUseInstance = true;
         return iucrs;
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
    
    
     //--------------------------JDBC 2.0-----------------------------------
    
     /*
      * RowSetInternal Interface
      */
    
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
    
    
    //--------------------------------------------------------------------------------
    // Methods added in JDBC 4.0 (JDK6)
    //--------------------------------------------------------------------------------
    
    public int getHoldability() throws SQLException {
       throw new UnsupportedOperationException("Operation not yet supported.");
    }

    public Reader getNCharacterStream(int arg0) throws SQLException {
       throw new UnsupportedOperationException("Operation not yet supported.");
    }

    public Reader getNCharacterStream(String arg0) throws SQLException {
       throw new UnsupportedOperationException("Operation not yet supported.");
    }

    public NClob getNClob(int arg0) throws SQLException {
       throw new UnsupportedOperationException("Operation not yet supported.");
    }

    public NClob getNClob(String arg0) throws SQLException {
       throw new UnsupportedOperationException("Operation not yet supported.");
    }

    public String getNString(int arg0) throws SQLException {
       throw new UnsupportedOperationException("Operation not yet supported.");
    }

    public String getNString(String arg0) throws SQLException {
       throw new UnsupportedOperationException("Operation not yet supported.");
    }

    public RowId getRowId(int arg0) throws SQLException {
       throw new UnsupportedOperationException("Operation not yet supported.");
    }

    public RowId getRowId(String arg0) throws SQLException {
       throw new UnsupportedOperationException("Operation not yet supported.");
    }

    public SQLXML getSQLXML(int arg0) throws SQLException {
       throw new UnsupportedOperationException("Operation not yet supported.");
    }

    public SQLXML getSQLXML(String arg0) throws SQLException {
       throw new UnsupportedOperationException("Operation not yet supported.");
    }

    public boolean isClosed() throws SQLException {
       throw new UnsupportedOperationException("Operation not yet supported.");
    }

    public void updateAsciiStream(int arg0, InputStream arg1, long arg2) throws SQLException {
       throw new UnsupportedOperationException("Operation not yet supported.");
    }

    public void updateAsciiStream(String arg0, InputStream arg1, long arg2) throws SQLException {
       throw new UnsupportedOperationException("Operation not yet supported.");
    }

    public void updateAsciiStream(int arg0, InputStream arg1) throws SQLException {
       throw new UnsupportedOperationException("Operation not yet supported.");
    }

    public void updateAsciiStream(String arg0, InputStream arg1) throws SQLException {
       throw new UnsupportedOperationException("Operation not yet supported.");
    }

    public void updateBinaryStream(int arg0, InputStream arg1, long arg2) throws SQLException {
       throw new UnsupportedOperationException("Operation not yet supported.");
    }

    public void updateBinaryStream(String arg0, InputStream arg1, long arg2) throws SQLException {
       throw new UnsupportedOperationException("Operation not yet supported.");
    }

    public void updateBinaryStream(int arg0, InputStream arg1) throws SQLException {
       throw new UnsupportedOperationException("Operation not yet supported.");
    }

    public void updateBinaryStream(String arg0, InputStream arg1) throws SQLException {
       throw new UnsupportedOperationException("Operation not yet supported.");
    }

    public void updateBlob(int arg0, InputStream arg1, long arg2) throws SQLException {
       throw new UnsupportedOperationException("Operation not yet supported.");
    }

    public void updateBlob(String arg0, InputStream arg1, long arg2) throws SQLException {
       throw new UnsupportedOperationException("Operation not yet supported.");
    }

    public void updateBlob(int arg0, InputStream arg1) throws SQLException {
       throw new UnsupportedOperationException("Operation not yet supported.");
    }

    public void updateBlob(String arg0, InputStream arg1) throws SQLException {
       throw new UnsupportedOperationException("Operation not yet supported.");
    }

    public void updateCharacterStream(int arg0, Reader arg1, long arg2) throws SQLException {
       throw new UnsupportedOperationException("Operation not yet supported.");
    }

    public void updateCharacterStream(String arg0, Reader arg1, long arg2) throws SQLException {
       throw new UnsupportedOperationException("Operation not yet supported.");
    }

    public void updateCharacterStream(int arg0, Reader arg1) throws SQLException {
       throw new UnsupportedOperationException("Operation not yet supported.");
    }

    public void updateCharacterStream(String arg0, Reader arg1) throws SQLException {
       throw new UnsupportedOperationException("Operation not yet supported.");
    }

    public void updateClob(int arg0, Reader arg1, long arg2) throws SQLException {
       throw new UnsupportedOperationException("Operation not yet supported.");
    }

    public void updateClob(String arg0, Reader arg1, long arg2) throws SQLException {
       throw new UnsupportedOperationException("Operation not yet supported.");
    }

    public void updateClob(int arg0, Reader arg1) throws SQLException {
       throw new UnsupportedOperationException("Operation not yet supported.");
    }

    public void updateClob(String arg0, Reader arg1) throws SQLException {
       throw new UnsupportedOperationException("Operation not yet supported.");
    }

    public void updateNCharacterStream(int arg0, Reader arg1, long arg2) throws SQLException {
       throw new UnsupportedOperationException("Operation not yet supported.");
    }

    public void updateNCharacterStream(String arg0, Reader arg1, long arg2) throws SQLException {
       throw new UnsupportedOperationException("Operation not yet supported.");
    }

    public void updateNCharacterStream(int arg0, Reader arg1) throws SQLException {
       throw new UnsupportedOperationException("Operation not yet supported.");
    }

    public void updateNCharacterStream(String arg0, Reader arg1) throws SQLException {
       throw new UnsupportedOperationException("Operation not yet supported.");
    }

    public void updateNClob(int arg0, NClob arg1) throws SQLException {
       throw new UnsupportedOperationException("Operation not yet supported.");
    }

    public void updateNClob(String arg0, NClob arg1) throws SQLException {
       throw new UnsupportedOperationException("Operation not yet supported.");
    }

    public void updateNClob(int arg0, Reader arg1, long arg2) throws SQLException {
       throw new UnsupportedOperationException("Operation not yet supported.");
    }

    public void updateNClob(String arg0, Reader arg1, long arg2) throws SQLException {
       throw new UnsupportedOperationException("Operation not yet supported.");
    }

    public void updateNClob(int arg0, Reader arg1) throws SQLException {
       throw new UnsupportedOperationException("Operation not yet supported.");
    }

    public void updateNClob(String arg0, Reader arg1) throws SQLException {
       throw new UnsupportedOperationException("Operation not yet supported.");
    }

    public void updateNString(int arg0, String arg1) throws SQLException {
       throw new UnsupportedOperationException("Operation not yet supported.");
    }

    public void updateNString(String arg0, String arg1) throws SQLException {
       throw new UnsupportedOperationException("Operation not yet supported.");
    }

    public void updateRowId(int arg0, RowId arg1) throws SQLException {
       throw new UnsupportedOperationException("Operation not yet supported.");
    }

    public void updateRowId(String arg0, RowId arg1) throws SQLException {
       throw new UnsupportedOperationException("Operation not yet supported.");
    }

    public void updateSQLXML(int arg0, SQLXML arg1) throws SQLException {
       throw new UnsupportedOperationException("Operation not yet supported.");
    }

    public void updateSQLXML(String arg0, SQLXML arg1) throws SQLException {
       throw new UnsupportedOperationException("Operation not yet supported.");
    }

    public boolean isWrapperFor(Class arg0) throws SQLException {
       return false;
    }

    public Object unwrap(Class arg0) throws SQLException {
	return null;
    }

} //end class
