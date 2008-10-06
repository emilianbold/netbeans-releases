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
/*
 * PMResourceTest.java
 *
 * Created on March 14, 2006, 2:01 PM
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
import org.netbeans.modules.j2ee.deployment.impl.ServerInstance;
import org.netbeans.modules.j2ee.deployment.impl.ServerRegistry;
import org.netbeans.modules.j2ee.sun.api.ServerInterface;
import org.netbeans.modules.j2ee.sun.api.SunDeploymentManagerInterface;
import org.netbeans.modules.j2ee.sun.api.restricted.ResourceUtils;
import org.netbeans.modules.j2ee.sun.dd.api.serverresources.Resources;
import org.netbeans.modules.j2ee.sun.ide.sunresources.resourcesloader.SunResourceDataObject;
import org.netbeans.modules.j2ee.sun.ide.sunresources.wizards.ResourceConfigData;
import org.netbeans.modules.j2ee.sun.sunresources.beans.WizardConstants;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Amanpreet Kaur
 */
public class PersistenceResourceMethods extends NbTestCase implements WizardConstants{
    
    private static String CONNECTION_POOL_NAME = "poolTest";
    private static String DATA_RESOURCE_NAME="resourceTest";
    private static String PERSISTENCE_RESOURCE_NAME="persistenceTest";
    
    
    /** Creates a new instance of PersistenceResourcesTest */
    public PersistenceResourceMethods(String testName) {
        super(testName);
    }
    public void registerConnectionPool() {
        try {
            //Project    project = (Project)Util.openProject(new File(Util.WEB_PROJECT_PATH));
            ServerInstance    inst = ServerRegistry.getInstance().getServerInstance(Util._URL);
            ResourceConfigData cpdata = new ResourceConfigData();
            //connection pool setting
            cpdata.setProperties(new Vector());
            cpdata.addProperty(__DerbyPortNumber, "1527");
            cpdata.addProperty(__DerbyDatabaseName, "sample");
            cpdata.addProperty(__ServerName, "localhost");
            cpdata.addProperty(__User, "app");
            cpdata.addProperty(__Password, "APP");
            cpdata.setString(__Name, CONNECTION_POOL_NAME);
            cpdata.setString(__ResType, "javax.sql.DataSource");
            cpdata.setString(__IsXA, "false");
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
            cpdata.setTargetFile("poolTest");
            ResourceUtils.saveConnPoolDatatoXml(cpdata);
            SunResourceDataObject resourceObj = (SunResourceDataObject)SunResourceDataObject.find(falseProject.getFileObject("setup/poolTest.sun-resource"));
            Resources res = Util.getResourcesObject(resourceObj);
            ServerInterface mejb = ((SunDeploymentManagerInterface)inst.getDeploymentManager()).getManagement();
            ResourceUtils.register(res.getJdbcConnectionPool(0), mejb, false);
            resourceObj.delete();
            falseProject.delete();
            //Util.closeProject(Util.WEB_PROJECT_NAME);
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
    public void registerDataResource() {
        try {
            ServerInstance    inst = ServerRegistry.getInstance().getServerInstance(Util._URL);
            //Project    project = (Project)Util.openProject(new File(Util.WEB_PROJECT_PATH));
            ResourceConfigData dsdata = new ResourceConfigData();
            ResourceConfigData cpdata = new ResourceConfigData();
            //dataresource settings
            dsdata.setString(__JndiName,DATA_RESOURCE_NAME);
            dsdata.setString(__Enabled, "true");
            dsdata.setString(__JdbcObjectType, "user");
            dsdata.setString(__PoolName,CONNECTION_POOL_NAME);
            //dsdata.setTargetFileObject(project.getProjectDirectory());
            File fpf = File.createTempFile("falseProject","");
            fpf.delete();
            FileObject falseProject = FileUtil.createFolder(fpf);
            falseProject.createFolder("setup");
            dsdata.setTargetFileObject(falseProject);
            dsdata.setTargetFile("resourceTest");
            ResourceUtils.saveJDBCResourceDatatoXml(dsdata,cpdata);
            SunResourceDataObject resourceObj = (SunResourceDataObject)SunResourceDataObject.find(falseProject.getFileObject("setup/resourceTest.sun-resource"));
            Resources res = Util.getResourcesObject(resourceObj);
            ServerInterface mejb = ((SunDeploymentManagerInterface)inst.getDeploymentManager()).getManagement();
            ResourceUtils.register(res.getJdbcResource(0), mejb, false);
            resourceObj.delete();
            falseProject.delete();
            //Util.closeProject(Util.WEB_PROJECT_NAME);
            Util.sleep(5000);
            String[] dataResources =Util.getResourcesNames("getJdbcResource","jndi-name",mejb);
            for(int i=0;i<dataResources.length;i++) {
                if(dataResources[i].equals(DATA_RESOURCE_NAME))
                    return;
            }
            throw new Exception("Data Resource hasn't been created !");
        } catch(Exception e) {
            fail(e.getMessage());
        }
    }
    
    public void registerPersistenceResource() {
        try {
            ServerInstance    inst = ServerRegistry.getInstance().getServerInstance(Util._URL);
            //Project    project = (Project)Util.openProject(new File(Util.WEB_PROJECT_PATH));
            ResourceConfigData dsdata = new ResourceConfigData();
            ResourceConfigData pmdata = new ResourceConfigData();
            ResourceConfigData cpdata=new ResourceConfigData();
            //persistence resource settings
            pmdata.setString(__JndiName,PERSISTENCE_RESOURCE_NAME);
            pmdata.setString(__Enabled, "true");
            pmdata.setString(__FactoryClass, "com.sun.jdo.spi.persistence.support.sqlstore.impl.PersistenceManagerFactoryImpl");
            pmdata.setString(__JdbcResourceJndiName,DATA_RESOURCE_NAME);
            //pmdata.setTargetFileObject(project.getProjectDirectory());
               File fpf = File.createTempFile("falseProject","");
            fpf.delete();
            FileObject falseProject = FileUtil.createFolder(fpf);
            falseProject.createFolder("setup");
            pmdata.setTargetFileObject(falseProject);
            pmdata.setTargetFile("persistenceTest");
            ResourceUtils.savePMFResourceDatatoXml(pmdata,dsdata,cpdata);
            SunResourceDataObject resourceObj = (SunResourceDataObject)SunResourceDataObject.find(falseProject.getFileObject("setup/persistenceTest.sun-resource"));
            Resources res = Util.getResourcesObject(resourceObj);
            ServerInterface mejb = ((SunDeploymentManagerInterface)inst.getDeploymentManager()).getManagement();
            ResourceUtils.register(res.getPersistenceManagerFactoryResource(0), mejb, false);
            resourceObj.delete();
            falseProject.delete();
            //Util.closeProject(Util.WEB_PROJECT_NAME);
            Util.sleep(5000);
            String[] perResources =Util.getResourcesNames("getPersistenceManagerFactoryResource","jndi-name",mejb);
            for(int i=0;i<perResources.length;i++) {
                if(perResources[i].equals(PERSISTENCE_RESOURCE_NAME))
                    return;
            }
            
            throw new Exception("Persistence Manager Factory Resource hasn't been created !");
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
            Util.sleep(5000);
            String[] connPools = Util.getResourcesNames("getJdbcConnectionPool", "name", mejb);
            for(int i=0;i<connPools.length;i++) {
                if(connPools[i].equals(CONNECTION_POOL_NAME))
                    throw new Exception("Connection Pool hasn't been removed !");
            }
        } catch(Exception e) {
            fail(e.getMessage());
        }
    }
    public void unregisterDataResource() {
        try {
            ServerInstance inst = ServerRegistry.getInstance().getServerInstance(Util._URL);
            ServerInterface mejb = ((SunDeploymentManagerInterface)inst.getDeploymentManager()).getManagement();
            String[] command = new String[] {"delete-jdbc-resource", "--user", "admin",  DATA_RESOURCE_NAME};
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
            Util.sleep(5000);
            String[] dataRes = Util.getResourcesNames("getJdbcResource", "jndi-name", mejb);
            for(int i=0;i<dataRes.length;i++) {
                if(dataRes[i].equals(DATA_RESOURCE_NAME))
                    throw new Exception("Data Resource hasn't been removed !");
            }
        } catch(Exception e) {
            fail(e.getMessage());
        }
    }
    public void unregisterPersistenceResource() {
        try {
            ServerInstance inst = ServerRegistry.getInstance().getServerInstance(Util._URL);
            ServerInterface mejb = ((SunDeploymentManagerInterface)inst.getDeploymentManager()).getManagement();
            String[] command = new String[] {"delete-persistence-resource", "--user", "admin",  PERSISTENCE_RESOURCE_NAME};
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
            Util.sleep(5000);
            String[] perRes = Util.getResourcesNames("getPersistenceManagerFactoryResource", "jndi-name", mejb);
            for(int i=0;i<perRes.length;i++) {
                if(perRes[i].equals(PERSISTENCE_RESOURCE_NAME))
                    throw new Exception("Persistence Resource hasn't been removed !");
            }
        } catch(Exception e) {
            fail(e.getMessage());
        }
    }
//    public static NbTestSuite suite() {
//        NbTestSuite suite = new NbTestSuite("PersistenceResourceMethods");
//        suite.addTest(new AddRemoveSjsasInstanceTest("addSjsasInstance"));
//        suite.addTest(new StartStopServerTest("startServer"));
//        suite.addTest(new PersistenceResourceMethods("registerConnectionPool"));
//        suite.addTest(new PersistenceResourceMethods("registerDataResource"));
//        suite.addTest(new PersistenceResourceMethods("registerPersistenceResource"));
//        suite.addTest(new PersistenceResourceMethods("unregisterPersistenceResource"));
//        suite.addTest(new PersistenceResourceMethods("unregisterDataResource"));
//        suite.addTest(new PersistenceResourceMethods("unregisterConnectionPool"));
//        suite.addTest(new StartStopServerTest("stopServer"));
//        suite.addTest(new AddRemoveSjsasInstanceTest("removeSjsasInstance"));
//        return suite;
//    }
}