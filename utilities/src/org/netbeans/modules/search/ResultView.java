/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.search;


import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.ErrorManager;
import org.openide.awt.Mnemonics;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.loaders.DataFilter;
import org.openide.loaders.RepositoryNodeFactory;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.openidex.search.SearchGroup;
import org.openidex.search.SearchType;


/**
 * Panel which displays search results in explorer like manner.
 * This panel is a singleton.
 *
 * @see  #getInstance
 * @author Petr Kuzel, Jiri Mzourek, Peter Zavadsky
 * @author  Marian Petras
 */
final class ResultView extends TopComponent
                       implements ChangeListener, ExplorerManager.Provider {
                           
    /** unique ID of <code>TopComponent</code> (singleton) */
    private static final String ID = "search-results";                  //NOI18N
    
    /**
     * instance/singleton of this class
     *
     * @see  #getInstance
     */
    private static ResultView instance = null;
    
    /** manages the tree of nodes representing found objects */
    private final ExplorerManager explorerManager;
    /**
     * panel for displaying details about an object currently selected
     * in the tree of found objects
     */
    private final DetailsPanel detailsPanel;

    /**
     * tree view for displaying found objects
     */
    private final BeanTreeView treeView;
    
    /** Result data model. */
    private ResultModel resultModel = null;


    /**
     * Returns a singleton of this class.
     *
     * @return  singleton of this <code>TopComponent</code>
     */
    static synchronized ResultView getInstance() {
        if (instance == null) {
            instance = (ResultView)
                       WindowManager.getDefault().findTopComponent(ID);
        }
        return instance;
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
        if (instance == null) {
            instance = new ResultView();
        }
        return instance;
    }
    
    /** Creates a new <code>ResultView</code>. */
    private ResultView() {
        initComponents();
        setName("Search Results");                                      //NOI18N
        setDisplayName(NbBundle.getMessage(ResultView.class,
                                           "TITLE_SEARCH_RESULTS"));    //NOI18N
        setIcon(Utilities.loadImage(
                "org/netbeans/modules/search/res/find.gif"));           //NOI18N
        
        buttonsPanel.add(Box.createHorizontalGlue(), 2);
        buttonsPanel.add(Box.createHorizontalStrut(5), 4);
        buttonsPanel.add(Box.createHorizontalStrut(5), 6);
        
        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(sortButton);
        buttonGroup.add(unsortButton);
        
        Node root = createTreeViewRoot();
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

        /* Create the left part of the window: */
        treeView =  new BeanTreeView();
        treeView.getAccessibleContext().setAccessibleDescription(
                NbBundle.getMessage(ResultView.class, "ACS_TREEVIEW")); //NOI18N
        treeView.setBorder(Utils.getExplorerViewBorder());

        /* Create the right part of the window: */
        detailsPanel = new DetailsPanel();
        detailsPanel.setBorder(BorderFactory.createEmptyBorder(6, 0, 0, 0));
        
        /* Put both parts into a split pane: */
        splitPane.setLeftComponent(treeView);
        splitPane.setRightComponent(detailsPanel);
        
        /* Modify UI of the split pane: */
        /* 1) remove the border around the whole split pane: */
        splitPane.setBorder(BorderFactory.createEmptyBorder());
        /* 2) remove decoration of the splitter: */
        javax.swing.plaf.basic.BasicSplitPaneDivider divider = null;
        java.awt.Component[] components = splitPane.getComponents();
        for (int i = 0; i < components.length; i++) {
            if (components[i] instanceof
                    javax.swing.plaf.basic.BasicSplitPaneDivider) {
                divider = (javax.swing.plaf.basic.BasicSplitPaneDivider)
                          components[i];
                break;
            }
        }
        if (divider != null) {
            divider.setBorder(BorderFactory.createEmptyBorder());
        }
        
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
    private final Node createTreeViewRoot() {
        AbstractNode node = new AbstractNode(Children.LEAF);
        node.setName(NbBundle.getMessage(ResultView.class,
                                         "TEXT_Search_in_filesystems"));//NOI18N
        node.setIconBase("org/netbeans/modules/search/res/find");       //NOI18N
        return node;
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
     * to PERSISTENCE_ALWAYS */
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ALWAYS;
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

        sortButton.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_TEXT_BUTTON_SORT"));           //NOI18N
        unsortButton.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_TEXT_BUTTON_UNSORT"));       //NOI18N
        btnModifySearch.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_TEXT_BUTTON_CUSTOMIZE")); //NOI18N
        btnShowDetails.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_TEXT_BUTTON_FILL"));         //NOI18N
        btnStop.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_TEXT_BUTTON_STOP"));           //NOI18N
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

        setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(0, 0, 5, 5)));
        mainPanel.setLayout(new java.awt.BorderLayout());

        mainPanel.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(0, 0, 5, 5)));
        splitPane.setDividerSize(5);
        mainPanel.add(splitPane, java.awt.BorderLayout.CENTER);

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

    /* overridden */
    protected void componentClosed() {
        stopSearching();
        if (resultModel != null) {
            resultModel.removeChangeListener(ResultView.this);
        }
    }
            
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private final javax.swing.JButton btnModifySearch = new javax.swing.JButton();
    private final javax.swing.JButton btnShowDetails = new javax.swing.JButton();
    private final javax.swing.JButton btnStop = new javax.swing.JButton();
    private javax.swing.JPanel buttonsPanel;
    private javax.swing.JPanel mainPanel;
    private final javax.swing.JRadioButton sortButton = new javax.swing.JRadioButton();
    private final javax.swing.JSplitPane splitPane = new javax.swing.JSplitPane();
    private final javax.swing.JRadioButton unsortButton = new javax.swing.JRadioButton();
    // End of variables declaration//GEN-END:variables
    

    /** Set new model. */
    void setModel(ResultModel resultModel) {
        if (this.resultModel != null) {
            this.resultModel.removeChangeListener(this);
        }
        
        this.resultModel = resultModel;
        
        Node root = resultModel.getRoot();
        explorerManager.setRootContext(root);
        selectAndActivateNode(root);
        
        resultModel.addChangeListener(this);
        
        initButtons();
        btnShowDetails.setEnabled(true);
    }
    
    /** Set visibility of buttons &amp; others... */
    private void initButtons() {
        if (resultModel.isSorted()) {
            sortButton.setSelected(true);
        } else {
            unsortButton.setSelected(true);
        }
        
        btnStop.setEnabled(!resultModel.isDone());
        sortButton.setEnabled(resultModel.isDone());
        unsortButton.setEnabled(resultModel.isDone());
        
        showDetails(null);
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
        if (nodes.length == 1 && resultModel != null) {
            showDetails(nodes[0]);
        } else {
            showDetails(null);
        }
    }
    
    /** (Re)open the dialog window for entering (new) search criteria. */
    private void customizeCriteria() {
        Node[] oldRoots;
        List searchTypes;
        if (resultModel != null) {
            oldRoots = resultModel.getSearchGroup().getSearchRoots();
            searchTypes = resultModel.getEnabledSearchTypes();
        } else {
            Node repositoryNode = RepositoryNodeFactory.getDefault()
                                  .repository(DataFilter.ALL);
            oldRoots = new Node[] {repositoryNode};
            List types = SearchPerformer.getTypes(oldRoots);

            /* Clone the list (deep copy): */
            searchTypes = new ArrayList(types.size());
            for (Iterator it = types.iterator(); it.hasNext(); ) {
                searchTypes.add(((SearchType) it.next()).clone());
            }
        }
        
        SearchPanel searchPanel = new SearchPanel(searchTypes, true);
        searchPanel.showDialog();
        
        if (searchPanel.getReturnStatus() == SearchPanel.RET_OK) {
            //stop previous search
            if (resultModel != null) {
                resultModel.stop();
            }
            
            // Start a new search.
            SearchEngine searchEngine = new SearchEngine();
            
            SearchGroup[] groups = SearchGroup.createSearchGroups(searchPanel.getCustomizedSearchTypes());

            SearchGroup searchGroup = null;

            if (groups.length > 0) {
                // PENDING Here should be solved cases when more groups were created,
                // if not only intersection result is necessary etc.
                searchGroup = groups[0];
            }
            
            ResultModel newResultModel = new ResultModel(searchTypes, searchGroup);

            SearchTask task = searchEngine.search(
                //criteriaModel.getNodes(),
                oldRoots,
                searchGroup,
                newResultModel);
            
            newResultModel.setTask(task);
            
            setModel(newResultModel);
            initButtons();
        }
    }
    
    /**
     */
    private void stopSearching() {
        if (resultModel != null) {
            resultModel.stop();
        }
    }
    
    /**
     * Displays a list of matches found within a selected object.
     * The list of matches is collected from answers of all
     * {@linkplain SearchType search types}.
     *
     * @param  node  node representing an object containing matches
     *               (if <code>null</code>, displays an empty list)
     * @see  SearchType#getDetails(Node)
     */
    private void showDetails(Node node) {
        if (node == null) {
            detailsPanel.showInfo(null, null);
        } else {
            SearchType[] searchTypes = resultModel.getSearchGroup()
                                                  .getSearchTypes();
            List allDetailNodes = new ArrayList();
            for (int i = 0; i < searchTypes.length; i++) {
                Node[] detailNodes = searchTypes[i].getDetails(node);
                if (detailNodes != null && detailNodes.length != 0) {
                    allDetailNodes.addAll(Arrays.asList(detailNodes));
                }
            }
            detailsPanel.showInfo(node, allDetailNodes);
        }
    }
    
    /**
     * Sorts or unsorts the list nodes representing found objects.
     *
     * @param  sorted  <code>true</code> to sort the nodes,
     *                 <code>false</code> to unsort the nodes
     */
    private void setNodesSorted(boolean sorted) {
        Node[] selectedNodes = explorerManager.getSelectedNodes();
        Node root = resultModel.sortNodes(sorted);
        explorerManager.setRootContext(root);
        initButtons();
        try {
            explorerManager.setSelectedNodes(selectedNodes);
            setActivatedNodes(selectedNodes);
        } catch(PropertyVetoException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            selectAndActivateNode(root);
        }
    }
    
    /** Respond to result model state change. */
    public void stateChanged(ChangeEvent evt) {
        if (evt.getSource() == resultModel) {
            if (resultModel.isDone()) {
                initButtons();
            }
        }
    }

    /**
     */
    private class ButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            Object source = e.getSource();
            if (source == btnStop) {
                stopSearching();
            } else if (source == btnModifySearch) {
                customizeCriteria();
            } else if (source == btnShowDetails) {
                resultModel.fillOutput();
            } else if (source == sortButton) {
                setNodesSorted(true);
            } else if (source == unsortButton) {
                setNodesSorted(false);
            }
        }
    }
    
}
