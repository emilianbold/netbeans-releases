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

package org.netbeans.modules.j2ee.deployment.impl.ui;

import org.netbeans.modules.j2ee.deployment.plugins.api.RegistryNodeFactory;
import org.netbeans.modules.j2ee.deployment.impl.*;
import org.netbeans.modules.j2ee.deployment.impl.ui.*;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import javax.enterprise.deploy.spi.factories.DeploymentFactory;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.Target;


/*
 * RegistryNodeProvider.java
 *
 * Created on December 19, 2003, 11:21 AM
 * @author  nn136682
 */
public class RegistryNodeProvider {
    RegistryNodeFactory factory;
    
    /** Creates a new instance of RegistryNodeProvider */
    public RegistryNodeProvider(RegistryNodeFactory factory) {
        this.factory = factory;
    }
       
    public Node createInstanceNode(ServerInstance instance) {
        return createInstanceNode(instance, false);
    }
    private Node createInstanceNode(ServerInstance instance, boolean removeRefreshListener) {
        InstanceNode xnode = new InstanceNode(instance);
        if (removeRefreshListener)
            instance.removeRefreshListener(xnode);
        
        if (factory != null) {
            Node original = factory.getManagerNode(createLookup(instance));
            if (original != null) {
                // if displayName was not explicitly set, use displayName from
                // the original node
                if (instance.getDisplayName() == null)
                    instance.setDisplayName(original.getDisplayName());
                return new FilterXNode(original, xnode, true, new FilterXNode.XChildren(xnode));
            }
        }
        return xnode;
    }
    
    public Node createTargetNode(ServerTarget target) {
        if (factory != null) {
            Node original = factory.getTargetNode(createLookup(target));
            if (original != null) {
                TargetBaseNode xnode = new TargetBaseNode(org.openide.nodes.Children.LEAF, target);
                return new FilterXNode(original, xnode, true);
            }
        }
        return new TargetBaseNode(org.openide.nodes.Children.LEAF, target);
    }
    
    public Node createInstanceTargetNode(ServerInstance instance) {
        Node original = createInstanceNode(instance, true);
        return new InstanceTargetXNode(original, instance);
    }
    
    static Lookup createLookup(final Server server) {
        return new Lookup() {
            public Object lookup(Class clazz) {
                if (DeploymentFactory.class.isAssignableFrom(clazz))
                    return server.getDeploymentFactory();
                if (DeploymentManager.class.isAssignableFrom(clazz))
                    return server.getDeploymentManager();
                return null;
            }
            public Lookup.Result lookup(Lookup.Template template) {
                return null;
            }
        };
    }
    
    static Lookup createLookup(final ServerInstance instance) {
        return new Lookup() {
            public Object lookup(Class clazz) {
                if (DeploymentFactory.class.isAssignableFrom(clazz))
                    return instance.getServer().getDeploymentFactory();
                if (DeploymentManager.class.isAssignableFrom(clazz)) {
                    return instance.isConnected() ? instance.getDeploymentManager()
                        : instance.getDisconnectedDeploymentManager();
                }
                return null;
            }
            public Lookup.Result lookup(Lookup.Template template) {
                return null;
            }
        };
    }
    
    static Lookup createLookup(final ServerTarget target) {
        return new Lookup() {
            public Object lookup(Class clazz) {
                if (DeploymentFactory.class.isAssignableFrom(clazz))
                    return target.getInstance().getServer().getDeploymentFactory();
                if (DeploymentManager.class.isAssignableFrom(clazz)) {
                    ServerInstance instance = target.getInstance();
                    return instance.isConnected() ? instance.getDeploymentManager()
                        : instance.getDisconnectedDeploymentManager();
                }
                if (Target.class.isAssignableFrom(clazz))
                    return target.getTarget();
                return null;
            }
            public Lookup.Result lookup(Lookup.Template template) {
                return null;
            }
        };    }
}
