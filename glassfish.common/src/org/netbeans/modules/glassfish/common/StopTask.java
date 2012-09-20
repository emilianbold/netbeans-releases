/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2011 Oracle and/or its affiliates. All rights reserved.
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
import org.glassfish.tools.ide.admin.ResultString;
import org.glassfish.tools.ide.admin.TaskState;
import org.glassfish.tools.ide.server.ServerTasks;
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
    public StopTask(CommonServerSupport support,
            OperationStateListener... stateListener) {
        super(support.getInstance(), stateListener);
        this.support = support;
    }
    
    /**
     * 
     */
    @SuppressWarnings("SleepWhileInLoop")
    @Override
    public OperationState call() {
        // save the current time so that we can deduct that the startup
        // failed due to timeout
        Logger.getLogger("glassfish").log(Level.FINEST,
                "StopTask.call() called on thread \"{0}\"",
                Thread.currentThread().getName()); // NOI18N
        long start = System.currentTimeMillis();
        
        String host; // = null;
        int port;
        
        host = instance.getProperty(GlassfishModule.HOSTNAME_ATTR);
        if(host == null || host.length() == 0) {
            return fireOperationStateChanged(OperationState.FAILED, 
                    "MSG_START_SERVER_FAILED_NOHOST", instanceName); // NOI18N
        }
               
        try {
            port = Integer.valueOf(instance.getProperty(GlassfishModule.ADMINPORT_ATTR));
            if(port < 0 || port > 65535) {
                return fireOperationStateChanged(OperationState.FAILED, 
                        "MSG_START_SERVER_FAILED_BADPORT", instanceName); // NOI18N
            }
        } catch(NumberFormatException ex) {
            return fireOperationStateChanged(OperationState.FAILED, 
                    "MSG_START_SERVER_FAILED_BADPORT", instanceName); // NOI18N
        }

        String target = Util.computeTarget(instance.getProperties());

        if (!Util.isDefaultOrServerTarget(instance.getProperties())) {
            // stop an instance/cluster
            return stopClusterOrInstance(target);
        }

        // stop a domain

        // !PW Can we have a single manager instance per instance, available on
        // demand through lookup?
        // !PW FIXME this uses doubly nested runnables.  Can we fix?
        ResultString result = ServerTasks.stopServer(instance);
        if (TaskState.FAILED.equals(result.getState())) {
             fireOperationStateChanged(OperationState.FAILED, "MSG_STOP_SERVER_FAILED", instanceName);
        }
        
        fireOperationStateChanged(OperationState.RUNNING, 
                "MSG_STOP_SERVER_IN_PROGRESS", instanceName); // NOI18N
        
        // Waiting for server to stop
        while(System.currentTimeMillis() - start < STOP_TIMEOUT) {
            // Send the 'completed' event and return when the server is stopped
            if(!support.isReallyRunning()) {
                try {
                    Thread.sleep(1000); // flush the process
                } catch (InterruptedException e) {
                }
                LogViewMgr.removeServerLogStream(instance);
                LogViewMgr logger = LogViewMgr.getInstance(instance.getProperty(GlassfishModule.URL_ATTR));
                logger.stopReaders();                

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
    
    private OperationState stopClusterOrInstance(String target) {
        ResultString result = ServerTasks.stopCluster(instance, target);

        if (TaskState.FAILED.equals(result.getState())) {
            // if start-cluster not successful, try start-instance
            result = ServerTasks.stopServerInstance(instance, target);
            if (TaskState.FAILED.equals(result.getState())) {
                // if start instance not suscessful fail
                return fireOperationStateChanged(OperationState.FAILED,
                        "MSG_STOP_TARGET_FAILED", instanceName, target); // NOI18N
            }
        }

        return fireOperationStateChanged(OperationState.COMPLETED,
                "MSG_SERVER_STOPPED", instanceName); // NOI18N

    }
}
