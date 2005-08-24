/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.versioning.system.cvss.ui.history;

import org.openide.util.RequestProcessor;
import org.openide.util.NbBundle;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Node;
import org.openide.windows.TopComponent;
import org.netbeans.modules.versioning.system.cvss.util.NoContentPanel;
import org.netbeans.modules.versioning.system.cvss.util.Utils;
import org.netbeans.modules.versioning.system.cvss.CvsVersioningSystem;
import org.netbeans.lib.cvsclient.command.log.LogInformation;

import javax.swing.*;
import java.io.File;
import java.util.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.awt.event.KeyEvent;
import java.awt.event.ActionEvent;

/**
 * Contains all components of the Search History panel.
 *
 * @author Maros Sandor
 */
class SearchHistoryPanel extends javax.swing.JPanel implements ExplorerManager.Provider, PropertyChangeListener {

    private final File[]                roots;
    private final SearchCriteriaPanel   criteria;
    
    private Action                  searchAction;
    private SearchExecutor          currentSearch;
    private RequestProcessor.Task   currentSearchTask;
    
    private boolean                 searchInProgress;
    private List                    results;
    private List                    dispResults;
    private SummaryView             summaryView;    
    private DiffResultsView         diffView;

    /** Creates new form SearchHistoryPanel */
    public SearchHistoryPanel(File [] roots, SearchCriteriaPanel criteria) {
        this.roots = roots;
        this.criteria = criteria;
        explorerManager = new ExplorerManager ();
        initComponents();
        setupComponents();
        refreshComponents();
    }

    private void setupComponents() {
        searchCriteriaPanel.add(criteria);
        searchAction = new AbstractAction(NbBundle.getMessage(SearchHistoryPanel.class,  "CTL_Search")) {
            public void actionPerformed(ActionEvent e) {
                search();
            }
        };
        getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "search"); //NOI18N
        getActionMap().put("search", searchAction);//NOI18N
        bSearch.setAction(searchAction);
    }

    private ExplorerManager             explorerManager;

    public void propertyChange(PropertyChangeEvent evt) {
        if (ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName())) {
            TopComponent tc = (TopComponent) SwingUtilities.getAncestorOfClass(TopComponent.class, this);
            if (tc == null) return;
            tc.setActivatedNodes((Node[]) evt.getNewValue());
        }
    }

    public void addNotify() {
        super.addNotify();
        explorerManager.addPropertyChangeListener(this);
    }

    public void removeNotify() {
        explorerManager.removePropertyChangeListener(this);
        super.removeNotify();
    }
    
    public ExplorerManager getExplorerManager () {
        return explorerManager;
    }
    
    private void refreshComponents() {
        bNext.setEnabled(!tbSummary.isSelected());
        bPrev.setEnabled(!tbSummary.isSelected());
        resultsPanel.removeAll();
        if (results == null) {
            if (searchInProgress) {
                resultsPanel.add(new NoContentPanel(NbBundle.getMessage(SearchHistoryPanel.class, "LBL_SearchHistory_Searching")));
            } else {
                resultsPanel.add(new NoContentPanel(NbBundle.getMessage(SearchHistoryPanel.class, "LBL_SearchHistory_NoResults")));
            }
        } else {
            if (tbSummary.isSelected()) {
                if (summaryView == null) {
                    summaryView = new SummaryView(this, dispResults);
                }
                resultsPanel.add(summaryView.getComponent());
            } else {
                if (diffView == null) {
                    diffView = new DiffResultsView(dispResults);
                }
                resultsPanel.add(diffView.getComponent());
            }
        }
        resultsPanel.revalidate();        
        resultsPanel.repaint();
    }
    
    public void setResults(List newResults) {
        setResults(newResults, false);
    }

    private void setResults(List newResults, boolean searching) {
        this.results = newResults;
        if (results != null) this.dispResults = createDisplayList(results);
        this.searchInProgress = searching;
        summaryView = null;
        diffView = null;
        refreshComponents();
    }
    
    public File[] getRoots() {
        return roots;
    }

    public SearchCriteriaPanel getCriteria() {
        return criteria;
    }

    private synchronized void search() {
        if (currentSearchTask != null) {
            currentSearchTask.cancel();
        }
        setResults(null, true);
        currentSearch = new SearchExecutor(this);
        currentSearchTask = RequestProcessor.getDefault().create(currentSearch);
        currentSearchTask.schedule(0);
    }
    
    private static List createDisplayList(List list) {
        List dispResults = new ArrayList();
        List results = new ArrayList(list);
        Collections.sort(results, new ByRemotePathRevisionNumberComparator());
        ResultsContainer currentContainer = null;
        LogInformation.Revision lastRevision = null;
        int n = results.size();
        for (int i = 0; i < n; i++) {
            LogInformation.Revision revision = (LogInformation.Revision) results.get(i);
            if (!sameCategory(revision, lastRevision)) {
                if (i < n - 1) {
                    LogInformation.Revision nextRevision = (LogInformation.Revision) results.get(i + 1);
                    if (sameCategory(revision, nextRevision)) {
                        currentContainer = new ResultsContainer(revision);
                        dispResults.add(currentContainer);
                    } else {
                        currentContainer = null;
                    }
                }
            } else {
                if (currentContainer != null) {
                    currentContainer.add(revision);
                }
            }
            if (currentContainer == null) {
                dispResults.add(new DispRevision(revision, false));
            }
            lastRevision = revision;
        }
        return dispResults;
    }

    private static boolean sameCategory(LogInformation.Revision revision, LogInformation.Revision lastRevision) {
        if (lastRevision == null) return false;
        if (!revision.getLogInfoHeader().getRepositoryFilename().equals(lastRevision.getLogInfoHeader().getRepositoryFilename())) return false;
        String b1 = revision.getNumber().substring(0, revision.getNumber().lastIndexOf('.'));
        String b2 = lastRevision.getNumber().substring(0, lastRevision.getNumber().lastIndexOf('.'));
        return b1.equals(b2);
    }

    void executeSearch() {
        search();
    }

    void showDiff(LogInformation.Revision revision) {
        tbDiff.setSelected(true);
        refreshComponents();
        diffView.select(revision);
    }

    public void showDiff(ResultsContainer container) {
        tbDiff.setSelected(true);
        refreshComponents();
        diffView.select(container);
    }

    static class ResultsContainer {
        
        private List revisions = new ArrayList(2);
        private String name;
        private String path;

        public ResultsContainer(LogInformation.Revision newestRevision) {
            revisions.add(newestRevision);
            File file = newestRevision.getLogInfoHeader().getFile();
            try {
                name = CvsVersioningSystem.getInstance().getAdminHandler().getRepositoryForDirectory(file.getParentFile().getAbsolutePath(), "") + "/" + file.getName();
            } catch (Exception e) {
                name = newestRevision.getLogInfoHeader().getRepositoryFilename();
                if (name.endsWith(",v")) name = name.substring(0, name.lastIndexOf(",v"));
            }
            path = name.substring(0, name.lastIndexOf('/'));
            name = name.substring(path.length() + 1); 
        }

        public void add(LogInformation.Revision revision) {
            revisions.add(revisions.size(), revision);
        }
        
        public String getName() {
            return name;
        }

        public List getRevisions() {
            return revisions;
        }

        public String getEldestRevision() {
            LogInformation.Revision rev = (LogInformation.Revision) revisions.get(revisions.size() - 1);
            return Utils.previousRevision(rev.getNumber());
        }

        public String getNewestRevision() {
            return ((LogInformation.Revision) revisions.get(0)).getNumber();
        }
        
        public String getPath() {
            return path;
        }

        public File getFile() {
            return ((LogInformation.Revision) revisions.get(0)).getLogInfoHeader().getFile();
        }
    }

    static class DispRevision {
        
        private final LogInformation.Revision revision;
        private final boolean indented;
        private String name;

        public DispRevision(LogInformation.Revision revision, boolean indented) {
            this.revision = revision;
            File file = revision.getLogInfoHeader().getFile();
            try {
                name = CvsVersioningSystem.getInstance().getAdminHandler().getRepositoryForDirectory(file.getParentFile().getAbsolutePath(), "") + "/" + file.getName();
            } catch (Exception e) {
                name = revision.getLogInfoHeader().getRepositoryFilename();
                if (name.endsWith(",v")) name = name.substring(0, name.lastIndexOf(",v"));
            }
            this.indented = indented;
        }

        public String getName() {
            return name;
        }

        public LogInformation.Revision getRevision() {
            return revision;
        }

        public boolean isIndented() {
            return indented;
        }
    }

    private static class ByRemotePathRevisionNumberComparator implements Comparator {

        public int compare(Object o1, Object o2) {
            LogInformation.Revision r1 = (LogInformation.Revision) o1;
            LogInformation.Revision r2 = (LogInformation.Revision) o2;
            int namec = r1.getLogInfoHeader().getRepositoryFilename().compareTo(r2.getLogInfoHeader().getRepositoryFilename());
            if (namec != 0) return namec;
            // 1.2  ?  1.4.4.2
            int revc = r2.getNumber().length() - r1.getNumber().length();
            if (revc != 0) return revc;
            // 1.4.4.3  ?  1.4.4.2
            long r1l = Long.parseLong(r1.getNumber().replaceAll("\\.", ""));
            long r2l = Long.parseLong(r2.getNumber().replaceAll("\\.", ""));
            return r1l < r2l ? 1 : r1l > r2l ? -1 : 0;
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        buttonGroup1 = new javax.swing.ButtonGroup();
        searchCriteriaPanel = new javax.swing.JPanel();
        bSearch = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        tbSummary = new javax.swing.JToggleButton();
        tbDiff = new javax.swing.JToggleButton();
        bNext = new javax.swing.JButton();
        bPrev = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JSeparator();
        resultsPanel = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(8, 8, 0, 8)));
        searchCriteriaPanel.setLayout(new java.awt.BorderLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 1.0;
        add(searchCriteriaPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        add(bSearch, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 4, 0);
        add(jSeparator1, gridBagConstraints);

        buttonGroup1.add(tbSummary);
        tbSummary.setSelected(true);
        tbSummary.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/versioning/system/cvss/ui/history/Bundle").getString("CTL_ShowSummary"));
        tbSummary.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                onViewToggle(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        add(tbSummary, gridBagConstraints);

        buttonGroup1.add(tbDiff);
        tbDiff.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/versioning/system/cvss/ui/history/Bundle").getString("CTL_ShowDiff"));
        tbDiff.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                onViewToggle(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        add(tbDiff, gridBagConstraints);

        bNext.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/versioning/system/cvss/resources/icons/diff-next.png")));
        bNext.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                onNext(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        add(bNext, gridBagConstraints);

        bPrev.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/versioning/system/cvss/resources/icons/diff-prev.png")));
        bPrev.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                onPrev(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 4);
        add(bPrev, gridBagConstraints);

        jSeparator2.setOrientation(javax.swing.SwingConstants.VERTICAL);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 8, 0, 8);
        add(jSeparator2, gridBagConstraints);

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

    }
    // </editor-fold>//GEN-END:initComponents

    private void onPrev(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_onPrev
        diffView.onPrevButton();
    }//GEN-LAST:event_onPrev

    private void onNext(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_onNext
        diffView.onNextButton();
    }//GEN-LAST:event_onNext

    private void onViewToggle(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_onViewToggle
        refreshComponents();
    }//GEN-LAST:event_onViewToggle
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton bNext;
    private javax.swing.JButton bPrev;
    private javax.swing.JButton bSearch;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JPanel resultsPanel;
    private javax.swing.JPanel searchCriteriaPanel;
    private javax.swing.JToggleButton tbDiff;
    private javax.swing.JToggleButton tbSummary;
    // End of variables declaration//GEN-END:variables
    
}
