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
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.deployment.impl.ServerString;
import org.netbeans.modules.j2ee.deployment.impl.Server;
import javax.enterprise.deploy.spi.DeploymentConfiguration;
import javax.enterprise.deploy.spi.exceptions.ConfigurationException;
import javax.enterprise.deploy.model.*;
import javax.enterprise.deploy.shared.ModuleType;
import javax.enterprise.deploy.spi.*;
import org.netbeans.modules.j2ee.deployment.config.ConfigurationStorage;
import org.netbeans.modules.j2ee.deployment.config.ModuleDeploymentSupport;
import org.netbeans.modules.j2ee.deployment.config.ui.ConfigUtils;
import org.netbeans.modules.j2ee.deployment.impl.ServerInstance;
import org.netbeans.modules.j2ee.deployment.impl.ServerRegistry;
import org.netbeans.modules.j2ee.deployment.impl.gen.nbd.WebContextRoot;
import org.netbeans.modules.j2ee.deployment.plugins.api.DeploymentPlanSplitter;
import org.openide.ErrorManager;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataFolder;
import org.openide.util.NbBundle;

/*
 * @author  nn136682
 */
public final class ConfigSupportImpl implements J2eeModuleProvider.ConfigSupport {
    private J2eeModuleProvider provider;
    private String webContextRootXpath;
    private String webContextRootPropName;
    private Server fakeServer = null;
    private java.util.Map relativePaths = null;
    private String configurationPrimaryFileName = null;
    private Map allRelativePaths = null;
    
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
    
    private Server getServer () {
        if (fakeServer != null) {
            return fakeServer;
        }
        Server s = ServerRegistry.getInstance ().getServer (getProvider ().getServerID ());
        if (s == null) {
            //PENDING some ntoifcation.
            s = ServerRegistry.getInstance().getDefaultInstance().getServer();
        }
        return s;
    }
    
    private DConfigBean getWebContextDConfigBean() {
        try {
            DeploymentConfiguration dc = getStorage().getDeploymentConfiguration();
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
                getStorage();
                return true;
            }
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ioe);
        }
        return false;
    }
     
    /**
     * Get context root
     * @return string value, null if not set or could not find
     */
    public String getWebContextRoot(Server server) {
        fakeServer = server;
        refresh ();
        if (webContextRootXpath == null || webContextRootPropName == null) {
            ErrorManager.getDefault ().log ("Cannot access configuration for server:"+server);
            fakeServer = null;
            return null;
        }

        DConfigBean configBean = getWebContextDConfigBean();
        fakeServer = null;
        if (configBean == null) {
            ErrorManager.getDefault ().log ("ConfigBean for "+webContextRootXpath+" not found"); //NOI18N
            return null;
        }
        return (String) ConfigUtils.getBeanPropertyValue(configBean, webContextRootPropName);
    }
    
    /**
     * Get context root
     * @return string value, null if not set or could not find
     */
    public String getWebContextRoot() {
        refresh ();
        if (webContextRootXpath == null || webContextRootPropName == null)
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
        refresh ();
        if (webContextRootXpath == null || webContextRootPropName == null)
            return;

        DConfigBean configBean = getWebContextDConfigBean();
        if (configBean == null) {
            ErrorManager.getDefault ().log ("ConfigBean for "+webContextRootXpath+":"+webContextRootPropName+" not found"); //NOI18N
            return;
        }
        ConfigUtils.setBeanPropertyValue(configBean, webContextRootPropName, contextRoot);
        try {
            getStorage ().save ();
        } catch (java.io.IOException e) {
            ErrorManager.getDefault ().notify (e);
        }
    }
    
    public void resetStorage() {
        try {
            FileObject fo = findPrimaryConfigurationFO();
            if (fo != null) {
                DataObject dobj = DataObject.find(fo);
                if (dobj instanceof ConfigDataObject) {
                    ConfigDataObject cdo = (ConfigDataObject) dobj;
                    cdo.resetStorage();
                }
            }
            
            relativePaths = null;
            configurationPrimaryFileName = null;
            
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
    }
    
    //from J2eeDeploymentLookup:
    /**
     * Note this call is temporary for backward compatibility.  Do not use.
     */
    public FileObject findConfigurationFO(String name) {
        if (name == null)
            return null;
        
        FileObject moduleFolder = getProvider().getModuleFolder();

        // should not happen, new project should have ovrridden this method to return non-null.
        if (moduleFolder == null) {
            throw new IllegalStateException("New J2eeProviderImplementation needs to override this method!"); //NOI18N
        }
        
        if (getProvider().useDirectoryPath()) {
            name = getContentRelativePath(name);
            if (name == null)
                return null;
        }
        return moduleFolder.getFileObject(name);
    }
    /**
     * Note this call is temporary for backward compatibility.  Do not use.
     */
    public FileObject getConfigurationFO(String name) throws IOException {
        FileObject moduleFolder = getProvider().getModuleFolder();

        // should not happen, new project should have ovrridden this method to return non-null.
        if (moduleFolder == null) {
            throw new IllegalStateException("New J2eeProviderImplementation needs to override this method!"); //NOI18N
        }
        
        if (getProvider().useDirectoryPath()) {
            name = getContentRelativePath(name);
        }
        FileObject configFO = (name == null) ? null : moduleFolder.getFileObject(name);
        if (configFO == null)
            configFO = FileUtil.createData(moduleFolder, name);
        return configFO;
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
        FileLock lock = null;
        OutputStream out = null;
        try {
            FileObject primary = findPrimaryConfigurationFO();
            if (primary == null) {
                ServerInstance instance = ServerRegistry.getInstance ().getServerInstance (getProvider ().getServerInstanceID ());
                ModuleDeploymentSupport mds = new ModuleDeploymentSupport(getProvider().getJ2eeModule());
                DeploymentConfiguration config;
                if(instance != null && fakeServer == null) {
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
                            fo = getProvider().getDeploymentConfigurationFile(fnames[i]);
                        }
                        files[i] = FileUtil.toFile(fo);
                        if (files[i].getName().equals(getPrimaryConfigurationFileName()))
                            primary = fo;
                    }
                    getDeploymentPlanSplitter().writeDeploymentPlanFiles(config, mds.getDeployableObject(),  files);
                }
            }
            DataFolder folder = DataFolder.findFolder(primary.getParent());
            DataObject dobj = folder.find(primary);
            // dobj = DataObject.find(primary);
            return (ConfigurationStorage) dobj.getCookie(ConfigurationStorage.class);

        } catch (Exception ex) {
            String msg = NbBundle.getMessage(ConfigSupportImpl.class, "MSG_ConfigStorageFailed",
            getServer (), getProvider().getJ2eeModule());
            ErrorManager.getDefault().annotate(ex, msg);
            ErrorManager.getDefault().notify(ex);
        } finally {
            if (lock != null) lock.releaseLock();
            try {
                if (out != null) out.close();
            } catch(IOException ioe) {
                ErrorManager.getDefault().log(ioe.toString());
            }
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
        return hasCustomSupport(getDeploymentPlanSplitter(), getModuleType());
    }
    private boolean hasCustomSupport(DeploymentPlanSplitter dps, ModuleType type) {
        if(dps == null) return false;
        return dps.getDeploymentPlanFileNames(type) != null;
    }

    private J2eeModuleProvider getProvider () {
        return provider;
    }
    
    private Map getRelativePaths() {
        if (relativePaths != null) 
            return relativePaths;
        
        relativePaths = new HashMap();
        if (hasCustomSupport()) {
            String [] paths = getDeploymentPlanSplitter().getDeploymentPlanFileNames(getModuleType());
            configurationPrimaryFileName = paths[0].substring(paths[0].lastIndexOf("/")+1);
        
            collectData(getServer(), relativePaths);
        }
        
        return relativePaths;
    }
    
    private void collectData(Server server, Map map) {
        if (! this.hasCustomSupport(getDeploymentPlanSplitter(), getModuleType()))
            return;
        
        String [] paths = server.getDeploymentPlanSplitter().getDeploymentPlanFileNames(getModuleType());
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
    
    
}
