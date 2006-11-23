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

package org.netbeans.modules.project.ui;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.openide.ErrorManager;
import org.openide.cookies.EditCookie;
import org.openide.cookies.OpenCookie;
import org.openide.explorer.ExplorerManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.ContextAwareAction;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/** The util methods for projectui module.
 *
 * @author  Jiri Rechtacek
 */
public class ProjectUtilities {
    
    static final String OPEN_FILES_NS = "http://www.netbeans.org/ns/projectui-open-files/1"; // NOI18N
    static final String OPEN_FILES_ELEMENT = "open-files"; // NOI18N
    static final String FILE_ELEMENT = "file"; // NOI18N
    
    // support class for xtesting in OpenProjectListTest
    static OpenCloseProjectDocument OPEN_CLOSE_PROJECT_DOCUMENT_IMPL = new OpenCloseProjectDocument () {
        public boolean open (FileObject fo) {
            DataObject dobj;
            try {
                dobj = DataObject.find (fo);
            } catch (DataObjectNotFoundException donfo) {
                assert false : "DataObject must exist for " + fo;
                return false;
            }
            EditCookie ec = (EditCookie) dobj.getCookie (EditCookie.class);
            OpenCookie oc = (OpenCookie) dobj.getCookie (OpenCookie.class);
            if (ec != null) {
                ((EditCookie) ec).edit ();
            } else if (oc != null) {
                ((OpenCookie) oc).open ();
            } else {
                if (ERR.isLoggable (ErrorManager.INFORMATIONAL)) ERR.log ("No EditCookie nor OpenCookie for " + dobj);
                return false;
            }
            return true;
        }
        
        public Map<Project,SortedSet<String>> close(final Project[] projects,
                                                    final boolean notifyUI) {
            final Wrapper wr = new Wrapper();

            wr.urls4project = new HashMap<Project,SortedSet<String>>();
            if (SwingUtilities.isEventDispatchThread()) {
                doClose(projects, notifyUI, wr);
            } else {
                try {
                    SwingUtilities.invokeAndWait(new Runnable() {
                         public void run() {
                             doClose(projects, notifyUI, wr);
                         }
                     });
                }
                catch (Exception ex) {
                    ERR.notify(ErrorManager.INFORMATIONAL, ex);
                }
            }
            return wr.urls4project;
        }

        private void doClose(Project[] projects, boolean notifyUI, Wrapper wr) {
            List<Project> listOfProjects = Arrays.asList(projects);
            Set<DataObject> openFiles = new HashSet<DataObject>();
            final Set<TopComponent> tc2close = new HashSet<TopComponent>();
            @SuppressWarnings(value = "unchecked")
            Iterator<TopComponent> openTCs = WindowManager.getDefault().getRegistry().getOpened().iterator();

            while (openTCs.hasNext()) {
                TopComponent tc = openTCs.next();

                // #57621: check if the closed top component isn't instance of ExplorerManager.Provider e.g. Projects/Files tab, if yes then do skip this loop
                if (tc instanceof ExplorerManager.Provider) {
                    continue;
                }
                // #68677: closing only documents in the editor, not eg. navigator window:
                Mode m = WindowManager.getDefault().findMode(tc);

                if (m == null ||
                    !CloneableEditorSupport.EDITOR_MODE.equals(m.getName())) {
                    continue;
                }
                DataObject dobj = (DataObject) tc.getLookup().lookup(DataObject.class);

                if (dobj != null) {
                    FileObject fobj = dobj.getPrimaryFile();
                    Project owner = FileOwnerQuery.getOwner(fobj);

                    if (listOfProjects.contains(owner)) {
                        if (notifyUI) {
                            openFiles.add(dobj);
                            tc2close.add(tc);
                        } else if (!dobj.isModified()) {
                            // when not called from UI, only include TCs that arenot modified
                            tc2close.add(tc);
                        }
                        if (!wr.urls4project.containsKey(owner)) {
                            // add project
                            wr.urls4project.put(owner, new TreeSet<String>());
                        }
                        URL url = null;

                        try {
                            url = dobj.getPrimaryFile().getURL();
                            wr.urls4project.get(owner).add(url.toExternalForm());
                        }
                        catch (FileStateInvalidException fsie) {
                            assert false : "FileStateInvalidException in " +
                                           dobj.getPrimaryFile();
                        }
                    }
                }
            }
            if (notifyUI) {
                Iterator iter = DataObject.getRegistry().getModifiedSet().iterator();

                while (iter.hasNext()) {
                    DataObject dobj = (DataObject) iter.next();

                    if (dobj != null) {
                        FileObject fobj = dobj.getPrimaryFile();
                        Project owner = FileOwnerQuery.getOwner(fobj);

                        if (listOfProjects.contains(owner) &&
                            !openFiles.contains(dobj)) {
                            openFiles.add(dobj);
                        }
                    }
                }
            }
            if (!notifyUI ||
                (!openFiles.isEmpty() && ExitDialog.showDialog(openFiles))) {
                // close documents
                Iterator it = tc2close.iterator();
                while (it.hasNext()) {
                    ((TopComponent) it.next()).close();
                }
            } else {
                // signal that close was vetoed
                if (!openFiles.isEmpty()) {
                    wr.urls4project = null;
                }
            }
        }
    };
    
    private static class Wrapper {
        Map<Project,SortedSet<String>> urls4project;
    }
    
    private static final ErrorManager ERR = ErrorManager.getDefault().getInstance(ProjectUtilities.class.getName());
    
    private ProjectUtilities() {}
    
    public static void selectAndExpandProject( final Project p ) {
        
        // invoke later to select the being opened project if the focus is outside ProjectTab
        SwingUtilities.invokeLater (new Runnable () {
            
            final ProjectTab ptLogial = ProjectTab.findDefault(ProjectTab.ID_LOGICAL);
            
            public void run () {
                Node root = ptLogial.getExplorerManager ().getRootContext ();
                // Node projNode = root.getChildren ().findChild( p.getProjectDirectory().getName () );
                Node[] nds = root.getChildren().getNodes();
                Node projNode = null;
                for (int i = 0; i < nds.length; i++) {
                    Project prj = (Project)nds[i].getLookup().lookup(Project.class);
                    if (prj != null && prj.getProjectDirectory().equals(p.getProjectDirectory())) {
                        projNode = nds[i];
                        break;
                    }
                }
                if (projNode == null) {
                    // fallback..
                    projNode = root.getChildren ().findChild( ProjectUtils.getInformation( p ).getName() );
                }
                
                if ( projNode != null ) {
                    try {                            
                        ptLogial.getExplorerManager ().setSelectedNodes( new Node[] { projNode } );
                        ptLogial.expandNode( projNode );
                        // ptLogial.open ();
                        // ptLogial.requestActive ();
                    } catch (Exception ignore) {
                        // may ignore it
                    }
                }
            }
        });
        
    }
    
    /** Invokes the preferred action on given object and tries to select it in
     * corresponding view, e.g. in logical view if possible otherwise
     * in physical project's view.
     * Note: execution this methods can invokes new threads to assure the action
     * is called in EQ.
     *
     * @param newDo new data object
     */   
    public static void openAndSelectNewObject (final DataObject newDo) {
        // call the preferred action on main class
        Mutex.EVENT.writeAccess (new Runnable () {
            public void run () {
                final Node node = newDo.getNodeDelegate ();
                Action a = node.getPreferredAction();
                if (a instanceof ContextAwareAction) {
                    a = ((ContextAwareAction) a).createContextAwareInstance(node.getLookup ());
                }
                if (a != null) {
                    a.actionPerformed(new ActionEvent(node, ActionEvent.ACTION_PERFORMED, "")); // NOI18N
                }

                // next action -> expand && select main class in package view
                final ProjectTab ptLogial  = ProjectTab.findDefault (ProjectTab.ID_LOGICAL);
                final ProjectTab ptPhysical  = ProjectTab.findDefault (ProjectTab.ID_PHYSICAL);
                // invoke later, Mutex.EVENT.writeAccess isn't suffice to 
                // select && expand if the focus is outside ProjectTab
                SwingUtilities.invokeLater (new Runnable () {
                    public void run () {
                        boolean success = ptLogial.selectNode (newDo.getPrimaryFile ());
                        if (!success) {
                            ptPhysical.selectNode (newDo.getPrimaryFile ());
                        }
                    }
                });
            }
        });
    }
    
    /** Makes the project tab visible
     * @param requestFocus if set to true the project tab will not only become visible but also
     *        will gain focus
     */
    public static void makeProjectTabVisible( final boolean requestFocus ) {
        final ProjectTab ptLogical  = ProjectTab.findDefault (ProjectTab.ID_LOGICAL);
        
//        SwingUtilities.invokeLater (new Runnable () {
//            public void run () {
                ptLogical.open();
                if ( requestFocus ) {
                    ptLogical.requestActive();
                }
                else {
                    ptLogical.requestVisible();
                }
//            }
//        });
                
    }
    
    
    /** Checks if the given file name can be created in the target folder.
     *
     * @param targetFolder target folder (e.g. source group)
     * @param folderName name of the folder relative to target folder (null or /-separated)
     * @param newObjectName name of created file
     * @param extension extension of created file
     * @param allowFileSeparator if '/' (and possibly other file separator, see {@link FileUtil#createFolder FileUtil#createFolder})
     *                           is allowed in the newObjectName
     * @return localized error message or null if all right
     */    
    public static String canUseFileName (FileObject targetFolder, String folderName, String newObjectName, String extension, boolean allowFileSeparator) {
        assert newObjectName != null; // SimpleTargetChooserPanel.isValid returns false if it is... XXX should it use an error label instead?

        boolean allowSlash = false;
        boolean allowBackslash = false;
        int errorVariant = 0;
        
        if (allowFileSeparator) {
            if (File.separatorChar == '\\') {
                errorVariant = 3;
                allowSlash = allowBackslash = true;
            } else {
                errorVariant = 1;
                allowSlash = true;
            }
        }
        
        if ((!allowSlash && newObjectName.indexOf('/') != -1) || (!allowBackslash && newObjectName.indexOf('\\') != -1)) {
            //if errorVariant == 3, the test above should never be true:
            assert errorVariant == 0 || errorVariant == 1 : "Invalid error variant: " + errorVariant;
            
            return NbBundle.getMessage(ProjectUtilities.class, "MSG_not_valid_filename", newObjectName, new Integer(errorVariant));
        }
        
        // test whether the selected folder on selected filesystem already exists
        if (targetFolder == null) {
            return NbBundle.getMessage (ProjectUtilities.class, "MSG_fs_or_folder_does_not_exist"); // NOI18N
        }
        
        // target filesystem should be writable
        if (!targetFolder.canWrite ()) {
            return NbBundle.getMessage (ProjectUtilities.class, "MSG_fs_is_readonly"); // NOI18N
        }

        // file should not already exist
        StringBuffer relFileName = new StringBuffer();
        if (folderName != null) {
            if (!allowBackslash && folderName.indexOf('\\') != -1) {
                return NbBundle.getMessage(ProjectUtilities.class, "MSG_not_valid_folder", folderName, new Integer(1));
            }
            relFileName.append(folderName);
            relFileName.append('/');
        }
        relFileName.append(newObjectName);
        if (extension != null && extension.length() != 0) {
            relFileName.append('.');
            relFileName.append(extension);
        }
        if (targetFolder.getFileObject(relFileName.toString()) != null) {
            return NbBundle.getMessage (ProjectUtilities.class, "MSG_file_already_exist", newObjectName); // NOI18N
        }
        
        // all ok
        return null;
    }
    
    
    public static class WaitCursor implements Runnable {
        
        private boolean show;
        
        private WaitCursor( boolean show ) {
            this.show = show;
        }
       
        public static void show() {            
            invoke( new WaitCursor( true ) );
        }
        
        public static void hide() {
            invoke( new WaitCursor( false ) );            
        }
        
        private static void invoke( WaitCursor wc ) {
            if ( SwingUtilities.isEventDispatchThread() ) {
                wc.run();
            }
            else {
                SwingUtilities.invokeLater( wc );
            }
        }
        
        public void run() {
            try {            
                JFrame f = (JFrame)WindowManager.getDefault ().getMainWindow ();
                Component c = f.getGlassPane ();
                c.setVisible ( show );
                c.setCursor (show ? Cursor.getPredefinedCursor (Cursor.WAIT_CURSOR) : null);
            } 
            catch (NullPointerException npe) {
                ErrorManager.getDefault ().notify (ErrorManager.INFORMATIONAL, npe);
            }
        }
    }
    
    /** Closes all documents in editor area which are owned by one of given projects.
     * If some documents are modified then an user is notified by Save/Discard/Cancel dialog.
     * Dialog is showed only once for all project's documents together.
     * URLs of closed documents are stored to <code>private.xml</code>.
     *
     * @param p project to close
     * @return false if the user cancelled the Save/Discard/Cancel dialog, true otherwise
     */    
    public static boolean closeAllDocuments(Project[] projects, boolean notifyUI) {
        if (projects == null) {
            throw new IllegalArgumentException ("No projects are specified."); // NOI18N
        }
        
        if (projects.length == 0) {
            // no projects to close, no documents will be closed
            return true;
        }
        
        Map/*<Project, SortedSet<String>>*/ urls4project = OPEN_CLOSE_PROJECT_DOCUMENT_IMPL.close (projects, notifyUI);

        if (urls4project != null) {
            // store project's documents
            // loop all project being closed
            Iterator loop = urls4project.keySet ().iterator ();
            Project p;
            while (loop.hasNext ()) {
                p = (Project) loop.next ();
                storeProjectOpenFiles (p, (SortedSet)urls4project.get (p));
            }
        }
        
        return urls4project != null;
    }
    
    static private void storeProjectOpenFiles (Project p, SortedSet/*<String>*/ urls) {
        AuxiliaryConfiguration aux = (AuxiliaryConfiguration) p.getLookup ().lookup (AuxiliaryConfiguration.class);
        if (aux != null) {
            
            aux.removeConfigurationFragment (OPEN_FILES_ELEMENT, OPEN_FILES_NS, false);

            Document xml = XMLUtil.createDocument (OPEN_FILES_ELEMENT, OPEN_FILES_NS, null, null);
            Element fileEl;
            
            Element openFiles = xml.createElementNS (OPEN_FILES_NS, OPEN_FILES_ELEMENT);
            
            // loop all open files of given project
            Iterator it = urls.iterator ();
            while (it.hasNext ()) {
                fileEl = openFiles.getOwnerDocument ().createElement (FILE_ELEMENT);
                fileEl.appendChild (fileEl.getOwnerDocument ().createTextNode ((String)it.next ()));
                openFiles.appendChild (fileEl);
            }
            
            aux.putConfigurationFragment (openFiles, false);
        }
    }
    
    /** Opens the project's files read from the private <code>project.xml</code> file
     * 
     * @param p project
     */
    public static void openProjectFiles (Project p) {
        boolean dolog = ERR.isLoggable(ErrorManager.INFORMATIONAL);
        if (dolog) ERR.log("Trying to open files from " + p + "...");
        
        AuxiliaryConfiguration aux = (AuxiliaryConfiguration) p.getLookup ().lookup (AuxiliaryConfiguration.class);
        
        if (aux == null) {
            if (dolog) ERR.log("No AuxiliaryConfiguration in " + p);
            return ;
        }
        
        Element openFiles = aux.getConfigurationFragment (OPEN_FILES_ELEMENT, OPEN_FILES_NS, false);
        if (openFiles == null) {
            if (dolog) ERR.log("No " + OPEN_FILES_ELEMENT + " in private.xml");
            return;
        }

        NodeList list = openFiles.getElementsByTagName (FILE_ELEMENT);
        if (list == null) {
            if (dolog) ERR.log("No " + FILE_ELEMENT + " in " + OPEN_FILES_ELEMENT);
            return ;
        }
        
        for (int i = 0; i < list.getLength (); i++) {
            String url = list.item (i).getChildNodes ().item (0).getNodeValue ();
            if (dolog) ERR.log("Will try to open " + url);
            FileObject fo;
            try {
                fo = URLMapper.findFileObject (new URL (url));
            } catch (MalformedURLException mue) {
                assert false : "MalformedURLException in " + url;
                continue;
            }
            if (fo == null) {
                if (dolog) ERR.log("Could not find " + url);
                continue;
            }
            
            OPEN_CLOSE_PROJECT_DOCUMENT_IMPL.open (fo);
        }
        
        // clean-up stored files
        aux.removeConfigurationFragment (OPEN_FILES_ELEMENT, OPEN_FILES_NS, false);
    }
    
    // interface for handling project's documents stored in project private.xml
    // it serves for a unit test of OpenProjectList
    static interface OpenCloseProjectDocument {
        
        // opens stored document in the document area
        public boolean open (FileObject fo);
        
        // closes documents of given projects and returns mapped document's urls by project
        // it's used as base for storing documents in project private.xml
        public Map<Project,SortedSet<String>> close(Project[] projects, boolean notifyUI);
    }
    
}
