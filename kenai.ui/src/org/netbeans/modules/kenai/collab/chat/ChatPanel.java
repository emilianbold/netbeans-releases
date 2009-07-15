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

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.Timer;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.*;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.packet.DelayInformation;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.kenai.api.Kenai;
import org.netbeans.modules.kenai.api.KenaiException;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.awt.HtmlBrowser.URLDisplayer;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.LineCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.text.Line;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import static org.netbeans.modules.kenai.collab.chat.ChatTopComponent.*;

/**
 * Panel representing single ChatRoom
 * @author Jan Becicka
 */
public class ChatPanel extends javax.swing.JPanel {

    private MultiUserChat muc;
    private Chat suc;
    private boolean disableAutoScroll = false;
    private HTMLEditorKit editorKit = null;
    private static final String[][] smileysMap = new String[][] {
        {"B)", "cool"}, // NOI18N
        {"B-)", "cool"}, // NOI18N
        {"8-)", "cool"}, // NOI18N
        {":]", "grin"}, // NOI18N
        {":-]", "grin"}, // NOI18N
        {":D", "laughing"}, // NOI18N
        {":-D", "laughing"}, // NOI18N
        {":(", "sad"}, // NOI18N
        {":-(", "sad"}, // NOI18N
        {":)", "smiley"}, // NOI18N
        {":-)", "smiley"}, // NOI18N
        {";)", "wink"}, // NOI18N
        {";-)", "wink"} // NOI18N
    };
    private CompoundUndoManager undo;
    private MessageHistoryManager history = new MessageHistoryManager();

    private static final String STACK_TRACE_STRING =
            "(|catch.)at.((?:[a-zA-Z_$][a-zA-Z0-9_$]*\\.)*)[a-zA-Z_$][a-zA-Z0-9_$]*\\.[a-zA-Z_$<][a-zA-Z0-9_$>]*\\(([a-zA-Z_$][a-zA-Z0-9_$]*\\.java):([0-9]+)\\)";//NOI18N
    /**
     * Regexp matching one line (not the first) of a stack trace.
     * Captured groups:
     * <ol>
     * <li>package
     * <li>filename
     * <li>line number
     * </ol>
     */
    private static final Pattern STACK_TRACE = Pattern.compile(STACK_TRACE_STRING);

    private static final String EXCEPTION_MESSAGE_STRING = 
            "(?:Exception in thread \"(?:main|Main Thread)\" )?(?:(?:[a-zA-Z_$][a-zA-Z0-9_$]*\\.)+)([a-zA-Z_$][a-zA-Z0-9_$]*(?:: .+)?)";//NOI18N
    /**
     * Regexp matching the first line of a stack trace, with the exception message.
     * Captured groups:
     * <ol>
     * <li>unqualified name of exception class plus possible message
     * </ol>
     */
    private static final Pattern EXCEPTION_MESSAGE = Pattern.compile(
    // #42894: JRockit uses "Main Thread" not "main"
    EXCEPTION_MESSAGE_STRING); 

    private static final String CLASSPATH_RESOURCE_STRING = 
            "(([a-zA-Z_$][a-zA-Z0-9_$]*/)+)(([a-zA-Z_$][a-zA-Z0-9_$]*\\.[a-zA-Z0-9_$]*):([0-9]+))";//NOI18N
    /**
     * group(5) is line
     * group(1) is folder
     * group(4) is file
     */
    private static final Pattern CLASSPATH_RESOURCE = Pattern.compile(CLASSPATH_RESOURCE_STRING);

    private static final String ABSOLUTE_RESOURCE_STRING = 
            "/(([a-zA-Z_$][a-zA-Z0-9_$]*/)+)(([a-zA-Z_$][a-zA-Z0-9_$]*\\.[a-zA-Z0-9_$]*):([0-9]+))";//NOI18N
    /**
     * group(5) is line
     * group(1) is folder
     * group(4) is file
     */
    private static final Pattern ABSOLUTE_RESOURCE = Pattern.compile(ABSOLUTE_RESOURCE_STRING);

    private static final String PROJECT_RESOURCE_STRING = 
            "\\{\\$([a-zA-Z_$][\\.a-zA-Z0-9_$]*)\\}/(([a-zA-Z_$][a-zA-Z0-9_$]*/)*)(([a-zA-Z_$][a-zA-Z0-9_$]*\\.[a-zA-Z0-9_$]*):([0-9]+))";//NOI18N

    /**
     * group(6) is line
     * group(2) is folder
     * group(5) is file
     * group(1) is project name
     */
    private static final Pattern PROJECT_RESOURCE = Pattern.compile(PROJECT_RESOURCE_STRING);

    private static final Pattern RESOURCES =
            Pattern.compile("("+STACK_TRACE_STRING+")|("+CLASSPATH_RESOURCE_STRING +")|("+PROJECT_RESOURCE_STRING +")|("+ABSOLUTE_RESOURCE_STRING + ")");//NOI18N

    private void selectStackTrace(Matcher m) {
        String pkg = m.group(2);
        String filename = m.group(3);
        String resource = pkg.replace('.', '/') + filename;
        int lineNumber = Integer.parseInt(m.group(4));
        org.netbeans.api.java.classpath.ClassPath cp = ClassPathSupport.createClassPath(GlobalPathRegistry.getDefault().getSourceRoots().toArray(new FileObject[0]));
        FileObject source = cp.findResource(resource);
        if (source != null) {
            doOpen(source, lineNumber);
        }
    }

    private void selectClasspathResource(Matcher m) {
        String resource = m.group(1) + m.group(4);
        int lineNumber = Integer.parseInt(m.group(5));
        org.netbeans.api.java.classpath.ClassPath cp = ClassPathSupport.createClassPath(GlobalPathRegistry.getDefault().getSourceRoots().toArray(new FileObject[0]));
        FileObject source = cp.findResource(resource);
        if (source != null) {
            doOpen(source, lineNumber);
        }
    }

    private void selectProjectResource(Matcher m) {
        String resource = m.group(2) + m.group(5);
        int lineNumber = Integer.parseInt(m.group(6));
        for (Project p:OpenProjects.getDefault().getOpenProjects()) {
            if (ProjectUtils.getInformation(p).getName().equals(m.group(1))) {
                FileObject source = p.getProjectDirectory().getFileObject(resource);
                doOpen(source, lineNumber);
                return;
            }
        }
    }

    private void selectAbsoluteResource(Matcher m) {
        String resource = "/" + m.group(1) + m.group(4);  //NOI18N
        int lineNumber = Integer.parseInt(m.group(5));
        File file = new File(resource);
        if (file.exists()) {
            FileObject source = FileUtil.toFileObject(FileUtil.normalizeFile(file));
                doOpen(source, lineNumber);
        }
    }

    private static boolean doOpen(FileObject fo, int line) {
        try {
            DataObject od = DataObject.find(fo);
            EditorCookie ec = (EditorCookie) od.getCookie(EditorCookie.class);
            LineCookie lc = (LineCookie) od.getCookie(LineCookie.class);

            if (ec != null && lc != null && line != -1) {
                StyledDocument doc = ec.openDocument();
                if (doc != null) {
                    if (line != -1) {
                        Line l = null;
                        try {
                            l = lc.getLineSet().getCurrent(line - 1);
                        } catch (IndexOutOfBoundsException e) { // try to open at least the file (line no. is too high?)
                            l = lc.getLineSet().getCurrent(0);
                        }

                        if (l != null) {
                            l.show(Line.ShowOpenType.OPEN, Line.ShowVisibilityType.FOCUS);
                            return true;
                        }
                    }
                }
            }

            OpenCookie oc = (OpenCookie) od.getCookie(OpenCookie.class);

            if (oc != null) {
                oc.open();
                return true;
            }
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
        }
        return false;
    }

    public ChatPanel(MultiUserChat muc) {
        super();
        this.muc=muc;
        setName(StringUtils.parseName(muc.getRoom()));
        init();
        this.muc.addParticipantListener(new PacketListener() {
            public void processPacket(Packet presence) {
                insertPresence((Presence) presence);
            }
        });
        KenaiConnection.getDefault().join(muc,new ChatListener());

    }

    public ChatPanel(String jid) {
        super();
        setName(createPrivateName(StringUtils.parseName(jid)));
        init();
        this.suc = KenaiConnection.getDefault().joinPrivate(jid, new ChatListener());
    }

    public boolean isPrivate() {
        return getName().startsWith("private.");  //NOI18N
    }

    public String getPrivateName() {
        assert isPrivate();
        return getName().substring(getName().indexOf('.')+1);
    }

    
    private void init() {
        initComponents();
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
        if (!isPrivate()) {
            MessagingHandleImpl handle = ChatNotifications.getDefault().getMessagingHandle(getName());
            handle.addPropertyChangeListener(new PresenceListener());
        }
        //KenaiConnection.getDefault().join(chat);
        inbox.setBackground(Color.WHITE);
        inbox.addHyperlinkListener(new HyperlinkListener() {

            public void hyperlinkUpdate(HyperlinkEvent e) {
                if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    if (e.getURL()!=null) {
                        URLDisplayer.getDefault().showURL(e.getURL());
                    } else {
                        String link = e.getDescription();
                        Matcher m = STACK_TRACE.matcher(link);
                        if (m.matches()) {
                            selectStackTrace(m);
                        } else {
                            m=CLASSPATH_RESOURCE.matcher(link);
                            if (m.matches()) {
                                selectClasspathResource(m);
                            } else {
                                m=PROJECT_RESOURCE.matcher(link);
                                if (m.matches()) {
                                    selectProjectResource(m);
                                } else {
                                    m=ABSOLUTE_RESOURCE.matcher(link);
                                    if (m.matches()) {
                                        selectAbsoluteResource(m);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        });
        outbox.setBackground(Color.WHITE);
        splitter.setResizeWeight(0.9);
        refreshOnlineStatus();
        NotificationsEnabledAction bubbleEnabled = new NotificationsEnabledAction();
        inbox.addMouseListener(bubbleEnabled);
        outbox.addMouseListener(bubbleEnabled);
        undo = new CompoundUndoManager(outbox);

        inboxScrollPane.addMouseWheelListener(new MouseWheelListener() {

            public void mouseWheelMoved(MouseWheelEvent e) {
                JScrollBar vbar = inboxScrollPane.getVerticalScrollBar();
                if (vbar==null)
                    return;
                disableAutoScroll = ((vbar.getValue() + vbar.getVisibleAmount()) != vbar.getMaximum());
            }
        });
        inboxScrollPane.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {

            public void adjustmentValueChanged(AdjustmentEvent event) {
                JScrollBar vbar = (JScrollBar) event.getSource();

                if (!event.getValueIsAdjusting()) {
                    return;
                }
                disableAutoScroll = ((vbar.getValue() + vbar.getVisibleAmount()) != vbar.getMaximum());
            }
        });
    }

    private class NotificationsEnabledAction extends MouseAdapter implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            JCheckBoxMenuItem m = (JCheckBoxMenuItem) e.getSource();
            ChatNotifications.getDefault().setEnabled(getName(),m.getState());
        }

        @Override
        public void mousePressed(MouseEvent e) {
            if (e.isPopupTrigger()) {
                try {
                    JPopupMenu menu = new JPopupMenu();
                    String name = isPrivate()?getPrivateName():Kenai.getDefault().getProject(getName()).getDisplayName();
                    JCheckBoxMenuItem jCheckBoxMenuItem = new JCheckBoxMenuItem(
                            NbBundle.getMessage(ChatPanel.class, "CTL_NotificationsFor", new Object[]{name}),  //NOI18N
                            ChatNotifications.getDefault().isEnabled(getName()));
                    jCheckBoxMenuItem.addActionListener(this);
                    menu.add(jCheckBoxMenuItem);
                    menu.show((Component) e.getSource(), e.getX(), e.getY());
                } catch (KenaiException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }

    @Override
    public void requestFocus() {
        super.requestFocus();
        outbox.requestFocus();
    }

    private String removeTags(String body) {
        String tmp = body;
        tmp.replaceAll("\r\n", "\n"); // NOI18N
        tmp.replaceAll("\r", "\n"); // NOI18N
        return tmp.replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("\n", "<br>"); // NOI18N
    }

    private String replaceLinks(String body) {
        // This regexp works quite nice, should be OK in most cases (does not handle [.,?!] in the end of the URL)
        String result = body.replaceAll("(http|https|ftp)://([a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,4}(/[^ ]*)*", "<a href=\"$0\">$0</a>"); //NOI18N

        result = RESOURCES.matcher(result).replaceAll("<a href=\"$0\">$0</a>");  //NOI18N
        return result.replaceAll("  ", " &nbsp;"); //NOI18N
    }

    private String replaceSmileys(String body) {
        if (body.matches(".*[B8:;]-?[]D()].*")) { // NOI18N
            for (int i = 0; i < smileysMap.length; i++) {
                body = body.replace(smileysMap[i][0],
                        "<img align=\"center\" src=\"" + // NOI18N
                        this.getClass().getResource("/org/netbeans/modules/kenai/collab/resources/emo_" + smileysMap[i][1] + "16.png") +  //NOI18N
                        "\"></img>"); // NOI18N
            }
        }
        return body;
    }

    private class ChatListener implements PacketListener, MessageListener {

        public void processPacket(Packet packet) {
            processMessage(suc,  (Message) packet);
        }

        public void processMessage(Chat arg0, final Message message) {
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
        if (muc!=null) {
        online.setText(NbBundle.getMessage(ChatPanel.class, "CTL_PresenceOnline", muc.getOccupantsCount()));  //NOI18N
        Iterator<String> string = muc.getOccupants();
        StringBuffer buffer = new StringBuffer();
        buffer.append("<html><body>"); // NOI18N
        while (string.hasNext()) {
            buffer.append(StringUtils.parseResource(string.next()) + "<br>"); // NOI18N
        }
        buffer.append("</body></html>"); // NOI18N
        online.setToolTipText(buffer.toString());
        }
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
        inbox = new JTextPane() {
            public void scrollRectToVisible(Rectangle aRect) {
                if (!disableAutoScroll)
                super.scrollRectToVisible(aRect);
            }
        };
        topPanel = new javax.swing.JPanel();
        online = new javax.swing.JLabel();
        statusLine = new javax.swing.JLabel();

        splitter.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        outboxScrollPane.setBorder(null);

        outbox.setBorder(null);
        outbox.setMaximumSize(new java.awt.Dimension(0, 16));
        outbox.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                outboxKeyPressed(evt);
            }
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

        topPanel.setBackground(java.awt.Color.white);
        topPanel.setLayout(new java.awt.BorderLayout());

        online.setBackground(java.awt.Color.white);
        online.setForeground(java.awt.Color.blue);
        online.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        online.setText(org.openide.util.NbBundle.getMessage(ChatPanel.class, "ChatPanel.online.text", new Object[] {})); // NOI18N
        online.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 1, 3, 1));
        topPanel.add(online, java.awt.BorderLayout.EAST);
        topPanel.add(statusLine, java.awt.BorderLayout.CENTER);

        inboxPanel.add(topPanel, java.awt.BorderLayout.NORTH);

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

    List undoCharsList = Arrays.asList(' ', '.', ',', '!', '\t', '?', ':', ';');

    private void keyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_keyTyped
        if (undoCharsList.contains(evt.getKeyChar())) { // undo state when one of special chars...
            undo.startNewCompoundEdit();
            return;
        }
        if (evt.getKeyChar() == '\n' || evt.getKeyChar() == '\r') {
            if (evt.isAltDown() || evt.isShiftDown() || evt.isControlDown()) {
                try {
                    outbox.getStyledDocument().insertString(outbox.getCaretPosition(), "\r\n", null); //NOI18N
                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                }
                return;
            }
            try {
                if (muc!=null&& (!KenaiConnection.getDefault().isConnected() || !muc.isJoined())) {
                    try {
                        KenaiConnection.getDefault().reconnect(muc);
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
                    if (muc!=null)
                        muc.sendMessage(outbox.getText().trim());
                    else {
                        Message m = new Message(suc.getParticipant());
                        m.setBody(outbox.getText().trim());
                        suc.sendMessage(m);
                        insertMessage(m);
                    }
                }
            } catch (XMPPException ex) {
                Exceptions.printStackTrace(ex);
            }
            outbox.setText("");
            undo.discardAllEdits();
            history.resetHistory();
        }
    }//GEN-LAST:event_keyTyped

    private void outboxKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_outboxKeyPressed
        if (evt.isControlDown() && evt.getKeyCode() == KeyEvent.VK_Z) {
            try {
                undo.undo();
                history.resetHistory();
            } catch (CannotUndoException e) {
                // end of the undo history
            }
            return;
        }
        if (evt.isControlDown() && evt.getKeyCode() == KeyEvent.VK_Y) {
            try {
                undo.redo();
                history.resetHistory();
            } catch (CannotRedoException e) {
                // end of the redo history
            }
            return;
        }
        if (evt.isControlDown() || evt.isAltDown() || evt.isShiftDown()) {
            if (evt.getKeyCode() == KeyEvent.VK_UP) {
                if (history.isOnStart()) {
                    history.setEditedMessage(outbox.getText());
                }
                String mess = history.getPreviousMessage();
                if (mess != null) {
                    outbox.setText(mess);
                }
                return;
            }
            if (evt.getKeyCode() == KeyEvent.VK_DOWN) {
                String message = history.getNextMessage();
                if (message != null) {
                    outbox.setText(message);
                }
                return;
            }
        }
        if (evt.isControlDown() && evt.getKeyCode() == KeyEvent.VK_P) {
            JTextComponent component = EditorRegistry.lastFocusedComponent();
            if (component!=null) {
                Document document = component.getDocument();
                FileObject fo = NbEditorUtilities.getFileObject(document);
                int line = NbDocument.findLineNumber((StyledDocument) document, component.getCaretPosition())+1;
                ClassPath cp = ClassPath.getClassPath(fo, ClassPath.SOURCE);
                String outText="";//NOI18N
                if (cp!=null) {
                    outText=cp.getResourceName(fo);
                } else {
                    Project p = FileOwnerQuery.getOwner(fo);
                    if (p!=null) {
                        outText = "{$" + ProjectUtils.getInformation(p).getName() +"}/"+ FileUtil.getRelativePath(p.getProjectDirectory(), fo);//NOI18N
                    } else {
                        outText = fo.getPath();
                    }
                }
                outText+= ":" + line;//NOI18N
                try {
                    outbox.getDocument().insertString(outbox.getCaretPosition(), outText, null);
                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }//GEN-LAST:event_outboxKeyPressed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextPane inbox;
    private javax.swing.JPanel inboxPanel;
    private javax.swing.JScrollPane inboxScrollPane;
    private javax.swing.JLabel online;
    private javax.swing.JTextPane outbox;
    private javax.swing.JScrollPane outboxScrollPane;
    private javax.swing.JSplitPane splitter;
    private javax.swing.JLabel statusLine;
    private javax.swing.JPanel topPanel;
    // End of variables declaration//GEN-END:variables


    private Date lastDatePrinted;
    private Date lastMessageDate;
    private String lastNickPrinted = null;
    private String rgb = null;

    protected void insertMessage(Message message) {
        try {
            HTMLDocument doc = (HTMLDocument) inbox.getStyledDocument();
            final Date timestamp = getTimestamp(message);
            String fromRes = suc==null?StringUtils.parseResource(message.getFrom()):StringUtils.parseName(message.getFrom());
            history.addMessage(message.getBody()); //Store the message to the history
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
                    text += "<div style=\"height: 3px; background-color: rgb(" + rgb + ")\"></div>"; // NOI18N
                }
                text += "<table border=\"0\" borderwith=\"0\" width=\"100%\"><tbody>" + //NOI18N
                        "<tr style=\"background-color: rgb(" + headerColor.getRed() + "," + headerColor.getGreen() + "," + headerColor.getBlue() + ")\">" + //NOI18N
                        "<td class=\"buddy\" align=\"left\">"+ fromRes + "</td><td class=\"time\" align=\"right\">" + // NOI18N
                        DateFormat.getTimeInstance(DateFormat.SHORT).format(getTimestamp(message)) + "</td></tr></tbody></table>"; // NOI18N
            }
            rgb = messageColor.getRed() + "," + messageColor.getGreen() + "," + messageColor.getBlue(); // NOI18N
            text += "<div class=\"message\" style=\"background-color: rgb(" + rgb + ")\">" + replaceSmileys(replaceLinks(removeTags(message.getBody()))) + "</div>"; // NOI18N

            editorKit.insertHTML(doc, doc.getLength(), text, 0, 0, null);
            inbox.revalidate();
            inbox.repaint();
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


    private class Fader {

        private JComponent target;
        private Timer timer = new Timer(50, new ActionListener() {

            int c = 0;

            public void actionPerformed(ActionEvent evt) {
                c += 10;
                if (c > 255) {
                    c = 0;
                    timer.stop();
                    return;
                }
                Color newc = new Color(c, c, c);
                target.setForeground(newc);
            }
        });

        public Fader(JComponent target) {
            this.target = target;
            timer.setInitialDelay(2000);
        }

        public void start() {
            timer.start();
        }
    }


        protected void insertPresence(final Presence presence) {
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    statusLine.setForeground(Color.black);
                    statusLine.setText("<html><b>" + StringUtils.parseResource(presence.getFrom()) + "</b> is now " + presence.getType() + "</html>");  //NOI18N
                    //fadein.start();
                    new Fader(statusLine).start();
                }
            });
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
