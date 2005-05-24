/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/*
 * NodeFactory.java
 *
 * Created on December 21, 2003, 8:19 AM
 */

package org.netbeans.modules.j2ee.sun.ide.j2ee.runtime.nodes;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.Target;

/**
 *
 * @author  ludo
 */
public class NodeFactory implements org.netbeans.modules.j2ee.deployment.plugins.api.RegistryNodeFactory {
    
    /** Creates a new instance of NodeFactory */
    public NodeFactory() {
    }
       
    public org.openide.nodes.Node getManagerNode(org.openide.util.Lookup lookup) {
        DeploymentManager depManager = (DeploymentManager) lookup.lookup(DeploymentManager.class);
        //SunDeploymentManagerInterface dm = (SunDeploymentManagerInterface)depManager;
        //System.out.println("User Name " + dm.getUserName() + " Host " + dm.getHost());
        
        if (depManager == null ) {
            System.out.println("WARNING: getManagerNode lookup returned "+depManager);//NOI18N
            return null;
        }
        return new ManagerNode(depManager);
    }
    
    public org.openide.nodes.Node getTargetNode(org.openide.util.Lookup lookup) {
        Target target = (Target) lookup.lookup(Target.class);
        DeploymentManager depManager = (DeploymentManager) lookup.lookup(DeploymentManager.class);
                        
        if (depManager == null ) {
            System.out.println("WARNING: getManagerNode lookup returned "+depManager);//NOI18N
        }
        if (target == null ) {
            System.out.println("WARNING: getTargetNode lookup returned "+target);//NOI18N
            return null;
        }
        
        
        try{
            return initializePluginTree(depManager);
        } catch (Exception e){
            System.out.println("Cannot create the instance node in the " +
                    "factory " + e);//NOI18N
        }

        
        return null;//too bad
    }
    
    
    /**
     *
     *
     */
    private org.openide.nodes.Node initializePluginTree( final DeploymentManager deployMgr) throws Exception {
        ClassLoader origClassLoader=Thread.currentThread().getContextClassLoader();
        try{
            Class pluginRootFactoryClass =org.netbeans.modules.j2ee.sun.ide.Installer.getPluginLoader().
                    loadClass("org.netbeans.modules.j2ee.sun.util.PluginRootNodeFactory");//NOI18N
            Constructor constructor =pluginRootFactoryClass.getConstructor(new Class[] {DeploymentManager.class});
            Object pluginRootFactory =constructor.newInstance(new Object[] {deployMgr});
            Class factoryClazz = pluginRootFactory.getClass();
            Method method =factoryClazz.getMethod("getPluginRootNode", null);
            
            
            Thread.currentThread().setContextClassLoader(
                    org.netbeans.modules.j2ee.sun.ide.Installer.getPluginLoader());
            
            
            return (org.openide.nodes.Node)method.invoke(pluginRootFactory, null);
        } catch (Exception e){
            throw e;
        } finally {
            Thread.currentThread().setContextClassLoader(origClassLoader);
        }
    }
    
    
    
}
