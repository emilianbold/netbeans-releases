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

import java.util.Collection;
import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.netbeans.modules.kenai.collab.chat.ui.PresenceIndicator;
import org.netbeans.modules.kenai.collab.chat.ui.PresenceIndicator.PresenceListener;
import org.netbeans.modules.kenai.collab.chat.ui.PresenceIndicator.Status;

/**
 * Class representing connection to kenai
 * Currently just hordcoded values connecting to localhost
 * @author Jan Becicka
 */
public class KenaiConnection {

    void tryConnect() {
        try {
            connect();
            joinChat();
            nbChat.addParticipantListener(PresenceIndicator.getDefault().new PresenceListener());
            PresenceIndicator.getDefault().setStatus(Status.ONLINE);
        } catch (XMPPException ex) {
            XMPPLOG.log(Level.WARNING, ex.getMessage());
        }
    }

    XMPPConnection connection;
    MultiUserChat nbChat;
    private static final String USER = "testuser1";
    private static final String PASSWORD = "password";
    private static final String XMPP_SERVER = "bco108.central.sun.com";
    private static final String CHAT_ROOM = "nbchat@muc.central.sun.com";

    private static KenaiConnection instance;

    private static Logger XMPPLOG = Logger.getLogger(KenaiConnection.class.getName());

    public static synchronized KenaiConnection getDefault() {
        if (instance==null) {
            instance = new KenaiConnection();
        }
        if (!instance.isConnected()) {
            instance.tryConnect();
        }
        return instance;
    }

    private KenaiConnection() {
    }

   public boolean isConnected() {
        return connection!=null && connection.isConnected() && connection.isAuthenticated() && nbChat!=null && nbChat.isJoined();
    }

    private void connect() throws XMPPException {
        connection = new XMPPConnection(XMPP_SERVER);
        connection.connect();
        login();
    }

    private void login() throws XMPPException {
        connection.login(USER,PASSWORD);
    }

    private MultiUserChat joinChat() {
        if (!connection.isConnected())
            return null;
        nbChat = new MultiUserChat(connection,CHAT_ROOM);
        try {
            // User2 joins the new room
            // The room service will decide the amount of history to send
            nbChat.join(USER);
        } catch (XMPPException ex) {
            Logger.getLogger(KenaiConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
        return nbChat;
    }

    public SortedSet<MultiUserChat> getChats() {
        return Collections.unmodifiableSortedSet(new TreeSet<MultiUserChat>(Collections.singleton(nbChat)));
    }

    public Roster getRoster() {
        return connection.getRoster();
    }

}
