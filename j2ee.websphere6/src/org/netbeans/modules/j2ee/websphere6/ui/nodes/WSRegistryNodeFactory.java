/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.j2ee.websphere6.ui.nodes;

import org.openide.util.*;
import org.openide.nodes.*;
import org.netbeans.modules.j2ee.deployment.plugins.api.*;

/**
 * Factory that creates nodes that will appear in the server registry.
 * 
 * @author Kirill Sorokin
 */
public class WSRegistryNodeFactory implements RegistryNodeFactory {
    /**
     * Creates a node that represents a concrete target in a particular server
     * instance. By default it is filtered and does not get visible
     * 
     * @param lookup a lookup with useful objects such as the deployment 
     *      manager for the instance
     * 
     * @return the node for the target
     */
    public Node getTargetNode(Lookup lookup) {
        return new WSTargetNode(lookup);
    }
    
    /**
     * Creates a node that represents a particular server instance.
     * 
     * @param lookup a lookup with useful objects such as the deployment 
     *      manager for the instance
     * 
     * @return the node for the instance
     */
    public Node getManagerNode(Lookup lookup) {
        return new WSManagerNode(new Children.Map(), lookup);
    }
}
