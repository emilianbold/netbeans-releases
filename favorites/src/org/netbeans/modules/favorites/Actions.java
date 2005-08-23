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

import java.awt.*;
import java.awt.event.ActionEvent;
import java.beans.PropertyVetoException;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.actions.*;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataShadow;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/** List of all actions available for Favorites module.
* @author   Jaroslav Tulach
*/
public final class Actions extends Object {
    
    /** Used to keep current dir from JFileChooser for Add to Favorites action
     * on root node. */
    private static File currentDir = null;
    
    private Actions () {
        // noinstances
    }
    
    public static Action view () { return View.getDefault(); }
    public static Action add () { return Add.getDefault(); }
    public static Action addOnFavoritesNode () { return AddOnFavoritesNode.getDefault(); }
    public static Action remove () { return Remove.getDefault(); }
    public static Action select () { return Select.getDefault(); }
    public static Action selectMainMenu () { return SelectMainMenu.getDefault(); }
    
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
            Image image = Utilities.loadImage("org/netbeans/modules/favorites/resources/actionView.gif"); // NOI18N
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
        
        protected void performAction(Node[] activatedNodes) {
            Tab proj = Tab.findDefault();
            proj.open();
            proj.requestActive();
            proj.doSelectNode((DataObject)activatedNodes[0].getCookie(DataObject.class));
        }

        protected boolean enable(Node[] activatedNodes) {
            if (activatedNodes.length != 1) {
                return false;
            }
            return true;
            /*
            DataObject dobj = (DataObject)activatedNodes[0].getCookie(DataObject.class);
            if (dobj == null) {
                return false;
            }
            return Tab.findDefault().containsNode(dobj);
             */
          }

        public String getName() {
            return NbBundle.getMessage(Select.class, "ACT_Select"); // NOI18N
        }

        protected String iconResource() {
            return "org/netbeans/modules/favorites/resources/actionSelect.gif"; // NOI18N
        }

        public HelpCtx getHelpCtx() {
            return null;
        }

        protected boolean asynchronous() {
            return false;
        }

    } // end of Select


    private static final class SelectMainMenu extends Select {
        private static final Select SELECT_MAIN_MENU = new SelectMainMenu ();

        private SelectMainMenu () {
            super();
            putValue("noIconInMenu", Boolean.TRUE); //NOI18N
        }
        
        public static Action getDefault () {
            return SELECT_MAIN_MENU;
        }
        
        public String getName() {
            return NbBundle.getMessage(Select.class, "ACT_Select_Main_Menu"); // NOI18N
        }
        
    } // end of SelectMainMenu

    
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
                DataObject shad = (DataObject) arr[i].getCookie (DataObject.class);
                //Disable when node is not shadow in Favorites folder.
                if (shad == null || shad.getFolder() != Favorites.getFolder()) {
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
            return org.openide.util.NbBundle.getMessage (
                    Actions.class, "ACT_Remove"); // NOI18N
        }

        /** Help context where to find more about the action.
        * @return the help context for this action
        */
        public HelpCtx getHelpCtx() {
            return new HelpCtx(Remove.class);
        }

        /** Icon resource.
        * @return name of resource for icon
        */
        protected String iconResource () {
            return "org/openide/resources/actions/empty.gif"; // NOI18N
        }

        /**
        * Removes the links.
        *
        * @param arr gives array of actually activated nodes.
        */
        protected void performAction (Node[] arr) {
            for (int i = 0; i < arr.length; i++) {
                DataObject shad = (DataObject) arr[i].getCookie (DataObject.class);
                if (shad != null && shad.getFolder() == Favorites.getFolder()) {
                    try {
                        shad.delete();
                    } catch (java.io.IOException ex) {
                        ErrorManager.getDefault().notify(ex);
                    }
                }
            }
        }

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
            if (arr.length == 1 && arr[0] instanceof Favorites) return true;
                
            

            for (int i = 0; i < arr.length; i++) {
                DataObject dataObject = (DataObject) arr[i].getCookie (DataObject.class);
                //Action is disabled for root folder eg:"/" on Linux or "C:" on Win
                if (dataObject == null) {
                    return false;
                }
                FileObject fo = dataObject.getPrimaryFile();
                if (fo != null) {
                    //Allow to link only once
                    if (isInFavorites(fo)) {
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
                    if(dataObject.getPrimaryFile().getFileSystem().isDefault()) {
                        return false;
                    }
                } catch(FileStateInvalidException fsie) {
                    return false;
                }
            }
            return true;
        }
        
        /** Check if given fileobject is already linked in favorites
         * @return true if given fileobject is already linked
         */
        private boolean isInFavorites (FileObject fo) {
            DataFolder f = Favorites.getFolder();
            
            DataObject [] arr = f.getChildren();
            for (int i = 0; i < arr.length; i++) {
                if (arr[i] instanceof DataShadow) {
                    if (fo.equals(((DataShadow) arr[i]).getOriginal().getPrimaryFile())) {
                        return true;
                    }
                }
            }
            return false;
        }
        
        /** Human presentable name of the action. This should be
        * presented as an item in a menu.
        * @return the name of the action
        */
        public String getName() {
            return org.openide.util.NbBundle.getMessage (
                    Actions.class, "ACT_Add"); // NOI18N
        }

        /** Help context where to find more about the action.
        * @return the help context for this action
        */
        public HelpCtx getHelpCtx() {
            return new HelpCtx(Add.class);
        }

        /** Icon resource.
        * @return name of resource for icon
        */
        protected String iconResource () {
            return "org/openide/resources/actions/empty.gif"; // NOI18N
        }

        /**
        * Standard perform action extended by actually activated nodes.
        *
        * @param activatedNodes gives array of actually activated nodes.
        */
        protected void performAction (final Node[] activatedNodes) {
            final DataFolder f = Favorites.getFolder();            
            final DataObject [] arr = f.getChildren();
            final List listAdd = new ArrayList();
            
            DataObject createdDO = null;
            Node[] toShadows = activatedNodes; 

            try {
                if (activatedNodes.length == 1 && activatedNodes[0] instanceof Favorites) {
                    // show JFileChooser
                    FileObject fo = chooseFileObject();
                    if (fo == null) return;
                    toShadows = new Node[] {DataObject.find(fo).getNodeDelegate()};                
                } 
                
                
                createdDO = createShadows(f, toShadows, listAdd);    
                
                //This is done to set desired order of nodes in view                             
                reorderAfterAddition(f, arr, listAdd);
                selectAfterAddition(createdDO);               
            } catch (DataObjectNotFoundException e) {
                ErrorManager.getDefault().notify(e);  
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
                if (!selectedFile.exists()) {
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
        
        private void selectAfterAddition(final DataObject createdDO) {
            final Tab projectsTab = Tab.findDefault();
            projectsTab.open();
            projectsTab.requestActive();
            //Try to locate newly added node and select it
            if (createdDO != null) {
                Node n = Favorites.getNode();
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

        private static DataObject createShadows(final DataFolder favourities, final Node[] activatedNodes, final List listAdd) {
            DataObject createdDO = null;
            for (int i = 0; i < activatedNodes.length; i++) {
                DataObject obj = (DataObject)activatedNodes[i].getCookie (DataObject.class);
                if (obj != null) {
                    try {
                        Favorites.ensureShadowsWork (obj.getPrimaryFile());
                        if (createdDO == null) {
                            //Select only first node in array added to favorites
                            createdDO = obj.createShadow(favourities);
                            listAdd.add(createdDO);
                        } else {
                            listAdd.add(obj.createShadow(favourities));
                        }
                    } catch (java.io.IOException ex) {
                        ErrorManager.getDefault().notify(ex);
                    }
                }
            }
            return createdDO;
        }

        private static void reorderAfterAddition(final DataFolder favourities, final DataObject[] children, final List listAdd) {
            List listDest = new ArrayList();
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
                DataObject [] newOrder = (DataObject []) listDest.toArray(new DataObject[listDest.size()]);
                try {
                    favourities.setOrder(newOrder);
                } catch (java.io.IOException ex) {
                    ErrorManager.getDefault().notify(ex);
                }
            }
        }

        protected boolean asynchronous() {
            return false;
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
        public String getName() {
            return org.openide.util.NbBundle.getMessage (
                    Actions.class, "ACT_AddOnFavoritesNode"); // NOI18N
        }
    }
    
}
