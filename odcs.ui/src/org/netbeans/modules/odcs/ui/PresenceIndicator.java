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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.odcs.ui;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import org.netbeans.modules.odcs.api.ODCSServer;
import org.netbeans.modules.odcs.api.ODCSManager;
import org.netbeans.modules.odcs.ui.utils.Utils;
import org.openide.awt.Actions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle.Messages;


/**
 * Icon showing online status on lower right corner
 * @author Ondra Vrabec
 */

public class PresenceIndicator {
    private static final String ICON_LOGGED_IN = "org/netbeans/modules/odcs/ui/resources/odcs.png"; //NOI18N
    private static PresenceIndicator instance;

    private JLabel label;
    private MouseL helper;

    @Messages({"LBL_LoggedIn=Logged in Team Server",
        "LBL_Offline_Tooltip=Not Logged in Team Server"})
    private void setLoggedIn (final boolean loggedIn) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (loggedIn) {
                    label.setToolTipText(Bundle.LBL_LoggedIn());
                } else {
                    label.setToolTipText(Bundle.LBL_Offline_Tooltip());
                }
                label.setVisible(loggedIn);
            }
        });
    }

    private static boolean isLoggedIn () {
        boolean loggedIn = false;
        for (ODCSServer k: ODCSManager.getDefault().getServers()) {
            if (k.isLoggedIn()) {
                loggedIn = true;
                break;
            }
        }
        return loggedIn;
    }

    Component getComponent() {
        return label;
    }

    public static synchronized PresenceIndicator getDefault() {
        if (instance == null) {
            instance = new PresenceIndicator();
        }
        return instance;
    }

    
    private PresenceIndicator() {
        label = new JLabel(ImageUtilities.loadImageIcon(ICON_LOGGED_IN, true));
        label.setVisible(false);
        label.setBorder(new EmptyBorder(0, 5, 0, 5));
        helper = new MouseL();
        label.addMouseListener(helper);
    }

    private boolean inited = false;
    public synchronized void init() {
        if (inited) {
            return;
        }
        ODCSManager.getDefault().addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                setLoggedIn(isLoggedIn());
            }
        });
        inited = true;
    }

    private class MouseL extends MouseAdapter {

            private Cursor oldCursor;

            @Override
            public void mouseEntered(MouseEvent e) {
                oldCursor = label.getCursor();
                label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                label.setCursor(oldCursor);
            }
        @Override
        @Messages("CTL_LogoutMenuItem=Logout from Team Server")
        public void mouseClicked(MouseEvent event) {
            if (isLoggedIn()) {
                JPopupMenu menu = new JPopupMenu();
                for (final ODCSServer k: ODCSManager.getDefault().getServers()) {
                    if (!k.isLoggedIn()) {
                        continue;
                    }
                    JMenu m = new JMenu(k.getDisplayName());
                    final JMenuItem logoutItem = new JMenuItem(Bundle.CTL_LogoutMenuItem());
                    m.add(logoutItem);
                    menu.add(m);

                    logoutItem.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            Utils.getRequestProcessor().post(new Runnable() {
                                @Override
                                public void run() {
                                    k.logout();
                                }
                            });
                        }
                    });
                }
                final JMenuItem logoutItem = new JMenuItem(Bundle.CTL_LogoutMenuItem());
                menu.add(logoutItem);
                logoutItem.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        Actions.forID("Team", "org.netbeans.modules.team.ui.LogoutAction").actionPerformed(e);
                    }
                });
                menu.show(label, event.getPoint().x, event.getPoint().y);
            }
        }
    }
}
