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
 * DukeStatefulTest.java
 *
 * Created on June 8, 2006, 11:00 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.j2ee.sun.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import javax.enterprise.deploy.shared.ModuleType;
import javax.enterprise.deploy.spi.TargetModuleID;
import org.netbeans.api.project.Project;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.j2ee.deployment.impl.ServerInstance;
import org.netbeans.modules.j2ee.deployment.impl.ServerRegistry;
import org.netbeans.spi.project.ActionProvider;

/**
 *
 * @author Amanpreet Kaur
 */
public class DukeStatefulTest extends NbTestCase{
    
    /** Creates a new instance of DukeStatefulTest */
    private final int SLEEP = 10000;
    
    public DukeStatefulTest(String testName) {
        super(testName);
    }
    /** calls the deploy api for the duke-stateful project*/
    public void deployApplication() {
        try {
            Util.deployModule(ModuleType.EAR,Util.STATEFUL_PROJECT_PATH, Util.STATEFUL_PROJECT_NAME );
        } catch(Exception e) {
            fail(e.getMessage());
        }
    }
    
    /**disables the application by using asadmin command and then checks for it availability in running modules in appserver domain*/
    public void disableApplication(){
        try{
            ServerInstance si = ServerRegistry.getInstance().getServerInstance(Util._URL);
            String[] command = new String[]{"disable", Util.STATEFUL_PROJECT_NAME};
            Process p=Util.runAsadmin(command);
            Util.sleep(Util.SLEEP);
            BufferedReader error = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            if(error.readLine()!=null)
                throw new Exception(error.readLine());
            BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String output=input.readLine();
            System.out.println(output);
            if (Util.getModuleID(ModuleType.EAR, Util.STATEFUL_PROJECT_NAME, si,true)!=null)
                throw new Exception("Disable of application failed.");
        }catch(Exception e){
            fail(e.getMessage());
        }
    }
    /**enables the application by using asadmin command and then checks for it availability in running modules in appserver domain*/
    public void enableApplication(){
        try{
            ServerInstance si = ServerRegistry.getInstance().getServerInstance(Util._URL);
            String[] command = new String[]{"enable", Util.STATEFUL_PROJECT_NAME};
            Process p=Util.runAsadmin(command);
            Util.sleep(Util.SLEEP);
            BufferedReader error = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            if(error.readLine()!=null)
                throw new Exception(error.readLine());
            BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String output=input.readLine();
            System.out.println(output);
        }catch(Exception e){
            fail(e.getMessage());
        }
    }
    /** undeploys the application and checks for its unavailability in running modules of appserver */
    public void undeployApplication() {
        try {
            ServerInstance si = ServerRegistry.getInstance().getServerInstance(Util._URL);
            TargetModuleID moduleID = Util.getModuleID(ModuleType.EAR, Util.CUSTOMER_APPLICATION_PROJECT_NAME, si,false);
            if(moduleID == null)
                return;
            Util.undeployModule(ModuleType.EAR, Util.STATEFUL_PROJECT_PATH, Util.STATEFUL_PROJECT_NAME, moduleID);
        } catch(Exception e) {
            fail(e.getMessage());
        }
    }
    /**only runs the verifier, no failures reported- need more work*/
    public void verifyApplication() {
        try{
            File f =new File(Util.STATEFUL_PROJECT_PATH + Util._SEP + "verifier_results");
            Project project = (Project)Util.openProject(new File(Util.STATEFUL_PROJECT_PATH));
            ActionProvider ap =(ActionProvider)project.getLookup().lookup(ActionProvider.class);
            OutputStream logger=(OutputStream)new FileOutputStream(f);
           ap.invokeAction("verify", project.getLookup());
            Util.sleep(10*Util.SLEEP);
            Util.closeProject(Util.STATEFUL_PROJECT_NAME);
            Util.sleep(Util.SLEEP);
            
        } catch(Exception e){
            fail(e.getMessage());
        }
        
        
    }
    
    /*runs the application client and checks for correct output*/
    public void runClient(){
        try{
            String[] getClientStubs = new String[]{"get-client-stubs", "--user", "admin","--appname", Util.STATEFUL_PROJECT_NAME, "." };
            Util.runAsadmin(getClientStubs);
            Util.sleep(Util.SLEEP);
            Process p = Runtime.getRuntime().exec(Util._PLATFORM_LOCATION+Util._SEP+"bin"+Util._SEP+"appclient -client "+Util.STATEFUL_PROJECT_NAME+"Client.jar");
            BufferedReader error = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            if(error.readLine()!=null)
                throw new Exception(error.readLine());
            BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String output=input.readLine();
            if (!output.equals("duke"))
                throw new Exception("The client of application giving wrong results");
        }catch(Exception e){
            fail(e.getMessage());
        }
    }
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite("DukeStatefulTest");
        // TODO : Retouche migration
//        suite.addTest(new AddRemoveSjsasInstanceTest("addSjsasInstance"));
//        suite.addTest(new StartStopServerTest("startServer"));
//        suite.addTest(new DukeStatefulTest("deployApplication"));
//        suite.addTest(new DukeStatefulTest("runClient"));
//        //suite.addTest(new DukeStatefulTest("verifyApplication"));
//        suite.addTest(new DukeStatefulTest("disableApplication"));
//        suite.addTest(new DukeStatefulTest("enableApplication"));
//        suite.addTest(new StartStopServerTest("restartServer"));
//        suite.addTest(new DukeStatefulTest("runClient"));
//        suite.addTest(new DukeStatefulTest("undeployApplication"));
//        suite.addTest(new StartStopServerTest("stopServer"));
//        suite.addTest(new AddRemoveSjsasInstanceTest("removeSjsasInstance"));
        return suite;
    }
}

