/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.j2ee.deployment.impl;

import javax.enterprise.deploy.shared.factories.DeploymentFactoryManager;
import javax.enterprise.deploy.spi.factories.DeploymentFactory;
import javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException;
import javax.enterprise.deploy.spi.*;
import org.netbeans.modules.j2ee.deployment.impl.gen.nbd.*;
import org.netbeans.modules.j2ee.deployment.plugins.spi.*;
import org.openide.filesystems.*;
import org.openide.loaders.DataObject;
import org.openide.cookies.InstanceCookie;
import org.openide.nodes.Node;
import java.util.*;

public class Server implements Node.Cookie {
    
    final NetbeansDeployment dep;
    final Class factoryCls;
    DeploymentFactory factory = null;
    DeploymentManager manager = null;
    final String name;
    Map configMap;
    Map customMap;
    
    public Server(FileObject fo) throws Exception {
        name = fo.getName();
        FileObject descriptor = fo.getFileObject("Descriptor");
        FileObject factoryinstance = fo.getFileObject("Factory.instance");
        if(descriptor == null || factoryinstance == null)
            throw new IllegalStateException("Incorrect server plugin installation");
        dep = NetbeansDeployment.createGraph(descriptor.getInputStream());
        DataObject dobj = DataObject.find(factoryinstance);
        InstanceCookie cookie = (InstanceCookie) dobj.getCookie(InstanceCookie.class);
        if(cookie == null)
            throw new IllegalStateException("Incorrect server plugin installation");
        factoryCls = cookie.instanceClass();
        
        // speculative code depending on the DF implementation and if it registers
        // itself with DFM or not
        
        try {
//            System.err.println("Trying to create plugin");
            factory = (DeploymentFactory) cookie.instanceCreate();
//            System.err.println("Created plugin");
        } catch (Exception e) {
//            System.err.println("Couldn't create factory instance from Server constructor");
            e.printStackTrace(System.err);
        }
    }
        
    
    private DeploymentFactory getFactory() {
        if (factory == null) {
            DeploymentFactoryManager dfm = DeploymentFactoryManager.getInstance();
//            System.err.println(dfm);
//            System.err.println(dfm.getDeploymentFactories().length);
//            System.err.println(factoryCls);
//            System.err.println(factoryCls.getName());
            try {
//                System.err.println(Class.forName(factoryCls.getName()));
                Thread.sleep(5000);
            } catch (Exception e) {}
//            System.err.println(dfm.getDeploymentFactories().length);
//            System.err.println(DeploymentFactoryManager.getInstance());
            DeploymentFactory[] factories = DeploymentFactoryManager.getInstance().getDeploymentFactories();
            for(int i = 0; i < factories.length; i++) {
//                System.err.println("Checking factory " + factories[i]);
                if(factoryCls.isInstance(factories[i])) {
                    factory = factories[i];
                    break;
                }
            }
        }
        if(factory == null) {
            //          ServerRegistry.getInstance().removePlugin(sfo);
            throw new IllegalStateException();
        }
        return factory;
    }
    
    public DeploymentManager getDeploymentManager() {
        if(manager == null) {
            getFactory();
            if (manager == null) {
                try {
                    manager = factory.getDisconnectedDeploymentManager(dep.getDisconnectedString());
                } catch (Exception e) {
                    e.printStackTrace(System.err);
                    // PENDING should remove this server and show error message.
                }
            }
        }
        return manager;
    }
    
    public boolean handlesUri(String uri) {
        return getFactory().handlesURI(uri);
    }
    
    public DeploymentManager getDeploymentManager(String uri, String username, String password) throws DeploymentManagerCreationException {
        return getFactory().getDeploymentManager(uri,username, password);
    }
    
    public String getDisplayName() {
        return getFactory().getDisplayName();
    }
    
    public String getShortName() {
        return name;
    }
    
    public String getIconBase() {
        return dep.getIcon(); 
    }
    
    public boolean canDeployEars() {
        return dep.getContainerLimitation() == null || dep.getContainerLimitation().isEarDeploy();
    }
    
    public boolean canDeployWars() {
        return dep.getContainerLimitation() == null || dep.getContainerLimitation().isWarDeploy();
    }
    
    public boolean canDeployEjbJars() {
        return dep.getContainerLimitation() == null || dep.getContainerLimitation().isEjbjarDeploy();
    }
    
    public String getFindServer() {
        return dep.getFinderUi();
    }
        
    public ConfigBeanDescriptor getConfigBeanDescriptor(String className) {
        if(configMap == null) {
            ConfigBean[] beans = dep.getConfigBean();
            configMap = new HashMap();
            for(int i = 0; i < beans.length; i++)
                configMap.put(beans[i].getClassName(),new ConfigBeanDescriptor(beans[i]));
        }
        return (ConfigBeanDescriptor) configMap.get(className);
    }
    
    public ConfigBeanCustomizer getCustomizer(String xpath) {
        if(customMap == null) {
            Customizer[] beans = dep.getCustomizer();
            customMap = new HashMap();
            for(int i = 0; i < beans.length; i++)
                customMap.put(beans[i].getXpath(),beans[i].getClassName());
        }
            String className = (String) customMap.get(xpath);
            return (ConfigBeanCustomizer) getClassFromPlugin(className);
    }
    
    private Object getClassFromPlugin(String className) {
        try {
        return factory.getClass().getClassLoader().loadClass(className).newInstance();
        } catch (Exception e) {
			e.printStackTrace();
            throw new IllegalStateException("Couldn't load class " + className + " from plugin.");
        }
    }
    
    // PENDING should be cached?
    public String getHelpId(String beanClass) {
        ConfigBean[] beans = dep.getConfigBean();
        for(int i = 0; i < beans.length; i++) {
            if(beans[i].getClassName().equals(beanClass))
                return beans[i].getHelpid();
        }
        return null;
    }
    
    public IncrementalDeployment getIncrementalDeployment() {
        String className = dep.getIncrementalDeploy();
        return (IncrementalDeployment) getClassFromPlugin(className);
    }
    
    public InplaceDeployment getInplaceDeployment() {
        String className = dep.getInplaceDeploy();
        return (InplaceDeployment) getClassFromPlugin(className);
    }
    
    public StartServer getStartServer() {
        String className = dep.getStartServer();
System.out.println("**** dep: " + dep);
System.out.println("**** classname: " + className);
        return (StartServer) getClassFromPlugin(className);
    }
    
    public TargetNameResolver getTargetResolver() {
        String className = dep.getNameMapper();
        return (TargetNameResolver) getClassFromPlugin(className);
    }

}
