/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicTreeUI;
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
import org.netbeans.modules.bugtracking.ui.issue.cache.IssueSettingsStorage;
import org.netbeans.modules.bugtracking.util.LinkButton;
import org.netbeans.modules.bugtracking.util.StackTraceSupport;
import org.netbeans.modules.bugtracking.util.TextUtils;
import org.netbeans.modules.jira.Jira;
import org.netbeans.modules.jira.kenai.KenaiRepository;
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
    private MouseMotionListener motionListener;
    private NewCommentHandler newCommentHandler;

    private Set<Long> collapsedComments = Collections.synchronizedSet(new HashSet<Long>());
    
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
        initCollapsedComments();
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
        addSection(
                layout, 
                new Long(0),    
                description, 
                reporter, 
                creationTxt, 
                horizontalGroup, 
                verticalGroup, 
                true);
        for (NbJiraIssue.Comment comment : issue.getComments()) {
            Date date = comment.getWhen();
            String when = (date == null) ? "" : format.format(date); // NOI18N
            addSection(
                    layout, 
                    comment.getNumber(),
                    comment.getText(), 
                    comment.getWho(), 
                    when, 
                    horizontalGroup, 
                    verticalGroup, false);
        }
        verticalGroup.addContainerGap();
        setLayout(layout);
    }

    public void setNewCommentHandler(NewCommentHandler handler) {
        newCommentHandler = handler;
    }

    private void addSection(GroupLayout layout, Long number, String text, String author, String dateTimeString,
            GroupLayout.ParallelGroup horizontalGroup, GroupLayout.SequentialGroup verticalGroup, boolean description) {
        JTextPane textPane = new JTextPane();
        JPanel headerPanel = new JPanel();
        JLabel leftLabel = new ExpandLabel(textPane, headerPanel, number);
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
        String rightTxt = MessageFormat.format(rightFormat, dateTimeString, author);
        rightLabel.setText(rightTxt);
        rightLabel.setLabelFor(textPane);
        JLabel stateLabel = null;
        if (issue.getRepository() instanceof KenaiRepository) {
            String host = ((KenaiRepository) issue.getRepository()).getHost();
            stateLabel = KenaiUtil.createUserWidget(author, host, KenaiUtil.getChatLink(issue));
            stateLabel.setText(null);
        }
        LinkButton replyButton = new LinkButton(bundle.getString("Comments.replyButton.text")); // NOI18N
        replyButton.addActionListener(getReplyListener());
        replyButton.putClientProperty(REPLY_TO_PROPERTY, textPane);
        replyButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CommentsPanel.class, "CommentsPanel.replyButton.AccessibleContext.accessibleDescription")); // NOI18N
        
        setupHeaderPanel(headerPanel, leftLabel, replyButton, rightLabel, stateLabel);
        setupTextPane(textPane, text);

        // Layout
        horizontalGroup
            .add(headerPanel)
            .add(textPane, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
        if (!description) {
            verticalGroup.addPreferredGap(LayoutStyle.UNRELATED);
        }
        verticalGroup
            .add(headerPanel)
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
                    Jira.LOG.log(Level.INFO, blex.getMessage(), blex);
                }
            }
        }

        textPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIManager.getColor("Label.foreground")), // NOI18N
                BorderFactory.createEmptyBorder(3,3,3,3)));
        textPane.setEditable(false);
        textPane.addMouseListener(listener);
        textPane.addMouseMotionListener(motionListener);
        textPane.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CommentsPanel.class, "CommentsPanel.textPane.AccessibleContext.accessibleName")); // NOI18N
        textPane.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CommentsPanel.class, "CommentsPanel.textPane.AccessibleContext.accessibleDescription")); // NOI18N
    }

    private void setupHeaderPanel(JPanel headerPanel, JLabel leftLabel, LinkButton replyButton, JLabel rightLabel, JLabel stateLabel) {
        headerPanel.setOpaque(false);
        GroupLayout layout = new org.jdesktop.layout.GroupLayout(headerPanel);
        headerPanel.setLayout(layout);
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
        layout.setHorizontalGroup(hGroup);
        GroupLayout.ParallelGroup vGroup = layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
            .add(leftLabel)
            .add(replyButton)
            .add(rightLabel);
        if (stateLabel != null) {
            vGroup.add(stateLabel);
        }
        layout.setVerticalGroup(vGroup);
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
        void openIssue(final String hyperlinkText) {
            final String issueKey = issueFinder.getIssueId(hyperlinkText);
            RequestProcessor.getDefault().post(new Runnable() {
                @Override
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

    private final JPopupMenu expandPopup = new ExpandPopupMenu();
    private Set<ExpandLabel> expandLabels = new HashSet<ExpandLabel>();
    
    private class ExpandPopupMenu extends JPopupMenu {
        public ExpandPopupMenu() {
            add(new JMenuItem(new AbstractAction(NbBundle.getMessage(CommentsPanel.class, "LBL_ExpandAll")) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    for (ExpandLabel l : expandLabels) {
                        l.setState(false);
                    }
                }
            }));
            add(new JMenuItem(new AbstractAction(NbBundle.getMessage(CommentsPanel.class, "LBL_CollapseAll")) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    for (ExpandLabel l : expandLabels) {
                        l.setState(true);
                    }
                }
            }));
        }
    }    

    private void commentCollapsed(Long number) {
        collapsedComments.add(number);
    }

    private Set<Long> touchedCommenst = Collections.synchronizedSet(new HashSet<Long>());
    private void commentExpanded(Long number) {
        if(collapsedComments.remove(number)) {
            touchedCommenst.add(number);
        }
    }

    private boolean isCollapsed(Long number) {
        return collapsedComments.contains(number);
    }
    
    private void initCollapsedComments() {
        RequestProcessor.getDefault().post(new Runnable() {
            @Override
            public void run() {
                Collection<Long> s = IssueSettingsStorage.getInstance().loadCollapsedCommenst(issue.getRepository().getUrl(), issue.getID());
                for (Long l : s) {
                    if(!touchedCommenst.contains(l)) {
                        collapsedComments.add(l);
                    }
                }
            }
        });
    }
    
    void storeSettings() {
        IssueSettingsStorage.getInstance().storeCollapsedComments(collapsedComments, issue.getRepository().getUrl(), issue.getID());
    }    
    
    private class ExpandLabel extends JLabel implements MouseListener {
        private final JTextPane textPane;
        private final JPanel headerPanel;
        private final Long number;
        private final Icon ei;
        private final Icon ci;
        
        private Border border;

        public ExpandLabel(JTextPane textPane, JPanel headerPanel, Long number) {
            this.textPane = textPane;
            this.headerPanel = headerPanel;
            this.number = number;

            border = headerPanel.getBorder(); 
            
            JTree tv = new JTree();
            BasicTreeUI tvui = (BasicTreeUI) tv.getUI();
            ei = tvui.getExpandedIcon();
            ci = tvui.getCollapsedIcon();
            
            addMouseListener(this);
            setComponentPopupMenu(expandPopup);
            setState(isCollapsed(number));
            expandLabels.add(this);
        }
        
        @Override
        public void mouseClicked(MouseEvent e) {
            if (SwingUtilities.isLeftMouseButton(e)) {
                setState(!isCollapsed(number)); 
            } 
        }

        @Override
        public void mousePressed(MouseEvent e) {}
        @Override
        public void mouseReleased(MouseEvent e) {}
        @Override
        public void mouseEntered(MouseEvent e) {}
        @Override
        public void mouseExited(MouseEvent e) {}
        
        private void setState(boolean collapsed) {
            if(collapsed) {
                textPane.setVisible(false);
                setIcon(ci);
                headerPanel.setBorder(BorderFactory.createLineBorder(UIManager.getColor("Label.foreground")));
                commentCollapsed(number);
            } else {
                textPane.setVisible(true);
                setIcon(ei);
                headerPanel.setBorder(border);
                commentExpanded(number);
            }           
        }
    }    
}
