/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.compapp.projects.jbi.ui;

import javax.swing.Action;
import org.netbeans.modules.compapp.projects.jbi.JbiProject;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
//import org.netbeans.modules.j2ee.ejbjar.project.ui.ServerResourceNode;
import org.netbeans.modules.compapp.projects.jbi.ui.customizer.JbiProjectProperties;

import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;

// import org.netbeans.modules.websvc.api.webservices.WebServicesView;
import org.openide.filesystems.FileChangeListener;

//import org.netbeans.modules.j2ee.common.ui.ServerResourceNode;
import org.openide.filesystems.FileObject;

import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;

import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;

import org.openide.nodes.NodeAdapter;
import org.openide.nodes.NodeEvent;
import org.openide.util.NbBundle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;
import org.netbeans.api.queries.VisibilityQuery;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.ChangeableDataFilter;
import org.openide.loaders.DataFilter;


/**
 * DOCUMENT ME!
 *
 * @author 
 * @version 
 */
class JbiViews {
    private JbiViews() {
    }
    
    /**
     * DOCUMENT ME!
     *
     * @author 
     * @version 
     */
    static final class LogicalViewChildren extends Children.Keys implements FileChangeListener {
        private static final String KEY_SVC_COMP_NODE = "SvcCompNode"; // NOI18N
        private static final String KEY_SOURCE_DIR = "srcDir"; // NOI18N
        private static final String KEY_DOC_BASE = "docBase"; // NOI18N
        private static final String KEY_JBIS = "jbiKey"; // NOI18N
//        private static final String WEBSERVICES_DIR = "webservicesDir"; // NOI18N
        private static final String KEY_SETUP_DIR = "setupDir"; // NOI18N
        private AntProjectHelper helper;
        private final PropertyEvaluator evaluator;
        private FileObject projectDir;
        private Project project;
        
        /**
         * Creates a new LogicalViewChildren object.
         *
         * @param helper DOCUMENT ME!
         * @param evaluator DOCUMENT ME!
         * @param project DOCUMENT ME!
         */
        public LogicalViewChildren(
                AntProjectHelper helper, PropertyEvaluator evaluator, Project project
                ) {
            assert helper != null;
            this.helper = helper;
            projectDir = helper.getProjectDirectory();
            this.evaluator = evaluator;
            this.project = project;
        }
        
        /**
         * DOCUMENT ME!
         */
        @Override
        protected void addNotify() {
            super.addNotify();
            projectDir.addFileChangeListener(FileUtil.weakFileChangeListener(this, projectDir));
            createNodes();
        }
        
        private void createNodes() {
            List l = new ArrayList();
            
//            DataFolder docBaseDir = getFolder(JbiProjectProperties.META_INF);
//
//            if (docBaseDir != null) {
//                l.add(KEY_DOC_BASE);
//            }
            l.add(KEY_SVC_COMP_NODE);
            DataFolder srcDir = getFolder(JbiProjectProperties.SRC_DIR);
            
            if (srcDir != null) {
                l.add(KEY_SOURCE_DIR);
            }
            
            FileObject setupFolder = getSetupFolder();
            
            if ((setupFolder != null) && setupFolder.isFolder()) {
                l.add(KEY_SETUP_DIR);
            }
            
            /*
               l.add(WEBSERVICES_DIR);
             */
            setKeys(l);
        }
        
        private FileObject getSetupFolder() {
            return projectDir.getFileObject("setup"); // NOI18N
        }
        
        /**
         * DOCUMENT ME!
         */
        @Override
        protected void removeNotify() {
            setKeys(Collections.EMPTY_SET);
            projectDir.removeFileChangeListener(this);
            super.removeNotify();
        }
        
        /**
         * DOCUMENT ME!
         *
         * @param key DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         */
        protected Node[] createNodes(Object key) {
            Node n = null;
            
            if (key == KEY_SOURCE_DIR) {
                FileObject srcRoot = helper.resolveFileObject(
                        evaluator.getProperty(JbiProjectProperties.SRC_DIR)
                        );
                Project p = FileOwnerQuery.getOwner(srcRoot);
                SourceGroup[] sgs = ProjectUtils.getSources(p).getSourceGroups(
                        JavaProjectConstants.SOURCES_TYPE_JAVA
                        );
                
                for (int i = 0; i < sgs.length; i++) {
                    if (sgs[i].contains(srcRoot)) {
//                        n = PackageView.createPackageView(sgs[i]);
                        try {
                            DataObject dobj = DataObject.find(sgs[i].getRootFolder());
                            n = new RootNode(DataFolder.findFolder(sgs[i].getRootFolder()));
                        } catch (DataObjectNotFoundException ex) {
                        }
                        break;
                    }
                }
//            } else if (key == KEY_DOC_BASE) {
//                n = new DocBaseNode(getFolder(JbiProjectProperties.META_INF).getNodeDelegate());
                
                /*
                   } else if (key == KEY_EJBS) {
                       FileObject srcRoot = helper.resolveFileObject(evaluator.getProperty (JbiProjectProperties.SRC_DIR));
                       Project project = FileOwnerQuery.getOwner (srcRoot);
                       DDProvider provider = DDProvider.getDefault();
                       EjbJarImplementation jp = (EjbJarImplementation) project.getLookup().lookup(EjbJarImplementation.class);
                       org.netbeans.modules.j2ee.dd.api.ejb.EjbJar ejbJar = null;
                       try {
                           ejbJar = provider.getDDRoot(jp.getDeploymentDescriptor());
                       } catch (IOException ioe) {
                           ErrorManager.getDefault().notify(ioe);
                       }
                       ClassPathProvider cpp = (ClassPathProvider)
                           project.getLookup().lookup(ClassPathProvider.class);
                       assert cpp != null;
                       ClassPath classPath = cpp.findClassPath(srcRoot, ClassPath.SOURCE);
                       n = new EjbContainerNode(ejbJar, classPath);
                       //Node nws =  new WebServicesNode(ejbJar, classPath);
                       return n == null ? new Node[0] : new Node[] {n};
                   } else if (key == WEBSERVICES_DIR){
                       FileObject         srcRoot = helper.resolveFileObject(evaluator.getProperty (JbiProjectProperties.SRC_DIR));
                       WebServicesView webServicesView = WebServicesView.getWebServicesView(srcRoot);
                       if(webServicesView != null)
                       {
                       n = webServicesView.createWebServicesView(srcRoot);
                       }
                 */
            } else if (key == KEY_SETUP_DIR) {
                try {
                    DataObject sdo = DataObject.find(getSetupFolder());
                    n = null; // new ServerResourceNode(project); // sdo.getNodeDelegate());
                } catch (org.openide.loaders.DataObjectNotFoundException dnfe) {
                }
            } else if (key == KEY_SVC_COMP_NODE) {
                n = ServiceCompositionNode.createServiceCompositionNode((JbiProject) project);
                if (n != null) {
                    n.addNodeListener(new NodeAdapter() {

                        @Override
                        public void nodeDestroyed(NodeEvent ev) {
                            refreshKey(KEY_SVC_COMP_NODE);
                        }
                    });
                }
            }
            
            return (n == null) ? new Node[0] : new Node[] {n};
        }
        
        private DataFolder getFolder(String propName) {
            String propValue = evaluator.getProperty(propName);
            
            if (propValue != null) {
                FileObject fo = helper.resolveFileObject(propValue);
                
                if (fo != null) {
                    DataFolder df = DataFolder.findFolder(fo);
                    
                    return df;
                }
            }
            
            return null;
        }
        
        // file change events in the project directory
        public void fileAttributeChanged(org.openide.filesystems.FileAttributeEvent fe) {
        }
        
        /**
         * DOCUMENT ME!
         *
         * @param fe DOCUMENT ME!
         */
        public void fileChanged(org.openide.filesystems.FileEvent fe) {
        }
        
        /**
         * DOCUMENT ME!
         *
         * @param fe DOCUMENT ME!
         */
        public void fileDataCreated(org.openide.filesystems.FileEvent fe) {
        }
        
        /**
         * DOCUMENT ME!
         *
         * @param fe DOCUMENT ME!
         */
        public void fileDeleted(org.openide.filesystems.FileEvent fe) {
            // setup folder deleted
            createNodes();
        }
        
        /**
         * DOCUMENT ME!
         *
         * @param fe DOCUMENT ME!
         */
        public void fileFolderCreated(org.openide.filesystems.FileEvent fe) {
            // setup folder could be created
            createNodes();
        }
        
        /**
         * DOCUMENT ME!
         *
         * @param fe DOCUMENT ME!
         */
        public void fileRenamed(org.openide.filesystems.FileRenameEvent fe) {
            // setup folder could be renamed
            createNodes();
        }
    }
    
    private static class DocBaseChildren extends FilterNode.Children {
        DocBaseChildren(Node orig) {
            super(orig);
        }
        
        protected Node[] createNodes(org.openide.nodes.Node key) {
            if (key instanceof FilterNode) {
                String name = ((FilterNode) key).getDisplayName();
                if ((name != null) && (name.equalsIgnoreCase("CVS"))) { // NOI18N
                    return new Node[] {};
                }
            }
            return super.createNodes(key);
        }
    }
    
    private static final DataFilter VISIBILITY_QUERY_FILTER = new VisibilityQueryDataFilter();
    
    private static final class RootNode extends FilterNode {
        public RootNode(DataFolder folder) {
            super(folder.getNodeDelegate(), folder.createNodeChildren(VISIBILITY_QUERY_FILTER));

            disableDelegation(DELEGATE_GET_DISPLAY_NAME|
                    DELEGATE_SET_DISPLAY_NAME|DELEGATE_GET_SHORT_DESCRIPTION|
                    DELEGATE_GET_ACTIONS);
            setDisplayName(
                    NbBundle.getMessage(RootNode.class, "LBL_ProcessFiles")); // NOI18N
            
        }
        
        //@Override
        public Action[] getActions(boolean context) {
            return new Action[] {
                CommonProjectActions.newFileAction(),
                null,
                org.openide.util.actions.SystemAction.get( org.openide.actions.FileSystemAction.class ),
                null,
                org.openide.util.actions.SystemAction.get( org.openide.actions.FindAction.class ),
                null,
                org.openide.util.actions.SystemAction.get( org.openide.actions.PasteAction.class ),
                null,
                org.openide.util.actions.SystemAction.get( org.openide.actions.ToolsAction.class ),
            };
        }
        
        //@Override
        public boolean canRename() {
            return false;
        }
        
        //@Override
        public boolean canCopy() {
            return false;
        }
        
        //@Override
        public boolean canCut() {
            return false;
        }
        
        //@Override
        public boolean canDestroy() {
            return false;
        }
        
    }
    

    
    static final class VisibilityQueryDataFilter implements ChangeListener, ChangeableDataFilter {
        
        EventListenerList ell = new EventListenerList();        
        
        public VisibilityQueryDataFilter() {
            VisibilityQuery.getDefault().addChangeListener(this);
        }
                
        public boolean acceptDataObject(DataObject obj) {                
            FileObject fileObject = obj.getPrimaryFile();              
            return VisibilityQuery.getDefault().isVisible(fileObject);
        }
        
        public void stateChanged( ChangeEvent e) {            
            Object[] listeners = ell.getListenerList();     
            ChangeEvent event = null;
            for (int i = listeners.length-2; i>=0; i-=2) {
                if (listeners[i] == ChangeListener.class) {             
                    if ( event == null) {
                        event = new ChangeEvent(this);
                    }
                    ((ChangeListener)listeners[i+1]).stateChanged(event);
                }
            }
        }        
    
        public void addChangeListener( ChangeListener listener ) {
            ell.add(ChangeListener.class, listener);
        }        
                        
        public void removeChangeListener( ChangeListener listener ) {
            ell.remove(ChangeListener.class, listener);
        }
        
    }
    
    
    
    
    
//    private static final class DocBaseNode extends FilterNode {
//        private static Image CONFIGURATION_FILES_BADGE = Utilities.loadImage(
//                "org/netbeans/modules/compapp/projects/jbi/ui/resources/configbadge.gif", true ); // NOI18N
//
//        /**
//         * Creates a new DocBaseNode object.
//         *
//         * @param orig DOCUMENT ME!
//         */
//        DocBaseNode(Node orig) {
//            super(orig, new DocBaseChildren(orig));
//        }
//
//        /**
//         * DOCUMENT ME!
//         *
//         * @param type DOCUMENT ME!
//         *
//         * @return DOCUMENT ME!
//         */
//        public Image getIcon(int type) {
//            return computeIcon(false, type);
//        }
//
//        /**
//         * DOCUMENT ME!
//         *
//         * @param type DOCUMENT ME!
//         *
//         * @return DOCUMENT ME!
//         */
//        public Image getOpenedIcon(int type) {
//            return computeIcon(true, type);
//        }
//
//        private Image computeIcon(boolean opened, int type) {
//            Node folderNode = getOriginal();
//            Image image = opened ? folderNode.getOpenedIcon(type) : folderNode.getIcon(type);
//
//            return Utilities.mergeImages(image, CONFIGURATION_FILES_BADGE, 7, 7);
//        }
//
//        /**
//         * DOCUMENT ME!
//         *
//         * @return DOCUMENT ME!
//         */
//        public String getDisplayName() {
//            return NbBundle.getMessage(JbiViews.class, "LBL_Node_DocBase"); // NOI18N
//        }
//    }
}
