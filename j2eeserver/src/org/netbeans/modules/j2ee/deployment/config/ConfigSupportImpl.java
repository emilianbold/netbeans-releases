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
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.deployment.impl.ServerString;
import org.netbeans.modules.j2ee.deployment.impl.Server;
import javax.enterprise.deploy.spi.DeploymentConfiguration;
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
import org.openide.util.NbBundle;

/*
 * @author  nn136682
 */
public final class ConfigSupportImpl implements J2eeModuleProvider.ConfigSupport {
    private J2eeModuleProvider provider;
    private String webContextRootXpath;
    private String webContextRootPropName;
    private ConfigurationStorage storage;
    private Server fakeServer = null;
    
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
        return ServerRegistry.getInstance ().getServer (getProvider ().getServerID ());
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
                ErrorManager.getDefault ().log (ErrorManager.EXCEPTION, "DDBeans not found");
                return null; //better than throw exception
            }
            return configBeanRoot.getDConfigBean(ddBeans[0]);
            
        } catch (Exception e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
        }
        return null;
    }
    
    public boolean createInitialConfiguration() {
        boolean existing = false;
        File f = getConfigurationFile();
        if (f.isFile()) {
            existing = true;
        }
        getStorage();
        return existing;
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
    
    public void resetStorage () {
        storage = null;
        getStorage ();
    }
    //from J2eeDeploymentLookup:
    
    //PENDIND should not be public!!!
    public File getConfigurationFile() {
        String fname = getConfigurationPrimaryFileName();
        File savefile = new File(FileUtil.toFile(getProvider().getModuleFolder()).getAbsolutePath()
        + System.getProperty("file.separator") + fname);
        // should never happen because there should be alway a default server
        if (savefile == null) {
            String msg = NbBundle.getMessage(ConfigSupportImpl.class, "MSG_NoTargetServer");
            ErrorManager.getDefault().log(ErrorManager.USER, msg);
        }
        return savefile;
    }
    
    private String getConfigurationPrimaryFileName() {
        ModuleType moduleType = (ModuleType) getProvider().getJ2eeModule().getModuleType();
        DeploymentPlanSplitter dps = getServer ().getDeploymentPlanSplitter();
        String fname = null;
        if (dps != null) {
            String[] fnames = dps.getDeploymentPlanFileNames(moduleType);
            if (fnames != null && fnames.length > 0)
                fname = fnames [0];
        }
        if (fname == null) {
            fname = getServer ().getShortName() + ".dpf";
        }
        return fname;
    }
    
    private FileObject getPrimaryConfigurationFO() {
        FileObject moduleFolder = getProvider().getModuleFolder();
        String configFileName = getConfigurationPrimaryFileName();
        FileObject configFO = moduleFolder.getFileObject(configFileName);
        return configFO;
    }
    
    private FileObject createPrimaryConfigurationFO() throws IOException {
        FileObject moduleFolder = getProvider().getModuleFolder();
        return FileUtil.createData(moduleFolder, getConfigurationPrimaryFileName());
    }
    
    /** Creates the cache if it does not exist for the selected server */
    //PENDIND should not be public!!!
    public ConfigurationStorage getStorage() {
        storage = null; //do not use cache
        
        FileLock lock = null;
        OutputStream out = null;
        try {
            File f = getConfigurationFile();
            FileObject fo = null;
            if (f.isFile()) {
                fo = getPrimaryConfigurationFO();
                if (fo == null) {
                    //this is a boundary condition that can be met when web
                    //module is being unmounted and this case it should not
                    //cause any problems
                    ErrorManager.getDefault().log(ErrorManager.ERROR,
                    NbBundle.getMessage(ConfigSupportImpl.class, "MSG_NoConfigurationFO", f));
                    return null;
                }
            } else {
                ServerInstance instance = ServerRegistry.getInstance ().getServerInstance (getProvider ().getServerInstanceID ());
                ModuleDeploymentSupport mds = new ModuleDeploymentSupport(getProvider().getJ2eeModule());
                DeploymentConfiguration config;
                if(instance != null && fakeServer == null) {
                    config = instance.getDeploymentManagerForConfiguration().createConfiguration(mds.getDeployableObject());
                } else {
                    config = getServer ().getDeploymentManager().createConfiguration(mds.getDeployableObject());
                }
                config.getDConfigBeanRoot(mds.getDeployableObject().getDDBeanRoot());
                DeploymentPlanSplitter dps = getServer ().getDeploymentPlanSplitter();
                if (dps == null || !hasCustomSupport(dps,mds.getType())) {
                    //standard configuration
                    fo = createPrimaryConfigurationFO();
                    fo.lock();
                    out = fo.getOutputStream(lock);
                    config.save(out);
                } else {
                    //server sepecific files
                    String fnames[] = dps.getDeploymentPlanFileNames(mds.getType());
                    File files[] = new File [fnames.length];
                    FileObject moduleFolder =  getProvider().getModuleFolder();
                    FileObject[] fileObjs = new FileObject[fnames.length];
                    for (int i = 0; i < files.length; i++) {
                        fileObjs[i] = FileUtil.createData(moduleFolder, fnames [i]);
                        files [i] = FileUtil.toFile(fileObjs[i]);
                    }
                    fo = fileObjs[0];
                    dps.writeDeploymentPlanFiles(config, mds.getDeployableObject(),  files);
                }
            }
            
            if (fo != null) {
                DataObject dobj = DataObject.find(fo);
                storage = (ConfigurationStorage) dobj.getCookie(ConfigurationStorage.class);
            }
            
        } catch (Exception ex) {
            String msg = NbBundle.getMessage(ConfigSupportImpl.class, "MSG_ConfigStorageFailed",
            getServer (), getProvider().getModuleFolder());
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
        
        if (storage == null) {
            String msg = NbBundle.getMessage(ConfigSupportImpl.class, "MSG_ConfigStorageFailed",
            getServer (), getProvider().getModuleFolder());
            throw new RuntimeException(msg);
        }
        return storage;
    }
    
    private boolean hasCustomSupport(DeploymentPlanSplitter dps, ModuleType type) {
        if(dps == null) return false;
        return dps.getDeploymentPlanFileNames(type) != null;
    }

    private J2eeModuleProvider getProvider () {
        return provider;
    }
}
