/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
package org.netbeans.modules.versioning;

import org.openide.awt.DynamicMenuContent;
import org.openide.awt.Mnemonics;
import org.openide.windows.TopComponent;
import org.netbeans.modules.versioning.spi.VersioningSystem;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.versioning.spi.VCSAnnotator;

import javax.swing.*;
import javax.swing.event.MenuListener;
import javax.swing.event.MenuEvent;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.*;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 * Top level main Versioninng menu.
 * 
 * @author Maros Sandor
 */
public class VersioningMainMenu extends AbstractAction implements DynamicMenuContent {

    @Override
    public void actionPerformed(ActionEvent e) {
        // does nothing, this is a popup menu
    }

    @Override
    public JComponent[] getMenuPresenters() {
        return createMenu();
    }

    @Override
    public JComponent[] synchMenuPresenters(JComponent[] items) {
        return createMenu();
    }
    
    private JComponent[] createMenu() {
        List<JComponent> items = new ArrayList<JComponent>(20);

        if(!VersioningManager.isInitialized()) {
            items.add(InitMenuItem.create(NbBundle.getMessage(VersioningMainMenu.class, "CTL_MenuItem_VersioningMenu")));
            items.add(Utils.createJSeparator());
            items.add(InitMenuItem.create(NbBundle.getMessage(VersioningMainMenu.class, "CTL_MenuItem_LocalHistory")));
        } else {
        
            final VCSContext ctx = VCSContext.forNodes(TopComponent.getRegistry().getActivatedNodes());
            List<VersioningSystem> systems = Arrays.asList(VersioningManager.getInstance().getVersioningSystems());
            VersioningSystem [] vs = VersioningManager.getInstance().getOwners(ctx);

            if (vs.length == 1) {
                if (vs[0].getVCSAnnotator() != null) {
                    List<JComponent> systemItems = actionsToItems(vs[0].getVCSAnnotator().getActions(ctx, VCSAnnotator.ActionDestination.MainMenu));
                    items.addAll(systemItems);
                }
                items.addAll(actionsToItems(appendAdditionalActions(ctx, vs[0], new Action[0])));
                items.add(Utils.createJSeparator());
            } else if (vs.length > 1) {
                JMenuItem dummy = new JMenuItem("<multiple systems>");
                dummy.setEnabled(false);
                items.add(dummy);
                items.add(Utils.createJSeparator());
            }

            Collections.sort(systems, new ByDisplayNameComparator());

            VersioningSystem localHistory = null;
            for (final VersioningSystem system : systems) {
                if (Utils.isLocalHistory(system)) {
                    localHistory = system;
                } else {
                    JMenu menu = createVersioningSystemMenu(system, true);
                    items.add(menu);
                }
            }

            if (localHistory != null) {
                items.add(Utils.createJSeparator());
                items.add(createVersioningSystemMenu(localHistory, false));
            }
        }
        return items.toArray(new JComponent[items.size()]);
    }

    private JMenu createVersioningSystemMenu(final VersioningSystem system, final boolean isRegularVCS) {
        final JMenu menu = new JMenu();
        Mnemonics.setLocalizedText(menu, Utils.getMenuLabel(system));
        menu.addMenuListener(new MenuListener() {
            @Override
            public void menuSelected(MenuEvent e) {
                if (menu.getItemCount() != 0) return;
                // context should be cached while the menu is displayed
                VCSContext ctx = VCSContext.forNodes(TopComponent.getRegistry().getActivatedNodes());
                constructMenu(menu, system, ctx, isRegularVCS);
            }
    
            @Override
            public void menuDeselected(MenuEvent e) {
            }
    
            @Override
            public void menuCanceled(MenuEvent e) {
            }
        });
        return menu;
    }

    private void constructMenu (JMenu menu, VersioningSystem system, VCSContext ctx, boolean isRegularVCS) {
        Action[] actions = null;
        if(system instanceof DelegatingVCS) {
            actions = ((DelegatingVCS)system).getActions(ctx, VCSAnnotator.ActionDestination.MainMenu);
        } else {
            if (system.getVCSAnnotator() != null) {
                actions = system.getVCSAnnotator().getActions(ctx, VCSAnnotator.ActionDestination.MainMenu);
            }
        }
        if (isRegularVCS) {
            actions = appendAdditionalActions(ctx, system, actions);
        }
        if(actions != null && actions.length > 0) {
            List<JComponent> systemItems = actionsToItems(actions);
            for (JComponent systemItem : systemItems) {
                menu.add(systemItem);
            }
        }
    }

    private static List<JComponent> actionsToItems(Action[] actions) {
        List<JComponent> items = new ArrayList<JComponent>(actions.length);
        for (Action action : actions) {
            if (action == null) {
                items.add(Utils.createJSeparator());
            } else {
                if (action instanceof DynamicMenuContent) {
                    DynamicMenuContent dmc = (DynamicMenuContent) action;
                    JComponent [] components = dmc.getMenuPresenters();
                    items.addAll(Arrays.asList(components));
                } else {
                    JMenuItem item = Utils.toMenuItem(action);
                    items.add(item);
                }
            }
        }
        return items;
    }

    static final class ByDisplayNameComparator implements Comparator<VersioningSystem> {
        @Override
        public int compare(VersioningSystem a, VersioningSystem b) {
            return Utils.getDisplayName(a).compareTo(Utils.getDisplayName(b));
        }
    }
    
    static class ConnectAction extends AbstractAction {
        private final File root;
        private final VersioningSystem vs;

        public ConnectAction (VersioningSystem vs, File root, String name) {
            super(name == null ? NbBundle.getMessage(VersioningMainMenu.class, "CTL_ConnectAction.name") : name); //NOI18N
            this.vs = vs;
            this.root = root;
        }

        @Override
        public void actionPerformed (ActionEvent e) {
            VersioningConfig.getDefault().connectRepository(vs, root);
            VersioningManager.getInstance().versionedRootsChanged();
        }
    }

    // should be available only from the main menu
    private static class DisconnectAction extends AbstractAction {
        private final File root;
        private final VersioningSystem vs;

        public DisconnectAction (VersioningSystem vs, File root) {
            super(NbBundle.getMessage(VersioningMainMenu.class, "CTL_DisconnectAction.name")); //NOI18N
            this.vs = vs;
            this.root = root;
        }

        @Override
        public void actionPerformed (ActionEvent e) {
            NotifyDescriptor nd = new NotifyDescriptor.Confirmation(
                    NbBundle.getMessage(VersioningMainMenu.class, "MSG_ConnectAction.confirmation.text", new Object[] { root.getName(), Utils.getDisplayName(vs) }), //NOI18N
                    NbBundle.getMessage(VersioningMainMenu.class, "LBL_ConnectAction.confirmation.title"), //NOI18N
                    NotifyDescriptor.OK_CANCEL_OPTION, NotifyDescriptor.QUESTION_MESSAGE);
            if (DialogDisplayer.getDefault().notify(nd) == NotifyDescriptor.OK_OPTION) {
                VersioningConfig.getDefault().disconnectRepository(vs, root);
                VersioningManager.getInstance().versionedRootsChanged();
            }
        }
    }

    /**
     * appends connect/disconnect actions to given actions
     * @param ctx
     * @param system
     * @param actions initial actions
     * @return enhanced actions
     */
    private Action[] appendAdditionalActions (VCSContext ctx, VersioningSystem system, Action[] actions) {
        if (ctx.getRootFiles().size() == 1) {
            // can connect or disconnect just one root
            File root = system.getTopmostManagedAncestor(ctx.getRootFiles().iterator().next());
            if (root != null) {
                VersioningSystem vs = system instanceof DelegatingVCS ? ((DelegatingVCS) system).getDelegate() : system;
                Action a;
                // adding connect/disconnect actions to the main menu
                if (VersioningConfig.getDefault().isDisconnected(vs, root)) {
                    actions = new Action[] { new ConnectAction(vs, root, null) };
                } else {
                    actions = actions == null ? new Action[2] : Arrays.copyOf(actions, actions.length + 2);
                    actions[actions.length - 2] = null;
                    actions[actions.length - 1] = new DisconnectAction(vs, root);
                }
            }
        }
        return actions;
    }
}
