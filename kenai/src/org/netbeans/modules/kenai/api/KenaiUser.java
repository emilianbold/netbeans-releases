/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.kenai.api;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashMap;
import java.util.HashSet;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.util.StringUtils;
import org.netbeans.modules.kenai.UserData;

/**
 * Class representing user on Kenai
 * @author Jan Becicka
 */
public final class KenaiUser {

    public static final String PROP_PRESENCE = "Presence";
    private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    private static final HashMap<String, KenaiUser> users = new HashMap();
    private static final HashSet<String> onlineUsers = new HashSet<String>();
    UserData data;

    private KenaiUser(UserData data) {
        this.data = data;
    }

    private KenaiUser(String username) {
        this.data = new UserData();
        this.data.user_name = username;
    }
    
    /**
     * getter for username
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
     * user status
     * @return
     */
    public Status getStatus() {
        if (Kenai.getDefault().getPasswordAuthentication()==null) {
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
        synchronized (users) {
            KenaiUser user = users.get(name);
            if (user==null) {
                user = new KenaiUser(name);
                users.put(name, user);
            }
            return user;
        }
    }
    
    static KenaiUser get(UserData data) {
        synchronized (users) {
            KenaiUser user = users.get(data.user_name);
            if (user!=null) {
                user.data = data;
            } else {
                user = new KenaiUser(data);
                users.put(data.user_name, user);
            }
            return user;
        }
    }


    static synchronized void clear() {
        synchronized (users) {
            users.clear();
        }
        synchronized (onlineUsers) {
            onlineUsers.clear();
    }
    }

    /**
     * Get the value of online
     *
     * @return the value of online
     */
    public boolean isOnline() {
        return isOnline(this.getUserName());
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

    public static boolean isOnline(String user) {
        synchronized (onlineUsers) {
            return onlineUsers.contains(user);
        }
    }

    static class KenaiPacketListener implements PacketListener {

        public void processPacket(Packet packet) {
            Presence presence = (Presence) packet;
            String from = presence.getFrom();
            if (null != packet.getExtension("x", "http://jabber.org/protocol/muc#user")) {
                String user = StringUtils.parseResource(from);
                if (presence.getType() == Presence.Type.available) {
                    synchronized (onlineUsers) {
                        onlineUsers.add(user);
                    }
                    synchronized (users) {
                        KenaiUser u = users.get(user);
                        if (u != null) {
                            u.propertyChangeSupport.firePropertyChange(PROP_PRESENCE,
                                    presence.getType() != Presence.Type.available, presence.getType() == Presence.Type.available);
                        }
                    }

                } else if (presence.getType() == Presence.Type.unavailable) {
                    synchronized (onlineUsers) {
                        onlineUsers.remove(user);
                    }
                    synchronized (users) {
                        KenaiUser u = users.get(user);
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
