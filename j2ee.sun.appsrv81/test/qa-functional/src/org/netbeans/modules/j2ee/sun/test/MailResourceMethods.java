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
 * MailResourceMethods.java
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
import org.netbeans.modules.j2ee.deployment.impl.ServerInstance;
import org.netbeans.modules.j2ee.deployment.impl.ServerRegistry;
import org.netbeans.modules.j2ee.sun.api.ServerInterface;
import org.netbeans.modules.j2ee.sun.api.SunDeploymentManagerInterface;
import org.netbeans.modules.j2ee.sun.api.restricted.ResourceUtils;
import org.netbeans.modules.j2ee.sun.dd.api.serverresources.Resources;
import org.netbeans.modules.j2ee.sun.ide.sunresources.wizards.ResourceConfigData;
import org.netbeans.modules.j2ee.sun.sunresources.beans.WizardConstants;
import org.netbeans.modules.j2ee.sun.ide.editors.NameValuePair;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
/**
 *
 * @author Amanpreet Kaur
 */
public class MailResourceMethods extends NbTestCase implements WizardConstants{
    
    private static String MAIL_RESOURCE_NAME = "mailResourceTest";
    private static String MAIL_USER= "default";
    private static String MAIL_FROM = "default@sun.com";
    
    /** Creates a new instance of MailResourcesTest */
    public MailResourceMethods(String testName) {
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
//    public static NbTestSuite suite() {
//        NbTestSuite suite = new NbTestSuite("MailResourcesTest");
//        suite.addTest(new AddRemoveSjsasInstanceTest("addSjsasInstance"));
//        suite.addTest(new StartStopServerTest("startServer"));
//        suite.addTest(new MailResourceMethods("registerMailResource"));
//        suite.addTest(new MailResourceMethods("unregisterMailResource"));
//        suite.addTest(new StartStopServerTest("stopServer"));
//        suite.addTest(new AddRemoveSjsasInstanceTest("removeSjsasInstance"));
//        return suite;
//    }
}
