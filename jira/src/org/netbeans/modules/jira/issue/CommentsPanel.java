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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextPane;
import javax.swing.LayoutStyle;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.basic.BasicTextPaneUI;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.text.Caret;
import javax.swing.text.DefaultCaret;
import org.netbeans.modules.bugtracking.commons.IssueSettingsStorage;
import org.netbeans.modules.bugtracking.commons.LinkButton;
import org.netbeans.modules.bugtracking.commons.UIUtils;
import org.netbeans.modules.jira.Jira;
import org.netbeans.modules.jira.client.spi.User;
import org.netbeans.modules.jira.kenai.KenaiRepository;
import org.netbeans.modules.jira.repository.JiraConfiguration;
import org.netbeans.modules.team.spi.TeamAccessorUtils;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Jan Stola
 */
public class CommentsPanel extends JPanel {
    private static final RequestProcessor RP = new RequestProcessor("Jira Comments Panel", 5, false); // NOI18N
    private final static String REPLY_TO_PROPERTY = "replyTo"; // NOI18N
    private final static String QUOTE_PREFIX = "> "; // NOI18N
    private NbJiraIssue issue;
    private NewCommentHandler newCommentHandler;

    private Set<Long> collapsedComments = Collections.synchronizedSet(new HashSet<Long>());
    private List<ExpandLabel> sections;

    private static Color blueBackground = null;
    private static Color greyForeground = null;
    
    static {
        blueBackground = UIManager.getColor( "nb.bugtracking.comment.background" ); //NOI18N
        if( null == blueBackground )
            blueBackground = new Color(0xf3f6fd);
        greyForeground = UIManager.getColor( "nb.bugtracking.comment.foreground" ); //NOI18N
        if( null == greyForeground )
            greyForeground = new Color(0x999999);
    }

    public CommentsPanel() {
        setOpaque( false );
    }

    public void setIssue(NbJiraIssue issue) {
        removeAll();
        this.issue = issue;
        initCollapsedComments();
        GroupLayout layout = new GroupLayout(this);
        GroupLayout.ParallelGroup horizontalGroup = layout.createParallelGroup(GroupLayout.Alignment.LEADING);
        layout.setHorizontalGroup(layout.createSequentialGroup()
            .addContainerGap()
            .addGroup(horizontalGroup)
            .addContainerGap());
        GroupLayout.SequentialGroup verticalGroup = layout.createSequentialGroup();
        verticalGroup.addContainerGap();
        layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(verticalGroup));
        DateFormat format = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.SHORT);
        String creationTxt = issue.getFieldValue(NbJiraIssue.IssueField.CREATION);
        try {
            long millis = Long.parseLong(creationTxt);
            creationTxt = format.format(new Date(millis));
        } catch (NumberFormatException nfex) {
            Jira.LOG.log(Level.INFO, null, nfex);
        }
        String description = issue.getFieldValue(NbJiraIssue.IssueField.DESCRIPTION);
        JiraConfiguration rc = issue.getRepository().getConfiguration();
        User user = rc != null ? rc.getUser(issue.getFieldValue(NbJiraIssue.IssueField.REPORTER)) : null;
        String reporter = user != null ? user.getFullName() : ""; // NOI18N
        NbJiraIssue.Comment[] comments = issue.getComments();
        this.sections = new ArrayList<>(comments.length);
        sections.add(addSection(
                layout, 
                new Long(0),    
                description, 
                reporter, 
                creationTxt, 
                horizontalGroup, 
                verticalGroup, 
                true));
        for (NbJiraIssue.Comment comment : comments) {
            Date date = comment.getWhen();
            String when = (date == null) ? "" : format.format(date); // NOI18N
            sections.add(addSection(
                    layout, 
                    comment.getNumber(),
                    comment.getText(), 
                    comment.getWho(), 
                    when, 
                    horizontalGroup, 
                    verticalGroup, false));
        }
        verticalGroup.addContainerGap();
        setLayout(layout);
    }

    public void setNewCommentHandler(NewCommentHandler handler) {
        newCommentHandler = handler;
    }

    private ExpandLabel addSection(GroupLayout layout, Long number, String text, String author, String dateTimeString,
            GroupLayout.ParallelGroup horizontalGroup, GroupLayout.SequentialGroup verticalGroup, boolean description) {
        
        JTextPane textPane = new JTextPane();
        setupTextPane(textPane, text);
        
        
        JPanel headerPanel = new JPanel();
        JPanel placeholder = createTextPanelPlaceholder();      
        JLabel commentLabel = new JLabel();
        ExpandLabel iconLabel = new ExpandLabel(placeholder, textPane, headerPanel, commentLabel, number);
        JLabel leftLabel = new JLabel();
        JLabel rightLabel = new JLabel();
        
        headerPanel.addMouseListener(iconLabel);
        headerPanel.setComponentPopupMenu(expandPopup);
        headerPanel.setOpaque( false );
        
        // left label
        ResourceBundle bundle = NbBundle.getBundle(CommentsPanel.class);
        String leftTxt = "";
        if (description) {
            String leftFormat = bundle.getString("CommentsPanel.leftLabel.format"); // NOI18N
            leftTxt = MessageFormat.format(leftFormat, author);
        } else {
            leftTxt = author;
        }
        leftLabel.setText(leftTxt);
        leftLabel.setLabelFor(textPane);
        leftLabel.setForeground(greyForeground);
        leftLabel.setOpaque(false);
        leftLabel.addMouseListener(iconLabel);
        leftLabel.setComponentPopupMenu(expandPopup);
        
        // comment label
        commentLabel.setOpaque(false);
        commentLabel.addMouseListener(iconLabel);
        commentLabel.setComponentPopupMenu(expandPopup);
        
        // right label
        rightLabel.setText(dateTimeString);
        rightLabel.setForeground(greyForeground);
        rightLabel.setOpaque(false);
        rightLabel.addMouseListener(iconLabel);
        rightLabel.setComponentPopupMenu(expandPopup);
        
        // state label
        JLabel stateLabel = null;
        if (issue.getRepository() instanceof KenaiRepository) {
            String host = ((KenaiRepository) issue.getRepository()).getHost();
            stateLabel = TeamAccessorUtils.createUserWidget(issue.getRepository().getUrl(), author, host, TeamAccessorUtils.getChatLink(issue.getKey()));
            if (stateLabel != null) {
                stateLabel.setText(null);
            }
        }
        
        // replay button
        LinkButton replyButton = new LinkButton(bundle.getString("Comments.replyButton.text")); // NOI18N
        replyButton.addActionListener(getReplyListener());
        replyButton.putClientProperty(REPLY_TO_PROPERTY, textPane);
        replyButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CommentsPanel.class, "CommentsPanel.replyButton.AccessibleContext.accessibleDescription")); // NOI18N
        replyButton.setOpaque(false);
        
        iconLabel.setBorder(BorderFactory.createEmptyBorder(1,1,1,1));
        placeholder.setBorder(BorderFactory.createEmptyBorder(1,1,1,1));
        leftLabel.setBorder(BorderFactory.createEmptyBorder(1,1,1,1));
        commentLabel.setBorder(BorderFactory.createEmptyBorder(1,1,1,1));
        rightLabel.setBorder(BorderFactory.createEmptyBorder(1,1,1,1));
        
        layoutHeaderPanel(headerPanel, iconLabel, leftLabel, commentLabel, rightLabel, replyButton, stateLabel);
        
        horizontalGroup
            .addComponent(headerPanel)
            .addGroup(layout.createSequentialGroup()
                .addComponent(placeholder, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(textPane));                
        
        if (!description) {
            verticalGroup.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED);
        }
        verticalGroup
            .addComponent(headerPanel)
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                .addComponent(placeholder)
                .addComponent(textPane, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));
        return iconLabel;
    }

    private JPanel createTextPanelPlaceholder() {
        JPanel placeholder = new JPanel();
        placeholder.setBackground(blueBackground);
        GroupLayout layout = new GroupLayout(placeholder);
        placeholder.setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, ICON_WIDTH, Short.MAX_VALUE));
        layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 0, Short.MAX_VALUE));
        return placeholder;
    }

    private void setupTextPane(JTextPane textPane, String comment) {
        if( UIUtils.isNimbus() ) {
            textPane.setUI( new BasicTextPaneUI() );
        }
        Caret caret = textPane.getCaret();
        if (caret instanceof DefaultCaret) {
            ((DefaultCaret)caret).setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
        }

        textPane.setText(comment);
        
        textPane.setBackground(blueBackground);
        textPane.setBorder(BorderFactory.createEmptyBorder(3,3,3,3));
        textPane.setEditable(false);
        textPane.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CommentsPanel.class, "CommentsPanel.textPane.AccessibleContext.accessibleName")); // NOI18N
        textPane.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CommentsPanel.class, "CommentsPanel.textPane.AccessibleContext.accessibleDescription")); // NOI18N
    }

    private void layoutHeaderPanel(JPanel headerPanel, JLabel iconLabel, JLabel leftLabel, JLabel commentLabel, JLabel rightLabel, LinkButton replyButton, JLabel stateLabel) {
        GroupLayout layout = new GroupLayout(headerPanel);
        headerPanel.setLayout(layout);
        GroupLayout.SequentialGroup hGroup = layout.createSequentialGroup()
            .addComponent(iconLabel)
            .addComponent(leftLabel);
        if (stateLabel != null) {
            hGroup.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                  .addComponent(stateLabel);
        }
        hGroup.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
              .addComponent(commentLabel,0, 0, Short.MAX_VALUE)
              .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
              .addComponent(rightLabel)
              .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
              .addComponent(replyButton);
        layout.setHorizontalGroup(hGroup);
        
        GroupLayout.ParallelGroup vGroup = layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
            .addComponent(iconLabel)
            .addComponent(leftLabel);
        if (stateLabel != null) {
            vGroup.addComponent(stateLabel);
        }
        vGroup.addComponent(commentLabel)
              .addComponent(rightLabel)
              .addComponent(replyButton);
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

    void collapseAll () {
        for (ExpandLabel lbl : sections) {
            lbl.setState(true);
        }
    }

    void expandAll () {
        for (ExpandLabel lbl : sections) {
            lbl.setState(false);
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
        RP.post(new Runnable() {
            @Override
            public void run() {
                Collection<Long> s = IssueSettingsStorage.getInstance().loadCollapsedCommenst(issue.getRepository().getUrl(), issue.getKey());
                for (Long l : s) {
                    if(!touchedCommenst.contains(l)) {
                        collapsedComments.add(l);
                    }
                }
            }
        });
    }
    
    void storeSettings() {
        if(issue != null) {
            IssueSettingsStorage.getInstance().storeCollapsedComments(collapsedComments, issue.getRepository().getUrl(), issue.getKey());
        } 
    }    

    private final static Icon ei;
    private final static Icon ci;
    private static final int ICON_WIDTH;
    static {
        JTree tv = new JTree();
        BasicTreeUI tvui = (BasicTreeUI) tv.getUI();
        ei = tvui.getExpandedIcon();
        ci = tvui.getCollapsedIcon();
        ICON_WIDTH = ei != null ? ei.getIconWidth() : 16;
    }
    
    private class ExpandLabel extends JLabel implements MouseListener {
        private final JTextPane textPane;
        private final JPanel headerPanel;
        private final JPanel placeholderPanel;
        private final JLabel commentLabel;
        private final Long number;
        
        public ExpandLabel(JPanel placeholderPanel, JTextPane textPane, JPanel headerPanel, JLabel commentLabel, Long number) {
            this.textPane = textPane;
            this.headerPanel = headerPanel;
            this.placeholderPanel = placeholderPanel;
            this.commentLabel = commentLabel;
            this.number = number;

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
                placeholderPanel.setVisible(false);
                commentLabel.setText(textPane.getText().replace("\n", " ").replace("\t", " "));
                setIcon(ci);
                headerPanel.setBackground(blueBackground);
                headerPanel.setOpaque( true );
                commentCollapsed(number);
            } else {
                textPane.setVisible(true);
                placeholderPanel.setVisible(true);
                commentLabel.setText("");
                setIcon(ei);
                headerPanel.setOpaque( false );
                commentExpanded(number);
            }           
        }
    }    
}
