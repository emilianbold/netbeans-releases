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

import java.awt.Color;
import java.awt.event.KeyEvent;
import javax.swing.DefaultListCellRenderer.UIResource;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.SwingConstants;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.netbeans.modules.kenai.collab.im.KenaiConnection;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.util.StringUtils;
import org.openide.util.Exceptions;

/**
 * Panel representing single ChatRoom
 * @author Jan Becicka
 */
public class ChatPanel extends javax.swing.JPanel {

    private KenaiConnection ctrl;
    static final SimpleAttributeSet ITALIC_GRAY = new SimpleAttributeSet();
    static final SimpleAttributeSet BOLD_BLACK = new SimpleAttributeSet();
    static final SimpleAttributeSet BLACK = new SimpleAttributeSet();

    static {
        StyleConstants.setForeground(ITALIC_GRAY, Color.gray);
        StyleConstants.setItalic(ITALIC_GRAY, true);
        StyleConstants.setFontFamily(ITALIC_GRAY, "Helvetica");
        StyleConstants.setFontSize(ITALIC_GRAY, 14);

        StyleConstants.setForeground(BOLD_BLACK, Color.black);
        StyleConstants.setBold(BOLD_BLACK, true);
        StyleConstants.setFontFamily(BOLD_BLACK, "Helvetica");
        StyleConstants.setFontSize(BOLD_BLACK, 14);
        StyleConstants.setFontSize(BOLD_BLACK, 14);

        StyleConstants.setForeground(BLACK, Color.black);
        StyleConstants.setFontFamily(BLACK, "Helvetica");
        StyleConstants.setFontSize(BLACK, 14);
    }


    public ChatPanel(MultiUserChat chat) {
        initComponents();
        this.ctrl = KenaiConnection.getDefault();
        users.setCellRenderer(new BuddyListCellRenderer());
        users.setModel(new BuddyListModel(chat));
        //users.setModel(new BuddyListModel(ctrl.getRoster()));
        chat.addParticipantListener(getBuddyListModel());
        chat.addParticipantListener(new PresenceListener());
        chat.addMessageListener(new ChatListener());
        inbox.setBackground(Color.WHITE);
        outbox.setBackground(Color.WHITE);
    }

    private class ChatListener implements PacketListener {

        public void processPacket(Packet packet) {
            final Message message = (Message) packet;
            java.awt.EventQueue.invokeLater(new Runnable() {
                public void run() {
                    setEndSelection();
                    insertText(StringUtils.parseResource(message.getFrom()) + ": ", ChatPanel.ITALIC_GRAY);
                    insertText(message.getBody() + "\n", ChatPanel.BLACK);
                }
            });
        }
    }

    private class PresenceListener implements PacketListener {

        public void processPacket(Packet packet) {
            final Presence presence = (Presence) packet;
            java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                setEndSelection();
                insertText(StringUtils.parseResource(presence.getFrom()) + " is now ", ChatPanel.ITALIC_GRAY);
                insertText(presence.getType() + "\n", ChatPanel.ITALIC_GRAY);
            }
        });
        }
    }


    private BuddyListModel getBuddyListModel() {
        return (BuddyListModel) users.getModel();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        usersScrollPane = new javax.swing.JScrollPane();
        users = new javax.swing.JList();
        splitter = new javax.swing.JSplitPane();
        inboxScrollPane = new javax.swing.JScrollPane();
        inbox = new javax.swing.JTextPane();
        outboxScrollPane = new javax.swing.JScrollPane();
        outbox = new javax.swing.JTextPane();
        sendButton = new javax.swing.JButton();

        usersScrollPane.setViewportView(users);

        splitter.setDividerLocation(200);
        splitter.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        inbox.setEditable(false);
        inboxScrollPane.setViewportView(inbox);

        splitter.setLeftComponent(inboxScrollPane);

        outbox.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                ChatPanel.this.keyTyped(evt);
            }
        });
        outboxScrollPane.setViewportView(outbox);

        splitter.setRightComponent(outboxScrollPane);

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/kenai/collab/chat/ui/Bundle"); // NOI18N
        sendButton.setText(bundle.getString("ChatPanel.sendButton.text")); // NOI18N
        sendButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sendButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(layout.createSequentialGroup()
                        .addContainerGap(375, Short.MAX_VALUE)
                        .add(sendButton))
                    .add(splitter, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 414, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(usersScrollPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 122, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(splitter, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 517, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(sendButton))
            .add(org.jdesktop.layout.GroupLayout.TRAILING, usersScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 551, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void keyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_keyTyped
        if (evt.getKeyChar() == '\n' && evt.getModifiers() == KeyEvent.ALT_MASK) {
            sendButton.doClick();
        }
    }//GEN-LAST:event_keyTyped

    private void sendButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sendButtonActionPerformed
        try {
            ctrl.getChats().first().sendMessage(outbox.getText().trim());
        } catch (XMPPException ex) {
            Exceptions.printStackTrace(ex);
        }
        outbox.setText("");
    }//GEN-LAST:event_sendButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextPane inbox;
    private javax.swing.JScrollPane inboxScrollPane;
    private javax.swing.JTextPane outbox;
    private javax.swing.JScrollPane outboxScrollPane;
    private javax.swing.JButton sendButton;
    private javax.swing.JSplitPane splitter;
    private javax.swing.JList users;
    private javax.swing.JScrollPane usersScrollPane;
    // End of variables declaration//GEN-END:variables

    protected void insertText(String text, AttributeSet set) {
        try {
            StyledDocument doc = inbox.getStyledDocument();
            doc.insertString(doc.getLength(), text, set);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    // Needed for inserting icons in the right places
    protected void setEndSelection() {
        inbox.setSelectionStart(inbox.getDocument().getLength());
        inbox.setSelectionEnd(inbox.getDocument().getLength());
    }

    void setUsersListVisible(boolean visible) {
        usersScrollPane.setVisible(visible);
    }
}
