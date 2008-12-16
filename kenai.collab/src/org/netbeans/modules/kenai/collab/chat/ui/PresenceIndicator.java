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
package org.netbeans.modules.kenai.collab.chat.ui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Packet;
import org.netbeans.modules.kenai.collab.im.KenaiConnection;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;


/**
 * Icon showing online status on lower right corner
 * @author Jan Becicka
 */

public class PresenceIndicator {
    private static ImageIcon ONLINE = new ImageIcon(ImageUtilities.loadImage("org/netbeans/modules/kenai/collab/resources/online.gif"));
    private static ImageIcon OFFLINE = new ImageIcon(ImageUtilities.loadImage("org/netbeans/modules/kenai/collab/resources/offline.gif"));
    private static PresenceIndicator instance;

    private JLabel label;
    private MouseL helper;

    public static enum Status {
        ONLINE,
        OFFLINE
    }

    public void setStatus(Status status) {
        label.setIcon(status == Status.ONLINE?ONLINE:OFFLINE);
    }

    Component getComponent() {
        return label;
    }

    public static synchronized PresenceIndicator getDefault() {
        if (instance == null) {
            instance = new PresenceIndicator();
        }
        return instance;
    }

    
    private PresenceIndicator() {
        helper = new MouseL();
        label = new JLabel(NbBundle.getMessage(PresenceIndicator.class, "CTL_PresenceOffline"), OFFLINE, JLabel.HORIZONTAL);
        label.addMouseListener(helper);
    }
    
    private class MouseL extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent event) {
            if (event.getClickCount() == 2) {
                ChatTopComponent.getDefault().open();
                ChatTopComponent.getDefault().requestActive();
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {
            processEvent(e);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            processEvent(e);
        }

        private void processEvent(MouseEvent e) {
            if (e.isPopupTrigger()) {
                JPopupMenu menu = new JPopupMenu();
                menu.add(new JMenuItem(new Online()));
                menu.add(new JMenuItem(new Offline()));
                menu.add(new JMenuItem(new OpenChat()));
                menu.show(label, 0, 0);
            }
        }


        
        private class Online extends AbstractAction {
            
            public Online() {
                super(org.openide.util.NbBundle.getMessage(PresenceIndicator.class, "CTL_Available", new Object[] {}));
            }

            public void actionPerformed(ActionEvent e) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        }
        
        private class Offline extends AbstractAction {
            public Offline() {
                super(org.openide.util.NbBundle.getMessage(PresenceIndicator.class, "CTL_Offline", new Object[] {}));
            }

            public void actionPerformed(ActionEvent e) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        }

        private class OpenChat extends AbstractAction {

            public OpenChat() {
                super(NbBundle.getMessage(PresenceIndicator.class, "CTL_OpenChat", new Object[] {}));
            }

            public void actionPerformed(ActionEvent e) {
                ChatTopComponent.getDefault().open();
                ChatTopComponent.getDefault().requestActive();
            }
        }

    }

    public class PresenceListener implements PacketListener {
        /**
         *
         * @param packet
         */
        public void processPacket(Packet packet) {
            label.setText(NbBundle.getMessage(PresenceIndicator.class, "CTL_PresenceOnline", new Object[] {KenaiConnection.getDefault().getChats().first().getOccupantsCount()}));
        }
    }

}
