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

package org.netbeans.modules.j2ee.sun.test;

import junit.framework.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.modules.j2ee.deployment.impl.ServerInstance;
import org.netbeans.modules.j2ee.deployment.impl.ServerRegistry;
import org.netbeans.modules.j2ee.deployment.impl.ui.ProgressUI;

/**
 *
 * @author Michal Mocnak
 */
public class ServerTest extends NbTestCase {
    
    private final int SLEEP = 10000;
    
    public ServerTest(String testName) {
        super(testName);
    }
    
    public void testBogus() {
        
    }
    
    public void startServer() {
        try {
            ServerInstance inst = ServerRegistry.getInstance().getServerInstance(Util._URL);
            
            if(inst.isRunning())
                return;
            
            ProgressUI pui = new ProgressUI("Start Sjsas", true);
            inst.start(pui);
            
            Util.sleep(SLEEP);
            
            if(!inst.isRunning())
                throw new Exception("Sjsas server start failed");
            
            if (inst.isDebuggable(null)) 
                fail("Server started in debug... it should not have done that");
            
            Util.sleep(SLEEP);
        } catch(Exception e) {
            fail(e.getMessage());
        }
    }
    
    public void stopServer() {
        try {
            ServerInstance inst = ServerRegistry.getInstance().getServerInstance(Util._URL);
            
            if(!inst.isRunning())
                return;
            
            ProgressUI pui = new ProgressUI("Stop Sjsas", true);
            inst.stop(pui);
            
            Util.sleep(SLEEP);
            
            if(inst.isRunning())
                throw new Exception("Sjsas server stop failed");
            
            Util.sleep(SLEEP);
        } catch(Exception e) {
            fail(e.getMessage());
        }
    }
    
    public void restartServer() {
        try {
            ServerInstance inst = ServerRegistry.getInstance().getServerInstance(Util._URL);
            
            if(!inst.isRunning())
                return;
            
            ProgressUI pui = new ProgressUI("Restart Sjsas", true);
            inst.restart(pui);
            
            Util.sleep(SLEEP);
            
            if(!inst.isRunning())
                throw new Exception("Sjsas server restart failed");
            
            Util.sleep(SLEEP);
        } catch(Exception e) {
            fail(e.getMessage());
        }
    }
    
    public void startDebugServer() {
        try {
            ServerInstance inst = ServerRegistry.getInstance().getServerInstance(Util._URL);
            
            if(inst.isRunning())
                return;
            
            ProgressUI pui = new ProgressUI("Start Debug Sjsas", true);
            inst.startDebug(pui);
            
            Util.sleep(SLEEP);
            
            if(!inst.isRunning())
                throw new Exception("Sjsas server start debug failed");
            
            if (!inst.isDebuggable(null)) 
                fail("server isn't debuggable...");
            
            Util.sleep(SLEEP);
        } catch(Exception e) {
            fail(e.getMessage());
        }
    }
    
    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(ServerTest.class).
                addTest(AddRemoveSjsasInstanceMethods.class, new String[] {"addSjsasInstance"}).
                addTest(DomainEditorMethods.class, new String [] {"poundOnEditor", "checkProfilerInsertion"}).
                addTest(ServerTest.class, new String[] { "startDebugServer","restartServer","stopServer","startServer","stopServer","startServer"}).
                addTest(AdminObjectResourceMethods.class, new String[] {"registerJMSQueueResource","registerJMSTopicResource","unregisterJMSQueueResource","unregisterJMSTopicResource"}).
                addTest(JDBCResourceMethods.class, new String[] {"registerConnectionPool","registerDataResource","unregisterDataResource","unregisterConnectionPool"}).
                addTest(MailResourceMethods.class, new String[] {"registerMailResource","unregisterMailResource"}).
                addTest(WebModuleMethods.class,new String[]
                    {"openProject",
                     "deployWebModule", "undeployWebModule", "closeProject" }).
                addTest(ServerTest.class, new String[] { "stopServer"}).
                addTest(AddRemoveSjsasInstanceMethods.class, new String[] {"removeSjsasInstance"}).enableModules(".*").clusters(".*"));
//        NbTestSuite suite = new NbTestSuite("ServerTest");
//        suite.addTest(new AddRemoveSjsasInstanceMethods("addSjsasInstance"));
//        // detect 88916 regression
//        suite.addTest(new ServerTest("startDebugServer"));
//        suite.addTest(new ServerTest("restartServer"));
//        suite.addTest(new ServerTest("stopServer"));
//        suite.addTest(new ServerTest("startServer"));
//        suite.addTest(new ServerTest("restartServer"));
//        suite.addTest(new ServerTest("stopServer"));
//        // detect 88608 regression
//        suite.addTest(new ServerTest("startServer"));
//        suite.addTest(new AddRemoveSjsasInstanceMethods("removeSjsasInstance"));
//        return suite;
    }
}