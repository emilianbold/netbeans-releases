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

package org.netbeans.modules.j2ee.deployment.config;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.netbeans.modules.j2ee.deployment.common.api.OriginalCMPMapping;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.deployment.impl.ServerString;
import org.netbeans.modules.j2ee.deployment.impl.Server;
import javax.enterprise.deploy.spi.exceptions.ConfigurationException;
import javax.enterprise.deploy.model.*;
import javax.enterprise.deploy.shared.ModuleType;
import javax.enterprise.deploy.spi.*;
import org.netbeans.modules.j2ee.deployment.config.ConfigurationStorage;
import org.netbeans.modules.j2ee.deployment.config.ModuleDeploymentSupport;
import org.netbeans.modules.j2ee.deployment.config.ui.ConfigUtils;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.impl.ServerInstance;
import org.netbeans.modules.j2ee.deployment.impl.ServerRegistry;
import org.netbeans.modules.j2ee.deployment.impl.gen.nbd.WebContextRoot;
import org.netbeans.modules.j2ee.deployment.plugins.api.ConfigurationSupport;
import org.netbeans.modules.j2ee.deployment.plugins.api.DeploymentPlanSplitter;
import org.openide.ErrorManager;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;

/**
 * Each J2eeModuleProvider hold a reference to an instance of this config support.
 * An instance of ConfigDataObject representing the current target configuration
 * and it is cached for to avoid performance penalty of creating new one for every
 * access to configuration.
 *
 * Whenenver target server of the module changes, a new config support is associate
 * with the module providing access to the right configuration data object.
 *
 * @author  nn136682
 */
//PENDING: cleanup the usage of fakeserver, refresh. Instead, provide UI feedback for
// case when provider does not associate with any server.

public final class ConfigSupportImpl implements J2eeModuleProvider.ConfigSupport {
    static public final File[] EMPTY_FILE_LIST = new File[0];
    private J2eeModuleProvider provider;
    private String webContextRootXpath;
    private String webContextRootPropName;
    private java.util.Map relativePaths = null;
    private String configurationPrimaryFileName = null;
    private Map allRelativePaths = null;
    private ConfigDataObject configDO = null;
    private boolean preparing = false; //to cut possible circularity of ensureConfigurationReady
    
    /** Creates a new instance of ConfigSupportImpl */
    public ConfigSupportImpl (J2eeModuleProvider provider) {
        this.provider = provider;
    }

    private void refresh () {
        Server s = getServer();
        if (s == null) {
            return;
        }
        WebContextRoot webContextRoot = s.getWebContextRoot();
        if (webContextRoot != null) {
            webContextRootXpath = webContextRoot.getXpath();
            webContextRootPropName = webContextRoot.getPropName();
        }
    }
    
    private boolean initForWeb() {
        if (! getProvider().getJ2eeModule().getModuleType().equals(J2eeModule.WAR))
            return false;
        
        if (webContextRootXpath != null && webContextRootPropName != null) {
            return true;
        }
        
        refresh();
        
        if (webContextRootXpath != null && webContextRootPropName != null) {
            return true;
        }
        return false;
    }
    
    private Server getServer () {
        return ServerRegistry.getInstance ().getServer (getProvider ().getServerID ());
    }
    
    private DConfigBean getWebContextDConfigBean() {
        refresh();
        try {
            ConfigurationStorage cs = getStorage();
            if (cs == null)
                return null;
            DeploymentConfiguration dc = cs.getDeploymentConfiguration();
            DeployableObject deployable = dc.getDeployableObject();
            //PENDIND: do we need if (deployable instanceof J2eeApplicationObject) ...
            DDBeanRoot ddBeanRoot = deployable.getDDBeanRoot();
            DConfigBeanRoot configBeanRoot = dc.getDConfigBeanRoot(ddBeanRoot);
            DDBean[] ddBeans = ddBeanRoot.getChildBean(webContextRootXpath);
            if (ddBeans == null || ddBeans.length != 1) {
                ErrorManager.getDefault ().log (ErrorManager.EXCEPTION, "DDBeans not found"); //NOI18N
                return null; //better than throw exception
            }
            return configBeanRoot.getDConfigBean(ddBeans[0]);
            
        } catch (Exception e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
        }
        return null;
    }
    
    public boolean createInitialConfiguration() {
        try {
            FileObject fo = findPrimaryConfigurationFO();
            if (fo == null) {
                return ensureConfigurationReady();
            }
        } catch (Exception e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
        }
        return false;
    }
    
    public boolean ensureConfigurationReady() {
        if (preparing) { // should be single place to check
            return true; // optimistic
        }
        preparing = true;
        try {
            ConfigurationStorage storage = getStorage();
            return (storage != null);
        } finally {
            preparing = false;
        }
    }
     
    /**
     * Get context root
     * @return string value, null if not set or could not find
     */
    public String getWebContextRoot() {
        if (initForWeb() == false)
            return null;

        DConfigBean configBean = getWebContextDConfigBean();
        if (configBean == null) {
            ErrorManager.getDefault ().log ("ConfigBean for "+webContextRootXpath+" not found"); //NOI18N
            return null;
        }
        return (String) ConfigUtils.getBeanPropertyValue(configBean, webContextRootPropName);
    }
    
    /**
     * Set context root
     */
    public void setWebContextRoot(String contextRoot) {
        if (initForWeb() == false)
            return;

        DConfigBean configBean = getWebContextDConfigBean();
        if (configBean == null) {
            ErrorManager.getDefault ().log ("ConfigBean for "+webContextRootXpath+":"+webContextRootPropName+" not found"); //NOI18N
            return;
        }
        ConfigUtils.setBeanPropertyValue(configBean, webContextRootPropName, contextRoot);
    }
    
    public void resetStorage() {
        try {
            preparing = false;
            if (configDO == null) {
                FileObject fo = findPrimaryConfigurationFO();
                if (fo != null) {
                    configDO = (ConfigDataObject) DataObject.find(fo);
                }
            }
            if (configDO != null) {
                configDO.resetStorage();
            }
            relativePaths = null;
            configurationPrimaryFileName = null;
            
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
    }
    
    /**
     * This method save configurations in deployment plan in content directory
     * and return the fileobject for the plan.  Primary use is for remote deployment
     * or standard jsr88 deployement.
     */
    public File getConfigurationFile() {
        try {
            return getDeploymentPlanFileForDistribution();
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
        return null;
    }

    public File getDeploymentPlanFileForDistribution() throws IOException, ConfigurationException {
        if (getServer() == null) {
            String msg = NbBundle.getMessage(ConfigSupportImpl.class, "MSG_NoTargetSelected");
            throw new ConfigurationException(msg);
        }
        
        FileLock lock = null;
        OutputStream out = null;
        try {
            FileObject dist = getProvider().getJ2eeModule().getContentDirectory();
            String planName = getStandardDeploymentPlanName();
            FileObject plan = dist.getFileObject(planName);
            if (plan == null) {
                plan = dist.createData(planName);
            }
            lock = plan.lock();
            out = plan.getOutputStream(lock);
            getStorage().getDeploymentConfiguration().save(out);
            return FileUtil.toFile(plan);
        } finally {
            if (lock != null) lock.releaseLock();
            try {
                if (out != null) out.close();
            } catch(IOException ioe) {
                ErrorManager.getDefault().log(ioe.toString());
            }
        }
    }
    
    private String getPrimaryConfigurationFileName() {
        getRelativePaths();
        
        if (configurationPrimaryFileName == null)
            return getStandardDeploymentPlanName();
        else
            return configurationPrimaryFileName;
    }

    private String getStandardDeploymentPlanName() {
        return ConfigDataLoader.getStandardDeploymentPlanName(getServer());
    }

    private FileObject findPrimaryConfigurationFO() throws IOException {
        String configFileName = getPrimaryConfigurationFileName();
        return getProvider().findDeploymentConfigurationFile(configFileName);
    }

    /** Creates the cache if it does not exist for the selected server */
    public ConfigurationStorage getStorage() {
        if (getServer() == null) {
            return null;
        }
        FileLock lock = null;
        OutputStream out = null;
        try {
            if (configDO != null) {
                preparing = true;
                return (ConfigurationStorage) configDO.getCookie(ConfigurationStorage.class);
            }
        
            FileObject primary = findPrimaryConfigurationFO();
            if (primary == null) {
                ServerInstance instance = ServerRegistry.getInstance ().getServerInstance (getProvider ().getServerInstanceID ());
                ModuleDeploymentSupport mds = new ModuleDeploymentSupport(getProvider());
                DeploymentConfiguration config;
                if(instance != null) {
                    config = instance.getDeploymentManagerForConfiguration().createConfiguration(mds.getDeployableObject());
                } else {
                    config = getServer ().getDeploymentManager().createConfiguration(mds.getDeployableObject());
                }
                config.getDConfigBeanRoot(mds.getDeployableObject().getDDBeanRoot());
                if (!hasCustomSupport()) {
                    //standard configuration
                    primary.lock();
                    out = primary.getOutputStream(lock);
                    config.save(out);
                } else {
                    //server sepecific files
                    String[] fnames = getDeploymentConfigurationFileNames();
                    File[] files = new File[fnames.length];
                    for (int i = 0; i < fnames.length; i++) {
                        FileObject fo = getProvider().findDeploymentConfigurationFile(fnames[i]);
                        if (fo == null) {
                            files[i] = getProvider().getDeploymentConfigurationFile(fnames[i]);
                        } else {
                            files[i] = FileUtil.toFile(fo);
                        }
                        if (files[i].getName().equals(getPrimaryConfigurationFileName()))
                            primary = fo;
                    }
                    getDeploymentPlanSplitter().writeDeploymentPlanFiles(config, mds.getDeployableObject(),  files);
                    // write should create the config files by now, so make sure
                    if (primary == null)
                        primary = FileUtil.toFileObject(files[0]);
                }
            }

            configDO = (ConfigDataObject) DataObject.find(primary);
            return (ConfigurationStorage) configDO.getCookie(ConfigurationStorage.class);

        } catch (Exception ex) {
            String msg = NbBundle.getMessage(ConfigSupportImpl.class, "MSG_ConfigStorageFailed",
            getServer (), getProvider().getJ2eeModule());
            ErrorManager.getDefault().log(ErrorManager.ERROR, ex.getMessage());
            StackTraceElement[] stes = ex.getStackTrace();
            if (stes != null && stes.length > 0)
                ErrorManager.getDefault().log(ErrorManager.ERROR, stes[0].toString());
        } finally {
            if (lock != null) lock.releaseLock();
            try {
                if (out != null) out.close();
            } catch(IOException ioe) {
                ErrorManager.getDefault().log(ioe.toString());
            }
            preparing = false;
        }
        return null;
    }    

    private DeploymentPlanSplitter getDeploymentPlanSplitter() {
        return getServer ().getDeploymentPlanSplitter();
    }
    private ModuleType getModuleType() {
        return (ModuleType) getProvider().getJ2eeModule().getModuleType();
    }
    private boolean hasCustomSupport() {
        return hasCustomSupport(getServer(), getModuleType());
    }
    private static boolean hasCustomSupport(Server server, ModuleType type) {
        if (server == null || server.getDeploymentPlanSplitter() == null)
            return false;
        return server.getDeploymentPlanFiles(type) != null;
    }

    private J2eeModuleProvider getProvider () {
        return provider;
    }
    
    private Map getRelativePaths() {
        if (relativePaths != null) 
            return relativePaths;
        
        relativePaths = new HashMap();
        if (hasCustomSupport()) {
            String [] paths = getServer().getDeploymentPlanFiles(getModuleType());
            configurationPrimaryFileName = paths[0].substring(paths[0].lastIndexOf("/")+1);
        
            collectData(getServer(), relativePaths);
        }
        
        return relativePaths;
    }
    
    private void collectData(Server server, Map map) {
        if (! this.hasCustomSupport(server, getModuleType()))
            return;
        
        String [] paths = server.getDeploymentPlanFiles(getModuleType());
        paths = (paths == null) ? new String[0] : paths;
        for (int i=0; i<paths.length; i++) {
            String name = paths[i].substring(paths[i].lastIndexOf("/")+1);
            map.put(name, paths[i]);
        }        
    }
    
    private Map getAllRelativePaths() {
        if (allRelativePaths != null)
            return allRelativePaths;
        
        allRelativePaths = new HashMap();
        Collection servers = ServerRegistry.getInstance().getServers();
        for (Iterator i=servers.iterator(); i.hasNext();) {
            Server server = (Server) i.next();
            collectData(server, allRelativePaths);
        }
        return allRelativePaths;
    }
    
    public String[] getDeploymentConfigurationFileNames() {
        if (hasCustomSupport()) {
            return (String[]) getRelativePaths().keySet().toArray(new String[relativePaths.size()]);
        }
        return new String[] { getStandardDeploymentPlanName() };
    }
    
    public String[] getAllDeploymentConfigurationFileNames() {
            return (String[]) getAllRelativePaths().keySet().toArray(new String[relativePaths.size()]);
    }
    
    public String getContentRelativePath(String configName) {
        if (! hasCustomSupport()) {
            return configName; //just return the name so that the .dpf file is writen at the root of dist directory.
        }
        return (String) getAllRelativePaths().get(configName);
    }
    
    public static File[] getDeploymentConfigurationFiles (J2eeModuleProvider provider, Server server) {
        return getDeploymentConfigurationFiles(provider, server, false);
    }
    
    public static File[] getDeploymentConfigurationFiles (J2eeModuleProvider provider, Server server, boolean existingOnly) {
        if (provider == null || server == null)
            return new File[0];
        
        ModuleType type = (ModuleType) provider.getJ2eeModule().getModuleType();
        DeploymentPlanSplitter dps = server.getDeploymentPlanSplitter();
        String[] fnames;
        if (hasCustomSupport(server, type)) {
            fnames = server.getDeploymentPlanFiles(type);
        } else if (server.supportsModuleType(type)) {
            fnames = new String[] { ConfigDataLoader.getStandardDeploymentPlanName(server) };
        } else {
            return EMPTY_FILE_LIST;
        }
        
        ArrayList files = new ArrayList();
        for (int i = 0; i < fnames.length; i++) {
            File path = new File(fnames[i]);
            String fname = path.getName();
            File file = null;
            if (existingOnly) {
                FileObject fo = provider.findDeploymentConfigurationFile(fname);
                if (fo != null) {
                    file = FileUtil.toFile(fo);
                }
            } else {
                file = provider.getDeploymentConfigurationFile(fname);
            }
            
            if (file != null) {
                files.add(file);
            }
        }
        return (File[])files.toArray(new File[files.size()]);
    }
    
    public static FileObject[] getConfigurationFiles(J2eeModuleProvider jmp) {
        Collection servers = ServerRegistry.getInstance().getServers();
        ArrayList files = new ArrayList();
        for (Iterator i=servers.iterator(); i.hasNext();) {
            Server s  = (Server) i.next();
            File[] configs = getDeploymentConfigurationFiles(jmp, s, true);
            for (int j=0; j<configs.length; j++) {
                files.add(FileUtil.toFileObject(configs[j]));
            }
        }
        return (FileObject[]) files.toArray(new FileObject[files.size()]);
    }

    public static void createInitialConfiguration(J2eeModuleProvider provider, ServerString server) {
        try {
            File[] files = getDeploymentConfigurationFiles(provider, server.getServer());
            if (files != null && files.length > 0 && ! files[0].isFile()) {
                new ConfigurationStorage(provider, server);
            }
        } catch (Exception e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
        }
    }
    
    public String getDeploymentName() {
        try {
            FileObject fo = getProvider().getJ2eeModule().getContentDirectory();
            if (fo == null) {
                fo = findPrimaryConfigurationFO();
            }
            if (fo == null)
                return null;
            Project owner = FileOwnerQuery.getOwner(fo);
            if (owner != null)
                return owner.getProjectDirectory().getName();
            
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,  ioe);
        }
        return null;
    }
    
    public void setCMPMappingInfo(final OriginalCMPMapping[] mappings) {
        ConfigurationStorage cs = getStorage();
        if (cs == null)
            return;
        DeploymentConfiguration config = cs.getDeploymentConfiguration();
        ConfigurationSupport serverConfig = getServer().geConfigurationSupport();
        serverConfig.setMappingInfo(config, mappings);
        saveConfiguration();
    }
    
    public void ensureResourceDefinedForEjb(final String ejbname, final String ejbtype) {
        ConfigurationStorage cs = getStorage();
        if (cs == null)
            return;
        DConfigBean ejb = null;
        DDRoot ddroot = cs.getEjbJarRoot();
        StandardDDImpl[] ddbeans = (StandardDDImpl[]) ddroot.getChildBean("/enterprise-beans/"+ejbtype); //NOI18N
        for (int i=0; i<ddbeans.length; i++) {
            String ejbName = (String) ddbeans[i].proxy.bean.getValue("EjbName"); //NOI18N
            if (ejbname.equals(ejbName)) {
                ConfigBeanStorage[] cbss = ddbeans[i].getConfigBeans();
                if (cbss != null && cbss.length > 0) {
                    ejb = cbss[0].getConfigBean();
                    break;
                }
            }
        }
        if (ejb == null) {
            if (ddbeans != null) {
                for (int i=0; i<ddbeans.length; i++) {
                    String msg = ddbeans[i].proxy.bean.dumpBeanNode();
                    ErrorManager.getDefault().log(ErrorManager.ERROR, msg);
                }
            }
            Exception e = new Exception("Failed to lookup: "+ejbname+" type "+ejbtype);
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            return;
        }
        DeploymentConfiguration config = cs.getDeploymentConfiguration();
        ConfigurationSupport serverConfig = getServer().geConfigurationSupport();
        serverConfig.ensureResourceDefined(config, ejb, provider.getEnterpriseResourceDirectory());
        saveConfiguration();
    }
    
    public void saveConfiguration() {
        ConfigurationStorage cs = getStorage();
        if (cs != null) {
            cs.autoSave();
        }
    }
}
