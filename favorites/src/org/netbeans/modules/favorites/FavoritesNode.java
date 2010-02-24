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

package org.netbeans.modules.favorites;

import java.awt.Image;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.openide.loaders.ChangeableDataFilter;
import org.openide.loaders.DataFilter;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataShadow;
import org.openide.loaders.LoaderTransfer;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Index;
import org.openide.nodes.Node;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.datatransfer.PasteType;

/**
 *
 * @author  Jaroslav Tulach
 */
public final class FavoritesNode extends FilterNode implements Index {
    /** default node */
    private static Node node;
    /** node that represents root of filesystems */
    private static Node root;

    /** Creates new ProjectRootFilterNode. */
    private FavoritesNode(Node node) {
        super(node, new Chldrn (node, false));
    }

    @Override
    public PasteType getDropType(Transferable t, int action, int index) {
        // #139713: drop into empty area creates new link, otherwise disabled
        if (index != -1)
            return null;
        // any kind of drop just creates link in Favorites
        DataObject[] dos = LoaderTransfer.getDataObjects(t, LoaderTransfer.DND_COPY_OR_MOVE | LoaderTransfer.CLIPBOARD_CUT);
        if (dos == null)
            return null;
        for (DataObject dataObject : dos) {
            if (! Actions.Add.isAllowed(dataObject))
                return null;
        }
        return new FavoritesPasteType(dos);
    }
    
    @Override
    public <T extends Node.Cookie> T getCookie(Class<T> cl) {
        if (cl == Index.class) {
            return cl.cast(this);
        } else {
            return super.getCookie(cl);
        }
    }
    
    public int getNodesCount() {
        Index ind = getOriginal().getCookie(Index.class);
        if (ind != null) {
            return ind.getNodesCount();
        } else {
            return 0;
        }
    }
    
    public Node[] getNodes() {
        Index ind = getOriginal().getCookie(Index.class);
        if (ind != null) {
            return ind.getNodes();
        } else {
            return new Node [] {};
        }        
    }

    public int indexOf(final Node node) {
        Index ind = getOriginal().getCookie(Index.class);
        if (ind != null) {
            if (node instanceof FavoritesNode.ProjectFilterNode) {
                FavoritesNode.ProjectFilterNode fn = (FavoritesNode.ProjectFilterNode) node;
                int i = ind.indexOf(fn.getOriginal());
                return i;
            } else {
                int i = ind.indexOf(node);
                return i;
            }
        } else {
            return -1;
        }                
    }

    public void reorder() {
        Index ind = getOriginal().getCookie(Index.class);
        if (ind != null) {
            ind.reorder();
        }
    }

    public void reorder(int[] perm) {
        Index ind = getOriginal().getCookie(Index.class);
        if (ind != null) {
            ind.reorder(perm);
        }
    }

    public void move(int x, int y) {
        Index ind = getOriginal().getCookie(Index.class);
        if (ind != null) {
            ind.move(x,y);
        }
    }

    public void exchange(int x, int y) {
        Index ind = getOriginal().getCookie(Index.class);
        if (ind != null) {
            ind.exchange(x,y);
        }
    }

    public void moveUp(int x) {
        Index ind = getOriginal().getCookie(Index.class);
        if (ind != null) {
            ind.moveUp(x);
        }
    }

    public void moveDown(int x) {
        Index ind = getOriginal().getCookie(Index.class);
        if (ind != null) {
            ind.moveDown(x);
        }
    }

    public void addChangeListener(final ChangeListener chl) {
        Index ind = getOriginal().getCookie(Index.class);
        if (ind != null) {
            ind.addChangeListener(chl);
        }
    }

    public void removeChangeListener(final ChangeListener chl) {
        Index ind = getOriginal().getCookie(Index.class);
        if (ind != null) {
            ind.removeChangeListener(chl);
        }
    }
    
    @Override
    public Image getIcon (int type) {
        return ImageUtilities.loadImage("org/netbeans/modules/favorites/resources/actionView.png"); // NOI18N
    }
    
    @Override
    public Image getOpenedIcon (int type) {
        return ImageUtilities.loadImage("org/netbeans/modules/favorites/resources/actionView.png"); // NOI18N
    }
    
    @Override
    public boolean canCopy () {
        return false;
    }
    
    @Override
    public boolean canCut () {
        return false;
    }
    
    @Override
    public boolean canRename () {
        return false;
    }
    
    public static DataFolder getFolder () {
        try {
            FileObject fo = FileUtil.createFolder (
                FileUtil.getConfigRoot(),
                "Favorites" // NOI18N
            );
            DataFolder folder = DataFolder.findFolder(fo);
            return folder;
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            return DataFolder.findFolder (FileUtil.getConfigRoot());
        }
        
    }
    
    /** Getter for default filter node.
     */
    public static synchronized Node getNode () {
        if (node == null) {
            node = new FavoritesNode (getFolder().getNodeDelegate ());
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
        DataObject obj = n.getCookie (DataObject.class);
        if (obj == null) return null;
        
        return FileUtil.toFile (
            obj.getPrimaryFile()
        );
    }

    @Override
    public Handle getHandle () {
        return new RootHandle ();
    }
    
    @Override
    public Action[] getActions(boolean context) {
        return new Action[] {Actions.addOnFavoritesNode()};
    }
    
    private static class RootHandle implements Node.Handle {
        static final long serialVersionUID = 1907300072945111595L;

        /** Return a node for the current project.
        */
        public Node getNode () {
            return FavoritesNode.getNode ();
        }
    }



    static class VisQ 
    implements DataFilter.FileBased, ChangeableDataFilter, ChangeListener {
        public static final VisQ DEFAULT = new VisQ();

        private ChangeListener weak;
        private ChangeSupport support = new ChangeSupport(this);

        VisQ() {
            weak = org.openide.util.WeakListeners.change(this, VisibilityQuery.getDefault());
            VisibilityQuery.getDefault().addChangeListener(weak);
        }
        
        public boolean acceptFileObject(FileObject fo) {
            return VisibilityQuery.getDefault().isVisible(fo);
        }

        public boolean acceptDataObject(DataObject obj) {
            return acceptFileObject(obj.getPrimaryFile());
        }

        public void addChangeListener(ChangeListener listener) {
            support.addChangeListener(listener);
        }

        public void removeChangeListener(ChangeListener listener) {
            support.removeChangeListener(listener);
        }

        public void stateChanged(ChangeEvent e) {
            support.fireChange();
        }
    } // end of VisQ
    
    private static class Chldrn extends FilterNode.Children {
        private boolean hideHidden;
        /** Creates new Chldrn. */
        public Chldrn(Node node, boolean hideHidden) {
            super (node);
            this.hideHidden = hideHidden;
        }
        
        @Override
        protected Node[] createNodes(Node node) {
            org.openide.nodes.Children ch = Children.LEAF;
            DataObject obj = node.getLookup().lookup(DataObject.class);
            if (hideHidden) {
                if (obj != null && !VisibilityQuery.getDefault().isVisible(obj.getPrimaryFile())) {
                    return null;
                }
            }

            DataFolder folder = node.getLookup().lookup(DataFolder.class);
            if (folder != null) {
                ch = new Chldrn(new FilterNode(node, folder.createNodeChildren(new VisQ())), true);
            } else {
                if (node.isLeaf()) {
                    ch = org.openide.nodes.Children.LEAF;
                } else {
                    ch = new Chldrn(node, true);
                }
            }
            
            return new Node[] { new ProjectFilterNode (node, ch) };
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
        
        @Override
        public void setName(String name) {
            // #113859 - keep order of children in favorites folder after rename
            final DataFolder favoritesFolder = FavoritesNode.getFolder();
            final DataObject[] children = favoritesFolder.getChildren();
            super.setName(name);
            try {
                favoritesFolder.setOrder(children);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        @Override
        public String getDisplayName () {
            //Change display name only for favorite nodes (links) under Favorites node.
            if (FavoritesNode.getNode().equals(this.getParentNode())) {
                DataShadow ds = getCookie(DataShadow.class);
                if (ds != null) {
                    String name = ds.getName();
                    String path = FileUtil.getFileDisplayName(ds.getOriginal().getPrimaryFile());
                    return NbBundle.getMessage(FavoritesNode.class, "CTL_DisplayNameTemplate", name, path);
                } else {
                    return super.getDisplayName();
                }
            } else {
                return super.getDisplayName();
            }
        }
        
        @Override
        public String getHtmlDisplayName() {
            if (FavoritesNode.getNode().equals(this.getParentNode())) {
                DataShadow ds = getCookie(DataShadow.class);
                if (ds != null) {
                    String name = ds.getName();
                    String path = FileUtil.getFileDisplayName(ds.getOriginal().getPrimaryFile());
                    return NbBundle.getMessage(FavoritesNode.class, "CTL_DisplayNameTemplateHtml", name, path); //NOI18N
                } else {
                    return super.getDisplayName();
                }
            } else {
                return getOriginal().getHtmlDisplayName();
            }
        }
        
        @Override
        protected Node getOriginal() {
            return super.getOriginal();
        }
                
        @Override
        public boolean canDestroy () {
            boolean canDestroy = super.canDestroy ();
            DataShadow link = getCookie (DataShadow.class);

            // if the DO of this node can be destroyed and the original DO should be destroyed too
            // ask the original if it's allowed to delete it
            if (canDestroy && isDeleteOriginal (link)) {
                canDestroy = link.getOriginal ().isDeleteAllowed ();
            }

            return canDestroy;
        }

        @Override
        public void destroy () throws IOException {
            if (canDestroy ()) {
                DataShadow link = getCookie (DataShadow.class);
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
        
        @Override
        public Action[] getActions(boolean context) {
            Action[] arr;
            arr = super.getActions(context);
            
            //Find if given node is root
            boolean isRoot = false;
            DataObject dataObject = getCookie(DataObject.class);
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
                if (FavoritesNode.getNode().equals(this.getParentNode())) {
                    DataShadow ds = getCookie(DataShadow.class);
                    if (ds != null) {
                        if (ds.getOriginal().getPrimaryFile().isFolder()) {
                            return createActionsForFavoriteFolder(arr);
                        } else {
                            return createActionsForFavoriteFile(arr);
                        }
                    }
                } else {
                    DataObject dObj = getCookie(DataObject.class);
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

    private static class FavoritesPasteType extends PasteType {
        private final DataObject[] dos;

        private FavoritesPasteType(DataObject[] dos) {
            this.dos = dos;
        }

        @Override
        public Transferable paste() throws IOException {
            Actions.Add.addToFavorites(Arrays.asList(dos));
            return null;
        }

    }
}
