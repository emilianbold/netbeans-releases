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

import java.awt.Component;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.ListModel;
import javax.swing.text.JTextComponent;
import org.eclipse.mylyn.internal.jira.core.model.IssueType;
import org.eclipse.mylyn.internal.jira.core.model.JiraStatus;
import org.eclipse.mylyn.internal.jira.core.model.Priority;
import org.eclipse.mylyn.internal.jira.core.model.Project;
import org.eclipse.mylyn.internal.jira.core.model.Resolution;
import org.eclipse.mylyn.internal.jira.core.model.Version;
import org.netbeans.modules.jira.repository.JiraConfiguration;

/**
 *
 * @author Jan Stola
 */
public class IssuePanel extends javax.swing.JPanel {
    private NbJiraIssue issue;

    public IssuePanel() {
        initComponents();
    }

    void setIssue(NbJiraIssue issue) {
        this.issue = issue;
        initRenderers();
        initProjectCombo();
        initPriorityCombo();
        initStatusCombo();
        initResolutionCombo();

        reloadForm();
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

    private void initStatusCombo() {
        JiraStatus[] status = issue.getRepository().getConfiguration().getStatuses();
        DefaultComboBoxModel model = new DefaultComboBoxModel(status);
        statusCombo.setModel(model);
    }

    private void initResolutionCombo() {
        Resolution[] resolution = issue.getRepository().getConfiguration().getResolutions();
        DefaultComboBoxModel model = new DefaultComboBoxModel(resolution);
        resolutionCombo.setModel(model);
    }

    private void reloadForm() {
        JiraConfiguration config = issue.getRepository().getConfiguration();
        String projectId = issue.getFieldValue(NbJiraIssue.IssueField.PROJECT);
        reloadField(projectCombo, config.getProjectById(projectId));
        reloadField(issueTypeCombo, config.getIssueTypeById(issue.getFieldValue(NbJiraIssue.IssueField.TYPE)));
        reloadField(summaryField, issue.getFieldValue(NbJiraIssue.IssueField.SUMMARY));
        reloadField(priorityCombo, config.getPriorityById(issue.getFieldValue(NbJiraIssue.IssueField.PRIORITY)));
        List<String> componentIds = issue.getFieldValues(NbJiraIssue.IssueField.COMPONENT);
        reloadField(componentList, componentsByIds(projectId, componentIds));
        List<String> affectsVersionIds = issue.getFieldValues(NbJiraIssue.IssueField.AFFECTSVERSIONS);
        reloadField(affectsVersionList, versionsByIds(projectId, affectsVersionIds));
        List<String> fixVersionIds = issue.getFieldValues(NbJiraIssue.IssueField.FIXVERSIONS);
        reloadField(fixVersionList, versionsByIds(projectId, fixVersionIds));
        reloadField(statusCombo, config.getStatusById(issue.getFieldValue(NbJiraIssue.IssueField.STATUS)));
        reloadField(resolutionCombo, config.getResolutionById(issue.getFieldValue(NbJiraIssue.IssueField.RESOLUTION)));
        reloadField(assigneeField, config.getUser(issue.getFieldValue(NbJiraIssue.IssueField.ASSIGNEE)).getFullName());
        reloadField(reporterField, config.getUser(issue.getFieldValue(NbJiraIssue.IssueField.REPORTER)).getFullName());
        reloadField(environmentArea, issue.getFieldValue(NbJiraIssue.IssueField.ENVIRONMENT));
        reloadField(descriptionArea, issue.getFieldValue(NbJiraIssue.IssueField.DESCRIPTION));
        reloadField(createdField, dateByMillis(issue.getFieldValue(NbJiraIssue.IssueField.CREATION), true));
        reloadField(updatedField, dateByMillis(issue.getFieldValue(NbJiraIssue.IssueField.MODIFICATION), true));
        reloadField(dueField, dateByMillis(issue.getFieldValue(NbJiraIssue.IssueField.DUE), false));
    }

    private void reloadField(JComponent fieldComponent, Object fieldValue) {
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

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        projectLabel = new javax.swing.JLabel();
        projectCombo = new javax.swing.JComboBox();
        issueTypeLabel = new javax.swing.JLabel();
        issueTypeCombo = new javax.swing.JComboBox();
        summaryLabel = new javax.swing.JLabel();
        summaryField = new javax.swing.JTextField();
        priorityLabel = new javax.swing.JLabel();
        priorityCombo = new javax.swing.JComboBox();
        componentLabel = new javax.swing.JLabel();
        componentScrollPane = new javax.swing.JScrollPane();
        componentList = new javax.swing.JList();
        affectsVersionLabel = new javax.swing.JLabel();
        affectsVersionScrollPane = new javax.swing.JScrollPane();
        affectsVersionList = new javax.swing.JList();
        fixVersionLabel = new javax.swing.JLabel();
        fixVersionScrollPane = new javax.swing.JScrollPane();
        fixVersionList = new javax.swing.JList();
        statusLabel = new javax.swing.JLabel();
        statusCombo = new javax.swing.JComboBox();
        resolutionLabel = new javax.swing.JLabel();
        resolutionCombo = new javax.swing.JComboBox();
        assigneeLabel = new javax.swing.JLabel();
        assigneeField = new javax.swing.JTextField();
        reporterLabel = new javax.swing.JLabel();
        reporterField = new javax.swing.JTextField();
        environmentLabel = new javax.swing.JLabel();
        environmentScrollPane = new javax.swing.JScrollPane();
        environmentArea = new javax.swing.JTextArea();
        descriptionLabel = new javax.swing.JLabel();
        descriptionScrollPane = new javax.swing.JScrollPane();
        descriptionArea = new javax.swing.JTextArea();
        createdLabel = new javax.swing.JLabel();
        createdField = new javax.swing.JTextField();
        updatedLabel = new javax.swing.JLabel();
        updatedField = new javax.swing.JTextField();
        dueLabel = new javax.swing.JLabel();
        dueField = new javax.swing.JTextField();

        projectLabel.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.projectLabel.text")); // NOI18N

        projectCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                projectComboActionPerformed(evt);
            }
        });

        issueTypeLabel.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.issueTypeLabel.text")); // NOI18N

        summaryLabel.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.summaryLabel.text")); // NOI18N

        summaryField.setColumns(30);

        priorityLabel.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.priorityLabel.text")); // NOI18N

        componentLabel.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.componentLabel.text")); // NOI18N

        componentScrollPane.setViewportView(componentList);

        affectsVersionLabel.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.affectsVersionLabel.text")); // NOI18N

        affectsVersionScrollPane.setViewportView(affectsVersionList);

        fixVersionLabel.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.fixVersionLabel.text")); // NOI18N

        fixVersionScrollPane.setViewportView(fixVersionList);

        statusLabel.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.statusLabel.text")); // NOI18N

        resolutionLabel.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.resolutionLabel.text")); // NOI18N

        assigneeLabel.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.assigneeLabel.text")); // NOI18N

        assigneeField.setColumns(20);

        reporterLabel.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.reporterLabel.text")); // NOI18N

        reporterField.setColumns(20);

        environmentLabel.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.environmentLabel.text")); // NOI18N

        environmentArea.setColumns(40);
        environmentArea.setRows(5);
        environmentScrollPane.setViewportView(environmentArea);

        descriptionLabel.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.descriptionLabel.text")); // NOI18N

        descriptionArea.setColumns(40);
        descriptionArea.setRows(5);
        descriptionScrollPane.setViewportView(descriptionArea);

        createdLabel.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.createdLabel.text")); // NOI18N

        createdField.setColumns(15);

        updatedLabel.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.updatedLabel.text")); // NOI18N

        updatedField.setColumns(15);

        dueLabel.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.dueLabel.text")); // NOI18N

        dueField.setColumns(10);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(projectLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(projectCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createSequentialGroup()
                        .add(issueTypeLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(issueTypeCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createSequentialGroup()
                        .add(summaryLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(summaryField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createSequentialGroup()
                        .add(priorityLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(priorityCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createSequentialGroup()
                        .add(componentLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(componentScrollPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createSequentialGroup()
                        .add(affectsVersionLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(affectsVersionScrollPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createSequentialGroup()
                        .add(fixVersionLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(fixVersionScrollPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createSequentialGroup()
                        .add(statusLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(statusCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createSequentialGroup()
                        .add(resolutionLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(resolutionCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createSequentialGroup()
                        .add(assigneeLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(assigneeField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createSequentialGroup()
                        .add(reporterLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(reporterField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createSequentialGroup()
                        .add(environmentLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(environmentScrollPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createSequentialGroup()
                        .add(descriptionLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(descriptionScrollPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createSequentialGroup()
                        .add(createdLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(createdField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createSequentialGroup()
                        .add(updatedLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(updatedField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createSequentialGroup()
                        .add(dueLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(dueField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(75, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(projectLabel)
                    .add(projectCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(issueTypeLabel)
                    .add(issueTypeCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(summaryLabel)
                    .add(summaryField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(priorityLabel)
                    .add(priorityCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(componentLabel)
                    .add(componentScrollPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(affectsVersionLabel)
                    .add(affectsVersionScrollPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(fixVersionLabel)
                    .add(fixVersionScrollPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(statusLabel)
                    .add(statusCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(resolutionLabel)
                    .add(resolutionCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(assigneeLabel)
                    .add(assigneeField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(reporterLabel)
                    .add(reporterField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(environmentLabel)
                    .add(environmentScrollPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(descriptionLabel)
                    .add(descriptionScrollPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(createdLabel)
                    .add(createdField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(updatedLabel)
                    .add(updatedField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(dueLabel)
                    .add(dueField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(29, Short.MAX_VALUE))
        );
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

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel affectsVersionLabel;
    private javax.swing.JList affectsVersionList;
    private javax.swing.JScrollPane affectsVersionScrollPane;
    private javax.swing.JTextField assigneeField;
    private javax.swing.JLabel assigneeLabel;
    private javax.swing.JLabel componentLabel;
    private javax.swing.JList componentList;
    private javax.swing.JScrollPane componentScrollPane;
    private javax.swing.JTextField createdField;
    private javax.swing.JLabel createdLabel;
    private javax.swing.JTextArea descriptionArea;
    private javax.swing.JLabel descriptionLabel;
    private javax.swing.JScrollPane descriptionScrollPane;
    private javax.swing.JTextField dueField;
    private javax.swing.JLabel dueLabel;
    private javax.swing.JTextArea environmentArea;
    private javax.swing.JLabel environmentLabel;
    private javax.swing.JScrollPane environmentScrollPane;
    private javax.swing.JLabel fixVersionLabel;
    private javax.swing.JList fixVersionList;
    private javax.swing.JScrollPane fixVersionScrollPane;
    private javax.swing.JComboBox issueTypeCombo;
    private javax.swing.JLabel issueTypeLabel;
    private javax.swing.JComboBox priorityCombo;
    private javax.swing.JLabel priorityLabel;
    private javax.swing.JComboBox projectCombo;
    private javax.swing.JLabel projectLabel;
    private javax.swing.JTextField reporterField;
    private javax.swing.JLabel reporterLabel;
    private javax.swing.JComboBox resolutionCombo;
    private javax.swing.JLabel resolutionLabel;
    private javax.swing.JComboBox statusCombo;
    private javax.swing.JLabel statusLabel;
    private javax.swing.JTextField summaryField;
    private javax.swing.JLabel summaryLabel;
    private javax.swing.JTextField updatedField;
    private javax.swing.JLabel updatedLabel;
    // End of variables declaration//GEN-END:variables

}
