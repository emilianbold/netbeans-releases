/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.team.server;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import org.netbeans.modules.team.server.api.TeamServerManager;
import org.netbeans.modules.team.server.ui.common.AddInstanceAction;
import org.netbeans.modules.team.server.ui.common.EditInstanceAction;
import org.netbeans.modules.team.server.ui.common.TeamServerComparator;
import org.netbeans.modules.team.server.ui.common.RemoveInstanceAction;
import org.netbeans.modules.team.server.ui.spi.TeamServer;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.awt.Actions;
import org.openide.awt.DynamicMenuContent;
import org.openide.util.NbBundle;

@NbBundle.Messages({"CTL_TeamServer=Team Server"})
public final class TeamMenuAction extends AbstractAction implements DynamicMenuContent {
    private static final String CATEGORY_TEAM = "Team";
    private static TeamMenuAction inst;

    @ActionID(category = CATEGORY_TEAM, id = "org.netbeans.modules.team.server.ui.TeamMenuAction")
    @ActionRegistration(displayName = "#CTL_TeamServer", lazy = false)
    @ActionReference(path = "Menu/Versioning", position = 180, separatorBefore = 179, separatorAfter = 181)
    public static synchronized TeamMenuAction getDefault () {
        if (inst == null) {
            inst = new TeamMenuAction();
        }
        return inst;
    }
    
    @Override
    public void actionPerformed (ActionEvent e) { }

    @Override
    public JComponent[] getMenuPresenters () {
        return synchMenuPresenters(null);
    }   

    @Override
    public JComponent[] synchMenuPresenters (JComponent[] items) {
        Collection<? extends TeamServer> c = TeamServerManager.getDefault().getTeamServers();
        List<TeamServer> servers = new ArrayList<>(c);
        Collections.sort(servers, new TeamServerComparator());
        
        JMenu menu = new JMenu(Bundle.CTL_TeamServer());
        
        menu.add(createItem(Actions.forID(CATEGORY_TEAM, LoginAction.ID)));
        menu.add(createItem(Actions.forID(CATEGORY_TEAM, LogoutAction.ID)));
        menu.add(new JSeparator());
        
        for (TeamServer server : servers) {
            menu.add(getSubMenu(server));
        }
        
        menu.add(new JSeparator());        
        menu.add(createItem(Actions.forID(CATEGORY_TEAM, AddInstanceAction.ID)));
        
        return new JComponent[] {menu};
    }

    private JMenuItem createItem(Action a) {
        JMenuItem item = new JMenuItem();
        Actions.connect(item, a, false);
        return item;
    }

    private JMenu getSubMenu(final TeamServer server) throws IllegalArgumentException {
        JMenu subMenu = new JMenu(server.getDisplayName());
        for(Action a : server.getTeamMenuActions()) {
            if(a == null) {
                subMenu.addSeparator();
            } else {
                subMenu.add(createItem(a));
            }
        }
        subMenu.addSeparator();
        subMenu.add(createItem(new EditInstanceAction(server)));
        subMenu.add(createItem(new RemoveInstanceAction(server)));
        
        return subMenu;
    }

}
