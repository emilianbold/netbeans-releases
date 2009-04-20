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
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.jdesktop.swingx.JXBusyLabel;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.netbeans.modules.kenai.api.Kenai;
import org.netbeans.modules.kenai.api.KenaiFeature;
import org.netbeans.modules.kenai.ui.spi.UIUtils;
import org.openide.awt.TabbedPaneFactory;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;
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
    static final String ICON_PATH = "org/netbeans/modules/kenai/collab/resources/online.gif"; // NOI18N

    private static final String PREFERRED_ID = "ChatTopComponent"; // NOI18N
    private final KenaiConnection kec = KenaiConnection.getDefault();

    //open chats
    private HashSet<String> open = new HashSet<String>();

    private final Preferences prefs = NbPreferences.forModule(ChatTopComponent.class);

    private final JPanel chatsPanel = new JPanel() {

        @Override
        public boolean isOptimizedDrawingEnabled() {
            return false;
        }
    };

    @Override
    public void requestActive() {
        super.requestActive();
        Component c = chats.getSelectedComponent();
        if (c!=null) {
            c.requestFocus();
        }
    }

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
                if (index>=0) {
                    chats.setForegroundAt(index, Color.BLACK);
                    ChatNotifications.getDefault().removeGroup(chats.getTitleAt(index));
                    chats.getComponentAt(index).requestFocus();
                }
            }
        };

        if (kec.isConnected()) {
            putChatsScreen();
            if (open.isEmpty()) {
                SwingUtilities.invokeLater(new Runnable(){
                    public void run() {
                        showPopup();
                    }
                });
            }
        } else {
            if (kec.getXMPPException()!=null) {
                putErrorScreen();
            } else {
                putLoginScreen();
            }
        }
        Kenai.getDefault().addPropertyChangeListener(new KenaiL());
        chats.addChangeListener(changeListener);
        chats.addPropertyChangeListener(TabbedPaneFactory.PROP_CLOSE, new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (TabbedPaneFactory.PROP_CLOSE.equals(evt.getPropertyName())) {
                    removeChat(((Component) evt.getNewValue()));
                }
            }
        });
        if( "Aqua".equals(UIManager.getLookAndFeel().getID()) ) { //NOI18N
            chats.setBackground(UIManager.getColor("NbExplorerView.background")); //NOI18N
            chats.setOpaque(true);
        }
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

    private void putErrorScreen() {
        Runnable r = new Runnable() {
            public void run() {
                removeAll();
                chats.removeAll();
                open.clear();
                add(errorScreen, BorderLayout.CENTER);
                validate();
            }
        };
        if (SwingUtilities.isEventDispatchThread()) {
            r.run();
        } else {
            SwingUtilities.invokeLater(r);
        }
    }


    private void putConnectingScreen() {
        Runnable r = new Runnable() {
            public void run() {
                removeAll();
                chats.removeAll();
                open.clear();
                ((JXBusyLabel) initLabel).setBusy(true);
                add(initPanel, BorderLayout.CENTER);
                validate();
            }
        };
        if (SwingUtilities.isEventDispatchThread()) {
            r.run();
        } else {
            SwingUtilities.invokeLater(r);
        }
    }

    public void setActive(String name) {
        ChatNotifications.getDefault().removeGroup(name);
        int indexOfTab = chats.indexOfTab(name+"    "); // NOI18N
        if (indexOfTab < 0) {
            MultiUserChat muc = kec.getChat(name);
            if (muc != null) {
                ChatPanel chatPanel = new ChatPanel(muc);
                addChat(chatPanel);
                indexOfTab=chats.getTabCount()-1;
            }

        }
        chats.setSelectedIndex(indexOfTab);
    }


    void addChat(ChatPanel chatPanel) {
        ChatNotifications.getDefault().removeGroup(chatPanel.getName());
        chats.add(chatPanel);
        open.add(chatPanel.getName());
        chats.setSelectedComponent(chatPanel);
        validate();
        storeOpenChats();
    }

    void removeChat(Component chatPanel) {
        int index = chats.indexOfComponent(chatPanel);
        assert index>=0: "Component not found in CloseButtonTabbedPane " + chatPanel;
        open.remove(chats.getTitleAt(index).trim());
        chats.remove(chatPanel);
        kec.leave(chatPanel.getName());
        validate();
        storeOpenChats();
    }

    void setModified(ChatPanel panel) {
        int i=chats.indexOfComponent(panel);
        chats.setForegroundAt(i, Color.BLUE);
    }

    void showPopup() {
        JPopupMenu menu = new JPopupMenu();
        for (KenaiFeature prj : kec.getMyChats()) {
            if (!open.contains(prj.getName())) {
                menu.add(new OpenChatAction(prj));
            }
        }
        if (menu.getComponentCount()==0) {
            final JMenuItem jMenuItem = new JMenuItem("<empty>"); // NOI18N
            jMenuItem.setEnabled(false);
            menu.add(jMenuItem);
        }
        menu.show(addChat, 0, addChat.getSize().height);
    }


    public static boolean isInitedAndVisible(String name) {
        return instance==null?false:instance.isVisible()&&instance.isOpened()&&instance.open.contains(name);
    }

    private void putChats() {
        final Collection<MultiUserChat> chs = kec.getChats();
        if (chs.size()==1) {
            final MultiUserChat next = chs.iterator().next();
            ChatPanel chatPanel = new ChatPanel(next);
            addChat(chatPanel);
        } else if (chs.size()!=0) {
            String s = prefs.get("kenai.open.chats." + Kenai.getDefault().getPasswordAuthentication().getUserName(),""); // NOI18N
            if (s.length() > 1) {
                for (String chat : s.split(",")) { // NOI18N
                    MultiUserChat muc = kec.getChat(chat);
                    if (muc != null) {
                        ChatPanel chatPanel = new ChatPanel(muc);
                        addChat(chatPanel);
                    } else {
                        Logger.getLogger(ChatTopComponent.class.getName()).warning("Cannot find chat " + chat);
                    }
                }
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
        lblNotLoggedIn = new javax.swing.JLabel();
        loginLink = new javax.swing.JLabel();
        errorScreen = new javax.swing.JPanel();
        lblXmppError = new javax.swing.JLabel();
        retryLink = new javax.swing.JLabel();
        initPanel = new javax.swing.JPanel();
        initLabel = new JXBusyLabel();

        chats.setTabLayoutPolicy(javax.swing.JTabbedPane.SCROLL_TAB_LAYOUT);

        glassPane.setBorder(javax.swing.BorderFactory.createEmptyBorder(2, 2, 2, 4));
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
                .addContainerGap(173, Short.MAX_VALUE)
                .add(addChat, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );
        glassPaneLayout.setVerticalGroup(
            glassPaneLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(glassPaneLayout.createSequentialGroup()
                .add(addChat, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(125, Short.MAX_VALUE))
        );

        org.openide.awt.Mnemonics.setLocalizedText(lblNotLoggedIn, org.openide.util.NbBundle.getMessage(ChatTopComponent.class, "ChatTopComponent.lblNotLoggedIn.text")); // NOI18N

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
                .add(lblNotLoggedIn)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(loginLink)
                .addContainerGap(61, Short.MAX_VALUE))
        );
        loginScreenLayout.setVerticalGroup(
            loginScreenLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(loginScreenLayout.createSequentialGroup()
                .addContainerGap()
                .add(loginScreenLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblNotLoggedIn)
                    .add(loginLink))
                .addContainerGap(406, Short.MAX_VALUE))
        );

        org.openide.awt.Mnemonics.setLocalizedText(lblXmppError, org.openide.util.NbBundle.getMessage(ChatTopComponent.class, "ChatTopComponent.lblXmppError.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(retryLink, org.openide.util.NbBundle.getMessage(ChatTopComponent.class, "ChatTopComponent.retryLink.text")); // NOI18N
        retryLink.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                retryLinkMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                retryLinkMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                retryLinkMouseExited(evt);
            }
        });

        org.jdesktop.layout.GroupLayout errorScreenLayout = new org.jdesktop.layout.GroupLayout(errorScreen);
        errorScreen.setLayout(errorScreenLayout);
        errorScreenLayout.setHorizontalGroup(
            errorScreenLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(errorScreenLayout.createSequentialGroup()
                .addContainerGap()
                .add(lblXmppError)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(retryLink)
                .addContainerGap(23, Short.MAX_VALUE))
        );
        errorScreenLayout.setVerticalGroup(
            errorScreenLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(errorScreenLayout.createSequentialGroup()
                .addContainerGap()
                .add(errorScreenLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblXmppError)
                    .add(retryLink))
                .addContainerGap(406, Short.MAX_VALUE))
        );

        setLayout(new java.awt.BorderLayout());

        org.openide.awt.Mnemonics.setLocalizedText(initLabel, org.openide.util.NbBundle.getMessage(ChatTopComponent.class, "ChatTopComponent.initLabel.text")); // NOI18N

        org.jdesktop.layout.GroupLayout initPanelLayout = new org.jdesktop.layout.GroupLayout(initPanel);
        initPanel.setLayout(initPanelLayout);
        initPanelLayout.setHorizontalGroup(
            initPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(initPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(initLabel)
                .addContainerGap(271, Short.MAX_VALUE))
        );
        initPanelLayout.setVerticalGroup(
            initPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(initPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(initLabel)
                .addContainerGap(272, Short.MAX_VALUE))
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

    private void retryLinkMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_retryLinkMouseClicked
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                kec.tryConnect();
                if (kec.isConnected()) {
                    putChatsScreen();
                } else {
                    if (kec.getXMPPException()!=null) {
                        putErrorScreen();
                    } else {
                        putLoginScreen();
                    }
                }
            }
        });
}//GEN-LAST:event_retryLinkMouseClicked

    private void retryLinkMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_retryLinkMouseEntered
        retryLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
}//GEN-LAST:event_retryLinkMouseEntered

    private void retryLinkMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_retryLinkMouseExited
        loginLink.setCursor(Cursor.getDefaultCursor());
}//GEN-LAST:event_retryLinkMouseExited

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addChat;
    private javax.swing.JTabbedPane chats;
    private javax.swing.JPanel errorScreen;
    private javax.swing.JPanel glassPane;
    private javax.swing.JLabel initLabel;
    private javax.swing.JPanel initPanel;
    private javax.swing.JLabel lblNotLoggedIn;
    private javax.swing.JLabel lblXmppError;
    private javax.swing.JLabel loginLink;
    private javax.swing.JPanel loginScreen;
    private javax.swing.JLabel retryLink;
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
                    "Cannot find " + PREFERRED_ID + " component. It will not be located properly in the window system."); // NOI18N
            return getDefault();
        }
        if (win instanceof ChatTopComponent) {
            return (ChatTopComponent) win;
        }
        Logger.getLogger(ChatTopComponent.class.getName()).warning(
                "There seem to be multiple components with the '" + PREFERRED_ID + // NOI18N
                "' ID. That is a potential source of errors and unexpected behavior."); // NOI18N
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

    private void storeOpenChats() {
        StringBuffer b = new StringBuffer();
        Iterator<String> it = open.iterator();
        while (it.hasNext()) {
            b.append(it.next());
            if (it.hasNext()) {
                b.append(","); // NOI18N
            }
        }
        prefs.put("kenai.open.chats." + Kenai.getDefault().getPasswordAuthentication().getUserName(), b.toString()); // NOI18N
    }

    final class KenaiL implements PropertyChangeListener {

        public void propertyChange(final PropertyChangeEvent e) {
            if (Kenai.PROP_LOGIN.equals(e.getPropertyName())) {
                kec.post(new Runnable() {

                    public void run() {
                        if (e.getNewValue() == null) {
                            putLoginScreen();
                        } else {
                            kec.getMyChats();
                            putChatsScreen();
                        }
                    }
                });
            } else if (Kenai.PROP_LOGIN_STARTED.equals(e.getPropertyName())) {
                putConnectingScreen();
            } else if (Kenai.PROP_LOGIN_FAILED.equals(e.getPropertyName())) {
                putLoginScreen();
            }
        }
    }
    
    private final class OpenChatAction extends AbstractAction {

        private KenaiFeature f;

        public OpenChatAction(KenaiFeature f) {
            super(f.getName());
            this.f = f;
        }

        public void actionPerformed(ActionEvent e) {
            addChat(new ChatPanel(kec.getChat(f)));
        }
    }
}
