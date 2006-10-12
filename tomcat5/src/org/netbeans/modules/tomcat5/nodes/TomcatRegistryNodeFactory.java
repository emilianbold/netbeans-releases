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

import org.netbeans.modules.j2ee.deployment.plugins.api.RegistryNodeFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

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
