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

package org.netbeans.modules.j2ee.ddloaders.client;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;
import javax.swing.JButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.xml.cookies.CheckXMLCookie;
import org.netbeans.api.xml.cookies.ValidateXMLCookie;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.modules.j2ee.dd.api.client.AppClient;
import org.netbeans.modules.j2ee.dd.api.client.DDProvider;
import org.netbeans.modules.j2ee.dd.api.common.RootInterface;
import org.netbeans.modules.j2ee.dd.impl.client.ClientParseUtils;
import org.netbeans.modules.j2ee.dd.impl.client.AppClientProxy;
import org.netbeans.modules.j2ee.ddloaders.multiview.DDMultiViewDataObject;
import org.netbeans.modules.j2ee.ddloaders.web.DDDataObject;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.xml.multiview.DesignMultiViewDesc;
import org.netbeans.spi.xml.cookies.CheckXMLSupport;
import org.netbeans.spi.xml.cookies.DataObjectAdapters;
import org.netbeans.spi.xml.cookies.ValidateXMLSupport;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataLoaderPool;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.OperationAdapter;
import org.openide.loaders.OperationEvent;
import org.openide.loaders.OperationListener;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 *
 * @author jungi
 */
public class ClientDataObject extends  DDMultiViewDataObject
        implements DDChangeListener, ChangeListener, PropertyChangeListener {
    
    private transient AppClient appClient;
    private transient FileObject srcRoots[];
    private transient FileObjectObserver fileListener;
    
    /** List of updates to servlets that should be processed */
    private Vector updates;
    
    private transient RequestProcessor.Task updateTask;
    
    /** Property name for documentDTD property */
    public static final String PROP_DOCUMENT_DTD = "documentDTD";   // NOI18N
    
    /** Creates a new instance of ClientDataObject */
    public ClientDataObject(FileObject pf, ClientDataLoader loader) throws DataObjectExistsException {
        super(pf, loader);
        init(pf,loader);
    }
    
    private void init(FileObject fo, ClientDataLoader loader) {
        // added ValidateXMLCookie
        InputSource in = DataObjectAdapters.inputSource(this);
        CheckXMLCookie checkCookie = new CheckXMLSupport(in);
        getCookieSet().add(checkCookie);
        ValidateXMLCookie validateCookie = new ValidateXMLSupport(in);
        getCookieSet().add(validateCookie);
        
        fileListener = new FileObjectObserver(fo);
        
        Project project = FileOwnerQuery.getOwner(getPrimaryFile());
        if (project != null) {
            Sources sources = ProjectUtils.getSources(project);
            sources.addChangeListener(this);
        }
        refreshSourceFolders();
        addPropertyChangeListener(this);
    }
    
    private void refreshSourceFolders() {
        ArrayList srcRootList = new ArrayList();
        
        Project project = FileOwnerQuery.getOwner(getPrimaryFile());
        if (project != null) {
            Sources sources = ProjectUtils.getSources(project);
            SourceGroup[] groups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
            for (int i = 0; i < groups.length; i++) {
                if (WebModule.getWebModule(groups [i].getRootFolder()) != null) {
                    srcRootList.add(groups [i].getRootFolder());
                    DataLoaderPool.getDefault().removeOperationListener(operationListener); //avoid being added multiple times
                    DataLoaderPool.getDefault().addOperationListener(operationListener);
                }
            }
        }
        srcRoots = (FileObject []) srcRootList.toArray(new FileObject [srcRootList.size()]);
    }
    
    private String getPackageName(FileObject clazz) {
        for (int i = 0; i < srcRoots.length; i++) {
            String rp = FileUtil.getRelativePath(srcRoots [i], clazz);
            if (rp != null) {
                if (clazz.getExt().length() > 0) {
                    rp = rp.substring(0, rp.length() - clazz.getExt().length() - 1);
                }
                return rp.replace('/', '.');
            }
        }
        return null;
    }
    
    /**
     * This methods gets called when object is changed
     *
     * @param evt - object that describes the change.
     */
    public void deploymentChange(DDChangeEvent evt) {
        /*
        // fix of #28542, don't add servlet, if it's already defined in DD
        if (evt.getType() == DDChangeEvent.SERVLET_ADDED && servletDefined(evt.getNewValue())) {
            return;
        }
         */
        
        synchronized (this) {
            if (updates == null) {
                updates = new Vector();
            }
            updates.addElement(evt);
        }
        
        // schedule processDDChangeEvent
        if (updateTask == null) {
            updateTask = RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    java.util.List changes = null;
                    synchronized (ClientDataObject.this) {
                        if (!ClientDataObject.this.isValid()) {
                            return;
                        }
                        if (updates != null) {
                            changes = updates;
                            updates = null;
                        }
                    }
                    if (changes != null) {
                        showDDChangesDialog(changes);
                    }
                }
            }, 2000, Thread.MIN_PRIORITY);
        } else {
            updateTask.schedule(2000);
        }
    }
    
    /**
     * Invoked when the target of the listener has changed its state.
     *
     *
     * @param e  a ChangeEvent object
     */
    public void stateChanged(ChangeEvent e) {
        refreshSourceFolders ();
    }
    
    /**
     * This method gets called when a bound property is changed.
     *
     * @param evt A PropertyChangeEvent object describing the event source
     *   	and the property that has changed.
     */
    public void propertyChange(PropertyChangeEvent evt) {
        if (ClientDataObject.PROP_DOCUMENT_VALID.equals (evt.getPropertyName ())) {
            ((ClientDataNode)getNodeDelegate()).iconChanged();
        }
    }
    
    protected String getPrefixMark() {
        return "<application-client";
    }

    
    /**
     * MultiViewDesc for MultiView editor
     */
    protected DesignMultiViewDesc[] getMultiViewDesc() {
        
        return new DesignMultiViewDesc[] {};
    }
    
        private class DesignMultiViewDescImpl extends DesignMultiViewDesc {
            public MultiViewElement createElement() {
                return null;
            }
            public Image getIcon() {
                return null;
            }
            public String preferredID() {
                return null;
            }
        }
    /**
     *
     *
     * @throws IOException
     */
    protected void validateDocument() throws IOException {
        // parse document without updating model
        parseDocument(false);
    }
    
    /**
     *
     *
     * @throws IOException
     */
    protected void parseDocument() throws IOException {
        if (appClient == null || ((AppClientProxy) appClient).getOriginal() == null) {
            try {
                appClient = DDProvider.getDefault().getDDRoot(getPrimaryFile());
            } catch (IOException e) {
                if (appClient == null) {
                    appClient = new AppClientProxy(null, null);
                }
            }
        }
        // update model with the document
        parseDocument(true);
    }
    
    /**
     * Used to detect if data model has already been created or not.
     * Method is called before switching to the design view from XML view when the document isn't parseable.
     */
    protected boolean isModelCreated() {
        return (appClient!=null && ((org.netbeans.modules.j2ee.dd.impl.client.AppClientProxy)appClient).getOriginal()!=null);
    }
    
    /**
     * Returns true if xml file is parseable(data model can be created),
     * Method is called before switching to the design view from XML view when the document isn't parseable.
     */
    protected boolean isDocumentParseable() {
        return AppClient.STATE_INVALID_UNPARSABLE != getAppClient().getStatus();
    }
    
    /**
     * Returns model of the deployment descriptor
     *
     * @return the model
     */
    protected RootInterface getDDModel() {
        return getAppClient();
    }
    
    protected org.openide.nodes.Node createNodeDelegate () {
        return new ClientDataNode(this);
    }

    
    private void showDDChangesDialog(List changes) {
        final JButton processButton;
        final JButton processAllButton;
        final JButton closeButton;
        final DDChangesPanel connectionPanel;
        final DialogDescriptor confirmChangesDescriptor;
        final Dialog confirmChangesDialog[] = { null };
        
        processButton = new JButton(NbBundle.getMessage(DDDataObject.class, "LAB_processButton"));
        processButton.setMnemonic(NbBundle.getMessage(DDDataObject.class, "LAB_processButton_Mnemonic").charAt(0));
        processButton.setToolTipText(NbBundle.getMessage(DDDataObject.class, "ACS_processButtonA11yDesc"));
        processAllButton = new JButton(NbBundle.getMessage(DDDataObject.class, "LAB_processAllButton"));
        processAllButton.setMnemonic(NbBundle.getMessage(DDDataObject.class, "LAB_processAllButton_Mnemonic").charAt(0));
        processAllButton.setToolTipText(NbBundle.getMessage(DDDataObject.class, "ACS_processAllButtonA11yDesc"));
        closeButton = new JButton(NbBundle.getMessage(DDDataObject.class, "LAB_closeButton"));
        closeButton.setMnemonic(NbBundle.getMessage(DDDataObject.class, "LAB_closeButton_Mnemonic").charAt(0));
        closeButton.setToolTipText(NbBundle.getMessage(DDDataObject.class, "ACS_closeButtonA11yDesc"));
        final Object [] options = new Object [] {
            processButton,
            processAllButton
        };
        final Object [] additionalOptions = new Object [] {
            closeButton
        };
        WebModule wm = WebModule.getWebModule(getPrimaryFile());
        String fsname=""; // NOI18N
        if (wm!=null) {
            fsname=wm.getContextPath();
        }
        String caption = NbBundle.getMessage(DDDataObject.class, "MSG_SynchronizeCaption", fsname);
        connectionPanel = new DDChangesPanel(caption, processButton);
        confirmChangesDescriptor = new DialogDescriptor(
                connectionPanel,
                NbBundle.getMessage(DDDataObject.class, "LAB_ConfirmDialog"),
                true,
                options,
                processButton,
                DialogDescriptor.RIGHT_ALIGN,
                HelpCtx.DEFAULT_HELP,
                new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (e.getSource() instanceof Component) {
                    Component root;
                    
                    // hack to avoid multiple calls for disposed dialogs:
                    root = javax.swing.SwingUtilities.getRoot((Component)e.getSource());
                    if (!root.isDisplayable()) {
                        return;
                    }
                }
                if (options[0].equals(e.getSource())) {
                    int min = connectionPanel.changesList.getMinSelectionIndex();
                    int max = connectionPanel.changesList.getMaxSelectionIndex();
                    for (int i = max; i >= min; i--) {
                        if (connectionPanel.changesList.isSelectedIndex(i)) {
                            final DDChangeEvent ev = (DDChangeEvent)connectionPanel.listModel.getElementAt(i);
                            processDDChangeEvent(ev);
                            connectionPanel.listModel.removeElementAt(i);
                        }
                    }
                    if (connectionPanel.listModel.isEmpty()) {
                        confirmChangesDialog[0].setVisible(false);
                    } else {
                        processButton.setEnabled(false);
                    }
                } else if (options[1].equals(e.getSource())) {
                    Enumeration en = connectionPanel.listModel.elements();
                    while (en.hasMoreElements()) {
                        processDDChangeEvent((DDChangeEvent)en.nextElement());
                    }
                    confirmChangesDialog[0].setVisible(false);
                    connectionPanel.setChanges(null);
                } else if (additionalOptions[0].equals(e.getSource())) {
                    confirmChangesDialog[0].setVisible(false);
                    connectionPanel.setChanges(null);
                }
            }
        }
        );
        confirmChangesDescriptor.setAdditionalOptions(additionalOptions);
        
        processButton.setEnabled(false);
        processAllButton.requestFocus();
        connectionPanel.setChanges(changes);
        
        try {
            confirmChangesDialog[0] = DialogDisplayer.getDefault().createDialog(confirmChangesDescriptor);
            confirmChangesDialog[0].setVisible(true);
        } finally {
            confirmChangesDialog[0].dispose();
        }
    }
    
    private void processDDChangeEvent(DDChangeEvent evt) {
        System.err.println("ClientDataObject.processDDChangeEvent");
        if (!isValid()) {
            return;
        }
        
        /*
        if (evt.getType () == DDChangeEvent.SERVLET_ADDED) {
            String clz = evt.getNewValue ();
             
            // new from template or copy of another servlet
            String urimapping = "/servlet/"+clz;    // NOI18N
            createDefaultServletConfiguration (clz, urimapping);
        }
        else if (evt.getType () == DDChangeEvent.SERVLET_CHANGED) {
            // update servlet-class in servlet element
            String old = evt.getOldValue ();
            if (old == null) {
                return;
            }
             
            Servlet [] servlets = getAppClient ().getServlet ();
            for (int i=0; i<servlets.length; i++) {
                if (old.equals (servlets[i].getServletClass ())) {
                    servlets[i].setServletClass ((String)evt.getNewValue ());
                }
            }
        }
        else if (evt.getType () == DDChangeEvent.SERVLET_DELETED) {
            // delete servlet and matching servlet-mappings
            String clz = evt.getNewValue ();
            if (clz == null) {
                return;
            }
            WebApp wa = getAppClient ();
            Servlet [] servlets = wa.getServlet ();
            java.util.Vector servletNames = new java.util.Vector ();
            for (int i=0; i<servlets.length; i++) {
                if (clz.equals (servlets[i].getServletClass ())) {
                    servletNames.addElement (servlets[i].getServletName ());
                    wa.removeServlet (servlets[i]);
                }
            }
            ServletMapping [] mappings = wa.getServletMapping ();
            for (int i=0; i<mappings.length; i++) {
                if (servletNames.contains (mappings[i].getServletName ())) {
                    wa.removeServletMapping (mappings[i]);
                }
            }
        }
        else if (evt.getType () == DDChangeEvent.FILTER_CHANGED) {
            String old = evt.getOldValue ();
            if (old == null) {
                return;
            }
             
            Filter [] filters = getAppClient ().getFilter ();
            for (int i=0; i<filters.length; i++) {
                if (old.equals (filters[i].getFilterClass ())) {
                    filters[i].setFilterClass ((String)evt.getNewValue ());
                }
            }
        }
        else if (evt.getType () == DDChangeEvent.FILTER_DELETED) {
            String clz = evt.getNewValue ();
            if (clz == null) {
                return;
            }
             
            WebApp wa = getAppClient ();
            Filter [] filters = wa.getFilter ();
            java.util.Vector filterNames = new java.util.Vector ();
            for (int i=0; i<filters.length; i++) {
                if (clz.equals (filters[i].getFilterClass ())) {
                    filterNames.addElement (filters[i].getFilterName ());
                    wa.removeFilter (filters[i]);
                }
            }
            FilterMapping [] mappings = wa.getFilterMapping ();
            for (int i=0; i<mappings.length; i++) {
                if (filterNames.contains (mappings[i].getFilterName ())) {
                    wa.removeFilterMapping (mappings[i]);
                }
            }
        }
        else if (evt.getType () == DDChangeEvent.LISTENER_CHANGED) {
            String old = evt.getOldValue ();
            if (old == null) {
                return;
            }
             
            Listener [] listeners = getAppClient ().getListener ();
            for (int i=0; i<listeners.length; i++) {
                if (old.equals (listeners[i].getListenerClass ())) {
                    listeners[i].setListenerClass ((String)evt.getNewValue ());
                }
            }
        }
        else if (evt.getType () == DDChangeEvent.LISTENER_DELETED) {
            String clz = evt.getNewValue ();
            if (clz == null) {
                return;
            }
             
            WebApp wa = getAppClient ();
            Listener [] listeners = wa.getListener ();
            for (int i=0; i<listeners.length; i++) {
                if (clz.equals (listeners[i].getListenerClass ())) {
                    wa.removeListener (listeners[i]);
                    break;
                }
            }
        }
        try {
            writeModel(getAppClient());
        } catch (IOException e) {
            ErrorManager.getDefault().notify(e);
        }
             */
    }
    
    private void parseDocument(boolean updateWebApp) throws IOException {
        AppClientProxy webAppProxy = (AppClientProxy) appClient;
        try {
            // preparsing
            SAXParseException error = ClientParseUtils.parse(new InputSource(createReader()));
            setSaxError(error);

            String version = ClientParseUtils.getVersion(new InputSource(createReader()));
            // creating model
            AppClientProxy app = new AppClientProxy(org.netbeans.modules.j2ee.dd.impl.common.DDUtils.createAppClient(
                    createInputStream(), version), version);
            if (updateWebApp) {
                String webAppProxyVersion = webAppProxy.getVersion() != null ? webAppProxy.getVersion().toString() : "";
                if (version.equals(webAppProxyVersion) && webAppProxy.getOriginal() != null) {
                    appClient.merge(app, AppClient.MERGE_UPDATE);
                } else if (app.getOriginal() != null) {
                    appClient = webAppProxy = app;
                }
            }
            webAppProxy.setStatus(error != null ? AppClient.STATE_INVALID_PARSABLE : AppClient.STATE_VALID);
            webAppProxy.setError(error);
        } catch (SAXException ex) {
            webAppProxy.setStatus(AppClient.STATE_INVALID_UNPARSABLE);
            if (ex instanceof SAXParseException) {
                webAppProxy.setError((SAXParseException) ex);
            } else if (ex.getException() instanceof SAXParseException) {
                webAppProxy.setError((SAXParseException) ex.getException());
            }
            setSaxError(ex);
        }
    }

    public AppClient getAppClient() {
        if (appClient == null) {
            try {
                appClient = createWebApp();
            } catch (IOException ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            }
        }
        return appClient;
    }

    private AppClient createWebApp() throws java.io.IOException {
        AppClient webApp = DDProvider.getDefault().getDDRoot(getPrimaryFile());
        if (webApp != null) {
            setSaxError(webApp.getError());
        }
        return webApp;
    }

    private OperationListener operationListener = new OperationAdapter() {
        public void operationDelete(OperationEvent ev) {
            FileObject fo = ev.getObject().getPrimaryFile();
            String resourceName = getPackageName(fo);
            if (resourceName != null && "java".equals(fo.getExt())) { //NOI18N
                boolean foundElement=false;
                /*
                Servlet[] servlets = getAppClient().getServlet();
                for (int i=0;i<servlets.length;i++) {
                    if (resourceName.equals(servlets[i].getServletClass())) {
                        DDChangeEvent ddEvent = new DDChangeEvent(DDDataObject.this,DDDataObject.this,null,resourceName,DDChangeEvent.SERVLET_DELETED);
                        deploymentChange (ddEvent);
                        foundElement=true;
                        break;
                    }
                }
                if (foundElement) {
                    return;
                }
                Filter[] filters = getAppClient().getFilter();
                for (int i=0;i<filters.length;i++) {
                    if (resourceName.equals(filters[i].getFilterClass())) {
                        DDChangeEvent ddEvent = new DDChangeEvent(DDDataObject.this,DDDataObject.this,null,resourceName,DDChangeEvent.FILTER_DELETED);
                        deploymentChange (ddEvent);
                        foundElement=true;
                        break;
                    }
                }
                if (foundElement) {
                    return;
                }
                Listener[] listeners = getAppClient().getListener();
                for (int i=0;i<listeners.length;i++) {
                    if (resourceName.equals(listeners[i].getListenerClass())) {
                        DDChangeEvent ddEvent = new DDChangeEvent(DDDataObject.this,DDDataObject.this,null,resourceName,DDChangeEvent.LISTENER_DELETED);
                        deploymentChange (ddEvent);
                        break; // listener with that class should be only one
                    }
                }
                 */
            }
        }
    };
    
    /** WeakListener for accepting external changes to web.xml
     */
    private class FileObjectObserver implements FileChangeListener {
        FileObjectObserver(FileObject fo) {
            fo.addFileChangeListener((FileChangeListener)org.openide.util.WeakListeners.create(
                    FileChangeListener.class, this, fo));
        }
        
        public void fileAttributeChanged(FileAttributeEvent fileAttributeEvent) {
        }
        
        public void fileChanged(FileEvent fileEvent) {
            /*
           WebAppProxy appClient = (WebAppProxy) DDDataObject.this.getAppClient();
           boolean needRewriting = true;
           if (appClient!= null && appClient.isWriting()) { // change from outside
               appClient.setWriting(false);
               needRewriting=false;
           }
           if (isSavingDocument()) {// document is being saved
               setSavingDocument(false);
               needRewriting=false;
           }
           if (needRewriting) getEditorSupport().restartTimer();
             */
        }
        
        public void fileDataCreated(FileEvent fileEvent) {
        }
        
        public void fileDeleted(FileEvent fileEvent) {
        }
        
        public void fileFolderCreated(FileEvent fileEvent) {
        }
        
        public void fileRenamed(FileRenameEvent fileRenameEvent) {
        }
    }
    
}
