/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License. When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP. Sun designates this
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
package org.netbeans.modules.bpel.project.ui;

import java.awt.Image;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.Action;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.spi.project.ui.support.CommonProjectActions;

import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.ImageUtilities;

import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;

import org.netbeans.modules.compapp.projects.base.ui.customizer.IcanproProjectProperties;
import org.openide.filesystems.FileChangeListener;
import org.openide.loaders.DataObject;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;
import org.netbeans.api.queries.VisibilityQuery;
import org.openide.loaders.ChangeableDataFilter;
import org.openide.loaders.DataFilter;
import org.netbeans.modules.bpel.project.ProjectConstants;

class ProjectViews {
    private static Logger logger = Logger.getLogger(ProjectViews.class.getName());
    
    private static final DataFilter NO_FOLDERS_FILTER = new NoFoldersDataFilter();
    
    private ProjectViews() {}

    static final class LogicalViewChildren extends Children.Keys implements FileChangeListener {

        private static final String SOURCE_NODE = "source_node"; // NOI18N

        private AntProjectHelper helper;
        private final PropertyEvaluator evaluator;
        private FileObject projectDir;
        private Project project;

        public LogicalViewChildren(AntProjectHelper helper, PropertyEvaluator evaluator, Project project) {
            assert helper != null;
            this.helper = helper;
            projectDir = helper.getProjectDirectory();
            this.evaluator = evaluator;
            this.project = project;
        }

        @Override
        protected void addNotify() {
            super.addNotify();
            projectDir.addFileChangeListener(this);
            createNodes();
        }

        @SuppressWarnings("unchecked")
        private void createNodes() {
            List l = new ArrayList();
            DataFolder srcDir = getFolder(IcanproProjectProperties.SRC_DIR);

            if (srcDir != null) {
                l.add(SOURCE_NODE);
            }
            if (l.size() > 0) {
                setKeys(l);
            }
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void removeNotify() {
            setKeys(Collections.EMPTY_SET);
            projectDir.removeFileChangeListener(this);
            super.removeNotify();
        }

        protected Node[] createNodes(Object key) {
          Node node = null;
          
          if (key == SOURCE_NODE) {
            FileObject srcRoot = helper.resolveFileObject(evaluator.getProperty(IcanproProjectProperties.SRC_DIR));
            Project p = FileOwnerQuery.getOwner(srcRoot);
            Sources s = ProjectUtils.getSources(p);
            SourceGroup sgs [] = ProjectUtils.getSources(p).getSourceGroups(ProjectConstants.SOURCES_TYPE_PROJECT);
    
            for (int i = 0; i < sgs.length; i++) {
              if (sgs [i].contains(srcRoot)) {
                try {
                    FileObject folder = sgs[i].getRootFolder();
                    DataObject dobj = DataObject.find(folder);
                    node = new RootNode(dobj.getNodeDelegate(), (DataFolder) dobj);
                } 
                catch (DataObjectNotFoundException ex) {
                }
                break;
              }
            }
          }
          return node == null ? new Node [0] : new Node [] { node };
        }

        private DataFolder getFolder(String propName) {
            String propertyValue = evaluator.getProperty (propName);
            if (propertyValue != null ) {
                FileObject fo = helper.resolveFileObject(evaluator.getProperty (propName));

                if (fo != null && fo.isValid()) {
                    try {
                        DataFolder df = DataFolder.findFolder(fo);
                        return df;
                    }
                    catch (Exception ex) {
                        logger.fine(ex.getMessage());
                    }
                }
            }
            return null;
        }

        public void fileAttributeChanged(org.openide.filesystems.FileAttributeEvent fe) {}

        public void fileChanged(org.openide.filesystems.FileEvent fe) {}

        public void fileDataCreated(org.openide.filesystems.FileEvent fe) {}

        public void fileDeleted(org.openide.filesystems.FileEvent fe) {}

        public void fileFolderCreated(org.openide.filesystems.FileEvent fe) {
            createNodes();
        }

        public void fileRenamed(org.openide.filesystems.FileRenameEvent fe) {
            createNodes();
        }
    }

    // ----------------------------------------------------- 
    private static final class RootNode extends FilterNode {

      public RootNode(Node n, DataFolder dataFolder) {
          super(n, dataFolder.createNodeChildren(NO_FOLDERS_FILTER));
          disableDelegation(DELEGATE_GET_DISPLAY_NAME | DELEGATE_SET_DISPLAY_NAME | DELEGATE_GET_SHORT_DESCRIPTION | DELEGATE_GET_ACTIONS);
          setDisplayName(NbBundle.getMessage(RootNode.class, "LBL_ProcessFiles")); // NOI18N
      }
      
      @Override
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

        @Override
      public boolean canDestroy() {
        return false;
      }

      @Override
      public boolean canRename() {
          return false;
      }
      
      @Override
      public boolean canCopy() {
          return false;
      }
      
      @Override
      public boolean canCut() {
          return false;
      }
    }
 
    private static final class DocBaseNode extends FilterNode { 
        private static Image CONFIGURATION_FILES_BADGE = ImageUtilities.loadImage( "org/netbeans/modules/bpel/project/ui/resources/docjar.gif", true ); // NOI18N

        DocBaseNode (Node orig) {
            super (orig);
        }

        @Override
        public Image getIcon( int type ) {
            return computeIcon( false, type );
        }

        @Override
        public Image getOpenedIcon( int type ) {
            return computeIcon( true, type );
        }

        private Image computeIcon( boolean opened, int type ) {
            Node folderNode = getOriginal();
            Image image = opened ? folderNode.getOpenedIcon( type ) : folderNode.getIcon( type );
            return ImageUtilities.mergeImages( image, CONFIGURATION_FILES_BADGE, 7, 7 );
        }

        @Override
        public String getDisplayName () {
            return NbBundle.getMessage(ProjectViews.class, "LBL_Node_DocBase"); //NOI18N
        }
    }
    
    static final class NoFoldersDataFilter implements ChangeListener, ChangeableDataFilter {
        
        EventListenerList ell = new EventListenerList();        
        
        public NoFoldersDataFilter() {
            VisibilityQuery.getDefault().addChangeListener( this );
        }
                
        public boolean acceptDataObject(DataObject obj) {                
            FileObject fo = obj.getPrimaryFile();                
            return  VisibilityQuery.getDefault().isVisible( fo );
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
}
