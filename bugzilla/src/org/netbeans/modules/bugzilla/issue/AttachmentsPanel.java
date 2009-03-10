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
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.text.DateFormat;
import java.util.Date;
import java.util.ResourceBundle;
import javax.swing.AbstractAction;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import org.jdesktop.layout.GroupLayout;
import org.jdesktop.layout.LayoutStyle;
import org.netbeans.modules.bugtracking.util.LinkButton;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Stola
 */
public class AttachmentsPanel extends JPanel {
    private BugzillaIssue issue;
    private JLabel noneLabel;
    private LinkButton createNewButton;

    public AttachmentsPanel() {
        setBackground(UIManager.getColor("EditorPane.background")); // NOI18N
        ResourceBundle bundle = NbBundle.getBundle(AttachmentsPanel.class);
        noneLabel = new JLabel(bundle.getString("AttachmentsPanel.noneLabel.text")); // NOI18N
        createNewButton = new LinkButton(new CreateNewAction());
    }

    public void setIssue(BugzillaIssue issue) {
        removeAll();
        this.issue = issue;

        GroupLayout layout = new GroupLayout(this);
        setLayout(layout);
        GroupLayout.ParallelGroup horizontalGroup = layout.createParallelGroup(GroupLayout.LEADING);
        GroupLayout.SequentialGroup verticalGroup = layout.createSequentialGroup();
        ResourceBundle bundle = NbBundle.getBundle(AttachmentsPanel.class);
        String createNewButtonText = bundle.getString("AttachmentsPanel.createNewButton.text"); // NOI18N

        BugzillaIssue.Attachment[] attachments = issue.getAttachments();
        if (attachments.length == 0) {
            // noneLabel + createNewButton
            createNewButton.setText('('+createNewButtonText+')');
            horizontalGroup.add(layout.createSequentialGroup()
                    .add(noneLabel)
                    .addPreferredGap(LayoutStyle.RELATED)
                    .add(createNewButton)
                    .add(0, 0, Short.MAX_VALUE));
            verticalGroup.add(layout.createParallelGroup(GroupLayout.BASELINE)
                    .add(noneLabel)
                    .add(createNewButton));
        } else {
            JLabel descriptionLabel = new JLabel(bundle.getString("AttachmentsPanel.table.description")); // NOI18N
            JLabel filenameLabel = new JLabel(bundle.getString("AttachmentsPanel.table.filename")); // NOI18N
            JLabel dateLabel =  new JLabel(bundle.getString("AttachmentsPanel.table.date")); // NOI18N
            JLabel authorLabel = new JLabel(bundle.getString("AttachmentsPanel.table.author")); // NOI18N
            makeBold(descriptionLabel);
            makeBold(filenameLabel);
            makeBold(dateLabel);
            makeBold(authorLabel);
            GroupLayout.ParallelGroup descriptionGroup = layout.createParallelGroup();
            GroupLayout.ParallelGroup filenameGroup = layout.createParallelGroup();
            GroupLayout.ParallelGroup dateGroup = layout.createParallelGroup();
            GroupLayout.ParallelGroup authorGroup = layout.createParallelGroup();
            int descriptionWidth = Math.max(descriptionLabel.getPreferredSize().width, 150);
            descriptionGroup.add(descriptionLabel, GroupLayout.PREFERRED_SIZE, descriptionWidth, GroupLayout.PREFERRED_SIZE);
            filenameGroup.add(filenameLabel);
            dateGroup.add(dateLabel);
            authorGroup.add(authorLabel);
            JPanel panel = createHighlightPanel();
            GroupLayout.ParallelGroup horizontalSubgroup = layout.createParallelGroup(GroupLayout.LEADING, false);
            horizontalGroup.add(horizontalSubgroup
                .add(panel, 0, 0, Short.MAX_VALUE)
                .add(layout.createSequentialGroup()
                    .add(descriptionGroup)
                    .addPreferredGap(LayoutStyle.UNRELATED)
                    .add(filenameGroup)
                    .addPreferredGap(LayoutStyle.UNRELATED)
                    .add(dateGroup)
                    .addPreferredGap(LayoutStyle.UNRELATED)
                    .add(authorGroup)));
            verticalGroup.add(layout.createParallelGroup(GroupLayout.LEADING, false)
                .add(panel, 0, 0, Short.MAX_VALUE)
                .add(layout.createParallelGroup(GroupLayout.BASELINE)
                    .add(descriptionLabel)
                    .add(filenameLabel)
                    .add(dateLabel)
                    .add(authorLabel)));
            for (BugzillaIssue.Attachment attachment : attachments) {
                String description = attachment.getDesc();
                String filename = attachment.getFilename();
                Date date = attachment.getDate();
                String author = attachment.getAuthor();
                descriptionLabel = new JLabel(description);
                LinkButton filenameButton = new LinkButton(filename);
                dateLabel = new JLabel(DateFormat.getDateInstance().format(date));
                authorLabel = new JLabel(author);
                descriptionGroup.add(descriptionLabel);
                filenameGroup.add(filenameButton);
                dateGroup.add(dateLabel);
                authorGroup.add(authorLabel);
                panel = createHighlightPanel();
                horizontalSubgroup.add(panel, 0, 0, Short.MAX_VALUE);
                verticalGroup
                    .addPreferredGap(LayoutStyle.RELATED)
                    .add(layout.createParallelGroup(GroupLayout.LEADING, false)
                        .add(panel, 0, 0, Short.MAX_VALUE)
                        .add(layout.createParallelGroup(GroupLayout.BASELINE)
                            .add(descriptionLabel)
                            .add(filenameButton)
                            .add(dateLabel)
                            .add(authorLabel)));
            }
            // createNewButton
            createNewButton.setText(createNewButtonText);
            horizontalGroup.add(layout.createSequentialGroup()
                    .add(createNewButton)
                    .add(0, 0, Short.MAX_VALUE));
            verticalGroup.addPreferredGap(LayoutStyle.RELATED);
            verticalGroup.add(createNewButton);
        }
        layout.setHorizontalGroup(horizontalGroup);
        layout.setVerticalGroup(verticalGroup);
        ((CreateNewAction)createNewButton.getAction()).setLayoutGroups(horizontalGroup, verticalGroup);
    }

    private void makeBold(JLabel label) {
        Font font = label.getFont().deriveFont(Font.BOLD);
        label.setFont(font);
    }

    private JPanel createHighlightPanel() {
        JPanel panel = new JPanel();
        // PENDING what color (e.g. what key from UIDefaults) should I use?
        panel.setBackground(new Color(220, 220, 220));
        add(panel);
        return panel;
    }

    class CreateNewAction extends AbstractAction {
        private GroupLayout.ParallelGroup horizontalGroup;
        private GroupLayout.SequentialGroup verticalGroup;

        void setLayoutGroups(GroupLayout.ParallelGroup horizontalGroup,
                GroupLayout.SequentialGroup verticalGroup) {
            this.horizontalGroup = horizontalGroup;
            this.verticalGroup = verticalGroup;
        }

        public void actionPerformed(ActionEvent e) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

    }

}
