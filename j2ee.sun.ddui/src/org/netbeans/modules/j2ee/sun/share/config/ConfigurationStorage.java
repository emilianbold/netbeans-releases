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
package org.netbeans.modules.j2ee.sun.share.config;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.*;
import java.util.*;
import javax.enterprise.deploy.model.*;
import javax.enterprise.deploy.spi.*;
import javax.enterprise.deploy.spi.exceptions.*;
import org.netbeans.modules.j2ee.dd.api.common.RootInterface;

import org.openide.*;
import org.openide.NotifyDescriptor.Confirmation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.*;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;
import org.openide.util.WeakListeners;

import org.xml.sax.SAXException;

import org.netbeans.modules.j2ee.deployment.devmodules.api.*;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.*;
import org.netbeans.modules.j2ee.sun.share.config.ui.ConfigBeanTopComponent;
import org.netbeans.modules.j2ee.sun.share.configbean.SunONEDeploymentConfiguration;


/* ConfigurationStorage stores a DeploymentConfiguration.  It is responsible
 * for creating the DeployableObjects and the DConfigBeanRoots and tracking
 * changes at the application level.
 */
public class ConfigurationStorage implements PropertyChangeListener, Node.Cookie {
    
    public static final String ROOT = "/"; // NOI18N
    private SunONEDeploymentConfiguration config;
    
    // Dataobject support -- ref to dataobject, property change listener
    // that deletes the reference and detaches itself when dataobject is made invalid.
    private ConfigDataObject configDataObject = null;
    private Object dobjMonitor = new Object();
    private PropertyChangeListener weakPropListener = null;
    
    // Map of url -> ModuleDeploymentSupport
    Map moduleMap = new HashMap();
    final Map versionListeners = new HashMap();
    private boolean needsSave = false;
    final J2eeModuleProvider module;
    boolean loaded = false;
    private Task autoSaveTask;
    private int saveInProgress;
    private int cleanInProgress;
    private boolean saveFailedDialogDisplayed;
    
    // PropertyChangeSupport instance to let us forward dataobject cookie changes
    // to any listeners (ie. ConfigBeanNode).
    private PropertyChangeSupport dobjCookieChangeSupport;
    
    public ConfigurationStorage(J2eeModuleProvider module, SunONEDeploymentConfiguration config) throws ConfigurationException, InvalidModuleException, IOException, SAXException {
        this.module = module;
        this.config = config;
        this.saveInProgress = 0;
        this.cleanInProgress = 0;
        this.dobjCookieChangeSupport = new PropertyChangeSupport(this);
        
        load(); // calls init(), below.
//        createVersionListeners();
    }
    
    private void init() throws ConfigurationException, InvalidModuleException, IOException {
        // do the clean up first
        try {
            cleanInProgress++;
            for (Iterator i = moduleMap.values().iterator(); i.hasNext();) {
                ((ModuleDDSupport)i.next()).cleanup();
            }
            moduleMap.clear();
        } finally {
            cleanInProgress--;
        }
        
        // create all MDS/ADS classes here.  MDS's should be GC'ed if the DC changes.
        ModuleDDSupport mds = new ModuleDDSupport(module, config);
        moduleMap.put(ROOT,mds);
        /*if(module.getJ2eeModule().getModuleType().equals(J2eeModule.EAR)) {
            J2eeApplicationProvider appProvider = (J2eeApplicationProvider) module;
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
    
   /** Normalizes a DDBeanRoot from j2eeserver module into the matching DDRoot
     *  that is managed by our ModuleDDSupport.
     */
    public RootInterface normalizeDDBeanRoot(RootInterface ddBeanRoot) {
        RootInterface ddNew = ddBeanRoot;

        // !PW FIXME enhance this algorithm to handle multiple modules if this class does.
        // Until then, there is only one entry in the table, under ROOT key.
        ModuleDDSupport mds = (ModuleDDSupport) moduleMap.get(ROOT);
        if(mds != null) {
            //DDBeanRoot newRoot = mds.getDDBeanRoot();
            RootInterface newRoot = mds.getRootInterface();
            if(newRoot != ddBeanRoot && newRoot != null) {
                ddNew = newRoot;

                // Not sure if we need to handle ddbeanroot for webservices.xml, but in case
                // someone passes that one in, it will cause this assert here.
                //assert ddBeanRoot.getXpath().equals(ddNew.getXpath()) : "Mismatched xpaths in normalizeDDBeanRoot for " + ddBeanRoot;
            }
        }
        
        return ddNew;
    }

    public DDBean normalizeEjbDDBean(DDBean ejbDDBean) {
        DDBean result = null;
        String theEjbName = Utils.getField(ejbDDBean, "ejb-name"); // NOI18N
        ModuleDDSupport mds = (ModuleDDSupport) moduleMap.get(ROOT);
        DDRoot ddRoot = mds.getDDBeanRoot(J2eeModule.EJBJAR_XML);
        StandardDDImpl[] ddBeans = (StandardDDImpl[]) ddRoot.getChildBean(ejbDDBean.getXpath());

        if(ddBeans != null) {
            for(int i = 0; i < ddBeans.length; i++) {
//                String ejbName = (String) ddBeans[i].proxy.bean.getValue("EjbName"); // NOI18N
                String ejbName = null; // (String) ddBeans[i].proxy.rooti. // NOI18N
                if (theEjbName.equals(ejbName)) {
                    result = ddBeans[i];
                    break;
                }
            }

            if(result == null) {
                for (int i = 0; i < ddBeans.length; i++) {
                    String msg = "FIXME normalizeEjbDDBean"; //ddBeans[i].proxy.bean.dumpBeanNode();
                    ErrorManager.getDefault().log(ErrorManager.ERROR, msg);
                }
                Exception ex = new Exception("Failed to lookup: " + theEjbName + ", type " + ejbDDBean.getXpath()); // NOI18N
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            }
        }

        return result;
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
        ConfigDataObject configDO;
        
        synchronized (dobjMonitor) {
            if(configDataObject == null) {
                FileObject configFO = FileUtil.toFileObject(config.getConfigFiles()[0]);
                if(configFO != null) {
                    try {
                        DataObject dObj = DataObject.find(configFO);
                        if(dObj instanceof ConfigDataObject) {
                            configDataObject = (ConfigDataObject) dObj;
                            weakPropListener = WeakListeners.propertyChange(this, configDataObject);
                            configDataObject.addPropertyChangeListener(weakPropListener);
//                            System.out.println("CS: Lookup & cached dataobject for " + configDataObject.getName());
                        }
                    } catch (DataObjectNotFoundException ex) {
                        // return null if not found.
                    }
                }
            }
            configDO = configDataObject;
        }
        return configDO;
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        // if dataobject is being made invalid, remove listener and null our reference.
//        System.out.println("CS.DataObject.propchange: " + evt.getPropertyName() + ", old = " + evt.getOldValue() + ", new = " + evt.getNewValue());
        if(DataObject.PROP_VALID.equals(evt.getPropertyName())) {
            if(Boolean.FALSE.equals(evt.getNewValue())) {
                synchronized (dobjMonitor) {
//                    System.out.println("CS: Removing invalid dataobject reference to " + configDataObject.getName());
                    configDataObject.removePropertyChangeListener(weakPropListener);
                    configDataObject = null;
                    weakPropListener = null;
                }
            }
        } 
        // Forward cookie changes to anyone listening to us (see ConfigBeanNode.java)
        else if(DataObject.PROP_COOKIE.equals(evt.getPropertyName())) {
            dobjCookieChangeSupport.firePropertyChange(evt);
        }
    }
    
    public void addPropertyChangeListener(PropertyChangeListener pCL) {
        dobjCookieChangeSupport.addPropertyChangeListener(pCL);
    }

    public void removePropertyChangeListener(PropertyChangeListener pCL) {
        dobjCookieChangeSupport.removePropertyChangeListener(pCL);
    }
    
    
    /**
     * Return comma separeted list of files.
     */
    private String filesToString(File[] files) {
        StringBuffer sb = new StringBuffer();
        if (files.length > 0) {
            sb.append(files[0].getPath());
        }
        for (int i = 1; i < files.length; i++) {
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
//        System.out.println("autosave... [" + System.currentTimeMillis() + "]");
        if (autoSaveTask == null) {
//            System.out.println("creating autosaver... [" + System.currentTimeMillis() + "]");
            autoSaveTask = RequestProcessor.getDefault().post(new Runnable() {
                private boolean dialogIsDisplayed;
                public void run() {
                    // TODO should be rewritten - currently needs to be run in 
                    // the event dispatch thread since we are accessing window api,
                    // see also ConfigDataObject.fileChanged
//                    System.out.println("running autosaver... [" + System.currentTimeMillis() + "]");
                    Mutex.EVENT.readAccess(new Runnable() {
                        public void run() {
//                            System.out.println("running nested autosaver... [" + System.currentTimeMillis() + "]");
                            if(dialogIsDisplayed) {
//                                System.out.println("editor save query active, no autosave... [" + System.currentTimeMillis() + "]");
                                return; // Reentrancy not supported nor needed.
                            }
                            
                            ConfigDataObject configDO = getPrimaryDataObject();
                            if (configDO == null) {
//                                System.out.println("no saver/dataobject... [" + System.currentTimeMillis() + "]");
                                // no dataobject -- if primary file does not exist, save configuration
                                FileObject configFO = FileUtil.toFileObject(config.getConfigFiles()[0]);
                                if(configFO == null) {
                                    try {
//                                        System.out.println("no file object, saving new configuration... [" + System.currentTimeMillis() + "]");
                                        save();
//                                        System.out.println("configuration saved... [" + System.currentTimeMillis() + "]");
                                    } catch (Exception ex) {
                                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                                    }
                                }
                                
                                // return because we've either just saved or there was no saver (and thus nothing to save).
                                return;
                            } 
                            
                            // proceed in auto save only if graphical config editor is not opened
                            if (!configDO.isConfigEditorOpened()) {
//                                System.out.println("editor is closed, saving... [" + System.currentTimeMillis() + "]");
                                try {
                                    // if config files are modified, ask whether to rewrite them
                                    if (configDO.areModified()) {
                                        File files[] = ConfigurationStorage.this.config.getConfigFiles();
                                        String serverName = ConfigurationStorage.this.config.getAppServerVersion().toString();
                                        String msg = NbBundle.getMessage(ConfigurationStorage.class, 
                                                "MSG_SaveGeneratedChanges", // NOI18N
                                                serverName, 
                                                filesToString(files));
                                        Confirmation cf = new Confirmation(msg, NotifyDescriptor.YES_NO_OPTION);
                                        dialogIsDisplayed = true;
                                        DialogDisplayer.getDefault().notify(cf);
                                        if (!NotifyDescriptor.YES_OPTION.equals(cf.getValue())) {
                                            return;
                                        }
                                    }
                                    save();
//                                    System.out.println("configuration saved... [" + System.currentTimeMillis() + "]");
                                } catch (Exception ex) {
                                    // ignore it
                                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                                } finally {
                                    dialogIsDisplayed = false;
                                }
                            } else {
//                                System.out.println("editor is open, marking dirty... [" + System.currentTimeMillis() + "]");
                                configDO.setChanged();
                            }
                        }
                    });
                }
            }, 100);
        } else {
//            System.out.println("scheduling autosave... [" + System.currentTimeMillis() + "]");
            autoSaveTask.schedule(100);
        }
    }
    
    public void setChanged() {
        if(cleanInProgress == 0) {
            needsSave = true;
            autoSave();
        }
    }
    
    public void updateDDRoot(FileObject dd) {
        if("webservices".equals(dd.getName())) {
            ModuleDDSupport mds = (ModuleDDSupport) moduleMap.get(ROOT);
            if(mds != null) {
                mds.getDDBeanRoot(ModuleDDSupport.filenameToPath(dd.getNameExt(), mds.getType()));
            }
        }
    }
    
    public DeploymentConfiguration getDeploymentConfiguration() {
        return config;
    }
    
    public static J2eeModuleProvider getChildModuleProvider(J2eeModuleProvider jmp, String uri) {
        if (uri == null) {
            return null;
        }
        J2eeModuleProvider child = null;
        if (jmp instanceof J2eeApplicationProvider) {
            J2eeApplicationProvider jap = (J2eeApplicationProvider) jmp;
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
        // If there is an exception during the save operation (either a ConfigurationException
        // for some bizarre non-I/O related reason or a true IOException), display an appropriate
        // message to the user.  Then rethrow the exception (wrapping as an IOException if required)
        // so that the caller can handle it intelligently.  For example, this is the only way that
        // ExitDialog can be notified that the file didn't save properly (and prevents the IDE from
        // shutting down prematurely and throwing away the user's still unsaved data).
        try {
            saveInProgress++;
            
            if(config != null) {
//                config.writeDeploymentPlanFiles(this);
                needsSave = false;
                ConfigDataObject configDO = getPrimaryDataObject();
                if(configDO != null) {
                    configDO.resetChanged();
                }
            } else {
                throw new IllegalStateException("Attempted to save configuration when DeploymentConfiguration is null.");
            }
//        } catch (ConfigurationException ce) {
//            reportExceptionDuringSave(ce);
//            
//            // 1. Must throw IOException here, otherwise, if caller is IDE's exit dialog
//            // this dataobject will be removed from the queue of savable objects.
//            // 2. IOException constructor in JDK 1.4.2 cannot chain exceptions.
//            IOException ioe = new IOException(ce.getLocalizedMessage());
//            ioe.initCause(ce);
//            throw ioe;
//        } catch(IOException ioe) {
//            reportExceptionDuringSave(ioe);
//            throw ioe;
        } finally {
            saveInProgress--;
        }
    }
    
    private void reportExceptionDuringSave(Exception ex) {
        if(!saveFailedDialogDisplayed) { // do not display multiple instances
            try {
                // Why is there a separate flag here instead of reusing saveInProgress?
                saveFailedDialogDisplayed = true;

                // Try to do a nice message box if this exception comes with a reason for the failure.
                String errorMsg;
                String exceptionMsg = ex.getLocalizedMessage();
                String appServerVersion = config.getAppServerVersion().toString();
                String fileList = filesToString(config.getConfigFiles());

                if(exceptionMsg != null && exceptionMsg.length() > 0) {
                    if(ex instanceof IOException) {
                        // For IOExceptions, if there is message, just display that.  This eliminates
                        // redundancy of the filename, e.g. "[filename]: Access denied" is the message if the file
                        // is read-only.
                        errorMsg = exceptionMsg;
                    } else {
                        // For all other exceptions, prefix the application server and affected file to the message.
                        errorMsg = NbBundle.getMessage(ConfigurationStorage.class, "MSG_ConfigurationSaveFailedHasMessage", 
                                appServerVersion, fileList, exceptionMsg);
                    }                        
                } else {
                    // If there is no message, simply indicate there was a problem saving the affected files.
                    errorMsg = NbBundle.getMessage(ConfigurationStorage.class, "MSG_ConfigurationSaveFailed", 
                            appServerVersion, fileList);
                }

                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(errorMsg));
            } finally {
                saveFailedDialogDisplayed = false;
            }
        }
    }
    
    public void load() throws IOException, InvalidModuleException, ConfigurationException {
        if(saveInProgress > 0) { // internal change - do not reload
            return;
        }

        init();
        
        /*if(module.getJ2eeModule().getModuleType().equals(J2eeModule.EAR)) {
            J2eeApplicationProvider appProvider = (J2eeApplicationProvider) module;
            J2eeModuleProvider[] modules = appProvider.getChildModuleProviders();
            for(int i = 0; i < modules.length; i++) {
                ModuleDeploymentSupport mds = (ModuleDeploymentSupport) moduleMap.get(modules[i].getJ2eeModule().getUrl());
                dps.readDeploymentPlanFiles(config, mds.getDeployableObject(), getSaveFiles(modules[i],serverString.getServer()));
            }
        }*/

        if(config instanceof SunONEDeploymentConfiguration) {
            SunONEDeploymentConfiguration s1dc = (SunONEDeploymentConfiguration) config;
            ModuleDDSupport rootSupport = (ModuleDDSupport) moduleMap.get(ROOT);
//            s1dc.readDeploymentPlanFiles(this, module.getJ2eeModule());
            createDConfigBean(rootSupport);
            loaded = true;
        } else {
            throw new IllegalArgumentException("Invalid DeploymentConfiguration: " + config);
        }
    }
    
    public void cleanup() {
        // This is called by the ModuleVersion listener to rebuild the tree if
        // the DD version changes since that forces creation of a new DDBean graph.
        // However, that means we should never clear 'config' here, since in that
        // case, the configuration is still valid and necessary.
        for(Iterator i = moduleMap.values().iterator();i.hasNext();) {
            ModuleDDSupport mds = (ModuleDDSupport) i.next();
//            removeVersionListener(mds.getProvider().getJ2eeModule());
            mds.cleanup();
        }
        moduleMap = new HashMap();
        /*if (dobj instanceof J2eeModuleContainer) {
            J2eeModuleContainer jmc = (J2eeModuleContainer) dobj;
            jmc.removeModuleListener(this);
        }*/
    }

    private String getKey(J2eeModule mod) {
        String key = mod.getUrl();
        if (key == null || key.trim().equals("")) { //NOI18N
            key = ROOT;
        }
        return key;
    }

    // DDBean Removal
//    
//    private void createVersionListener(J2eeModule mod) {
//        String key = getKey(mod);
//        J2eeModule.VersionListener listener = new ModuleVersionListener(key);
//        mod.addVersionListener(listener);
//        versionListeners.put(key, listener);
//    }
//    
//    private void removeVersionListener(J2eeModule mod) {
//        J2eeModule.VersionListener vl = (J2eeModule.VersionListener) versionListeners.remove(getKey(mod));
//        if (vl != null) {
//            mod.removeVersionListener(vl);
//        }
//    }
//    
//    private void createVersionListeners() {
//        createVersionListener(module.getJ2eeModule());
//        /*if(module.getJ2eeModule().getModuleType().equals(J2eeModule.EAR)) {
//            J2eeModuleContainer appProvider = (J2eeModuleContainer) module;
//            J2eeModule[] modules = appProvider.getModules(this);
//            for(int i = 0; i < modules.length; i++) {
//                createVersionListener(modules[i]);
//            }
//        }*/
//    }

    boolean saveInProgress() {
        return (saveInProgress > 0);
    }

    // DDBean Removal
//
//    private class ModuleVersionListener implements J2eeModule.VersionListener {
//        private String moduleUri;
//        ModuleVersionListener(String moduleUri) {
//            this.moduleUri = moduleUri;
//        }
//        public void versionChanged(String oldVersion, String newVersion) {
//            try {
//                saveOnDemand();
//                cleanup();
//                init();
//            } catch(java.util.NoSuchElementException e) {
//                String msg = NbBundle.getMessage(
//                ConfigurationStorage.class, "MSG_DescriptorError", "TBD", e.getMessage());
//                NotifyDescriptor nd = new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE);
//                DialogDisplayer.getDefault().notify(nd);
//                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
//            } catch(IOException ex) {
//                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
//            } catch (Exception e2) {
//                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e2);
//            }
//            
//            ConfigBeanTopComponent tc = ConfigBeanTopComponent.findByConfigStorage(ConfigurationStorage.this);
//            if (tc != null) {
//                tc.refresh();
//            }
//        }
//    }

    public DDBeanRoot getDDBeanRoot(J2eeModule module) {
        DDBeanRoot ddNew = null; //ddBeanRoot;

        // !PW FIXME enhance this algorithm to handle multiple modules if this class does.
        // Until then, there is only one entry in the table, under ROOT key.
        ModuleDDSupport mds = (ModuleDDSupport) moduleMap.get(ROOT);
        if(mds != null) {
            DDBeanRoot newRoot = mds.getDDBeanRoot();
            if(newRoot != null) {
                ddNew = newRoot;

                // Not sure if we need to handle ddbeanroot for webservices.xml, but in case
                // someone passes that one in, it will cause this assert here.
                //assert ddBeanRoot.getXpath().equals(ddNew.getXpath()) : "Mismatched xpaths in normalizeDDBeanRoot for " + ddBeanRoot;
            }
        }
        
        return ddNew;
    }
}
