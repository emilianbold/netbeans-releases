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

import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.TargetModuleID;
import org.netbeans.modules.tomcat5.TomcatModule;
import org.netbeans.modules.tomcat5.nodes.actions.*;
import org.openide.actions.DeleteAction;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author  Petr Pisl
 */


public class TomcatWebModuleNode extends AbstractNode {

    private static String  ICON_BASE = "org/netbeans/modules/tomcat5/resources/WebModule"; // NOI18N
    
    private TomcatWebModule module;
    
    /** Creates a new instance of TomcatWebModuleNode */
    public TomcatWebModuleNode(TomcatWebModule module) {
        super(Children.LEAF);
        this.module = module;
        setDisplayName(constructName());
        setShortDescription(module.getTomcatModule ().getWebURL());
        getCookieSet().add(module);
        setIconBase(ICON_BASE);
    }
    
    protected SystemAction[] createActions(){
        return new SystemAction[] {
            SystemAction.get (StartAction.class),
            SystemAction.get (StopAction.class),
            null,
            SystemAction.get(OpenURLAction.class),
            SystemAction.get(org.netbeans.modules.tomcat5.nodes.actions.ContextLogAction.class),
            null,
            SystemAction.get (UndeployAction.class)
        };
    }
   
    
    private String constructName(){
        if (module.isRunning())
            return module.getTomcatModule ().getPath();
        else
            return module.getTomcatModule ().getPath() + " [" +
                NbBundle.getMessage(TomcatWebModuleNode.class, "LBL_Stopped")  // NOI18N
                + "]";
    }
      
}
