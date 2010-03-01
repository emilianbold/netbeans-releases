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
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.prefs.Preferences;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.util.StringUtils;
import org.netbeans.modules.kenai.api.Kenai;
import org.netbeans.modules.kenai.api.KenaiProject;
import org.netbeans.modules.kenai.ui.Utilities;
import org.netbeans.modules.kenai.ui.dashboard.DashboardImpl;
import org.openide.awt.Notification;
import org.openide.awt.NotificationDisplayer;
import org.openide.awt.NotificationDisplayer.Priority;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 *
 * @author Jan Becicka
 */
public class ChatNotifications {
    public static final String NOTIFICATIONS_PREF = "chat.notifications."; // NOI18N
    
    private static ImageIcon NEWMSG = ImageUtilities.loadImageIcon("org/netbeans/modules/kenai/collab/resources/newmessage.png", true); // NOI18N
    private static ChatNotifications instance;

    //key is FQN (e.g. anagram-game@muc.kenai.com)
    private HashMap<String, MessagingHandleImpl> groupMessages = new HashMap<String, MessagingHandleImpl>();
    //key is FQN (e.g. john@kenai.com)
    private HashMap<String, Notification> privateNotifications = new HashMap();
    //key is FQN (e.g. john@kenai.com)
    private HashMap<String, Integer> privateMessagesCounter = new HashMap();
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
        Utilities.assertJid(name);
        MessagingHandleImpl r = groupMessages.get(name);
        if (r != null) {
            r.disposeNotification();
            r.notifyMessagesRead();
            //groupMessages.remove(name);
        }
    }

    /**
     * @param name user short name
     */
    public synchronized void removePrivate(final String name) {
        Utilities.assertJid(name);
        Notification n = privateNotifications.get(name);
        if (n != null) {
            n.clear();
            privateNotifications.remove(name);
            privateMessagesCounter.remove(name);
        }
    }


    synchronized void addGroupMessage(final Message msg) {
        assert SwingUtilities.isEventDispatchThread();
        KenaiProject prj = KenaiConnection.getKenaiProject(StringUtils.parseBareAddress(msg.getFrom()));
        final MessagingHandleImpl r = getMessagingHandle(prj);
        r.notifyMessageReceived(msg);
        String title = null;
        int count = r.getMessageCount();
        if (count==1) {
            title = NbBundle.getMessage(ChatTopComponent.class, "LBL_ChatNotification", new Object[]{prj.getDisplayName(), count});
        } else {
            title = NbBundle.getMessage(ChatTopComponent.class, "LBL_ChatNotifications", new Object[]{prj.getDisplayName(), count});
        }
            final String description = NbBundle.getMessage(ChatTopComponent.class, "LBL_ReadIt");

            final ActionListener l = new ActionListener() {

                public void actionPerformed(ActionEvent arg0) {
                    final ChatTopComponent chatTc = ChatTopComponent.findInstance();
                    ChatTopComponent.openAction(chatTc, "", "", false).actionPerformed(arg0); // NOI18N
                    chatTc.setActiveGroup(StringUtils.parseBareAddress(msg.getFrom()));
                }
            };

            if (r.getMessageCount()>0) {
                Notification n = NotificationDisplayer.getDefault().notify(title, getIcon(), description, l, Priority.NORMAL);
                r.updateNotification(n);
            }
        ChatTopComponent.refreshContactList();
    }

    synchronized void addPrivateMessage(final Message msg) {
        assert SwingUtilities.isEventDispatchThread();
        final String name = StringUtils.parseBareAddress(msg.getFrom());
        Notification n = privateNotifications.get(name);
        if (n != null) {
            n.clear();
            privateNotifications.remove(name);
        }
        increasePrivateMessagesCount(name);
        String title;
        int count = getPrivateMessagesCount(name);
        if (count==1) {
            title = NbBundle.getMessage(ChatTopComponent.class, "LBL_ChatNotification", new Object[]{name, count}); // NOI18N
        } else {
            title = NbBundle.getMessage(ChatTopComponent.class, "LBL_ChatNotifications", new Object[]{name, count}); // NOI18N
        }
        String description = NbBundle.getMessage(ChatTopComponent.class, "LBL_ReadIt"); // NOI18N
        n = NotificationDisplayer.getDefault().notify(title, getIcon(), description, new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                ChatTopComponent tc = ChatTopComponent.findInstance();
                tc.open();
                tc.setActivePrivate(name);
            }
        }, Priority.NORMAL);
        privateNotifications.put(name, n);
        ChatTopComponent.refreshContactList();
        DashboardImpl.getInstance().getComponent().repaint();
    }

    public synchronized boolean hasNewPrivateMessages(String name) {
        Utilities.assertJid(name);
        return privateNotifications.get(name)!=null;
    }

    public synchronized  MessagingHandleImpl getMessagingHandle(KenaiProject prj) {
        //TODO: plain project name will not work for multiple instances
        MessagingHandleImpl handle=groupMessages.get(prj.getName() + "@muc." + prj.getKenai().getUrl().getHost());
        if (handle==null) {
            handle =new MessagingHandleImpl(prj);
            groupMessages.put(prj.getName() + "@muc." + prj.getKenai().getUrl().getHost(), handle);
        }
        return handle;
    }

    synchronized void clearAll(Kenai kenai) {
        String name = "@muc." + kenai.getUrl().getHost();
        Iterator<Entry<String, MessagingHandleImpl>> iterator = groupMessages.entrySet().iterator();
        while (iterator.hasNext()) {
            java.util.Map.Entry<String, MessagingHandleImpl> entry = iterator.next();
            if (entry.getKey().endsWith(name)) {
                MessagingHandleImpl h = entry.getValue();
                h.disposeNotification();
                h.setMessageCount(0);
                h.setOnlineCount(-1);
                iterator.remove();
            }
        }
    }

    boolean isEnabled(String name) {
        Utilities.assertJid(name);
        assert name!=null;
        return preferences.getBoolean(NOTIFICATIONS_PREF + name, true);
    }

    void setEnabled(String name, boolean b) {
        Utilities.assertJid(name);
        assert name!=null;
        preferences.putBoolean(NOTIFICATIONS_PREF + name, b);
    }
    
    private Icon getIcon() {
        return NEWMSG;
    }

    private void increasePrivateMessagesCount(String name) {
        Utilities.assertJid(name);
        privateMessagesCounter.put(name, getPrivateMessagesCount(name)+1);
    }

    private int getPrivateMessagesCount(String name) {
        Utilities.assertJid(name);
        Integer count = privateMessagesCounter.get(name);
        if (count==null) {
            return 0;
        }
        return count;
    }
}

