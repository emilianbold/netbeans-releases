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
 * ConnectionFactoryResourceMethods.java
 *
 * Created on March 23, 2006, 3:17 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.j2ee.sun.test;

import java.io.File;
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
 * @author Amanpreet Kaur
 */
public class ConnectionFactoryResourceMethods extends NbTestCase implements WizardConstants{
    private static String JMS_QUEUE_RESOURCE_NAME = "jmsqResourceTest";
    private static String JMS_TOPIC_RESOURCE_NAME = "jmstResourceTest";
    /** Creates a new instance of ConnectionFactoryResourcesTest */
    public ConnectionFactoryResourceMethods(String testName) {
        super(testName);
    }
    
    public void registerJMSQueueResource() {
        try {
            // TODO : retouche migration
            //Project project = (Project)Util.openProject(new File(Util.EJB_PROJECT_PATH));
            ResourceConfigData jmsdata = new ResourceConfigData();
            ServerInstance inst = ServerRegistry.getInstance().getServerInstance(Util._URL);
            //JMS Resource Setting
            jmsdata.setString(__JndiName, JMS_QUEUE_RESOURCE_NAME);
            jmsdata.setString(__ResType, "javax.jms.QueueConnectionFactory");
            jmsdata.setString(__Enabled, "true");
           // TODO :retouche migration
            //jmsdata.setTargetFileObject(project.getProjectDirectory());
            File fpf = File.createTempFile("falseProject","");
            fpf.delete();
            FileObject falseProject = FileUtil.createFolder(fpf);
            falseProject.createFolder("setup");
            jmsdata.setTargetFileObject(falseProject);
            ResourceUtils.saveJMSResourceDatatoXml(jmsdata);
            File resourceObj = FileUtil.toFile(falseProject.getFileObject("sun-resources.xml"));
            Resources res = ResourceUtils.getResourcesGraph(resourceObj);
            ServerInterface mejb = ((SunDeploymentManagerInterface)inst.getDeploymentManager()).getManagement();
            ResourceUtils.register(res.getConnectorConnectionPool(0),mejb,false);
            ResourceUtils.register(res.getConnectorResource(0), mejb, false);
            resourceObj.delete();
            falseProject.delete();
            //Util.closeProject(Util.EJB_PROJECT_NAME);
            Util.sleep(5000);
            String[] jmsResource=Util.getResourcesNames("getConnectorResource","jndi-name",mejb);
            for(int i=0;i<jmsResource.length;i++) {
                if(jmsResource[i].equals(JMS_QUEUE_RESOURCE_NAME))
                    return;
            }
            throw new Exception("JMS Queue Connection Factory Resource hasn't been created !");
        } catch(Exception e) {
            fail(e.getMessage());
        }
    }
    public void registerJMSTopicResource() {
        try {
            // TODO : retouche migration
            //Project project = (Project)Util.openProject(new File(Util.EJB_PROJECT_PATH));
            ResourceConfigData jmsdata = new ResourceConfigData();
            ServerInstance inst = ServerRegistry.getInstance().getServerInstance(Util._URL);
            //JMS Resource Setting
            jmsdata.setString(__JndiName, JMS_TOPIC_RESOURCE_NAME);
            jmsdata.setString(__ResType, "javax.jms.TopicConnectionFactory");
            jmsdata.setString(__Enabled, "true");
           // jmsdata.addProperty("Name",JMS_TOPIC_RESOURCE_NAME);
            // TODO : retouche migration 
            //jmsdata.setTargetFileObject(project.getProjectDirectory());
            File fpf = File.createTempFile("falseProject","");
            fpf.delete();
            FileObject falseProject = FileUtil.createFolder(fpf);
            falseProject.createFolder("setup");
            jmsdata.setTargetFileObject(falseProject);
            ResourceUtils.saveJMSResourceDatatoXml(jmsdata);
            File resourceObj = FileUtil.toFile(falseProject.getFileObject("sun-resources.xml"));
            Resources res = ResourceUtils.getResourcesGraph(resourceObj);
            ServerInterface mejb = ((SunDeploymentManagerInterface)inst.getDeploymentManager()).getManagement();
            ResourceUtils.register(res.getConnectorConnectionPool(0),mejb,false);
            ResourceUtils.register(res.getConnectorResource(0), mejb, false);
            resourceObj.delete();
            falseProject.delete();
            //Util.closeProject(Util.EJB_PROJECT_NAME);
            Util.sleep(5000);
            String[] jmsResource=Util.getResourcesNames("getConnectorResource","jndi-name",mejb);
            for(int i=0;i<jmsResource.length;i++) {
                if(jmsResource[i].equals(JMS_TOPIC_RESOURCE_NAME))
                    return;
            }
            throw new Exception("JMS Topic Connection Factory Resource hasn't been created !");
        } catch(Exception e) {
            fail(e.getMessage());
        }
    }
    public void unregisterJMSQueueResource() {
        try {
            ServerInstance inst = ServerRegistry.getInstance().getServerInstance(Util._URL);
            ServerInterface mejb = ((SunDeploymentManagerInterface)inst.getDeploymentManager()).getManagement();
            String[] command = new String[] {"delete-jms-resource", "--user", "admin", JMS_QUEUE_RESOURCE_NAME};
            Util.runAsadmin(command);
            Util.closeProject(Util.EJB_PROJECT_NAME);
            Util.sleep(10000);
            String[] jmsResource = Util.getResourcesNames("getConnectorResource", "jndi-name", mejb);
            for(int i=0;i<jmsResource.length;i++) {
                if(jmsResource[i].equals(JMS_QUEUE_RESOURCE_NAME))
                    throw new Exception("JMS Queue Connection Factory Resource hasn't been removed !");
            }
        } catch(Exception e) {
            fail(e.getMessage());
        }
    }
    public void unregisterJMSTopicResource() {
        try {
            ServerInstance inst = ServerRegistry.getInstance().getServerInstance(Util._URL);
            ServerInterface mejb = ((SunDeploymentManagerInterface)inst.getDeploymentManager()).getManagement();
            String[] command = new String[] {"delete-jms-resource", "--user", "admin",  JMS_TOPIC_RESOURCE_NAME};
            Util.runAsadmin(command);
            Util.closeProject(Util.EJB_PROJECT_NAME);
            Util.sleep(10000);
            String[] jmsResource = Util.getResourcesNames("getConnectorResource", "jndi-name", mejb);
            for(int i=0;i<jmsResource.length;i++) {
                if(jmsResource[i].equals(JMS_TOPIC_RESOURCE_NAME))
                    throw new Exception("JMS Resource hasn't been removed !");
            }
        } catch(Exception e) {
            fail(e.getMessage());
        }
    }
//    public static NbTestSuite suite() {
//        NbTestSuite suite = new NbTestSuite("JMSConnectionFactoryResourcesTest");
//        suite.addTest(new AddRemoveSjsasInstanceTest("addSjsasInstance"));
//        suite.addTest(new StartStopServerTest("startServer"));
//        suite.addTest(new ConnectionFactoryResourceMethods("registerJMSQueueResource"));
//        suite.addTest(new ConnectionFactoryResourceMethods("registerJMSTopicResource"));
//        suite.addTest(new ConnectionFactoryResourceMethods("unregisterJMSQueueResource"));
//        suite.addTest(new ConnectionFactoryResourceMethods("unregisterJMSTopicResource"));
//        suite.addTest(new StartStopServerTest("stopServer"));
//        suite.addTest(new AddRemoveSjsasInstanceTest("removeSjsasInstance"));
//        return suite;
//    }
}
