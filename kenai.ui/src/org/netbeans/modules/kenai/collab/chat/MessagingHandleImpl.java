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

package org.netbeans.modules.kenai.collab.chat;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Date;
import java.util.prefs.Preferences;
import org.jivesoftware.smack.packet.Message;
import org.netbeans.modules.kenai.api.Kenai;
import org.netbeans.modules.kenai.api.KenaiException;
import org.netbeans.modules.kenai.api.KenaiProject;
import org.netbeans.modules.kenai.ui.spi.MessagingHandle;
import org.openide.awt.Notification;
import org.openide.util.Exceptions;
import org.openide.util.NbPreferences;

/**
 *
 * @author Jan Becicka
 */
public class MessagingHandleImpl extends MessagingHandle {
    private static final String LASTMESSAGEAT = ".last.message.at"; //NOI18N

    private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    private Notification notification;
    private int onlineCount;
    private int messageCount = -1;
    private int newMessageCount = 0;
    private Date lastMessage;
    private Date lastMessageRead;
    private String id;
    private static Preferences prefs = NbPreferences.forModule(MessagingHandleImpl.class);


    MessagingHandleImpl(String id) {
        this.id = id;
        Kenai k = Kenai.getDefault();
        try {
            final KenaiProject prj = k.getProject(id);
            if (k.getMyProjects().contains(prj) && k.getStatus()==Kenai.Status.ONLINE) {
                onlineCount = -2;
            } else {
                onlineCount = -1;
            }
        } catch (KenaiException kenaiException) {
            Exceptions.printStackTrace(kenaiException);
        }
        lastMessage = lastMessageRead = new Date(Long.parseLong(prefs.get(id + LASTMESSAGEAT, "0"))); // NOI18N
    }
    /**
     * Get the value of messageCount
     *
     * @return the value of messageCount
     */
    public int getMessageCount() {
        return messageCount;
    }

    /**
     * Set the value of messageCount
     *
     * @param messageCount new value of messageCount
     */
    public void setMessageCount(int messageCount) {
        int oldMessageCount = this.messageCount;
        this.messageCount = messageCount;
        propertyChangeSupport.firePropertyChange(PROP_MESSAGE_COUNT, oldMessageCount, messageCount);
    }

    /**
     * Get the value of onlineCount
     *
     * @return the value of onlineCount
     */
    public int getOnlineCount() {
        return onlineCount;
    }

    /**
     * Set the value of onlineCount
     *
     * @param onlineCount new value of onlineCount
     */
    public void setOnlineCount(int onlineCount) {
        int oldOnlineCount = this.onlineCount;
        this.onlineCount = onlineCount;
        propertyChangeSupport.firePropertyChange(PROP_ONLINE_COUNT, oldOnlineCount, onlineCount);
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

    public int getNewMessageCount() {
        return newMessageCount;
    }

    public void notifyMessageReceived(Message m) {
        setMessageCount(messageCount+1);
        lastMessage = ChatPanel.getTimestamp(m);
        if (lastMessage.after(lastMessageRead)) {
            newMessageCount++;
        }
    }

    public void notifyMessagesRead() {
        lastMessageRead = lastMessage;
        prefs.put(id+LASTMESSAGEAT, Long.toString(lastMessageRead.getTime()));
        newMessageCount=0;
    }

    public void updateNotification(Notification notification) {
        disposeNotification();
        this.notification = notification;
    }

    void disposeNotification() {
        if (notification!=null) {
            notification.clear();
        }
    }
}
