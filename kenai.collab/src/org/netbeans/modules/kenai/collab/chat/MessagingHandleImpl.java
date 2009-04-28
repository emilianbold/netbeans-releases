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
import org.netbeans.modules.kenai.api.Kenai;
import org.netbeans.modules.kenai.api.KenaiException;
import org.netbeans.modules.kenai.api.KenaiProject;
import org.netbeans.modules.kenai.ui.spi.MessagingHandle;
import org.openide.awt.Notification;
import org.openide.util.Exceptions;

/**
 *
 * @author Jan Becicka
 */
public class MessagingHandleImpl extends MessagingHandle {

    private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    private Notification notification;
    private int onlineCount;
    private int messageCount = -1;

    MessagingHandleImpl(String id) {
        if (System.getProperty("kenai.com.url", "https://kenai.com").endsWith("testkenai.com")) {
            Kenai k = Kenai.getDefault();
            try {
                final KenaiProject prj = k.getProject(id);
                if (k.getMyProjects().contains(prj)) {
                    onlineCount = -2;
                } else {
                    onlineCount = -1;
                }
            } catch (KenaiException kenaiException) {
                Exceptions.printStackTrace(kenaiException);
            }
        } else {
            onlineCount = -1;
        }
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

    public Notification getNotification() {
        return notification;
    }

    public void setNotification(Notification notification) {
        this.notification = notification;
    }

    void disposeNotification() {
        if (notification!=null)
            notification.clear();
    }
}
