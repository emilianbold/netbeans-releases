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
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.logging.Level;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.MouseInputAdapter;
import javax.swing.event.MouseInputListener;
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
import org.netbeans.modules.bugtracking.util.IssueFinder;
import org.netbeans.modules.bugtracking.util.LinkButton;
import org.netbeans.modules.bugtracking.util.StackTraceSupport;
import org.netbeans.modules.bugzilla.Bugzilla;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Jan Stola
 */
public class CommentsPanel extends JPanel {
    private static final DateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm"); // NOI18N
    private final static String STACKTRACE_ATTRIBUTE = "stacktrace"; // NOI18N
    private final static String ISSUE_ATTRIBUTE = "issue"; // NOI18N
    private final static String REPLY_TO_PROPERTY = "replyTo"; // NOI18N
    private final static String QUOTE_PREFIX = "> "; // NOI18N
    private BugzillaIssue issue;
    private MouseInputListener listener;
    private NewCommentHandler newCommentHandler;

    public CommentsPanel() {
        setBackground(UIManager.getColor("EditorPane.background")); // NOI18N
        listener = new MouseInputAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    if (SwingUtilities.isLeftMouseButton(e)) {
                        openStackTrace(e, false);
                        Element elem = element(e);
                        AttributeSet as = elem.getAttributes();
                        IssueAction issueAction = (IssueAction)as.getAttribute(ISSUE_ATTRIBUTE);
                        if (issueAction != null) {
                            issueAction.openIssue(elem.getDocument().getText(elem.getStartOffset(), elem.getEndOffset() - elem.getStartOffset()));
                        }
                    }
                } catch(Exception ex) {
                    Bugzilla.LOG.log(Level.SEVERE, null, ex);
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                showMenu(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                showMenu(e);
            }

            private void showMenu(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    try {
                        Element elem = element(e);
                        String stackFrame = elem.getDocument().getText(elem.getStartOffset(), elem.getEndOffset() - elem.getStartOffset());
                        JPopupMenu menu = new JPopupMenu();
                        menu.add(new StackTraceAction(stackFrame, false));
                        menu.add(new StackTraceAction(stackFrame, true));
                        menu.show((JTextPane)e.getSource(), e.getX(), e.getY());
                    } catch(Exception ex) {
                        Bugzilla.LOG.log(Level.SEVERE, null, ex);
                    }
                }
            }

            private Element element(MouseEvent e) {
                JTextPane pane = (JTextPane)e.getSource();
                StyledDocument doc = pane.getStyledDocument();
                return doc.getCharacterElement(pane.viewToModel(e.getPoint()));
            }

            private void openStackTrace(MouseEvent e, boolean showHistory) {
                Element elem = element(e);
                AttributeSet as = elem.getAttributes();
                StackTraceAction stacktraceAction = (StackTraceAction) as.getAttribute(STACKTRACE_ATTRIBUTE);
                if (stacktraceAction != null) {
                    try {
                        StackTraceAction.openStackTrace(elem.getDocument().getText(elem.getStartOffset(), elem.getEndOffset() - elem.getStartOffset()), showHistory);
                    } catch(Exception ex) {
                        Bugzilla.LOG.log(Level.SEVERE, null, ex);
                    }
                }
            }

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
    }

    public void setIssue(BugzillaIssue issue) {
        removeAll();
        this.issue = issue;
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
        String creationTxt = issue.getFieldValue(BugzillaIssue.IssueField.CREATION);
        try {
            Date creation = dateTimeFormat.parse(creationTxt);
            creationTxt = format.format(creation);
        } catch (ParseException pex) {
            Bugzilla.LOG.log(Level.INFO, null, pex);
        }
        addSection(layout, issue.getFieldValue(BugzillaIssue.IssueField.DESCRIPTION), issue.getFieldValue(BugzillaIssue.IssueField.REPORTER_NAME), creationTxt, horizontalGroup, verticalGroup, true);
        for (BugzillaIssue.Comment comment : issue.getComments()) {
            String when = format.format(comment.getWhen());
            addSection(layout, comment.getText(), comment.getWho(), when, horizontalGroup, verticalGroup, false);
        }
        verticalGroup.addContainerGap();
        setLayout(layout);
    }

    public void setNewCommentHandler(NewCommentHandler handler) {
        newCommentHandler = handler;
    }

    private void addSection(GroupLayout layout, String text, String author, String dateTimeString,
            GroupLayout.ParallelGroup horizontalGroup, GroupLayout.SequentialGroup verticalGroup, boolean description) {
        JTextPane textPane = new JTextPane();
        JLabel leftLabel = new JLabel();
        ResourceBundle bundle = NbBundle.getBundle(CommentsPanel.class);
        String leftTxt;
        if (description) {
            String leftFormat = bundle.getString("CommentsPanel.leftLabel.format"); // NOI18N
            leftTxt = MessageFormat.format(leftFormat, issue.getSummary());
        } else {
            leftTxt = bundle.getString("CommentsPanel.leftLabel.text"); // NOI18N
        }
        leftLabel.setText(leftTxt);
        JLabel rightLabel = new JLabel();
        String rightFormat = bundle.getString("CommentsPanel.rightLabel.format"); // NOI18N
        String rightTxt = MessageFormat.format(rightFormat, dateTimeString, author);
        rightLabel.setText(rightTxt);
        LinkButton replyButton = new LinkButton(bundle.getString("Comments.replyButton.text")); // NOI18N
        replyButton.addActionListener(getReplyListener());
        replyButton.putClientProperty(REPLY_TO_PROPERTY, textPane);
        setupTextPane(textPane, text);

        // Layout
        horizontalGroup.add(layout.createSequentialGroup()
            .add(leftLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(LayoutStyle.RELATED)
            .add(replyButton)
            .addPreferredGap(LayoutStyle.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .add(rightLabel))
        .add(textPane, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
        if (!description) {
            verticalGroup.addPreferredGap(LayoutStyle.UNRELATED);
        }
        verticalGroup.add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
            .add(leftLabel)
            .add(replyButton)
            .add(rightLabel))
            .addPreferredGap(LayoutStyle.RELATED)
            .add(textPane, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE);
    }

    private void setupTextPane(JTextPane textPane, String comment) {
        StyledDocument doc = textPane.getStyledDocument();
        Caret caret = textPane.getCaret();
        if (caret instanceof DefaultCaret) {
            ((DefaultCaret)caret).setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
        }

        // Stack traces
        List<StackTraceSupport.StackTracePosition> stacktraces = StackTraceSupport.find(comment);
        if(stacktraces.isEmpty()) {
            textPane.setText(comment);
        } else {
            Style defStyle = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
            Style hlStyle = doc.addStyle("regularBlue", defStyle); // NOI18N
            hlStyle.addAttribute(STACKTRACE_ATTRIBUTE, new StackTraceAction());
            StyleConstants.setForeground(hlStyle, Color.BLUE);
            StyleConstants.setUnderline(hlStyle, true);

            int last = 0;
            textPane.setText(""); // NOI18N
            for (StackTraceSupport.StackTracePosition stp : stacktraces) {
                int start = stp.getStartOffset();
                int end = stp.getEndOffset();

                String st = comment.substring(start, end);
                try {
                    doc.insertString(doc.getLength(), comment.substring(last, start), defStyle);
                    doc.insertString(doc.getLength(), st, hlStyle);
                } catch (BadLocationException ex) {
                    Bugzilla.LOG.log(Level.SEVERE, null, ex);
                }
                last = end;
            }
            try {
                doc.insertString(doc.getLength(), comment.substring(last), defStyle);
            } catch (BadLocationException ex) {
                Bugzilla.LOG.log(Level.SEVERE, null, ex);
            }
        }

        // Issues/bugs
        int[] pos = IssueFinder.getIssueSpans(comment);
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

        textPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIManager.getColor("Label.foreground")), // NOI18N
                BorderFactory.createEmptyBorder(3,3,3,3)));
        textPane.setEditable(false);
        textPane.addMouseListener(listener);
        textPane.addMouseMotionListener(listener);
    }

    private ActionListener replyListener;
    private ActionListener getReplyListener() {
        if (replyListener == null) {
            replyListener = new ActionListener() {
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

    private static class StackTraceAction extends AbstractAction {
        private String stackFrame;
        private boolean showHistory;

        StackTraceAction() {
        }

        StackTraceAction(String stackFrame, boolean showHistory) {
            this.stackFrame = stackFrame;
            this.showHistory = showHistory;
            String name = NbBundle.getMessage(StackTraceAction.class, showHistory ? "CommentsPanel.StackTraceAction.showHistory" : "CommentsPanel.StackTraceAction.open"); // NOI18N
            putValue(Action.NAME, name);
        }

        static void openStackTrace(String text, boolean showHistory) {
            if (showHistory) {
                StackTraceSupport.findAndShowHistory(text);
            } else {
                StackTraceSupport.findAndOpen(text);
            }
        }

        public void actionPerformed(ActionEvent e) {
            openStackTrace(stackFrame, showHistory);
        }
    }

    private class IssueAction {
        void openIssue(String text) {
            final String issueNo = IssueFinder.getIssueNumber(text);
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    Issue is = issue.getRepository().getIssue(issueNo);
                    if (is != null) {
                        is.open();
                    }
                }
            });
        }
    }

    public interface NewCommentHandler {
        void append(String text);
    }

}
