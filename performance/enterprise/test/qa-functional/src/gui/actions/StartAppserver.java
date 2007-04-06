/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package gui.actions;



import java.io.PrintStream;
import javax.swing.tree.TreePath;
import org.netbeans.jellytools.RuntimeTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.progress.module.Controller;
import org.netbeans.progress.spi.InternalHandle;
import org.netbeans.progress.spi.TaskModel;

/**
 * Measure application server Startup time via NetBeans TaskModel API.
 *
 * @author rashid@netbeans.org, mkhramov@netbeans.org
 *
 */
public class StartAppserver extends org.netbeans.performance.test.utilities.PerformanceTestCase {
    
   private String  project_name;
   private RuntimeTabOperator rto;
   private Node asNode;
   
   private static PrintStream logger;
   
    /** Creates a new instance of StartAppserver 
     *
     *  @param testName
     * 
     **/
    public StartAppserver(String testName) {
        super(testName);
        logger = this.getLog();
        //TODO: Adjust expectedTime value        
        expectedTime = 45000;
        WAIT_AFTER_OPEN=4000;        
    }
    /** Creates a new instance of StartAppserver 
     *
     *  @param testName
     *  @param performanceDataName
     * 
     **/    
    public StartAppserver(String testName, String  performanceDataName) {
        super(testName);
        logger = this.getLog();
        
        //TODO: Adjust expectedTime value
        expectedTime = 45000;
        WAIT_AFTER_OPEN=4000;
        
    }
    

   
    public void prepare() {
        log(":: prepare");
        rto = RuntimeTabOperator.invoke();
        TreePath path = null;
        
        try {
            path = rto.tree().findPath("Servers|Sun Java System Application Server"); // NOI18N
        } catch (TimeoutExpiredException exc) {
            exc.printStackTrace(System.err);
            throw new Error("Cannot find Application Server Node");
        }
        
        asNode = new Node(rto.tree(),path);
        asNode.select();
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
    
    protected void shutdown() {
        log("::shutdown");
    }
   

  public void close(){
        log("::close");
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
 private static void waitForAppServerTask(String taskName, String serverIDEName) {
        Controller controller = Controller.getDefault();
        TaskModel model = controller.getModel();
     
        InternalHandle task = waitServerTaskHandle(model,taskName+" "+serverIDEName);
        long taskTimestamp = task.getTimeStampStarted();
        
        logger.print("task started at : "+taskTimestamp);
        
        while(1!=0) {
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
    private static InternalHandle waitServerTaskHandle(TaskModel model, String serverIDEName) {
        while(1!=0) {
            InternalHandle[] handles =  model.getHandles();
            InternalHandle  serverTask = getServerTaskHandle(handles,serverIDEName);            
            if(serverTask != null) {
                logger.print("Returning task handle");
                return serverTask; 
            }
            try {
                Thread.sleep(50);
            } catch (InterruptedException exc) {
                exc.printStackTrace(System.err);
            }              
        }
    }
    private static InternalHandle getServerTaskHandle(InternalHandle[] handles, String taskName) {
       if(handles.length == 0)  { 
            logger.print("Empty tasks queue");
           return null; 
       }
       for(int i=0;i<handles.length;i++) {
           if(handles[i].getDisplayName().equals(taskName)) {
               logger.print("Expected task found...");
               return handles[i];
           }
       }
       return null;
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(new StartAppserver("measureTime"));
    }    
    
}