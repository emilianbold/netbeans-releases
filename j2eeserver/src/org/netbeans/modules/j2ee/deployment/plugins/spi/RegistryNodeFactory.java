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

package org.netbeans.modules.j2ee.deployment.plugins.spi;


import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 * This interface allows plugin to create all the registry nodes
 * (other than the root node) as {@link org.openide.nodes.Node} subclasses,
 * and use {@link org.openide.nodes.FilterNode} to generate the display,
 * adding infrastructure actions in, and exposing certain infrastructure to
 * the plugins for use in constructing nodes.
 * Use a look-like infrastructure so migration to looks can happen easier.
 * Plugins need to register an instance of this class in module layer in folder
 * <code>J2EE/DeploymentPlugins/{plugin_name}</code>.
 *
 * @see org.openide.nodes.Node
 * @see org.openide.nodes.FilterNode
 *
 * @author  George Finklang
 */
public interface RegistryNodeFactory {

     /**
      * Return node representing the admin server.  Children of this node are filtered.
      * Start/Stop/Remove/SetAsDefault actions will be added by FilterNode if appropriate.
      * @param lookup will contain DeploymentFactory, DeploymentManager, Management objects. 
      * @return admin server node.
      */
     public Node getManagerNode(Lookup lookup);

     /**
      * Provide node representing Deployment API Target object.  
      * Start/Stop/SetAsDefault actions will be added by FilterNode if appropriate.
      * @param lookup will contain DeploymentFactory, DeploymentManager, Target, Management objects.
      * @return target server node
      */
     public Node getTargetNode(Lookup lookup);
}
