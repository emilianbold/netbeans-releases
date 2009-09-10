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
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import org.netbeans.modules.kenai.api.KenaiUser;
import org.netbeans.modules.kenai.collab.chat.ChatTopComponent;
import org.openide.util.ImageUtilities;

/**
 *
 * @author Jan Becicka
 */
public final class KenaiUserUI {

    KenaiUser user;

    private static ImageIcon ONLINE = new ImageIcon(ImageUtilities.loadImage("org/netbeans/modules/kenai/collab/resources/user_online.png"));
    private static ImageIcon OFFLINE = new ImageIcon(ImageUtilities.loadImage("org/netbeans/modules/kenai/collab/resources/user_offline.png"));
    private Icon icon;

    public KenaiUserUI(String userName) {
        this.user=KenaiUser.forName(userName);
    }
    
    public Icon getIcon() {
        if (icon==null) {
            icon = new Icon() {
                public void paintIcon(Component c, Graphics g, int x, int y) {
                    if (user.isOnline()) {
                        ONLINE.paintIcon(c, g, x, y);
                    } else {
                        OFFLINE.paintIcon(c, g, x, y);
                    }
                }

                public int getIconWidth() {
                    if (user.isOnline()) {
                        return ONLINE.getIconWidth();
                    } else {
                        return ONLINE.getIconWidth();
                    }
                }

                public int getIconHeight() {
                    if (user.isOnline()) {
                        return ONLINE.getIconHeight();
                    } else {
                        return ONLINE.getIconHeight();
                    }
                }
            };
        }
        return icon;
    }

    public JLabel createUserWidget() {
        return UIUtils.createUserWidget(this);
    }

    public String getUserName() {
        return user.getUserName();
    }

    public void startChat() {
        Runnable run = new Runnable() {
            public void run() {
                ChatTopComponent tc = ChatTopComponent.findInstance();
                tc.open();
                tc.setActivePrivate(user.getUserName());
            }
        };
        if (SwingUtilities.isEventDispatchThread()) {
            run.run();
        } else {
            SwingUtilities.invokeLater(run);
        }
    }

    public KenaiUser getKenaiUser() {
        return user;
    }
}
