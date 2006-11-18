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
 * JSFModuleTest.java
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
public class JSFModuleTest extends NbTestCase {
    
    private final int SLEEP = 10000;
    
    public JSFModuleTest(String testName) {
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
            if(error.readLine()!=null)
                throw new Exception(error.readLine());
            BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String output=input.readLine();
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
            if(error.readLine()!=null)
                throw new Exception(error.readLine());
            BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String output=input.readLine();
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
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite("JSFModuleTest");
        // TODO : Retouche migration
//        suite.addTest(new AddRemoveSjsasInstanceTest("addSjsasInstance"));
//        suite.addTest(new StartStopServerTest("startServer"));
//        suite.addTest(new JSFModuleTest("deployJSFModule"));
//        suite.addTest(new JSFModuleTest("executeJSFModule"));
//        suite.addTest(new JSFModuleTest("updateModule"));
//        //suite.addTest(new JSFModuleTest("verifyJSFModule"));
//        suite.addTest(new JSFModuleTest("disableJSFModule"));
//        suite.addTest(new JSFModuleTest("enableJSFModule"));
//        suite.addTest(new StartStopServerTest("restartServer"));
//        suite.addTest(new JSFModuleTest("executeJSFModule"));
//        suite.addTest(new JSFModuleTest("runJSFModule"));
//        suite.addTest(new JSFModuleTest("undeployJSFModule"));
//        suite.addTest(new StartStopServerTest("stopServer"));
//        suite.addTest(new AddRemoveSjsasInstanceTest("removeSjsasInstance"));
        return suite;
    }
}
