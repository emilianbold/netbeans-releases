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

package org.netbeans.modules.j2ee.ejbjar.project.ui;

import java.awt.Image;
import org.openide.ErrorManager;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.Repository;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

import javax.swing.Action;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;
import org.netbeans.api.project.Project;
import org.netbeans.api.queries.VisibilityQuery;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.openide.actions.FindAction;
import org.openide.actions.PasteAction;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.ChangeableDataFilter;
import org.openide.loaders.DataFilter;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;

/**
 * Node to represent the setup folder described in the blueprints.
 *
 * @author Chris Webster, Andrei Badea
 */
public class ServerResourceNode extends FilterNode {
    
    private static final ErrorManager LOGGER = ErrorManager.getDefault().getInstance(ServerResourceNode.class.getName());
    private static final boolean LOG = LOGGER.isLoggable(ErrorManager.INFORMATIONAL);
    
    private static final Image RESOURCE_FILE_BADGE = Utilities.loadImage( "org/netbeans/modules/j2ee/ejbjar/project/ui/resourcesBadge.gif", true ); // NOI18N
    private static final String SETUP_DIR = "setup"; // NOI18N
    private static final DataFilter VISIBILITY_QUERY_FILTER = new VisibilityQueryDataFilter();
    
    private Project project;
    private FileChangeListener projectDirectoryListener;
    
    /** Creates a new instance of ServerResourceNode */
    public ServerResourceNode(Project project) {
        this(getSetupDataFolder(project), project);
    }
    
    private ServerResourceNode(DataFolder folderDo, Project project) {
        super(getDataFolderNode(folderDo, project), getDataFolderNodeChildren(folderDo));
        projectDirectoryListener = new ProjectDirectoryListener();
        if (LOG) {
            LOGGER.log(ErrorManager.INFORMATIONAL, "Adding file listener to " + project.getProjectDirectory()); // NOI18N
        }
        project.getProjectDirectory().addFileChangeListener(FileUtil.weakFileChangeListener(projectDirectoryListener, project.getProjectDirectory()));
        this.project = project;
    }
    
    public Image getIcon(int type) {
        return badgeIcon(super.getIcon(type));
    }
    
    public Image getOpenedIcon( int type ) {
        return badgeIcon(super.getOpenedIcon(type));
    }
    
    private static Image badgeIcon(Image icon) {
        return Utilities.mergeImages(icon, RESOURCE_FILE_BADGE, 7, 7);
    }
    
    public String getDisplayName() {
        return NbBundle.getMessage(ServerResourceNode.class, "LBL_Node_ResourceNode");
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
            CommonProjectActions.newFileAction(),
            null,
            SystemAction.get(FindAction.class),
            null,
            SystemAction.get(PasteAction.class),
        };
    }
        
    private void refresh() {
        if (LOG) {
            LOGGER.log(ErrorManager.INFORMATIONAL, "Refreshing"); // NOI18N
        }
        final DataFolder folderDo = getSetupDataFolder(project);
        if (LOG) {
            LOGGER.log(ErrorManager.INFORMATIONAL, "The DataFolder is: " + folderDo); // NOI18N
        }
        // #64665: should not call FilterNode.changeOriginal() or Node.setChildren() 
        // under Children.MUTEX read access
        Children.MUTEX.postWriteRequest(new Runnable() {
            public void run() {
                changeOriginal(getDataFolderNode(folderDo, project), false);
                org.openide.nodes.Children children = getDataFolderNodeChildren(folderDo);
                setChildren(children);
                if (LOG) {
                    LOGGER.log(ErrorManager.INFORMATIONAL, "Children count: " + children.getNodes(true).length); // NOI18N
                }
            }
        });

    }
    
    private static DataFolder getSetupDataFolder(Project project) {
        FileObject folderFo = project.getProjectDirectory().getFileObject(SETUP_DIR);
        DataFolder folderDo = null;
        if (folderFo != null && folderFo.isFolder()) {
            try {
                folderDo = DataFolder.findFolder(folderFo);
            }
            catch (IllegalArgumentException e) {}
        }
        return folderDo;
    }
    
    private static Node getDataFolderNode(DataFolder folderDo, Project project) {
        // The project in the placeholder node lookup is needed for the New File action.
        return (folderDo != null) ? folderDo.getNodeDelegate() : new PlaceHolderNode(Lookups.singleton(project));
    }
    
    private static org.openide.nodes.Children getDataFolderNodeChildren(DataFolder folderDo) {
        return (folderDo != null) ? folderDo.createNodeChildren(VISIBILITY_QUERY_FILTER) : Children.LEAF;
    }
    
    final private class ProjectDirectoryListener extends FileChangeAdapter {
        
        public void fileDeleted(FileEvent fe) {
            if (isWatchedFile(getFileName(fe)))
                refresh();
        }

        public void fileFolderCreated(FileEvent fe) {
            if (isWatchedFile(getFileName(fe))) {
                refresh();
            }
        }

        public void fileRenamed(org.openide.filesystems.FileRenameEvent fe) {
            if (isWatchedFile(getFileName(fe)) || isWatchedFile(getOldFileName(fe)))
                refresh();
        }

        private boolean isWatchedFile(String fileName) {
            return fileName.equals(SETUP_DIR);
        }

        private String getFileName(FileEvent fe) {
            return fe.getFile().getNameExt();
        }

        private String getOldFileName(FileRenameEvent fe) {
            String result = fe.getName();
            if (fe.getExt() != "") // NOI18N
                result = result + "." + fe.getExt(); // NOI18N

            return result;
        }
    }
    
    final private static class VisibilityQueryDataFilter implements ChangeListener, ChangeableDataFilter {
        
        private EventListenerList ell = new EventListenerList();
        
        public VisibilityQueryDataFilter() {
            VisibilityQuery.getDefault().addChangeListener(this);
        }
                
        public boolean acceptDataObject(DataObject obj) {
            FileObject fo = obj.getPrimaryFile();
            return VisibilityQuery.getDefault().isVisible(fo);
        }
        
        public void stateChanged( ChangeEvent e) {
            Object[] listeners = ell.getListenerList();
            ChangeEvent event = null;
            for (int i = listeners.length - 2; i >= 0;  i-= 2) {
                if (listeners[i] == ChangeListener.class) {             
                    if (event == null) {
                        event = new ChangeEvent( this );
                    }
                    ((ChangeListener)listeners[i + 1]).stateChanged(event);
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
    
    /**
     * A placeholder node for a folder node.
     */
    private static final class PlaceHolderNode extends AbstractNode {
        
        public PlaceHolderNode(Lookup lookup) {
            super(Children.LEAF, lookup);
        }

        public Image getIcon(int type) {
            Image image = null;
            Node imageDelegate = getImageDelegate();
            if (imageDelegate != null) {
                image = imageDelegate.getIcon(type);
            }
            if (image == null) {
                image = super.getIcon(type);
            }
            return image;
        }
        
        public Image getOpenedIcon(int type) {
            Image image = null;
            Node imageDelegate = getImageDelegate();
            if (imageDelegate != null) {
                image = imageDelegate.getOpenedIcon(type);
            }
            if (image == null) {
                image = super.getOpenedIcon(type);
            }
            return image;
        }
        
        private static Node getImageDelegate() {
            FileObject imageFo = Repository.getDefault().getDefaultFileSystem().getRoot();
            if (imageFo != null) {
                try {
                    DataObject imageDo = DataObject.find(imageFo);
                    return imageDo.getNodeDelegate();
                } catch (DataObjectNotFoundException donfe) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, donfe);
                }
            }
            return null;
        }
    }
}

