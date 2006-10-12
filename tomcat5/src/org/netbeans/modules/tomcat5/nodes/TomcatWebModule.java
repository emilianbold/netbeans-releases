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

package org.netbeans.modules.tomcat5.nodes;

import java.util.Comparator;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.status.ProgressListener;
import javax.enterprise.deploy.spi.status.ProgressObject;
import javax.enterprise.deploy.shared.StateType;
import javax.enterprise.deploy.shared.CommandType;
import org.netbeans.modules.tomcat5.TomcatModule;
import org.netbeans.modules.tomcat5.nodes.actions.TomcatWebModuleCookie;
import org.openide.awt.StatusDisplayer;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.NotifyDescriptor;
import org.openide.DialogDisplayer;
import javax.enterprise.deploy.spi.status.ProgressEvent;
import javax.enterprise.deploy.spi.status.DeploymentStatus;
import org.netbeans.modules.tomcat5.TomcatManager;

/**
 *
 * @author  Petr Pisl
 */
public class TomcatWebModule implements TomcatWebModuleCookie {
 
    private final TomcatModule tomcatModule;
    private final TomcatManager manager;
    
    private boolean isRunning;
    
    private Node node;
    
    private final TargetModuleID[] target;

    
    /** Creates a new instance of TomcatWebModule */
    public TomcatWebModule(DeploymentManager manager, TomcatModule tomcatModule, boolean isRunning) {
        this.tomcatModule = tomcatModule;
        this.manager = (TomcatManager)manager;
        this.isRunning = isRunning;
        target = new TargetModuleID[]{tomcatModule};
    }
    
    public TomcatModule getTomcatModule () {
        return tomcatModule;
    }
    
    public void setRepresentedNode(Node node){
        this.node = node;
    }
    
    public Node getRepresentedNode (){
        return node;
    }
    
    public DeploymentManager getDeploymentManager() {
        return manager;
    }
    
    public void undeploy() {
        RequestProcessor.getDefault().post(new Runnable() {
            public void run () {
                StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(TomcatWebModule.class, "MSG_START_UNDEPLOY",  // NOI18N
                    new Object []{getTomcatModule ().getPath()})); 
                ProgressObject po = manager.undeploy(target);
                po.addProgressListener(new TomcatProgressListener());
            }
        }, 0);                
    }

    public void start() {
        RequestProcessor.getDefault().post(new Runnable() {
            public void run () {
                StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(TomcatWebModule.class, "MSG_START_STARTING",  // NOI18N
                    new Object []{getTomcatModule ().getPath()}));
                ProgressObject po = manager.start(target);
                po.addProgressListener(new TomcatProgressListener());
            }
        }, 0);
    }

    public void stop() {        
        RequestProcessor.getDefault().post(new Runnable() {
            public void run () {                
                StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(TomcatWebModule.class, "MSG_START_STOPPING",  // NOI18N
                    new Object []{getTomcatModule ().getPath()}));
                ProgressObject po = manager.stop(target);
                po.addProgressListener(new TomcatProgressListener());
            }
        }, 0);
    }

    public boolean isRunning() {
        return isRunning;
    }   
    
    
    private String constructDisplayName(){
        if (isRunning())
            return getTomcatModule ().getPath();
        else
            return getTomcatModule ().getPath() + " [" + NbBundle.getMessage(TomcatWebModuleNode.class, "LBL_Stopped")  // NOI18N
               +  "]";
    }            
       
    /**
     * Opens the log file defined for this web moudel in the ouput window.
     */
    public void openLog() {
        manager.logManager().openContextLog(tomcatModule);
    }
    
    /**
     * Returns <code>true</code> if there is a logger defined for this module, 
     * <code>false</code> otherwise.
     *
     * @return <code>true</code> if there is a logger defined for this module, 
     *         <code>false</code> otherwise.
     */
    public boolean hasLogger() {
         return manager.logManager().hasContextLogger(tomcatModule);
    }
    
    private class TomcatProgressListener implements ProgressListener {
        public void handleProgressEvent(ProgressEvent progressEvent) {
            DeploymentStatus deployStatus = progressEvent.getDeploymentStatus();
            if (deployStatus.getState() == StateType.COMPLETED) {
                CommandType command = deployStatus.getCommand();
                if (command == CommandType.START || command == CommandType.STOP) {
                        StatusDisplayer.getDefault().setStatusText(deployStatus.getMessage());
                        if (command == CommandType.START) isRunning = true; else isRunning = false;
                        node.setDisplayName(constructDisplayName());
                } else if (command == CommandType.UNDEPLOY) {
                        Node parent = node.getParentNode();
                        if (parent != null) {
                            Children children = parent.getChildren();
                            if (children instanceof TomcatWebModuleChildren){
                                ((TomcatWebModuleChildren)children).updateKeys();
                                StatusDisplayer.getDefault().setStatusText(deployStatus.getMessage());
                            }
                        }
                }
            } else if (deployStatus.getState() == StateType.FAILED) {
                NotifyDescriptor notDesc = new NotifyDescriptor.Message(
                        deployStatus.getMessage(), 
                        NotifyDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(notDesc);
                StatusDisplayer.getDefault().setStatusText(deployStatus.getMessage());                
            }
        }
    }
    
    public static class TomcatWebModuleComparator implements Comparator {
        
        public int compare(Object o1, Object o2) {
            TomcatWebModule wm1 = (TomcatWebModule) o1;
            TomcatWebModule wm2 = (TomcatWebModule) o2;
            
            return wm1.getTomcatModule ().getModuleID().compareTo(wm2.getTomcatModule ().getModuleID());
        }
        
    }
}
