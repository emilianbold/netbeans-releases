/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.kenai.collab.chat;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.prefs.Preferences;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.util.StringUtils;
import org.netbeans.modules.kenai.api.Kenai;
import org.netbeans.modules.kenai.api.KenaiException;
import org.openide.awt.Notification;
import org.openide.awt.NotificationDisplayer;
import org.openide.awt.NotificationDisplayer.Priority;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 *
 * @author Jan Becicka
 */
public class ChatNotifications {
    public static final String NOTIFICATIONS_PREF = "chat.notifications."; // NOI18N
    
    private static ImageIcon NEWMSG = new ImageIcon(ImageUtilities.loadImage("org/netbeans/modules/kenai/collab/resources/newmessage.png"));
    private static ChatNotifications instance;

    private HashMap<String, MessagingHandleImpl> groupMessages = new HashMap<String, MessagingHandleImpl>();
    private HashMap<String, Notification> privateNotifications = new HashMap();
    private Preferences preferences = NbPreferences.forModule(ChatNotifications.class);

    
    private ChatNotifications() {
    }

    public static synchronized ChatNotifications getDefault() {
        if (instance==null) {
            instance = new ChatNotifications();
        }
        return instance;
    }

    /**
     * @param name kenai project name
     */
    public synchronized void removeGroup(final String name) {
        MessagingHandleImpl r = groupMessages.get(name);
        if (r != null) {
            r.disposeNotification();
            r.notifyMessagesRead();
            groupMessages.remove(name);
        }
    }

    /**
     * @param name user short name
     */
    public synchronized void removePrivate(final String name) {
        Notification n = privateNotifications.get(name);
        if (n != null) {
            n.clear();
            privateNotifications.remove(name);
        }
    }


    synchronized void addGroupMessage(final Message msg) {
        assert SwingUtilities.isEventDispatchThread();
        final String chatRoomName = StringUtils.parseName(msg.getFrom());
        final MessagingHandleImpl r = getMessagingHandle(chatRoomName);
        r.notifyMessageReceived(msg);
        String title = null;
        try {
            title = NbBundle.getMessage(ChatTopComponent.class, "LBL_GroupChatNotification", new Object[]{Kenai.getDefault().getProject(chatRoomName).getDisplayName(), r.getNewMessageCount()});
            final String description = NbBundle.getMessage(ChatTopComponent.class, "LBL_ReadIt");

            final ActionListener l = new ActionListener() {

                public void actionPerformed(ActionEvent arg0) {
                    final ChatTopComponent chatTc = ChatTopComponent.findInstance();
                    ChatTopComponent.openAction(chatTc, "", "", false).actionPerformed(arg0); // NOI18N
                    chatTc.setActiveGroup(chatRoomName);
                }
            };

            if (r.getNewMessageCount()>0) {
                Notification n = NotificationDisplayer.getDefault().notify(title, getIcon(), description, l, Priority.NORMAL);
                r.updateNotification(n);
            }
        } catch (KenaiException ex) {
            Exceptions.printStackTrace(ex);
        }
        ChatTopComponent.findInstance().repaint();
    }

    synchronized void addPrivateMessage(final Message msg) {
        assert SwingUtilities.isEventDispatchThread();
        final String name = StringUtils.parseName(msg.getFrom());
        Notification n = privateNotifications.get(name);
        if (n != null) {
            n.clear();
            privateNotifications.remove(name);
        }
        n = NotificationDisplayer.getDefault().notify("New Message", getIcon(), "New Message from " + name, new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                ChatTopComponent tc = ChatTopComponent.findInstance();
                tc.open();
                tc.setActivePrivate(name);
            }
        }, Priority.NORMAL);
        privateNotifications.put(name, n);
        ChatTopComponent.findInstance().repaint();
    }

    public synchronized  MessagingHandleImpl getMessagingHandle(String id) {
        MessagingHandleImpl handle=groupMessages.get(id);
        if (handle==null) {
            handle =new MessagingHandleImpl(id);
            groupMessages.put(id, handle);
            handle.setMessageCount(0);
        }
        return handle;
    }

    synchronized void clearAll() {
        for (MessagingHandleImpl h:groupMessages.values()) {
            h.disposeNotification();
            h.setMessageCount(-1);
            h.setOnlineCount(-1);
        }
        groupMessages.clear();
    }

    boolean isEnabled(String name) {
        assert name!=null;
        return preferences.getBoolean(NOTIFICATIONS_PREF + name, true);
    }

    void setEnabled(String name, boolean b) {
        assert name!=null;
        preferences.putBoolean(NOTIFICATIONS_PREF + name, b);
    }
    
    private Icon getIcon() {
        return NEWMSG;
    }
}

