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
package org.netbeans.modules.kenai.collab.im;

import java.net.PasswordAuthentication;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Message.Type;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.netbeans.modules.kenai.api.Kenai;
import org.netbeans.modules.kenai.api.KenaiEvent;
import org.netbeans.modules.kenai.api.KenaiException;
import org.netbeans.modules.kenai.api.KenaiListener;
import org.netbeans.modules.kenai.api.KenaiProject;
import org.netbeans.modules.kenai.collab.chat.ui.ChatTopComponent;
import org.netbeans.modules.kenai.collab.chat.ui.PresenceIndicator;
import org.netbeans.modules.kenai.collab.chat.ui.PresenceIndicator.PresenceListener;
import org.netbeans.modules.kenai.collab.chat.ui.PresenceIndicator.Status;
import org.netbeans.modules.kenai.ui.spi.UIUtils;

/**
 * Class representing connection to kenai xmpp server
 * @author Jan Becicka
 */
public class KenaiConnection implements KenaiListener {

    //Map <kenai project name, message listener>
    private HashMap<String, PacketListener> listeners = new HashMap();
    private XMPPConnection connection;
    //Map <kenai project name, multi user chat>
    private HashMap<String, MultiUserChat> chats = new HashMap<String, MultiUserChat>();

    //singleton instance
    private static KenaiConnection instance;

    //just logger
    private static Logger XMPPLOG = Logger.getLogger(KenaiConnection.class.getName());

    // Map<name of kenai project, message queue>
    private HashMap<String, LinkedList<Message>> messageQueue = new HashMap<String, LinkedList<Message>>();

    private static PrivateChatNotification privateNotification = new PrivateChatNotification();
    private static GroupChatNotification groupNotification = new GroupChatNotification();

    /**
     * Default singleton instance representing XMPP connection to kenai server
     * @return
     */
    public static synchronized KenaiConnection getDefault() {
        if (instance == null) {
            instance = new KenaiConnection();
            Kenai.getDefault().addKenaiListener(instance);
        }
        return instance;
    }

    /**
     * private constructor to prevent mulitple instances
     */
    private KenaiConnection() {
    }


    private void join(MultiUserChat chat) {
        try {
            chat.addParticipantListener(PresenceIndicator.getDefault().new PresenceListener());
            chat.join(getUserName());
        } catch (XMPPException ex) {
            XMPPLOG.log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Adds listener to given MultiUserChat
     * only one listener can listen on given MultiUserChat
     * @param muc
     * @param lsn
     */
    public void join(MultiUserChat muc, PacketListener lsn) {
        final String name = StringUtils.parseName(muc.getRoom());
        PacketListener put = listeners.put(name, lsn);
        for (Message m : messageQueue.get(name)) {
            lsn.processPacket(m);
        }
        assert put == null;
    }

    private void tryConnect() {
        try {
            connect();
            initChats();
            PresenceIndicator.getDefault().setStatus(Status.ONLINE);
        } catch (XMPPException ex) {
            XMPPLOG.log(Level.WARNING, ex.getMessage());
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
        connection = new XMPPConnection(XMPP_SERVER);
        connection.connect();
        login();
        connection.addPacketListener(new PacketL(), new MessageTypeFilter(Type.chat));
    }

    private class PacketL implements PacketListener {
        public void processPacket(Packet packet) {
            final Message msg = (Message) packet;
            privateNotification.addMessage(msg);
            privateNotification.add();
        }
    }

    private class MessageL implements PacketListener {
        public void processPacket(Packet packet) {
            final Message msg = (Message) packet;
            final String name = StringUtils.parseName(msg.getFrom());
            final LinkedList<Message> thisQ = messageQueue.get(name);
            thisQ.add(msg);
            final PacketListener listener = listeners.get(name);
            if (listener != null) {
                listener.processPacket(msg);
            } else {
                groupNotification.setMessage(msg);
                groupNotification.add();
            }
        }
    }

    private void login() throws XMPPException {
        connection.login(USER, PASSWORD);
    }

    private void initChats() {
        if (!connection.isConnected()) {
            return;
        }
        for (KenaiProject prj : KenaiConnection.getDefault().getMyProjects()) {
            final MultiUserChat multiUserChat = new MultiUserChat(connection, getChatroomName(prj));
            chats.put(prj.getName(), multiUserChat);
            messageQueue.put(prj.getName(), new LinkedList<Message>());
            multiUserChat.addMessageListener(new MessageL());
            join(multiUserChat);
        }
    }

    public Collection<MultiUserChat> getChats() {
        return chats.values();
    }

    /**
     * 
     * @param prj
     * @return
     */
    public MultiUserChat getChat(KenaiProject prj) {
        return chats.get(prj.getName());
    }

    /**
     *
     * @param name
     * @return
     */
    public MultiUserChat getChat(String name) {
        return chats.get(name);
    }


    public Roster getRoster() {
        return connection.getRoster();
    }

    public void stateChanged(KenaiEvent e) {
        if (e.getType() == KenaiEvent.LOGIN) {
            final PasswordAuthentication pa = (PasswordAuthentication) e.getSource();
            if (pa != null) {
                myProjects = null;
                tryConnect();
            } else {
                for (MultiUserChat muc:getChats()) {
                    muc.leave();
                }
                chats.clear();
                connection.disconnect();
                messageQueue.clear();
                listeners.clear();
            }
        }
    }
    
//------------------------------------------
//TODO this should be removed when xmpp server starts working on kenai.com    

//    bcol4 test server
    private static final String USER = "testuser1";
    private static final String PASSWORD = "password";
    private static final String XMPP_SERVER = "bco14.central.sun.com";
    private static final String CHAT_ROOM = "@muc.central.sun.com";

////  test server on localhost
//    private static final String USER = "netbeans";
//    private static final String PASSWORD = "netbeans";
//    private static final String XMPP_SERVER = "127.0.0.1";
//    private static final String CHAT_ROOM = "@conference.127.0.0.1";

    /**
     * TODO: should return kenai account name
     * @return
     */
    private String getUserName() {
        return USER;
    }


    /**
     * TODO: should return data from KenaiProjectFeature
     */
    private String getChatroomName(KenaiProject prj) {
        return prj.getName() + CHAT_ROOM;
    //return prj.getFeatures(KenaiFeature.CHAT)[0].getName();
    }

    private Collection<KenaiProject> myProjects;

    //TODO: my projects does not work so far
    public Collection<KenaiProject> getMyProjects() {
        if (myProjects == null) {
            try {
        //        myProjects = new ArrayList();
        //        myProjects.add(Kenai.getDefault().getProject("kenai"));
        //        myProjects.add(Kenai.getDefault().getProject("alligator"));
                  myProjects = Kenai.getDefault().getMyProjects();
            } catch (KenaiException ex) {
                myProjects = Collections.emptyList();
            }
        }
        return myProjects;
    }
}
