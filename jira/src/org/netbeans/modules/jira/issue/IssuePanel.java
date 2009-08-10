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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import javax.swing.AbstractAction;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.JTextComponent;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.mylyn.internal.jira.core.JiraRepositoryConnector;
import org.eclipse.mylyn.internal.jira.core.model.IssueType;
import org.eclipse.mylyn.internal.jira.core.model.JiraStatus;
import org.eclipse.mylyn.internal.jira.core.model.Priority;
import org.eclipse.mylyn.internal.jira.core.model.Project;
import org.eclipse.mylyn.internal.jira.core.model.Resolution;
import org.eclipse.mylyn.internal.jira.core.model.Version;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.core.data.TaskOperation;
import org.jdesktop.layout.GroupLayout;
import org.jdesktop.layout.LayoutStyle;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.bugtracking.spi.Issue;
import org.netbeans.modules.bugtracking.util.BugtrackingUtil;
import org.netbeans.modules.bugtracking.util.LinkButton;
import org.netbeans.modules.jira.Jira;
import org.netbeans.modules.jira.repository.JiraConfiguration;
import org.netbeans.modules.jira.util.JiraUtils;
import org.netbeans.modules.jira.util.PriorityRenderer;
import org.netbeans.modules.jira.util.ProjectRenderer;
import org.netbeans.modules.jira.util.ResolutionRenderer;
import org.netbeans.modules.jira.util.StatusRenderer;
import org.netbeans.modules.jira.util.TypeRenderer;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Jan Stola
 */
public class IssuePanel extends javax.swing.JPanel {
    private static final Color ORIGINAL_ESTIMATE_COLOR = new Color(137, 175, 215);
    private static final Color REMAINING_ESTIMATE_COLOR = new Color(236, 142, 0);
    private static final Color TIME_SPENT_COLOR = new Color(81, 168, 37);
    private static final Color HIGHLIGHT_COLOR = new Color(217, 255, 217);
    private NbJiraIssue issue;
    private CommentsPanel commentsPanel;
    private AttachmentsPanel attachmentsPanel;
    private boolean skipReload;
    private boolean reloading;
    private Map<NbJiraIssue.IssueField,Object> initialValues = new HashMap<NbJiraIssue.IssueField,Object>();

    public IssuePanel() {
        initComponents();
        createdField.setBackground(getBackground());
        updatedField.setBackground(getBackground());
        originalEstimateField.setBackground(getBackground());
        remainingEstimateField.setBackground(getBackground());
        timeSpentField.setBackground(getBackground());
        resolutionField.setBackground(getBackground());
        projectField.setBackground(getBackground());
        statusField.setBackground(getBackground());
        customFieldPanelLeft.setBackground(getBackground());
        customFieldPanelRight.setBackground(getBackground());
        parentHeaderPanel.setBackground(getBackground());
        BugtrackingUtil.fixFocusTraversalKeys(environmentArea);
        BugtrackingUtil.fixFocusTraversalKeys(addCommentArea);
        BugtrackingUtil.issue163946Hack(componentScrollPane);
        BugtrackingUtil.issue163946Hack(affectsVersionScrollPane);
        BugtrackingUtil.issue163946Hack(fixVersionScrollPane);
        BugtrackingUtil.issue163946Hack(environmentScrollPane);
        BugtrackingUtil.issue163946Hack(addCommentScrollPane);
        summaryField.setPreferredSize(summaryField.getMinimumSize());
        initAttachmentsPanel();
        attachFieldStatusListeners();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        if (issue != null) {
            issue.opened();
        }
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        if(issue != null) {
            issue.closed();
        }
    }

    void setIssue(NbJiraIssue issue) {
        if (this.issue == null) {
            attachIssueListener(issue);
        }
        this.issue = issue;
        initRenderers();
        initProjectCombo();
        initPriorityCombo();
        initResolutionCombo();
        initHeaderLabel();
        initCommentsPanel();

        reloadForm(true);
    }

    private void initAttachmentsPanel() {
        attachmentsPanel = new AttachmentsPanel();
        GroupLayout layout = (GroupLayout)getLayout();
        layout.replace(dummyAttachmentPanel, attachmentsPanel);
        attachmentLabel.setLabelFor(attachmentsPanel);
    }

    private void initRenderers() {
        // Project combo
        projectCombo.setRenderer(new ProjectRenderer());

        // Issue type combo
        issueTypeCombo.setRenderer(new TypeRenderer());

        // Priority combo
        priorityCombo.setRenderer(new PriorityRenderer());

        // Component list
        componentList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                if (value instanceof org.eclipse.mylyn.internal.jira.core.model.Component) {
                    value = ((org.eclipse.mylyn.internal.jira.core.model.Component)value).getName();
                }
                return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            }
        });

        // Status combo
        statusCombo.setRenderer(new StatusRenderer());

        // Resolution combo
        resolutionCombo.setRenderer(new ResolutionRenderer());
    }

    private void initProjectCombo() {
        Project[] projects = issue.getRepository().getConfiguration().getProjects();
        DefaultComboBoxModel model = new DefaultComboBoxModel(projects);
        projectCombo.setModel(model);
    }

    private void initPriorityCombo() {
        Priority[] priority = issue.getRepository().getConfiguration().getPriorities();
        DefaultComboBoxModel model = new DefaultComboBoxModel(priority);
        priorityCombo.setModel(model);
    }

    private void initStatusCombo(JiraStatus status) {
        List<JiraStatus> statusList = allowedStatusTransitions(status);
        JiraStatus[] statuses = statusList.toArray(new JiraStatus[statusList.size()]);
        DefaultComboBoxModel model = new DefaultComboBoxModel(statuses);
        statusCombo.setModel(model);
    }

    private void initResolutionCombo() {
        Resolution[] resolution = issue.getRepository().getConfiguration().getResolutions();
        DefaultComboBoxModel model = new DefaultComboBoxModel(resolution);
        resolutionCombo.setModel(model);
    }

    private void initHeaderLabel() {
        Font font = headerLabel.getFont();
        headerLabel.setFont(font.deriveFont((float)(font.getSize()*1.7)));
    }

    private void initCommentsPanel() {
        commentsPanel = new CommentsPanel();
        commentsPanel.setNewCommentHandler(new CommentsPanel.NewCommentHandler() {
            public void append(String text) {
                addCommentArea.append(text);
                addCommentArea.requestFocus();
                scrollRectToVisible(addCommentScrollPane.getBounds());
            }
        });
        GroupLayout layout = (GroupLayout)getLayout();
        layout.replace(dummyCommentPanel, commentsPanel);
    }

    private Map<String,JLabel> customFieldLabels = new HashMap<String,JLabel>();
    private Map<String,JComponent> customFieldComponents = new HashMap<String,JComponent>();
    private void initCustomFields() {
        customFieldPanelLeft.removeAll();
        customFieldPanelRight.removeAll();
        List<NbJiraIssue.CustomField> supportedFields = getSupportedCustomFields();
        customFieldPanelLeft.setVisible(!supportedFields.isEmpty());
        customFieldPanelRight.setVisible(!supportedFields.isEmpty());
        if (!supportedFields.isEmpty()) {
            GroupLayout labelLayout = new GroupLayout(customFieldPanelLeft);
            customFieldPanelLeft.setLayout(labelLayout);
            GroupLayout fieldLayout = new GroupLayout(customFieldPanelRight);
            customFieldPanelRight.setLayout(fieldLayout);
            GroupLayout.ParallelGroup labelHorizontalGroup = labelLayout.createParallelGroup(GroupLayout.LEADING);
            GroupLayout.SequentialGroup labelVerticalGroup = labelLayout.createSequentialGroup();
            GroupLayout.ParallelGroup fieldHorizontalGroup = fieldLayout.createParallelGroup(GroupLayout.LEADING);
            GroupLayout.SequentialGroup fieldVerticalGroup = fieldLayout.createSequentialGroup();
            boolean first = true;
            for (NbJiraIssue.CustomField cField : supportedFields) {
                JLabel label = new JLabel(cField.getLabel());
                JTextField field = new JTextField();
                customFieldLabels.put(cField.getId(), label);
                customFieldComponents.put(cField.getId(), field);
                label.setLabelFor(field);
                label.setPreferredSize(new Dimension(label.getPreferredSize().width, field.getPreferredSize().height));
                if (!first) {
                    labelVerticalGroup.addPreferredGap(LayoutStyle.RELATED);
                    fieldVerticalGroup.addPreferredGap(LayoutStyle.RELATED);
                }
                labelHorizontalGroup.add(label);
                labelVerticalGroup.add(label, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE);
                fieldHorizontalGroup.add(field);
                fieldVerticalGroup.add(field);
                first = false;
            }
            labelLayout.setHorizontalGroup(labelHorizontalGroup);
            labelLayout.setVerticalGroup(labelVerticalGroup);
            fieldLayout.setHorizontalGroup(fieldHorizontalGroup);
            fieldLayout.setVerticalGroup(fieldVerticalGroup);
        }
    }

    private List<NbJiraIssue.CustomField> getSupportedCustomFields() {
        NbJiraIssue.CustomField[] cFields = issue.getCustomFields();
        List<NbJiraIssue.CustomField> supportedFields = new LinkedList<NbJiraIssue.CustomField>();
        for (NbJiraIssue.CustomField cField : cFields) {
            if (isSupportedCustomField(cField)) {
                supportedFields.add(cField);
            }
        }
        return supportedFields;
    }

    private void attachFieldStatusListeners() {
        issueTypeCombo.addActionListener(new CancelHighlightListener(issueTypeLabel));
        statusCombo.addActionListener(new CancelHighlightListener(statusLabel));
        statusCombo.addActionListener(new CancelHighlightListener(resolutionLabel));
        priorityCombo.addActionListener(new CancelHighlightListener(priorityLabel));
        dueField.getDocument().addDocumentListener(new CancelHighlightListener(dueLabel));
        assigneeField.getDocument().addDocumentListener(new CancelHighlightListener(assigneeLabel));
        summaryField.getDocument().addDocumentListener(new CancelHighlightListener(summaryLabel));
        environmentArea.getDocument().addDocumentListener(new CancelHighlightListener(environmentLabel));
        componentList.addListSelectionListener(new CancelHighlightListener(componentLabel));
        affectsVersionList.addListSelectionListener(new CancelHighlightListener(affectsVersionLabel));
        fixVersionList.addListSelectionListener(new CancelHighlightListener(fixVersionLabel));
    }

    private void reloadForm(boolean force) {
        if (skipReload) {
            return;
        }
        reloading = true;

        boolean isNew = issue.getTaskData().isNew();
        headerLabel.setVisible(!isNew);
        createdLabel.setVisible(!isNew);
        createdField.setVisible(!isNew);
        updatedLabel.setVisible(!isNew);
        updatedField.setVisible(!isNew);
        separator.setVisible(!isNew);
        commentsPanel.setVisible(!isNew);
        attachmentLabel.setVisible(!isNew);
        attachmentsPanel.setVisible(!isNew);
        resolutionCombo.setVisible(!isNew);
        resolutionField.setVisible(!isNew);
        originalEstimateLabel.setVisible(!isNew);
        originalEstimateField.setVisible(!isNew);
        originalEstimatePanel.setVisible(!isNew);
        remainingEstimateLabel.setVisible(!isNew);
        remainingEstimateField.setVisible(!isNew);
        remainingEstimatePanel.setVisible(!isNew);
        timeSpentLabel.setVisible(!isNew);
        timeSpentField.setVisible(!isNew);
        timeSpentPanel.setVisible(!isNew);
        originalEstimateLabelNew.setVisible(isNew);
        originalEstimateFieldNew.setVisible(isNew);
        originalEstimateHint.setVisible(isNew);
        logWorkButton2.setVisible(!isNew);

        createSubtaskButton.setVisible(false);
        convertToSubtaskButton.setVisible(false);
        dummySubtaskPanel.setVisible(false);

        if (force) {
            initCustomFields();
        }

        // Operations
        boolean startProgressAvailable = false;
        boolean stopProgressAvailable = false;
        boolean resolveIssueAvailable = false;
        boolean closeIssueAvailable = false;
        boolean reopenIssueAvailable = false;
        for (TaskOperation operation : issue.getAvailableOperations().values()) {
            String label = operation.getLabel();
            if (JiraUtils.isStartProgressOperation(label)) {
                startProgressAvailable = true;
            } else if (JiraUtils.isStopProgressOperation(label)) {
                stopProgressAvailable = true;
            } else if (JiraUtils.isResolveOperation(label)) {
                resolveIssueAvailable = true;
            } else if (JiraUtils.isCloseOperation(label)) {
                closeIssueAvailable = true;
            } else if (JiraUtils.isReopenOperation(label)) {
                reopenIssueAvailable = true;
            }
        }
        startProgressButton.setVisible(startProgressAvailable);
        stopProgressButton.setVisible(stopProgressAvailable);
        resolveIssueButton.setVisible(resolveIssueAvailable);
        closeIssueButton.setVisible(closeIssueAvailable);
        reopenIssueButton.setVisible(reopenIssueAvailable);

        org.openide.awt.Mnemonics.setLocalizedText(addCommentLabel, NbBundle.getMessage(IssuePanel.class, isNew ? "IssuePanel.description" : "IssuePanel.addCommentLabel.text")); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(submitButton, NbBundle.getMessage(IssuePanel.class, isNew ? "IssuePanel.submitButton.text.new" : "IssuePanel.submitButton.text")); // NOI18N
        if (isNew != (projectCombo.getParent() != null)) {
            GroupLayout layout = (GroupLayout)getLayout();
            layout.replace(isNew ? projectField : projectCombo, isNew ? projectCombo : projectField);
        }
        if (isNew != (statusField.getParent() != null)) {
            GroupLayout layout = (GroupLayout)getLayout();
            layout.replace(isNew ? statusCombo : statusField, isNew ? statusField : statusCombo);
        }
        if (isNew != (actionPanel.getParent() == null)) {
            GroupLayout layout = (GroupLayout)getLayout();
            layout.replace(isNew ? actionPanel : dummyActionPanel, isNew ? dummyActionPanel : actionPanel);
        }
        if (isNew != (cancelButton.getParent() == null)) {
            GroupLayout layout = (GroupLayout)getLayout();
            layout.replace(isNew ? cancelButton : dummyCancelButton, isNew ? dummyCancelButton : cancelButton);
        }

        reloadCustomFields();

        final String parentKey = issue.getParentKey();
        boolean hasParent = (parentKey != null) && (parentKey.trim().length() > 0);
        if  (hasParent) {
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    Issue parentIssue = issue.getRepository().getIssueCache().getIssue(parentKey);
                    if (parentIssue == null) {
                        parentIssue = issue.getRepository().getIssue(parentKey);
                    }
                    final Issue parent = parentIssue;
                    EventQueue.invokeLater(new Runnable() {
                        public void run() {
                            parentHeaderPanel.setVisible(true);
                            headerLabel.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/jira/resources/subtask.png", true)); // NOI18N
                            GroupLayout layout = new GroupLayout(parentHeaderPanel);
                            JLabel parentLabel = new JLabel();
                            parentLabel.setText(parent.getSummary());
                            LinkButton parentButton = new LinkButton(new AbstractAction() {
                                public void actionPerformed(ActionEvent e) {
                                    parent.open();
                                }
                            });
                            parentButton.setText(parentKey+':');
                            layout.setHorizontalGroup(
                                layout.createSequentialGroup()
                                    .add(parentButton)
                                    .addPreferredGap(LayoutStyle.RELATED)
                                    .add(parentLabel)
                            );
                            layout.setVerticalGroup(
                                layout.createParallelGroup(GroupLayout.BASELINE)
                                    .add(parentButton)
                                    .add(parentLabel)
                            );
                            parentHeaderPanel.setLayout(layout);
                        }
                    });
                }
            });
        } else {
            parentHeaderPanel.setVisible(false);
            headerLabel.setIcon(null);
        }

        JiraConfiguration config = issue.getRepository().getConfiguration();
        if (isNew) {
            String projectId = issue.getFieldValue(NbJiraIssue.IssueField.PROJECT);
            if ((projectId != null) && !projectId.equals("")) { // NOI18N
                Project project = config.getProjectById(projectId);
                if (!project.equals(projectCombo.getSelectedItem())) {
                    projectCombo.setSelectedItem(project);
                }
            } else {
                projectCombo.setSelectedIndex(0); // Preselect the project
            }
            reloadField(issueTypeCombo, config.getIssueTypeById(issue.getFieldValue(NbJiraIssue.IssueField.TYPE)), NbJiraIssue.IssueField.TYPE);
            reloadField(priorityCombo, config.getPriorityById(issue.getFieldValue(NbJiraIssue.IssueField.PRIORITY)), NbJiraIssue.IssueField.PRIORITY);
            statusField.setText(STATUS_OPEN);
            fixPrefSize(statusField);
        } else {
            ResourceBundle bundle = NbBundle.getBundle(IssuePanel.class);
            // Header label
            String headerFormat = bundle.getString("IssuePanel.headerLabel.format"); // NOI18N
            String headerTxt = MessageFormat.format(headerFormat, issue.getKey(), issue.getSummary());
            headerLabel.setText(headerTxt);
            Dimension dim = headerLabel.getPreferredSize();
            headerLabel.setMinimumSize(new Dimension(0, dim.height));
            headerLabel.setPreferredSize(new Dimension(0, dim.height));
            // Created field
            String createdFormat = bundle.getString("IssuePanel.createdField.format"); // NOI18N
            String reporter = config.getUser(issue.getFieldValue(NbJiraIssue.IssueField.REPORTER)).getFullName();
            String creation = dateByMillis(issue.getFieldValue(NbJiraIssue.IssueField.CREATION), true);
            String createdTxt = MessageFormat.format(createdFormat, creation, reporter);
            createdField.setText(createdTxt);
            fixPrefSize(createdField);
            String projectId = issue.getFieldValue(NbJiraIssue.IssueField.PROJECT);
            Project project = config.getProjectById(projectId);
            reloadField(projectCombo, project, NbJiraIssue.IssueField.PROJECT);
            reloadField(projectField, project.getName(), NbJiraIssue.IssueField.PROJECT);
            reloadField(issueTypeCombo, config.getIssueTypeById(issue.getFieldValue(NbJiraIssue.IssueField.TYPE)), NbJiraIssue.IssueField.TYPE);
            initStatusCombo(config.getStatusById(issue.getFieldValue(NbJiraIssue.IssueField.STATUS)));
            reloadField(summaryField, issue.getFieldValue(NbJiraIssue.IssueField.SUMMARY), NbJiraIssue.IssueField.SUMMARY);
            reloadField(priorityCombo, config.getPriorityById(issue.getFieldValue(NbJiraIssue.IssueField.PRIORITY)), NbJiraIssue.IssueField.PRIORITY);
            List<String> componentIds = issue.getFieldValues(NbJiraIssue.IssueField.COMPONENT);
            reloadField(componentList, componentsByIds(projectId, componentIds), NbJiraIssue.IssueField.COMPONENT);
            List<String> affectsVersionIds = issue.getFieldValues(NbJiraIssue.IssueField.AFFECTSVERSIONS);
            reloadField(affectsVersionList, versionsByIds(projectId, affectsVersionIds),NbJiraIssue.IssueField.AFFECTSVERSIONS);
            List<String> fixVersionIds = issue.getFieldValues(NbJiraIssue.IssueField.FIXVERSIONS);
            reloadField(fixVersionList, versionsByIds(projectId, fixVersionIds), NbJiraIssue.IssueField.FIXVERSIONS);
            Resolution resolution = config.getResolutionById(issue.getFieldValue(NbJiraIssue.IssueField.RESOLUTION));
            reloadField(resolutionField, (resolution==null) ? "" : resolution.getName(), NbJiraIssue.IssueField.RESOLUTION); // NOI18N
            fixPrefSize(resolutionField);
            reloadField(statusCombo, config.getStatusById(issue.getFieldValue(NbJiraIssue.IssueField.STATUS)), NbJiraIssue.IssueField.STATUS);
            reloadField(assigneeField, issue.getFieldValue(NbJiraIssue.IssueField.ASSIGNEE), NbJiraIssue.IssueField.ASSIGNEE);
            reloadField(environmentArea, issue.getFieldValue(NbJiraIssue.IssueField.ENVIRONMENT), NbJiraIssue.IssueField.ENVIRONMENT);
            reloadField(updatedField, dateByMillis(issue.getFieldValue(NbJiraIssue.IssueField.MODIFICATION), true), NbJiraIssue.IssueField.MODIFICATION);
            fixPrefSize(updatedField);
            reloadField(dueField, dateByMillis(issue.getFieldValue(NbJiraIssue.IssueField.DUE)), NbJiraIssue.IssueField.DUE);

            // Work-log
            String originalEstimateTxt = issue.getFieldValue(NbJiraIssue.IssueField.INITIAL_ESTIMATE);
            int originalEstimate = toInt(originalEstimateTxt);
            int remainintEstimate = toInt(issue.getFieldValue(NbJiraIssue.IssueField.ESTIMATE));
            int timeSpent = toInt(issue.getFieldValue(NbJiraIssue.IssueField.ACTUAL));
            if ((originalEstimateTxt.length() == 0) && (remainintEstimate + timeSpent > 0)) {
                // originalEstimate is sometimes empty incorrectly
                originalEstimate = remainintEstimate + timeSpent;
            }
            int daysPerWeek = issue.getRepository().getConfiguration().getWorkDaysPerWeek();
            int hoursPerDay = issue.getRepository().getConfiguration().getWorkHoursPerDay();
            reloadField(originalEstimateField, JiraUtils.getWorkLogText(originalEstimate, daysPerWeek, hoursPerDay, false), NbJiraIssue.IssueField.INITIAL_ESTIMATE);
            reloadField(remainingEstimateField, JiraUtils.getWorkLogText(remainintEstimate, daysPerWeek, hoursPerDay, true), NbJiraIssue.IssueField.ESTIMATE);
            reloadField(timeSpentField, JiraUtils.getWorkLogText(timeSpent, daysPerWeek, hoursPerDay, false), NbJiraIssue.IssueField.ACTUAL);
            fixPrefSize(originalEstimateField);
            fixPrefSize(remainingEstimateField);
            fixPrefSize(timeSpentField);
            int scale = Math.max(originalEstimate, timeSpent+remainintEstimate);
            Color bgColor = UIManager.getDefaults().getColor("EditorPane.background"); // NOI18N
            setupWorkLogPanel(originalEstimatePanel, ORIGINAL_ESTIMATE_COLOR, Color.lightGray, Color.lightGray, originalEstimate, scale-originalEstimate, 0);
            setupWorkLogPanel(remainingEstimatePanel, Color.lightGray, REMAINING_ESTIMATE_COLOR, bgColor, timeSpent, remainintEstimate, scale-timeSpent-remainintEstimate);
            setupWorkLogPanel(timeSpentPanel, TIME_SPENT_COLOR, Color.lightGray, bgColor, timeSpent, remainintEstimate, scale-timeSpent-remainintEstimate);

            // Comments
            commentsPanel.setIssue(issue);
            BugtrackingUtil.keepFocusedComponentVisible(commentsPanel);
            if (force) {
                addCommentArea.setText(""); // NOI18N
            }

            // Attachments
            attachmentsPanel.setIssue(issue);
            BugtrackingUtil.keepFocusedComponentVisible(attachmentsPanel);

            // Sub-tasks
            boolean hasSubtasks = issue.hasSubtasks();
            subtaskLabel.setVisible(hasSubtasks);
            if (subTaskScrollPane != null) {
                subTaskScrollPane.setVisible(hasSubtasks);
            }
            if (hasSubtasks) {
                if (subTaskTable == null) {
                    subTaskTable = new JTable();
                    subTaskTable.setDefaultRenderer(JiraStatus.class, new StatusRenderer());
                    subTaskTable.setDefaultRenderer(Priority.class, new PriorityRenderer());
                    subTaskScrollPane = new JScrollPane(subTaskTable);
                }
                RequestProcessor.getDefault().post(new Runnable() {
                    public void run() {
                        final SubtaskTableModel tableModel = new SubtaskTableModel(issue);
                        EventQueue.invokeLater(new Runnable() {
                            public void run() {
                                subTaskTable.setModel(tableModel);

                                // Table height tweaks
                                int height = 0;
                                for(int row=0; row<tableModel.getRowCount(); row++) {
                                    height += subTaskTable.getRowHeight(row);
                                }
                                subTaskTable.setPreferredScrollableViewportSize(new Dimension(
                                        subTaskTable.getPreferredScrollableViewportSize().width,
                                        height
                                ));

                                if (subTaskScrollPane.getParent() == null) {
                                    ((GroupLayout)getLayout()).replace(dummySubtaskPanel, subTaskScrollPane);
                                }
                            }
                        });
                    }
                });
            }
        }
        updateFieldStatuses();
        reloading = false;
    }
    private JComponent dummyCancelButton = new JLabel();
    private JComponent dummyActionPanel = new JLabel();
    private JTable subTaskTable;
    private JScrollPane subTaskScrollPane;

    private void reloadCustomFields() {
        for (NbJiraIssue.CustomField cField : getSupportedCustomFields()) {
            String type = cField.getType();
            if ("com.atlassian.jira.plugin.labels:labels".equals(type)) { // NOI18N
                JTextField field = (JTextField)customFieldComponents.get(cField.getId());
                if (field != null) {
                    field.setText(cField.getValues().get(0));
                }
            }
        }
    }

    private boolean isSupportedCustomField(NbJiraIssue.CustomField field) {
        return "com.atlassian.jira.plugin.labels:labels".equals(field.getType()); // NOI18N
    }

    private void reloadField(JComponent fieldComponent, Object fieldValue, NbJiraIssue.IssueField field) {
        if (fieldComponent instanceof JComboBox) {
            ((JComboBox)fieldComponent).setSelectedItem(fieldValue);
        } else if (fieldComponent instanceof JFormattedTextField) {
            ((JFormattedTextField)fieldComponent).setValue(fieldValue);
        } else if (fieldComponent instanceof JTextComponent) {
            ((JTextComponent)fieldComponent).setText(fieldValue.toString());
        } else if (fieldComponent instanceof JList) {
            JList list = (JList)fieldComponent;
            list.clearSelection();
            ListModel model = list.getModel();
            for (Object value : (List)fieldValue) {
                for (int i=0; i<model.getSize(); i++) {
                    if (model.getElementAt(i).equals(value)) {
                        list.getSelectionModel().addSelectionInterval(i, i);
                    }
                }
            }
        }
        initialValues.put(field, fieldValue);
    }

    private void storeFieldValue(NbJiraIssue.IssueField field, JComboBox combo) {
        Object value = combo.getSelectedItem();
        if (value != null) {
            String key;
            switch (field) {
                case PROJECT: key = ((Project)value).getId(); break;
                case TYPE: key = ((IssueType)value).getId(); break;
                case STATUS: key = ((JiraStatus)value).getId(); break;
                case RESOLUTION: key = ((Resolution)value).getId(); break;
                case PRIORITY: key = ((Priority)value).getId(); break;
                default: throw new UnsupportedOperationException();
            }
            storeFieldValue(field, key);
        }
    }

    private void storeFieldValue(NbJiraIssue.IssueField field, JList combo) {
        Object[] values = combo.getSelectedValues();
        List<String> keys = new ArrayList<String>(values.length);
        for (int i=0; i<values.length; i++) {
            switch (field) {
                case COMPONENT: keys.add(((org.eclipse.mylyn.internal.jira.core.model.Component)values[i]).getId()); break;
                case AFFECTSVERSIONS: keys.add(((Version)values[i]).getId()); break;
                case FIXVERSIONS: keys.add(((Version)values[i]).getId()); break;
                default: throw new UnsupportedOperationException();
            }
        }
        storeFieldValue(field, keys);
    }

    private void storeFieldValue(NbJiraIssue.IssueField field, JFormattedTextField formattedField) {
        Object value = formattedField.getValue();
        storeFieldValue(field, (value == null) ? "" : ((Date)value).getTime()+""); // NOI18N
    }

    private void storeFieldValue(NbJiraIssue.IssueField field, JTextComponent textComponent) {
        storeFieldValue(field, textComponent.getText());
    }

    private void storeFieldValue(NbJiraIssue.IssueField field, String value) {
        if (issue.getTaskData().isNew() || !value.equals(initialValues.get(field))) {
            issue.setFieldValue(field, value);
        }
    }

    private void storeFieldValue(NbJiraIssue.IssueField field, List<String> values) {
        Object initValue = initialValues.get(field);
        boolean identical = false;
        if (initValue instanceof List) {
            List<?> initValues = (List<?>)initValue;
            identical = values.containsAll(initValues) && initValues.containsAll(values);
        }
        if (issue.getTaskData().isNew() || !identical) {
            issue.setFieldValues(field, values);
        }
    }

    private void storeCustomFieldValue(NbJiraIssue.CustomField cField) {
        String type = cField.getType();
        if ("com.atlassian.jira.plugin.labels:labels".equals(type)) { // NOI18N
            JTextField field = (JTextField)customFieldComponents.get(cField.getId());
            if (field != null) {
                List<String> values = Collections.singletonList(field.getText());
                cField.setValues(values);
                issue.setCustomField(cField);
            }
        }
    }

    private void storeStatusAndResolution() {
        Object statusValue = initialValues.get(NbJiraIssue.IssueField.STATUS);
        Object selectedValue = statusCombo.getSelectedItem();
        if (!(statusValue instanceof JiraStatus) || (!(selectedValue instanceof JiraStatus))) {
            return; // should not happen
        }
        JiraStatus initialStatus = (JiraStatus)statusValue;
        JiraStatus selectedStatus = (JiraStatus)selectedValue;
        if (initialStatus.equals(selectedStatus)) {
            return; // no change
        }
        String statusName = selectedStatus.getName();
        if (statusName.equals(STATUS_OPEN)) {
            issue.stopProgress();
        } else if (statusName.equals(STATUS_IN_PROGRESS)) {
            issue.startProgress();
        } else if (statusName.equals(STATUS_REOPENED)) {
            issue.reopen(null);
        } else if (statusName.equals(STATUS_RESOLVED)) {
            Resolution resolution = (Resolution)resolutionCombo.getSelectedItem();
            issue.resolve(resolution, null);
        } else if (statusName.equals(STATUS_CLOSED)) {
            Resolution resolution = (Resolution)resolutionCombo.getSelectedItem();
            issue.close(resolution, null);
        }
    }

    private void setupWorkLogPanel(JPanel panel, Color color1,  Color color2, Color color3, int val1, int val2, int val3) {
        panel.setLayout(new GridBagLayout());

        JLabel label1 = new JLabel();
        label1.setOpaque(true);
        label1.setBackground(color1);
        label1.setPreferredSize(new Dimension(0,10));
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = val1;
        panel.add(label1, c);

        JLabel label2 = new JLabel();
        label2.setOpaque(true);
        label2.setBackground(color2);
        label2.setPreferredSize(new Dimension(0,10));
        c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = val2;
        panel.add(label2, c);

        JLabel label3 = new JLabel();
        label3.setOpaque(true);
        label3.setBackground(color3);
        label3.setPreferredSize(new Dimension(0,10));
        c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = val3;
        panel.add(label3, c);
    }

    private List<org.eclipse.mylyn.internal.jira.core.model.Component> componentsByIds(String projectId, List<String> componentIds) {
        JiraConfiguration config = issue.getRepository().getConfiguration();
        List<org.eclipse.mylyn.internal.jira.core.model.Component> components = new ArrayList<org.eclipse.mylyn.internal.jira.core.model.Component>(componentIds.size());
        for (String id : componentIds) {
            components.add(config.getComponentById(projectId, id));
        }
        return components;
    }

    private List<Version> versionsByIds(String projectId, List<String> versionIds) {
        JiraConfiguration config = issue.getRepository().getConfiguration();
        List<Version> versions = new ArrayList<Version>(versionIds.size());
        for (String id : versionIds) {
            versions.add(config.getVersionById(projectId, id));
        }
        return versions;
    }

    private String dateByMillis(String text, boolean includeTime) {
        if (text.trim().length() > 0) {
            try {
                long millis = Long.parseLong(text);
                DateFormat format = includeTime ? DateFormat.getDateTimeInstance() : DateFormat.getDateInstance();
                return format.format(new Date(millis));
            } catch (NumberFormatException nfex) {
                nfex.printStackTrace();
            }
        }
        return ""; // NOI18N
    }

    private Date dateByMillis(String text) {
        if (text.trim().length() > 0) {
            try {
                long millis = Long.parseLong(text);
                return new Date(millis);
            } catch (NumberFormatException nfex) {
                nfex.printStackTrace();
            }
        }
        return null;
    }

    private int toInt(String text) {
        if (text.trim().length() > 0) {
            try {
                return Integer.parseInt(text);
            } catch (NumberFormatException nfex) {
                nfex.printStackTrace();
            }
        }
        return 0;
    }

    void reloadFormInAWT(final boolean force) {
        if (EventQueue.isDispatchThread()) {
            reloadForm(force);
        } else {
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    reloadForm(force);
                }
            });
        }
    }

    private void attachIssueListener(final NbJiraIssue issue) {
        issue.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getSource() != issue) {
                    return;
                }
                if (Issue.EVENT_ISSUE_DATA_CHANGED.equals(evt.getPropertyName())) {
                    reloadFormInAWT(false);
                } else if (Issue.EVENT_ISSUE_SEEN_CHANGED.equals(evt.getPropertyName())) {
                    updateFieldStatuses();
                }
            }
        });
    }

    private static void fixPrefSize(JTextField textField) {
        // The preferred size of JTextField on (Classic) Windows look and feel
        // is one pixel shorter. The following code is a workaround.
        textField.setPreferredSize(null);
        Dimension dim = textField.getPreferredSize();
        Dimension fixedDim = new Dimension(dim.width+1, dim.height);
        textField.setPreferredSize(fixedDim);
    }

    private void updateFieldStatuses() {
        updateFieldStatus(NbJiraIssue.IssueField.PROJECT, projectLabel);
        updateFieldStatus(NbJiraIssue.IssueField.TYPE, issueTypeLabel);
        updateFieldStatus(NbJiraIssue.IssueField.STATUS, statusLabel);
        updateFieldStatus(NbJiraIssue.IssueField.RESOLUTION, resolutionLabel);
        updateFieldStatus(NbJiraIssue.IssueField.PRIORITY, priorityLabel);
        updateFieldStatus(NbJiraIssue.IssueField.DUE, dueLabel);
        updateFieldStatus(NbJiraIssue.IssueField.ASSIGNEE, assigneeLabel);
        updateFieldStatus(NbJiraIssue.IssueField.COMPONENT, componentLabel);
        updateFieldStatus(NbJiraIssue.IssueField.AFFECTSVERSIONS, affectsVersionLabel);
        updateFieldStatus(NbJiraIssue.IssueField.FIXVERSIONS, fixVersionLabel);
        updateFieldStatus(NbJiraIssue.IssueField.INITIAL_ESTIMATE, originalEstimateLabel);
        updateFieldStatus(NbJiraIssue.IssueField.ESTIMATE, remainingEstimateLabel);
        updateFieldStatus(NbJiraIssue.IssueField.ACTUAL, timeSpentLabel);
        updateFieldStatus(NbJiraIssue.IssueField.SUMMARY, summaryLabel);
        updateFieldStatus(NbJiraIssue.IssueField.ENVIRONMENT, environmentLabel);
    }

    private void updateFieldStatus(NbJiraIssue.IssueField field, JLabel label) {
        boolean highlight = false;
        if (!issue.getTaskData().isNew()) {
            int status = issue.getFieldStatus(field);
            highlight = (status == NbJiraIssue.FIELD_STATUS_NEW) || (status == NbJiraIssue.FIELD_STATUS_MODIFIED);
        }
        label.setOpaque(highlight);
        if (highlight) {
            label.setBackground(HIGHLIGHT_COLOR);
        }
        label.repaint();
    }

    // This is ugly, but I don't see a better way if we want to have a status combo
    private static final String STATUS_OPEN = "Open"; // NOI18N
    private static final String STATUS_IN_PROGRESS = "In Progress"; // NOI18N
    private static final String STATUS_REOPENED = "Reopened"; // NOI18N
    private static final String STATUS_RESOLVED = "Resolved"; // NOI18N
    private static final String STATUS_CLOSED = "Closed"; // NOI18N
    private List<JiraStatus> allowedStatusTransitions(JiraStatus status) {
        String statusName = status.getName();
        List<String> allowedNames = new ArrayList<String>(3);
        allowedNames.add(statusName);
        if (STATUS_OPEN.equals(statusName) || STATUS_REOPENED.equals(statusName)) {
            allowedNames.add(STATUS_IN_PROGRESS);
            allowedNames.add(STATUS_RESOLVED);
            allowedNames.add(STATUS_CLOSED);
        } else if (STATUS_IN_PROGRESS.equals(statusName)) {
            allowedNames.add(STATUS_OPEN);
            allowedNames.add(STATUS_RESOLVED);
            allowedNames.add(STATUS_CLOSED);
        } else if (STATUS_RESOLVED.equals(statusName)) {
            allowedNames.add(STATUS_REOPENED);
            allowedNames.add(STATUS_CLOSED);
        } else if (STATUS_CLOSED.equals(statusName)) {
            allowedNames.add(STATUS_REOPENED);
        }
        List<JiraStatus> allowedStatuses = new ArrayList<JiraStatus>(allowedNames.size());
        for (JiraStatus s : issue.getRepository().getConfiguration().getStatuses()) {
            if (allowedNames.contains(s.getName())) {
                allowedStatuses.add(s);
            }
        }
        return allowedStatuses;
    }

    private void submitChange(final Runnable change, String progressMessage)  {
        final ProgressHandle handle = ProgressHandleFactory.createHandle(progressMessage);
        handle.start();
        handle.switchToIndeterminate();
        skipReload = true;
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                try {
                    change.run();
                } finally {
                    EventQueue.invokeLater(new Runnable() {
                        public void run() {
                            skipReload = false;
                        }
                    });
                    handle.finish();
                    reloadFormInAWT(true);
                }
            }
        });
    }

    private void cancelHighlight(JLabel label) {
        if (!reloading) {
            label.setOpaque(false);
            label.getParent().repaint();
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        resolutionField = new javax.swing.JTextField();
        projectField = new javax.swing.JTextField();
        statusField = new javax.swing.JTextField();
        parentHeaderPanel = new javax.swing.JPanel();
        customFieldPanelLeft = new javax.swing.JPanel();
        customFieldPanelRight = new javax.swing.JPanel();
        headerLabel = new javax.swing.JLabel();
        projectLabel = new javax.swing.JLabel();
        projectCombo = new javax.swing.JComboBox();
        issueTypeLabel = new javax.swing.JLabel();
        issueTypeCombo = new javax.swing.JComboBox();
        statusLabel = new javax.swing.JLabel();
        statusCombo = new javax.swing.JComboBox();
        resolutionLabel = new javax.swing.JLabel();
        resolutionCombo = new javax.swing.JComboBox();
        priorityLabel = new javax.swing.JLabel();
        priorityCombo = new javax.swing.JComboBox();
        createdLabel = new javax.swing.JLabel();
        createdField = new javax.swing.JTextField();
        updatedLabel = new javax.swing.JLabel();
        updatedField = new javax.swing.JTextField();
        dueLabel = new javax.swing.JLabel();
        dueField = new javax.swing.JFormattedTextField();
        assigneeLabel = new javax.swing.JLabel();
        assigneeField = new javax.swing.JTextField();
        componentLabel = new javax.swing.JLabel();
        componentScrollPane = new javax.swing.JScrollPane();
        componentList = new javax.swing.JList();
        affectsVersionLabel = new javax.swing.JLabel();
        affectsVersionScrollPane = new javax.swing.JScrollPane();
        affectsVersionList = new javax.swing.JList();
        fixVersionLabel = new javax.swing.JLabel();
        fixVersionScrollPane = new javax.swing.JScrollPane();
        fixVersionList = new javax.swing.JList();
        originalEstimateLabel = new javax.swing.JLabel();
        originalEstimateField = new javax.swing.JTextField();
        remainingEstimateLabel = new javax.swing.JLabel();
        remainingEstimateField = new javax.swing.JTextField();
        timeSpentLabel = new javax.swing.JLabel();
        timeSpentField = new javax.swing.JTextField();
        attachmentLabel = new javax.swing.JLabel();
        subtaskLabel = new javax.swing.JLabel();
        summaryLabel = new javax.swing.JLabel();
        summaryField = new javax.swing.JTextField();
        environmentLabel = new javax.swing.JLabel();
        environmentScrollPane = new javax.swing.JScrollPane();
        environmentArea = new javax.swing.JTextArea();
        addCommentLabel = new javax.swing.JLabel();
        addCommentScrollPane = new javax.swing.JScrollPane();
        addCommentArea = new javax.swing.JTextArea();
        separator = new javax.swing.JSeparator();
        submitButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        dummyCommentPanel = new javax.swing.JPanel();
        dummySubtaskPanel = new javax.swing.JPanel();
        dummyAttachmentPanel = new javax.swing.JPanel();
        actionPanel = new javax.swing.JPanel();
        actionLabel = new javax.swing.JLabel();
        startProgressButton = new org.netbeans.modules.bugtracking.util.LinkButton();
        stopProgressButton = new org.netbeans.modules.bugtracking.util.LinkButton();
        resolveIssueButton = new org.netbeans.modules.bugtracking.util.LinkButton();
        closeIssueButton = new org.netbeans.modules.bugtracking.util.LinkButton();
        createSubtaskButton = new org.netbeans.modules.bugtracking.util.LinkButton();
        convertToSubtaskButton = new org.netbeans.modules.bugtracking.util.LinkButton();
        logWorkButton = new org.netbeans.modules.bugtracking.util.LinkButton();
        refreshButton = new org.netbeans.modules.bugtracking.util.LinkButton();
        reopenIssueButton = new org.netbeans.modules.bugtracking.util.LinkButton();
        originalEstimatePanel = new javax.swing.JPanel();
        remainingEstimatePanel = new javax.swing.JPanel();
        timeSpentPanel = new javax.swing.JPanel();
        originalEstimateFieldNew = new javax.swing.JTextField();
        originalEstimateLabelNew = new javax.swing.JLabel();
        originalEstimateHint = new javax.swing.JLabel();
        logWorkButton2 = new org.netbeans.modules.bugtracking.util.LinkButton();

        resolutionField.setEditable(false);
        resolutionField.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        projectField.setEditable(false);
        projectField.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        statusField.setEditable(false);
        statusField.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        setBackground(javax.swing.UIManager.getDefaults().getColor("EditorPane.background"));

        projectLabel.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.projectLabel.text")); // NOI18N

        projectCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                projectComboActionPerformed(evt);
            }
        });

        issueTypeLabel.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.issueTypeLabel.text")); // NOI18N

        statusLabel.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.statusLabel.text")); // NOI18N

        statusCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                statusComboActionPerformed(evt);
            }
        });

        resolutionLabel.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.resolutionLabel.text")); // NOI18N

        priorityLabel.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.priorityLabel.text")); // NOI18N

        createdLabel.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.createdLabel.text")); // NOI18N

        createdField.setEditable(false);
        createdField.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        updatedLabel.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.updatedLabel.text")); // NOI18N

        updatedField.setEditable(false);
        updatedField.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        dueLabel.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.dueLabel.text")); // NOI18N

        dueField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.DateFormatter() {
            public Object stringToValue(String text) throws java.text.ParseException {
                if (text == null || text.trim().length() == 0) {
                    return null;
                }
                return super.stringToValue(text);
            }
        }));

        assigneeLabel.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.assigneeLabel.text")); // NOI18N

        assigneeField.setColumns(15);

        componentLabel.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.componentLabel.text")); // NOI18N

        componentList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Dummy" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        componentScrollPane.setViewportView(componentList);

        affectsVersionLabel.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.affectsVersionLabel.text")); // NOI18N

        affectsVersionList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Dummy" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        affectsVersionScrollPane.setViewportView(affectsVersionList);

        fixVersionLabel.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.fixVersionLabel.text")); // NOI18N

        fixVersionList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Dummy" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        fixVersionScrollPane.setViewportView(fixVersionList);

        originalEstimateLabel.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.originalEstimateLabel.text")); // NOI18N

        originalEstimateField.setEditable(false);
        originalEstimateField.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        remainingEstimateLabel.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.remainingEstimateLabel.text")); // NOI18N

        remainingEstimateField.setEditable(false);
        remainingEstimateField.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        timeSpentLabel.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.timeSpentLabel.text")); // NOI18N

        timeSpentField.setEditable(false);
        timeSpentField.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        attachmentLabel.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.attachmentLabel.text")); // NOI18N

        subtaskLabel.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.subtaskLabel.text")); // NOI18N

        summaryLabel.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.summaryLabel.text")); // NOI18N

        environmentLabel.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.environmentLabel.text")); // NOI18N

        environmentArea.setRows(5);
        environmentScrollPane.setViewportView(environmentArea);

        addCommentLabel.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.addCommentLabel.text")); // NOI18N

        addCommentArea.setRows(5);
        addCommentScrollPane.setViewportView(addCommentArea);

        submitButton.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.submitButton.text")); // NOI18N
        submitButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                submitButtonActionPerformed(evt);
            }
        });

        cancelButton.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.cancelButton.text")); // NOI18N
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        dummySubtaskPanel.setOpaque(false);

        dummyAttachmentPanel.setOpaque(false);

        actionPanel.setBackground(new java.awt.Color(233, 236, 245));

        actionLabel.setFont(actionLabel.getFont().deriveFont(actionLabel.getFont().getStyle() | java.awt.Font.BOLD));
        actionLabel.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.actionLabel.text")); // NOI18N

        startProgressButton.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.startProgressButton.text")); // NOI18N
        startProgressButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startProgressButtonActionPerformed(evt);
            }
        });

        stopProgressButton.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.stopProgressButton.text")); // NOI18N
        stopProgressButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stopProgressButtonActionPerformed(evt);
            }
        });

        resolveIssueButton.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.resolveIssueButton.text")); // NOI18N
        resolveIssueButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resolveIssueButtonActionPerformed(evt);
            }
        });

        closeIssueButton.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.closeIssueButton.text")); // NOI18N
        closeIssueButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeIssueButtonActionPerformed(evt);
            }
        });

        createSubtaskButton.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.createSubtaskButton.text")); // NOI18N

        convertToSubtaskButton.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.convertToSubtaskButton.text")); // NOI18N

        logWorkButton.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.logWorkButton.text")); // NOI18N
        logWorkButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                logWorkButtonActionPerformed(evt);
            }
        });

        refreshButton.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.refreshButton.text")); // NOI18N
        refreshButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshButtonActionPerformed(evt);
            }
        });

        reopenIssueButton.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.reopenIssueButton.text")); // NOI18N
        reopenIssueButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                reopenIssueButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout actionPanelLayout = new org.jdesktop.layout.GroupLayout(actionPanel);
        actionPanel.setLayout(actionPanelLayout);
        actionPanelLayout.setHorizontalGroup(
            actionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(actionPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(actionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(actionLabel)
                    .add(startProgressButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(resolveIssueButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(createSubtaskButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(convertToSubtaskButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(logWorkButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(refreshButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(stopProgressButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(closeIssueButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(reopenIssueButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        actionPanelLayout.setVerticalGroup(
            actionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(actionPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(actionLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(startProgressButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(stopProgressButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(reopenIssueButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(resolveIssueButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(closeIssueButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(createSubtaskButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(convertToSubtaskButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(logWorkButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(refreshButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        originalEstimatePanel.setOpaque(false);

        remainingEstimatePanel.setOpaque(false);

        timeSpentPanel.setOpaque(false);

        originalEstimateFieldNew.setColumns(15);

        originalEstimateLabelNew.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.originalEstimateLabelNew.text")); // NOI18N

        originalEstimateHint.setFont(originalEstimateHint.getFont().deriveFont(originalEstimateHint.getFont().getSize()-2f));
        originalEstimateHint.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.originalEstimateHint.text")); // NOI18N

        logWorkButton2.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.logWorkButton2.text")); // NOI18N
        logWorkButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                logWorkButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(separator)
            .add(dummyCommentPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap(475, Short.MAX_VALUE)
                .add(logWorkButton2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(remainingEstimateLabel)
                            .add(timeSpentLabel)
                            .add(originalEstimateLabel))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(timeSpentField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(timeSpentPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .add(layout.createSequentialGroup()
                                .add(remainingEstimateField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(remainingEstimatePanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .add(layout.createSequentialGroup()
                                .add(originalEstimateField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(originalEstimatePanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                            .add(customFieldPanelLeft, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(summaryLabel)
                            .add(environmentLabel)
                            .add(addCommentLabel)
                            .add(attachmentLabel)
                            .add(subtaskLabel)
                            .add(originalEstimateLabelNew, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(environmentScrollPane)
                            .add(summaryField)
                            .add(addCommentScrollPane)
                            .add(layout.createSequentialGroup()
                                .add(submitButton)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(cancelButton))
                            .add(dummyAttachmentPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(dummySubtaskPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(originalEstimateFieldNew, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(originalEstimateHint, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(customFieldPanelRight, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(projectLabel)
                                    .add(issueTypeLabel)
                                    .add(statusLabel)
                                    .add(resolutionLabel)
                                    .add(priorityLabel))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(priorityCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(layout.createSequentialGroup()
                                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                            .add(projectCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                            .add(issueTypeCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                            .add(statusCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                            .add(resolutionCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                        .add(18, 18, 18)
                                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                            .add(createdLabel)
                                            .add(updatedLabel)
                                            .add(dueLabel)
                                            .add(assigneeLabel))
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                            .add(assigneeField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                            .add(dueField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                            .add(updatedField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                            .add(createdField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))))
                            .add(layout.createSequentialGroup()
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(componentLabel)
                                    .add(componentScrollPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                .add(18, 18, 18)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(affectsVersionLabel)
                                    .add(affectsVersionScrollPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                .add(18, 18, 18)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(fixVersionScrollPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(fixVersionLabel)))
                            .add(headerLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(parentHeaderPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(actionPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        layout.linkSize(new java.awt.Component[] {affectsVersionScrollPane, componentScrollPane, fixVersionScrollPane}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.linkSize(new java.awt.Component[] {originalEstimateField, remainingEstimateField, timeSpentField}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.linkSize(new java.awt.Component[] {assigneeField, dueField}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.linkSize(new java.awt.Component[] {issueTypeCombo, priorityCombo, projectCombo, resolutionCombo, statusCombo}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.linkSize(new java.awt.Component[] {cancelButton, submitButton}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(parentHeaderPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(0, 0, 0)
                        .add(headerLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(projectLabel)
                            .add(projectCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(createdLabel)
                            .add(createdField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(issueTypeLabel)
                            .add(issueTypeCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(updatedLabel)
                            .add(updatedField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(statusLabel)
                            .add(statusCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(dueLabel)
                            .add(dueField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(resolutionLabel)
                            .add(resolutionCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(assigneeLabel)
                            .add(assigneeField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(priorityLabel)
                            .add(priorityCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .add(18, 18, 18)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(componentLabel)
                            .add(affectsVersionLabel)
                            .add(fixVersionLabel))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(componentScrollPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(affectsVersionScrollPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(fixVersionScrollPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                    .add(actionPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(originalEstimateLabel)
                        .add(originalEstimateField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(originalEstimatePanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(remainingEstimateLabel)
                        .add(remainingEstimateField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(remainingEstimatePanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(timeSpentLabel)
                        .add(timeSpentField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(timeSpentPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(logWorkButton2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(attachmentLabel)
                    .add(dummyAttachmentPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(subtaskLabel)
                    .add(dummySubtaskPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(summaryLabel)
                    .add(summaryField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(environmentLabel)
                    .add(environmentScrollPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(customFieldPanelRight, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(customFieldPanelLeft, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(addCommentScrollPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(addCommentLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(originalEstimateFieldNew, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(originalEstimateLabelNew))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(originalEstimateHint)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(submitButton)
                    .add(cancelButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(separator, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(dummyCommentPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        layout.linkSize(new java.awt.Component[] {remainingEstimateField, remainingEstimatePanel}, org.jdesktop.layout.GroupLayout.VERTICAL);

        layout.linkSize(new java.awt.Component[] {timeSpentField, timeSpentPanel}, org.jdesktop.layout.GroupLayout.VERTICAL);

        layout.linkSize(new java.awt.Component[] {originalEstimateField, originalEstimatePanel}, org.jdesktop.layout.GroupLayout.VERTICAL);

    }// </editor-fold>//GEN-END:initComponents

    private void projectComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_projectComboActionPerformed
        Object value = projectCombo.getSelectedItem();
        if (!(value instanceof Project)) return;
        final Project project = (Project)value;

        String msgPattern = NbBundle.getMessage(IssuePanel.class, "IssuePanel.projectMetaData"); // NOI18N
        String msg = MessageFormat.format(msgPattern, project.getName());
        final boolean wasReloading = reloading;
        final ProgressHandle handle = ProgressHandleFactory.createHandle(msg);
        handle.start();
        handle.switchToIndeterminate();
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                // The project meta-data may not be initialized.
                // Their intialization must be performed outside event-dispatch thread
                try {
                    JiraConfiguration config =  issue.getRepository().getConfiguration();
                    config.ensureProjectLoaded(project);
                    if (project.getIssueTypes() == null) {
                        // The cached project data had been created before we started
                        // to use project specific issue types. Forcing reload.
                        config.forceProjectReload(project);
                    }
                } finally {
                    handle.finish();
                }
                EventQueue.invokeLater(new Runnable() {
                    public void run () {
                        boolean oldReloading = reloading;
                        reloading = wasReloading;

                        // --- Reload dependent combos
                        JiraConfiguration config =  issue.getRepository().getConfiguration();
                        boolean subtask = issue.isSubtask();
                        IssueType[] issueTypes = config.getIssueTypes(project);
                        List<IssueType> types = new ArrayList<IssueType>(issueTypes.length);
                        for (IssueType issueType : issueTypes) {
                            if (issueType.isSubTaskType() == subtask) {
                                types.add(issueType);
                            }
                        }
                        issueTypeCombo.setModel(new DefaultComboBoxModel(types.toArray(new IssueType[types.size()])));
                        reloadField(issueTypeCombo, config.getIssueTypeById(issue.getFieldValue(NbJiraIssue.IssueField.TYPE)), NbJiraIssue.IssueField.TYPE);

                        // Reload components
                        DefaultListModel componentModel = new DefaultListModel();
                        for (org.eclipse.mylyn.internal.jira.core.model.Component component : config.getComponents(project)) {
                            componentModel.addElement(component);
                        }
                        componentList.setModel(componentModel);
                        List<String> componentIds = issue.getFieldValues(NbJiraIssue.IssueField.COMPONENT);
                        reloadField(componentList, componentsByIds(project.getId(), componentIds), NbJiraIssue.IssueField.COMPONENT);

                        // Reload versions
                        DefaultListModel versionModel = new DefaultListModel();
                        for (Version version : config.getVersions(project)) {
                            versionModel.addElement(version);
                        }
                        affectsVersionList.setModel(versionModel);
                        fixVersionList.setModel(versionModel);
                        List<String> affectsVersionIds = issue.getFieldValues(NbJiraIssue.IssueField.AFFECTSVERSIONS);
                        reloadField(affectsVersionList, versionsByIds(project.getId(), affectsVersionIds),NbJiraIssue.IssueField.AFFECTSVERSIONS);
                        List<String> fixVersionIds = issue.getFieldValues(NbJiraIssue.IssueField.FIXVERSIONS);
                        reloadField(fixVersionList, versionsByIds(project.getId(), fixVersionIds), NbJiraIssue.IssueField.FIXVERSIONS);

                        reloading = oldReloading;

                        TaskData data = issue.getTaskData();
                        if (data.isNew()) {
                            issue.setFieldValue(NbJiraIssue.IssueField.PROJECT, project.getId());
                            JiraRepositoryConnector connector = Jira.getInstance().getRepositoryConnector();
                            try {
                                connector.getTaskDataHandler().initializeTaskData(issue.getRepository().getTaskRepository(), data, connector.getTaskMapping(data), new NullProgressMonitor());
                                reloadForm(false);
                            } catch (CoreException cex) {
                                cex.printStackTrace();
                            }
                        }
                    }
                });
            }
        });
    }//GEN-LAST:event_projectComboActionPerformed

    private void refreshButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshButtonActionPerformed
        String refreshMessageFormat = NbBundle.getMessage(IssuePanel.class, "IssuePanel.refreshMessage"); // NOI18N
        String refreshMessage = MessageFormat.format(refreshMessageFormat, issue.getKey());
        final ProgressHandle handle = ProgressHandleFactory.createHandle(refreshMessage);
        handle.start();
        handle.switchToIndeterminate();
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                issue.refresh();
                handle.finish();
                reloadFormInAWT(true);
            }
        });
    }//GEN-LAST:event_refreshButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        reloadForm(true);
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void submitButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_submitButtonActionPerformed
        boolean isNew = issue.getTaskData().isNew();
        storeStatusAndResolution();
        storeFieldValue(NbJiraIssue.IssueField.PROJECT, projectCombo);
        storeFieldValue(NbJiraIssue.IssueField.TYPE, issueTypeCombo);
        storeFieldValue(NbJiraIssue.IssueField.PRIORITY, priorityCombo);
        storeFieldValue(NbJiraIssue.IssueField.COMPONENT, componentList);
        storeFieldValue(NbJiraIssue.IssueField.AFFECTSVERSIONS, affectsVersionList);
        storeFieldValue(NbJiraIssue.IssueField.FIXVERSIONS, fixVersionList);
        storeFieldValue(NbJiraIssue.IssueField.SUMMARY, summaryField);
        storeFieldValue(NbJiraIssue.IssueField.ENVIRONMENT, environmentArea);
        storeFieldValue(NbJiraIssue.IssueField.DUE, dueField);
        storeFieldValue(NbJiraIssue.IssueField.ASSIGNEE, assigneeField);
        for (NbJiraIssue.CustomField cField : getSupportedCustomFields()) {
            storeCustomFieldValue(cField);
        }
        String submitMessage;
        if (isNew) {
            String estimateCode = originalEstimateFieldNew.getText();
            String estimateTxt = JiraUtils.getWorkLogSeconds(
                    estimateCode,
                    issue.getRepository().getConfiguration().getWorkDaysPerWeek(),
                    issue.getRepository().getConfiguration().getWorkHoursPerDay()) + ""; // NOI18N
            storeFieldValue(NbJiraIssue.IssueField.INITIAL_ESTIMATE, estimateTxt);
            storeFieldValue(NbJiraIssue.IssueField.ESTIMATE, estimateTxt);
            storeFieldValue(NbJiraIssue.IssueField.DESCRIPTION, addCommentArea);
            submitMessage = NbBundle.getMessage(IssuePanel.class, "IssuePanel.submitNewMessage"); // NOI18N
        } else {
            String submitMessageFormat = NbBundle.getMessage(IssuePanel.class, "IssuePanel.submitMessage"); // NOI18N
            submitMessage = MessageFormat.format(submitMessageFormat, issue.getKey());
        }
        final ProgressHandle handle = ProgressHandleFactory.createHandle(submitMessage);
        handle.start();
        handle.switchToIndeterminate();
        skipReload = true;
        if (!isNew && !"".equals(addCommentArea.getText().trim())) { // NOI18N
            issue.addComment(addCommentArea.getText());
        }
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                boolean ret = false;
                try {
                    ret = issue.submitAndRefresh();
                    for (File attachment : attachmentsPanel.getNewAttachments()) {
                        if (attachment.exists()) {
                            issue.addAttachment(attachment, null, null);
                        } else {
                            // PENDING notify user
                        }
                    }
                } finally {
                    EventQueue.invokeLater(new Runnable() {
                        public void run() {
                            skipReload = false;
                        }
                    });
                    handle.finish();
                    if(ret) {
                        reloadFormInAWT(true);
                    }
                }
            }
        });
    }//GEN-LAST:event_submitButtonActionPerformed

    private void statusComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_statusComboActionPerformed
        Object selection = statusCombo.getSelectedItem();
        if (!(selection instanceof JiraStatus)) {
            return;
        }
        String selectedStatus = ((JiraStatus)selection).getName();
        Object ir = initialValues.get(NbJiraIssue.IssueField.RESOLUTION);
        boolean initiallyWithResolution = (ir != null) && (ir.toString().trim().length() > 0);
        boolean nowWithResolution = STATUS_CLOSED.equals(selectedStatus) || STATUS_RESOLVED.equals(selectedStatus);
        boolean showCombo = !initiallyWithResolution && nowWithResolution;
        boolean showField = initiallyWithResolution && nowWithResolution;
        GroupLayout layout = (GroupLayout)getLayout();
        if (showCombo && (resolutionCombo.getParent() == null)) {
            layout.replace(resolutionField, resolutionCombo);
        }
        if (showField && (resolutionField.getParent() == null)) {
            layout.replace(resolutionCombo, resolutionField);
        }
        resolutionCombo.setVisible(nowWithResolution);
        resolutionField.setVisible(nowWithResolution);
    }//GEN-LAST:event_statusComboActionPerformed

    private void startProgressButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startProgressButtonActionPerformed
        String pattern = NbBundle.getMessage(IssuePanel.class, "IssuePanel.startProgressMessage"); // NOI18N
        String message = MessageFormat.format(pattern, issue.getKey());
        submitChange(new Runnable() {
            public void run() {
                issue.startProgress();
                issue.submitAndRefresh();
            }
        }, message);
    }//GEN-LAST:event_startProgressButtonActionPerformed

    private void stopProgressButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stopProgressButtonActionPerformed
        String pattern = NbBundle.getMessage(IssuePanel.class, "IssuePanel.stopProgressMessage"); // NOI18N
        String message = MessageFormat.format(pattern, issue.getKey());
        submitChange(new Runnable() {
            public void run() {
                issue.stopProgress();
                issue.submitAndRefresh();
            }
        }, message);
    }//GEN-LAST:event_stopProgressButtonActionPerformed

    private void resolveIssueButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resolveIssueButtonActionPerformed
        ResolveIssuePanel panel = new ResolveIssuePanel(issue);
        String title = NbBundle.getMessage(IssuePanel.class, "IssuePanel.resolveIssueButton.text"); // NOI18N
        if (BugtrackingUtil.show(panel, title, title)) {
            String pattern = NbBundle.getMessage(IssuePanel.class, "IssuePanel.resolveIssueMessage"); // NOI18N
            String message = MessageFormat.format(pattern, issue.getKey());
            final Resolution resolution = panel.getSelectedResolution();
            submitChange(new Runnable() {
                public void run() {
                    issue.resolve(resolution, null);
                    issue.submitAndRefresh();
                }
            }, message);
        }
    }//GEN-LAST:event_resolveIssueButtonActionPerformed

    private void closeIssueButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeIssueButtonActionPerformed
        ResolveIssuePanel panel = new ResolveIssuePanel(issue);
        String resolution = issue.getFieldValue(NbJiraIssue.IssueField.RESOLUTION);
        final Resolution newResolution;
        if ((resolution == null) || resolution.trim().equals("")) { // NOI18N
            String title = NbBundle.getMessage(IssuePanel.class, "IssuePanel.closeIssueButton.text"); // NOI18N
            if (BugtrackingUtil.show(panel, title, title)) {
                newResolution = panel.getSelectedResolution();
            } else {
                newResolution = null;
            }
        } else {
            newResolution = issue.getRepository().getConfiguration().getResolutionById(resolution);
        }
        if (newResolution != null) {
            String pattern = NbBundle.getMessage(IssuePanel.class, "IssuePanel.closeIssueMessage"); // NOI18N
            String message = MessageFormat.format(pattern, issue.getKey());
            submitChange(new Runnable() {
                public void run() {
                    issue.close(newResolution, null);
                    issue.submitAndRefresh();
                }
            }, message);
        }

    }//GEN-LAST:event_closeIssueButtonActionPerformed

    private void reopenIssueButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_reopenIssueButtonActionPerformed
        String pattern = NbBundle.getMessage(IssuePanel.class, "IssuePanel.reopenIssueMessage"); // NOI18N
        String message = MessageFormat.format(pattern, issue.getKey());
        submitChange(new Runnable() {
            public void run() {
                issue.reopen(null);
                issue.submitAndRefresh();
            }
        }, message);
    }//GEN-LAST:event_reopenIssueButtonActionPerformed

    private void logWorkButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_logWorkButtonActionPerformed
        final WorkLogPanel panel = new WorkLogPanel(issue);
        if (panel.showDialog()) {
            String pattern = NbBundle.getMessage(IssuePanel.class, "IssuePanel.logWorkMessage"); // NOI18N
            String message = MessageFormat.format(pattern, issue.getKey());
            submitChange(new Runnable() {
                public void run() {
                    issue.addWorkLog(panel.getStartDate(), panel.getTimeSpent(), panel.getDescription());
                    int remainingEstimate = panel.getRemainingEstimate();
                    if (remainingEstimate != -1) { // -1 means auto-adjust
                        issue.setFieldValue(NbJiraIssue.IssueField.ESTIMATE, (remainingEstimate+panel.getTimeSpent())+""); // NOI18N
                    }
                    issue.submitAndRefresh();
                }
            }, message);
        }
    }//GEN-LAST:event_logWorkButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel actionLabel;
    private javax.swing.JPanel actionPanel;
    private javax.swing.JTextArea addCommentArea;
    private javax.swing.JLabel addCommentLabel;
    private javax.swing.JScrollPane addCommentScrollPane;
    private javax.swing.JLabel affectsVersionLabel;
    private javax.swing.JList affectsVersionList;
    private javax.swing.JScrollPane affectsVersionScrollPane;
    private javax.swing.JTextField assigneeField;
    private javax.swing.JLabel assigneeLabel;
    private javax.swing.JLabel attachmentLabel;
    private javax.swing.JButton cancelButton;
    private org.netbeans.modules.bugtracking.util.LinkButton closeIssueButton;
    private javax.swing.JLabel componentLabel;
    private javax.swing.JList componentList;
    private javax.swing.JScrollPane componentScrollPane;
    private org.netbeans.modules.bugtracking.util.LinkButton convertToSubtaskButton;
    private org.netbeans.modules.bugtracking.util.LinkButton createSubtaskButton;
    private javax.swing.JTextField createdField;
    private javax.swing.JLabel createdLabel;
    private javax.swing.JPanel customFieldPanelLeft;
    private javax.swing.JPanel customFieldPanelRight;
    private javax.swing.JFormattedTextField dueField;
    private javax.swing.JLabel dueLabel;
    private javax.swing.JPanel dummyAttachmentPanel;
    private javax.swing.JPanel dummyCommentPanel;
    private javax.swing.JPanel dummySubtaskPanel;
    private javax.swing.JTextArea environmentArea;
    private javax.swing.JLabel environmentLabel;
    private javax.swing.JScrollPane environmentScrollPane;
    private javax.swing.JLabel fixVersionLabel;
    private javax.swing.JList fixVersionList;
    private javax.swing.JScrollPane fixVersionScrollPane;
    private javax.swing.JLabel headerLabel;
    private javax.swing.JComboBox issueTypeCombo;
    private javax.swing.JLabel issueTypeLabel;
    private org.netbeans.modules.bugtracking.util.LinkButton logWorkButton;
    private org.netbeans.modules.bugtracking.util.LinkButton logWorkButton2;
    private javax.swing.JTextField originalEstimateField;
    private javax.swing.JTextField originalEstimateFieldNew;
    private javax.swing.JLabel originalEstimateHint;
    private javax.swing.JLabel originalEstimateLabel;
    private javax.swing.JLabel originalEstimateLabelNew;
    private javax.swing.JPanel originalEstimatePanel;
    private javax.swing.JPanel parentHeaderPanel;
    private javax.swing.JComboBox priorityCombo;
    private javax.swing.JLabel priorityLabel;
    private javax.swing.JComboBox projectCombo;
    private javax.swing.JTextField projectField;
    private javax.swing.JLabel projectLabel;
    private org.netbeans.modules.bugtracking.util.LinkButton refreshButton;
    private javax.swing.JTextField remainingEstimateField;
    private javax.swing.JLabel remainingEstimateLabel;
    private javax.swing.JPanel remainingEstimatePanel;
    private org.netbeans.modules.bugtracking.util.LinkButton reopenIssueButton;
    private javax.swing.JComboBox resolutionCombo;
    private javax.swing.JTextField resolutionField;
    private javax.swing.JLabel resolutionLabel;
    private org.netbeans.modules.bugtracking.util.LinkButton resolveIssueButton;
    private javax.swing.JSeparator separator;
    private org.netbeans.modules.bugtracking.util.LinkButton startProgressButton;
    private javax.swing.JComboBox statusCombo;
    private javax.swing.JTextField statusField;
    private javax.swing.JLabel statusLabel;
    private org.netbeans.modules.bugtracking.util.LinkButton stopProgressButton;
    private javax.swing.JButton submitButton;
    private javax.swing.JLabel subtaskLabel;
    private javax.swing.JTextField summaryField;
    private javax.swing.JLabel summaryLabel;
    private javax.swing.JTextField timeSpentField;
    private javax.swing.JLabel timeSpentLabel;
    private javax.swing.JPanel timeSpentPanel;
    private javax.swing.JTextField updatedField;
    private javax.swing.JLabel updatedLabel;
    // End of variables declaration//GEN-END:variables

    class CancelHighlightListener implements DocumentListener, ActionListener, ListSelectionListener {
        private JLabel label;

        CancelHighlightListener(JLabel label) {
            this.label = label;
        }

        public void insertUpdate(DocumentEvent e) {
            cancelHighlight(label);
        }

        public void removeUpdate(DocumentEvent e) {
            cancelHighlight(label);
        }

        public void changedUpdate(DocumentEvent e) {
            cancelHighlight(label);
        }

        public void actionPerformed(ActionEvent e) {
            cancelHighlight(label);
        }

        public void valueChanged(ListSelectionEvent e) {
            if (e.getValueIsAdjusting()) {
                return;
            }
            cancelHighlight(label);
        }
    }

}
