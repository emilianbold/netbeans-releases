/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.favorites;

import java.awt.Image;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;

import org.openide.ErrorManager;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.actions.*;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;

/** List of all actions available for Favorites module.
* @author   Jaroslav Tulach
*/
public final class Actions extends Object {
    private static final View VIEW = new View ();
    private static final Select SELECT = new Select ();
    private static final Add ADD = new Add ();
    private static final Remove REMOVE = new Remove ();
    
    private Actions () {
        // noinstances
    }
    
    
    public static Action view () { return VIEW; }
    public static Action add () { return ADD; }
    public static Action remove () { return REMOVE; }
    public static Action select () { return SELECT; }
    
    /**
     * Action which opend <code>CurrentProjectNode.ProjectsTab</code> default component.
     *
     * @author  Peter Zavadsky
     */
    private static class View extends AbstractAction implements HelpCtx.Provider {
        public View() {
            putValue(NAME, NbBundle.getMessage(Actions.class,
                    "ACT_View"));
            Image image = Utilities.loadImage("org/netbeans/modules/favorites/resources/actionView.gif"); // NOI18N
            putValue(SMALL_ICON, image != null ? new ImageIcon(image) : null);
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
    private static final class Select extends NodeAction {

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
    
    
    /** Removes root link from favorites
    * @author   Jaroslav Tulach
    */
    private static class Remove extends NodeAction {
        static final long serialVersionUID =-6471281373153172312L;
        /** generated Serialized Version UID */
        //  static final long serialVersionUID = -5280204757097896304L;

        /** Enabled only if the current project is ProjectDataObject.
        */
        public boolean enable (Node[] arr) {
            if ((arr == null) || (arr.length == 0)) return false;

            for (int i = 0; i < arr.length; i++) {
                DataObject shad = (DataObject) arr[i].getCookie (DataObject.class);
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

        /** Enabled only if the current project is ProjectDataObject.
        */
        public boolean enable (Node[] arr) {
            if ((arr == null) || (arr.length == 0)) return false;

            for (int i = 0; i < arr.length; i++) {
                DataObject dataObject = (DataObject) arr[i].getCookie (DataObject.class);
                if (dataObject == null) {
                    return false;
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
        protected void performAction (Node[] activatedNodes) {
            org.openide.loaders.DataFolder f = Favorites.getFolder();

            for (int i = 0; i < activatedNodes.length; i++) {
                DataObject obj = (DataObject)activatedNodes[i].getCookie (DataObject.class);
                if (obj != null) {
                    try {
                        Favorites.ensureShadowsWork (obj.getPrimaryFile());
                        obj.createShadow (f);
                    } catch (java.io.IOException ex) {
                        ErrorManager.getDefault().notify(ex);
                    }
                }
            }

            org.openide.windows.TopComponent projectsTab = Tab.findDefault();
            projectsTab.open();
            projectsTab.requestActive();
        }

        protected boolean asynchronous() {
            return false;
        }

    } // end of Add
    
    
}
