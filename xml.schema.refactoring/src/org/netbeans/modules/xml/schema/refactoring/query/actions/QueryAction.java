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

/*
 * QueryAction.java
 *
 * Created on May 30, 2006, 11:24 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.schema.refactoring.query.actions;

import javax.swing.*;

import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;
import org.openide.util.actions.Presenter;
import org.openide.util.actions.SystemAction;

/**
 * Action which just holds a few other SystemAction's for grouping purposes.
 * @author cwebster
 * @author Jeri Lockhart
 */
public class QueryAction  extends NodeAction {
    private static final long serialVersionUID = 1L;

    /**
     * Get a human presentable name of the action.
     * This may be
     * presented as an item in a menu.
     * <p>Using the normal menu presenters, an included ampersand
     * before a letter will be treated as the name of a mnemonic.
     * 
     * @return the name of the action
     */
    public String getName() {
        return NbBundle.getMessage(QueryAction.class,
                "LBL_Query");
    }

    /** List of system actions to be displayed within this one's toolbar or submenu. */
    private static final SystemAction[] grouped() {
        return new SystemAction[] {
            SystemAction.get(FindUnusedAction.class),  
            SystemAction.get(FindCTDerivationsAction.class),  
            SystemAction.get(FindSubstitutionGroupsAction.class),  
//            null
        };
    }
    
      
    public JMenuItem getPopupPresenter() {
        return new LazyMenu(getName());
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
        // If you will provide context help then use:
        // return new HelpCtx(PromoteBusinessMethodAction.class);
    }

    protected boolean enable(org.openide.nodes.Node[] activatedNodes) {
        return true;
    }

    protected void performAction(org.openide.nodes.Node[] activatedNodes) {
         assert false : "Should never be called: ";
    }


    /**
     * Avoids constructing submenu until it will be needed.
     */
    protected class LazyMenu extends JMenu {
        private final static long serialVersionUID = 1L;

        public LazyMenu(String name) {
            super(name);
        }

        public JPopupMenu getPopupMenu() {
            if (getItemCount() == 0) {
                SystemAction[] grouped = grouped();
                for (int i = 0; i < grouped.length; i++) {
                    SystemAction action = grouped[i];
                    if (action == null) {
                        addSeparator();
                    } else if (action instanceof Presenter.Popup) {
                        add(((Presenter.Popup)action).getPopupPresenter());
                    } else {
                        assert false : "Action had no popup presenter: " + action;
                    }
                }
            }
            return super.getPopupMenu();
        }

    }

    
}
