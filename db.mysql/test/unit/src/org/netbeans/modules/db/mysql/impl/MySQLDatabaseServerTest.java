/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.db.mysql.impl;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.event.ChangeListener;
import org.netbeans.api.db.explorer.JDBCDriverManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.db.mysql.Database;
import org.netbeans.modules.db.mysql.DatabaseServer;
import org.netbeans.modules.db.mysql.DatabaseUser;
import org.netbeans.modules.db.mysql.util.Utils;
import org.openide.filesystems.Repository;
import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;

/**
 *
 * @author David
 */
public class MySQLDatabaseServerTest extends NbTestCase {
    private String host;
    private String port;
    private String user;
    private String password;
    
    private DatabaseServer server;
    
    public MySQLDatabaseServerTest(String testName) {
        super(testName); 
    }     
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        Lookup.getDefault().lookup(ModuleInfo.class);
        
        // We need to set up netbeans.dirs so that the NBInst URLMapper correctly
        // finds the mysql jar file
        File jarFile = new File(JDBCDriverManager.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        File clusterDir = jarFile.getParentFile().getParentFile();
        System.setProperty("netbeans.dirs", clusterDir.getAbsolutePath());
        
        getProperties();

        server = MySQLDatabaseServer.getDefault();
        server.setUser(user);
        server.setPassword(password);
        server.setHost(host);
        server.setPort(port);
    }
        
    private void getProperties() {
        host = System.getProperty("mysql.host", null);
        port = System.getProperty("mysql.port", null);
        user = System.getProperty("mysql.user", null);
        password = System.getProperty("mysql.password", null);
        
        String message = "\nPlease set the following in nbproject/private/private.properties:\n" +
                "test-unit-sys-prop.mysql.host, test-unit-sys-prop.mysql.port, \n" +
                "test-unitsys-prop.mysql.user, test-unit-sys-prop.mysql.password";
        
        if ( host == null ) {
            fail("mysql.host was not set.  " + message);
        }
        if ( port == null ) {
            fail("mysql.port was not set.  " + message);            
        }
        if ( user == null ) {
            fail("mysql.user was not set.  " + message);
        }
        if ( password == null ) {
            fail("mysql.password was not set. " + message);
        }                        
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test of getDefault method, of class MySQLDatabaseServer.
     */
    public void testGetDefault() {
        DatabaseServer expResult = MySQLDatabaseServer.getDefault();
        DatabaseServer result = MySQLDatabaseServer.getDefault();
        assertEquals(expResult, result);
    }

    /**
     * Test of getHost method, of class MySQLDatabaseServer.
     */
    public void testHost() throws Exception {
        testStringProperty("host", "localhost");
    }
    
    private void testStringProperty(String propName, String defaultValue) throws Exception {
        propName = propName.substring(0, 1).toUpperCase() + 
                propName.substring(1);
        String setter = "set" + propName;
        String getter = "get" + propName;
        
        String value = "Testing " + propName;
        
        Method setMethod = server.getClass().getMethod(setter, String.class);
        Method getMethod = server.getClass().getMethod(getter);
        
        setMethod.invoke(server, value);
        String result = (String)getMethod.invoke(server);
        
        assertEquals(result, value); 
        
        if ( defaultValue != null ) {
            setMethod.invoke(server, (Object)null);
            result = (String)getMethod.invoke(server);
            assertEquals(defaultValue, result);
        }
        
    }
    
    private void testStringProperty(String propName) throws Exception {
        testStringProperty(propName, null);
    }


    /**
     * Test of getPort method, of class MySQLDatabaseServer.
     */
    public void testPort() throws Exception {
        testStringProperty("port", "3306");
    }


    /**
     * Test of getUser method, of class MySQLDatabaseServer.
     */
    public void testUser() throws Exception {
        testStringProperty("user", "root");
    }

    /**
     * Test of getPassword method, of class MySQLDatabaseServer.
     */
    public void testPassword() throws Exception {
        testStringProperty("password", "");
    }

    /**
     * Test of isSavePassword method, of class MySQLDatabaseServer.
     */
    public void testSavePassword() {
        boolean value = ! server.isSavePassword();
        server.setSavePassword(value);
        assert(value == server.isSavePassword());
    }

    /**
     * Test of getAdminPath method, of class MySQLDatabaseServer.
     */
    public void testAdminPath() throws Exception {
        testStringProperty("adminPath");
    }

    /**
     * Test of getStartPath method, of class MySQLDatabaseServer.
     */
    public void testStartPath() throws Exception {
        testStringProperty("startPath");
    }

    /**
     * Test of getStopPath method, of class MySQLDatabaseServer.
     */
    public void testStopPath() throws Exception {
        testStringProperty("stopPath");
    }

    /**
     * Test of getStopArgs method, of class MySQLDatabaseServer.
     */
    public void testStopArgs() throws Exception {
        testStringProperty("stopArgs");
    }

    /**
     * Test of getStartArgs method, of class MySQLDatabaseServer.
     */
    public void testStartArgs() throws Exception {
        testStringProperty("startArgs");
    }

    /**
     * Test of getAdminArgs method, of class MySQLDatabaseServer.
     */
    public void testAdminArgs() throws Exception {
        testStringProperty("adminArgs");
    }


    /**
     * Test of isConnected method, of class MySQLDatabaseServer.
     */
    public void testIsConnected() throws Exception {
        System.out.println(Repository.getDefault().getDefaultFileSystem());
        System.out.println(Arrays.asList(Repository.getDefault().getDefaultFileSystem().getRoot().getFileObject("Databases/JDBCDrivers").getChildren()));
        assertFalse(server.isConnected());
        server.reconnect();
        assertTrue(server.isConnected());
        server.disconnect();
        assertFalse(server.isConnected());
    }

    /**
     * Test of getDisplayName method, of class MySQLDatabaseServer.
     */
    public void testGetDisplayName() throws Exception {
        ResourceBundle bundle = Utils.getBundle();
        String disconnectedString = bundle.getString("LBL_ServerNotConnectedDisplayName");
        String connectedString = bundle.getString("LBL_ServerDisplayName");
        
        disconnectedString = disconnectedString.replace("{0}", this.host + ":" + this.port).replace("{1}", this.user);

        connectedString = connectedString.replace("{0}", this.host + ":" + this.port).replace("{1}", this.user);
        
        server.disconnect();
        assertEquals(disconnectedString, server.getDisplayName());
        
        server.reconnect();
        assertEquals(connectedString, server.getDisplayName());
    }

    /**
     * Test of getShortDescription method, of class MySQLDatabaseServer.
     */
    public void testGetShortDescription() {
        ResourceBundle bundle = Utils.getBundle();
        String description = bundle.getString("LBL_ServerShortDescription");
        description = description.replace("{0}", this.host + ":" + this.port).replace("{1}", this.user);
        assertEquals(description, server.getShortDescription());
    }

    /**
     * Test of getURL method, of class MySQLDatabaseServer.
     */
    public void testGetURL_0args() {
    }

    /**
     * Test of getURL method, of class MySQLDatabaseServer.
     */
    public void testGetURL_String() {
    }

    /**
     * Test of refreshDatabaseList method, of class MySQLDatabaseServer.
     */
    public void testRefreshDatabaseList() throws Exception {
    }

    /**
     * Test of getDatabases method, of class MySQLDatabaseServer.
     */
    public void testGetDatabases() throws Exception {
    }

    /**
     * Test of reconnect method, of class MySQLDatabaseServer.
     */
    public void testReconnect() throws Exception {
    }

    /**
     * Test of disconnect method, of class MySQLDatabaseServer.
     */
    public void testDisconnect() {
    }

    /**
     * Test of reconnectAsync method, of class MySQLDatabaseServer.
     */
    public void testReconnectAsync_0args() {
    }

    /**
     * Test of reconnectAsync method, of class MySQLDatabaseServer.
     */
    public void testReconnectAsync_boolean() {
    }

    /**
     * Test of databaseExists method, of class MySQLDatabaseServer.
     */
    public void testDatabaseExists() throws Exception {
    }

    /**
     * Test of createDatabase method, of class MySQLDatabaseServer.
     */
    public void testCreateDatabase() throws Exception {
    }

    /**
     * Test of dropDatabase method, of class MySQLDatabaseServer.
     */
    public void testDropDatabase() throws Exception {
    }

    /**
     * Test of getUsers method, of class MySQLDatabaseServer.
     */
    public void testGetUsers() throws Exception {
    }

    /**
     * Test of grantFullDatabaseRights method, of class MySQLDatabaseServer.
     */
    public void testGrantFullDatabaseRights() throws Exception {
    }

    /**
     * Test of start method, of class MySQLDatabaseServer.
     */
    public void testStart() throws Exception {
    }

    /**
     * Test of stop method, of class MySQLDatabaseServer.
     */
    public void testStop() throws Exception {
    }

    /**
     * Test of startAdmin method, of class MySQLDatabaseServer.
     */
    public void testStartAdmin() throws Exception {
    }

    /**
     * Test of addChangeListener method, of class MySQLDatabaseServer.
     */
    public void testAddChangeListener() {
    }

    /**
     * Test of removeChangeListener method, of class MySQLDatabaseServer.
     */
    public void testRemoveChangeListener() {
    }

}
