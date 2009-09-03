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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.PasswordAuthentication;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.border.EmptyBorder;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.netbeans.modules.kenai.api.Kenai;
import org.netbeans.modules.kenai.api.KenaiException;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;


/**
 * Icon showing online status on lower right corner
 * @author Jan Becicka
 */

public class PresenceIndicator {
    private static ImageIcon ONLINE = new ImageIcon(ImageUtilities.loadImage("org/netbeans/modules/kenai/collab/resources/online.png"));
    private static ImageIcon OFFLINE = new ImageIcon(ImageUtilities.loadImage("org/netbeans/modules/kenai/collab/resources/offline.png"));
    private static PresenceIndicator instance;

    private JLabel label;
    private MouseL helper;

    public void setStatus(Kenai.Status status) {
        label.setIcon(status == Kenai.Status.ONLINE?ONLINE:OFFLINE);
        if (status==Kenai.Status.OFFLINE) {
            label.setText("");
            label.setToolTipText(NbBundle.getMessage(PresenceIndicator.class, "LBL_Offline"));
            label.setVisible(false);
        } else {
            label.setVisible(true);
        }
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
        label = new JLabel(OFFLINE, JLabel.HORIZONTAL);
        label.setVisible(false);
        label.setBorder(new EmptyBorder(0, 5, 0, 5));
        label.setToolTipText(NbBundle.getMessage(PresenceIndicator.class, "LBL_Offline"));
        helper = new MouseL();
        label.addMouseListener(helper);
        Kenai.getDefault().addPropertyChangeListener(new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent evt) {
                setStatus(Kenai.getDefault().getStatus());
            }
        });
    }

    void showPopup() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                ToolTipManager tooltipManager = ToolTipManager.sharedInstance();
                int initialDelay = tooltipManager.getInitialDelay();
                tooltipManager.setInitialDelay(0);
                tooltipManager.mouseMoved(new MouseEvent(label, 0, 0, 0, 0, 0, 0, false));
                tooltipManager.setInitialDelay(initialDelay);
            }
        });
    }

    private class MouseL extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent event) {
              Kenai.Status s = Kenai.getDefault().getStatus();
            if (event.getClickCount() == 2) {
                if (s == Kenai.Status.ONLINE) {
                    ChatTopComponent.openAction(ChatTopComponent.findInstance(), "", "", false).actionPerformed(new ActionEvent(event, event.getID(), "")); // NOI18N
                }
            } else {
                if (s!=Kenai.Status.OFFLINE) {
                    JPopupMenu menu = new JPopupMenu();
                    final JCheckBoxMenuItem onlineCheckBox = new JCheckBoxMenuItem("Online On Chat",s==Kenai.Status.ONLINE);
                    menu.add(onlineCheckBox);
                    final JMenuItem logoutItem = new JMenuItem("Logout from Kenai");
                    menu.add(logoutItem);
                    final Kenai kenai = Kenai.getDefault();
                    onlineCheckBox.addActionListener(new ActionListener() {

                        public void actionPerformed(ActionEvent e) {
                            RequestProcessor.getDefault().post(new Runnable() {

                                public void run() {
                                    try {
                                        PasswordAuthentication passwordAuthentication = kenai.getPasswordAuthentication();
                                        kenai.login(passwordAuthentication.getUserName(), passwordAuthentication.getPassword(), onlineCheckBox.isSelected());
                                    } catch (KenaiException ex) {
                                        Exceptions.printStackTrace(ex);
                                    }
                                }
                            });
                        }
                    });
                    logoutItem.addActionListener(new ActionListener() {

                        public void actionPerformed(ActionEvent e) {
                            kenai.logout();
                        }
                    });
                    menu.show(label, 0, 0);
                }
            }
        }
    }

    private static RequestProcessor presenceUpdater = new RequestProcessor();


    public static class PresenceListener implements PacketListener {
        private RequestProcessor.Task task;
        public PresenceListener() {
            task = presenceUpdater.create(new Runnable() {

                private String getDisplayName(MultiUserChat muc) {
                       String chatName = StringUtils.parseName(muc.getRoom());
                        try {
                            String displayName = Kenai.getDefault().getProject(chatName).getDisplayName();
                            return displayName;
                        } catch (KenaiException kenaiException) {
                            return chatName;
                        }
                }

                public void run() {
                    HashSet<String> onlineUsers = new HashSet<String>();
                    StringBuffer tipBuffer = new StringBuffer();
                    tipBuffer.append("<html><body>"); // NOI18N

                    List<MultiUserChat> chats = KenaiConnection.getDefault().getChats();
                    Collections.sort(chats, new Comparator() {
                        public int compare(Object o1, Object o2) {
                            return getDisplayName((MultiUserChat) o1).compareTo(getDisplayName((MultiUserChat)o2));
                        }
                    });
                    for (MultiUserChat muc : chats) {
                        String chatName = StringUtils.parseName(muc.getRoom());
                        assert chatName!=null: "muc.getRoom() = " + muc.getRoom();
                        tipBuffer.append("<font color=gray>" + getDisplayName(muc) + "</font><br>"); // NOI18N
                        Iterator<String> i = muc.getOccupants();
                        ArrayList<String> occupants = new ArrayList<String>();
                        ChatNotifications.getDefault().getMessagingHandle(chatName).setOnlineCount(muc.getOccupantsCount());
                        while (i.hasNext()) {
                            String uname = StringUtils.parseResource(i.next());
                            occupants.add(uname);
                        }
                        Collections.sort(occupants);
                        for (String uname:occupants) {
                            onlineUsers.add(uname);
                            tipBuffer.append("&nbsp;&nbsp;" + uname + "<br>"); // NOI18N
                        }
                    }
                    tipBuffer.append("</body></html>"); // NOI18N
                    if (onlineUsers.size() == 0) {
                        PresenceIndicator.getDefault().setStatus(Kenai.Status.OFFLINE);
                    } else {
                        PresenceIndicator.getDefault().label.setToolTipText(tipBuffer.toString());
                        PresenceIndicator.getDefault().label.setText(String.valueOf(onlineUsers.size()));
                    }

                }
            });
        }

        /**
         * @param packet
         */
        public void processPacket(Packet packet) {
            task.schedule(100);
        }
    }
}
