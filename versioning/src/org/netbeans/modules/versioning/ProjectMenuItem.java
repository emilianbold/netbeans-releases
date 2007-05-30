/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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
package org.netbeans.modules.versioning;

import org.openide.util.actions.Presenter;
import org.openide.util.actions.SystemAction;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.nodes.Node;
import org.openide.awt.DynamicMenuContent;
import org.openide.awt.Mnemonics;
import org.openide.awt.Actions;
import org.openide.windows.TopComponent;
import org.netbeans.modules.versioning.spi.VersioningSystem;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.versioning.spi.VCSAnnotator;
import org.netbeans.modules.diff.PatchAction;

import javax.swing.*;
import java.io.File;
import java.awt.event.ActionEvent;
import java.util.*;

/**
 * Appears in a project's popup menu.
 * 
 * @author Maros Sandor
 */
public class ProjectMenuItem extends AbstractAction implements Presenter.Popup {

    public JMenuItem getPopupPresenter() {
        return new DynamicDummyItem();
    }
    
    public void actionPerformed(ActionEvent e) {
        // dummy, not used
    }

    private JComponent [] createItems() {
        Node [] nodes = getActivatedNodes();
        if (nodes.length > 0) {
            VersioningSystem owner = getOwner(nodes);
            if (owner == null) {
                return new JComponent[0];
            }
            VersioningSystem localHistory = getLocalHistory(nodes);
            List<JComponent> popups = new ArrayList<JComponent>();            
            if (owner != null) {
                JMenu menu = createVersioningSystemPopup(owner, nodes);
                if (menu != null) {
                    popups.add(menu);
                }
            } else {                
                JMenu vmenu = new JMenu(NbBundle.getMessage(ProjectMenuItem.class, "CTL_MenuItem_VersioningMenu"));                
                Lookup.Result<VersioningSystem> result = Lookup.getDefault().lookup(new Lookup.Template<VersioningSystem>(VersioningSystem.class));
                Collection<? extends VersioningSystem> vcs = result.allInstances();
                for (VersioningSystem vs : vcs) {
                    if (vs == localHistory) continue;
                    JComponent [] items = createVersioningSystemItems(vs, nodes);
                    if (items != null) {
                        for (JComponent item : items) {
                            vmenu.add(item);
                        }
                    }
                }
                vmenu.add(new JSeparator());
                vmenu.add(createmenuItem(SystemAction.get(PatchAction.class)));
                popups.add(vmenu);
            }
            if(localHistory != null) {
                JMenu localHistoryMenu = createVersioningSystemPopup(localHistory, nodes);
                if(localHistoryMenu != null) {
                    popups.add(localHistoryMenu);    
                }                                    
            }
            return popups.toArray(new JComponent[popups.size()]);
        }
        return new JComponent[0];
    }

    private VersioningSystem getLocalHistory(Node [] nodes) {
        VCSContext ctx = VCSContext.forNodes(nodes);
        VersioningSystem owner = null;
        for (File file : ctx.getRootFiles()) {
            VersioningSystem fileOwner = VersioningManager.getInstance().getLocalHistory(file);
            if (owner != null) {
                if (fileOwner != null && fileOwner != owner) return null;
            } else {
                owner = fileOwner;
            }
        }
        return owner;
    }

    private VersioningSystem getOwner(Node [] nodes) {
        VCSContext ctx = VCSContext.forNodes(nodes);
        VersioningSystem owner = null;
        for (File file : ctx.getRootFiles()) {
            VersioningSystem fileOwner = VersioningManager.getInstance().getOwner(file);
            if (owner != null) {
                if (fileOwner != null && fileOwner != owner) return null;
            } else {
                owner = fileOwner;
            }
        }
        return owner;
    }
    
    private JComponent [] createVersioningSystemItems(VersioningSystem owner, Node[] nodes) {
        VCSAnnotator an = owner.getVCSAnnotator();
        if (an == null) return null;
        VCSContext ctx = VCSContext.forNodes(nodes);
        Action [] actions = an.getActions(ctx, VCSAnnotator.ActionDestination.PopupMenu);
        JComponent [] items = new JComponent[actions.length];
        int i = 0;
        for (Action action : actions) {
            if (action == null) {
                items[i++] = new JSeparator();
            } else {
                JMenuItem item = createmenuItem(action);
                items[i++] = item;
            }
        }
        return items;
    }

    private JMenuItem createmenuItem(Action action) {
        JMenuItem item;
        if (action instanceof SystemAction) {
            item = new JMenuItem();
            Actions.connect(item, action);
        } else {
            item = new JMenuItem(action);
            Mnemonics.setLocalizedText(item, (String) action.getValue(Action.NAME));
        }
        return item;
    }

    private JMenu createVersioningSystemPopup(VersioningSystem owner, Node[] nodes) {
        JComponent [] items = createVersioningSystemItems(owner, nodes);
        if (items == null) return null;
        JMenu menu = new JMenu(Utils.getDisplayName(owner));
        for (JComponent item : items) {
            menu.add(item);
        }
        return menu;
    }

    private Node[] getActivatedNodes() {
        return TopComponent.getRegistry().getActivatedNodes();
    }

    private class DynamicDummyItem extends JMenuItem implements DynamicMenuContent {
        public JComponent[] getMenuPresenters() {
            return createItems();
        }

        public JComponent[] synchMenuPresenters(JComponent[] items) {
            return createItems();
        }
    }
}
