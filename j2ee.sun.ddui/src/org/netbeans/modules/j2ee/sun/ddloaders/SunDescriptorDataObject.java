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

package org.netbeans.modules.j2ee.sun.ddloaders;

//import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.core.spi.multiview.MultiViewElement;
//import org.netbeans.modules.j2ee.common.Util;
//import org.netbeans.modules.j2ee.dd.api.common.RootInterface;
//import org.netbeans.modules.j2ee.dd.api.ejb.*;
//import org.netbeans.modules.j2ee.dd.impl.common.DDUtils;
//import org.netbeans.modules.j2ee.dd.impl.ejb.EjbJarProxy;
//import org.netbeans.modules.j2ee.ddloaders.ejb.DDChangeEvent;
//import org.netbeans.modules.j2ee.ddloaders.ejb.DDChangeListener;
//import org.netbeans.modules.j2ee.ddloaders.ejb.EjbJarDataLoader;
//import org.netbeans.modules.j2ee.common.DDEditorNavigator;
import org.netbeans.modules.xml.multiview.DesignMultiViewDesc;
import org.netbeans.modules.xml.multiview.SectionNode;
import org.netbeans.modules.xml.multiview.ui.SectionNodeInnerPanel;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;
import org.netbeans.modules.xml.multiview.ToolBarMultiViewElement;
import org.netbeans.api.xml.cookies.CheckXMLCookie;
import org.netbeans.api.xml.cookies.ValidateXMLCookie;
import org.netbeans.spi.xml.cookies.CheckXMLSupport;
import org.netbeans.spi.xml.cookies.DataObjectAdapters;
import org.netbeans.spi.xml.cookies.ValidateXMLSupport;
import org.openide.ErrorManager;
import org.openide.cookies.ViewCookie;
import org.openide.filesystems.*;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.nodes.CookieSet;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.xml.sax.InputSource;
import javax.swing.event.ChangeListener;
import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.enterprise.deploy.shared.ModuleType;
import org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion;
import org.netbeans.modules.j2ee.sun.dd.api.DDProvider;
import org.netbeans.modules.j2ee.sun.dd.api.RootInterface;
import org.netbeans.modules.j2ee.sun.dd.impl.RootInterfaceImpl;
import org.netbeans.modules.j2ee.sun.dd.impl.ejb.SunEjbJarProxy;
import org.netbeans.modules.j2ee.sun.ddloaders.editor.ErrorAnnotation;
import org.netbeans.modules.j2ee.sun.ddloaders.DDMultiViewDataObject;
//import org.openide.awt.HtmlBrowser;
import org.netbeans.modules.schema2beans.Schema2BeansException;
import org.netbeans.modules.schema2beans.Schema2BeansRuntimeException;
import org.openide.awt.HtmlBrowser;
import org.openide.loaders.MultiDataObject;
import org.openide.util.Mutex;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Represents a DD object in the Repository.
 *
 * @author pfiala
 * @author Peter Williams
 */
public class SunDescriptorDataObject extends DDMultiViewDataObject
//        implements 
//        DDChangeListener, 
//        DDEditorNavigator, 
//        FileChangeListener, 
//        ChangeListener
{

    /**
     * Property name for documentDTD property
     */
    public static final String PROP_DOCUMENT_DTD = "documentDTD";   // NOI18N
    
    // Serialization
    private static final long serialVersionUID = 8957663189355029479L;
    
    
    private Object proxyMonitor = new Object();
    private volatile RootInterfaceImpl ddRootProxy;
//    private FileObject srcRoots[];
    private PropertyChangeListener ddRootChangeListener;
//    private Map entityHelperMap = new HashMap();
//    private Map sessionHelperMap = new HashMap();
    private DDType descriptorType;
    
    public SunDescriptorDataObject(FileObject pf, SunDescriptorDataLoader loader) throws DataObjectExistsException {
        super(pf, loader);
        
        descriptorType = DDType.getDDType(pf.getNameExt());
        
        // XML Validation cookies
        InputSource in = DataObjectAdapters.inputSource(this);
        CheckXMLCookie checkCookie = new CheckXMLSupport(in);
        getCookieSet().add(checkCookie);
        ValidateXMLCookie validateCookie = new ValidateXMLSupport(in);
        CookieSet set = getCookieSet();
        set.add(validateCookie);
        
//        Project project = getProject();
//        if (project != null) {
//            Sources sources = ProjectUtils.getSources(project);
//            sources.addChangeListener(this);
//        }
//        refreshSourceFolders();
    }
    
    /** Returns what the module type ought to be for this particular descriptor 
     *  file (e.g. if someone puts sun-ejb-jar.xml into a web module folder, this
     *  api will return ModuleType.EJB for this dataobject even though j2eeserver
     *  will return ModuleType.WAR for the project's module type.
     */
    public ModuleType getModuleType() {
        // FIXME What should this return for a sun-resource.xml file?  Right, it returns null.
        return descriptorType.getEditorModuleType();
    }
    
    
//    private void refreshSourceFolders() {
//        ArrayList srcRootList = new ArrayList();
//        
//        SourceGroup[] groups;
//        Project project = getProject();
//        if (project != null) {
//            Sources sources = ProjectUtils.getSources(project);
//            groups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
//        } else {
//            groups = null;
//        }
//        if (groups != null) {
//            for (int i = 0; i < groups.length; i++) {
//                org.netbeans.modules.j2ee.api.ejbjar.EjbJar ejbModule = org.netbeans.modules.j2ee.api.ejbjar.EjbJar.getEjbJar(groups[i].getRootFolder());
//                if ((ejbModule != null) && (ejbModule.getDeploymentDescriptor() != null)) {
//                    try {
//                        FileObject fo = groups[i].getRootFolder();
//                        srcRootList.add(groups[i].getRootFolder());
//                        FileSystem fs = fo.getFileSystem();
//                        fs.removeFileChangeListener(this); //avoid being added multiple times
//                        fs.addFileChangeListener(this);
//                    } catch (FileStateInvalidException ex) {
//                        ErrorManager.getDefault().notify(ex);
//                    }
//                }
//            }
//        }
//        srcRoots = (FileObject[]) srcRootList.toArray(new FileObject[srcRootList.size()]);
//    }
    
    
    private Project getProject() {
        return FileOwnerQuery.getOwner(getPrimaryFile());
    }
    
    public FileObject getProjectDirectory() {
        Project project = getProject();
        return project == null ? null : project.getProjectDirectory();
    }
    
//    public SourceGroup[] getSourceGroups() {
//        Project project = getProject();
//        if (project != null) {
//            return ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
//        } else {
//            return null;
//        }
//    }
    
//    private String getPackageName(FileObject clazz) {
//        for (int i = 0; i < srcRoots.length; i++) {
//            String rp = FileUtil.getRelativePath(srcRoots[i], clazz);
//            if (rp != null) {
//                if (clazz.getExt().length() > 0) {
//                    rp = rp.substring(0, rp.length() - clazz.getExt().length() - 1);
//                }
//                return rp.replace('/', '.');
//            }
//        }
//        return null;
//    }

    public ASDDVersion getASDDVersion() {
        // !PW FIXME default version ought to be current project server version,
        // if any, otherwise, current installed server, if any.
        return DDProvider.getASDDVersion(getDDModel(), ASDDVersion.SUN_APPSERVER_8_1);
    }
    
    public RootInterface getDDRoot() {
        return getDDRootImpl(true);
    }
    
    private RootInterface getDDRootImpl(final boolean notify) {
        RootInterface localProxy = null;
        synchronized (proxyMonitor) {
            if (ddRootProxy == null) {
                try {
                    parseDocument();
                } catch (IOException ex) {
                    if(notify) {
                        notifyError(ex);
                    } else {
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                    }
                }
            }
            localProxy = ddRootProxy != null ? ddRootProxy.getRootInterface() : null;
        }
        return localProxy;
    }
    
    @Override
    protected Node createNodeDelegate() {
        return new SunDescriptorDataNode(this);
    }
    
    /**
     * gets the Icon Base for node delegate when parser accepts the xml document as valid
     * <p/>
     * PENDING: move into node
     *
     * @return Icon Base for node delegate
     */
    protected String getIconBaseForValidDocument() {
        return Utils.ICON_BASE_DD_VALID;
    }
    
    /**
     * gets the Icon Base for node delegate when parser finds error(s) in xml document
     *
     * @return Icon Base for node delegate
     *         <p/>
     *         PENDING: move into node
     */
    protected String getIconBaseForInvalidDocument() {
        return Utils.ICON_BASE_DD_INVALID; // NOI18N
    }
    
    @Override
    protected DataObject handleCopy(DataFolder f) throws IOException {
        DataObject dataObject = super.handleCopy(f);
        try {
            dataObject.setValid(false);
        } catch (PropertyVetoException e) {
            // should not occur
        }
        return dataObject;
    }
    
    /**
     * This methods gets called when servlet is changed
     *
     * @param evt - object that describes the change.
     */
//    public void deploymentChange(DDChangeEvent evt) {
//    }
    
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
//    private RequestProcessor.Task elementTask;
//    private List deletedEjbNames;
//    private List newFileNames;
//    
//    private void elementCreated(final String elementName) {
//        synchronized (this) {
//            if (newFileNames == null) {
//                newFileNames = new ArrayList();
//            }
//            newFileNames.add(elementName);
//        }
//        
//        if (elementTask == null) {
//            elementTask = RequestProcessor.getDefault().post(new Runnable() {
//                public void run() {
//                    if (deletedEjbNames != null) {
//                        for (int i = 0; i < deletedEjbNames.size(); i++) {
//                            String deletedServletName = (String) deletedEjbNames.get(i);
//                            String deletedName = deletedServletName;
//                            int index = deletedServletName.lastIndexOf("."); //NOI18N
//                            if (index > 0) {
//                                deletedName = deletedServletName.substring(index + 1);
//                            }
//                            boolean found = false;
//                            for (int j = 0; j < newFileNames.size(); j++) {
//                                String newFileName = (String) newFileNames.get(j);
//                                String newName = newFileName;
//                                int ind = newFileName.lastIndexOf("."); //NOI18N
//                                if (ind > 0) {
//                                    newName = newFileName.substring(ind + 1);
//                                }
//                                if (deletedName.equals(newName)) { // servlet was removed
//                                    found = true;
//                                    DDChangeEvent ddEvent =
//                                            new DDChangeEvent(SunDescriptorDataObject.this,
//                                            SunDescriptorDataObject.this, deletedServletName, newFileName,
//                                            DDChangeEvent.EJB_CHANGED);
//                                    deploymentChange(ddEvent);
//                                    synchronized (SunDescriptorDataObject.this) {
//                                        newFileNames.remove(newFileName);
//                                    }
//                                    break;
//                                }
//                            }
//                            if (!found) {
//                                DDChangeEvent ddEvent =
//                                        new DDChangeEvent(SunDescriptorDataObject.this,
//                                        SunDescriptorDataObject.this, null, deletedServletName,
//                                        DDChangeEvent.EJB_DELETED);
//                                deploymentChange(ddEvent);
//                            }
//                        } //end for
//                        synchronized (SunDescriptorDataObject.this) {
//                            deletedEjbNames = null;
//                        }
//                    } // servlets
//                    
//                    synchronized (SunDescriptorDataObject.this) {
//                        newFileNames = null;
//                    }
//                    
//                }///end run
//}, 1500, Thread.MIN_PRIORITY);
//        } else {
//            elementTask.schedule(1500);
//        }
//    }
    
//    public void fileRenamed(FileRenameEvent fileRenameEvent) {
//        FileObject fo = fileRenameEvent.getFile();
//        String resourceName = getPackageName(fo);
//        if (resourceName != null) {
//            int index = resourceName.lastIndexOf("."); //NOI18N
//            String oldName = fileRenameEvent.getName();
//            String oldResourceName = (index >= 0 ? resourceName.substring(0, index + 1) : "") + oldName;
//            EjbJar ddRoot = getEjbJar();
//            if (ddRoot.getStatus() == EjbJar.STATE_VALID) {
//                fireEvent(oldResourceName, resourceName, DDChangeEvent.EJB_CHANGED);
//            }
//        }
//    }
//    
//    public void fileFolderCreated(FileEvent fileEvent) {
//    }
//    
//    public void fileDeleted(FileEvent fileEvent) {
//        FileObject fo = fileEvent.getFile();
//        String resourceName = getPackageName(fo);
//        if (resourceName != null) {
//            if (newFileNames == null) {
//                fireEvent(null, resourceName, DDChangeEvent.EJB_DELETED);
//            } else {
//                Ejb[] ejbs = getEjbJar().getEnterpriseBeans().getEjbs();
//                for (int i = 0; i < ejbs.length; i++) {
//                    if (resourceName.equals(ejbs[i].getEjbClass())) {
//                        synchronized (this) {
//                            if (deletedEjbNames == null) {
//                                deletedEjbNames = new ArrayList();
//                            }
//                            deletedEjbNames.add(resourceName);
//                        }
//                        break;
//                    }
//                }
//            }
//        }
//    }
//    
//    public void fileDataCreated(FileEvent fileEvent) {
//        FileObject fo = fileEvent.getFile();
//        String resourceName = getPackageName(fo);
//        if (resourceName != null) {
//            elementCreated(resourceName);
//        }
//    }
//    
//    public void fileChanged(FileEvent fileEvent) {
//    }
//    
//    public void fileAttributeChanged(FileAttributeEvent fileAttributeEvent) {
//    }
    
//    public void stateChanged(javax.swing.event.ChangeEvent e) {
//        refreshSourceFolders();
//    }
    
    protected void parseDocument() throws IOException {
        DDProvider ddProvider = DDProvider.getDefault();
        SAXParseException saxEx = null;
        synchronized (proxyMonitor) {
            if(ddRootProxy == null || !ddRootProxy.hasOriginal()) {
                try {
                    RootInterfaceImpl newDDRoot = (RootInterfaceImpl) ddProvider.getDDRoot(getPrimaryFile());
                    if(ddRootProxy != null && ddRootChangeListener != null) {
                        ddRootProxy.removePropertyChangeListener(ddRootChangeListener);
                    }
                    ddRootProxy = newDDRoot;
                    if(ddRootProxy != null) {
                        if(ddRootChangeListener == null) {
                            ddRootChangeListener = new SunDDPropertyChangeListener();
                        }
                        ddRootProxy.addPropertyChangeListener(ddRootChangeListener);
                    }
                } catch(IOException ex) {
//                    if (ddRootProxy == null) {
//                        setSunDDRoot(new NullProxy(null, null));
//                    }
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                }
            } else {
                ddProvider.merge(ddRootProxy.getRootInterface(), createReader());
            }
            saxEx = ddRootProxy != null ? ddRootProxy.getError() : new SAXParseException("No proxy object found created by parser.", null);
        }
        
//        if(saxEx != null) {
//            System.out.println(this.getName() + ": Parse error = " + saxEx.getMessage());
//        } else {
//            System.out.println(this.getName() + " parsed successfully.");
//        }
               
        setSaxError(saxEx);
    }
    
    protected void validateDocument() throws IOException {
        try {
            RootInterfaceImpl proxyImpl = (RootInterfaceImpl) DDProvider.getDefault().getDDRoot(createReader());
            setSaxError(proxyImpl.getError());
        } catch(Schema2BeansException ex) {
            setSaxError(new SAXException(ex));
        } catch(Schema2BeansRuntimeException ex) {
            setSaxError(new SAXException(ex));
        } catch(SAXException ex) {
            setSaxError(ex);
        }
    }
    
    protected RootInterface getDDModel() {
        return getDDRootImpl(false);
    }
    
    public boolean isDocumentParseable() {
        RootInterface ddRoot = getDDRoot();
        return ddRoot != null ? (ddRoot.getStatus() != RootInterface.STATE_INVALID_UNPARSABLE) : false;
    }
    
    protected String getPrefixMark() {
        // Not used anywhere at this time (ever?) so no point in writing the code
        // to figure this out (lookup table, etc.)
        return "<notused";
    }
    
    /** MultiViewDesc for MultiView editor
     */
    protected DesignMultiViewDesc[] getMultiViewDesc() {
        DDViewFactory factory = DDViewFactory.getViewFactory(descriptorType);
        
        if(factory != null) {
            return factory.getMultiViewDesc(this);
        }
        
        return new DDViewFactory.DDView[0];
    }
    
    /** Used to detect if data model has already been created or not.
     * Method is called before switching to the design view from XML view when the document isn't parseable.
     */
    protected boolean isModelCreated() {
        boolean result = false;
        synchronized (proxyMonitor) {
            result = ddRootProxy != null && ddRootProxy.hasOriginal();
        }
        return result;
    }
    
    @Override
    public void showElement(final Object element) {
//        if (element instanceof Relationships || element instanceof EjbRelation) {
//            openView(1);
//        } else {
//            openView(0);
//        }
        Mutex.EVENT.readAccess(new Runnable() {
            public void run() {
                final SectionNodeView sectionView =
                        (SectionNodeView) SunDescriptorDataObject.this.getActiveMVElement().getSectionView();
                final Node root = sectionView.getRoot();
                final SectionNode node = ((SectionNode) root.getChildren().getNodes()[0]).getNodeForElement(element);
                if (node != null) {
                    sectionView.openPanel(node);
                    ((SectionNodeInnerPanel) node.getSectionNodePanel().getInnerPanel()).focusData(element);
                }
            }
        });
    }
    
    
    /** Enable to access Active element
     * 
     * @return toolbar element to use for this editor.
     */
    public ToolBarMultiViewElement getActiveMVElement() {
        return (ToolBarMultiViewElement) super.getActiveMultiViewElement();
    }
    
//    private Ejb getEjbFromEjbClass(String ejbClassName) {
//        EnterpriseBeans enterpriseBeans = getEjbJar().getEnterpriseBeans();
//        if(enterpriseBeans != null) {
//            Ejb[] ejbs = enterpriseBeans.getEjbs();
//            for (int i = 0; i < ejbs.length; i++) {
//                if (ejbs[i].getEjbClass() != null && ejbs[i].getEjbClass().equals(ejbClassName)) {
//                    return ejbs[i];
//                }
//            }
//        }
//        return null;
//    }
    
//    private int getBeanInterfaceType(String interfaceName) {
//        int interfaceType = -1;
//        EjbJar ddRoot = getEjbJar();
//        if (ddRoot == null) {
//            return interfaceType;
//        }
//        EnterpriseBeans eb = ddRoot.getEnterpriseBeans();
//        if (eb == null) {
//            return interfaceType;
//        }
//        EntityAndSession[] beans = eb.getSession();
//        for (int i = 0; i < beans.length; i++) {
//            if (beans[i].getHome() != null &&
//                    beans[i].getHome().equals(interfaceName)) {
//                interfaceType = HOME;
//                break;
//            }
//            if (beans[i].getRemote() != null &&
//                    beans[i].getRemote().equals(interfaceName)) {
//                interfaceType = REMOTE;
//                break;
//            }
//            if (beans[i].getLocalHome() != null &&
//                    beans[i].getLocalHome().equals(interfaceName)) {
//                interfaceType = LOCAL_HOME;
//                break;
//            }
//            if (beans[i].getLocal() != null &&
//                    beans[i].getLocal().equals(interfaceName)) {
//                interfaceType = LOCAL;
//                break;
//            }
//        }
//        return interfaceType;
//    }
    
//    private int getSpecificEvent(int eventType, int interfaceType) {
//        if (eventType == DDChangeEvent.EJB_CHANGED) {
//            switch (interfaceType) {
//                case HOME:
//                {
//                    return DDChangeEvent.EJB_HOME_CHANGED;
//                }
//                case REMOTE:
//                {
//                    return DDChangeEvent.EJB_REMOTE_CHANGED;
//                }
//                case LOCAL_HOME:
//                {
//                    return DDChangeEvent.EJB_LOCAL_HOME_CHANGED;
//                }
//                case LOCAL:
//                {
//                    return DDChangeEvent.EJB_LOCAL_CHANGED;
//                }
//            }
//        }
//        if (eventType == DDChangeEvent.EJB_DELETED) {
//            switch (interfaceType) {
//                case HOME:
//                {
//                    return DDChangeEvent.EJB_HOME_DELETED;
//                }
//                case REMOTE:
//                {
//                    return DDChangeEvent.EJB_REMOTE_DELETED;
//                }
//                case LOCAL_HOME:
//                {
//                    return DDChangeEvent.EJB_LOCAL_HOME_DELETED;
//                }
//                case LOCAL:
//                {
//                    return DDChangeEvent.EJB_LOCAL_DELETED;
//                }
//            }
//        }
//        return -1;
//    }
    
    private boolean fireEvent(String oldResourceName, String resourceName, int eventType) {
// TODO what should this do?
//        boolean elementFound = false;
//        String resource;
//        int specificEventType = -1;
//        if (eventType == DDChangeEvent.EJB_CHANGED) {
//            resource = oldResourceName;
//        } else {
//            resource = resourceName;
//        }
//        Ejb ejb = getEjbFromEjbClass(resource);
//        
//        if (ejb != null) {
//            if (eventType == DDChangeEvent.EJB_CHANGED) {
//                specificEventType = DDChangeEvent.EJB_CLASS_CHANGED;
//            } else {
//                specificEventType = DDChangeEvent.EJB_CLASS_DELETED;
//            }
//            elementFound = true;
//        }
//        
//        if (!elementFound) {
//            int interfaceType = getBeanInterfaceType(resource);
//            
//            if (interfaceType > 0) {
//                specificEventType =
//                        getSpecificEvent(eventType, interfaceType);
//                elementFound = true;
//            }
//        }
//        if (elementFound) {
//            assert(specificEventType > 0);
//            DDChangeEvent ddEvent =
//                    new DDChangeEvent(this, this, oldResourceName,
//                    resourceName, specificEventType);
//            deploymentChange(ddEvent);
//        }
//        return elementFound;
        return false;
    }
    
// DEAD CODE    
//    public EntityHelper getEntityHelper(Entity entity) {
//        EntityHelper entityHelper = (EntityHelper) entityHelperMap.get(entity);
//        if (entityHelper == null) {
//            entityHelper = new EntityHelper(this, entity);
//            entityHelperMap.put(entity, entityHelper);
//        }
//        return entityHelper;
//    }
//    
//    public SessionHelper getSessionHelper(Session session) {
//        SessionHelper sessionHelper = (SessionHelper) sessionHelperMap.get(session);
//        if (sessionHelper == null) {
//            sessionHelper = new SessionHelper(this, session);
//            sessionHelperMap.put(session, sessionHelper);
//        }
//        return sessionHelper;
//    }
    
    private static class SunDDPropertyChangeListener implements PropertyChangeListener {
        
        public void propertyChange(PropertyChangeEvent evt) {
// TODO what should this do?
//            if (EjbJar.PROPERTY_STATUS.equals(evt.getPropertyName())) {
//                return;
//            }
//            Object source = evt.getSource();
//            if (source instanceof EnterpriseBeans) {
//                Object oldValue = evt.getOldValue();
//                Object newValue = evt.getNewValue();
//                if ((oldValue instanceof Entity || newValue instanceof Entity)) {
//                    entityHelperMap.keySet().retainAll(Arrays.asList(((EnterpriseBeans) source).getEntity()));
//                } else if ((oldValue instanceof Session || newValue instanceof Session)) {
//                    sessionHelperMap.keySet().retainAll(Arrays.asList(((EnterpriseBeans) source).getSession()));
//                }
//            }
        }
    }
    
    
}
