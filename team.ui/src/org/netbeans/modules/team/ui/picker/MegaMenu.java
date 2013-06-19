/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.team.ui.picker;

import java.awt.BorderLayout;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.LookAndFeel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.team.ui.TeamServerManager;
import org.netbeans.modules.team.ui.common.TeamServerComparator;
import org.netbeans.modules.team.ui.spi.TeamServer;
import org.netbeans.modules.team.ui.util.treelist.ListNode;

/**
 *
 * @author S. Aubrecht
 */
public class MegaMenu {

    private final SelectionModel selModel = new SelectionModel();
    private JComponent invoker;
    private final TeamServerManager serverManager = TeamServerManager.getDefault();

    private static WeakReference<MegaMenu> current;
    private TeamServer selectedServer;

    private MegaMenu() {
    }

    public static MegaMenu create() {
        return new MegaMenu();
    }

    public void show( JComponent invoker ) {
        if( PopupWindow.isShowing() )
            PopupWindow.hidePopup();
        this.invoker = invoker;
        JPanel content = new JPanel( new BorderLayout() );

        List<JComponent> serverPanels = new ArrayList<JComponent>( 3 );
        for( TeamServer server : getServers() ) {
            JComponent c = ServerPanel.create( server, selModel );
            serverPanels.add( c );
        }

        content.add( ServersContainer.create( serverPanels ), BorderLayout.CENTER );

        LookAndFeel.installProperty(content, "opaque", Boolean.TRUE); //NOI18N
        LookAndFeel.installBorder(content, "PopupMenu.border"); //NOI18N
        LookAndFeel.installColorsAndFont(content,
                                         "PopupMenu.background", //NOI18N
                                         "PopupMenu.foreground", //NOI18N
                                         "PopupMenu.font"); //NOI18N

        current = new WeakReference<MegaMenu>( this );  

        PopupWindow.showPopup( content, invoker );
        selModel.addChangeListener( new ChangeListener() {

            @Override
            public void stateChanged( ChangeEvent e ) {
                PopupWindow.hidePopup();
                selModel.removeChangeListener( this );

                //TODO process the new selection
            }
        });
    }
 
    public static MegaMenu getCurrent() {
        return current.get();
    }

    void showAgain() {
        if( null != invoker && invoker.isShowing() ) {
            PopupWindow.hidePopup();
            show( invoker );
        }
    }

    public void setInitialSelection( TeamServer server, ListNode selNode ) {
        selModel.setInitialSelection( selNode );
        if( selNode != null ) {
            this.selectedServer = server;
        }
    }

    // XXX persist and do not hold in static
    private static List<TeamServer> servers;
    private Collection<TeamServer> getServers() {
        List<TeamServer> currentServers = new ArrayList<TeamServer>(serverManager.getTeamServers());
        Collections.sort(currentServers, new TeamServerComparator());
        if(servers == null) {
            servers = currentServers;
        }         
        if(selectedServer != null) {
            for (int i = 0; i < servers.size(); i++) {
                TeamServer teamServer = servers.get(i);
                if(teamServer == selectedServer) {
                    if( i == 0) {
                        break;
                    } 
                    servers.add(0, servers.remove(i));
//                    if(i > 1) {
//                        servers.add(i, servers.remove(1));
//                    }
                    break;
                }
            }
        }
        servers.retainAll(currentServers);
        for (TeamServer teamServer : currentServers) {
            if(!servers.contains(teamServer)) {
                servers.add(teamServer);
            }
        }
        
        return servers;
    }

    public void hide() {
        PopupWindow.hidePopup();
    }
}
