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
import java.util.HashMap;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import org.netbeans.modules.kenai.collab.chat.ChatTopComponent;
import org.netbeans.modules.kenai.collab.chat.KenaiConnection;
import org.netbeans.modules.kenai.collab.chat.SPIAccessor;
import org.openide.util.ImageUtilities;

/**
 *
 * @author Jan Becicka
 */
public final class KenaiUserUI {

    static {
        SPIAccessor.DEFAULT = new SPIAccessorImpl();
    }

    public static final String PROP_PRESENCE = "Presence";

    private String user;

    private static ImageIcon ONLINE = new ImageIcon(ImageUtilities.loadImage("org/netbeans/modules/kenai/collab/resources/user_online.png"));
    private static ImageIcon OFFLINE = new ImageIcon(ImageUtilities.loadImage("org/netbeans/modules/kenai/collab/resources/user_offline.png"));
    private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    private Icon icon;

    private static HashMap<String, KenaiUserUI> users = new HashMap();

    private KenaiUserUI(String user) {
        this.user=user;
    }
    
    public static synchronized KenaiUserUI forName(final String user) {
        KenaiUserUI kuser = users.get(user);
        if (kuser==null) {
            kuser = new KenaiUserUI(user);
            users.put(user, kuser);
        }
        return kuser;
    }

    static synchronized void clear() {
        users.clear();
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

    void firePropertyChange(String string, boolean b, boolean b0) {
        propertyChangeSupport.firePropertyChange(string, b, b0);
    }

    public static boolean isOnline(String user) {
        return KenaiConnection.getDefault().isUserOnline(user);
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
                tc.setActivePrivate(user);
            }
        };
        if (SwingUtilities.isEventDispatchThread()) {
            run.run();
        } else {
            SwingUtilities.invokeLater(run);
        }
    }
}
