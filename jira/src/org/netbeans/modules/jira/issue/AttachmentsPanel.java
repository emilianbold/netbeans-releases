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

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import org.eclipse.mylyn.internal.tasks.core.data.FileTaskAttachmentSource;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.bugtracking.util.LinkButton;
import org.netbeans.modules.bugtracking.util.PatchUtils;
import org.netbeans.modules.bugtracking.util.UIUtils;
import org.netbeans.modules.jira.Jira;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.HtmlBrowser;
import org.openide.cookies.OpenCookie;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Jan Stola
 */
public class AttachmentsPanel extends JPanel {
    private static final String NAME_FIELD_CP = "ATTACHMENT_NAME_FIELD"; // NOI18N
    private static final String BROWSE_CP = "ATTACHMENT_BROWSE_BUTTON"; // NOI18N
    private List<JTextField> newAttachments = new LinkedList<JTextField>();
    private JLabel noneLabel;
    private LinkButton createNewButton;
    private JLabel dummyLabel = new JLabel();
    private boolean hadNoAttachments;
    private IssuePanel issuePanel;

    public AttachmentsPanel(IssuePanel issuePanel) {
        this.issuePanel = issuePanel;
        setBackground(UIManager.getColor("TextArea.background")); // NOI18N
        ResourceBundle bundle = NbBundle.getBundle(AttachmentsPanel.class);
        noneLabel = new JLabel(bundle.getString("AttachmentsPanel.noneLabel.text")); // NOI18N
        createNewButton = new LinkButton(new CreateNewAction());
        createNewButton.getAccessibleContext().setAccessibleDescription(bundle.getString("AttachmentsPanel.createNewButton.AccessibleContext.accessibleDescription")); // NOI18N
    }

    public void setIssue(NbJiraIssue issue) {
        newAttachments.clear();
        removeAll();

        GroupLayout layout = new GroupLayout(this);
        GroupLayout.ParallelGroup horizontalGroup = layout.createParallelGroup(GroupLayout.Alignment.LEADING);
        GroupLayout.SequentialGroup verticalGroup = layout.createSequentialGroup();
        ResourceBundle bundle = NbBundle.getBundle(AttachmentsPanel.class);
        GroupLayout.SequentialGroup newVerticalGroup = layout.createSequentialGroup();

        NbJiraIssue.Attachment[] attachments = issue.getAttachments();
        hadNoAttachments = (attachments.length == 0);
        horizontalGroup.addGroup(layout.createSequentialGroup()
            .addComponent(noneLabel)
            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(hadNoAttachments ? createNewButton : dummyLabel)
            .addGap(0, 0, Short.MAX_VALUE));
        verticalGroup.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
            .addComponent(noneLabel)
            .addComponent(hadNoAttachments ? createNewButton : dummyLabel));
        dummyLabel.setVisible(false);
        noneLabel.setVisible(hadNoAttachments);
        updateCreateNewButton(hadNoAttachments);
        if (hadNoAttachments) {
            // noneLabel + createNewButton
            verticalGroup.addGroup(newVerticalGroup);
        } else {
            JLabel filenameLabel = new JLabel(bundle.getString("AttachmentsPanel.table.filename")); // NOI18N
            JLabel dateLabel =  new JLabel(bundle.getString("AttachmentsPanel.table.date")); // NOI18N
            JLabel authorLabel = new JLabel(bundle.getString("AttachmentsPanel.table.author")); // NOI18N
            makeBold(filenameLabel);
            makeBold(dateLabel);
            makeBold(authorLabel);
            GroupLayout.ParallelGroup filenameGroup = layout.createParallelGroup();
            GroupLayout.ParallelGroup dateGroup = layout.createParallelGroup();
            GroupLayout.ParallelGroup authorGroup = layout.createParallelGroup();
            filenameGroup.addComponent(filenameLabel);
            dateGroup.addComponent(dateLabel);
            authorGroup.addComponent(authorLabel);
            JPanel panel = createHighlightPanel();
            GroupLayout.ParallelGroup horizontalSubgroup = layout.createParallelGroup(GroupLayout.Alignment.LEADING, false);
            horizontalGroup.addGroup(horizontalSubgroup
                .addComponent(panel, 0, 0, Short.MAX_VALUE)
                .addGroup(layout.createSequentialGroup()
                    .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                    .addGroup(filenameGroup)
                    .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                    .addGroup(dateGroup)
                    .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                    .addGroup(authorGroup)));
            verticalGroup.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                .addComponent(panel, 0, 0, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(filenameLabel)
                    .addComponent(dateLabel)
                    .addComponent(authorLabel)));
            for (NbJiraIssue.Attachment attachment : attachments) {
                String filename = attachment.getFilename();
                Date date = attachment.getDate();
                String email = attachment.getEmail();
                String authorName = attachment.getAuthor();
                authorName = ((authorName != null) && (authorName.trim().length() > 0)) ? authorName : email;                
                LinkButton filenameButton = new LinkButton();
                JPopupMenu menu = menuFor(attachment);
                filenameButton.setAction(new DefaultAttachmentAction(attachment));
                filenameButton.setText(filename);
                filenameButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(AttachmentsPanel.class, "AttachmentsPanel.filenameButton.AccessibleContext.accessibleDescription")); // NOI18N
                dateLabel = new JLabel(date != null ? DateFormat.getDateInstance().format(date) : ""); // NOI18N
                JComponent authorComponent;
                if(email != null && !email.isEmpty()) {
                    authorComponent = new LinkButton.MailtoButton(authorName, NbBundle.getMessage(AttachmentsPanel.class, "AttachmentPanel.authorButton.AccessibleContext.accessibleDescription"), email);
                } else {
                    authorComponent = new JLabel(authorName);
                }
                filenameButton.setComponentPopupMenu(menu);
                dateLabel.setComponentPopupMenu(menu);
                authorComponent.setComponentPopupMenu(menu);
                filenameGroup.addComponent(filenameButton);
                dateGroup.addComponent(dateLabel);
                authorGroup.addComponent(authorComponent);
                panel = createHighlightPanel();
                panel.addMouseListener(new MouseAdapter() {}); // Workaround for bug 6272233
                panel.setComponentPopupMenu(menu);
                horizontalSubgroup.addComponent(panel, 0, 0, Short.MAX_VALUE);
                GroupLayout.ParallelGroup pGroup = layout.createParallelGroup(GroupLayout.Alignment.BASELINE);
                pGroup.addComponent(filenameButton);
                pGroup.addComponent(dateLabel);
                pGroup.addComponent(authorComponent);
                verticalGroup
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                        .addComponent(panel, 0, 0, Short.MAX_VALUE)
                        .addGroup(pGroup));
            }
            verticalGroup.addGroup(newVerticalGroup);
        }
        horizontalGroup.addGroup(layout.createSequentialGroup()
                .addComponent(hadNoAttachments ? dummyLabel : createNewButton)
                .addGap(0, 0, Short.MAX_VALUE));
        verticalGroup.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED);
        verticalGroup.addComponent(hadNoAttachments ? dummyLabel : createNewButton);

        layout.setHorizontalGroup(horizontalGroup);
        layout.setVerticalGroup(verticalGroup);
        ((CreateNewAction)createNewButton.getAction()).setLayoutGroups(horizontalGroup, newVerticalGroup);
        setLayout(layout);
    }

    private JPopupMenu menuFor(NbJiraIssue.Attachment attachment) {
        JPopupMenu menu = new JPopupMenu();
        menu.add(new DefaultAttachmentAction(attachment));
        menu.add(new SaveAttachmentAction(attachment));
        return menu;
    }

    private void updateCreateNewButton(boolean noAttachments) {
        String createNewButtonText = NbBundle.getMessage(AttachmentsPanel.class, "AttachmentsPanel.createNewButton.text"); // NOI18N
        createNewButton.setText(noAttachments ? ('('+createNewButtonText+')') : createNewButtonText);
    }

    private void makeBold(JLabel label) {
        Font font = label.getFont().deriveFont(Font.BOLD);
        label.setFont(font);
    }

    private JPanel createHighlightPanel() {
        JPanel panel = new JPanel();
        // PENDING what color (e.g. what key from UIDefaults) should I use?
        panel.setBackground(UIUtils.getSectionPanelBackground());
        add(panel);
        return panel;
    }

    private ActionListener deletedListener;
    ActionListener getDeletedListener() {
        if (deletedListener == null) {
            deletedListener = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    JComponent comp = (JComponent)e.getSource();
                    comp.setVisible(false);
                    JTextField nameField = (JTextField)comp.getClientProperty(NAME_FIELD_CP);
                    nameField.setVisible(false);
                    JComponent browseButton = (JComponent)comp.getClientProperty(BROWSE_CP);
                    browseButton.setVisible(false);
                    newAttachments.remove(nameField);
                    if (hadNoAttachments && newAttachments.isEmpty()) {
                        // The last attachment deleted
                        noneLabel.setVisible(true);
                        switchHelper();
                        updateCreateNewButton(true);
                    }
                }
            };
        }
        return deletedListener;
    }

    private ActionListener browseListener;
    ActionListener getBrowseListener() {
        if (browseListener == null) {
            browseListener = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    File attachment = new FileChooserBuilder(AttachmentsPanel.class).showOpenDialog();
                    if (attachment != null) {
                        attachment = FileUtil.normalizeFile(attachment);
                        JComponent comp = (JComponent)e.getSource();
                        JTextField nameField = (JTextField)comp.getClientProperty(NAME_FIELD_CP);
                        nameField.setText(attachment.getAbsolutePath());
                    }
                }
            };
        }
        return browseListener;
    }

    private void switchHelper() {
        JLabel temp = new JLabel();
        GroupLayout layout = (GroupLayout)getLayout();
        layout.replace(dummyLabel, temp);
        layout.replace(createNewButton, dummyLabel);
        layout.replace(temp, createNewButton);
    }

    static File saveToTempFile(NbJiraIssue.Attachment attachment) throws IOException {
        String filename = attachment.getFilename();
        int index = filename.lastIndexOf('.'); // NOI18N
        String prefix = (index == -1) ? filename : filename.substring(0, index);
        String suffix = (index == -1) ? null : filename.substring(index);
        if (prefix.length()<3) {
            prefix = prefix+"tmp"; // NOI18N
        }
        File file = File.createTempFile(prefix, suffix);
        FileOutputStream fos = new FileOutputStream(file);
        attachment.getAttachementData(fos);
        fos.close();
        return file;
    }

    public List<File> getNewAttachments() {
        List<File> files = new ArrayList<File>(newAttachments.size());
        for (JTextField field : newAttachments) {
            files.add(new File(field.getText()));
        }
        return files;
    }

    class CreateNewAction extends AbstractAction {
        private GroupLayout.ParallelGroup horizontalGroup;
        private GroupLayout.SequentialGroup verticalGroup;

        void setLayoutGroups(GroupLayout.ParallelGroup horizontalGroup,
                GroupLayout.SequentialGroup verticalGroup) {
            this.horizontalGroup = horizontalGroup;
            this.verticalGroup = verticalGroup;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JTextField nameField = new JTextField();
            nameField.setColumns(30);
            JButton browseButton = new JButton(NbBundle.getMessage(AttachmentsPanel.class, "AttachmentsPanel.browseButton.text")); // NOI18N
            browseButton.putClientProperty(NAME_FIELD_CP, nameField);
            browseButton.addActionListener(getBrowseListener());
            LinkButton deleteButton = new LinkButton(NbBundle.getMessage(AttachmentsPanel.class, "AttachmentsPanel.deleteButton.text")); // NOI18N
            deleteButton.putClientProperty(NAME_FIELD_CP, nameField);
            deleteButton.putClientProperty(BROWSE_CP, browseButton);
            deleteButton.addActionListener(getDeletedListener());
            GroupLayout layout = (GroupLayout)getLayout();
            GroupLayout.Group hGroup = layout.createSequentialGroup()
                .addComponent(nameField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(browseButton)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(deleteButton);
            GroupLayout.Group vGroup = layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(nameField)
                .addComponent(browseButton)
                .addComponent(deleteButton);
            horizontalGroup.addGroup(hGroup);
            verticalGroup.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED);
            verticalGroup.addGroup(vGroup);
            if (noneLabel.isVisible()) {
                noneLabel.setVisible(false);
                switchHelper();
                updateCreateNewButton(false);
            }
            newAttachments.add(nameField);
            UIUtils.keepFocusedComponentVisible(nameField, issuePanel);
            UIUtils.keepFocusedComponentVisible(browseButton, issuePanel);
            UIUtils.keepFocusedComponentVisible(deleteButton, issuePanel);
            revalidate();
        }
    }

    static class DefaultAttachmentAction extends AbstractAction {
        private NbJiraIssue.Attachment attachment;

        public DefaultAttachmentAction(NbJiraIssue.Attachment attachment) {
            this.attachment = attachment;
            putValue(Action.NAME, NbBundle.getMessage(DefaultAttachmentAction.class, "AttachmentsPanel.DefaultAttachmentAction.name")); // NOI18N
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            RequestProcessor.getDefault().post(new Runnable() {
                @Override
                public void run() {                 
                    final File[] file = new File[1];
                    boolean isPatch = false;
                    try {
                        file[0] = saveToTempFile(attachment);
                        isPatch = PatchUtils.isPatch(file[0]);
                    } catch (IOException ioex) {
                        Jira.LOG.log(Level.INFO, null, ioex);
                    }

                    if(file[0] == null) {
                        return;
                    }            
                    String fileName = attachment.getFilename();
                    if (isPatch && shouldApplyPatch(fileName)) {
                        PatchUtils.applyPatch(file[0]);
                    } else {
                        String progressFormat = NbBundle.getMessage(DefaultAttachmentAction.class, "AttachmentsPanel.DefaultAttachmentAction.progress"); // NOI18N
                        String progressMessage = MessageFormat.format(progressFormat, attachment.getFilename());
                        final ProgressHandle handle = ProgressHandleFactory.createHandle(progressMessage);
                        handle.start();
                        handle.switchToIndeterminate();
                       
                        try {    
                            file[0] = FileUtil.normalizeFile(file[0]);
                            String contentType = FileUtil.getMIMEType(FileUtil.toFileObject(file[0]));
                            if ((contentType == null) || ("content/unknown".equals(contentType))) { // NOI18N
                                contentType = FileTaskAttachmentSource.getContentTypeFromFilename(file[0].getName());
                            }
                            if ("image/png".equals(contentType) // NOI18N
                                || "image/gif".equals(contentType) // NOI18N
                                || "image/jpeg".equals(contentType)) { // NOI18N
                                    HtmlBrowser.URLDisplayer.getDefault().showURL(file[0].toURI().toURL());
                            } else {
                                FileObject fob = FileUtil.toFileObject(file[0]);
                                DataObject dob = DataObject.find(fob);
                                OpenCookie open = dob.getCookie(OpenCookie.class);
                                if (open != null) {
                                    open.open();
                                } else {
                                    // PENDING
                                }
                            }
                        } catch (DataObjectNotFoundException dnfex) {
                            dnfex.printStackTrace();
                        } catch (IOException ioex) {
                            ioex.printStackTrace();
                        } finally {
                            handle.finish();
                        }
                    }      
                }
            });                
        }

        private boolean shouldApplyPatch(String patchName) {
            ResourceBundle bundle = NbBundle.getBundle(AttachmentsPanel.class);
            JButton apply = new JButton(bundle.getString("AttachmentsPanel.DefaultAttachmentAction.apply")); // NOI18N
            apply.getAccessibleContext().setAccessibleDescription(apply.getText());
            JButton open = new JButton(bundle.getString("AttachmentsPanel.DefaultAttachmentAction.name")); // NOI18N
            open.getAccessibleContext().setAccessibleDescription(open.getText());
            String pattern = bundle.getString("AttachmentsPanel.DefaultAttachmentAction.patchRecognized"); // NOI18N
            String message = MessageFormat.format(pattern, patchName);
            JLabel label = new JLabel(message); // NOI18N
            LayoutStyle lStyle = LayoutStyle.getInstance();
            label.setBorder(BorderFactory.createEmptyBorder(
                lStyle.getContainerGap(label, SwingConstants.NORTH, null),
                lStyle.getContainerGap(label, SwingConstants.WEST, null), 0,
                lStyle.getContainerGap(label, SwingConstants.EAST, null)));
            DialogDescriptor dd = new DialogDescriptor(
                label,
                bundle.getString("AttachmentsPanel.DefaultAttachmentAction.applyPatch"), // NOI18N
                true,
                new Object[]{apply, open},
                apply,
                DialogDescriptor.DEFAULT_ALIGN,
                HelpCtx.DEFAULT_HELP,
                null);
            return DialogDisplayer.getDefault().notify(dd) == apply;
        }
    }

    static class SaveAttachmentAction extends AbstractAction {
        private NbJiraIssue.Attachment attachment;

        public SaveAttachmentAction(NbJiraIssue.Attachment attachment) {
            this.attachment = attachment;
            putValue(Action.NAME, NbBundle.getMessage(SaveAttachmentAction.class, "AttachmentsPanel.SaveAttachmentAction.name")); // NOI18N
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            final File file = new FileChooserBuilder(AttachmentsPanel.class)
                    .setFilesOnly(true).showSaveDialog();
            if (file != null) {
                String progressFormat = NbBundle.getMessage(SaveAttachmentAction.class, "AttachmentsPanel.SaveAttachmentAction.progress"); // NOI18N
                String progressMessage = MessageFormat.format(progressFormat, attachment.getFilename());
                final ProgressHandle handle = ProgressHandleFactory.createHandle(progressMessage);
                handle.start();
                handle.switchToIndeterminate();
                RequestProcessor.getDefault().post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            FileOutputStream fos = new FileOutputStream(file);
                            attachment.getAttachementData(fos);
                            fos.close();
                        } catch (IOException ioex) {
                            ioex.printStackTrace();
                        } finally {
                            handle.finish();
                        }
                    }
                });
            }
        }
    }

}
