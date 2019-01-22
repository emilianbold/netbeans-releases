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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.odcs.tasks.query;

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
import org.netbeans.modules.bugtracking.commons.UIUtils;

/**
 *
 * 
 */
public class QueryPanel extends javax.swing.JPanel implements FocusListener {

    final ExpandablePanel byText;
    final ExpandablePanel byDetails;
    final ExpandablePanel byProperties;
    final ExpandablePanel byLastChange;
    final ExpandablePanel byPeople;
    private static final Color ERROR_COLOR = new Color(153,0,0);
    private Color defaultTextColor;

    /** Creates new form QueryPanel */
    public QueryPanel(JComponent tableComponent) {
        initComponents();

        Font f = new JLabel().getFont();
        int s = f.getSize();
        nameLabel.setFont(jLabel1.getFont().deriveFont(s * 1.7f));
        defaultTextColor = noContentLabel.getForeground();

        Color bkColor = UIUtils.getSectionPanelBackground();
        gotoPanel.setBackground( bkColor );
        tablePanel.setBackground( bkColor );
        criteriaPanel.setBackground( bkColor );
    
        tablePanel.add(tableComponent);

        JTree tv = new JTree();
        BasicTreeUI tvui = (BasicTreeUI) tv.getUI();
        Icon ei = tvui.getExpandedIcon();
        Icon ci = tvui.getCollapsedIcon();

        byTextContainer.add(byTextPanel);
        byDetailsContainer.add(byDetailsPanel);
        byPropertiesContainer.add(byPropertiesPanel);
        byLastChangeContainer.add(byDatePanel);
        byPeopleContainer.add(byPeoplePanel);

        byText = new ExpandablePanel(byTextLabel, byTextContainer, ei, ci);
        byDetails = new ExpandablePanel(byDetailsLabel, byDetailsContainer, ei, ci);
        byProperties = new ExpandablePanel(byPropertiesLabel, byPropertiesContainer, ei, ci);
        byLastChange = new ExpandablePanel(byLastChangeLabel, byLastChangeContainer, ei, ci);
        byPeople = new ExpandablePanel(byPeopleLabel, byPeopleContainer, ei, ci);

        byText.expand();
        byDetails.expand();
        byProperties.expand();
        byLastChange.colapse();
        byPeople.colapse();

        queryHeaderPanel.setVisible(false);
        tableFieldsPanel.setVisible(false);
        cancelChangesButton.setVisible(false);
        filterComboBox.setVisible(false);
        filterLabel.setVisible(false);
        noContentPanel.setVisible(false);

        creatorCheckBox.setOpaque(false);
        ownerCheckBox.setOpaque(false);
        commenterCheckBox.setOpaque(false);
        ccCheckBox.setOpaque(false);
        searchByDescriptionCheckBox.setOpaque(false);
        searchBySummaryCheckBox.setOpaque(false);

        cancelChangesButton.setEnabled(false);
        
        filterComboBox.setRenderer(new FilterCellRenderer());

        UIUtils.keepFocusedComponentVisible(this);

        validate();
        repaint();
    }

    void setRemoteInvocationRunning(final boolean running) {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                modifyButton.setEnabled(!running);
                seenButton.setEnabled(!running);
                removeButton.setEnabled(!running);
                refreshButton.setEnabled(!running);
                cloneQueryButton.setEnabled(!running);
                findIssuesButton.setEnabled(!running);
                filterLabel.setEnabled(!running);
                filterComboBox.setEnabled(!running);
                
                saveChangesButton.setEnabled(!running);
                cancelChangesButton.setEnabled(!running);                
            }
        };
        UIUtils.runInAWT(r);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        byDatePanel = new javax.swing.JPanel();
        startLabel = new javax.swing.JLabel();
        endLabel = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        byPeoplePanel = new javax.swing.JPanel();
        byTextPanel = new javax.swing.JPanel();
        tableFieldsPanel = new javax.swing.JPanel();
        tableHeaderPanel = new javax.swing.JPanel();
        filterLabel = new javax.swing.JLabel();
        criteriaPanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        queryHeaderPanel = new javax.swing.JPanel();
        lastRefreshLabel = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        separatorLabel1 = new javax.swing.JLabel();
        noContentPanel = new javax.swing.JPanel();
        noContentLabel = new javax.swing.JLabel();

        byDatePanel.setBackground(javax.swing.UIManager.getDefaults().getColor("TextArea.background"));

        startLabel.setLabelFor(startTextField);
        org.openide.awt.Mnemonics.setLocalizedText(startLabel, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.startLabel.text")); // NOI18N

        startTextField.setColumns(12);

        endLabel.setLabelFor(endTextField);
        org.openide.awt.Mnemonics.setLocalizedText(endLabel, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.endLabel.text")); // NOI18N

        endTextField.setColumns(12);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.jLabel3.text")); // NOI18N
        jLabel3.setEnabled(false);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel9, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.jLabel9.text")); // NOI18N
        jLabel9.setEnabled(false);

        javax.swing.GroupLayout byDatePanelLayout = new javax.swing.GroupLayout(byDatePanel);
        byDatePanel.setLayout(byDatePanelLayout);
        byDatePanelLayout.setHorizontalGroup(
            byDatePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(byDatePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(byDateComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(startLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(byDatePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(byDatePanelLayout.createSequentialGroup()
                        .addComponent(startTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(endLabel))
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(byDatePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel9)
                    .addComponent(endTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        byDatePanelLayout.setVerticalGroup(
            byDatePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(byDatePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(byDatePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(byDatePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(endLabel)
                        .addComponent(endTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(byDatePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(byDateComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(startLabel)
                        .addComponent(startTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(byDatePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3)
                    .addComponent(jLabel9))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        byPeoplePanel.setBackground(javax.swing.UIManager.getDefaults().getColor("TextArea.background"));

        ownerCheckBox.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(ownerCheckBox, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.ownerCheckBox.text")); // NOI18N

        creatorCheckBox.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(creatorCheckBox, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.creatorCheckBox.text")); // NOI18N

        commenterCheckBox.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(commenterCheckBox, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.commenterCheckBox.text")); // NOI18N

        ccCheckBox.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(ccCheckBox, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.ccCheckBox.text")); // NOI18N

        userLabel.setFont(userLabel.getFont().deriveFont(userLabel.getFont().getStyle() | java.awt.Font.BOLD));
        userLabel.setLabelFor(userList);
        org.openide.awt.Mnemonics.setLocalizedText(userLabel, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.userLabel.text")); // NOI18N

        userScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        userList.setMinimumSize(new java.awt.Dimension(100, 2));
        userList.setVisibleRowCount(6);
        userScrollPane.setViewportView(userList);

        javax.swing.GroupLayout byPeoplePanelLayout = new javax.swing.GroupLayout(byPeoplePanel);
        byPeoplePanel.setLayout(byPeoplePanelLayout);
        byPeoplePanelLayout.setHorizontalGroup(
            byPeoplePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(byPeoplePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(byPeoplePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(userLabel)
                    .addGroup(byPeoplePanelLayout.createSequentialGroup()
                        .addComponent(userScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(creatorCheckBox)
                        .addGap(15, 15, 15)
                        .addComponent(ownerCheckBox)
                        .addGap(15, 15, 15)
                        .addComponent(commenterCheckBox)
                        .addGap(15, 15, 15)
                        .addComponent(ccCheckBox)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        byPeoplePanelLayout.setVerticalGroup(
            byPeoplePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, byPeoplePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(userLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(byPeoplePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(userScrollPane)
                    .addGroup(byPeoplePanelLayout.createSequentialGroup()
                        .addGroup(byPeoplePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(ownerCheckBox)
                            .addComponent(creatorCheckBox)
                            .addComponent(commenterCheckBox)
                            .addComponent(ccCheckBox))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );

        byDetailsPanel.setBackground(javax.swing.UIManager.getDefaults().getColor("TextArea.background"));

        productLabel.setFont(productLabel.getFont().deriveFont(productLabel.getFont().getStyle() | java.awt.Font.BOLD));
        productLabel.setLabelFor(productList);
        org.openide.awt.Mnemonics.setLocalizedText(productLabel, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.productLabel.text")); // NOI18N

        releaseLabel.setFont(releaseLabel.getFont().deriveFont(releaseLabel.getFont().getStyle() | java.awt.Font.BOLD));
        releaseLabel.setLabelFor(releaseList);
        org.openide.awt.Mnemonics.setLocalizedText(releaseLabel, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.releaseLabel.text")); // NOI18N

        jScrollPane2.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        releaseList.setMinimumSize(new java.awt.Dimension(100, 2));
        releaseList.setVisibleRowCount(6);
        jScrollPane2.setViewportView(releaseList);
        releaseList.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.releaseList.AccessibleContext.accessibleDescription")); // NOI18N

        componentLabel.setFont(componentLabel.getFont().deriveFont(componentLabel.getFont().getStyle() | java.awt.Font.BOLD));
        componentLabel.setLabelFor(componentList);
        org.openide.awt.Mnemonics.setLocalizedText(componentLabel, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.componentLabel.text")); // NOI18N

        jScrollPane6.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        componentList.setMinimumSize(new java.awt.Dimension(100, 2));
        componentList.setVisibleRowCount(6);
        jScrollPane6.setViewportView(componentList);
        componentList.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.componentList.AccessibleContext.accessibleDescription")); // NOI18N

        jScrollPane7.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        productList.setMinimumSize(new java.awt.Dimension(100, 2));
        productList.setVisibleRowCount(6);
        jScrollPane7.setViewportView(productList);
        productList.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.productList.AccessibleContext.accessibleDescription")); // NOI18N

        iterationLabel.setFont(iterationLabel.getFont().deriveFont(iterationLabel.getFont().getStyle() | java.awt.Font.BOLD));
        iterationLabel.setLabelFor(iterationList);
        org.openide.awt.Mnemonics.setLocalizedText(iterationLabel, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.iterationLabel.text")); // NOI18N

        tmScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        iterationList.setMinimumSize(new java.awt.Dimension(100, 2));
        iterationList.setVisibleRowCount(6);
        tmScrollPane.setViewportView(iterationList);

        javax.swing.GroupLayout byDetailsPanelLayout = new javax.swing.GroupLayout(byDetailsPanel);
        byDetailsPanel.setLayout(byDetailsPanelLayout);
        byDetailsPanelLayout.setHorizontalGroup(
            byDetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(byDetailsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(byDetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(productLabel)
                    .addComponent(jScrollPane7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(byDetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(componentLabel)
                    .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(byDetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(releaseLabel)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(byDetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(iterationLabel)
                    .addComponent(tmScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        byDetailsPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {componentLabel, jScrollPane6});

        byDetailsPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jScrollPane2, releaseLabel});

        byDetailsPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {iterationLabel, tmScrollPane});

        byDetailsPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jScrollPane7, productLabel});

        byDetailsPanelLayout.setVerticalGroup(
            byDetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(byDetailsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(byDetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(productLabel)
                    .addComponent(componentLabel)
                    .addComponent(releaseLabel)
                    .addComponent(iterationLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(byDetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane7)
                    .addComponent(jScrollPane6)
                    .addComponent(jScrollPane2)
                    .addComponent(tmScrollPane))
                .addContainerGap())
        );

        byTextPanel.setBackground(javax.swing.UIManager.getDefaults().getColor("TextArea.background"));

        byTextTextField.setColumns(40);

        searchBySummaryCheckBox.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(searchBySummaryCheckBox, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.searchBySummaryCheckBox.text")); // NOI18N

        searchByDescriptionCheckBox.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(searchByDescriptionCheckBox, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.searchByDescriptionCheckBox.text")); // NOI18N

        javax.swing.GroupLayout byTextPanelLayout = new javax.swing.GroupLayout(byTextPanel);
        byTextPanel.setLayout(byTextPanelLayout);
        byTextPanelLayout.setHorizontalGroup(
            byTextPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(byTextPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(byTextPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(byTextPanelLayout.createSequentialGroup()
                        .addComponent(searchBySummaryCheckBox)
                        .addGap(18, 18, 18)
                        .addComponent(searchByDescriptionCheckBox))
                    .addComponent(byTextTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        byTextPanelLayout.setVerticalGroup(
            byTextPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(byTextPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(byTextTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(byTextPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(searchBySummaryCheckBox)
                    .addComponent(searchByDescriptionCheckBox))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        byTextTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.byTextTextField.AccessibleContext.accessibleName")); // NOI18N
        byTextTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.byTextTextField.AccessibleContext.accessibleDescription")); // NOI18N

        byPropertiesPanel.setBackground(javax.swing.UIManager.getDefaults().getColor("TextArea.background"));

        jScrollPane10.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        priorityList.setMinimumSize(new java.awt.Dimension(100, 2));
        priorityList.setVisibleRowCount(6);
        jScrollPane10.setViewportView(priorityList);

        priorityLabel.setFont(priorityLabel.getFont().deriveFont(priorityLabel.getFont().getStyle() | java.awt.Font.BOLD));
        priorityLabel.setLabelFor(priorityList);
        org.openide.awt.Mnemonics.setLocalizedText(priorityLabel, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.priorityLabel.text")); // NOI18N

        severityLabel.setFont(severityLabel.getFont().deriveFont(severityLabel.getFont().getStyle() | java.awt.Font.BOLD));
        severityLabel.setLabelFor(severityList);
        org.openide.awt.Mnemonics.setLocalizedText(severityLabel, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.severityLabel.text")); // NOI18N

        severityScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        severityList.setMinimumSize(new java.awt.Dimension(100, 2));
        severityList.setVisibleRowCount(6);
        severityScrollPane1.setViewportView(severityList);

        issueTypeLabel.setFont(issueTypeLabel.getFont().deriveFont(issueTypeLabel.getFont().getStyle() | java.awt.Font.BOLD));
        issueTypeLabel.setLabelFor(issueTypeList);
        org.openide.awt.Mnemonics.setLocalizedText(issueTypeLabel, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.issueTypeLabel.text")); // NOI18N

        issueTypeScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        issueTypeList.setMinimumSize(new java.awt.Dimension(100, 2));
        issueTypeList.setVisibleRowCount(6);
        issueTypeScrollPane1.setViewportView(issueTypeList);

        keywordsLabel.setFont(keywordsLabel.getFont().deriveFont(keywordsLabel.getFont().getStyle() | java.awt.Font.BOLD));
        keywordsLabel.setLabelFor(keywordsList);
        org.openide.awt.Mnemonics.setLocalizedText(keywordsLabel, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.keywordsLabel.text")); // NOI18N

        severityScrollPane2.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        keywordsList.setMinimumSize(new java.awt.Dimension(100, 2));
        keywordsList.setVisibleRowCount(6);
        severityScrollPane2.setViewportView(keywordsList);

        statusLabel.setFont(statusLabel.getFont().deriveFont(statusLabel.getFont().getStyle() | java.awt.Font.BOLD));
        statusLabel.setLabelFor(statusList);
        org.openide.awt.Mnemonics.setLocalizedText(statusLabel, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.statusLabel.text")); // NOI18N

        jScrollPane9.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        statusList.setMinimumSize(new java.awt.Dimension(100, 2));
        statusList.setVisibleRowCount(6);
        jScrollPane9.setViewportView(statusList);

        resolutionLabel.setFont(resolutionLabel.getFont().deriveFont(resolutionLabel.getFont().getStyle() | java.awt.Font.BOLD));
        resolutionLabel.setLabelFor(resolutionList);
        org.openide.awt.Mnemonics.setLocalizedText(resolutionLabel, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.resolutionLabel.text")); // NOI18N

        jScrollPane11.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        resolutionList.setMinimumSize(new java.awt.Dimension(100, 2));
        resolutionList.setVisibleRowCount(6);
        jScrollPane11.setViewportView(resolutionList);

        javax.swing.GroupLayout byPropertiesPanelLayout = new javax.swing.GroupLayout(byPropertiesPanel);
        byPropertiesPanel.setLayout(byPropertiesPanelLayout);
        byPropertiesPanelLayout.setHorizontalGroup(
            byPropertiesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(byPropertiesPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(byPropertiesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(issueTypeLabel)
                    .addComponent(issueTypeScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(byPropertiesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(priorityLabel)
                    .addComponent(jScrollPane10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(byPropertiesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(severityLabel)
                    .addComponent(severityScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(byPropertiesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(statusLabel)
                    .addComponent(jScrollPane9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(byPropertiesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(resolutionLabel)
                    .addComponent(jScrollPane11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(byPropertiesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(keywordsLabel)
                    .addComponent(severityScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        byPropertiesPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {issueTypeLabel, issueTypeScrollPane1});

        byPropertiesPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jScrollPane10, priorityLabel});

        byPropertiesPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {severityLabel, severityScrollPane1});

        byPropertiesPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jScrollPane9, statusLabel});

        byPropertiesPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jScrollPane11, resolutionLabel});

        byPropertiesPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {keywordsLabel, severityScrollPane2});

        byPropertiesPanelLayout.setVerticalGroup(
            byPropertiesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(byPropertiesPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(byPropertiesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(keywordsLabel)
                    .addComponent(issueTypeLabel)
                    .addComponent(priorityLabel)
                    .addComponent(severityLabel)
                    .addComponent(statusLabel)
                    .addComponent(resolutionLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(byPropertiesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(severityScrollPane2)
                    .addComponent(issueTypeScrollPane1)
                    .addComponent(jScrollPane10)
                    .addComponent(severityScrollPane1)
                    .addComponent(jScrollPane9)
                    .addComponent(jScrollPane11))
                .addContainerGap())
        );

        setBackground(javax.swing.UIManager.getDefaults().getColor("TextArea.background"));

        tableFieldsPanel.setBackground(javax.swing.UIManager.getDefaults().getColor("TextArea.background"));

        tablePanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        tablePanel.setLayout(new java.awt.BorderLayout());

        tableHeaderPanel.setBackground(javax.swing.UIManager.getDefaults().getColor("TextArea.background"));

        org.openide.awt.Mnemonics.setLocalizedText(tableSummaryLabel, "jLabel2");

        filterComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        filterLabel.setLabelFor(filterComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(filterLabel, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.filterLabel.text_1")); // NOI18N

        javax.swing.GroupLayout tableHeaderPanelLayout = new javax.swing.GroupLayout(tableHeaderPanel);
        tableHeaderPanel.setLayout(tableHeaderPanelLayout);
        tableHeaderPanelLayout.setHorizontalGroup(
            tableHeaderPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(tableHeaderPanelLayout.createSequentialGroup()
                .addComponent(tableSummaryLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(filterLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(filterComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        tableHeaderPanelLayout.setVerticalGroup(
            tableHeaderPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(tableHeaderPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(tableSummaryLabel)
                .addComponent(filterComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(filterLabel))
        );

        filterComboBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.filterComboBox.AccessibleContext.accessibleDescription")); // NOI18N

        javax.swing.GroupLayout tableFieldsPanelLayout = new javax.swing.GroupLayout(tableFieldsPanel);
        tableFieldsPanel.setLayout(tableFieldsPanelLayout);
        tableFieldsPanelLayout.setHorizontalGroup(
            tableFieldsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(tablePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(tableHeaderPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        tableFieldsPanelLayout.setVerticalGroup(
            tableFieldsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(tableFieldsPanelLayout.createSequentialGroup()
                .addComponent(tableHeaderPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tablePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        searchPanel.setBackground(javax.swing.UIManager.getDefaults().getColor("TextArea.background"));

        org.openide.awt.Mnemonics.setLocalizedText(searchButton, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.searchButton.text")); // NOI18N

        byTextLabel.setFont(byTextLabel.getFont().deriveFont(byTextLabel.getFont().getStyle() | java.awt.Font.BOLD));
        org.openide.awt.Mnemonics.setLocalizedText(byTextLabel, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.byTextLabel.text_1")); // NOI18N

        byTextContainer.setLayout(new java.awt.BorderLayout());

        byDetailsLabel.setFont(byDetailsLabel.getFont().deriveFont(byDetailsLabel.getFont().getStyle() | java.awt.Font.BOLD));
        org.openide.awt.Mnemonics.setLocalizedText(byDetailsLabel, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.byDetailsLabel.text")); // NOI18N

        byDetailsContainer.setLayout(new java.awt.BorderLayout());

        byPropertiesLabel.setFont(byPropertiesLabel.getFont().deriveFont(byPropertiesLabel.getFont().getStyle() | java.awt.Font.BOLD));
        org.openide.awt.Mnemonics.setLocalizedText(byPropertiesLabel, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.byPropertiesLabel.text")); // NOI18N

        byPropertiesContainer.setLayout(new java.awt.BorderLayout());

        byPeopleLabel.setFont(byPeopleLabel.getFont().deriveFont(byPeopleLabel.getFont().getStyle() | java.awt.Font.BOLD));
        org.openide.awt.Mnemonics.setLocalizedText(byPeopleLabel, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.byPeopleLabel.text")); // NOI18N

        byPeopleContainer.setLayout(new java.awt.BorderLayout());

        byLastChangeLabel.setFont(byLastChangeLabel.getFont().deriveFont(byLastChangeLabel.getFont().getStyle() | java.awt.Font.BOLD));
        org.openide.awt.Mnemonics.setLocalizedText(byLastChangeLabel, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.byLastChangeLabel.text")); // NOI18N

        byLastChangeContainer.setLayout(new java.awt.BorderLayout());

        javax.swing.GroupLayout criteriaPanelLayout = new javax.swing.GroupLayout(criteriaPanel);
        criteriaPanel.setLayout(criteriaPanelLayout);
        criteriaPanelLayout.setHorizontalGroup(
            criteriaPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(byPropertiesContainer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(byTextContainer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(byLastChangeContainer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(byPeopleContainer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(byDetailsContainer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(criteriaPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(criteriaPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(byLastChangeLabel)
                    .addComponent(byTextLabel)
                    .addComponent(byDetailsLabel)
                    .addComponent(byPropertiesLabel)
                    .addComponent(byPeopleLabel))
                .addGap(0, 0, Short.MAX_VALUE))
        );
        criteriaPanelLayout.setVerticalGroup(
            criteriaPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(criteriaPanelLayout.createSequentialGroup()
                .addComponent(byTextLabel)
                .addGap(0, 0, 0)
                .addComponent(byTextContainer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(byDetailsLabel)
                .addGap(0, 0, 0)
                .addComponent(byDetailsContainer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(byPropertiesLabel)
                .addGap(0, 0, 0)
                .addComponent(byPropertiesContainer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(byLastChangeLabel)
                .addGap(0, 0, 0)
                .addComponent(byLastChangeContainer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(byPeopleLabel)
                .addGap(0, 0, 0)
                .addComponent(byPeopleContainer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        org.openide.awt.Mnemonics.setLocalizedText(cancelChangesButton, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.cancelChangesButton.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(saveChangesButton, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.saveQueryButton.text")); // NOI18N

        jLabel1.setLabelFor(idTextField);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.jLabel1.text_1")); // NOI18N

        idTextField.setColumns(6);

        org.openide.awt.Mnemonics.setLocalizedText(gotoIssueButton, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.gotoIssueButton.text")); // NOI18N

        javax.swing.GroupLayout gotoPanelLayout = new javax.swing.GroupLayout(gotoPanel);
        gotoPanel.setLayout(gotoPanelLayout);
        gotoPanelLayout.setHorizontalGroup(
            gotoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(gotoPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(idTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(gotoIssueButton)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        gotoPanelLayout.setVerticalGroup(
            gotoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(gotoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(gotoIssueButton)
                .addComponent(jLabel1)
                .addComponent(idTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        idTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.idTextField.AccessibleContext.accessibleDescription")); // NOI18N
        gotoIssueButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.gotoIssueButton.AccessibleContext.accessibleDescription")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(refreshConfigurationButton, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.refreshConfigurationButton.text")); // NOI18N

        javax.swing.GroupLayout searchPanelLayout = new javax.swing.GroupLayout(searchPanel);
        searchPanel.setLayout(searchPanelLayout);
        searchPanelLayout.setHorizontalGroup(
            searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(gotoPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(criteriaPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(searchPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(searchButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(saveChangesButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cancelChangesButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(refreshConfigurationButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        searchPanelLayout.setVerticalGroup(
            searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(searchPanelLayout.createSequentialGroup()
                .addComponent(gotoPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(criteriaPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(refreshConfigurationButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cancelChangesButton)
                    .addComponent(saveChangesButton)
                    .addComponent(searchButton))
                .addGap(6, 6, 6))
        );

        searchButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.searchButton.AccessibleContext.accessibleDescription")); // NOI18N
        cancelChangesButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.cancelChangesButton.AccessibleContext.accessibleDescription")); // NOI18N
        saveChangesButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.saveChangesButton.AccessibleContext.accessibleDescription")); // NOI18N
        refreshConfigurationButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.refreshConfigurationButton.AccessibleContext.accessibleDescription")); // NOI18N

        queryHeaderPanel.setBackground(javax.swing.UIManager.getDefaults().getColor("TextArea.background"));

        org.openide.awt.Mnemonics.setLocalizedText(seenButton, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.seenButton.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lastRefreshLabel, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.lastRefreshLabel.text_1")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(removeButton, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.removeButton.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lastRefreshDateLabel, "jLabel4");

        org.openide.awt.Mnemonics.setLocalizedText(refreshButton, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.refreshButton.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(nameLabel, "jLabel4");

        org.openide.awt.Mnemonics.setLocalizedText(modifyButton, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.modifyButton.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.jLabel4.text_1")); // NOI18N
        jLabel4.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        org.openide.awt.Mnemonics.setLocalizedText(jLabel5, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.jLabel5.text")); // NOI18N
        jLabel5.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        org.openide.awt.Mnemonics.setLocalizedText(jLabel6, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.jLabel6.text")); // NOI18N
        jLabel6.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        org.openide.awt.Mnemonics.setLocalizedText(jLabel7, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.jLabel7.text")); // NOI18N
        jLabel7.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        org.openide.awt.Mnemonics.setLocalizedText(findIssuesButton, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.findIssuesButton.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel8, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.jLabel8.text")); // NOI18N
        jLabel8.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        org.openide.awt.Mnemonics.setLocalizedText(cloneQueryButton, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.cloneQueryButton.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(webButton, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.webButton.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(separatorLabel1, org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.separatorLabel1.text")); // NOI18N
        separatorLabel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        javax.swing.GroupLayout queryHeaderPanelLayout = new javax.swing.GroupLayout(queryHeaderPanel);
        queryHeaderPanel.setLayout(queryHeaderPanelLayout);
        queryHeaderPanelLayout.setHorizontalGroup(
            queryHeaderPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(queryHeaderPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(queryHeaderPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(queryHeaderPanelLayout.createSequentialGroup()
                        .addComponent(nameLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(lastRefreshLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lastRefreshDateLabel))
                    .addGroup(queryHeaderPanelLayout.createSequentialGroup()
                        .addComponent(seenButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(modifyButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(refreshButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(removeButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(findIssuesButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cloneQueryButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(separatorLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(webButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        queryHeaderPanelLayout.setVerticalGroup(
            queryHeaderPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(queryHeaderPanelLayout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addGroup(queryHeaderPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(nameLabel)
                    .addComponent(lastRefreshDateLabel)
                    .addComponent(lastRefreshLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(queryHeaderPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(webButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cloneQueryButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(findIssuesButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(seenButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(removeButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(refreshButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(modifyButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(separatorLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(6, 6, 6))
        );

        seenButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.refreshButton.AccessibleContext.accessibleDescription")); // NOI18N
        removeButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.removeButton.AccessibleContext.accessibleDescription")); // NOI18N
        refreshButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.seenButton.AccessibleContext.accessibleDescription")); // NOI18N
        modifyButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.modifyButton.AccessibleContext.accessibleDescription")); // NOI18N
        webButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.webButton.AccessibleContext.accessibleDescription")); // NOI18N

        noContentPanel.setBackground(javax.swing.UIManager.getDefaults().getColor("TextArea.background"));
        noContentPanel.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(noContentLabel, "jLabel9");
        noContentPanel.add(noContentLabel, new java.awt.GridBagConstraints());

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(noContentPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(queryHeaderPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(searchPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tableFieldsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(queryHeaderPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(searchPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tableFieldsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(noContentPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    final javax.swing.JComboBox byDateComboBox = new javax.swing.JComboBox();
    private javax.swing.JPanel byDatePanel;
    final javax.swing.JPanel byDetailsContainer = new javax.swing.JPanel();
    final javax.swing.JLabel byDetailsLabel = new javax.swing.JLabel();
    final javax.swing.JPanel byDetailsPanel = new javax.swing.JPanel();
    final javax.swing.JPanel byLastChangeContainer = new javax.swing.JPanel();
    final javax.swing.JLabel byLastChangeLabel = new javax.swing.JLabel();
    final javax.swing.JPanel byPeopleContainer = new javax.swing.JPanel();
    final javax.swing.JLabel byPeopleLabel = new javax.swing.JLabel();
    private javax.swing.JPanel byPeoplePanel;
    final javax.swing.JPanel byPropertiesContainer = new javax.swing.JPanel();
    final javax.swing.JLabel byPropertiesLabel = new javax.swing.JLabel();
    final javax.swing.JPanel byPropertiesPanel = new javax.swing.JPanel();
    final javax.swing.JPanel byTextContainer = new javax.swing.JPanel();
    final javax.swing.JLabel byTextLabel = new javax.swing.JLabel();
    private javax.swing.JPanel byTextPanel;
    final javax.swing.JTextField byTextTextField = new javax.swing.JTextField();
    final javax.swing.JButton cancelChangesButton = new javax.swing.JButton();
    final javax.swing.JCheckBox ccCheckBox = new javax.swing.JCheckBox();
    public final org.netbeans.modules.bugtracking.commons.LinkButton cloneQueryButton = new org.netbeans.modules.bugtracking.commons.LinkButton();
    final javax.swing.JCheckBox commenterCheckBox = new javax.swing.JCheckBox();
    final javax.swing.JLabel componentLabel = new javax.swing.JLabel();
    final javax.swing.JList componentList = new javax.swing.JList();
    final javax.swing.JCheckBox creatorCheckBox = new javax.swing.JCheckBox();
    private javax.swing.JPanel criteriaPanel;
    private javax.swing.JLabel endLabel;
    final javax.swing.JTextField endTextField = new javax.swing.JTextField();
    final javax.swing.JComboBox filterComboBox = new javax.swing.JComboBox();
    private javax.swing.JLabel filterLabel;
    public final org.netbeans.modules.bugtracking.commons.LinkButton findIssuesButton = new org.netbeans.modules.bugtracking.commons.LinkButton();
    final javax.swing.JButton gotoIssueButton = new javax.swing.JButton();
    final javax.swing.JPanel gotoPanel = new javax.swing.JPanel();
    final javax.swing.JTextField idTextField = new javax.swing.JTextField();
    final javax.swing.JLabel issueTypeLabel = new javax.swing.JLabel();
    final javax.swing.JList issueTypeList = new javax.swing.JList();
    final javax.swing.JScrollPane issueTypeScrollPane1 = new HackedScrollPane();
    final javax.swing.JLabel iterationLabel = new javax.swing.JLabel();
    final javax.swing.JList iterationList = new javax.swing.JList();
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    final javax.swing.JScrollPane jScrollPane10 = new HackedScrollPane();
    final javax.swing.JScrollPane jScrollPane11 = new HackedScrollPane();
    final javax.swing.JScrollPane jScrollPane2 = new HackedScrollPane();
    final javax.swing.JScrollPane jScrollPane6 = new HackedScrollPane();
    final javax.swing.JScrollPane jScrollPane7 = new javax.swing.JScrollPane();
    final javax.swing.JScrollPane jScrollPane9 = new HackedScrollPane();
    final javax.swing.JLabel keywordsLabel = new javax.swing.JLabel();
    final javax.swing.JList keywordsList = new javax.swing.JList();
    final javax.swing.JLabel lastRefreshDateLabel = new javax.swing.JLabel();
    private javax.swing.JLabel lastRefreshLabel;
    public final org.netbeans.modules.bugtracking.commons.LinkButton modifyButton = new org.netbeans.modules.bugtracking.commons.LinkButton();
    final javax.swing.JLabel nameLabel = new javax.swing.JLabel();
    private javax.swing.JLabel noContentLabel;
    private javax.swing.JPanel noContentPanel;
    final javax.swing.JCheckBox ownerCheckBox = new javax.swing.JCheckBox();
    final javax.swing.JLabel priorityLabel = new javax.swing.JLabel();
    final javax.swing.JList priorityList = new javax.swing.JList();
    final javax.swing.JLabel productLabel = new javax.swing.JLabel();
    final javax.swing.JList productList = new javax.swing.JList();
    private javax.swing.JPanel queryHeaderPanel;
    final org.netbeans.modules.bugtracking.commons.LinkButton refreshButton = new org.netbeans.modules.bugtracking.commons.LinkButton();
    final org.netbeans.modules.bugtracking.commons.LinkButton refreshConfigurationButton = new org.netbeans.modules.bugtracking.commons.LinkButton();
    final javax.swing.JLabel releaseLabel = new javax.swing.JLabel();
    final javax.swing.JList releaseList = new javax.swing.JList();
    public final org.netbeans.modules.bugtracking.commons.LinkButton removeButton = new org.netbeans.modules.bugtracking.commons.LinkButton();
    final javax.swing.JLabel resolutionLabel = new javax.swing.JLabel();
    final javax.swing.JList resolutionList = new javax.swing.JList();
    final javax.swing.JButton saveChangesButton = new javax.swing.JButton();
    final javax.swing.JButton searchButton = new javax.swing.JButton();
    final javax.swing.JCheckBox searchByDescriptionCheckBox = new javax.swing.JCheckBox();
    final javax.swing.JCheckBox searchBySummaryCheckBox = new javax.swing.JCheckBox();
    final javax.swing.JPanel searchPanel = new javax.swing.JPanel();
    final org.netbeans.modules.bugtracking.commons.LinkButton seenButton = new org.netbeans.modules.bugtracking.commons.LinkButton();
    private javax.swing.JLabel separatorLabel1;
    final javax.swing.JLabel severityLabel = new javax.swing.JLabel();
    final javax.swing.JList severityList = new javax.swing.JList();
    final javax.swing.JScrollPane severityScrollPane1 = new HackedScrollPane();
    final javax.swing.JScrollPane severityScrollPane2 = new HackedScrollPane();
    private javax.swing.JLabel startLabel;
    final javax.swing.JTextField startTextField = new javax.swing.JTextField();
    final javax.swing.JLabel statusLabel = new javax.swing.JLabel();
    final javax.swing.JList statusList = new javax.swing.JList();
    private javax.swing.JPanel tableFieldsPanel;
    private javax.swing.JPanel tableHeaderPanel;
    final javax.swing.JPanel tablePanel = new javax.swing.JPanel();
    final javax.swing.JLabel tableSummaryLabel = new javax.swing.JLabel();
    final javax.swing.JScrollPane tmScrollPane = new HackedScrollPane();
    final javax.swing.JLabel userLabel = new javax.swing.JLabel();
    final javax.swing.JList userList = new javax.swing.JList();
    final javax.swing.JScrollPane userScrollPane = new HackedScrollPane();
    final org.netbeans.modules.bugtracking.commons.LinkButton webButton = new org.netbeans.modules.bugtracking.commons.LinkButton();
    // End of variables declaration//GEN-END:variables

    /**
     * enables/disables all but the parameter fields
     * @param enable
     */
    void enableFields(final boolean enable) {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                idTextField.setEnabled(enable);
                gotoIssueButton.setEnabled(enable);

                searchBySummaryCheckBox.setEnabled(enable);
                searchByDescriptionCheckBox.setEnabled(enable);
                
                productLabel.setEnabled(enable);
                componentLabel.setEnabled(enable);
                releaseLabel.setEnabled(enable);
                statusLabel.setEnabled(enable);
                severityLabel.setEnabled(enable);
                resolutionLabel.setEnabled(enable);
                priorityLabel.setEnabled(enable);
                iterationLabel.setEnabled(enable);
                issueTypeLabel.setEnabled(enable);
                
                searchButton.setEnabled(enable);
                webButton.setEnabled(enable);
                refreshConfigurationButton.setEnabled(enable);
            }
        };
        UIUtils.runInAWT(r);
    }

    void switchQueryFields(final boolean showAdvanced) {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                byDetails.setVisible(showAdvanced);
                byText.setVisible(showAdvanced);
                byLastChange.setVisible(showAdvanced);
                byPeople.setVisible(showAdvanced);
            }
        };
        UIUtils.runInAWT(r);
    }

    void showError(final String text) {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                noContentPanel.setVisible(true);
                tableSummaryLabel.setVisible(false);
                tableFieldsPanel.setVisible(false);
                if (text != null) {
                    noContentLabel.setForeground(ERROR_COLOR);
                    noContentLabel.setText(text);
                }
            }
        };
        UIUtils.runInAWT(r);
    }

    void showSearchingProgress(final boolean on, final String text) {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                noContentPanel.setVisible(on);
                if (!on) {
                    if (QueryPanel.this.isShowing()) {
                        if (!tableFieldsPanel.isVisible()) { // going to show the table but not shown yet
                            tableJustShown = true;
                            makeTableVisibleAfterSearchStarted();
                        } else if (tableJustShown) {
                            tableJustShown = false;
                            tablePanel.getComponent(0).requestFocus();
                        }
                    }
                } else {
                    tableJustShown = false;
                }
                tableSummaryLabel.setVisible(!on);
                tableFieldsPanel.setVisible(!on);
                if (on && text != null) {
                    noContentLabel.setForeground(defaultTextColor);
                    noContentLabel.setText(text);
                }
            }
        };
        UIUtils.runInAWT(r);
    }

    private boolean tableJustShown;
    private void makeTableVisibleAfterSearchStarted() {
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (tablePanel.isShowing()) {
                    tablePanel.getComponent(0).requestFocus();
                }
            }
        });
    }

    void showRetrievingProgress(final boolean on, final String text, final boolean searchPanelVisible) {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                noContentPanel.setVisible(on);
                noContentLabel.setForeground(Color.red);
                if (searchPanelVisible) {
                    searchPanel.setVisible(!on);
                }
                if (on && text != null) {
                    noContentLabel.setForeground(defaultTextColor);
                    noContentLabel.setText(text);
                }
            }
        };
        UIUtils.runInAWT(r);
    }

    void showNoContentPanel(boolean on) {
        showSearchingProgress(on, null);
    }

    void setModifyVisible(final boolean b) {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                searchPanel.setVisible(b);
                cancelChangesButton.setVisible(b);
                saveChangesButton.setVisible(b);
                
                tableFieldsPanel.setVisible(!b);
                searchButton.setVisible(!b);
                
                modifyButton.setEnabled(!b);
            }
        };
        UIUtils.runInAWT(r);
    }

    void setSaved(final String name, final String lastRefresh) {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                searchPanel.setVisible(false);
                queryHeaderPanel.setVisible(true);
                tableHeaderPanel.setVisible(true);
                filterComboBox.setVisible(true); // XXX move to bugtracking IssueTable component
                filterLabel.setVisible(true);
                tablePanel.setVisible(true);
                nameLabel.setText(name);
                setLastRefresh(lastRefresh);
            }
        };
        UIUtils.runInAWT(r);
    }

    void setLastRefresh(final String lastRefresh) {
        Runnable r = new Runnable() {
            public void run() {
                lastRefreshDateLabel.setText(lastRefresh);
            }
        };
        UIUtils.runInAWT(r);
    }

    void hideModificationFields () {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                // can't change the controllers data
                // so alwasy keep those fields disabled
                modifyButton.setEnabled(false);
                removeButton.setEnabled(false);
                refreshConfigurationButton.setEnabled(false);
                cloneQueryButton.setEnabled(false);
            }
        };
        UIUtils.runInAWT(r);
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
