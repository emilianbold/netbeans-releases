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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListModel;
import javax.swing.text.JTextComponent;
import org.eclipse.mylyn.internal.jira.core.model.IssueType;
import org.eclipse.mylyn.internal.jira.core.model.JiraStatus;
import org.eclipse.mylyn.internal.jira.core.model.Priority;
import org.eclipse.mylyn.internal.jira.core.model.Project;
import org.eclipse.mylyn.internal.jira.core.model.Resolution;
import org.eclipse.mylyn.internal.jira.core.model.Version;
import org.jdesktop.layout.GroupLayout;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.bugtracking.spi.Issue;
import org.netbeans.modules.bugtracking.util.BugtrackingUtil;
import org.netbeans.modules.jira.repository.JiraConfiguration;
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
    private NbJiraIssue issue;
    private CommentsPanel commentsPanel;
    private AttachmentsPanel attachmentsPanel;
    private boolean skipReload;
    private Map<NbJiraIssue.IssueField,Object> initialValues = new HashMap<NbJiraIssue.IssueField,Object>();

    public IssuePanel() {
        initComponents();
        createdField.setBackground(getBackground());
        updatedField.setBackground(getBackground());
        originalEstimateField.setBackground(getBackground());
        remainingEstimateField.setBackground(getBackground());
        timeSpentField.setBackground(getBackground());
        resolutionField.setBackground(getBackground());
        BugtrackingUtil.fixFocusTraversalKeys(environmentArea);
        BugtrackingUtil.fixFocusTraversalKeys(addCommentArea);
        BugtrackingUtil.issue163946Hack(componentScrollPane);
        BugtrackingUtil.issue163946Hack(affectsVersionScrollPane);
        BugtrackingUtil.issue163946Hack(fixVersionScrollPane);
        BugtrackingUtil.issue163946Hack(environmentScrollPane);
        BugtrackingUtil.issue163946Hack(addCommentScrollPane);
        initAttachmentsPanel();
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
        projectCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                if (value instanceof Project) {
                    value = ((Project)value).getName();
                }
                return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            }
        });

        // Issue type combo
        issueTypeCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                if (value instanceof IssueType) {
                    value = ((IssueType)value).getName();
                }
                return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            }
        });

        // Priority combo
        priorityCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                if (value instanceof Priority) {
                    value = ((Priority)value).getName();
                }
                return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            }
        });

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
        statusCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                if (value instanceof JiraStatus) {
                    value = ((JiraStatus)value).getName();
                }
                return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            }
        });

        // Resolution combo
        resolutionCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                if (value instanceof Resolution) {
                    value = ((Resolution)value).getName();
                }
                return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            }
        });
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

    private void reloadForm(boolean force) {
        if (skipReload) {
            return;
        }
        ResourceBundle bundle = NbBundle.getBundle(IssuePanel.class);
        JiraConfiguration config = issue.getRepository().getConfiguration();
        String projectId = issue.getFieldValue(NbJiraIssue.IssueField.PROJECT);
        // Header label
        String headerFormat = bundle.getString("IssuePanel.headerLabel.format"); // NOI18N
        String headerTxt = MessageFormat.format(headerFormat, issue.getKey(), issue.getSummary());
        headerLabel.setText(headerTxt);
        // Created field
        String createdFormat = bundle.getString("IssuePanel.createdField.format"); // NOI18N
        String reporter = config.getUser(issue.getFieldValue(NbJiraIssue.IssueField.REPORTER)).getFullName();
        String creation = dateByMillis(issue.getFieldValue(NbJiraIssue.IssueField.CREATION), true);
        String createdTxt = MessageFormat.format(createdFormat, creation, reporter);
        createdField.setText(createdTxt);
        reloadField(projectCombo, config.getProjectById(projectId), NbJiraIssue.IssueField.PROJECT);
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
        reloadField(statusCombo, config.getStatusById(issue.getFieldValue(NbJiraIssue.IssueField.STATUS)), NbJiraIssue.IssueField.STATUS);
        reloadField(assigneeField, config.getUser(issue.getFieldValue(NbJiraIssue.IssueField.ASSIGNEE)).getFullName(), NbJiraIssue.IssueField.ASSIGNEE);
        reloadField(environmentArea, issue.getFieldValue(NbJiraIssue.IssueField.ENVIRONMENT), NbJiraIssue.IssueField.ENVIRONMENT);
        reloadField(updatedField, dateByMillis(issue.getFieldValue(NbJiraIssue.IssueField.MODIFICATION), true), NbJiraIssue.IssueField.MODIFICATION);
        reloadField(dueField, dateByMillis(issue.getFieldValue(NbJiraIssue.IssueField.DUE), false), NbJiraIssue.IssueField.DUE);

        // Work-log
        String originalEstimateTxt = issue.getFieldValue(NbJiraIssue.IssueField.INITIAL_ESTIMATE);
        int originalEstimate = toInt(originalEstimateTxt);
        int remainintEstimate = toInt(issue.getFieldValue(NbJiraIssue.IssueField.ESTIMATE));
        int timeSpent = toInt(issue.getFieldValue(NbJiraIssue.IssueField.ACTUAL));
        if ((originalEstimate == 0) && (remainintEstimate + timeSpent > 0)) {
            // originalEstimate is sometimes 0 (incorrectly)
            originalEstimate = remainintEstimate + timeSpent;
        }
        reloadField(originalEstimateField, workBySeconds(originalEstimate, false), NbJiraIssue.IssueField.INITIAL_ESTIMATE);
        reloadField(remainingEstimateField, workBySeconds(remainintEstimate, true), NbJiraIssue.IssueField.ESTIMATE);
        reloadField(timeSpentField, workBySeconds(timeSpent, false), NbJiraIssue.IssueField.ACTUAL);
        int scale = Math.max(originalEstimate, timeSpent);
        setupWorkLogPanel(originalEstimatePanel, ORIGINAL_ESTIMATE_COLOR, Color.lightGray, originalEstimate, scale-originalEstimate);
        setupWorkLogPanel(remainingEstimatePanel, Color.lightGray, REMAINING_ESTIMATE_COLOR, timeSpent, scale-timeSpent);
        setupWorkLogPanel(timeSpentPanel, TIME_SPENT_COLOR, Color.lightGray, timeSpent, scale-timeSpent);

        // Comments
        commentsPanel.setIssue(issue);
        BugtrackingUtil.keepFocusedComponentVisible(commentsPanel);
        if (force) {
            addCommentArea.setText(""); // NOI18N
        }

        // Attachments
        attachmentsPanel.setIssue(issue);
        BugtrackingUtil.keepFocusedComponentVisible(attachmentsPanel);
    }

    private void reloadField(JComponent fieldComponent, Object fieldValue, NbJiraIssue.IssueField field) {
        if (fieldComponent instanceof JComboBox) {
            ((JComboBox)fieldComponent).setSelectedItem(fieldValue);
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
            List initValues = (List)initValue;
            identical = values.containsAll(initValues) && initValues.containsAll(values);
        }
        if (issue.getTaskData().isNew() || !identical) {
            issue.setFieldValues(field, values);
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

    private void setupWorkLogPanel(JPanel panel, Color color1,  Color color2, int val1, int val2) {
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
    }

    private List<org.eclipse.mylyn.internal.jira.core.model.Component> componentsByIds(String projectId, List<String> componentIds) {
        JiraConfiguration config = issue.getRepository().getConfiguration();
        List<org.eclipse.mylyn.internal.jira.core.model.Component> components = new ArrayList(componentIds.size());
        for (String id : componentIds) {
            components.add(config.getComponentById(projectId, id));
        }
        return components;
    }

    private List<Version> versionsByIds(String projectId, List<String> versionIds) {
        JiraConfiguration config = issue.getRepository().getConfiguration();
        List<Version> versions = new ArrayList(versionIds.size());
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

    private String workBySeconds(int seconds, boolean remainingEstimate) {
        ResourceBundle bundle = NbBundle.getBundle(IssuePanel.class);
        if (seconds == 0) {
            return bundle.getString(remainingEstimate ? "IssuePanel.emptyWorkLog1" : "IssuePanel.emptyWorkLog2"); // NOI18N
        }
        int minutes = seconds/60;
        int hours = minutes/60;
        minutes = minutes%60;
        JiraConfiguration config = issue.getRepository().getConfiguration();
        int days = hours/config.getWorkHoursPerDay();
        hours = hours%config.getWorkHoursPerDay();
        int weeks = days/config.getWorkDaysPerWeek();
        days = days%config.getWorkDaysPerWeek();
        String format = bundle.getString("IssuePanel.workLog"); // NOI18N
        String work = MessageFormat.format(format, weeks, days, hours, minutes);
        // Removing trailing space and comma
        if (work.length() > 0 && work.charAt(work.length()-1) == ' ') {
            work = work.substring(0, work.length()-2);
        }
        return work;
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

    private void attachIssueListener(NbJiraIssue issue) {
        issue.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (Issue.EVENT_ISSUE_DATA_CHANGED.equals(evt.getPropertyName())) {
                    reloadFormInAWT(false);
                } else if (Issue.EVENT_ISSUE_SEEN_CHANGED.equals(evt.getPropertyName())) {
                    updateFieldStatuses();
                }
            }
        });
    }

    private void updateFieldStatuses() {
        // PENDING
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

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        resolutionField = new javax.swing.JTextField();
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
        dueField = new javax.swing.JTextField();
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
        acceptIssueButton = new org.netbeans.modules.bugtracking.util.LinkButton();
        resolveIssueButton = new org.netbeans.modules.bugtracking.util.LinkButton();
        createSubtaskButton = new org.netbeans.modules.bugtracking.util.LinkButton();
        convertToSubtaskButton = new org.netbeans.modules.bugtracking.util.LinkButton();
        logWorkButton = new org.netbeans.modules.bugtracking.util.LinkButton();
        refreshButton = new org.netbeans.modules.bugtracking.util.LinkButton();
        originalEstimatePanel = new javax.swing.JPanel();
        remainingEstimatePanel = new javax.swing.JPanel();
        timeSpentPanel = new javax.swing.JPanel();

        resolutionField.setEditable(false);
        resolutionField.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

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

        dueField.setColumns(15);

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

        acceptIssueButton.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.acceptIssueButton.text")); // NOI18N

        resolveIssueButton.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.resolveIssueButton.text")); // NOI18N

        createSubtaskButton.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.createSubtaskButton.text")); // NOI18N

        convertToSubtaskButton.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.convertToSubtaskButton.text")); // NOI18N

        logWorkButton.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.logWorkButton.text")); // NOI18N

        refreshButton.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.refreshButton.text")); // NOI18N
        refreshButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshButtonActionPerformed(evt);
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
                    .add(acceptIssueButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(resolveIssueButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(createSubtaskButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(convertToSubtaskButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(logWorkButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(refreshButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        actionPanelLayout.setVerticalGroup(
            actionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(actionPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(actionLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(acceptIssueButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(resolveIssueButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
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

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
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
                                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                            .add(dueField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                            .add(updatedField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                            .add(createdField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .add(actionPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))))
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
                    .add(headerLabel)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(summaryLabel)
                            .add(environmentLabel)
                            .add(addCommentLabel)
                            .add(attachmentLabel)
                            .add(subtaskLabel))
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
                            .add(dummySubtaskPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
            .add(separator)
            .add(dummyCommentPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        layout.linkSize(new java.awt.Component[] {affectsVersionScrollPane, componentScrollPane, fixVersionScrollPane}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.linkSize(new java.awt.Component[] {originalEstimateField, remainingEstimateField, timeSpentField}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.linkSize(new java.awt.Component[] {issueTypeCombo, priorityCombo, projectCombo, resolutionCombo, statusCombo}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.linkSize(new java.awt.Component[] {cancelButton, submitButton}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(headerLabel)
                        .add(16, 16, 16)
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
                .add(18, 18, 18)
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
                .add(18, 18, 18)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(attachmentLabel)
                    .add(dummyAttachmentPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(18, 18, 18)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(subtaskLabel)
                    .add(dummySubtaskPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(19, 19, 19)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(summaryLabel)
                    .add(summaryField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(environmentLabel)
                    .add(environmentScrollPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(addCommentLabel)
                    .add(addCommentScrollPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
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
        Project project = (Project)value;
        JiraConfiguration config =  issue.getRepository().getConfiguration();

        // --- Reload dependent combos
        // PENDING JiraConfiguration doesn't provide project-specific info for issue-type
        issueTypeCombo.setModel(new DefaultComboBoxModel(config.getIssueTypes()));

        // Reload components
        DefaultListModel componentModel = new DefaultListModel();
        for (org.eclipse.mylyn.internal.jira.core.model.Component component : config.getComponents(project)) {
            componentModel.addElement(component);
        }
        componentList.setModel(componentModel);

        // Reload versions
        DefaultListModel versionModel = new DefaultListModel();
        for (Version version : config.getVersions(project)) {
            versionModel.addElement(version);
        }
        affectsVersionList.setModel(versionModel);
        fixVersionList.setModel(versionModel);
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
        String submitMessage;
        if (isNew) {
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

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private org.netbeans.modules.bugtracking.util.LinkButton acceptIssueButton;
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
    private javax.swing.JLabel componentLabel;
    private javax.swing.JList componentList;
    private javax.swing.JScrollPane componentScrollPane;
    private org.netbeans.modules.bugtracking.util.LinkButton convertToSubtaskButton;
    private org.netbeans.modules.bugtracking.util.LinkButton createSubtaskButton;
    private javax.swing.JTextField createdField;
    private javax.swing.JLabel createdLabel;
    private javax.swing.JTextField dueField;
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
    private javax.swing.JTextField originalEstimateField;
    private javax.swing.JLabel originalEstimateLabel;
    private javax.swing.JPanel originalEstimatePanel;
    private javax.swing.JComboBox priorityCombo;
    private javax.swing.JLabel priorityLabel;
    private javax.swing.JComboBox projectCombo;
    private javax.swing.JLabel projectLabel;
    private org.netbeans.modules.bugtracking.util.LinkButton refreshButton;
    private javax.swing.JTextField remainingEstimateField;
    private javax.swing.JLabel remainingEstimateLabel;
    private javax.swing.JPanel remainingEstimatePanel;
    private javax.swing.JComboBox resolutionCombo;
    private javax.swing.JTextField resolutionField;
    private javax.swing.JLabel resolutionLabel;
    private org.netbeans.modules.bugtracking.util.LinkButton resolveIssueButton;
    private javax.swing.JSeparator separator;
    private javax.swing.JComboBox statusCombo;
    private javax.swing.JLabel statusLabel;
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

}
