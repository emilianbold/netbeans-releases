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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.PasswordAuthentication;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Message.Type;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.netbeans.modules.kenai.api.Kenai;
import org.netbeans.modules.kenai.api.KenaiException;
import org.netbeans.modules.kenai.api.KenaiFeature;
import org.netbeans.modules.kenai.api.KenaiProject;
import org.netbeans.modules.kenai.api.KenaiService;
import org.netbeans.modules.kenai.ui.spi.KenaiUserUI;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 * Class representing connection to kenai xmpp server
 * @author Jan Becicka
 */
public class KenaiConnection implements PropertyChangeListener {

    //Map <kenai project name, message listener>
    private HashMap<String, PacketListener> groupListeners = new HashMap<String, PacketListener>();
    private HashMap<String, PacketListener> privateListeners = new HashMap<String, PacketListener>();
    private XMPPConnection connection;
    //Map <kenai project name, multi user chat>
    final private Map<String, MultiUserChat> groupChats = new HashMap<String, MultiUserChat>();

    //Map <user short name, chat>
    final private Map<String, Chat> privateChats = new HashMap();

    //singleton instance
    private static KenaiConnection instance;

    //just logger
    private static Logger XMPPLOG = Logger.getLogger(KenaiConnection.class.getName());

    // Map<name of kenai project, message queue>
    private HashMap<String, LinkedList<Message>> groupMessageQueue = new HashMap<String, LinkedList<Message>>();

    // Map<name of kenai project, message queue>
    private HashMap<String, LinkedList<Message>> privateMessageQueue = new HashMap<String, LinkedList<Message>>();

    private static ChatNotifications chatNotifications = ChatNotifications.getDefault();

    private final HashSet<String> onlineUsers = new HashSet<String>();


    /**
     * Default singleton instance representing XMPP connection to kenai server
     * @return
     */
    public static synchronized KenaiConnection getDefault() {
        if (instance == null) {
            instance = new KenaiConnection();
            ProviderManager providerManager = ProviderManager.getInstance();
            providerManager.addExtensionProvider("delay", "urn:xmpp:delay", new DelayExtensionProvider());//NOI18N
            providerManager.addExtensionProvider("notification", "jabber:client", new NotificationExtensionProvider());
            Kenai.getDefault().addPropertyChangeListener(instance);
        }
        return instance;
    }

    /**
     * private constructor to prevent mulitple instances
     */
    private KenaiConnection() {
    }

    synchronized void leaveGroup(String name) {
        groupListeners.remove(name);
    }

    synchronized void leavePrivate(String name) {
        privateListeners.remove(name);
    }

    synchronized void tryJoinChat(MultiUserChat chat) throws XMPPException {
        chat.join(getUserName());
    }

    private synchronized  MultiUserChat createChat(KenaiFeature prj) {
        MultiUserChat multiUserChat = new MultiUserChat(connection, getChatroomName(prj));
        groupChats.put(prj.getName(), multiUserChat);
        groupMessageQueue.put(prj.getName(), new LinkedList<Message>());
        multiUserChat.addMessageListener(new MessageL());
        join(multiUserChat);
        return multiUserChat;
    }


    private void join(MultiUserChat chat) {
        try {
            chat.addParticipantListener(new PresenceIndicator.PresenceListener());
            chat.addParticipantListener(new PresenceListener());
            chat.join(getUserName());
        } catch (XMPPException ex) {
            XMPPLOG.log(Level.INFO, "Cannot join "  + chat.getRoom(), ex);
        }
    }


    public synchronized Chat joinPrivate(String jid, PacketListener lsn) {
        final String name = StringUtils.parseName(jid);
        Chat result = privateChats.get(name);
        if (result == null) {
            result = Kenai.getDefault().getXMPPConnection().getChatManager().createChat(jid, null);
            privateChats.put(name, result);
        }
        if (privateMessageQueue.get(name)==null) {
            privateMessageQueue.put(name, new LinkedList<Message>());
        }
        PacketListener put = privateListeners.put(name, lsn);
        for (Message m : privateMessageQueue.get(name)) {
            lsn.processPacket(m);
        }
        assert put == null:"User " + name + " already joined";
        return result;
    }

    public synchronized int getMessagesCountFor(String name) {
        LinkedList<Message> m = privateMessageQueue.get(name);
        return m==null?0:m.size();
    }

    /**
     * Adds listener to given MultiUserChat
     * only one listener can listen on given MultiUserChat
     * @param muc
     * @param lsn
     */
    public synchronized void join(MultiUserChat muc, PacketListener lsn) {
        final String name = StringUtils.parseName(muc.getRoom());
        PacketListener put = groupListeners.put(name, lsn);
        for (Message m : groupMessageQueue.get(name)) {
            lsn.processPacket(m);
        }
        assert put == null:"Chat room " + name + " already joined";
    }

    /**
     * 
     */
    public synchronized void tryConnect()  {
        try {
            connect();
            initChats();
            PresenceIndicator.getDefault().setStatus(Kenai.Status.ONLINE);
            isConnectionFailed = false;
        } catch (XMPPException ex) {
            isConnectionFailed = true;
        }
    }

    /**
     * is connection to kenai xmpp up and living?
     * @return
     */
    public boolean isConnected() {
        return connection != null && connection.isConnected() && connection.isAuthenticated();
    }

    private void connect() throws XMPPException {
        connection = Kenai.getDefault().getXMPPConnection();
        connection.addPacketListener(new PacketL(), new MessageTypeFilter(Type.chat));
    }

    public synchronized void reconnect(MultiUserChat muc) throws XMPPException {
        if (!connection.isConnected()) {
            connection.connect();
        }
        if (muc==null) {
            for (MultiUserChat m:getChats()) {
                if (!m.isJoined())
                    tryJoinChat(m);
            }
        } else if (!muc.isJoined())
            tryJoinChat(muc);
        isConnectionFailed=false;
    }

    public boolean isUserOnline(String user) {
        synchronized (onlineUsers) {
            return onlineUsers.contains(user);
        }
    }

    /**
     * temporary method do not use it
     * @return
     */
    public Collection<String> getMembers(String id) {
        MultiUserChat muc=getChat(id);
        if (muc!=null) {
            Iterator<String> i = muc.getOccupants();
            ArrayList<String> result = new ArrayList<String>();
            while (i.hasNext()) {
                result.add(StringUtils.parseResource(i.next()));
            }
            return result;
        } else {
            return Collections.emptyList();
        }
    }

    private class PacketL implements PacketListener {

        public void processPacket(Packet packet) {
            final Message msg = (Message) packet;
            final String name = StringUtils.parseName(msg.getFrom());
            LinkedList<Message> thisQ = privateMessageQueue.get(name);
            if (thisQ==null) {
                thisQ = new LinkedList<Message>();
                privateMessageQueue.put(name, thisQ);
            }
            thisQ.add(msg);
            final PacketListener listener = privateListeners.get(name);
            if (listener != null) {
                listener.processPacket(msg);
            }
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    if (/*chatNotifications.isEnabled(ChatTopComponent.createPrivateName(name)) &&*/ !ChatTopComponent.isPrivateInitedAndVisible(ChatTopComponent.createPrivateName(StringUtils.parseName(msg.getFrom())))) {
                        chatNotifications.addPrivateMessage(msg);
                    }
                }
            });
        }
    }

    private class MessageL implements PacketListener {
        public void processPacket(Packet packet) {
            synchronized (KenaiConnection.this) {
                final Message msg = (Message) packet;
                String n = StringUtils.parseName(msg.getFrom());
                if (n.contains("@")) {
                    n = StringUtils.parseName(n);
                }
                final String name = n;
                final NotificationExtension ne = (NotificationExtension) msg.getExtension("notification", "jabber:client");
                if (ne != null && msg.getExtension("x", "jabber:x:delay") == null) {
                    post(new Runnable() {
                        public void run() {
                            try {
                                Kenai.getDefault().getProject(name).firePropertyChange(KenaiProject.PROP_PROJECT_NOTIFICATION, null, ne);
                            } catch (KenaiException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                        }
                    });
                }
                final LinkedList<Message> thisQ = groupMessageQueue.get(name);
                thisQ.add(msg);
                final PacketListener listener = groupListeners.get(name);
                if (listener != null) {
                    listener.processPacket(msg);
                }
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        if (chatNotifications.isEnabled(name) && (listener == null || !ChatTopComponent.isGroupInitedAndVisible(name))) {
                            chatNotifications.addGroupMessage(msg);
                        } else {
                            chatNotifications.getMessagingHandle(name).notifyMessageReceived(msg);
                            chatNotifications.getMessagingHandle(name).notifyMessagesRead();
                        }
                    }
                });
            }
        }
    }

    private void initChats() {
        if (!connection.isConnected()) {
            return;
        }
        for (KenaiFeature prj : KenaiConnection.getDefault().getMyChats()) {
            try {
                createChat(prj);
            } catch (IllegalStateException ise) {
                Exceptions.printStackTrace(ise);
            }
        }
    }

    public synchronized List<MultiUserChat> getChats() {
        ArrayList<MultiUserChat> copy = new ArrayList<MultiUserChat>(groupChats.values());
        return copy;
    }

    /**
     * 
     * @param prj
     * @return
     */
    public synchronized MultiUserChat getChat(KenaiFeature prj) {
        MultiUserChat multiUserChat = groupChats.get(prj.getName());
        if (multiUserChat==null) {
            multiUserChat=createChat(prj);
        }
        return multiUserChat;
    }

    /**
     *
     * @param name
     * @return
     */
    public synchronized MultiUserChat getChat(String name) {
        return groupChats.get(name);
    }

    public synchronized Chat getPrivateChat(String name) {
        return privateChats.get(name);
    }


    private RequestProcessor xmppProcessor = new RequestProcessor("XMPP Processor"); // NOI18N
    public RequestProcessor.Task post(Runnable run) {
        return xmppProcessor.post(run);
    }

   private boolean isConnectionFailed;
    public boolean isConnectionFailed() {
        return isConnectionFailed;
    }

    public void propertyChange(final PropertyChangeEvent e) {
        if (Kenai.PROP_XMPP_LOGIN.equals(e.getPropertyName())) {
            if (e.getNewValue() != null) {
                post(new Runnable() {
                    public void run() {
                        synchronized(KenaiConnection.this) {
                            final PasswordAuthentication pa = Kenai.getDefault().getPasswordAuthentication();
                            USER = pa.getUserName();
                            tryConnect();
                        }
                    }
                });
            } else {
                try {
                    synchronized(KenaiConnection.this) {
                        for (MultiUserChat muc : getChats()) {
                            try {
                                muc.leave();
                            } catch (IllegalStateException ise) {
                                //we can ignore exceptions on logout
                                XMPPLOG.log(Level.FINE, null, ise);
                            }
                        }
                        groupChats.clear();
                        groupMessageQueue.clear();
                        groupListeners.clear();
                        privateListeners.clear();
                        privateMessageQueue.clear();
                        privateChats.clear();
                        if (SPIAccessor.DEFAULT!=null)
                            SPIAccessor.DEFAULT.clear();
                    }
                    PresenceIndicator.getDefault().setStatus(Kenai.Status.OFFLINE);
                    ChatNotifications.getDefault().clearAll();
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }

//------------------------------------------

    private String USER;
    
    private static final String CHAT_ROOM = "@muc." + Kenai.getDefault().getName(); // NOI18N

    /**
     * TODO: should return kenai account name
     * @return
     */
    private String getUserName() {
        return USER;
    }


    private String getChatroomName(KenaiFeature prj) {
        return prj.getName() + CHAT_ROOM;
    }

    public Collection<KenaiFeature> getMyChats() {
        ArrayList myChats = new ArrayList();
        try {
            for (KenaiProject prj: Kenai.getDefault().getMyProjects()) {
                myChats.addAll(Arrays.asList(prj.getFeatures(KenaiService.Type.CHAT)));
            }
            return myChats;
        } catch (KenaiException ex) {
            throw new RuntimeException(ex);
        }
    }

    public class PresenceListener implements PacketListener {

        public void processPacket(final Packet packet) {
             xmppProcessor.post(new Runnable() {

                public void run() {
                    synchronized (onlineUsers) {
                        onlineUsers.clear();
                        for (MultiUserChat muc : KenaiConnection.getDefault().getChats()) {
                            Iterator<String> i = muc.getOccupants();
                            while (i.hasNext()) {
                                String uname = StringUtils.parseResource(i.next());
                                onlineUsers.add(uname);
                            }
                        }
                    }
                    Presence presence = (Presence) packet;
                    KenaiUserUI user = KenaiUserUI.forName(StringUtils.parseResource(packet.getFrom()));
                    SPIAccessor.DEFAULT.firePropertyChange(
                            user,
                            presence.getType() != Presence.Type.available, presence.getType() == Presence.Type.available);
                    SwingUtilities.invokeLater(new Runnable() {

                        public void run() {
                            ChatTopComponent.refreshContactList();
                        }
                    });
                }
            }, 100);
        }
    }
}
