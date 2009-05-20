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
package org.netbeans.modules.kenai.collab.chat;

import java.awt.Color;
import java.awt.Font;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.MissingResourceException;
import java.util.Random;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.packet.DelayInformation;
import org.openide.awt.HtmlBrowser.URLDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * Panel representing single ChatRoom
 * @author Jan Becicka
 */
public class ChatPanel extends javax.swing.JPanel {

    MultiUserChat muc;
    private final HTMLEditorKit editorKit;

    public ChatPanel(MultiUserChat chat) {
        this.muc=chat;
        initComponents();
        setName(StringUtils.parseName(chat.getRoom()));
        editorKit= (HTMLEditorKit) inbox.getEditorKit();

        Font font = UIManager.getFont("Label.font"); // NOI18N
        String bodyRule = "body { font-family: " + font.getFamily() + "; " + // NOI18N
                "font-size: " + font.getSize() + "pt; }"; // NOI18N
        final StyleSheet styleSheet = ((HTMLDocument) inbox.getDocument()).getStyleSheet();

        styleSheet.addRule(bodyRule);
        styleSheet.addRule(".buddy {color: black; font-weight: bold; padding: 4px;}"); // NOI18N
        styleSheet.addRule(".time {color: lightgrey; padding: 4px;"); // NOI18N
        styleSheet.addRule(".message {color: lightgrey; padding: 2px 4px;"); // NOI18N
        styleSheet.addRule(".date {color: #cc9922; padding: 7px 0;"); // NOI18N


//        users.setCellRenderer(new BuddyListCellRenderer());
//        users.setModel(new BuddyListModel(chat));
//        users.setModel(new BuddyListModel(ctrl.getRoster()));
//        chat.addParticipantListener(getBuddyListModel());
        MessagingHandleImpl handle = ChatNotifications.getDefault().getMessagingHandle(getName());
        handle.addPropertyChangeListener(new PresenceListener());
        KenaiConnection.getDefault().join(chat,new ChatListener());
        //KenaiConnection.getDefault().join(chat);
        inbox.setBackground(Color.WHITE);
        inbox.addHyperlinkListener(new HyperlinkListener() {

            public void hyperlinkUpdate(HyperlinkEvent e) {
                if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    URLDisplayer.getDefault().showURL(e.getURL());
                }
            }
        });
        outbox.setBackground(Color.WHITE);
        splitter.setResizeWeight(0.9);
        refreshOnlineStatus();
//        setUpPrivateMessages();
    }

    @Override
    public void requestFocus() {
        super.requestFocus();
        outbox.requestFocus();
    }

    private String removeTags(String body) {
        return body.replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("\n", "<br>"); // NOI18N
    }

    private String replaceLinks(String removeTags) {
        // This regexp works quite nice, should be OK in most cases (does not handle [.,?!] in the end of the URL)
        return removeTags.replaceAll("(http|https|ftp)://([a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,4}(/[^ ]*)*", "<a href=\"$0\">$0</a>"); //NOI18N
    }

//    void setUpPrivateMessages() {
//
//        final JPopupMenu popupMenu = new JPopupMenu();
//        popupMenu.add(new SendPrivateMessage());
//
//        users.addMouseListener(new MouseAdapter() {
//
//            @Override
//            public void mousePressed(MouseEvent me) {
//                processMouseEvent(me);
//            }
//
//            @Override
//            public void mouseReleased(MouseEvent e) {
//                processMouseEvent(e);
//            }
//
//            private void processMouseEvent(MouseEvent me) {
//                if (me.isPopupTrigger()) {
//                    users.setSelectedIndex(users.locationToIndex(me.getPoint()));
//                    popupMenu.show(users, me.getX(), me.getY());
//                }
//            }
//        });
//    }
//
//    private class SendPrivateMessage extends AbstractAction {
//
//        public SendPrivateMessage() {
//            super("Send Private Message");
//        }
//
//        public void actionPerformed(ActionEvent e) {
//            Buddy b = (Buddy) users.getModel().getElementAt(users.getSelectedIndex());
//            try {
//                JEditorPane pane = new JEditorPane();
//                JScrollPane scrollPane = new JScrollPane(pane);
//                DialogDescriptor sendMessage = new DialogDescriptor(scrollPane, "Send private message to " + b.getLabel());
//                DialogDisplayer.getDefault().createDialog(sendMessage).setVisible(true);
//                if (sendMessage.getValue()==DialogDescriptor.OK_OPTION) {
//                    muc.createPrivateChat(b.getJid(), null).sendMessage(pane.getText());
//                }
//            } catch (XMPPException ex) {
//                Exceptions.printStackTrace(ex);
//            }
//        }
//
//    }

    private class ChatListener implements PacketListener {

        public void processPacket(Packet packet) {
            final Message message = (Message) packet;
            java.awt.EventQueue.invokeLater(new Runnable() {
                public void run() {
                    setEndSelection();
                    insertMessage(message);
                    if (!ChatPanel.this.isVisible()) {
                        ChatTopComponent.findInstance().setModified(ChatPanel.this);
                    }
                }
            });
        }
    }

    private void refreshOnlineStatus() throws MissingResourceException {
        online.setText(NbBundle.getMessage(ChatPanel.class, "CTL_PresenceOnline", muc.getOccupantsCount()));
        Iterator<String> string = muc.getOccupants();
        StringBuffer buffer = new StringBuffer();
        buffer.append("<html><body>"); // NOI18N
        while (string.hasNext()) {
            buffer.append(StringUtils.parseResource(string.next()) + "<br>"); // NOI18N
        }
        buffer.append("</body></html>"); // NOI18N
        online.setToolTipText(buffer.toString());
    //setEndSelection();
    //insertPresence(presence);
    }

    private class PresenceListener implements PropertyChangeListener {

        public void propertyChange(PropertyChangeEvent arg0) {
            java.awt.EventQueue.invokeLater(new Runnable() {
                public void run() {
                    refreshOnlineStatus();
                }
            });

        }
    }


//    private BuddyListModel getBuddyListModel() {
//        return (BuddyListModel) users.getModel();
//    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        splitter = new javax.swing.JSplitPane();
        outboxScrollPane = new javax.swing.JScrollPane();
        outbox = new javax.swing.JTextPane();
        inboxPanel = new javax.swing.JPanel();
        inboxScrollPane = new javax.swing.JScrollPane();
        inbox = new javax.swing.JTextPane();
        online = new javax.swing.JLabel();

        splitter.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        outboxScrollPane.setBorder(null);

        outbox.setBorder(null);
        outbox.setMaximumSize(new java.awt.Dimension(0, 16));
        outbox.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                ChatPanel.this.keyTyped(evt);
            }
        });
        outboxScrollPane.setViewportView(outbox);

        splitter.setRightComponent(outboxScrollPane);

        inboxPanel.setBackground(java.awt.Color.white);
        inboxPanel.setLayout(new java.awt.BorderLayout());

        inboxScrollPane.setBorder(null);
        inboxScrollPane.setViewportBorder(null);

        inbox.setBorder(null);
        inbox.setContentType("text/html"); // NOI18N
        inbox.setEditable(false);
        inbox.setText(org.openide.util.NbBundle.getMessage(ChatPanel.class, "ChatPanel.inbox.text", new Object[] {})); // NOI18N
        inboxScrollPane.setViewportView(inbox);

        inboxPanel.add(inboxScrollPane, java.awt.BorderLayout.CENTER);

        online.setBackground(java.awt.Color.white);
        online.setForeground(java.awt.Color.blue);
        online.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        online.setText(org.openide.util.NbBundle.getMessage(ChatPanel.class, "ChatPanel.online.text", new Object[] {})); // NOI18N
        online.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 1, 3, 1));
        inboxPanel.add(online, java.awt.BorderLayout.PAGE_START);

        splitter.setTopComponent(inboxPanel);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(splitter, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 268, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(splitter, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 402, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void keyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_keyTyped
        if (evt.getKeyChar() == '\n') {
            if (evt.isAltDown() || evt.isShiftDown() || evt.isControlDown()) {
                try {
                    outbox.getStyledDocument().insertString(outbox.getCaretPosition(), "\n", null); //NOI18N
                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                }
                return;
            }
            try {
                if (!KenaiConnection.getDefault().isConnected()) {
                    try {
                        KenaiConnection.getDefault().reconnect();
                    } catch (XMPPException xMPPException) {
                        JOptionPane.showMessageDialog(this, xMPPException.getMessage());
                        return;
                    }
                }
                if (!outbox.getText().trim().equals("")) {
                    //remove NL if before the caret...
                    int pos = outbox.getCaretPosition();
                    if (pos > 1 && (outbox.getText().charAt(pos - 1) == '\n' || outbox.getText().charAt(pos - 1) == '\r'))  {
                        try {
                            boolean tryRemoveR = outbox.getText().charAt(pos - 1) == '\n'; // it can be \r\n, \n to be removed
                            outbox.getDocument().remove(pos - 1, 1);
                            pos = outbox.getCaretPosition();
                            if (tryRemoveR && pos > 1 && outbox.getText().charAt(pos - 1) == '\r')  {
                                outbox.getDocument().remove(pos - 1, 1);
                            }
                        } catch (BadLocationException ex) {
                            // harmless
                        }
                    }
                    muc.sendMessage(outbox.getText().trim());
                }
            } catch (XMPPException ex) {
                Exceptions.printStackTrace(ex);
            }
            outbox.setText("");
        }
    }//GEN-LAST:event_keyTyped


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextPane inbox;
    private javax.swing.JPanel inboxPanel;
    private javax.swing.JScrollPane inboxScrollPane;
    private javax.swing.JLabel online;
    private javax.swing.JTextPane outbox;
    private javax.swing.JScrollPane outboxScrollPane;
    private javax.swing.JSplitPane splitter;
    // End of variables declaration//GEN-END:variables


    private Date lastDatePrinted;
    private Date lastMessageDate;
    private String lastNickPrinted = null;
    private String rgb = null;

    protected void insertMessage(Message message) {
        try {
            HTMLDocument doc = (HTMLDocument) inbox.getStyledDocument();
            final Date timestamp = getTimestamp(message);
            String fromRes = StringUtils.parseResource(message.getFrom());
            Random random = new Random(fromRes.hashCode());
            float randNum = random.nextFloat();
            Color headerColor = Color.getHSBColor(randNum, 0.1F, 0.95F);
            Color messageColor = Color.getHSBColor(randNum, 0.1F, 1.0F);
            boolean printheader = ((lastNickPrinted != null)?(!lastNickPrinted.equals(fromRes)):true); //Nickname is different from the last one, or...
            printheader |= (lastMessageDate != null && timestamp != null)?(timestamp.getTime() > lastMessageDate.getTime() + 120000):true;
            lastNickPrinted = fromRes;
            lastMessageDate = timestamp;
            if (!isSameDate(lastDatePrinted,timestamp)) {
                lastDatePrinted = timestamp;
                printheader = true;
                rgb = null;
                String d = "<table border=\"0\" borderwith=\"0\" width=\"100%\"><tbody><tr><td class=\"date\" align=\"left\">" + // NOI18N
                    (isToday(timestamp)?NbBundle.getMessage(ChatPanel.class, "LBL_Today"):DateFormat.getDateInstance().format(timestamp)) + "</td><td class=\"date\" align=\"right\">" + // NOI18N
                    DateFormat.getTimeInstance(DateFormat.SHORT).format(timestamp) + "</td></tr></tbody></table>"; // NOI18N
                editorKit.insertHTML(doc, doc.getLength(), d, 0, 0, null);
            }
            String text = "";
            if (printheader) {
                if (rgb != null) {
                    text += "<div style=\"height: 3px; background-color: rgb(" + rgb + ")\"></div>";
                }
                text += "<table border=\"0\" borderwith=\"0\" width=\"100%\"><tbody>" + //NOI18N
                        "<tr style=\"background-color: rgb(" + headerColor.getRed() + "," + headerColor.getGreen() + "," + headerColor.getBlue() + ")\">" + //NOI18N
                        "<td class=\"buddy\" align=\"left\">"+ fromRes + "</td><td class=\"time\" align=\"right\">" + // NOI18N
                        DateFormat.getTimeInstance(DateFormat.SHORT).format(getTimestamp(message)) + "</td></tr></tbody></table>"; // NOI18N
            }
            rgb = messageColor.getRed() + "," + messageColor.getGreen() + "," + messageColor.getBlue(); // NOI18N
            text += "<div class=\"message\" style=\"background-color: rgb(" + rgb + ")\">" + replaceLinks(removeTags(message.getBody())) + "</div>"; // NOI18N

            editorKit.insertHTML(doc, doc.getLength(), text, 0, 0, null);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    public static Date getTimestamp(Packet packet) {
         DelayInformation delay = (DelayInformation) packet.getExtension("x", "jabber:x:delay"); // NOI18N
         if (delay != null) {
             return delay.getStamp();
         } else {
             //this is realtime message
             return new Date();
         }
    }

    boolean isToday(Date date) {
        return isSameDate(date, new Date());
    }

    boolean isSameDate(Date date, Date date2) {
        if (date==null) {
            return false;
        }
        return date.getDate() == date2.getDate() && date.getMonth() == date2.getMonth() && date.getYear() == date2.getYear();
    }


    protected void insertPresence(Presence presence) {
        try {
            HTMLDocument doc = (HTMLDocument) inbox.getStyledDocument();
            String text = "<i><b>" + StringUtils.parseResource(presence.getFrom()) + "</b> is now " + presence.getType() + "</i>"; // NOI18N
            editorKit.insertHTML(doc, doc.getLength(), text, 0, 0, null);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }


    // Needed for inserting icons in the right places
    protected void setEndSelection() {
        inbox.setSelectionStart(inbox.getDocument().getLength());
        inbox.setSelectionEnd(inbox.getDocument().getLength());
    }

//    void setUsersListVisible(boolean visible) {
//        usersScrollPane.setVisible(visible);
//    }
}
