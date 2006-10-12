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

package org.netbeans.modules.j2ee.earproject.ui;

import java.awt.Image;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.Action;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;
import org.netbeans.api.project.Project;
import org.netbeans.api.queries.VisibilityQuery;
import org.netbeans.modules.j2ee.earproject.ui.customizer.EarProjectProperties;
import org.netbeans.modules.j2ee.spi.ejbjar.support.J2eeProjectView;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.actions.FindAction;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.ChangeableDataFilter;
import org.openide.loaders.DataFilter;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;
import org.openide.util.actions.SystemAction;

class ArchiveViews {
        
    private ArchiveViews() {
    }
    
    static final class LogicalViewChildren extends Children.Keys/*<FileObject>*/  implements FileChangeListener {
        
        // XXX does not react correctly to addition or removal of src/ subdir

        private static final String KEY_DOC_BASE = "docBase"; //NOI18N
        private static final String KEY_SETUP_DIR = "setupDir"; //NOI18N
        
        private final Project project;
        private final AntProjectHelper helper;
        private final PropertyEvaluator evaluator;
        private final FileObject projectDir;

        public LogicalViewChildren (Project project, AntProjectHelper helper, PropertyEvaluator evaluator) {
            assert project != null;
            this.project = project;
            assert helper != null;
            this.helper = helper;
            projectDir = helper.getProjectDirectory();
            this.evaluator = evaluator;
        }
        
        protected void addNotify() {
            super.addNotify();
            projectDir.addFileChangeListener(FileUtil.weakFileChangeListener(this, projectDir));
            createNodes();
        }
        
        private void createNodes() {
            List<String> keys = new ArrayList<String>();
           
            DataFolder docBaseDir = getFolder(EarProjectProperties.META_INF);
            if (docBaseDir != null) {
                keys.add(KEY_DOC_BASE);
            }
            keys.add(KEY_SETUP_DIR);
            
            setKeys(keys);
        }
        
        protected void removeNotify() {
            setKeys(Collections.EMPTY_SET);
            projectDir.removeFileChangeListener(this);
            super.removeNotify();
        }
        
        protected Node[] createNodes(Object key) {
            Node n = null;
            if (key == KEY_DOC_BASE) {
                n = new DocBaseNode (getFolder(EarProjectProperties.META_INF));
            } else if (key == KEY_SETUP_DIR) {
                n = J2eeProjectView.createServerResourcesNode(project);
            }
            return n == null ? new Node[0] : new Node[] {n};
        }
            
        private DataFolder getFolder(String propName) {
            String prop = evaluator.getProperty (propName);
            if (prop != null) {
                FileObject fo = helper.resolveFileObject(prop);
                if (fo != null && fo.isValid() && fo.isFolder()) {
                    DataFolder df = DataFolder.findFolder(fo);
                    return df;
                }
            }
            return null;
        }
        
        // file change events in the project directory
        public void fileAttributeChanged(FileAttributeEvent fe) {
        }
        
        public void fileChanged(FileEvent fe) {
        }
        
        public void fileDataCreated(FileEvent fe) {
        }
        
        public void fileDeleted(FileEvent fe) {
            // setup folder deleted
           createNodes();
        }
        
        public void fileFolderCreated(FileEvent fe) {
            // setup folder could be created
            createNodes();
        }
        
        public void fileRenamed(FileRenameEvent fe) {
            // setup folder could be renamed
            createNodes();
        }
    }
    
    private static final class VisibilityQueryDataFilter implements ChangeListener, ChangeableDataFilter {
        
        EventListenerList ell = new EventListenerList();        
        
        public VisibilityQueryDataFilter() {
            VisibilityQuery.getDefault().addChangeListener( this );
        }
                
        public boolean acceptDataObject(DataObject obj) {                
            FileObject fo = obj.getPrimaryFile();                
            return VisibilityQuery.getDefault().isVisible( fo );
        }
        
        public void stateChanged( ChangeEvent e) {            
            Object[] listeners = ell.getListenerList();     
            ChangeEvent event = null;
            for (int i = listeners.length-2; i>=0; i-=2) {
                if (listeners[i] == ChangeListener.class) {             
                    if ( event == null) {
                        event = new ChangeEvent( this );
                    }
                    ((ChangeListener)listeners[i+1]).stateChanged( event );
                }
            }
        }        
    
        public void addChangeListener( ChangeListener listener ) {
            ell.add( ChangeListener.class, listener );
        }        
                        
        public void removeChangeListener( ChangeListener listener ) {
            ell.remove( ChangeListener.class, listener );
        }
        
    }

    private static final class DocBaseNode extends FilterNode {

        private static final DataFilter VISIBILITY_QUERY_FILTER = new VisibilityQueryDataFilter();
        private static final Image CONFIGURATION_FILES_BADGE = Utilities.loadImage( "org/netbeans/modules/j2ee/earproject/ui/resources/archive.gif", true ); // NOI18N
        
        public DocBaseNode(DataFolder folder) {
            super(folder.getNodeDelegate(), folder.createNodeChildren(VISIBILITY_QUERY_FILTER));
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
        
        public boolean canCopy() {
            return false;
        }
        
        public boolean canCut() {
            return false;
        }
        
        public boolean canRename() {
            return false;
        }
        
        public boolean canDestroy() {
            return false;
        }
        
        public Action[] getActions( boolean context ) {
            return new Action[] {
//                CommonProjectActions.newFileAction(),
//                null,
//                SystemAction.get(FileSystemRefreshAction.class),
//                null,
                SystemAction.get(FindAction.class),
//                null,
//                SystemAction.get(PasteAction.class),
//                null,
//                SystemAction.get(ToolsAction.class),
            };
        }
        
        public String getDisplayName() {
            return NbBundle.getMessage(ArchiveViews.class, "LBL_Node_DocBase"); //NOI18N
        }
    }
}
