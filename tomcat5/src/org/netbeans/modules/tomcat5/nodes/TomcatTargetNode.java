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

import java.util.ArrayList;
import javax.enterprise.deploy.shared.ModuleType;
import javax.enterprise.deploy.spi.Target;
import org.openide.nodes.*;
import org.openide.util.Lookup;

import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.TargetModuleID;
import org.netbeans.modules.tomcat5.TomcatManager;
import org.netbeans.modules.tomcat5.TomcatModule;
import org.netbeans.modules.tomcat5.nodes.actions.RefreshWebModulesAction;
import org.netbeans.modules.tomcat5.nodes.actions.RefreshWebModulesCookie;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author  Petr Pisl
 */

public class TomcatTargetNode extends AbstractNode {
    
    private static String  ICON_BASE = "org/netbeans/modules/tomcat5/resources/mfolder"; // NOI18N
    
    private Lookup lkp;
    
    /** Creates a new instance of TomcatTargetNode */
    public TomcatTargetNode(Lookup lookup) {
        super(new Children.Array());
        getChildren().add(new Node[] {new WebModuleHolderNode(lookup)});
        lkp = lookup;
        
    }
 
    public class WebModuleHolderNode extends AbstractNode {
        
        public WebModuleHolderNode (Lookup lookup){
            super(new TomcatWebModuleChildren(lookup));
            setDisplayName(NbBundle.getMessage(TomcatTargetNode.class, "LBL_WebApps"));  // NOI18N
            getCookieSet().add(new RefreshWebModuleChildren ((TomcatWebModuleChildren)getChildren()));
            setIconBase(ICON_BASE);
        }
        
        public javax.swing.Action[] getActions(boolean context) {
            return new SystemAction[] {
                   SystemAction.get(RefreshWebModulesAction.class)
               };        
        }
    }
    
    
    class RefreshWebModuleChildren implements RefreshWebModulesCookie {
        TomcatWebModuleChildren children;

        RefreshWebModuleChildren (TomcatWebModuleChildren children){
            this.children = children;
        }

        public void refresh() {
            children.updateKeys();
        }
    }
}
