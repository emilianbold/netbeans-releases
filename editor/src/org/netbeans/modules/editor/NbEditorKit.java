/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.editor;

import java.awt.event.ActionEvent;
import javax.swing.JEditorPane;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.text.Document;
import javax.swing.text.Caret;
import javax.swing.text.JTextComponent;
import javax.swing.text.TextAction;
import org.netbeans.editor.ActionFactory;
import org.netbeans.editor.EditorUI;
import org.netbeans.editor.ext.ExtKit;
import org.netbeans.editor.ext.FindDialogSupport;
import org.netbeans.editor.ext.GotoDialogSupport;
import org.openide.TopManager;
import org.openide.windows.TopComponent;
import org.openide.util.actions.SystemAction;
import org.openide.util.actions.Presenter;
import org.openide.actions.UndoAction;
import org.openide.actions.RedoAction;
import org.openide.windows.TopComponent;

/**
* Java editor kit with appropriate document
*
* @author Miloslav Metelka
* @version 1.00
*/

public class NbEditorKit extends ExtKit {

    /** Action property that stores the name of the corresponding nb-system-action */
    public static final String SYSTEM_ACTION_CLASS_NAME_PROPERTY = "systemActionClassName";

    static final long serialVersionUID =4482122073483644089L;

    public Document createDefaultDocument() {
        return new NbEditorDocument(this.getClass());
    }

    protected EditorUI createEditorUI() {
        return new NbEditorUI();
    }

    protected Action[] createActions() {
        Action[] nbEditorActions = new Action[] {
                                       new NbBuildPopupMenuAction(),
                                       new NbUndoAction(),
                                       new NbRedoAction(),
                                   };
        return TextAction.augmentList(super.createActions(), nbEditorActions);
    }


    protected void addSystemActionMapping(String editorActionName, Class systemActionClass) {
        Action a = getActionByName(editorActionName);
        if (a != null) {
            a.putValue(SYSTEM_ACTION_CLASS_NAME_PROPERTY, systemActionClass.getName());
        }
    }

    protected void updateActions() {
        addSystemActionMapping(cutAction, org.openide.actions.CutAction.class);
        addSystemActionMapping(copyAction, org.openide.actions.CopyAction.class);
        addSystemActionMapping(pasteAction, org.openide.actions.PasteAction.class);
        addSystemActionMapping(removeSelectionAction, org.openide.actions.DeleteAction.class);

        addSystemActionMapping(findAction, org.openide.actions.FindAction.class);
        addSystemActionMapping(replaceAction, org.openide.actions.ReplaceAction.class);
        addSystemActionMapping(gotoAction, org.openide.actions.GotoAction.class);
    }

    public Class getFocusableComponentClass(JTextComponent c) {
        return TopComponent.class;
    }

    public class NbBuildPopupMenuAction extends BuildPopupMenuAction {

        static final long serialVersionUID =-8623762627678464181L;

        protected void addAction(JTextComponent target, JPopupMenu popupMenu,
        String actionName) {
            if (actionName != null) { // try if it's an action class name
                // Check for the TopComponent actions
                if (TopComponent.class.getName().equals(actionName)) {
                    // Get the cloneable-editor instance
                    TopComponent tc = NbEditorUtilities.getTopComponent(target);
                    if (tc != null) {
                        // Add all the actions
                        SystemAction[] actions = tc.getSystemActions();
                        TopManager tm = NbEditorUtilities.getTopManager();
                        if (tm != null) { // IDE initialized
                            for (int i = 0; i < actions.length; i++) {
/*                                System.out.println("NbEditorKit.java:126 top-component actions["
                                        + i + "]=" + ((actions[i] != null)
                                            ? actions[i].getClass().getName() : "NULL"));
*/
                                if (actions[i] instanceof Presenter.Popup) {
                                    JMenuItem item = ((Presenter.Popup)actions[i]).getPopupPresenter();
                                    if (item != null && !(item instanceof JMenu)) {
                                        KeyStroke[] keys
                                            = tm.getGlobalKeymap().getKeyStrokesForAction(actions[i]);
                                        if (keys != null && keys.length > 0) {
                                            item.setAccelerator(keys[0]);
                                        }

                                    }

                                    if (item != null) {
                                        popupMenu.add(item);
                                    }

                                } else if (actions[i] == null) {
                                    popupMenu.addSeparator();
                                }
                            }
                        }
                    }

                    return;

                } else { // not cloneable-editor actions
                    Class saClass;
                    try {
                        saClass = Class.forName(actionName);
                    } catch (Throwable t) {
                        saClass = null;
                    }

                    if (saClass != null && SystemAction.class.isAssignableFrom(saClass)) {
                        TopManager tm = NbEditorUtilities.getTopManager();
                        if (tm != null) { // IDE initialized
                            SystemAction sa = SystemAction.get(saClass);
                            if (sa instanceof Presenter.Popup) {
                                JMenuItem item = ((Presenter.Popup)sa).getPopupPresenter();
                                if (item != null && !(item instanceof JMenu)) {
                                    KeyStroke[] keys = tm.getGlobalKeymap().getKeyStrokesForAction(sa);
                                    if (keys != null && keys.length > 0) {
                                        item.setAccelerator(keys[0]);
                                    }
                                }

                                if (item != null) {
                                    popupMenu.add(item);
                                }
                            }
                        }

                        return;
                    }
                }

            }

            super.addAction(target, popupMenu, actionName);

        }


    }
    
    public static class NbUndoAction extends ActionFactory.UndoAction {

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (NbEditorUtilities.getTopManager() != null) {
                // Delegate to system undo action
                UndoAction ua = (UndoAction)SystemAction.get(UndoAction.class);
                if (ua != null && ua.isEnabled()) {
                    ua.actionPerformed(evt);
                }

            } else {
                super.actionPerformed(evt, target);
            }
        }

    }

    public static class NbRedoAction extends ActionFactory.RedoAction {

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (NbEditorUtilities.getTopManager() != null) {
                // Delegate to system redo action
                RedoAction ra = (RedoAction)SystemAction.get(RedoAction.class);
                if (ra != null && ra.isEnabled()) {
                    ra.actionPerformed(evt);
                }

            } else {
                super.actionPerformed(evt, target);
            }
        }

    }




}
