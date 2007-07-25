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
 * ConnectionFactoryResourceTest.java
 *
 * Created on March 23, 2006, 3:17 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.j2ee.sun.test;

import java.io.File;
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
public class ConnectionFactoryResourceTest extends NbTestCase implements WizardConstants{
    private static String JMS_QUEUE_RESOURCE_NAME = "jmsqResourceTest";
    private static String JMS_TOPIC_RESOURCE_NAME = "jmstResourceTest";
    /** Creates a new instance of ConnectionFactoryResourcesTest */
    public ConnectionFactoryResourceTest(String testName) {
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
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite("JMSConnectionFactoryResourcesTest");
        suite.addTest(new AddRemoveSjsasInstanceTest("addSjsasInstance"));
        suite.addTest(new StartStopServerTest("startServer"));
        suite.addTest(new ConnectionFactoryResourceTest("registerJMSQueueResource"));
        suite.addTest(new ConnectionFactoryResourceTest("registerJMSTopicResource"));
        suite.addTest(new ConnectionFactoryResourceTest("unregisterJMSQueueResource"));
        suite.addTest(new ConnectionFactoryResourceTest("unregisterJMSTopicResource"));
        suite.addTest(new StartStopServerTest("stopServer"));
        suite.addTest(new AddRemoveSjsasInstanceTest("removeSjsasInstance"));
        return suite;
    }
}
