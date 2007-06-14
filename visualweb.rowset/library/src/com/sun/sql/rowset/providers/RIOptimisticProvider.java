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

package com.sun.sql.rowset.providers;

import java.io.*;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.sql.*;

import javax.sql.rowset.spi.*;

import com.sun.sql.rowset.internal.*;

/**
 * An implemenation of based on Sun's reference implementation of RIOptimisticProvider.
 *
 * @see javax.sql.rowset.spi.SyncProvider
 * @see javax.sql.rowset.spi.SyncProviderException
 * @see javax.sql.rowset.spi.SyncFactory
 * @see javax.sql.rowset.spi.SyncFactoryException
 *
 */
public final class RIOptimisticProvider extends SyncProvider implements Serializable {

    private static ResourceBundle rb =
        ResourceBundle.getBundle("com.sun.sql.rowset.providers.Bundle", Locale.getDefault()); // NOI18N
        
    private CachedRowSetXReader reader;
    private CachedRowSetXWriter writer;
    
    /**
     * The unique provider indentifier.
     */
    private String providerID = "com.sun.rowset.providers.RIOptimisticProvider"; //NOI18N
    
    /**
     * The vendor name of this SyncProvider implementation
     */
    private String vendorName = "Sun Microsystems Inc.";     //NOI18N
    
    /**
     * The version number of this SyncProvider implementation
     */
    private String versionNumber = "1.0"; //NOI18N

    /** 
     * Creates an <code>RIOptimisticProvider</code> object initialized with the
     * fully qualified class name of this <code>SyncProvider</code> implementation
     * and a default reader and writer.
     * <P>
     * This provider is available to all disconnected <code>RowSet</code> implementations
     *  as the default persistence provider.
     */ 
    public RIOptimisticProvider() {        
        providerID = this.getClass().getName();
        reader = new CachedRowSetXReader();
        writer = new CachedRowSetXWriter();
    }

    /**
     * Returns the <code>'javax.sql.rowset.providers.RIOptimisticProvider'</code> 
     * provider identification string. 
     *
     * @return String Provider ID of this persistence provider
     */
    public String getProviderID() {
        return providerID;
    }
    
    /**
     * Returns the <code>javax.sql.RowSetWriter</code> object for this
     * <code>RIOptimisticProvider</code> object.  This is the writer that will
     * write changes made to the <code>Rowset</code> object back to the data source.
     *
     * @return the <code>javax.sql.RowSetWriter</code> object for this
     *     <code>RIOptimisticProvider</code> object
     */
    public RowSetWriter getRowSetWriter() {
	try {
	    writer.setReader(reader);
	} catch (java.sql.SQLException e) {}
	return writer;
    }    

    /**
     * Returns the <code>javax.sql.RowSetReader</code> object for this
     * <code>RIOptimisticProvider</code> object.  This is the reader that will
     * populate a <code>RowSet</code> object using this <code>RIOptimisticProvider</code>.
     *
     * @return the <code>javax.sql.RowSetReader</code> object for this
     *     <code>RIOptimisticProvider</code> object
     */
    public RowSetReader getRowSetReader() {
        return reader;
    }    

    /**
     * Returns the <code>SyncProvider</code> grade of synchronization that
     * <code>RowSet</code> objects can expect when using this 
     * implementation. As an optimisic synchonization provider, the writer
     * will only check rows that have been modified in the <code>RowSet</code>
     * object.
     */
    public int getProviderGrade() {
        return SyncProvider.GRADE_CHECK_MODIFIED_AT_COMMIT;
    }

    /**
     * Modifies the data source lock severity according to the standard
     * <code>SyncProvider</code> classifications.  
     *
     * @param datasource_lock An <code>int</code> indicating the level of locking to be 
     *        set; must be one of the following constants:
     * <PRE>            
     *       SyncProvider.DATASOURCE_NO_LOCK,
     *       SyncProvider.DATASOURCE_ROW_LOCK,
     *       SyncProvider.DATASOURCE_TABLE_LOCK,
     *       SyncProvider.DATASOURCE_DB_LOCk
     * </PRE>            
     * @throws SyncProviderException if the parameter specified is not
     *           <code>SyncProvider.DATASOURCE_NO_LOCK</code>
     */
    public void setDataSourceLock(int datasource_lock) throws SyncProviderException {
        if(datasource_lock != SyncProvider.DATASOURCE_NO_LOCK ) {
            throw new SyncProviderException(rb.getString("LOCKING_CLASSIFICATION_NOT_SUPPORTED")); //NOI18N
        }   
    }

    /**
     * Returns the active data source lock severity in this 
     * reference implementation of the <code>SyncProvider</code>
     * abstract class.
     * 
     * @return <code>SyncProvider.DATASOURCE_NO_LOCK</code>.
     *     The reference implementation does not support data source locks.
     */
    public int getDataSourceLock() throws SyncProviderException {
        return SyncProvider.DATASOURCE_NO_LOCK;
    }

    /**
     * Returns the supported updatable view abilities of the
     * reference implementation of the <code>SyncProvider</code>
     * abstract class.
     *
     * @return <code>SyncProvider.NONUPDATABLE_VIEW_SYNC</code>. The 
     *     the reference implementation does not support updating tables
     *     that are the source of a view.
     */
    public int supportsUpdatableView() {
	return SyncProvider.NONUPDATABLE_VIEW_SYNC;
    }
    
    /**
     * Returns the release version ID of the Reference Implementation Optimistic 
     * Synchronization Provider.
     *
     * @return the <code>String</code> detailing the version number of this SyncProvider
     */
    public String getVersion() {
        return this.versionNumber;
    }
    
    /**
     * Returns the vendor name of the Reference Implemntation Optimistic
     * Syncchronication Provider
     *
     * @return the <code>String</code> detailing the vendor name of this
     *      SyncProvider
     */
    public String getVendor() {
        return this.vendorName;
    }
}
