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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.compapp.projects.base.ui;

import java.awt.Image;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.netbeans.api.project.*;
//import org.netbeans.modules.j2ee.common.ui.ServerResourceNode;
//import org.netbeans.modules.j2ee.dd.api.ejb.DDProvider;

import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

//import org.netbeans.spi.java.project.support.ui.PackageView;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;

import org.netbeans.modules.compapp.projects.base.IcanproProject;
import org.netbeans.modules.compapp.projects.base.ui.customizer.IcanproProjectProperties;
// import org.netbeans.modules.websvc.api.webservices.WebServicesView;
import org.openide.filesystems.FileChangeListener;

class IcanproViews {

    private IcanproViews() {
    }

    static final class LogicalViewChildren extends Children.Keys implements FileChangeListener {

        private static final String KEY_SOURCE_DIR = "srcDir"; // NOI18N
        private static final String KEY_DOC_BASE = "docBase"; //NOI18N
        private static final String KEY_EJBS = "ejbKey"; //NOI18N
        private static final String WEBSERVICES_DIR = "webservicesDir"; // NOI18N 
        private static final String KEY_SETUP_DIR = "setupDir"; //NOI18N

        private AntProjectHelper helper;
        private final PropertyEvaluator evaluator;
        private FileObject projectDir;
        private Project project;

        public LogicalViewChildren (AntProjectHelper helper, PropertyEvaluator evaluator, Project project) {
            assert helper != null;
            this.helper = helper;
            projectDir = helper.getProjectDirectory();
            this.evaluator = evaluator;
            this.project = project;
        }

        protected void addNotify() {
            super.addNotify();
            projectDir.addFileChangeListener(this);
            createNodes();
        }

        private void createNodes() {
            List l = new ArrayList();
            /*
            l.add(KEY_EJBS);
            */

            DataFolder docBaseDir = getFolder(IcanproProjectProperties.META_INF);
            if (docBaseDir != null) {
                /*
                l.add(KEY_DOC_BASE);
                 */
            }

            DataFolder srcDir = getFolder(IcanproProjectProperties.SRC_DIR);
            if (srcDir != null) {
                l.add(KEY_SOURCE_DIR);
            }

            FileObject setupFolder = getSetupFolder();
            if (setupFolder != null && setupFolder.isFolder()) {
                l.add(KEY_SETUP_DIR);
            }
/*
            l.add(WEBSERVICES_DIR);
*/
            setKeys(l);
        }

        private FileObject getSetupFolder() {
            return projectDir.getFileObject("setup"); //NOI18N
        }

        protected void removeNotify() {
            setKeys(Collections.EMPTY_SET);
            projectDir.removeFileChangeListener(this);
            super.removeNotify();
        }

        protected Node[] createNodes(Object key) {
            Node n = null;
            if (key == KEY_SOURCE_DIR) {
                FileObject srcRoot = helper.resolveFileObject(evaluator.getProperty (IcanproProjectProperties.SRC_DIR));
                Project p = FileOwnerQuery.getOwner (srcRoot);
                Sources s = ProjectUtils.getSources(p);
                SourceGroup sgs [] = ProjectUtils.getSources (p).getSourceGroups (IcanproProject.SOURCES_TYPE_ICANPRO);
                for (int i = 0; i < sgs.length; i++) {
                    if (sgs [i].contains (srcRoot)) {
                        n = PackageView.createPackageView (sgs [i]);
                        break;
                    }
                }
            /*
            } else if (key == KEY_DOC_BASE) {
                n = new DocBaseNode (getFolder(IcanproProjectProperties.META_INF).getNodeDelegate());
            } else if (key == KEY_EJBS) {
                FileObject srcRoot = helper.resolveFileObject(evaluator.getProperty (IcanproProjectProperties.SRC_DIR));
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
		FileObject 	srcRoot = helper.resolveFileObject(evaluator.getProperty (IcanproProjectProperties.SRC_DIR));
                WebServicesView webServicesView = WebServicesView.getWebServicesView(srcRoot);
                if(webServicesView != null)
		{
		n = webServicesView.createWebServicesView(srcRoot);
                }
	    } else if (key == KEY_SETUP_DIR) {
                try {
                    DataObject sdo = DataObject.find(getSetupFolder());
                    n = new ServerResourceNode(project); // sdo.getNodeDelegate());
                } catch (org.openide.loaders.DataObjectNotFoundException dnfe) {}
                */
            }

            return n == null ? new Node[0] : new Node[] {n};
        }

        private DataFolder getFolder(String propName) {
            FileObject fo = helper.resolveFileObject(evaluator.getProperty (propName));
            if (fo != null) {
                DataFolder df = DataFolder.findFolder(fo);
                return df;
            }
            return null;
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
           createNodes();
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

    private static final class DocBaseNode extends FilterNode {
        private static Image CONFIGURATION_FILES_BADGE = Utilities.loadImage( "org/netbeans/modules/compapp/projects/base/ui/resources/docjar.gif", true ); // NOI18N

        DocBaseNode (Node orig) {
            super (orig);
        }

        public Image getIcon( int type ) {
            return computeIcon( false, type );
        }

        public Image getOpenedIcon( int type ) {
            return computeIcon( true, type );
        }

        private Image computeIcon( boolean opened, int type ) {
            Node folderNode = getOriginal();
            Image image = opened ? folderNode.getOpenedIcon( type ) : folderNode.getIcon( type );
            return Utilities.mergeImages( image, CONFIGURATION_FILES_BADGE, 7, 7 );
        }

        public String getDisplayName () {
            return NbBundle.getMessage(IcanproViews.class, "LBL_Node_DocBase"); //NOI18N
        }
    }
}
