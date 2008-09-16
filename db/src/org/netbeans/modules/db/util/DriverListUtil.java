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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.db.util;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import org.netbeans.api.db.explorer.JDBCDriver;
import org.netbeans.api.db.explorer.JDBCDriverManager;
import org.openide.util.NbBundle;

public class DriverListUtil {

    private static List<JdbcUrl> urls = new LinkedList<JdbcUrl>();
        
    private static String getMessage(String key, String ... params) {
        return NbBundle.getMessage(DriverListUtil.class, key, params);
    }

            //private default constructor -> singleton
    private DriverListUtil() {
    }
    
    
    private static void add(JdbcUrl url) {
        urls.add(url);
    }
    
    private static void add(String name, String type, String driverClassName, String urlTemplate) {
        urls.add(new JdbcUrl(name, name, driverClassName, type, urlTemplate));
    }
    
    private static void add(String name, String type, String driverClassName, String urlTemplate, boolean parseUrl) {
        urls.add(new JdbcUrl(name, name, driverClassName, type, urlTemplate, parseUrl));
    }
        
    private  static void add(String name, String driverClassName, String urlTemplate) {
        add(name, null, driverClassName, urlTemplate);
    }
    
    /**
     * Do NOT use this version of add() (with parseUrl set to true) unless you have added a 
     * unit test to DriverListUtilTest for the driver, to make sure that it is parsed correctly.
     */
    private static void add(String name, String driverClassName, String urlTemplate, boolean parseUrl) {
        add(name, null, driverClassName, urlTemplate, parseUrl);
    }

    static {
        add("IBM DB2 (net)",
        "COM.ibm.db2.jdbc.net.DB2Driver",
        "jdbc:db2://<HOST>:<PORT>/<DB>");

        add("IBM DB2 (local)",
        "COM.ibm.db2.jdbc.app.DB2Driver",
        "jdbc:db2:<DB>");
        
        add("JDBC-ODBC Bridge",
        "sun.jdbc.odbc.JdbcOdbcDriver",
        "jdbc:odbc:<DB>");
        
        add("Microsoft SQL Server (Weblogic driver)",
        "weblogic.jdbc.mssqlserver4.Driver",
        "jdbc:weblogic:mssqlserver4:<DB>@<HOST>[:<PORT>]");
        
        add("PointBase", "Network Server",
        "com.pointbase.jdbc.jdbcUniversalDriver",
        "jdbc:pointbase://<HOST>[:<PORT>]/<DB>");

        add("PointBase", "Embedded", 
        "com.pointbase.jdbc.jdbcUniversalDriver",
        "jdbc:pointbase://embedded[:<PORT>]/<DB>");
        
        add("PointBase", "Mobile Edition",
        "com.pointbase.jdbc.jdbcUniversalDriver",
        "jdbc:pointbase:<DB>");
        
        add("Cloudscape",
        "COM.cloudscape.core.JDBCDriver",
        "jdbc:cloudscape:<DB>");
        
        add("Cloudscape RMI",
        "RmiJdbc.RJDriver",
        "jdbc:rmi://<HOST>[:<PORT>]/jdbc:cloudscape:<DB>");
        
        add(getMessage("DRIVERNAME_JavaDbEmbedded"),
        "org.apache.derby.jdbc.EmbeddedDriver",
        "jdbc:derby:<DB>[;<ADDITIONAL>]", true);
        
        add(getMessage("DRIVERNAME_JavaDbNetwork"),
        "org.apache.derby.jdbc.ClientDriver",
        "jdbc:derby://<HOST>[:<PORT>]/<DB>[;<ADDITIONAL>]", true);
        
        add(getMessage("DRIVERNAME_DB2JCC"),
            "com.ibm.db2.jcc.DB2Driver",
            "jdbc:db2://<HOST>:<PORT>/<DB>[:<ADDITIONAL>]", true);
        
        add(getMessage("DRIVERNAME_DB2JCC"), getMessage("TYPE_IDS"),
                "com.ibm.db2.jcc.DB2Driver",
                "jdbc:ids://<HOST>:<PORT>/<DB>[:<ADDITIONAL>]", true);
        
        add(getMessage("DRIVERNAME_DB2JCC"), getMessage("TYPE_Cloudscape"),
                "com.ibm.db2.jcc.DB2Driver",
                "jdbc:db2j:net://<HOST>:<PORT>/<DB>[:<ADDITIONAL>]", true);

        add("Firebird (JCA/JDBC driver)",
        "org.firebirdsql.jdbc.FBDriver",
        "jdbc:firebirdsql:[//<HOST>[:<PORT>]/]<DB>");
        
        add("FirstSQL/J", "Enterprise Server Edition",
        "COM.FirstSQL.Dbcp.DbcpDriver",
        "jdbc:dbcp://<HOST>[:<PORT>]");
        
        add("FirstSQL/J" , "Professional Edition", 
        "COM.FirstSQL.Dbcp.DbcpDriver",
        "jdbc:dbcp://local");
        
        add("IBM DB2 (DataDirect Connect for JDBC)",
        "com.ddtek.jdbc.db2.DB2Driver",
        "jdbc:datadirect:db2://<HOST>[:<PORT>][;databaseName=<DB>]");

        add("IDS Server",
        "ids.sql.IDSDriver",
        "jdbc:ids://<HOST>[:<PORT>]/conn?dsn='<DSN>'");
        
        add("Informix Dynamic Server",
        "com.informix.jdbc.IfxDriver",
        "jdbc:informix-sqli://<HOST>[:<PORT>]/<DB>:INFORMIXSERVER=<SERVER_NAME>");

        add("Informix Dynamic Server (DataDirect Connect for JDBC)",
        "com.ddtek.jdbc.informix.InformixDriver",
        "jdbc:datadirect:informix://<HOST>[:<PORT>];informixServer=<SERVER_NAME>;databaseName=<DB>");
        
        add("InstantDB (v3.13 and earlier)",
        "jdbc.idbDriver",
        "jdbc:idb:<DB>");
        
        add("InstantDB (v3.14 and later)",
        "org.enhydra.instantdb.jdbc.idbDriver",
        "jdbc:idb:<DB>");
        
        add("Interbase (InterClient driver)",
        "interbase.interclient.Driver",
        "jdbc:interbase://<HOST>/<DB>");
        
        add("HSQLDB", "Server", 
        "org.hsqldb.jdbcDriver",
        "jdbc:hsqldb:hsql://<HOST>[:<PORT>]");
        
        add("HSQLDB", "Embedded",
        "org.hsqldb.jdbcDriver",
        "jdbc:hsqldb:<DB>");
        
        add("HSQLDB", "Web Server",
        "org.hsqldb.jdbcDriver",
        "jdbc:hsqldb:http://<HOST>[:<PORT>]");
        
        add("HSQLDB", "In-Memory",
        "org.hsqldb.jdbcDriver",
        "jdbc:hsqldb:.");
        
        add("Hypersonic SQL (v1.2 and earlier)",
        "hSql.hDriver",
        "jdbc:HypersonicSQL:<DB>");
        
        add("Hypersonic SQL (v1.3 and later)",
        "org.hsql.jdbcDriver",
        "jdbc:HypersonicSQL:<DB>");
        
        add(getMessage("DRIVERNAME_JTDS"), getMessage("TYPE_ForSQLServer"),
        "net.sourceforge.jtds.jdbc.Driver",
        "jdbc:jtds:sqlserver://<HOST>[:<PORT>][/<DB>][;<ADDITIONAL>]", true);
        
        add(getMessage("DRIVERNAME_JTDS"), getMessage("TYPE_ForSybase"),
        "net.sourceforge.jtds.jdbc.Driver",
        "jdbc:jtds:sybase://<HOST>[:<PORT>][/<DB>][;<ADDITIONAL>]", true);

        add("Mckoi SQL Database", "Server",
        "com.mckoi.JDBCDriver",
        "jdbc:mckoi://<HOST>[:<PORT>]");
        
        add("Mckoi SQL Database", "Embedded",
        "com.mckoi.JDBCDriver",
        "jdbc:mckoi:local://<DB>");
        
        add("Microsoft SQL Server (DataDirect Connect for JDBC)",
        "com.ddtek.jdbc.sqlserver.SQLServerDriver",
        "jdbc:datadirect:sqlserver://<HOST>[:<PORT>][;databaseName=<DB>]");
        
        add("Microsoft SQL Server (JTurbo driver)",
        "com.ashna.jturbo.driver.Driver",
        "jdbc:JTurbo://<HOST>:<PORT>/<DB>");
        
        add("Microsoft SQL Server (Sprinta driver)",
        "com.inet.tds.TdsDriver",
        "jdbc:inetdae:<HOST>[:<PORT>]?database=<DB>");
        
        add("Microsoft SQL Server 2000 (Microsoft driver)",
        "com.microsoft.jdbc.sqlserver.SQLServerDriver",
        "jdbc:microsoft:sqlserver://<HOST>[:<PORT>][;DatabaseName=<DB>]");

        add(getMessage("DRIVERNAME_MSSQL2005"),
        "com.microsoft.sqlserver.jdbc.SQLServerDriver",
        "jdbc:sqlserver://[<HOST>[\\<INSTANCE>][:<PORT>]][;databaseName=<DB>][;<ADDITIONAL>]", true);
        
        add(getMessage("DRIVERNAME_MySQL"),
                "com.mysql.jdbc.Driver", 
                "jdbc:mysql://[<HOST>[:<PORT>]]/<DB>[?<ADDITIONAL>]", true); // NOI18N
        
        add("MySQL (MM.MySQL driver)",
        "org.gjt.mm.mysql.Driver",
        "jdbc:mysql://<HOST>[:<PORT>]/<DB>");
        
        add(getMessage("DRIVERNAME_OracleThin"), 
                getMessage("TYPE_SID"), "oracle.jdbc.OracleDriver", 
                "jdbc:oracle:thin:@<HOST>:<PORT>:<SID>[?<ADDITIONAL>]", true); // NOI18N
        
        add(getMessage("DRIVERNAME_OracleThin"),
                getMessage("TYPE_Service"), "oracle.jdbc.OracleDriver", 
                "jdbc:oracle:thin:@//<HOST>:<PORT>/<SERVICE>[?<ADDITIONAL>]", true); // NOI18N
        
        add(getMessage("DRIVERNAME_OracleThin"),
                getMessage("TYPE_TNSName"), "oracle.jdbc.OracleDriver",
                "jdbc:oracle:thin:@<TNSNAME>[?<ADDITIONAL>]", true); // NOI18N
                
        add(getMessage("DRIVERNAME_OracleOCI"), 
                "OCI8 " + getMessage("TYPE_SID"), "oracle.jdbc.driver.OracleDriver", 
                "jdbc:oracle:oci8:@<HOST>:<PORT>:<SID>[?<ADDITIONAL>]", true); // NOI18N
        
        add(getMessage("DRIVERNAME_OracleOCI"),
                "OCI8 " + getMessage("TYPE_Service"), "oracle.jdbc.driver.OracleDriver", 
                "jdbc:oracle:oci8:@//<HOST>:<PORT>/<SERVICE>[?<ADDITIONAL>]", true); // NOI18N
        
        add(getMessage("DRIVERNAME_OracleOCI"),
                getMessage("TYPE_TNSName"), "oracle.jdbc.driver.OracleDriver",
                "jdbc:oracle:oci:@<TNSNAME>[?<ADDITIONAL>]", true); // NOI18N
        
        add(getMessage("DRIVERNAME_OracleOCI"), 
                getMessage("TYPE_SID"), "oracle.jdbc.driver.OracleDriver", 
                "jdbc:oracle:oci:@<HOST>:<PORT>:<SID>[?<ADDITIONAL>]", true); // NOI18N
        
        add(getMessage("DRIVERNAME_OracleOCI"),
                getMessage("TYPE_Service"), "oracle.jdbc.driver.OracleDriver", 
                "jdbc:oracle:oci:@//<HOST>:<PORT>/<SERVICE>[?<ADDITIONAL>]", true); // NOI18N
        
        add("Oracle (DataDirect Connect for JDBC)",
        "com.ddtek.jdbc.oracle.OracleDriver",
        "jdbc:datadirect:oracle://<HOST>[:<PORT>];SID=<SID>");
        
        add("PostgreSQL (v6.5 and earlier)",
        "postgresql.Driver",
        "jdbc:postgresql:[//<HOST>[:<PORT>]/]<DB>[?<ADDITIONAL>]");
        
        add(getMessage("DRIVERNAME_PostgreSQL"), // 7.0 and later
        "org.postgresql.Driver",
        "jdbc:postgresql:[//<HOST>[:<PORT>]/]<DB>[?<ADDITIONAL>]", true);
        
        add("Quadcap Embeddable Database",
        "com.quadcap.jdbc.JdbcDriver",
        "jdbc:qed:<DB>");
        
        add("Sybase (jConnect 4.2 and earlier)",
        "com.sybase.jdbc.SybDriver",
        "jdbc:sybase:Tds:<HOST>[:<PORT>]");
        
        add("Sybase (jConnect 5.2)",
        "com.sybase.jdbc2.jdbc.SybDriver",
        "jdbc:sybase:Tds:<HOST>[:<PORT>]");
        
        add("Sybase (DataDirect Connect for JDBC)",
        "com.ddtek.jdbc.sybase.SybaseDriver",
        "jdbc:datadirect:sybase://<HOST>[:<PORT>][;databaseName=<DB>]");

        // Following four entries for drivers to be included in Java Studio Enterprise 7 (Bow)
        add("Microsoft SQL Server Driver",
        "com.sun.sql.jdbc.sqlserver.SQLServerDriver",
        "jdbc:sun:sqlserver://<HOST>[:<PORT>]");       
        
        add("DB2 Driver",
        "com.sun.sql.jdbc.db2.DB2Driver",
        "jdbc:sun:db2://<HOST>[:<PORT>];databaseName=<DB>");  
        
        add("Oracle Driver",
        "com.sun.sql.jdbc.oracle.OracleDriver",
        "jdbc:sun:oracle://<HOST>[:<PORT>][;SID=<SID>]");  
        
        add("Sybase Driver",
        "com.sun.sql.jdbc.sybase.SybaseDriver",
        "jdbc:sun:sybase://<HOST>[:<PORT]");          
    }
    
    public static Set<String> getDrivers() {
        TreeSet<String> drivers = new TreeSet<String>();
        for (JdbcUrl url : urls) {
            // A set contains no duplicate elements, so if the same class name 
            // is found twice, that's OK, because it just replaces the entry
            // that was already there
            drivers.add(url.getClassName());
        }
        return drivers;
    }
    
    public static List<JdbcUrl> getJdbcUrls(JDBCDriver driver) {
        ArrayList<JdbcUrl> driverUrls = new ArrayList<JdbcUrl>();
        JdbcUrl newurl = null;
        
        for (JdbcUrl url : urls) {
            if (url.getClassName().equals(driver.getClassName())) {
                if (url.getDriver() == null) {
                    url.setDriver(driver);
                    // Clear out any properties that may be set
                    url.clear();

                    driverUrls.add(url);
                } else {
                    // We already have one driver registered for this class name.
                    // This is a new driver for the same driver class.  That means
                    // it should be on the list with its own entry, but with the
                    // same URL tempate
                    newurl = new JdbcUrl(driver, url.getUrlTemplate(), url.isParseUrl());
                    driverUrls.add(newurl);
                }
            }
        }

        // Have to do this out of the loop or we get a ConcurrentModificationException
        if (newurl != null) {
            add(newurl);
        }

        if (driverUrls.isEmpty()) {
            driverUrls.add(new JdbcUrl(driver));
        }

        return driverUrls;
    }
    
    static List<JdbcUrl> getJdbcUrls() {
        // For unit testing
        return urls;
    }
    
    public static String getName(String driverClass) {
        // Find the first match
        for ( JdbcUrl url : urls) {
            if (url.getClassName().equals(driverClass)) {
                return url.getName();
            }
        }
        
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
