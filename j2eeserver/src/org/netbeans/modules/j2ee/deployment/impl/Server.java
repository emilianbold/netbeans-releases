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


package org.netbeans.modules.j2ee.deployment.impl;

import java.io.OutputStream;
import javax.enterprise.deploy.shared.factories.DeploymentFactoryManager;
import javax.enterprise.deploy.spi.factories.DeploymentFactory;
import javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException;
import javax.enterprise.deploy.spi.*;
import org.netbeans.modules.j2ee.deployment.common.api.ValidationException;
import org.netbeans.modules.j2ee.deployment.impl.gen.nbd.*;
import org.netbeans.modules.j2ee.deployment.plugins.api.*;
import org.netbeans.modules.j2ee.deployment.impl.ui.RegistryNodeProvider;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.ModuleConfigurationFactory;
import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.util.Lookup;
import org.openide.cookies.InstanceCookie;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import javax.enterprise.deploy.shared.ModuleType;
import org.netbeans.modules.j2ee.deployment.plugins.spi.J2eePlatformFactory;
import org.netbeans.modules.j2ee.deployment.plugins.spi.OptionalDeploymentManagerFactory;
import org.netbeans.modules.j2ee.deployment.plugins.spi.RegistryNodeFactory;
import org.netbeans.modules.j2ee.deployment.plugins.spi.VerifierSupport;
import org.openide.util.lookup.Lookups;


public class Server implements Node.Cookie {
    
    static public final String ATTR_needsFindServerUI = "needsFindServerUI";
    
    final NetbeansDeployment dep;
    final Class factoryCls;
    DeploymentFactory factory = null;
    DeploymentManager manager = null;
    RegistryNodeProvider nodeProvider = null;
    final String name;
    Map configMap;
    Map customMap;
    Lookup lkp;
    boolean needsFindServerUI = false;
    
    public Server(FileObject fo) throws Exception {
        //long t0 = System.currentTimeMillis();
        initDeploymentConfigurationFileList(fo);
        name = fo.getName();
        FileObject descriptor = fo.getFileObject("Descriptor");
        if(descriptor == null) {
            String msg = NbBundle.getMessage(Server.class, "MSG_InvalidServerPlugin", name);
            throw new IllegalStateException(msg);
        }
        needsFindServerUI = getBooleanValue(descriptor.getAttribute(ATTR_needsFindServerUI), false);
        
        dep = NetbeansDeployment.createGraph(descriptor.getInputStream());
        
        lkp = Lookups.forPath(fo.getPath());
        factory = lkp.lookup (DeploymentFactory.class);
        if (factory != null) {
            factoryCls = factory.getClass ();
        } else {
            FileObject factoryinstance = fo.getFileObject("Factory.instance");
            if(factoryinstance == null) {
                String msg = NbBundle.getMessage(Server.class, "MSG_NoFactoryInstanceClass", name);
                Logger.getLogger("global").log(Level.SEVERE, msg);
                factoryCls = null;
                return;
            }
            DataObject dobj = DataObject.find(factoryinstance);
            InstanceCookie cookie = (InstanceCookie) dobj.getCookie(InstanceCookie.class);
            if(cookie == null) {
                String msg = NbBundle.getMessage(Server.class, "MSG_FactoryFailed", name, cookie);
                Logger.getLogger("global").log(Level.SEVERE, msg);
                factoryCls = null;
                return;
            }
            factoryCls = cookie.instanceClass();

            // speculative code depending on the DF implementation and if it registers
            // itself with DFM or not

            try {
                factory = (DeploymentFactory) cookie.instanceCreate();
            } catch (Exception e) {
                Logger.getLogger("global").log(Level.INFO, null, e);
            }
        }
        //System.out.println("Create plugin "+name+" in "+(System.currentTimeMillis() - t0));
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
    
    public DeploymentManager getDisconnectedDeploymentManager() throws DeploymentManagerCreationException  {
        if(manager == null) {
            manager = getDisconnectedDeploymentManager(dep.getDisconnectedString());
        }
        return manager;
    }
    
    public DeploymentManager getDisconnectedDeploymentManager(String uri) throws DeploymentManagerCreationException {
        return getFactory().getDisconnectedDeploymentManager(uri);
    }
    
    public boolean handlesUri(String uri) {
        try {
            return getFactory().handlesURI(uri);
        } catch (Exception e) {
            Logger.getLogger("global").log(Level.INFO, null, e);
            return false;
        }
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
    
    // PENDING should be cached?
    public String getHelpId(String beanClass) {
        ConfigBean[] beans = dep.getConfigBean();
        for(int i = 0; i < beans.length; i++) {
            if(beans[i].getClassName().equals(beanClass))
                return beans[i].getHelpid();
        }
        return null;
    }
    
    public RegistryNodeProvider getNodeProvider() {
        if (nodeProvider != null)
            return nodeProvider;
        
        RegistryNodeFactory nodeFact = (RegistryNodeFactory) lkp.lookup(RegistryNodeFactory.class);
        if (nodeFact == null) {
            String msg = NbBundle.getMessage(Server.class, "MSG_NoInstance", name, RegistryNodeFactory.class);
            Logger.getLogger("global").log(Level.INFO, msg);
        }
        nodeProvider = new RegistryNodeProvider(nodeFact); //null is acceptable
        return nodeProvider;
    }
    
    public RegistryNodeFactory getRegistryNodeFactory() {
        return (RegistryNodeFactory) lkp.lookup(RegistryNodeFactory.class);
    }
    
    /** returns OptionalDeploymentManagerFactory or null it is not provided by the plugin */
    public OptionalDeploymentManagerFactory getOptionalFactory () {
        OptionalDeploymentManagerFactory o = (OptionalDeploymentManagerFactory) lkp.lookup (OptionalDeploymentManagerFactory.class);
        return o;
    }
    
    /** returns J2eePlatformFactory or null if it is not provided by the plugin */
    public J2eePlatformFactory getJ2eePlatformFactory () {
        J2eePlatformFactory o = (J2eePlatformFactory) lkp.lookup (J2eePlatformFactory.class);
        return o;
    }
    
//    /** returns DConfigBeanUIFactory or null it is not provided by the plugin */
//    public DConfigBeanUIFactory getDConfigBeanUIFactory () {
//        DConfigBeanUIFactory o = (DConfigBeanUIFactory) lkp.lookup (DConfigBeanUIFactory.class);
//        return o;
//    }
    
//    public DConfigBeanProperties getDConfigBeanProperties(DConfigBean bean) {
//        DConfigBeanUIFactory beanUIFactory = getDConfigBeanUIFactory();
//        if (beanUIFactory == null) return null;
//        return beanUIFactory.getUICustomization(bean);
//    }
    
//    public ConfigurationSupport getConfigurationSupport() {
//        ConfigurationSupport cs = (ConfigurationSupport) lkp.lookup (ConfigurationSupport.class);
//        return cs;
//    }
    
    public ModuleConfigurationFactory getModuleConfigurationFactory() {
        return lkp.lookup(ModuleConfigurationFactory.class);
    }

    public VerifierSupport getVerifierSupport() {
        VerifierSupport vs = (VerifierSupport) lkp.lookup (VerifierSupport.class);
        return vs;
    }
    
    public boolean canVerify(Object moduleType) {
        VerifierSupport vs = getVerifierSupport();
        return  vs != null && vs.supportsModuleType(moduleType);
    }
    
    public void verify(FileObject target, OutputStream logger) throws ValidationException {
        getVerifierSupport().verify(target, logger);
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

    static public boolean getBooleanValue(Object v, boolean dvalue) {
        if (v instanceof Boolean) 
            return ((Boolean)v).booleanValue();
        if (v instanceof String)
            return Boolean.valueOf((String) v).booleanValue();
        return dvalue;
    }
    
    public boolean needsFindServerUI() {
        return needsFindServerUI;
    }
    
    public String toString () {
        return getShortName ();
    }
    
    public boolean supportsModuleType(ModuleType type) {
        if (J2eeModule.WAR.equals(type)) {
            return this.canDeployWars();
        } else if (J2eeModule.EJB.equals(type)) {
            return this.canDeployEjbJars();
        } else if (J2eeModule.EAR.equals(type)) {
            return this.canDeployEars();
        } else {
            // PENDING, precise answer for other module types, for now assume true
            return true;
        }
    }

    public static final String LAYER_DEPLOYMENT_FILE_NAMES = "DeploymentFileNames"; //NOI18N
    private Map deployConfigDescriptorMap;
    private void initDeploymentConfigurationFileList(FileObject fo) {
        deployConfigDescriptorMap = new HashMap();
        FileObject deplFNames = fo.getFileObject(LAYER_DEPLOYMENT_FILE_NAMES);
        if (deplFNames != null) {
            FileObject mTypes [] = deplFNames.getChildren();
            for (int j=0; j < mTypes.length; j++) {
                String mTypeName = mTypes [j].getName().toUpperCase();
                FileObject allNames [] = mTypes [j].getChildren();
                if (allNames == null || allNames.length == 0)
                    continue;
                ArrayList filepaths = new ArrayList();
                for (int i = 0; i < allNames.length; i++) {
                    if (allNames[i] == null)
                        continue;
                    String fname = allNames [i].getNameExt();
                    filepaths.add(fname.replace('\\', '/')); //just in case..
                }
                deployConfigDescriptorMap.put(mTypeName, filepaths.toArray(new String[filepaths.size()]));
            }
        }
    }
    
    public String[] getDeploymentPlanFiles(Object type) {
        return (String[]) deployConfigDescriptorMap.get(type.toString().toUpperCase());
    }
}
