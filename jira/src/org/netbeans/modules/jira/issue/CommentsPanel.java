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

package org.netbeans.modules.jira.issue;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.logging.Level;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
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
import org.netbeans.modules.bugtracking.util.LinkButton;
import org.netbeans.modules.bugtracking.util.StackTraceSupport;
import org.netbeans.modules.jira.Jira;
import org.netbeans.modules.jira.kenai.KenaiRepository;
import org.netbeans.modules.kenai.ui.spi.KenaiUser;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Jan Stola
 */
public class CommentsPanel extends JPanel {
    private final static String ISSUE_ATTRIBUTE = "issue"; // NOI18N
    private final static String REPLY_TO_PROPERTY = "replyTo"; // NOI18N
    private final static String QUOTE_PREFIX = "> "; // NOI18N
    private NbJiraIssue issue;
    private JiraIssueFinder issueFinder;
    private MouseAdapter listener;
    private NewCommentHandler newCommentHandler;

    public CommentsPanel() {
        setBackground(UIManager.getColor("EditorPane.background")); // NOI18N
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
                            issueAction.openIssue(elem.getDocument().getText(elem.getStartOffset(), elem.getEndOffset() - elem.getStartOffset()));
                        }
                    }
                } catch(Exception ex) {
                    Jira.LOG.log(Level.SEVERE, null, ex);
                }
            }
        };

        issueFinder = Lookup.getDefault().lookup(JiraIssueFinder.class);
        assert issueFinder != null;
    }

    public void setIssue(NbJiraIssue issue) {
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
        String creationTxt = issue.getFieldValue(NbJiraIssue.IssueField.CREATION);
        try {
            long millis = Long.parseLong(creationTxt);
            creationTxt = format.format(new Date(millis));
        } catch (NumberFormatException nfex) {
            Jira.LOG.log(Level.INFO, null, nfex);
        }
        String description = issue.getFieldValue(NbJiraIssue.IssueField.DESCRIPTION);
        String reporter = issue.getRepository().getConfiguration().getUser(issue.getFieldValue(NbJiraIssue.IssueField.REPORTER)).getFullName();
        addSection(layout, description, reporter, creationTxt, horizontalGroup, verticalGroup, true);
        for (NbJiraIssue.Comment comment : issue.getComments()) {
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
        rightLabel.setLabelFor(textPane);
        JLabel stateLabel = null;
        if (issue.getRepository() instanceof KenaiRepository) {
            stateLabel = KenaiUser.forName(author).createUserWidget();
            stateLabel.setText(null);
        }
        LinkButton replyButton = new LinkButton(bundle.getString("Comments.replyButton.text")); // NOI18N
        replyButton.addActionListener(getReplyListener());
        replyButton.putClientProperty(REPLY_TO_PROPERTY, textPane);
        replyButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CommentsPanel.class, "CommentsPanel.replyButton.AccessibleContext.accessibleDescription")); // NOI18N
        setupTextPane(textPane, text);

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
        .add(textPane, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
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
            .addPreferredGap(LayoutStyle.RELATED)
            .add(textPane, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE);
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

        textPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIManager.getColor("Label.foreground")), // NOI18N
                BorderFactory.createEmptyBorder(3,3,3,3)));
        textPane.setEditable(false);
        textPane.addMouseListener(listener);
        textPane.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CommentsPanel.class, "CommentsPanel.textPane.AccessibleContext.accessibleName")); // NOI18N
        textPane.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CommentsPanel.class, "CommentsPanel.textPane.AccessibleContext.accessibleDescription")); // NOI18N
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

    private class IssueAction {
        void openIssue(final String hyperlinkText) {
            final String issueKey = issueFinder.getIssueId(hyperlinkText);
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    Issue is = issue.getRepository().getIssue(issueKey);
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
