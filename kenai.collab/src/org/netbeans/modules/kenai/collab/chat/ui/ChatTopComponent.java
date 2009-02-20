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
import java.awt.Color;
import java.awt.Component;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;
import javax.swing.OverlayLayout;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.netbeans.modules.kenai.api.KenaiProject;
import org.netbeans.modules.kenai.collab.im.KenaiConnection;
import org.netbeans.modules.kenai.ui.spi.UIUtils;
import org.openide.awt.Actions.MenuItem;
import org.openide.awt.TabbedPaneFactory;
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

    private static final String PREFERRED_ID = "ChatTopComponent";

    //open chats
    private HashSet<String> open = new HashSet<String>();

    private ChatTopComponent() {
        initComponents();
        setName(NbBundle.getMessage(ChatTopComponent.class, "CTL_ChatTopComponent"));
        setToolTipText(NbBundle.getMessage(ChatTopComponent.class, "HINT_ChatTopComponent"));
        setIcon(ImageUtilities.loadImage(ICON_PATH, true));

        final JPanel panel = new JPanel() {
            @Override
            public boolean isOptimizedDrawingEnabled() {
                return false;
            }
        };
        LayoutManager overlay = new OverlayLayout(panel);
        panel.setLayout(overlay);
        glassPane.setAlignmentX(0);
        glassPane.setAlignmentY(0);
        chats.setAlignmentX(0);
        chats.setAlignmentY(0);
        panel.add(glassPane);
        panel.add(chats);

        ChangeListener changeListener = new ChangeListener() {

            public void stateChanged(ChangeEvent changeEvent) {
                int index = chats.getSelectedIndex();
                chats.setForegroundAt(index, Color.BLACK);
            }
        };

        chats.addChangeListener(changeListener);
        if (!KenaiConnection.getDefault().isConnected()) {
            retry.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (!KenaiConnection.getDefault().isConnected()) {
                        UIUtils.showLogin();
                        remove(retry);
                        add(panel, BorderLayout.CENTER);
                        putChats();
                    }
                }
            });
            add(retry, BorderLayout.CENTER);
        } else {
            add(panel, BorderLayout.CENTER);
            putChats();
        }
        //putClientProperty("netbeans.winsys.tc.keep_preferred_size_when_slided_in", Boolean.TRUE);
    }

    public static synchronized void reload() {
        if (instance==null)
            return;
        else {
            instance.open.clear();
            instance.putChats();
        }
    }

    public void setActive(String parseName) {
        chats.setSelectedIndex(chats.indexOfTab(parseName));
    }


    void addChat(ChatPanel chatPanel) {
        chats.addTab(chatPanel.getName(),chatPanel);
        open.add(chatPanel.getName());
    }

    void removeChat(Component chatPanel) {
// currently does not work
//        open.remove(chats.getTitleAt(chats.indexOfTabComponent(chatPanel)));
//        chats.remove(chatPanel);
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



    public boolean isHandled(Message msg) {
        return open.contains(StringUtils.parseName(msg.getFrom()));
    }

    private void putChats() {
        final Collection<MultiUserChat> chs = KenaiConnection.getDefault().getChats();
        if (chs.size()==1) {
            final MultiUserChat next = chs.iterator().next();
            ChatPanel chatPanel = new ChatPanel(next);
            addChat(chatPanel);
        } else if (chs.size()!=0) {
            SwingUtilities.invokeLater(new Runnable(){
                public void run() {
                    //showPopup();
                }
            });
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

        chats.setTabLayoutPolicy(javax.swing.JTabbedPane.SCROLL_TAB_LAYOUT);
        chats.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                chatsPropertyChange(evt);
            }
        });

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

        setLayout(new java.awt.BorderLayout());
    }// </editor-fold>//GEN-END:initComponents

    private void addChatActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addChatActionPerformed
        showPopup();
}//GEN-LAST:event_addChatActionPerformed

    private void chatsPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_chatsPropertyChange
        if (TabbedPaneFactory.PROP_CLOSE.equals(evt.getPropertyName())) {
            this.removeChat((Component) evt.getNewValue());
        }
    }//GEN-LAST:event_chatsPropertyChange

     private JButton retry = new JButton("Login");
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addChat;
    private javax.swing.JTabbedPane chats;
    private javax.swing.JPanel glassPane;
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
        return new ResolvableHelper(open);
    }

    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }

    final static class ResolvableHelper implements Serializable {

        private static final long serialVersionUID = 1L;

        private final HashSet<String> open;
        public ResolvableHelper(HashSet<String> open) {
            this.open=open;
        }

        public Object readResolve() {
            final ChatTopComponent tc = ChatTopComponent.getDefault();
            if (open!=null)
                for (String chat:open)
                    tc.addChat(new ChatPanel(KenaiConnection.getDefault().getChat(chat)));
            return tc;
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
