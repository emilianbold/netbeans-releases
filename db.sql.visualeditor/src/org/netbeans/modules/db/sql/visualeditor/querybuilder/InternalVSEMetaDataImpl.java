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
/*
 * InternalVSEMetaDataImpl.java
 *
 * Fetches MetaData for VSE, from DatabaseConnection
 */

package org.netbeans.modules.db.sql.visualeditor.querybuilder;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

import org.netbeans.api.db.explorer.DatabaseConnection;

import org.netbeans.modules.db.sql.visualeditor.Log;
import org.netbeans.modules.db.sql.visualeditor.api.VisualSQLEditorMetaData;

/**
 * Database meta data cache for the QueryBuilder.
 *
 * @author jimdavidson
 */
public class InternalVSEMetaDataImpl implements VisualSQLEditorMetaData {

    private DatabaseMetaData 	databaseMetaData = null ;
    private DatabaseConnection	dbconn;
    private List<String>	schemas;
    private List<List<String>> 	allTables = null ;
    private int 		hashSizeForTables = 30 ;
    private Hashtable 		fkExportedTable = new Hashtable(30) ;
    private Hashtable 		fkImportedTable = new Hashtable(30) ;
    private Hashtable 		columnNameTable = new Hashtable(30) ;
    private Hashtable 		allColumnsTable = new Hashtable(400) ;

    /** Constructor */

    public InternalVSEMetaDataImpl(DatabaseConnection dbconn)
    {
	this.dbconn = dbconn;
        try {
	    // JDTODO:  add listeners for MetaData changes.
	    // dataSourceInfo.addConnectionListener(listener) ;
	    initMetaData() ;

        } catch (SQLException sqle) {
	    // TODO: Better error handling
            Log.getLogger().warning("Could not create cache for " + sqle.getLocalizedMessage()) ;
        }
    }


    /*
     * getSchemas()
     */
    /* getSchemas() is different from the other metadata calls, in that it just asks the
     * dbconn or datasource for the list of schemas, not the database
     */
    public List<String> getSchemas()
    {
	if (schemas!=null)
	    return schemas;

	schemas = new ArrayList();
	schemas.add(dbconn.getSchema());
        return schemas;
    }


    /**
     * getTables()
     */
    public List<List<String>> getTables()
	throws SQLException
    {
	if (databaseMetaData==null)
	    initMetaData() ;
        
        if ( allTables != null ) 
	    return allTables ;

        Log.getLogger().finest( " loading tables" ) ;
        
        allTables = new ArrayList<List<String>>() ;
        
        List<String> schemas = getSchemas() ;
        if ( schemas != null && schemas.size() > 0 ) {
            for ( String schema : schemas ) {
                Log.getLogger().finest( "  schema: " + schema ) ;
		ResultSet rs = databaseMetaData.getTables(null, schema, "%", new String[] {"TABLES","VIEWS"}); // NOI18N
		while (rs.next()) {
		    ArrayList table = new ArrayList();
		    table.add(rs.getString("TABLE_SCHEM"));
		    table.add(rs.getString("TABLE_NAME"));
		    allTables.add(table);
		}
            }
        } else {
            Log.getLogger().finest( " all schemas" ) ;
	    ResultSet rs = databaseMetaData.getTables(null, null, "%", new String[] {"TABLES","VIEWS"}); // NOI18N
	    while (rs.next()) {
		ArrayList table = new ArrayList();
		table.add(rs.getString("TABLE_SCHEM"));
		table.add(rs.getString("TABLE_NAME"));
		allTables.add(table);
	    }
        }
        Log.getLogger().finest( " tables loaded " + allTables.size() ) ;

        return allTables ;
    }


    /*
     * getPrimaryKeys()
     */
    public List<String> getPrimaryKeys(String schema, String table)
	throws SQLException
     {
         Log.getLogger().entering( "InternalVSEMetaDataImpl", "getPrimaryKeys", new Object[] {schema, table});

         if ( databaseMetaData == null )
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



    /**
     * getImportedKeys()
     */
    public List<List<String>> getImportedKeys(String schema, String table)
	throws SQLException
    {
	return getForeignKeys(schema, table, false);
    }

    /**
     * getExportedKeys()
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
        Log.getLogger().entering("InternalVSEMetaDataImpl", "getForeignKeys", new Object[]{ schema, table }) ;

        if ( databaseMetaData == null )
	    initMetaData() ;

        Hashtable lookupTable ;
        if (exported) {
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


    /*
     * getColumns
     */
    public List<String> getColumns(String schema, String table)
	throws SQLException
    {
        Log.getLogger().entering( "InternalVSEMetaDataImpl", "getColumns", new Object[]{schema,table});

        if ( databaseMetaData == null )
	    initMetaData() ;
        
	String fullTableName=mergeTableName(schema, table);
        List<String> columnNames = (List<String>)columnNameTable.get(fullTableName) ;
        Log.getLogger().finest( "    cache hit="+ (columnNames!=null)) ;
        if (columnNames != null)
	    return columnNames ;
        
        columnNames = new ArrayList<String>() ;
        ResultSet rs = databaseMetaData.getColumns(null, schema, table, "%"); // NOI18N
        if (rs != null) {
            while (rs.next()) {
                columnNames.add(rs.getString("COLUMN_NAME")); // NOI18N
            }
            rs.close();
            
            if ( Log.getLogger().isLoggable(java.util.logging.Level.FINEST)) {
                for (int j=0; j<columnNames.size(); j++)
                    Log.getLogger().finest("     Column:" + (String) columnNames.get(j) ); // NOI18N
            }
        }
	Log.getLogger().finest( "   getColumnNames loaded  "+columnNames.size() ) ;
        columnNameTable.put(fullTableName, columnNames) ;
//         for ( int i = 0 ; i < columnNames.size() ; i++) {
//             allColumnsTable.put(columnNames.get(i),fullTableName) ;
//         }
        return columnNames ;
    }


//     private DatasourceConnectionListener listener = new DatasourceConnectionListener() {
//         public void dataSourceConnectionModified() {
//             logInfo( dataSourceInfo.getName() + " connectionModified event." ) ;
//             refresh() ;
//         }
//     } ;

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


    private void initMetaData() throws SQLException {
        databaseMetaData = getMetaData() ;
        refreshCacheTables() ;
    }
    
    private void refresh() {
	Log.getLogger().entering("InternalVSEMetaDataImpl", "refresh");
        databaseMetaData = null ;
        refreshCacheTables() ;
    }
    
    /**
     * clears the cache held in this instance.
     */
    private void refreshCacheTables() {
	schemas=null ;
        columnNameTable.clear() ;
        fkExportedTable.clear() ;
        fkImportedTable.clear() ;
        allTables = null ;
        allColumnsTable.clear() ;
        pkTable.clear() ;
    }

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
    
    private DatabaseMetaData getMetaData() throws SQLException {
        if (databaseMetaData == null) {
            databaseMetaData = dbconn.getJDBCConnection().getMetaData();
        }
        return databaseMetaData;
    }

}
