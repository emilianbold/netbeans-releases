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

package org.netbeans.modules.kenai.ui.spi;

import java.awt.Component;
import java.awt.Graphics;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.netbeans.modules.kenai.api.Kenai;
import org.netbeans.modules.kenai.collab.chat.ChatPanel;
import org.netbeans.modules.kenai.collab.chat.ChatTopComponent;
import org.openide.util.ImageUtilities;

/**
 *
 * @author Jan Becicka
 */
public class KenaiUser {

    public static final String PROP_PRESENCE = "Presence";

    private String user;
    private String fullName;

    private static ImageIcon ONLINE = new ImageIcon(ImageUtilities.loadImage("org/netbeans/modules/kenai/collab/resources/online.png"));
    private static ImageIcon OFFLINE = new ImageIcon(ImageUtilities.loadImage("org/netbeans/modules/kenai/collab/resources/offline.png"));
    private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    private static final String XMPP_SERVER = System.getProperty("kenai.com.url","https://kenai.com").substring(System.getProperty("kenai.com.url","https://kenai.com").lastIndexOf("/")+1);
    private Icon icon;

    public KenaiUser(String user) {
        this.user=user;
        this.fullName = user+"@"+XMPP_SERVER;
    }
    
    public static KenaiUser forName(final String user) {
        XMPPConnection con = Kenai.getDefault().getXMPPConnection();
        final KenaiUser result = new KenaiUser(user);
        con.addPacketListener(new PacketListener() {
            public void processPacket(Packet packet) {
                if (packet.getFrom().equals(result.fullName)) {
                    Presence presence = (Presence) packet;
                    result.firePropertyChange(PROP_PRESENCE, presence.getType() != Presence.Type.available, presence.getType() == Presence.Type.available);
                }
            }
        }, new PacketTypeFilter(Presence.class));
        return result;
    }

    /**
     * Get the value of online
     *
     * @return the value of online
     */
    public boolean isOnline() {
        return isOnline(this.user);
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

    public Icon getIcon() {
        if (icon==null) {
            icon = new Icon() {
                public void paintIcon(Component c, Graphics g, int x, int y) {
                    if (isOnline()) {
                        ONLINE.paintIcon(c, g, x, y);
                    } else {
                        OFFLINE.paintIcon(c, g, x, y);
                    }
                }

                public int getIconWidth() {
                    if (isOnline()) {
                        return ONLINE.getIconWidth();
                    } else {
                        return ONLINE.getIconWidth();
                    }
                }

                public int getIconHeight() {
                    if (isOnline()) {
                        return ONLINE.getIconHeight();
                    } else {
                        return ONLINE.getIconHeight();
                    }
                }
            };
        }
        return icon;
    }

    private void firePropertyChange(String string, boolean b, boolean b0) {
        propertyChangeSupport.firePropertyChange(string, b, b0);
    }

    public static boolean isOnline(String user) {
        XMPPConnection con = Kenai.getDefault().getXMPPConnection();
        return con.getRoster().getPresence(user+"@"+XMPP_SERVER).getType() == Presence.Type.available;
    }

    public JLabel createUserWidget() {
        return UIUtils.createUserWidget(this);
    }

    public String getUser() {
        return user;
    }

    public void startChat() {
        Runnable run = new Runnable() {
            public void run() {
                ChatTopComponent tc = ChatTopComponent.findInstance();
                tc.open();
                tc.addChat(new ChatPanel(fullName));
            }
        };
        if (SwingUtilities.isEventDispatchThread()) {
            run.run();
        } else {
            SwingUtilities.invokeLater(run);
        }
    }
}
