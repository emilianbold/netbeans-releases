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


package org.netbeans.modules.search;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import javax.accessibility.AccessibleContext;
import javax.swing.AbstractButton;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.text.DefaultEditorKit;
import org.openide.ErrorManager;
import org.openide.awt.Mnemonics;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.openidex.search.SearchType;


/**
 * Panel which displays search results in explorer like manner.
 * This panel is a singleton.
 *
 * @see  <a href="doc-files/results-class-diagram.png">Class diagram</a>
 * @author Petr Kuzel, Jiri Mzourek, Peter Zavadsky
 * @author  Marian Petras
 */
final class ResultView extends TopComponent
                       implements ExplorerManager.Provider {
    
    /** */
    private volatile boolean hasResults = false;
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
    private static WeakReference instance = null;
    
    /** manages the tree of nodes representing found objects */
    private final ExplorerManager explorerManager;

    /**
     * tree view for displaying found objects
     */
    private final BeanTreeView treeView;
    
    /** Result data model. */
    private ResultModel resultModel = null;
    
    /** */
    private final RootNode root;
    
    /** */
    private ResultTreeChildren children;
    
    /** */
    private Node[] lastSearchNodes;
    /** */
    private List lastEnabledSearchTypes;


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
            instance = new WeakReference(view);
        } else {
            view = (ResultView) instance.get();
            if (view == null) {
                view = new ResultView();
                instance = new WeakReference(view);
            }
        }
        return view;
    }
    
    /** Creates a new <code>ResultView</code>. */
    private ResultView() {
        initComponents();
        setName("Search Results");                                      //NOI18N
        setDisplayName(NbBundle.getMessage(ResultView.class,
                                           "TITLE_SEARCH_RESULTS"));    //NOI18N
        setToolTipText(NbBundle.getMessage(ResultView.class,
                                           "TOOLTIP_SEARCH_RESULTS"));    //NOI18N
        setIcon(Utilities.loadImage(
                "org/netbeans/modules/search/res/find.gif"));           //NOI18N
        
        // Issue 46261
        mainPanel.setOpaque(true);
        
        buttonsPanel.add(Box.createHorizontalGlue(), 2);
        buttonsPanel.add(Box.createHorizontalStrut(5), 4);
        buttonsPanel.add(Box.createHorizontalStrut(5), 6);
        
        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(sortButton);
        buttonGroup.add(unsortButton);
        
        root = createTreeViewRoot();
        explorerManager = new ExplorerManager();
        explorerManager.setRootContext(root);
        selectAndActivateNode(root);
        explorerManager.addPropertyChangeListener(
                new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent evt) {
                        if (ExplorerManager.PROP_SELECTED_NODES.equals(
                               evt.getPropertyName())) {
                            nodeSelectionChanged();
                        }
                    }
                });
        setupActions();

        /* Create the left part of the window: */
        treeView =  new BeanTreeView();
        treeView.getAccessibleContext().setAccessibleDescription(
                NbBundle.getMessage(ResultView.class, "ACS_TREEVIEW")); //NOI18N
        treeView.setBorder(BorderFactory.createEmptyBorder());
        mainPanel.add(treeView, java.awt.BorderLayout.CENTER);


        /* initialize listening for buttons: */
        ActionListener buttonListener = new ButtonListener();
        sortButton.addActionListener(buttonListener);
        unsortButton.addActionListener(buttonListener);
        btnShowDetails.addActionListener(buttonListener);
        btnModifySearch.addActionListener(buttonListener);
        btnStop.addActionListener(buttonListener);
        
        initAccessibility();

    }
    
    /**
     * Creates an initial node to be displayed in the left pane of this window.
     *
     * @return  the created node
     */
    private final RootNode createTreeViewRoot() {
        RootNode root = new RootNode();
        root.setName(getInitialRootNodeText());
        root.setIconBase("org/netbeans/modules/search/res/find");       //NOI18N
        return root;
    }
    
    /**
     */
    private String getInitialRootNodeText() {
        return NbBundle.getMessage(ResultView.class,
                                   "TEXT_Search_in_filesystems");       //NOI18N
    }
    
    /**
     * Sets up actions Copy, Cut, Paste &amp; Delete so that they are
     * activated/deactivated appropriately and so that they do what they
     * should do.
     */
    private void setupActions() {
        Object copyActionKey   = DefaultEditorKit.copyAction;
        Object cutActionKey    = DefaultEditorKit.cutAction;
        Object pasteActionKey  = DefaultEditorKit.pasteAction;
        Object deleteActionKey = "delete";                              //NOI18N
        
        ActionMap map = getActionMap();
        map.put(copyActionKey,   ExplorerUtils.actionCopy(explorerManager));
        map.put(cutActionKey,    ExplorerUtils.actionCut(explorerManager));
        map.put(pasteActionKey,  ExplorerUtils.actionPaste(explorerManager));
        map.put(deleteActionKey, ExplorerUtils.actionDelete(explorerManager,
                                                            true));
        
        associateLookup(ExplorerUtils.createLookup(explorerManager, map));
    }
    
    protected void componentActivated() {
        ExplorerUtils.activateActions(explorerManager, true);
        treeView.requestFocusInWindow();
    }
    
    protected void componentDeactivated() {
        ExplorerUtils.activateActions(explorerManager, false);
    }

    /**
     * Selects and activates a given node.
     * Selects a given node in the tree of found objects.
     * If the nodes cannot be selected and/or activated,
     * clears the selection (and notifies that no node is currently
     * activated).
     * 
     * @param  node  node to be selected and activated
     */
    private final void selectAndActivateNode(final Node node) {
        Node[] nodeArray = new Node[] {node};
        try {
            explorerManager.setSelectedNodes(nodeArray);
            setActivatedNodes(nodeArray);
        } catch (PropertyVetoException ex) {
            ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, ex);
            nodeArray = new Node[0];
            try {
                explorerManager.setSelectedNodes(nodeArray);
                setActivatedNodes(nodeArray);
            } catch (PropertyVetoException ex2) {
                ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, ex2);
            }
        }
    }
    
    /** Overriden to explicitely set persistence type of ResultView
     * to PERSISTENCE_NEVER */
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ALWAYS;  // XXX protimluv
    }

    /** Replaces this in object stream. */
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
        
        sortButton.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_TEXT_BUTTON_SORT"));           //NOI18N
        unsortButton.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_TEXT_BUTTON_UNSORT"));       //NOI18N
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
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        mainPanel = new javax.swing.JPanel();
        buttonsPanel = new javax.swing.JPanel();

        setLayout(new java.awt.BorderLayout());

        mainPanel.setLayout(new java.awt.BorderLayout());

        mainPanel.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(0, 0, 2, 0)));
        buttonsPanel.setLayout(new javax.swing.BoxLayout(buttonsPanel, javax.swing.BoxLayout.X_AXIS));

        buttonsPanel.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(5, 0, 0, 0)));
        Mnemonics.setLocalizedText(sortButton, NbBundle.getMessage(ResultView.class, "TEXT_BUTTON_SORT"));     //NOI18N
        sortButton.setEnabled(false);
        buttonsPanel.add(sortButton);

        unsortButton.setSelected(true);
        Mnemonics.setLocalizedText(unsortButton, NbBundle.getMessage(ResultView.class, "TEXT_BUTTON_UNSORT"));     //NOI18N
        unsortButton.setEnabled(false);
        buttonsPanel.add(unsortButton);

        Mnemonics.setLocalizedText(
            btnShowDetails,
            NbBundle.getMessage(ResultView.class,
                "TEXT_BUTTON_FILL"));               //NOI18N
            btnShowDetails.setEnabled(false);
            buttonsPanel.add(btnShowDetails);

            Mnemonics.setLocalizedText(
                btnModifySearch,
                NbBundle.getMessage(ResultView.class,
                    "TEXT_BUTTON_CUSTOMIZE"));          //NOI18N
                buttonsPanel.add(btnModifySearch);

                Mnemonics.setLocalizedText(
                    btnStop,
                    NbBundle.getMessage(ResultView.class,
                        "TEXT_BUTTON_STOP"));               //NOI18N
                    btnStop.setEnabled(false);
                    buttonsPanel.add(btnStop);

                    mainPanel.add(buttonsPanel, java.awt.BorderLayout.SOUTH);

                    add(mainPanel, java.awt.BorderLayout.CENTER);

                }//GEN-END:initComponents

    /** Send search details to output window. */
    public void fillOutput() {
        btnShowDetails.setEnabled(false);
        Manager.getInstance()
               .schedulePrintingDetails(children, resultModel.getSearchGroup());
    }
    
    /* overridden */
    protected void componentOpened() {
        Manager.getInstance().searchWindowOpened();
        
        root.setDisplayName(getInitialRootNodeText());
        explorerManager.setRootContext(root);
        selectAndActivateNode(root);
        if (lastSearchNodes == null) {
            btnModifySearch.setEnabled(false);
        }
    }

    /* overridden */
    protected void componentClosed() {
        rememberInput(null, null);
        Manager.getInstance().searchWindowClosed();
        
        Node empty = Node.EMPTY;
        explorerManager.setRootContext(empty);
        selectAndActivateNode(empty);
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
        setRootNodeText(NbBundle.getMessage(ResultView.class, msgKey));
        setStateFromAWT(btnStop, true);
        setStateFromAWT(sortButton, false);
        setStateFromAWT(unsortButton, false);
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
        updateShowAllDetailsBtn();
    }
    
    /**
     */
    private void searchStarted() {
        setRootNodeText(NbBundle.getMessage(ResultView.class,
                                            "TEXT_SEARCHING___"));      //NOI18N
        
        searchInProgress = true;
        updateShowAllDetailsBtn();
        updateSortUnsortBtns();
        setStateFromAWT(btnModifySearch, true);
        setStateFromAWT(btnStop, true);
    }
    
    /**
     */
    private void searchFinished() {
        setFinalRootNodeText();
        
        searchInProgress = false;
        hasDetails = (children != null) ? children.hasDetails()
                                        : false;
        updateShowAllDetailsBtn();
        updateSortUnsortBtns();
        setStateFromAWT(btnStop, false);
    }
    
    /**
     */
    private void searchInterrupted() {
        searchFinished();
    }
    
    /**
     */
    private void searchCancelled() {
        setRootNodeText(NbBundle.getMessage(ResultView.class,
                                            "TEXT_TASK_CANCELLED"));    //NOI18N
        
        searchInProgress = true;
        updateShowAllDetailsBtn();
        updateSortUnsortBtns();
        setStateFromAWT(btnStop, false);
    }
    
    /**
     */
    private void setFinalRootNodeText() {
        int resultSize = resultModel.size();
        
        if (resultModel.wasLimitReached()) {
            setRootNodeText(
                    NbBundle.getMessage(ResultView.class,
                                        "TEXT_MSG_FOUND_X_NODES_LIMIT", //NOI18N
                                        new Integer(resultSize)));
            return;
        }
        
        String baseMsg;
        if (resultSize == 0) {
            baseMsg = NbBundle.getMessage(ResultView.class,
                                          "TEXT_MSG_NO_NODE_FOUND");    //NOI18N
        } else if (resultSize == 1) {
            baseMsg = NbBundle.getMessage(ResultView.class,
                                          "TEXT_MSG_FOUND_A_NODE");     //NOI18N
        } else {
            baseMsg = NbBundle.getMessage(ResultView.class,
                                         "TEXT_MSG_FOUND_X_NODES",      //NOI18N
                                          new Integer(resultSize));
        }
        String exMsg = resultModel.getExceptionMsg();
        String msg = exMsg == null ? baseMsg
                                   : baseMsg + " (" + exMsg + ")";      //NOI18N
        setRootNodeText(msg);
    }
    
    /**
     */
    private void updateShowAllDetailsBtn() {
        setStateFromAWT(btnShowDetails, hasResults
                                        &&
                                        !searchInProgress
                                        &&
                                        hasDetails);
    }
    
    /**
     */
    private void updateSortUnsortBtns() {
        boolean enabled = hasResults && !searchInProgress;
        setStateFromAWT(sortButton, enabled);
        setStateFromAWT(unsortButton, enabled);
    }
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private final javax.swing.JButton btnModifySearch = new javax.swing.JButton();
    private final javax.swing.JButton btnShowDetails = new javax.swing.JButton();
    private final javax.swing.JButton btnStop = new javax.swing.JButton();
    private javax.swing.JPanel buttonsPanel;
    private javax.swing.JPanel mainPanel;
    private final javax.swing.JRadioButton sortButton = new javax.swing.JRadioButton();
    private final javax.swing.JRadioButton unsortButton = new javax.swing.JRadioButton();
    // End of variables declaration//GEN-END:variables
    

    /** Set new model. */
    void setResultModel(final ResultModel resultModel) {
        if ((this.resultModel == null) && (resultModel == null)) {
            return;
        }
        
        if (this.resultModel != null) {
            if (children != null) {
                children.clear();
            }
            children = null;
        }
        this.resultModel = resultModel;
        if (resultModel != null) {
            setChildren(children = new ResultTreeChildren(resultModel));
            hasResults = !children.isEmpty();
            hasDetails = hasResults && children.hasDetails();
            children.setObserver(this);
        } else {
            hasResults = false;
            hasDetails = false;
            setChildren(Children.LEAF);
        }
        
        Mutex.EVENT.writeAccess(new Runnable() {
            public void run() {
                selectAndActivateNode(root);
                
                updateShowAllDetailsBtn();
                updateSortUnsortBtns();
                if (children != null) {
                    if (children.isSorted()) {
                        sortButton.setSelected(true);
                    } else {
                        unsortButton.setSelected(true);
                    }
                }
            }
        });
        
    }
    
    /**
     */
    public void objectFound(Object foundObject) {
        hasResults = true;
        updateRootDisplayName();
    }
    
    /**
     * Updates the number of found nodes in the name of the root node.
     */
    private void updateRootDisplayName() {
        int count = children.getSize();
        setRootNodeText(NbBundle.getMessage(ResultModel.class,
                                            "TXT_RootSearchedNodes",    //NOI18N
                                            Integer.toString(count)));
    }
    
    /**
     */
    private void setRootNodeText(String txt) {
        Method method;
        try {
            method = root.getClass().getMethod("setDisplayName",        //NOI18N
                                               new Class[] {String.class});
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ErrorManager.ERROR, ex);
            return;
        }
        callFromAWT(method, root, txt);
    }
            
    /**
     */
    private void setChildren(final Children children) {
        Method method;
        try {
            method = root.getClass().getDeclaredMethod(
                                             "changeChildren",          //NOI18N
                                             new Class[] {Children.class});
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ErrorManager.ERROR, ex);
            return;
        }
        callFromAWT(method, root, children);
    }
    
    /**
     */
    private void setStateFromAWT(AbstractButton button,
                                 boolean enabled) {
        Method method;
        try {
            method = button.getClass().getMethod("setEnabled",          //NOI18N
                                                 new Class[] {Boolean.TYPE});
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ErrorManager.ERROR, ex);
            return;
        }
        callFromAWT(method, button, Boolean.valueOf(enabled));
    }
    
    /**
     */
    private void callFromAWT(final Method method,
                             final Object object,
                             final Object param) {
        final Runnable routine = new Runnable() {
                public void run() {
                    try {
                        method.invoke(object, new Object[] {param});
                    } catch (Exception ex) {
                        ErrorManager.getDefault().notify(ex);
                    }
                }};
        if (EventQueue.isDispatchThread()) {
            routine.run();
        } else {
            try {
                EventQueue.invokeAndWait(routine);
            } catch (Exception ex) {
                ErrorManager.getDefault().notify(ex);
            }
        }
    }
    
    /* Implements interface ExplorerManager.Provider */
    public ExplorerManager getExplorerManager() {
        return explorerManager;
    }
    
    /**
     * Displays information about the node currently selected in the tree
     * of matching objects. If there is no selected node or if there are
     * multiple nodes selected, clears the panel for displaying details.
     */
    private void nodeSelectionChanged() {
        Node[] nodes = explorerManager.getSelectedNodes();
        setActivatedNodes(nodes);
    }
    
    /**
     */
    void rememberInput(Node[] nodes,
                       List enabledSearchTypes) {
        lastSearchNodes = nodes;
        lastEnabledSearchTypes = enabledSearchTypes;
    }
    
    /** (Re)open the dialog window for entering (new) search criteria. */
    private void customizeCriteria() {
        Node[] nodesToSearch;
        List searchTypes;
        
        assert (lastSearchNodes != null);
        nodesToSearch = lastSearchNodes;
        searchTypes = lastEnabledSearchTypes;
            
        /*
        if (resultModel != null) {
            nodesToSearch = resultModel.getSearchGroup().getSearchRoots();
            searchTypes = resultModel.getEnabledSearchTypes();
        } else {
            Node repositoryNode = RepositoryNodeFactory.getDefault()
                                  .repository(DataFilter.ALL);
            nodesToSearch = new Node[] {repositoryNode};
            searchTypes = SearchPerformer.getTypes(nodesToSearch);
        }
         */

        /* Clone the list (deep copy): */
        List searchTypesClone = new ArrayList(searchTypes.size());
        for (Iterator it = searchTypes.iterator(); it.hasNext(); ) {
            searchTypesClone.add(((SearchType) it.next()).clone());
        }
        
        lastEnabledSearchTypes = searchTypesClone;
        
        SearchPanel searchPanel = new SearchPanel(searchTypesClone, true);
        searchPanel.showDialog();
        
        if (searchPanel.getReturnStatus() != SearchPanel.RET_OK) {
            return;
        }
        
        rememberInput(nodesToSearch,
                      Utils.cloneSearchTypes(searchTypesClone));

        Manager.getInstance().scheduleSearchTask(
                new SearchTask(nodesToSearch,
                               searchTypesClone,
                               searchPanel.getCustomizedSearchTypes()));
    }
    
    protected String preferredID() {
        return getClass().getName();
    }
    
    /**
     * Sorts or unsorts the list nodes representing found objects.
     *
     * @param  sorted  <code>true</code> to sort the nodes,
     *                 <code>false</code> to unsort the nodes
     */
    private void setNodesSorted(boolean sorted) {
        assert children != null;
        Node[] selectedNodes = explorerManager.getSelectedNodes();
        children.sort(sorted);
        //Node root = resultModel.sortNodes(sorted);
        //explorerManager.setRootContext(root);
        try {
            explorerManager.setSelectedNodes(selectedNodes);
            setActivatedNodes(selectedNodes);
        } catch(PropertyVetoException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            selectAndActivateNode(root);
        }
    }
    
    /**
     */
    private class ButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            Object source = e.getSource();
            if (source == btnStop) {
                Manager.getInstance().stopSearching();
            } else if (source == btnModifySearch) {
                customizeCriteria();
            } else if (source == btnShowDetails) {
                fillOutput();
            } else if (source == sortButton) {
                setNodesSorted(true);
            } else if (source == unsortButton) {
                setNodesSorted(false);
            }
        }
    }
    
    
    /**
     */
    private class RootNode extends AbstractNode {
        
        /**
         */
        RootNode() {
            super(Children.LEAF);
        }
        
        /**
         */
        void changeChildren(final Children children) {
            super.setChildren(children);
        }
        
    }
    
}
