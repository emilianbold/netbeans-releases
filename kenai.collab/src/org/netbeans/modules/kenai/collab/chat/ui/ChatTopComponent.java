/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.kenai.collab.chat.ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.netbeans.modules.kenai.api.Kenai;
import org.netbeans.modules.kenai.api.KenaiException;
import org.netbeans.modules.kenai.api.KenaiProject;
import org.netbeans.modules.kenai.collab.im.KenaiConnection;
import org.netbeans.modules.kenai.ui.spi.UIUtils;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Top component which displays all available ChatRooms
 * @see ChatPanel
 * @see ChatContainer
 * @author Jan Becicka
 */
public class ChatTopComponent extends TopComponent {
    private static ChatTopComponent instance;

    /** path to the icon used by the component and its open action */
    static final String ICON_PATH = "org/netbeans/modules/kenai/collab/resources/online.gif";

    private static ImageIcon ONLINE = new ImageIcon(ImageUtilities.loadImage("org/netbeans/modules/kenai/collab/resources/online.gif"));

    private static final String PREFERRED_ID = "ChatTopComponent";

    private JToggleButton activeButton;

    private void setActive(String chat, JToggleButton b) {
        if (activeButton!=null)
            activeButton.setSelected(false);
        b.setSelected(true);
        activeButton=b;
        ((CardLayout) chats.getLayout()).show(chats, chat);
    }

    public void setActive(String chat) {
        for (Component c:buttonsGroup.getComponents()) {
            JToggleButton b=(JToggleButton)c;
            if (b.getText().equals(chat)) {
                setActive(chat, b);
                return;
            }
        }
        try {
            addChats(chat, new ChatPanel(KenaiConnection.getDefault().getChat(Kenai.getDefault().getProject(chat))));
        } catch (KenaiException ex) {
            Exceptions.printStackTrace(ex);
        }
    }



    void addChats(final String string, ChatPanel chatPanel) {
        chats.add(chatPanel,string);
        final JToggleButton jToggleButton = new JToggleButton(string, ONLINE);
        jToggleButton.setSelected(true);
        jToggleButton.setHorizontalTextPosition(SwingConstants.LEFT);
        jToggleButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setActive(string, (JToggleButton) e.getSource());
            }
        });
        buttonsGroup.add(jToggleButton);
        open.add(string);
        setActive(string, jToggleButton);
        validate();
    }

    void showPopup() {
        JPopupMenu menu = new JPopupMenu();
        for (KenaiProject prj : KenaiConnection.getDefault().getMyProjects()) {
            menu.add(new OpenChatAction(prj));
        }
        menu.show(addChat, 0, addChat.getSize().height);
    }

    private ChatTopComponent() {
        initComponents();
        setName(NbBundle.getMessage(ChatTopComponent.class, "CTL_ChatTopComponent"));
        setToolTipText(NbBundle.getMessage(ChatTopComponent.class, "HINT_ChatTopComponent"));
        setIcon(ImageUtilities.loadImage(ICON_PATH, true));

        if (Kenai.getDefault().getPasswordAuthentication()==null) {
            retry.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (Kenai.getDefault().getPasswordAuthentication()==null) {
                        UIUtils.showLogin();
                        KenaiConnection.getDefault();
                        putChats();
                    }
                }
            });
            add(retry, BorderLayout.CENTER);
        } else {
            putChats();
        }
        //putClientProperty("netbeans.winsys.tc.keep_preferred_size_when_slided_in", Boolean.TRUE);
    }

    public boolean isHandled(Message msg) {
        return open.contains(StringUtils.parseName(msg.getFrom()));
    }

    private HashSet<String> open = new HashSet<String>();

    private void putChats() {
        remove(retry);
        final Collection<MultiUserChat> chs = KenaiConnection.getDefault().getChats();
        if (chs.size()==1) {
            final MultiUserChat next = chs.iterator().next();
            ChatPanel chatPanel = new ChatPanel(next);
            addChats(next.getRoom(), chatPanel);
        } else if (chs.size()!=0) {
//            SwingUtilities.invokeLater(new Runnable(){
//                public void run() {
//                    //showPopup();
//                }
//            });
        }
        
        add(chats, BorderLayout.CENTER);
        validate();
    }



    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        chats = new javax.swing.JPanel();
        topPanel = new javax.swing.JPanel();
        buttonsGroup = new javax.swing.JPanel();
        addChat = new javax.swing.JButton();

        setLayout(new java.awt.BorderLayout());

        chats.setLayout(new java.awt.CardLayout());
        add(chats, java.awt.BorderLayout.CENTER);

        buttonsGroup.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        addChat.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/kenai/collab/resources/plus.gif"))); // NOI18N
        addChat.setIconTextGap(0);
        addChat.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addChatActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout topPanelLayout = new org.jdesktop.layout.GroupLayout(topPanel);
        topPanel.setLayout(topPanelLayout);
        topPanelLayout.setHorizontalGroup(
            topPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, topPanelLayout.createSequentialGroup()
                .add(buttonsGroup, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 379, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(addChat))
        );
        topPanelLayout.setVerticalGroup(
            topPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.CENTER, buttonsGroup, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 28, Short.MAX_VALUE)
            .add(org.jdesktop.layout.GroupLayout.CENTER, addChat)
        );

        add(topPanel, java.awt.BorderLayout.NORTH);
    }// </editor-fold>//GEN-END:initComponents

    private void addChatActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addChatActionPerformed
        showPopup();
}//GEN-LAST:event_addChatActionPerformed

    private JButton retry = new JButton("Login");

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addChat;
    private javax.swing.JPanel buttonsGroup;
    private javax.swing.JPanel chats;
    private javax.swing.JPanel topPanel;
    // End of variables declaration//GEN-END:variables
    /**
     * Gets default instance. Do not use directly: reserved for *.settings files only,
     * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
     * To obtain the singleton instance, use {@link #findInstance}.
     */
    public static synchronized ChatTopComponent getDefault() {
        if (instance == null) {
            instance = new ChatTopComponent();
        }
        return instance;
    }

    /**
     * Obtain the ChatTopComponent instance. Never call {@link #getDefault} directly!
     */
    public static synchronized ChatTopComponent findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (win == null) {
            Logger.getLogger(ChatTopComponent.class.getName()).warning(
                    "Cannot find " + PREFERRED_ID + " component. It will not be located properly in the window system.");
            return getDefault();
        }
        if (win instanceof ChatTopComponent) {
            return (ChatTopComponent) win;
        }
        Logger.getLogger(ChatTopComponent.class.getName()).warning(
                "There seem to be multiple components with the '" + PREFERRED_ID +
                "' ID. That is a potential source of errors and unexpected behavior.");
        return getDefault();
    }

    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ALWAYS;
    }

    @Override
    public void componentOpened() {
        // TODO add custom code on component opening
    }

    @Override
    public void componentClosed() {
        // TODO add custom code on component closing
    }

    /** replaces this in object stream */
    @Override
    public Object writeReplace() {
        return new ResolvableHelper();
    }

    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }

    final static class ResolvableHelper implements Serializable {

        private static final long serialVersionUID = 1L;

        public Object readResolve() {
            return ChatTopComponent.getDefault();
        }
    }

    private final class OpenChatAction extends AbstractAction {

        private KenaiProject prj;

        public OpenChatAction(KenaiProject prj) {
            super(prj.getName());
            this.prj = prj;
        }

        public void actionPerformed(ActionEvent e) {
            addChats(prj.getName(), new ChatPanel(KenaiConnection.getDefault().getChat(prj)));
        }

    }
}
