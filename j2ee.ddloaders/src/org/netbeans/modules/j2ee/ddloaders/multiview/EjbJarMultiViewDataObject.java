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
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.xml.cookies.ValidateXMLCookie;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.modules.j2ee.dd.api.ejb.DDProvider;
import org.netbeans.modules.j2ee.dd.api.ejb.Ejb;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans;
import org.netbeans.modules.j2ee.dd.api.ejb.EntityAndSession;
import org.netbeans.modules.j2ee.dd.api.ejb.Session;
import org.netbeans.modules.j2ee.dd.impl.ejb.EjbJarProxy;
import org.netbeans.modules.j2ee.ddloaders.ejb.DDChangeEvent;
import org.netbeans.modules.j2ee.ddloaders.ejb.DDChangeListener;
import org.netbeans.modules.j2ee.ddloaders.ejb.DDChangesPanel;
import org.netbeans.modules.j2ee.ddloaders.ejb.EjbJarDDUtils;
import org.netbeans.modules.j2ee.ddloaders.ejb.EjbJarDataLoader;
import org.netbeans.modules.j2ee.spi.ejbjar.EjbJarImplementation;
import org.netbeans.modules.xml.multiview.DesignMultiViewDesc;
import org.netbeans.modules.xml.multiview.XmlMultiViewDataObject;
import org.netbeans.spi.xml.cookies.DataObjectAdapters;
import org.netbeans.spi.xml.cookies.ValidateXMLSupport;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.filesystems.*;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataNode;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.xml.sax.InputSource;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

/**
 * Represents a DD object in the Repository.
 *
 * @author pfiala
 */
public class EjbJarMultiViewDataObject extends XmlMultiViewDataObject
        implements DDChangeListener, EjbJarProxy.OutputProvider, FileChangeListener, ChangeListener,
        PropertyChangeListener {
    private EjbJar ejbJar;
    private FileObject srcRoots[];
    protected final static RequestProcessor RP = new RequestProcessor("XML Parsing");   // NOI18N

    private static final long serialVersionUID = 8857563089355069362L;

    /**
     * Property name for documentDTD property
     */
    public static final String PROP_DOCUMENT_DTD = "documentDTD";   // NOI18N

    /**
     * Property name for property documentValid
     */
    public static final String PROP_DOC_VALID = "documentValid"; // NOI18N

    /**
     * List of updates to ejbs that should be processed
     */
    private Vector updates;

    private RequestProcessor.Task updateTask;

    private static final int HOME = 10;
    private static final int REMOTE = 20;
    private static final int LOCAL_HOME = 30;
    private static final int LOCAL = 40;
    private static final String OVERVIEW = "Overview";

    public EjbJarMultiViewDataObject(FileObject pf, EjbJarDataLoader loader) throws DataObjectExistsException {
        super(pf, loader);
        init(pf);
    }

    public boolean isRenameAllowed() {
        return false;
    }

    private void init(FileObject fo) {
        // added ValidateXMLCookie
        InputSource in = DataObjectAdapters.inputSource(this);
        ValidateXMLCookie validateCookie = new ValidateXMLSupport(in);
        getCookieSet().add(validateCookie);

        new FileObjectObserver(fo);

        Project project = getProject();
        if (project != null) {
            Sources sources = ProjectUtils.getSources(project);
            sources.addChangeListener(this);
        }
        refreshSourceFolders();
        addPropertyChangeListener(this);
    }

    private void refreshSourceFolders() {
        ArrayList srcRootList = new ArrayList();

        SourceGroup[] groups;
        Project project = getProject();
        SourceGroup[] groups1;
        if (project != null) {
            Sources sources = ProjectUtils.getSources(project);
            sources.addChangeListener(this);
            groups1 = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        } else {
            groups1 = null;
        }
        groups = groups1;
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
        return ejbJar;
    }

    private EjbJar createEjbJar() throws java.io.IOException {
        return DDProvider.getDefault().getDDRoot(getPrimaryFile());
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
     * Method from EjbJarProfy.OutputProvider
     */
    public void write(EjbJar ejbJarProxy) throws IOException {
        EjbJar app = getEjbJar();
        if (app != null) {
            app.merge(ejbJarProxy, EjbJar.MERGE_UNION);
        }
    }

    /**
     * Method from EjbJarProxy.OutputProvider
     */
    public FileObject getTarget() {
        return getPrimaryFile();
    }

    /**
     * Adds Ejb
     * <p/>
     * One ejb element element. The ejb-name is
     * set to Ejb_&lt clazz&gt by default.
     *
     * @param clazz      class name of ejb
     * @param urlPattern path to ejb class (pkg/foo/Bar)
     */
    public void createDefaultEJBConfiguration(String clazz, String urlPattern) {
        // PENDING: should be synchronized
        EnterpriseBeans a = getEjbJar().getEnterpriseBeans();
        try {
            Session newEjb = a.newSession();//Ludo todo add more ejb type cmp, mdb.
            newEjb.setEjbClass(clazz);
            String name = "Ludo was there Name123";//DDUtils.findFreeName (a.getServlet (), "EjbName" , "Ejb_"+clazz); // NOI18N
            newEjb.setEjbName(name);
            newEjb.setDescription(NbBundle.getMessage(EjbJarMultiViewDataObject.class, "TXT_newEjbElementDescription"));
            newEjb.setDisplayName("Session " + clazz); // NOI18N
            a.addSession(newEjb);
        } catch (Exception ex) {
        }
    }

    protected DataObject handleCopy(DataFolder f) throws IOException {
        DataObject dataObject = super.handleCopy(f);
        try {
            dataObject.setValid(false);
        } catch (PropertyVetoException e) {
        }
        return dataObject;
    }

    protected void dispose() {
        // no more changes in DD
        synchronized (this) {
            updates = null;
            if (updateTask != null) {
                updateTask.cancel();
            }
        }
        super.dispose();
    }
    
    /** Getter for property documentDTD.
     * @return Value of property documentDTD or <CODE>null</CODE> if documentDTD cannot be obtained.
     */
    /***  public String getDocumentDTD () {
     if (documentDTD == null) {
     EjbJar wa = getEjbJar ();
     }
     return documentDTD;
     }***/

    /**
     * This methods gets called when servlet is changed
     *
     * @param evt - object that describes the change.
     */
    public void deploymentChange(DDChangeEvent evt) {
        // fix of #28542, don't add ejb, if it's already defined in DD
        if (evt.getType() == DDChangeEvent.EJB_ADDED && EjbDefined(evt.getNewValue())) {
            return;
        }

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
                    List changes = null;
                    synchronized (EjbJarMultiViewDataObject.this) {
                        if (!EjbJarMultiViewDataObject.this.isValid()) {
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

    private boolean EjbDefined(String classname) {
        EjbJar ejbJar = getEjbJar();
        if (ejbJar == null) {
            return true;
        }
        Ejb[] ejbs = ejbJar.getEnterpriseBeans().getEjbs();
        for (int i = 0; i < ejbs.length; i++) {
            //Ludo TODO need to check the other class names: remote, home....depending on the ejb type
            if (ejbs[i].getEjbClass() != null && ejbs[i].getEjbClass().equals(classname)) {
                return true;
            }
        }
        return false;
    }

    private void showDDChangesDialog(List changes) {
        final JButton processButton;
        final JButton processAllButton;
        final JButton closeButton;
        final DDChangesPanel connectionPanel;
        final DialogDescriptor confirmChangesDescriptor;
        final Dialog confirmChangesDialog[] = {null};

        processButton = new JButton(NbBundle.getMessage(EjbJarMultiViewDataObject.class, "LAB_processButton"));
        processButton.setMnemonic(
                NbBundle.getMessage(EjbJarMultiViewDataObject.class, "LAB_processButton_Mnemonic").charAt(0));
        processButton.setToolTipText(NbBundle.getMessage(EjbJarMultiViewDataObject.class, "ACS_processButtonA11yDesc"));
        processAllButton = new JButton(NbBundle.getMessage(EjbJarMultiViewDataObject.class, "LAB_processAllButton"));
        processAllButton.setMnemonic(
                NbBundle.getMessage(EjbJarMultiViewDataObject.class, "LAB_processAllButton_Mnemonic").charAt(0));
        processAllButton.setToolTipText(
                NbBundle.getMessage(EjbJarMultiViewDataObject.class, "ACS_processAllButtonA11yDesc"));
        closeButton = new JButton(NbBundle.getMessage(EjbJarMultiViewDataObject.class, "LAB_closeButton"));
        closeButton.setMnemonic(
                NbBundle.getMessage(EjbJarMultiViewDataObject.class, "LAB_closeButton_Mnemonic").charAt(0));
        closeButton.setToolTipText(NbBundle.getMessage(EjbJarMultiViewDataObject.class, "ACS_closeButtonA11yDesc"));
        final Object[] options = new Object[]{
            processButton,
            processAllButton
        };
        final Object[] additionalOptions = new Object[]{
            closeButton
        };

        String fsname = "";                                             //NOI18N
        Project project = getProject();
        if (project != null) {
            ProjectInformation projectInfo = ProjectUtils.getInformation(project);
            if (projectInfo != null) {
                fsname = projectInfo.getName();
            }
        }

        ///LUDO    WebModule wm = WebModule.getWebModule(getPrimaryFile ());
        ///    if (wm!=null) {
        ///        fsname=wm.getContextPath();
        ///    }
        String caption = NbBundle.getMessage(EjbJarMultiViewDataObject.class, "MSG_SynchronizeCaption", fsname);
        connectionPanel = new DDChangesPanel(caption, processButton);
        confirmChangesDescriptor = new DialogDescriptor(connectionPanel,
                NbBundle.getMessage(EjbJarMultiViewDataObject.class, "LAB_ConfirmDialog"),
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
                            root = javax.swing.SwingUtilities.getRoot((Component) e.getSource());
                            if (!root.isDisplayable()) {
                                return;
                            }
                        }
                        if (options[0].equals(e.getSource())) {
                            int min = connectionPanel.getChangesList().getMinSelectionIndex();
                            int max = connectionPanel.getChangesList().getMaxSelectionIndex();
                            for (int i = max; i >= min; i--) {
                                if (connectionPanel.getChangesList().isSelectedIndex(i)) {
                                    final DDChangeEvent ev = (DDChangeEvent) connectionPanel.getListModel()
                                            .getElementAt(i);
                                    processDDChangeEvent(ev);
                                    connectionPanel.getListModel().removeElementAt(i);
                                }
                            }
                            if (connectionPanel.getListModel().isEmpty()) {
                                confirmChangesDialog[0].setVisible(false);
                            } else {
                                processButton.setEnabled(false);
                            }
                        } else if (options[1].equals(e.getSource())) {
                            Enumeration en = connectionPanel.getListModel().elements();
                            while (en.hasMoreElements()) {
                                processDDChangeEvent((DDChangeEvent) en.nextElement());
                            }
                            confirmChangesDialog[0].setVisible(false);
                            connectionPanel.setChanges(null);
                        } else if (additionalOptions[0].equals(e.getSource())) {
                            confirmChangesDialog[0].setVisible(false);
                            connectionPanel.setChanges(null);
                        }
                    }
                });
        confirmChangesDescriptor.setAdditionalOptions(additionalOptions);

        processButton.setEnabled(false);
        processAllButton.requestFocus();
        connectionPanel.setChanges(changes);

        try {
            confirmChangesDialog[0] = DialogDisplayer.getDefault().createDialog(confirmChangesDescriptor);
            confirmChangesDialog[0].show();
        } finally {
            confirmChangesDialog[0].dispose();
        }
    }

    private void processDDChangeEvent(DDChangeEvent evt) {
        if (!isValid()) {
            return;
        }

        if (evt.getType() == DDChangeEvent.EJB_ADDED) {
            String clz = evt.getNewValue();

            // new from template or copy of another servlet
            String urimapping = "/servlet/" + clz;    // NOI18N
            createDefaultEJBConfiguration(clz, urimapping);
        } else {
            updateDD(evt.getOldValue(), (String) evt.getNewValue(), evt.getType());
        }
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
        System.out.println("fileRenamed");                              //NOI18N
        System.out.println("fileRenameEvent : " + fileRenameEvent);     //NOI18N

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
        System.out.println("fileDeleted");                              //NOI18N
        System.out.println("fileEvent : " + fileEvent);                 //NOI18N

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

    /**
     * Create the data model from file object. Called from constructor.
     *
     * @return true if model was succesfully created, false otherwise
     */
    protected boolean createModelFromFileObject(FileObject fo) throws IOException {
        ejbJar = createEjbJar();
        if (ejbJar != null) {
            org.xml.sax.SAXParseException error = ejbJar.getError();
            setSaxError(error);
            int status = ejbJar.getStatus();
            if (status == EjbJar.STATE_INVALID_UNPARSABLE) {
                return false;
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

    /**
     * Update data model from document text . Called when something is changed in xml editor.
     *
     * @return true if model was succesfully created, false otherwise
     */
    protected boolean updateModelFromDocument() throws IOException {
        InputSource is = createInputSource();
        if (is != null) { // merging model with the document
            org.xml.sax.SAXParseException error = null;
            org.xml.sax.SAXException oldError = getSaxError();
            String version = null;
            final EjbJarProxy oldEjbJar = (EjbJarProxy) ejbJar;
            try {
                // preparsing
                // TODO: error = ... parse(is) ???
                EjbJarProxy newEjbJar = (EjbJarProxy) EjbJarDDUtils.createEjbJar(is);
                if (ejbJar != null && oldEjbJar.getOriginal() != null) {
                    ejbJar.merge(newEjbJar, EjbJar.MERGE_UPDATE);
                } else {
                    ejbJar = newEjbJar;
                }
                if (error != null) {
                    oldEjbJar.setStatus(EjbJar.STATE_INVALID_PARSABLE);
                    oldEjbJar.setError(error);
                    ((EjbJarMultiViewDataNode) getNodeDelegate()).descriptionChanged(
                            oldError == null ? null : oldError.getMessage(), error.getMessage());
                } else {
                    oldEjbJar.setStatus(EjbJar.STATE_VALID);
                    oldEjbJar.setError(null);
                    ((EjbJarMultiViewDataNode) getNodeDelegate()).descriptionChanged(
                            oldError == null ? null : oldError.getMessage(), null);
                }
                System.out.println("version:" + ejbJar.getVersion() + " Status:" + ejbJar.getStatus() + " Error:" +
                        ejbJar.getError());
                setSaxError(error);
                return true;
            } catch (org.xml.sax.SAXException ex) {
                if (ejbJar == null || oldEjbJar.getOriginal() == null) {
                    ejbJar = new EjbJarProxy(null, version);
                    oldEjbJar.setStatus(EjbJar.STATE_INVALID_UNPARSABLE);
                    if (ex instanceof org.xml.sax.SAXParseException) {
                        oldEjbJar.setError((org.xml.sax.SAXParseException) ex);
                    } else if (ex.getException() instanceof org.xml.sax.SAXParseException) {
                        oldEjbJar.setError((org.xml.sax.SAXParseException) ex.getException());
                    }
                }
                ((EjbJarMultiViewDataNode) getNodeDelegate()).descriptionChanged(
                        oldError == null ? null : oldError.getMessage(), ex.getMessage());
                setSaxError(ex);
                return false;
            }
        }
        return false;
    }

    /**
     * Called on close-discard option.
     * The data model is updated from corresponding file object(s).
     */
    protected void reloadModelFromFileObject() throws IOException {
        InputSource is = new InputSource(getPrimaryFile().getInputStream());
        if (is != null) { // merging model with the document
            org.xml.sax.SAXParseException error = null;
            org.xml.sax.SAXException oldError = getSaxError();
            String version = null;
            final EjbJarProxy oldEjbJar = (EjbJarProxy) ejbJar;
            try {
                // preparsing
                // TODO: error = ... parse(is) ???
                EjbJarProxy newEjbJar = (EjbJarProxy) EjbJarDDUtils.createEjbJar(is);
                if (ejbJar != null && oldEjbJar.getOriginal() != null) {
                    ejbJar.merge(newEjbJar, EjbJar.MERGE_UPDATE);
                } else {
                    ejbJar = newEjbJar;
                }
                if (error != null) {
                    oldEjbJar.setStatus(EjbJar.STATE_INVALID_PARSABLE);
                    oldEjbJar.setError(error);
                    ((EjbJarMultiViewDataNode) getNodeDelegate()).descriptionChanged(
                            oldError == null ? null : oldError.getMessage(), error.getMessage());
                } else {
                    oldEjbJar.setStatus(EjbJar.STATE_VALID);
                    oldEjbJar.setError(null);
                    ((EjbJarMultiViewDataNode) getNodeDelegate()).descriptionChanged(
                            oldError == null ? null : oldError.getMessage(), null);
                }
                System.out.println("version:" + ejbJar.getVersion() + " Status:" + ejbJar.getStatus() + " Error:" +
                        ejbJar.getError());
                setSaxError(error);
            } catch (org.xml.sax.SAXException ex) {
                if (ejbJar == null || oldEjbJar.getOriginal() == null) {
                    ejbJar = new EjbJarProxy(null, version);
                    oldEjbJar.setStatus(EjbJar.STATE_INVALID_UNPARSABLE);
                    if (ex instanceof org.xml.sax.SAXParseException) {
                        oldEjbJar.setError((org.xml.sax.SAXParseException) ex);
                    } else if (ex.getException() instanceof org.xml.sax.SAXParseException) {
                        oldEjbJar.setError((org.xml.sax.SAXParseException) ex.getException());
                    }
                }
                ((EjbJarMultiViewDataNode) getNodeDelegate()).descriptionChanged(
                        oldError == null ? null : oldError.getMessage(), ex.getMessage());
                setSaxError(ex);
            }
        }
    }

    /**
     * Update text document from data model. Called when something is changed in visual editor.
     */
    protected String generateDocumentFromModel() {
        //System.out.println("Generating document - generate....");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
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
     * Display Name for MultiView editor
     */
    protected String getDisplayName() {
        return "ejb-jar.xml";
    }

    /**
     * Icon Base for MultiView editor
     */
    protected String getIconBase() {
        return Utils.ICON_BASE_EJB_MODULE_NODE;
    }

    /**
     * Icon Base for MultiView editor
     */
    protected DesignMultiViewDesc[] getMultiViewDesc() {
        return new DesignMultiViewDesc[]{
            new DDView(this, OVERVIEW),
        };
    }

    /**
     * This method gets called when a bound property is changed.
     *
     * @param evt A PropertyChangeEvent object describing the event source
     *            and the property that has changed.
     */

    public void propertyChange(PropertyChangeEvent evt) {
        if (EjbJarMultiViewDataObject.PROP_DOCUMENT_VALID.equals(evt.getPropertyName())) {
            if (Boolean.TRUE.equals(evt.getNewValue())) {
                ((DataNode) getNodeDelegate()).setIconBase(Utils.ICON_BASE_DD_VALID);
            } else {
                ((DataNode) getNodeDelegate()).setIconBase(Utils.ICON_BASE_DD_VALID);
            }
        }

    }

    private static class DDView extends DesignMultiViewDesc {

        DDView(EjbJarMultiViewDataObject dataObject, String name) {
            super(dataObject, name);
        }

        public MultiViewElement createElement() {
            String name = getDisplayName();
            EjbJarMultiViewDataObject dataObject = (EjbJarMultiViewDataObject) getDataObject();
            if (name.equals(OVERVIEW)) {
                return new EjbMultiViewElement(dataObject);
            } else {
                // This case should not arise
                return null;
            }
        }

        public Image getIcon() {
            return Utilities.loadImage(Utils.ICON_BASE_DD_VALID + ".gif"); //NOI18N
        }

        public String preferredID() {
            return "dd_multiview_" + getDisplayName(); //NOI18N
        }

    }

    /**
     * WeakListener for accepting external changes to web.xml
     */
    private class FileObjectObserver implements FileChangeListener {
        FileObjectObserver(FileObject fo) {
            fo.addFileChangeListener((FileChangeListener) org.openide.util.WeakListeners.create(
                    FileChangeListener.class, this, fo));
        }

        public void fileAttributeChanged(FileAttributeEvent fileAttributeEvent) {
        }

        public void fileChanged(FileEvent fileEvent) {
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
        EntityAndSession[] beans = ejbJar.getEnterpriseBeans().getSession();
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

    private EntityAndSession getBeanFromInterface(String interfaceName) {
        EntityAndSession returnValue = null;
        EntityAndSession[] beans = ejbJar.getEnterpriseBeans().getSession();
        for (int i = 0; i < beans.length; i++) {
            if (beans[i].getHome() != null &&
                    beans[i].getHome().equals(interfaceName)) {
                returnValue = beans[i];
                break;
            }
            if (beans[i].getRemote() != null &&
                    beans[i].getRemote().equals(interfaceName)) {
                returnValue = beans[i];
                break;
            }
            if (beans[i].getLocalHome() != null &&
                    beans[i].getLocalHome().equals(interfaceName)) {
                returnValue = beans[i];
                break;
            }
            if (beans[i].getLocal() != null &&
                    beans[i].getLocal().equals(interfaceName)) {
                returnValue = beans[i];
                break;
            }
        }
        return returnValue;
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
        ///assert(false : "control should never reach here -unsupported event type detected"); //NOI18N
        return -1;
    }

    private boolean fireEvent(String oldResourceName, String resourceName,
            int eventType) {
        System.out.println("fireEvent");                            //NOI18N
        System.out.println("oldResourceName : " + oldResourceName); //NOI18N
        System.out.println("resourceName : " + resourceName);       //NOI18N
        System.out.println("eventType : " + eventType);             //NOI18N

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

    private void updateDD(String oldResourceName, String resourceName,
            int eventType) {
        System.out.println("updateDD");                                 //NOI18N
        System.out.println("oldResourceName : " + oldResourceName);     //NOI18N
        System.out.println("resourceName : " + resourceName);           //NOI18N
        System.out.println("eventType : " + eventType);                 //NOI18N

        switch (eventType) {
            case DDChangeEvent.EJB_CLASS_CHANGED:
                {
                    // update ejb-class
                    if (oldResourceName == null) {
                        return;
                    }

                    Ejb ejb = getEjbFromEjbClass(oldResourceName);
                    if (ejb != null) {
                        ejb.setEjbClass(resourceName);
                    }
                    break;
                }
            case DDChangeEvent.EJB_CLASS_DELETED:
                {
                    // delete the whole ejb(impl file deletion)
                    if (resourceName == null) {
                        return;
                    }

                    Ejb ejb = getEjbFromEjbClass(resourceName);
                    if (ejb != null) {
                        EjbJar ejbJar = getEjbJar();
                        EnterpriseBeans enterpriseBeans = ejbJar.getEnterpriseBeans();
                        if (enterpriseBeans != null) {
                            enterpriseBeans.removeEjb(ejb);
                        }
                    }
                    break;
                }

            case DDChangeEvent.EJB_HOME_CHANGED:
                {
                    if (oldResourceName == null) {
                        return;
                    }

                    EntityAndSession bean = getBeanFromInterface(oldResourceName);
                    if (bean != null) {
                        assert(oldResourceName.equals(bean.getHome()));
                        bean.setHome(resourceName);
                    }
                    break;
                }

            case DDChangeEvent.EJB_REMOTE_CHANGED:
                {
                    if (oldResourceName == null) {
                        return;
                    }

                    EntityAndSession bean = getBeanFromInterface(oldResourceName);
                    if (bean != null) {
                        assert(oldResourceName.equals(bean.getRemote()));
                        bean.setRemote(resourceName);
                    }
                    break;
                }

            case DDChangeEvent.EJB_LOCAL_HOME_CHANGED:
                {
                    if (oldResourceName == null) {
                        return;
                    }

                    EntityAndSession bean = getBeanFromInterface(oldResourceName);
                    if (bean != null) {
                        assert(oldResourceName.equals(bean.getLocalHome()));
                        bean.setLocalHome(resourceName);
                    }
                    break;
                }

            case DDChangeEvent.EJB_LOCAL_CHANGED:
                {
                    if (oldResourceName == null) {
                        return;
                    }

                    EntityAndSession bean = getBeanFromInterface(oldResourceName);
                    if (bean != null) {
                        assert(oldResourceName.equals(bean.getLocal()));
                        bean.setLocal(resourceName);
                    }
                    break;
                }

            case DDChangeEvent.EJB_HOME_DELETED:
                {
                    if (resourceName == null) {
                        return;
                    }

                    EntityAndSession bean = getBeanFromInterface(resourceName);
                    if (bean != null) {
                        assert(resourceName.equals(bean.getHome()));
                        bean.setHome(null);
                    }
                    break;
                }

            case DDChangeEvent.EJB_REMOTE_DELETED:
                {
                    if (resourceName == null) {
                        return;
                    }

                    EntityAndSession bean = getBeanFromInterface(resourceName);
                    if (bean != null) {
                        assert(resourceName.equals(bean.getRemote()));
                        bean.setRemote(null);
                    }
                    break;
                }

            case DDChangeEvent.EJB_LOCAL_HOME_DELETED:
                {
                    if (resourceName == null) {
                        return;
                    }

                    EntityAndSession bean = getBeanFromInterface(resourceName);
                    if (bean != null) {
                        assert(resourceName.equals(bean.getLocalHome()));
                        bean.setLocalHome(null);
                    }
                    break;
                }

            case DDChangeEvent.EJB_LOCAL_DELETED:
                {
                    if (resourceName == null) {
                        return;
                    }

                    EntityAndSession bean = getBeanFromInterface(resourceName);
                    if (bean != null) {
                        assert(resourceName.equals(bean.getLocal()));
                        bean.setLocal(null);
                    }
                    break;
                }
        }
    }
}
