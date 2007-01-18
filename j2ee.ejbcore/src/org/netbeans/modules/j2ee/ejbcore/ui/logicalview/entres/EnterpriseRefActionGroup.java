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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.ejbcore.ui.logicalview.entres;
import javax.swing.*;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;

import org.openide.util.*;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.api.ejbjar.EnterpriseReferenceContainer;
import org.openide.util.actions.Presenter;
import org.openide.util.actions.NodeAction;
import org.openide.util.actions.SystemAction;

/**
 * Action which just holds a few other SystemAction's for grouping purposes.
 * @author cwebster
 */
public class EnterpriseRefActionGroup extends NodeAction implements Presenter.Popup {
    
    public String getName() {
        return NbBundle.getMessage(EnterpriseRefActionGroup.class, "LBL_EnterpriseActionGroup");
    }
    
    /** List of system actions to be displayed within this one's toolbar or submenu. */
    private static final SystemAction[] grouped() {
        return new SystemAction[] {
            SystemAction.get(CallEjbAction.class),
            SystemAction.get(UseDatabaseAction.class),
            SystemAction.get(SendJMSMessageAction.class),
            SystemAction.get(SendEmailAction.class)
        };
    }
    
    public JMenuItem getPopupPresenter() {
        boolean oneNodeSelected = getActivatedNodes().length == 1;
        if (oneNodeSelected && hasEnterpriseRefStrategy()) {
            return new LazyMenu();
        }
        JMenuItem jMenuItem = super.getPopupPresenter();
        jMenuItem.setVisible(false);
        return jMenuItem;
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
        // If you will provide context help then use:
        // return new HelpCtx(PromoteBusinessMethodAction.class);
    }
    
    protected boolean enable(org.openide.nodes.Node[] activatedNodes) {
        //TODO: RETOUCHE
        return false;
    }
    
    protected void performAction(org.openide.nodes.Node[] activatedNodes) {
         assert false : "Should never be called: ";
    }
    
    private Project getSelectedProject() {
        Node[] activatedNodes = getActivatedNodes();
        DataObject dobj = (DataObject) 
            activatedNodes[0].getLookup().lookup(DataObject.class);
        
        return dobj == null ? null:FileOwnerQuery.getOwner(dobj.getPrimaryFile());
    }
    
    private boolean hasEnterpriseRefStrategy() {
        Project project = getSelectedProject();
        return project != null &&
            project.getLookup().lookup(EnterpriseReferenceContainer.class) != null;
    }

    /**
     * Avoids constructing submenu until it will be needed.
     */
    private final class LazyMenu extends JMenu {
        
        public LazyMenu() {
            super(EnterpriseRefActionGroup.this.getName());
        }
        
        public JPopupMenu getPopupMenu() {
            if (getItemCount() == 0) {
                Action[] grouped = grouped();
                for (int i = 0; i < grouped.length; i++) {
                    Action action = grouped[i];
                    if (action == null && getItemCount() != 0) {
                        addSeparator();
                    } else {
                        if (action instanceof ContextAwareAction) {
                            action = ((ContextAwareAction)action).createContextAwareInstance(Utilities.actionsGlobalContext());
                        }
                        if (action instanceof Presenter.Popup) {
                            add(((Presenter.Popup)action).getPopupPresenter());
                        }
                    }
                }
            }
            return super.getPopupMenu();
        }
    }
    
}
