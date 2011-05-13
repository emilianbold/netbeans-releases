/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.git.ui.history;

import java.awt.Color;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.openide.util.RequestProcessor;
import org.openide.util.NbBundle;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Node;
import org.openide.windows.TopComponent;
import org.openide.awt.Mnemonics;
import org.netbeans.modules.versioning.util.NoContentPanel;
import javax.swing.*;
import java.io.File;
import java.util.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.awt.event.KeyEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Dimension;
import org.netbeans.modules.git.Git;
import org.netbeans.modules.git.GitModuleConfig;

/**
 * Contains all components of the Search History panel.
 *
 * @author Maros Sandor
 */
class SearchHistoryPanel extends javax.swing.JPanel implements ExplorerManager.Provider, PropertyChangeListener, ActionListener, DocumentListener {

    private final File[]                roots;
    private final SearchCriteriaPanel   criteria;
    
    private Divider                 divider;
    private Action                  searchAction;
    private SearchExecutor          currentSearch;
    private RequestProcessor.Task   currentSearchTask;

    private boolean                 criteriaVisible;
    private boolean                 searchInProgress;
    private List<RepositoryRevision> results;
    private SummaryView             summaryView;    
    private DiffResultsView         diffView;
    private AbstractAction nextAction;
    private AbstractAction prevAction;
    private final File repository;

    /** Creates new form SearchHistoryPanel */
    public SearchHistoryPanel(File repository, File [] roots, SearchCriteriaPanel criteria) {
        this.roots = roots;
        this.repository = repository;
        this.criteria = criteria;
        criteriaVisible = true;
        explorerManager = new ExplorerManager ();
        initComponents();
        setupComponents();
        aquaBackgroundWorkaround();
        refreshComponents(true);
    }
    
    private void aquaBackgroundWorkaround() {
        if( "Aqua".equals( UIManager.getLookAndFeel().getID() ) ) {             // NOI18N
            Color color = UIManager.getColor("NbExplorerView.background");      // NOI18N
            setBackground(color); 
            jToolBar1.setBackground(color); 
            resultsPanel.setBackground(color); 
            jPanel1.setBackground(color); 
            searchCriteriaPanel.setBackground(color); 
            criteria.setBackground(color); 
        }
    }

    public void disableFileChangesOption(boolean b) {
        fileInfoCheckBox.setEnabled(false);
        fileInfoCheckBox.setSelected(false);
    }

    boolean isShowInfo() {
        return fileInfoCheckBox.isSelected();
    }

    void setSearchCriteria(boolean b) {
        criteriaVisible = b;
        refreshComponents(false);
    }

    private void setupComponents() {
        remove(jPanel1);

        divider = new Divider(this);
        java.awt.GridBagConstraints gridBagConstraints;
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 2, 0);
        add(divider, gridBagConstraints);

        searchCriteriaPanel.add(criteria);
        searchAction = new AbstractAction(NbBundle.getMessage(SearchHistoryPanel.class,  "CTL_Search")) { // NOI18N
            {
                putValue(Action.SHORT_DESCRIPTION, NbBundle.getMessage(SearchHistoryPanel.class, "TT_Search")); // NOI18N
            }
            @Override
            public void actionPerformed(ActionEvent e) {
                executeSearch();
            }
        };
        getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "search"); // NOI18N
        getActionMap().put("search", searchAction); // NOI18N
        bSearch.setAction(searchAction);
        Mnemonics.setLocalizedText(bSearch, NbBundle.getMessage(SearchHistoryPanel.class,  "CTL_Search")); // NOI18N
        
        Dimension d1 = tbSummary.getPreferredSize();
        Dimension d2 = tbDiff.getPreferredSize();
        if (d1.width > d2.width) {
            tbDiff.setPreferredSize(d1);
        }
        
        nextAction = new AbstractAction(null, new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/git/resources/icons/diff-next.png"))) { // NOI18N
            {
                putValue(Action.SHORT_DESCRIPTION, NbBundle.getMessage(SearchHistoryPanel.class, "CTL_DiffPanel_Next_Tooltip")); // NOI18N
            }
            @Override
            public void actionPerformed(ActionEvent e) {
                diffView.onNextButton();
            }
        };
        prevAction = new AbstractAction(null, new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/git/resources/icons/diff-prev.png"))) { // NOI18N
            {
                putValue(Action.SHORT_DESCRIPTION, NbBundle.getMessage(SearchHistoryPanel.class, "CTL_DiffPanel_Prev_Tooltip")); // NOI18N
            }
            @Override
            public void actionPerformed(ActionEvent e) {
                diffView.onPrevButton();
            }
        };
        bNext.setAction(nextAction);
        bPrev.setAction(prevAction);

        criteria.tfFrom.getDocument().addDocumentListener(this);
        criteria.tfTo.getDocument().addDocumentListener(this);
        
        getActionMap().put("jumpNext", nextAction); // NOI18N
        getActionMap().put("jumpPrev", prevAction); // NOI18N
        
        criteria.showMergesChkBox.setSelected(GitModuleConfig.getDefault().getShowHistoryMerges());
        if(roots.length == 1) {
            File file = roots[0];
            if(!file.isFile()) fileInfoCheckBox.setEnabled(false);
        }
        if(fileInfoCheckBox.isEnabled()) {
            fileInfoCheckBox.setSelected(GitModuleConfig.getDefault().getShowFileInfo());
        } else {
            fileInfoCheckBox.setSelected(true);
        }
        criteria.showMergesChkBox.setOpaque(false);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getID() == Divider.DIVIDER_CLICKED) {
            criteriaVisible = !criteriaVisible;
            refreshComponents(false);
        }
    }

    private ExplorerManager             explorerManager;

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName())) {
            TopComponent tc = (TopComponent) SwingUtilities.getAncestorOfClass(TopComponent.class, this);
            if (tc == null) return;
            tc.setActivatedNodes((Node[]) evt.getNewValue());
        }
    }

    @Override
    public void addNotify() {
        super.addNotify();
        explorerManager.addPropertyChangeListener(this);
    }

    @Override
    public void removeNotify() {
        explorerManager.removePropertyChangeListener(this);
        super.removeNotify();
    }
    
    @Override
    public ExplorerManager getExplorerManager () {
        return explorerManager;
    }
    
    final void refreshComponents(boolean refreshResults) {
        if (refreshResults) {
            resultsPanel.removeAll();
            if (results == null) {
                if (searchInProgress) {
                    resultsPanel.add(new NoContentPanel(NbBundle.getMessage(SearchHistoryPanel.class, "LBL_SearchHistory_Searching"))); // NOI18N
                } else {
                    resultsPanel.add(new NoContentPanel(NbBundle.getMessage(SearchHistoryPanel.class, "LBL_SearchHistory_NoResults"))); // NOI18N
                }
            } else {
                if (tbSummary.isSelected()) {
                    if (summaryView == null) {
                        summaryView = new SummaryView(this, results);
                    }
                    resultsPanel.add(summaryView.getComponent());
                } else {
                    if (diffView == null) {
                        diffView = new DiffResultsView(this, results);
                    }
                    resultsPanel.add(diffView.getComponent());
                }
            }
            resultsPanel.revalidate();
            resultsPanel.repaint();
        }
        updateActions();

        divider.setArrowDirection(criteriaVisible ? Divider.UP : Divider.DOWN);
        searchCriteriaPanel.setVisible(criteriaVisible);
        bSearch.setVisible(criteriaVisible);
        revalidate();
        repaint();
    }
 
    final void updateActions () {
        nextAction.setEnabled(!tbSummary.isSelected() && diffView != null && diffView.isNextEnabled());
        prevAction.setEnabled(!tbSummary.isSelected() && diffView != null && diffView.isPrevEnabled());
    }

    void setResults (List<RepositoryRevision> newResults) {
        setResults(newResults, false);
    }

    private void setResults(List<RepositoryRevision> newResults, boolean searching) {
        this.results = newResults;
        this.searchInProgress = searching;
        summaryView = null;
        diffView = null;
        refreshComponents(true);
    }

    public File[] getRoots() {
        return roots;
    }

    public SearchCriteriaPanel getCriteria() {
        return criteria;
    }

    void executeSearch() {
        cancelBackgroundTasks();
        setResults(null, true);
        currentSearch = new SearchExecutor(this);
        currentSearch.start(Git.getInstance().getRequestProcessor(), repository, NbBundle.getMessage(SearchExecutor.class, "MSG_Search_Progress", repository)); //NOI18N
    }
    
    void cancelBackgroundTasks () {
        if (currentSearch != null) {
            currentSearch.cancel();
        }
    }    

    void showDiff(RepositoryRevision.Event revision) {
        tbDiff.setSelected(true);
        refreshComponents(true);
        diffView.select(revision);
    }

    public void showDiff(RepositoryRevision container) {
        tbDiff.setSelected(true);
        refreshComponents(true);
        diffView.select(container);
    }
    
//    Collection getSetups(RepositoryRevision [] revisions, RepositoryRevision.Event [] events) {
//        long fromRevision = Long.MAX_VALUE;
//        long toRevision = Long.MIN_VALUE;
//        HgLogMessage from = null;
//        HgLogMessage to = null;
//        Set<File> filesToDiff = new HashSet<File>();
//        
//        for (RepositoryRevision revision : revisions) {
//            long rev = Long.parseLong(revision.getLog().getRevisionNumber());
//            if (rev > toRevision) {
//                toRevision = rev;
//                to = revision.getLog();
//            }
//            if (rev < fromRevision) {
//                fromRevision = rev;
//                from = revision.getLog();
//            }
//            List<RepositoryRevision.Event> evs = revision.getEvents();
//            for (RepositoryRevision.Event event : evs) {
//                File file = event.getFile();
//                if (file != null) {
//                    filesToDiff.add(file);
//                }
//            }
//        }
//
//        for (RepositoryRevision.Event event : events) {
//            long rev = Long.parseLong(event.getLogInfoHeader().getLog().getRevisionNumber());
//            if (rev > toRevision) {
//                toRevision = rev;
//                to = event.getLogInfoHeader().getLog();
//            }
//            if (rev < fromRevision) {
//                fromRevision = rev;
//                from = event.getLogInfoHeader().getLog();
//            }
//            if (event.getFile() != null) {
//                filesToDiff.add(event.getFile());
//            }
//        }
//
//        List<Setup> setups = new ArrayList<Setup>();
//        for (File file : filesToDiff) {
//            HgRevision fromHgRevision = from.getHgRevision();
//            if (from.getRevisionNumber().equals(to.getRevisionNumber())) {
//                fromHgRevision = from.getAncestor(file);
//            }
//            Setup setup = new Setup(file, fromHgRevision, to.getHgRevision(), false);
//            setups.add(setup);
//        }
//        return setups;
//    }

    public static int compareRevisions(String r1, String r2) {
        StringTokenizer st1 = new StringTokenizer(r1, "."); // NOI18N
        StringTokenizer st2 = new StringTokenizer(r2, "."); // NOI18N
        for (;;) {
            if (!st1.hasMoreTokens()) {
                return st2.hasMoreTokens() ? -1 : 0;
            }
            if (!st2.hasMoreTokens()) {
                return st1.hasMoreTokens() ? 1 : 0;
            }
            int n1 = Integer.parseInt(st1.nextToken());
            int n2 = Integer.parseInt(st2.nextToken());
            if (n1 != n2) return n2 - n1;
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        buttonGroup1 = new javax.swing.ButtonGroup();
        searchCriteriaPanel = new javax.swing.JPanel();
        bSearch = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jToolBar1 = new javax.swing.JToolBar();
        tbSummary = new javax.swing.JToggleButton();
        tbDiff = new javax.swing.JToggleButton();
        jSeparator2 = new javax.swing.JSeparator();
        jSeparator3 = new javax.swing.JToolBar.Separator();
        resultsPanel = new javax.swing.JPanel();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 8, 0, 8));
        setLayout(new java.awt.GridBagLayout());

        searchCriteriaPanel.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 1.0;
        add(searchCriteriaPanel, gridBagConstraints);

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/git/ui/history/Bundle"); // NOI18N
        bSearch.setToolTipText(bundle.getString("TT_Search")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        add(bSearch, gridBagConstraints);

        jPanel1.setPreferredSize(new java.awt.Dimension(10, 6));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 0);
        add(jPanel1, gridBagConstraints);

        jToolBar1.setFloatable(false);
        jToolBar1.setRollover(true);

        buttonGroup1.add(tbSummary);
        tbSummary.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(tbSummary, bundle.getString("CTL_ShowSummary")); // NOI18N
        tbSummary.setToolTipText(bundle.getString("TT_Summary")); // NOI18N
        tbSummary.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                onViewToggle(evt);
            }
        });
        jToolBar1.add(tbSummary);

        buttonGroup1.add(tbDiff);
        org.openide.awt.Mnemonics.setLocalizedText(tbDiff, bundle.getString("CTL_ShowDiff")); // NOI18N
        tbDiff.setToolTipText(bundle.getString("TT_ShowDiff")); // NOI18N
        tbDiff.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                onViewToggle(evt);
            }
        });
        jToolBar1.add(tbDiff);

        jSeparator2.setOrientation(javax.swing.SwingConstants.VERTICAL);
        jSeparator2.setMaximumSize(new java.awt.Dimension(2, 32767));
        jToolBar1.add(jSeparator2);

        bNext.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/git/resources/icons/diff-next.png"))); // NOI18N
        jToolBar1.add(bNext);
        bNext.getAccessibleContext().setAccessibleName(bundle.getString("ACSN_NextDifference")); // NOI18N
        bNext.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SearchHistoryPanel.class, "ACSD_NextDifference")); // NOI18N

        bPrev.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/git/resources/icons/diff-prev.png"))); // NOI18N
        jToolBar1.add(bPrev);
        bPrev.getAccessibleContext().setAccessibleName(bundle.getString("ACSN_PrevDifference")); // NOI18N
        bPrev.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SearchHistoryPanel.class, "ACSD_PrevDifference")); // NOI18N

        jToolBar1.add(jSeparator3);

        org.openide.awt.Mnemonics.setLocalizedText(fileInfoCheckBox, org.openide.util.NbBundle.getMessage(SearchHistoryPanel.class, "LBL_SearchHistoryPanel_AllInfo")); // NOI18N
        fileInfoCheckBox.setToolTipText(org.openide.util.NbBundle.getMessage(SearchHistoryPanel.class, "LBL_TT_SearchHistoryPanel_AllInfo")); // NOI18N
        fileInfoCheckBox.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        fileInfoCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fileInfoCheckBoxActionPerformed(evt);
            }
        });
        jToolBar1.add(fileInfoCheckBox);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(jToolBar1, gridBagConstraints);

        resultsPanel.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(8, 0, 8, 0);
        add(resultsPanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void onViewToggle(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_onViewToggle
        refreshComponents(true);
    }//GEN-LAST:event_onViewToggle

private void fileInfoCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fileInfoCheckBoxActionPerformed
        GitModuleConfig.getDefault().setShowFileInfo( fileInfoCheckBox.isSelected() && fileInfoCheckBox.isEnabled());
}//GEN-LAST:event_fileInfoCheckBoxActionPerformed

    @Override
    public void insertUpdate(DocumentEvent e) {
        validateUserInput();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        validateUserInput();        
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        validateUserInput();        
    }
    
    private void validateUserInput() {
        String from = criteria.getFrom();
        if(from == null && criteria.tfFrom.getText().trim().length() > 0) {
            bSearch.setEnabled(false);
            return;
        }
        String to = criteria.getTo();
        if(to == null && criteria.tfTo.getText().trim().length() > 0) {
            bSearch.setEnabled(false);
            return;
        }        
        bSearch.setEnabled(true);
    }    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    final javax.swing.JButton bNext = new javax.swing.JButton();
    final javax.swing.JButton bPrev = new javax.swing.JButton();
    private javax.swing.JButton bSearch;
    private javax.swing.ButtonGroup buttonGroup1;
    final javax.swing.JCheckBox fileInfoCheckBox = new javax.swing.JCheckBox();
    private javax.swing.JPanel jPanel1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JToolBar.Separator jSeparator3;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JPanel resultsPanel;
    private javax.swing.JPanel searchCriteriaPanel;
    private javax.swing.JToggleButton tbDiff;
    private javax.swing.JToggleButton tbSummary;
    // End of variables declaration//GEN-END:variables

    File getRepository () {
        return repository;
    }
}
