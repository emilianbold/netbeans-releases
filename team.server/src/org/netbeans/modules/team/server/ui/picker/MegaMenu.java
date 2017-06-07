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

package org.netbeans.modules.team.server.ui.picker;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import org.netbeans.modules.team.server.ui.common.TeamServerComparator;
import org.netbeans.modules.team.server.ui.spi.TeamServer;
import org.netbeans.modules.team.commons.treelist.ListNode;
import org.netbeans.modules.team.server.api.TeamServerManager;

/**
 *
 * @author S. Aubrecht
 */
public class MegaMenu {

    public interface Invoker {
        JComponent getInvokingComponent();
        TeamServer getSelectedServer();
        ListNode getSelectedNode();
    }

    private static WeakReference<Invoker> invokerRef;

    private static PropertyChangeListener serverManagerListener;

    public static void show(Invoker invoker) {
        SelectionModel selModel = new SelectionModel();
        TeamServer selectedServer = invoker.getSelectedServer();
        ListNode selectedProjectNode = invoker.getSelectedNode();
        if (selectedServer != null && selectedProjectNode != null) {
            selModel.setInitialSelection(selectedProjectNode);
        } else {
            selectedServer = null;
        }

        if( isShowing() ) {
            PopupWindow.hidePopup();
        }
        JPanel content = new JPanel( new BorderLayout() );

        List<JComponent> serverPanels = new ArrayList<>(5);
        JComponent selectedComponent = null;
        TeamServerManager serverManager = TeamServerManager.getDefault();
        List<TeamServer> servers = new ArrayList<>(serverManager.getTeamServers());
        Collections.sort(servers, new TeamServerComparator());
        for (TeamServer server : servers) {
            JComponent c = ServerPanel.create( server, selModel );
            if(server == selectedServer) {
                selectedComponent = c;
            }
            serverPanels.add( c );
        }
        
        content.add( ServersContainer.create( serverPanels, selectedComponent ), BorderLayout.CENTER );
        
        LookAndFeel.installProperty(content, "opaque", Boolean.TRUE); //NOI18N
        LookAndFeel.installBorder(content, "PopupMenu.border"); //NOI18N
        LookAndFeel.installColorsAndFont(content,
                                         "PopupMenu.background", //NOI18N
                                         "PopupMenu.foreground", //NOI18N
                                         "PopupMenu.font"); //NOI18N

        MegaMenu.invokerRef = new WeakReference<>(invoker);

        PopupWindow.showPopup(content, invoker.getInvokingComponent());
        
        if(serverManagerListener == null) {
            serverManagerListener = new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if(TeamServerManager.PROP_INSTANCES.equals(evt.getPropertyName())) {
                        if(TeamServerManager.getDefault().getTeamServers().isEmpty()) {
                            hide();
                        } else {
                            showAgain();
                        }
                    }
                }
            };
            serverManager.addPropertyChangeListener(serverManagerListener);
        }
    }
 
    public static boolean isShowing() {
        return PopupWindow.isShowing();
    }
    
    public static void showAgain() {
        if (PopupWindow.isShowing()) {
            Runnable hideShow = new Runnable() {
                @Override
                public void run() {
                    if (PopupWindow.isShowing()) {
                        PopupWindow.hidePopup();
                        SwingUtilities.invokeLater(this);
                    } else {
                        Invoker invoker = MegaMenu.invokerRef != null ? MegaMenu.invokerRef.get() : null;
                        if (invoker != null && invoker.getInvokingComponent().isShowing()) {
                            show(invoker);
                        }
                    }
                }
            };
            SwingUtilities.invokeLater(hideShow);
        }
    }

    public static void hide() {
        PopupWindow.hidePopup();
        invokerRef = null;
    }

    static ListNode getPreSelectedProject() {
        Invoker invoker = invokerRef != null ? invokerRef.get() : null;
        return invoker != null ? invoker.getSelectedNode() : null;
    }    
}
