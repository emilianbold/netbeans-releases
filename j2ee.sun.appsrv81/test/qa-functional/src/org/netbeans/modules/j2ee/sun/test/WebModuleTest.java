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

package org.netbeans.modules.j2ee.sun.test;

import java.io.File;
import javax.enterprise.deploy.shared.ModuleType;
import javax.enterprise.deploy.spi.TargetModuleID;
import org.netbeans.api.project.Project;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.j2ee.deployment.impl.ServerInstance;
import org.netbeans.modules.j2ee.deployment.impl.ServerRegistry;

/**
 *
 * @author Michal Mocnak
 */
public class WebModuleTest extends NbTestCase {
    
    private final int SLEEP = 10000;
    static private Project p = null;
    
    public WebModuleTest(String testName) {
        super(testName);
    }
    
    public void deployWebModule() {
        try {
            // touch the index...
            //
//            File f = new File(Util.WEB_PROJECT_PATH+"/web/index.jsp");
//            f.setLastModified((new java.util.Date()).getTime());
            Util.deployModule(ModuleType.WAR, p, Util.WEB_PROJECT_NAME);
        } catch(Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
    
    public void undeployWebModule() {
        try {
            ServerInstance si = ServerRegistry.getInstance().getServerInstance(Util._URL);
            TargetModuleID moduleID = Util.getModuleID(ModuleType.WAR, Util.WEB_PROJECT_NAME, si,false);
            
            if(moduleID == null)
                fail("isn't the web module supposed to be here???");
            
            Util.undeployModule(ModuleType.WAR, Util.WEB_PROJECT_PATH, Util.WEB_PROJECT_NAME, moduleID);
        } catch(Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
    
    public void openProject() {
        p = (Project) Util.openProject(new java.io.File(Util.WEB_PROJECT_PATH));        
    }
    
    public void closeProject() {
        Util.closeProject(Util.WEB_PROJECT_NAME);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite("WebModuleTest");
        suite.addTest(new AddRemoveSjsasInstanceTest("addSjsasInstance"));
        suite.addTest(new WebModuleTest("openProject"));
        
        // deploy, then redeploy 19 time
        suite.addTest(new WebModuleTest("deployWebModule"));
        suite.addTest(new WebModuleTest("deployWebModule"));
        suite.addTest(new WebModuleTest("deployWebModule"));
        suite.addTest(new WebModuleTest("deployWebModule"));
        suite.addTest(new WebModuleTest("deployWebModule"));
        suite.addTest(new WebModuleTest("deployWebModule"));
        suite.addTest(new WebModuleTest("deployWebModule"));
        suite.addTest(new WebModuleTest("deployWebModule"));
        suite.addTest(new WebModuleTest("deployWebModule"));
        suite.addTest(new WebModuleTest("deployWebModule"));
        suite.addTest(new WebModuleTest("deployWebModule"));
        suite.addTest(new WebModuleTest("deployWebModule"));
        suite.addTest(new WebModuleTest("deployWebModule"));
        suite.addTest(new WebModuleTest("deployWebModule"));
        suite.addTest(new WebModuleTest("deployWebModule"));
        suite.addTest(new WebModuleTest("deployWebModule"));
        suite.addTest(new WebModuleTest("deployWebModule"));
        suite.addTest(new WebModuleTest("deployWebModule"));
        suite.addTest(new WebModuleTest("deployWebModule"));
        suite.addTest(new WebModuleTest("deployWebModule"));
        suite.addTest(new WebModuleTest("undeployWebModule"));
        
        // deploy+undeploy 20 times
        suite.addTest(new WebModuleTest("deployWebModule"));
        suite.addTest(new WebModuleTest("undeployWebModule"));
        suite.addTest(new WebModuleTest("deployWebModule"));
        suite.addTest(new WebModuleTest("undeployWebModule"));
        suite.addTest(new WebModuleTest("deployWebModule"));
        suite.addTest(new WebModuleTest("undeployWebModule"));
        suite.addTest(new WebModuleTest("deployWebModule"));
        suite.addTest(new WebModuleTest("undeployWebModule"));
        suite.addTest(new WebModuleTest("deployWebModule"));
        suite.addTest(new WebModuleTest("undeployWebModule"));
        suite.addTest(new WebModuleTest("deployWebModule"));
        suite.addTest(new WebModuleTest("undeployWebModule"));
        suite.addTest(new WebModuleTest("deployWebModule"));
        suite.addTest(new WebModuleTest("undeployWebModule"));
        suite.addTest(new WebModuleTest("deployWebModule"));
        suite.addTest(new WebModuleTest("undeployWebModule"));
        suite.addTest(new WebModuleTest("deployWebModule"));
        suite.addTest(new WebModuleTest("undeployWebModule"));
        suite.addTest(new WebModuleTest("deployWebModule"));
        suite.addTest(new WebModuleTest("undeployWebModule"));
        suite.addTest(new WebModuleTest("deployWebModule"));
        suite.addTest(new WebModuleTest("undeployWebModule"));
        suite.addTest(new WebModuleTest("deployWebModule"));
        suite.addTest(new WebModuleTest("undeployWebModule"));
        suite.addTest(new WebModuleTest("deployWebModule"));
        suite.addTest(new WebModuleTest("undeployWebModule"));
        suite.addTest(new WebModuleTest("deployWebModule"));
        suite.addTest(new WebModuleTest("undeployWebModule"));
        suite.addTest(new WebModuleTest("deployWebModule"));
        suite.addTest(new WebModuleTest("undeployWebModule"));
        suite.addTest(new WebModuleTest("deployWebModule"));
        suite.addTest(new WebModuleTest("undeployWebModule"));
        suite.addTest(new WebModuleTest("deployWebModule"));
        suite.addTest(new WebModuleTest("undeployWebModule"));
        suite.addTest(new WebModuleTest("deployWebModule"));
        suite.addTest(new WebModuleTest("undeployWebModule"));
        suite.addTest(new WebModuleTest("deployWebModule"));
        suite.addTest(new WebModuleTest("undeployWebModule"));
        suite.addTest(new WebModuleTest("deployWebModule"));
        suite.addTest(new WebModuleTest("undeployWebModule"));
        suite.addTest(new WebModuleTest("closeProject"));
        suite.addTest(new StartStopServerTest("stopServer"));
        suite.addTest(new AddRemoveSjsasInstanceTest("removeSjsasInstance"));
        return suite;
    }
}