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
import org.netbeans.modules.j2ee.deployment.impl.ui.RegistryNodeProvider;
import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.util.Lookup;
import org.openide.cookies.InstanceCookie;
import org.openide.nodes.Node;
import java.util.*;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;


public class Server implements Node.Cookie {
    
    final NetbeansDeployment dep;
    final Class factoryCls;
    DeploymentFactory factory = null;
    DeploymentManager manager = null;
    RegistryNodeProvider nodeProvider = null;
    final String name;
    Map configMap;
    Map customMap;
    Lookup lkp;
    
    public Server(FileObject fo) throws Exception {
        name = fo.getName();
        FileObject descriptor = fo.getFileObject("Descriptor");
        if(descriptor == null)
            throw new IllegalStateException("Incorrect server plugin installation");
        dep = NetbeansDeployment.createGraph(descriptor.getInputStream());
        
        lkp = new FolderLookup (DataFolder.findContainer (fo)).getLookup ();
        factory = (DeploymentFactory) lkp.lookup (DeploymentFactory.class);
        if (factory != null) {
            factoryCls = factory.getClass ();
        } else {
            FileObject factoryinstance = fo.getFileObject("Factory.instance");
            if(factoryinstance == null)
                throw new IllegalStateException("Incorrect server plugin installation");
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
    
    public FindServer getFindServer() {
        FindServer oo = (FindServer) lkp.lookup (FindServer.class);
        if (oo != null) {
            return oo;
        }
        Object o = getClassFromPlugin(dep.getFinderUi());
        if (o instanceof FindServer)
            return (FindServer) o;
        return null;
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
    
    private Object getClassFromPlugin(String className) {
        if (className == null || "".equals(className.trim())) return null; //NOI18N
        try {
        return factory.getClass().getClassLoader().loadClass(className).newInstance();
        } catch (Exception e) {
            ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, e.getMessage());
            return null;
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
    
    public DeploymentManagerWrapper getDeploymentManagerWrapper(Class wrapperClass) {
        if (StartServer.class.isAssignableFrom(wrapperClass))
            return getStartServer();
        else if (IncrementalDeployment.class.isAssignableFrom(wrapperClass))
            return getIncrementalDeployment();
        else if (FileDeploymentLayout.class.isAssignableFrom(wrapperClass))
            return getFileDeploymentLayout();
        else if (ModuleUrlResolver.class.isAssignableFrom(wrapperClass))
            return getModuleUrlResolver();
        else 
            throw new IllegalArgumentException("Unknown DeploymentManagerWrapper class " + wrapperClass.getName()); //NOI18N
    }
    
    public IncrementalDeployment getIncrementalDeployment() {
        IncrementalDeployment o = (IncrementalDeployment) lkp.lookup (IncrementalDeployment.class);
        if (o != null) {
            return o;
        }
        String className = dep.getIncrementalDeploy();
        return (IncrementalDeployment) getClassFromPlugin(className);
    }
    
    public FileDeploymentLayout getFileDeploymentLayout() {
        FileDeploymentLayout o = (FileDeploymentLayout) lkp.lookup (FileDeploymentLayout.class);
        if (o != null) {
            return o;
        }
        String className = dep.getFileDeploymentLayout();
        return (FileDeploymentLayout) getClassFromPlugin(className);
    }
    
    public StartServer getStartServer() {
        StartServer o = (StartServer) lkp.lookup (StartServer.class);
        if (o != null) {
            return o;
        }
        String className = dep.getStartServer();
        return (StartServer) getClassFromPlugin(className);
    }

    public ModuleUrlResolver getModuleUrlResolver() {
        ModuleUrlResolver o = (ModuleUrlResolver) lkp.lookup (ModuleUrlResolver.class);
        if (o != null) {
            return o;
        }
        String className = dep.getModuleUrlResolver();
        return (ModuleUrlResolver) getClassFromPlugin(className);
    }
    
    public DeploymentPlanSplitter getDeploymentPlanSplitter() {
        DeploymentPlanSplitter o = (DeploymentPlanSplitter) lkp.lookup (DeploymentPlanSplitter.class);
        if (o != null) {
            return o;
        }
        String className = dep.getDeploymentPlanSplitter();
        return (DeploymentPlanSplitter) getClassFromPlugin(className);
    }
    
    public RegistryNodeProvider getNodeProvider() {
        if (nodeProvider != null)
            return nodeProvider;
        
        RegistryNodeFactory nodeFact = (RegistryNodeFactory) lkp.lookup(RegistryNodeFactory.class);
        if (nodeFact == null) {
            String msg = NbBundle.getMessage(Server.class, "MSG_NoInstance", name, RegistryNodeFactory.class);
            ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, msg);
        }
        nodeProvider = new RegistryNodeProvider(nodeFact); //null is acceptable
        return nodeProvider;
    }
    
    public ManagementMapper getManagementMapper() {
        ManagementMapper o = (ManagementMapper) lkp.lookup (ManagementMapper.class);
        if (o != null) {
            return o;
        }
        String className = dep.getNameMapper();
        Object mapper = getClassFromPlugin(className);
        if (mapper instanceof ManagementMapper)
            return (ManagementMapper) mapper;
        else {
            ErrorManager.getDefault().log(
            ErrorManager.WARNING, NbBundle.getMessage(Server.class, "MSG_InvalidNameMapper", className));
            return null;
        }
    }
    
    public ServerInstance[] getInstances() {
        Collection ret = new ArrayList();
        for (Iterator i=ServerRegistry.getInstance().getInstances().iterator(); i.hasNext();) {
            ServerInstance inst = (ServerInstance) i.next();
            if (name.equals(inst.getServer().getShortName()))
                ret.add(inst);
        }
        return (ServerInstance[]) ret.toArray(new ServerInstance[ret.size()]);
    }
    
    public WebContextRoot getWebContextRoot() {
        return dep.getWebContextRoot();
    }
    
    public DeploymentFactory getDeploymentFactory() {
        return factory;
    }
}
