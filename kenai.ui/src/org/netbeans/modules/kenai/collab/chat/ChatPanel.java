/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
import java.net.URL;
import java.text.DateFormat;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.packet.DelayInformation;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.kenai.api.Kenai;
import org.netbeans.modules.kenai.api.KenaiException;
import org.netbeans.modules.kenai.api.KenaiFeature;
import org.netbeans.modules.kenai.api.KenaiNotification;
import org.netbeans.modules.kenai.api.KenaiProject;
import org.netbeans.modules.kenai.api.KenaiService;
import org.netbeans.modules.kenai.api.KenaiService.Type;
import org.netbeans.modules.kenai.ui.spi.KenaiIssueAccessor;
import org.netbeans.modules.kenai.ui.spi.KenaiIssueAccessor.IssueHandle;
import org.netbeans.modules.kenai.ui.api.KenaiUserUI;
import org.netbeans.modules.kenai.ui.api.KenaiUIUtils;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.awt.DropDownButtonFactory;
import org.openide.awt.HtmlBrowser.URLDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.text.Line;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.openide.text.NbDocument;

/**
 * Panel representing single ChatRoom
 * 
 */
public class ChatPanel extends javax.swing.JPanel {

    private MultiUserChat muc;
    private Chat suc;
    private Kenai kenai;
    private KenaiConnection kc;
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

    private static final String ISSUE_REPORT_STRING = "(issue |bug |ISSUE:|ISSUE: )#?([A-Z_]+-)*([0-9]+)"; //NOI18N
    /**
     * Pattern for matching issue references in the form "issue 123", "issue #123", "bug 123" and "bug #123"
     */
    private static final Pattern ISSUE_REPORT = Pattern.compile(ISSUE_REPORT_STRING, Pattern.CASE_INSENSITIVE);

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
            "FILE:(([a-zA-Z_$][a-zA-Z0-9_$]*/)+)(([a-zA-Z_$][a-zA-Z0-9_$]*\\.[a-zA-Z0-9_$]*):([0-9]+))";//NOI18N
    /**
     * group(5) is line
     * group(1) is folder
     * group(4) is file
     */
    private static final Pattern CLASSPATH_RESOURCE = Pattern.compile(CLASSPATH_RESOURCE_STRING);

    private static final String ABSOLUTE_RESOURCE_STRING = 
            "FILE:/(([a-zA-Z_$][a-zA-Z0-9_$]*/)+)(([a-zA-Z_$][a-zA-Z0-9_$]*\\.[a-zA-Z0-9_$]*):([0-9]+))";//NOI18N
    /**
     * group(5) is line
     * group(1) is folder
     * group(4) is file
     */
    private static final Pattern ABSOLUTE_RESOURCE = Pattern.compile(ABSOLUTE_RESOURCE_STRING);

    private static final String PROJECT_RESOURCE_STRING = 
            "FILE:\\{\\$([a-zA-Z_$][\\.a-zA-Z0-9_$]*)\\}/(([a-zA-Z_$][a-zA-Z0-9_$]*/)*)(([a-zA-Z_$][a-zA-Z0-9_$]*\\.[a-zA-Z0-9_$]*):([0-9]+))";//NOI18N

    /**
     * group(6) is line
     * group(2) is folder
     * group(5) is file
     * group(1) is project name
     */
    private static final Pattern PROJECT_RESOURCE = Pattern.compile(PROJECT_RESOURCE_STRING);

    private static final Pattern RESOURCES =
            Pattern.compile("("+STACK_TRACE_STRING+")|("+CLASSPATH_RESOURCE_STRING +")|("+PROJECT_RESOURCE_STRING +")|("+ABSOLUTE_RESOURCE_STRING + ")");//NOI18N

    private void addNotificationsMenuItem(JPopupMenu menu) throws MissingResourceException {
        NotificationsEnabledAction bubbleEnabled = new NotificationsEnabledAction();
        String name = isPrivate() ? getShortName() : getKenaiProject().getDisplayName();
        JCheckBoxMenuItem jCheckBoxMenuItem = new JCheckBoxMenuItem(NbBundle.getMessage(ChatPanel.class, "CTL_NotificationsFor", new Object[]{name}), ChatNotifications.getDefault().isEnabled(getName()));
        jCheckBoxMenuItem.addActionListener(bubbleEnabled);
        menu.add(jCheckBoxMenuItem);
    }

    private void insertLink() {
        Mode editor = WindowManager.getDefault().findMode("editor"); //NOI18N
        TopComponent tc = editor.getSelectedTopComponent();
        if (getTopComponent(EditorRegistry.lastFocusedComponent()) == tc) {
            insertLinkToEditor();
        } else if (tc != null && isIssueRelated(tc)) {
            insertLinkToIssue();
        } else {
            insertLinkToEditor();
        }
    }
    private void insertLinkToEditor() {
        if (EditorRegistry.lastFocusedComponent() != null) {
            InsertLinkAction a = InsertLinkAction.create(EditorRegistry.lastFocusedComponent(), outbox, true, false);
            if(a != null) {
                a.actionPerformed(null);
            }
        }
    }

    private KenaiProject getKenaiProject() {
        assert !isPrivate();
        return KenaiConnection.getKenaiProject(muc);
    }

    private TopComponent selectedEditorComponent() {
        Mode editor = WindowManager.getDefault().findMode("editor");//NOI18N
        TopComponent tc = editor.getSelectedTopComponent();
        TopComponent topComponent = getTopComponent(EditorRegistry.lastFocusedComponent());
        if (topComponent == tc) {
            return tc;
        }
        return null;
    }

    private void insertLinkToIssue() {
        IssueHandle[] issues = null;
        KenaiIssueAccessor issueAccessor = KenaiIssueAccessor.getDefault();
        if (issueAccessor != null) {
            if (muc==null) {
                issues = issueAccessor.getRecentIssues();
            } else {
                issues = issueAccessor.getRecentIssues(getKenaiProject());
            }
        }
        if (issues != null && issues.length >0) {
            InsertLinkAction a = InsertLinkAction.create(issues[0], outbox, false);
            if(a != null) {
                a.actionPerformed(null);
            }
        }
    }
    private void selectIssueReport(Matcher m) {
        final String issueNumber = m.group(3);
        KenaiProject _proj = null;
        if (isPrivate()) {
            try { // in case of private chats, use the first project's repo with the correct type of issuetracker
                Iterator<KenaiProject> it = kenai.getMyProjects().iterator();
                _proj = it.next();
                if (m.group(2) == null || m.group(2).equals("")) { //Bugzilla issue?
                    while (it.hasNext()) { // iterate over all your projects and find any project with BZ issuetracker
                        KenaiFeature[] bt = _proj.getFeatures(Type.ISSUES);
                        if (bt.length == 0) {
                            continue;
                        }
                        KenaiFeature btinst = bt[0];
                        if (btinst.getService().equals(KenaiService.Names.BUGZILLA)) {
                            break;
                        }
                        _proj = it.next();
                    }
                } else { //Jira issue?
                    while (it.hasNext()) { // find the project according to the part of the issue, for example "bug SAMPLE_PROJECT-1"=>sample-project
                        System.out.println(_proj.getName());
                        String s = m.group(2).replaceAll("-", "_").toLowerCase(); // NOI18N
                        s = s.substring(0, s.length() - 1);
                        if (_proj.getName().replaceAll("-", "_").toLowerCase().equals(s)) { // NOI18N
                            break;
                        }
                        _proj = it.next();
                    }
                }
            } catch (KenaiException ex1) {
                Exceptions.printStackTrace(ex1);
            }
        } else {
            _proj = getKenaiProject(); // project name ~ chatroom name
        }
        final KenaiProject proj = _proj;
        if (proj == null) return;
        KenaiFeature[] trackers = null;
        try {
            trackers = proj.getFeatures(Type.ISSUES);
        } catch (KenaiException ex) {
            Exceptions.printStackTrace(ex);
        }
        final KenaiIssueAccessor acc = KenaiIssueAccessor.getDefault();
        if (acc == null || trackers.length == 0) { // No issue trackers found for the project
            return;
        }
        if (trackers[0].getService().equals(KenaiService.Names.JIRA)) {
            SwingUtilities.invokeLater(new Runnable() {

                public void run() { // issue ID format: PROJECT_NAME-123
                    acc.open(proj, proj.getName().toUpperCase().replaceAll("-", "_") + "-" + issueNumber); // NOI18N
                }
            });
        } else if (trackers[0].getService().equals(KenaiService.Names.BUGZILLA)) {
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    acc.open(proj, issueNumber);
                }
            });
        }
    }


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
            return NbDocument.openDocument(od, line, -1, Line.ShowOpenType.OPEN, Line.ShowVisibilityType.FOCUS);
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
        }
        return false;
    }

    public ChatPanel(MultiUserChat muc) {
        super();
        this.muc=muc;
        KenaiProject kenaiProject = getKenaiProject();
        kenai = kenaiProject.getKenai();
        setName(muc.getRoom()); //NOI18N
        kc = KenaiConnection.getDefault(kenai);
        init();
        XMPPConnection xmppConnection = kenai.getXMPPConnection();
        if (xmppConnection != null && !xmppConnection.isConnected()) {
            try {
                kc.reconnect(muc);
            } catch (XMPPException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        this.muc.addParticipantListener(new PacketListener() {
            public void processPacket(Packet presence) {
                insertPresence((Presence) presence);
            }
        });
        kc.join(muc,new ChatListener());

    }

    public ChatPanel(String jid) {
        super();
        setName(jid);
        init();
        kenai = KenaiConnection.getKenai(jid);
        kc = KenaiConnection.getDefault(kenai);
        this.suc = kc.joinPrivate(jid, new ChatListener());
    }

    public boolean isPrivate() {
        return muc == null;  //NOI18N
    }

    public String getShortName() {
        return StringUtils.parseName(getName());
    }

    
    private void init() {
        initComponents();
        online.setHorizontalTextPosition(JLabel.LEFT);
        if (isPrivate()) {
            topPanel.remove(online);
            online = new KenaiUserUI(getName()).createUserWidget();
            online.setText(null);
            online.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 1, 3, 1));
            topPanel.add(online, java.awt.BorderLayout.EAST);
            topPanel.validate();
        }
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
            MessagingHandleImpl handle = ChatNotifications.getDefault().getMessagingHandle(getKenaiProject());
            handle.addPropertyChangeListener(new PresenceListener());
        }
        //KenaiConnection.getDefault().join(chat);
        inbox.setBackground(Color.WHITE);
        inbox.addHyperlinkListener(new HyperlinkListener() {

            public void hyperlinkUpdate(HyperlinkEvent e) {
                if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    URL url = e.getURL();
                    if (url!=null && !(url.getProtocol().equals("file") || url.getProtocol().equals("issue")) ) { // NOI18N
                        URLDisplayer.getDefault().showURL(url);
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
                                    } else {
                                        m = ISSUE_REPORT.matcher(link);
                                        if (m.matches()) {
                                            selectIssueReport(m);
                                        }
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

        inbox.setComponentPopupMenu(dropDownMenu);
        outbox.setComponentPopupMenu(dropDownMenu);


        KenaiUIUtils.logKenaiUsage("CHAT", isPrivate() ? "PRIVATE_CHAT" : "CHATROOM"); // NOI18N
    }

    void insertToInputArea(String message) {
        if (message==null)
            return;
        try {
            outbox.getDocument().insertString(outbox.getCaretPosition(), message, null);
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    void leave() {
        if (isPrivate()) {
            kc.leavePrivate(getName());
        } else {
            kc.leaveGroup(getShortName());
        }
    }

    private class NotificationsEnabledAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            JCheckBoxMenuItem m = (JCheckBoxMenuItem) e.getSource();
            ChatNotifications.getDefault().setEnabled(getName(),m.getState());
        }
    }

    @Override
    public void requestFocus() {
        super.requestFocus();
        outbox.requestFocus();
    }

    private String removeTags(String body) {
        String tmp = body;
        tmp = tmp.replaceAll("\r\n", "\n"); // NOI18N
        tmp = tmp.replaceAll("\r", "\n"); // NOI18N
        return tmp.replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("\n", "<br>"); // NOI18N
    }

    private String replaceLinks(String body) {
        // This regexp works quite nice, should be OK in most cases (does not handle [.,?!] in the end of the URL)
        String result = body.replaceAll("(http|https|ftp)://([a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,4}(/[^ ]*)*", "<a href=\"$0\">$0</a>"); //NOI18N

        result = RESOURCES.matcher(result).replaceAll("<a href=\"$0\">$0</a>");  //NOI18N
        
        result = ISSUE_REPORT.matcher(result).replaceAll("<a href=\"$0\">$0</a>"); //NOI18N

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
        online.setText(muc.getOccupantsCount()+"");  //NOI18N
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

        dropDownMenu = new javax.swing.JPopupMenu();
        temp = new javax.swing.JMenuItem();
        splitter = new javax.swing.JSplitPane();
        outboxPanel = new javax.swing.JPanel();
        buttons = new javax.swing.JPanel();
        sendLinkButton = DropDownButtonFactory.createDropDownButton(ImageUtilities.loadImageIcon("/org/netbeans/modules/kenai/ui/resources/insertlink.png", true), dropDownMenu);
        sendButton = new javax.swing.JButton();
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

        dropDownMenu.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {
                dropDownMenuPopupMenuWillBecomeVisible(evt);
            }
            public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent evt) {
                dropDownMenuPopupMenuWillBecomeInvisible(evt);
            }
            public void popupMenuCanceled(javax.swing.event.PopupMenuEvent evt) {
            }
        });
        dropDownMenu.add(temp);

        splitter.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        outboxPanel.setLayout(new java.awt.BorderLayout());

        buttons.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 3, 3, 3));

        sendLinkButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/kenai/ui/resources/insertlink.png"))); // NOI18N
        sendLinkButton.setToolTipText(org.openide.util.NbBundle.getBundle(ChatPanel.class).getString("ChatPanel.sendLinkButton.toolTipText")); // NOI18N
        sendLinkButton.setFocusable(false);
        sendLinkButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        sendLinkButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        sendLinkButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sendLinkButtonActionPerformed(evt);
            }
        });

        sendButton.setText(org.openide.util.NbBundle.getMessage(ChatPanel.class, "ChatPanel.sendButton.text", new Object[] {})); // NOI18N
        sendButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sendButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout buttonsLayout = new javax.swing.GroupLayout(buttons);
        buttons.setLayout(buttonsLayout);
        buttonsLayout.setHorizontalGroup(
            buttonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, buttonsLayout.createSequentialGroup()
                .addComponent(sendLinkButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 140, Short.MAX_VALUE)
                .addComponent(sendButton))
        );
        buttonsLayout.setVerticalGroup(
            buttonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(sendButton)
            .addComponent(sendLinkButton)
        );

        outboxPanel.add(buttons, java.awt.BorderLayout.SOUTH);

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

        outboxPanel.add(outboxScrollPane, java.awt.BorderLayout.CENTER);

        splitter.setRightComponent(outboxPanel);

        inboxPanel.setBackground(java.awt.Color.white);
        inboxPanel.setLayout(new java.awt.BorderLayout());

        inboxScrollPane.setBorder(null);

        inbox.setBorder(null);
        inbox.setContentType("text/html"); // NOI18N
        inbox.setEditable(false);
        inbox.setText(org.openide.util.NbBundle.getMessage(ChatPanel.class, "ChatPanel.inbox.text", new Object[] {})); // NOI18N
        inbox.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                inboxFocusGained(evt);
            }
        });
        inboxScrollPane.setViewportView(inbox);

        inboxPanel.add(inboxScrollPane, java.awt.BorderLayout.CENTER);

        topPanel.setBackground(java.awt.Color.white);
        topPanel.setLayout(new java.awt.BorderLayout());

        online.setBackground(java.awt.Color.white);
        online.setForeground(java.awt.Color.blue);
        online.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        online.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/kenai/collab/resources/user_online.png"))); // NOI18N
        online.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 1, 3, 1));
        topPanel.add(online, java.awt.BorderLayout.EAST);
        topPanel.add(statusLine, java.awt.BorderLayout.CENTER);

        inboxPanel.add(topPanel, java.awt.BorderLayout.NORTH);

        splitter.setTopComponent(inboxPanel);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(splitter, javax.swing.GroupLayout.DEFAULT_SIZE, 268, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(splitter, javax.swing.GroupLayout.DEFAULT_SIZE, 403, Short.MAX_VALUE)
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
                if (muc!=null&& (!kc.isConnected() || !muc.isJoined())) {
                    try {
                        kc.reconnect(muc);
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
                    KenaiUIUtils.logKenaiUsage("CHAT", "MESSAGE_SENT"); // NOI18N
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
        if (evt.isControlDown() || evt.isAltDown()) {
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
        if (evt.isControlDown() && evt.getKeyCode() == KeyEvent.VK_L) {
            Mode editor = WindowManager.getDefault().findMode("editor");//NOI18N
            TopComponent tc = editor.getSelectedTopComponent();
            if (getTopComponent(EditorRegistry.lastFocusedComponent()) == tc) {
                insertLinkToEditor();
            } else if (tc != null && isIssueRelated(tc)) {
                insertLinkToIssue();
            } else {
                Point magicCaretPosition = outbox.getCaret().getMagicCaretPosition();
                if (magicCaretPosition == null) {
                    magicCaretPosition = new Point(0, 0);
                }
                outbox.getComponentPopupMenu().show(outbox, magicCaretPosition.x, magicCaretPosition.y);
            }
        }
    }//GEN-LAST:event_outboxKeyPressed

    private boolean isIssueRelated(TopComponent tc) {
        IssueHandle[] issues;
        KenaiIssueAccessor accessor = KenaiIssueAccessor.getDefault();
        if (accessor == null) {
            return false;
        } else if (muc==null) {
            issues = accessor.getRecentIssues();
            if (issues.length<1) {
                return false;
            }
            return issues[0].isShowing();
        } else {
            issues = accessor.getRecentIssues(getKenaiProject());
            IssueHandle[] allIssues = accessor.getRecentIssues();
            if (issues.length < 1) {
                return false;
            }
            if (issues[0].getID().equals(allIssues[0].getID())) {
                return issues[0].isShowing();
            }
        }
        return false;
    }

    private TopComponent getTopComponent(Component comp) {
        while (comp!=null && ! (comp instanceof TopComponent)) {
            comp = comp.getParent();
        }
        return (TopComponent) comp;
    }

    private void sendButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sendButtonActionPerformed
        KeyEvent e = new KeyEvent((Component) evt.getSource(),evt.getID(), evt.getWhen(), evt.getModifiers(), KeyEvent.VK_ENTER, '\n');
        keyTyped(e);
        outbox.requestFocus();
    }//GEN-LAST:event_sendButtonActionPerformed

    private void inboxFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_inboxFocusGained
        // Workaround for MacOS - even non-editable components show caret whenever
        // the component is focused
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                inbox.getCaret().setVisible(false);
            }
        });
    }//GEN-LAST:event_inboxFocusGained

    private void dropDownMenuPopupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {//GEN-FIRST:event_dropDownMenuPopupMenuWillBecomeVisible
        dropDownMenu.removeAll();
        JPopupMenu menu = (JPopupMenu) evt.getSource();
        if (menu.getInvoker() == inbox) {
            addNotificationsMenuItem(dropDownMenu);
            return;
        }
        
        JTextComponent lastFocused = EditorRegistry.lastFocusedComponent();
        if (lastFocused!=null) {
            InsertLinkAction a = InsertLinkAction.create(lastFocused, outbox, true, selectedEditorComponent()!=null);
            if(a != null) {
                dropDownMenu.add(a);
                dropDownMenu.addSeparator();
            }
            
        }
        
        HashSet<FileObject> fos = new HashSet();
        for (JTextComponent comp:EditorRegistry.componentList()) {
            //ugly fix of #181134
            TopComponent tc = getTopComponent(comp);
            if (tc != null && tc.getClass().getName().endsWith("DiffTopComponent")) //NOI18N
                continue;
            InsertLinkAction a = InsertLinkAction.create(comp, outbox, false, false);
            if(a != null) {
                dropDownMenu.add(a);
            }
        }
        if (lastFocused!=null) {
            dropDownMenu.addSeparator();
        }

        IssueHandle[] issues;
        KenaiIssueAccessor issueAccessor = KenaiIssueAccessor.getDefault();
        if (issueAccessor == null) {
            issues = null;
        } else if (muc==null) {
            issues = issueAccessor.getRecentIssues();
        } else {
            issues = issueAccessor.getRecentIssues(getKenaiProject());
        }
        if (issues == null) {
            issues = new IssueHandle[0];
        }

        for (int i=0;i<3 && i< issues.length;i++) {
            Mode editor = WindowManager.getDefault().findMode("editor");//NOI18N
            TopComponent tc = editor.getSelectedTopComponent();
            InsertLinkAction a = InsertLinkAction.create(issues[i], outbox, isIssueRelated(tc));
            if(a != null) {
                dropDownMenu.add(a);
            }
        }
        if (issues.length>0) {
            dropDownMenu.addSeparator();
        }

        dropDownMenu.add(new LinkOtherFileAction(outbox));
        dropDownMenu.add(new LinkOtherIssue(outbox));
        
        if (menu.getInvoker()==outbox) {
            dropDownMenu.addSeparator();
            addNotificationsMenuItem(dropDownMenu);
        }
    }//GEN-LAST:event_dropDownMenuPopupMenuWillBecomeVisible

    private void dropDownMenuPopupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent evt) {//GEN-FIRST:event_dropDownMenuPopupMenuWillBecomeInvisible
        dropDownMenu.removeAll();
        dropDownMenu.add(temp);
    }//GEN-LAST:event_dropDownMenuPopupMenuWillBecomeInvisible

    private void sendLinkButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sendLinkButtonActionPerformed
        insertLink();
        outbox.requestFocus();
}//GEN-LAST:event_sendLinkButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel buttons;
    private javax.swing.JPopupMenu dropDownMenu;
    private javax.swing.JTextPane inbox;
    private javax.swing.JPanel inboxPanel;
    private javax.swing.JScrollPane inboxScrollPane;
    private javax.swing.JLabel online;
    private javax.swing.JTextPane outbox;
    private javax.swing.JPanel outboxPanel;
    private javax.swing.JScrollPane outboxScrollPane;
    private javax.swing.JButton sendButton;
    private javax.swing.JButton sendLinkButton;
    private javax.swing.JSplitPane splitter;
    private javax.swing.JLabel statusLine;
    private javax.swing.JMenuItem temp;
    private javax.swing.JPanel topPanel;
    // End of variables declaration//GEN-END:variables


    private Date lastDatePrinted;
    private Date lastMessageDate;
    private String lastNickPrinted = null;
    private String rgb = null;

    private String getMessageBody(Message m) {
        final NotificationExtension ne = (NotificationExtension) m.getExtension("notification", NotificationExtensionProvider.NAMESPACE); // NOI18N
        String b = m.getBody();
        if (b==null) {
            b="";
        }
        if (ne==null) {
            return b;
        }
        KenaiNotification n = ne.getNotification();
        if (!((n.getType() == KenaiService.Type.SOURCE) || (n.getType() == KenaiService.Type.ISSUES))) {
            return b;
        }
        int i = b.indexOf(']');
        String body;
        if (i>=0) {
            body = b.substring(i+2);
        } else {
            body = b;
        }
        if (n.getType() == KenaiService.Type.ISSUES) {
            return body;
        }

        String id;
        if (n.getModifications().isEmpty()) {
            Logger.getLogger(ChatPanel.class.getName()).log(Level.INFO, "empty modifications: {0}", n.getUri());
            id = ""; //NOI18N
        } else {
            id = n.getModifications().get(0).getId();
        }
        String projectName = StringUtils.parseName(m.getFrom());
        if (projectName.contains("@")) { // NOI18N
            projectName = StringUtils.parseName(projectName);
        }
        String url = kenai.getUrl().toString()+ "/projects/" + projectName + "/sources/" + (n.getFeatureName()!=null?n.getFeatureName():n.getServiceName()) + (id.isEmpty() ? "/show" : "/revision/" + id); //NOI18N
        return body + "\n" + url; // NOI18N
    }

    protected void insertMessage(Message message) {
        try {
            HTMLDocument doc = (HTMLDocument) inbox.getStyledDocument();
            final Date timestamp = getTimestamp(message);
            String fromRes = suc==null?StringUtils.parseResource(message.getFrom()):StringUtils.parseName(message.getFrom());
            String messageBody = getMessageBody(message);
            history.addMessage(messageBody); //Store the message to the history
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
            text += "<div class=\"message\" style=\"background-color: rgb(" + rgb + ")\">" + replaceSmileys(replaceLinks(removeTags(messageBody))) + "</div>"; // NOI18N

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


    private static class Fader {

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
