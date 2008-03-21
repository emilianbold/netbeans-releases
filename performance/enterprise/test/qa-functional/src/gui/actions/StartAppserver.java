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

package gui.actions;

import javax.swing.tree.TreePath;

import org.netbeans.jellytools.RuntimeTabOperator;
import org.netbeans.jellytools.nodes.Node;

import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.jemmy.EventTool;

import org.netbeans.progress.module.Controller;
import org.netbeans.progress.spi.InternalHandle;
import org.netbeans.progress.spi.TaskModel;

/**
 * Measure application server Startup time via NetBeans TaskModel API.
 *
 * @author rashid@netbeans.org, mkhramov@netbeans.org, mmirilovic@netbeans.org
 *
 */
public class StartAppserver extends org.netbeans.performance.test.utilities.PerformanceTestCase {
    
    private RuntimeTabOperator rto;
    private Node asNode;
    
    /** Creates a new instance of StartAppserver
     *  @param testName
     **/
    public StartAppserver(String testName) {
        super(testName);
        expectedTime = 45000; //TODO: Adjust expectedTime value
        WAIT_AFTER_OPEN=4000;
    }
    
    /** Creates a new instance of StartAppserver
     *  @param testName
     *  @param performanceDataName
     **/
    public StartAppserver(String testName, String  performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = 45000; //TODO: Adjust expectedTime value
        WAIT_AFTER_OPEN=4000;
    }
    
    public void prepare() {
        log(":: prepare");
        rto = RuntimeTabOperator.invoke();
        TreePath path = null;
        
        try {
            path = rto.tree().findPath("Servers|GlassFish V2"); // NOI18N
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
        }
        
        waitForAppServerTask("Starting", serverIDEName);
        return null;
    }
    
    public void close(){
        log("::close");
        if (asNode != null) {
            String serverIDEName = asNode.getText();

            JPopupMenuOperator popup = asNode.callPopup();
            if (popup == null) {
                throw new Error("Cannot get context menu for Application server node ");
            }
            boolean startEnabled = popup.showMenuItem("Stop").isEnabled(); // NOI18N
            if(startEnabled) {
                popup.pushMenuNoBlock("Stop"); // NOI18N
            }
            waitForAppServerTask("Stopping", serverIDEName);
        }
    }
    
    private void waitForAppServerTask(String taskName, String serverIDEName) {
        Controller controller = Controller.getDefault();
        TaskModel model = controller.getModel();
        
        InternalHandle task = waitServerTaskHandle(model,taskName+" "+serverIDEName);
        long taskTimestamp = task.getTimeStampStarted();
        
        log("task started at : "+taskTimestamp);
        
        while(true) {
            int state = task.getState();
            if(state == task.STATE_FINISHED) { return; }
            try {
                Thread.sleep(50);
            } catch (InterruptedException exc) {
                exc.printStackTrace(System.err);
                return;
            }
        }
    }
    
    private InternalHandle waitServerTaskHandle(TaskModel model, String serverIDEName) {
        while(true) {
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
            }
        }
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
    
    public static void main(java.lang.String[] args) {
        repeat = 2;
        junit.textui.TestRunner.run(new StartAppserver("measureTime"));
    }
    
}