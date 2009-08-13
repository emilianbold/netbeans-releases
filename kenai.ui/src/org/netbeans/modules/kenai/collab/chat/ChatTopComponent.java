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

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.jdesktop.swingx.JXBusyLabel;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.netbeans.modules.kenai.api.*;
import org.netbeans.modules.kenai.ui.spi.UIUtils;
import org.openide.awt.TabbedPaneFactory;
import org.openide.util.*;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Top component which displays all available ChatRooms
 * @see ChatPanel
 * @see ChatContainer
 * @author Jan Becicka
 */
public class ChatTopComponent extends TopComponent {
    private static final String KENAI_OPEN_CHATS_PREF = "kenai.open.chats.";
    private static ChatTopComponent instance;

    /** path to the icon used by the component and its open action */
    static final String ICON_PATH = "org/netbeans/modules/kenai/collab/resources/chat.png"; // NOI18N
    static final String PLUS = "org/netbeans/modules/kenai/collab/resources/plus.png"; // NOI18N
    private static final String XMPP_SERVER = System.getProperty("kenai.com.url","https://kenai.com").substring(System.getProperty("kenai.com.url","https://kenai.com").lastIndexOf("/")+1);

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

    public void reconnect() {
        RequestProcessor.getDefault().post(new Runnable() {

            public void run() {
                synchronized (kec) {
                    try {
                        kec.reconnect(null);
                    } catch (XMPPException ex) {
                        Logger.getLogger(ChatTopComponent.class.getName()).log(Level.INFO, ex.getMessage(), ex);
                    }
                }

                if (kec.isConnected()) {
                    putChatsScreen();
                } else {
                    if (kec.isConnectionFailed()) {
                        putErrorScreen();
                    } else {
                        putLoginScreen();
                    }
                }
            }
        });
    }

    @Override
    public void requestActive() {
        requestActive(true);
    }

    void requestActive(final boolean b) {
        super.requestActive();
        Component c = chats.getSelectedComponent();
        if (c!=null) {
            c.requestFocus();
        }
        if (chats.getTabCount()==1 && b) {
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    if (chats.getTabCount()==1 && b && chats.isShowing()) {
                        //showPopup(null);
                    }
                }
            });
        }
    }


    private ChatTopComponent() {
        initComponents();
        clearChatsTabbedPane();
        contactList.putClientProperty(TabbedPaneFactory.NO_CLOSE_BUTTON, Boolean.TRUE);
        setName(NbBundle.getMessage(ChatTopComponent.class, "CTL_ChatTopComponent"));
        setToolTipText(NbBundle.getMessage(ChatTopComponent.class, "HINT_ChatTopComponent"));
        setIcon(ImageUtilities.loadImage(ICON_PATH, true));

        LayoutManager overlay = new OverlayLayout(chatsPanel);
        chatsPanel.setLayout(overlay);
        chats.setAlignmentX(0);
        chats.setAlignmentY(0);
        chatsPanel.add(chats);

        ChangeListener changeListener = new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                int index = chats.getSelectedIndex();
                if (index>=0) {
                    chats.setForegroundAt(index, Color.BLACK);
                    if (!initInProgress)
                        ChatNotifications.getDefault().removeGroup(chats.getComponentAt(index).getName());
                    chats.getComponentAt(index).requestFocus();
                }
            }
        };

        Kenai.getDefault().addPropertyChangeListener(new KenaiL());
        chats.addChangeListener(changeListener);
        chats.addPropertyChangeListener(TabbedPaneFactory.PROP_CLOSE, new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (TabbedPaneFactory.PROP_CLOSE.equals(evt.getPropertyName())) {
                    removeChat(((ChatPanel) evt.getNewValue()));
                }
            }
        });
        if (kec.isConnected()) {
            putChatsScreen();
        } else {
            if (kec.isConnectionFailed()) {
                putErrorScreen();
            } else {
                putLoginScreen();
            }
        }
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
                contactList.updateFilter();
                validate();
                repaint();
            }
        };
        if (SwingUtilities.isEventDispatchThread()) {
            r.run();
        } else {
            SwingUtilities.invokeLater(r);
        }
    }

    private void clearChatsTabbedPane() {
        chats.removeAll();
        addMoreChatsTab(0);
    }

    private void addMoreChatsTab(int index) {
        chats.add(contactList);
        chats.setIconAt(index, ImageUtilities.loadImageIcon(PLUS, true));
        //chats.setEnabledAt(index, false);
        chats.setToolTipTextAt(index, NbBundle.getMessage(ChatTopComponent.class, "LBL_MoreChats"));

    }
    private void putLoginScreen() {
        Runnable r = new Runnable() {
            public void run() {
                removeAll();
                clearChatsTabbedPane();
                open.clear();
                add(loginScreen, BorderLayout.CENTER);
                validate();
                repaint();
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
                clearChatsTabbedPane();
                open.clear();
                add(errorScreen, BorderLayout.CENTER);
                validate();
                repaint();
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
                clearChatsTabbedPane();
                open.clear();
                ((JXBusyLabel) initLabel).setBusy(true);
                add(initPanel, BorderLayout.CENTER);
                validate();
                repaint();
            }
        };
        if (SwingUtilities.isEventDispatchThread()) {
            r.run();
        } else {
            SwingUtilities.invokeLater(r);
        }
    }

    private int getTab(String name) {
        for (int i= 0; i<chats.getTabCount(); i++) {
            if (name.equals(chats.getComponentAt(i).getName())) {
                return i;
            }
        }
        return -1;
    }

    public void setActiveGroup(String name) {
        ChatNotifications.getDefault().removeGroup(name);
        int indexOfTab = getTab(name); 
        if (indexOfTab < 0) {
            MultiUserChat muc = kec.getChat(name);
            if (muc != null) {
                ChatPanel chatPanel = new ChatPanel(muc);
                addChat(chatPanel);
                indexOfTab=chats.getTabCount()-1;
                chats.setSelectedComponent(chatPanel);
            }

        } else {
            chats.setSelectedIndex(indexOfTab);
        }
    }
    public void setActivePrivate(String name) {
        ChatNotifications.getDefault().removePrivate(name);
        int indexOfTab = getTab(createPrivateName(name));
        if (indexOfTab < 0) {
            ChatPanel chatPanel = new ChatPanel(name + '@' + XMPP_SERVER);
            addChat(chatPanel);
            indexOfTab = chats.getTabCount() - 1;
            chats.setSelectedComponent(chatPanel);
        } else {
            chats.setSelectedIndex(indexOfTab);
        }
    }

    public static final String createPrivateName(String name) {
        return "private." + name;
    }

    public void addChat(ChatPanel chatPanel) { 
        //ChatNotifications.getDefault().removeGroup(chatPanel.getName());
        int idx = chats.getTabCount();
        chats.add(chatPanel);
        try {
            if (!chatPanel.isPrivate())
                chats.setTitleAt(idx, Kenai.getDefault().getProject(chatPanel.getName()).getDisplayName());
            else {
                chats.setTitleAt(idx, chatPanel.getName().substring(chatPanel.getName().indexOf('.')+1));
            }
        } catch (KenaiException ex) {
            Exceptions.printStackTrace(ex);
        }

        if (!chatPanel.isPrivate()) {
            open.add(chatPanel.getName());
            storeOpenChats();
        }
        chats.setSelectedComponent(chatPanel);
        validate();
    }

    void removeChat(ChatPanel chatPanel) {
        int index = chats.indexOfComponent(chatPanel);
        assert index>=0: "Component not found in CloseButtonTabbedPane " + chatPanel;
        open.remove(chatPanel.getName());
        chats.remove(chatPanel);
        if (chats.getSelectedIndex()==chats.getTabCount()-1 && chats.getTabCount()>1) {
            chats.setSelectedIndex(chats.getSelectedIndex()-1);
        }
        if (chatPanel.isPrivate()) {
            kec.leavePrivate(chatPanel.getPrivateName());
        } else {
            kec.leaveGroup(chatPanel.getName());
        }
        validate();
        storeOpenChats();
    }

    void setModified(ChatPanel panel) {
        int i=chats.indexOfComponent(panel);
        chats.setForegroundAt(i, Color.BLUE);
    }

    void showPopup(MouseEvent evt) {
        JPopupMenu menu = new JPopupMenu();
        HashSet<String> projectNames = new HashSet<String>();
        try {
            for (KenaiProject prj : Kenai.getDefault().getMyProjects()) {
                projectNames.add(prj.getName());
            }
        } catch (KenaiException kenaiException) {
            Exceptions.printStackTrace(kenaiException);
        }
        ArrayList<Action> tree = new ArrayList<Action>();

        for (KenaiFeature prj : kec.getMyChats()) {
            projectNames.remove(prj.getName());
            if (!open.contains(prj.getName())) {
                tree.add(new OpenChatAction(prj));
            }
        }
        for (String name:projectNames) {
            tree.add(new CreateChatAction(name));
        }
        
        Collections.sort(tree, new Comparator<Action>() {
            public int compare(Action o1, Action o2) {
                return ((String) o1.getValue(Action.NAME)).compareTo((String) o2.getValue(Action.NAME));
            }
        });

        for (Action a:tree) {
            menu.add(a);
        }
        if (menu.getComponentCount()==0) {
            final JMenuItem jMenuItem = new JMenuItem(org.openide.util.NbBundle.getMessage(ChatTopComponent.class, "CTL_NoMoreChats")); // NOI18N
            jMenuItem.setEnabled(false);
            menu.add(jMenuItem);
        }

        menu.add(new CreatePrivateChatAction());
        if (evt!=null) {
            menu.show((Component) evt.getSource(),evt.getX(), evt.getY());
        } else {
            menu.show(chats, 0, 0);
        }
    }


    public static boolean isGroupInitedAndVisible(String name) {
        return instance==null?false:instance.isShowing()&&instance.isOpened()&&instance.open.contains(name) && name.equals(instance.chats.getSelectedComponent().getName());
    }

    public static boolean isPrivateInitedAndVisible(String name) {
        return instance==null?false:instance.isShowing()&&instance.isOpened()&& name.equals(instance.chats.getSelectedComponent().getName());
    }


    private boolean initInProgress = false;
    private void putChats() {
        initInProgress =true;
        final Collection<MultiUserChat> chs = kec.getChats();
        if (chs.size()==1) {
            final MultiUserChat next = chs.iterator().next();
            ChatPanel chatPanel = new ChatPanel(next);
            addChat(chatPanel);
        } else if (chs.size()!=0) {
            String s = prefs.get(KENAI_OPEN_CHATS_PREF + Kenai.getDefault().getPasswordAuthentication().getUserName(),""); // NOI18N
            if (s.length() > 1) {
                ChatPanel chatPanel = null;
                for (String chat : s.split(",")) { // NOI18N
                    MultiUserChat muc = kec.getChat(chat);
                    if (muc != null) {
                        chatPanel = new ChatPanel(muc);
                        addChat(chatPanel);
                    } else {
                        Logger.getLogger(ChatTopComponent.class.getName()).warning("Cannot find chat " + chat);
                    }
                }
                if (chatPanel!=null)
                    ChatNotifications.getDefault().removeGroup(chatPanel.getName());
            }
        }
        validate();
        initInProgress =false;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        chats = TabbedPaneFactory.createCloseButtonTabbedPane();
        contactList = new org.netbeans.modules.kenai.collab.chat.ContactList();
        loginScreen = new javax.swing.JPanel();
        loginLink = new javax.swing.JLabel();
        errorScreen = new javax.swing.JPanel();
        lblXmppError = new javax.swing.JLabel();
        retryLink = new javax.swing.JLabel();
        initPanel = new javax.swing.JPanel();
        initLabel = new JXBusyLabel(new Dimension(16,16));

        chats.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                chatsMouseClicked(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                chatsMousePressed(evt);
            }
        });
        chats.addTab(org.openide.util.NbBundle.getMessage(ChatTopComponent.class, "ChatTopComponent.contactList.TabConstraints.tabTitle", new Object[] {}), contactList); // NOI18N

        loginScreen.setBackground(javax.swing.UIManager.getDefaults().getColor("EditorPane.background"));

        loginLink.setForeground(java.awt.Color.blue);
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
                .add(4, 4, 4)
                .add(loginLink)
                .addContainerGap(194, Short.MAX_VALUE))
        );
        loginScreenLayout.setVerticalGroup(
            loginScreenLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(loginScreenLayout.createSequentialGroup()
                .add(4, 4, 4)
                .add(loginLink)
                .addContainerGap(414, Short.MAX_VALUE))
        );

        errorScreen.setBackground(javax.swing.UIManager.getDefaults().getColor("EditorPane.background"));

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
                .add(5, 5, 5)
                .add(lblXmppError)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(retryLink)
                .addContainerGap(31, Short.MAX_VALUE))
        );
        errorScreenLayout.setVerticalGroup(
            errorScreenLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(errorScreenLayout.createSequentialGroup()
                .add(5, 5, 5)
                .add(errorScreenLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblXmppError)
                    .add(retryLink))
                .addContainerGap(413, Short.MAX_VALUE))
        );

        setLayout(new java.awt.BorderLayout());

        initPanel.setBackground(javax.swing.UIManager.getDefaults().getColor("EditorPane.background"));

        org.openide.awt.Mnemonics.setLocalizedText(initLabel, org.openide.util.NbBundle.getMessage(ChatTopComponent.class, "ChatTopComponent.initLabel.text")); // NOI18N

        org.jdesktop.layout.GroupLayout initPanelLayout = new org.jdesktop.layout.GroupLayout(initPanel);
        initPanel.setLayout(initPanelLayout);
        initPanelLayout.setHorizontalGroup(
            initPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(initPanelLayout.createSequentialGroup()
                .add(5, 5, 5)
                .add(initLabel)
                .addContainerGap(292, Short.MAX_VALUE))
        );
        initPanelLayout.setVerticalGroup(
            initPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(initPanelLayout.createSequentialGroup()
                .add(5, 5, 5)
                .add(initLabel)
                .addContainerGap(279, Short.MAX_VALUE))
        );

        add(initPanel, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void retryLinkMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_retryLinkMouseClicked
        reconnect();
}//GEN-LAST:event_retryLinkMouseClicked

    private void retryLinkMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_retryLinkMouseEntered
        retryLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
}//GEN-LAST:event_retryLinkMouseEntered

    private void retryLinkMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_retryLinkMouseExited
        retryLink.setCursor(Cursor.getDefaultCursor());
}//GEN-LAST:event_retryLinkMouseExited

    private void chatsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_chatsMouseClicked
        int tab = chats.getUI().tabForCoordinate(chats, evt.getX(), evt.getY());
        if (tab == chats.getTabCount() - 1) {
        //    showPopup(evt);
        } 
    }//GEN-LAST:event_chatsMouseClicked

    private void loginLinkMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_loginLinkMouseExited
        loginLink.setCursor(Cursor.getDefaultCursor());
}//GEN-LAST:event_loginLinkMouseExited

    private void loginLinkMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_loginLinkMouseEntered
        loginLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
}//GEN-LAST:event_loginLinkMouseEntered

    private void loginLinkMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_loginLinkMouseClicked
        UIUtils.showLogin();
}//GEN-LAST:event_loginLinkMouseClicked

    private void chatsMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_chatsMousePressed
        int tab = chats.getUI().tabForCoordinate(chats, evt.getX(), evt.getY());
        if (tab != 0) {
            if (evt.isPopupTrigger()) {
                JPopupMenu menu = new JPopupMenu();
                menu.add(new Close());
                if (chats.getTabCount() > 2) {
                    menu.add(new CloseAll());
                    menu.add(new CloseAllButCurrent());
                }
                menu.show((Component) evt.getSource(), evt.getX(), evt.getY());
            }
        }
    }//GEN-LAST:event_chatsMousePressed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTabbedPane chats;
    private org.netbeans.modules.kenai.collab.chat.ContactList contactList;
    private javax.swing.JPanel errorScreen;
    private javax.swing.JLabel initLabel;
    private javax.swing.JPanel initPanel;
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

    static void refreshContactList() {
        if (instance!=null && instance.contactList!=null)
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                instance.contactList.updateContacts();
                instance.repaint();
            }
        });
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
        prefs.put(KENAI_OPEN_CHATS_PREF + Kenai.getDefault().getPasswordAuthentication().getUserName(), b.toString()); // NOI18N
    }

    final class KenaiL implements PropertyChangeListener {

        public void propertyChange(final PropertyChangeEvent e) {
            if (Kenai.PROP_LOGIN.equals(e.getPropertyName())) {
                if (e.getNewValue() == null) {
                    putLoginScreen();
                } else {
                    kec.post(new Runnable() {
                        public void run() {
                            if (kec.isConnectionFailed()) {
                                putErrorScreen();
                            } else {
                                kec.getMyChats();
                                putChatsScreen();
                            }
                        }
                    });
                }
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
            super();
            try {
                String name = Kenai.getDefault().getProject(f.getName()).getDisplayName();
                putValue(Action.NAME, name);
            } catch (KenaiException ex) {
                Exceptions.printStackTrace(ex);
            }
            this.f = f;
        }

        public void actionPerformed(ActionEvent e) {
            addChat(new ChatPanel(kec.getChat(f)));
        }
    }

    private class Close extends AbstractAction {

        public Close() {
            super(NbBundle.getMessage(ChatTopComponent.class, "LBL_CloseWindow"));
        }

        public void actionPerformed(ActionEvent e) {
            ChatPanel panel = (ChatPanel) chats.getSelectedComponent();
            removeChat(panel);
        }
    }

    private final class CloseAll extends AbstractAction {

        public CloseAll() {
            super(NbBundle.getMessage(ChatTopComponent.class, "LBL_CloseAll"));
        }

        public void actionPerformed(ActionEvent e) {
            for (Component c:chats.getComponents()) {
                if (c instanceof ChatPanel) {
                    removeChat((ChatPanel) c);
                }
            }
        }
    }

    private class CloseAllButCurrent extends AbstractAction {

        public CloseAllButCurrent() {
            super(NbBundle.getMessage(ChatTopComponent.class, "LBL_CloseAllButCurrent"));
        }

        public void actionPerformed(ActionEvent e) {
            for (Component c:chats.getComponents()) {
                if (c instanceof ChatPanel) {
                    if (!c.isShowing())
                        removeChat((ChatPanel) c);
                }
            }
        }
    }
}

