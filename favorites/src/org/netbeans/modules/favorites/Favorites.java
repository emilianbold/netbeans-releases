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

package org.netbeans.modules.favorites;

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;

import org.openide.actions.CopyAction;
import org.openide.actions.CutAction;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataShadow;
import org.openide.loaders.DataObject;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
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
        super(node, new Chldrn (node));
    }
    
    public Image getIcon (int type) {
        return Utilities.loadImage("org/netbeans/modules/favorites/resources/actionSelect.gif"); // NOI18N
    }
    
    public Image getOpenedIcon (int type) {
        return Utilities.loadImage("org/netbeans/modules/favorites/resources/actionSelect.gif"); // NOI18N
    }
    
    public boolean canCopy () {
        return false;
    }
    
    public boolean canCut () {
        return false;
    }
    
    public static org.openide.loaders.DataFolder getFolder () {
        try {
            org.openide.filesystems.FileObject fo = org.openide.filesystems.FileUtil.createFolder (
                org.openide.filesystems.Repository.getDefault().getDefaultFileSystem().getRoot(), 
                "Favorites" // NOI18N
            );
            org.openide.loaders.DataFolder folder = org.openide.loaders.DataFolder.findFolder(fo);
            return folder;
        } catch (java.io.IOException ex) {
            org.openide.ErrorManager.getDefault().notify (ex);
            return org.openide.loaders.DataFolder.findFolder (
                org.openide.filesystems.Repository.getDefault().getDefaultFileSystem().getRoot()
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
    
    /** Getter root node.
     */
    public static synchronized Node getRoot () {
        if (root == null) {
            root = org.openide.loaders.RepositoryNodeFactory.getDefault().repository (
                org.openide.loaders.DataFilter.ALL
            );
        }
        return root;
    }
    
    /** Get name of home directory. Used from layer.
     */
    public static java.net.URL getHome () 
    throws org.openide.filesystems.FileStateInvalidException, java.net.MalformedURLException {
        ensureShadowsWork (null);
        
        String s = System.getProperty("user.home"); // NOI18N
        
        File home = new File (s);
        home = FileUtil.normalizeFile (home);
        
        return home.toURI ().toURL ();
    }
    
    static void ensureShadowsWork (FileObject fo) throws org.openide.filesystems.FileStateInvalidException {
        /*
        if (fo == null) {
            File r = new File (System.getProperty("user.home"));
            fo = FileUtil.fromFile (r)[0];
        }
        
        // make sure the filesystem is in repository otherwise
        // the shadows will not work, workaround for issue 42690
        org.openide.filesystems.Repository.getDefault().addFileSystem(fo.getFileSystem());
         */
    }

    /** Finds file for a given node 
     */
    static File fileForNode (Node n) {
        DataObject obj = (DataObject)n.getCookie (DataObject.class);
        if (obj == null) return null;
        
        return org.openide.filesystems.FileUtil.toFile (
            obj.getPrimaryFile()
        );
    }

    public Handle getHandle () {
        return new RootHandle ();
    }
    
    public javax.swing.Action[] getActions(boolean context) {
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

    private static class Chldrn extends FilterNode.Children {
        /** Creates new Chldrn. */
        public Chldrn (Node node) {
            super (node);
        }
        
        protected Node[] createNodes(Object key) {
            InstanceCookie cookie = (InstanceCookie) ((Node) key).getCookie(InstanceCookie.class);
            Object o = null;
            if (cookie != null) {
                try {
                    o = cookie.instanceCreate();
                } catch (IOException exc) {
                } catch (ClassNotFoundException exc) {
                }
            }
            if (o instanceof Repository) {
                // list all roots
                File[] roots = File.listRoots();
                List list = new ArrayList ();
                for (int i = 0; i < roots.length; i++) {
                    FileObject r = org.openide.filesystems.FileUtil.toFileObject (roots[i]);
                    if (r == null) {
                        continue;
                    }
                    try {
                        DataObject obj = DataObject.find (r);
                        list.add (
                            new ProjectFilterNode (obj.getNodeDelegate(), new Chldrn (obj.getNodeDelegate ()))
                        );
                    } catch (org.openide.loaders.DataObjectNotFoundException ex) {
                        org.openide.ErrorManager.getDefault ().notify (ex);
                    }
                }
                if (Utilities.isWindows()) {
                    Node n = (Node) key;
                    Node [] nodes = n.getChildren().getNodes();
                    Children.Array ch = new Children.Array();
                    ch.add((Node[]) list.toArray(new Node[0]));
                    n.setName(NbBundle.getBundle(Favorites.class).getString ("CTL_MyComputer"));
                    return new Node[] { new FilterNode(n, ch) };
                } else {
                    return (Node[])list.toArray(new Node[0]);
                }
            }
            
            Node node = (Node)key;
            return new Node[] { new ProjectFilterNode (
                node,
                (node.isLeaf ()) ? org.openide.nodes.Children.LEAF : new Chldrn (node)
            )};
        }
        
    }

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
                    String name = ds.getOriginal().getName();
                    String path = FileUtil.getFileDisplayName(ds.getOriginal().getPrimaryFile());
                    return NbBundle.getMessage(Favorites.class, "CTL_DisplayNameTemplate", name, path);
                } else {
                    return super.getDisplayName();
                }
            } else {
                return super.getDisplayName();
            }
            /*String s = super.getDisplayName ();
            for (;;) {
                int indx = s.indexOf("(->)"); // NOI18N
                if (indx == -1) return s;
                
                s = s.substring(0, indx) + s.substring (indx + 4);
            }*/
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

        public void destroy () throws java.io.IOException {
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
            List newArr = new ArrayList();
            for (int i = 0; i < arr.length; i++) {
                //Add before CopyAction or CutAction
                if (!added && ((arr[i] instanceof CopyAction) || (arr[i] instanceof CutAction))) {
                    added = true;
                    newArr.add(Actions.remove());
                    newArr.add(null);
                }
                newArr.add(arr[i]);
            }
            if (!added) {
                added = true;
                newArr.add(null);
                newArr.add(Actions.remove());
            }
            
            return (Action[])newArr.toArray (new Action[newArr.size()]);
        }
        
        /** Add action 'Remove from Favorites'. */
        private Action [] createActionsForFavoriteFile (Action [] arr) {
            boolean added = false;
            List newArr = new ArrayList();
            for (int i = 0; i < arr.length; i++) {
                //Add before CopyAction or CutAction
                if (!added && ((arr[i] instanceof CopyAction) || (arr[i] instanceof CutAction))) {
                    added = true;
                    newArr.add(Actions.remove());
                    newArr.add(null);
                }
                newArr.add(arr[i]);
            }
            if (!added) {
                added = true;
                newArr.add(null);
                newArr.add(Actions.remove());
            }
            return (Action[])newArr.toArray (new Action[newArr.size()]);
        }
        
        /** Add action 'Add to Favorites'. */
        private Action [] createActionsForFolder (Action [] arr) {
            boolean added = false;
            List newArr = new ArrayList();
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
            return (Action[])newArr.toArray (new Action[newArr.size()]);
        }
        
        /** Add action 'Add to Favorites'. */
        private Action [] createActionsForFile (Action [] arr) {
            boolean added = false;
            List newArr = new ArrayList();
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
            return (Action[])newArr.toArray (new Action[newArr.size()]);
        }
        
    }
}
