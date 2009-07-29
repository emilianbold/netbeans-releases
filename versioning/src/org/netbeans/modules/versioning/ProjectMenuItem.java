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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
package org.netbeans.modules.versioning;

import org.openide.util.actions.Presenter;
import org.openide.util.actions.SystemAction;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.nodes.Node;
import org.openide.awt.DynamicMenuContent;
import org.openide.awt.Mnemonics;
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
            Set<VersioningSystem> owners = getOwners(nodes);
            if (owners.size() != 1) {
                return new JComponent[0];
            }
            VersioningSystem owner = owners.iterator().next();
            VersioningSystem localHistory = getLocalHistory(nodes);
            List<JComponent> popups = new ArrayList<JComponent>();            
            if (owner == null || owner.getVCSAnnotator() != null) {
                // prepare a lazy menu, it's items will be properly created at the time the menu is expanded
                JMenu menu = new LazyMenu(nodes, owner);
                popups.add(menu);
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

    private Set<VersioningSystem> getOwners(Node [] nodes) {
        VCSContext ctx = VCSContext.forNodes(nodes);
        Set<VersioningSystem> owners = new HashSet<VersioningSystem>(2);
        for (File file : ctx.getRootFiles()) {
            VersioningSystem fileOwner = VersioningManager.getInstance().getOwner(file);
            owners.add(fileOwner);
        }
        return owners;
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
                items[i++] = Utils.createJSeparator();
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
            final SystemAction sa = (SystemAction) action;
            item = new JMenuItem(new AbstractAction(sa.getName()) {
                public void actionPerformed(ActionEvent e) {
                    sa.actionPerformed(e);
                }
            });
        } else {
            item = new JMenuItem(action);
        }
        Mnemonics.setLocalizedText(item, (String) action.getValue(Action.NAME));
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

    /**
     * Items for this popup menu are created when really needed, that is at the time when the menu is expanded.
     */
    private class LazyMenu extends JMenu {
        private final Node[] nodes;
        private final VersioningSystem owner;
        boolean initialized; // create only once, prevents recreating items when user repeatedly expends and collapses the menu

        private LazyMenu(Node[] nodes, VersioningSystem owner) {
            // owner == null ? 'default versioning menu' : 'specific menu of a versioning system'
            super(owner == null ? NbBundle.getMessage(ProjectMenuItem.class, "CTL_MenuItem_VersioningMenu") : Utils.getDisplayName(owner));
            this.nodes = nodes;
            this.owner = owner;
        }

        @Override
        public JPopupMenu getPopupMenu() {
            if (!initialized) {
                // clear created items
                super.removeAll();
                if (owner == null) {
                    // default Versioning menu (Import into...)
                    Lookup.Result<VersioningSystem> result = Lookup.getDefault().lookup(new Lookup.Template<VersioningSystem>(VersioningSystem.class));
                    List<? extends VersioningSystem> vcs = new ArrayList(result.allInstances());
                    Collections.sort(vcs, new VersioningMainMenu.ByDisplayNameComparator());
                    for (VersioningSystem vs : vcs) {
                        if (vs.getProperty(VersioningSystem.PROP_LOCALHISTORY_VCS) != null) {
                            continue;
                        }
                        addVersioningSystemItems(vs, nodes);
                    }
                    addSeparator();
                    add(createmenuItem(SystemAction.get(PatchAction.class)));
                } else {
                    // specific versioning system menu
                    addVersioningSystemItems(owner, nodes);
                }
                initialized = true;
            }
            return super.getPopupMenu();
        }

        private void addVersioningSystemItems(VersioningSystem owner, Node[] nodes) {
            JComponent[] items = createVersioningSystemItems(owner, nodes);
            if (items != null) {
                for (JComponent item : items) {
                    add(item);
                }
            }
        }
    }
}
