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
 * JDBCResourceNewCPMethods.java
 *
 * Created on March 21, 2006, 4:35 PM
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
import org.netbeans.modules.j2ee.sun.ide.sunresources.wizards.ResourceConfigData;
import org.netbeans.modules.j2ee.sun.sunresources.beans.WizardConstants;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * 
 */
public class JDBCResourceNewCPMethods extends NbTestCase implements WizardConstants{
    
    private static String CONNECTION_POOL_NAME = "poolTest";
    private static String DATA_RESOURCE_NAME="resourceTest";
    
    
    
    /** Creates a new instance of JDBCResourceNewCPMethods */
    public JDBCResourceNewCPMethods(String testName) {
        
        super(testName);
    }
    
    public void registerDataResource() {
        try {
            ServerInstance    inst = ServerRegistry.getInstance().getServerInstance(Util._URL);
            //Project    project = (Project)Util.openProject(new File(Util.WEB_PROJECT_PATH));
            ResourceConfigData dsdata = new ResourceConfigData();
            ResourceConfigData cpdata = new ResourceConfigData();
            //connection pool settings
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
            //datasouce settings
            dsdata.setString(__JndiName,DATA_RESOURCE_NAME);
            dsdata.setString(__Enabled, "true");
            dsdata.setString(__JdbcObjectType, "user");
            dsdata.setString(__PoolName,CONNECTION_POOL_NAME);
//            dsdata.setTargetFileObject(project.getProjectDirectory());
            File fpf = File.createTempFile("falseProject","");
            fpf.delete();
            FileObject falseProject = FileUtil.createFolder(fpf);
            falseProject.createFolder("setup");
            dsdata.setTargetFileObject(falseProject);
            cpdata.setTargetFileObject(falseProject);
            ResourceUtils.saveJDBCResourceDatatoXml(dsdata,cpdata,"sun-resources");
            File resourceObj = FileUtil.toFile(falseProject.getFileObject("sun-resources.xml"));
            Resources res = ResourceUtils.getResourcesGraph(resourceObj);
            ServerInterface mejb = ((SunDeploymentManagerInterface)inst.getDeploymentManager()).getManagement();
            ResourceUtils.register(res.getJdbcConnectionPool(0), mejb, false);
            ResourceUtils.register(res.getJdbcResource(0), mejb, false);
            resourceObj.delete();
            Util.closeProject(Util.WEB_PROJECT_NAME);
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
    public void unregisterDataResource() {
        try {
            ServerInstance inst = ServerRegistry.getInstance().getServerInstance(Util._URL);
            ServerInterface mejb = ((SunDeploymentManagerInterface)inst.getDeploymentManager()).getManagement();
            String[] commandds = new String[] {"delete-jdbc-resource", "--user", "admin",  DATA_RESOURCE_NAME};
            String[] commandcp = new String[] {"delete-jdbc-connection-pool", "--user", "admin", CONNECTION_POOL_NAME};
            Process p=Util.runAsadmin(commandds);
            Util.sleep(Util.SLEEP);
            BufferedReader error = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            String errorMess = error.readLine();
            BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String output=input.readLine();
            if(errorMess!=null)
                throw new Exception(errorMess+"\n"+output);
            System.out.println(output);
           Process pc=Util.runAsadmin(commandcp);
            Util.sleep(Util.SLEEP);
            BufferedReader errorcp = new BufferedReader(new InputStreamReader(pc.getErrorStream()));
            errorMess = error.readLine();
            BufferedReader inputcp = new BufferedReader(new InputStreamReader(pc.getInputStream()));
            String outputcp=inputcp.readLine();
            if(errorMess!=null)
                throw new Exception(errorMess+"\n"+outputcp);
            System.out.println(outputcp);
            Util.closeProject(Util.WEB_PROJECT_NAME);
            Util.sleep(5000);
            String[] dataRes = Util.getResourcesNames("getJdbcResource", "jndi-name", mejb);
            for(int i=0;i<dataRes.length;i++) {
                if(dataRes[i].equals(DATA_RESOURCE_NAME))
                    throw new Exception("Data Resource hasn't been removed !");}
            String[] connPools = Util.getResourcesNames("getJdbcConnectionPool", "name", mejb);
            for(int i=0;i<connPools.length;i++) {
                if(connPools[i].equals(CONNECTION_POOL_NAME))
                    throw new Exception("Connection Pool hasn't been removed !");
            }
        } catch(Exception e) {
            fail(e.getMessage());
        }
    }
//    public static NbTestSuite suite() {
//        NbTestSuite suite = new NbTestSuite("JDBCResourceNewCPMethods");
//        suite.addTest(new AddRemoveSjsasInstanceTest("addSjsasInstance"));
//        suite.addTest(new StartStopServerTest("startServer"));
//        suite.addTest(new JDBCResourceNewCPMethods("registerDataResource"));
//        suite.addTest(new JDBCResourceNewCPMethods("unregisterDataResource"));
//        suite.addTest(new StartStopServerTest("stopServer"));
//        suite.addTest(new AddRemoveSjsasInstanceTest("removeSjsasInstance"));
//        return suite;
//    }
}
