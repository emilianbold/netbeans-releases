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

package org.netbeans.modules.j2ee.ddloaders.app;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.JButton;
import javax.swing.event.ChangeListener;

import org.openide.DialogDescriptor;
import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.nodes.*;
import org.openide.util.HelpCtx;
import org.openide.util.RequestProcessor;

import org.xml.sax.*;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;

import org.netbeans.modules.j2ee.ddloaders.common.xmlutils.SAXParseError;
import org.netbeans.modules.j2ee.dd.api.application.*;
import org.netbeans.api.xml.cookies.ValidateXMLCookie;
import org.netbeans.spi.xml.cookies.*;
import org.openide.DialogDisplayer;
import org.netbeans.modules.j2ee.dd.impl.application.ApplicationProxy;

//////////import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ProjectInformation;

/////////////ludo  import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.j2ee.ddloaders.common.DD2beansDataObject;


/** Represents a DD object in the Repository.
 *
 * @author  mkuchtiak, Ludovic Champenois
 */
public class EarDataObject extends DD2beansDataObject 
    implements DDChangeListener, ApplicationProxy.OutputProvider, FileChangeListener, ChangeListener {
    private Application ejbJar;
    private FileObject srcRoots[];
    protected final static RequestProcessor RP = new RequestProcessor("XML Parsing");   // NOI18N

    private static final long serialVersionUID = 8857563089355069362L;
    
    /** Property name for documentDTD property */
    public static final String PROP_DOCUMENT_DTD = "documentDTD";   // NOI18N


    private boolean unparsable=true;
    
    /** List of updates to ejbs that should be processed */
    private Vector updates;
    
    private RequestProcessor.Task updateTask;
    private FileObjectObserver fileListener;

    private static final int HOME = 10;
    private static final int REMOTE = 20;
    private static final int LOCAL_HOME = 30;
    private static final int LOCAL = 40;

    public EarDataObject (FileObject pf, EarDataLoader loader) throws DataObjectExistsException {
        super (pf, loader);
        init (pf,loader);
    }
    public boolean isRenameAllowed(){
        return false;
    }
    public boolean isUnparsable(){
        return unparsable;
    }
    
    private void init (FileObject fo,EarDataLoader loader) {
        // added ValidateXMLCookie        
        InputSource in = DataObjectAdapters.inputSource(this);
        ValidateXMLCookie validateCookie = new ValidateXMLSupport(in);
        getCookieSet().add(validateCookie);
        


        fileListener = new FileObjectObserver(fo);
        
        Project project = FileOwnerQuery.getOwner (getPrimaryFile ());
        if (project != null) {
            Sources sources = ProjectUtils.getSources(project);
            sources.addChangeListener (this);
        }
        refreshSourceFolders ();
    }

    private void refreshSourceFolders () {
        ArrayList srcRootList = new ArrayList ();
        
        Project project = FileOwnerQuery.getOwner (getPrimaryFile ());
        if (project != null) {
            Sources sources = ProjectUtils.getSources(project);
            sources.addChangeListener (this);
            SourceGroup[] groups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
            for (int i = 0; i < groups.length; i++) {
                org.netbeans.modules.j2ee.api.ejbjar.EjbJar ejbModule = org.netbeans.modules.j2ee.api.ejbjar.EjbJar.getEjbJar(groups[i].getRootFolder());
                if ((ejbModule != null) && (ejbModule.getDeploymentDescriptor() != null)) {
                    try {
                        FileObject fo = groups [i].getRootFolder ();
                        srcRootList.add (groups [i].getRootFolder ());
                        FileSystem fs = fo.getFileSystem ();
                        fs.removeFileChangeListener(this); //avoid being added multiple times
                        fs.addFileChangeListener (this);
                    } catch (FileStateInvalidException ex) {
                        ErrorManager.getDefault ().notify (ex);
                    }
                }
            }
        }
        srcRoots = (FileObject []) srcRootList.toArray (new FileObject [srcRootList.size ()]);
    }
    
    private String getPackageName (FileObject clazz) {
        for (int i = 0; i < srcRoots.length; i++) {
            String rp = FileUtil.getRelativePath (srcRoots [i], clazz);
            if (rp != null) {
                if (clazz.getExt ().length () > 0) {
                    rp = rp.substring (0, rp.length () - clazz.getExt ().length () - 1);
                }
                return rp.replace ('/', '.');
            }
        }
        return null;
    }
    
    public Application getApplication(){
        if (ejbJar==null){
            parsingDocument();
        }
        return ejbJar;
    }
    
    public Application getOriginalApplication() throws IOException {
        return DDProvider.getDefault().getDDRoot(getPrimaryFile());
    }
    
    protected org.openide.nodes.Node createNodeDelegate () {
        return new EarDataNode(this);
    }


    /** gets the Icon Base for node delegate when parser accepts the xml document as valid
     *
     * PENDING: move into node
     * @return Icon Base for node delegate
     */
    protected String getIconBaseForValidDocument() {
        return "org/netbeans/modules/j2ee/ddloaders/ejb/DDDataIcon"; // NOI18N
    }
    
    /** gets the Icon Base for node delegate when parser finds error(s) in xml document
     * @return Icon Base for node delegate
     *
     * PENDING: move into node
     */
    protected String getIconBaseForInvalidDocument() {
        return "org/netbeans/modules/j2ee/ddloaders/ejb/DDDataIcon1"; // NOI18N
    }    
    
    /** gets the String for node delegate when parser accepts the xml document as valid
     * @return String for valid xml document
    */
    public String getStringForValidDocument() {
        return NbBundle.getMessage (EarDataObject.class, "LAB_deploymentDescriptor");          
    }
    
    /** gets the String for node delegate when parser finds error(s) in xml document
     * @param error Error description
     * @return String for node delegate
    */
    public String getStringForInvalidDocument(SAXParseError error) {
        return NbBundle.getMessage (EarDataObject.class, "TXT_errorOnLine", new Integer(error.getErrorLine()));
    }
                    
    /** Create document from the Node. This method is called after Node (Node properties)is changed.
     * The document is generated from data modul (isDocumentGenerable=true) 
    */
    protected String generateDocument() {
        //System.out.println("Generating document - generate....");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            ejbJar.write(out);
            out.close();
            return out.toString("UTF8"); //NOI18N
        }
        catch (IOException e) {
            ErrorManager.getDefault ().notify(org.openide.ErrorManager.INFORMATIONAL, e);
        }
        catch (IllegalStateException e){
            ErrorManager.getDefault ().notify(org.openide.ErrorManager.INFORMATIONAL, e);
	}
	return out.toString ();
    }
        
    /** Update document in text editor. This method is called after Node (Node properties)is changed.
     * The document is updated programaticaly - not from data modul (isDocumentGenerable=false)
     * @param doc document which should be updated
    */
    protected String updateDocument(javax.swing.text.Document doc) {
        return null;
    }
    /** Method from EjbJarProfy.OutputProvider
    */
    public void write(Application ejbJarProxy) throws IOException {
        Application app = getApplication();
        if (app!=null) {
            app.merge(ejbJarProxy,Application.MERGE_UNION);
        }
        setNodeDirty(true);
    }
    /** Method from EjbJarProfy.OutputProvider
    */    
    public FileObject  getTarget() {
        return getPrimaryFile();
    }
    
    protected SAXParseError updateNode(InputSource is) throws IOException {
        //System.out.println("updateNode() ");
        if (ejbJar==null) {
            try {
                ejbJar = DDProvider.getDefault().getDDRoot(getPrimaryFile());
                firePropertyChange(Node.PROP_PROPERTY_SETS,null,null);
                if (unparsable) {
                    unparsable=false;
                }
                return null;
            } catch (RuntimeException e) {
                if (!unparsable) {
                    unparsable=true;
                }
                return new SAXParseError(new SAXParseException(e.getMessage(),new org.xml.sax.helpers.LocatorImpl()));
            }
        }
        Application app=null;
        try {
            app = EarDDUtils.createApplication(is);
            if (!ejbJar.getVersion().equals(app.getVersion())) {
                ((ApplicationProxy)ejbJar).setOriginal(app);
            }
            unparsable=false;
            if (app.getError()!= null) {
                // semantic error we can badge or tooltip here
                System.out.println(app.getError().getMessage());
            }
        } catch (SAXParseException e) {
            if (!unparsable) {
                unparsable=true;
            }
            return new SAXParseError(e);                
        } catch (SAXException e) {
            if (!unparsable) {
                unparsable=true;
            }
            throw new IOException();
        }
        if (app!=null){
            // set new graph or merge it with old one
            ejbJar.merge(app,org.netbeans.modules.schema2beans.BaseBean.MERGE_UPDATE);

        }
        return null;
    }
    
    /**
     * Adds Ejb
     *
     * One ejb element element. The ejb-name is
     * set to Ejb_&lt clazz&gt by default.
     *
     * @param clazz class name of ejb
     * @param pathName path to ejb class (pkg/foo/Bar)
     */
//   public void createDefaultEJBConfiguration (String clazz, String urlPattern) {
//        // PENDING: should be synchronized
//        EnterpriseBeans a = getEjbJar ().getEnterpriseBeans();
//        try {
//            Session newEjb = a.newSession();//Ludo todo add more ejb type cmp, mdb.
//            newEjb.setEjbClass (clazz);
//            String name = "Ludo was there Name123";//DDUtils.findFreeName (a.getServlet (), "EjbName" , "Ejb_"+clazz); // NOI18N
//            newEjb.setEjbName (name);
//            newEjb.setDescription (NbBundle.getMessage (EarDataObject.class, "TXT_newEjbElementDescription"));
//            newEjb.setDisplayName ("Session "+clazz); // NOI18N
//            a.addSession (newEjb);
//
//            setNodeDirty (true);
//        } catch (Exception ex) {}
//    }
    
    protected DataObject handleCopy(DataFolder f) throws IOException {
        DataObject dObj = super.handleCopy(f);
        try { dObj.setValid(false); }catch(java.beans.PropertyVetoException e){}
        return dObj;
    }

    protected void dispose () {
        // no more changes in DD
        synchronized (this) {
            updates = null;
            if (updateTask != null) {
                updateTask.cancel();
            }
        }
        super.dispose ();
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

    /** This methods gets called when servlet is changed
     * @param evt - object that describes the change.
     */
    public void deploymentChange (DDChangeEvent evt) {
        // fix of #28542, don't add ejb, if it's already defined in DD
//        if ( evt.getType() == DDChangeEvent.EJB_ADDED && EjbDefined( evt.getNewValue() ) )
//            return;
        
        synchronized (this) {
            if (updates == null) {
                updates = new Vector ();
            }
            updates.addElement (evt);
        }
        
        // schedule processDDChangeEvent
        if (updateTask == null) {
            updateTask = RequestProcessor.getDefault().post (new Runnable () {
                public void run () {
                    java.util.List changes = null;
                    synchronized (EarDataObject.this) {
                        if (!EarDataObject.this.isValid())
                            return;
                        if (updates != null) {
                            changes = updates;
                            updates = null;
                        }
                    }
                    if (changes != null)
                        showDDChangesDialog (changes);
                }
            }, 2000, Thread.MIN_PRIORITY);
        }
        else {
            updateTask.schedule (2000);
        }
    }
    
//    private boolean EjbDefined(String classname) {
//        EjbJar ejbJar = getEjbJar();
//        if (ejbJar==null) return true;
//        Ejb[] ejbs = ejbJar.getEnterpriseBeans(). getEjbs();
//        for ( int i = 0; i < ejbs.length; i++ ) {
//            //Ludo TODO need to check the other class names: remote, home....depending on the ejb type
//            if ( ejbs[i].getEjbClass() != null && ejbs[i].getEjbClass().equals( classname ) )
//                return true;
//        }
//        return false;
//    }
    
    private void showDDChangesDialog (List changes) {
        final JButton processButton;
        final JButton processAllButton;
        final JButton closeButton;
        final DDChangesPanel connectionPanel;
        final DialogDescriptor confirmChangesDescriptor;
        final Dialog confirmChangesDialog[] = { null };
        
        processButton = new JButton (NbBundle.getMessage (EarDataObject.class, "LAB_processButton"));
        processButton.setMnemonic (NbBundle.getMessage (EarDataObject.class, "LAB_processButton_Mnemonic").charAt (0));
        processButton.setToolTipText (NbBundle.getMessage (EarDataObject.class, "ACS_processButtonA11yDesc"));
        processAllButton = new JButton (NbBundle.getMessage (EarDataObject.class, "LAB_processAllButton"));
        processAllButton.setMnemonic (NbBundle.getMessage (EarDataObject.class, "LAB_processAllButton_Mnemonic").charAt (0));
        processAllButton.setToolTipText (NbBundle.getMessage (EarDataObject.class, "ACS_processAllButtonA11yDesc"));
        closeButton = new JButton (NbBundle.getMessage (EarDataObject.class, "LAB_closeButton"));
        closeButton.setMnemonic (NbBundle.getMessage (EarDataObject.class, "LAB_closeButton_Mnemonic").charAt (0));
        closeButton.setToolTipText (NbBundle.getMessage (EarDataObject.class, "ACS_closeButtonA11yDesc"));
        final Object [] options = new Object [] {
            processButton,
            processAllButton
        };
        final Object [] additionalOptions = new Object [] {
            closeButton
        };
    
        String fsname = "";                                             //NOI18N
        Project project = FileOwnerQuery.getOwner (getPrimaryFile ());
        if (project != null) {
            String projectName = null;
            ProjectInformation projectInfo = ProjectUtils.getInformation(project);
            if(projectInfo != null){
                fsname = projectInfo.getName();
            }
        }

    ///LUDO    WebModule wm = WebModule.getWebModule(getPrimaryFile ());
    ///    if (wm!=null) {
    ///        fsname=wm.getContextPath();
    ///    }
        String caption = NbBundle.getMessage (EarDataObject.class, "MSG_SynchronizeCaption", fsname);
        connectionPanel = new DDChangesPanel (caption, processButton);
        confirmChangesDescriptor = new DialogDescriptor (
            connectionPanel,
            NbBundle.getMessage (EarDataObject.class, "LAB_ConfirmDialog"),
            true,
            options,
            processButton,
            DialogDescriptor.RIGHT_ALIGN,
            HelpCtx.DEFAULT_HELP,
            new ActionListener () {
                public void actionPerformed (ActionEvent e) {
                    if (e.getSource () instanceof Component) {
                        Component root;

                        // hack to avoid multiple calls for disposed dialogs:
                        root = javax.swing.SwingUtilities.getRoot ((Component)e.getSource ());
                        if (!root.isDisplayable ()) {
                            return;
                        }
                    }
                    if (options[0].equals (e.getSource ())) {
                        int min = connectionPanel.changesList.getMinSelectionIndex ();
                        int max = connectionPanel.changesList.getMaxSelectionIndex ();
                        for (int i = max; i >= min; i--) {
                            if (connectionPanel.changesList.isSelectedIndex (i)) {
                                final DDChangeEvent ev = (DDChangeEvent)connectionPanel.listModel.getElementAt (i);
                                processDDChangeEvent (ev);
                                connectionPanel.listModel.removeElementAt (i);
                            }
                        }
                        if (connectionPanel.listModel.isEmpty ()) {
                            confirmChangesDialog[0].setVisible (false);
                        }
                        else {
                            processButton.setEnabled (false);
                        }
                    }
                    else if (options[1].equals (e.getSource ())) {
                        Enumeration en = connectionPanel.listModel.elements ();
                        while (en.hasMoreElements ()) {
                            processDDChangeEvent ((DDChangeEvent)en.nextElement ());
                        }
                        confirmChangesDialog[0].setVisible (false);
                        connectionPanel.setChanges (null);
                    }
                    else if (additionalOptions[0].equals (e.getSource ())) {
                        confirmChangesDialog[0].setVisible (false);
                        connectionPanel.setChanges (null);
                    }
                }
            }
        );
        confirmChangesDescriptor.setAdditionalOptions (additionalOptions);
        
        processButton.setEnabled (false);
        processAllButton.requestFocus ();
        connectionPanel.setChanges (changes);
        
        try {
            confirmChangesDialog[0] = DialogDisplayer.getDefault ().createDialog (confirmChangesDescriptor);
            confirmChangesDialog[0].show ();
        } finally {
            confirmChangesDialog[0].dispose ();
        }
    }
    
    private void processDDChangeEvent (DDChangeEvent evt) {
        if (!isValid())
            return;
       
        if (evt.getType () == DDChangeEvent.EJB_ADDED) {
            String clz = evt.getNewValue ();

            // new from template or copy of another servlet
            String urimapping = "/servlet/"+clz;    // NOI18N
            //createDefaultEJBConfiguration (clz, urimapping);
        } else {
            updateDD(evt.getOldValue(), (String)evt.getNewValue (), evt.getType());
        }
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(EarDataObject.class);
    }

    private RequestProcessor.Task elementTask;
    private List deletedEjbNames;
    private List newFileNames;
    
    private void elementCreated(final String elementName) {
        synchronized (this) {
            if (newFileNames==null) {
                newFileNames=new ArrayList();
            }
            newFileNames.add(elementName);
        }
        
        if (elementTask == null) {
            elementTask = RequestProcessor.getDefault().post (new Runnable () {
                public void run () {
                    if (deletedEjbNames!=null) {
                        for (int i=0;i<deletedEjbNames.size();i++) {
                            String deletedServletName = (String)deletedEjbNames.get(i);
                            String deletedName=deletedServletName;
                            int index = deletedServletName.lastIndexOf("."); //NOI18N
                            if (index>0) deletedName = deletedServletName.substring(index+1);
                            boolean found = false;
                            for (int j=0;j<newFileNames.size();j++) {
                                String newFileName = (String)newFileNames.get(j);
                                String newName = newFileName;
                                int ind = newFileName.lastIndexOf("."); //NOI18N
                                if (ind>0) newName = newFileName.substring(ind+1);
                                if (deletedName.equals(newName)) { // servlet was removed
                                    found=true;
                                    DDChangeEvent ddEvent = 
                                        new DDChangeEvent(EarDataObject.this,EarDataObject.this,deletedServletName,newFileName,DDChangeEvent.EJB_CHANGED);
                                    deploymentChange (ddEvent);
                                    synchronized (EarDataObject.this) {
                                        newFileNames.remove(newFileName);
                                    }
                                    break;
                                }
                            }
                            if (!found) {
                                DDChangeEvent ddEvent = 
                                    new DDChangeEvent(EarDataObject.this,EarDataObject.this,null,deletedServletName,DDChangeEvent.EJB_DELETED);
                                deploymentChange (ddEvent);                                
                            }
                        } //end for
                        synchronized (EarDataObject.this) {
                            deletedEjbNames=null;
                        }
                    } // servlets

                    synchronized (EarDataObject.this) {
                        newFileNames=null;
                    }
                    
                }///end run

            }, 1500, Thread.MIN_PRIORITY);
        }
        else {
            elementTask.schedule (1500);
        }        
    }

    public void fileRenamed(org.openide.filesystems.FileRenameEvent fileRenameEvent) {
//        System.out.println("fileRenamed");                              //NOI18N
//        System.out.println("fileRenameEvent : " + fileRenameEvent);     //NOI18N

        FileObject fo = fileRenameEvent.getFile();
        String resourceName = getPackageName (fo);
        if (resourceName != null) {
            int index = resourceName.lastIndexOf("."); //NOI18N
            String oldName =  fileRenameEvent.getName();
            String oldResourceName = (index>=0?resourceName.substring(0,index+1):"")+oldName;
            Application ejbJar = getApplication();
            if (ejbJar.getStatus()==Application.STATE_VALID) {
                fireEvent(oldResourceName, resourceName, DDChangeEvent.EJB_CHANGED);
                ///Ejb ejb = (Ejb)ejbJar.findBeanByName("Session","EjbClass",oldResourceName); //NOI18N
                /****  
                Listener listener = (Listener)ejbJar.findBeanByName("Listener","ListenerClass",oldResourceName); //NOI18N
                if (listener!=null) {
                    DDChangeEvent ddEvent = 
                        new DDChangeEvent(this,this,oldResourceName,resourceName,DDChangeEvent.LISTENER_CHANGED);
                    deploymentChange (ddEvent);
                }
                Filter filter = (Filter)ejbJar.findBeanByName("Filter","FilterClass",oldResourceName); //NOI18N
                if (filter!=null) {
                    DDChangeEvent ddEvent = 
                        new DDChangeEvent(this,this,oldResourceName,resourceName,DDChangeEvent.FILTER_CHANGED);
                    deploymentChange (ddEvent);
                }***********/
            }
        }
    }
    
    public void fileFolderCreated(org.openide.filesystems.FileEvent fileEvent) {
    }
    
    public void fileDeleted(org.openide.filesystems.FileEvent fileEvent) {
//        System.out.println("fileDeleted");                              //NOI18N
//        System.out.println("fileEvent : " + fileEvent);                 //NOI18N

        FileObject fo = fileEvent.getFile();
        String resourceName = getPackageName (fo);
        if (resourceName != null) {
            boolean foundElement=false;
            if (newFileNames==null) {
                foundElement = 
                    fireEvent(null, resourceName, DDChangeEvent.EJB_DELETED);
                
                /*if (foundElement) return;
                Filter[] filters = getEjbJar().getFilter();
                for (int i=0;i<filters.length;i++) {
                    if (resourceName.equals(filters[i].getFilterClass())) {
                        DDChangeEvent ddEvent = new DDChangeEvent(this,this,null,resourceName,DDChangeEvent.FILTER_DELETED);
                        deploymentChange (ddEvent);
                        foundElement=true;
                        break;
                    }
                }
                if (foundElement) return;
                Listener[] listeners = getEjbJar().getListener();
                for (int i=0;i<listeners.length;i++) {
                    if (resourceName.equals(listeners[i].getListenerClass())) {
                        DDChangeEvent ddEvent = new DDChangeEvent(this,this,null,resourceName,DDChangeEvent.LISTENER_DELETED);
                        deploymentChange (ddEvent);
                        break; // listener with that class should be only one
                    }
                }*/
            } else {
//                Ejb[] ejbs = getEjbJar().getEnterpriseBeans(). getEjbs();
//                for (int i=0;i<ejbs.length;i++) {
//                    if (resourceName.equals(ejbs[i].getEjbClass())) {
//                        synchronized (this) {
//                            if (deletedEjbNames==null) {
//                                deletedEjbNames=new ArrayList();
//                            }
//                            deletedEjbNames.add(resourceName);
//                        }
//                        foundElement=true;
//                        break;
//                    }
//                }
                if (foundElement) return;

               /* Filter[] filters = getEjbJar().getFilter();
                for (int i=0;i<filters.length;i++) {
                    if (resourceName.equals(filters[i].getFilterClass())) {
                        synchronized (this) {
                            if (deletedFilterNames==null) {
                                deletedFilterNames=new ArrayList();
                            }
                            deletedFilterNames.add(resourceName);
                        }
                        foundElement=true;
                        break;
                    }
                }
                if (foundElement) return;
                Listener[] listeners = getEjbJar().getListener();
                for (int i=0;i<listeners.length;i++) {
                    if (resourceName.equals(listeners[i].getListenerClass())) {
                        synchronized (this) {
                            if (deletedListenerNames==null) {
                                deletedListenerNames=new ArrayList();
                            }
                            deletedListenerNames.add(resourceName);
                        }
                        break;
                    }
                }*/
            }
        }
    }
    
    public void fileDataCreated(org.openide.filesystems.FileEvent fileEvent) {
        FileObject fo = fileEvent.getFile();
        String resourceName = getPackageName (fo);
        if (resourceName != null) {
            elementCreated(resourceName);
        }
    }
    
    public void fileChanged(org.openide.filesystems.FileEvent fileEvent) {
    }
    
    public void fileAttributeChanged(org.openide.filesystems.FileAttributeEvent fileAttributeEvent) {
    }
    
    public void stateChanged (javax.swing.event.ChangeEvent e) {
        refreshSourceFolders ();
    }
    
    /** WeakListener for accepting external changes to web.xml
    */
    private class FileObjectObserver implements FileChangeListener {
        FileObjectObserver (FileObject fo) {
            fo.addFileChangeListener((FileChangeListener)org.openide.util.WeakListeners.create(
                                        FileChangeListener.class, this, fo));
        }
        
        public void fileAttributeChanged(FileAttributeEvent fileAttributeEvent) {
        }
        
        public void fileChanged(FileEvent fileEvent) {
            ApplicationProxy ejbJar = (ApplicationProxy) EarDataObject.this.getApplication();
            boolean needRewriting = true;
            if (ejbJar!= null && ejbJar.isWriting()) { // change from outside
                ejbJar.setWriting(false);
                needRewriting=false;
            }
            if (isSavingDocument()) {// document is being saved
                setSavingDocument(false);
                needRewriting=false;
            }
            if (needRewriting) getEditorSupport().restartTimer();
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

//    private Application getEjbFromEjbClass(String ejbClassName){
//        Ejb returnValue = null;
//        Ejb[] ejbs = getEjbJar().getEnterpriseBeans(). getEjbs();
//        for ( int i = 0; i < ejbs.length; i++ ) {
//            if ( ejbs[i].getEjbClass() != null && 
//                    ejbs[i].getEjbClass().equals( ejbClassName ) ){
//                returnValue = ejbs[i];
//                break;
//            }
//        }
//        return returnValue;
//    }
//
//    private int getBeanInterfaceType(String interfaceName){
//        int interfaceType =  -1;
//        EntityAndSession[] beans = ejbJar.getEnterpriseBeans().getSession();
//        for ( int i = 0; i < beans.length; i++ ) {
//            if ( beans[i].getHome() != null && 
//                    beans[i].getHome().equals( interfaceName ) ){
//                interfaceType = HOME;
//                break;
//            }
//            if ( beans[i].getRemote() != null &&
//                    beans[i].getRemote().equals( interfaceName ) ){
//                interfaceType = REMOTE;
//                break;
//            }
//            if ( beans[i].getLocalHome() != null && 
//                    beans[i].getLocalHome().equals( interfaceName ) ){
//                interfaceType = LOCAL_HOME;
//                break;
//            }
//            if ( beans[i].getLocal() != null && 
//                    beans[i].getLocal().equals( interfaceName ) ){
//                interfaceType = LOCAL;
//                break;
//            }
//        }
//        return interfaceType;
//    }
//    
//    private EntityAndSession getBeanFromInterface(String interfaceName){
//        EntityAndSession returnValue =  null;
//        EntityAndSession[] beans = ejbJar.getEnterpriseBeans().getSession();
//        for ( int i = 0; i < beans.length; i++ ) {
//            if ( beans[i].getHome() != null && 
//                    beans[i].getHome().equals( interfaceName ) ){
//                returnValue = beans[i];
//                break;
//            }
//            if ( beans[i].getRemote() != null &&
//                    beans[i].getRemote().equals( interfaceName ) ){
//                returnValue = beans[i];
//                break;
//            }
//            if ( beans[i].getLocalHome() != null && 
//                    beans[i].getLocalHome().equals( interfaceName ) ){
//                returnValue = beans[i];
//                break;
//            }
//            if ( beans[i].getLocal() != null && 
//                    beans[i].getLocal().equals( interfaceName ) ){
//                returnValue = beans[i];
//                break;
//            }
//        }
//        return returnValue;
//    }

    private int getSpecificEvent(int eventType, int interfaceType){
        if(eventType == DDChangeEvent.EJB_CHANGED){
            switch(interfaceType){
                case HOME :{
                    return DDChangeEvent.EJB_HOME_CHANGED;
                }
                case REMOTE :{
                    return DDChangeEvent.EJB_REMOTE_CHANGED;
                }
                case LOCAL_HOME :{
                    return DDChangeEvent.EJB_LOCAL_HOME_CHANGED;
                }
                case LOCAL :{
                    return DDChangeEvent.EJB_LOCAL_CHANGED;
                }
            }
        }
        if(eventType == DDChangeEvent.EJB_DELETED){
            switch(interfaceType){
                case HOME :{
                    return DDChangeEvent.EJB_HOME_DELETED;
                }
                case REMOTE :{
                    return DDChangeEvent.EJB_REMOTE_DELETED; 
                }
                case LOCAL_HOME :{
                    return DDChangeEvent.EJB_LOCAL_HOME_DELETED;
                }
                case LOCAL :{
                    return DDChangeEvent.EJB_LOCAL_DELETED;
                }
            }
        }
        ///assert(false : "control should never reach here -unsupported event type detected"); //NOI18N
        return -1 ;
    }

    private boolean fireEvent(String oldResourceName, String resourceName,
                int eventType){
//            System.out.println("fireEvent");                            //NOI18N
//            System.out.println("oldResourceName : " + oldResourceName); //NOI18N
//            System.out.println("resourceName : " + resourceName);       //NOI18N
//            System.out.println("eventType : " + eventType);             //NOI18N

            boolean elementFound = false;
            String resource = null;
            int specificEventType = -1;
            if(eventType == DDChangeEvent.EJB_CHANGED){
                resource = oldResourceName;
            } else {
                resource = resourceName;
            }
//            Ejb ejb = getEjbFromEjbClass(resource);
//
//            if(ejb != null){
//                if(eventType == DDChangeEvent.EJB_CHANGED){
//                    specificEventType = DDChangeEvent.EJB_CLASS_CHANGED;
//                } else {
//                    specificEventType = DDChangeEvent.EJB_CLASS_DELETED;
//                }
//                elementFound = true;
//            }
//
//            if(!elementFound){
//                int interfaceType = getBeanInterfaceType(resource);
//
//                if(interfaceType > 0 ){
//                    specificEventType = 
//                        getSpecificEvent(eventType, interfaceType);
//                    elementFound = true;
//                }
//            }
            if (elementFound) {
                assert(specificEventType > 0);
                DDChangeEvent ddEvent = 
                    new DDChangeEvent(this,this,oldResourceName,
                            resourceName, specificEventType);
                deploymentChange (ddEvent);
            }
            return elementFound;
    }

    private void updateDD(String oldResourceName, String resourceName,
                int eventType){
//        System.out.println("updateDD");                                 //NOI18N
//        System.out.println("oldResourceName : " + oldResourceName);     //NOI18N
//        System.out.println("resourceName : " + resourceName);           //NOI18N
//        System.out.println("eventType : " + eventType);                 //NOI18N

        boolean ddModified = false;

        switch(eventType){
            case DDChangeEvent.EJB_CLASS_CHANGED :  {
                // update ejb-class
                if (oldResourceName == null)
                    return;

//                Ejb ejb = getEjbFromEjbClass(oldResourceName);
//                if(ejb != null){
//                    ejb.setEjbClass(resourceName);
//                    ddModified = true;
//                }
                break;
            }
            case DDChangeEvent.EJB_CLASS_DELETED :  {
                // delete the whole ejb(impl file deletion)
                if (resourceName == null){
                    return;
                }

//                Ejb ejb = getEjbFromEjbClass(resourceName);
//                if(ejb != null){
//                    EjbJar ejbJar = getEjbJar ();
//                    EnterpriseBeans enterpriseBeans = ejbJar.getEnterpriseBeans();
//                    if(enterpriseBeans != null){
//                        enterpriseBeans.removeEjb (ejb);
//                        ddModified = true;
//                    }
//                }
                break;
            }

            case DDChangeEvent.EJB_HOME_CHANGED :  {
                if (oldResourceName == null)
                    return;

//                EntityAndSession bean = getBeanFromInterface(oldResourceName);
//                if(bean != null){
//                    assert(oldResourceName.equals (bean.getHome()));
//                    bean.setHome(resourceName);
//                    ddModified = true;
//                }
                break;
            }

            case DDChangeEvent.EJB_REMOTE_CHANGED :  {
                if (oldResourceName == null)
                    return;

//                EntityAndSession bean = getBeanFromInterface(oldResourceName);
//                if(bean != null){
//                    assert(oldResourceName.equals (bean.getRemote()));
//                    bean.setRemote(resourceName);
//                    ddModified = true;
//                }
                break;
            }

            case DDChangeEvent.EJB_LOCAL_HOME_CHANGED :  {
                if (oldResourceName == null)
                    return;

//                EntityAndSession bean = getBeanFromInterface(oldResourceName);
//                if(bean != null){
//                    assert(oldResourceName.equals (bean.getLocalHome()));
//                    bean.setLocalHome(resourceName);
//                    ddModified = true;
//                }
                break;
            }

            case DDChangeEvent.EJB_LOCAL_CHANGED :  {
                if (oldResourceName == null)
                    return;

//                EntityAndSession bean = getBeanFromInterface(oldResourceName);
//                if(bean != null){
//                    assert(oldResourceName.equals (bean.getLocal()));
//                    bean.setLocal(resourceName);
//                    ddModified = true;
//                }
                break;
            }

            case DDChangeEvent.EJB_HOME_DELETED :  {
                if (resourceName == null){
                    return;
                }

//                EntityAndSession bean = getBeanFromInterface(resourceName);
//                if(bean != null){
//                    assert(resourceName.equals (bean.getHome()));
//                    bean.setHome(null);
//                    ddModified = true;
//                }
                break;
            }
            
            case DDChangeEvent.EJB_REMOTE_DELETED :  {
                if (resourceName == null){
                    return;
                }

//                EntityAndSession bean = getBeanFromInterface(resourceName);
//                if(bean != null){
//                    assert(resourceName.equals (bean.getRemote()));
//                    bean.setRemote(null);
//                    ddModified = true;
//                }
                break;
            }

            case DDChangeEvent.EJB_LOCAL_HOME_DELETED :  {
                if (resourceName == null){
                    return;
                }

//                EntityAndSession bean = getBeanFromInterface(resourceName);
//                if(bean != null){
//                    assert(resourceName.equals (bean.getLocalHome()));
//                    bean.setLocalHome(null);
//                    ddModified = true;
//                }
                break;
            }

            case DDChangeEvent.EJB_LOCAL_DELETED :  {
                if (resourceName == null){
                    return;
                }

//                EntityAndSession bean = getBeanFromInterface(resourceName);
//                if(bean != null){
//                    assert(resourceName.equals (bean.getLocal()));
//                    bean.setLocal(null);
//                    ddModified = true;
//                }
                break;
            }
        }

        if(ddModified){
            setNodeDirty (true);
        }
            /*
            EjbJar a = getEjbJar ();
            Ejb[] ejbs = a.getEnterpriseBeans(). getEjbs();
            java.util.Vector EJBNames = new java.util.Vector ();
            for (int i=0; i<ejbs.length; i++) {
                if (clz.equals (ejbs[i].getEjbClass ())) {
                    EJBNames.addElement (ejbs[i].getEjbName ());
                    a.getEnterpriseBeans().removeEjb (ejbs[i]);
                }
            }
           */
           //delete other elements, if any, refering to this ejb
           // ServletMapping [] mappings = wa.getServletMapping ();
           // for (int i=0; i<mappings.length; i++) {
           //     if (EJBNames.contains (mappings[i].getServletName ())) {
           //         wa.removeEJBMapping (mappings[i]);
           //     }
           // }
    }
}
