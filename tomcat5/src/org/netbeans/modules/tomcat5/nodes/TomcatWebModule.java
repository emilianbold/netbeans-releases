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
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.TargetModuleID;
import org.netbeans.modules.tomcat5.TomcatModule;
import org.netbeans.modules.tomcat5.nodes.actions.TomcatWebModuleCookie;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

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
        manager.undeploy(target);
        Children children = node.getParentNode().getChildren();
        if (children instanceof TomcatWebModuleChildren)
            ((TomcatWebModuleChildren)children).updateKeys();
    }

    public void start() {
        manager.start(target);
        isRunning = true;
        node.setDisplayName(constructDisplayName());
    }

    public void stop() {
        manager.stop(target);
        isRunning = false;
        node.setDisplayName(constructDisplayName());
    }

    public boolean isRunning() {
        return isRunning;
    }   
    
    
    private String constructDisplayName(){
        if (isRunning())
            return getPath();
        else
            return getPath() + " (" + NbBundle.getMessage(TomcatWebModuleNode.class, "LBL_Stopped")  // NOI18N
               +  " )";
    }
}
