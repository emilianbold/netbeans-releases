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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.editor.impl.actions;

import javax.swing.JMenuItem;
import org.netbeans.modules.editor.MainMenuAction;
import org.openide.util.NbBundle;

/**
 *
 * @author phrebejk
 */
public class LineActionsMainMenu {

    // From BaseKit
    private static final String moveSelectionElseLineUpAction = "move-selection-else-line-up"; // NOI18N
    private static final String moveSelectionElseLineDownAction = "move-selection-else-line-down"; // NOI18N
    private static final String copySelectionElseLineUpAction = "copy-selection-else-line-up"; // NOI18N
    private static final String copySelectionElseLineDownAction = "copy-selection-else-line-down"; // NOI18N
    
    
    public static final class MoveUp extends MainMenuAction {

        private JMenuItem menuItem;

        public MoveUp() {
            super();
            menuItem = new JMenuItem(getMenuItemText());
            setMenu();
        }

        protected String getMenuItemText() {
            return NbBundle.getBundle(LineActionsMainMenu.class).getString(moveSelectionElseLineUpAction + "-main_menu_item"); //NOI18N
        }

        public JMenuItem getMenuPresenter() {
            return menuItem;
        }

        protected String getActionName() {
            return moveSelectionElseLineUpAction;
        }
    } 
    
    public static final class MoveDown extends MainMenuAction {

        private JMenuItem menuItem;

        public MoveDown() {
            super();
            menuItem = new JMenuItem(getMenuItemText());
            setMenu();
        }

        protected String getMenuItemText() {
            return NbBundle.getBundle(LineActionsMainMenu.class).getString(moveSelectionElseLineDownAction + "-main_menu_item"); //NOI18N
        }

        public JMenuItem getMenuPresenter() {
            return menuItem;
        }

        protected String getActionName() {
            return moveSelectionElseLineDownAction;
        }
    } // end of ShiftLineLeftAction
    
    public static final class DuplicateUp extends MainMenuAction {

        private JMenuItem menuItem;

        public DuplicateUp() {
            super();
            menuItem = new JMenuItem(getMenuItemText());
            setMenu();
        }

        protected String getMenuItemText() {
            return NbBundle.getBundle(LineActionsMainMenu.class).getString(copySelectionElseLineUpAction + "-main_menu_item"); //NOI18N
        }

        public JMenuItem getMenuPresenter() {
            return menuItem;
        }

        protected String getActionName() {
            return copySelectionElseLineUpAction;
        }
    } 
    
    public static final class DuplicateDown extends MainMenuAction {

        private JMenuItem menuItem;

        public DuplicateDown() {
            super();
            menuItem = new JMenuItem(getMenuItemText());
            setMenu();
        }

        protected String getMenuItemText() {
            return NbBundle.getBundle(LineActionsMainMenu.class).getString(copySelectionElseLineDownAction + "-main_menu_item"); //NOI18N
        }

        public JMenuItem getMenuPresenter() {
            return menuItem;
        }

        protected String getActionName() {
            return copySelectionElseLineDownAction;
        }
    } 
    
}
