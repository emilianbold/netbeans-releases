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

package org.netbeans.modules.favorites;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;
import org.netbeans.modules.favorites.api.Favorites;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/** List of all actions available for Favorites module.
* @author   Jaroslav Tulach
*/
public final class Actions extends Object {
    
    /** Used to keep current dir from JFileChooser for Add to Favorites action
     * on root node. */
    private static File currentDir = null;

    private static final Logger LOG = Logger.getLogger(Actions.class.getName());

    private Actions () {
        // noinstances
    }
    
    public static Action view () { return View.getDefault(); }
    public static Action add () { return Add.getDefault(); }
    public static Action addOnFavoritesNode () { return AddOnFavoritesNode.getDefault(); }
    public static Action remove () { return Remove.getDefault(); }
    public static Action select () { return Select.getDefault(); }
    
    /**
     * Action which opend <code>CurrentProjectNode.ProjectsTab</code> default component.
     *
     * @author  Peter Zavadsky
     */
    private static class View extends AbstractAction implements HelpCtx.Provider {
        
        private static final View VIEW = new View ();
        
        public View() {
            putValue(NAME, NbBundle.getMessage(Actions.class,
                    "ACT_View"));
            Image image = ImageUtilities.loadImage("org/netbeans/modules/favorites/resources/actionView.png"); // NOI18N
            putValue(SMALL_ICON, image != null ? new ImageIcon(image) : null);
        }
        
        public static Action getDefault () {
            return VIEW;
        }
        
        public void actionPerformed(ActionEvent evt) {
            final TopComponent projectsTab = Tab.findDefault();
            projectsTab.open();
            projectsTab.requestActive();
        }

        public HelpCtx getHelpCtx() {
            return new HelpCtx(View.class);
        }
    } // end of View
    
    
    /** An action which selects activated nodes in the Explorer's tab.
    * @author   Dusan Balek
    */
    private static class Select extends NodeAction {
        private static final Select SELECT = new Select ();
        
        public static Action getDefault () {
            return SELECT;
        }
        
        private Select () {
            super();
            putValue("noIconInMenu", Boolean.TRUE); //NOI18N
        }
        
        protected void performAction(Node[] activatedNodes) {
            Tab proj = Tab.findDefault();
            proj.open();
            proj.requestActive();
            proj.doSelectNode(activatedNodes[0].getCookie(DataObject.class));
        }

        protected boolean enable(Node[] activatedNodes) {
            if (activatedNodes.length != 1) {
                return false;
            }
            DataObject dobj = activatedNodes[0].getCookie(DataObject.class);
            if (dobj == null) {
                return false;
            }
            return true;
            /*return Tab.findDefault().containsNode(dobj);*/
          }

        public String getName() {
            return NbBundle.getMessage(Select.class, "ACT_Select_Main_Menu"); // NOI18N
        }

        /** Overriden to have different title in popup menu, works but ugly a bit */
        @Override
        public JMenuItem getPopupPresenter() {
            JMenuItem mi = super.getPopupPresenter();
            mi.setText(NbBundle.getMessage(Select.class, "ACT_Select")); // NOI18N
            return mi;
        }

        @Override
        protected String iconResource() {
            return "org/netbeans/modules/favorites/resources/actionView.png"; // NOI18N
        }

        public HelpCtx getHelpCtx() {
            return null;
        }

        @Override
        protected boolean asynchronous() {
            return false;
        }

    } // end of Select

    /** Removes root link from favorites
    * @author   Jaroslav Tulach
    */
    private static class Remove extends NodeAction {
        static final long serialVersionUID =-6471281373153172312L;
        /** generated Serialized Version UID */
        //  static final long serialVersionUID = -5280204757097896304L;
        
        private static final Remove REMOVE = new Remove ();
        
        public static Action getDefault () {
            return REMOVE;
        }
        
        /** Enabled only if the current project is ProjectDataObject.
        */
        public boolean enable (Node[] arr) {
            if ((arr == null) || (arr.length == 0)) return false;

            for (int i = 0; i < arr.length; i++) {
                DataObject shad = arr[i].getCookie(DataObject.class);
                //Disable when node is not shadow in Favorites folder.
                if (shad == null || shad.getFolder() != FavoritesNode.getFolder()) {
                    return false;
                }
            }
            return true;
        }

        /** Human presentable name of the action. This should be
        * presented as an item in a menu.
        * @return the name of the action
        */
        public String getName() {
            return NbBundle.getMessage (
                    Actions.class, "ACT_Remove"); // NOI18N
        }

        /** Help context where to find more about the action.
        * @return the help context for this action
        */
        public HelpCtx getHelpCtx() {
            return new HelpCtx(Remove.class);
        }

        /**
        * Removes the links.
        *
        * @param arr gives array of actually activated nodes.
        */
        protected void performAction (Node[] arr) {
            for (int i = 0; i < arr.length; i++) {
                DataObject shad = arr[i].getCookie(DataObject.class);

                if (shad != null && shad.getFolder() == FavoritesNode.getFolder()) {
                    try {
                        shad.delete();
                    } catch (IOException ex) {
                        LOG.log(Level.WARNING, null, ex);
                    }
                }
            }
        }

        @Override
        protected boolean asynchronous() {
            return false;
        }

    } // end of Remove
    

    /** Adds something to favorites. Made public so it can be referenced
    * directly from manifest.
    *
    * @author   Jaroslav Tulach
    */
    public static class Add extends NodeAction {
        static final long serialVersionUID =-6471281373153172312L;
        /** generated Serialized Version UID */
        //  static final long serialVersionUID = -5280204757097896304L;
        private static final Add ADD = new Add ();
        
        public static Action getDefault () {
            return ADD;
        }
        
        private Add () {
            putValue("noIconInMenu", Boolean.TRUE); // NOI18N
        }
        
        /** Enabled only if the current project is ProjectDataObject.
        */
        public boolean enable (Node[] arr) {
            if ((arr == null) || (arr.length == 0)) return false;
            if (arr.length == 1 && arr[0] instanceof FavoritesNode) return true;
                
            

            for (int i = 0; i < arr.length; i++) {
                DataObject dataObject = arr[i].getCookie(DataObject.class);
                if (! isAllowed(dataObject))
                    return false;
            }
            return true;
        }
        
        /** Human presentable name of the action. This should be
        * presented as an item in a menu.
        * @return the name of the action
        */
        public String getName() {
            return NbBundle.getMessage (
                    Actions.class, "ACT_Add"); // NOI18N
        }

        /** Help context where to find more about the action.
        * @return the help context for this action
        */
        public HelpCtx getHelpCtx() {
            return new HelpCtx(Add.class);
        }

        /**
        * Standard perform action extended by actually activated nodes.
        *
        * @param activatedNodes gives array of actually activated nodes.
        */
        protected void performAction (final Node[] activatedNodes) {
            List<DataObject> toShadows;

            try {
                if (activatedNodes.length == 1 && activatedNodes[0] instanceof FavoritesNode) {
                    // show JFileChooser
                    FileObject fo = chooseFileObject();
                    if (fo == null) return;
                    toShadows = Collections.singletonList(DataObject.find(fo));
                } else {
                    toShadows = new ArrayList<DataObject>();
                    for (Node node : activatedNodes) {
                        DataObject obj = node.getCookie(DataObject.class);
                        if (obj != null)
                            toShadows.add(obj);
                    }
                }
                addToFavorites(toShadows);
            } catch (DataObjectNotFoundException e) {
                LOG.log(Level.WARNING, null, e);
            }
        }
        
        /**
         * 
         * @return FileObject or null if FileChooser dialog is cancelled
         */ 
        private static FileObject chooseFileObject() {
            FileObject retVal = null;
            File chooserSelection = null;
            JFileChooser chooser = new JFileChooser ();
            chooser.setFileSelectionMode( JFileChooser.FILES_AND_DIRECTORIES );
            chooser.setDialogTitle(NbBundle.getBundle(Actions.class).getString ("CTL_DialogTitle"));
            chooser.setApproveButtonText(NbBundle.getBundle(Actions.class).getString ("CTL_ApproveButtonText"));
            if (currentDir != null) {
                chooser.setCurrentDirectory(currentDir);
            }
            int option = chooser.showOpenDialog( WindowManager.getDefault().getMainWindow() ); // Show the chooser
            if ( option == JFileChooser.APPROVE_OPTION ) {                    
                chooserSelection = chooser.getSelectedFile();
                File selectedFile = FileUtil.normalizeFile(chooserSelection);
                //Workaround for JDK bug #5075580 (filed also in IZ as #46882)
                if (!selectedFile.exists()) {
                    if ((selectedFile.getParentFile() != null) && selectedFile.getParentFile().exists()) {
                        if (selectedFile.getName().equals(selectedFile.getParentFile().getName())) {
                            selectedFile = selectedFile.getParentFile();
                        }
                    }
                }
                //#50482: Check if selected file exists eg. user can enter any file name to text box.
                //#144985: Create new File because of inconsistence in File.exists (JDK bug 6751997)
                if (!new File(selectedFile, "").exists()) {
                    String message = NbBundle.getMessage(Actions.class,"ERR_FileDoesNotExist",selectedFile.getPath());
                    String title = NbBundle.getMessage(Actions.class,"ERR_FileDoesNotExistDlgTitle");
                    DialogDisplayer.getDefault().notify
                    (new NotifyDescriptor(message,title,NotifyDescriptor.DEFAULT_OPTION,
                    NotifyDescriptor.INFORMATION_MESSAGE, new Object[] { NotifyDescriptor.CLOSED_OPTION },
                    NotifyDescriptor.OK_OPTION));
                } else {
                    retVal = FileUtil.toFileObject(selectedFile);
                    assert retVal != null;
                }
            }
            currentDir = chooser.getCurrentDirectory();
            return retVal;
        }
        
        public static void selectAfterAddition(final DataObject createdDO) {
            final Tab projectsTab = Tab.findDefault();
            projectsTab.open();
            projectsTab.requestActive();
            //Try to locate newly added node and select it
            if (createdDO != null) {
                Node [] nodes = projectsTab.getExplorerManager().getRootContext().getChildren().getNodes(true);
                final Node [] toSelect = new Node[1];
                boolean setSelected = false;
                for (int i = 0; i < nodes.length; i++) {
                    if (createdDO.getName().equals(nodes[i].getName())) {
                        toSelect[0] = nodes[i];
                        setSelected = true;
                        break;
                    }
                }
                if (setSelected) {
                    SwingUtilities.invokeLater(new Runnable () {
                        public void run() {
                            try {
                                projectsTab.getExplorerManager().setExploredContextAndSelection(toSelect[0],toSelect);
                            } catch (PropertyVetoException ex) {
                                //Nothing to do
                            }
                        }
                    });
                }
            }
        }

        static DataObject createShadows(final DataFolder favourities, final List<DataObject> dos, final List<DataObject> listAdd) {
            DataObject createdDO = null;
            for (DataObject obj : dos) {
                try {
                    if (createdDO == null) {
                        // Select only first node in array added to favorites
                        createdDO = obj.createShadow(favourities);
                        listAdd.add(createdDO);
                    } else {
                        listAdd.add(obj.createShadow(favourities));
                    }
                } catch (IOException ex) {
                    LOG.log(Level.WARNING, null, ex);
                }
            }
            return createdDO;
        }

        public static void reorderAfterAddition(final DataFolder favourities, final DataObject[] children, final List<? extends DataObject> listAdd) {
            List<DataObject> listDest = new ArrayList<DataObject>();
            if (listAdd.size() > 0) {
                //Insert new nodes just before last (root) node
                DataObject root = null;
                //Find root
                for (int i = 0; i < children.length; i++) {
                    FileObject fo = children[i].getPrimaryFile();
                    if ("Favorites/Root.instance".equals(fo.getPath())) { //NOI18N
                        root = children[i];
                    }
                }
                if (root != null) {
                    for (int i = 0; i < children.length; i++) {
                        if (!root.equals(children[i])) {
                            listDest.add(children[i]);
                        }
                    }
                    listDest.addAll(listAdd);
                    listDest.add(root);
                } else {
                    //Root not found. It should not happen because root is defined in layer
                    for (int i = 0; i < children.length; i++) {
                        listDest.add(children[i]);
                    }
                    listDest.addAll(listAdd);
                }
                //Set desired order
                DataObject [] newOrder = listDest.toArray(new DataObject[listDest.size()]);
                try {
                    favourities.setOrder(newOrder);
                } catch (IOException ex) {
                    LOG.log(Level.WARNING, null, ex);
                }
            }
        }

        @Override
        protected boolean asynchronous() {
            return false;
        }

        static void addToFavorites(List<DataObject> toShadows) {
            final DataFolder f = FavoritesNode.getFolder();
            final DataObject[] arr = f.getChildren();
            final List<DataObject> listAdd = new ArrayList<DataObject>();
            DataObject createdDO = createShadows(f, toShadows, listAdd);
            //This is done to set desired order of nodes in view
            reorderAfterAddition(f, arr, listAdd);
            selectAfterAddition(createdDO);
        }

        static boolean isAllowed(DataObject dataObject) {
            //Action is disabled for root folder eg:"/" on Linux or "C:" on Win
            if (dataObject == null) {
                return false;
            }
            FileObject fo = dataObject.getPrimaryFile();
            if (fo != null) {
                //#63459: Do not enable action on internal object/URL.
                if (URLMapper.findURL(fo, URLMapper.EXTERNAL) == null) {
                    return false;
                }
                //Allow to link only once
                if (Favorites.getDefault().isInFavorites(fo)) {
                    return false;
                }
                //Check if it is root.
                File file = FileUtil.toFile(fo);
                if (file != null) {
                    if (file.getParent() == null) {
                        //It is root: disable.
                        return false;
                    }
                }
            }

            // Fix #14740 disable action on SystemFileSystem.
            try {
                if (dataObject.getPrimaryFile().getFileSystem().isDefault()) {
                    return false;
                }
            } catch (FileStateInvalidException fsie) {
                return false;
            }
            return true;
        }

    } // end of Add
    /** Subclass of Add. Only its display name is different otherwise the same as Add.
    *
    * @author   Marek Slama
    */
    public static class AddOnFavoritesNode extends Add {
        static final long serialVersionUID =-6471284573153172312L;
        
        private static final AddOnFavoritesNode ADD_ON_FAVORITES_NODE = new AddOnFavoritesNode ();
        
        public static Action getDefault () {
            return ADD_ON_FAVORITES_NODE;
        }
        
        /** Human presentable name of the action. This should be
        * presented as an item in a menu.
        * @return the name of the action
        */
        @Override
        public String getName() {
            return NbBundle.getMessage (
                    Actions.class, "ACT_AddOnFavoritesNode"); // NOI18N
        }
    }
    
}
