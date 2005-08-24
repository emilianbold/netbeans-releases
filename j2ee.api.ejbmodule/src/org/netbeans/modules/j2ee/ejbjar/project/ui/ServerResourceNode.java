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

package org.netbeans.modules.j2ee.ejbjar.project.ui;

import java.awt.Image;
import org.openide.ErrorManager;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
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
 * @author Chris Webster
 * @author Andrei Badea
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
    
    public Image getIcon( int type ) {
        return computeIcon( false, type );
    }
    
    public Image getOpenedIcon( int type ) {
        return computeIcon( true, type );
    }
    
    private Image computeIcon( boolean opened, int type ) {
        // AB: always use the generic icon of a folder
        // because this node is displayed even if the setup directory 
        // doesn't exist (in which case there's no node to get the icon from)
        Image image = getDefaultFolderImage(opened, type);
        if (image == null) {
            Node folderNode = getOriginal();
            image = opened ? getOriginal().getOpenedIcon( type ) : getOriginal().getIcon( type );
        }
        return Utilities.mergeImages( image, RESOURCE_FILE_BADGE, 7, 7 );
    }
    
    /**
     * Tries to get a folder icon. We can't get it from then node of the
     * setup directory, because the setup directory may be inexistent
     */
    public Image getDefaultFolderImage(boolean opened, int type) {
        FileObject projectDirFo = project.getProjectDirectory();
        if (projectDirFo != null) {
            try {
                DataObject projectDirDo = DataObject.find(projectDirFo);
                return opened ? projectDirDo.getNodeDelegate().getOpenedIcon(type) : projectDirDo.getNodeDelegate().getIcon(type);
            } catch (DataObjectNotFoundException donfe) {}
        }
        return null;
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
        DataFolder folderDo = getSetupDataFolder(project);
        if (LOG) {
            LOGGER.log(ErrorManager.INFORMATIONAL, "The DataFolder is: " + folderDo); // NOI18N
        }
        changeOriginal(getDataFolderNode(folderDo, project), false);
        org.openide.nodes.Children children = getDataFolderNodeChildren(folderDo);
        setChildren(children);
        if (LOG) {
            LOGGER.log(ErrorManager.INFORMATIONAL, "Children count: " + children.getNodes(true).length); // NOI18N
        }
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
        return (folderDo != null) ? folderDo.getNodeDelegate() : new AbstractNode(Children.LEAF, Lookups.singleton(project));
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
}

