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
 * JSFModuleMethods.java
 *
 * Created on May 15, 2006, 10:29 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.j2ee.sun.test;


import java.io.BufferedReader;
import javax.enterprise.deploy.shared.ModuleType;
import javax.enterprise.deploy.spi.TargetModuleID;

import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.j2ee.deployment.impl.ServerInstance;
import org.netbeans.modules.j2ee.deployment.impl.ServerRegistry;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.api.project.Project;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.sun.ide.j2ee.VerifierImpl;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;


/**
 *
 * @author Amanpreet
 */
public class JSFModuleMethods extends NbTestCase {
    
    private final int SLEEP = 10000;
    
    public JSFModuleMethods(String testName) {
        super(testName);
    }
    
    public void deployJSFModule() {
        try {
            Util.deployModule(ModuleType.WAR, Util.JSF_PROJECT_PATH, Util.JSF_PROJECT_NAME);
        } catch(Exception e) {
            fail(e.getMessage());
        }
    }
    //checks the http error code for web url of the project
    public void executeJSFModule() {
        try{
            
            int errorcode=Util.executeWebModule(ModuleType.WAR, Util.JSF_PROJECT_NAME);
            if(errorcode!=200)
                throw new Exception("Execution of project failed with errorcode "+errorcode);
        }catch(Exception e){
            fail(e.getMessage());
        }
    }
    public void disableJSFModule(){
        try{
            ServerInstance si = ServerRegistry.getInstance().getServerInstance(Util._URL);
            String[] command = new String[]{"disable", Util.JSF_PROJECT_NAME};
            Process p=Util.runAsadmin(command);
            Util.sleep(Util.SLEEP);
            BufferedReader error = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            String errorMess = error.readLine();
            BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String output=input.readLine();
            if(errorMess!=null)
                throw new Exception(errorMess+"\n"+output);
            System.out.println(output);
            if(Util.getModuleID(ModuleType.WAR, Util.JSF_PROJECT_NAME, si,true)!=null)
                throw new Exception("Disable of application failed.");
        }catch(Exception e){
            fail(e.getMessage());
        }
        
    }
    public void enableJSFModule(){
        try{
            ServerInstance si = ServerRegistry.getInstance().getServerInstance(Util._URL);
            String[] command = new String[]{"enable", Util.JSF_PROJECT_NAME};
            Process p=Util.runAsadmin(command);
            Util.sleep(Util.SLEEP);
            BufferedReader error = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            String errorMess = error.readLine();
            BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String output=input.readLine();
            if(errorMess!=null)
                throw new Exception(errorMess+"\n"+output);
            System.out.println(output);
            executeJSFModule();
        }catch(Exception e){
            fail(e.getMessage());
        }
    }
    public void undeployJSFModule() {
        try {
            ServerInstance si = ServerRegistry.getInstance().getServerInstance(Util._URL);
            TargetModuleID moduleID = Util.getModuleID(ModuleType.WAR, Util.JSF_PROJECT_NAME, si,false);
            
            if(moduleID == null)
                return;
            
            Util.undeployModule(ModuleType.WAR, Util.JSF_PROJECT_PATH, Util.JSF_PROJECT_NAME, moduleID);
        } catch(Exception e) {
            fail(e.getMessage());
        }
    }
    //only runs the verifier, no failures reported- need more work
    public void verifyJSFModule() {
        try{
            File f =new File(Util.JSF_PROJECT_PATH + Util._SEP + "verifier_results");
            Project project = (Project)Util.openProject(new File(Util.JSF_PROJECT_PATH));
            ActionProvider ap=(ActionProvider)project.getLookup().lookup(ActionProvider.class);
             ap.invokeAction("verify", project.getLookup());
            Util.sleep(10*Util.SLEEP);
            Util.closeProject(Util.JSF_PROJECT_NAME);
            Util.sleep(Util.SLEEP);
            
        } catch(Exception e){
            fail(e.getMessage());
        }
        
        
    }
    public void updateModule(){
        try{
            File javaFile = new File(Util.JSF_PROJECT_PATH+Util._SEP+"src"+Util._SEP+"java"+Util._SEP+"org"+Util._SEP+"demo"+Util._SEP+"Customer.java");
            File warFile = new File(Util.JSF_PROJECT_PATH+Util._SEP+"dist"+Util._SEP+Util.JSF_PROJECT_NAME+".war");
            Long initialDate=warFile.lastModified();
            javaFile.setLastModified(new java.util.Date().getTime());
            deployJSFModule();
            if(warFile.lastModified()!= initialDate)
                System.out.println("war updated");
            else
                throw new Exception("war was not updated despite java code being modified");
        }catch(Exception e){
            fail(e.getMessage());
        }
        
    }
//checks the run api
    public void runJSFModule(){
        try{
            Project project = (Project)Util.openProject(new File(Util.JSF_PROJECT_PATH));
            ActionProvider ap = (ActionProvider)project.getLookup().lookup(ActionProvider.class);
            J2eeModuleProvider jmp = (J2eeModuleProvider)project.getLookup().lookup(J2eeModuleProvider.class);
            final ServerInstance si = ServerRegistry.getInstance().getServerInstance(Util._URL);
            
            Runnable startCondition = new Runnable() {
                public void run() {
                    while(!si.isConnected()) {
                        try {
                            Thread.sleep(5000);
                        } catch(Exception e) {}
                    }
                }
            };
            
            Runnable runCondition = new Runnable() {
                public void run() {
                    while(Util.getModuleID(ModuleType.WAR, Util.JSF_PROJECT_NAME, si,true) == null) {
                        try {
                            
                            Thread.sleep(5000);
                        } catch(Exception e) {}
                    }
                }
            };
            
            Task t = RequestProcessor.getDefault().create(startCondition);
            ap.invokeAction(ap.COMMAND_RUN, project.getLookup());
            t.run();
            if(!t.waitFinished(300000))
                throw new Exception("Server start timeout");
            
            Util.sleep(Util.SLEEP);
            
            t = RequestProcessor.getDefault().create(runCondition);
            t.run();
            if(!t.waitFinished(300000))
                throw new Exception("WEB Application execution timeout");
            
            Util. sleep(Util.SLEEP);
            
            Util.closeProject(Util.JSF_PROJECT_NAME);
            
            Util.sleep(Util.SLEEP);
            executeJSFModule();
            
        }catch(Exception e){
            fail(e.getMessage());
        }
        
        
    }
    
//    public static NbTestSuite suite() {
//        NbTestSuite suite = new NbTestSuite("JSFModuleMethods");
//        // TODO : Retouche migration
//        suite.addTest(new AddRemoveSjsasInstanceTest("addSjsasInstance"));
//        suite.addTest(new StartStopServerTest("startServer"));
//        suite.addTest(new JSFModuleMethods("deployJSFModule"));
//        suite.addTest(new JSFModuleMethods("executeJSFModule"));
//        suite.addTest(new JSFModuleMethods("updateModule"));
//        //suite.addTest(new JSFModuleMethods("verifyJSFModule"));
//        suite.addTest(new JSFModuleMethods("disableJSFModule"));
//        suite.addTest(new JSFModuleMethods("enableJSFModule"));
//        suite.addTest(new StartStopServerTest("restartServer"));
//        suite.addTest(new JSFModuleMethods("executeJSFModule"));
//        suite.addTest(new JSFModuleMethods("runJSFModule"));
//        suite.addTest(new JSFModuleMethods("undeployJSFModule"));
//        suite.addTest(new StartStopServerTest("stopServer"));
//        suite.addTest(new AddRemoveSjsasInstanceTest("removeSjsasInstance"));
//        return suite;
//    }
}
