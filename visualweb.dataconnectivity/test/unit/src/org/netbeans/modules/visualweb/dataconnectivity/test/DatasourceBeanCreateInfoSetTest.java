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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.visualweb.dataconnectivity.test;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Driver;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Logger;
import org.netbeans.api.db.explorer.JDBCDriver;
import org.netbeans.api.project.Project;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.db.explorer.DbDriverManager;
import org.netbeans.modules.visualweb.api.j2ee.common.RequestedJdbcResource;
import org.netbeans.modules.visualweb.dataconnectivity.explorer.RowSetBeanCreateInfoSet;
import org.netbeans.modules.visualweb.dataconnectivity.model.DataSourceInfo;
import org.netbeans.modules.visualweb.dataconnectivity.test.utils.SetupProject;

/**
 *
 * @author JohnBaker
 */
public class DatasourceBeanCreateInfoSetTest extends NbTestCase {

    private static String DRIVER_PROPERTY = "db.driverclass";
    private static String URL_PROPERTY = "db.url";
    private static String USERNAME_PROPERTY = "db.username";
    private static String PASSWORD_PROPERTY = "db.password";
    private static String DBDIR_PROPERTY = "db.dir";
    private static String DBNAME_PROPERTY = "db.name";

    protected static String dblocation;
    private Project project;
    private RequestedJdbcResource jdbcResource;
    private Properties props;
    private Connection conn;
    private String schemaName;
    private DatabaseMetaData metaData;
    private RowSetBeanCreateInfoSet rowSetBeanCreateInfoSet;
    private  JDBCDriver jdbcDriver;


    private static final Logger LOGGER = 
            Logger.getLogger(DatasourceBeanCreateInfoSetTest.class.getName());

    public DatasourceBeanCreateInfoSetTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        project = openProject();
        props = new Properties();
        props.setProperty(DRIVER_PROPERTY, "org.apache.derby.jdbc.EmbeddedDriver");
        props.setProperty(URL_PROPERTY, "jdbc:derby://localhost:1527/travel");
        props.setProperty(USERNAME_PROPERTY, "travel");
        props.setProperty(PASSWORD_PROPERTY, "travel");
        props.setProperty(DBNAME_PROPERTY, "ApacheDerby");

        testCreateJdbcResource();
        testCreateRowSetInfoSet();
        testCreateDataSource();
    }

    
    // TODO add test methods here. The name must begin with 'test'. For example:
    // public void testHello() {}
    public Project openProject() throws IOException {
        project = SetupProject.setup(getWorkDir());
        return project;
    }

    public void testCreateJdbcResource() {
        jdbcResource = new RequestedJdbcResource("jdbc/VIR",
                "org.apache.derby.jdbc.ClientDriver", "jdbc:derby://localhost:1527/vir", "vir",
                "vir");

        assertNotNull("jdbcResource should not be null", jdbcResource);
    }

    @SuppressWarnings("empty-statement")
    public void testDatasourceBeanCreateInfoSet() {
        DatabaseMetaData metaData = null;
        try {

            jdbcDriver = createDummyJDBCDriver(getDataDir());

            // Register driver
            Driver d = new DriverImpl(props.getProperty(URL_PROPERTY));
            DbDriverManager.getDefault().registerDriver(d);
            
            conn = DbDriverManager.getDefault().getConnection(props.getProperty(URL_PROPERTY), props, jdbcDriver);
            System.out.println("driver  = " + jdbcDriver);
            schemaName = "TestSchema";                
           
        } catch (SQLException sqe) {
            sqe.printStackTrace();

        } catch (MalformedURLException mue) {
            mue.printStackTrace();            
        }       
    }

    public void testCreateRowSetInfoSet() {
        // Create infoset for storing a data source
        try {
            metaData = (conn == null) ? null : conn.getMetaData();
            String tableName =
                    ((schemaName == null) || (schemaName.equals(""))) ? "RowSetBeanCreateInfoSet" : schemaName + "." + "RowSetBeanCreateInfoSet";
            rowSetBeanCreateInfoSet = new RowSetBeanCreateInfoSet("TestSchema", metaData);
            assertNotNull("RowSetBeanCreateInfoSet failed creation", rowSetBeanCreateInfoSet);
        } catch (SQLException sqe) {
            sqe.printStackTrace();
        }   
    }

    public void testCreateDataSource() {
        try {
            jdbcDriver = createDummyJDBCDriver(getDataDir());

            // Register driver
            Driver d = new DriverImpl(props.getProperty(URL_PROPERTY));
            DbDriverManager.getDefault().registerDriver(d);
            
            conn = DbDriverManager.getDefault().getConnection(props.getProperty(URL_PROPERTY), props, jdbcDriver);
            System.out.println("driver  = " + jdbcDriver);
            schemaName = "TestSchema";         

            metaData = (conn == null) ? null : conn.getMetaData();
            String tableName =
                    ((schemaName == null) || (schemaName.equals(""))) ? "RowSetBeanCreateInfoSet" : schemaName + "." + "RowSetBeanCreateInfoSet";
            rowSetBeanCreateInfoSet = new RowSetBeanCreateInfoSet("TestSchema", metaData);
            assertNotNull("RowSetBeanCreateInfoSet failed creation", rowSetBeanCreateInfoSet);
            // Create data source
            DataSourceInfo dsi = new DataSourceInfo("TestDataSourceName", jdbcDriver.getName(), props.getProperty(URL_PROPERTY), null, props.getProperty(USERNAME_PROPERTY), props.getProperty(PASSWORD_PROPERTY));
            rowSetBeanCreateInfoSet.setDataSourceInfo(dsi);
            assertNotNull("RowSetBeanCreateInfoSet, data source not created", rowSetBeanCreateInfoSet.getDataSourceInfo(dsi));
        } catch (SQLException sqe) {
            sqe.printStackTrace();
        } catch (MalformedURLException mue) {
            mue.printStackTrace();            
        } 
    }

    private static JDBCDriver createDummyJDBCDriver(File dataDir) throws MalformedURLException {
        URL url = dataDir.toURL();
        return JDBCDriver.create("test_driver", "DbDriverManagerTest DummyDriver", "DummyDriver", new URL[] { url });
    }
 

    protected void tearDown() throws Exception {
        super.tearDown();     
    }

 public static final class DriverImpl implements Driver {
        
        public static final String DEFAULT_URL = "jdbc:DbDriverManagerTest";        
        private String url;
        
        public DriverImpl() {
            this(DEFAULT_URL);
        }
        
        public DriverImpl(String url) {
            this.url = url;
        }
        
        public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
            return new DriverPropertyInfo[0];
        }

        public Connection connect(String url, Properties info) throws SQLException {
            return (Connection)Proxy.newProxyInstance(DriverImpl.class.getClassLoader(), new Class[] { ConnectionEx.class }, new InvocationHandler() {
                public Object invoke(Object proxy, Method m, Object[] args) {
                    String methodName = m.getName();
                    if (methodName.equals("getDriver")) {
                        return DriverImpl.this;
                    } else if (methodName.equals("hashCode")) {
                        Integer i = new Integer(System.identityHashCode(proxy));
                        return i;
                    } else if (methodName.equals("equals")) {
                        return Boolean.valueOf(proxy == args[0]);
                    }
                    return null;
                }
            });
        }

        public boolean acceptsURL(String url) throws SQLException {
            return (this.url.equals(url));
        }

        public boolean jdbcCompliant() {
            return true;
        }

        public int getMinorVersion() {
            return 0;
        }

        public int getMajorVersion() {
            return 0;
        }
    }
    
    private static interface ConnectionEx extends Connection {
        
        public Driver getDriver();
    }
}
