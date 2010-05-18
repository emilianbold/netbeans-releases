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
 * MDBMethods.java
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
public class MDBMethods extends NbTestCase {
    
    private final int SLEEP = 10000;
    
    public MDBMethods(String testName) {
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
//    public static NbTestSuite suite() {
//        NbTestSuite suite = new NbTestSuite("MDBMethods");
//        // TODO : Retouche migration
////        suite.addTest(new AddRemoveSjsasInstanceTest("addSjsasInstance"));
////        suite.addTest(new StartStopServerTest("startServer"));
////        suite.addTest(new MDBMethods("deployMDB"));
////        //suite.addTest(new MDBMethods("verifyMDB"));
////        suite.addTest(new MDBMethods("updateMDB"));
////        suite.addTest(new MDBMethods("disableMDB"));
////        suite.addTest(new MDBMethods("enableMDB"));
////        suite.addTest(new MDBMethods("undeployMDB"));
////        suite.addTest(new StartStopServerTest("stopServer"));
////        suite.addTest(new AddRemoveSjsasInstanceTest("removeSjsasInstance"));
//        return suite;
//    }
    
}
