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

import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.j2ee.deployment.impl.ServerInstance;
import org.netbeans.modules.j2ee.deployment.impl.ServerRegistry;
import org.netbeans.modules.j2ee.deployment.impl.ui.ProgressUI;

/**
 *
 * @author Michal Mocnak
 */
public class StartStopServerTest extends NbTestCase {
    
    private final int SLEEP = 10000;
    
    public StartStopServerTest(String testName) {
        super(testName);
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
            
            Util.sleep(SLEEP);
        } catch(Exception e) {
            fail(e.getMessage());
        }
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite("StartStopServerTest");
        suite.addTest(new AddRemoveSjsasInstanceTest("addSjsasInstance"));
        suite.addTest(new StartStopServerTest("startServer"));
        suite.addTest(new StartStopServerTest("restartServer"));
        suite.addTest(new StartStopServerTest("stopServer"));
        suite.addTest(new StartStopServerTest("startDebugServer"));
        suite.addTest(new StartStopServerTest("restartServer"));
        suite.addTest(new StartStopServerTest("stopServer"));
        suite.addTest(new AddRemoveSjsasInstanceTest("removeSjsasInstance"));
        return suite;
    }
}