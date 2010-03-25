/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.bugzilla.issue;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.net.URI;
import java.net.URL;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.logging.Level;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.DefaultCaret;
import javax.swing.text.Element;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import org.jdesktop.layout.GroupLayout;
import org.jdesktop.layout.LayoutStyle;
import org.netbeans.modules.bugtracking.spi.Issue;
import org.netbeans.modules.bugtracking.kenai.spi.KenaiUtil;
import org.netbeans.modules.bugtracking.util.LinkButton;
import org.netbeans.modules.bugtracking.util.StackTraceSupport;
import org.netbeans.modules.bugtracking.util.TextUtils;
import org.netbeans.modules.bugtracking.util.WebUrlHyperlinkSupport;
import org.netbeans.modules.bugzilla.Bugzilla;
import org.netbeans.modules.bugzilla.kenai.KenaiRepository;
import org.netbeans.modules.bugzilla.repository.IssueField;
import org.openide.ErrorManager;
import org.openide.awt.HtmlBrowser;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Jan Stola
 */
public class CommentsPanel extends JPanel {
    private static final DateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm"); // NOI18N
    private final static String ISSUE_ATTRIBUTE = "issue"; // NOI18N
    private final static String URL_ATTRIBUTE = "url hyperlink";        //NOI18N
    private final static String ATTACHMENT_ATTRIBUTE = "attachment hyperlink"; //NOI18N
    private final static String REPLY_TO_PROPERTY = "replyTo"; // NOI18N
    private final static String QUOTE_PREFIX = "> "; // NOI18N
    private final static int MAX_COMMENT_HEIGHT = 10000;
    private final JPopupMenu commentsPopup = new PopupMenu();
    private final BugzillaIssueFinder issueFinder;
    private BugzillaIssue issue;
    private List<BugzillaIssue.Attachment> attachments;
    private List<String> attachmentIds;
    private MouseAdapter listener;
    private MouseMotionListener motionListener;
    private NewCommentHandler newCommentHandler;

    public CommentsPanel() {
        setBackground(UIManager.getColor("EditorPane.background")); // NOI18N
        motionListener = new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                JTextPane pane = (JTextPane)e.getSource();
                StyledDocument doc = pane.getStyledDocument();
                Element elem = doc.getCharacterElement(pane.viewToModel(e.getPoint()));
                AttributeSet as = elem.getAttributes();
                if (StyleConstants.isUnderline(as)) {
                    pane.setCursor(new Cursor(Cursor.HAND_CURSOR));
                } else {
                    pane.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                }
            }
        };
        listener = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    if (SwingUtilities.isLeftMouseButton(e)) {
                        JTextPane pane = (JTextPane)e.getSource();
                        StyledDocument doc = pane.getStyledDocument();
                        Element elem = doc.getCharacterElement(pane.viewToModel(e.getPoint()));
                        AttributeSet as = elem.getAttributes();

                        IssueAction issueAction = (IssueAction)as.getAttribute(ISSUE_ATTRIBUTE);
                        if (issueAction != null) {
                            int startOffset = elem.getStartOffset();
                            int endOffset = elem.getEndOffset();
                            int length = endOffset - startOffset;
                            String hyperlinkText = doc.getText(startOffset, length);
                            issueAction.openIssue(hyperlinkText);
                            return;
                        }

                        UrlAction urlAction = (UrlAction) as.getAttribute(URL_ATTRIBUTE);
                        if (urlAction != null) {
                            int startOffset = elem.getStartOffset();
                            int endOffset = elem.getEndOffset();
                            int length = endOffset - startOffset;
                            String hyperlinkText = doc.getText(startOffset, length);
                            urlAction.openUrlHyperlink(hyperlinkText);
                            return;
                        }

                        if (as.getAttribute(ATTACHMENT_ATTRIBUTE) != null) {
                            CommentsPanel.this.openAttachmentHyperlink(pane);
                            return;
                        }
                    }
                } catch(Exception ex) {
                    Bugzilla.LOG.log(Level.SEVERE, null, ex);
                }
            }
        };

        issueFinder = Lookup.getDefault().lookup(BugzillaIssueFinder.class);
        assert issueFinder != null;
    }

    void setIssue(BugzillaIssue issue,
                  List<BugzillaIssue.Attachment> attachments) {
        removeAll();
        this.issue = issue;
        this.attachments = attachments;
        this.attachmentIds = getAttachmentIds(attachments);
        GroupLayout layout = new GroupLayout(this);
        GroupLayout.ParallelGroup horizontalGroup = layout.createParallelGroup(GroupLayout.LEADING);
        layout.setHorizontalGroup(layout.createSequentialGroup()
            .addContainerGap()
            .add(horizontalGroup)
            .addContainerGap());
        GroupLayout.SequentialGroup verticalGroup = layout.createSequentialGroup();
        verticalGroup.addContainerGap();
        layout.setVerticalGroup(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(verticalGroup));
        DateFormat format = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.SHORT);
        String creationTxt = issue.getFieldValue(IssueField.CREATION);
        try {
            Date creation = dateTimeFormat.parse(creationTxt);
            creationTxt = format.format(creation);
        } catch (ParseException pex) {
            Bugzilla.LOG.log(Level.INFO, null, pex);
        }
        addSection(layout,
            issue.getFieldValue(IssueField.DESCRIPTION),
            issue.getFieldValue(IssueField.REPORTER),
            issue.getFieldValue(IssueField.REPORTER_NAME),
            creationTxt, horizontalGroup, verticalGroup, true);
        for (BugzillaIssue.Comment comment : issue.getComments()) {
            String when = format.format(comment.getWhen());
            addSection(layout, comment.getText(), comment.getAuthor(), comment.getAuthorName(), when, horizontalGroup, verticalGroup, false);
        }
        verticalGroup.addContainerGap();
        setLayout(layout);
    }

    private static List<String> getAttachmentIds(
                                   List<BugzillaIssue.Attachment> attachments) {
        if (attachments.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> result = new ArrayList<String>(attachments.size());
        for (BugzillaIssue.Attachment attachment : attachments) {
            result.add(attachment.getId());
        }
        return result;
    }

    public void setNewCommentHandler(NewCommentHandler handler) {
        newCommentHandler = handler;
    }

    private void addSection(GroupLayout layout, String text, String author, String authorName, String dateTimeString,
            GroupLayout.ParallelGroup horizontalGroup, GroupLayout.SequentialGroup verticalGroup, boolean description) {
        JTextPane textPane = new JTextPane();
        JLabel leftLabel = new JLabel();
        ResourceBundle bundle = NbBundle.getBundle(CommentsPanel.class);
        String leftTxt;
        if (description) {
            String leftFormat = bundle.getString("CommentsPanel.leftLabel.format"); // NOI18N
            String summary = TextUtils.escapeForHTMLLabel(issue.getSummary());
            leftTxt = MessageFormat.format(leftFormat, summary);
        } else {
            leftTxt = bundle.getString("CommentsPanel.leftLabel.text"); // NOI18N
        }
        leftLabel.setText(leftTxt);
        JLabel rightLabel = new JLabel();
        String rightFormat = bundle.getString("CommentsPanel.rightLabel.format"); // NOI18N
        String authorTxt = ((authorName != null) && (authorName.trim().length() > 0)) ? authorName : author;
        String rightTxt = MessageFormat.format(rightFormat, dateTimeString, authorTxt);
        rightLabel.setText(rightTxt);
        rightLabel.setLabelFor(textPane);
        JLabel stateLabel = null;
        if (issue.getRepository() instanceof KenaiRepository) {
            int index = author.indexOf('@');
            String userName = (index == -1) ? author : author.substring(0,index);
            String host = ((KenaiRepository) issue.getRepository()).getHost();
            stateLabel = KenaiUtil.createUserWidget(userName, host, KenaiUtil.getChatLink(issue));
            stateLabel.setText(null);
        }
        LinkButton replyButton = new LinkButton(bundle.getString("Comments.replyButton.text")); // NOI18N
        replyButton.addActionListener(getReplyListener());
        replyButton.putClientProperty(REPLY_TO_PROPERTY, textPane);
        replyButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CommentsPanel.class, "CommentsPanel.replyButton.AccessibleContext.accessibleDescription")); // NOI18N
        setupTextPane(textPane, text);

        // Issue 172653 - JTextPane too big
        JComponent pane = textPane;
        if (textPane.getPreferredSize().height>Short.MAX_VALUE) {
            pane = new JScrollPane(textPane);
            pane.setBorder(BorderFactory.createLineBorder(UIManager.getColor("Label.foreground"))); // NOI18N
            textPane.setBorder(null);
            Dimension dim = new Dimension(textPane.getPreferredSize());
            dim.height = MAX_COMMENT_HEIGHT;
            pane.setPreferredSize(dim);
        }

        // Layout
        GroupLayout.SequentialGroup hGroup = layout.createSequentialGroup()
            .add(leftLabel, 0, 0, Short.MAX_VALUE)
            .addPreferredGap(LayoutStyle.RELATED)
            .add(replyButton)
            .addPreferredGap(LayoutStyle.RELATED)
            .add(rightLabel);
        if (stateLabel != null) {
            hGroup.addPreferredGap(LayoutStyle.RELATED);
            hGroup.add(stateLabel);
        }
        horizontalGroup.add(hGroup)
            .add(pane, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
        if (!description) {
            verticalGroup.addPreferredGap(LayoutStyle.UNRELATED);
        }
        GroupLayout.ParallelGroup vGroup = layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
            .add(leftLabel)
            .add(replyButton)
            .add(rightLabel);
        if (stateLabel != null) {
            vGroup.add(stateLabel);
        }
        verticalGroup.add(vGroup)
            .add(pane, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE);
    }

    private void setupTextPane(JTextPane textPane, String comment) {
        StyledDocument doc = textPane.getStyledDocument();
        Caret caret = textPane.getCaret();
        if (caret instanceof DefaultCaret) {
            ((DefaultCaret)caret).setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
        }

        // Stack-traces
        textPane.setText(comment);
        StackTraceSupport.addHyperlinks(textPane);

        // Issues/bugs
        int[] pos = issueFinder.getIssueSpans(comment);
        if (pos.length > 0) {
            Style defStyle = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
            Style hlStyle = doc.addStyle("bugBlue", defStyle); // NOI18N
            hlStyle.addAttribute(ISSUE_ATTRIBUTE, new IssueAction());
            StyleConstants.setForeground(hlStyle, Color.BLUE);
            StyleConstants.setUnderline(hlStyle, true);

            for (int i=0; i<pos.length; i+=2) {
                int off = pos[i];
                int length = pos[i+1]-pos[i];
                try {
                    doc.remove(off, length);
                    doc.insertString(off, comment.substring(pos[i], pos[i+1]), hlStyle);
                } catch (BadLocationException blex) {
                        blex.printStackTrace();
                }
            }
        }

        // URL hyperlinks
        {
            final int[] boundaries = WebUrlHyperlinkSupport.findBoundaries(comment);
            if ((boundaries != null) && (boundaries.length != 0)) {
                Style defStyle = StyleContext.getDefaultStyleContext()
                                 .getStyle(StyleContext.DEFAULT_STYLE);
                Style hlStyle = doc.addStyle("regularBlue", defStyle);      //NOI18N
                hlStyle.addAttribute(URL_ATTRIBUTE, new UrlAction());
                StyleConstants.setForeground(hlStyle, Color.BLUE);
                StyleConstants.setUnderline(hlStyle, true);

                for (int i = 0; i < boundaries.length; i+=2) {
                    int off = boundaries[i];
                    int length = boundaries[i + 1] - boundaries[i];
                    try {
                        doc.remove(off, length);
                        doc.insertString(off, comment.substring(boundaries[i],
                                                                boundaries[i + 1]),
                                                                hlStyle);
                    } catch (BadLocationException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }

        // attachments
        if (!attachmentIds.isEmpty()) {
            final int[] boundaries = AttachmentHyperlinkSupport
                                     .findBoundaries(comment, attachmentIds);
            if ((boundaries != null) && (boundaries.length != 0)) {
                Style defStyle = StyleContext.getDefaultStyleContext()
                                 .getStyle(StyleContext.DEFAULT_STYLE);
                Style hlStyle = doc.addStyle("regularBlue", defStyle);      //NOI18N
                hlStyle.addAttribute(ATTACHMENT_ATTRIBUTE, new Object());
                StyleConstants.setForeground(hlStyle, Color.BLUE);
                StyleConstants.setUnderline(hlStyle, true);

                for (int i = 0; i < boundaries.length; i+=2) {
                    int off = boundaries[i];
                    int length = boundaries[i + 1] - boundaries[i];
                    try {
                        doc.remove(off, length);
                        doc.insertString(off, comment.substring(boundaries[i],
                                                                boundaries[i + 1]),
                                                                hlStyle);
                    } catch (BadLocationException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }

        // pop-ups
        textPane.setComponentPopupMenu(commentsPopup);

        textPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIManager.getColor("Label.foreground")), // NOI18N
                BorderFactory.createEmptyBorder(3,3,3,3)));
        textPane.setEditable(false);
        textPane.addMouseListener(listener);
        textPane.addMouseMotionListener(motionListener);
        textPane.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CommentsPanel.class, "CommentsPanel.textPane.AccessibleContext.accessibleName")); // NOI18N
        textPane.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CommentsPanel.class, "CommentsPanel.textPane.AccessibleContext.accessibleDescription")); // NOI18N
    }

    private ActionListener replyListener;
    private ActionListener getReplyListener() {
        if (replyListener == null) {
            replyListener = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Object source = e.getSource();
                    if (source instanceof JComponent) {
                        JComponent comp = (JComponent)source;
                        Object value = comp.getClientProperty(REPLY_TO_PROPERTY);
                        if (value instanceof JTextPane) {
                            JTextPane pane = (JTextPane)value;
                            String text = pane.getText();
                            StringBuilder sb = new StringBuilder();
                            StringTokenizer tokenizer = new StringTokenizer(text, "\n"); // NOI18N
                            while (tokenizer.hasMoreElements()) {
                                String line = tokenizer.nextToken();
                                sb.append(QUOTE_PREFIX).append(line).append('\n');
                            }
                            newCommentHandler.append(sb.toString());
                        }
                    }
                }
            };
        }
        return replyListener;
    }

    private class IssueAction {
        void openIssue(String hyperlinkText) {
            final String issueNo = issueFinder.getIssueId(hyperlinkText);
            RequestProcessor.getDefault().post(new Runnable() {
                @Override
                public void run() {
                    Issue is = issue.getRepository().getIssue(issueNo);
                    if (is != null) {
                        is.open();
                    }
                }
            });
        }
    }

    private class UrlAction {
        void openUrlHyperlink(String hyperlinkText) {
            try {
                URL url = new URI(hyperlinkText).toURL();
                HtmlBrowser.URLDisplayer.getDefault().showURL(url);
            } catch (Exception ex) {
                assert false;
                ErrorManager.getDefault().log(ErrorManager.WARNING,
                                              "Could not open URL: "    //NOI18N
                                                      + hyperlinkText);
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                                                 ex);
            }
        }
    }

    private void openAttachmentHyperlink(JTextPane textPane) {
        String attachmentId = null;
        try {
            BugzillaIssue.Attachment attachment = getAttachment(textPane);
            if (attachment != null) {
                attachment.open();
            }
        } catch (Exception ex) {
            assert false;
            String errMsg = "Could not open attachment";                //NOI18N
            if (attachmentId != null) {
                errMsg += " #" + attachmentId;                          //NOI18N
            }
            ErrorManager.getDefault().log(ErrorManager.WARNING, errMsg);
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
    }

    private BugzillaIssue.Attachment getAttachment(JTextPane textPane) {
        String commentText = textPane.getText();
        String attachmentId = AttachmentHyperlinkSupport
                              .getAttachmentId(commentText);
        if (attachmentId != null) {
            int index = attachmentIds.indexOf(attachmentId);
            if (index != -1) {
                return attachments.get(index);
            }
        }
        return null;
    }

    class PopupMenu extends JPopupMenu {

        /*
         * Holds the location of where the user invoked the pop-up menu.
         * It must be remembered before calling super.show(...) because
         * the method show() may change the location of the pop-up menu,
         * so the original location might not be available.
         */
        private final Point clickPoint = new Point();

        @Override
        public void show(Component invoker, int x, int y) {
            clickPoint.setLocation(x, y);
            super.show(invoker, x, y);
        }

        @Override
        public void setVisible(boolean b) {
            if (b) {
                JTextPane pane = (JTextPane) getInvoker();
                StyledDocument doc = pane.getStyledDocument();
                Element elem = doc.getCharacterElement(pane.viewToModel(clickPoint));
                if (elem.getAttributes().getAttribute(ATTACHMENT_ATTRIBUTE) != null) {
                    BugzillaIssue.Attachment attachment = getAttachment(pane);
                    if (attachment != null) {
                        add(new JMenuItem(attachment.new DefaultAttachmentAction()));
                        add(new JMenuItem(attachment.new SaveAttachmentAction()));
                        if ("1".equals(attachment.getIsPatch())) { // NOI18N
                            add(attachment.new ApplyPatchAction());
                        }
                        super.setVisible(true);
                    }
                }
            } else {
                super.setVisible(false);
                removeAll();
            }
        }

    }

    public interface NewCommentHandler {
        void append(String text);
    }

}
