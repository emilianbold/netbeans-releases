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

package org.netbeans.modules.favorites;

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.netbeans.api.queries.VisibilityQuery;
import org.openide.actions.CopyAction;
import org.openide.actions.CutAction;
import org.openide.actions.DeleteAction;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataShadow;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author  Jaroslav Tulach
 */
final class Favorites extends FilterNode {
    /** default node */
    private static Node node;
    /** node that represents root of filesystems */
    private static Node root;

    /** Creates new ProjectRootFilterNode. */
    private Favorites(Node node) {
        super(node, new Chldrn (node, false));
    }
    
    public Image getIcon (int type) {
        return Utilities.loadImage("org/netbeans/modules/favorites/resources/actionSelect.png"); // NOI18N
    }
    
    public Image getOpenedIcon (int type) {
        return Utilities.loadImage("org/netbeans/modules/favorites/resources/actionSelect.png"); // NOI18N
    }
    
    public boolean canCopy () {
        return false;
    }
    
    public boolean canCut () {
        return false;
    }
    
    public boolean canRename () {
        return false;
    }
    
    public static DataFolder getFolder () {
        try {
            FileObject fo = FileUtil.createFolder (
                Repository.getDefault().getDefaultFileSystem().getRoot(), 
                "Favorites" // NOI18N
            );
            DataFolder folder = DataFolder.findFolder(fo);
            return folder;
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            return DataFolder.findFolder (
                Repository.getDefault().getDefaultFileSystem().getRoot()
            );
        }
        
    }
    
    /** Getter for default filter node.
     */
    public static synchronized Node getNode () {
        if (node == null) {
            node = new Favorites (getFolder().getNodeDelegate ());
        }
        return node;
    }
    
    /** Get name of home directory. Used from layer.
     */
    public static URL getHome () 
    throws FileStateInvalidException, MalformedURLException {
        String s = System.getProperty("user.home"); // NOI18N
        
        File home = new File (s);
        home = FileUtil.normalizeFile (home);
        
        return home.toURI ().toURL ();
    }

    /** Finds file for a given node 
     */
    static File fileForNode (Node n) {
        DataObject obj = (DataObject)n.getCookie (DataObject.class);
        if (obj == null) return null;
        
        return FileUtil.toFile (
            obj.getPrimaryFile()
        );
    }

    public Handle getHandle () {
        return new RootHandle ();
    }
    
    public Action[] getActions(boolean context) {
        return new Action[] {Actions.addOnFavoritesNode()};
    }
    
    private static class RootHandle implements Node.Handle {
        static final long serialVersionUID = 1907300072945111595L;

        /** Return a node for the current project.
        */
        public Node getNode () {
            return Favorites.getNode ();
        }
    }

    private static class Chldrn extends FilterNode.Children 
    implements ChangeListener, Runnable {
        private ChangeListener weak;
        private boolean hideHidden;
        /** Creates new Chldrn. */
        public Chldrn (Node node, boolean hideHidden) {
            super (node);
            this.hideHidden = hideHidden;
            
            weak = org.openide.util.WeakListeners.change(this, VisibilityQuery.getDefault());
            VisibilityQuery.getDefault().addChangeListener(weak);
        }
        
        protected Node[] createNodes(Node node) {
            if (hideHidden) {
                DataObject obj = (DataObject)node.getCookie(DataObject.class);
                if (obj != null && !VisibilityQuery.getDefault().isVisible(obj.getPrimaryFile())) {
                    return null;
                }
            }
            
            return new Node[] { new ProjectFilterNode (
                node,
                (node.isLeaf ()) ? org.openide.nodes.Children.LEAF : new Chldrn (node, true)
            )};
        }

        public void stateChanged(ChangeEvent e) {
            MUTEX.postWriteRequest(this);
        }
        
        public void run() {
            Node[] arr = original.getChildren().getNodes();
            for (int i = 0; i < arr.length; i++) {
                refreshKey(arr[i]);
            }
        }
    } // end of Chldrn

    /** This FilterNode is sensitive to 'Delete Original Files' property of {@link ProjectOption}.
     * When this property is true then original DataObjects pointed to by links under the project's node
     * are deleted as the Delete is performed on the link's node.
     */
    private static class ProjectFilterNode extends FilterNode {

        /** Creates new ProjectFilterNode. */
        public ProjectFilterNode (Node node, org.openide.nodes.Children children) {
            super (node, children);
        }
        
        public String getDisplayName () {
            //Change display name only for favorite nodes (links) under Favorites node.
            if (Favorites.getNode().equals(this.getParentNode())) {
                DataShadow ds = (DataShadow) getCookie(DataShadow.class);
                if (ds != null) {
                    String name = ds.getName();
                    String path = FileUtil.getFileDisplayName(ds.getOriginal().getPrimaryFile());
                    return NbBundle.getMessage(Favorites.class, "CTL_DisplayNameTemplate", name, path);
                } else {
                    return super.getDisplayName();
                }
            } else {
                return super.getDisplayName();
            }
        }
        
        // Must be overridden since getDisplayName is.
        public String getHtmlDisplayName() {
            return getOriginal().getHtmlDisplayName();
        }
        
        public boolean canDestroy () {
            boolean canDestroy = super.canDestroy ();
            DataShadow link = (DataShadow) getCookie (DataShadow.class);

            // if the DO of this node can be destroyed and the original DO should be destroyed too
            // ask the original if it's allowed to delete it
            if (canDestroy && isDeleteOriginal (link)) {
                canDestroy = link.getOriginal ().isDeleteAllowed ();
            }

            return canDestroy;
        }

        public void destroy () throws IOException {
            if (canDestroy ()) {
                DataShadow link = (DataShadow) getCookie (DataShadow.class);
                DataObject original = isDeleteOriginal (link) ? link.getOriginal () : null;

                super.destroy ();

                if (original != null) {
                    original.delete ();
                }
            }
        }

        private boolean isDeleteOriginal (DataShadow link) {
            return false;
        }
        
        public Action[] getActions(boolean context) {
            Action[] arr;
            arr = super.getActions(context);
            
            //Find if given node is root
            boolean isRoot = false;
            DataObject dataObject = (DataObject) getCookie(DataObject.class);
            if (dataObject != null) {
                FileObject fo = dataObject.getPrimaryFile();
                if (fo != null) {
                    //Check if it is root.
                    File file = FileUtil.toFile(fo);
                    if (file != null) {
                        if (file.getParent() == null) {
                            isRoot = true;
                        }
                    }
                }
            }
            
            if (isRoot) {
                return createActionsForRoot(arr);
            } else {
                if (Favorites.getNode().equals(this.getParentNode())) {
                    DataShadow ds = (DataShadow) getCookie(DataShadow.class);
                    if (ds != null) {
                        if (ds.getOriginal().getPrimaryFile().isFolder()) {
                            return createActionsForFavoriteFolder(arr);
                        } else {
                            return createActionsForFavoriteFile(arr);
                        }
                    }
                } else {
                    DataObject dObj = (DataObject) getCookie(DataObject.class);
                    if (dObj != null) {
                        if (dObj.getPrimaryFile().isFolder()) {
                            return createActionsForFolder(arr);
                        } else {
                            return createActionsForFile(arr);
                        }
                    }
                }
            }
            //Unknown node - return unmodified actions.
            return arr;
        }
        
        /** Do not change original actions. */
        private Action [] createActionsForRoot (Action [] arr) {
            //Actions are not modified.
            return arr;
        }
        
        /** Add action 'Remove from Favorites'. */
        private Action [] createActionsForFavoriteFolder (Action [] arr) {
            boolean added = false;
            List<Action> newArr = new ArrayList<Action>();
            for (int i = 0; i < arr.length; i++) {
                //Add before CopyAction or CutAction
                if (!added && ((arr[i] instanceof CopyAction) || (arr[i] instanceof CutAction))) {
                    added = true;
                    newArr.add(Actions.remove());
                    newArr.add(null);
                }
                //Do not add Delete action
                if (!(arr[i] instanceof DeleteAction)) {
                    newArr.add(arr[i]);
                }
            }
            if (!added) {
                added = true;
                newArr.add(null);
                newArr.add(Actions.remove());
            }
            
            return newArr.toArray (new Action[newArr.size()]);
        }
        
        /** Add action 'Remove from Favorites'. */
        private Action [] createActionsForFavoriteFile (Action [] arr) {
            boolean added = false;
            List<Action> newArr = new ArrayList<Action>();
            for (int i = 0; i < arr.length; i++) {
                //Add before CopyAction or CutAction
                if (!added && ((arr[i] instanceof CopyAction) || (arr[i] instanceof CutAction))) {
                    added = true;
                    newArr.add(Actions.remove());
                    newArr.add(null);
                }
                //Do not add Delete action
                if (!(arr[i] instanceof DeleteAction)) {
                    newArr.add(arr[i]);
                }
            }
            if (!added) {
                added = true;
                newArr.add(null);
                newArr.add(Actions.remove());
            }
            return newArr.toArray (new Action[newArr.size()]);
        }
        
        /** Add action 'Add to Favorites'. */
        private Action [] createActionsForFolder (Action [] arr) {
            boolean added = false;
            List<Action> newArr = new ArrayList<Action>();
            for (int i = 0; i < arr.length; i++) {
                //Add before CopyAction or CutAction
                if (!added && ((arr[i] instanceof CopyAction) || (arr[i] instanceof CutAction))) {
                    added = true;
                    newArr.add(Actions.add());
                    newArr.add(null);
                }
                newArr.add(arr[i]);
            }
            if (!added) {
                added = true;
                newArr.add(null);
                newArr.add(Actions.add());
            }
            return newArr.toArray (new Action[newArr.size()]);
        }
        
        /** Add action 'Add to Favorites'. */
        private Action [] createActionsForFile (Action [] arr) {
            boolean added = false;
            List<Action> newArr = new ArrayList<Action>();
            for (int i = 0; i < arr.length; i++) {
                //Add before CopyAction or CutAction
                if (!added && ((arr[i] instanceof CopyAction) || (arr[i] instanceof CutAction))) {
                    added = true;
                    newArr.add(Actions.add());
                    newArr.add(null);
                }
                newArr.add(arr[i]);
            }
            if (!added) {
                added = true;
                newArr.add(null);
                newArr.add(Actions.add());
            }
            return newArr.toArray (new Action[newArr.size()]);
        }
        
    }
}
