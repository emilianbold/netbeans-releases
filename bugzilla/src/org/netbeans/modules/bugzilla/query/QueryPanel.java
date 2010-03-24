/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.bugzilla.query;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.plaf.basic.BasicTreeUI;
import org.netbeans.modules.bugtracking.issuetable.Filter;
import org.netbeans.modules.bugzilla.query.QueryParameter.ParameterValueCellRenderer;

/**
 *
 * @author Tomas Stupka, Jan Stola
 */
public class QueryPanel extends javax.swing.JPanel implements FocusListener {

    final ExpandablePanel byText;
    final ExpandablePanel byDetails;
    final ExpandablePanel byPeople;
    final ExpandablePanel byLastChange;
    private QueryController controller;
    private static final Color ERROR_COLOR = new Color(153,0,0);
    private Color defaultTextColor;

    /** Creates new form QueryPanel */
    public QueryPanel(JComponent tableComponent, QueryController controller) {
        initComponents();
        this.controller = controller;

        Font f = new JLabel().getFont();
        int s = f.getSize();
        nameLabel.setFont(jLabel1.getFont().deriveFont(s * 1.7f));
        defaultTextColor = noContentLabel.getForeground();

        setBoldFont(byDetailsLabel);
        setBoldFont(byLastChangeLabel);
        setBoldFont(byPeopleLabel);
        setBoldFont(byTextLabel);

        tablePanel.add(tableComponent);

        JTree tv = new JTree();
        BasicTreeUI tvui = (BasicTreeUI) tv.getUI();
        Icon ei = tvui.getExpandedIcon();
        Icon ci = tvui.getCollapsedIcon();

        byTextContainer.add(byTextPanel);
        byDetailsContainer.add(byDetailsPanel);
        byPeopleContainer.add(byPeoplePanel);
        byLastChangeContainer.add(byLastChangePanel);

        byText = new ExpandablePanel(byTextLabel, byTextContainer, ei, ci);
        byDetails = new ExpandablePanel(byDetailsLabel, byDetailsContainer, ei, ci);
        byPeople = new ExpandablePanel(byPeopleLabel, byPeopleContainer, ei, ci);
        byLastChange = new ExpandablePanel(byLastChangeLabel, byLastChangeContainer, ei, ci);

        byText.expand();
        byDetails.expand();
        byPeople.colapse();
        byLastChange.colapse();

        urlPanel.setVisible(false);
        queryHeaderPanel.setVisible(false);
        tableFieldsPanel.setVisible(false);
        saveChangesButton.setVisible(false);
        cancelChangesButton.setVisible(false);
        filterComboBox.setVisible(false);
        filterLabel.setVisible(false);
        refreshCheckBox.setVisible(false);
        noContentPanel.setVisible(false);

        bugAssigneeCheckBox.setOpaque(false);
        reporterCheckBox.setOpaque(false);
        ccCheckBox.setOpaque(false);
        commenterCheckBox.setOpaque(false);
        refreshCheckBox.setOpaque(false);

        summaryComboBox.setModel(new DefaultComboBoxModel());
        commentComboBox.setModel(new DefaultComboBoxModel());
        keywordsComboBox.setModel(new DefaultComboBoxModel());
        peopleComboBox.setModel(new DefaultComboBoxModel());
        
        summaryComboBox.setRenderer(new ParameterValueCellRenderer());
        commentComboBox.setRenderer(new ParameterValueCellRenderer());
        whiteboardComboBox.setRenderer(new ParameterValueCellRenderer());
        keywordsComboBox.setRenderer(new ParameterValueCellRenderer());
        peopleComboBox.setRenderer(new ParameterValueCellRenderer());
        severityList.setCellRenderer(new ParameterValueCellRenderer());
        issueTypeList.setCellRenderer(new ParameterValueCellRenderer());
        productList.setCellRenderer(new ParameterValueCellRenderer());
        componentList.setCellRenderer(new ParameterValueCellRenderer());
        versionList.setCellRenderer(new ParameterValueCellRenderer());
        statusList.setCellRenderer(new ParameterValueCellRenderer());
        resolutionList.setCellRenderer(new ParameterValueCellRenderer());
        priorityList.setCellRenderer(new QueryParameter.PriorityRenderer());
        changedList.setCellRenderer(new ParameterValueCellRenderer());
        tmList.setCellRenderer(new ParameterValueCellRenderer());

        filterComboBox.setRenderer(new FilterCellRenderer());

        bugAssigneeCheckBox.addFocusListener(this);
        cancelChangesButton.addFocusListener(this);
        ccCheckBox.addFocusListener(this);
        changedFromTextField.addFocusListener(this);
        changedList.addFocusListener(this);
        changedToTextField.addFocusListener(this);
        commentComboBox.addFocusListener(this);
        commentTextField.addFocusListener(this);
        whiteboardComboBox.addFocusListener(this);
        whiteboardTextField.addFocusListener(this);
        commenterCheckBox.addFocusListener(this);
        componentList.addFocusListener(this);
        filterComboBox.addFocusListener(this);
        gotoIssueButton.addFocusListener(this);
        idTextField.addFocusListener(this);
        keywordsButton.addFocusListener(this);
        keywordsComboBox.addFocusListener(this);
        keywordsTextField.addFocusListener(this);
        modifyButton.addFocusListener(this);
        newValueTextField.addFocusListener(this);
        peopleComboBox.addFocusListener(this);
        peopleTextField.addFocusListener(this);
        priorityList.addFocusListener(this);
        productList.addFocusListener(this);
        seenButton.addFocusListener(this);
        refreshCheckBox.addFocusListener(this);
        removeButton.addFocusListener(this);
        reporterCheckBox.addFocusListener(this);
        resolutionList.addFocusListener(this);
        saveButton.addFocusListener(this);
        saveChangesButton.addFocusListener(this);
        searchButton.addFocusListener(this);
        refreshButton.addFocusListener(this);
        severityList.addFocusListener(this);
        issueTypeList.addFocusListener(this);
        statusList.addFocusListener(this);
        summaryComboBox.addFocusListener(this);
        summaryTextField.addFocusListener(this);
        tablePanel.addFocusListener(this);
        tableSummaryLabel.addFocusListener(this);
        urlTextField.addFocusListener(this);
        urlToggleButton.addFocusListener(this);
        versionList.addFocusListener(this);
        webButton.addFocusListener(this);
        refreshConfigurationButton.addFocusListener(this);

        validate();
        repaint();
    }

    void setQueryRunning(boolean running) {
        modifyButton.setEnabled(!running);
        seenButton.setEnabled(!running);
        removeButton.setEnabled(!running);
        refreshButton.setEnabled(!running);
        filterLabel.setEnabled(!running);
        filterComboBox.setEnabled(!running);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        byLastChangePanel = new javax.swing.JPanel();
        changedLabel = new javax.swing.JLabel();
        changedAndLabel = new javax.swing.JLabel();
        changedHintLabel = new javax.swing.JLabel();
        changedWhereLabel = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        changedBlaBlaLabel = new javax.swing.JLabel();
        byPeoplePanel = new javax.swing.JPanel();
        byTextPanel = new javax.swing.JPanel();
        tableFieldsPanel = new javax.swing.JPanel();
        tableHeaderPanel = new javax.swing.JPanel();
        filterLabel = new javax.swing.JLabel();
        criteriaPanel = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        separatorLabel1 = new javax.swing.JLabel();
        separatorLabel2 = new javax.swing.JLabel();
        separatorLabel3 = new javax.swing.JLabel();
        queryHeaderPanel = new javax.swing.JPanel();
        lastRefreshLabel = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        noContentPanel = new javax.swing.JPanel();
        noContentLabel = new javax.swing.JLabel();

        byLastChangePanel.setBackground(javax.swing.UIManager.getDefaults().getColor("TextArea.background"));

        changedLabel.setLabelFor(changedFromTextField);
        org.openide.awt.Mnemonics.setLocalizedText(changedLabel, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.changedLabel.text_1")); // NOI18N

        changedFromTextField.setColumns(8);
        changedFromTextField.setText(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.changedFromTextField.text")); // NOI18N

        changedAndLabel.setLabelFor(changedToTextField);
        org.openide.awt.Mnemonics.setLocalizedText(changedAndLabel, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.changedAndLabel.text")); // NOI18N

        changedToTextField.setColumns(8);
        changedToTextField.setText(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.changedToTextField.text")); // NOI18N

        changedHintLabel.setForeground(javax.swing.UIManager.getDefaults().getColor("Label.disabledForeground"));
        org.openide.awt.Mnemonics.setLocalizedText(changedHintLabel, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.changedHintLabel.text")); // NOI18N

        changedWhereLabel.setLabelFor(changedList);
        org.openide.awt.Mnemonics.setLocalizedText(changedWhereLabel, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.changedWhereLabel.text")); // NOI18N

        jScrollPane1.setViewportView(changedList);
        changedList.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.changedList.AccessibleContext.accessibleDescription")); // NOI18N

        changedBlaBlaLabel.setLabelFor(newValueTextField);
        org.openide.awt.Mnemonics.setLocalizedText(changedBlaBlaLabel, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.changedBlaBlaLabel.text")); // NOI18N

        newValueTextField.setColumns(20);

        org.jdesktop.layout.GroupLayout byLastChangePanelLayout = new org.jdesktop.layout.GroupLayout(byLastChangePanel);
        byLastChangePanel.setLayout(byLastChangePanelLayout);
        byLastChangePanelLayout.setHorizontalGroup(
            byLastChangePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(byLastChangePanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(byLastChangePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(changedWhereLabel)
                    .add(changedLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(byLastChangePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(changedHintLabel)
                    .add(byLastChangePanelLayout.createSequentialGroup()
                        .add(changedFromTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(changedAndLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(changedToTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(byLastChangePanelLayout.createSequentialGroup()
                        .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(changedBlaBlaLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(newValueTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        byLastChangePanelLayout.setVerticalGroup(
            byLastChangePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(byLastChangePanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(byLastChangePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(changedLabel)
                    .add(changedAndLabel)
                    .add(changedToTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(changedFromTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(4, 4, 4)
                .add(changedHintLabel)
                .add(18, 18, 18)
                .add(byLastChangePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(changedWhereLabel)
                    .add(byLastChangePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(changedBlaBlaLabel)
                        .add(newValueTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 71, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(25, Short.MAX_VALUE))
        );

        changedFromTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.changedFromTextField.AccessibleContext.accessibleDescription")); // NOI18N
        changedToTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.changedToTextField.AccessibleContext.accessibleDescription")); // NOI18N
        newValueTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.newValueTextField.AccessibleContext.accessibleDescription")); // NOI18N

        byPeoplePanel.setBackground(javax.swing.UIManager.getDefaults().getColor("TextArea.background"));

        org.openide.awt.Mnemonics.setLocalizedText(peopleLabel, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.peopleLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(bugAssigneeCheckBox, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.bugAssigneeCheckBox.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(reporterCheckBox, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.reporterCheckBox.text")); // NOI18N
        reporterCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                reporterCheckBoxActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(ccCheckBox, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.ccCheckBox.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(commenterCheckBox, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.commenterCheckBox.text")); // NOI18N

        peopleComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        peopleTextField.setColumns(17);

        org.jdesktop.layout.GroupLayout byPeoplePanelLayout = new org.jdesktop.layout.GroupLayout(byPeoplePanel);
        byPeoplePanel.setLayout(byPeoplePanelLayout);
        byPeoplePanelLayout.setHorizontalGroup(
            byPeoplePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(byPeoplePanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(peopleLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(byPeoplePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(commenterCheckBox)
                    .add(ccCheckBox)
                    .add(byPeoplePanelLayout.createSequentialGroup()
                        .add(bugAssigneeCheckBox)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(peopleComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(7, 7, 7)
                        .add(peopleTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(reporterCheckBox))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        byPeoplePanelLayout.setVerticalGroup(
            byPeoplePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(byPeoplePanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(byPeoplePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(byPeoplePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(peopleComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(peopleTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(byPeoplePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(bugAssigneeCheckBox)
                        .add(peopleLabel)))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(reporterCheckBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(ccCheckBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(commenterCheckBox)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        bugAssigneeCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.bugAssigneeCheckBox.AccessibleContext.accessibleDescription")); // NOI18N
        reporterCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.reporterCheckBox.AccessibleContext.accessibleDescription")); // NOI18N
        ccCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.ccCheckBox.AccessibleContext.accessibleDescription")); // NOI18N
        commenterCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.commenterCheckBox.AccessibleContext.accessibleDescription")); // NOI18N
        peopleComboBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.peopleComboBox.AccessibleContext.accessibleName")); // NOI18N
        peopleComboBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.peopleComboBox.AccessibleContext.accessibleDescription")); // NOI18N
        peopleTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.peopleTextField.AccessibleContext.accessibleName")); // NOI18N
        peopleTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.peopleTextField.AccessibleContext.accessibleDescription")); // NOI18N

        byDetailsPanel.setBackground(javax.swing.UIManager.getDefaults().getColor("TextArea.background"));

        productLabel.setFont(new java.awt.Font("Lucida Grande", 1, 13));
        productLabel.setLabelFor(productList);
        org.openide.awt.Mnemonics.setLocalizedText(productLabel, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.productLabel.text")); // NOI18N

        versionLabel.setFont(new java.awt.Font("Lucida Grande", 1, 13));
        versionLabel.setLabelFor(versionList);
        org.openide.awt.Mnemonics.setLocalizedText(versionLabel, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.versionLabel.text")); // NOI18N

        jScrollPane2.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        versionList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        versionList.setMinimumSize(new java.awt.Dimension(100, 2));
        versionList.setVisibleRowCount(6);
        jScrollPane2.setViewportView(versionList);
        versionList.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.versionList.AccessibleContext.accessibleDescription")); // NOI18N

        statusLabel.setFont(new java.awt.Font("Lucida Grande", 1, 13));
        statusLabel.setLabelFor(statusList);
        org.openide.awt.Mnemonics.setLocalizedText(statusLabel, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.statusLabel.text")); // NOI18N

        jScrollPane3.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        statusList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        statusList.setMinimumSize(new java.awt.Dimension(100, 2));
        statusList.setVisibleRowCount(6);
        jScrollPane3.setViewportView(statusList);
        statusList.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.statusList.AccessibleContext.accessibleDescription")); // NOI18N

        resolutionLabel.setFont(new java.awt.Font("Lucida Grande", 1, 13));
        resolutionLabel.setLabelFor(resolutionList);
        org.openide.awt.Mnemonics.setLocalizedText(resolutionLabel, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.resolutionLabel.text")); // NOI18N

        jScrollPane4.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        priorityList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        priorityList.setMinimumSize(new java.awt.Dimension(100, 2));
        priorityList.setVisibleRowCount(6);
        jScrollPane4.setViewportView(priorityList);
        priorityList.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.priorityList.AccessibleContext.accessibleDescription")); // NOI18N

        priorityLabel.setFont(new java.awt.Font("Lucida Grande", 1, 13));
        priorityLabel.setLabelFor(priorityList);
        org.openide.awt.Mnemonics.setLocalizedText(priorityLabel, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.priorityLabel.text")); // NOI18N

        jScrollPane5.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        resolutionList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        resolutionList.setMinimumSize(new java.awt.Dimension(100, 2));
        resolutionList.setVisibleRowCount(6);
        jScrollPane5.setViewportView(resolutionList);
        resolutionList.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.resolutionList.AccessibleContext.accessibleDescription")); // NOI18N

        componentLabel.setFont(new java.awt.Font("Lucida Grande", 1, 13));
        componentLabel.setLabelFor(componentList);
        org.openide.awt.Mnemonics.setLocalizedText(componentLabel, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.componentLabel.text")); // NOI18N

        jScrollPane6.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        componentList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        componentList.setMinimumSize(new java.awt.Dimension(100, 2));
        componentList.setVisibleRowCount(6);
        jScrollPane6.setViewportView(componentList);
        componentList.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.componentList.AccessibleContext.accessibleDescription")); // NOI18N

        jScrollPane7.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        productList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        productList.setMaximumSize(new java.awt.Dimension(100, 2));
        productList.setVisibleRowCount(6);
        jScrollPane7.setViewportView(productList);
        productList.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.productList.AccessibleContext.accessibleDescription")); // NOI18N

        severityLabel.setFont(new java.awt.Font("Lucida Grande", 1, 13));
        severityLabel.setLabelFor(severityList);
        org.openide.awt.Mnemonics.setLocalizedText(severityLabel, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.severityLabel.text")); // NOI18N

        severityScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        severityList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        severityList.setMinimumSize(new java.awt.Dimension(100, 2));
        severityList.setVisibleRowCount(6);
        severityScrollPane.setViewportView(severityList);
        severityList.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.severityList.AccessibleContext.accessibleDescription")); // NOI18N

        issueTypeLabel.setFont(new java.awt.Font("Lucida Grande", 1, 13));
        issueTypeLabel.setLabelFor(severityList);
        org.openide.awt.Mnemonics.setLocalizedText(issueTypeLabel, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.issueTypeLabel.text")); // NOI18N

        issueTypeScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        issueTypeList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        issueTypeList.setMinimumSize(new java.awt.Dimension(100, 2));
        issueTypeList.setVisibleRowCount(6);
        issueTypeScrollPane.setViewportView(issueTypeList);

        tmLabel.setFont(new java.awt.Font("Lucida Grande", 1, 13));
        tmLabel.setLabelFor(severityList);
        org.openide.awt.Mnemonics.setLocalizedText(tmLabel, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.tmLabel.text")); // NOI18N

        tmScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        tmList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        tmList.setMinimumSize(new java.awt.Dimension(100, 2));
        tmList.setVisibleRowCount(6);
        tmScrollPane.setViewportView(tmList);

        org.jdesktop.layout.GroupLayout byDetailsPanelLayout = new org.jdesktop.layout.GroupLayout(byDetailsPanel);
        byDetailsPanel.setLayout(byDetailsPanelLayout);
        byDetailsPanelLayout.setHorizontalGroup(
            byDetailsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(byDetailsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(byDetailsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(productLabel)
                    .add(jScrollPane7, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(byDetailsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(componentLabel)
                    .add(jScrollPane6, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(byDetailsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(versionLabel)
                    .add(jScrollPane2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(18, 18, 18)
                .add(byDetailsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(statusLabel)
                    .add(jScrollPane3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(byDetailsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(resolutionLabel)
                    .add(jScrollPane5, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(byDetailsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(jScrollPane4)
                    .add(priorityLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(byDetailsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(severityLabel)
                    .add(severityScrollPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(byDetailsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(issueTypeLabel)
                    .add(issueTypeScrollPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(byDetailsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(tmLabel)
                    .add(tmScrollPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        byDetailsPanelLayout.setVerticalGroup(
            byDetailsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(byDetailsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(byDetailsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(byDetailsPanelLayout.createSequentialGroup()
                        .add(tmLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(tmScrollPane))
                    .add(byDetailsPanelLayout.createSequentialGroup()
                        .add(issueTypeLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(issueTypeScrollPane))
                    .add(byDetailsPanelLayout.createSequentialGroup()
                        .add(byDetailsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(productLabel)
                            .add(componentLabel)
                            .add(versionLabel)
                            .add(statusLabel)
                            .add(resolutionLabel)
                            .add(priorityLabel)
                            .add(severityLabel))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(byDetailsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jScrollPane4)
                            .add(jScrollPane7)
                            .add(jScrollPane6)
                            .add(jScrollPane2)
                            .add(jScrollPane3)
                            .add(jScrollPane5)
                            .add(severityScrollPane))))
                .addContainerGap())
        );

        byTextPanel.setBackground(javax.swing.UIManager.getDefaults().getColor("TextArea.background"));

        summaryLabel.setLabelFor(summaryComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(summaryLabel, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.summaryLabel.text_1")); // NOI18N

        commentLabel.setLabelFor(commentComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(commentLabel, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.commentLabel.text")); // NOI18N

        keywordsLabel.setLabelFor(keywordsComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(keywordsLabel, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.keywordsLabel.text")); // NOI18N

        summaryComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        commentComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        keywordsComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        summaryTextField.setColumns(30);

        commentTextField.setColumns(30);

        keywordsTextField.setColumns(30);

        org.openide.awt.Mnemonics.setLocalizedText(keywordsButton, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.keywordsButton.text")); // NOI18N
        keywordsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                keywordsButtonActionPerformed(evt);
            }
        });

        whiteboardLabel.setLabelFor(commentComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(whiteboardLabel, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.whiteboardLabel.text")); // NOI18N

        whiteboardComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        whiteboardTextField.setColumns(30);

        org.jdesktop.layout.GroupLayout byTextPanelLayout = new org.jdesktop.layout.GroupLayout(byTextPanel);
        byTextPanel.setLayout(byTextPanelLayout);
        byTextPanelLayout.setHorizontalGroup(
            byTextPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(byTextPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(byTextPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(summaryLabel)
                    .add(commentLabel)
                    .add(whiteboardLabel)
                    .add(keywordsLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(byTextPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(commentComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(summaryComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(whiteboardComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(keywordsComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(byTextPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(byTextPanelLayout.createSequentialGroup()
                        .add(byTextPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(commentTextField)
                            .add(summaryTextField))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED))
                    .add(byTextPanelLayout.createSequentialGroup()
                        .add(byTextPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(keywordsTextField)
                            .add(whiteboardTextField))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(keywordsButton)))
                .addContainerGap())
        );

        byTextPanelLayout.linkSize(new java.awt.Component[] {commentComboBox, keywordsComboBox, summaryComboBox}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        byTextPanelLayout.setVerticalGroup(
            byTextPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(byTextPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(byTextPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(summaryLabel)
                    .add(summaryComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(summaryTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(byTextPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(commentLabel)
                    .add(commentTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(commentComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(byTextPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(whiteboardLabel)
                    .add(whiteboardTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(whiteboardComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(byTextPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(keywordsLabel)
                    .add(keywordsComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(keywordsTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(keywordsButton))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        summaryComboBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.summaryComboBox.AccessibleContext.accessibleDescription")); // NOI18N
        commentComboBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.commentComboBox.AccessibleContext.accessibleDescription")); // NOI18N
        keywordsComboBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.keywordsComboBox.AccessibleContext.accessibleDescription")); // NOI18N
        summaryTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.summaryTextField.AccessibleContext.accessibleName")); // NOI18N
        summaryTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.summaryTextField.AccessibleContext.accessibleDescription")); // NOI18N
        commentTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.commentTextField.AccessibleContext.accessibleName")); // NOI18N
        commentTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.commentTextField.AccessibleContext.accessibleDescription")); // NOI18N
        keywordsTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.keywordsTextField.AccessibleContext.accessibleName")); // NOI18N
        keywordsTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.keywordsTextField.AccessibleContext.accessibleDescription")); // NOI18N
        keywordsButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.keywordsButton.AccessibleContext.accessibleDescription")); // NOI18N

        setBackground(javax.swing.UIManager.getDefaults().getColor("TextArea.background"));

        tableFieldsPanel.setBackground(javax.swing.UIManager.getDefaults().getColor("EditorPane.background"));

        tablePanel.setBackground(new java.awt.Color(224, 224, 224));
        tablePanel.setMinimumSize(new java.awt.Dimension(100, 350));
        tablePanel.setLayout(new java.awt.BorderLayout());

        tableHeaderPanel.setBackground(javax.swing.UIManager.getDefaults().getColor("EditorPane.background"));

        org.openide.awt.Mnemonics.setLocalizedText(tableSummaryLabel, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.tableSummaryLabel.text_1")); // NOI18N

        filterComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        filterLabel.setLabelFor(filterComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(filterLabel, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.filterLabel.text_1")); // NOI18N

        org.jdesktop.layout.GroupLayout tableHeaderPanelLayout = new org.jdesktop.layout.GroupLayout(tableHeaderPanel);
        tableHeaderPanel.setLayout(tableHeaderPanelLayout);
        tableHeaderPanelLayout.setHorizontalGroup(
            tableHeaderPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(tableHeaderPanelLayout.createSequentialGroup()
                .add(tableSummaryLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 673, Short.MAX_VALUE)
                .add(filterLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(filterComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );
        tableHeaderPanelLayout.setVerticalGroup(
            tableHeaderPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(tableHeaderPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                .add(tableSummaryLabel)
                .add(filterComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(filterLabel))
        );

        filterComboBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.filterComboBox.AccessibleContext.accessibleDescription")); // NOI18N

        org.jdesktop.layout.GroupLayout tableFieldsPanelLayout = new org.jdesktop.layout.GroupLayout(tableFieldsPanel);
        tableFieldsPanel.setLayout(tableFieldsPanelLayout);
        tableFieldsPanelLayout.setHorizontalGroup(
            tableFieldsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(tableFieldsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(tableFieldsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(tablePanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 855, Short.MAX_VALUE)
                    .add(tableHeaderPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        tableFieldsPanelLayout.setVerticalGroup(
            tableFieldsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(tableFieldsPanelLayout.createSequentialGroup()
                .add(tableHeaderPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(tablePanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 350, Short.MAX_VALUE)
                .addContainerGap())
        );

        searchPanel.setBackground(javax.swing.UIManager.getDefaults().getColor("EditorPane.background"));

        org.openide.awt.Mnemonics.setLocalizedText(webButton, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.webButton.text")); // NOI18N
        webButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                webButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(urlToggleButton, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.urlToggleButton.textUrl")); // NOI18N
        urlToggleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                urlToggleButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(searchButton, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.searchButton.text")); // NOI18N

        criteriaPanel.setBackground(new java.awt.Color(224, 224, 224));

        org.openide.awt.Mnemonics.setLocalizedText(byTextLabel, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.byTextLabel.text_1")); // NOI18N

        byTextContainer.setLayout(new java.awt.BorderLayout());

        byDetailsContainer.setLayout(new java.awt.BorderLayout());

        org.openide.awt.Mnemonics.setLocalizedText(byDetailsLabel, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.byDetailsLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(byPeopleLabel, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.byPeopleLabel.text")); // NOI18N

        byPeopleContainer.setLayout(new java.awt.BorderLayout());

        org.openide.awt.Mnemonics.setLocalizedText(byLastChangeLabel, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.byLastChangeLabel.text")); // NOI18N

        byLastChangeContainer.setLayout(new java.awt.BorderLayout());

        urlPanel.setBackground(new java.awt.Color(224, 224, 224));

        jLabel2.setLabelFor(urlTextField);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.jLabel2.text_1")); // NOI18N

        urlTextField.setColumns(50);

        org.jdesktop.layout.GroupLayout urlPanelLayout = new org.jdesktop.layout.GroupLayout(urlPanel);
        urlPanel.setLayout(urlPanelLayout);
        urlPanelLayout.setHorizontalGroup(
            urlPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(urlPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel2)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(urlTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 782, Short.MAX_VALUE)
                .addContainerGap())
        );
        urlPanelLayout.setVerticalGroup(
            urlPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(urlPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                .add(urlTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(jLabel2))
        );

        urlTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.urlTextField.AccessibleContext.accessibleDescription")); // NOI18N

        org.jdesktop.layout.GroupLayout criteriaPanelLayout = new org.jdesktop.layout.GroupLayout(criteriaPanel);
        criteriaPanel.setLayout(criteriaPanelLayout);
        criteriaPanelLayout.setHorizontalGroup(
            criteriaPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(byLastChangeContainer, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 895, Short.MAX_VALUE)
            .add(urlPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .add(byTextContainer, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 895, Short.MAX_VALUE)
            .add(criteriaPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(byTextLabel))
            .add(criteriaPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(byDetailsLabel))
            .add(byDetailsContainer, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 895, Short.MAX_VALUE)
            .add(criteriaPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(byPeopleLabel))
            .add(byPeopleContainer, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 895, Short.MAX_VALUE)
            .add(criteriaPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(byLastChangeLabel))
        );
        criteriaPanelLayout.setVerticalGroup(
            criteriaPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(criteriaPanelLayout.createSequentialGroup()
                .add(byTextLabel)
                .add(0, 0, 0)
                .add(byTextContainer, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(0, 0, 0)
                .add(byDetailsLabel)
                .add(0, 0, 0)
                .add(byDetailsContainer, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(0, 0, 0)
                .add(byPeopleLabel)
                .add(0, 0, 0)
                .add(byPeopleContainer, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(0, 0, 0)
                .add(byLastChangeLabel)
                .add(0, 0, 0)
                .add(byLastChangeContainer, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(urlPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );

        org.openide.awt.Mnemonics.setLocalizedText(cancelChangesButton, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.cancelChangesButton.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(saveChangesButton, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.saveChangesButton.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(saveButton, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.saveButton.text")); // NOI18N
        saveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveButtonActionPerformed(evt);
            }
        });

        gotoPanel.setBackground(new java.awt.Color(224, 224, 224));

        jLabel1.setLabelFor(idTextField);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.jLabel1.text_1")); // NOI18N

        idTextField.setColumns(6);

        org.openide.awt.Mnemonics.setLocalizedText(gotoIssueButton, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.gotoIssueButton.text")); // NOI18N

        org.jdesktop.layout.GroupLayout gotoPanelLayout = new org.jdesktop.layout.GroupLayout(gotoPanel);
        gotoPanel.setLayout(gotoPanelLayout);
        gotoPanelLayout.setHorizontalGroup(
            gotoPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(gotoPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(idTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(gotoIssueButton)
                .addContainerGap(597, Short.MAX_VALUE))
        );
        gotoPanelLayout.setVerticalGroup(
            gotoPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(gotoPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                .add(gotoIssueButton)
                .add(jLabel1))
            .add(idTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
        );

        idTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.idTextField.AccessibleContext.accessibleDescription")); // NOI18N
        gotoIssueButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.gotoIssueButton.AccessibleContext.accessibleDescription")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(separatorLabel1, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.separatorLabel1.text")); // NOI18N
        separatorLabel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        org.openide.awt.Mnemonics.setLocalizedText(separatorLabel2, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.separatorLabel2.text")); // NOI18N
        separatorLabel2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        org.openide.awt.Mnemonics.setLocalizedText(refreshConfigurationButton, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.refreshConfigurationButton.text")); // NOI18N
        refreshConfigurationButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshConfigurationButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(separatorLabel3, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.separatorLabel3.text")); // NOI18N
        separatorLabel3.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        org.jdesktop.layout.GroupLayout searchPanelLayout = new org.jdesktop.layout.GroupLayout(searchPanel);
        searchPanel.setLayout(searchPanelLayout);
        searchPanelLayout.setHorizontalGroup(
            searchPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(gotoPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, searchPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(searchButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(saveChangesButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(cancelChangesButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(saveButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(separatorLabel1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(webButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(separatorLabel2)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(urlToggleButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(separatorLabel3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(refreshConfigurationButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
            .add(criteriaPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        searchPanelLayout.setVerticalGroup(
            searchPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(searchPanelLayout.createSequentialGroup()
                .add(gotoPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(criteriaPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 8, Short.MAX_VALUE)
                .add(searchPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(searchPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(searchButton)
                        .add(cancelChangesButton)
                        .add(saveChangesButton))
                    .add(searchPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(refreshConfigurationButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(urlToggleButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(webButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(saveButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(separatorLabel1)
                        .add(separatorLabel2)
                        .add(separatorLabel3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 18, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        searchPanelLayout.linkSize(new java.awt.Component[] {saveButton, separatorLabel1, separatorLabel2, urlToggleButton, webButton}, org.jdesktop.layout.GroupLayout.VERTICAL);

        webButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.webButton.AccessibleContext.accessibleDescription")); // NOI18N
        urlToggleButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.urlToggleButton.AccessibleContext.accessibleDescription")); // NOI18N
        searchButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.searchButton.AccessibleContext.accessibleDescription")); // NOI18N
        cancelChangesButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.cancelChangesButton.AccessibleContext.accessibleDescription")); // NOI18N
        saveChangesButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.saveChangesButton.AccessibleContext.accessibleDescription")); // NOI18N
        saveButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.saveButton.AccessibleContext.accessibleDescription")); // NOI18N
        refreshConfigurationButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.refreshConfigurationButton.AccessibleContext.accessibleDescription")); // NOI18N

        queryHeaderPanel.setBackground(javax.swing.UIManager.getDefaults().getColor("EditorPane.background"));

        org.openide.awt.Mnemonics.setLocalizedText(seenButton, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.seenButton.text")); // NOI18N
        seenButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                seenButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(lastRefreshLabel, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.lastRefreshLabel.text_1")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(removeButton, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.removeButton.text")); // NOI18N
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(lastRefreshDateLabel, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.lastRefreshDateLabel.text_1")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(refreshButton, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.refreshButton.text")); // NOI18N
        refreshButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(nameLabel, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.nameLabel.text_1")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(modifyButton, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.modifyButton.text")); // NOI18N
        modifyButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                modifyButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.jLabel4.text_1")); // NOI18N
        jLabel4.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        org.openide.awt.Mnemonics.setLocalizedText(jLabel5, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.jLabel5.text")); // NOI18N
        jLabel5.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        org.openide.awt.Mnemonics.setLocalizedText(jLabel6, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.jLabel6.text")); // NOI18N
        jLabel6.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        org.openide.awt.Mnemonics.setLocalizedText(refreshCheckBox, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.refreshCheckBox.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel7, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.jLabel7.text")); // NOI18N
        jLabel7.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        org.openide.awt.Mnemonics.setLocalizedText(findIssuesButton, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.findIssuesButton.text")); // NOI18N
        findIssuesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                findIssuesButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel8, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.jLabel8.text")); // NOI18N
        jLabel8.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        org.openide.awt.Mnemonics.setLocalizedText(cloneQueryButton, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.cloneQueryButton.text")); // NOI18N
        cloneQueryButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cloneQueryButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout queryHeaderPanelLayout = new org.jdesktop.layout.GroupLayout(queryHeaderPanel);
        queryHeaderPanel.setLayout(queryHeaderPanelLayout);
        queryHeaderPanelLayout.setHorizontalGroup(
            queryHeaderPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(queryHeaderPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(queryHeaderPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(queryHeaderPanelLayout.createSequentialGroup()
                        .add(nameLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 501, Short.MAX_VALUE)
                        .add(refreshCheckBox)
                        .add(18, 18, 18)
                        .add(lastRefreshLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(lastRefreshDateLabel))
                    .add(queryHeaderPanelLayout.createSequentialGroup()
                        .add(seenButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jLabel4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(modifyButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(7, 7, 7)
                        .add(jLabel5, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(refreshButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(5, 5, 5)
                        .add(jLabel6)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(removeButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jLabel7)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(findIssuesButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(6, 6, 6)
                        .add(jLabel8)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(cloneQueryButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        queryHeaderPanelLayout.setVerticalGroup(
            queryHeaderPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(queryHeaderPanelLayout.createSequentialGroup()
                .add(queryHeaderPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(queryHeaderPanelLayout.createSequentialGroup()
                        .add(11, 11, 11)
                        .add(nameLabel))
                    .add(queryHeaderPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .add(queryHeaderPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(lastRefreshDateLabel)
                            .add(lastRefreshLabel)
                            .add(refreshCheckBox))))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 10, Short.MAX_VALUE)
                .add(queryHeaderPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.CENTER)
                    .add(jLabel4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 11, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel5, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 11, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel6)
                    .add(modifyButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(refreshButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(removeButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(seenButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel7)
                    .add(findIssuesButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel8, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 17, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(cloneQueryButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        queryHeaderPanelLayout.linkSize(new java.awt.Component[] {jLabel4, jLabel5, jLabel6, jLabel7, modifyButton, refreshButton, removeButton}, org.jdesktop.layout.GroupLayout.VERTICAL);

        seenButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.refreshButton.AccessibleContext.accessibleDescription")); // NOI18N
        removeButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.removeButton.AccessibleContext.accessibleDescription")); // NOI18N
        refreshButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.seenButton.AccessibleContext.accessibleDescription")); // NOI18N
        modifyButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.modifyButton.AccessibleContext.accessibleDescription")); // NOI18N
        refreshCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.refreshCheckBox.AccessibleContext.accessibleDescription")); // NOI18N

        noContentPanel.setBackground(javax.swing.UIManager.getDefaults().getColor("EditorPane.background"));
        noContentPanel.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(noContentLabel, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.noContentLabel.text")); // NOI18N
        noContentPanel.add(noContentLabel, new java.awt.GridBagConstraints());

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(queryHeaderPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .add(searchPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .add(layout.createSequentialGroup()
                .add(tableFieldsPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
            .add(noContentPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 895, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(queryHeaderPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(searchPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(tableFieldsPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(noContentPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void seenButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_seenButtonActionPerformed
        // TODO add your handling code here:
}//GEN-LAST:event_seenButtonActionPerformed

    private void modifyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_modifyButtonActionPerformed
        // TODO add your handling code here:
}//GEN-LAST:event_modifyButtonActionPerformed

    private void refreshButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshButtonActionPerformed
        // TODO add your handling code here:
}//GEN-LAST:event_refreshButtonActionPerformed

    private void removeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
        // TODO add your handling code here:
}//GEN-LAST:event_removeButtonActionPerformed

    private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveButtonActionPerformed
        // TODO add your handling code here:
}//GEN-LAST:event_saveButtonActionPerformed

    private void webButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_webButtonActionPerformed
        // TODO add your handling code here:
}//GEN-LAST:event_webButtonActionPerformed

    private void urlToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_urlToggleButtonActionPerformed
        // TODO add your handling code here:
}//GEN-LAST:event_urlToggleButtonActionPerformed

    private void keywordsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_keywordsButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_keywordsButtonActionPerformed

    private void reporterCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_reporterCheckBoxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_reporterCheckBoxActionPerformed

    private void refreshConfigurationButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshConfigurationButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_refreshConfigurationButtonActionPerformed

    private void findIssuesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_findIssuesButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_findIssuesButtonActionPerformed

    private void cloneQueryButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cloneQueryButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cloneQueryButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    final javax.swing.JCheckBox bugAssigneeCheckBox = new javax.swing.JCheckBox();
    final javax.swing.JPanel byDetailsContainer = new javax.swing.JPanel();
    final javax.swing.JLabel byDetailsLabel = new javax.swing.JLabel();
    final javax.swing.JPanel byDetailsPanel = new javax.swing.JPanel();
    final javax.swing.JPanel byLastChangeContainer = new javax.swing.JPanel();
    final javax.swing.JLabel byLastChangeLabel = new javax.swing.JLabel();
    private javax.swing.JPanel byLastChangePanel;
    final javax.swing.JPanel byPeopleContainer = new javax.swing.JPanel();
    final javax.swing.JLabel byPeopleLabel = new javax.swing.JLabel();
    private javax.swing.JPanel byPeoplePanel;
    final javax.swing.JPanel byTextContainer = new javax.swing.JPanel();
    final javax.swing.JLabel byTextLabel = new javax.swing.JLabel();
    private javax.swing.JPanel byTextPanel;
    final javax.swing.JButton cancelChangesButton = new javax.swing.JButton();
    final javax.swing.JCheckBox ccCheckBox = new javax.swing.JCheckBox();
    private javax.swing.JLabel changedAndLabel;
    private javax.swing.JLabel changedBlaBlaLabel;
    final javax.swing.JTextField changedFromTextField = new javax.swing.JTextField();
    private javax.swing.JLabel changedHintLabel;
    private javax.swing.JLabel changedLabel;
    final javax.swing.JList changedList = new javax.swing.JList();
    final javax.swing.JTextField changedToTextField = new javax.swing.JTextField();
    private javax.swing.JLabel changedWhereLabel;
    public final org.netbeans.modules.bugtracking.util.LinkButton cloneQueryButton = new org.netbeans.modules.bugtracking.util.LinkButton();
    final javax.swing.JComboBox commentComboBox = new javax.swing.JComboBox();
    final javax.swing.JLabel commentLabel = new javax.swing.JLabel();
    final javax.swing.JTextField commentTextField = new javax.swing.JTextField();
    final javax.swing.JCheckBox commenterCheckBox = new javax.swing.JCheckBox();
    final javax.swing.JLabel componentLabel = new javax.swing.JLabel();
    final javax.swing.JList componentList = new javax.swing.JList();
    private javax.swing.JPanel criteriaPanel;
    final javax.swing.JComboBox filterComboBox = new javax.swing.JComboBox();
    private javax.swing.JLabel filterLabel;
    public final org.netbeans.modules.bugtracking.util.LinkButton findIssuesButton = new org.netbeans.modules.bugtracking.util.LinkButton();
    final javax.swing.JButton gotoIssueButton = new javax.swing.JButton();
    final javax.swing.JPanel gotoPanel = new javax.swing.JPanel();
    final javax.swing.JTextField idTextField = new javax.swing.JTextField();
    final javax.swing.JLabel issueTypeLabel = new javax.swing.JLabel();
    final javax.swing.JList issueTypeList = new javax.swing.JList();
    final javax.swing.JScrollPane issueTypeScrollPane = new HackedScrollPane();
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JScrollPane jScrollPane1;
    final javax.swing.JScrollPane jScrollPane2 = new HackedScrollPane();
    final javax.swing.JScrollPane jScrollPane3 = new HackedScrollPane();
    final javax.swing.JScrollPane jScrollPane4 = new HackedScrollPane();
    final javax.swing.JScrollPane jScrollPane5 = new HackedScrollPane();
    final javax.swing.JScrollPane jScrollPane6 = new HackedScrollPane();
    final javax.swing.JScrollPane jScrollPane7 = new javax.swing.JScrollPane();
    final javax.swing.JButton keywordsButton = new javax.swing.JButton();
    final javax.swing.JComboBox keywordsComboBox = new javax.swing.JComboBox();
    final javax.swing.JLabel keywordsLabel = new javax.swing.JLabel();
    final javax.swing.JTextField keywordsTextField = new javax.swing.JTextField();
    final javax.swing.JLabel lastRefreshDateLabel = new javax.swing.JLabel();
    private javax.swing.JLabel lastRefreshLabel;
    public final org.netbeans.modules.bugtracking.util.LinkButton modifyButton = new org.netbeans.modules.bugtracking.util.LinkButton();
    final javax.swing.JLabel nameLabel = new javax.swing.JLabel();
    final javax.swing.JTextField newValueTextField = new javax.swing.JTextField();
    private javax.swing.JLabel noContentLabel;
    private javax.swing.JPanel noContentPanel;
    final javax.swing.JComboBox peopleComboBox = new javax.swing.JComboBox();
    final javax.swing.JLabel peopleLabel = new javax.swing.JLabel();
    final javax.swing.JTextField peopleTextField = new javax.swing.JTextField();
    final javax.swing.JLabel priorityLabel = new javax.swing.JLabel();
    final javax.swing.JList priorityList = new javax.swing.JList();
    final javax.swing.JLabel productLabel = new javax.swing.JLabel();
    final javax.swing.JList productList = new javax.swing.JList();
    private javax.swing.JPanel queryHeaderPanel;
    final org.netbeans.modules.bugtracking.util.LinkButton refreshButton = new org.netbeans.modules.bugtracking.util.LinkButton();
    final javax.swing.JCheckBox refreshCheckBox = new javax.swing.JCheckBox();
    final org.netbeans.modules.bugtracking.util.LinkButton refreshConfigurationButton = new org.netbeans.modules.bugtracking.util.LinkButton();
    public final org.netbeans.modules.bugtracking.util.LinkButton removeButton = new org.netbeans.modules.bugtracking.util.LinkButton();
    final javax.swing.JCheckBox reporterCheckBox = new javax.swing.JCheckBox();
    final javax.swing.JLabel resolutionLabel = new javax.swing.JLabel();
    final javax.swing.JList resolutionList = new javax.swing.JList();
    final org.netbeans.modules.bugtracking.util.LinkButton saveButton = new org.netbeans.modules.bugtracking.util.LinkButton();
    final javax.swing.JButton saveChangesButton = new javax.swing.JButton();
    final javax.swing.JButton searchButton = new javax.swing.JButton();
    final javax.swing.JPanel searchPanel = new javax.swing.JPanel();
    final org.netbeans.modules.bugtracking.util.LinkButton seenButton = new org.netbeans.modules.bugtracking.util.LinkButton();
    private javax.swing.JLabel separatorLabel1;
    private javax.swing.JLabel separatorLabel2;
    private javax.swing.JLabel separatorLabel3;
    final javax.swing.JLabel severityLabel = new javax.swing.JLabel();
    final javax.swing.JList severityList = new javax.swing.JList();
    final javax.swing.JScrollPane severityScrollPane = new HackedScrollPane();
    final javax.swing.JLabel statusLabel = new javax.swing.JLabel();
    final javax.swing.JList statusList = new javax.swing.JList();
    final javax.swing.JComboBox summaryComboBox = new javax.swing.JComboBox();
    final javax.swing.JLabel summaryLabel = new javax.swing.JLabel();
    final javax.swing.JTextField summaryTextField = new javax.swing.JTextField();
    private javax.swing.JPanel tableFieldsPanel;
    private javax.swing.JPanel tableHeaderPanel;
    final javax.swing.JPanel tablePanel = new javax.swing.JPanel();
    final javax.swing.JLabel tableSummaryLabel = new javax.swing.JLabel();
    final javax.swing.JLabel tmLabel = new javax.swing.JLabel();
    final javax.swing.JList tmList = new javax.swing.JList();
    final javax.swing.JScrollPane tmScrollPane = new HackedScrollPane();
    final javax.swing.JPanel urlPanel = new javax.swing.JPanel();
    final javax.swing.JTextField urlTextField = new javax.swing.JTextField();
    final org.netbeans.modules.bugtracking.util.LinkButton urlToggleButton = new org.netbeans.modules.bugtracking.util.LinkButton();
    final javax.swing.JLabel versionLabel = new javax.swing.JLabel();
    final javax.swing.JList versionList = new javax.swing.JList();
    final org.netbeans.modules.bugtracking.util.LinkButton webButton = new org.netbeans.modules.bugtracking.util.LinkButton();
    final javax.swing.JComboBox whiteboardComboBox = new javax.swing.JComboBox();
    final javax.swing.JLabel whiteboardLabel = new javax.swing.JLabel();
    final javax.swing.JTextField whiteboardTextField = new javax.swing.JTextField();
    // End of variables declaration//GEN-END:variables

    /**
     * enables/disables all but the parameter fields
     * @param enable
     */
    void enableFields(boolean enable) {
//        summaryComboBox.setEnabled(enable);
//        commentComboBox.setEnabled(enable);
//        keywordsComboBox.setEnabled(enable);
//        summaryTextField.setEnabled(enable);
//        commentTextField.setEnabled(enable);
//        keywordsTextField.setEnabled(enable);
        summaryLabel.setEnabled(enable);
        whiteboardLabel.setEnabled(enable);
        commentLabel.setEnabled(enable);
        keywordsLabel.setEnabled(enable);
        keywordsButton.setEnabled(enable);
        
        productLabel.setEnabled(enable);
//        productList.setEnabled(enable);
        componentLabel.setEnabled(enable);
//        componentList.setEnabled(enable);
        versionLabel.setEnabled(enable);
//        versionList.setEnabled(enable);
        statusLabel.setEnabled(enable);
        severityLabel.setEnabled(enable);
//        statusList.setEnabled(enable);
        resolutionLabel.setEnabled(enable);
//        resolutionList.setEnabled(enable);
        priorityLabel.setEnabled(enable);
        tmLabel.setEnabled(enable);
        issueTypeLabel.setEnabled(enable);
//        priorityList.setEnabled(enable);

        peopleLabel.setEnabled(enable);
//        peopleComboBox.setEnabled(enable);
        peopleTextField.setEnabled(enable);
//        bugAssigneeCheckBox.setEnabled(enable);
//        reporterCheckBox.setEnabled(enable);
//        ccCheckBox.setEnabled(enable);
//        commenterCheckBox.setEnabled(enable);

        searchButton.setEnabled(enable);
        saveButton.setEnabled(enable);
        webButton.setEnabled(enable);
        urlToggleButton.setEnabled(enable);
        refreshConfigurationButton.setEnabled(enable);

        changedLabel.setEnabled(enable);
//        changedFromTextField.setEnabled(enable);
        changedAndLabel.setEnabled(enable);
//        changedToTextField.setEnabled(enable);
        changedWhereLabel.setEnabled(enable);
//        changedList.setEnabled(enable);
        changedBlaBlaLabel.setEnabled(enable);
        changedHintLabel.setEnabled(enable);
        refreshCheckBox.setEnabled(enable);
//        newValueTextField.setEnabled(enable);
    }

    void switchQueryFields(boolean showAdvanced) {
        byDetails.setVisible(showAdvanced);
        byText.setVisible(showAdvanced);
        byLastChange.setVisible(showAdvanced);
        byPeople.setVisible(showAdvanced);

        urlPanel.setVisible(!showAdvanced);
        if(showAdvanced) {
            urlToggleButton.setText(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.urlToggleButton.textUrl"));
        } else {
            urlToggleButton.setText(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.urlToggleButton.textForm"));
        }
    }

    void showError(String text) {
        noContentPanel.setVisible(true);
        tableSummaryLabel.setVisible(false);
        tableFieldsPanel.setVisible(false);
        if(text != null) {
            noContentLabel.setForeground(ERROR_COLOR);
            noContentLabel.setText(text);
        }
    }

    void showSearchingProgress(boolean on, String text) {
        noContentPanel.setVisible(on);
        tableSummaryLabel.setVisible(!on);
        tableFieldsPanel.setVisible(!on);
        if(on && text != null) {
            noContentLabel.setForeground(defaultTextColor);
            noContentLabel.setText(text);
        }
    }

    void showRetrievingProgress(boolean on, String text, boolean searchPanelVisible) {
        noContentPanel.setVisible(on);
        noContentLabel.setForeground(Color.red);
        if(searchPanelVisible) {
            searchPanel.setVisible(!on);
        }
        if(on && text != null) {
            noContentLabel.setForeground(defaultTextColor);
            noContentLabel.setText(text);
        }
    }

    void showNoContentPanel(boolean on) {
        showSearchingProgress(on, null);
    }

    void setModifyVisible(boolean b) {
        searchPanel.setVisible(b);
        cancelChangesButton.setVisible(b);
        saveChangesButton.setVisible(b);

        tableFieldsPanel.setVisible(!b);
        searchButton.setVisible(!b);
        saveButton.setVisible(!b);
        webButton.setVisible(!b);
        urlToggleButton.setVisible(!b);

        separatorLabel1.setVisible(!b);
        separatorLabel2.setVisible(!b);
        separatorLabel3.setVisible(!b);
    }

    void setSaved(String name, String lastRefresh) {
        searchPanel.setVisible(false);
        queryHeaderPanel.setVisible(true);
        tableHeaderPanel.setVisible(true);
        filterComboBox.setVisible(true); // XXX move to bugtracking IssueTable component
        filterLabel.setVisible(true);
        tablePanel.setVisible(true);
        nameLabel.setText(name);
        setLastRefresh(lastRefresh);
    }

    void setLastRefresh(String lastRefresh) {
        lastRefreshDateLabel.setText(lastRefresh);
    }

    void setNBFieldsVisible(boolean visible) {
        whiteboardLabel.setVisible(visible);
        whiteboardComboBox.setVisible(visible);
        whiteboardTextField.setVisible(visible);

        issueTypeLabel.setVisible(visible);
        issueTypeList.setVisible(visible);
        issueTypeScrollPane.setVisible(visible);
        severityLabel.setVisible(!visible);
        severityList.setVisible(!visible);
        severityScrollPane.setVisible(!visible);
        
        tmLabel.setVisible(visible);
        tmList.setVisible(visible);
        tmScrollPane.setVisible(visible);
    }

    public void focusGained(FocusEvent e) {
        Component c = e.getComponent();
        if(c instanceof JComponent) {
            Point p = SwingUtilities.convertPoint(c.getParent(), c.getLocation(), QueryPanel.this);
            final Rectangle r = new Rectangle(p, c.getSize());
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    QueryPanel.this.scrollRectToVisible(r);
                }
            });
        }
    }

    public void focusLost(FocusEvent e) {
        // do nothing
    }

    private void setBoldFont(JLabel label) {
        Font f = label.getFont().deriveFont(Font.BOLD);
        label.setFont(f);
    }

    class ExpandablePanel {
        private final JPanel panel;
        private final JLabel label;
        private final Icon ei;
        private final Icon ci;
        private boolean expaned = true;
        public ExpandablePanel(JLabel l, JPanel p, final Icon ei, final Icon ci) {
            this.panel = p;
            this.label = l;
            this.ci = ci;
            this.ei = ei;
            this.label.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if(panel.isVisible()) {
                        colapse();
                    } else {
                        expand();
                    }
                }
            });
        }
        public void expand() {
            expaned = true;
            panel.setVisible(true);
            label.setIcon(ei);
        }
        public void colapse() {
            expaned = false;
            panel.setVisible(false);
            label.setIcon(ci);
        }
        public void setVisible(boolean visible ) {
            label.setVisible(visible);
            panel.setVisible(visible && expaned);
        }
    }

    private static class FilterCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            if(value instanceof Filter) {
                value = ((Filter)value).getDisplayName();
            }
            return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        }
    }

    static class HackedScrollPane extends JScrollPane {
        @Override
        public Dimension getPreferredSize() {
            setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            Dimension dim = super.getPreferredSize();
            setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            return dim;
        }
    }

}
