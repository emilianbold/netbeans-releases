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

package org.netbeans.modules.j2ee.deployment.plugins.spi;


import org.openide.nodes.Node;
import org.openide.util.Lookup;

/** 
 * This interface allows plugin to create all the registry nodes 
 * (other than the root node) as opg.openide.nodes.Node subclasses, 
 * and use FilterNode to generate the display, adding infrastructure actions in,
 * and exposing certain infrastructure to the plugins for use in
 * constructing nodes.  Use a look-like infrastructure so migration to
 * looks can happen easier.
 * @author  George Finklang
 */
public interface RegistryNodeFactory {

    /**
      *  Return node representing the plugin.  Children of this node are filtered.
      *  @param lookup contains DeploymentFactory, DeploymentManager(s)
      *  @return a FilterNode
      */
     public Node getFactoryNode(Lookup lookup);

     /**
      * Return node representing the admin server.  Children of this node are filtered.
      * Start/Stop/Remove/SetAsDefault actions will be added by FilterNode if appropriate.
      * @param lookup will contain DeploymentFactory, DeploymentManager, Management objects. 
      * @return admin server node.
      */
     public Node getManagerNode(Lookup lookup);

     /**
      * Provide node representing JSR88 Target object.  
      * Start/Stop/SetAsDefault actions will be added by FilterNode if appropriate.
      * @param lookup will contain DeploymentFactory, DeploymentManager, Target, Management objects.
      * @return target server node
      */
     public Node getTargetNode(Lookup lookup);
}
