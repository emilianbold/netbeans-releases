/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */


package org.netbeans.modules.j2ee.archive.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.Action;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.j2ee.archive.project.ArchiveProject;
import org.netbeans.modules.j2ee.archive.project.ArchiveProjectProperties;
import org.netbeans.modules.j2ee.spi.ejbjar.support.J2eeProjectView;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.netbeans.spi.project.ui.support.ProjectSensitiveActions;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
//import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;

public class RootNode extends org.openide.nodes.AbstractNode {
    
//    private boolean broken;
    
//    private BrokenLinksAction brokenLinksAction;
//
//    private BrokenServerAction brokenServerAction;
    
    public RootNode(ArchiveProject ap) {
        super(new LogicalViewChildren(ap), createLookup(ap));
        super.setName(ProjectUtils.getInformation(ap).getDisplayName());
        setIconBaseWithExtension("org/netbeans/modules/j2ee/archive/project/resources/packaged_archive_16.png"); // NOI18N
//        if (hasBrokenLinks()) {
//            broken = true;
//        }
//        brokenLinksAction = new BrokenLinksAction();
//        brokenServerAction = new BrokenServerAction();
        
        ArchiveProjectProperties app = ap.getArchiveProjectProperties();
        String sourceArchive = ap.getPropertyEvaluator().evaluate((String)app.get(ArchiveProjectProperties.SOURCE_ARCHIVE));
        setShortDescription(NbBundle.getMessage(RootNode.class,"LBL_ProjectToolTip",        // NOI18N
                sourceArchive)); 
    }
    
    public Action[] getActions( boolean context ) {
        Action[] retVal;
        if ( context ) {
            retVal = super.getActions( true );
        } else {
            retVal = getAdditionalActions();
        }
        return retVal;
    }

    private Action[] getAdditionalActions() {
        
        ResourceBundle bundle = NbBundle.getBundle(RootNode.class);
        
        List actions = new ArrayList(30);
        actions.add(CommonProjectActions.newFileAction());
        actions.add(null);
        actions.add(ProjectSensitiveActions.projectCommandAction( 
                ActionProvider.COMMAND_BUILD, bundle.getString("LBL_BuildAction_Name"), null)); // NOI18N
        actions.add(ProjectSensitiveActions.projectCommandAction( 
                "verify", bundle.getString("LBL_VerifyAction_Name"), null)); // NOI18N
        actions.add(ProjectSensitiveActions.projectCommandAction(
                ActionProvider.COMMAND_RUN, bundle.getString("LBL_RunAction_Name"), null)); // NOI18N
        actions.add(null);
        actions.add(CommonProjectActions.setAsMainProjectAction());
        actions.add(CommonProjectActions.openSubprojectsAction());
        actions.add(CommonProjectActions.closeProjectAction());
        actions.add(null);
        actions.add(CommonProjectActions.renameProjectAction());
        actions.add(CommonProjectActions.moveProjectAction());
        actions.add(CommonProjectActions.copyProjectAction());
        actions.add(CommonProjectActions.deleteProjectAction());
        actions.add(null);
        actions.add(SystemAction.get( org.openide.actions.FindAction.class ));
        
        // honor 57874 contact
        actions.add(null);
        actions.addAll(Utilities.actionsForPath("Projects/Actions")); // NOI18N
        
//        actions.add(null);
//        if (brokenLinksAction != null && brokenLinksAction.isEnabled()) {
//            actions.add(brokenLinksAction);
//        }
//        if (brokenServerAction.isEnabled()) {
//            actions.add(brokenServerAction);
//        }
        actions.add(null);
        actions.add(CommonProjectActions.customizeProjectAction());
        
        return (Action[])actions.toArray(new Action[actions.size()]);
    }
    
    static final class LogicalViewChildren extends Children.Keys/*<FileObject>*/  implements FileChangeListener {
        
        // XXX does not react correctly to addition or removal of src/ subdir
        
        private static final String KEY_DOC_BASE = "docBase"; //NOI18N
        private static final String KEY_SETUP_DIR = "setupDir"; //NOI18N
        
        private ArchiveProject project;
        
        public LogicalViewChildren(ArchiveProject project) { 
            assert project != null;
            this.project = project;
        }
        
        protected void addNotify() {
            super.addNotify();
            project.getProjectDirectory().addFileChangeListener(this);
            createNodes();
        }

        private void createNodes() {
            List l = new ArrayList();            
            DataFolder docBaseDir = getFolder((String)project.getArchiveProjectProperties().get(ArchiveProjectProperties.PROXY_PROJECT_DIR)); // WEB_CONF);
            if (docBaseDir != null) {
                l.add(KEY_DOC_BASE);
            }
            l.add(KEY_SETUP_DIR);
            
            String dir = (String)project.getArchiveProjectProperties().get(ArchiveProjectProperties.PROXY_PROJECT_DIR);
            FileObject fo = project.getProjectDirectory().getFileObject("subarchives");     // NOI18N
            if (null != fo) {
                FileObject subarchives[] = fo.getChildren();
                for (FileObject innerFo : subarchives) {
                    FileObject innerProj = null;
                    if (null != innerFo) {
                        innerProj = innerFo.getFileObject("tmpproj");           // NOI18N
                    }
                    if (null != innerProj && innerProj.isFolder()) {
                        DataFolder folder = getFolder("subarchives/"+innerFo.getName()+"/"+dir);        // NOI18N
                        if (null == folder) {
                            continue;
                        }
                        Node n = new ModuleNode(folder, 
                                NbBundle.getMessage(RootNode.class,"LBL_ModuleNode",        // NOI18N
                                project.getEarPath(innerFo.getName())));
                        l.add(n);
                    }
                }
            }
            setKeys(l);
        }
        
        protected void removeNotify() {
            setKeys(Collections.EMPTY_SET);
            project.getProjectDirectory().removeFileChangeListener(this);
            super.removeNotify();
        }
        
        protected Node[] createNodes(Object key) {
            Node n;
            if (key == KEY_DOC_BASE) {
                n = new ConfigFilesNode(getFolder((String)project.getArchiveProjectProperties().get(ArchiveProjectProperties.PROXY_PROJECT_DIR)));
            }  else if (key == KEY_SETUP_DIR) {
                n = J2eeProjectView.createServerResourcesNode(project);
            } else {
                n = (Node) key;
            }
            return n == null ? new Node[0] : new Node[] {n};
        }
        
        private DataFolder getFolder(String dir) {
            FileObject fo = project.getProjectDirectory();
            if (null == dir) {
                return null;
            }
            if (null != fo) {
                fo = fo.getFileObject(dir);
            }
            if (null == fo) {
                return null;
            }
            
            // check for a web app
            fo = fo.getFileObject("web");                                       // NOI18N
            if (null != fo) {
                fo = fo.getFileObject("WEB-INF");                               // NOI18N
                if (fo != null) {
                    DataFolder df = DataFolder.findFolder(fo);
                    return df;
                } else {
                    return null;
                }
            }
            fo = project.getProjectDirectory().getFileObject(dir);
            if (null == fo) {
                return null;
            }
            fo = fo.getFileObject("src");                                       // NOI18N
            if (null == fo) {
                return null;
            }
            fo = fo.getFileObject("conf");                                      // NOI18N
            if (null == fo) {
                return null;
            }
            return DataFolder.findFolder(fo);
        }
        
        // file change events in the project directory
        public void fileAttributeChanged(org.openide.filesystems.FileAttributeEvent fe) {
        }
        
        public void fileChanged(org.openide.filesystems.FileEvent fe) {
        }
        
        public void fileDataCreated(org.openide.filesystems.FileEvent fe) {
        }
        
        public void fileDeleted(org.openide.filesystems.FileEvent fe) {
            // setup folder deleted
             //this was playing in a deadlock stack of 74613.
             // This needs to be re-evaluated
             // Filed as 76844
             //createNodes();
        }
        
        public void fileFolderCreated(org.openide.filesystems.FileEvent fe) {
            // setup folder could be created
            createNodes();
        }
        
        public void fileRenamed(org.openide.filesystems.FileRenameEvent fe) {
            // setup folder could be renamed
            createNodes();
        }
    }
    
    
    private static Lookup createLookup( Project project ) {
        DataFolder rootFolder = DataFolder.findFolder( project.getProjectDirectory() );
        // XXX Remove root folder after FindAction rewrite
        return Lookups.fixed( new Object[] { project, rootFolder } );
    }
    
//    private static final String[] BREAKABLE_PROPERTIES = new String[] {
//        ArchiveProjectProperties.SOURCE_ARCHIVE,
//    };
    
//    public boolean hasBrokenLinks() {
//        return BrokenReferencesSupport.isBroken(helper.getAntProjectHelper(), resolver, getBreakableProperties(),
//                new String[] {WebProjectProperties.JAVA_PLATFORM});
//    }
    
//    private String[] getBreakableProperties() {
////        SourceRoots roots = this.project.getSourceRoots();
////        String[] srcRootProps = roots.getRootProperties();
////        roots = this.project.getTestSourceRoots();
////        String[] testRootProps = roots.getRootProperties();
//        String[] result = new String [BREAKABLE_PROPERTIES.length]; //  + srcRootProps.length + testRootProps.length];
//        System.arraycopy(BREAKABLE_PROPERTIES, 0, result, 0, BREAKABLE_PROPERTIES.length);
////        System.arraycopy(srcRootProps, 0, result, BREAKABLE_PROPERTIES.length, srcRootProps.length);
////        System.arraycopy(testRootProps, 0, result, BREAKABLE_PROPERTIES.length + srcRootProps.length, testRootProps.length);
//        return result;
//    }
    
//    private static Image brokenProjectBadge = Utilities.loadImage( "org/netbeans/modules/web/project/ui/resources/brokenProjectBadge.gif" ); // NOI18N
    
    /** This action is created only when project has broken references.
     * Once these are resolved the action is disabled.
     */
//    private class BrokenLinksAction extends AbstractAction implements PropertyChangeListener, ChangeListener, Runnable {
//
//        private RequestProcessor.Task task = null;
//
//        private PropertyChangeListener weakPCL;
//
//        public BrokenLinksAction() {
//            putValue(Action.NAME, NbBundle.getMessage(RootNode.class, "LBL_Fix_Broken_Links_Action"));
//            setEnabled(broken);
////            evaluator.addPropertyChangeListener( this );
//            // When evaluator fires changes that platform properties were
//            // removed the platform still exists in JavaPlatformManager.
//            // That's why I have to listen here also on JPM:
//            //weakPCL = WeakListeners.propertyChange( this, JavaPlatformManager.getDefault() );
//            //JavaPlatformManager.getDefault().addPropertyChangeListener( weakPCL );
//            RootNode.this.addChangeListener((ChangeListener)WeakListeners.change(this, RootNode.this));
//        }
//
//        public void actionPerformed(ActionEvent e) {
//            try {
//                helper.requestSave();
//                BrokenReferencesSupport.showCustomizer(helper.getAntProjectHelper(), resolver, getBreakableProperties(), new String[]{WebProjectProperties.JAVA_PLATFORM});
//                run();
//            } catch (IOException ioe) {
//                ErrorManager.getDefault().notify(ioe);
//            }
//        }
//
//        public void propertyChange(PropertyChangeEvent evt) {
//            refsMayChanged();
//        }
//
//        public void stateChanged(ChangeEvent evt) {
//            refsMayChanged();
//        }
//
//        public synchronized void run() {
//            boolean old = broken;
//            broken = hasBrokenLinks();
//            if (old != broken) {
//                setEnabled(broken);
//                fireIconChange();
//                fireOpenedIconChange();
//                fireDisplayNameChange(null, null);
//                //project.getWebProjectProperties().save();
//            }
//        }
//
//        public void refsMayChanged() {
//            // check project state whenever there was a property change
//            // or change in list of platforms.
//            // Coalesce changes since they can come quickly:
//            if (task == null) {
//                task = BROKEN_LINKS_RP.create(this);
//            }
//            task.schedule(100);
//        }
//
//    }
//
//    private class BrokenServerAction extends AbstractAction implements
//            InstanceListener, PropertyChangeListener {
//
//        private boolean brokenServer;
//
//        public BrokenServerAction() {
//            putValue(Action.NAME, NbBundle.getMessage(RootNode.class, "LBL_Fix_Missing_Server_Action")); // NOI18N
//            evaluator.addPropertyChangeListener(this);
//            checkMissingServer();
//        }
//
//        public boolean isEnabled() {
//            return brokenServer;
//        }
//
//        public void actionPerformed(ActionEvent e) {
//            BrokenServerSupport.showCustomizer(project, helper);
//            checkMissingServer();
//        }
//
//        public void propertyChange(PropertyChangeEvent evt) {
//            if (ArchiveProjectProperties.J2EE_SERVER_INSTANCE.equals(evt.getPropertyName())) {
//                checkMissingServer();
//            }
//        }
//
//        public void changeDefaultInstance(String oldServerInstanceID, String newServerInstanceID) {
//        }
//
//        public void instanceAdded(String serverInstanceID) {
//            checkMissingServer();
//        }
//
//        public void instanceRemoved(String serverInstanceID) {
//            checkMissingServer();
//        }
//
//        private void checkMissingServer() {
//            boolean old = brokenServer;
//            String servInstID = evaluator.getProperty(ArchiveProjectProperties.J2EE_SERVER_INSTANCE);
//            brokenServer = BrokenServerSupport.isBroken(servInstID);
//            if (old != brokenServer) {
//                fireIconChange();
//                fireOpenedIconChange();
//                fireDisplayNameChange(null, null);
//            }
//        }
//    }
    
}
