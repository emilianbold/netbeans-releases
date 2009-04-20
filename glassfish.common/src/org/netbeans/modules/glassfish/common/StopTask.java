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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.glassfish.common;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.glassfish.spi.GlassfishModule;
import org.netbeans.modules.glassfish.spi.GlassfishModule.OperationState;
import org.netbeans.modules.glassfish.spi.OperationStateListener;


/**
 * @author Ludovic Chamenois
 * @author Peter Williams
 */
public class StopTask extends BasicTask<OperationState> {

    private final CommonServerSupport support;

    /**
     * 
     * @param support common support object for the server instance being stopped
     * @param stateListener state monitor to track start progress
     */
    public StopTask(CommonServerSupport support, OperationStateListener... stateListener) {
        super(support.getInstanceProperties(), stateListener);
        this.support = support;
    }
    
    /**
     * 
     */
    public OperationState call() {
        // save the current time so that we can deduct that the startup
        // failed due to timeout
        Logger.getLogger("glassfish").log(Level.FINEST, "StopTask.call() called on thread \"" + Thread.currentThread().getName() + "\""); // NOI18N
        long start = System.currentTimeMillis();
        
        String host = null;
        int port = 0;
        
        host = ip.get(GlassfishModule.HOSTNAME_ATTR);
        if(host == null || host.length() == 0) {
            return fireOperationStateChanged(OperationState.FAILED, 
                    "MSG_START_SERVER_FAILED_NOHOST", instanceName); // NOI18N
        }
               
        try {
            port = Integer.valueOf(ip.get(GlassfishModule.HTTPPORT_ATTR));
            if(port < 0 || port > 65535) {
                return fireOperationStateChanged(OperationState.FAILED, 
                        "MSG_START_SERVER_FAILED_BADPORT", instanceName); // NOI18N
            }
        } catch(NumberFormatException ex) {
            return fireOperationStateChanged(OperationState.FAILED, 
                    "MSG_START_SERVER_FAILED_BADPORT", instanceName); // NOI18N
        }

        // !PW Can we have a single manager instance per instance, available on
        // demand through lookup?
        // !PW FIXME this uses doubly nested runnables.  Can we fix?
        CommandRunner mgr = new CommandRunner(ip, new OperationStateListener() {
            // if the http command is successful, we are not done yet...
            // The server still has to stop. If we signal success to the 'stateListener'
            // for the task, it may be premature.
            public void operationStateChanged(OperationState newState, String message) {
                if (newState == OperationState.FAILED) {
                    fireOperationStateChanged(newState, message, instanceName);
                }
            }
        });
        mgr.stopServer();
        
        fireOperationStateChanged(OperationState.RUNNING, 
                "MSG_STOP_SERVER_IN_PROGRESS", instanceName); // NOI18N
        
        // Waiting for server to stop
        while(System.currentTimeMillis() - start < STOP_TIMEOUT) {
            // Send the 'completed' event and return when the server is stopped
            if(!CommonServerSupport.isRunning(host, port)) {
                support.setEnvironmentProperty(GlassfishModule.DEBUG_PORT, "", true); // NOI18N
                try {
                    Thread.sleep(1000); // flush the process
                } catch (InterruptedException e) {
                }
                return fireOperationStateChanged(OperationState.COMPLETED, 
                        "MSG_SERVER_STOPPED", instanceName); // NOI18N
            }
            
            // Sleep for a little so that we do not make our checks too often
            try {
                Thread.sleep(DELAY);
            } catch (InterruptedException e) {}
            
            fireOperationStateChanged(OperationState.RUNNING, "MSG_STOP_SERVER_IN_PROGRESS", instanceName); // NOI18N
        }
        
        return fireOperationStateChanged(OperationState.FAILED, "MSG_STOP_SERVER_FAILED", instanceName); // NOI18N
    }
    
    
}