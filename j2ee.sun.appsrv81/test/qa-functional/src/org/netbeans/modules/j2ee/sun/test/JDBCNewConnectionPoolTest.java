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
 * JDBCNewConnectionPoolTest.java
 *
 * Created on March 21, 2006, 10:35 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.j2ee.sun.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Vector;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.j2ee.deployment.impl.ServerInstance;
import org.netbeans.modules.j2ee.deployment.impl.ServerRegistry;
import org.netbeans.modules.j2ee.sun.api.ServerInterface;
import org.netbeans.modules.j2ee.sun.api.SunDeploymentManagerInterface;
import org.netbeans.modules.j2ee.sun.dd.api.serverresources.Resources;
import org.netbeans.modules.j2ee.sun.ide.sunresources.beans.ResourceUtils;
import org.netbeans.modules.j2ee.sun.ide.sunresources.wizards.ResourceConfigData;
import org.netbeans.modules.j2ee.sun.sunresources.beans.WizardConstants;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Amanpreet Kaur
 */
public class JDBCNewConnectionPoolTest extends NbTestCase implements WizardConstants{
    
    private static String CONNECTION_POOL_NAME = "poolTest";
    
    /** Creates a new instance of JDBCNewConnectionPoolResourcesTest */
    public JDBCNewConnectionPoolTest(String testName) {
        super(testName);
    }
    
    public void registerConnectionPool() {
        try {
            // TODO : retoche migration
            //Project project = (Project)Util.openProject(new File(Util.WEB_PROJECT_PATH));
            ResourceConfigData cpdata = new ResourceConfigData();
            ServerInstance inst = ServerRegistry.getInstance().getServerInstance(Util._URL);
            //connection pool setting
            cpdata.setProperties(new Vector());
            cpdata.addProperty(__DatabaseVendor, "derby_net");
            cpdata.addProperty(__User, "app");
            cpdata.addProperty(__Password,"app");
            cpdata.addProperty(__ServerName,"localhost");
            cpdata.addProperty(__DerbyPortNumber,"1527");
            cpdata.addProperty(__DatabaseName,"sample");
            cpdata.setString(__Name, CONNECTION_POOL_NAME);
            cpdata.setString(__ResType, "javax.sql.DataSource");
            cpdata.setString(__DatasourceClassname, "org.apache.derby.jdbc.ClientDataSource");
            cpdata.setString(__SteadyPoolSize, "8");
            cpdata.setString(__MaxPoolSize, "32");
            cpdata.setString(__MaxWaitTimeInMillis, "60000");
            cpdata.setString(__PoolResizeQuantity, "2");
            cpdata.setString(__IdleTimeoutInSeconds, "300");
            //cpdata.setTargetFileObject(project.getProjectDirectory());
            File fpf = File.createTempFile("falseProject","");
            fpf.delete();
            FileObject falseProject = FileUtil.createFolder(fpf);
            falseProject.createFolder("setup");
            cpdata.setTargetFileObject(falseProject);
            ResourceUtils.saveConnPoolDatatoXml(cpdata);
            File resourceObj = FileUtil.toFile(falseProject.getFileObject("sun-resources.xml"));
            Resources res = ResourceUtils.getResourcesGraph(resourceObj);
            ServerInterface mejb = ((SunDeploymentManagerInterface)inst.getDeploymentManager()).getManagement();
            ResourceUtils.register(res.getJdbcConnectionPool(0), mejb, false);
            resourceObj.delete();
            falseProject.delete();
            ///Util.closeProject(Util.WEB_PROJECT_NAME);
            Util.sleep(5000);
            String[] connPools = Util.getResourcesNames("getJdbcConnectionPool", "name", mejb);
            for(int i=0;i<connPools.length;i++) {
                if(connPools[i].equals(CONNECTION_POOL_NAME))
                    return;
            }
            throw new Exception("Connection Pool hasn't been created !");
        } catch(Exception e) {
            fail(e.getMessage());
        }
    }
    
    public void unregisterConnectionPool() {
        try {
            ServerInstance inst = ServerRegistry.getInstance().getServerInstance(Util._URL);
            ServerInterface mejb = ((SunDeploymentManagerInterface)inst.getDeploymentManager()).getManagement();
            String[] command = new String[] {"delete-jdbc-connection-pool", "--user", "admin", CONNECTION_POOL_NAME};
            Process p=Util.runAsadmin(command);
            Util.sleep(Util.SLEEP);
            BufferedReader error = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            String errorMess = error.readLine();
            BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String output=input.readLine();
            if(errorMess!=null)
                throw new Exception(errorMess+"\n"+output);
            System.out.println(output);
            Util.closeProject(Util.WEB_PROJECT_NAME);
            Util.sleep(10000);
            String[] connPools = Util.getResourcesNames("getJdbcConnectionPool", "name", mejb);
            for(int i=0;i<connPools.length;i++) {
                if(connPools[i].equals(CONNECTION_POOL_NAME))
                    throw new Exception("Connection Pool hasn't been removed !");
            }
        } catch(Exception e) {
            fail(e.getMessage());
        }
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite("JDBCNewConnectionPoolTest");
        suite.addTest(new AddRemoveSjsasInstanceTest("addSjsasInstance"));
        suite.addTest(new StartStopServerTest("startServer"));
        suite.addTest(new JDBCNewConnectionPoolTest("registerConnectionPool"));
        suite.addTest(new JDBCNewConnectionPoolTest("unregisterConnectionPool"));
        suite.addTest(new StartStopServerTest("stopServer"));
        suite.addTest(new AddRemoveSjsasInstanceTest("removeSjsasInstance"));
        return suite;
    }
}
