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

package org.netbeans.modules.j2ee.deployment.impl.ui;

import org.netbeans.modules.j2ee.deployment.plugins.api.RegistryNodeFactory;
import org.netbeans.modules.j2ee.deployment.impl.*;
import org.openide.ErrorManager;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import javax.enterprise.deploy.spi.factories.DeploymentFactory;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException;


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
        return new InstanceNodeDecorator(createInstanceNodeImpl(instance, true), instance);
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
        Node original = createInstanceNodeImpl(instance, false);
        return new InstanceNodeDecorator(new InstanceTargetXNode(original, instance), instance);
    }
    
    private Node createInstanceNodeImpl(ServerInstance instance, boolean addStateListener) {
        InstanceNode xnode = new InstanceNode(instance, addStateListener);
        
        if (factory != null) {
            Node original = factory.getManagerNode(createLookup(instance));
            if (original != null) {
                return new FilterXNode(original, xnode, true, new FilterXNode.XChildren(xnode));
            }
        }
        return xnode;
    }
    
    static Lookup createLookup(final Server server) {
        return new Lookup() {
            public Object lookup(Class clazz) {
                if (DeploymentFactory.class.isAssignableFrom(clazz))
                    return server.getDeploymentFactory();
                if (DeploymentManager.class.isAssignableFrom(clazz)) {
                    try {
                        return server.getDisconnectedDeploymentManager();
                    } catch (DeploymentManagerCreationException dmce) {
                        ErrorManager.getDefault().notify(dmce);
                    }
                }
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
                    try {
                        return instance.isConnected() ? instance.getDeploymentManager()
                                                      : instance.getDisconnectedDeploymentManager();
                    }  catch (DeploymentManagerCreationException dmce) {
                        ErrorManager.getDefault().notify(dmce);
                    }
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
                    try {
                        return instance.isConnected() ? instance.getDeploymentManager()
                                                      : instance.getDisconnectedDeploymentManager();
                    }  catch (DeploymentManagerCreationException dmce) {
                        ErrorManager.getDefault().notify(dmce);
                    }
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
