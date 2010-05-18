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

package com.sun.sql.rowset.internal;

import java.sql.*;
import javax.sql.*;
import java.util.*;
import java.io.*;

import javax.sql.rowset.*;
import javax.sql.rowset.spi.*;

import com.sun.sql.rowset.CachedRowSetX;
import com.sun.sql.rowset.CachedRowSetXImpl;
import com.sun.sql.rowset.RowSetMetaDataXImpl;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;


/**
 * An implementation of RowSetWriter to work with CachedRowSetX rowsets.
 *
 * Based on the Sun reference implementation of CachedRowSetWriter.
 *
 * @see javax.sql.rowset.spi.SyncProvider
 * @see javax.sql.rowset.spi.SyncFactory
 * @see javax.sql.rowset.spi.SyncFactoryException
 */
public class CachedRowSetXWriter implements TransactionalWriter, Serializable {
    
    private static ResourceBundle rb =
            ResourceBundle.getBundle("com.sun.sql.rowset.internal.Bundle", Locale.getDefault()); // NOI18N
    
    /**
     * The <code>Connection</code> object that this writer will use to make a
     * connection to the data source to which it will write data.
     *
     * @serial
     */
    private Connection con;
    
    /**
     * The SQL <code>SELECT</code> command that this writer will call
     * internally. The method <code>initSQLStatements</code> builds this
     * command by supplying the words "SELECT" and "FROM," and using
     * metadata to get the table name and column names .
     *
     * @serial
     */
    private String selectCmd;
    
    /**
     * The SQL <code>UPDATE</code> command that this writer will call
     * internally to write data to the rowset's underlying data source.
     * The method <code>initSQLStatements</code> builds this <code>String</code>
     * object.
     *
     * @serial
     */
    private String updateCmd;
    
    /**
     * The SQL <code>WHERE</code> clause the writer will use for update
     * statements in the <code>PreparedStatement</code> object
     * it sends to the underlying data source.
     *
     * @serial
     */
    private String updateWhere;
    
    /**
     * The SQL <code>DELETE</code> command that this writer will call
     * internally to delete a row in the rowset's underlying data source.
     *
     * @serial
     */
    private String deleteCmd;
    
    /**
     * The SQL <code>WHERE</code> clause the writer will use for delete
     * statements in the <code>PreparedStatement</code> object
     * it sends to the underlying data source.
     *
     * @serial
     */
    private String deleteWhere;
    
    /**
     * The SQL <code>INSERT INTO</code> command that this writer will internally use
     * to insert data into the rowset's underlying data source.  The method
     * <code>initSQLStatements</code> builds this command with a question
     * mark parameter placeholder for each column in the rowset.
     *
     * @serial
     */
    private String insertCmd;
    
    /**
     * The columns to include in an insert.  Only the columns from one
     * table can be inserted.  This list is computed when the insertCmd
     * is built.
     *
     * @serial
     */
    private List insertableColumnsInThisTable;
    
    /**
     * The columns to include in an update.  Only the columns from one
     * table can be updated.  This list is computed when the updateCmd
     * is built.
     *
     * *NOTE*: The values are 1 based Integers!
     *
     * @serial
     */
    private List updatableColumnsInThisTable;
    
    /**
     * The columns to include in a where clause.  This list is computed when the
     * sql commands are bullt.
     *
     * *NOTE*: The values are 1 based Integers!
     *
     * *ALSO NOTE*: We could have separate lists for selectColumns and whereColumns
     *              At present, it is not known that this is necessary and, for now,
     *              we won't add this in the interests of not adding more complexity
     *              and not adding something that might not work.  If we did add this,
     *              we would compute the selectColumnsInThisTable list and then pare
     *              that list down (by calling isSearchable on the column #), to get
     *              the whereColumnsInThisTable list.  If isSearchable is not reliable,
     *              we might need to also add a searchableColumns boolean[] property on
     *              the rowset.
     *
     * @serial
     */
    private List whereColumnsInThisTable;
    
    /**
     * Real column names.
     *
     * @serial
     */
    private String[] realColumnNames;
    
    /**
     * An array containing the column numbers of the columns that are
     * needed to uniquely identify a row in the <code>CachedRowSet</code> object
     * for which this <code>CachedRowSetXWriter</code> object is the writer.
     *
     * NOTE: keyColumns in CachedRowSetXImpl are ignored and they are computed
     *       from whereColumnsInThisTable
     *
     * @serial
     */
    private int[] keyCols;
    
    /**
     * An array of the parameters that should be used to set the parameter
     * placeholders in a <code>PreparedStatement</code> object that this
     * writer will execute.
     *
     * @serial
     */
    private Object[] params;
    
    /**
     * The <code>CachedRowSetXReader</code> object that has been
     * set as the reader for the <code>CachedRowSet</code> object
     * for which this <code>CachedRowSetXWriter</code> object is the writer.
     *
     * @serial
     */
    private CachedRowSetXReader reader;
    
    /**
     * The <code>ResultSetMetaData</code> object that contains information
     * about the columns in the <code>CachedRowSet</code> object
     * for which this <code>CachedRowSetXWriter</code> object is the writer.
     *
     * @serial
     */
    private ResultSetMetaData callerMd;
    
    /**
     * The number of columns in the <code>CachedRowSet</code> object
     * for which this <code>CachedRowSetXWriter</code> object is the writer.
     *
     * @serial
     */
    private int callerColumnCount;
    
    /**
     * This <code>CachedRowSet<code> will hold the conflicting values
     *  retrieved from the db and hold it.
     */
    private CachedRowSetXImpl crsResolve;
    
    /**
     * This <code>ArrayList<code> will hold the values of SyncResolver.*
     */
    private ArrayList status;
    
    /**
     * This <code>ArrayList<code> will hold the SQLExceptions of SyncResolver.*
     */
    private ArrayList exceptions;
    
    /**
     * This will check whether the same field value has changed both
     * in database and CachedRowSet.
     */
    private int iChangedValsInDbAndCRS;
    
    /**
     * This will hold the number of cols for which the values have
     * changed only in database.
     */
    private int iChangedValsinDbOnly ;
    
    /**
     * Propagates changes in the given <code>RowSet</code> object
     * back to its underlying data source and returns <code>true</code>
     * if successful. The writer will check to see if
     * the data in the pre-modified rowset (the original values) differ
     * from the data in the underlying data source.  If data in the data
     * source has been modified by someone else, there is a conflict,
     * and in that case, the writer will not write to the data source.
     * In other words, the writer uses an optimistic concurrency algorithm:
     * It checks for conflicts before making changes rather than restricting
     * access for concurrent users.
     * <P>
     * This method is called by the rowset internally when
     * the application invokes the method <code>acceptChanges</code>.
     * The <code>writeData</code> method in turn calls private methods that
     * it defines internally.
     * The following is a general summary of what the method
     * <code>writeData</code> does, much of which is accomplished
     * through calls to its own internal methods.
     * <OL>
     * <LI>Creates a <code>CachedRowSet</code> object from the given
     *     <code>RowSet</code> object
     * <LI>Makes a connection with the data source
     *   <UL>
     *      <LI>Disables autocommit mode if it is not already disabled
     *      <LI>Sets the transaction isolation level to that of the rowset
     *   </UL>
     * <LI>Checks to see if the reader has read new data since the writer
     *     was last called and, if so, calls the method
     *    <code>initSQLStatements</code> to initialize new SQL statements
     *   <UL>
     *       <LI>Builds new <code>SELECT</code>, <code>UPDATE</code>,
     *           <code>INSERT</code>, and <code>DELETE</code> statements
     *       <LI>Uses the <code>CachedRowSet</code> object's metadata to
     *           determine the table name, column names, and the columns
     *           that make up the primary key
     *   </UL>
     * <LI>When there is no conflict, propagates changes made to the
     *     <code>CachedRowSet</code> object back to its underlying data source
     *   <UL>
     *      <LI>Iterates through each row of the <code>CachedRowSet</code> object
     *          to determine whether it has been updated, inserted, or deleted
     *      <LI>If the corresponding row in the data source has not been changed
     *          since the rowset last read its
     *          values, the writer will use the appropriate command to update,
     *          insert, or delete the row
     *      <LI>If any data in the data source does not match the original values
     *          for the <code>CachedRowSet</code> object, the writer will roll
     *          back any changes it has made to the row in the data source.
     *   </UL>
     * </OL>
     *
     * @return <code>true</code> if changes to the rowset were successfully
     *         written to the rowset's underlying data source;
     *         <code>false</code> otherwise
     */
    public boolean writeData(RowSetInternal caller) throws SQLException {
        boolean conflict = false;
        boolean showDel = false;
        PreparedStatement pstmtIns = null;
        iChangedValsInDbAndCRS = 0;
        iChangedValsinDbOnly = 0;
        
        // We assume caller is a CachedRowSet
        CachedRowSetXImpl crs = (CachedRowSetXImpl)caller;
        // crsResolve = new CachedRowSetXImpl();
        this.crsResolve = CachedRowSetXImpl.createInternalUseInstance();
        
        // The reader is registered with the writer at design time.
        // This is not required, in general.  The reader has logic
        // to get a JDBC connection, so call it.
        
        con = reader.connect(caller);
        
        if (con == null) {
            throw new SQLException(rb.getString("UNABLE_TO_GET_CONNECTION")); //NOI18N
        }
        if (con.getAutoCommit() == true) {
            con.setAutoCommit(false);
        }
        
        con.setTransactionIsolation(crs.getTransactionIsolation());
        
        initSQLStatements(crs);
        int iColCount;
        
        RowSetMetaDataXImpl rsmdWrite = (RowSetMetaDataXImpl)crs.getMetaData();
        RowSetMetaDataXImpl rsmdResolv = new RowSetMetaDataXImpl();
        
        iColCount = rsmdWrite.getColumnCount();
        int sz= crs.size()+1;
        status = new ArrayList(sz);
        exceptions = new ArrayList(sz);
        
        status.add(0,null);
        exceptions.add(0,null);
        rsmdResolv.setColumnCount(iColCount);
        
        boolean[] insertableColumns = crs.getInsertableColumns();
        boolean[] updatableColumns = crs.getUpdatableColumns();
        for(int i =1; i <= iColCount; i++) {
            rsmdResolv.setColumnType(i, rsmdWrite.getColumnType(i));
            rsmdResolv.setColumnName(i, rsmdWrite.getColumnName(i));
            rsmdResolv.setColumnLabel(i, rsmdWrite.getColumnLabel(i)); 
            rsmdResolv.setNullable(i, ResultSetMetaData.columnNullableUnknown);
            rsmdResolv.setColumnClassName(i, rsmdWrite.getColumnClassName(i));
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
            if (insertableColumns != null && insertableColumns.length >= i) {
                rsmdResolv.setWritable(i, insertableColumns[i-1]);
                rsmdResolv.setDefinitelyWritable(i, insertableColumns[i-1]);
                rsmdResolv.setReadOnly(i, !insertableColumns[i-1]);
            } else if (updatableColumns != null && updatableColumns.length >= i) {
                rsmdResolv.setWritable(i, updatableColumns[i-1]);
                rsmdResolv.setDefinitelyWritable(i, updatableColumns[i-1]);
                rsmdResolv.setReadOnly(i, !updatableColumns[i-1]);
            } else {
                rsmdResolv.setWritable(i, true);
                rsmdResolv.setDefinitelyWritable(i, false); // We don't know for sure
                rsmdResolv.setReadOnly(i, false);
            }
        }
        this.crsResolve.setMetaData(rsmdResolv);
        
        // moved outside the insert inner loop
        if (crs.getPrintStatements()) {
            System.out.println(insertCmd);
        }
        pstmtIns = con.prepareStatement(insertCmd);
        
        if (callerColumnCount < 1) {
            // No data, so return success.
            // moved outside the insert inner loop
            if (crs.getPrintStatements()) {
                System.out.println("No columns found (callerColumnCount==0).");
            }
            if (reader.getCloseConnection() == true) {
                try {
                    if (!con.getAutoCommit()) {
                        con.rollback();
                    }
                } catch (Exception dummy) {
                        /*
                         * not an error condition, we're closing anyway, but
                         * we'd like to clean up any locks if we can since
                         * it is not clear the connection pool will clean
                         * these connections in a timely manner
                         */
                }
                con.close();
                con = null;
            }
            return true;
        }
        // We need to see rows marked for deletion.
        showDel = crs.getShowDeleted();
        crs.setShowDeleted(true);
        
        // Look at all the rows.
        crs.beforeFirst();
        
        int rows =1;
        while (crs.next()) {
            // System.out.println("row is 1:"+rows);
            if (crs.rowDeleted()) {
                // The row has been deleted.
                if (crs.rowInserted()) {
                    // Special case: row was inserted and then deleted
                    status.add(rows, new Integer(SyncResolver.NO_ROW_CONFLICT));
                    exceptions.add(rows, null);
                } else {
                    try {
                        deleteOriginalRow(crs, this.crsResolve);
                        status.add(rows, new Integer(SyncResolver.NO_ROW_CONFLICT));
                        exceptions.add(rows, null);
                    } catch (SQLException sqle) {
                        status.add(rows, new Integer(SyncResolver.DELETE_ROW_CONFLICT));
                        exceptions.add(rows, sqle);
                    }
                }
            } else if (crs.rowInserted()) {
                // The row has been inserted.
                try {
                    insertNewRow(crs, pstmtIns, this.crsResolve);
                    // insert happened without any occurrence of conflicts
                    // so update status accordingly
                    status.add(rows, new Integer(SyncResolver.NO_ROW_CONFLICT));
                    exceptions.add(rows, null);
                } catch (SQLException sqle) {
                    status.add(rows, new Integer(SyncResolver.INSERT_ROW_CONFLICT));
                    exceptions.add(rows, sqle);
                }
            } else  if (crs.rowUpdated()) {
                // The row has been updated.
                try {
                    updateOriginalRow(crs);
                    status.add(rows, new Integer(SyncResolver.NO_ROW_CONFLICT));
                    exceptions.add(rows, null);
                } catch (SQLException sqle) {
                    status.add(rows, new Integer(SyncResolver.UPDATE_ROW_CONFLICT));
                    exceptions.add(rows, sqle);
                }
            } else {
                /** The row is neither of inserted, updated or deleted.
                 *  So set nulls in the this.crsResolve for this row,
                 *  as nothing is to be done for such rows.
                 *  Also note that if such a row has been changed in database
                 *  and we have not changed(inserted, updated or deleted)
                 *  that is fine.
                 **/
                int icolCount = crs.getMetaData().getColumnCount();
                status.add(rows, new Integer(SyncResolver.NO_ROW_CONFLICT));
                exceptions.add(rows, null);
                
                this.crsResolve.moveToInsertRow();
                for(int cols=0;cols<iColCount;cols++) {
                    this.crsResolve.updateNull(cols+1);
                } //end for
                
                this.crsResolve.insertRow();
                this.crsResolve.moveToCurrentRow();
                
            } //end if
            rows++;
        } //end while
        
        // close the insert statement
        try {
            pstmtIns.close();
        } catch (SQLException e) {
            System.err.println(MessageFormat.format(rb.getString("NON_FATAL_ERROR"), //NOI18N
                    new Object[] { e }));
                    //System.err.println("Non-Fatal error stackTrace follows:");
                    //e.printStackTrace();
        }
        // reset
        crs.setShowDeleted(showDel);
        
        boolean boolConf = false;
        int noOfConflicts = 0;
        for (int j=1;j<status.size();j++){
            // ignore status for index = 0 which is set to null
            if(! ((status.get(j)).equals(new Integer(SyncResolver.NO_ROW_CONFLICT)))) {
                // there is at least one conflict which needs to be resolved
                boolConf = true;
                noOfConflicts++;
                //break;
            }
        }
        
        crs.beforeFirst();
        this.crsResolve.beforeFirst();
        
        if(boolConf) {
            
            SyncProviderException spe = new SyncProviderException(MessageFormat.format(rb.getString("NO_OF_CONFLICTS"), //NOI18N
                    new Object[] { new Integer(noOfConflicts) }));
                    spe.setSyncResolver(new SyncResolverXImpl());
                    //SyncResolver syncRes = spe.getSyncResolver();
                    
                    SyncResolverXImpl syncResImpl = (SyncResolverXImpl) spe.getSyncResolver();
                    
                    syncResImpl.setCachedRowSet(crs);
                    syncResImpl.setCachedRowSetResolver(this.crsResolve);
                    
                    syncResImpl.setStatus(status);
                    syncResImpl.setExceptions(exceptions);
                    syncResImpl.setCachedRowSetXWriter(this);
                    
                    throw spe;
        } else {
            return true;
        }
    } //end writeData
    
    /**
     * Updates the given <code>CachedRowSet</code> object's underlying data
     * source so that updates to the rowset are reflected in the original
     * data source, and returns <code>false</code> if the update was successful.
     * A return value of <code>true</code> indicates that there is a conflict,
     * meaning that a value updated in the rowset has already been changed by
     * someone else in the underlying data source.  A conflict can also exist
     * if, for example, more than one row in the data source would be affected
     * by the update or if no rows would be affected.  In any case, if there is
     * a conflict, this method does not update the underlying data source.
     * <P>
     * This method is called internally by the method <code>writeData</code>
     * if a row in the <code>CachedRowSet</code> object for which this
     * <code>CachedRowSetXWriter</code> object is the writer has been updated.
     *
     * @return <code>false</code> if the update to the underlying data source is
     *         successful; <code>true</code> otherwise
     * @throws SQLException if a database access error occurs
     */
    private void updateOriginalRow(CachedRowSet crs)
    throws SQLException {
        PreparedStatement pstmt;
        int i = 0;
        int idx = 0;
        
        // Select the row from the database.
        ResultSet origVals = crs.getOriginalRow();
        origVals.next();
        
        try {
            updateWhere = buildWhereClause(updateWhere, origVals);
            
            if (((CachedRowSetX)crs).getPrintStatements()) {
                System.out.println(selectCmd + updateWhere);
            }
            pstmt = con.prepareStatement(selectCmd + updateWhere,
                    ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
            
            StringBuffer msg = null ;
            for (i = 0; i < keyCols.length; i++) {
                if (((CachedRowSetX)crs).getPrintStatements()) {
                    if ( msg == null ) msg = new StringBuffer(100) ;
                    msg.append("  Paramm[" + (idx+1) +"]=(" );
                    if ( params[i] != null ) {
                        msg.append(params[i].getClass().getName() + "," + params[i].toString() +")" );
                    } else {
                        msg.append("null)" ) ;
                    }
                }
                if (params[i] != null) {
                    pstmt.setObject(++idx, params[i]);
                } else {
                    continue;
                }
            }
            if ( msg != null ) System.out.println(msg);
            
            // drivers may not support the following, so ignore exceptions.
            try {
                pstmt.setMaxRows( 2 ); // we only care if there's not 1!
            } catch (Exception ex) { ; }
            try {
                pstmt.setMaxFieldSize(crs.getMaxFieldSize());
            } catch (Exception ex) { ; }
            try {
                pstmt.setEscapeProcessing(crs.getEscapeProcessing());
            } catch (Exception ex) { ; }
            try {
                pstmt.setQueryTimeout(crs.getQueryTimeout());
            } catch (Exception ex) { ; }
            
            ResultSet rs = null;
            if (((CachedRowSetX)crs).getPrintStatements()) {
                System.out.println("Writer:  executing pre-update SELECT");
            }
            rs = pstmt.executeQuery();
            if (rs.next() == true) {
                
                if (rs.next()) {
                    /**  More than one row conflict.
                     *  If rs has only one row we are able to
                     *  uniquely identify the row where update
                     *  have to happen else if more than one
                     *  row implies we cannot uniquely identify the row
                     *  where we have to do updates.
                     *  crs.setKeyColumns needs to be set to
                     *  come out of this situation.
                     */
                    if (((CachedRowSetX)crs).getPrintStatements()) {
                        System.out.println("   Writer: pre-update SELECT returned >1 row:  BAD");
                    }
                    
                    return;
                }
                
                // don't close the rs
                // we require the record in rs to be used.
                // rs.close();
                // pstmt.close();
                rs.first();
                
                // how many fields need to be updated
                int colsNotChanged = 0;
                Vector cols = new Vector();
                String updateExec = new String(updateCmd);
                Object orig;
                Object curr;
                Object rsval;
                boolean boolNull = true;
                Object objVal = null;
                
                // There's only one row and the cursor
                // needs to be on that row.
                
                boolean first = true;
                
                this.crsResolve.moveToInsertRow();
                
                for (i = 1; i <= callerColumnCount; i++) {
                    if (!updatableColumnsInThisTable.contains(new Integer(i))) {
                        colsNotChanged++;
                        continue;
                    }
                    orig = origVals.getObject(i);
                    curr = crs.getObject(i);
                    // The object is NOT at position i, it is at the whatever place it was added
                    // in whereColumnsInThisTable!!!
                    rsval = rs.getObject(whereColumnsInThisTable.indexOf(new Integer(i))+1);
                    // reset boolNull if it had been set
                    boolNull = true;
                    
                    if ((rsval == null && orig != null) || (rsval != null && !rsval.equals(orig))) {
                        // value in db has changed
                        // don't proceed with synchronization
                        // get the value in db and pass it to the resolver.
                        
                        iChangedValsinDbOnly++;
                        // Set the boolNull to false,
                        // in order to set the actual value;
                        boolNull = false;
                        objVal = rsval;
                    } else if ((orig == null || curr == null) ) {
                        if (first == false) {
                            updateExec += ", "; //NOI18N
                        }
                        updateExec += crs.getMetaData().getColumnName(i);
                        cols.add(new Integer(i));
                        updateExec += " = ? "; //NOI18N
                        first = false;
                        
                    } else if ((orig == null && curr == null) ||
                            (orig != null && orig.equals(curr))) {
                        colsNotChanged++;
                        //nothing to update in this case since values are equal
                        
                    } else if ((orig == null && curr !=  null) || orig.equals(curr) == false ) {
                        // When values from db and values in CachedRowSet are not equal,
                        // if db value is same as before updation for each col in
                        // the row before fetching into CachedRowSet,
                        // only then we go ahead with updation, else we
                        // throw SyncProviderException.
                        
                        // if value has changed in db after fetching from db
                        // for some cols of the row and at the same time, some other cols
                        // have changed in CachedRowSet, no synchronization happens
                        
                        // Synchronization happens only when data when fetching is
                        // same or at most has changed in cachedrowset
                        
                        // check orig value with what is there in crs for a column
                        // before updation in crs.
                        
                        if(crs.columnUpdated(i)) {
                            if(rsval.equals(orig)) {
                                // At this point we are sure that
                                // the value updated in crs was from
                                // what is in db now and has not changed
                                if (first == false) {
                                    updateExec += ", "; //NOI18N
                                }
                                updateExec += crs.getMetaData().getColumnName(i);
                                cols.add(new Integer(i));
                                updateExec += " = ? "; //NOI18N
                                first = false;
                            } else {
                                // Here the value has changed in the db after
                                // data was fetched
                                // Plus store this row from CachedRowSet and keep it
                                // in a new CachedRowSet
                                boolNull= false;
                                objVal = rsval;
                                iChangedValsInDbAndCRS++;
                            }
                        }
                    }
                    
                    if(!boolNull) {
                        this.crsResolve.updateObject(i,objVal);
                    } else {
                        this.crsResolve.updateNull(i);
                    }
                } //end for
                
                
                /**
                 * if nothing has changed return now - this can happen
                 * if column is updated to the same value.
                 * if colsNotChanged == callerColumnCount implies we are updating
                 * the database with ALL COLUMNS HAVING SAME VALUES,
                 * so skip going to database, else do as usual.
                 **/
                if ( (first == false && cols.size() == 0)  ||
                        colsNotChanged == callerColumnCount ) {
                        if (((CachedRowSetX)crs).getPrintStatements()) {
                            System.out.println("Writer: no columns changed, nothing to update"); //NOI18N
                        }
                    return;
                }
                
                if(iChangedValsInDbAndCRS != 0 || iChangedValsinDbOnly != 0) {
                    throw new SQLException(rb.getString("VALUES_CHANGED_IN_DB")); //NOI18N
                }
                
                updateExec += updateWhere;
                if (((CachedRowSetX)crs).getPrintStatements()) {
                    System.out.println("Writer:  "+updateExec);
                }
                pstmt = con.prepareStatement(updateExec);
                
                // Comments needed here
                msg = null ; // for logging (optional)
                for (i = 0; i < cols.size(); i++) {
                    if (updatableColumnsInThisTable.contains((Integer)cols.get(i))) {
                        
                        
                        Object obj = crs.getObject(((Integer)cols.get(i)).intValue());
                        if (((CachedRowSetX)crs).getPrintStatements()) {
                            if ( msg == null ) msg = new StringBuffer(100) ;
                            msg.append(" UpdateCol[" + (i+1) +"]=("  );
                            if ( obj != null ) {
                                msg.append(obj.getClass().getName() + "," + obj.toString() +")" );
                            } else {
                                msg.append("null:" + crs.getMetaData().getColumnType( ((Integer)cols.get(i)).intValue() )  +")" ) ;
                            }
                        }
                        if (obj != null)
                            pstmt.setObject(i + 1, obj);
                        else {
                            pstmt.setNull(i + 1,crs.getMetaData().getColumnType( ((Integer)cols.get(i)).intValue() ) );
                        }
                    }
                }
                if (msg !=null ) System.out.println(msg); // only if ( getPrintStatements() )
                idx = i;
                
                for (i = 0; i < keyCols.length; i++) {
                    if (params[i] != null) {
                        pstmt.setObject(++idx, params[i]);
                    } else {
                        continue;
                    }
                }
                if (((CachedRowSetX)crs).getPrintStatements()) {
                    System.out.println("Writer:  executing update() ");
                }
                i = pstmt.executeUpdate();
                
                this.crsResolve.insertRow();
                this.crsResolve.moveToCurrentRow();
                
                /**
                 * i should be equal to 1(row count), because we update
                 * one row(returned as row count) at a time, if all goes well.
                 * if 1 != 1, this implies we have not been able to
                 * do updations properly i.e there is a conflict in database
                 * versus what is in CachedRowSet for this particular row.
                 **/
                
                return;
                
            } else {
                /**
                 * Cursor will be here, if the ResultSet may not return even a single row
                 * i.e. we can't find the row where to update because it has been updated
                 * or deleted from the db.
                 * Present the whole row as null to user, to force null to be sync'ed
                 * and hence nothing to be synced.
                 **/
                if (((CachedRowSetX)crs).getPrintStatements()) {
                    System.out.println("   Writer: pre-update SELECT returned 0 rows");
                }
                throw new SQLException(rb.getString("ATTEMPT_TO_UPDATE_ROW_UP_OR_DEL")); //NOI18N
            }
        } catch (SQLException ex) {
            //System.out.println("updateOriginalRow: caught exception: " + ex);
            // if executeUpdate fails it will come here,
            // update crsResolve with null rows
            this.crsResolve.moveToInsertRow();
            
            for(i = 1; i <= callerColumnCount; i++) {
                this.crsResolve.updateNull(i);
            }
            
            this.crsResolve.insertRow();
            this.crsResolve.moveToCurrentRow();
            
            throw ex;
        }
    }
    
    /**
     * Inserts a row that has been inserted into the given
     * <code>CachedRowSet</code> object into the data source from which
     * the rowset is derived, returning <code>false</code> if the insertion
     * was successful.
     *
     * @param crs the <code>CachedRowSet</code> object that has had a row inserted
     *            and to whose underlying data source the row will be inserted
     * @param pstmt the <code>PreparedStatement</code> object that will be used
     *              to execute the insertion
     * @return <code>false</code> to indicate that the insertion was successful;
     *         <code>true</code> otherwise
     * @throws SQLException if a database access error occurs
     */
    private void insertNewRow(CachedRowSet crs,
            PreparedStatement pstmt, CachedRowSetXImpl crsRes) throws SQLException {
        int i = 0;
        int icolCount = 0 ;
        
        try {
            icolCount = crs.getMetaData().getColumnCount();
            
            int pstmtColIdx = 1;
            StringBuffer msg = null ;
            for (i = 1; i <= icolCount; i++) {
                if (insertableColumnsInThisTable.contains(new Integer(i))) {
                    Object obj = crs.getObject(i);
                    if (((CachedRowSetX)crs).getPrintStatements()) {
                        if ( msg == null ) msg = new StringBuffer(", params: ") ;
                        msg.append("  Col[" + (i) +"]=(" ) ;
                        if ( obj != null ) {
                            msg.append(obj.getClass().getName() + "," + obj.toString() +")" );
                        } else {
                            msg.append("null:" + crs.getMetaData().getColumnType( i )  +")"  );
                        }
                    }
                    if (obj != null) {
                        pstmt.setObject(pstmtColIdx++, obj);
                    } else {
                        pstmt.setNull(pstmtColIdx++, crs.getMetaData().getColumnType(i));
                    }
                }
            }
            
            if (((CachedRowSetX)crs).getPrintStatements()) {
                System.out.println("Writer:  executing insert " + (msg==null?".":msg.toString()) );
            }
            i = pstmt.executeUpdate();
            
            this.crsResolve.moveToInsertRow();
            this.crsResolve.insertRow();
            this.crsResolve.moveToCurrentRow();
            
        } catch (SQLException ex) {
            //System.out.println("InsertNewRow: caught exception: " + ex);
            /**
             * Cursor will come here if executeUpdate fails.
             * There can be many reasons why the insertion failed,
             * one can be violation of primary key.
             * Hence we cannot exactly identify why the insertion failed
             * Present the current row as a null row to the user.
             **/
            this.crsResolve.moveToInsertRow();
            for(i = 1; i <= icolCount; i++) {
                this.crsResolve.updateNull(i);
            }
            this.crsResolve.insertRow();
            this.crsResolve.moveToCurrentRow();
            
            throw ex;
        }
    }
    
    /**
     * Deletes the row in the underlying data source that corresponds to
     * a row that has been deleted in the given <code> CachedRowSet</code> object
     * and returns <code>false</code> if the deletion was successful.
     * <P>
     * This method is called internally by this writer's <code>writeData</code>
     * method when a row in the rowset has been deleted. The values in the
     * deleted row are the same as those that are stored in the original row
     * of the given <code>CachedRowSet</code> object.  If the values in the
     * original row differ from the row in the underlying data source, the row
     * in the data source is not deleted, and <code>deleteOriginalRow</code>
     * returns <code>true</code> to indicate that there was a conflict.
     *
     *
     * @return <code>false</code> if the deletion was successful, which means that
     *         there was no conflict; <code>true</code> otherwise
     * @throws SQLException if there was a database access error
     */
    private void deleteOriginalRow(CachedRowSet crs, CachedRowSetXImpl crsRes) throws SQLException {

            PreparedStatement pstmt;
            int i;
            int idx = 0;
            String strSelect;
            // Select the row from the database.
            ResultSet origVals = crs.getOriginalRow();
            origVals.next();
            
            deleteWhere = buildWhereClause(deleteWhere, origVals);
            if (((CachedRowSetX)crs).getPrintStatements()) {
                System.out.println("Writer:  pre-delete select " + selectCmd + deleteWhere);
            }
            pstmt = con.prepareStatement(selectCmd + deleteWhere,
                    ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
            
            
            StringBuffer msg = null ;
            for (i = 0; i < keyCols.length; i++) {
                if (params[i] != null) {
                    pstmt.setObject(++idx, params[i]);
                    if (((CachedRowSetX)crs).getPrintStatements()) {
                        if ( msg == null ) msg = new StringBuffer(100) ;
                        msg.append(" DeleteParam[" + (idx) +"]=("  );
                        if ( params[i] != null ) {
                            msg.append(params[i].getClass().getName() + "," + params[i].toString() +")" );
                        } else {
                            msg.append("null" ) ;
                        }
                    }
                } else {
                    continue;
                }
            }
            if ( msg != null ) System.out.println(msg); // log delete parameters
            
            // drivers may not support the following, so ignore exceptions.
            try {
                pstmt.setMaxRows( 2 ); // we only care if there's not 1!
            } catch (Exception ex) { ; }
            try {
                pstmt.setMaxFieldSize(crs.getMaxFieldSize());
            } catch (Exception ex) { ; }
            try {
                pstmt.setEscapeProcessing(crs.getEscapeProcessing());
            } catch (Exception ex) { ; }
            try {
                pstmt.setQueryTimeout(crs.getQueryTimeout());
            } catch (Exception ex) { ; }
            
            if (((CachedRowSetX)crs).getPrintStatements()) {
                System.out.println("Writer: executing pre-delete select" ) ;
            }
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next() == true) {
                if (rs.next()) {
                    // more than one row
                    throw new SQLException(rb.getString("MORE_THAN_ONE_ROW_MATCHED_DELETE")); //NOI18N
                }
                rs.first();
                
                // Now check all the values in rs to be same in
                // db also before actually going ahead with deleting
                boolean boolChanged = false;
                
                crsRes.moveToInsertRow();
                int rsColIdx = 1;
                for (i = 1; i <= crs.getMetaData().getColumnCount(); i++) {
                    if (whereColumnsInThisTable.contains(new Integer(i))) {
                        if(origVals.getObject(i) == null && rs.getObject(rsColIdx) == null) {
                            crsRes.updateNull(i);
                        } else if ((origVals.getObject(i) == null && rs.getObject(rsColIdx) != null) ||
                                (origVals.getObject(i) != null && rs.getObject(rsColIdx) == null) ||
                                !(((origVals.getObject(i)).toString()).equals(
                                (rs.getObject(rsColIdx)).toString()))) {
                            boolChanged = true;
                            crsRes.updateObject(i,origVals.getObject(i));
                        } else {
                            crsRes.updateNull(i);
                        }
                        rsColIdx++;
                    }
                }
                crsRes.insertRow();
                crsRes.moveToCurrentRow();
                
                if(boolChanged) {
                    // do not delete as values in db have changed
                    // deletion will not happen for this row from db
                    // exit now returning true. i.e. conflict
                    throw new SQLException(rb.getString("WILL_NOT_DELETE_AS_DB_VALS_CHANGED")); //NOI18N
                } else {
                    // delete the row.
                    // Go ahead with deleting,
                    // don't do anything here
                }
                
                String cmd = deleteCmd + deleteWhere;
                
                pstmt = con.prepareStatement(cmd);
                
                idx = 0;
                for (i = 0; i < keyCols.length; i++) {
                    if (params[i] != null) {
                        pstmt.setObject(++idx, params[i]);
                    } else {
                        continue;
                    }
                }
                
                if (((CachedRowSetX)crs).getPrintStatements()) {
                    System.out.println("Writer:  executing delete " + cmd );
                }
                if (pstmt.executeUpdate() != 1) {
                    throw new SQLException(rb.getString("DELETED_MORE_THAN_ONE_ROW")); //NOI18N
                }
                pstmt.close();
            } else {
                // didn't find the row
                throw new SQLException(rb.getString("DID_NOT_FIND_ROW_TO_DELETE")); //NOI18N
            }

    }
    
    /**
     * Sets the reader for this writer to the given reader.
     *
     * @throws SQLException if a database access error occurs
     */
    public void setReader(CachedRowSetXReader reader) throws SQLException {
        this.reader = reader;
    }
    
    /**
     * Gets the reader for this writer.
     *
     * @throws SQLException if a database access error occurs
     */
    public CachedRowSetXReader getReader() throws SQLException {
        return reader;
    }
    
    /**
     * Composes a <code>SELECT</code>, <code>UPDATE</code>, <code>INSERT</code>,
     * and <code>DELETE</code> statement that can be used by this writer to
     * write data to the data source backing the given <code>CachedRowSet</code>
     * object.
     *
     * @ param caller a <code>CachedRowSet</code> object for which this
     *                <code>CachedRowSetXWriter</code> object is the writer
     * @throws SQLException if a database access error occurs
     */
    private void initSQLStatements(CachedRowSet caller) throws SQLException {
        
        int i;
        
        callerMd = caller.getMetaData();
        callerColumnCount = callerMd.getColumnCount();
        if (callerColumnCount < 1)
            // No data, so return.
            return;
        
        /*
         * If the RowSet has a Table name we should use it.
         * This is really a hack to get round the fact that
         * a lot of the jdbc drivers can't provide the tab.
         */
        String table = caller.getTableName();
        if (table == null) {
            /*
             * attempt to build a table name using the info
             * that the driver gave us for the first column
             * in the source result set.
             */
            table = callerMd.getTableName(1);
            if (table == null || table.length() == 0) {
                throw new SQLException(rb.getString("CANNOT_DETERMINE_TABLE_NAME")); //NOI18N
            }
        }
        /*
         * If the rowset has a catalogName, we will use it.
         */
        String catalog = ((CachedRowSetX)caller).getCatalogName();
        if (catalog == null) {
            catalog = callerMd.getCatalogName(1);
            if (catalog != null && catalog.length() == 0) {
                catalog = null;
            }
        }
        /*
         * If the rowset has a schemaName, we will use it.
         */
        String schema = ((CachedRowSetX)caller).getSchemaName();
        if (schema == null) {
            schema = callerMd.getSchemaName(1);
            if (schema != null && schema.length() == 0) {
                schema = null;
            }
        }
        DatabaseMetaData dbmd = con.getMetaData();
        
        /* NOTE: first do whereColumns,
         * then do updataable -- same if updateable is null
         * then dto insertable -- same as updatable if insertable is null
         */
        
        // compute the real column names
        realColumnNames = new String[callerColumnCount];
        String[] callerColumnNames = ((CachedRowSetX)caller).getColumnNames();
        for (i=0; i < callerColumnCount; i++) {
            realColumnNames[i] = callerMd.getColumnName(i+1);
            if (callerColumnNames != null && callerColumnNames.length > i
                    && callerColumnNames[i] != null) {
                realColumnNames[i] = callerColumnNames[i];
            }
        }
        
        // compute the where columns for the table
        whereColumnsInThisTable = new ArrayList();
        String[] columnCatalogNames = ((CachedRowSetX)caller).getColumnCatalogNames();
        String[] columnSchemaNames = ((CachedRowSetX)caller).getColumnSchemaNames();
        String[] columnTableNames = ((CachedRowSetX)caller).getColumnTableNames();
        
        /*
         * If the user has not set catalog, schema nor any catalog, schema, or table for an column,
         * then perhaps we should not be so harsh in rejecting columns that match the table.
         *
         * That is, if the user has not set any advanced properties, then lets allow the
         * "relaxed" column matching that existed before adding the advanced properties.  Yes,
         * one can get into trouble this way, but that can still be fixed by setting one ore
         * more advanced properties.  Once any of these are set, the relaxed column matching
         * is turned off.
         *
         * This change will allow, without the user setting any properties, updates on Oracle
         * databases even though it returns blank table names for all columns in a result set.
         */
        boolean relaxed = (columnCatalogNames == null &&
                columnSchemaNames == null &&
                columnTableNames == null &&
                ((CachedRowSetX)caller).getCatalogName() == null &&
                ((CachedRowSetX)caller).getSchemaName() == null);
        
        for (i = 0; i < callerColumnCount; i++) {
            
            // attempt to match on catalog
            if (!matcher(i, catalog, columnCatalogNames, callerMd.getCatalogName(i+1), relaxed)) {
                continue;
            }
            // attempt to match on schema
            if (!matcher(i, schema, columnSchemaNames, callerMd.getSchemaName(i+1), relaxed)) {
                continue;
            }
            // attempt to match on table
            if (!matcher(i, table, columnTableNames, callerMd.getTableName(i+1), relaxed)) {
                continue;
            }
            // We are part of the table (or we don't know because we have nulls)
            // tricky for cases where we have nulls
            // we don't know whether we should call isColumnPartOfTable!!!!!
            boolean match = true;
            if (columnCatalogNames == null &&
                    columnSchemaNames == null &&
                    columnTableNames == null) {
                // do things the old way, look up column in table
                match = isColumnPartOfTable(dbmd, realColumnNames[i], catalog, schema,
                        table, callerMd.getTableName(i+1));
            }
            if (match) {
                whereColumnsInThisTable.add(new Integer(i+1));
            }
        }
        /*
         * compute columns to be updated
         * if caller.getUpdatableColumns() is null, just use the where columns
         */
        updatableColumnsInThisTable = new ArrayList();
        boolean[] callerUpdatableColumns = ((CachedRowSetX)caller).getUpdatableColumns();
        for (i=0; i < callerColumnCount; i++) {
            boolean match;
            if (callerUpdatableColumns != null && callerUpdatableColumns.length > i) {
                match = callerUpdatableColumns[i];
            } else {
                match = whereColumnsInThisTable.contains(new Integer(i+1));
            }
            if (match) {
                updatableColumnsInThisTable.add(new Integer(i+1));
            }
        }
        /*
         * compute columns to be inserted
         * if caller.getInsertableColumns() is null, just use the updatable columns
         */
        insertableColumnsInThisTable = new ArrayList();
        boolean[] callerInsertableColumns = ((CachedRowSetX)caller).getInsertableColumns();
        for (i=0; i < callerColumnCount; i++) {
            boolean match;
            if (callerInsertableColumns != null && callerInsertableColumns.length > i) {
                match = callerInsertableColumns[i];
            } else {
                match = updatableColumnsInThisTable.contains(new Integer(i+1));
            }
            if (match) {
                insertableColumnsInThisTable.add(new Integer(i+1));
            }
        }
        // Compose SELECT statement
        boolean firstTime = true;
        selectCmd = "SELECT "; //NOI18N
        
        /*
         * Compose a SELECT statement.  There are three parts.
         * JK It is currently unclear what columns should constitute the select.  For now, we'll
         * use the same columns as in the where clause -- that is, we might include columns
         * that are not updatable.
         */
        if (whereColumnsInThisTable.size() == 0) {
            throw new SQLException(MessageFormat.format(rb.getString("NO_COLS_IN_TABLE"), //NOI18N
                    new Object[] { table }));
        }
        for (i=0; i < callerColumnCount; i++) {
            if (whereColumnsInThisTable.contains(new Integer(i+1))) {
                if (firstTime) {
                    firstTime = false;
                } else {
                    selectCmd += ", "; //NOI18N
                }
                selectCmd += realColumnNames[i];
            }
        }
        selectCmd += " "; //NOI18N
        
        // FROM clause.
        selectCmd += "FROM " + buildTableName(dbmd, catalog, schema, table); //NOI18N
        
        /*
         * Compose an UPDATE statement.
         */
        updateCmd = "UPDATE " + buildTableName(dbmd, catalog, schema, table); //NOI18N
        updateCmd += "SET "; //NOI18N
        
        /*
         * Compose an INSERT statement.
         */
        insertCmd = "INSERT INTO " + buildTableName(dbmd, catalog, schema, table); //NOI18N
        // Column list
        insertCmd += "("; //NOI18N
        firstTime = true;
        for (i=0; i < callerColumnCount; i++) {
            if (insertableColumnsInThisTable.contains(new Integer(i+1))) {
                if (firstTime) {
                    firstTime = false;
                } else {
                    insertCmd += ", "; //NOI18N
                }
                insertCmd += realColumnNames[i];
            }
        }
        insertCmd += ") VALUES ("; //NOI18N
        firstTime = true;
        for (i = 0; i < callerColumnCount; i++) {
            if (insertableColumnsInThisTable.contains(new Integer(i+1))) {
                if (firstTime) {
                    firstTime = false;
                } else {
                    insertCmd += ", "; //NOI18N
                }
                insertCmd += "?"; //NOI18N
            }
        }
        insertCmd += ")"; //NOI18N
        
        /*
         * Compose a DELETE statement.
         */
        deleteCmd = "DELETE FROM " + buildTableName(dbmd, catalog, schema, table); //NOI18N
        
        /*
         * set the key desriptors that will be
         * needed to construct where clauses.
         */
        buildKeyDesc(caller);
    }
    
    /**
     * Returns a fully qualified table name built from the given catalog and
     * table names. The given metadata object is used to get the proper order
     * and separator.
     *
     * @param dbmd a <code>DatabaseMetaData</code> object that contains metadata
     * 		about this writer's <code>CachedRowSet</code> object
     * @param catalog a <code>String</code> object with the rowset's catalog
     * 		name
     * @param table a <code>String</code> object with the name of the table from
     * 		which this writer's rowset was derived
     * @return a <code>String</code> object with the fully qualified name of the
     *		table from which this writer's rowset was derived
     * @throws SQLException if a database access error occurs
     */
    private String buildTableName(DatabaseMetaData dbmd,
            String catalog, String schema, String table) throws SQLException {
        
        // trim all the leading and trailing whitespaces,
        // white spaces can never be catalog, schema or a table name.
        
        String cmd = new String();
        
        if (catalog != null) {
            catalog = catalog.trim();
        }
        if (schema != null) {
            schema = schema.trim();
        }
        if (table != null) {
            table = table.trim();
        }
        
        if (dbmd.isCatalogAtStart() == true) {
            if (catalog != null && catalog.length() > 0) {
                cmd += catalog + dbmd.getCatalogSeparator();
            }
            if (schema != null && schema.length() > 0) {
                cmd += schema + "."; //NOI18N
            }
            cmd += table;
        } else {
            if (schema != null && schema.length() > 0) {
                cmd += schema + "."; //NOI18N
            }
            cmd += table;
            if (catalog != null && catalog.length() > 0) {
                cmd += dbmd.getCatalogSeparator() + catalog;
            }
        }
        cmd += " "; //NOI18N
        return cmd;
    }
    
    /*
     * matcher:
     *
     * idx                 = i (zero based)
     * value               = catalog | schema | table
     * columnValueNames    = columnCatalogNames | columnSchemaNames | columnTableNames
     * rsmdColumnValueName = callerMd.getCatalogName(i+1) | getSchemaName(i+1) | getTableName(i+1)
     */
    private boolean matcher(int idx, String value, String[] columnValueNames, String rsmdName,
            boolean relaxedMatch) {
        /*
         * some drivers, e.g., mysql, will return schemaName.tableName for rsmd.getTableName()
         * let's strip it (i.e., let's strip any prepended value from rsmdName)
         */
        int pos = rsmdName.lastIndexOf('.');
        if (pos != -1) {
            rsmdName = rsmdName.substring(pos+1);
        }
        // attempt to match on value
        String columnValue = null;
        if (columnValueNames != null && columnValueNames.length > idx &&
                columnValueNames[idx] != null) {
            columnValue = columnValueNames[idx];
        }
        // if columnValue is still null, attempt to get it from the rsmd
        if (columnValue == null) {
            columnValue = rsmdName;
            if (columnValue != null && columnValue.length() == 0) {
                columnValue = null;
            }
        }
        // let's see if there is a match with value
        if ((value == null && columnValue != null) ||
                (value != null && columnValue == null)) {
            /*
             * one null, the other not, don't include it in the where
             * except in the case of relaxedMatch, then we don't want to preclude a match
             */
            if (relaxedMatch) {
                return true;
            }
            return false;
        } else if (value == null && columnValue == null) {
            // columns match, fall through as it has potential for inclusion
        } else if (!columnValue.equals(value)) {
            // both are non-null and they don't match, don't include it in the where
            return false;
        }
        // at this point, either the column value matched or both it and value were null
        // as such, we have a potential for inclusion in the where clause
        return true;
    }
    
    /**
     * Note: We ignore keyColumns in cachedRowSet and build the keyColumns
     * with whereColumnsInThisTable
     *
     * Assigns to the given <code>CachedRowSet</code> object's
     * <code>params</code>
     * field an array whose length equals the number of columns needed
     * to uniquely identify a row in the rowset. The array is given
     * values by the method <code>buildWhereClause</code>.
     * <P>
     * If the <code>CachedRowSet</code> object's <code>keyCols</code>
     * field has length <code>0</code> or is <code>null</code>, the array
     * is set with the column number of every column in the rowset.
     * Otherwise, the array in the field <code>keyCols</code> is set with only
     * the column numbers of the columns that are required to form a unique
     * identifier for a row.
     *
     * @param crs the <code>CachedRowSet</code> object for which this
     *     <code>CachedRowSetXWriter</code> object is the writer
     *
     * @throws SQLException if a database access error occurs
     */
    private void buildKeyDesc(CachedRowSet crs) throws SQLException {
        
        /* We now always construct keyCols from whereColumnsInThisTable
        keyCols = crs.getKeyColumns();
        keyCols = null;
        if (keyCols == null || keyCols.length == 0) {
            keyCols = new int[callerColumnCount];
            for (int i = 0; i < keyCols.length; ) {
                keyCols[i] = ++i;
            }
        }
         */
        keyCols = new int[whereColumnsInThisTable.size()];
        int idx = 0;
        for (Iterator i = whereColumnsInThisTable.iterator(); i.hasNext();) {
            keyCols[idx++] = ((Integer)i.next()).intValue();
        }
        params = new Object[keyCols.length];
    }
    
    /**
     * <p>
     * Constructs an SQL <code>WHERE</code> clause using the given
     * string as a starting point. The resulting clause will contain
     * a column name and " = ?" for each key column, that is, each column
     * that is needed to form a unique identifier for a row in the rowset.
     * This <code>WHERE</code> clause can be added to
     * a <code>PreparedStatement</code> object that updates, inserts, or
     * deletes a row.
     * </p>
     *
     * <p>
     * Note: In the case of a join, the where clause will only cotain
     *       columns from the table specified by the CachedRowSet's
     *       table property.
     * </p>
     *
     * <p>
     * This method uses the given result set to access values in the
     * <code>CachedRowSet</code> object that called this writer.  These
     * values are used to build the array of parameters that will serve as
     * replacements for the "?" parameter placeholders in the
     * <code>PreparedStatement</code> object that is sent to the
     * <code>CachedRowSet</code> object's underlying data source.
     * </p>
     *
     * @param whereClause a <code>String</code> object that is an empty
     *                    string ("")
     * @param rs a <code>ResultSet</code> object that can be used
     *           to access the <code>CachedRowSet</code> object's data
     * @return a <code>WHERE</code> clause of the form "<code>WHERE</code>
     *         columnName = ? AND columnName = ? AND columnName = ? ..."
     * @throws SQLException if a database access error occurs
     */
    private String buildWhereClause(String whereClause, ResultSet rs) throws SQLException {
        whereClause = "WHERE "; //NOI18N
        
        boolean firstTime = true;
        for (int i = 0; i < keyCols.length; i++) {
            if (firstTime) {
                firstTime = false;
            } else {
                whereClause += "AND "; //NOI18N
            }
            whereClause += callerMd.getColumnName(keyCols[i]);
            params[i] = rs.getObject(keyCols[i]);
            if (rs.wasNull() == true) {
                whereClause += " IS NULL "; //NOI18N
            } else {
                whereClause += " = ? "; //NOI18N
            }
        }
        return whereClause;
    }
    
    /**
     * determine if if this column is part of table tableName
     */
    private static boolean isColumnPartOfTable(DatabaseMetaData dbmd,
            String columnName,
            String catalogName,
            String schemaName,
            String tableName,
            String columnTableName)
            throws SQLException {
        
        // some databases return a qualified tableName for ResultSetMetaData.getTableName(int)
        // so we will strip it
        int idx = columnTableName.lastIndexOf('.');
        if (idx != -1) {
            columnTableName = columnTableName.substring(idx+1);
        }
        
        ResultSet colRs = dbmd.getColumns(
                (catalogName == null || catalogName.equals(""))? null: catalogName, //NOI18N
                (schemaName  == null || schemaName.equals("")) ? null: schemaName, //NOI18N
                tableName,
                columnName);
        boolean rc = colRs.next();
        colRs.close();
        if (rc) {
            rc = columnTableName == null || columnTableName.equals("") || //NOI18N
                    columnTableName.equals(tableName);
        }
        return rc;
    }
    
    void updateResolvedConflictToDB(CachedRowSet crs, Connection con) throws SQLException {
        //String updateExe = ;
        PreparedStatement pStmt  ;
        String strWhere = "WHERE " ; //NOI18N
        String strExec =" "; //NOI18N
        String strUpdate = "UPDATE "; //NOI18N
        int icolCount = crs.getMetaData().getColumnCount();
        int keyColumns[] = crs.getKeyColumns();
        Object param[];
        String strSet=""; //NOI18N
        
        strWhere = buildWhereClause(strWhere, crs);
        
        if (keyColumns == null || keyColumns.length == 0) {
            keyColumns = new int[icolCount];
            for (int i = 0; i < keyColumns.length; ) {
                keyColumns[i] = ++i;
            }
        }
        param = new Object[keyColumns.length];
        
        strUpdate = "UPDATE " + buildTableName(con.getMetaData(), //NOI18N
                crs.getMetaData().getCatalogName(1),
                crs.getMetaData().getSchemaName(1),
                crs.getTableName());
        
        // changed or updated values will become part of
        // set clause here
        strUpdate += "SET "; //NOI18N
        
        boolean first = true;
        
        for (int i=1; i<=icolCount;i++) {
            if (crs.columnUpdated(i)) {
                if (first == false) {
                    strSet += ", "; //NOI18N
                }
                strSet += crs.getMetaData().getColumnName(i);
                strSet += " = ? "; //NOI18N
                first = false;
            } //end if
        } //end for
        
        // keycols will become part of where clause
        strUpdate += strSet;
        strWhere = "WHERE "; //NOI18N
        
        for (int i = 0; i < keyColumns.length; i++) {
            if (i > 0) {
                strWhere += "AND "; //NOI18N
            }
            strWhere += crs.getMetaData().getColumnName(keyColumns[i]);
            param[i] = crs.getObject(keyColumns[i]);
            if (crs.wasNull() == true) {
                strWhere += " IS NULL "; //NOI18N
            } else {
                strWhere += " = ? "; //NOI18N
            }
        }
        strUpdate += strWhere;
        
        if (((CachedRowSetX)crs).getPrintStatements()) {
            System.out.println(strUpdate);
        }
        pStmt = con.prepareStatement(strUpdate);
        
        int idx = 0;
        for (int i = 0; i < icolCount; i++) {
            if(crs.columnUpdated(i+1)) {
                Object obj = crs.getObject(i+1);
                if (obj != null) {
                    pStmt.setObject(++idx, obj);
                } else {
                    pStmt.setNull(++idx, crs.getMetaData().getColumnType(i + 1));
                } //end if ..else
            } //end if crs.column...
        } //end for
        
        // Set the key cols for after WHERE =? clause
        for (int i = 0; i < keyColumns.length; i++) {
            if (param[i] != null) {
                pStmt.setObject(++idx, param[i]);
            }
        }
        
        int id = pStmt.executeUpdate();
    }
    
    
    /**
     *
     */
    public void commit() throws SQLException {
        con.commit();
        if (reader.getCloseConnection() == true) {
            con.close();
            con = null;
        }
    }
    
    /**
     *
     */
    public void rollback() throws SQLException {
        con.rollback();
        if (reader.getCloseConnection() == true) {
            con.close();
            con = null;
        }
    }
    
    /**
     *
     */
    public void rollback(Savepoint s) throws SQLException {
        con.rollback(s);
        if (reader.getCloseConnection() == true) {
            con.close();
            con = null;
        }
    }
    
    public void closeConnection() throws SQLException {
        if (con != null && reader.getCloseConnection() == true) {
            try {
                if (!con.getAutoCommit()) {
                    con.rollback();
                }
            } catch (Exception dummy) {
                /*
                 * not an error condition, we're closing anyway, but
                 * we'd like to clean up any locks if we can since
                 * it is not clear the connection pool will clean
                 * these connections in a timely manner
                 */
            }
            con.close();
        }
    }
}
