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
import javax.swing.Icon;
import javax.swing.SwingUtilities;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.util.StringUtils;
import org.netbeans.modules.kenai.api.Kenai;
import org.netbeans.modules.kenai.api.KenaiException;
import org.openide.awt.Notification;
import org.openide.awt.NotificationDisplayer;
import org.openide.awt.NotificationDisplayer.Priority;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Becicka
 */
public class ChatNotifications {

    private static ChatNotifications instance;

    private HashMap<String, Room> groupMessages = new HashMap<String, Room>();
    
    private ChatNotifications() {
    }

    public static synchronized ChatNotifications getDefault() {
        if (instance==null) {
            instance = new ChatNotifications();
        }
        return instance;
    }

    public void removeGroup(String name) {
        Room r=groupMessages.get(name);
        if (r!=null) {
            r.notification.dispose();
            groupMessages.remove(r);
        }
    }

    void addGroupMessage(Message msg) {
        assert !SwingUtilities.isEventDispatchThread();
        final String chatRoomName = StringUtils.parseName(msg.getFrom());
        Room r = groupMessages.get(chatRoomName);
        final int count;
        if (r!=null) {
            count = r.msgCount+1;
            r.notification.dispose();
        } else {
            count=1;
        }
        String t=null;
        try {
            t = NbBundle.getMessage(ChatTopComponent.class, "LBL_GroupChatNotification",new Object[]{Kenai.getDefault().getProject(chatRoomName).getDisplayName(), count});
        } catch (KenaiException ex) {
            Exceptions.printStackTrace(ex);
        }
        final String title =t;
        
        final String description=NbBundle.getMessage(ChatTopComponent.class, "LBL_ReadIt");
        
        final ActionListener l = new ActionListener() {

            public void actionPerformed(ActionEvent arg0) {
                final ChatTopComponent chatTC = ChatTopComponent.getDefault();
                chatTC.open();
                chatTC.requestActive();
                chatTC.setActive(StringUtils.parseName(chatRoomName));
            }
        };

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                Notification n = NotificationDisplayer.getDefault().notify(title, getIcon(), description, l, Priority.NORMAL);
                groupMessages.put(chatRoomName, new Room(count, n));
            }
        });

    }

    void addPrivateMessage(Message msg) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    int getMessageCountFor(String name) {
        Room room = groupMessages.get(name);
        if (room!=null) {
            return room.msgCount;
        }
        return 0;
    }

    private Icon getIcon() {
        return null;
    }

    private class Room {
        private int msgCount;
        private Notification notification;
        private Room(int count, Notification n) {
            msgCount=count;
            this.notification=n;
        }
    }
}



