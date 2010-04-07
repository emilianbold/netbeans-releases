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

package org.netbeans.modules.jira.query;

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
import org.netbeans.modules.jira.util.PriorityRenderer;
import org.netbeans.modules.jira.util.ProjectRenderer;
import org.netbeans.modules.jira.util.ResolutionRenderer;
import org.netbeans.modules.jira.util.StatusRenderer;
import org.netbeans.modules.jira.util.TypeRenderer;

/**
 *
 * @author Tomas Stupka, Jan Stola
 */
public class QueryPanel extends javax.swing.JPanel implements FocusListener {

    final ExpandablePanel byText;
    final ExpandablePanel byDetails;
    final ExpandablePanel byPeople;
    final ExpandablePanel byOther;
    private static final Color ERROR_COLOR = new Color(153,0,0);
    private Color defaultTextColor;

    /** Creates new form QueryPanel */
    public QueryPanel(JComponent tableComponent, QueryController controller, boolean isFilter) {
        initComponents();

        Font f = new JLabel().getFont();
        int s = f.getSize();
        nameLabel.setFont(jLabel1.getFont().deriveFont(s * 1.7f));
        jiraFilterLabel.setVisible(isFilter);
        if(isFilter) {
            jiraFilterLabel.setFont(new Font(f.getName(), f.getStyle(), (int) (s * 1.7)));
        } 
        defaultTextColor = noContentLabel.getForeground();

        tablePanel.add(tableComponent);

        JTree tv = new JTree();
        BasicTreeUI tvui = (BasicTreeUI) tv.getUI();
        Icon ei = tvui.getExpandedIcon();
        Icon ci = tvui.getCollapsedIcon();

        byTextContainer.add(byTextPanel);
        byDetailsContainer.add(byDetailsPanel);
        byPeopleContainer.add(byAttributesPanel);
        byOtherContainer.add(byOtherPanel);

        byText = new ExpandablePanel(byTextLabel, byTextContainer, ei, ci);
        byDetails = new ExpandablePanel(byDetailsLabel, byDetailsContainer, ei, ci);
        byPeople = new ExpandablePanel(byPeopleLabel, byPeopleContainer, ei, ci);
        byOther = new ExpandablePanel(byOtherLabel, byOtherContainer, ei, ci);

        byText.expand();
        byDetails.expand();
        byPeople.colapse();
        byOther.colapse();

        queryHeaderPanel.setVisible(false);
        tableFieldsPanel.setVisible(false);
        saveChangesButton.setVisible(false);
        cancelChangesButton.setVisible(false);
        filterComboBox.setVisible(false);
        filterLabel.setVisible(false);
        refreshCheckBox.setVisible(false);
        noContentPanel.setVisible(false);

        summaryCheckBox.setSelected(true);
        descriptionCheckBox.setSelected(true);
        commentsCheckBox.setSelected(false);
        environmentCheckBox.setSelected(false);

        projectList.setCellRenderer(new ProjectRenderer());
        typeList.setCellRenderer(new TypeRenderer());
        statusList.setCellRenderer(new StatusRenderer());
        resolutionList.setCellRenderer(new ResolutionRenderer());
        priorityList.setCellRenderer(new PriorityRenderer());

        UserSearchRenderer userSearchRenderer = new UserSearchRenderer();
        reporterComboBox.setRenderer(userSearchRenderer);
        assigneeComboBox.setRenderer(userSearchRenderer);
        
        filterComboBox.setRenderer(new FilterCellRenderer());

        cancelChangesButton.addFocusListener(this);
        typeList.addFocusListener(this);
        statusList.addFocusListener(this);
        resolutionList.addFocusListener(this);
        priorityList.addFocusListener(this);
        filterComboBox.addFocusListener(this);
        reporterComboBox.addFocusListener(this);
        assigneeComboBox.addFocusListener(this);
        reporterTextField.addFocusListener(this);
        assigneeTextField.addFocusListener(this);
        gotoIssueButton.addFocusListener(this);
        idTextField.addFocusListener(this);
        modifyButton.addFocusListener(this);
        projectList.addFocusListener(this);
        refreshButton.addFocusListener(this);
        refreshCheckBox.addFocusListener(this);
        removeButton.addFocusListener(this);
        saveButton.addFocusListener(this);
        saveChangesButton.addFocusListener(this);
        searchButton.addFocusListener(this);
        seenButton.addFocusListener(this);
        queryTextField.addFocusListener(this);
        tablePanel.addFocusListener(this);
        tableSummaryLabel.addFocusListener(this);
        webButton.addFocusListener(this);

        summaryCheckBox.setOpaque(false);
        descriptionCheckBox.setOpaque(false);
        commentsCheckBox.setOpaque(false);
        environmentCheckBox.setOpaque(false);
        refreshCheckBox.setOpaque(false);

        lblIssueKeyWarning.setVisible(false);
        warningLabel.setVisible(false);

        validate();
        repaint();
    }

    void setQueryRunning(boolean running) {
        modifyButton.setEnabled(!running);
        refreshButton.setEnabled(!running);
        seenButton.setEnabled(!running);
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

        byOtherPanel = new javax.swing.JPanel();
        jLabel17 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jLabel11 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        byAttributesPanel = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        byTextPanel = new javax.swing.JPanel();
        tableFieldsPanel = new javax.swing.JPanel();
        tableHeaderPanel = new javax.swing.JPanel();
        filterLabel = new javax.swing.JLabel();
        criteriaPanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        separatorLabel1 = new javax.swing.JLabel();
        separatorLabel2 = new javax.swing.JLabel();
        queryHeaderPanel = new javax.swing.JPanel();
        lastRefreshLabel = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        noContentPanel = new javax.swing.JPanel();
        noContentLabel = new javax.swing.JLabel();

        byOtherPanel.setBackground(javax.swing.UIManager.getDefaults().getColor("TextArea.background"));

        org.openide.awt.Mnemonics.setLocalizedText(jLabel17, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.jLabel17.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel18, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.jLabel18.text")); // NOI18N

        ratioMinTextField.setText(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.ratioMinTextField.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel19, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.jLabel19.text")); // NOI18N

        ratioMaxTextField.setText(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.ratioMaxTextField.text")); // NOI18N
        ratioMaxTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ratioMaxTextFieldActionPerformed(evt);
            }
        });

        jLabel20.setForeground(javax.swing.UIManager.getDefaults().getColor("Button.disabledText"));
        org.openide.awt.Mnemonics.setLocalizedText(jLabel20, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.jLabel20.text")); // NOI18N

        jPanel1.setBackground(javax.swing.UIManager.getDefaults().getColor("TextArea.background"));

        org.openide.awt.Mnemonics.setLocalizedText(jLabel11, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.jLabel11.text")); // NOI18N

        dueFromTextField.setText(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.dueFromTextField.text")); // NOI18N

        jLabel10.setForeground(javax.swing.UIManager.getDefaults().getColor("Button.disabledText"));
        org.openide.awt.Mnemonics.setLocalizedText(jLabel10, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.jLabel10.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel12, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.jLabel12.text")); // NOI18N

        updatedToTextField.setText(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.updatedToTextField.text")); // NOI18N
        updatedToTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updatedToTextFieldActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel14, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.jLabel14.text")); // NOI18N

        jLabel16.setForeground(javax.swing.UIManager.getDefaults().getColor("Button.disabledText"));
        org.openide.awt.Mnemonics.setLocalizedText(jLabel16, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.jLabel16.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel9, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.jLabel9.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel7, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.jLabel7.text")); // NOI18N

        createdFromTextField.setText(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.createdFromTextField.text")); // NOI18N

        dueToTextField.setText(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.dueToTextField.text")); // NOI18N
        dueToTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dueToTextFieldActionPerformed(evt);
            }
        });

        updatedFromTextField.setText(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.updatedFromTextField.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel15, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.jLabel15.text")); // NOI18N

        createdToTextField.setText(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.createdToTextField.text")); // NOI18N
        createdToTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createdToTextFieldActionPerformed(evt);
            }
        });

        jLabel13.setForeground(javax.swing.UIManager.getDefaults().getColor("Button.disabledText"));
        org.openide.awt.Mnemonics.setLocalizedText(jLabel13, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.jLabel13.text")); // NOI18N

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(jLabel7, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(16, 16, 16)
                        .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel10)
                            .add(jPanel1Layout.createSequentialGroup()
                                .add(createdFromTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 115, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(jLabel9)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(createdToTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 115, Short.MAX_VALUE))))
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel14, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jLabel11, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel16)
                            .add(jPanel1Layout.createSequentialGroup()
                                .add(dueFromTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 115, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(jLabel15)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(dueToTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 79, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(jLabel13)
                            .add(jPanel1Layout.createSequentialGroup()
                                .add(updatedFromTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 115, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(jLabel12)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(updatedToTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 92, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap())
        );

        jPanel1Layout.linkSize(new java.awt.Component[] {createdFromTextField, createdToTextField, dueFromTextField, dueToTextField, updatedFromTextField, updatedToTextField}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel7, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(createdFromTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(jLabel9)
                        .add(createdToTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(36, 36, 36)
                        .add(jLabel10)))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel12)
                    .add(updatedToTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(updatedFromTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel11, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel13)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(dueFromTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel15)
                    .add(dueToTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel14, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel16)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        org.jdesktop.layout.GroupLayout byOtherPanelLayout = new org.jdesktop.layout.GroupLayout(byOtherPanel);
        byOtherPanel.setLayout(byOtherPanelLayout);
        byOtherPanelLayout.setHorizontalGroup(
            byOtherPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(byOtherPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(18, 18, 18)
                .add(jLabel17)
                .add(18, 18, 18)
                .add(byOtherPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(byOtherPanelLayout.createSequentialGroup()
                        .add(byOtherPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel19)
                            .add(jLabel18))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(byOtherPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(ratioMinTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 79, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(ratioMaxTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 79, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                    .add(byOtherPanelLayout.createSequentialGroup()
                        .add(jLabel20)
                        .add(0, 0, 0)))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        byOtherPanelLayout.setVerticalGroup(
            byOtherPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(byOtherPanelLayout.createSequentialGroup()
                .addContainerGap(20, Short.MAX_VALUE)
                .add(byOtherPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(byOtherPanelLayout.createSequentialGroup()
                        .add(byOtherPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jLabel17)
                            .add(jLabel18)
                            .add(ratioMinTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(byOtherPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jLabel19)
                            .add(ratioMaxTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jLabel20))))
        );

        byAttributesPanel.setBackground(javax.swing.UIManager.getDefaults().getColor("TextArea.background"));

        jLabel2.setFont(jLabel2.getFont().deriveFont(jLabel2.getFont().getStyle() | java.awt.Font.BOLD));
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.jLabel2.text_1")); // NOI18N

        reporterTextField.setText(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.reporterTextField.text")); // NOI18N

        jLabel8.setFont(jLabel8.getFont().deriveFont(jLabel8.getFont().getStyle() | java.awt.Font.BOLD));
        org.openide.awt.Mnemonics.setLocalizedText(jLabel8, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.jLabel8.text")); // NOI18N

        assigneeTextField.setText(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.assigneeTextField.text")); // NOI18N

        projectLabel1.setFont(projectLabel1.getFont().deriveFont(projectLabel1.getFont().getStyle() | java.awt.Font.BOLD));
        org.openide.awt.Mnemonics.setLocalizedText(projectLabel1, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.projectLabel1.text")); // NOI18N

        jScrollPane9.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        resolutionList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        resolutionList.setMaximumSize(new java.awt.Dimension(100, 2));
        jScrollPane9.setViewportView(resolutionList);

        projectLabel2.setFont(projectLabel2.getFont().deriveFont(projectLabel2.getFont().getStyle() | java.awt.Font.BOLD));
        org.openide.awt.Mnemonics.setLocalizedText(projectLabel2, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.projectLabel2.text")); // NOI18N

        projectLabel3.setFont(projectLabel3.getFont().deriveFont(projectLabel3.getFont().getStyle() | java.awt.Font.BOLD));
        org.openide.awt.Mnemonics.setLocalizedText(projectLabel3, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.projectLabel3.text")); // NOI18N

        jScrollPane10.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        priorityList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        priorityList.setMaximumSize(new java.awt.Dimension(100, 2));
        jScrollPane10.setViewportView(priorityList);

        reporterComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        assigneeComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jScrollPane11.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        statusList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        statusList.setMaximumSize(new java.awt.Dimension(100, 2));
        jScrollPane11.setViewportView(statusList);

        org.jdesktop.layout.GroupLayout byAttributesPanelLayout = new org.jdesktop.layout.GroupLayout(byAttributesPanel);
        byAttributesPanel.setLayout(byAttributesPanelLayout);
        byAttributesPanelLayout.setHorizontalGroup(
            byAttributesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(byAttributesPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(byAttributesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel8)
                    .add(byAttributesPanelLayout.createSequentialGroup()
                        .add(reporterComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(reporterTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 116, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(jLabel2)
                    .add(byAttributesPanelLayout.createSequentialGroup()
                        .add(assigneeComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(assigneeTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 116, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .add(18, 18, 18)
                .add(byAttributesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(projectLabel1)
                    .add(jScrollPane11, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(byAttributesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(projectLabel2)
                    .add(jScrollPane9, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(byAttributesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(projectLabel3)
                    .add(jScrollPane10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        byAttributesPanelLayout.setVerticalGroup(
            byAttributesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(byAttributesPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(byAttributesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(byAttributesPanelLayout.createSequentialGroup()
                        .add(byAttributesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(projectLabel1)
                            .add(projectLabel2)
                            .add(projectLabel3))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(byAttributesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jScrollPane9, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 89, Short.MAX_VALUE)
                            .add(jScrollPane11, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 89, Short.MAX_VALUE)
                            .add(jScrollPane10, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 89, Short.MAX_VALUE)))
                    .add(byAttributesPanelLayout.createSequentialGroup()
                        .add(jLabel2)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(byAttributesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(reporterTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(reporterComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jLabel8)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(byAttributesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(assigneeComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(assigneeTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );

        byDetailsPanel.setBackground(javax.swing.UIManager.getDefaults().getColor("TextArea.background"));

        projectLabel.setFont(projectLabel.getFont().deriveFont(projectLabel.getFont().getStyle() | java.awt.Font.BOLD));
        org.openide.awt.Mnemonics.setLocalizedText(projectLabel, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.projectLabel.text")); // NOI18N

        typeLabel.setFont(typeLabel.getFont().deriveFont(typeLabel.getFont().getStyle() | java.awt.Font.BOLD));
        org.openide.awt.Mnemonics.setLocalizedText(typeLabel, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.typeLabel.text")); // NOI18N

        jScrollPane6.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        typeList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        typeList.setMinimumSize(new java.awt.Dimension(100, 2));
        jScrollPane6.setViewportView(typeList);

        jScrollPane7.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        projectList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        projectList.setMaximumSize(new java.awt.Dimension(100, 2));
        jScrollPane7.setViewportView(projectList);

        fixForLabel.setFont(fixForLabel.getFont().deriveFont(fixForLabel.getFont().getStyle() | java.awt.Font.BOLD));
        org.openide.awt.Mnemonics.setLocalizedText(fixForLabel, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.fixForLabel.text")); // NOI18N

        fixForScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        fixForList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        fixForList.setMaximumSize(new java.awt.Dimension(100, 2));
        fixForScrollPane.setViewportView(fixForList);

        componentsLabel.setFont(componentsLabel.getFont().deriveFont(componentsLabel.getFont().getStyle() | java.awt.Font.BOLD));
        org.openide.awt.Mnemonics.setLocalizedText(componentsLabel, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.componentsLabel.text")); // NOI18N

        componentsScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        componentsList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        componentsList.setMaximumSize(new java.awt.Dimension(100, 2));
        componentsScrollPane.setViewportView(componentsList);

        affectsVersionsLabel.setFont(affectsVersionsLabel.getFont().deriveFont(affectsVersionsLabel.getFont().getStyle() | java.awt.Font.BOLD));
        org.openide.awt.Mnemonics.setLocalizedText(affectsVersionsLabel, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.affectsVersionsLabel.text")); // NOI18N

        affectsVersionsScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        affectsVersionList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        affectsVersionList.setMaximumSize(new java.awt.Dimension(100, 2));
        affectsVersionsScrollPane.setViewportView(affectsVersionList);

        org.jdesktop.layout.GroupLayout byDetailsPanelLayout = new org.jdesktop.layout.GroupLayout(byDetailsPanel);
        byDetailsPanel.setLayout(byDetailsPanelLayout);
        byDetailsPanelLayout.setHorizontalGroup(
            byDetailsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(byDetailsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(byDetailsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jScrollPane7, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(projectLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(byDetailsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(typeLabel)
                    .add(jScrollPane6, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(18, 18, 18)
                .add(byDetailsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(fixForScrollPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(fixForLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(byDetailsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(componentsScrollPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(componentsLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(byDetailsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(affectsVersionsScrollPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(affectsVersionsLabel))
                .addContainerGap())
        );
        byDetailsPanelLayout.setVerticalGroup(
            byDetailsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(byDetailsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(byDetailsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(byDetailsPanelLayout.createSequentialGroup()
                        .add(affectsVersionsLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(affectsVersionsScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 99, Short.MAX_VALUE))
                    .add(byDetailsPanelLayout.createSequentialGroup()
                        .add(componentsLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(componentsScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 99, Short.MAX_VALUE))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, byDetailsPanelLayout.createSequentialGroup()
                        .add(fixForLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(fixForScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 99, Short.MAX_VALUE))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, byDetailsPanelLayout.createSequentialGroup()
                        .add(byDetailsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(projectLabel)
                            .add(typeLabel))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(byDetailsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jScrollPane7, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 99, Short.MAX_VALUE)
                            .add(jScrollPane6, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 99, Short.MAX_VALUE))))
                .addContainerGap())
        );

        byTextPanel.setBackground(javax.swing.UIManager.getDefaults().getColor("TextArea.background"));

        queryLabel.setFont(queryLabel.getFont().deriveFont(queryLabel.getFont().getStyle() | java.awt.Font.BOLD));
        org.openide.awt.Mnemonics.setLocalizedText(queryLabel, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.queryLabel.text_1")); // NOI18N

        queryTextField.setText(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.queryTextField.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(summaryCheckBox, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.summaryCheckBox.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(descriptionCheckBox, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.descriptionCheckBox.text")); // NOI18N
        descriptionCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                descriptionCheckBoxActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(commentsCheckBox, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.commentsCheckBox.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(environmentCheckBox, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.environmentCheckBox.text")); // NOI18N

        org.jdesktop.layout.GroupLayout byTextPanelLayout = new org.jdesktop.layout.GroupLayout(byTextPanel);
        byTextPanel.setLayout(byTextPanelLayout);
        byTextPanelLayout.setHorizontalGroup(
            byTextPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(byTextPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(queryLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(byTextPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(byTextPanelLayout.createSequentialGroup()
                        .add(byTextPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(summaryCheckBox)
                            .add(commentsCheckBox))
                        .add(18, 18, 18)
                        .add(byTextPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(environmentCheckBox)
                            .add(descriptionCheckBox)))
                    .add(queryTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 229, Short.MAX_VALUE))
                .addContainerGap())
        );
        byTextPanelLayout.setVerticalGroup(
            byTextPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(byTextPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(byTextPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(queryLabel)
                    .add(queryTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(byTextPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(byTextPanelLayout.createSequentialGroup()
                        .add(summaryCheckBox)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(commentsCheckBox))
                    .add(byTextPanelLayout.createSequentialGroup()
                        .add(descriptionCheckBox)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(environmentCheckBox)))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        setBackground(javax.swing.UIManager.getDefaults().getColor("TextArea.background"));

        tableFieldsPanel.setBackground(javax.swing.UIManager.getDefaults().getColor("EditorPane.background"));

        tablePanel.setBackground(new java.awt.Color(224, 224, 224));
        tablePanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        tablePanel.setMinimumSize(new java.awt.Dimension(100, 350));
        tablePanel.setLayout(new java.awt.BorderLayout());

        tableHeaderPanel.setBackground(javax.swing.UIManager.getDefaults().getColor("EditorPane.background"));

        org.openide.awt.Mnemonics.setLocalizedText(tableSummaryLabel, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.tableSummaryLabel.text_1")); // NOI18N

        filterComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        org.openide.awt.Mnemonics.setLocalizedText(filterLabel, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.filterLabel.text_1")); // NOI18N

        org.jdesktop.layout.GroupLayout tableHeaderPanelLayout = new org.jdesktop.layout.GroupLayout(tableHeaderPanel);
        tableHeaderPanel.setLayout(tableHeaderPanelLayout);
        tableHeaderPanelLayout.setHorizontalGroup(
            tableHeaderPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(tableHeaderPanelLayout.createSequentialGroup()
                .add(tableSummaryLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 544, Short.MAX_VALUE)
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

        org.jdesktop.layout.GroupLayout tableFieldsPanelLayout = new org.jdesktop.layout.GroupLayout(tableFieldsPanel);
        tableFieldsPanel.setLayout(tableFieldsPanelLayout);
        tableFieldsPanelLayout.setHorizontalGroup(
            tableFieldsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, tableFieldsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(tableFieldsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, tablePanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 726, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, tableHeaderPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
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

        org.openide.awt.Mnemonics.setLocalizedText(searchButton, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.searchButton.text")); // NOI18N

        criteriaPanel.setBackground(new java.awt.Color(224, 224, 224));
        criteriaPanel.setBorder(javax.swing.BorderFactory.createLineBorder(javax.swing.UIManager.getDefaults().getColor("Button.disabledText")));

        byTextLabel.setFont(byTextLabel.getFont().deriveFont(byTextLabel.getFont().getStyle() | java.awt.Font.BOLD));
        org.openide.awt.Mnemonics.setLocalizedText(byTextLabel, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.byTextLabel.text_1")); // NOI18N

        byTextContainer.setLayout(new java.awt.BorderLayout());

        byDetailsContainer.setLayout(new java.awt.BorderLayout());

        byDetailsLabel.setFont(byDetailsLabel.getFont().deriveFont(byDetailsLabel.getFont().getStyle() | java.awt.Font.BOLD));
        org.openide.awt.Mnemonics.setLocalizedText(byDetailsLabel, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.byDetailsLabel.text")); // NOI18N

        byPeopleLabel.setFont(byPeopleLabel.getFont().deriveFont(byPeopleLabel.getFont().getStyle() | java.awt.Font.BOLD));
        org.openide.awt.Mnemonics.setLocalizedText(byPeopleLabel, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.byPeopleLabel.text")); // NOI18N

        byPeopleContainer.setLayout(new java.awt.BorderLayout());

        byOtherLabel.setFont(byOtherLabel.getFont().deriveFont(byOtherLabel.getFont().getStyle() | java.awt.Font.BOLD));
        org.openide.awt.Mnemonics.setLocalizedText(byOtherLabel, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.byOtherLabel.text")); // NOI18N

        byOtherContainer.setLayout(new java.awt.BorderLayout());

        org.jdesktop.layout.GroupLayout criteriaPanelLayout = new org.jdesktop.layout.GroupLayout(criteriaPanel);
        criteriaPanel.setLayout(criteriaPanelLayout);
        criteriaPanelLayout.setHorizontalGroup(
            criteriaPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(criteriaPanelLayout.createSequentialGroup()
                .add(byTextLabel)
                .addContainerGap())
            .add(criteriaPanelLayout.createSequentialGroup()
                .add(byOtherLabel)
                .addContainerGap())
            .add(byTextContainer, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 744, Short.MAX_VALUE)
            .add(byOtherContainer, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 744, Short.MAX_VALUE)
            .add(criteriaPanelLayout.createSequentialGroup()
                .add(byDetailsLabel)
                .addContainerGap())
            .add(byDetailsContainer, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 744, Short.MAX_VALUE)
            .add(criteriaPanelLayout.createSequentialGroup()
                .add(byPeopleLabel)
                .addContainerGap())
            .add(byPeopleContainer, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 744, Short.MAX_VALUE)
        );
        criteriaPanelLayout.setVerticalGroup(
            criteriaPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(criteriaPanelLayout.createSequentialGroup()
                .add(byTextLabel)
                .add(0, 0, 0)
                .add(byTextContainer, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(0, 0, 0)
                .add(byDetailsLabel)
                .add(0, 0, 0)
                .add(byDetailsContainer, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(0, 0, 0)
                .add(byPeopleLabel)
                .add(0, 0, 0)
                .add(byPeopleContainer, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(0, 0, 0)
                .add(byOtherLabel)
                .add(0, 0, 0)
                .add(byOtherContainer, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
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

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.jLabel1.text_1")); // NOI18N

        idTextField.setText(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.idTextField.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(gotoIssueButton, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.gotoIssueButton.text")); // NOI18N

        lblIssueKeyWarning.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/jira/resources/warning.gif"))); // NOI18N

        org.jdesktop.layout.GroupLayout gotoPanelLayout = new org.jdesktop.layout.GroupLayout(gotoPanel);
        gotoPanel.setLayout(gotoPanelLayout);
        gotoPanelLayout.setHorizontalGroup(
            gotoPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(gotoPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(lblIssueKeyPrefix)
                .add(0, 0, 0)
                .add(idTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 191, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(gotoIssueButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(lblIssueKeyWarning)
                .addContainerGap())
        );
        gotoPanelLayout.setVerticalGroup(
            gotoPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(gotoPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                .add(jLabel1)
                .add(lblIssueKeyPrefix)
                .add(gotoIssueButton)
                .add(idTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(lblIssueKeyWarning))
        );

        org.openide.awt.Mnemonics.setLocalizedText(separatorLabel1, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.separatorLabel1.text")); // NOI18N
        separatorLabel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        org.openide.awt.Mnemonics.setLocalizedText(separatorLabel2, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.separatorLabel2.text")); // NOI18N
        separatorLabel2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        org.openide.awt.Mnemonics.setLocalizedText(reloadAttributesButton, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.reloadAttributesButton.text")); // NOI18N
        reloadAttributesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                reloadAttributesButtonActionPerformed(evt);
            }
        });

        warningLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/jira/resources/error.gif"))); // NOI18N

        org.jdesktop.layout.GroupLayout searchPanelLayout = new org.jdesktop.layout.GroupLayout(searchPanel);
        searchPanel.setLayout(searchPanelLayout);
        searchPanelLayout.setHorizontalGroup(
            searchPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(gotoPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 786, Short.MAX_VALUE)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, searchPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(searchPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, searchPanelLayout.createSequentialGroup()
                        .add(searchButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(saveChangesButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(cancelChangesButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(warningLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(saveButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(separatorLabel1)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(webButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(separatorLabel2)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(reloadAttributesButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(criteriaPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        searchPanelLayout.setVerticalGroup(
            searchPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(searchPanelLayout.createSequentialGroup()
                .add(gotoPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(criteriaPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(searchPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(searchPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(searchButton)
                        .add(cancelChangesButton)
                        .add(saveChangesButton)
                        .add(warningLabel))
                    .add(searchPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(reloadAttributesButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(webButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(saveButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(separatorLabel1)
                        .add(separatorLabel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 14, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
        );

        searchPanelLayout.linkSize(new java.awt.Component[] {saveButton, separatorLabel1, separatorLabel2, webButton}, org.jdesktop.layout.GroupLayout.VERTICAL);

        queryHeaderPanel.setBackground(javax.swing.UIManager.getDefaults().getColor("EditorPane.background"));

        org.openide.awt.Mnemonics.setLocalizedText(refreshButton, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.refreshButton.text")); // NOI18N
        refreshButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshButtonActionPerformed(evt);
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

        org.openide.awt.Mnemonics.setLocalizedText(seenButton, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.seenButton.text")); // NOI18N
        seenButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                seenButtonActionPerformed(evt);
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

        org.openide.awt.Mnemonics.setLocalizedText(jiraFilterLabel, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.jiraFilterLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(cloneQueryButton, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.cloneQueryButton.text")); // NOI18N
        cloneQueryButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cloneQueryButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel21, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.jLabel21.text")); // NOI18N
        jLabel21.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        org.openide.awt.Mnemonics.setLocalizedText(jLabel22, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.jLabel22.text")); // NOI18N
        jLabel22.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        org.openide.awt.Mnemonics.setLocalizedText(findIssuesButton, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.findIssuesButton.text")); // NOI18N
        findIssuesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                findIssuesButtonActionPerformed(evt);
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
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(jiraFilterLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 309, Short.MAX_VALUE)
                        .add(refreshCheckBox)
                        .add(18, 18, 18)
                        .add(lastRefreshLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(lastRefreshDateLabel))
                    .add(queryHeaderPanelLayout.createSequentialGroup()
                        .add(refreshButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jLabel4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(modifyButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(7, 7, 7)
                        .add(jLabel5, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(seenButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(5, 5, 5)
                        .add(jLabel6)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(removeButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jLabel22, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(findIssuesButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jLabel21, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
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
                        .add(queryHeaderPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(nameLabel)
                            .add(jiraFilterLabel)))
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
                    .add(seenButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(removeButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(refreshButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel21, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 18, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(cloneQueryButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel22, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 18, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(findIssuesButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        queryHeaderPanelLayout.linkSize(new java.awt.Component[] {jLabel4, jLabel5, jLabel6, modifyButton, removeButton, seenButton}, org.jdesktop.layout.GroupLayout.VERTICAL);

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
            .add(noContentPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 786, Short.MAX_VALUE)
            .add(layout.createSequentialGroup()
                .add(tableFieldsPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(queryHeaderPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(searchPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(tableFieldsPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(noContentPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void refreshButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshButtonActionPerformed
        // TODO add your handling code here:
}//GEN-LAST:event_refreshButtonActionPerformed

    private void modifyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_modifyButtonActionPerformed
        // TODO add your handling code here:
}//GEN-LAST:event_modifyButtonActionPerformed

    private void seenButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_seenButtonActionPerformed
        // TODO add your handling code here:
}//GEN-LAST:event_seenButtonActionPerformed

    private void removeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
        // TODO add your handling code here:
}//GEN-LAST:event_removeButtonActionPerformed

    private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveButtonActionPerformed
        // TODO add your handling code here:
}//GEN-LAST:event_saveButtonActionPerformed

    private void webButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_webButtonActionPerformed
        // TODO add your handling code here:
}//GEN-LAST:event_webButtonActionPerformed

    private void descriptionCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_descriptionCheckBoxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_descriptionCheckBoxActionPerformed

    private void reloadAttributesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_reloadAttributesButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_reloadAttributesButtonActionPerformed

    private void createdToTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createdToTextFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_createdToTextFieldActionPerformed

    private void updatedToTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updatedToTextFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_updatedToTextFieldActionPerformed

    private void dueToTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dueToTextFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_dueToTextFieldActionPerformed

    private void ratioMaxTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ratioMaxTextFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_ratioMaxTextFieldActionPerformed

    private void cloneQueryButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cloneQueryButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cloneQueryButtonActionPerformed

    private void findIssuesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_findIssuesButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_findIssuesButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    final javax.swing.JList affectsVersionList = new javax.swing.JList();
    final javax.swing.JLabel affectsVersionsLabel = new javax.swing.JLabel();
    final javax.swing.JScrollPane affectsVersionsScrollPane = new HackedScrollPane();
    final javax.swing.JComboBox assigneeComboBox = new javax.swing.JComboBox();
    final javax.swing.JTextField assigneeTextField = new javax.swing.JTextField();
    private javax.swing.JPanel byAttributesPanel;
    final javax.swing.JPanel byDetailsContainer = new javax.swing.JPanel();
    final javax.swing.JLabel byDetailsLabel = new javax.swing.JLabel();
    final javax.swing.JPanel byDetailsPanel = new javax.swing.JPanel();
    final javax.swing.JPanel byOtherContainer = new javax.swing.JPanel();
    final javax.swing.JLabel byOtherLabel = new javax.swing.JLabel();
    private javax.swing.JPanel byOtherPanel;
    final javax.swing.JPanel byPeopleContainer = new javax.swing.JPanel();
    final javax.swing.JLabel byPeopleLabel = new javax.swing.JLabel();
    final javax.swing.JPanel byTextContainer = new javax.swing.JPanel();
    final javax.swing.JLabel byTextLabel = new javax.swing.JLabel();
    private javax.swing.JPanel byTextPanel;
    final javax.swing.JButton cancelChangesButton = new javax.swing.JButton();
    public final org.netbeans.modules.bugtracking.util.LinkButton cloneQueryButton = new org.netbeans.modules.bugtracking.util.LinkButton();
    final javax.swing.JCheckBox commentsCheckBox = new javax.swing.JCheckBox();
    final javax.swing.JLabel componentsLabel = new javax.swing.JLabel();
    final javax.swing.JList componentsList = new javax.swing.JList();
    final javax.swing.JScrollPane componentsScrollPane = new HackedScrollPane();
    final javax.swing.JTextField createdFromTextField = new javax.swing.JTextField();
    final javax.swing.JTextField createdToTextField = new javax.swing.JTextField();
    private javax.swing.JPanel criteriaPanel;
    final javax.swing.JCheckBox descriptionCheckBox = new javax.swing.JCheckBox();
    final javax.swing.JTextField dueFromTextField = new javax.swing.JTextField();
    final javax.swing.JTextField dueToTextField = new javax.swing.JTextField();
    final javax.swing.JCheckBox environmentCheckBox = new javax.swing.JCheckBox();
    final javax.swing.JComboBox filterComboBox = new javax.swing.JComboBox();
    private javax.swing.JLabel filterLabel;
    public final org.netbeans.modules.bugtracking.util.LinkButton findIssuesButton = new org.netbeans.modules.bugtracking.util.LinkButton();
    final javax.swing.JLabel fixForLabel = new javax.swing.JLabel();
    final javax.swing.JList fixForList = new javax.swing.JList();
    final javax.swing.JScrollPane fixForScrollPane = new HackedScrollPane();
    final javax.swing.JButton gotoIssueButton = new javax.swing.JButton();
    final javax.swing.JPanel gotoPanel = new javax.swing.JPanel();
    final javax.swing.JTextField idTextField = new javax.swing.JTextField();
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    final javax.swing.JScrollPane jScrollPane10 = new HackedScrollPane();
    final javax.swing.JScrollPane jScrollPane11 = new HackedScrollPane();
    final javax.swing.JScrollPane jScrollPane6 = new HackedScrollPane();
    final javax.swing.JScrollPane jScrollPane7 = new HackedScrollPane();
    final javax.swing.JScrollPane jScrollPane9 = new HackedScrollPane();
    final javax.swing.JLabel jiraFilterLabel = new javax.swing.JLabel();
    final javax.swing.JLabel lastRefreshDateLabel = new javax.swing.JLabel();
    private javax.swing.JLabel lastRefreshLabel;
    final javax.swing.JLabel lblIssueKeyPrefix = new javax.swing.JLabel();
    final javax.swing.JLabel lblIssueKeyWarning = new javax.swing.JLabel();
    public final org.netbeans.modules.bugtracking.util.LinkButton modifyButton = new org.netbeans.modules.bugtracking.util.LinkButton();
    final javax.swing.JLabel nameLabel = new javax.swing.JLabel();
    private javax.swing.JLabel noContentLabel;
    private javax.swing.JPanel noContentPanel;
    final javax.swing.JList priorityList = new javax.swing.JList();
    final javax.swing.JLabel projectLabel = new javax.swing.JLabel();
    final javax.swing.JLabel projectLabel1 = new javax.swing.JLabel();
    final javax.swing.JLabel projectLabel2 = new javax.swing.JLabel();
    final javax.swing.JLabel projectLabel3 = new javax.swing.JLabel();
    final javax.swing.JList projectList = new javax.swing.JList();
    private javax.swing.JPanel queryHeaderPanel;
    final javax.swing.JLabel queryLabel = new javax.swing.JLabel();
    final javax.swing.JTextField queryTextField = new javax.swing.JTextField();
    final javax.swing.JTextField ratioMaxTextField = new javax.swing.JTextField();
    final javax.swing.JTextField ratioMinTextField = new javax.swing.JTextField();
    final org.netbeans.modules.bugtracking.util.LinkButton refreshButton = new org.netbeans.modules.bugtracking.util.LinkButton();
    final javax.swing.JCheckBox refreshCheckBox = new javax.swing.JCheckBox();
    final org.netbeans.modules.bugtracking.util.LinkButton reloadAttributesButton = new org.netbeans.modules.bugtracking.util.LinkButton();
    public final org.netbeans.modules.bugtracking.util.LinkButton removeButton = new org.netbeans.modules.bugtracking.util.LinkButton();
    final javax.swing.JComboBox reporterComboBox = new javax.swing.JComboBox();
    final javax.swing.JTextField reporterTextField = new javax.swing.JTextField();
    final javax.swing.JList resolutionList = new javax.swing.JList();
    final org.netbeans.modules.bugtracking.util.LinkButton saveButton = new org.netbeans.modules.bugtracking.util.LinkButton();
    final javax.swing.JButton saveChangesButton = new javax.swing.JButton();
    final javax.swing.JButton searchButton = new javax.swing.JButton();
    final javax.swing.JPanel searchPanel = new javax.swing.JPanel();
    final org.netbeans.modules.bugtracking.util.LinkButton seenButton = new org.netbeans.modules.bugtracking.util.LinkButton();
    private javax.swing.JLabel separatorLabel1;
    private javax.swing.JLabel separatorLabel2;
    final javax.swing.JList statusList = new javax.swing.JList();
    final javax.swing.JCheckBox summaryCheckBox = new javax.swing.JCheckBox();
    private javax.swing.JPanel tableFieldsPanel;
    private javax.swing.JPanel tableHeaderPanel;
    final javax.swing.JPanel tablePanel = new javax.swing.JPanel();
    final javax.swing.JLabel tableSummaryLabel = new javax.swing.JLabel();
    final javax.swing.JLabel typeLabel = new javax.swing.JLabel();
    final javax.swing.JList typeList = new javax.swing.JList();
    final javax.swing.JTextField updatedFromTextField = new javax.swing.JTextField();
    final javax.swing.JTextField updatedToTextField = new javax.swing.JTextField();
    final javax.swing.JLabel warningLabel = new javax.swing.JLabel();
    final org.netbeans.modules.bugtracking.util.LinkButton webButton = new org.netbeans.modules.bugtracking.util.LinkButton();
    // End of variables declaration//GEN-END:variables

    /**
     * Sets the issue prefix' text. Null value hides the field
     * @param text
     */
    public void setIssuePrefixText (String text) {
        if (text == null) {
            lblIssueKeyPrefix.setVisible(false);
        } else {
            lblIssueKeyPrefix.setText(text);
            lblIssueKeyPrefix.setVisible(true);
        }
    }

    /**
     * Returns the value of issue' prefix
     * @return
     */
    public String getIssuePrefixText () {
        return lblIssueKeyPrefix.getText();
    }

    /**
     * enables/disables all but the parameter fields
     * @param enable
     */
    void enableFields(boolean enable) {
        queryLabel.setEnabled(enable);

        projectLabel.setEnabled(enable);
        typeLabel.setEnabled(enable);
        jLabel2.setEnabled(enable);
        jLabel8.setEnabled(enable);
        projectLabel1.setEnabled(enable);
        projectLabel2.setEnabled(enable);
        projectLabel3.setEnabled(enable);

        projectList.setEnabled(enable);
        typeList.setEnabled(enable);
        statusList.setEnabled(enable);
        resolutionList.setEnabled(enable);
        priorityList.setEnabled(enable);

        searchButton.setEnabled(enable);
        saveButton.setEnabled(enable);
        webButton.setEnabled(enable);

        refreshCheckBox.setEnabled(enable);

        queryTextField.setEnabled(enable);
        summaryCheckBox.setEnabled(enable);
        descriptionCheckBox.setEnabled(enable);
        commentsCheckBox.setEnabled(enable);
        environmentCheckBox.setEnabled(enable);
        reloadAttributesButton.setEnabled(enable);
        reporterComboBox.setEnabled(enable);
        reporterTextField.setEnabled(enable);
        assigneeComboBox.setEnabled(enable);
        assigneeTextField.setEnabled(enable);
    }

    void switchQueryFields(boolean showAdvanced) {
        byDetails.setVisible(showAdvanced);
        byText.setVisible(showAdvanced);
        byOther.setVisible(showAdvanced);
        byPeople.setVisible(showAdvanced);
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
        webButton.setVisible(!b);
        separatorLabel1.setVisible(!b);
        separatorLabel2.setVisible(!b);
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
    
    // XXX reuse with bugzilla
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
