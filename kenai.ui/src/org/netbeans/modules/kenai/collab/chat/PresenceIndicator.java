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
package org.netbeans.modules.kenai.collab.chat;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.PasswordAuthentication;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.border.EmptyBorder;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.netbeans.modules.kenai.api.Kenai;
import org.netbeans.modules.kenai.api.KenaiException;
import org.netbeans.modules.kenai.api.KenaiManager;
import org.netbeans.modules.kenai.api.KenaiUser;
import org.netbeans.modules.kenai.ui.LogoutAction;
import org.netbeans.modules.kenai.ui.Utilities;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;


/**
 * Icon showing online status on lower right corner
 * @author Jan Becicka
 */

public class PresenceIndicator {
    private static final ImageIcon ONLINE = ImageUtilities.loadImageIcon("org/netbeans/modules/kenai/collab/resources/online.png", true); // NOI18N
    private static final ImageIcon OFFLINE = ImageUtilities.loadImageIcon("org/netbeans/modules/kenai/collab/resources/offline.png", true); // NOI18N
    private static PresenceIndicator instance;

    private JLabel label;
    private MouseL helper;

    private void setStatus(Kenai.Status status) {
        label.setIcon(status == Kenai.Status.ONLINE?ONLINE:OFFLINE);
        if (status!=Kenai.Status.ONLINE) {
            label.setText(""); // NOI18N
        }
        switch (status) {
            case OFFLINE:
                label.setToolTipText(NbBundle.getMessage(PresenceIndicator.class, "LBL_Offline_Tooltip")); // NOI18N
                break;
            case LOGGED_IN:
                label.setToolTipText(NbBundle.getMessage(PresenceIndicator.class, "LBL_LoggedInButNotOnChat_Tooltip")); // NOI18N
                break;
            case ONLINE:
                label.setToolTipText(NbBundle.getMessage(PresenceIndicator.class, "LBL_LoggedIn_Tooltip", KenaiUser.getOnlineUserCount()>0?KenaiUser.getOnlineUserCount()-1:"")); // NOI18N
                break;
        }
            label.setVisible(status!=Kenai.Status.OFFLINE);
    }

    private static Kenai.Status getKenaiStatus() {
        Kenai.Status s = Kenai.Status.OFFLINE;
        for (Kenai k: KenaiManager.getDefault().getKenais()) {
            if (k.getStatus() == Kenai.Status.ONLINE)
                return Kenai.Status.ONLINE;
            else if (k.getStatus() == Kenai.Status.LOGGED_IN) {
                s = Kenai.Status.LOGGED_IN;
            }
        }
        return s;
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
        label = new JLabel(); //OFFLINE, JLabel.HORIZONTAL);
        label.setVisible(false);
        label.setBorder(new EmptyBorder(0, 5, 0, 5));
//        label.setToolTipText(NbBundle.getMessage(PresenceIndicator.class, "LBL_Offline")); // NOI18N
        helper = new MouseL();
        label.addMouseListener(helper);
    }

    private boolean inited = false;
    public synchronized void init() {
        if (inited) {
            return;
        }
        KenaiManager.getDefault().addPropertyChangeListener(new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent evt) {
                setStatus(getKenaiStatus());
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
        public void mouseClicked(MouseEvent event) {
              Kenai.Status s = getKenaiStatus();
            if (event.getClickCount() == 2) {
                if (s == Kenai.Status.ONLINE) {
                    ChatTopComponent.openAction(ChatTopComponent.findInstance(), "", "", false).actionPerformed(new ActionEvent(event, event.getID(), "")); // NOI18N
                }
            } else {
                if (s!=Kenai.Status.OFFLINE) {
                    JPopupMenu menu = new JPopupMenu();
                    final JMenuItem contactListMenu = new JMenuItem(NbBundle.getMessage(PresenceIndicator.class, "CTL_WhoIsOnlineAction"));
                    menu.add(contactListMenu);
                    contactListMenu.setEnabled(s==Kenai.Status.ONLINE);
                    final JCheckBoxMenuItem onlineCheckBox = new JCheckBoxMenuItem(NbBundle.getMessage(PresenceIndicator.class, "CTL_OnlineCheckboxMenuItem"),s==Kenai.Status.ONLINE); // NOI18N
                    menu.add(onlineCheckBox);

                    for (final Kenai k: KenaiManager.getDefault().getKenais()) {
                        if (k.getStatus()==Kenai.Status.OFFLINE)
                            continue;
                        JMenu m = new JMenu(k.getName());
                        final JCheckBoxMenuItem onlineCheckBoxForKenai = new JCheckBoxMenuItem(NbBundle.getMessage(PresenceIndicator.class, "CTL_OnlineCheckboxMenuItem"),k.getStatus()==Kenai.Status.ONLINE); // NOI18N
                        m.add(onlineCheckBoxForKenai);
                        final JMenuItem logoutItem = new JMenuItem(NbBundle.getMessage(PresenceIndicator.class, "CTL_LogoutMenuItem")); // NOI18N
                        m.add(logoutItem);
                        menu.add(m);

                        onlineCheckBoxForKenai.setEnabled(Utilities.isChatSupported(k));
                        onlineCheckBoxForKenai.addActionListener(new ActionListener() {

                            @Override
                            public void actionPerformed(ActionEvent e) {
                                try {
                                    PasswordAuthentication passwordAuthentication = k.getPasswordAuthentication();
                                    if (passwordAuthentication != null) {
                                        k.login(passwordAuthentication.getUserName(), passwordAuthentication.getPassword(), onlineCheckBoxForKenai.isSelected());
                                    }
                                } catch (KenaiException ex) {
                                    Exceptions.printStackTrace(ex);
                                }
                            }
                        });

                        logoutItem.addActionListener(new ActionListener() {

                            public void actionPerformed(ActionEvent e) {
                                RequestProcessor.getDefault().post(new Runnable() {

                                    @Override
                                    public void run() {
                                        k.logout();
                                    }
                                });
                            }
                        });
                    }
                    final JMenuItem logoutItem = new JMenuItem(NbBundle.getMessage(PresenceIndicator.class, "CTL_LogoutMenuItem")); // NOI18N
                    menu.add(logoutItem);
                    onlineCheckBox.addActionListener(new ActionListener() {

                        public void actionPerformed(ActionEvent e) {
                            RequestProcessor.getDefault().post(new Runnable() {

                                public void run() {
                                    try {
                                        for(Kenai kenai: KenaiManager.getDefault().getKenais()) {
                                            PasswordAuthentication passwordAuthentication = kenai.getPasswordAuthentication();
                                            if (passwordAuthentication!=null && Utilities.isChatSupported(kenai)) {
                                                kenai.login(passwordAuthentication.getUserName(), passwordAuthentication.getPassword(), onlineCheckBox.isSelected());
                                            }
                                        }
                                    } catch (KenaiException ex) {
                                        Exceptions.printStackTrace(ex);
                                    }
                                }
                            });
                        }
                    });
                    logoutItem.addActionListener(new ActionListener() {

                        public void actionPerformed(ActionEvent e) {
                            LogoutAction.getDefault().actionPerformed(e);
                        }
                    });

                    contactListMenu.addActionListener(new WhoIsOnlineAction());
                    menu.show(label, event.getPoint().x, event.getPoint().y);
                }
            }
        }
    }

    public static class PresenceListener implements PacketListener {
        /**
         * @param packet
         */
        public void processPacket(Packet packet) {
            PresenceIndicator.getDefault().label.setText(KenaiUser.getOnlineUserCount()>0?KenaiUser.getOnlineUserCount()-1+"":""); // NOI18N
            PresenceIndicator.getDefault().label.setToolTipText(NbBundle.getMessage(PresenceIndicator.class, "LBL_LoggedIn_Tooltip", KenaiUser.getOnlineUserCount()>0?KenaiUser.getOnlineUserCount()-1:""));
            for (MultiUserChat muc : KenaiConnection.getDefault(KenaiConnection.getKenai(StringUtils.parseBareAddress(packet.getFrom()))).getChats()) {
                String chatName = StringUtils.parseName(muc.getRoom());
                assert chatName != null : "muc.getRoom() = " + muc.getRoom(); // NOI18N
                ChatNotifications.getDefault().getMessagingHandle(KenaiConnection.getKenaiProject(muc)).setOnlineCount(muc.getOccupantsCount());
            }
        }
    }
}
