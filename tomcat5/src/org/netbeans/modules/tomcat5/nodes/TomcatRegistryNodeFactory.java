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

import org.netbeans.modules.j2ee.deployment.plugins.api.RegistryNodeFactory;
import org.openide.nodes.*;
import org.openide.util.Lookup;
import org.openide.util.actions.*;
import org.openide.actions.DeleteAction;

/** 
 * @author Petr Pisl
 */

public class TomcatRegistryNodeFactory implements RegistryNodeFactory {
    
    
    /** Creates a new instance of TomcatRegistryNodeFactory */
    public TomcatRegistryNodeFactory() {
    }
    
    /**
      * Return node representing the admin server.  Children of this node are filtered.
      * @param lookup will contain DeploymentFactory, DeploymentManager, Management objects. 
      * @return admin server node.
      */
    public Node getManagerNode(Lookup lookup) {
        TomcatInstanceNode tn = new TomcatInstanceNode (new Children.Map(), lookup);
        return tn;
    }
    
    /**
      * Provide node representing JSR88 Target object.  
      * @param lookup will contain DeploymentFactory, DeploymentManager, Target, Management objects.
      * @return target server node
      */
    public Node getTargetNode(Lookup lookup) {
        return new TomcatTargetNode(lookup);

    }
}
