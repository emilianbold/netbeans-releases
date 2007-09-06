/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.search;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.EventQueue;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.lang.ref.Reference;
import java.text.MessageFormat;
import java.util.List;
import java.util.ResourceBundle;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import javax.accessibility.AccessibleContext;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.tree.TreePath;
import org.openide.awt.Mnemonics;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.openidex.search.SearchType;
import static java.lang.Thread.NORM_PRIORITY;

/**
 * Panel which displays search results in explorer like manner.
 * This panel is a singleton.
 *
 * @see  <a href="doc-files/results-class-diagram.png">Class diagram</a>
 * @author Petr Kuzel, Jiri Mzourek, Peter Zavadsky
 * @author  Marian Petras
 */
final class ResultView extends TopComponent {
    
    /** display the matching string location in context by default? */
    private static final boolean SHOW_CONTEXT_BY_DEFAULT = true;
    /** */
    private static final String RESULTS_CARD = "results";               //NOI18N
    /** */
    private static final String ISSUES_CARD = "issues";                 //NOI18N
    
    /** should the context view be visible when doing search &amp; replace? */
    private boolean contextViewEnabled = SHOW_CONTEXT_BY_DEFAULT;
    /** is the context view visible? */
    private boolean contextViewVisible = false;
    /** */
    private double dividerLocation = -1.0d;
    /** */
    private boolean ignoreContextButtonToggle = false;
    /** */
    private boolean hasResults = false;     //accessed only from the EventQueue
    /** */
    private int objectsCount = 0;           //accessed only from the EventQueue
    /** */
    private volatile boolean hasDetails = false;
    /** */
    private volatile boolean searchInProgress = false;
                           
    /** unique ID of <code>TopComponent</code> (singleton) */
    private static final String ID = "search-results";                  //NOI18N
    
    /**
     * instance/singleton of this class
     *
     * @see  #getInstance
     */
    private static Reference<ResultView> instance = null;
    
    /**
     * tree view for displaying found objects
     */
    private final JScrollPane treeView;
    
    /** Result data model. */
    private ResultModel resultModel = null;
    /** */
    private ResultTreeModel treeModel = null;
    
    /** */
    private final JTree tree;
    /** listens on various actions performed on nodes in the tree */
    private final NodeListener nodeListener;
    
    /** */
    private SearchScope searchScope;
    /** */
    private BasicSearchCriteria basicSearchCriteria;
    /** */
    private List<SearchType> searchTypes;
    
    /** template for displaying number of matching files found so far */
    private MessageFormat nodeCountFormat;
    /**
     * template for displaying of number of matching files and total number
     * of matches found so far
     */
    private MessageFormat nodeCountFormatFullText;

    /**
     * Returns a singleton of this class.
     *
     * @return  singleton of this <code>TopComponent</code>
     */
    static synchronized ResultView getInstance() {
        ResultView view;
        view = (ResultView) WindowManager.getDefault().findTopComponent(ID);
        if (view == null) {
            view = getDefault();
        }
        return view;
    }

    /**
     * Singleton accessor reserved for the window systemm only. The window
     * system calls this method to create an instance of this
     * <code>TopComponent</code> from a <code>.settings</code> file.
     * <p>
     * <em>This method should not be called anywhere except from the window
     * system's code. </em>
     *
     * @return  singleton - instance of this class
     */
    public static synchronized ResultView getDefault() {
        ResultView view;
        if (instance == null) {
            view = new ResultView();
            instance = new WeakReference<ResultView>(view);
        } else {
            view = instance.get();
            if (view == null) {
                view = new ResultView();
                instance = new WeakReference<ResultView>(view);
            }
        }
        return view;
    }
    
    private final CardLayout contentCards;
    private final CardLayout resultViewCards;
    private final JPanel mainPanel;
    private final JPanel resultsPanel;
    private final JButton btnReplace;
    private final JButton btnModifySearch;
    private final JButton btnShowDetails;
    private final JButton btnStop;
    private final JButton btnPrev;
    private final JButton btnNext;
    private final JToggleButton btnDisplayContext;
    
    private JSplitPane splitPane;
    private ContextView contextView;
    private IssuesPanel issuesPanel;
    
    /** Creates a new <code>ResultView</code>. */
    private ResultView() {
        //PENDING - icons, accessible names, accessible descriptions
        JToolBar toolBar = new JToolBar(SwingConstants.VERTICAL);
        btnDisplayContext = new JToggleButton();
        btnDisplayContext.setIcon(new ImageIcon(Utilities.loadImage(
                "org/netbeans/modules/search/res/context.gif", true))); //NOI18N
        btnDisplayContext.setToolTipText(
                NbBundle.getMessage(getClass(), "TOOLTIP_ShowContext"));//NOI18N
        btnDisplayContext.getAccessibleContext().setAccessibleDescription(
                NbBundle.getMessage(getClass(), "ACSD_ShowContext"));   //NOI18N
        btnDisplayContext.setSelected(SHOW_CONTEXT_BY_DEFAULT);
        btnPrev = new JButton();
        btnPrev.setIcon(new ImageIcon(Utilities.loadImage(
                "org/netbeans/modules/search/res/prev.png", true)));    //NOI18N
        btnNext = new JButton();
        btnNext.setIcon(new ImageIcon(Utilities.loadImage(
                "org/netbeans/modules/search/res/next.png", true)));    //NOI18N
        toolBar.add(btnDisplayContext);
        toolBar.add(new JToolBar.Separator());
        toolBar.add(btnPrev);
        toolBar.add(btnNext);
        toolBar.setRollover(true);
        toolBar.setFloatable(false);
        
        treeModel = createTreeModel();
        tree = createTree(treeModel, nodeListener = new NodeListener());
        treeView = new JScrollPane(tree);
        treeView.getAccessibleContext().setAccessibleDescription(
                NbBundle.getMessage(ResultView.class, "ACS_TREEVIEW")); //NOI18N
        treeView.setBorder(BorderFactory.createEmptyBorder());
        
        resultsPanel = new JPanel(resultViewCards = new CardLayout());

        btnShowDetails = new JButton();
        btnModifySearch = new JButton();
        btnStop = new JButton();
        btnReplace = new JButton();
        
        /* initialize listening for buttons: */
        ButtonListener buttonListener = new ButtonListener();
        btnShowDetails.addActionListener(buttonListener);
        btnModifySearch.addActionListener(buttonListener);
        btnStop.addActionListener(buttonListener);
        btnReplace.addActionListener(buttonListener);
        btnPrev.addActionListener(buttonListener);
        btnNext.addActionListener(buttonListener);
        btnDisplayContext.addItemListener(buttonListener);
        
        Mnemonics.setLocalizedText(
                btnStop,
                NbBundle.getMessage(getClass(), "TEXT_BUTTON_STOP"));   //NOI18N
        Mnemonics.setLocalizedText(
                btnShowDetails,
                NbBundle.getMessage(getClass(), "TEXT_BUTTON_FILL"));   //NOI18N
        Mnemonics.setLocalizedText(
                btnReplace,
                NbBundle.getMessage(getClass(), "TEXT_BUTTON_REPLACE"));//NOI18N
        Mnemonics.setLocalizedText(
                btnModifySearch,
                NbBundle.getMessage(getClass(),
                                    "TEXT_BUTTON_CUSTOMIZE"));          //NOI18N
        
        btnStop.setEnabled(false);
        btnShowDetails.setEnabled(false);
        
        btnReplace.setVisible(false);
        
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.X_AXIS));
        buttonsPanel.add(btnReplace);
        buttonsPanel.add(Box.createHorizontalGlue());
        buttonsPanel.add(btnShowDetails);
        buttonsPanel.add(btnModifySearch);
        buttonsPanel.add(btnStop);

        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(0, 5));
        mainPanel.add(toolBar, BorderLayout.WEST);
        mainPanel.add(resultsPanel, BorderLayout.CENTER);
        mainPanel.add(buttonsPanel, BorderLayout.SOUTH);
        //issue #46261 - "Search Results window must be opaque under GTK"
        mainPanel.setOpaque(true);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 2, 0));
        
        setLayout(contentCards = new CardLayout());
        add(mainPanel, RESULTS_CARD);

        setName("Search Results");                                      //NOI18N
        setDisplayName(NbBundle.getMessage(ResultView.class,
                                           "TITLE_SEARCH_RESULTS"));    //NOI18N
        setToolTipText(NbBundle.getMessage(ResultView.class,
                                           "TOOLTIP_SEARCH_RESULTS"));  //NOI18N
        setIcon(Utilities.loadImage(
                "org/netbeans/modules/search/res/find.gif"));           //NOI18N
        
        initAccessibility();
        
        resultModelChanged();
    }
    
    /**
     */
    private static ResultTreeModel createTreeModel() {
        ResultTreeModel treeModel = new ResultTreeModel(null);
        treeModel.setRootDisplayName(getInitialRootNodeText());
        return treeModel;
    }
    
    /**
     */
    private static JTree createTree(ResultTreeModel treeModel,
                                    NodeListener nodeListener) {
        JTree tree = new JTree(treeModel);
        tree.setCellRenderer(new NodeRenderer(false));
        tree.putClientProperty("JTree.lineStyle", "Angled");            //NOI18N
        
        tree.addMouseListener(nodeListener);
        tree.addKeyListener(nodeListener);
        tree.addTreeWillExpandListener(nodeListener);
        tree.addTreeExpansionListener(nodeListener);
        
        tree.setToggleClickCount(0);
        
        return tree;
    }
    
    /**
     */
    private static String getInitialRootNodeText() {
        return NbBundle.getMessage(ResultView.class,
                                   "TEXT_Search_in_filesystems");       //NOI18N
    }
    
    /** Overriden to explicitely set persistence type of ResultView
     * to PERSISTENCE_NEVER */
    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ALWAYS;  // XXX protimluv
    }

    /** Replaces this in object stream. */
    @Override
    public Object writeReplace() {
        return new ResolvableHelper();
    }

    final public static class ResolvableHelper implements java.io.Serializable {
        static final long serialVersionUID = 7398708142639457544L;
        public Object readResolve() {
            return ResultView.getDefault();
        }
    }

    /**
     * Resolves to the {@linkplain #getDefault default instance} of this class.
     *
     * This method is necessary for correct functinality of window system's
     * mechanism of persistence of top components.
     */
    private Object readResolve() throws java.io.ObjectStreamException {
        return ResultView.getDefault();
    }
    
    private void initAccessibility() {
        ResourceBundle bundle = NbBundle.getBundle(ResultView.class);
        getAccessibleContext ().setAccessibleName (bundle.getString ("ACSN_ResultViewTopComponent"));                   //NOI18N
        getAccessibleContext ().setAccessibleDescription (bundle.getString ("ACSD_ResultViewTopComponent"));            //NOI18N

        AccessibleContext accessCtx;
        
        accessCtx = treeView.getHorizontalScrollBar().getAccessibleContext();
        accessCtx.setAccessibleName(
                bundle.getString("ACSN_HorizontalScrollbar"));          //NOI18N
        
        accessCtx = treeView.getVerticalScrollBar().getAccessibleContext();
        accessCtx.setAccessibleName(
                bundle.getString("ACSN_VerticalScrollbar"));            //NOI18N

        accessCtx = treeView.getAccessibleContext();
        accessCtx.setAccessibleName(
                bundle.getString("ACSN_ResultTree"));                   //NOI18N
        accessCtx.setAccessibleDescription(
                bundle.getString("ACSD_ResultTree"));                   //NOI18N
        
        btnReplace.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_TEXT_BUTTON_REPLACE"));    //NOI18N
        btnModifySearch.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_TEXT_BUTTON_CUSTOMIZE")); //NOI18N
        btnShowDetails.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_TEXT_BUTTON_FILL"));         //NOI18N
        btnStop.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_TEXT_BUTTON_STOP"));           //NOI18N
    }       
    
    /**
     * This method exists just to make the <code>close()</code> method
     * accessible via <code>Class.getDeclaredMethod(String, Class[])</code>.
     * It is used in <code>Manager</code>.
     */
    void closeResults() {
        close();
    }
    
    /** Send search details to output window. */
    public void fillOutput() {
        btnShowDetails.setEnabled(false);
        Manager.getInstance()
               .schedulePrintingDetails(resultModel.getFoundObjects(),
                                        basicSearchCriteria,
                                        searchTypes);
    }
    
    /**
     */
    private void setRootDisplayName(String displayName) {
        treeModel.setRootDisplayName(displayName);
    }
    
    @Override
    protected void componentOpened() {
        assert EventQueue.isDispatchThread();
        
        Manager.getInstance().searchWindowOpened();
        
        setRootDisplayName(getInitialRootNodeText());
        /*selectAndActivateNode(root);*/
        if (searchScope == null) {
            btnModifySearch.setEnabled(false);
        }
    }

    @Override
    public void requestFocus() {
        tree.requestFocus();
    }

    @Override
    public boolean requestFocusInWindow() {
        return tree.requestFocusInWindow();
    }

    @Override
    protected void componentClosed() {
        assert EventQueue.isDispatchThread();
        
        rememberInput(null, null, null);
        Manager.getInstance().searchWindowClosed();
        
        if (contextView != null) {
            contextView.unbindFromTreeSelection(tree);
            contextView = null;
        }
        if (splitPane != null) {
            rememberDividerLocation();
            resultsPanel.remove(splitPane);
            splitPane = null;
        }
        if (issuesPanel != null) {
            removeIssuesPanel();
        }
        contextViewVisible = false;
        contextViewEnabled = SHOW_CONTEXT_BY_DEFAULT;
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
    void notifySearchPending(final int blockingTask) {
        assert EventQueue.isDispatchThread();
        
        removeIssuesPanel();
        
        String msgKey = null;
        switch (blockingTask) {
            case Manager.SEARCHING:
                msgKey = "TEXT_FINISHING_PREV_SEARCH";                  //NOI18N
                break;
            case Manager.CLEANING_RESULT:
                msgKey = "TEXT_CLEANING_RESULT";                        //NOI18N
                break;
            case Manager.PRINTING_DETAILS:
                msgKey = "TEXT_PRINTING_DETAILS";                       //NOI18N
                break;
            default:
                assert false;
        }
        setRootDisplayName(NbBundle.getMessage(ResultView.class, msgKey));
        btnStop.setEnabled(true);
        btnReplace.setEnabled(false);
    }
    
    /**
     */
    void searchTaskStateChanged(final int changeType) {
        switch (changeType) {
            case Manager.EVENT_SEARCH_STARTED:
                searchStarted();
                break;
            case Manager.EVENT_SEARCH_FINISHED:
                searchFinished();
                break;
            case Manager.EVENT_SEARCH_INTERRUPTED:
                searchInterrupted();
                break;
            case Manager.EVENT_SEARCH_CANCELLED:
                searchCancelled();
                break;
            default:
                assert false;
        }
    }
    
    /**
     */
    void showAllDetailsFinished() {
        assert EventQueue.isDispatchThread();
        
        updateShowAllDetailsBtn();
    }
    
    /**
     */
    private void searchStarted() {
        assert EventQueue.isDispatchThread();
        
        removeIssuesPanel();
        
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
        btnModifySearch.setEnabled(true);
        btnStop.setEnabled(true);
        btnReplace.setEnabled(false);
    }
    
    /**
     */
    private void searchFinished() {
        assert EventQueue.isDispatchThread();
        
        setFinalRootNodeText();
        
        searchInProgress = false;
        hasDetails = (resultModel != null) ? resultModel.hasDetails() : false;
        updateShowAllDetailsBtn();
        btnStop.setEnabled(false);
        btnReplace.setEnabled(true);
    }
    
    /**
     */
    private void searchInterrupted() {
        assert EventQueue.isDispatchThread();
        
        searchFinished();
    }
    
    /**
     */
    private void searchCancelled() {
        assert EventQueue.isDispatchThread();
        
        setRootDisplayName(NbBundle.getMessage(ResultView.class,
                                               "TEXT_TASK_CANCELLED"));//NOI18N
        
        searchInProgress = true;
        updateShowAllDetailsBtn();
        btnStop.setEnabled(false);
        btnReplace.setEnabled(true);
    }
    
    /**
     */
    private void setFinalRootNodeText() {
        assert EventQueue.isDispatchThread();
        
        int resultSize = resultModel.size();
        
        if (resultModel.wasLimitReached()) {
            setRootDisplayName(
                    NbBundle.getMessage(ResultView.class,
                                        "TEXT_MSG_FOUND_X_NODES_LIMIT", //NOI18N
                                        new Integer(resultSize)));
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
    
    /**
     */
    private void updateShowAllDetailsBtn() {
        assert EventQueue.isDispatchThread();
        
        btnShowDetails.setEnabled(hasResults
                                  && !searchInProgress
                                  && hasDetails);
    }
    
    /** Set new model. */
    synchronized void setResultModel(final ResultModel resultModel) {
        assert EventQueue.isDispatchThread();
        
        if ((this.resultModel == null) && (resultModel == null)) {
            return;
        }
        
        boolean hadCheckBoxes = (this.resultModel != null)
                                && this.resultModel.searchAndReplace;
        boolean hasCheckBoxes = (resultModel != null)
                                && resultModel.searchAndReplace;
        
        this.resultModel = resultModel;     //may be null!
        
        tree.setModel(treeModel = new ResultTreeModel(resultModel));
        if (hasCheckBoxes != hadCheckBoxes) {
            tree.setCellRenderer(new NodeRenderer(hasCheckBoxes));
            btnReplace.setVisible(hasCheckBoxes);
        }
        if (resultModel != null) {
            hasResults = !resultModel.isEmpty();
            hasDetails = hasResults && resultModel.hasDetails();
            resultModel.setObserver(this);
        } else {
            hasResults = false;
            hasDetails = false;
        }

        resultModelChanged();
        
        updateShowAllDetailsBtn();
    }
    
    /**
     */
    private void resultModelChanged() {
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
     * Enables or disables the <em>Display Context</em> button,
     * according to the result model currently displayed.
     * 
     * @see  #updateContextViewVisibility
     */
    private void updateDisplayContextButton() {
        boolean searchAndReplace = isSearchAndReplace();
        btnDisplayContext.setEnabled(searchAndReplace);
        
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
    void removeIssuesPanel() {
        if (issuesPanel != null) {
            remove(issuesPanel);
            issuesPanel = null;
        }
        contentCards.show(this, RESULTS_CARD);
    }
    
    /**
     * Jumps to the next or previous match.
     * 
     * @param  forward  {@code true} for the <em>next</em> match,
     *                  {@code false} for the <em>previous</em> match
     * @see  #goToPrev()
     */
    private void goToNext(final boolean forward) {
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
        
        final MatchingObject[] matchingObjs = resultModel.getMatchingObjects();
        int currMatchingObjIndex = getMatchingObjIndex(matchingObjs,
                                                       matchingObj,
                                                       forward);
        MatchingObject nextMatchingObj;
        int i;
        
        if (forward) {
            for (i = currMatchingObjIndex + 1; i < matchingObjs.length; i++) {
                nextMatchingObj = matchingObjs[i];
                if (resultModel.hasDetails(nextMatchingObj)) {
                    return new TreePath(new Object[] {
                            root,
                            nextMatchingObj,
                            resultModel.getDetails(nextMatchingObj)[0]});
                }
            }
        } else {
            for (i = currMatchingObjIndex - 1; i >= 0; i--) {
                nextMatchingObj = matchingObjs[i];
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
    private int getMatchingObjIndex(final MatchingObject[] matchingObjs,
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
                                      matchingObjs.length - 1);
                endIndex = Math.min(
                                  matchingObjIndexCacheIndex + quickSearchRange,
                                  matchingObjs.length - 1);
                for (i = startIndex; i <= endIndex; i++) {
                    if (matchingObjs[i] == matchingObj) {
                        foundIndex = i;
                        break;
                    }
                }
                if ((foundIndex == -1) && (matchingObjIndexCacheIndex > 0)) {
                    if (matchingObjs[i = matchingObjIndexCacheIndex - 1]
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
                    if (matchingObjs[i] == matchingObj) {
                        foundIndex = i;
                        break;
                    }
                }
                if ((foundIndex == -1)
                    && (matchingObjIndexCacheIndex < matchingObjs.length - 1)) {
                    if (matchingObjs[i = matchingObjIndexCacheIndex + 1]
                            == matchingObj) {
                        foundIndex = i;
                    }
                }
            }
        }
        
        /* Nothing found near the cached position - search from the beginning */
        if (foundIndex == -1) {
            for (int i = 0; i < matchingObjs.length; i++) {
                if (matchingObj == matchingObjs[i]) {
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
     */
    void rememberInput(SearchScope searchScope,
                       BasicSearchCriteria basicSearchCriteria,
                       List<SearchType> searchTypes) {
        this.searchScope = searchScope;
	this.basicSearchCriteria = basicSearchCriteria;
        this.searchTypes = searchTypes;
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
                searchScope,
                basicSearchCriteriaClone,
                extraSearchTypesClones);
        searchPanel.showDialog();
        
        if (searchPanel.getReturnStatus() != SearchPanel.RET_OK) {
            return;
        }
        
        searchScope = searchPanel.getSearchScope();
        basicSearchCriteria = searchPanel.getBasicSearchCriteria();
        searchTypes = searchPanel.getSearchTypes();

        Manager.getInstance().scheduleSearchTask(
                new SearchTask(searchScope,
                               basicSearchCriteria,
                               searchPanel.getCustomizedSearchTypes()));
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
    
    /**
     * Called when the <em>Replace</em> button is pressed.
     */
    private void replaceMatches() {
        assert EventQueue.isDispatchThread();
        
        nodeListener.setSelectionChangeEnabled(false);
        btnReplace.setEnabled(false);
        
        Manager.getInstance().scheduleReplaceTask(
                        new ReplaceTask(resultModel.getMatchingObjects()));
    }
    
    /**
     */
    void closeAndSendFocusToEditor() {
        assert EventQueue.isDispatchThread();
        
        close();
        
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
    void displayIssuesToUser(String title, String[] problems, boolean reqAtt) {
        assert EventQueue.isDispatchThread();
        assert issuesPanel == null;
        
        issuesPanel = new IssuesPanel(title, problems);
        add(issuesPanel, ISSUES_CARD);
        contentCards.show(this, ISSUES_CARD);
        
        if (!isOpened()) {
            open();
        }
        if (reqAtt) {
            requestAttention(true);
        }
    }
    
    /**
     */
    void rescan() {
        assert EventQueue.isDispatchThread();
        
        removeIssuesPanel();
        Manager.getInstance().scheduleSearchTaskRerun();
    }

    @Override
    protected String preferredID() {
        return getClass().getName();
    }
    
    /**
     */
    private class ButtonListener implements ActionListener, ItemListener {
        
        public void actionPerformed(ActionEvent e) {
            Object source = e.getSource();
            if (source == btnStop) {
                Manager.getInstance().stopSearching();
            } else if (source == btnModifySearch) {
                customizeCriteria();
            } else if (source == btnShowDetails) {
                fillOutput();
            } else if (source == btnReplace) {
                replaceMatches();
            } else if (source == btnPrev) {
                goToNext(false);
            } else if (source == btnNext) {
                goToNext(true);
            } else {
                assert false;
            }
        }

        /**
         * Called when the Display Context button is selected or deselected.
         */
        public void itemStateChanged(ItemEvent e) {
            assert e.getSource() == btnDisplayContext;
            if (!ignoreContextButtonToggle) {
                contextViewEnabled = (e.getStateChange() == ItemEvent.SELECTED);
                updateContextViewVisibility();
            }
        }
        
    }
    
}
