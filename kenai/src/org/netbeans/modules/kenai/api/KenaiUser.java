/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.kenai.api;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.packet.MUCUser;
import org.netbeans.modules.kenai.UserData;

/**
 * Class representing user on Kenai
 * @author Jan Becicka
 */
public final class KenaiUser {

    public static final String PROP_PRESENCE = "Presence";
    private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    UserData data;
    private Kenai kenai;

    private KenaiUser(Kenai kenai, UserData data) {
        this.kenai = kenai;
        this.data = data;
    }

    private KenaiUser(Kenai kenai, String username) {
        this.data = new UserData();
        this.data.user_name = username.substring(0, username.indexOf('@'));
        this.kenai=kenai;
    }

    /**
     * kenai instance
     * @return
     */
    public Kenai getKenai() {
        return kenai;
    }
    
    /**
     * getter for short username
     * @return
     */
    public String getUserName() {
        return data.user_name;
    }
    
    /**
     * getter for first name
     * @return
     */
    public String getFirstName() {
        return data.first_name;
    }
    
    /**
     * getter for last name
     * @return
     */
    public String getLastName() {
        return data.last_name;
    }

    /**
     * fully qualified name. E.g. john@kenai.com
     * @return
     */
    public String getFQN() {
        return getUserName() + "@" + kenai.getUrl().getHost();
    }
    
    /**
     * user status
     * @return
     */
    public Status getStatus() {
        if (kenai.getPasswordAuthentication()==null) {
            return Status.UNKNOWN;
        }
        if (isOnline())
            return Status.ONLINE;
        return Status.OFFLINE;
    }

    @Override
    public String toString() {
        return getUserName();
    }

    public static KenaiUser forName(final String name) {
        assert name !=null;
        assert name.contains("@"): "username must be FQN";
        assert !name.contains("/"): "username cannot contain '/'";
        Kenai kenai = getKenai(name);
        assert kenai!=null: "kenai instance not found for " + name;
        synchronized (kenai.users) {
            String shortName = StringUtils.parseName(name);
            KenaiUser user = kenai.users.get(shortName);
            if (user==null) {
                user = new KenaiUser(kenai, name);
                kenai.users.put(shortName, user);
            }
            return user;
        }
    }

    public static int getOnlineUserCount() {
        int i = 0;
        for (Kenai k : KenaiManager.getDefault().getKenais()) {
            synchronized (k.onlineUsers) {
                i += k.onlineUsers.size();
            }
        }
        return i;
    }
    
    static KenaiUser get(Kenai kenai, UserData data) {
        synchronized (kenai.users) {
            KenaiUser user = kenai.users.get(data.user_name);
            if (user!=null) {
                user.data = data;
            } else {
                user = new KenaiUser(kenai, data);
                kenai.users.put(data.user_name, user);
            }
            return user;
        }
    }

    /**
     * Get the value of online
     *
     * @return the value of online
     */
    public boolean isOnline() {
        return isOnline(this.getUserName() + "@" +kenai.getUrl().getHost());
    }

    /**
     * Add PropertyChangeListener.
     *
     * @param listener
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }
    
    /**
     * Remove PropertyChangeListener.
     *
     * @param listener
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    /**
     * is user online
     * @param user's FQN
     * @return
     */
    public static boolean isOnline(String user) {
        assert user !=null;
        assert user.contains("@"): "username must be FQN";
        assert !user.contains("/"): "username cannot contain '/'";
        Kenai k = getKenai(user);
        synchronized (k.onlineUsers) {
            return k.onlineUsers.contains(StringUtils.parseName(user));
        }
    }

    private static Kenai getKenai(String jid) {
        if (jid.contains("@muc.")) {
            final String server = StringUtils.parseServer(jid);
            return KenaiManager.getDefault().getKenai("https://" + server.substring(4));
        } else {
            return KenaiManager.getDefault().getKenai("https://" + StringUtils.parseServer(jid));
        }
    }

    static class KenaiPacketListener implements PacketListener {

        public void processPacket(Packet packet) {
            Presence presence = (Presence) packet;
            String from = presence.getFrom();
            Kenai k = getKenai(from);
            final MUCUser mucUser = (MUCUser) packet.getExtension("x", "http://jabber.org/protocol/muc#user");
            if (null != mucUser) {
                String user = StringUtils.parseName(mucUser.getItem().getJid());
                if (presence.getType() == Presence.Type.available) {
                    synchronized (k.onlineUsers) {
                        k.onlineUsers.add(user);
                    }
                    synchronized (k.users) {
                        KenaiUser u = k.users.get(user);
                        if (u != null) {
                            u.propertyChangeSupport.firePropertyChange(PROP_PRESENCE,
                                    presence.getType() != Presence.Type.available, presence.getType() == Presence.Type.available);
                        }
                    }

                } else if (presence.getType() == Presence.Type.unavailable) {
                    synchronized (k.onlineUsers) {
                        k.onlineUsers.remove(user);
                    }
                    synchronized (k.users) {
                        KenaiUser u = k.users.get(user);
                        if (u != null) {
                            u.propertyChangeSupport.firePropertyChange(PROP_PRESENCE,
                                    presence.getType() != Presence.Type.available, presence.getType() == Presence.Type.available);
                        }
                    }
                }
            }
        }
    }

    /**
     * user status
     */
    public static enum Status {
        ONLINE,
        OFFLINE,
        DND,
        UNKNOWN
    }
    
}
