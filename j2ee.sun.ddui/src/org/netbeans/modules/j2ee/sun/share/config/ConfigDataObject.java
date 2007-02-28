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
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import javax.enterprise.deploy.spi.exceptions.InvalidModuleException;
import javax.enterprise.deploy.spi.exceptions.ConfigurationException;

import org.xml.sax.InputSource;

import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileLock;
import org.openide.cookies.CloseCookie;
import org.openide.cookies.EditCookie;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.OpenCookie;
import org.openide.cookies.PrintCookie;
import org.openide.cookies.SaveCookie;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiFileLoader;
import org.openide.loaders.XMLDataObject;
import org.openide.nodes.CookieSet;
import org.openide.nodes.Node;
import org.openide.text.DataEditorSupport;
import org.openide.text.NbDocument;
import org.openide.util.HelpCtx;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.openide.windows.CloneableTopComponent;
import org.openide.windows.CloneableOpenSupport;
import org.openide.windows.TopComponent;

import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.xml.cookies.CheckXMLCookie;
import org.netbeans.api.xml.cookies.ValidateXMLCookie;
import org.netbeans.spi.xml.cookies.CheckXMLSupport;
import org.netbeans.spi.xml.cookies.DataObjectAdapters;
import org.netbeans.spi.xml.cookies.ValidateXMLSupport;
import org.netbeans.modules.xml.api.EncodingUtil;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.deployment.plugins.api.ConfigurationSupport;
import org.netbeans.modules.j2ee.sun.share.config.ui.*;
import org.netbeans.modules.j2ee.sun.share.configbean.SunONEDeploymentConfiguration;


/** Data object representing a deployment plan file.
 * Only interesting feature is the {@link OpenCookie}
 * which lets you open it in graphical editor.
 * @author Pavel Buzek
 */
public class ConfigDataObject extends XMLDataObject implements ConfigurationSaver, FileChangeListener {

    //PENDING: create serialVersionUID
    //    private static final long serialVersionUID = -1073885636989804140L;
    
    public static final String SERVER_ID = "J2EE"; //NOI18N

    private final File configKey;
    private HashSet secondaries = null; //SecondaryConfigDataObject
    private boolean isEdited = false;
    private boolean isEditedChecked = false;
    private ValidateXMLCookie validateCookie = null;
    private CheckXMLCookie checkCookie = null;
    private XMLEditorSupport xmlEditorSupport = null;
    private XMLOpenSupport xmlOpenSupport = null;

    /** Whether the reload dialog is currently opened. Prevents poping of multiple
     * reload dialogs if there is more external saves.
     */
    private boolean reloadDialogOpened;
    
    
    public ConfigDataObject(FileObject pf, MultiFileLoader loader) throws DataObjectExistsException {
        super(pf, loader);
        configKey = FileUtil.toFile(pf);
//        System.out.println("Configuration Key: " + configKey.getPath());
        pf.addFileChangeListener((FileChangeListener) WeakListeners.create(FileChangeListener.class, this, pf));
        initCookies();
    }
    
    private void initCookies() {
        // Need to provide null factory for these four cookies to override what
        // XMLDataObject installed.  Otherwise a duplicate XMLEditorSupport object
        // will be created and cause bizarre problems.  Real cookie cleanup can wait
        // until we migrate to multiview framework.
        CookieSet.Factory factory = new CookieSet.Factory() {
            public Node.Cookie createCookie(Class klass) {
                return null;
            }
        };
                
        // !PW This comment inherited from XMLDataObject.  I'm not exactly sure what it means.
        // EditorCookie.class must be synchronized with XMLEditor.Env->findCloneableOpenSupport
        CookieSet cookies = getCookieSet();
        cookies.add(EditorCookie.class, factory);
        cookies.add(OpenCookie.class, factory);
        cookies.add(CloseCookie.class, factory);
        cookies.add(PrintCookie.class, factory);
    }
    
    public HelpCtx getHelpCtx() {
        return new HelpCtx(getPrimaryFile().getName()+"_help"); //NOI18N
    }
    
    public boolean isRenameAllowed() {
        return false;
    }
    
    protected Node createNodeDelegate() {
        return new ConfigDataNode(this);
    }
    
    protected DataObject handleCopy(DataFolder f) throws IOException {
        DataObject newDo = super.handleCopy(f);
        try {
            FileObject primary = newDo.getPrimaryFile();
            newDo.setValid(false);
            newDo = DataObject.find(primary);
        } catch (java.beans.PropertyVetoException pve) {
            //nothing
        }
        return newDo;
    }

    public SunONEDeploymentConfiguration getDeploymentConfiguration() throws ConfigurationException {
        // Look up configuration bound to this object.
        SunONEDeploymentConfiguration config = SunONEDeploymentConfiguration.getConfiguration(configKey);

        if(config == null) {
            // Request deployment configuration for SJSAS from j2eeserver module
            FileObject configFO = FileUtil.toFileObject(configKey);
            ConfigurationSupport.requestCreateConfiguration(configFO, SERVER_ID); // NOI18N
            config = SunONEDeploymentConfiguration.getConfiguration(configKey);
            if(config == null) {
                // If config is still null here, there is some kind of initialization
                // problem (or bug).
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, new IllegalStateException(
                        "Unable to initialize DeploymentConfiguration for " + configKey.getPath() + " on server " + SERVER_ID));
            }
        }

        return config;
    }
    
    /** This is really just "has sun-cmp-mappings.xml", but since this area is still
     *  written generally, this method is too.
     */
    public boolean hasSecondaries()  {
        return getSecondaries().size() > 0;
    }
    
    protected void addSecondary(SecondaryConfigDataObject secondary) {
        getSecondaries().add(secondary);
    }
    
    protected void removeSecondary(SecondaryConfigDataObject secondary) {
        getSecondaries().remove(secondary);
    }
    
    protected Set getSecondaries() {
        if (secondaries != null) {
            return secondaries;
        }
        secondaries = new HashSet();
        String [] paths = this.getProvider().getConfigSupport().getDeploymentConfigurationFileNames();
        for (int i=0; i<paths.length; i++) {
            File path = new File(paths[i]);
            FileObject fo = getProvider().findDeploymentConfigurationFile(path.getName());
            if (fo == null) {
                continue;
            }
            try {
                SecondaryConfigDataObject second = (SecondaryConfigDataObject) DataObject.find(fo);
                secondaries.add(second);
            } catch (Exception ex) {
                continue;
            }
        }
        return secondaries;
    }
    
    protected boolean isConfigEditorOpened() {
        Boolean result = (Boolean) Mutex.EVENT.readAccess(new Mutex.Action() {
            public Object run() {
                ConfigBeanTopComponent configEditor = findOpenedConfigEditor();
                boolean isOpen = (configEditor != null) ? configEditor.isOpened() : false;
                return isOpen ? Boolean.TRUE : Boolean.FALSE;
            }
        });
        return result.booleanValue();
    }
    
    private OpenCookie _getOpenCookie() {
        OpenCookie myOpen = getOpenCookie();
        for (Iterator i=getSecondaries().iterator(); i.hasNext();) {
            SecondaryConfigDataObject secondary = (SecondaryConfigDataObject) i.next();
            if (secondary.getOpenCookie() == null) {
                return null;
            }
        }
        return myOpen;
    }
    
    protected OpenCookie getOpenCookie() {
        // !PW Only enable configuration editor if SJSAS is the current target
        // server.
        if (!Utils.isSunServer(getProvider().getServerID())) {
            return null;
        }
        if (!isEditedChecked) {
            isEdited = checkIsEdited();
        }
        if (!isEdited) {
            if (xmlOpenSupport == null) {
                xmlOpenSupport = new XMLOpenSupport(this);
            }
            return xmlOpenSupport;
        } else {
            return null;
        }
    }
    
    private EditCookie _getEditCookie() {
        EditCookie myEdit = getEditCookie();
        for (Iterator i=getSecondaries().iterator(); i.hasNext();) {
            SecondaryConfigDataObject secondary = (SecondaryConfigDataObject) i.next();
            if (secondary.getEditCookie() == null) {
                return null;
            }
        }
        return myEdit;
    }

    protected EditCookie getEditCookie() {
        if (!isConfigEditorOpened()) {
            if (xmlEditorSupport == null) {
                xmlEditorSupport = new XMLEditorSupport(this);
            }
            return xmlEditorSupport;
        } else {
            return null;
        }
    }
    
    public org.openide.nodes.Node.Cookie getCookie(Class c) {
        Node.Cookie retValue = null;
        if (OpenCookie.class.isAssignableFrom(c)) {
            return _getOpenCookie();
        } else if (EditCookie.class.isAssignableFrom(c) 
                || EditorCookie.class.isAssignableFrom(c)
                || CloseCookie.class.isAssignableFrom(c)
                || PrintCookie.class.isAssignableFrom(c)) {
            return _getEditCookie();
        } else if (ConfigurationStorage.class.isAssignableFrom(c)) {
            retValue = getStorage();
        } else if (ValidateXMLCookie.class.isAssignableFrom(c)) {
            if (validateCookie == null) {
                InputSource in = DataObjectAdapters.inputSource(this);
                validateCookie = new ValidateXMLSupport(in);
            }
            return validateCookie;
        } else if (CheckXMLCookie.class.isAssignableFrom(c)) {
            if (checkCookie == null) {
                InputSource in = DataObjectAdapters.inputSource(this);
                checkCookie = new CheckXMLSupport(in);
            }
            return checkCookie;
        }
        
        if (retValue == null) {
            retValue = super.getCookie(c);
        }
        return retValue;
    }
    
    private boolean checkIsEdited() {
        if (xmlEditorSupport != null) {
            isEditedChecked = true;
            return (xmlEditorSupport.getOpenedPanes() != null);
        }
        return false;
    }
    
    public void editorClosed(ConfigBeanTopComponent tc) {
        if (xmlOpenSupport != null) {
            xmlOpenSupport.reset();
        }
        
        tc.reset();
        fireCookieChange();
    }
    
    protected J2eeModuleProvider getProvider() {
        FileObject f = getPrimaryFile();
        J2eeModuleProvider provider = null;
        Project p = FileOwnerQuery.getOwner(f);
        if (p != null) {
            provider = (J2eeModuleProvider) p.getLookup().lookup(J2eeModuleProvider.class);
        }
        if (provider == null) {
            throw new RuntimeException("Project " + p + " does not provide J2eeModuleProvider in its lookup"); // NOI18N
        }
        return provider;
    }
    
    protected ConfigurationStorage getStorage() {
        ConfigurationStorage storage = null;
        
        try {
            SunONEDeploymentConfiguration config = getDeploymentConfiguration();
            if(config != null) {
                storage = config.getStorage();
            }
        } catch (ConfigurationException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
        
        return storage;
    }
    
    public static FileObject getRelative(FileObject from, String path) throws IOException {
        FileObject step = from;
        //up
        while (path.startsWith("..")) {
            step = step.getParent();
            path = path.substring(2);
            if (path.startsWith("/")) {
                path = path.substring(1);
            }
        }
        //down
        if (path.length() > 0) {
            step = step.getFileObject(path);
        }
        return step;
    }
    
    public static String getRelativePath(FileObject from, FileObject to) {
        String path = "";
        //up
        while (!FileUtil.isParentOf(from, to) || from.equals(to)) {
            if (from.equals(to)) {
                break;
            }
            path = path + "../";
            from = from.getParent();
        }
        //down
        if (!from.equals(to)) {
            path = path + FileUtil.getRelativePath(from, to);
        }
        return path;
    }
    
    public void resetChanged() {
        removeSaveCookie();
    }
    
    public void removeEditorChanges() {
        if (xmlEditorSupport != null) {
            xmlEditorSupport.notifyUnmodified();
        }
    }
    
    public void removeAllEditorChanges() {
        removeEditorChanges();
        for (Iterator i = getSecondaries().iterator(); i.hasNext(); ) {
            SecondaryConfigDataObject second = (SecondaryConfigDataObject) i.next();
            second.removeEditorChanges();
        }
    }
    
    /** Check whether the data object or any of its secondary data objects are modified */
    public boolean areModified() {
        if (isModified()) {
            return true;
        }
        for (Iterator i = getSecondaries().iterator(); i.hasNext(); ) {
            SecondaryConfigDataObject second = (SecondaryConfigDataObject) i.next();
            if (second.isModified()) {
                return true;
            }
        }
        return false;
    }
    
    
    public void resetAllChanged() {
        resetChanged();
        for (Iterator i=getSecondaries().iterator(); i.hasNext();) {
            SecondaryConfigDataObject second = (SecondaryConfigDataObject) i.next();
            second.resetChanged();
        }
    }    
    
    public void setChanged() {
        addSaveCookie(new S());
    }
    
    public boolean isModified() {
        return super.isModified();
    }
    
    private void handleReload(FileObject fo) throws IOException, InvalidModuleException, ConfigurationException {
        ConfigurationStorage cs = getStorage();
        if (cs != null && !cs.saveInProgress()) {
            // check for reload
            boolean doReload = true;
            boolean doReopen = false;

            if(isConfigEditorOpened()) {
                if(isModified()) {
                    doReload = false;
                    if(!reloadDialogOpened) {
                        String message = NbBundle.getMessage(ConfigDataObject.class, "MSG_ExternalChange", fo.getName());
                        NotifyDescriptor nd = new NotifyDescriptor.Confirmation(message, NotifyDescriptor.YES_NO_OPTION);
                        reloadDialogOpened = true;

                        try {
                            Object ret = DialogDisplayer.getDefault().notify(nd);
                            if(NotifyDescriptor.YES_OPTION.equals(ret)) {
                                resetChanged();
                                closeConfigEditors();
                                doReload = true;
                                doReopen = true;
                            } else {
                                doReload = false;
                            }
                        } finally {
                            reloadDialogOpened = false;
                        }
                    }
                } else {
                    closeConfigEditors();
                    doReopen = true;
                }
            }

            // reload configuration
            if(doReload) {
                cs.load();
            }

            // restart config editor if necessary
            if(doReopen) {
                Mutex.EVENT.readAccess(new Runnable() {
                    public void run() {
                        // reopen editor
                        OpenCookie opener = (OpenCookie) ConfigDataObject.this.getCookie(OpenCookie.class);
                        if(opener != null) {
                            opener.open();
                        } 
                    }
                });
            }
        }
    }
    
    public void fileAttributeChanged(org.openide.filesystems.FileAttributeEvent fe) {
//        System.out.println("ConfigDataObject.fileAttributeChanged: " + fe);
    }
    
    public void fileChanged(org.openide.filesystems.FileEvent fe) {
//        System.out.println("ConfigDataObject.fileChanged: " + fe);
        try {
            if(isValid() && fe.getFile().equals(getPrimaryFile())) {
                handleReload(fe.getFile());
            }
        } catch (Exception e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
        }
    }
    
    public void fileDataCreated(org.openide.filesystems.FileEvent fe) {
//        System.out.println("ConfigDataObject.fileDataCreated: " + fe);
    }
    
    public void fileDeleted(org.openide.filesystems.FileEvent fe) {
//        System.out.println("ConfigDataObject.fileDeleted: " + fe);
    }
    
    public void fileFolderCreated(org.openide.filesystems.FileEvent fe) {
//        System.out.println("ConfigDataObject.fileFolderCreated: " + fe);
    }
    
    public void fileRenamed(org.openide.filesystems.FileRenameEvent fe) {
//        System.out.println("ConfigDataObject.fileRenamed: " + fe);
    }
    
    protected void fireCookieChange() {
        fireLimitedCookieChange();
        for (Iterator i=getSecondaries().iterator(); i.hasNext();) {
            SecondaryConfigDataObject secondary = (SecondaryConfigDataObject) i.next();
            secondary.fireLimitedCookieChange();
        }
    }
    
    protected void fireLimitedCookieChange() {
        firePropertyChange(PROP_COOKIE, null, null);
    }
    
    private class S implements SaveCookie {
        public void save() throws java.io.IOException {
            // Do not clean up the editor's modified flags here, as there may have
            // been an exception during save and if so, the flags are still correct.
            // If the save is completed properly, the save code will remove the SaveCookie
            // and clean up the flags for us.
            ConfigurationStorage cs = getStorage();
            if (cs != null) {
                cs.save();
            }
        }
    }
    
    protected final void addSaveCookie(SaveCookie save) {
        getCookieSet().add(save);
        setModified(true);
    }
    
    protected final void removeSaveCookie() {
        SaveCookie sc = null;
        while ((sc = (SaveCookie) getCookie(SaveCookie.class)) != null) {
            getCookieSet().remove(sc);
        }
        this.setModified(false);
    }
    
    private static class XMLOpenSupport extends DataEditorSupport implements OpenCookie, PropertyChangeListener  {
        
        public XMLOpenSupport(ConfigDataObject obj) {
            super(obj, new XMLEditorEnv(obj, OpenCookie.class));
            setMIMEType("text/xml"); // NOI18N
            
//            System.out.println("SunCfg GUI Open Support @ " + System.currentTimeMillis());
//            Thread.currentThread().dumpStack();
        }
        
        public void open() {
            ConfigDataObject cdo = (ConfigDataObject) getDataObject();
            cdo.addPropertyChangeListener(this);
            cdo.getPrimaryFile().refresh(); //check for external changes
            ConfigurationStorage configStorage = (ConfigurationStorage) cdo.getCookie(ConfigurationStorage.class);
            if (configStorage == null) {
                EditCookie editor = (EditCookie) cdo.getCookie(EditCookie.class);
                if (editor != null) {
                    editor.edit();
                }
                return;
            }
            
// calling super.open() causes the correct data structures to be created by the underlying
// DataEditorSupport & lower subclasses which fixes some bugs related to how the editor behaves
// when the underlying file/dataobject is deleted while the editor is open.  However, it also
// creates some other more obnoxious bugs related to the fact that this code doesn't want
// DataEditorSupport performing some of the things it would otherwise do if these data structures
// are updated.  Stay with old system for now while I investigate options.           
//            super.open();
            cdo.openConfigEditor();
        }
        
        protected CloneableTopComponent createCloneableTopComponent() {
            ConfigDataObject cdo = (ConfigDataObject) getDataObject();
            ConfigurationStorage storage = (ConfigurationStorage) 
                    cdo.getCookie(ConfigurationStorage.class);
            return new ConfigBeanTopComponent(storage);
        }
        
        public void propertyChange(PropertyChangeEvent evt) {
            if (DataObject.PROP_MODIFIED.equals(evt.getPropertyName())) {
                // mark/unmark editor as modified
                final ConfigDataObject cdo = (ConfigDataObject) getDataObject();
                if(cdo.isValid()) {
                    final ConfigurationStorage storage = cdo.getStorage();
                    final String newDisplayName = messageName();
                    Mutex.EVENT.readAccess(new Runnable() {
                        public void run() {
                            Iterator it  = TopComponent.getRegistry().getOpened().iterator();
                            while (it.hasNext()) {
                                TopComponent tc = (TopComponent) it.next();
                                if (tc instanceof ConfigBeanTopComponent) {
                                    ConfigBeanTopComponent configEditor = (ConfigBeanTopComponent) tc;
                                    if (configEditor.isFor(storage)) {
                                        configEditor.setDisplayName(newDisplayName);
                                    }
                                }
                            }
                        }
                    });
                } else {
//                    System.out.println("Display name change for editor w/ invalid dataobject.");
                }
            }
        }
        
        public void reset() {
            ((ConfigDataObject)getDataObject()).removePropertyChangeListener(this);
        }
    }

    protected void openConfigEditor() {
        ConfigurationStorage storage = getStorage();
        ConfigBeanTopComponent tc = findOpenedConfigEditor(storage);
        if (tc == null) {
            tc = new ConfigBeanTopComponent(storage);
        }
        
        tc.open();
        tc.requestActive();
        fireCookieChange();
    }
    
    /** will not open new one, can return null */
    protected ConfigBeanTopComponent findOpenedConfigEditor() {
        return findOpenedConfigEditor(getStorage());
    }
    
    public static ConfigBeanTopComponent findOpenedConfigEditor(final ConfigurationStorage storage) {
        // This code must run synchronously on the AWT thread, so issue blocking call.
        // If this causes a deadlock, find a way not to call this method from a background
        // thread or reimplement (which probably would involve cacheing the list of open
        // editors and clones and clearing the cache as they are closed.
        ConfigBeanTopComponent result = (ConfigBeanTopComponent) Mutex.EVENT.readAccess(new Mutex.Action() {
            public Object run() {
                Iterator it  = TopComponent.getRegistry().getOpened().iterator();
                while (it.hasNext()) {
                    TopComponent tc = (TopComponent) it.next();
                    if (tc instanceof ConfigBeanTopComponent) {
                        ConfigBeanTopComponent beanTC = (ConfigBeanTopComponent) tc;
                        if (beanTC.isFor(storage)) {
                            return beanTC;
                        }
                    }
                }
                return null;
            }
        });
        
        return result;
    }
    
//    private static class OpenEditorFinder implements Runnable {
//        
//        private ConfigurationStorage storage;
//        private ConfigBeanTopComponent openEditor;
//        
//        public OpenEditorFinder(ConfigurationStorage storage) {
//            this.storage = storage;
//            this.openEditor = null;
//        }
//        
//        public void run() {
//            Iterator it  = TopComponent.getRegistry().getOpened().iterator();
//            while (it.hasNext()) {
//                TopComponent tc = (TopComponent) it.next();
//                if (tc instanceof ConfigBeanTopComponent) {
//                    ConfigBeanTopComponent beanTC = (ConfigBeanTopComponent) tc;
//                    if (beanTC.isFor(storage)) {
//                        openEditor = beanTC;
//                        break;
//                    }
//                }
//            }
//        }
//        
//        public ConfigBeanTopComponent getOpenEditor() {
//            return openEditor;
//        }
//    }
    
    /** Closes all open config editors on this dataobject.
     *
     * @result true if the editors were all closed (or none were open).  false if
     *  there was an open editor that failed to close.
     */
    public boolean closeConfigEditors() {
        final ArrayList editorList = new ArrayList();
        ConfigurationStorage storage = getStorage();
        
        // Can't close the editors as we find them or we'll get a ConcurrentModificationException.
        Iterator iter = TopComponent.getRegistry().getOpened().iterator();
        while(iter.hasNext()) {
            Object tc = iter.next();
            if(tc instanceof ConfigBeanTopComponent && ((ConfigBeanTopComponent) tc).isFor(storage)) {
                editorList.add(tc);
            }
        }
        
        Boolean result = (Boolean) Mutex.EVENT.readAccess(new Mutex.Action() {
            public Object run() {
                Boolean result = Boolean.TRUE;
                Iterator iter = editorList.iterator();
                while(iter.hasNext()) {
                    ConfigBeanTopComponent configTC = (ConfigBeanTopComponent) iter.next();
                    result = (configTC.close() && result.booleanValue()) ? Boolean.TRUE : Boolean.FALSE;
                }
                return result;
            }
        });
        
        return result.booleanValue();
    }

    private static class XMLEditorSupport extends DataEditorSupport implements EditCookie, EditorCookie.Observable, PrintCookie, CloseCookie {
        
        public XMLEditorSupport(ConfigDataObject obj) {
            super(obj, new XMLEditorEnv(obj, EditCookie.class));
            setMIMEType("text/xml"); // NOI18N
//            System.out.println("SunCfg XML Open Support @ " + System.currentTimeMillis());
//            Thread.currentThread().dumpStack();
        }
        
        protected boolean canClose() {
            boolean result = super.canClose();
            return result;
        }
        
        class Save implements SaveCookie {
            public void save() throws IOException {
                saveDocument();
            }
        }
        
        /* Save document using encoding declared in XML prolog if possible otherwise
         * at UTF-8 (in such case it updates the prolog).
         */
        public void saveDocument() throws java.io.IOException {
            final String defaultEncoding = "UTF8"; // NOI18N
            final StyledDocument doc = getDocument();
            String enc = EncodingUtil.detectEncoding(doc); // api in xml/core
            
            if (enc == null) {
                enc = defaultEncoding;
            }

            try {
                //test encoding on dummy stream
                new java.io.OutputStreamWriter(new java.io.ByteArrayOutputStream(1), enc);
                if(queryCanEncode(doc, enc)) {
                    super.saveDocument();
                    getDataObject().setModified(false);
                }
            } catch(java.io.UnsupportedEncodingException ex) {
                if(queryUpdateProlog(doc, enc, defaultEncoding)) {
                    super.saveDocument();
                    getDataObject().setModified(false);
                }
            }
        }
        
        private boolean queryCanEncode(final StyledDocument doc, final String enc) {
            boolean result = true;
            // is it possible to save the document in the encoding?
            try {
                java.nio.charset.CharsetEncoder coder = java.nio.charset.Charset.forName(enc).newEncoder();
                if(!coder.canEncode(doc.getText(0, doc.getLength()))){
                    NotifyDescriptor nd = new NotifyDescriptor.Confirmation(
                            NbBundle.getMessage (XMLEditorSupport.class, "MSG_BadCharConversion", //NOI18N
                                    new Object [] { getDataObject().getPrimaryFile().getNameExt(), enc}),
                            NotifyDescriptor.YES_NO_OPTION,
                            NotifyDescriptor.WARNING_MESSAGE);
                    nd.setValue(NotifyDescriptor.NO_OPTION);
                    DialogDisplayer.getDefault().notify(nd);
                    
                    if(nd.getValue() != NotifyDescriptor.YES_OPTION) {
                        result = false;
                    }
                }
            } catch (javax.swing.text.BadLocationException e){
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);            
            }
            return result;
        }
        
        private boolean queryUpdateProlog(final StyledDocument doc, final String enc, final String defaultEncoding) {
            boolean needsSave = false;
            
            // ask user what next?
            NotifyDescriptor nd = new NotifyDescriptor.Confirmation(
                    NbBundle.getMessage(XMLEditorSupport.class, "MSG_BadEncodingDuringSave", //NOI18N
                            new Object [] { getDataObject().getPrimaryFile().getNameExt(), enc, defaultEncoding}),
                    NotifyDescriptor.YES_NO_OPTION,
                    NotifyDescriptor.WARNING_MESSAGE);
            nd.setValue(NotifyDescriptor.NO_OPTION);
            DialogDisplayer.getDefault().notify(nd);

            if(nd.getValue() == NotifyDescriptor.YES_OPTION) {

                // update prolog to new valid encoding
                try {
                    final int MAX_PROLOG = 1000;
                    int maxPrologLen = Math.min(MAX_PROLOG, doc.getLength());
                    final char prolog[] = doc.getText(0, maxPrologLen).toCharArray();
                    int prologLen = 0;  // actual prolog length

                    //parse prolog and get prolog end
                    if (prolog[0] == '<' && prolog[1] == '?' && prolog[2] == 'x') {

                        // look for delimitting ?>
                        for (int i = 3; i<maxPrologLen; i++) {
                            if (prolog[i] == '?' && prolog[i+1] == '>') {
                                prologLen = i + 1;
                                break;
                            }
                        }
                    }

                    final int passPrologLen = prologLen;
                    
                    Runnable edit = new Runnable() {
                         public void run() {
                             try {
                                doc.remove(0, passPrologLen + 1); // +1 it removes exclusive
                                doc.insertString(0, "<?xml version='1.0' encoding='" + defaultEncoding + "' ?> \n<!-- was: " + new String(prolog, 0, passPrologLen + 1) + " -->", null); // NOI18N
                             } catch (BadLocationException e) {
                                 if (System.getProperty("netbeans.debug.exceptions") != null) { // NOI18N
                                     e.printStackTrace();
                                 }
                             }
                         }
                    };

                    NbDocument.runAtomic(doc, edit);
                    
                    // Mark for saving on return
                    needsSave = true;
                } catch (BadLocationException lex) {
                    org.openide.ErrorManager.getDefault().notify(lex);
                }
            }

            return needsSave;
        }

// In case we want to rely on java.io.UnsupportedEncodingException in saveDocument(), above.
//        
//        private boolean isSupportedEncoding(String encoding){
//            boolean supported;
//            try {
//                supported = java.nio.charset.Charset.isSupported(encoding);
//            } catch (java.nio.charset.IllegalCharsetNameException e){
//                supported = false;
//            }
//            return supported;
//        }

        protected boolean notifyModified() {
            if (! super.notifyModified()) {
                return false;
            }
            ((ConfigDataObject) getDataObject()).addSaveCookie(new Save());
            return true;
        }
        
        protected void notifyUnmodified() {
            super.notifyUnmodified();
            ((ConfigDataObject) getDataObject()).resetChanged();
        }
        
        public void edit() {
            ConfigDataObject cdo = (ConfigDataObject) getDataObject();
            cdo.isEdited = true;
            open();
            cdo.fireCookieChange();
        }
        
        protected void notifyClosed() {
            super.notifyClosed();
            ConfigDataObject cdo = (ConfigDataObject) getDataObject();
            cdo.isEdited = false;
            cdo.fireCookieChange();
        }
        
        public void refreshDocument() {
            super.reloadDocument();
        }
    }
    
    //!!! it also stays for SaveCookie however does not understand
    // encoding declared in XML header => need to be rewritten.
    private static class XMLEditorEnv extends DataEditorSupport.Env {
        private static final long serialVersionUID = 6593415381104273008L;
        
        private final Class openSupportCookieClass;
        
        public XMLEditorEnv(ConfigDataObject dobj, Class cookieClass) {
            super(dobj);
            openSupportCookieClass = cookieClass;
        }
        protected FileObject getFile() {
            return getDataObject().getPrimaryFile();
        }
        protected FileLock takeLock() throws IOException {
            return ((ConfigDataObject) getDataObject()).getPrimaryEntry().takeLock();
        }
        public CloneableOpenSupport findCloneableOpenSupport() {
            // must be sync with cookies.add(EditorCookie.class, factory);
            // #12938 XML files do not persist in Source editor
            return (CloneableOpenSupport) getDataObject().getCookie(openSupportCookieClass);
        }
    }
}
