/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.tomcat5.nodes;

import java.io.IOException;
import java.util.Comparator;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.status.ProgressListener;
import javax.enterprise.deploy.spi.status.ProgressObject;
import org.netbeans.modules.tomcat5.TomcatModule;
import org.netbeans.modules.tomcat5.nodes.actions.TomcatWebModuleCookie;
import org.openide.awt.StatusDisplayer;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author  Petr Pisl
 */
public class TomcatWebModule extends TomcatModule implements TomcatWebModuleCookie{
    
    private DeploymentManager manager;
    
    private boolean isRunning;
    
    private Node node;
    
    private TargetModuleID[] target;
    /** Creates a new instance of TomcatWebModule */
    public TomcatWebModule(DeploymentManager manager, TomcatModule targetModule, boolean isRunning) {
        super(targetModule.getTarget(), targetModule.getPath());
        this.manager = manager;
        this.isRunning = isRunning;
        target = new TargetModuleID[]{(TargetModuleID)this} ;
    }
    
    
    public void setRepresentedNode(Node node){
        this.node = node;
    }
    
    public Node getRepresentedNode (){
        return node;
    }
    
    public void undeploy() {
        RequestProcessor.getDefault().post(new Runnable() {
            public void run () {
                StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(TomcatWebModule.class, "MSG_START_UNDEPLOY",  // NOI18N
                    new Object []{getPath()})); 
                ProgressObject po = manager.undeploy(target);
                po.addProgressListener(new TomcatProgressListener());
            }
        }, 0);
        
    }

    public void start() {
        RequestProcessor.getDefault().post(new Runnable() {
            public void run () {
                StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(TomcatWebModule.class, "MSG_START_STARTING",  // NOI18N
                    new Object []{getPath()}));
                ProgressObject po = manager.start(target);
                po.addProgressListener(new TomcatProgressListener());
                isRunning = true;
            }
        }, 0);
    }

    public void stop() {
        RequestProcessor.getDefault().post(new Runnable() {
            public void run () {
                
                StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(TomcatWebModule.class, "MSG_START_STOPPING",  // NOI18N
                    new Object []{getPath()}));
                ProgressObject po = manager.stop(target);
                po.addProgressListener(new TomcatProgressListener());
                isRunning = false;
            }
        }, 0);
    }

    public boolean isRunning() {
        return isRunning;
    }   
    
    
    private String constructDisplayName(){
        if (isRunning())
            return getPath();
        else
            return getPath() + " [" + NbBundle.getMessage(TomcatWebModuleNode.class, "LBL_Stopped")  // NOI18N
               +  "]";
    }
    
    private class TomcatProgressListener implements ProgressListener {
        
        public void handleProgressEvent(javax.enterprise.deploy.spi.status.ProgressEvent progressEvent) {
            if (progressEvent.getDeploymentStatus().getState() == javax.enterprise.deploy.shared.StateType.COMPLETED){
                javax.enterprise.deploy.shared.CommandType command = progressEvent.getDeploymentStatus().getCommand();
                if (command == javax.enterprise.deploy.shared.CommandType.START
                    || command == javax.enterprise.deploy.shared.CommandType.STOP){
                        StatusDisplayer.getDefault().setStatusText(progressEvent.getDeploymentStatus().getMessage());
                        node.setDisplayName(constructDisplayName());
                }
                else {
                    if (command == javax.enterprise.deploy.shared.CommandType.UNDEPLOY){
                        Children children = node.getParentNode().getChildren();
                        if (children instanceof TomcatWebModuleChildren){
                            ((TomcatWebModuleChildren)children).updateKeys();
                            StatusDisplayer.getDefault().setStatusText(progressEvent.getDeploymentStatus().getMessage());
                        }
                    }
                }
            }
        }
    }
    
    public static class TomcatWebModuleComparator implements Comparator {
        
        public int compare(Object o1, Object o2) {
            TomcatWebModule wm1 = (TomcatWebModule) o1;
            TomcatWebModule wm2 = (TomcatWebModule) o2;
            
            return wm1.getModuleID().compareTo(wm2.getModuleID());
        }
        
    }
}
