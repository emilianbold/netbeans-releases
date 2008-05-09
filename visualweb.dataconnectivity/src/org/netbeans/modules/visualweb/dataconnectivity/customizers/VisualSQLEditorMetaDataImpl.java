/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
/*
 * VisualSQLEditorMetaDataImpl.java
 *
 * Retrieves the meta data for a datasource/datasourceinfo
 * Created on April 30, 2005, 5:36 PM

 */

package org.netbeans.modules.visualweb.dataconnectivity.customizers;

import org.netbeans.modules.visualweb.dataconnectivity.Log;
import org.netbeans.modules.visualweb.dataconnectivity.model.DataSourceInfo;
import org.netbeans.modules.visualweb.dataconnectivity.model.DatasourceConnectionListener;
import org.netbeans.modules.db.sql.visualeditor.api.VisualSQLEditorMetaData;
import org.netbeans.modules.visualweb.dataconnectivity.sql.DatabaseMetaDataHelper;
import java.sql.DatabaseMetaData;

import org.netbeans.modules.visualweb.dataconnectivity.model.DatasourceConnectionListener ;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import javax.naming.NamingException;
import org.netbeans.modules.visualweb.dataconnectivity.sql.DesignTimeDataSourceHelper;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;

/**
 * Database meta data cache for the QueryBuilder.
 *
 * @author jfbrown
 */
public class VisualSQLEditorMetaDataImpl implements VisualSQLEditorMetaData {

    private int hashSizeForTables = 30 ;

    private DataSourceInfo dataSourceInfo = null ;
    private DatabaseMetaData databaseMetaData = null ;
    private DatabaseMetaDataHelper dbmdh = null ;

    private static Hashtable dataSourceCache = new Hashtable() ;

    /**
     * Factory methods for finding a cache for a datasource.
     */
    public static synchronized VisualSQLEditorMetaData getDataSourceCache(String dsName) throws SQLException, NamingException {
        // data source name not found, most likely the name had been renamed
        if (dsName == null) {
            throw new NamingException(NbBundle.getMessage(VisualSQLEditorMetaDataImpl.class, "NAME_REMOVED") + " " + dsName); //NOI18N    
        }
        
	VisualSQLEditorMetaData cache = (VisualSQLEditorMetaData)dataSourceCache.get(dsName) ;
        if ( cache == null ){
            logInfo("Creating cache for " + dsName) ;
            cache = new VisualSQLEditorMetaDataImpl(dsName) ;
            dataSourceCache.put(dsName,cache) ;
        }
        return cache ;
    }
    public static synchronized boolean hasDataSourceCache(String dsName) {
        VisualSQLEditorMetaData cache = (VisualSQLEditorMetaData)dataSourceCache.get(dsName) ;
        if ( cache == null ){
            return false ;
        }
        return true ;
    }

    public static synchronized void removeDataSourceCache(String dsName) throws SQLException {
        VisualSQLEditorMetaDataImpl cache = (VisualSQLEditorMetaDataImpl)dataSourceCache.get(dsName) ;
        if ( cache != null ){
            cache.refresh() ;
            dataSourceCache.remove(dsName) ;
            logInfo("Removed cache for "+dsName) ;
        }
    }

    /** Constructor */

    static final String contextSuffix = "java:comp/env/jdbc/" ; // NOI18N
    public VisualSQLEditorMetaDataImpl(String dsName)
        throws SQLException
    {
        try {
	    if ( dsName.startsWith(contextSuffix)) dsName = dsName.substring(contextSuffix.length()) ;
            dataSourceInfo = DesignTimeDataSourceHelper.getDsInfo(dsName);

	    // TODO:  add listeners to dataSourceInfo for DatabaseMetaDataHelper changes.
	    dataSourceInfo.addConnectionListener(listener) ;

	    initMetaData() ;
        } catch (SQLException sqle) {
            Log.log("Could not create cache for " + sqle.getLocalizedMessage()) ;
            throw sqle ;
        }
    }

    private DatasourceConnectionListener listener = new DatasourceConnectionListener() {
        public void dataSourceConnectionModified() {
            logInfo( dataSourceInfo.getName() + " connectionModified event." ) ;
            refresh() ;
        }
    } ;

    public void initMetaData() throws SQLException {
        dbmdh = dataSourceInfo.getDatabaseMetaDataHelper() ;
        databaseMetaData = dbmdh.getMetaData() ;
        refreshCacheTables() ;
    }
    
    public void refresh() {
            logInfo( dataSourceInfo.getName() + " nulling cache." ) ;
        dbmdh = null ;
        databaseMetaData = null ;
        refreshCacheTables() ;
    }
    
    /**
     * clears the cache held in this instance.
     */
    public void refreshCacheTables() {
        columnNameTable.clear() ;
        fkExportedTable.clear() ;
        fkImportedTable.clear() ;
        allTables = null ;
        allColumnsTable.clear() ;
        allColumnsTableLoaded = false ;
        pkTable.clear() ;
	identifierQuoteString = null;
    }

//     public String[] getSchemas() {
//         // logInfo( dataSourceInfo.getName() + " getting schemas." ) ;
//         return dataSourceInfo.getDataSource().getSchemas() ;
//     }        

    /*
     * Updated version of getSchemas -- returns List instead of Array
     */
    public List<String> getSchemas() {
	List<String> schemas = Arrays.asList(dataSourceInfo.getDataSource().getSchemas());
	return schemas;
    }        

    
    /***
     * this validates the connection to the database.
     * returns true if valid connection.
     * If not valid, throws exception.
     * If the connection fails, we retry once.
     * 
     * If users get an SQLException on any other method, they
     * should call this.  If this method returns true, then retry the
     * method.  If it fails again, then the
     * method is the issue and there's probably no way for the consumer
     * to recover.  If this method throws an exception, then
     * the database isn't available.
     */
//     public boolean checkDataBaseConnection() throws SQLException {

//         boolean good = dataSourceInfo.testConnection() ;
//         if ( ! good ) {
//             TestConnectionResults res = dataSourceInfo.getLastTestResults() ;
//             refresh() ;
//             throw res.sqlExceptionRootCause ;
//         }
        
//         return good ; 
//     }
    
    /**
     * Returns the list of tables and views in the datasource's schemas.
     */
    private List<List<String>> allTables = null ;
    public List<List<String>> getTables()
	throws SQLException
    {
        if ( dbmdh == null )
	    initMetaData() ;
        
        if ( allTables != null ) 
	    return allTables ;

        logInfo( dataSourceInfo.getName() + " loading tables" ) ;
        
        allTables = new ArrayList<List<String>>() ;
        
        String[] schemas = dataSourceInfo.getSchemas() ;
        if ( schemas != null && schemas.length > 0 ) {
            for ( int scnt = 0 ; scnt < schemas.length ; scnt++ ) {
                logInfo( "  schema " + schemas[scnt] ) ;
                String tabs[] = dbmdh.getTables(schemas[scnt]) ;
                for (int icnt = 0 ; icnt < tabs.length ; icnt++ ) {
                    allTables.add(Arrays.asList(parseTableName(tabs[icnt]))) ;
                }
                String views[] = dbmdh.getViews(schemas[scnt]) ;
                for (int icnt = 0 ; icnt < views.length ; icnt++ ) {
                    allTables.add(Arrays.asList(parseTableName(views[icnt]))) ;
                }
            }
        } else {
             logInfo( "   all schemas" ) ;
            // get all of them.
            String tabs[] = dbmdh.getTables() ;
            for (int icnt = 0 ; icnt < tabs.length ; icnt++ ) {
                allTables.add(Arrays.asList(parseTableName(tabs[icnt]))) ;
            }
            String views[] = dbmdh.getViews() ;
            for (int icnt = 0 ; icnt < views.length ; icnt++ ) {
                allTables.add(Arrays.asList(parseTableName(views[icnt]))) ;
            }
        }
        logInfo( dataSourceInfo.getName() + " tables loaded " + allTables.size() ) ;

        return allTables ;
    }
    
// This will replace the original getTables() above, if we ever resolve the issue
// of schemas in DataSources/DatabaseConnections
//     public List<List<String>> getTables(String schema)
// 	throws SQLException
//     {
// 	List<List<String>> tables = null ;
// 	String tabs[] = dbmdh.getTables(schema) ;
// 	for (int icnt = 0 ; icnt < tabs.length ; icnt++ ) {
// 	    tables.add(Arrays.asList(parseTableName(tabs[icnt]))) ;
// 	}
// 	String views[] = dbmdh.getViews(schema) ;
// 	for (int icnt = 0 ; icnt < views.length ; icnt++ ) {
// 	    tables.add(Arrays.asList(parseTableName(tabs[icnt]))) ;
// 	}
// 	return tables;
//     }


    /****
     * Returns the a List of String objects of the primary key columns.
     */
    public Hashtable pkTable = new Hashtable(hashSizeForTables) ;
//     public List getPrimaryKeys(String fullTableName) throws SQLException
//     {
//         logInfo( dataSourceInfo.getName() + " getPrimaryKeys " + fullTableName ) ;

//         if ( dbmdh == null )
// 	    initMetaData() ;
        
//         List primaryKeys = (List)pkTable.get(fullTableName) ;
//         if ( primaryKeys != null )
// 	    return primaryKeys ;
        
//         primaryKeys = new ArrayList();

//         String[] tableDesrip = parseTableName(fullTableName) ;

//         ResultSet rs = databaseMetaData.getPrimaryKeys(null, tableDesrip[0], tableDesrip[1] );
//         if (rs != null) {
//             String name;
//             while (rs.next()) {
//                 name = rs.getString("COLUMN_NAME"); // NOI18N
//                 primaryKeys.add(name);
//             }
//             rs.close();
//         }
//         pkTable.put(fullTableName, primaryKeys) ;
//         return primaryKeys ;
//     }
       
    public List<String> getPrimaryKeys(String schema, String table) throws SQLException
     {
         logInfo( dataSourceInfo.getName() + " getPrimaryKeys " + schema + "." + table ) ;

         if ( dbmdh == null )
 	    initMetaData() ;
        
	 String fullTableName = mergeTableName(schema, table);
         List primaryKeys = (List)pkTable.get(fullTableName) ;
         if ( primaryKeys != null )
 	    return primaryKeys ;
        
         primaryKeys = new ArrayList();

         String[] tableDesrip = parseTableName(fullTableName) ;

         ResultSet rs = databaseMetaData.getPrimaryKeys(null, schema, table) ;
         if (rs != null) {
             String name;
             while (rs.next()) {
                 name = rs.getString("COLUMN_NAME"); // NOI18N
                 primaryKeys.add(name);
             }
             rs.close();
         }
         pkTable.put(fullTableName, primaryKeys) ;
         return primaryKeys ;
     }

    /****
     * 
     */ 
    public Hashtable fkExportedTable = new Hashtable(hashSizeForTables) ;
    public Hashtable fkImportedTable = new Hashtable(hashSizeForTables) ;
//     public List getForeignKeys(String fullTableName, boolean exported) throws SQLException {
//         logInfo( dataSourceInfo.getName() + " getForeignKeys " + fullTableName ) ;
//         if ( dbmdh == null ) initMetaData() ;
//         Hashtable lookupTable ;
//         if (exported ) {
//             lookupTable = fkExportedTable ;
//         } else {
//             lookupTable = fkImportedTable ;
//         }
//         List keys = (List)lookupTable.get(fullTableName) ;
//         if ( keys != null ) return keys ;
        
//         keys = new ArrayList() ;

//         String[] tableDesrip = parseTableName(fullTableName) ;

//         ResultSet rs =
//                 exported ?
//                     databaseMetaData.getExportedKeys(null, tableDesrip[0], tableDesrip[1] ) :
//                     databaseMetaData.getImportedKeys(null, tableDesrip[0], tableDesrip[1] );
//         if (rs != null) {
//             while (rs.next()) {
//                 String fschem = rs.getString("FKTABLE_SCHEM"); // NOI18N
//                 String pschem = rs.getString("PKTABLE_SCHEM"); // NOI18N
//                 String[] key = new String[] {
//                     ((fschem!=null) ? fschem+"." : "") + rs.getString("FKTABLE_NAME"), // NOI18N
//                             rs.getString("FKCOLUMN_NAME"), // NOI18N
//                             ((pschem!=null) ? pschem+"." : "") + rs.getString("PKTABLE_NAME"), // NOI18N
//                             rs.getString("PKCOLUMN_NAME") }; // NOI18N
//                 keys.add(key);
//             }
//             rs.close();
//         }
//         lookupTable.put(fullTableName, keys) ;
        
//         return keys ;
//     }
    
    /*
    public List getForeignKeys( String fullTableName ) throws SQLException {
        List keys = getForeignKeys(fullTableName, true);
        keys.addAll(getForeignKeys(fullTableName, false));
        return keys ;
    }
    */
    
    /**
     * New
     */
    public List<List<String>> getImportedKeys(String schema, String table)
	throws SQLException
    {
	return getForeignKeys(schema, table, false);
    }

    /**
     * New
     */
    public List<List<String>> getExportedKeys(String schema, String table)
	throws SQLException
    {
	return getForeignKeys(schema, table, true);
    }
    
    /**
     * New
     */
    private List<List<String>> getForeignKeys(String schema, String table, boolean exported)
	throws SQLException
    {
        logInfo( dataSourceInfo.getName() + " getForeign " + schema + " " + table ) ;

        if ( dbmdh == null )
	    initMetaData() ;

        Hashtable lookupTable ;
        if (exported ) {
            lookupTable = fkExportedTable ;
        } else {
            lookupTable = fkImportedTable ;
        }
	String fullTableName=mergeTableName(schema, table);
        List keys = (List)lookupTable.get(fullTableName) ;
        if ( keys != null )
	    return keys ;
        
        keys = new ArrayList() ;
        ResultSet rs =
                exported ?
                    databaseMetaData.getExportedKeys(null, schema, table ) :
                    databaseMetaData.getImportedKeys(null, schema, table );
        if (rs != null) {
            while (rs.next()) {
                String fschem = rs.getString("FKTABLE_SCHEM"); // NOI18N
                String pschem = rs.getString("PKTABLE_SCHEM"); // NOI18N
		List<String> key =
		    Arrays.asList(((fschem!=null) ? fschem+"." : "") + rs.getString("FKTABLE_NAME"), // NOI18N
				  rs.getString("FKCOLUMN_NAME"), // NOI18N
				  ((pschem!=null) ? pschem+"." : "") + rs.getString("PKTABLE_NAME"), // NOI18N
				  rs.getString("PKCOLUMN_NAME")) ; // NOI18N
                keys.add(key);
            }
            rs.close();
        }
        lookupTable.put(fullTableName, keys) ;
        
        return keys ;
    }

//     /**
//      * Returns the imported key columns for this table -- i.e., the columns
//      * whose value is a foreign key for another table.  These columns are
//      * displayed with a special icon in the Query Builder.
//      */
//     public Hashtable importKcTable = new Hashtable(hashSizeForTables) ;
//     public List getImportedKeyColumns(String fullTableName)
// 	throws SQLException
//     {
//         logInfo( dataSourceInfo.getName() + " getImportedKeyColumns " + fullTableName ) ;
//         if ( dbmdh == null )
// 	    initMetaData() ;
        
//         List keys = (List)importKcTable.get(fullTableName) ;
//         if ( keys != null )
// 	    return keys ;
        
//         keys = new ArrayList();
//         String[] tableDesrip = parseTableName(fullTableName) ;
//         ResultSet rs = databaseMetaData.getImportedKeys(null, tableDesrip[0], tableDesrip[1] );
//         if (rs != null) {
//             String name;
//             while (rs.next()) {
//                 name = rs.getString("FKCOLUMN_NAME"); // NOI18N
//                 keys.add(name);
//             }
//             rs.close();
//         }
//         importKcTable.put(fullTableName, keys) ;
//         return keys ;        
//     }

    /**
     * Returns the set of columns in the specified table
     * as a List of String instances.
     * If the specified table is null, return all columns.
     */
    private Hashtable columnNameTable = new Hashtable(hashSizeForTables) ;
    private Hashtable allColumnsTable = new Hashtable(400) ;
    private boolean allColumnsTableLoaded = false ;
    
//     public List  getColumnNames(String fullTableName) throws SQLException {
//         logInfo( dataSourceInfo.getName() + " getColumnNames " + fullTableName ) ;
//         if ( dbmdh == null )
// 	    initMetaData() ;
        
//         List columnNames = (List)columnNameTable.get(fullTableName) ;
//         logInfo( "    cache hit="+ (columnNames!=null)) ;
//         if (columnNames != null)
// 	    return columnNames ;
        
//         columnNames = new ArrayList() ;
        
//         String[] tableDesrip = parseTableName(fullTableName) ;
        
//         ResultSet rs = databaseMetaData.getColumns(null, tableDesrip[0],tableDesrip[1], "%"); // NOI18N
//         if (rs != null) {
//             while (rs.next()) {
//                 columnNames.add(rs.getString("COLUMN_NAME")); // NOI18N
//             }
//             rs.close();
            
//             if ( Log.isLoggable()) {
//                 for (int j=0; j<columnNames.size(); j++)
//                     Log.log("     Column:" + (String) columnNames.get(j) ); // NOI18N
//             }
//         }
//         logInfo( "   getColumnNames loaded  "+columnNames.size() ) ;
//         columnNameTable.put(fullTableName, columnNames) ;
//          for ( int i = 0 ; i < columnNames.size() ; i++) {
//              allColumnsTable.put(columnNames.get(i),fullTableName) ;
//          }
//         return columnNames ;
//     }

//     public java.util.Map getAllColumnNames() throws SQLException {
//         if ( ! allColumnsTableLoaded ) {
//             loadAllColumns() ;
//         }
//         return allColumnsTable ;
//     }


    /*
     * New
     */
    public List<String> getColumns(String schema, String table)
	throws SQLException
    {
        logInfo( dataSourceInfo.getName() + " getColumnsNames " + schema + " " + table ) ;
        if ( dbmdh == null )
	    initMetaData() ;
        
	String fullTableName=mergeTableName(schema, table);
        List<String> columnNames = (List<String>)columnNameTable.get(fullTableName) ;
        logInfo( "    cache hit="+ (columnNames!=null)) ;
        if (columnNames != null)
	    return columnNames ;
        
        columnNames = new ArrayList<String>() ;
        ResultSet rs = databaseMetaData.getColumns(null, schema, table, "%"); // NOI18N
        if (rs != null) {
            while (rs.next()) {
                columnNames.add(rs.getString("COLUMN_NAME")); // NOI18N
            }
            rs.close();
            
            if ( Log.isLoggable()) {
                for (int j=0; j<columnNames.size(); j++)
                    Log.log("     Column:" + (String) columnNames.get(j) ); // NOI18N
            }
        }
        logInfo( "   getColumnNames loaded  "+columnNames.size() ) ;
        columnNameTable.put(fullTableName, columnNames) ;
//         for ( int i = 0 ; i < columnNames.size() ; i++) {
//             allColumnsTable.put(columnNames.get(i),fullTableName) ;
//         }
        return columnNames ;
    }


//     /****
//      * gets the tables that have cached colmumn names 
//      */
//     public String[] getCachedColmnNameTables() throws SQLException {
//         List ret = new ArrayList() ;
//         java.util.Enumeration keys = columnNameTable.keys() ;
//         while ( keys.hasMoreElements() ) {
//             ret.add( (String)columnNameTable.get(keys.nextElement())) ;
//         }
//         return (String[])ret.toArray( new String[ret.size()]) ;
//     }
    
//     private void loadAllColumns() throws SQLException {

//         logInfo( dataSourceInfo.getName() + " loading all columns" ) ;

//         List<List<String>> tabs = getTables() ;
//         for ( int i = 0 ; i < tabs.size() ; i++ ) {
// 	    String schema = tabs.get(i).get(0);
// 	    String table = tabs.get(i).get(1);
// 	    String fullTableName=
// 		((schema==null) || schema.equals("")) ?
// 		table :
// 		schema + "." + table;
//             getColumnNames( fullTableName ) ;
// //            getColumnNames( (String)tabs.get(i)) ;
//         }
//         allColumnsTableLoaded = true ;
//         logInfo( dataSourceInfo.getName() + " finished loading all columns" ) ;
//     }

    String identifierQuoteString = null;
    
    public String getIdentifierQuoteString()
	throws SQLException
    {
        logInfo( dataSourceInfo.getName() + " getIdentifierQuoteString ");
        if ( dbmdh == null )
	    initMetaData() ;
	if ( identifierQuoteString != null) {
	    return identifierQuoteString;
	} else {
	    identifierQuoteString = databaseMetaData.getIdentifierQuoteString();
	    return identifierQuoteString;
	}
    }
    
    /* ================================================================ */
    /*****
     * parse a full table name, e.g. Schema.Table or Table
     * and returns an array where 
     * [0] = schema (or null if none found)
     * [1] = table name.
     */
    private static String[]  parseTableName(String fullTableName) {
        
        String[] retVal = new String[2] ;
        
        String[] table = fullTableName.split("\\."); // NOI18N
        if (table.length>1) {
            retVal[0] = table[0];
            retVal[1] = table[1];
        } else {
            retVal[0] = null ;
            retVal[1] = table[0];
        }
        return retVal ;
    }
    
    /*****
     * The opposite of parseTableName -- combine a schema and table into schema.tableName,
     * allowing for null or empty schema name
     */
    private static String mergeTableName(String schema, String table) {
	return
	    ((schema==null) || (schema.equals(""))) ?
	    table :
	    schema + "." + table;
    }
    
	
    private static final String LOGPREFIX = "DBCache: " ; // NOI18N
    private static ErrorManager err = ErrorManager.getDefault().getInstance("org.netbeans.modules.visualweb.dataconnectivity.customizers"); // NOI18N
    private static void logInfo(String msg) {
        err.log(ErrorManager.INFORMATIONAL, LOGPREFIX + msg);
    }
    private boolean isLoggable() {
        return err.isLoggable(ErrorManager.INFORMATIONAL);
    }    
    private boolean isLoggable(int sev) {
        return err.isLoggable(sev);
    }
    private void log(int sev, String msg) {
        err.log(sev, msg) ;
    }
    private static void logErrorInfo(String msg) {
        err.log(ErrorManager.ERROR, LOGPREFIX + msg);
    }

}
