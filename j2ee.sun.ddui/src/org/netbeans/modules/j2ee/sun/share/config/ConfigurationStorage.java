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


package org.netbeans.modules.j2ee.sun.share.config;

import java.io.*;
import java.util.*;

import javax.swing.SwingUtilities;
import javax.enterprise.deploy.model.*;
import javax.enterprise.deploy.spi.*;
import javax.enterprise.deploy.spi.exceptions.*;
import org.netbeans.modules.j2ee.sun.share.configbean.SunONEDeploymentConfiguration;

import org.openide.*;
import org.openide.NotifyDescriptor.Confirmation;
import org.openide.filesystems.*;
import org.openide.nodes.*;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;

import org.xml.sax.SAXException;

import org.netbeans.modules.j2ee.deployment.devmodules.api.*;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.*;
import org.netbeans.modules.j2ee.deployment.plugins.api.*;

import org.netbeans.modules.j2ee.sun.share.config.ModuleDDSupport;
import org.netbeans.modules.j2ee.sun.share.config.ui.ConfigBeanTopComponent;
import org.netbeans.modules.j2ee.sun.share.configbean.SunONEDeploymentConfiguration;


/* ConfigurationStorage stores a DeploymentConfiguration.  It is responsible
 * for creating the DeployableObjects and the DConfigBeanRoots and tracking
 * changes at the application level.
 */
public class ConfigurationStorage implements /* !PW Removed DeploymentConfigurationProvider,*/ /*ModuleListener,*/ Node.Cookie {
    
    public static final String ROOT = "/"; // NOI18N
    private DeploymentConfiguration config;
    // Map of url -> ModuleDeploymentSupport
    Map moduleMap = new HashMap();
    final Map versionListeners = new HashMap();
    ConfigurationSaver saver;
    private boolean needsSave = false;
    final J2eeModuleProvider module;
    boolean loaded = false;
    private Task autoSaveTask;
    private boolean saveInProgress;
    private boolean saveFailedDialogDisplayed;
    
    public ConfigurationStorage(J2eeModuleProvider module, DeploymentConfiguration config) throws ConfigurationException, InvalidModuleException, IOException, SAXException {
        this.module = module;
        this.config = config;
        
        load(); // calls init(), below.
        createVersionListeners();
    }
    
    private void init() throws ConfigurationException, InvalidModuleException, IOException {
        // do the clean up first
        for (Iterator i = moduleMap.values().iterator(); i.hasNext();) {
            ((ModuleDDSupport)i.next()).cleanup();
        }
        moduleMap.clear();
        
        // create all MDS/ADS classes here.  MDS's should be GC'ed if the DC changes.
        ModuleDDSupport mds = new ModuleDDSupport(module, config);
        moduleMap.put(ROOT,mds);
        /*if(module.getJ2eeModule().getModuleType().equals(J2eeModule.EAR)) {
            J2eeAppProvider appProvider = (J2eeAppProvider) module;
            J2eeModuleContainer container = (J2eeModuleContainer) module.getJ2eeModule();
            dobj = new ApplImpl(mds,moduleMap);
            J2eeModuleProvider[] modules = appProvider.getChildModuleProviders();
            container.addModuleListener(this);
            for(int i = 0; i < modules.length; i++) {
                moduleMap.put(modules[i].getJ2eeModule().getUrl(),new ModuleDeploymentSupport(modules[i]));
            }
        }*/
    }

    /** Normalizes a DDBeanRoot from j2eeserver module into the matching DDRoot
     *  that is managed by our ModuleDDSupport.
     */
    public DDBeanRoot normalizeDDBeanRoot(DDBeanRoot ddBeanRoot) {
        DDBeanRoot ddNew = ddBeanRoot;

        // !PW FIXME enhance this algorithm to handle multiple modules if this class does.
        // Until then, there is only one entry in the table, under ROOT key.
        ModuleDDSupport mds = (ModuleDDSupport) moduleMap.get(ROOT);
        if(mds != null) {
            DDBeanRoot newRoot = mds.getDDBeanRoot();
            if(newRoot != ddBeanRoot && newRoot != null) {
                ddNew = newRoot;

                // Not sure if we need to handle ddbeanroot for webservices.xml, but in case
                // someone passes that one in, it will cause this assert here.
                assert ddBeanRoot.getXpath().equals(ddNew.getXpath()) : "Mismatched xpaths in normalizeDDBeanRoot for " + ddBeanRoot;
            }
        }
        
        return ddNew;
    }

    private boolean ensureLoaded() {
        if(loaded) {
            return true;
        }
        
        try {
            load();
            return true;
        } catch (Exception e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            return false;
        }
    }
    
    public ConfigDataObject getPrimaryDataObject() {
        if (saver instanceof ConfigDataObject) {
            return (ConfigDataObject) saver;
        }
        return null;
    }
    
    public void setSaver(ConfigurationSaver saver)  {
        this.saver = saver;
    }
    
    /**
     * Return comma separeted list of files.
     */
    private String filesToString(File[] files) {
        StringBuffer sb = new StringBuffer();
        if (files.length > 0) {
            sb.append(files[0].getPath());
        }        for (int i = 1; i < files.length; i++) {
            sb.append(", "); // NOI18N
            sb.append(files[i].getPath());
        }
        return sb.toString();
    }
    
    /**
     * Save configuration only if the graphical config editor is not opened, mark as
     * modified otherwise.
     */
    public void autoSave() {
        if (autoSaveTask == null) {
            autoSaveTask = RequestProcessor.getDefault().post(new Runnable() {
                private boolean dialogIsDisplayed;
                public void run() {
                    // TODO should be rewritten - currently needs to be run in 
                    // the event dispatch thread since we are accessing window api,
                    // see also ConfigDataObject.fileChanged
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            ConfigDataObject configDO = getPrimaryDataObject();
                            if (configDO == null || dialogIsDisplayed) {
                                return;
                            }
                            // proceed in auto save only if graphical config editor is not opened
                            if (!configDO.isConfigEditorOpened()) {
                                try {
                                    // if config files are modified, ask whether to rewrite them
                                    if (configDO.areModified()) {
// !PW FIXME                              File files[] = getSaveFiles(module);
                                        String msg = NbBundle.getMessage(ConfigurationStorage.class, 
                                                "MSG_SaveGeneratedChanges", 
                                                "!PW FIXME Unknown SJSAS server", 
// !PW FIXME                                      filesToString(files)
                                                "(!PW FIXME unknown files)");
                                        Confirmation cf = new Confirmation(msg, NotifyDescriptor.YES_NO_OPTION);
                                        dialogIsDisplayed = true;
                                        DialogDisplayer.getDefault().notify(cf);
                                        if (!NotifyDescriptor.YES_OPTION.equals(cf.getValue())) {
                                            return;
                                        }
                                        configDO.removeAllEditorChanges();
                                    }
                                    save();
                                } catch (Exception e) {
                                     // ignore it
                                } finally {
                                    dialogIsDisplayed = false;
                                }
                            } else {
                                configDO.setChanged();
                            }
                        }
                    });
                }
            });
        }
        autoSaveTask.schedule(100);
    }
    
    void setChanged() {
        needsSave = true;
        autoSave();
    }
    
    DeploymentConfiguration getDeploymentConfiguration() {
        return config;
    }
    
    public static J2eeModuleProvider getChildModuleProvider(J2eeModuleProvider jmp, String uri) {
        if (uri == null) {
            return null;
        }
        J2eeModuleProvider child = null;
        if (jmp instanceof J2eeAppProvider) {
            J2eeAppProvider jap = (J2eeAppProvider) jmp;
            child = jap.getChildModuleProvider(uri);
            if (child == null) {
                if (uri.startsWith(ROOT)) {
                    uri = uri.substring(1);
                } else {
                    uri = ROOT + uri;
                }
                child = jap.getChildModuleProvider(uri);
            }
        }
        return child;
    }
    
    // J2eeModule.ModuleListener methods
    /*public void addModule(J2eeModule child) {
        if (child == null || ! ensureLoaded())
            return;
        
        // more tolerant to multiple firing
        if (moduleMap.get(child.getUrl()) != null)
            return;
        
        J2eeModuleProvider jmp = getChildModuleProvider(module, child.getUrl());
        assert (jmp != null);
        ModuleDeploymentSupport mds = new ModuleDeploymentSupport(jmp);
        moduleMap.put(child.getUrl(), mds);
        createVersionListener(child);
        try {
            createDConfigBean(mds);
        } catch (ConfigurationException ce) {
            ErrorManager.getDefault().notify(ErrorManager.EXCEPTION,ce);
            // PENDING what do I do with this error?  need to somehow
            // abort configuration
        }
    }
    
    public void removeModule(J2eeModule child) {
        if (! ensureLoaded())
            return;

        ModuleDeploymentSupport mds = (ModuleDeploymentSupport) moduleMap.get(child.getUrl());
        moduleMap.remove(child.getUrl());
        mds.dispose(config);
        removeVersionListener(child);
    }*/
    
    public Node getMainNode() {
        Node[] nodes = getMainNodes();
        return nodes.length > 0 ? nodes[0] : null;
    }
    
    public Node[] getMainNodes() {
        if (! ensureLoaded()) {
            return new Node[0];
        }
        
        ModuleDDSupport mds = (ModuleDDSupport) moduleMap.get(ROOT);
        return mds == null? new Node[0] : mds.getNodes();
    }
    
    public Node[] getNodes(J2eeModule mod) {
        if (! ensureLoaded()) {
            return new Node[0];
        }
        
        ModuleDDSupport mds = (ModuleDDSupport) moduleMap.get(mod.getUrl());
        /*if (mds == null) {
            addModule(mod);
            mds = (ModuleDeploymentSupport) moduleMap.get(mod.getUrl());
        }*/
        if (mds == null) {
            return new Node[0];
        } else {
            return mds.getNodes();
        }
    }
    
    private void createDConfigBean(ModuleDDSupport mod) throws ConfigurationException {
        mod.createConfigs(this);
    }
    
    public void saveOnDemand() throws IOException {
        if (needsSave) {
            save();
        }
    }
    
    public void save() throws IOException {
        try {
            saveInProgress = true;
            
            if(config instanceof SunONEDeploymentConfiguration) {
                SunONEDeploymentConfiguration s1dc = (SunONEDeploymentConfiguration) config;
                s1dc.writeDeploymentPlanFiles(this);
                
                needsSave = false;
                if(saver != null) {
                    saver.resetChanged();
                }
            } else {
                throw new IllegalArgumentException("Invalid DeploymentConfiguration: " + config);
            }
        } catch (ConfigurationException ce) {
            if(!saveFailedDialogDisplayed) { // do not display multiple instances
                saveFailedDialogDisplayed = true;
                String msg = NbBundle.getMessage(ConfigurationStorage.class, "MSG_ConfigurationSaveFailed", 
                        // !PW FIXME where to server name for this??? Do we still need it?
                        "!PW FIXME server display name", "!PW FIXME file list???");
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(msg));
                saveFailedDialogDisplayed = false;
            }
        } finally {
            saveInProgress = false;
        }
    }
    
    public void load() throws IOException, InvalidModuleException, ConfigurationException {
        if(saveInProgress) { // internal change - do not reload
            return;
        }

        init();
        
        /*if(module.getJ2eeModule().getModuleType().equals(J2eeModule.EAR)) {
            J2eeAppProvider appProvider = (J2eeAppProvider) module;
            J2eeModuleProvider[] modules = appProvider.getChildModuleProviders();
            for(int i = 0; i < modules.length; i++) {
                ModuleDeploymentSupport mds = (ModuleDeploymentSupport) moduleMap.get(modules[i].getJ2eeModule().getUrl());
                dps.readDeploymentPlanFiles(config, mds.getDeployableObject(), getSaveFiles(modules[i],serverString.getServer()));
            }
        }*/

        if(config instanceof SunONEDeploymentConfiguration) {
            SunONEDeploymentConfiguration s1dc = (SunONEDeploymentConfiguration) config;
            s1dc.readDeploymentPlanFiles(this);
            
            for(Iterator i = moduleMap.values().iterator();i.hasNext();) {
                ModuleDDSupport mod = (ModuleDDSupport) i.next();
                createDConfigBean(mod);
            }
            loaded = true;
        } else {
            throw new IllegalArgumentException("Invalid DeploymentConfiguration: " + config);
        }
    }
    
    public void cleanup() {
        for(Iterator i = moduleMap.values().iterator();i.hasNext();) {
            ModuleDDSupport mds = (ModuleDDSupport) i.next();
            removeVersionListener(mds.getProvider().getJ2eeModule());
            mds.cleanup();
        }
        moduleMap = new HashMap();
        /*if (dobj instanceof J2eeModuleContainer) {
            J2eeModuleContainer jmc = (J2eeModuleContainer) dobj;
            jmc.removeModuleListener(this);
        }*/
    }

// !PW Removed when moved from j2eeserver to sunddui
//    public DeployableObject getDeployableObject(String moduleUri) {
//        if (moduleUri == null)
//            return config.getDeployableObject();
//        ModuleDeploymentSupport mds = (ModuleDeploymentSupport) moduleMap.get(moduleUri);
//        return mds.getDeployableObject();
//    }
    
// !PW Removed when moved from j2eeserver to sunddui
//    public DeploymentConfiguration getDeploymentConfiguration() {
//        // we are now called during load while building config bean cache
//        if (config == null) { 
//            ensureLoaded();
//        }
//        return config;
//    }
    
    private String getKey(J2eeModule mod) {
        String key = mod.getUrl();
        if (key == null || key.trim().equals("")) { //NOI18N
            key = ROOT;
        }
        return key;
    }
    
    private void createVersionListener(J2eeModule mod) {
        String key = getKey(mod);
        J2eeModule.VersionListener listener = new ModuleVersionListener(key);
        mod.addVersionListener(listener);
        versionListeners.put(key, listener);
    }
    
    private void removeVersionListener(J2eeModule mod) {
        J2eeModule.VersionListener vl = (J2eeModule.VersionListener) versionListeners.remove(getKey(mod));
        if (vl != null) {
            mod.removeVersionListener(vl);
        }
    }
    
    private void createVersionListeners() {
        createVersionListener(module.getJ2eeModule());
        /*if(module.getJ2eeModule().getModuleType().equals(J2eeModule.EAR)) {
            J2eeModuleContainer appProvider = (J2eeModuleContainer) module;
            J2eeModule[] modules = appProvider.getModules(this);
            for(int i = 0; i < modules.length; i++) {
                createVersionListener(modules[i]);
            }
        }*/
    }
    
    private class ModuleVersionListener implements J2eeModule.VersionListener {
        private String moduleUri;
        ModuleVersionListener(String moduleUri) {
            this.moduleUri = moduleUri;
        }
        public void versionChanged(String oldVersion, String newVersion) {
            try {
                saveOnDemand();
                cleanup();
                init();
            } catch(java.util.NoSuchElementException e) {
                String msg = NbBundle.getMessage(
                ConfigurationStorage.class, "MSG_DescriptorError", "TBD", e.getMessage());
                NotifyDescriptor nd = new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(nd);
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            } catch (Exception e2) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e2);
            }
            
            ConfigBeanTopComponent tc = ConfigBeanTopComponent.findByConfigStorage(ConfigurationStorage.this);
            if (tc != null) {
                tc.refresh();
            }
        }
    }
    
    // !PW Was used by ConfigurationStorageImpl.ensureResourceDefined()
    //
//    public DDRoot getEjbJarRoot() {
//        if (! J2eeModule.EJB.equals(module.getJ2eeModule().getModuleType())) {
//            throw new IllegalArgumentException("Trying to get config bean for ejb on non ejb module!"); //NONI18N
//        }
//        ModuleDeploymentSupport mds = (ModuleDeploymentSupport) moduleMap.get(ROOT);
//        return mds.getDDBeanRoot(J2eeModule.EJBJAR_XML);
//    }
}
