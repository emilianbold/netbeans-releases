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
import java.awt.Component;
import java.awt.Dialog;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.Date;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.DateFormatter;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.Document;
import org.netbeans.modules.jira.repository.JiraConfiguration;
import org.netbeans.modules.jira.util.JiraUtils;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Stola
 */
public class WorkLogPanel extends javax.swing.JPanel {
    private NbJiraIssue issue;
    private boolean initialized;

    public WorkLogPanel(NbJiraIssue issue) {
        this.issue = issue;
        initComponents();
        startDateField.setFormatterFactory(new DefaultFormatterFactory(new DateFormatter(DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.SHORT))));
        startDateField.setValue(new Date());

        // Leave estimate choice
        int estimate = getCurrentRemainingEstimate();
        JiraConfiguration config = issue.getRepository().getConfiguration();
        int daysPerWeek = config.getWorkDaysPerWeek();
        int hoursPerDay = config.getWorkHoursPerDay();
        String pattern = NbBundle.getMessage(WorkLogPanel.class, "WorkLogPanel.leaveEstimateChoice.text"); // NOI18N
        String leaveEstimateText = MessageFormat.format(pattern, JiraUtils.getWorkLogText(estimate, daysPerWeek, hoursPerDay, true));
        leaveEstimateChoice.setText(leaveEstimateText);

        // Listeners
        WorkLogFormatListener workLogFormatListener = new WorkLogFormatListener();
        timeSpentField.getDocument().addDocumentListener(workLogFormatListener);
        reduceEstimatedTimeField.getDocument().addDocumentListener(workLogFormatListener);
        setEstimatedTimeField.getDocument().addDocumentListener(workLogFormatListener);
        EstimateAdjustmentTypeListener estimateAdjustmentTypeListener = new EstimateAdjustmentTypeListener();
        reduceEstimatedTimeChoice.addChangeListener(estimateAdjustmentTypeListener);
        setEstimatedTimeChoice.addChangeListener(estimateAdjustmentTypeListener);

        updateMessagePanel();
        initialized = true;
    }

    private int getCurrentRemainingEstimate() {
        String estimateTxt = issue.getFieldValue(NbJiraIssue.IssueField.ESTIMATE);
        int estimate = 0;
        if (estimateTxt != null) {
            try {
                estimate = Integer.parseInt(estimateTxt);
            } catch (NumberFormatException nfex) {
                estimate = 0;
            }
        }
        return estimate;
    }

    public boolean showDialog() {
        DialogDescriptor dd = new DialogDescriptor(
                this,
                NbBundle.getMessage(WorkLogPanel.class, "WorkLogPanel.dialog.title"), // NOI18N
                true,
                new Object[] {submitButton, discardButton},
                submitButton,
                SwingConstants.CENTER,
                HelpCtx.DEFAULT_HELP,
                null);
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);
        dialog.setVisible(true);
        return (submitButton == dd.getValue());
    }

    public Date getStartDate() {
        return (Date)startDateField.getValue();
    }

    public int getTimeSpent() {
        return getWorkLog(timeSpentField);
    }

    int getWorkLog(JTextField field) {
        String timeSpentTxt = field.getText();
        JiraConfiguration config = issue.getRepository().getConfiguration();
        int daysPerWeek = config.getWorkDaysPerWeek();
        int hoursPerDay = config.getWorkHoursPerDay();
        return JiraUtils.getWorkLogSeconds(timeSpentTxt, daysPerWeek, hoursPerDay);
    }

    public String getDescription() {
        return workDescriptionArea.getText();
    }

    public int getRemainingEstimate() {
        if (autoAdjustChoice.isSelected()) {
            return -1;
        } else if (leaveEstimateChoice.isSelected()) {
            return getCurrentRemainingEstimate();
        } else if (setEstimatedTimeChoice.isSelected()) {
            JiraConfiguration config = issue.getRepository().getConfiguration();
            int daysPerWeek = config.getWorkDaysPerWeek();
            int hoursPerDay = config.getWorkHoursPerDay();
            return JiraUtils.getWorkLogSeconds(setEstimatedTimeField.getText(), daysPerWeek, hoursPerDay);
        } else {
            assert reduceEstimatedTimeChoice.isSelected();
            JiraConfiguration config = issue.getRepository().getConfiguration();
            int daysPerWeek = config.getWorkDaysPerWeek();
            int hoursPerDay = config.getWorkHoursPerDay();
            return Math.max(0, getCurrentRemainingEstimate()-JiraUtils.getWorkLogSeconds(reduceEstimatedTimeField.getText(), daysPerWeek, hoursPerDay));
        }
    }

    void checkReduceEstimateTime() {
        reduceEstimatedTimeChoice.setSelected(true);
        int reduceAmount = getWorkLog(reduceEstimatedTimeField);
        boolean invalid = (reduceAmount < 0) || (reduceEstimatedTimeField.getText().trim().length() == 0);
        if (invalid != reduceEstimateFormat) {
            reduceEstimateFormat = invalid;
            updateMessagePanel();
        }
    }

    void checkSetEstimateTime() {
        setEstimatedTimeChoice.setSelected(true);
        int newEstimate = getWorkLog(setEstimatedTimeField);
        boolean invalid = (newEstimate < 0) || (setEstimatedTimeField.getText().trim().length() == 0);
        if (invalid != setEstimateFormat) {
            setEstimateFormat = invalid;
            updateMessagePanel();
        }
    }

    private boolean timeSpentFormat;
    private boolean timeSpentZero = true;
    private boolean reduceEstimateFormat;
    private boolean setEstimateFormat;

    void updateMessagePanel() {
        messagePanel.removeAll();
        if (timeSpentFormat) {
            JLabel timeSpentFormatLbl = new JLabel();
            timeSpentFormatLbl.setText(NbBundle.getMessage(IssuePanel.class, "WorkLogPanel.timeSpentFormat")); // NOI18N
            messagePanel.add(timeSpentFormatLbl);
        }
        if (timeSpentZero) {
            JLabel timeSpentZeroLbl = new JLabel();
            timeSpentZeroLbl.setText(NbBundle.getMessage(IssuePanel.class, "WorkLogPanel.timeSpentZero")); // NOI18N
            messagePanel.add(timeSpentZeroLbl);
        }
        if (reduceEstimateFormat) {
            JLabel reduceEstimateFormatLbl = new JLabel();
            reduceEstimateFormatLbl.setText(NbBundle.getMessage(IssuePanel.class, "WorkLogPanel.reduceEstimateFormat")); // NOI18N
            messagePanel.add(reduceEstimateFormatLbl);
        }
        if (setEstimateFormat) {
            JLabel setEstimateFormatLbl = new JLabel();
            setEstimateFormatLbl.setText(NbBundle.getMessage(IssuePanel.class, "WorkLogPanel.setEstimateFormat")); // NOI18N
            messagePanel.add(setEstimateFormatLbl);
        }
        if (timeSpentFormat || timeSpentZero || reduceEstimateFormat || setEstimateFormat) {
            String iconRes = initialized ? "org/netbeans/modules/jira/resources/error.gif" : "org/netbeans/modules/jira/resources/info.png"; // NOI18N
            Icon icon = new ImageIcon(ImageUtilities.loadImage(iconRes));
            for (Component comp : messagePanel.getComponents()) {
                ((JLabel)comp).setIcon(icon);
                if (initialized) {
                    comp.setForeground(Color.RED);
                }
            }
            messagePanel.setVisible(true);
            submitButton.setEnabled(false);
            messagePanel.revalidate();
            messagePanel.repaint();
        } else {
            messagePanel.setVisible(false);
            submitButton.setEnabled(true);
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

        submitButton = new javax.swing.JButton();
        discardButton = new javax.swing.JButton();
        adjustEstimateGroup = new javax.swing.ButtonGroup();
        hintLabel = new javax.swing.JLabel();
        timeSpentLabel = new javax.swing.JLabel();
        timeSpentField = new javax.swing.JTextField();
        startDateLabel = new javax.swing.JLabel();
        startDateField = new javax.swing.JFormattedTextField();
        timeSpentHint = new javax.swing.JLabel();
        dummyLabel = new javax.swing.JLabel();
        startDateHint = new javax.swing.JLabel();
        adjustEstimateLabel = new javax.swing.JLabel();
        autoAdjustChoice = new javax.swing.JRadioButton();
        leaveEstimateChoice = new javax.swing.JRadioButton();
        setEstimatedTimeChoice = new javax.swing.JRadioButton();
        setEstimatedTimeField = new javax.swing.JTextField();
        reduceEstimatedTimeChoice = new javax.swing.JRadioButton();
        reduceEstimatedTimeField = new javax.swing.JTextField();
        workDescriptionLabel = new javax.swing.JLabel();
        workDescriptionScrollPane = new javax.swing.JScrollPane();
        workDescriptionArea = new javax.swing.JTextArea();
        messagePanel = new javax.swing.JPanel();

        submitButton.setText(org.openide.util.NbBundle.getMessage(WorkLogPanel.class, "WorkLogPanel.submitButton.text")); // NOI18N

        discardButton.setText(org.openide.util.NbBundle.getMessage(WorkLogPanel.class, "WorkLogPanel.discardButton.text")); // NOI18N

        hintLabel.setText(org.openide.util.NbBundle.getMessage(WorkLogPanel.class, "WorkLogPanel.hintLabel.text")); // NOI18N

        timeSpentLabel.setText(org.openide.util.NbBundle.getMessage(WorkLogPanel.class, "WorkLogPanel.timeSpentLabel.text")); // NOI18N

        timeSpentField.setColumns(15);

        startDateLabel.setText(org.openide.util.NbBundle.getMessage(WorkLogPanel.class, "WorkLogPanel.startDateLabel.text")); // NOI18N

        startDateField.setColumns(13);

        timeSpentHint.setFont(timeSpentHint.getFont().deriveFont(timeSpentHint.getFont().getSize()-2f));
        timeSpentHint.setText(org.openide.util.NbBundle.getMessage(WorkLogPanel.class, "WorkLogPanel.timeSpentHint.text")); // NOI18N

        startDateHint.setFont(startDateHint.getFont().deriveFont(startDateHint.getFont().getSize()-2f));
        startDateHint.setText(org.openide.util.NbBundle.getMessage(WorkLogPanel.class, "WorkLogPanel.startDateHint.text")); // NOI18N

        adjustEstimateLabel.setText(org.openide.util.NbBundle.getMessage(WorkLogPanel.class, "WorkLogPanel.adjustEstimateLabel.text")); // NOI18N

        adjustEstimateGroup.add(autoAdjustChoice);
        autoAdjustChoice.setSelected(true);
        autoAdjustChoice.setText(org.openide.util.NbBundle.getMessage(WorkLogPanel.class, "WorkLogPanel.autoAdjustChoice.text")); // NOI18N

        adjustEstimateGroup.add(leaveEstimateChoice);
        leaveEstimateChoice.setText(org.openide.util.NbBundle.getMessage(WorkLogPanel.class, "WorkLogPanel.leaveEstimateChoice.text")); // NOI18N

        adjustEstimateGroup.add(setEstimatedTimeChoice);
        setEstimatedTimeChoice.setText(org.openide.util.NbBundle.getMessage(WorkLogPanel.class, "WorkLogPanel.setEstimatedTimeChoice.text")); // NOI18N

        setEstimatedTimeField.setColumns(8);

        adjustEstimateGroup.add(reduceEstimatedTimeChoice);
        reduceEstimatedTimeChoice.setText(org.openide.util.NbBundle.getMessage(WorkLogPanel.class, "WorkLogPanel.reduceEstimatedTimeChoice.text")); // NOI18N

        reduceEstimatedTimeField.setColumns(8);

        workDescriptionLabel.setText(org.openide.util.NbBundle.getMessage(WorkLogPanel.class, "WorkLogPanel.workDescriptionLabel.text")); // NOI18N

        workDescriptionArea.setRows(5);
        workDescriptionScrollPane.setViewportView(workDescriptionArea);

        messagePanel.setLayout(new javax.swing.BoxLayout(messagePanel, javax.swing.BoxLayout.PAGE_AXIS));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(messagePanel, javax.swing.GroupLayout.DEFAULT_SIZE, 649, Short.MAX_VALUE)
                    .addComponent(hintLabel)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(timeSpentHint, javax.swing.GroupLayout.Alignment.LEADING, 0, 0, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addComponent(timeSpentLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(timeSpentField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(dummyLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(startDateLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(startDateField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(startDateHint)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(adjustEstimateLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(leaveEstimateChoice, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(autoAdjustChoice)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(setEstimatedTimeChoice)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(setEstimatedTimeField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(reduceEstimatedTimeChoice)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(reduceEstimatedTimeField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(workDescriptionLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(workDescriptionScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 508, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(hintLabel)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(timeSpentLabel)
                    .addComponent(timeSpentField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(startDateLabel)
                    .addComponent(startDateField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(dummyLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(timeSpentHint, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(startDateHint))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(adjustEstimateLabel)
                    .addComponent(autoAdjustChoice))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(leaveEstimateChoice, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(setEstimatedTimeChoice)
                    .addComponent(setEstimatedTimeField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(reduceEstimatedTimeChoice)
                    .addComponent(reduceEstimatedTimeField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(workDescriptionLabel)
                    .addComponent(workDescriptionScrollPane))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(messagePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup adjustEstimateGroup;
    private javax.swing.JLabel adjustEstimateLabel;
    private javax.swing.JRadioButton autoAdjustChoice;
    private javax.swing.JButton discardButton;
    private javax.swing.JLabel dummyLabel;
    private javax.swing.JLabel hintLabel;
    private javax.swing.JRadioButton leaveEstimateChoice;
    private javax.swing.JPanel messagePanel;
    private javax.swing.JRadioButton reduceEstimatedTimeChoice;
    private javax.swing.JTextField reduceEstimatedTimeField;
    private javax.swing.JRadioButton setEstimatedTimeChoice;
    private javax.swing.JTextField setEstimatedTimeField;
    private javax.swing.JFormattedTextField startDateField;
    private javax.swing.JLabel startDateHint;
    private javax.swing.JLabel startDateLabel;
    private javax.swing.JButton submitButton;
    private javax.swing.JTextField timeSpentField;
    private javax.swing.JLabel timeSpentHint;
    private javax.swing.JLabel timeSpentLabel;
    private javax.swing.JTextArea workDescriptionArea;
    private javax.swing.JLabel workDescriptionLabel;
    private javax.swing.JScrollPane workDescriptionScrollPane;
    // End of variables declaration//GEN-END:variables

    class WorkLogFormatListener implements DocumentListener {

        @Override
        public void insertUpdate(DocumentEvent e) {
            changedUpdate(e);
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            changedUpdate(e);
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            Document document = e.getDocument();
            if (document == timeSpentField.getDocument()) {
                int timeSpent = getTimeSpent();
                if ((timeSpent < 0) != timeSpentFormat) {
                    timeSpentFormat = (timeSpent < 0);
                    updateMessagePanel();
                }
                if ((timeSpent == 0) != timeSpentZero) {
                    timeSpentZero = (timeSpent == 0);
                    updateMessagePanel();
                }
            } else if (document == reduceEstimatedTimeField.getDocument()) {
                checkReduceEstimateTime();
            } else if (document == setEstimatedTimeField.getDocument()) {
                checkSetEstimateTime();
            }
        }

    }

    class EstimateAdjustmentTypeListener implements ChangeListener {

        @Override
        public void stateChanged(ChangeEvent e) {
            if (reduceEstimatedTimeChoice.isSelected()) {
                checkReduceEstimateTime();
            } else if (reduceEstimateFormat) {
                reduceEstimateFormat = false;
                updateMessagePanel();
            }
            if (setEstimatedTimeChoice.isSelected()) {
                checkSetEstimateTime();
            } else if (setEstimateFormat) {
                setEstimateFormat = false;
                updateMessagePanel();
            }
        }

    }

}
