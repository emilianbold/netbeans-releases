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

package org.netbeans.modules.kenai.collab.chat;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.OverlayLayout;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.netbeans.modules.kenai.api.Kenai;
import org.netbeans.modules.kenai.api.KenaiEvent;
import org.netbeans.modules.kenai.api.KenaiListener;
import org.netbeans.modules.kenai.api.KenaiProject;
import org.netbeans.modules.kenai.collab.chat.ChatNotifications;
import org.netbeans.modules.kenai.collab.chat.KenaiConnection;
import org.netbeans.modules.kenai.ui.spi.UIUtils;
import org.openide.awt.TabbedPaneFactory;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
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

    private static final String PREFERRED_ID = "ChatTopComponent";

    //open chats
    private HashSet<String> open = new HashSet<String>();

    private final Preferences prefs = NbPreferences.forModule(ChatTopComponent.class);

    private final JPanel chatsPanel = new JPanel() {

        @Override
        public boolean isOptimizedDrawingEnabled() {
            return false;
        }
    };


    private ChatTopComponent() {
        initComponents();
        setName(NbBundle.getMessage(ChatTopComponent.class, "CTL_ChatTopComponent"));
        setToolTipText(NbBundle.getMessage(ChatTopComponent.class, "HINT_ChatTopComponent"));
        setIcon(ImageUtilities.loadImage(ICON_PATH, true));

        LayoutManager overlay = new OverlayLayout(chatsPanel);
        chatsPanel.setLayout(overlay);
        glassPane.setAlignmentX(0);
        glassPane.setAlignmentY(0);
        chats.setAlignmentX(0);
        chats.setAlignmentY(0);
        chatsPanel.add(glassPane);
        chatsPanel.add(chats);

        ChangeListener changeListener = new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                int index = chats.getSelectedIndex();
                if (index>=0)
                    chats.setForegroundAt(index, Color.BLACK);
            }
        };

        if (KenaiConnection.getDefault().isConnected()) {
            putChatsScreen();
        } else {
            putLoginScreen();
        }
        Kenai.getDefault().addKenaiListener(new KenaiL());
        chats.addChangeListener(changeListener);
        chats.addPropertyChangeListener(TabbedPaneFactory.PROP_CLOSE, new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (TabbedPaneFactory.PROP_CLOSE.equals(evt.getPropertyName())) {
                    removeChat(((Component) evt.getNewValue()));
                }
            }
        });
        //putClientProperty("netbeans.winsys.tc.keep_preferred_size_when_slided_in", Boolean.TRUE);
    }

    private void putChatsScreen() {
        Runnable r = new Runnable() {
            public void run() {
                removeAll();
                add(chatsPanel, BorderLayout.CENTER);
                putChats();
                validate();
            }
        };
        if (SwingUtilities.isEventDispatchThread()) {
            r.run();
        } else {
            SwingUtilities.invokeLater(r);
        }
    }

    private void putLoginScreen() {
        Runnable r = new Runnable() {
            public void run() {
                removeAll();
                chats.removeAll();
                open.clear();
                add(loginScreen, BorderLayout.CENTER);
                validate();
            }
        };
        if (SwingUtilities.isEventDispatchThread()) {
            r.run();
        } else {
            SwingUtilities.invokeLater(r);
        }

    }


    public void setActive(String parseName) {
        chats.setSelectedIndex(chats.indexOfTab(parseName));
    }


    void addChat(ChatPanel chatPanel) {
        ChatNotifications.getDefault().removeGroup(chatPanel.getName());
        chats.addTab(chatPanel.getName(),chatPanel);
        open.add(chatPanel.getName());
        StringBuffer b= new StringBuffer();
        Iterator<String> it = open.iterator();
        while (it.hasNext()) {
            b.append(it.next());
            if (it.hasNext())
                b.append(",");
        }
        prefs.put("kenai.open.chats." + Kenai.getDefault().getPasswordAuthentication().getUserName(),b.toString());
    }

    void removeChat(Component chatPanel) {
        int index = chats.indexOfComponent(chatPanel);
        assert index>=0: "Component not found in CloseButtonTabbedPane " + chatPanel;
        open.remove(chats.getTitleAt(index));
    }

    void setModified(ChatPanel panel) {
        int i=chats.indexOfComponent(panel);
        chats.setForegroundAt(i, Color.BLUE);
    }

    void showPopup() {
        JPopupMenu menu = new JPopupMenu();
        for (KenaiProject prj : KenaiConnection.getDefault().getMyProjects()) {
            if (!open.contains(prj.getName())) {
                menu.add(new OpenChatAction(prj));
            }
        }
        if (menu.getComponentCount()==0) {
            final JMenuItem jMenuItem = new JMenuItem("<empty>");
            jMenuItem.setEnabled(false);
            menu.add(jMenuItem);
        }
        menu.show(addChat, 0, addChat.getSize().height);
    }


    public static boolean isInitedAndVisible(String name) {
        return instance==null?false:instance.isVisible()&&instance.open.contains(name);
    }

    private void putChats() {
        final KenaiConnection cc = KenaiConnection.getDefault();
        final Collection<MultiUserChat> chs = cc.getChats();
        if (chs.size()==1) {
            final MultiUserChat next = chs.iterator().next();
            ChatPanel chatPanel = new ChatPanel(next);
            addChat(chatPanel);
        } else if (chs.size()!=0) {
            String s = prefs.get("kenai.open.chats." + Kenai.getDefault().getPasswordAuthentication().getUserName(),"");
            for (String chat:s.split(",")) {
                MultiUserChat muc = cc.getChat(chat);
                if (muc!=null) {
                    ChatPanel chatPanel = new ChatPanel(muc);
                    addChat(chatPanel);
                } else {
                    Logger.getLogger(ChatTopComponent.class.getName()).warning("Cannot find chat " + chat);
                }
            }
            if (open.isEmpty()) {
                SwingUtilities.invokeLater(new Runnable(){
                    public void run() {
                        showPopup();
                    }
                });
            }
        }
        validate();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        chats = TabbedPaneFactory.createCloseButtonTabbedPane();
        glassPane = new javax.swing.JPanel();
        addChat = new javax.swing.JButton();
        loginScreen = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        loginLink = new javax.swing.JLabel();
        initPanel = new javax.swing.JPanel();
        initLabel = new javax.swing.JLabel();
        jProgressBar1 = new javax.swing.JProgressBar();

        chats.setTabLayoutPolicy(javax.swing.JTabbedPane.SCROLL_TAB_LAYOUT);

        glassPane.setOpaque(false);

        addChat.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/kenai/collab/resources/plus.gif"))); // NOI18N
        addChat.setIconTextGap(0);
        addChat.setPreferredSize(new java.awt.Dimension(15, 15));
        addChat.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addChatActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout glassPaneLayout = new org.jdesktop.layout.GroupLayout(glassPane);
        glassPane.setLayout(glassPaneLayout);
        glassPaneLayout.setHorizontalGroup(
            glassPaneLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, glassPaneLayout.createSequentialGroup()
                .addContainerGap(179, Short.MAX_VALUE)
                .add(addChat, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );
        glassPaneLayout.setVerticalGroup(
            glassPaneLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(glassPaneLayout.createSequentialGroup()
                .add(addChat, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(129, Short.MAX_VALUE))
        );

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(ChatTopComponent.class, "ChatTopComponent.jLabel1.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(loginLink, org.openide.util.NbBundle.getMessage(ChatTopComponent.class, "ChatTopComponent.loginLink.text")); // NOI18N
        loginLink.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                loginLinkMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                loginLinkMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                loginLinkMouseExited(evt);
            }
        });

        org.jdesktop.layout.GroupLayout loginScreenLayout = new org.jdesktop.layout.GroupLayout(loginScreen);
        loginScreen.setLayout(loginScreenLayout);
        loginScreenLayout.setHorizontalGroup(
            loginScreenLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(loginScreenLayout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(loginLink)
                .addContainerGap(61, Short.MAX_VALUE))
        );
        loginScreenLayout.setVerticalGroup(
            loginScreenLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(loginScreenLayout.createSequentialGroup()
                .addContainerGap()
                .add(loginScreenLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(loginLink))
                .addContainerGap(406, Short.MAX_VALUE))
        );

        setLayout(new java.awt.BorderLayout());

        org.openide.awt.Mnemonics.setLocalizedText(initLabel, org.openide.util.NbBundle.getMessage(ChatTopComponent.class, "ChatTopComponent.initLabel.text")); // NOI18N

        jProgressBar1.setIndeterminate(true);

        org.jdesktop.layout.GroupLayout initPanelLayout = new org.jdesktop.layout.GroupLayout(initPanel);
        initPanel.setLayout(initPanelLayout);
        initPanelLayout.setHorizontalGroup(
            initPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(initPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(initPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jProgressBar1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 376, Short.MAX_VALUE)
                    .add(initLabel))
                .addContainerGap())
        );
        initPanelLayout.setVerticalGroup(
            initPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(initPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(initLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jProgressBar1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(248, Short.MAX_VALUE))
        );

        add(initPanel, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void addChatActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addChatActionPerformed
        showPopup();
}//GEN-LAST:event_addChatActionPerformed

    private void loginLinkMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_loginLinkMouseClicked
        UIUtils.showLogin();
}//GEN-LAST:event_loginLinkMouseClicked

    private void loginLinkMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_loginLinkMouseEntered
        loginLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
}//GEN-LAST:event_loginLinkMouseEntered

    private void loginLinkMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_loginLinkMouseExited
         loginLink.setCursor(Cursor.getDefaultCursor());
}//GEN-LAST:event_loginLinkMouseExited

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addChat;
    private javax.swing.JTabbedPane chats;
    private javax.swing.JPanel glassPane;
    private javax.swing.JLabel initLabel;
    private javax.swing.JPanel initPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JProgressBar jProgressBar1;
    private javax.swing.JLabel loginLink;
    private javax.swing.JPanel loginScreen;
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

    final class KenaiL implements KenaiListener {

        public void stateChanged(KenaiEvent e) {
            if (KenaiEvent.LOGIN==e.getType()) {
                if (e.getSource()==null) {
                    putLoginScreen();
                } else {
                    KenaiConnection.getDefault().getMyProjects();
                    putChatsScreen();
                }
            }
        }
    }
    
    private final class OpenChatAction extends AbstractAction {

        private KenaiProject prj;

        public OpenChatAction(KenaiProject prj) {
            super(prj.getName());
            this.prj = prj;
        }

        public void actionPerformed(ActionEvent e) {
            addChat(new ChatPanel(KenaiConnection.getDefault().getChat(prj)));
        }
    }
}
