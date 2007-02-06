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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.versioning;

import org.openide.awt.DynamicMenuContent;
import org.openide.awt.Mnemonics;
import org.openide.windows.TopComponent;
import org.netbeans.modules.versioning.spi.VersioningSystem;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.versioning.spi.VCSAnnotator;
import org.netbeans.modules.versioning.spi.LocalHistory;
import org.netbeans.modules.versioning.util.Utils;

import javax.swing.*;
import javax.swing.event.MenuListener;
import javax.swing.event.MenuEvent;
import java.awt.event.ActionEvent;
import java.util.*;

/**
 * Top level main Versioninng menu.
 * 
 * @author Maros Sandor
 */
public class VersioningMainMenu extends AbstractAction implements DynamicMenuContent {

    public void actionPerformed(ActionEvent e) {
        // does nothing, this is a popup menu
    }

    public JComponent[] getMenuPresenters() {
        return createMenu();
    }

    public JComponent[] synchMenuPresenters(JComponent[] items) {
        return createMenu();
    }
    
    private JComponent[] createMenu() {
        List<JComponent> items = new ArrayList<JComponent>(20);

        final VCSContext ctx = VCSContext.forNodes(TopComponent.getRegistry().getActivatedNodes());
        List<VersioningSystem> systems = Arrays.asList(VersioningManager.getInstance().getVersioningSystems());
        VersioningSystem [] vs = VersioningManager.getInstance().getOwners(ctx);
        VersioningSystem ownerVS = null;

        if (vs.length == 1) {
            if (vs[0].getVCSAnnotator() != null) {
                ownerVS = vs[0];
                List<JComponent> systemItems = actionsToItems(vs[0].getVCSAnnotator().getActions(ctx, VCSAnnotator.DEST_MAINMENU));
                items.addAll(systemItems);
            }
            items.add(new JSeparator());
        } else if (vs.length > 1) {
            JMenuItem dummy = new JMenuItem("<multiple systems>");
            dummy.setEnabled(false);
            items.add(dummy);
            items.add(new JSeparator());
        }
        
        Collections.sort(systems, new Comparator<VersioningSystem>() {
            public int compare(VersioningSystem a, VersioningSystem b) {
                return a.getDisplayName().compareTo(b.getDisplayName());
            }
        });

        VersioningSystem localHistory = null;
        for (final VersioningSystem system : systems) {
            if (system instanceof LocalHistory) {
                localHistory = system;
            } else {
                JMenu menu = createVersioningSystemMenu(system, ctx);
                if (system == ownerVS) {
                    menu.setEnabled(false);
                }
                items.add(menu);
            }
        }
        
        if (localHistory != null) {
            items.add(new JSeparator());
            items.add(createVersioningSystemMenu(localHistory, ctx));
        }

        return items.toArray(new JComponent[items.size()]);
    }

    private JMenu createVersioningSystemMenu(final VersioningSystem system, final VCSContext ctx) {
        final JMenu menu = new JMenu();
        Mnemonics.setLocalizedText(menu, "&" + system.getDisplayName());
        menu.addMenuListener(new MenuListener() {
            public void menuSelected(MenuEvent e) {
                if (menu.getItemCount() != 0) return;
                constructMenu(menu, system, ctx);
            }
    
            public void menuDeselected(MenuEvent e) {
            }
    
            public void menuCanceled(MenuEvent e) {
            }
        });
        return menu;
    }

    private void constructMenu(JMenu menu, VersioningSystem system, VCSContext ctx) {
        if (system.getVCSAnnotator() != null) {
            List<JComponent> systemItems = actionsToItems(system.getVCSAnnotator().getActions(ctx, VCSAnnotator.DEST_MAINMENU));
            for (JComponent systemItem : systemItems) {
                menu.add(systemItem);
            }
        }
    }

    private static List<JComponent> actionsToItems(Action[] actions) {
        List<JComponent> items = new ArrayList<JComponent>(actions.length);
        for (Action action : actions) {
            if (action == null) {
                items.add(new JSeparator());
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
}
