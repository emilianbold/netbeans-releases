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

package org.netbeans.modules.j2ee.genericserver.nodes;

import org.netbeans.modules.j2ee.deployment.plugins.api.RegistryNodeFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 *
 * @author Martin Adamek
 */
public class GSRegistryNodeFactory implements RegistryNodeFactory {
    
    public Node getTargetNode(Lookup lookup) {
        return new GSTargetNode(lookup);
    }
    
    public Node getManagerNode(Lookup lookup) {
        GSInstanceNode tn = new GSInstanceNode(new Children.Map(), lookup);
        return tn;
    }
    
}
