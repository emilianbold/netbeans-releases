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

import java.util.*;
import org.netbeans.modules.tomcat5.TomcatManager;
import org.openide.nodes.*;
import org.openide.util.NbBundle;
import org.openide.util.Lookup;

import javax.enterprise.deploy.spi.DeploymentManager;

/**
 *
 * @author  Petr Pisl
 */

public class TomcatInstanceNode extends AbstractNode {
    
    private static String  ICON_BASE = "org/netbeans/modules/tomcat5/resources/tomcat5"; // NOI18N
    
    private Lookup lkp;
    
    /** Creates a new instance of TomcatInstaceNode 
      @param lookup will contain DeploymentFactory, DeploymentManager, Management objects. 
     */
    public TomcatInstanceNode(Children children, Lookup lookup) {
        super(children);
        lkp = lookup;
        setIconBase(ICON_BASE);
        this.setName("TomcatInstanceNode"); //NOI18N
    }
    
    
    public String getDisplayName(){
        return NbBundle.getMessage(TomcatInstanceNode.class, "LBL_TomcatInstanceNode",  // NOI18N
            new Object []{getPort()});
    }
    
    private String getPort (){
        // TODO Port has to be obtained 
        DeploymentManager m = (DeploymentManager)lkp.lookup(DeploymentManager.class);
        if (m instanceof TomcatManager){
            return ((TomcatManager)m).getUri();
        }
        return "0";
    }
    
    
}
