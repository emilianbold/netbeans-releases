/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

        if (vs.length == 1) {
            if (vs[0].getVCSAnnotator() != null) {
                List<JComponent> systemItems = actionsToItems(vs[0].getVCSAnnotator().getActions(ctx, VCSAnnotator.ActionDestination.MainMenu));
                items.addAll(systemItems);
            }
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
                JMenu menu = createVersioningSystemMenu(system, ctx);
                items.add(menu);
            }
        }
        
        if (localHistory != null) {
            items.add(Utils.createJSeparator());
            items.add(createVersioningSystemMenu(localHistory, ctx));
        }

        return items.toArray(new JComponent[items.size()]);
    }

    private JMenu createVersioningSystemMenu(final VersioningSystem system, final VCSContext ctx) {
        final JMenu menu = new JMenu();
        Mnemonics.setLocalizedText(menu, Utils.getMenuLabel(system));
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
            List<JComponent> systemItems = actionsToItems(system.getVCSAnnotator().getActions(ctx, VCSAnnotator.ActionDestination.MainMenu));
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
        public int compare(VersioningSystem a, VersioningSystem b) {
            return Utils.getDisplayName(a).compareTo(Utils.getDisplayName(b));
        }
    }
}
