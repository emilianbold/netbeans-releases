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
 * MDBTest.java
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
import org.netbeans.api.project.Project;
import java.io.File;
import java.io.InputStreamReader;
import org.netbeans.modules.j2ee.deployment.common.api.ValidationException;
import org.netbeans.spi.project.ActionProvider;


/**
 *
 * @author Amanpreet Kaur
 */
public class MDBTest extends NbTestCase {
    
    private final int SLEEP = 10000;
    
    public MDBTest(String testName) {
        super(testName);
    }
    
    public void deployMDB() {
        try {
            Util.deployModule(ModuleType.EJB, Util.MDB_PROJECT_PATH, Util.MDB_PROJECT_NAME);
        } catch(Exception e) {
            fail(e.getMessage());
        }
    }
    /**disables the application by using asadmin command and then checks for it availability in running modules in appserver domain*/
    public void disableMDB(){
        try{
            ServerInstance si = ServerRegistry.getInstance().getServerInstance(Util._URL);
            String[] command = new String[]{"disable", Util.MDB_PROJECT_NAME};
            Process p=Util.runAsadmin(command);
            Util.sleep(Util.SLEEP);
            BufferedReader error = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            String errorMess = error.readLine();
            BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String output=input.readLine();
            if(errorMess!=null)
                throw new Exception(errorMess+"\n"+output);
            System.out.println(output);
            if(Util.getModuleID(ModuleType.EJB, Util.MDB_PROJECT_NAME, si,true)!=null)
                throw new Exception("Disable of bean failed.");
        }catch(Exception e){
            fail(e.getMessage());
        }
        
    }
    /**enables the application by using asadmin command and then checks for it availability in running modules in appserver domain*/
    public void enableMDB(){
        try{
            ServerInstance si = ServerRegistry.getInstance().getServerInstance(Util._URL);
            String[] command = new String[]{"enable", Util.MDB_PROJECT_NAME};
            Process p=Util.runAsadmin(command);
            Util.sleep(Util.SLEEP);
            BufferedReader error = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            String errorMess = error.readLine();
            BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String output=input.readLine();
            if(errorMess!=null)
                throw new Exception(errorMess+"\n"+output);
            System.out.println(output);
            if(Util.getModuleID(ModuleType.EJB, Util.MDB_PROJECT_NAME, si,true)==null)
                throw new Exception("Enable of bean failed.");
        }catch(Exception e){
            fail(e.getMessage());
        }
    }
    /** undeploys the application and checks for its unavailability in running modules of appserver */
    public void undeployMDB() {
        try {
            ServerInstance si = ServerRegistry.getInstance().getServerInstance(Util._URL);
            TargetModuleID moduleID = Util.getModuleID(ModuleType.EJB, Util.MDB_PROJECT_NAME, si,false);
            
            if(moduleID == null)
                return;
            
            Util.undeployModule(ModuleType.EJB, Util.MDB_PROJECT_PATH, Util.MDB_PROJECT_NAME, moduleID);
        } catch(Exception e) {
            fail(e.getMessage());
        }
    }
    /** updates the java code and then checks if jar was updated during deployment*/
    public void updateMDB(){
        
        try{
            File javaFile = new File(Util.MDB_PROJECT_PATH+Util._SEP+"src"+Util._SEP+"java"+Util._SEP+"beans"+Util._SEP+"SimpleMessageBean.java");
            File jarFile = new File(Util.MDB_PROJECT_PATH+Util._SEP+"dist"+Util._SEP+Util.MDB_PROJECT_NAME+".jar");
            Long initialDate=jarFile.lastModified();
            javaFile.setLastModified(new java.util.Date().getTime());
            deployMDB();
            if(jarFile.lastModified()!= initialDate)
                System.out.println("jar updated");
            else
                throw new Exception("jar was not updated despite java code being modified");
        }catch(Exception e){
            fail(e.getMessage());
        }
        
    }
    /**only runs the verifier, no failures reported- need more work*/
    public void verifyMDB() {
        try{
            File f =new File(Util.MDB_PROJECT_PATH + Util._SEP + "verifier_results");
            Project project = (Project)Util.openProject(new File(Util.MDB_PROJECT_PATH));
            ActionProvider ap=(ActionProvider)project.getLookup().lookup(ActionProvider.class);
            ap.invokeAction("verify", project.getLookup());
            Util.sleep(10*Util.SLEEP);
            Util.closeProject(Util.MDB_PROJECT_NAME);
            Util.sleep(Util.SLEEP);
        } catch(Exception e){
            fail(e.getMessage());
        }
        
        
    }
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite("MDBTest");
        // TODO : Retouche migration
//        suite.addTest(new AddRemoveSjsasInstanceTest("addSjsasInstance"));
//        suite.addTest(new StartStopServerTest("startServer"));
//        suite.addTest(new MDBTest("deployMDB"));
//        //suite.addTest(new MDBTest("verifyMDB"));
//        suite.addTest(new MDBTest("updateMDB"));
//        suite.addTest(new MDBTest("disableMDB"));
//        suite.addTest(new MDBTest("enableMDB"));
//        suite.addTest(new MDBTest("undeployMDB"));
//        suite.addTest(new StartStopServerTest("stopServer"));
//        suite.addTest(new AddRemoveSjsasInstanceTest("removeSjsasInstance"));
        return suite;
    }
    
}