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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.PasswordAuthentication;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
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
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.netbeans.modules.kenai.api.Kenai;
import org.netbeans.modules.kenai.api.KenaiException;
import org.netbeans.modules.kenai.api.KenaiFeature;
import org.netbeans.modules.kenai.api.KenaiManager;
import org.netbeans.modules.kenai.api.KenaiProject;
import org.netbeans.modules.kenai.api.KenaiService;
import org.netbeans.modules.kenai.ui.Utilities;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;

/**
 * Class representing connection to kenai xmpp server
 * @author Jan Becicka
 */
public class KenaiConnection implements PropertyChangeListener {

    public static final String PROP_XMPP_STARTED = "xmpp_started"; // NOI18N

    public static final String PROP_XMPP_FINISHED = "xmpp_finished"; // NOI18N

    static synchronized Iterable<KenaiConnection> getAllInstances() {
        return new ArrayList<KenaiConnection>(instances.values());
    }
    //Map <kenai project name, message listener>
    private HashMap<String, PacketListener> groupListeners = new HashMap<String, PacketListener>();
    private HashMap<String, PacketListener> privateListeners = new HashMap<String, PacketListener>();
    private XMPPConnection connection;
    //Map <kenai project name, multi user chat>
    final private Map<String, MultiUserChat> groupChats = new HashMap<String, MultiUserChat>();

    //Map <user jid, chat>
    final private Map<String, Chat> privateChats = new HashMap();

    //singleton instance
    private static WeakHashMap<Kenai,KenaiConnection> instances = new WeakHashMap();

    //just logger
    private static Logger XMPPLOG = Logger.getLogger(KenaiConnection.class.getName());

    // Map<name of kenai project, message queue>
    private HashMap<String, LinkedList<Message>> groupMessageQueue = new HashMap<String, LinkedList<Message>>();

    // Map<name of kenai project, message queue>
    private HashMap<String, LinkedList<Message>> privateMessageQueue = new HashMap<String, LinkedList<Message>>();

    private static ChatNotifications chatNotifications = ChatNotifications.getDefault();

    private java.beans.PropertyChangeSupport propertyChangeSupport = new java.beans.PropertyChangeSupport(this);

    private Kenai kenai;
    static {
        ProviderManager providerManager = ProviderManager.getInstance();
        providerManager.addExtensionProvider("delay", "urn:xmpp:delay", new DelayExtensionProvider());//NOI18N
        providerManager.addExtensionProvider("notification", NotificationExtensionProvider.NAMESPACE, new NotificationExtensionProvider()); // NOI18N
    }
    /**
     * Default singleton instance representing XMPP connection to kenai server
     * @return
     */
    public static synchronized KenaiConnection getDefault(Kenai k) {
        KenaiConnection kc = instances.get(k);
        if (kc == null) {
            kc = new KenaiConnection(k);
            k.addPropertyChangeListener(WeakListeners.propertyChange(kc, k));
            instances.put(k, kc);
        }
        return kc;
    }

    public Kenai getKenai() {
        return kenai;
    }

    public static KenaiProject getKenaiProject(String room) {
        assert !room.contains("/") : "room name cannot contain '/'";
        String kenaiUrl = "https://" + room.substring(room.indexOf("@muc.")+"@muc.".length());
        Kenai kenai = KenaiManager.getDefault().getKenai(kenaiUrl);
        try {
            return kenai.getProject(room.substring(0, room.indexOf("@muc."))); // NOI18N
        } catch (KenaiException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    public static KenaiProject getKenaiProject(MultiUserChat muc) {
        return getKenaiProject(muc.getRoom());
    }


    public static Kenai getKenai(String jid) {
        Utilities.assertJid(jid);
        int index = jid.indexOf("@muc."); // NOI18N
        if (index<0) {
            index = jid.indexOf("@") + 1; // NOI18N
        } else {
            index+=5;
        }
        String kenaiUrl = "https://" + jid.substring(index);
        return KenaiManager.getDefault().getKenai(kenaiUrl);
    }

    /**
     * private constructor to prevent mulitple instances
     */
    private KenaiConnection(Kenai kenai) {
        this.kenai=kenai;
    }

    synchronized void leaveGroup(String name) {
        assert !name.contains("@") : "FQN cannot be used";
        groupListeners.remove(name);
    }

    synchronized void leavePrivate(String name) {
        Utilities.assertJid(name);
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
            assert connection.isConnected();
            assert connection.isAuthenticated();
            chat.addParticipantListener(new PresenceIndicator.PresenceListener());
            chat.addParticipantListener(new PresenceListener());
            chat.join(getUserName());
        } catch (XMPPException ex) {
            XMPPLOG.log(Level.INFO, "Cannot join "  + chat.getRoom(), ex);
        }
    }


    public synchronized Chat joinPrivate(String jid, PacketListener lsn) {
        Utilities.assertJid(jid);
        Chat result = privateChats.get(jid);
        if (result == null) {
            result = KenaiConnection.getKenai(jid).getXMPPConnection().getChatManager().createChat(jid, null);
            privateChats.put(jid, result);
        }
        if (privateMessageQueue.get(jid)==null) {
            privateMessageQueue.put(jid, new LinkedList<Message>());
        }
        PacketListener put = privateListeners.put(jid, lsn);
        for (Message m : privateMessageQueue.get(jid)) {
            lsn.processPacket(m);
        }
        assert put == null:"User " + jid + " already joined";
        return result;
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
        assert put == null:"Chat room " + muc.getRoom() + " already joined";
    }

    /**
     * 
     */
    public synchronized void tryConnect()  {
        try {
            propertyChangeSupport.firePropertyChange(PROP_XMPP_STARTED, null, null);
            connect();
            initChats();
            isConnectionFailed = false;
        } catch (XMPPException ex) {
            isConnectionFailed = true;
        } finally {
            propertyChangeSupport.firePropertyChange(PROP_XMPP_FINISHED, null, null);
        }
    }

    /**
     * Adds listener to Kenai instance
     * @param l
     */
    public void addPropertyChangeListener(PropertyChangeListener l) {
        propertyChangeSupport.addPropertyChangeListener(l);
    }

    /**
     * Removes listener from Kenai instance
     * @param l
     */
    public void removePropertyChangeListener(PropertyChangeListener l) {
        propertyChangeSupport.removePropertyChangeListener(l);
    }

    /**
     * is connection to kenai xmpp up and living?
     * @return
     */
    public boolean isConnected() {
        return connection != null && connection.isConnected() && connection.isAuthenticated();
    }

    private void connect() throws XMPPException {
        connection = kenai.getXMPPConnection();
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
            final String name = StringUtils.parseBareAddress(msg.getFrom());
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
                    if (chatNotifications.isEnabled(name) && !ChatTopComponent.isPrivateInitedAndVisible(name)) {
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
                if (n.contains("@")) { // NOI18N
                    n = StringUtils.parseName(n);
                }
                final String name = n;
                final NotificationExtension ne = (NotificationExtension) msg.getExtension("notification", NotificationExtensionProvider.NAMESPACE); // NOI18N
                if (ne != null && msg.getExtension("x", "jabber:x:delay") == null) { // NOI18N
                    post(new Runnable() {
                        public void run() {
                            try {
                                kenai.getProject(name).firePropertyChange(KenaiProject.PROP_PROJECT_NOTIFICATION, null, ne.getNotification());
                            } catch (KenaiException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                        }
                    });
                }
                if (ne!=null && !Boolean.parseBoolean(System.getProperty("kenai.show.notifications.in.chat", "true"))) {
                    return;
                }
                final LinkedList<Message> thisQ = groupMessageQueue.get(name);
                thisQ.add(msg);
                final PacketListener listener = groupListeners.get(name);
                if (listener != null) {
                    listener.processPacket(msg);
                }
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        if (chatNotifications.isEnabled(StringUtils.parseBareAddress(msg.getFrom())) && (listener == null || !ChatTopComponent.isGroupInitedAndVisible(StringUtils.parseBareAddress(msg.getFrom())))) {
                            chatNotifications.addGroupMessage(msg);
                        } else {
                            try {
                                chatNotifications.getMessagingHandle(kenai.getProject(name)).notifyMessageReceived(msg);
                                chatNotifications.getMessagingHandle(kenai.getProject(name)).notifyMessagesRead();
                            } catch (KenaiException ex) {
                                Exceptions.printStackTrace(ex);
                            }
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
        for (KenaiFeature prj : getMyChats()) {
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
        assert !name.contains("@") : "getChat does not accept FQNs";
        return groupChats.get(name);
    }

    public synchronized Chat getPrivateChat(String jid) {
        Utilities.assertJid(jid);
        return privateChats.get(jid);
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
                            final PasswordAuthentication pa = kenai.getPasswordAuthentication();
                            USER = pa.getUserName();
                            try {
                                tryConnect();
                            } catch (IllegalStateException ise) {
                                if (kenai.getXMPPConnection() != null) {
                                    Exceptions.printStackTrace(ise);
                                }
                            }
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
                    }
                    ChatNotifications.getDefault().clearAll(kenai);
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }

//------------------------------------------

    private String USER;
    
    /**
     * @return
     */
    private String getUserName() {
        return USER;
    }


    private String getChatroomName(KenaiFeature prj) {
        return prj.getName() + "@muc." + kenai.getUrl().getHost(); // NOI18N
    }

    public Collection<KenaiFeature> getMyChats() {
        ArrayList myChats = new ArrayList();
        try {
            for (KenaiProject prj: kenai.getMyProjects()) {
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
