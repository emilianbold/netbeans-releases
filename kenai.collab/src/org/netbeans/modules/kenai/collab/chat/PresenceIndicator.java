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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.Iterator;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.openide.util.ImageUtilities;
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

    public static enum Status {
        ONLINE,
        OFFLINE
    }

    public void setStatus(Status status) {
        label.setIcon(status == Status.ONLINE?ONLINE:OFFLINE);
        if (status==Status.OFFLINE) {
            label.setText("");
            label.setToolTipText("");
        }
    }

    Component getComponent() {
        return label;
    }

    public static synchronized PresenceIndicator getDefault() {
        if (instance == null) {
            instance = new PresenceIndicator();
            if (System.getProperty(("kenai.com.url"), "https://kenai.com").endsWith("testkenai.com")) {
                RequestProcessor.getDefault().post(new Runnable() {
                    public void run() {
                        KenaiConnection.getDefault();
                    }
                });
            }
        }
        return instance;
    }

    
    private PresenceIndicator() {
        label = new JLabel(OFFLINE, JLabel.HORIZONTAL);
        /*
        * TODO: delete this
        */
        if (System.getProperty(("kenai.com.url"), "https://kenai.com").endsWith("testkenai.com")) {
            helper = new MouseL();
            label.addMouseListener(helper);
        }
    }

    void showPopup() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                ToolTipManager.sharedInstance().mouseMoved(new MouseEvent(label, 0, 0, 0, 0, 0, 0, false));
            }
        });
    }

    private class MouseL extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent event) {
            if (event.getClickCount() == 2) {
                ChatTopComponent.openAction(ChatTopComponent.findInstance(), "", "", false).actionPerformed(new ActionEvent(event,event.getID(),"")); // NOI18N
            }
        }
    }

    private static RequestProcessor presenceUpdater = new RequestProcessor();


    public class PresenceListener implements PacketListener {
        private RequestProcessor.Task task;
        public PresenceListener() {
            task = presenceUpdater.create(new Runnable() {

                public void run() {
                    HashSet<String> onlineUsers = new HashSet<String>();
                    StringBuffer tipBuffer = new StringBuffer();
                    tipBuffer.append("<html><body>"); // NOI18N

                    for (MultiUserChat muc : KenaiConnection.getDefault().getChats()) {
                        String displayName = null;
                        displayName = StringUtils.parseName(muc.getRoom());
                        tipBuffer.append("<font color=gray>" + displayName + "</font><br>"); // NOI18N
                        Iterator<String> i = muc.getOccupants();
                        ChatNotifications.getDefault().getMessagingHandle(displayName).setOnlineCount(muc.getOccupantsCount());
                        while (i.hasNext()) {
                            String uname = StringUtils.parseResource(i.next());
                            onlineUsers.add(uname);
                            tipBuffer.append("&nbsp;&nbsp;" + uname + "<br>"); // NOI18N
                        }
                    }
                    tipBuffer.append("</body></html>"); // NOI18N
                    if (onlineUsers.size() == 0) {
                        setStatus(Status.OFFLINE);
                    } else {
                        label.setToolTipText(tipBuffer.toString());
                        label.setText(String.valueOf(onlineUsers.size()));
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
