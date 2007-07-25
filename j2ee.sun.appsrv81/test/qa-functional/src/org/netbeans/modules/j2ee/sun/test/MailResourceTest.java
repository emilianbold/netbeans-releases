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
 * MailResourceTest.java
 *
 * Created on March 15, 2006, 1:49 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.j2ee.sun.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
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
import org.netbeans.modules.j2ee.sun.ide.editors.NameValuePair;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
/**
 *
 * @author Amanpreet Kaur
 */
public class MailResourceTest extends NbTestCase implements WizardConstants{
    
    private static String MAIL_RESOURCE_NAME = "mailResourceTest";
    private static String MAIL_USER= "default";
    private static String MAIL_FROM = "default@sun.com";
    
    /** Creates a new instance of MailResourcesTest */
    public MailResourceTest(String testName) {
        super(testName);
    }
    
    public void registerMailResource() {
        try {
            //Project project = (Project)Util.openProject(new File(Util.WEB_PROJECT_PATH));
            ResourceConfigData mrdata = new ResourceConfigData();
            ServerInstance inst = ServerRegistry.getInstance().getServerInstance(Util._URL);
            //Java Mail Resource Setting
            NameValuePair v =new NameValuePair();
            v.setParamName("imqDestination");
            v.setParamValue("dont know");
            v.setParamDescription("test property");
            mrdata.addProperty(v);
            mrdata.setString(__JndiName, MAIL_RESOURCE_NAME);
            mrdata.setString(__StoreProtocol, "imap");
            mrdata.setString(__StoreProtocolClass, "com.sun.mail.imap.IMAPStore");
            mrdata.setString(__TransportProtocol, "smtp");
            mrdata.setString(__TransportProtocolClass, "com.sun.mail.smtp.SMTPTransport");
            mrdata.setString(__Host, "mail-apac.sun.com");
            mrdata.setString(__MailUser, MAIL_USER);
            mrdata.setString(__Debug, "false");
            mrdata.setString(__From,MAIL_FROM);
            mrdata.setString(__Enabled,"true");
            //mrdata.setTargetFileObject(project.getProjectDirectory());
            File fpf = File.createTempFile("falseProject","");
            fpf.delete();
            FileObject falseProject = FileUtil.createFolder(fpf);
            falseProject.createFolder("setup");
            mrdata.setTargetFileObject(falseProject);
            ResourceUtils.saveMailResourceDatatoXml(mrdata);
            File resourceObj = FileUtil.toFile(falseProject.getFileObject("sun-resources.xml"));
            Resources res = ResourceUtils.getResourcesGraph(resourceObj);
            ServerInterface mejb = ((SunDeploymentManagerInterface)inst.getDeploymentManager()).getManagement();
            ResourceUtils.register(res.getMailResource(0), mejb, false);
            resourceObj.delete();
            falseProject.delete();
            //Util.closeProject(Util.WEB_PROJECT_NAME);
            Util.sleep(5000);
            String[] mailResource=Util.getResourcesNames("getMailResource","jndi-name",mejb);
            for(int i=0;i<mailResource.length;i++) {
                if(mailResource[i].equals(MAIL_RESOURCE_NAME))
                    return;
            }
            throw new Exception("Java Mail Resource hasn't been created !");
        } catch(Exception e) {
            fail(e.getMessage());
        }
    }
    public void unregisterMailResource() {
        try {
            ServerInstance inst = ServerRegistry.getInstance().getServerInstance(Util._URL);
            ServerInterface mejb = ((SunDeploymentManagerInterface)inst.getDeploymentManager()).getManagement();
            String[] command = new String[] {"delete-javamail-resource", "--user", "admin", MAIL_RESOURCE_NAME};
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
            String[] mailResource = Util.getResourcesNames("getMailResource", "jndi-name", mejb);
            for(int i=0;i<mailResource.length;i++) {
                if(mailResource[i].equals(MAIL_RESOURCE_NAME))
                    throw new Exception("Java Mail Resource hasn't been removed !");
            }
        } catch(Exception e) {
            fail(e.getMessage());
        }
    }
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite("MailResourcesTest");
        suite.addTest(new AddRemoveSjsasInstanceTest("addSjsasInstance"));
        suite.addTest(new StartStopServerTest("startServer"));
        suite.addTest(new MailResourceTest("registerMailResource"));
        suite.addTest(new MailResourceTest("unregisterMailResource"));
        suite.addTest(new StartStopServerTest("stopServer"));
        suite.addTest(new AddRemoveSjsasInstanceTest("removeSjsasInstance"));
        return suite;
    }
}
