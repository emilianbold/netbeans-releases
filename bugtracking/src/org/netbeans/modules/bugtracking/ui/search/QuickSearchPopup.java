/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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


package org.netbeans.modules.bugtracking.ui.search;

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.JList;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import org.netbeans.modules.bugtracking.spi.Issue;
import org.netbeans.modules.bugtracking.spi.Query;
import org.netbeans.modules.bugtracking.spi.Repository;
import org.netbeans.modules.bugtracking.ui.search.PopupItem.IssueItem;
import org.netbeans.modules.bugtracking.util.BugtrackingUtil;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;
import org.openide.util.TaskListener;

/**
 * Component representing drop down for quick search
 * @author  Jan Becicka
 */
public class QuickSearchPopup extends javax.swing.JPanel 
        implements ListDataListener, ActionListener, TaskListener, Runnable {

    private QuickSearchComboBar comboBar;

    private ResultsModel rModel;

    /* Rect to store repetitive bounds computation */
    private Rectangle popupBounds = new Rectangle();

    /** coalesce times varying according to lenght of input text for searching */
    private static final int[] COALESCE_TIMES = new int[] {
        150, // time to wait before running search when input text has 0 characters
        400, // ...when input text has 1 character
        300, // ...2 characters
        200// ...3 and more characters
    };

    private Timer updateTimer;

    /** text to search for */
    private String searchedText;

    private RequestProcessor.Task evalTask;
    private Repository repository;
    private RequestProcessor rp;
    private List<PopupItem> currentHitlist;

    /** Creates new form SilverPopup */
    public QuickSearchPopup (QuickSearchComboBar comboBar) {
        this.comboBar = comboBar;
        initComponents();
        hintLabel.setVisible(false);
        hintSep.setVisible(false);
        rModel = ResultsModel.getInstance();
        jList1.setModel(rModel);
        jList1.setCellRenderer(new SearchResultRender(comboBar, this));
        rp = new RequestProcessor("Bugtracking quick issue search", 1, true); // NOI18N

        updateStatusPanel();
    }

    @Override
    public void addNotify() {
        rModel.addListDataListener(this);
        super.addNotify();
    }

    @Override
    public void removeNotify() {
        rModel.removeListDataListener(this);
        super.removeNotify();
    }

    void invoke() {
        int selection = jList1.getSelectedIndex();
        ListModel model = jList1.getModel();
        Object item = model.getElementAt(selection);
        if(item == null) return;
        if(item instanceof PopupItem.IssueItem) {
            Issue issue = ((PopupItem.IssueItem) item).getIssue();
            if (issue != null) {
                comboBar.setIssue(issue);
                clearModel();
            }
        } else {
            PopupItem pitem = (PopupItem) item;
            pitem.invoke();
        }
    }

    void selectNext() {
        int oldSel = jList1.getSelectedIndex();
        if (oldSel >= 0 && oldSel < jList1.getModel().getSize() - 1) {
            int idx = oldSel + 1;
            jList1.setSelectedIndex(idx);
            jList1.scrollRectToVisible(jList1.getCellBounds(idx, idx));
        } else if(oldSel < 0 && rModel.getSize() > 0) {
            jList1.setSelectedIndex(0);
        }
    }

    void selectPrev() {
        int oldSel = jList1.getSelectedIndex();
        if (oldSel > 0) {
            int idx = oldSel - 1;
            jList1.setSelectedIndex(idx);
            jList1.scrollRectToVisible(jList1.getCellBounds(idx, idx));
        } 
    }

    public JList getList() {
        return jList1;
    }

    public void clearModel () {
        rModel.setContent(null);
    }

    public void cancel () {
        cancelTask();
        rModel.setContent(null);
    }

    private void cancelTask() {
        if(evalTask != null) {
            evalTask.removeTaskListener(this);
            evalTask.cancel();
            updateTimer.stop();
        }
    }

    public void maybeEvaluate (String text) {
        this.searchedText = text;

        if (updateTimer == null) {
            updateTimer = new Timer(200, this);
        }

        if (!updateTimer.isRunning()) {
            // first change in possible flurry, start timer with proper delay
            updateTimer.setDelay(COALESCE_TIMES [ Math.min(text.length(), 3) ]);
            updateTimer.start();
        } else {
            // text change came too fast, let's wait until user calms down :)
            updateTimer.restart();
        }
    }

    /** implementation of ActionListener, called by timer,
     * actually runs search */
    public void actionPerformed(ActionEvent e) {
        updateTimer.stop();
        // search only if we are not cancelled already
        if (comboBar.getCommand().isFocusOwner()) {
            // start waiting on all providers execution
            runTask(new Runnable() {
                public void run() {
                    searchLocalIssues();
                }
            });
        }
    }

    void setRepository(Repository repo) {
        repository = repo;
    }

    private void runTask(Runnable r) {
        cancelTask();
        evalTask = rp.create(r);
        evalTask.addTaskListener(this);
        evalTask.schedule(0);
    }

    private void searchLocalIssues() {
        String criteria = comboBar.getCommand().getText().trim();
        if(criteria.equals("")) { // NOI18N
            rModel.setContent(null);
            return;
        }
        currentHitlist = new ArrayList<PopupItem>();

        // first add opened issues
        Set<String> ids = new HashSet<String>();
        addIssues(BugtrackingUtil.getByIdOrSummary(BugtrackingUtil.getOpenIssues(), criteria), ids);

        // all localy known issues
        Query[] queries = repository.getQueries();
        for (Query q : queries) {
            Issue[] issues = q.getIssues(criteria);
            addIssues(issues, ids);
        }

        // or at least what's already cached
        addIssues(BugtrackingUtil.getByIdOrSummary(ResultsModel.getInstance().getCachedIssues(repository), criteria), ids);
        populateModel(criteria, false);
    }

    private void addIssues(Issue[] issues, Set<String> ids) {
        if(issues == null) {
            return;
        }
        for (Issue issue : issues) {
            if (ids.contains(issue.getID())) {
                continue;
            }
            currentHitlist.add(new PopupItem.IssueItem(issue));
            ids.add(issue.getID());
        }
    }

    private void populateModel(final String criteria, boolean fullList) {
        ArrayList<PopupItem> modelList = new ArrayList<PopupItem>();
        for (PopupItem item : currentHitlist) {
            modelList.add(item);
            if(modelList.size() > 4 && !fullList) {
                modelList.add(new PopupItem() {
                    @Override
                    void invoke() {
                        populateModel(criteria, true);
                    }
                    @Override
                    String getDisplayText() {
                        return "...";  // NOI18N
                    }
                });
                break;
            }
        }
        modelList.add(new SearchItem(criteria));
        rModel.setContent(modelList);
    }


    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jScrollPane1 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList();
        statusPanel = new javax.swing.JPanel();
        searchingSep = new javax.swing.JSeparator();
        searchingLabel = new javax.swing.JLabel();
        noResultsLabel = new javax.swing.JLabel();
        hintSep = new javax.swing.JSeparator();
        hintLabel = new javax.swing.JLabel();

        setBorder(javax.swing.BorderFactory.createLineBorder(QuickSearchComboBar.getPopupBorderColor()));
        setMaximumSize(new java.awt.Dimension(2147483647, 150));
        setLayout(new java.awt.BorderLayout());

        jScrollPane1.setBorder(null);
        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        jList1.setFocusable(false);
        jList1.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                jList1MouseMoved(evt);
            }
        });
        jList1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jList1MouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(jList1);

        add(jScrollPane1, java.awt.BorderLayout.CENTER);

        statusPanel.setBackground(QuickSearchComboBar.getResultBackground());
        statusPanel.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        statusPanel.add(searchingSep, gridBagConstraints);

        searchingLabel.setText(org.openide.util.NbBundle.getMessage(QuickSearchPopup.class, "QuickSearchPopup.searchingLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        statusPanel.add(searchingLabel, gridBagConstraints);

        noResultsLabel.setForeground(java.awt.Color.red);
        noResultsLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        noResultsLabel.setText(org.openide.util.NbBundle.getMessage(QuickSearchPopup.class, "QuickSearchPopup.noResultsLabel.text")); // NOI18N
        noResultsLabel.setFocusable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        statusPanel.add(noResultsLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        statusPanel.add(hintSep, gridBagConstraints);

        hintLabel.setBackground(QuickSearchComboBar.getResultBackground());
        hintLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        statusPanel.add(hintLabel, gridBagConstraints);

        add(statusPanel, java.awt.BorderLayout.PAGE_END);
    }// </editor-fold>//GEN-END:initComponents

private void jList1MouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jList1MouseMoved
    // selection follows mouse move
    Point loc = evt.getPoint();
    int index = jList1.locationToIndex(loc);
    if (index == -1) {
        return;
    }
    Rectangle rect = jList1.getCellBounds(index, index);
    if (rect != null && rect.contains(loc)) {
        jList1.setSelectedIndex(index);
    }

}//GEN-LAST:event_jList1MouseMoved

private void jList1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jList1MouseClicked
    if (!SwingUtilities.isLeftMouseButton(evt)) {
        return;
    }   
    invoke();
}//GEN-LAST:event_jList1MouseClicked


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel hintLabel;
    private javax.swing.JSeparator hintSep;
    private javax.swing.JList jList1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel noResultsLabel;
    private javax.swing.JLabel searchingLabel;
    private javax.swing.JSeparator searchingSep;
    private javax.swing.JPanel statusPanel;
    // End of variables declaration//GEN-END:variables


    /*** impl of reactions to results data change */

    public void intervalAdded(ListDataEvent e) {
        updatePopup();
    }

    public void intervalRemoved(ListDataEvent e) {
        updatePopup();
    }

    public void contentsChanged(ListDataEvent e) {
        updatePopup();
    }

    /**
     * Updates size and visibility of this panel according to model content
     */
    public void updatePopup () {
        int modelSize = rModel.getSize();

        // plug this popup into layered pane if needed
        JLayeredPane lPane = JLayeredPane.getLayeredPaneAbove(comboBar);
        // lPane can be null when the corresponding dialog is closed already
        // for example, when the user didn't want to wait until the search finishes
        if (!isDisplayable() && (lPane != null)) {
            lPane.add(this, new Integer(JLayeredPane.POPUP_LAYER + 1) );
        }

        boolean statusVisible = updateStatusPanel();

        if(lPane != null) {
            computePopupBounds(popupBounds, lPane, modelSize);
            setBounds(popupBounds);
        }

        // popup visibility constraints
        if ((modelSize > 0 || statusVisible) && comboBar.getCommand().isFocusOwner()) {
            if (jList1.getSelectedIndex() >= modelSize) {
                jList1.setSelectedIndex(modelSize - 1);
            }
            setVisible(true);
        } else {
            setVisible(false);
        }

        // needed on JDK 1.5.x to repaint correctly
        revalidate();
    }

    public int getResultWidth () {
        return comboBar.getWidth();
    }

    /** Implementation of TaskListener, listen to when providers are finished
     * with their searching work
     */
    public void taskFinished(Task task) {
        evalTask = null;
        // update UI in ED thread
        if (SwingUtilities.isEventDispatchThread()) {
            run();
        } else {
            SwingUtilities.invokeLater(this);
        }
    }

    /** Runnable implementation, updates popup */
    public void run() {
        updatePopup();
    }

    private void computePopupBounds (Rectangle result, JLayeredPane lPane, int modelSize) {
        Point location =
                new Point(
                    comboBar.getCommand().getX(),
                    comboBar.getCommand().getY() + comboBar.getCommand().getHeight() - 1);
        location = SwingUtilities.convertPoint(comboBar, location, lPane); // XXX terrible hack! fix this
        result.setLocation(location);

        // hack to make jList.getpreferredSize work correctly
        // JList is listening on ResultsModel same as us and order of listeners
        // is undefined, so we have to force update of JList's layout data
        jList1.setFixedCellHeight(15);
        jList1.setFixedCellHeight(-1);
        // end of hack

        jList1.setVisibleRowCount(modelSize);
        Dimension preferredSize = jList1.getPreferredSize();

        preferredSize.width = comboBar.getCommand().getWidth();
        preferredSize.height += statusPanel.getPreferredSize().height + 3;
        if(preferredSize.height > 150) preferredSize.height = 150;

        result.setSize(preferredSize);
    }

    /** Computes width of string up to maxCharCount, with font of given JComponent
     * and with maximum percentage of owning Window that can be taken */
    private static int computeWidth (JComponent comp, int maxCharCount, int percent) {
        FontMetrics fm = comp.getFontMetrics(comp.getFont());
        int charW = fm.charWidth('X');
        int result = charW * maxCharCount;
        // limit width to 50% of containing window
        Window w = SwingUtilities.windowForComponent(comp);
        if (w != null) {
            result = Math.min(result, w.getWidth() * percent / 100);
        }
        return result;
    }

    /** Updates visibility and content of status labels.
     *
     * @return true when update panel should be visible (some its part is visible),
     * false otherwise
     */
    private boolean updateStatusPanel () {
        boolean shouldBeVisible = false;

        boolean isInProgress = evalTask != null;
        searchingSep.setVisible(isInProgress && (rModel.getSize() > 0));
        searchingLabel.setVisible(isInProgress);
        shouldBeVisible = shouldBeVisible || isInProgress;

        boolean searchedNotEmpty = searchedText != null && searchedText.trim().length() > 0;
        boolean areNoResults = rModel.getSize() <= 0 && searchedNotEmpty && !isInProgress;
        noResultsLabel.setVisible(areNoResults);
        comboBar.setNoResults(areNoResults);
        shouldBeVisible = shouldBeVisible || areNoResults;

        // XXX
        Issue issue = comboBar.getIssue();
        shouldBeVisible = shouldBeVisible && (issue == null || !IssueItem.getIssueDescription(issue).trim().equals(comboBar.getCommand().getText().trim()));

        return shouldBeVisible;
    }

    private  class SearchItem extends PopupItem {
        private String criteria;

        public SearchItem(String criteria) {
            this.criteria = criteria;
        }

        @Override
        void invoke() {
            runTask(new Runnable() {
                public void run() {
                    clearModel();

                    currentHitlist = new ArrayList<PopupItem>();
                    Set<String> ids = new HashSet<String>();
                    addIssues(BugtrackingUtil.getByIdOrSummary(BugtrackingUtil.getOpenIssues(), criteria), ids);

                    Issue[] issues = repository.simpleSearch(criteria);
                    addIssues(issues, ids);
                    populateModel(criteria, false);

                    ResultsModel.getInstance().cacheIssues(repository, issues); // XXX wasting response time?
                }
            });
        }

        @Override
        String getDisplayText() {
            return NbBundle.getMessage(PopupItem.class, "LBL_SearchCommand");   // NOI18N
        }
    }
}
