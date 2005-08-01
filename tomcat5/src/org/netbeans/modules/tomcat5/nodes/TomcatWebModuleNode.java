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

import java.util.LinkedList;
import javax.swing.Action;
import org.netbeans.modules.tomcat5.TomcatManager;
import org.netbeans.modules.tomcat5.nodes.actions.*;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author  Petr Pisl
 */


public class TomcatWebModuleNode extends AbstractNode {
    
    private TomcatWebModule module;
    
    /** Creates a new instance of TomcatWebModuleNode */
    public TomcatWebModuleNode(TomcatWebModule module) {
        super(Children.LEAF);
        this.module = module;
        setDisplayName(constructName());
        setShortDescription(module.getTomcatModule ().getWebURL());
        getCookieSet().add(module);
        setIconBaseWithExtension("org/netbeans/modules/tomcat5/resources/WebModule.gif"); // NOI18N
    }
    
    public Action[] getActions(boolean context){
        TomcatManager tm = (TomcatManager)module.getDeploymentManager();
        java.util.List actions = new LinkedList();
        actions.add(SystemAction.get(StartAction.class));
        actions.add(SystemAction.get(StopAction.class));
        actions.add(null);
        actions.add(SystemAction.get(OpenURLAction.class));
        if (tm != null && tm.isTomcat50()) {
            actions.add(SystemAction.get(ContextLogAction.class));
        }
        actions.add(null);
        actions.add(SystemAction.get(UndeployAction.class));
        return (SystemAction[])actions.toArray(new SystemAction[actions.size()]);
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
