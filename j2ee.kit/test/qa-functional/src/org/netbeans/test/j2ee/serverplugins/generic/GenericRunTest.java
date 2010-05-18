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

package org.netbeans.test.j2ee.serverplugins.generic;

import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.j2ee.deployment.impl.ServerInstance;
import org.netbeans.modules.j2ee.deployment.impl.ServerRegistry;
import org.netbeans.modules.j2ee.deployment.impl.ui.ProgressUI;
import org.netbeans.test.j2ee.serverplugins.api.ConstantsProvider;
import org.netbeans.test.j2ee.serverplugins.api.ServerProvider;
import org.openide.util.NbBundle;

/**
 * Generic run tests
 *
 * @author Michal Mocnak
 */
public class GenericRunTest extends NbTestCase {
    
    private ConstantsProvider cProvider;
    private ServerProvider sProvider;
    
    /**
     * Creates a new instance of GenericInstanceTest
     *
     * @param name name of the test method which has to be performed
     * @param cProvider instance of ConstantsProvider
     * @param sProvider instance of ServerProvider
     */
    public GenericRunTest(String name, ConstantsProvider cProvider, ServerProvider sProvider) {
        super(name);
        
        this.cProvider = cProvider;
        this.sProvider = sProvider;
    }
    
    /**
     * Start server test
     */
    public void startServerTest() {
        try {
            // Get instance from ServerRegistry
            ServerInstance si = ServerRegistry.getInstance().getServerInstance(cProvider.getServerURI());
            
            // Check if the server is running
            if (si.isRunning())
                return;
            
            // Check if the server can be started
            if (!si.canStartServer())
                return;
            
            // Start server
            ProgressUI pui = new ProgressUI(NbBundle.getMessage(GenericInstanceTest.class,
                    "MSG_Starting", cProvider.getServerURI()), false);
            si.setServerState(ServerInstance.STATE_WAITING);
            
            try {
                pui.start();
                si.start(pui);
            } finally {
                pui.finish();
            }
            
            // Check if the server is running
            if (!si.isRunning())
                throw new Exception(NbBundle.getMessage(GenericInstanceTest.class, "MSG_Start_Failed", cProvider.getServerURI()));
        } catch(Exception e) {
            fail(e.getMessage());
        }
    }
    
    /**
     * Start server in debug mode test
     */
    public void startDebugServerTest() {
        try {
            // Get instance from ServerRegistry
            ServerInstance si = ServerRegistry.getInstance().getServerInstance(cProvider.getServerURI());
            
            // Check if the server is running
            if (si.isRunning())
                return;
            
            // Check if the server can be started
            if (!si.canStartServer() || !si.isDebugSupported())
                return;
            
            // Start server
            ProgressUI pui = new ProgressUI(NbBundle.getMessage(GenericInstanceTest.class,
                    "MSG_Starting_Debug", cProvider.getServerURI()), false);
            si.setServerState(ServerInstance.STATE_WAITING);
            
            try {
                pui.start();
                si.startDebug(pui);
            } finally {
                pui.finish();
            }
            
            // Check if the server is running
            if (!si.isRunning())
                throw new Exception(NbBundle.getMessage(GenericInstanceTest.class, "MSG_Start_Debug_Failed", cProvider.getServerURI()));
        } catch(Exception e) {
            fail(e.getMessage());
        }
    }
    
    /**
     * Stop server test
     */
    public void stopServerTest() {
        try {
            // Get instance from ServerRegistry
            ServerInstance si = ServerRegistry.getInstance().getServerInstance(cProvider.getServerURI());
            
            // Check if the server is running
            if (!si.isRunning())
                return;
            
            // Start server
            ProgressUI pui = new ProgressUI(NbBundle.getMessage(GenericInstanceTest.class,
                    "MSG_Stopping", cProvider.getServerURI()), false);
            si.setServerState(ServerInstance.STATE_WAITING);
            
            try {
                pui.start();
                si.stop(pui);
            } finally {
                pui.finish();
            }
            
            // Check if the server is running
            if (si.isRunning())
                throw new Exception(NbBundle.getMessage(GenericInstanceTest.class,
                        "MSG_Stop_Failed", cProvider.getServerURI()));
        } catch(Exception e) {
            fail(e.getMessage());
        }
    }
}
