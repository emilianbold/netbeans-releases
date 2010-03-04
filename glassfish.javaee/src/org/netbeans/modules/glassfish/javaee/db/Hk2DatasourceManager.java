/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008-2010 Sun Microsystems, Inc. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.glassfish.javaee.db;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import org.netbeans.modules.glassfish.javaee.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.common.api.Datasource;
import org.netbeans.modules.j2ee.deployment.common.api.DatasourceAlreadyExistsException;
import org.netbeans.modules.j2ee.deployment.plugins.spi.DatasourceManager;
import org.netbeans.modules.xml.api.EncodingUtil;
import org.netbeans.modules.glassfish.spi.GlassfishModule;
import org.netbeans.modules.glassfish.spi.TreeParser;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import org.netbeans.modules.glassfish.eecommon.api.UrlData;

/**
 *
 * @author Peter Williams
 */
public class Hk2DatasourceManager implements DatasourceManager {
    
    private static final String DOMAIN_XML_PATH = "config/domain.xml";
    
    private Hk2DeploymentManager dm;
    
    public Hk2DatasourceManager(Hk2DeploymentManager dm) {
        this.dm = dm;
    }
    
    /**
     * Retrieves the data sources deployed on the server.
     *
     * @return the set of data sources deployed on the server.
     * @throws ConfigurationException reports problems in retrieving data source
     *         definitions.
     */
    public Set<Datasource> getDatasources() throws ConfigurationException {
        GlassfishModule commonSupport = dm.getCommonServerSupport();
        String domainsDir = commonSupport.getInstanceProperties().get(GlassfishModule.DOMAINS_FOLDER_ATTR);
        String domainName = commonSupport.getInstanceProperties().get(GlassfishModule.DOMAIN_NAME_ATTR);
        // XXX Fix to work with current server domain, not just default domain.
        File domainXml = new File(domainsDir, domainName + File.separatorChar + DOMAIN_XML_PATH);
        // TODO -- need to account for remote domain here?
        return readDatasources(domainXml, "/domain/", null);
    }

    /**
     * Deploys the given set of data sources.
     *
     * @param Set of datasources to deploy.
     * @throws ConfigurationException if there is some problem with data source
     *         configuration.
     * @throws DatasourceAlreadyExistsException if module data source(s) are
     *         conflicting with data source(s) already deployed on the server.
     */
    public void deployDatasources(Set<Datasource> datasources) 
            throws ConfigurationException, DatasourceAlreadyExistsException {
        // since a connection pool is not a Datasource, the deploy has to
        // happen in a different part of the deploy processing...
    }
    

    
    // ------------------------------------------------------------------------
    //  Used by ModuleConfigurationImpl since 
    // ------------------------------------------------------------------------
    public static Set<Datasource> getDatasources(File resourceDir) {
        File resourcesXml = new File(resourceDir, "sun-resources.xml");
        return readDatasources(resourcesXml, "/", resourceDir);
    }
    
//    public Datasource createDataSource(String jndiName, String url, String username,
//            String password, String driver, File resourceDir) throws DatasourceAlreadyExistsException {
//        SunDatasource result = null;
//        try {
//            // Throw an exception if the data source already exists.
//            for(Datasource ds: getDatasources(resourceDir)) {
//                if(jndiName.equals(ds.getJndiName())) {
//                    throw new DatasourceAlreadyExistsException(new SunDatasource(
//                            jndiName, url, username, password, driver));
//                }
//            }
//            
//            if(url != null) {
////                String vendorName = convertToValidName(url);
////                if(vendorName == null) {
////                    vendorName = jndiName;
////                }else{
////                    if(vendorName.equals("derby_embedded")){ //NOI18N
////                        NotifyDescriptor d = new NotifyDescriptor.Message(bundle.getString("Err_UnSupportedDerby"), NotifyDescriptor.WARNING_MESSAGE); // NOI18N
////                        DialogDisplayer.getDefault().notify(d);
////                        return null;
////                    }
////                }
//                if(resourceDir.exists()) {
//                    FileUtil.createFolder(resourceDir);
//                }
//                
//                // Create connection pool if needed.
//                String poolName = createCheckForConnectionPool(vendorName, url, username, password, driver, resourceDir);
//                boolean jdbcExists = requiredResourceExists(jndiName, resourceDir, JDBC_RESOURCE);
//                if (jdbcExists) {
//                    result = null;
//                } else {
//                    createJDBCResource(jndiName, poolName, resourceDir);
//                    result = new SunDatasource(jndiName, url, username, password, driver);
//                }
//            }
//        } catch(IOException ex) {
//            Logger.getLogger("glassfish-javaee").log(Level.WARNING, ex.getLocalizedMessage(), ex);
//        }
//        return result;
//    }    
    
    
    // ------------------------------------------------------------------------
    //  Internal logic
    // ------------------------------------------------------------------------
    private static Set<Datasource> readDatasources(File xmlFile, String xPathPrefix, File resourcesDir) {
        Set<Datasource> dataSources = new HashSet<Datasource>();
        if(xmlFile.exists()) {
            Map<String, JdbcResource> jdbcResourceMap = new HashMap<String, JdbcResource>();
            Map<String, ConnectionPool> connectionPoolMap = new HashMap<String, ConnectionPool>();

            List<TreeParser.Path> pathList = new ArrayList<TreeParser.Path>();
            pathList.add(new TreeParser.Path(xPathPrefix + "resources/jdbc-resource", new JdbcReader(jdbcResourceMap)));
            pathList.add(new TreeParser.Path(xPathPrefix + "resources/jdbc-connection-pool", new ConnectionPoolReader(connectionPoolMap)));

            try {
                TreeParser.readXml(xmlFile, pathList);
            } catch(IllegalStateException ex) {
                Logger.getLogger("glassfish-javaee").log(Level.INFO, ex.getLocalizedMessage(), ex);
            }

            for (JdbcResource jdbc : jdbcResourceMap.values()) {
                ConnectionPool pool = connectionPoolMap.get(jdbc.getPoolName());
                if (pool != null) {
                    try {
                        pool.normalize();

                        // add to sun datasource list
                        String url = pool.getProperty("URL"); //NOI18N
                        if ((url != null) && (!url.equals(""))) { //NOI18N
                            String username = pool.getProperty("User"); //NOI18N
                            String password = pool.getProperty("Password"); //NOI18N
                            String driverClassName = pool.getProperty("driverClass"); //NOI18N
                            dataSources.add(new SunDatasource(jdbc.getJndiName(), url, username,
                                    password, driverClassName, resourcesDir));
                        }
                    } catch (NullPointerException npe) {
                        Logger.getLogger("glassfish-javaee").log(Level.INFO, pool.toString(), npe);
                    }
                }
            }
        }
        return dataSources;
    }
    
    private static class JdbcResource {

        private final String jndiName;
        private final String poolName;
        
        public JdbcResource(String jndiName) {
            this(jndiName, "");
        }
        
        public JdbcResource(String jndiName, String poolName) {
            this.jndiName = jndiName;
            this.poolName = poolName;
        }
        
        public String getJndiName() {
            return jndiName;
        }

        public String getPoolName() {
            return poolName;
        }
    }

    private static class JdbcReader extends TreeParser.NodeReader {

        private final Map<String, JdbcResource> resourceMap;
        
        public JdbcReader(Map<String, JdbcResource> resourceMap) {
            this.resourceMap = resourceMap;
        }
        
        // <jdbc-resource 
        //      enabled="true" 
        //      pool-name="DerbyPool" 
        //      jndi-name="jdbc/__default" 
        //      object-type="user" />
        
        @Override
        public void readAttributes(String qname, Attributes attributes) throws SAXException {
            String type = attributes.getValue("object-type");
            
            // Ignore system resources
            if(type != null && type.startsWith("system-")) {
                return;
            }
            
            String jndiName = attributes.getValue("jndi-name");
            String poolName = attributes.getValue("pool-name");
            if(jndiName != null && jndiName.length() > 0 && 
                    poolName != null && poolName.length() > 0) {
                // add to jdbc resource list
                resourceMap.put(poolName, 
                        new JdbcResource(jndiName, poolName));
            }
        }
    }
    
    private static class ConnectionPool {
        
        private final Map<String, String> properties;
        
        public ConnectionPool(String poolName) {
            this.properties = new HashMap<String, String>();
        }
        
        public void setProperty(String key, String value) {
            properties.put(key, value);
        }
        
        public String getProperty(String key) {
            return properties.get(key);
        }
        
        public void normalize() {
           DbUtil.normalizePoolMap(properties);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Hk2DatasourceManager$ConnectionPool[");
            for (Entry<String,String> e : properties.entrySet()) {
                sb.append(e.getKey());
                sb.append("=");
                sb.append(e.getValue());
                sb.append("--%%--");
            }
            return sb.append("]").toString();
        }
        
    }
    
    private static class ConnectionPoolReader extends TreeParser.NodeReader {
        
        private Map<String, ConnectionPool> resourceMap;
        private ConnectionPool currentPool;
        
        public ConnectionPoolReader(Map<String, ConnectionPool> resourceMap) {
            this.resourceMap = resourceMap;
        }
        
        //<jdbc-connection-pool 
        //        datasource-classname="org.apache.derby.jdbc.ClientDataSource" 
        //        name="DerbyPool" 
        //        res-type="javax.sql.DataSource" 
        //    <property name="PortNumber" value="1527" />
        //    <property name="Password" value="APP" />
        //    <property name="User" value="APP" />
        //    <property name="serverName" value="localhost" />
        //    <property name="DatabaseName" value="sun-appserv-samples" />
        //    <property name="connectionAttributes" value=";create=true" />
        //</jdbc-connection-pool>
    
        @Override
        public void readAttributes(String qname, Attributes attributes) throws SAXException {
            String poolName = attributes.getValue("name");
            if(poolName != null && poolName.length() > 0) {
                currentPool = new ConnectionPool(poolName);
                currentPool.setProperty("dsClassName", attributes.getValue("datasource-classname"));
                currentPool.setProperty("resType", attributes.getValue("res-type"));
                resourceMap.put(poolName, currentPool);
            } else {
                currentPool = null;
            }
        }

        @Override
        public void readChildren(String qname, Attributes attributes) throws SAXException {
            if(currentPool != null) {
                String key = attributes.getValue("name");
                if(key != null && key.length() > 0) {
                    currentPool.setProperty(key, attributes.getValue("value"));
                }
            }
        }
    }    

    /**
     * Create a data source (jdbc-resource and jdbc-connection-pool) and add it
     * to sun-resources.xml in the specified resource folder.
     * 
     * @param jndiName
     * @param url
     * @param username
     * @param password
     * @param driver
     * @param resourceDir
     * @return
     * @throws DatasourceAlreadyExistsException if the required resources already
     *         exist.
     */
    public static Datasource createDataSource(String jndiName, String url, 
            String username, String password, String driver, File resourceDir) 
            throws ConfigurationException, DatasourceAlreadyExistsException {
        SunDatasource ds;
        DuplicateJdbcResourceFinder jdbcFinder = new DuplicateJdbcResourceFinder(jndiName);
        ConnectionPoolFinder cpFinder = new ConnectionPoolFinder();
        
        File xmlFile = new File(resourceDir, "sun-resources.xml");
        if(xmlFile.exists()) {
            List<TreeParser.Path> pathList = new ArrayList<TreeParser.Path>();
            pathList.add(new TreeParser.Path("/resources/jdbc-resource", jdbcFinder));
            pathList.add(new TreeParser.Path("/resources/jdbc-connection-pool", cpFinder));
            
            try {
                TreeParser.readXml(xmlFile, pathList);
                if(jdbcFinder.isDuplicate()) {
                    throw new DatasourceAlreadyExistsException(new SunDatasource(
                            jndiName, url, username, password, driver));
                }
            } catch(IllegalStateException ex) {
                Logger.getLogger("glassfish-javaee").log(Level.INFO, ex.getLocalizedMessage(), ex);
                throw new ConfigurationException(ex.getLocalizedMessage(), ex);
            }
        }

        try {
            String vendorName = VendorNameMgr.vendorNameFromDbUrl(url);
            if(vendorName == null) {
                vendorName = jndiName;
            } else {
                if("derby_embedded".equals(vendorName)) {
                    // !PW FIXME display as dialog warning?
                    Logger.getLogger("glassfish-javaee").log(Level.WARNING, 
                            "Embedded derby not supported as a datasource");
                    return null;
                }
            }

            // Is there a connection pool we can reuse, or do we need to create one?
            String defaultPoolName = computePoolName(url, vendorName, username);
            Map<String, CPool> pools = cpFinder.getPoolData();
            CPool defaultPool = pools.get(defaultPoolName);
            
            String poolName = null;
            if(defaultPool != null && isSameDatabaseConnection(defaultPool, url, username, password)) {
                poolName = defaultPoolName;
            } else {
                for(CPool pool: pools.values()) {
                    if(isSameDatabaseConnection(pool, url, username, password)) {
                        poolName = pool.getPoolName();
                        break;
                    }
                }
            }
            
            if(poolName == null) {
                poolName = defaultPool == null ? defaultPoolName : generateUniqueName(defaultPoolName, pools.keySet());
                createConnectionPool(xmlFile, poolName, url, username, password, driver);
            }
            
            // create jdbc resource
            createJdbcResource(xmlFile, jndiName, poolName);

            ds = new SunDatasource(jndiName, url, username, password, driver, resourceDir);
        } catch(IOException ex) {
            Logger.getLogger("glassfish-javaee").log(Level.INFO, ex.getLocalizedMessage(), ex);
            throw new ConfigurationException(ex.getLocalizedMessage(), ex);
        }
        
        return ds;
    }
    
    private static String generateUniqueName(String prefix, Set<String> keys) {
        for(int i = 1; ; i++) {
            String candidate = prefix + "_" + i; // NOI18N
            if(!keys.contains(candidate)) {
                return candidate;
            }
        }
    }
    
    private static boolean isSameDatabaseConnection(final CPool pool, final String url, 
            final String username, final String password) {
        boolean result = false;
        boolean matchedSettings = false;
        
        UrlData urlData = new UrlData(url);
        if(DbUtil.strEmpty(pool.getUrl())) {
            matchedSettings = DbUtil.strEquivalent(urlData.getHostName(), pool.getHostname()) &&
                    DbUtil.strEquivalent(urlData.getPort(), pool.getPort()) &&
                    DbUtil.strEquivalent(urlData.getDatabaseName(), pool.getDatabaseName()) &&
                    DbUtil.strEquivalent(urlData.getSid(), pool.getSid());
        } else {
            matchedSettings = DbUtil.strEquivalent(url, pool.getUrl());
        }
        
        if(matchedSettings) {
            if(DbUtil.strEquivalent(username, pool.getUsername()) && 
                    DbUtil.strEquivalent(password, pool.getPassword())) {
                result = true;
            }
        }
        
        return result;
    }
    
    private static final String CP_TAG_1 = 
            "    <jdbc-connection-pool " +
            "allow-non-component-callers=\"false\" " +
            "associate-with-thread=\"false\" " +
            "connection-creation-retry-attempts=\"0\" " +
            "connection-creation-retry-interval-in-seconds=\"10\" " +
            "connection-leak-reclaim=\"false\" " +
            "connection-leak-timeout-in-seconds=\"0\" " +
            "connection-validation-method=\"auto-commit\" ";
    
//            "datasource-classname=\"org.postgresql.ds.PGSimpleDataSource\" " +
    private static final String ATTR_DATASOURCE_CLASSNAME = "datasource-classname";
    private static final String CP_TAG_2 = 
            "fail-all-connections=\"false\" " +
            "idle-timeout-in-seconds=\"300\" " +
            "is-connection-validation-required=\"false\" " +
            "is-isolation-level-guaranteed=\"true\" " +
            "lazy-connection-association=\"false\" " +
            "lazy-connection-enlistment=\"false\" " +
            "match-connections=\"false\" " +
            "max-connection-usage-count=\"0\" " +
            "max-pool-size=\"32\" " +
            "max-wait-time-in-millis=\"60000\" ";
//            "name=\"sawhorse-pool\" " +
    private static final String ATTR_POOL_NAME = "name";
    private static final String CP_TAG_3 = 
            "non-transactional-connections=\"false\" " +
            "pool-resize-quantity=\"2\" ";
//            "res-type=\"javax.sql.DataSource\" " +
    private static final String ATTR_RES_TYPE = "res-type";
    private static final String CP_TAG_4 = 
            "statement-timeout-in-seconds=\"-1\" " +
            "steady-pool-size=\"8\" " +
            "validate-atmost-once-period-in-seconds=\"0\" " +
            "wrap-jdbc-objects=\"false\">\n";
    private static final String PROP_SERVER_NAME = "serverName";
    private static final String PROP_PORT_NUMBER = "portNumber";
    private static final String PROP_DATABASE_NAME = "databaseName";
    private static final String PROP_USER = "User";
    private static final String PROP_PASSWORD = "Password";
    private static final String PROP_URL = "URL";
    private static final String PROP_DRIVER_CLASS = "driverClass";
    private static final String CP_TAG_5 = "    </jdbc-connection-pool>\n";
    
    private static final String RESTYPE_DATASOURCE = "javax.sql.DataSource";

    public static void createConnectionPool(File sunResourcesXml, String poolName, 
            String url, String username, String password, String driver) throws IOException {
            
//  <jdbc-connection-pool allow-non-component-callers="false" associate-with-thread="false" connection-creation-retry-attempts="0" connection-creation-retry-interval-in-seconds="10" connection-leak-reclaim="false" connection-leak-timeout-in-seconds="0" connection-validation-method="auto-commit" datasource-classname="org.postgresql.ds.PGSimpleDataSource" fail-all-connections="false" idle-timeout-in-seconds="300" is-connection-validation-required="false" is-isolation-level-guaranteed="true" lazy-connection-association="false" lazy-connection-enlistment="false" match-connections="false" max-connection-usage-count="0" max-pool-size="32" max-wait-time-in-millis="60000" name="sawhorse-pool" non-transactional-connections="false" pool-resize-quantity="2" res-type="javax.sql.DataSource" statement-timeout-in-seconds="-1" steady-pool-size="8" validate-atmost-once-period-in-seconds="0" wrap-jdbc-objects="false">
//    <property name="serverName" value="localhost"/>
//    <property name="portNumber" value="5432"/>
//    <property name="databaseName" value="cookbook2_development"/>
//    <property name="User" value="cookbook2"/>
//    <property name="Password" value="cookbook2"/>
//    <property name="URL" value="jdbc:postgresql://localhost:5432/cookbook2_development"/>
//    <property name="driverClass" value="org.postgresql.Driver"/>
//  </jdbc-connection-pool>

        UrlData urlData = new UrlData(url);

        // Maybe move this logic into UrlData?
        String dsClassName = computeDataSourceClassName(url, driver);
        
        StringBuilder xmlBuilder = new StringBuilder(2000);
        xmlBuilder.append(CP_TAG_1);
        appendAttr(xmlBuilder, ATTR_DATASOURCE_CLASSNAME, dsClassName, false);
        xmlBuilder.append(CP_TAG_2);
        appendAttr(xmlBuilder, ATTR_POOL_NAME, poolName, true);
        xmlBuilder.append(CP_TAG_3);
        appendAttr(xmlBuilder, ATTR_RES_TYPE, RESTYPE_DATASOURCE, true);
        xmlBuilder.append(CP_TAG_4);
        appendProperty(xmlBuilder, PROP_SERVER_NAME, urlData.getHostName(), true);
        appendProperty(xmlBuilder, PROP_PORT_NUMBER, urlData.getPort(), false);
        appendProperty(xmlBuilder, PROP_DATABASE_NAME, urlData.getDatabaseName(), false);
        appendProperty(xmlBuilder, PROP_USER, username, true);
        // blank password is ok so check just null here and pass force=true.
        if(password != null) {
            appendProperty(xmlBuilder, PROP_PASSWORD, password, true);
        }
        appendProperty(xmlBuilder, PROP_URL, url, true);
        appendProperty(xmlBuilder, PROP_DRIVER_CLASS, driver, true);
        xmlBuilder.append(CP_TAG_5);
        
        String xmlFragment = xmlBuilder.toString();
        Logger.getLogger("glassfish-javaee").log(Level.FINER, "New connection pool resource:\n" + xmlFragment);
        appendResource(sunResourcesXml, xmlFragment);
    }
    
    private static String computeDataSourceClassName(String url, String driver) {
        String vendorName = VendorNameMgr.vendorNameFromDbUrl(url);
        String dsClassName = VendorNameMgr.dsClassNameFromVendorName(vendorName);
        
        if(dsClassName == null || dsClassName.length() == 0) {
            dsClassName = DriverMaps.getDSClassName(url);
            if(dsClassName == null || dsClassName.length() == 0) {
                dsClassName = driver;
            } 
        }
        
        return dsClassName;
    }
    
    private static String computePoolName(String url, String vendorName, String username){
        UrlData urlData = new UrlData(url);
        StringBuilder poolName = new StringBuilder(vendorName);
        String dbName = getDatabaseName(urlData);
        if (dbName != null) {
            poolName.append("_" + dbName); //NOI18N
        }
        if (username != null) {
            poolName.append("_" + username); //NOI18N
        }
        poolName.append("Pool"); //NOI18N
        return poolName.toString(); 
    }

    private static String getDatabaseName(UrlData urlData) {
        String databaseName = urlData.getDatabaseName();
        if (databaseName == null) {
            databaseName = urlData.getAlternateDBName();
        }

        return databaseName;
    }
    
    private static final String JDBC_TAG_1 = 
            "    <jdbc-resource " +
            "enabled=\"true\" ";
        //      pool-name="DerbyPool" 
    private static final String ATTR_POOLNAME = "pool-name";
        //      jndi-name="jdbc/__default" 
    private static final String ATTR_JNDINAME = "jndi-name";
    private static final String JDBC_TAG_2 = 
            " object-type=\"user\"/>\n";

    public static void createJdbcResource(File sunResourcesXml, String jndiName, String poolName) throws IOException {
        
        // <jdbc-resource 
        //      enabled="true" 
        //      pool-name="DerbyPool" 
        //      jndi-name="jdbc/__default" 
        //      object-type="user" />
        
        StringBuilder xmlBuilder = new StringBuilder(500);
        xmlBuilder.append(JDBC_TAG_1);
        appendAttr(xmlBuilder, ATTR_POOLNAME, poolName, true);
        appendAttr(xmlBuilder, ATTR_JNDINAME, jndiName, true);
        xmlBuilder.append(JDBC_TAG_2);
        
        String xmlFragment = xmlBuilder.toString();
        Logger.getLogger("glassfish-javaee").log(Level.FINER, "New JDBC resource:\n" + xmlFragment);
        appendResource(sunResourcesXml, xmlFragment);
    }
    
    private static void appendAttr(StringBuilder builder, String name, String value, boolean force) {
        if(force || (name != null && name.length() > 0)) {
            builder.append(name);
            builder.append("=\"");
            builder.append(value);
            builder.append("\" ");
        }
    }
    
    private static void appendProperty(StringBuilder builder, String name, String value, boolean force) {
        if(force || (value != null && value.length() > 0)) {
            builder.append("        <property name=\"");
            builder.append(name);
            builder.append("\" value=\"");
            builder.append(value);
            builder.append("\"/>\n");
        }
    }
    
    private static void appendResource(File sunResourcesXml, String fragment) throws IOException {
        String sunResourcesBuf = readResourceFile(sunResourcesXml);
        sunResourcesBuf = insertFragment(sunResourcesBuf, fragment);
        writeResourceFile(sunResourcesXml, sunResourcesBuf);
    }
    
    private static final String SUN_RESOURCES_XML_HEADER =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
        "<!DOCTYPE resources PUBLIC " + 
            "\"-//Sun Microsystems, Inc.//DTD Application Server 9.0 Resource Definitions //EN\" " + 
            "\"http://www.sun.com/software/appserver/dtds/sun-resources_1_3.dtd\">\n" +
        "<resources>\n";
    private static final String SUN_RESOURCES_XML_FOOTER =
        "</resources>\n";
    
    private static String insertFragment(String sunResourcesBuf, String fragment) throws IOException {
        String header = SUN_RESOURCES_XML_HEADER;
        String footer = SUN_RESOURCES_XML_FOOTER;
        boolean insertNewLine = false;
        
        if(sunResourcesBuf != null) {
            int closeIndex = sunResourcesBuf.indexOf("</resources>");
            if(closeIndex == -1) {
                throw new IOException("Malformed XML");
            }
            header = sunResourcesBuf.substring(0, closeIndex);
            footer = sunResourcesBuf.substring(closeIndex);
            
            if(closeIndex > 0 && sunResourcesBuf.charAt(closeIndex-1) != '\n') {
                insertNewLine = true;
            }
        }
        
        int length = header.length() + footer.length() + 2;
        if(fragment != null) {
            length += fragment.length();
        }
        
        StringBuilder builder = new StringBuilder(length);
        builder.append(header);
        
        if(insertNewLine) {
            String lineSeparator = System.getProperty("line.separator");
            builder.append(lineSeparator != null ? lineSeparator : "\n");
        }
        
        if(fragment != null) {
            builder.append(fragment);
        }
        
        builder.append(footer);
        return builder.toString();
    }

    private static String readResourceFile(File sunResourcesXml) throws IOException {
        String content = null;
        if(sunResourcesXml.exists()) {
            sunResourcesXml = FileUtil.normalizeFile(sunResourcesXml);
            FileObject sunResourcesFO = FileUtil.toFileObject(sunResourcesXml);
            
            if(sunResourcesFO != null) {
                InputStream is = null;
                Reader reader = null;
                try {
                    long flen = sunResourcesFO.getSize();
                    if(flen > 1000000) {
                        throw new IOException(sunResourcesXml.getAbsolutePath() + " is too long to update.");
                    }

                    int length = (int) (2 * flen + 32);
                    char [] buf = new char[length];
                    is = new BufferedInputStream(sunResourcesFO.getInputStream());
                    String encoding = EncodingUtil.detectEncoding(is);
                    reader = new InputStreamReader(is, encoding);
                    int max = reader.read(buf);
                    if(max > 0) {
                        content = new String(buf, 0, max);
                    }
                } finally {
                    if(is != null) {
                        try { is.close(); } catch(IOException ex) { }
                    }
                    if(reader != null) {
                        try { reader.close(); } catch(IOException ex) { }
                    }
                }
            } else {
                throw new IOException("Unable to get FileObject for " + sunResourcesXml.getAbsolutePath());
            }
        }
        return content;
    }
    
    private static void writeResourceFile(final File sunResourcesXml, final String content) throws IOException {
        FileObject parentFolder = FileUtil.createFolder(sunResourcesXml.getParentFile());
        FileSystem fs = parentFolder.getFileSystem();
        writeResourceFile(fs, sunResourcesXml, content);
    }
    
    private static void writeResourceFile(FileSystem fs, final File sunResourcesXml, final String content) throws IOException {
        fs.runAtomicAction(new FileSystem.AtomicAction() {
            @Override
            public void run() throws IOException {
                FileLock lock = null;
                BufferedWriter writer = null;
                try {
                    FileObject sunResourcesFO = FileUtil.createData(sunResourcesXml);
                    lock = sunResourcesFO.lock();
                    writer = new BufferedWriter(new OutputStreamWriter(sunResourcesFO.getOutputStream(lock)));
                    writer.write(content);
                } finally {
                    if(writer != null) {
                        try { writer.close(); } catch(IOException ex) { }
                    }
                    if(lock != null) {
                        lock.releaseLock();
                    }
                }
            }
        });
    }
    
    private static class DuplicateJdbcResourceFinder extends TreeParser.NodeReader {
        
        private final String targetJndiName;
        private boolean duplicate;
        private String poolName;
        
        public DuplicateJdbcResourceFinder(String jndiName) {
            targetJndiName = jndiName;
            duplicate = false;
            poolName = null;
        }
        
        @Override
        public void readAttributes(String qname, Attributes attributes) throws SAXException {
            String jndiName = attributes.getValue("jndi-name");
            if(targetJndiName.equals(jndiName)) {
                if(duplicate) {
                    Logger.getLogger("glassfish-javaee").log(Level.WARNING, 
                            "Duplicate jndi-names defined for JDBC resources.");
                }
                duplicate = true;
                poolName = attributes.getValue("pool-name");
            }
        }
        
        public boolean isDuplicate() {
            return duplicate;
        }
        
        public String getPoolName() {
            return poolName;
        }
        
    }
    
    private static class ConnectionPoolFinder extends TreeParser.NodeReader {
        
        private Map<String, String> properties = null;
        private Map<String, CPool> pools = new HashMap<String, CPool>();
        
        @Override
        public void readAttributes(String qname, Attributes attributes) throws SAXException {
            properties = new HashMap<String, String>();
            
            String poolName = attributes.getValue("name");
            if(poolName != null && poolName.length() > 0) {
                if(!pools.containsKey(poolName)) {
                    properties.put("name", poolName);
                } else {
                    Logger.getLogger("glassfish-javaee").log(Level.WARNING, 
                            "Duplicate pool-names defined for JDBC Connection Pools.");
                }
            }
        }

        @Override
        public void readChildren(String qname, Attributes attributes) throws SAXException {
            properties.put(attributes.getValue("name").toLowerCase(Locale.ENGLISH), 
                    attributes.getValue("value"));
        }
        
        @Override
        public void endNode(String qname) throws SAXException {
            String poolName = properties.get("name");
            CPool pool = new CPool(
                    poolName,
                    properties.get("url"),
                    properties.get("servername"),
                    properties.get("portnumber"),
                    properties.get("databasename"),
                    properties.get("user"),
                    properties.get("password"),
                    properties.get("connectionattributes")
                    );
            pools.put(poolName, pool);
        }
        
        public List<String> getPoolNames() {
            return new ArrayList<String>(pools.keySet());
        }
        
        public Map<String, CPool> getPoolData() {
            return Collections.unmodifiableMap(pools);
        }
        
    }

    private static class CPool {
        
        private final String poolName;
        private final String url;
        private final String hostname;
        private final String port;
        private final String databaseName;
        private final String username;
        private final String password;
        private final String sid;
        
        public CPool(String poolName, String url, String hostName, String port, 
                String databaseName, String username, String password, String sid) {
            this.poolName = poolName;
            this.url = url;
            this.hostname = hostName;
            this.port = port;
            this.databaseName = databaseName;
            this.username = username;
            this.password = password;
            this.sid = sid;
        }
        
        public String getPoolName() {
            return poolName;
        }
        
        public String getUrl() {
            return url;
        }

        public String getDatabaseName() {
            return databaseName;
        }

        public String getHostname() {
            return hostname;
        }

        public String getPassword() {
            return password;
        }

        public String getPort() {
            return port;
        }

        public String getSid() {
            return sid;
        }

        public String getUsername() {
            return username;
        }
        
    }
    
}
