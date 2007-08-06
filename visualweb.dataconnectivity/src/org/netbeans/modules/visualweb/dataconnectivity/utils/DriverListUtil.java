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

package org.netbeans.modules.visualweb.dataconnectivity.utils;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import org.netbeans.api.db.explorer.JDBCDriver;
import org.netbeans.api.db.explorer.JDBCDriverManager;

public class DriverListUtil {

    private static List drivers = new LinkedList();
    private static List urls = new LinkedList();
    private static List names = new LinkedList();

    //private default constructor -> singleton
    private DriverListUtil() {
    }

    static {
        names.add("IBM DB2 (net)");
        drivers.add("COM.ibm.db2.jdbc.net.DB2Driver");
        urls.add("jdbc:db2://<HOST>:<PORT>/<DB>");

        names.add("IBM DB2 (local)");
        drivers.add("COM.ibm.db2.jdbc.app.DB2Driver");
        urls.add("jdbc:db2:<DB>");
        
        names.add("JDBC-ODBC Bridge");
        drivers.add("sun.jdbc.odbc.JdbcOdbcDriver");
        urls.add("jdbc:odbc:<DB>");
        
        names.add("Microsoft SQL Server (Weblogic driver)");
        drivers.add("weblogic.jdbc.mssqlserver4.Driver");
        urls.add("jdbc:weblogic:mssqlserver4:<DB>@<HOST>:<PORT>");
        
        names.add("Oracle"); //thin
        drivers.add("oracle.jdbc.OracleDriver");
        urls.add("jdbc:oracle:thin:@<HOST>:<PORT>:<SID>");
        
        names.add("PointBase"); //Network Server
        drivers.add("com.pointbase.jdbc.jdbcUniversalDriver");
        urls.add("jdbc:pointbase://<HOST>[:<PORT>]/<DB>");

        names.add("PointBase"); //Embedded Server
        drivers.add("com.pointbase.jdbc.jdbcUniversalDriver");
        urls.add("jdbc:pointbase://embedded[:<PORT>]/<DB>");
        
        names.add("PointBase"); //Mobile Edition
        drivers.add("com.pointbase.jdbc.jdbcUniversalDriver");
        urls.add("jdbc:pointbase:<DB>");
        
        names.add("Cloudscape");
        drivers.add("COM.cloudscape.core.JDBCDriver");
        urls.add("jdbc:cloudscape:<DB>");
        
        names.add("Cloudscape RMI");
        drivers.add("RmiJdbc.RJDriver");
        urls.add("jdbc:rmi://<HOST>:<PORT>/jdbc:cloudscape:<DB>");
        
        names.add("Java DB (Embedded)");
        drivers.add("org.apache.derby.jdbc.EmbeddedDriver");
        urls.add("jdbc:derby:<DB>");
        
        names.add("Java DB (Network)");
        drivers.add("org.apache.derby.jdbc.ClientDriver");
        urls.add("jdbc:derby://<HOST>[:<PORT>]/databaseName[;attr1=value1[;...]]");
        
        names.add("Apache Derby (Net)");
        drivers.add("com.ibm.db2.jcc.DB2Driver");
        urls.add("jdbc:derby:net://<HOST>[:<PORT>]/<DB>");
        
        names.add("Firebird (JCA/JDBC driver)");
        drivers.add("org.firebirdsql.jdbc.FBDriver");
        urls.add("jdbc:firebirdsql:[//<HOST>[:<PORT>]/]<DB>");
        
        names.add("FirstSQL/J"); //Enterprise Server Edition
        drivers.add("COM.FirstSQL.Dbcp.DbcpDriver");
        urls.add("jdbc:dbcp://<HOST>:<PORT>");
        
        names.add("FirstSQL/J"); //Professional Edition
        drivers.add("COM.FirstSQL.Dbcp.DbcpDriver");
        urls.add("jdbc:dbcp://local");
        
        names.add("IBM DB2 (DataDirect Connect for JDBC)");
        drivers.add("com.ddtek.jdbc.db2.DB2Driver");
        urls.add("jdbc:datadirect:db2://<HOST>:<PORT>[;databaseName=<DB>]");

        names.add("IDS Server");
        drivers.add("ids.sql.IDSDriver");
        urls.add("jdbc:ids://<HOST>:<PORT>/conn?dsn='<ODBC_DSN_NAME>'");
        
        names.add("Informix Dynamic Server");
        drivers.add("com.informix.jdbc.IfxDriver");
        urls.add("jdbc:informix-sqli://<HOST>:<PORT>/<DB>:INFORMIXSERVER=<SERVER_NAME>");

        names.add("Informix Dynamic Server (DataDirect Connect for JDBC)");
        drivers.add("com.ddtek.jdbc.informix.InformixDriver");
        urls.add("jdbc:datadirect:informix://<HOST>:<PORT>;informixServer=<SERVER_NAME>;databaseName=<DB>");
        
        names.add("InstantDB (v3.13 and earlier)");
        drivers.add("jdbc.idbDriver");
        urls.add("jdbc:idb:<DB>");
        
        names.add("InstantDB (v3.14 and later)");
        drivers.add("org.enhydra.instantdb.jdbc.idbDriver");
        urls.add("jdbc:idb:<DB>");
        
        names.add("Interbase (InterClient driver)");
        drivers.add("interbase.interclient.Driver");
        urls.add("jdbc:interbase://<HOST>/<DB>");
        
        names.add("HSQLDB"); //(server)
        drivers.add("org.hsqldb.jdbcDriver");
        urls.add("jdbc:hsqldb:hsql://<HOST>[:<PORT>]");
        
        names.add("HSQLDB"); //(standalone)
        drivers.add("org.hsqldb.jdbcDriver");
        urls.add("jdbc:hsqldb:<DB>");
        
        names.add("HSQLDB"); //(webserver)
        drivers.add("org.hsqldb.jdbcDriver");
        urls.add("jdbc:hsqldb:http://<HOST>[:<PORT>]");
        
        names.add("HSQLDB"); //(in-memory)
        drivers.add("org.hsqldb.jdbcDriver");
        urls.add("jdbc:hsqldb:.");
        
        names.add("Hypersonic SQL (v1.2 and earlier)");
        drivers.add("hSql.hDriver");
        urls.add("jdbc:HypersonicSQL:<DB>");
        
        names.add("Hypersonic SQL (v1.3 and later)");
        drivers.add("org.hsql.jdbcDriver");
        urls.add("jdbc:HypersonicSQL:<DB>");
        
        names.add("jTDS");
        drivers.add("net.sourceforge.jtds.jdbc.Driver");
        urls.add("jdbc:jtds:sqlserver://<server>[:<PORT>][/<DATABASE>]");
        
        names.add("jTDS");
        drivers.add("net.sourceforge.jtds.jdbc.Driver");
        urls.add("jdbc:jtds:sybase://<server>[:<PORT>][/<DATABASE>]");
        
        names.add("Mckoi SQL Database"); //(server)
        drivers.add("com.mckoi.JDBCDriver");
        urls.add("jdbc:mckoi://<HOST>[:<PORT>]");
        
        names.add("Mckoi SQL Database"); //(standalone)
        drivers.add("com.mckoi.JDBCDriver");
        urls.add("jdbc:mckoi:local://<DB>");
        
        names.add("Microsoft SQL Server (DataDirect Connect for JDBC)");
        drivers.add("com.ddtek.jdbc.sqlserver.SQLServerDriver");
        urls.add("jdbc:datadirect:sqlserver://<HOST>:<PORT>[;databaseName=<DB>]");
        
        names.add("Microsoft SQL Server (JTurbo driver)");
        drivers.add("com.ashna.jturbo.driver.Driver");
        urls.add("jdbc:JTurbo://<HOST>:<PORT>/<DB>");
        
        names.add("Microsoft SQL Server (Sprinta driver)");
        drivers.add("com.inet.tds.TdsDriver");
        urls.add("jdbc:inetdae:<HOST>:<PORT>?database=<DB>");
        
        names.add("Microsoft SQL Server 2000 (Microsoft driver)");
        drivers.add("com.microsoft.jdbc.sqlserver.SQLServerDriver");
        urls.add("jdbc:microsoft:sqlserver://<HOST>:<PORT>[;DatabaseName=<DB>]");
        
        names.add("MySQL (Connector/J driver)");
        drivers.add("com.mysql.jdbc.Driver");
        urls.add("jdbc:mysql://<HOST>:<PORT>/<DB>");
        
        names.add("MySQL (MM.MySQL driver)");
        drivers.add("org.gjt.mm.mysql.Driver");
        urls.add("jdbc:mysql://<HOST>:<PORT>/<DB>");
        
        names.add("Oracle"); //OCI 8i
        drivers.add("oracle.jdbc.driver.OracleDriver");
        urls.add("jdbc:oracle:oci8:@<SID>");
        
        names.add("Oracle"); //OCI 9i
        drivers.add("oracle.jdbc.driver.OracleDriver");
        urls.add("jdbc:oracle:oci:@<SID>");
        
        names.add("Oracle (DataDirect Connect for JDBC)");
        drivers.add("com.ddtek.jdbc.oracle.OracleDriver");
        urls.add("jdbc:datadirect:oracle://<HOST>:<PORT>;SID=<SID>");
        
        names.add("PostgreSQL (v6.5 and earlier)");
        drivers.add("postgresql.Driver");
        urls.add("jdbc:postgresql://<HOST>:<PORT>/<DB>");
        
        names.add("PostgreSQL (v7.0 and later)");
        drivers.add("org.postgresql.Driver");
        urls.add("jdbc:postgresql://<HOST>:<PORT>/<DB>");
        
        names.add("Quadcap Embeddable Database");
        drivers.add("com.quadcap.jdbc.JdbcDriver");
        urls.add("jdbc:qed:<DB>");
        
        names.add("Sybase (jConnect 4.2 and earlier)");
        drivers.add("com.sybase.jdbc.SybDriver");
        urls.add("jdbc:sybase:Tds:<HOST>:<PORT>");
        
        names.add("Sybase (jConnect 5.2)");
        drivers.add("com.sybase.jdbc2.jdbc.SybDriver");
        urls.add("jdbc:sybase:Tds:<HOST>:<PORT>");
        
        names.add("Sybase (DataDirect Connect for JDBC)");
        drivers.add("com.ddtek.jdbc.sybase.SybaseDriver");
        urls.add("jdbc:datadirect:sybase://<HOST>:<PORT>[;databaseName=<DB>]");

        // Following four entries for drivers to be included in Java Studio Enterprise 7 (Bow)
        names.add("Microsoft SQL Server Driver");
        drivers.add("com.sun.sql.jdbc.sqlserver.SQLServerDriver");
        urls.add("jdbc:sun:sqlserver://server_name[:portNumber]");        
        
        names.add("DB2 Driver");
        drivers.add("com.sun.sql.jdbc.db2.DB2Driver");
        urls.add("jdbc:sun:db2://server_name:portNumber;databaseName=DATABASENAME");  
        
        names.add("Oracle Driver");
        drivers.add("com.sun.sql.jdbc.oracle.OracleDriver");
        urls.add("jdbc:sun:oracle://server_name[:portNumber][;SID=DATABASENAME]");  
        
        names.add("Sybase Driver");
        drivers.add("com.sun.sql.jdbc.sybase.SybaseDriver");
        urls.add("jdbc:sun:sybase://server_name[:portNumber]");          
    }
    
    public static Set getDrivers() {
        return new TreeSet(drivers);
    }
    
    public static List getURLs(String drv) {
        List ret = new LinkedList();
        
        for (int i = 0; i < drivers.size(); i++)
            if (((String) drivers.get(i)).equals(drv))
                ret.add(urls.get(i));
        
        return ret;
    }
    
    public static String getName(String drv) {
        for (int i = 0; i < drivers.size(); i++)
            if (((String) drivers.get(i)).equals(drv))
                return (String) names.get(i);
        
        return "";
    }
    
    public static String getDriver(String name) {
        for (int i = 0; i < names.size(); i++)
            if (((String) names.get(i)).equals(name))
                return (String) drivers.get(i);
        
        return "";
    }

    public static String findFreeName(String name) {
        String ret;
        Vector names = new Vector();
        JDBCDriver[] drivers = JDBCDriverManager.getDefault().getDrivers();
        for (int i = 0; i < drivers.length; i++)
            names.add(drivers[i].getDisplayName());
        
        if (names.contains(name))
            for (int i = 1;;i++) {
                ret = name + " (" + i + ")";
                if (!names.contains(ret))
                    return ret;
            }
        else
            return name;
    }
}
