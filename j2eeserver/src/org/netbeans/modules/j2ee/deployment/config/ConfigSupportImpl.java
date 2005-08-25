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
import java.util.Map.Entry;
import java.util.Set;
import org.netbeans.modules.j2ee.deployment.common.api.OriginalCMPMapping;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.deployment.impl.Server;
import javax.enterprise.deploy.spi.exceptions.ConfigurationException;
import javax.enterprise.deploy.model.*;
import javax.enterprise.deploy.shared.ModuleType;
import javax.enterprise.deploy.spi.*;
import javax.enterprise.deploy.spi.exceptions.InvalidModuleException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.impl.ServerInstance;
import org.netbeans.modules.j2ee.deployment.impl.ServerRegistry;
import org.netbeans.modules.j2ee.deployment.plugins.api.ConfigurationSupport;
import org.openide.ErrorManager;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.execution.DeploymentConfigurationProvider;

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

public final class ConfigSupportImpl implements J2eeModuleProvider.ConfigSupport, 
        DeploymentConfigurationProvider {
    
    private static final File[] EMPTY_FILE_LIST = new File[0];
    private static final String GENERIC_EXTENSION = ".dpf"; // NOI18N
    
    private String configurationPrimaryFileName = null;
    private Map relativePaths = null;
    private Map allRelativePaths = null;
    
    private J2eeModuleProvider provider;
    private ModuleDeploymentSupport mds;
    private DeploymentConfiguration deploymentConfiguration;
    
    private Server server;
    private ServerInstance instance;
    
    /** Creates a new instance of ConfigSupportImpl */
    public ConfigSupportImpl (J2eeModuleProvider provider) {
        this.provider = provider;
        mds = new ModuleDeploymentSupport(provider, true);
        instance = ServerRegistry.getInstance().getServerInstance(provider.getServerInstanceID());
        server = instance != null 
                ? instance.getServer() 
                : ServerRegistry.getInstance().getServer(provider.getServerID());
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
    
    /** Return an initiliazed deployment configuration */
    public void createDeploymentConfiguration(Server server) {
        if (server == this.server) {
            createInitialConfiguration();
        } else {
            ModuleDeploymentSupport mds = new ModuleDeploymentSupport(provider, false);
            DeployableObject dobj = mds.getDeployableObject();
            try {
                DeploymentConfiguration deployConf = server.getDisconnectedDeploymentManager().createConfiguration(dobj);
                ConfigurationSupport serverConfig = server.getConfigurationSupport();
                File[] files = getDeploymentConfigurationFiles(getProvider(), server);
                serverConfig.initConfiguration(deployConf, files, getProvider().getEnterpriseResourceDirectory(), false);
            } catch(InvalidModuleException ime) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ime);
            } catch (ConfigurationException ce) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ce);
            }
        }
    }
    
    /**
     * Return list of server specific configuration files.
     */
    public static File[] getDeploymentConfigurationFiles (J2eeModuleProvider provider, Server server) {
        return getDeploymentConfigurationFiles(provider, server, false);
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
    
    /** dispose all created deployment configurations */
    public void dispose() {
        ConfigurationSupport serverConfig = server.getConfigurationSupport();
        if (deploymentConfiguration != null && serverConfig != null) {
            serverConfig.disposeConfiguration(deploymentConfiguration);
        }
        if (mds != null) {
            mds.cleanup();
        }
    }
    
    // J2eeModuleProvider.ConfigSupport ---------------------------------------
    
    public boolean createInitialConfiguration() {
        return getDeploymentConfiguration() != null;
    }
    
    public boolean ensureConfigurationReady() {
        return getDeploymentConfiguration() != null;
    }
     
    /**
     * Get context root (context path)
     *
     * @return string value, null if not set or not a WAR module
     */
    public String getWebContextRoot() {
        if (!getProvider().getJ2eeModule().getModuleType().equals(J2eeModule.WAR)) {
            ErrorManager.getDefault().log("getWebContextRoot called on other module type then WAR"); //NOI18N
            return null;
        }
        
        DeploymentConfiguration config = getDeploymentConfiguration();
        if (config == null || server == null) {
            return null;
        }
        ConfigurationSupport serverConfig = server.getConfigurationSupport();
        try {
            return serverConfig.getWebContextRoot(config, config.getDeployableObject());
        } catch (ConfigurationException ce) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ce);
            return null;
        }
    }
    
    /**
     * Set context root (context path)
     */
    public void setWebContextRoot(String contextRoot) {
        if (!getProvider().getJ2eeModule().getModuleType().equals(J2eeModule.WAR)) {
            ErrorManager.getDefault().log("setWebContextRoot called on other module type then WAR"); //NOI18N
            return;
        }
        DeploymentConfiguration config = getDeploymentConfiguration();
        if (config == null || server == null) {
            return;
        }
        ConfigurationSupport serverConfig = server.getConfigurationSupport();
        try {
            serverConfig.setWebContextRoot(config, config.getDeployableObject(), contextRoot);
        } catch (ConfigurationException ce) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ce);
        }
    }
        
    public String[] getDeploymentConfigurationFileNames() {
        if (hasCustomSupport()) {
            return (String[]) getRelativePaths().keySet().toArray(new String[relativePaths.size()]);
        }
        return new String[] { getStandardDeploymentPlanName() };
    }
    
    public String getContentRelativePath(String configName) {
        if (! hasCustomSupport()) {
            return configName; //just return the name so that the .dpf file is writen at the root of dist directory.
        }
        return (String) getAllRelativePaths().get(configName);
    }
    
    public void setCMPMappingInfo(final OriginalCMPMapping[] mappings) {
        DeploymentConfiguration config = getDeploymentConfiguration();
        ConfigurationSupport serverConfig = server.getConfigurationSupport();
        serverConfig.setMappingInfo(config, mappings);
    }
    
    public void ensureResourceDefinedForEjb(final String ejbname, final String ejbtype) {
        if (! J2eeModule.EJB.equals(provider.getJ2eeModule().getModuleType())) {
            throw new IllegalArgumentException("Trying to get config bean for ejb on non ejb module!"); //NONI18N
        }
        DDBean ejbBean = null;
        DDRoot ddroot = mds.getDDBeanRoot(J2eeModule.EJBJAR_XML);
        StandardDDImpl[] ddbeans = (StandardDDImpl[]) ddroot.getChildBean(
                "/enterprise-beans/" + ejbtype); //NOI18N
        for (int i=0; i<ddbeans.length; i++) {
            String ejbName = (String) ddbeans[i].proxy.bean.getValue("EjbName"); //NOI18N
            if (ejbname.equals(ejbName)) {
                ejbBean = ddbeans[i];
                break;
            }
        }
        if (ejbBean == null) {
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
        DeploymentConfiguration config = getDeploymentConfiguration();
        ConfigurationSupport serverConfig = server.getConfigurationSupport();
        serverConfig.ensureResourceDefined(config, ejbBean);
    }
    
    // DeploymentConfigurationProvider implementation -------------------------
    
    /**
     * Create and cache deployment configuration for the current server.
     */
    public DeploymentConfiguration getDeploymentConfiguration() {
        if (deploymentConfiguration == null) {
            DeployableObject dobj = mds.getDeployableObject();
            try {
                if (instance != null) {
                    deploymentConfiguration = instance.getDeploymentManagerForConfiguration().createConfiguration(dobj);
                } else {
                    deploymentConfiguration = server.getDisconnectedDeploymentManager().createConfiguration(dobj);
                }
                ConfigurationSupport serverConfig = server.getConfigurationSupport();
                File[] files = getDeploymentConfigurationFiles(getProvider(), server);
                serverConfig.initConfiguration(deploymentConfiguration, files, 
                        getProvider().getEnterpriseResourceDirectory(), true);
            } catch(InvalidModuleException ime) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ime);
                return null;
            } catch (ConfigurationException ce) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ce);
                return null;
            }
        }
        return deploymentConfiguration;
    }
        
    public DeployableObject getDeployableObject(String moduleUri) {
        DeployableObject deplObj = mds.getDeployableObject();
        if (deplObj instanceof J2eeApplicationObject) {
            return ((J2eeApplicationObject)deplObj).getDeployableObject(moduleUri);
        }
        return mds.getDeployableObject();
    }
    
    // private helpers --------------------------------------------------------
    
    /**
     * Return list of server specific configuration files.
     */
    private static File[] getDeploymentConfigurationFiles (J2eeModuleProvider provider, Server server, boolean existingOnly) {
        if (provider == null || server == null)
            return new File[0];
        
        ModuleType type = (ModuleType) provider.getJ2eeModule().getModuleType();
        String[] fnames;
        if (hasCustomSupport(server, type)) {
            fnames = server.getDeploymentPlanFiles(type);
        } else if (server.supportsModuleType(type)) {
            fnames = new String[] { getStandardDeploymentPlanName(server) };
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
    
    /**
     * Creates and returns the JSR-88 deployment plan file for the current 
     * deployment configuration.
     *
     * @return deployment plan file.
     */
    private File getDeploymentPlanFileForDistribution() throws IOException, ConfigurationException {
        if (server == null) {
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
            DeploymentConfiguration conf = getDeploymentConfiguration();
            if (conf != null) {
                conf.save(out);
                return FileUtil.toFile(plan);
            }
            return null;
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
        return getStandardDeploymentPlanName(server);

    }
    
    private static String getStandardDeploymentPlanName(Server server) {
        return server.getShortName() + GENERIC_EXTENSION;
    }

    private FileObject findPrimaryConfigurationFO() throws IOException {
        String configFileName = getPrimaryConfigurationFileName();
        return getProvider().findDeploymentConfigurationFile(configFileName);
    }   

    private ModuleType getModuleType() {
        return (ModuleType) getProvider().getJ2eeModule().getModuleType();
    }
    
    private boolean hasCustomSupport() {
        return hasCustomSupport(server, getModuleType());
    }
    
    private static boolean hasCustomSupport(Server server, ModuleType type) {
        if (server == null || server.getConfigurationSupport() == null) {
            return false;
        }
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
            String [] paths = server.getDeploymentPlanFiles(getModuleType());
            configurationPrimaryFileName = paths[0].substring(paths[0].lastIndexOf("/")+1);
        
            collectData(server, relativePaths);
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
}
