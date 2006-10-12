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

import java.awt.Image;
import java.util.LinkedList;
import javax.swing.Action;
import org.netbeans.modules.j2ee.deployment.plugins.api.UISupport;
import org.netbeans.modules.j2ee.deployment.plugins.api.UISupport.ServerIcon;
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
    
    
    public Image getIcon(int type) {
        return UISupport.getIcon(ServerIcon.WAR_ARCHIVE);
    }

    public Image getOpenedIcon(int type) {
        return getIcon(type);
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
