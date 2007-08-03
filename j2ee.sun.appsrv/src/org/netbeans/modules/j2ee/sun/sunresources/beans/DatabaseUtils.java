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
/*
 * DatabaseUtils.java
 *
 * Created on June 26, 2006, 1:55 PM
 *
 */

package org.netbeans.modules.j2ee.sun.sunresources.beans;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 *
 * @author Nitya Doraisamy
 */
public class DatabaseUtils {
    
    private static HashMap driverMap;
    private static HashMap dsClassMap;
    
    /** Creates a new instance of DatabaseUtils */
    private DatabaseUtils() {
    }
    
    public static String getDriverName(String url){
        String driverName = null;
        Set urlKeys = driverMap.keySet();
        Iterator it = urlKeys.iterator();
        while(it.hasNext()){
            String urlPrefix = (String)it.next();
            if(url.startsWith(urlPrefix)){
                driverName = (String)driverMap.get(urlPrefix);
                return driverName;
            }
        }
        return driverName;
    }
    
    public static String getDSClassName(String url){
        String dsClass = null;
        Set urlKeys = dsClassMap.keySet();
        Iterator it = urlKeys.iterator();
        while(it.hasNext()){
            String urlPrefix = (String)it.next();
            if(url.startsWith(urlPrefix)){
                dsClass = (String)dsClassMap.get(urlPrefix);
                return dsClass;
            }
        }
        return dsClass;
    }
    
    
    /**
     * Gets the URL prefix fiven a Datasource Classname
     * @param inClass Datasource Classname
     * @return URL prefix for given Datasource Classname
     */
    public static String getUrlPrefix(String inClass){
        String prefix = null;
        Set urlKeys = dsClassMap.keySet();
        Iterator it = urlKeys.iterator();
        while(it.hasNext()){
            String urlPrefix = (String)it.next();
            String dsClass = (String)dsClassMap.get(urlPrefix);
            if(dsClass.equalsIgnoreCase(inClass))
                return urlPrefix;           
        }
        return prefix; 
    }
    
    static {
        driverMap = new HashMap();
        
        //IBM DB2 
        driverMap.put("jdbc:db2:", "COM.ibm.db2.jdbc.net.DB2Driver");
        //JDBC-ODBC Bridge
        driverMap.put("jdbc:odbc:", "sun.jdbc.odbc.JdbcOdbcDriver");
        //Microsoft SQL Server (Weblogic driver)
        driverMap.put("jdbc:weblogic:mssqlserver4:", "weblogic.jdbc.mssqlserver4.Driver");
        
        //Oracle-thin
        driverMap.put("jdbc:oracle:thin:", "oracle.jdbc.driver.OracleDriver");
        //Oracle //OCI 8i
        driverMap.put("jdbc:oracle:oci8:", "oracle.jdbc.driver.OracleDriver");
        //Oracle //OCI 9i
        driverMap.put("jdbc:oracle:oci:", "oracle.jdbc.driver.OracleDriver");
        
        //PointBase
        driverMap.put("jdbc:pointbase:", "com.pointbase.jdbc.jdbcUniversalDriver");
        //Cloudscape
        driverMap.put("jdbc:cloudscape:", "COM.cloudscape.core.JDBCDriver");
        //Java DB (Net)
        driverMap.put("jdbc:derby:", "org.apache.derby.jdbc.ClientDriver");
        //Firebird (JCA/JDBC driver)
        driverMap.put("jdbc:firebirdsql:", "org.firebirdsql.jdbc.FBDriver");
        //FirstSQL/J //Enterprise Server Edition
        driverMap.put("jdbc:dbcp:", "COM.FirstSQL.Dbcp.DbcpDriver");
        //FirstSQL/J //Professional Edition
        driverMap.put("jdbc:dbcp:", "COM.FirstSQL.Dbcp.DbcpDriver");
        
        //IBM DB2 (DataDirect Connect for JDBC)
        driverMap.put("jdbc:datadirect:db2:", "com.ddtek.jdbc.db2.DB2Driver");
        //Informix Dynamic Server (DataDirect Connect for JDBC)
        driverMap.put("jdbc:datadirect:informix:", "com.ddtek.jdbc.informix.InformixDriver");
        //Oracle (DataDirect Connect for JDBC)
        driverMap.put("jdbc:datadirect:oracle:", "com.ddtek.jdbc.oracle.OracleDriver");
        //Microsoft SQL Server (DataDirect Connect for JDBC)
        driverMap.put("jdbc:datadirect:sqlserver:", "com.ddtek.jdbc.sqlserver.SQLServerDriver");
        //Sybase (DataDirect Connect for JDBC)
        driverMap.put("jdbc:datadirect:sybase:", "com.ddtek.jdbc.sybase.SybaseDriver");
        
        //IDS Server
        driverMap.put("jdbc:ids:", "ids.sql.IDSDriver");
        //Informix Dynamic Server
        driverMap.put("jdbc:informix-sqli:", "com.informix.jdbc.IfxDriver");
        //InstantDB (v3.13 and earlier)
        driverMap.put("jdbc:idb:", "jdbc.idbDriver");
        //InstantDB (v3.14 and later)
        driverMap.put("jdbc:idb:", "org.enhydra.instantdb.jdbc.idbDriver");
        //Interbase (InterClient driver)
        driverMap.put("jdbc:interbase:", "interbase.interclient.Driver");
        //HSQLDB //(server)
        driverMap.put("jdbc:hsqldb:hsql:", "org.hsqldb.jdbcDriver");
        //HSQLDB //(webserver)
        driverMap.put("jdbc:hsqldb:http:", "org.hsqldb.jdbcDriver");
        //Hypersonic SQL (v1.2 and earlier)
        driverMap.put("jdbc:HypersonicSQL:", "hSql.hDriver");
        //Hypersonic SQL (v1.3 and later)
        driverMap.put("jdbc:HypersonicSQL:", "org.hsql.jdbcDriver");
        //jTDS
        driverMap.put("jdbc:jtds:sqlserver:", "net.sourceforge.jtds.jdbc.Driver");
        //jTDS
        driverMap.put("jdbc:jtds:sybase:", "net.sourceforge.jtds.jdbc.Driver");
        //Mckoi SQL Database //(server)
        driverMap.put("jdbc:mckoi:", "com.mckoi.JDBCDriver");
        //Mckoi SQL Database //(standalone)
        driverMap.put("jdbc:mckoi:local:", "com.mckoi.JDBCDriver");
        //Microsoft SQL Server (JTurbo driver)
        driverMap.put("jdbc:JTurbo:", "com.ashna.jturbo.driver.Driver");
        //Microsoft SQL Server (JTurbo driver 3.0)
        //driverMap.put("jdbc:JTurbo:", "com.newatlanta.jturbo.driver.Driver");
        //Microsoft SQL Server (Sprinta driver)
        driverMap.put("jdbc:inetdae:", "com.inet.tds.TdsDriver");
        //Microsoft SQL Server 2000 (Microsoft driver)
        driverMap.put("jdbc:microsoft:sqlserver:", "com.microsoft.jdbc.sqlserver.SQLServerDriver");
        driverMap.put("jdbc:sqlserver:", "com.microsoft.sqlserver.jdbc.SQLServerDriver"); //NOI18N
        //MySQL (Connector/J driver)
        driverMap.put("jdbc:mysql:", "com.mysql.jdbc.Driver");
        //MySQL (MM.MySQL driver)
        //driverMap.put("jdbc:mysql:", "org.gjt.mm.mysql.Driver");
        
        //PostgreSQL (v6.5 and earlier)
        //driverMap.put("jdbc:postgresql:", "postgresql.Driver");
        //PostgreSQL (v7.0 and later)
        driverMap.put("jdbc:postgresql:", "org.postgresql.Driver");
        //Quadcap Embeddable Database
        driverMap.put("jdbc:qed:", "com.quadcap.jdbc.JdbcDriver");
        //Sybase (jConnect 4.2 and earlier)
        //driverMap.put("jdbc:sybase:Tds:", "com.sybase.jdbc.SybDriver");
        //Sybase (jConnect 5.2)
        driverMap.put("jdbc:sybase:Tds:", "com.sybase.jdbc2.jdbc.SybDriver");
        
        // Following four entries for drivers to be included in Java Studio Enterprise 7 (Bow)
        //Microsoft SQL Server Driver
        driverMap.put("jdbc:sun:sqlserver:", "com.sun.sql.jdbc.sqlserver.SQLServerDriver");
        //DB2 Driver
        driverMap.put("jdbc:sun:db2:", "com.sun.sql.jdbc.db2.DB2Driver");
        //Oracle Driver
        driverMap.put("jdbc:sun:oracle:", "com.sun.sql.jdbc.oracle.OracleDriver");
        //Sybase Driver
        driverMap.put("jdbc:sun:sybase:", "com.sun.sql.jdbc.sybase.SybaseDriver");
        
    }

    static {
        dsClassMap = new HashMap();
        
        //IBM DB2 
        dsClassMap.put("jdbc:db2:", "com.ibm.db2.jcc.DB2DataSource");
        //JDBC-ODBC Bridge
        dsClassMap.put("jdbc:odbc:", "sun.jdbc.odbc.JdbcOdbcDriver");
        //Microsoft SQL Server (Weblogic driver)
        //dsClassMap.put("jdbc:weblogic:mssqlserver4:", "weblogic.jdbc.mssqlserver4.Driver");
        
        //Oracle-thin
        dsClassMap.put("jdbc:oracle:thin:", "oracle.jdbc.pool.OracleDataSource");
        //Oracle //OCI 8i
        dsClassMap.put("jdbc:oracle:oci8:", "oracle.jdbc.pool.OracleDataSource");
        //Oracle //OCI 9i
        dsClassMap.put("jdbc:oracle:oci:", "oracle.jdbc.pool.OracleDataSource");
        
        //PointBase
        dsClassMap.put("jdbc:pointbase:", "com.pointbase.jdbc.jdbcDataSource");
        //Cloudscape
        dsClassMap.put("jdbc:cloudscape:", "com.cloudscape.core.BasicDataSource");
        //Java DB (Net)
        dsClassMap.put("jdbc:derby:", "org.apache.derby.jdbc.ClientDataSource");
        //Firebird (JCA/JDBC driver)
        //dsClassMap.put("jdbc:firebirdsql:", "org.firebirdsql.jdbc.FBDriver");
        //FirstSQL/J //Enterprise Server Edition
        //dsClassMap.put("jdbc:dbcp:", "COM.FirstSQL.Dbcp.DbcpDriver");
        //FirstSQL/J //Professional Edition
        //dsClassMap.put("jdbc:dbcp:", "COM.FirstSQL.Dbcp.DbcpDriver");
        
        //IBM DB2 (DataDirect Connect for JDBC)
        dsClassMap.put("jdbc:datadirect:db2:", "com.ddtek.jdbcx.db2.DB2DataSource");
        //Informix Dynamic Server (DataDirect Connect for JDBC)
        dsClassMap.put("jdbc:datadirect:informix:", "com.ddtek.jdbcx.informix.InformixDataSource");
        //Oracle (DataDirect Connect for JDBC)
        dsClassMap.put("jdbc:datadirect:oracle:", "com.ddtek.jdbcx.oracle.OracleDataSource");
        //Microsoft SQL Server (DataDirect Connect for JDBC)
        dsClassMap.put("jdbc:datadirect:sqlserver:", "com.ddtek.jdbcx.sqlserver.SQLServerDataSource");
        //Sybase (DataDirect Connect for JDBC)
        dsClassMap.put("jdbc:datadirect:sybase:", "com.ddtek.jdbcx.sybase.SybaseDataSource");
        
        //IDS Server
        dsClassMap.put("jdbc:ids:", "ids.sql.IDSDriver");
        //Informix Dynamic Server
        dsClassMap.put("jdbc:informix-sqli:", "com.informix.jdbcx.IfxDataSource");
        //InstantDB (v3.13 and earlier)
        //dsClassMap.put("jdbc:idb:", "jdbc.idbDriver");
        //InstantDB (v3.14 and later)
        //dsClassMap.put("jdbc:idb:", "org.enhydra.instantdb.jdbc.idbDriver");
        //Interbase (InterClient driver)
        //dsClassMap.put("jdbc:interbase:", "interbase.interclient.Driver");
        //HSQLDB //(server)
        //dsClassMap.put("jdbc:hsqldb:hsql:", "org.hsqldb.jdbcDriver");
        //HSQLDB //(webserver)
        //dsClassMap.put("jdbc:hsqldb:http:", "org.hsqldb.jdbcDriver");
        //Hypersonic SQL (v1.2 and earlier)
        //dsClassMap.put("jdbc:HypersonicSQL:", "hSql.hDriver");
        //Hypersonic SQL (v1.3 and later)
        //dsClassMap.put("jdbc:HypersonicSQL:", "org.hsql.jdbcDriver");
        //jTDS
        dsClassMap.put("jdbc:jtds:sqlserver:", "net.sourceforge.jtds.jdbcx.JtdsDataSource");
        //jTDS
        dsClassMap.put("jdbc:jtds:sybase:", "net.sourceforge.jtds.jdbcx.JtdsDataSource");
        //Mckoi SQL Database //(server)
        //dsClassMap.put("jdbc:mckoi:", "com.mckoi.JDBCDriver");
        //Mckoi SQL Database //(standalone)
        //dsClassMap.put("jdbc:mckoi:local:", "com.mckoi.JDBCDriver");
        //Microsoft SQL Server (JTurbo driver old version)
        //dsClassMap.put("jdbc:JTurbo:", "com.ashna.jturbo.driver.DataSource");
        //Microsoft SQL Server (JTurbo driver 3.0)
        dsClassMap.put("jdbc:JTurbo:", "com.newatlanta.jturbo.driver.DataSource");
        //Microsoft SQL Server (Sprinta driver)
        dsClassMap.put("jdbc:inetdae:", "com.inet.tds.TdsDataSource");
        //Microsoft SQL Server 2000 (Microsoft driver)
        dsClassMap.put("jdbc:microsoft:sqlserver:", "com.microsoft.jdbc.sqlserver.SQLServerDataSource");
        dsClassMap.put("jdbc:sqlserver:", "com.microsoft.sqlserver.jdbc.SQLServerDataSource"); //NOI18N
        //MySQL (Connector/J driver)
        //dsClassMap.put("jdbc:mysql:", "com.mysql.jdbc.Driver");
        //MySQL (MM.MySQL driver)
        dsClassMap.put("jdbc:mysql:", "com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource");
        
        //PostgreSQL (v6.5 and earlier)
        //dsClassMap.put("jdbc:postgresql:", "postgresql.Driver");
        //PostgreSQL (v7.0 and later)
        dsClassMap.put("jdbc:postgresql:", "org.postgresql.ds.PGSimpleDataSource");
        //Quadcap Embeddable Database
        //dsClassMap.put("jdbc:qed:", "com.quadcap.jdbc.JdbcDriver");
        //Sybase (jConnect 4.2 and earlier)
        //dsClassMap.put("jdbc:sybase:Tds:", "com.sybase.jdbc.SybDriver");
        //Sybase (jConnect 5.2)
        dsClassMap.put("jdbc:sybase:Tds:", "com.sybase.jdbc2.jdbc.SybDataSource");
        
        // Following four entries for drivers to be included in Java Studio Enterprise 7 (Bow)
        //Microsoft SQL Server Driver
        dsClassMap.put("jdbc:sun:sqlserver:", "com.sun.sql.jdbcx.sqlserver.SQLServerDataSource");
        //DB2 Driver
        dsClassMap.put("jdbc:sun:db2:", "com.sun.sql.jdbcx.db2.DB2DataSource");
        //Oracle Driver
        dsClassMap.put("jdbc:sun:oracle:", "com.sun.sql.jdbcx.oracle.OracleDataSource");
        //Sybase Driver
        dsClassMap.put("jdbc:sun:sybase:", "com.sun.sql.jdbcx.sybase.SybaseDataSource");
        
    }
}
