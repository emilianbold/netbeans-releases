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

package org.netbeans.modules.search;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import javax.accessibility.AccessibleContext;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.JToolBar.Separator;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;
import org.netbeans.modules.search.TextDetail.DetailNode;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openidex.search.SearchType;

/**
 *
 * @author kaktus
 */
class ResultViewPanel extends JPanel{

    private static final String CONTEXT_ICON =
            "org/netbeans/modules/search/res/context.gif"; //NOI18N
    private static final String REPLACE_ICON =
            "org/netbeans/modules/search/res/replaceChecked.gif"; //NOI18N
    private static final String CUSTOMIZER_ICON =
            "org/netbeans/modules/search/res/customizeReport.png"; //NOI18N
    private static final String STOP_ICON =
            "org/netbeans/modules/search/res/stop.png"; //NOI18N
    private static final String SEARCH_ICON =
            "org/netbeans/modules/search/res/search.gif"; //NOI18N
    private static final String PREV_ICON =
            "org/netbeans/modules/search/res/prev.png"; //NOI18N
    private static final String NEXT_ICON =
            "org/netbeans/modules/search/res/next.png"; //NOI18N

    private static final boolean isMacLaf = "Aqua".equals(UIManager.getLookAndFeel().getID()); //NOI18N
    private static final Color macBackground = UIManager.getColor("NbExplorerView.background"); //NOI18N
    
    /** display the matching string location in context by default? */
    private static final boolean SHOW_CONTEXT_BY_DEFAULT = true;

    /** should the context view be visible when doing search &amp; replace? */
    private boolean contextViewEnabled = SHOW_CONTEXT_BY_DEFAULT;

    /** */
    private boolean ignoreContextButtonToggle = false;
    /** */
    private boolean hasResults = false;     //accessed only from the EventQueue

    /** */
    private ResultTreeModel treeModel = null;

    /** */
    final JTree tree;

    /** listens on various actions performed on nodes in the tree */
    private final NodeListener nodeListener;

    /** Result data model. */
    private ResultModel resultModel = null;

    /** */
    private BasicSearchCriteria basicSearchCriteria;
    /** */
    private List<SearchType> searchTypes;

    /** */
    private double dividerLocation = -1.0d;

    /** */
    private String searchScopeType;

    /** template for displaying number of matching files found so far */
    private MessageFormat nodeCountFormat;
    /**
     * template for displaying of number of matching files and total number
     * of matches found so far
     */
    private MessageFormat nodeCountFormatFullText;

    private IssuesPanel issuesPanel;

    /**
     * tree view for displaying found objects
     */
    private final JScrollPane treeView;
    private final CardLayout resultViewCards;
    private JSplitPane splitPane;

    private final JPanel resultsPanel;
    private final JToolBar toolBar;
    private JSeparator toolbarSeparator;
    private JButton btnShowDetails = new JButton();
    private JButton btnModifySearch = new JButton();
    private JButton btnStop = new JButton();
    private JButton btnReplace = new JButton();
    private JButton btnPrev;
    private JButton btnNext;
    private JToggleButton btnDisplayContext = new JToggleButton();
    private Separator sepDisplayContext;

    /** is the context view visible? */
    private boolean contextViewVisible = false;

    /** */
    private volatile boolean hasDetails = false;
    /** */
    private volatile boolean searchInProgress = false;

    private final ArrowStatusUpdater arrowUpdater;

    private ContextView contextView;
    /** */
    private int objectsCount = 0;           //accessed only from the EventQueue

    private SearchTask task;

    public ResultViewPanel(SearchTask task) {
        setLayout(new GridBagLayout());
        arrowUpdater = new ArrowStatusUpdater(this);

        this.task = task;
        treeModel = createTreeModel();
        tree = createTree(treeModel, nodeListener = new NodeListener(), arrowUpdater);
        treeView = new JScrollPane(tree);
        treeView.getAccessibleContext().setAccessibleDescription(
                NbBundle.getMessage(ResultView.class, "ACS_TREEVIEW")); //NOI18N
        treeView.setBorder(BorderFactory.createEmptyBorder());

        resultsPanel = new JPanel(resultViewCards = new CardLayout());


        //Toolbar
        toolBar = new JToolBar(SwingConstants.VERTICAL);
        btnDisplayContext.setIcon(
                ImageUtilities.loadImageIcon(CONTEXT_ICON, true));
        btnDisplayContext.setToolTipText(
                NbBundle.getMessage(getClass(), "TOOLTIP_ShowContext"));//NOI18N
        btnDisplayContext.getAccessibleContext().setAccessibleDescription(
                NbBundle.getMessage(getClass(), "ACSD_ShowContext"));   //NOI18N
        btnDisplayContext.setSelected(SHOW_CONTEXT_BY_DEFAULT);
        btnPrev = new JButton();
        btnPrev.setIcon(ImageUtilities.loadImageIcon(PREV_ICON, true));
        btnPrev.setToolTipText(
                NbBundle.getMessage(getClass(), "TEXT_BUTTON_PREV_MATCH"));//NOI18N);
        btnNext = new JButton();
        btnNext.setIcon(ImageUtilities.loadImageIcon(NEXT_ICON, true));
        btnNext.setToolTipText(
                NbBundle.getMessage(getClass(), "TEXT_BUTTON_NEXT_MATCH"));//NOI18N);

        toolBar.add(btnDisplayContext);
        toolBar.add(sepDisplayContext = new JToolBar.Separator());
        toolBar.add(btnPrev);
        toolBar.add(btnNext);
        toolBar.setRollover(true);
        toolBar.setFloatable(false);

        btnPrev.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                goToNext(false);
            }
        });

        btnNext.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                goToNext(true);
            }
        });

        btnDisplayContext.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (!ignoreContextButtonToggle) {
                    contextViewEnabled = (e.getStateChange() == ItemEvent.SELECTED);
                    updateContextViewVisibility();
                }
            }
        });

        // Toolbar separator
        toolbarSeparator = new JSeparator();
        toolbarSeparator.setOrientation(SwingConstants.VERTICAL);

        //Buttons panel
        btnShowDetails.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                fillOutput();
            }
        });
        btnModifySearch.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                customizeCriteria();
            }
        });
        btnStop.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                Manager.getInstance().stopSearching(getTask());
            }
        });
        btnReplace.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                replaceMatches();
            }
        });

        btnStop.setToolTipText(NbBundle.getMessage(ResultView.class,
                                                   "TEXT_BUTTON_STOP"));//NOI18N
        btnShowDetails.setToolTipText(NbBundle.getMessage(ResultView.class,
                                                   "TEXT_BUTTON_FILL"));//NOI18N
        btnReplace.setToolTipText(NbBundle.getMessage(ResultView.class,
                                                "TEXT_BUTTON_REPLACE"));//NOI18N
        btnModifySearch.setToolTipText(NbBundle.getMessage(ResultView.class,
                                              "TEXT_BUTTON_CUSTOMIZE"));//NOI18N

        btnModifySearch.setIcon(ImageUtilities.loadImageIcon(CUSTOMIZER_ICON, true));
        btnStop.setIcon(ImageUtilities.loadImageIcon(STOP_ICON, true));
        btnShowDetails.setIcon(ImageUtilities.loadImageIcon(SEARCH_ICON, true));
        btnReplace.setIcon(ImageUtilities.loadImageIcon(REPLACE_ICON, true));

        btnStop.setEnabled(false);
        btnShowDetails.setEnabled(false);

        btnReplace.setVisible(false);

        toolBar.add(new JToolBar.Separator());
        toolBar.add(btnReplace);
        toolBar.add(Box.createHorizontalGlue());
        toolBar.add(btnShowDetails);
        toolBar.add(btnModifySearch);
        toolBar.add(btnStop);

        add(toolBar, getToolbarConstraints());
        add(toolbarSeparator, getToolbarSeparatorConstraints());
        add(resultsPanel, getMainPanelConstraints());

        GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.weightx = 1.0;


        //issue #46261 - "Search Results window must be opaque under GTK"
        setOpaque(true);
        setBorder(BorderFactory.createEmptyBorder(0, 0, 2, 0));

        initAccessibility();
        resultModelChanged();

        if( isMacLaf ) {
            tree.setBackground(macBackground);
            treeView.setBackground(macBackground);
            toolBar.setBackground(macBackground);
            resultsPanel.setBackground(macBackground);
        }
    }

    ResultModel getResultModel(){
        return resultModel;
    }

    void setResultModel(ResultModel resultModel){

        boolean hadCheckBoxes = (this.resultModel != null)
                                && this.resultModel.searchAndReplace;
        boolean hasCheckBoxes = (resultModel != null)
                                && resultModel.searchAndReplace;

        this.resultModel = resultModel;
        this.basicSearchCriteria = resultModel.basicCriteria;
        this.searchTypes = Arrays.asList(resultModel.getSearchGroup().getSearchTypes());
        this.searchScopeType = resultModel.getSearchGroup().getSearchScope().getTypeId();

        tree.setModel(treeModel = new ResultTreeModel(resultModel));
        if (hasCheckBoxes != hadCheckBoxes) {
            tree.setCellRenderer(new NodeRenderer(hasCheckBoxes));
            setBtnReplaceVisible(hasCheckBoxes);
        }
        if (resultModel != null) {
            hasResults = resultModel.size() != 0;
            hasDetails = hasResults && resultModel.hasDetails();
            resultModel.setObserver(this);
        } else {
            hasResults = false;
            hasDetails = false;
        }
        resultModelChanged();
    }

    private void initAccessibility() {
        ResourceBundle bundle = NbBundle.getBundle(ResultView.class);

        AccessibleContext accessCtx;
        
        accessCtx = treeView.getHorizontalScrollBar().getAccessibleContext();
        accessCtx.setAccessibleName(bundle.getString("ACSN_HorizontalScrollbar"));          //NOI18N

        accessCtx = treeView.getVerticalScrollBar().getAccessibleContext();
        accessCtx.setAccessibleName(bundle.getString("ACSN_VerticalScrollbar"));            //NOI18N

        accessCtx = treeView.getAccessibleContext();
        accessCtx.setAccessibleName(bundle.getString("ACSN_ResultTree"));                   //NOI18N
        accessCtx.setAccessibleDescription(bundle.getString("ACSD_ResultTree"));                   //NOI18N

        btnReplace.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_TEXT_BUTTON_REPLACE"));    //NOI18N
        btnModifySearch.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_TEXT_BUTTON_CUSTOMIZE")); //NOI18N
        btnShowDetails.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_TEXT_BUTTON_FILL"));         //NOI18N
        btnStop.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_TEXT_BUTTON_STOP"));           //NOI18N
    }

    /** Send search details to output window. */
    public void fillOutput() {
        btnShowDetails.setEnabled(false);
        Manager.getInstance().schedulePrintTask(
                new PrintDetailsTask(resultModel.getMatchingObjects(),
                                     basicSearchCriteria,
                                     searchTypes));
    }

    /**
     * Updates the number of found nodes in the name of the root node.
     */
    private synchronized void updateObjectsCount(final int totalDetailsCount) {
        assert EventQueue.isDispatchThread();

        if(resultModel == null) {
            // a new search was scheduled, so don't do anything
            return;
        }

        objectsCount++;
        hasResults = true;

        setRootDisplayName(
                resultModel.isBasicCriteriaOnly && resultModel.basicCriteria.isFullText()
                ? nodeCountFormatFullText.format(
                            new Object[] {new Integer(objectsCount),
                                          new Integer(totalDetailsCount)})
                : nodeCountFormat.format(
                            new Object[] {new Integer(objectsCount)}));
    }

    /**
     */
    void objectFound(Object foundObject, final int totalDetailsCount) {
        assert !EventQueue.isDispatchThread();

        EventQueue.invokeLater(new Runnable() {
            public void run() {
                updateObjectsCount(totalDetailsCount);
            }
        });
    }

    /**
     */
    void rememberInput(String searchScopeType,
                       BasicSearchCriteria basicSearchCriteria,
                       List<SearchType> searchTypes) {
        this.searchScopeType = searchScopeType;
        this.basicSearchCriteria = basicSearchCriteria;
        this.searchTypes = searchTypes;
    }

    void componentOpened() {
        if (searchScopeType == null) {
            setBtnModifyEnabled(false);
        }
    }

    void componentClosed() {
        rememberInput(null, null, null);

        if (contextView != null) {
            contextView.unbindFromTreeSelection(tree);
            contextView = null;
        }
        if (splitPane != null) {
            rememberDividerLocation();
            resultsPanel.remove(splitPane);
            splitPane = null;
        }
        contextViewVisible = false;
    }

    void resultModelChanged() {
        updateDisplayContextButton();
        updateContextViewVisibility();
        if (contextView != null) {
            contextView.setResultModel(resultModel);
        }
        nodeListener.setSelectionChangeEnabled(true);

        btnPrev.setEnabled(resultModel != null);
        btnNext.setEnabled(resultModel != null);
        resetMatchingObjIndexCache();

        objectsCount = 0;
    }

    private SearchTask getTask(){
        return task;
    }

    private MatchingObject matchingObjIndexCacheObj = null;
    private int            matchingObjIndexCacheIndex = -1;

    /**
     */
    private void resetMatchingObjIndexCache() {
        matchingObjIndexCacheObj = null;
        matchingObjIndexCacheIndex = -1;
    }

    /**
     */
    private ResultTreeModel createTreeModel() {
        ResultTreeModel treeModel = new ResultTreeModel(null);
        treeModel.setRootDisplayName(getInitialRootNodeText());
        return treeModel;
    }

    /**
     */
    private JTree createTree(ResultTreeModel treeModel,
                                    NodeListener nodeListener,
                                    ArrowStatusUpdater arrowUpdater) {
        JTree tree = new JTree(treeModel);

        TreeCellRenderer cellRenderer = new NodeRenderer(false);
        tree.setCellRenderer(cellRenderer);
        tree.setRowHeight(cellRenderer.getTreeCellRendererComponent(
                                                tree,       //tree
                                                treeModel,  //value
                                                true,       //selected
                                                true,       //expanded
                                                false,      //leaf
                                                0,          //row
                                                true)       //hasFocus
                          .getPreferredSize()
                          .height + 2);

        tree.putClientProperty("JTree.lineStyle", "Angled");            //NOI18N

        tree.addMouseListener(nodeListener);
        tree.addKeyListener(nodeListener);
        tree.addTreeWillExpandListener(nodeListener);
        tree.addTreeExpansionListener(nodeListener);

        tree.setToggleClickCount(0);

        tree.addMouseListener(arrowUpdater);
        tree.addKeyListener(arrowUpdater);

        return tree;
    }

    /**
     */
    private String getInitialRootNodeText() {
        return NbBundle.getMessage(ResultView.class, "TEXT_Search_in_filesystems");       //NOI18N
    }

    /**
     */
    void setRootDisplayName(String displayName) {
        treeModel.setRootDisplayName(displayName);
    }

    /**
     * Checks whether this result view displays search results for operation
     * <em>search &amp; replace</em> or for plain search.
     *
     * @return  {@code true} if results for <em>search &amp; replace</em>
     *          are displayed, {@code false} otherwise
     */
    private boolean isSearchAndReplace() {
        return (resultModel != null) && resultModel.searchAndReplace;
    }

    /**
     */
    private void setFinalRootNodeText() {
        assert EventQueue.isDispatchThread();

        int resultSize = resultModel.size();

        if (resultModel.wasLimitReached()) {
            setRootDisplayName(
                    NbBundle.getMessage(
                            ResultView.class,
                            "TEXT_MSG_FOUND_X_NODES_LIMIT",             //NOI18N
                            Integer.valueOf(resultSize),
                            Integer.valueOf(resultModel.getTotalDetailsCount()))
                            + ' ' + resultModel.getLimitDisplayName()); //NOI18N
            return;
        }

        String baseMsg;
        if (resultSize == 0) {
            baseMsg = NbBundle.getMessage(ResultView.class,
                                          "TEXT_MSG_NO_NODE_FOUND");    //NOI18N
        } else {
            String bundleKey;
            Object[] args;
            if (resultModel.searchAndReplace) {
                bundleKey = "TEXT_MSG_FOUND_X_NODES_REPLACE";           //NOI18N
                args = new Object[4];
            } else if (resultModel.isBasicCriteriaOnly && resultModel.basicCriteria.isFullText()) {
                bundleKey = "TEXT_MSG_FOUND_X_NODES_FULLTEXT";          //NOI18N
                args = new Object[2];
            } else {
                bundleKey = "TEXT_MSG_FOUND_X_NODES";                   //NOI18N
                args = new Object[1];
            }
            args[0] = new Integer(objectsCount);
            if (args.length > 1) {
                args[1] = new Integer(resultModel.getTotalDetailsCount());
            }
            if (args.length > 2) {
               args[2] = resultModel.basicCriteria.getTextPatternExpr();
               args[3] = resultModel.basicCriteria.getReplaceExpr();
            }
            baseMsg = NbBundle.getMessage(getClass(), bundleKey, args);
        }
        String exMsg = resultModel.getExceptionMsg();
        String msg = exMsg == null ? baseMsg
                                   : baseMsg + " (" + exMsg + ")";      //NOI18N
        setRootDisplayName(msg);
    }

    boolean isSearchInProgress(){
        return searchInProgress;
    }

    /**
     */
    void updateShowAllDetailsBtn() {
        assert EventQueue.isDispatchThread();

        if (hasResults && !searchInProgress)
            tree.setSelectionPath(new TreePath(tree.getModel().getRoot()));
        setBtnShowDetailsEnabled(hasResults && !searchInProgress && hasDetails);
    }

    /**
     */
    void searchStarted() {
        setRootDisplayName(NbBundle.getMessage(ResultView.class,
                                                "TEXT_SEARCHING___"));  //NOI18N
        nodeCountFormat = new MessageFormat(
                NbBundle.getMessage(getClass(),
                                    "TXT_RootSearchedNodes"));          //NOI18N
        nodeCountFormatFullText = new MessageFormat(
                NbBundle.getMessage(getClass(),
                                    "TXT_RootSearchedNodesFulltext"));  //NOI18N

        searchInProgress = true;
        updateShowAllDetailsBtn();
        setBtnModifyEnabled(true);
        setBtnStopEnabled(true);
        setBtnReplaceEnabled(false);
        arrowUpdater.update();
    }

    /**
     */
    void searchFinished() {
        setFinalRootNodeText();

        searchInProgress = false;
        hasDetails = (resultModel != null) ? resultModel.hasDetails() : false;
        updateShowAllDetailsBtn();
        setBtnStopEnabled(false);
        setBtnReplaceEnabled(true);
        arrowUpdater.update();
    }

    /**
     */
    void searchInterrupted() {
        searchFinished();
    }

    /**
     */
    void searchCancelled() {
        setRootDisplayName(NbBundle.getMessage(ResultView.class, "TEXT_TASK_CANCELLED"));//NOI18N
        searchInProgress = true;
        updateShowAllDetailsBtn();
        setBtnStopEnabled(false);
        setBtnReplaceEnabled(true);
        arrowUpdater.update();
    }

    void displayIssues(IssuesPanel issuesPanel) {
        if (issuesPanel != null){
            this.issuesPanel = issuesPanel;
            remove(toolBar);
            remove(toolbarSeparator);
            remove(resultsPanel);
            add(issuesPanel, getMainPanelConstraints());
            validate();
            repaint();
        }
    }

    /**
     */
    void removeIssuesPanel() {
        if (issuesPanel != null) {
            remove(issuesPanel);
            add(toolBar, getToolbarConstraints());
            add(toolbarSeparator, getToolbarSeparatorConstraints());
            add(resultsPanel, getMainPanelConstraints());
            issuesPanel = null;
            validate();
            repaint();
        }
    }

    private GridBagConstraints getMainPanelConstraints(){
        GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;

        return gridBagConstraints;
    }

    private GridBagConstraints getToolbarConstraints(){
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weighty = 1.0;
        
        return gridBagConstraints;
    }

    private GridBagConstraints getToolbarSeparatorConstraints(){
        GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weighty = 1.0;

        return gridBagConstraints;
    }

    /**
     * Enables or disables the <em>Display Context</em> button,
     * according to the result model currently displayed.
     *
     * @see  #updateContextViewVisibility
     */
    private void updateDisplayContextButton() {
        boolean searchAndReplace = isSearchAndReplace();
        btnDisplayContext.setVisible(searchAndReplace);
        sepDisplayContext.setVisible(searchAndReplace);

        ignoreContextButtonToggle = true;
        try {
            btnDisplayContext.setSelected(searchAndReplace
                                          && contextViewEnabled);
        } finally {
            ignoreContextButtonToggle = false;
        }
    }

    /**
     * Shows or hides the context view, according to the state of the
     * <em>Display Context</em> button.
     *
     * @see  #updateDisplayContextButton
     */
    private void updateContextViewVisibility() {
        setContextViewVisible(isSearchAndReplace() && contextViewEnabled);
    }

    /**
     * Shows or hides the context view.
     */
    private void setContextViewVisible(boolean visible) {
        assert EventQueue.isDispatchThread();
        assert (splitPane == null) == (contextView == null);

        final int componentCount = resultsPanel.getComponentCount();
        if ((visible == contextViewVisible) && (componentCount != 0)) {
            return;
        }

        this.contextViewVisible = visible;

        final String cardName;
        if (visible == false) {
            cardName = "tree only";                                     //NOI18N
            /*
             * This code is executed either the first time the result view
             * is displayed or when the context view is closed.
             * In the former case, we must add the tree view simply because
             * the result view does not contain it yet.
             * In the latter case, we must add it, too, because it was
             * removed from the resultsPanel the last time the context view
             * was displayed.
             */
            assert componentCount < 2;
            /*
             * The following line removes the treeView from the splitPane
             * as a side-effect.
             */
            resultsPanel.add(treeView, cardName);
            if (contextView != null) {
                contextView.unbindFromTreeSelection(tree);
                rememberDividerLocation();
            }
        } else {
            assert resultModel != null;

            cardName = "tree and context";                              //NOI18N
            if (splitPane == null) {
                contextView = new ContextView(resultModel);
                splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                                           true,     //continuous layout
                                           treeView,         //left pane
                                           contextView);     //right pane
                splitPane.setBorder(BorderFactory.createEmptyBorder());
                splitPane.setResizeWeight(0.4d);
                if( isMacLaf ) {
                    contextView.setBackground(macBackground);
                    splitPane.setBackground(macBackground);
                }
                resultsPanel.add(splitPane, cardName);
            } else {
                /*
                 * The following line removes the treeView from the resultsPanel
                 * as a side-effect.
                 */
                splitPane.setLeftComponent(treeView);
            }
            setDividerLocation();
            contextView.bindToTreeSelection(tree);
        }
        /*
         * Changing cards hides the component that represented the previous
         * card and shows the new card. In this case, if card "tree only"
         * is going to be replaced with "tree and context", component 'treeView'
         * is going to be hidden. But we want 'treeView' to be visible - just
         * at a different context (inside the split pane). To ensure visibility
         * of 'treeView', we must make call setVisible(true) explicitely
         * after the card swap.
         */
        resultViewCards.show(resultsPanel, cardName);
        treeView.setVisible(true);
    }

    /**
     * Jumps to the next or previous match.
     *
     * @param  forward  {@code true} for the <em>next</em> match,
     *                  {@code false} for the <em>previous</em> match
     * @see  #goToPrev()
     */
    void goToNext(final boolean forward) {
        assert EventQueue.isDispatchThread();
        assert resultModel != null;

        if (!hasResults) {
            return;
        }

        TreePath leadPath = tree.getLeadSelectionPath();
        if (leadPath == null) {
            leadPath = new TreePath(tree.getModel().getRoot());
        }

        final TreePath nextPath = findNextPath(leadPath, forward);

        if (nextPath != null) {
            /*
             * If we did not expand the parent path explicitely,
             * attached TreeSelectionListeners might not be able to get
             * row for the 'nextPath' because of the parent path still
             * collapsed.
             */
            tree.expandPath(nextPath.getParentPath());

            tree.setSelectionPath(nextPath);
            tree.scrollRectToVisible(tree.getPathBounds(nextPath));

            arrowUpdater.update();

            goToDetail(nextPath);
        }
    }

    private void goToDetail(TreePath treePath){
        Object comp = treePath.getLastPathComponent();
        if ((comp != null) && (comp instanceof DetailNode)){
            DetailNode nodeDetail = (DetailNode)comp;
            nodeDetail.gotoDetail();
        }
    }

    /**
     */
    private TreePath findNextPath(final TreePath forPath,
                                  final boolean forward) {
        TreePath nextPath;

        Object root;
        TreePath parentPath = forPath.getParentPath();
        if (parentPath == null) {         //selected = root node
            root = forPath.getLastPathComponent();
            nextPath = forward ? getNextDetail(root, null, -1, forward) : null;
        } else {
            MatchingObject matchingObj;
            Object lastComp = forPath.getLastPathComponent();
            if (lastComp.getClass() == MatchingObject.class) {
                root = parentPath.getLastPathComponent();
                matchingObj = (MatchingObject) lastComp;
                nextPath = getNextDetail(root, matchingObj, -1, forward);
            } else {
                root = parentPath.getParentPath().getLastPathComponent();
                Object parentComp = parentPath.getLastPathComponent();
                assert parentComp.getClass() == MatchingObject.class;
                matchingObj = (MatchingObject) parentComp;

                int parentPathRow = tree.getRowForPath(parentPath);
                int row = tree.getRowForPath(forPath);
                int index = row - parentPathRow - 1;
                nextPath = getNextDetail(root, matchingObj, index, forward);
            }
        }

        return nextPath;
    }

    /**
     * Finds path to the first detail node following or preceding
     * the node specified by couple ({@code MatchingObject}, detail index).
     *
     * @param  root  root object of the tree model
     *               (it will be used as a first component of any
     *               non-{@code null} returned path)
     * @param  matchingObj  the currently selected {@code MatchingObject},
     *                      or {@code null} if the tree's root is selected
     * @param  detailIndex  index of the currently selected detail node,
     *                      or {@code -1} if no detail node is selected
     * @param  forward  {@code true} for forward search,
     *                  {@code false} for backward search
     * @return  path to the next detail node,
     *          or {@code null} if no next detail node is available
     */
    private TreePath getNextDetail(final Object root,
                                   final MatchingObject matchingObj,
                                   final int detailIndex,
                                   final boolean forward) {
        if (matchingObj != null) {
            int nextDetailIndex = forward ? detailIndex + 1
                                          : detailIndex - 1;
            if ((nextDetailIndex >= 0)
                    && (nextDetailIndex < resultModel.getDetailsCount(
                                                                matchingObj))) {
                return new TreePath(new Object[] {
                         root,
                         matchingObj,
                         resultModel.getDetails(matchingObj)[nextDetailIndex]});
            }
        } else /*(matchingObj == null)*/ if (!forward) {
            return null;
        }

        final List<MatchingObject> matchingObjs =
                resultModel.getMatchingObjects();
        int currMatchingObjIndex = getMatchingObjIndex(matchingObjs,
                                                       matchingObj,
                                                       forward);
        MatchingObject nextMatchingObj;
        int i;

        if (forward) {
            for (i = currMatchingObjIndex + 1; i < matchingObjs.size(); i++) {
                nextMatchingObj = matchingObjs.get(i);
                if (resultModel.hasDetails(nextMatchingObj)) {
                    Node[] details = resultModel.getDetails(nextMatchingObj);
                    if(details == null) { // #177642
                        continue;
                    }
                    return new TreePath(new Object[] {
                            root,
                            nextMatchingObj,
                            details[0]});
                }
            }
        } else {
            for (i = currMatchingObjIndex - 1; i >= 0; i--) {
                nextMatchingObj = matchingObjs.get(i);
                if (resultModel.hasDetails(nextMatchingObj)) {
                    Node[] details = resultModel.getDetails(nextMatchingObj);
                    return new TreePath(new Object[] {
                            root,
                            nextMatchingObj,
                            details[details.length - 1]});
                }
            }
        }
        return null;
    }

    /**
     */
    private int getMatchingObjIndex(final List<MatchingObject> matchingObjs,
                                    final MatchingObject matchingObj,
                                    final boolean forward) {
        if (matchingObj == null) {
            return -1;
        }

        if (matchingObj == matchingObjIndexCacheObj) {
            assert (matchingObjIndexCacheIndex != -1);
            return matchingObjIndexCacheIndex;
        }

        int foundIndex = -1;

        /* Probe several positions below and above the cached index: */
        if (matchingObjIndexCacheIndex != -1) {
            final int quickSearchRange = 3;
            int startIndex;
            int endIndex;
            int i;
            if (forward) {
                startIndex = Math.min(matchingObjIndexCacheIndex + 1,
                                      matchingObjs.size() - 1);
                endIndex = Math.min(
                                  matchingObjIndexCacheIndex + quickSearchRange,
                                  matchingObjs.size() - 1);
                for (i = startIndex; i <= endIndex; i++) {
                    if (matchingObjs.get(i) == matchingObj) {
                        foundIndex = i;
                        break;
                    }
                }
                if ((foundIndex == -1) && (matchingObjIndexCacheIndex > 0)) {
                    if (matchingObjs.get(i = matchingObjIndexCacheIndex - 1)
                            == matchingObj) {
                        foundIndex = i;
                    }
                }
            } else { /*backward*/
                startIndex = Math.max(matchingObjIndexCacheIndex - 1, 0);
                endIndex = Math.max(
                                  matchingObjIndexCacheIndex - quickSearchRange,
                                  0);
                for (i = startIndex; i >= endIndex; i--) {
                    if (matchingObjs.get(i) == matchingObj) {
                        foundIndex = i;
                        break;
                    }
                }
                if ((foundIndex == -1)
                    && (matchingObjIndexCacheIndex < matchingObjs.size() - 1)) {
                    if (matchingObjs.get(i = matchingObjIndexCacheIndex + 1)
                            == matchingObj) {
                        foundIndex = i;
                    }
                }
            }
        }

        /* Nothing found near the cached position - search from the beginning */
        if (foundIndex == -1) {
            for (int i = 0; i < matchingObjs.size(); i++) {
                if (matchingObj == matchingObjs.get(i)) {
                    foundIndex = i;
                    break;
                }
            }
        }

        /* If the matching index is found, store it to the cache: */
        if (foundIndex != -1) {
            matchingObjIndexCacheObj = matchingObj;
            matchingObjIndexCacheIndex = foundIndex;
        }

        return foundIndex;
    }

    /**
     * Makes a list of clones of given {@code SearchType}s.
     * The given {@code SearchType}s are checked such that those that are
     * no longer supported by the current set of IDE modules are skipped.
     *
     * @param  searchTypes  list of {@code SearchType}s to be cloned
     * @return  list of cloned {@code SearchType}s, with unsupported
     *		{@code SearchType}s omitted
     */
    private static List<SearchType> cloneAvailableSearchTypes(List<SearchType> searchTypes) {
        /* build a collection of class names of supported SearchTypes: */
        Collection<? extends SearchType> availableSearchTypes = Utils.getSearchTypes();
            Collection<String> availableSearchTypeNames
                    = new ArrayList<String>(availableSearchTypes.size());
            for (SearchType searchType : availableSearchTypes) {
                availableSearchTypeNames.add(searchType.getClass().getName());
            }

        if (availableSearchTypeNames.isEmpty()) {
                return Collections.<SearchType>emptyList();     //trivial case
        }

        /* clone all supported SearchTypes: */
        List<SearchType> clones = new ArrayList<SearchType>(searchTypes.size());
        for (SearchType searchType : searchTypes) {
                if (availableSearchTypeNames.contains(searchType.getClass().getName())) {
                    clones.add((SearchType) searchType.clone());
                }
        }
        return clones;
    }

    /** (Re)open the dialog window for entering (new) search criteria. */
    private void customizeCriteria() {
        assert EventQueue.isDispatchThread();

        BasicSearchCriteria basicSearchCriteriaClone
            = (basicSearchCriteria != null)
                  ? new BasicSearchCriteria(basicSearchCriteria)
                  : new BasicSearchCriteria();
        List<SearchType> extraSearchTypesClones
            = cloneAvailableSearchTypes(searchTypes);

        SearchPanel searchPanel = new SearchPanel(
                SearchScopeRegistry.getDefault().getSearchScopes(),
                searchScopeType,
                basicSearchCriteriaClone,
                extraSearchTypesClones);
        searchPanel.showDialog();

        if (searchPanel.getReturnStatus() != SearchPanel.RET_OK) {
            return;
        }

        SearchScope searchScope = searchPanel.getSearchScope();
        searchScopeType = searchScope.getTypeId();
        basicSearchCriteria = searchPanel.getBasicSearchCriteria();
        searchTypes = searchPanel.getSearchTypes();

        Manager.getInstance().stopSearching(task);
        task = new SearchTask(searchScope, basicSearchCriteria, searchPanel.getCustomizedSearchTypes());
        ResultView.getInstance().addSearchPair(this, task);
        Manager.getInstance().scheduleSearchTask(task);
    }

    /**
     * Called when the <em>Replace</em> button is pressed.
     */
    private void replaceMatches() {
        assert EventQueue.isDispatchThread();

        nodeListener.setSelectionChangeEnabled(false);
        btnReplace.setEnabled(false);

        ReplaceTask taskReplace =
                new ReplaceTask(resultModel.getMatchingObjects());
        ResultView.getInstance().addReplacePair(taskReplace, this);
        Manager.getInstance().scheduleReplaceTask(taskReplace);
    }

    void setBtnModifyEnabled(boolean enabled){
        btnModifySearch.setEnabled(enabled);
    }

    void setBtnStopEnabled(boolean enabled){
        btnStop.setEnabled(enabled);
    }

    void setBtnReplaceEnabled(boolean enabled){
        btnReplace.setEnabled(enabled && !basicSearchCriteria.isReplacePatternInvalid());
    }

    void setBtnShowDetailsEnabled(boolean enabled){
        btnShowDetails.setEnabled(enabled);
    }

    void setBtnReplaceVisible(boolean visible){
        btnReplace.setVisible(visible);
    }

    /**
     */
    private void rememberDividerLocation() {
        if (splitPane == null) {
            return;
        }

        dividerLocation
               = (double) splitPane.getDividerLocation()
                 / (double) (splitPane.getWidth() - splitPane.getDividerSize());
    }

    /**
     */
    private void setDividerLocation() {
        assert splitPane != null;

        if (dividerLocation != -1.0d) {
            splitPane.setDividerLocation(dividerLocation);
        }
    }

    /**
     * This listener updates "enabled" property of Up and Down button
     */
    private class ArrowStatusUpdater implements KeyListener, MouseListener {

        private ResultViewPanel resultViewPanel;

        public ArrowStatusUpdater(ResultViewPanel component) {
            resultViewPanel = component;
        }

        private void update() {
            if (resultModel == null || tree == null) {
                return;
            }

            if (!resultViewPanel.hasResults) {
                btnPrev.setEnabled(false);
                btnNext.setEnabled(false);
            } else {
                TreePath leadPath = tree.getLeadSelectionPath();
                if (leadPath == null) {
                    btnPrev.setEnabled(false);
                    btnNext.setEnabled(true);
                } else {
                    btnPrev.setEnabled(findNextPath(leadPath, false) != null);
                    btnNext.setEnabled(findNextPath(leadPath, true) != null);
                }
            }
        }

        public void keyReleased(KeyEvent e) {
            int key = e.getKeyCode();

            if (key == KeyEvent.VK_ENTER || key == KeyEvent.VK_SPACE
                    || key == KeyEvent.VK_UP || key == KeyEvent.VK_DOWN
                    || key == KeyEvent.VK_LEFT || key == KeyEvent.VK_RIGHT) {
                update();
            }
        }

        public void mousePressed(MouseEvent e) {
            update();
        }

        public void keyPressed(KeyEvent e) {
        }

        public void keyTyped(KeyEvent e) {
        }

        public void mouseClicked(MouseEvent e) {
        }

        public void mouseReleased(MouseEvent e) {
        }

        public void mouseEntered(MouseEvent e) {
        }

        public void mouseExited(MouseEvent e) {
        }
    }

}
