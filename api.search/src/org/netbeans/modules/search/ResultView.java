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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

package org.netbeans.modules.search;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import org.netbeans.spi.search.provider.SearchResultsDisplayer;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.MouseUtils;
import org.openide.awt.TabbedPaneFactory;
import org.openide.util.NbBundle;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;


/**
 * Panel which displays search results in explorer like manner.
 * This panel is a singleton.
 *
 * @see  <a href="doc-files/results-class-diagram.png">Class diagram</a>
 * @author Petr Kuzel, Jiri Mzourek, Peter Zavadsky
 * @author Marian Petras
 * @author kaktus
 */
@TopComponent.Description(preferredID=ResultView.ID, persistenceType=TopComponent.PERSISTENCE_ALWAYS, iconBase="org/netbeans/modules/search/res/find.gif")
@TopComponent.Registration(mode="output", position=1900, openAtStartup=false)
@ActionID(id = "org.netbeans.modules.search.ResultViewOpenAction", category = "Window")
@TopComponent.OpenActionRegistration(displayName="#TEXT_ACTION_SEARCH_RESULTS", preferredID=ResultView.ID)
@ActionReferences({
    @ActionReference(path = "Shortcuts", name = "DS-0"),
    @ActionReference(path = "Menu/Window/Output", name = "ResultViewOpenAction", position = 200)
})
public final class ResultView extends TopComponent {

    private static final boolean isMacLaf = "Aqua".equals(UIManager.getLookAndFeel().getID()); //NOI18N
    private static final Color macBackground = UIManager.getColor("NbExplorerView.background"); //NOI18N
    
    /** unique ID of <code>TopComponent</code> (singleton) */
    static final String ID = "search-results";                  //NOI18N
    
    private JPopupMenu pop;
    private PopupListener popL;
    private CloseListener closeL;

    private JPanel emptyPanel;

    /**
     * Returns a singleton of this class.
     *
     * @return  singleton of this <code>TopComponent</code>
     */
    static synchronized ResultView getInstance() {
        ResultView view;
        view = (ResultView) WindowManager.getDefault().findTopComponent(ID);
        if (view == null) {
            view = new ResultView(); // should not happen
        }
        return view;
    }

    private final CardLayout contentCards;
    
    public ResultView() {
        setLayout(contentCards = new CardLayout());

        setName("Search Results");                                      //NOI18N
        setDisplayName(NbBundle.getMessage(ResultView.class, "TITLE_SEARCH_RESULTS"));    //NOI18N
        setToolTipText(NbBundle.getMessage(ResultView.class, "TOOLTIP_SEARCH_RESULTS"));  //NOI18N
        
        initAccessibility();

        pop = new JPopupMenu();
        pop.add(new Close());
        pop.add(new CloseAll());
        pop.add(new CloseAllButCurrent());
        popL = new PopupListener();
        closeL = new CloseListener();

        initActions();
        
        emptyPanel = new JPanel();
        add(emptyPanel, BorderLayout.CENTER);
        if( isMacLaf ) {
            emptyPanel.setBackground(macBackground);
            setBackground(macBackground);
            setOpaque(true);
        }
    }
    
    private void initActions() {
        ActionMap map = getActionMap();

        map.put("jumpNext", new PrevNextAction (false)); // NOI18N
        map.put("jumpPrev", new PrevNextAction (true)); // NOI18N
    }
    

    @Deprecated
    final public static class ResolvableHelper implements java.io.Serializable {
        static final long serialVersionUID = 7398708142639457544L;
        public Object readResolve() {
            return null;
        }
    }

    private void initAccessibility() {
        ResourceBundle bundle = NbBundle.getBundle(ResultView.class);
        getAccessibleContext().setAccessibleName (bundle.getString ("ACSN_ResultViewTopComponent"));                   //NOI18N
        getAccessibleContext().setAccessibleDescription (bundle.getString ("ACSD_ResultViewTopComponent"));            //NOI18N
    }       

    public void fillOutput() {
        getCurrentResultViewPanel().fillOutput();
    }

    /**
     * This method exists just to make the <code>close()</code> method
     * accessible via <code>Class.getDeclaredMethod(String, Class[])</code>.
     * It is used in <code>Manager</code>.
     */
    void closeResults() {
        close();
    }

    /**
     */
    void displayIssuesToUser(ReplaceTask task, String title, String[] problems, boolean reqAtt) {
        assert EventQueue.isDispatchThread();

        IssuesPanel issuesPanel = new IssuesPanel(title, problems);
        if( isMacLaf ) {
            issuesPanel.setBackground(macBackground);
        }
        searchToViewMap.get(replaceToSearchMap.get(task)).displayIssues(issuesPanel);
        if (!isOpened()) {
            open();
        }
        if (reqAtt) {
            requestAttention(true);
        }
    }

    @Override
    protected void componentOpened() {
        assert EventQueue.isDispatchThread();
        Manager.getInstance().searchWindowOpened();

        ResultViewPanel panel = getCurrentResultViewPanel();
        if (panel != null)
            panel.componentOpened();
    }

    @Override
    @SuppressWarnings("deprecation")
    public void requestFocus() {
        ResultViewPanel panel = getCurrentResultViewPanel();
        if (panel != null)
            panel.tree.requestFocus();
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean requestFocusInWindow() {
        ResultViewPanel panel = getCurrentResultViewPanel();
        if (panel != null)
            return panel.tree.requestFocusInWindow();
        else
            return false;
    }

    private ResultViewPanel getCurrentResultViewPanel(){
        if (getComponentCount() > 0) {
            Component comp = getComponent(0);
            if (comp instanceof JTabbedPane) {
                comp = ((JTabbedPane)comp).getSelectedComponent();
                if (comp instanceof ResultViewPanel) {
                    return (ResultViewPanel) comp;
                }
            } else if (comp instanceof ResultViewPanel) {
                    return (ResultViewPanel) comp;
            }
        }
        return null;
    }

    private void addTabPanel(JPanel panel) {
        Component comp = getComponent(0);
        if (comp instanceof JTabbedPane) {
            ((JTabbedPane) comp).addTab(getTabTitle(panel), null, panel, panel.getToolTipText());
            ((JTabbedPane) comp).setSelectedComponent(panel);
            comp.validate();
        } else {
            remove(comp);
            JTabbedPane pane = TabbedPaneFactory.createCloseButtonTabbedPane();
            pane.setMinimumSize(new Dimension(0, 0));
            pane.addMouseListener(popL);
            pane.addPropertyChangeListener(closeL);
            if( isMacLaf ) {
                pane.setBackground(macBackground);
                pane.setOpaque(true);
            }
            add(pane, BorderLayout.CENTER);
            if (comp instanceof ResultViewPanel){
                pane.addTab(getTabTitle(comp), null, comp, ((JPanel) comp).getToolTipText());
            }
            pane.addTab(getTabTitle(panel), null, panel, panel.getToolTipText());
            pane.setSelectedComponent(panel);
            pane.validate();
        }
        validate();
        requestActive();
    }

    private String getTabTitle(Component panel){
        return NbBundle.getMessage(ResultView.class,
                                   "TEXT_MSG_RESULTS_FOR_X",   //NOI18N
                                   String.valueOf(panel.getName()));
    }

    private void updateTabTitle(JPanel panel) {
        if (getComponentCount() != 0) {
            Component comp = getComponent(0);
            if (comp instanceof JTabbedPane) {
                JTabbedPane pane = (JTabbedPane)comp;
                int index = pane.indexOfComponent(panel);
                pane.setTitleAt(index, getTabTitle(panel));
                pane.setToolTipTextAt(index, panel.getToolTipText());
            }
        }
    }
    private void removePanel(JPanel panel) {
        Component comp = getComponentCount() > 0 ? getComponent(0) : null;
        if (comp instanceof JTabbedPane) {
            JTabbedPane tabs = (JTabbedPane) comp;
            if (panel == null) {
                panel = (JPanel) tabs.getSelectedComponent();
            }
            ResultViewPanel rvp = (ResultViewPanel)panel;
            if (rvp.isSearchInProgress()){
                Manager.getInstance().stopSearching(viewToSearchMap.get(panel));
            }
            tabs.remove(panel);
            if (tabs.getComponentCount() == 1) {
                Component c = tabs.getComponent(0);
                tabs.removeMouseListener(popL);
                tabs.removePropertyChangeListener(closeL);
                remove(tabs);
                add(c, BorderLayout.CENTER);
                setName(((JPanel)c).getToolTipText());
            }
        } else if (comp instanceof ResultViewPanel)  {
            ResultViewPanel rvp = (ResultViewPanel)comp;
            if (rvp.isSearchInProgress()){
                Manager.getInstance().stopSearching(viewToSearchMap.get(comp));
            }
            remove(comp);
            add(emptyPanel, BorderLayout.CENTER);
            close();
        } else {
            close();
        }
        // Manager.getInstance().scheduleCleanTask(new CleanTask(viewToSearchMap.get(panel).getResultModel())); TODO
        
        SearchTask sTask = viewToSearchMap.remove(panel);
        searchToViewMap.remove(sTask);
        ReplaceTask rTask = searchToReplaceMap.remove(sTask);
        replaceToSearchMap.remove(rTask);

        validate();
    }

    @Override
    protected void componentClosed() {
        assert EventQueue.isDispatchThread();

        Manager.getInstance().searchWindowClosed();
        closeAll(false); // #170545
    }
    
    /**
     * Displays a message informing about the task which blocks the search
     * from being started. The search may also be blocked by a not yet finished
     * previous search task.
     *
     * @param  blockingTask  constant identifying the blocking task
     * @see  Manager#SEARCHING
     * @see  Manager#CLEANING_RESULT
     * @see  Manager#PRINTING_DETAILS
     */
    void notifySearchPending(final SearchTask task,final int blockingTask) {
        assert EventQueue.isDispatchThread();

        if (task.getDisplayer() instanceof ResultDisplayer) {

            ResultDisplayer rd = (ResultDisplayer) task.getDisplayer();

            ResultViewPanel panel = rd.getResultModel().getResultView();
            panel.removeIssuesPanel();
            String msgKey = null;
            switch (blockingTask) {
                case Manager.REPLACING:
                    msgKey = "TEXT_FINISHING_REPLACE";                  //NOI18N
                    break;
                case Manager.SEARCHING:
                    msgKey = "TEXT_FINISHING_PREV_SEARCH";                  //NOI18N
                    break;
                /*
                 * case Manager.CLEANING_RESULT: msgKey =
                 * "TEXT_CLEANING_RESULT"; //NOI18N break; case
                 * Manager.PRINTING_DETAILS: msgKey = "TEXT_PRINTING_DETAILS";
                 * //NOI18N break;
                 */
                default:
                    assert false;
            }
            panel.setRootDisplayName(NbBundle.getMessage(ResultView.class, msgKey));
            panel.setBtnStopEnabled(true);
            panel.setBtnReplaceEnabled(false);
        }
    }
    
    /**
     */
    void searchTaskStateChanged(final SearchTask task, final int changeType) {
        assert EventQueue.isDispatchThread();
        if (!(task.getDisplayer() instanceof ResultDisplayer)) {
            return;
        }
        ResultDisplayer rd = (ResultDisplayer) task.getDisplayer();
        ResultViewPanel panel = rd.getResultModel().getResultView();
        switch (changeType) {
            case Manager.EVENT_SEARCH_STARTED:
                panel.removeIssuesPanel();
                updateTabTitle(panel);
                panel.searchStarted();
                break;
            case Manager.EVENT_SEARCH_FINISHED:
                panel.searchFinished();
                break;
            case Manager.EVENT_SEARCH_INTERRUPTED:
                panel.searchInterrupted();
                break;
            case Manager.EVENT_SEARCH_CANCELLED:
                panel.searchCancelled();
                break;
            default:
                assert false;
        }
    }
    
    /**
     */
    void showAllDetailsFinished() {
        assert EventQueue.isDispatchThread();
        
//        mainPanel.updateShowAllDetailsBtn();
    }

    private Map<SearchTask, ResultViewPanel> searchToViewMap = new HashMap();
    private Map<ResultViewPanel, SearchTask> viewToSearchMap = new HashMap();

    void addSearchPair(ResultViewPanel panel, SearchTask task){
        if ((task != null) && (panel != null)){
            SearchTask oldTask = viewToSearchMap.get(panel);
            if (oldTask != null){
                searchToViewMap.remove(oldTask);
            }
            searchToViewMap.put(task, panel);
            viewToSearchMap.put(panel, task);
        }
    }

    private Map<ReplaceTask, SearchTask> replaceToSearchMap = new HashMap();
    private Map<SearchTask, ReplaceTask> searchToReplaceMap = new HashMap();

    void addReplacePair(ReplaceTask taskReplace, ResultViewPanel panel){
        if ((taskReplace != null) && (panel != null)){
            SearchTask taskSearch = viewToSearchMap.get(panel);
            replaceToSearchMap.put(taskReplace, taskSearch);
            searchToReplaceMap.put(taskSearch, taskReplace);
        }
    }

    synchronized ResultViewPanel initiateResultView(SearchTask task){
        assert EventQueue.isDispatchThread();

        ResultViewPanel panel = searchToViewMap.get(task);
        if (panel == null){
            panel = new ResultViewPanel(task.getComposition());
            if( isMacLaf ) {
                panel.setBackground(macBackground);
            }

            addSearchPair(panel, task);
            // #176312 tab name needs to be set so scrolling is performed correctly
            // after setSelectedComponent() in addTabPanel()
            panel.setName(getPanelName(task));
            addTabPanel(panel);
        } else {
            panel.setName(getPanelName(task));
        }
        return panel;
    }



    /** Get string that will be used as name of the panel.
     * 
     * @param task
     * @return 
     */
    private String getPanelName(SearchTask task) {

        return task.getDisplayer().getTitle();
    }
    
    /**
     */
    void closeAndSendFocusToEditor(ReplaceTask task) {
        assert EventQueue.isDispatchThread();

        removePanel(searchToViewMap.get(replaceToSearchMap.get(task)));
//        close();
        
        Mode m = WindowManager.getDefault().findMode("editor");         //NOI18N
        if (m != null) {
            TopComponent tc = m.getSelectedTopComponent();
            if (tc != null) {
                tc.requestActive();
            }
        }
    }
        
    /**
     */
    void rescan(ReplaceTask task) {
        assert EventQueue.isDispatchThread();

        SearchTask lastSearchTask = replaceToSearchMap.get(task);
        SearchTask newSearchTask = lastSearchTask.createNewGeneration();

        if (lastSearchTask.getDisplayer() instanceof ResultDisplayer) {

            ResultDisplayer sd = (ResultDisplayer) lastSearchTask.getDisplayer();

            if(sd.getResultModel() != null){
            ResultViewPanel panel = sd.getResultModel().getResultView();
            if (panel != null){
                ResultView.getInstance().addSearchPair(sd.getResultModel().getResultView(), newSearchTask);
                panel.removeIssuesPanel();
            }
        }
        Manager.getInstance().scheduleSearchTask(newSearchTask);
        }
    }

    private void closeAll(boolean butCurrent) {
        Component comp = getComponent(0);
        if (comp instanceof JTabbedPane) {
            JTabbedPane tabs = (JTabbedPane) comp;
            Component current = tabs.getSelectedComponent();
            Component[] c =  tabs.getComponents();
            for (int i = 0; i< c.length; i++) {
                if (butCurrent && c[i]==current) {
                    continue;
                }
                if(c[i] instanceof ResultViewPanel) { // #172546
                    removePanel((ResultViewPanel) c[i]);
                }
            }
        }
    }

    private class CloseListener implements PropertyChangeListener {
        public void propertyChange(java.beans.PropertyChangeEvent evt) {
            if (TabbedPaneFactory.PROP_CLOSE.equals(evt.getPropertyName())) {
                removePanel((JPanel) evt.getNewValue());
            }
        }
    }

    private class PopupListener extends MouseUtils.PopupMouseAdapter {
        protected void showPopup (MouseEvent e) {
            pop.show(ResultView.this, e.getX(), e.getY());
        }
    }

    private class Close extends AbstractAction {
        public Close() {
            super(NbBundle.getMessage(ResultView.class, "LBL_CloseWindow"));  //NOI18N
        }
        public void actionPerformed(ActionEvent e) {
            removePanel(null);
        }
    }

    private final class CloseAll extends AbstractAction {
        public CloseAll() {
            super(NbBundle.getMessage(ResultView.class, "LBL_CloseAll"));  //NOI18N
        }
        public void actionPerformed(ActionEvent e) {
            closeAll(false);
        }
    }

    private class CloseAllButCurrent extends AbstractAction {
        public CloseAllButCurrent() {
            super(NbBundle.getMessage(ResultView.class, "LBL_CloseAllButCurrent"));  //NOI18N
        }
        public void actionPerformed(ActionEvent e) {
            closeAll(true);
        }
    }

    private final class PrevNextAction extends javax.swing.AbstractAction {
        private boolean prev;

        public PrevNextAction (boolean prev) {
            this.prev = prev;
        }

        public void actionPerformed (java.awt.event.ActionEvent actionEvent) {
            ResultViewPanel panel = getCurrentResultViewPanel();
            if (panel != null) {
                panel.goToNext(!prev);
            }
        }
    }

    /**
     * Add a tab for a new displayer.
     */
    public void addTab(SearchResultsDisplayer<?> resultDisplayer) {

        JComponent panel = resultDisplayer.createVisualComponent();
        String title = resultDisplayer.getTitle();

        Component comp = getComponent(0);
        if (comp instanceof JTabbedPane) {
            ((JTabbedPane) comp).addTab(title, null, panel, panel.getToolTipText());
            ((JTabbedPane) comp).setSelectedComponent(panel);
            comp.validate();
        } else {
            remove(comp);
            JTabbedPane pane = TabbedPaneFactory.createCloseButtonTabbedPane();
            pane.setMinimumSize(new Dimension(0, 0));
            pane.addMouseListener(popL);
            pane.addPropertyChangeListener(closeL);
            if( isMacLaf ) {
                pane.setBackground(macBackground);
                pane.setOpaque(true);
            }
            add(pane, BorderLayout.CENTER);
            if (comp instanceof ResultViewPanel){
                pane.addTab(getTabTitle(comp), null, comp, ((JPanel) comp).getToolTipText());
            }
            pane.addTab(title, null, panel, panel.getToolTipText());
            pane.setSelectedComponent(panel);
            pane.validate();
        }
        validate();
        requestActive();
    }
}
