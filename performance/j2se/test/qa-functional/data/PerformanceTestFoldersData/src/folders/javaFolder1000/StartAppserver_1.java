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

package folders.javaFolder1000;

import javax.swing.tree.TreePath;

import junit.framework.Test;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.RuntimeTabOperator;
import org.netbeans.jellytools.nodes.Node;

import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.jemmy.EventTool;

import org.netbeans.jemmy.operators.Operator;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.progress.module.Controller;
import org.netbeans.progress.spi.InternalHandle;
import org.netbeans.progress.spi.TaskModel;

/**
 * Measure application server Startup time via NetBeans TaskModel API.
 *
 * @author rashid@netbeans.org, mkhramov@netbeans.org, mmirilovic@netbeans.org
 *
 */
public class StartAppserver_1 extends PerformanceTestCase {
    public static final int WAIT_SERVER_TASK_HANDLE_TIMEOUT = 40000;
    public static final int WAIT_FOR_APP_SERVER_TASK = 120000;
    
    private RuntimeTabOperator rto;
    private Node asNode;
    
    /** Creates a new instance of StartAppserver
     *  @param testName
     **/
    public StartAppserver_1(String testName) {
        super(testName);
        expectedTime = 45000; //TODO: Adjust expectedTime value
        WAIT_AFTER_OPEN=4000;
    }
    
    /** Creates a new instance of StartAppserver
     *  @param testName
     *  @param performanceDataName
     **/
    public StartAppserver_1(String testName, String  performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = 45000; //TODO: Adjust expectedTime value
        WAIT_AFTER_OPEN=4000;
    }

    @Override
    protected void initialize() {
        super.initialize();
        log(":: initialize");
        stopServer();
    }

    public void prepare() {
        log(":: prepare");
        obtainNode();
    }
    
    private void obtainNode() {
        rto = RuntimeTabOperator.invoke();
        rto.setComparator(new Operator.DefaultStringComparator(true, true));
        TreePath path = null;
        
        try {
            // "Server"
            String server = Bundle.getStringTrimmed("org.netbeans.modules.server.ui.manager.Bundle", "Server_Registry_Node_Name");
            // "GlassFish V2"
            String glassfishv2 = Bundle.getStringTrimmed("org.netbeans.modules.j2ee.sun.ide.Bundle", "LBL_GlassFishV2");
            path = rto.tree().findPath(server + "|" + glassfishv2); // NOI18N
        } catch (TimeoutExpiredException exc) {
            exc.printStackTrace(System.err);
            throw new Error("Cannot find Application Server Node");
        }
        
        asNode = new Node(rto.tree(),path);
        asNode.select();
        new EventTool().waitNoEvent(5000);
    }
    
    public ComponentOperator open() {
        log("::open");
        String serverIDEName = asNode.getText();
        
        JPopupMenuOperator popup = asNode.callPopup();
        if (popup == null) {
            throw new Error("Cannot get context menu for Application server node ");
        }
        
        boolean startEnabled = popup.showMenuItem("Start").isEnabled(); // NOI18N
        if(startEnabled) {
            popup.pushMenuNoBlock("Start"); // NOI18N
            waitForAppServerTask("Starting", serverIDEName);
        } else {
            fail("Server already started");
        }
        
        return null;
    }
    
    @Override
    public void close(){
        log("::close");
        stopServer();
        new EventTool().waitNoEvent(3000);
    }
    
    private void stopServer() {
        if (asNode == null) {
            obtainNode();
        }
        if (asNode != null) {
            String serverIDEName = asNode.getText();

            JPopupMenuOperator popup = asNode.callPopup();
            if (popup == null) {
                throw new Error("Cannot get context menu for Application server node ");
            }
            boolean stopEnabled = popup.showMenuItem("Stop").isEnabled(); // NOI18N
            if(stopEnabled) {
                popup.pushMenuNoBlock("Stop"); // NOI18N
                waitForAppServerTask("Stopping", serverIDEName);
            }
        }
    }
    
    private void waitForAppServerTask(String taskName, String serverIDEName) {
        Controller controller = Controller.getDefault();
        TaskModel model = controller.getModel();
        
        InternalHandle task = waitServerTaskHandle(model,taskName+" "+serverIDEName);
        long taskTimestamp = task.getTimeStampStarted();
        
        log("task started at : "+taskTimestamp);
        
        long end = System.currentTimeMillis() + WAIT_FOR_APP_SERVER_TASK;
        while(System.currentTimeMillis() < end) {
            int state = task.getState();
            if (state == InternalHandle.STATE_FINISHED) { 
                return; 
            }
            try {                
                Thread.sleep(50);
            } catch (InterruptedException exc) {
                exc.printStackTrace(System.err);
                fail(exc);
            }
        }
        fail("AppServer task wasn't finished during " + WAIT_FOR_APP_SERVER_TASK + " ms");
    }
    
    private InternalHandle waitServerTaskHandle(TaskModel model, String serverIDEName) {
        long end = System.currentTimeMillis() + WAIT_SERVER_TASK_HANDLE_TIMEOUT;
        while(System.currentTimeMillis() < end) {
            InternalHandle[] handles =  model.getHandles();
            InternalHandle  serverTask = getServerTaskHandle(handles,serverIDEName);
            if(serverTask != null) {
                log("Returning task handle");
                return serverTask;
            }
            
            try {
                Thread.sleep(50);
            } catch (InterruptedException exc) {
                exc.printStackTrace(System.err);
                fail(exc);
            }
        }
        fail("No task handle obtained during " + WAIT_SERVER_TASK_HANDLE_TIMEOUT + " ms");
        // Not reachable
        return null;
    }
    
    private InternalHandle getServerTaskHandle(InternalHandle[] handles, String taskName) {
        if(handles.length == 0)  {
//            log("Empty tasks queue");
            return null;
        }
        
        for (InternalHandle internalHandle : handles) {
            if(internalHandle.getDisplayName().equals(taskName)) {
                log("Expected task found...");
                return internalHandle;
            }
        }
        return null;
    }
    
    public static Test suite() {
        return NbModuleSuite.create(
            NbModuleSuite.createConfiguration(StartAppserver.class)
            .addTest("measureTime")
            .enableModules(".*")
            .clusters(".*")
        );    
    }
    
}