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

package org.netbeans.modules.j2ee.ddloaders.multiview;

import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.xml.cookies.ValidateXMLCookie;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.modules.j2ee.dd.api.ejb.*;
import org.netbeans.modules.j2ee.dd.impl.ejb.EjbJarProxy;
import org.netbeans.modules.j2ee.ddloaders.ejb.DDChangeEvent;
import org.netbeans.modules.j2ee.ddloaders.ejb.DDChangeListener;
import org.netbeans.modules.j2ee.ddloaders.ejb.EjbJarDDUtils;
import org.netbeans.modules.j2ee.ddloaders.ejb.EjbJarDataLoader;
import org.netbeans.modules.j2ee.ejbjarproject.ui.logicalview.ejb.shared.DDEditorNavigator;
import org.netbeans.modules.j2ee.spi.ejbjar.EjbJarImplementation;
import org.netbeans.modules.xml.multiview.DesignMultiViewDesc;
import org.netbeans.modules.xml.multiview.SectionNode;
import org.netbeans.modules.xml.multiview.XmlMultiViewDataObject;
import org.netbeans.modules.xml.multiview.ui.SectionNodeInnerPanel;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;
import org.netbeans.modules.xml.multiview.ToolBarMultiViewElement;
import org.netbeans.spi.xml.cookies.DataObjectAdapters;
import org.netbeans.spi.xml.cookies.ValidateXMLSupport;
import org.openide.ErrorManager;
import org.openide.filesystems.*;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.swing.event.ChangeListener;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a DD object in the Repository.
 *
 * @author pfiala
 */
public class EjbJarMultiViewDataObject extends XmlMultiViewDataObject
        implements DDChangeListener, EjbJarProxy.OutputProvider, DDEditorNavigator, FileChangeListener, ChangeListener {

    private EjbJarProxy ejbJar;
    private FileObject srcRoots[];
    protected final static RequestProcessor RP = new RequestProcessor("XML Parsing");   // NOI18N
    private PropertyChangeListener ejbJarChangeListener;
    private Map entityHelperMap = new HashMap();
    private Map sessionHelperMap = new HashMap();

    private static final long serialVersionUID = 8857563089355069362L;

    /**
     * Property name for documentDTD property
     */
    public static final String PROP_DOCUMENT_DTD = "documentDTD";   // NOI18N

    private static final int HOME = 10;
    private static final int REMOTE = 20;
    private static final int LOCAL_HOME = 30;
    private static final int LOCAL = 40;
    private static final String OVERVIEW = Utils.getBundleMessage("LBL_Overview");
    private static final String CMP_RELATIONSHIPS = Utils.getBundleMessage("LBL_CmpRelationships");

    public EjbJarMultiViewDataObject(FileObject pf, EjbJarDataLoader loader) throws DataObjectExistsException {
        super(pf, loader);

        // added ValidateXMLCookie
        InputSource in = DataObjectAdapters.inputSource(this);
        ValidateXMLCookie validateCookie = new ValidateXMLSupport(in);
        getCookieSet().add(validateCookie);

        Project project = getProject();
        if (project != null) {
            Sources sources = ProjectUtils.getSources(project);
            sources.addChangeListener(this);
        }
        refreshSourceFolders();
    }

    private void refreshSourceFolders() {
        ArrayList srcRootList = new ArrayList();

        SourceGroup[] groups;
        Project project = getProject();
        if (project != null) {
            Sources sources = ProjectUtils.getSources(project);
            groups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        } else {
            groups = null;
        }
        if (groups != null) {
            for (int i = 0; i < groups.length; i++) {
                EjbJarImplementation jarImpl = (EjbJarImplementation) project.getLookup().lookup(
                        EjbJarImplementation.class);

                if ((jarImpl != null) && (jarImpl.getDeploymentDescriptor() != null)) {
                    try {
                        FileObject fo = groups[i].getRootFolder();
                        srcRootList.add(groups[i].getRootFolder());
                        FileSystem fs = fo.getFileSystem();
                        fs.removeFileChangeListener(this); //avoid being added multiple times
                        fs.addFileChangeListener(this);
                    } catch (FileStateInvalidException ex) {
                        ErrorManager.getDefault().notify(ex);
                    }
                }
            }
        }
        srcRoots = (FileObject[]) srcRootList.toArray(new FileObject[srcRootList.size()]);
    }


    private Project getProject() {
        return FileOwnerQuery.getOwner(getPrimaryFile());
    }

    public FileObject getProjectDirectory() {
        Project project = getProject();
        return project == null ? null : project.getProjectDirectory();
    }

    public SourceGroup[] getSourceGroups() {
        Project project = getProject();
        if (project != null) {
            return ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        } else {
            return null;
        }
    }

    private String getPackageName(FileObject clazz) {
        for (int i = 0; i < srcRoots.length; i++) {
            String rp = FileUtil.getRelativePath(srcRoots[i], clazz);
            if (rp != null) {
                if (clazz.getExt().length() > 0) {
                    rp = rp.substring(0, rp.length() - clazz.getExt().length() - 1);
                }
                return rp.replace('/', '.');
            }
        }
        return null;
    }

    public EjbJar getEjbJar() {
        if (ejbJar == null) {
            try {
                parseDocument(false);
            } catch (IOException e) {
                Utils.notifyError(e);
            }
        }
        return ejbJar;
    }

    protected Node createNodeDelegate() {
        return new EjbJarMultiViewDataNode(this);
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

    /**
     * Method from EjbJarProxy.OutputProvider
     */
    public void write(EjbJar ejbJarProxy) throws IOException {
        writeModel();
    }

    /**
     * Method from EjbJarProxy.OutputProvider
     */
    public FileObject getTarget() {
        return getPrimaryFile();
    }

    protected DataObject handleCopy(DataFolder f) throws IOException {
        DataObject dataObject = super.handleCopy(f);
        try {
            dataObject.setValid(false);
        } catch (PropertyVetoException e) {
        }
        return dataObject;
    }

    /**
     * This methods gets called when servlet is changed
     *
     * @param evt - object that describes the change.
     */
    public void deploymentChange(DDChangeEvent evt) {
    }


    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    private RequestProcessor.Task elementTask;
    private List deletedEjbNames;
    private List newFileNames;

    private void elementCreated(final String elementName) {
        synchronized (this) {
            if (newFileNames == null) {
                newFileNames = new ArrayList();
            }
            newFileNames.add(elementName);
        }

        if (elementTask == null) {
            elementTask = RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    if (deletedEjbNames != null) {
                        for (int i = 0; i < deletedEjbNames.size(); i++) {
                            String deletedServletName = (String) deletedEjbNames.get(i);
                            String deletedName = deletedServletName;
                            int index = deletedServletName.lastIndexOf("."); //NOI18N
                            if (index > 0) {
                                deletedName = deletedServletName.substring(index + 1);
                            }
                            boolean found = false;
                            for (int j = 0; j < newFileNames.size(); j++) {
                                String newFileName = (String) newFileNames.get(j);
                                String newName = newFileName;
                                int ind = newFileName.lastIndexOf("."); //NOI18N
                                if (ind > 0) {
                                    newName = newFileName.substring(ind + 1);
                                }
                                if (deletedName.equals(newName)) { // servlet was removed
                                    found = true;
                                    DDChangeEvent ddEvent =
                                            new DDChangeEvent(EjbJarMultiViewDataObject.this,
                                                    EjbJarMultiViewDataObject.this, deletedServletName, newFileName,
                                                    DDChangeEvent.EJB_CHANGED);
                                    deploymentChange(ddEvent);
                                    synchronized (EjbJarMultiViewDataObject.this) {
                                        newFileNames.remove(newFileName);
                                    }
                                    break;
                                }
                            }
                            if (!found) {
                                DDChangeEvent ddEvent =
                                        new DDChangeEvent(EjbJarMultiViewDataObject.this,
                                                EjbJarMultiViewDataObject.this, null, deletedServletName,
                                                DDChangeEvent.EJB_DELETED);
                                deploymentChange(ddEvent);
                            }
                        } //end for
                        synchronized (EjbJarMultiViewDataObject.this) {
                            deletedEjbNames = null;
                        }
                    } // servlets

                    synchronized (EjbJarMultiViewDataObject.this) {
                        newFileNames = null;
                    }

                }///end run

            }, 1500, Thread.MIN_PRIORITY);
        } else {
            elementTask.schedule(1500);
        }
    }

    public void fileRenamed(FileRenameEvent fileRenameEvent) {
        FileObject fo = fileRenameEvent.getFile();
        String resourceName = getPackageName(fo);
        if (resourceName != null) {
            int index = resourceName.lastIndexOf("."); //NOI18N
            String oldName = fileRenameEvent.getName();
            String oldResourceName = (index >= 0 ? resourceName.substring(0, index + 1) : "") + oldName;
            EjbJar ejbJar = getEjbJar();
            if (ejbJar.getStatus() == EjbJar.STATE_VALID) {
                fireEvent(oldResourceName, resourceName, DDChangeEvent.EJB_CHANGED);
            }
        }
    }

    public void fileFolderCreated(FileEvent fileEvent) {
    }

    public void fileDeleted(FileEvent fileEvent) {
        FileObject fo = fileEvent.getFile();
        String resourceName = getPackageName(fo);
        if (resourceName != null) {
            boolean foundElement = false;
            if (newFileNames == null) {
                foundElement = fireEvent(null, resourceName, DDChangeEvent.EJB_DELETED);
            } else {
                Ejb[] ejbs = getEjbJar().getEnterpriseBeans().getEjbs();
                for (int i = 0; i < ejbs.length; i++) {
                    if (resourceName.equals(ejbs[i].getEjbClass())) {
                        synchronized (this) {
                            if (deletedEjbNames == null) {
                                deletedEjbNames = new ArrayList();
                            }
                            deletedEjbNames.add(resourceName);
                        }
                        foundElement = true;
                        break;
                    }
                }
                if (foundElement) {
                    return;
                }
            }
        }
    }

    public void fileDataCreated(FileEvent fileEvent) {
        FileObject fo = fileEvent.getFile();
        String resourceName = getPackageName(fo);
        if (resourceName != null) {
            elementCreated(resourceName);
        }
    }

    public void fileChanged(FileEvent fileEvent) {
    }

    public void fileAttributeChanged(FileAttributeEvent fileAttributeEvent) {
    }

    public void stateChanged(javax.swing.event.ChangeEvent e) {
        refreshSourceFolders();
    }

    protected boolean parseDocument(boolean updateModel) throws IOException {
        if (ejbJar == null || ejbJar.getOriginal() == null) {
            try {
                setEjbJar((EjbJarProxy) DDProvider.getDefault().getDDRoot(getPrimaryFile()));
            } catch (IOException e) {
                if (ejbJar == null) {
                    setEjbJar(new EjbJarProxy(null, null));
                }
            }
        }
        parseable = false;
        InputSource is = createInputSource();
        if (is != null) { // merging model with the document
            try {
                EjbJarProxy newEjbJar = (EjbJarProxy) EjbJarDDUtils.createEjbJar(is);
                ejbJar.setStatus(newEjbJar.getStatus());
                SAXParseException error = newEjbJar.getError();
                ejbJar.setError(error);
                setSaxError(error);
                if (updateModel) {
                    if (ejbJar.getOriginal() != null) {
                        ejbJar.merge(newEjbJar, EjbJar.MERGE_UPDATE);
                    } else {
                        ejbJar.setOriginal(newEjbJar.getOriginal());
                    }
                }
                parseable = true;
            } catch (SAXException ex) {
                if (ejbJar.getOriginal() == null) {
                    ejbJar.setStatus(EjbJar.STATE_INVALID_UNPARSABLE);
                    if (ex instanceof SAXParseException) {
                        ejbJar.setError((SAXParseException) ex);
                    } else if (ex.getException() instanceof SAXParseException) {
                        ejbJar.setError((SAXParseException) ex.getException());
                    }
                }
                setSaxError(ex);
            }
        }
        return parseable;
    }

    private void setEjbJar(EjbJarProxy newEjbJar) {
        if(ejbJar != null) {
            ejbJar.removePropertyChangeListener(ejbJarChangeListener);
        }
        ejbJar = newEjbJar;
        if (ejbJarChangeListener == null) {
            ejbJarChangeListener = new EjbJarPropertyChangeListener();
        }
        ejbJar.addPropertyChangeListener(ejbJarChangeListener);
    }

    /**
     * Update text document from data model. Called when something is changed in visual editor.
     */
    protected String generateDocumentFromModel() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            if (ejbJar == null) {
                parseDocument(false);
            }
            ejbJar.write(out);
            out.close();
            return out.toString("UTF8"); //NOI18N
        } catch (IOException e) {
            ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, e);
        } catch (IllegalStateException e) {
            ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, e);
        }
        return out.toString();
    }

    /**
     * Icon Base for MultiView editor
     */
    protected DesignMultiViewDesc[] getMultiViewDesc() {
        return new DesignMultiViewDesc[]{
            new DDView(this, OVERVIEW),
            new DDView(this, CMP_RELATIONSHIPS),
        };
    }

    /** Used to detect if data model has already been created or not.
     * Method is called before switching to the design view from XML view when the document isn't parseable.
     */
    protected boolean isModelCreated() {
        return (ejbJar!=null && ejbJar.getOriginal()!=null);
    }

    public void showElement(final Object element) {
        if (element instanceof Relationships || element instanceof EjbRelation) {
            openView(1);
        } else {
            openView(0);
        }
        Utils.runInAwtDispatchThread(new Runnable() {
            public void run() {
                final SectionNodeView sectionView =
                        (SectionNodeView) EjbJarMultiViewDataObject.this .getActiveMVElement().getSectionView();
                final Node root = sectionView.getRoot();
                final SectionNode node = ((SectionNode) root.getChildren().getNodes()[0]).getNodeForElement(element);
                if (node != null) {
                    sectionView.openPanel(node);
                    ((SectionNodeInnerPanel) node.getSectionNodePanel().getInnerPanel()).focusData(element);
                }
            }
        });
    }

    private static class DDView extends DesignMultiViewDesc implements java.io.Serializable {

        static final long serialVersionUID = -8759598009819101630L;

        DDView(EjbJarMultiViewDataObject dataObject, String name) {
            super(dataObject, name);
        }

        public MultiViewElement createElement() {
            String name = getDisplayName();
            EjbJarMultiViewDataObject dataObject = (EjbJarMultiViewDataObject) getDataObject();
            if (name.equals(OVERVIEW)) {
                return new EjbMultiViewElement(dataObject);
            } else if (name.equals(CMP_RELATIONSHIPS)) {
                return new CmpRelationshipsMultiViewElement(dataObject);
            } else {
                // This case should not arise
                return null;
            }
        }

        public HelpCtx getHelpCtx() {
            final EjbJarMultiViewDataObject dataObject = ((EjbJarMultiViewDataObject)getDataObject());
            return new HelpCtx(dataObject.getActiveMVElement().getSectionView().getClass());
        }

        public Image getIcon() {
            return Utilities.loadImage(Utils.ICON_BASE_DD_VALID + ".gif"); //NOI18N
        }

        public String preferredID() {
            return "dd_multiview_" + getDisplayName(); //NOI18N
        }

    }
    
    /** Enable to access Active element 
     */
    public ToolBarMultiViewElement getActiveMVElement() {
        return (ToolBarMultiViewElement)super.getActiveMultiViewElement();
    }

    private Ejb getEjbFromEjbClass(String ejbClassName) {
        Ejb returnValue = null;
        Ejb[] ejbs = getEjbJar().getEnterpriseBeans().getEjbs();
        for (int i = 0; i < ejbs.length; i++) {
            if (ejbs[i].getEjbClass() != null &&
                    ejbs[i].getEjbClass().equals(ejbClassName)) {
                returnValue = ejbs[i];
                break;
            }
        }
        return returnValue;
    }

    private int getBeanInterfaceType(String interfaceName) {
        int interfaceType = -1;
        EntityAndSession[] beans = getEjbJar().getEnterpriseBeans().getSession();
        for (int i = 0; i < beans.length; i++) {
            if (beans[i].getHome() != null &&
                    beans[i].getHome().equals(interfaceName)) {
                interfaceType = HOME;
                break;
            }
            if (beans[i].getRemote() != null &&
                    beans[i].getRemote().equals(interfaceName)) {
                interfaceType = REMOTE;
                break;
            }
            if (beans[i].getLocalHome() != null &&
                    beans[i].getLocalHome().equals(interfaceName)) {
                interfaceType = LOCAL_HOME;
                break;
            }
            if (beans[i].getLocal() != null &&
                    beans[i].getLocal().equals(interfaceName)) {
                interfaceType = LOCAL;
                break;
            }
        }
        return interfaceType;
    }

    private int getSpecificEvent(int eventType, int interfaceType) {
        if (eventType == DDChangeEvent.EJB_CHANGED) {
            switch (interfaceType) {
                case HOME:
                    {
                        return DDChangeEvent.EJB_HOME_CHANGED;
                    }
                case REMOTE:
                    {
                        return DDChangeEvent.EJB_REMOTE_CHANGED;
                    }
                case LOCAL_HOME:
                    {
                        return DDChangeEvent.EJB_LOCAL_HOME_CHANGED;
                    }
                case LOCAL:
                    {
                        return DDChangeEvent.EJB_LOCAL_CHANGED;
                    }
            }
        }
        if (eventType == DDChangeEvent.EJB_DELETED) {
            switch (interfaceType) {
                case HOME:
                    {
                        return DDChangeEvent.EJB_HOME_DELETED;
                    }
                case REMOTE:
                    {
                        return DDChangeEvent.EJB_REMOTE_DELETED;
                    }
                case LOCAL_HOME:
                    {
                        return DDChangeEvent.EJB_LOCAL_HOME_DELETED;
                    }
                case LOCAL:
                    {
                        return DDChangeEvent.EJB_LOCAL_DELETED;
                    }
            }
        }
        return -1;
    }

    private boolean fireEvent(String oldResourceName, String resourceName, int eventType) {
        boolean elementFound = false;
        String resource = null;
        int specificEventType = -1;
        if (eventType == DDChangeEvent.EJB_CHANGED) {
            resource = oldResourceName;
        } else {
            resource = resourceName;
        }
        Ejb ejb = getEjbFromEjbClass(resource);

        if (ejb != null) {
            if (eventType == DDChangeEvent.EJB_CHANGED) {
                specificEventType = DDChangeEvent.EJB_CLASS_CHANGED;
            } else {
                specificEventType = DDChangeEvent.EJB_CLASS_DELETED;
            }
            elementFound = true;
        }

        if (!elementFound) {
            int interfaceType = getBeanInterfaceType(resource);

            if (interfaceType > 0) {
                specificEventType =
                        getSpecificEvent(eventType, interfaceType);
                elementFound = true;
            }
        }
        if (elementFound) {
            assert(specificEventType > 0);
            DDChangeEvent ddEvent =
                    new DDChangeEvent(this, this, oldResourceName,
                            resourceName, specificEventType);
            deploymentChange(ddEvent);
        }
        return elementFound;
    }

    public EntityHelper getEntityHelper(Entity entity) {
        EntityHelper entityHelper = (EntityHelper) entityHelperMap.get(entity);
        if (entityHelper == null) {
            entityHelper = new EntityHelper(this, entity);
            entityHelperMap.put(entity, entityHelper);
        }
        return entityHelper;
    }

    public SessionHelper getSessionHelper(Session session) {
        SessionHelper sessionHelper = (SessionHelper) entityHelperMap.get(session);
        if (sessionHelper == null) {
            sessionHelper = new SessionHelper(this, session);
            entityHelperMap.put(session, sessionHelper);
        }
        return sessionHelper;
    }

    private class EjbJarPropertyChangeListener implements PropertyChangeListener {

        public void propertyChange(PropertyChangeEvent evt) {
            if (EjbJar.PROPERTY_STATUS.equals(evt.getPropertyName())) {
                return;
            }
            //modelChanged();
            Object source = evt.getSource();
            if (source instanceof EnterpriseBeans) {
                Object oldValue = evt.getOldValue();
                Object newValue = evt.getNewValue();
                if ((oldValue instanceof Entity || newValue instanceof Entity)) {
                    entityHelperMap.keySet().retainAll(Arrays.asList(((EnterpriseBeans) source).getEntity()));
                } else if ((oldValue instanceof Session || newValue instanceof Session)) {
                    sessionHelperMap.keySet().retainAll(Arrays.asList(((EnterpriseBeans) source).getSession()));
                }
            }
        }
    }

}
