/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.search;


import java.awt.BorderLayout;
import java.awt.ComponentOrientation;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.awt.Mnemonics;
import org.openide.awt.SplittedPanel;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerPanel;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openidex.search.SearchGroup;
import org.openidex.search.SearchType;


/**
 * Panel which displays search results in explorer like manner.
 * Use method {@link #createDialogDescriptor()} to access this panel.
 *
 * @see  #createDialogDescriptor
 * @author Petr Kuzel, Jiri Mzourek, Peter Zavadsky
 * @author  Marian Petras
 */
final class ResultView extends JPanel
                       implements ChangeListener, ExplorerManager.Provider {
    
    /** tree of found <code>DataObject</code>s */
    private final ExplorerPanel explorerPanel;
    /**
     * panel for displaying details about an object currently selected
     * in the tree of found objects
     */
    private final DetailsPanel detailsPanel;
    /** button <em>Stop Search</em> */
    private final JButton stopButton;
    /** button <em>Show All Details</em> */
    private final JButton outputButton;
    /** button <em>Modify Search</em> */
    private final JButton customizeButton;
    /** button <em>Close</em> */
    private final JButton closeButton;
    
    /** Result data model. */
    private ResultModel resultModel = null;
    /**
     * whether to lay UI components from left to right (<code>true</code>)
     * or right to left (<code>false</code>)
     */
    private boolean orientationLeftToRigth = true;

    
    /** Creates a new <code>ResultView</code>. */
    ResultView() {
        setupOrientation();
        initComponents();
        
        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(sortButton);
        buttonGroup.add(unsortButton);
        
        stopButton = new JButton();
        Mnemonics.setLocalizedText(
                stopButton,
                NbBundle.getMessage(ResultView.class,
                                    "TEXT_BUTTON_STOP"));               //NOI18N
        outputButton = new JButton();
        Mnemonics.setLocalizedText(
                outputButton,
                NbBundle.getMessage(ResultView.class,
                                    "TEXT_BUTTON_FILL"));               //NOI18N
        customizeButton = new JButton();
        Mnemonics.setLocalizedText(
                customizeButton,
                NbBundle.getMessage(ResultView.class,
                                    "TEXT_BUTTON_CUSTOMIZE"));          //NOI18N
        closeButton = new JButton();
        Mnemonics.setLocalizedText(
                closeButton,
                NbBundle.getMessage(ResultView.class,
                                    "TEXT_BUTTON_CANCEL"));             //NOI18N
        
        explorerPanel = new ExplorerPanel();
        explorerPanel.setLayout(new BorderLayout());
        explorerPanel.getExplorerManager().addPropertyChangeListener(
                new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent evt) {
                        if (ExplorerManager.PROP_SELECTED_NODES.equals(
                               evt.getPropertyName())) {
                            nodeSelectionChanged();
                        }
                    }
                });

        detailsPanel = new DetailsPanel();
        
        BeanTreeView treeView =  new BeanTreeView();
        treeView.getAccessibleContext().setAccessibleDescription(
                NbBundle.getMessage(ResultView.class,"ACS_TREEVIEW"));  //NOI18N
        
        treeView.setBorder(new EtchedBorder());
        explorerPanel.add(treeView, BorderLayout.CENTER);

        resultLabel.setLabelFor(treeView);
        
        splitPane.setLeftComponent(explorerPanel);
        splitPane.setRightComponent(detailsPanel);
        
        initAccessibility();
    }
    
    /**
     */
    private void setupOrientation() {
        Locale locale = Locale.getDefault();
        ComponentOrientation orientation
                = ComponentOrientation.getOrientation(locale);
        
        /* ResultView does not handle vertical orientation: */
        orientationLeftToRigth = orientation.isLeftToRight()
                                 || !orientation.isHorizontal();
    }

    private void initAccessibility() {
        ResourceBundle bundle = NbBundle.getBundle(ResultView.class);
        getAccessibleContext ().setAccessibleName (bundle.getString ("ACSN_ResultViewTopComponent"));                   //NOI18N
        getAccessibleContext ().setAccessibleDescription (bundle.getString ("ACSD_ResultViewTopComponent"));            //NOI18N

        sortButton.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_TEXT_BUTTON_SORT"));           //NOI18N
        stopButton.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_TEXT_BUTTON_STOP"));           //NOI18N
        closeButton.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_TEXT_BUTTON_CANCEL"));        //NOI18N
        customizeButton.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_TEXT_BUTTON_CUSTOMIZE")); //NOI18N
        outputButton.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_TEXT_BUTTON_FILL"));         //NOI18N
        unsortButton.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_TEXT_BUTTON_UNSORT"));       //NOI18N
    }       
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        javax.swing.JPanel sortingPanel;

        sortingPanel = new javax.swing.JPanel();

        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.Y_AXIS));

        setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(12, 12, 0, 11)));
        Mnemonics.setLocalizedText(resultLabel, NbBundle.getMessage(ResultView.class, "TEXT_LABEL_SEARCH_RESULTS"));   //NOI18N
        resultLabel.setAlignmentX(orientationLeftToRigth ? 0.0f : 1.0f);
        resultLabel.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
        add(resultLabel);

        splitPane.setAlignmentX(orientationLeftToRigth ? 0.0f : 1.0f);
        add(splitPane);

        sortingPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 11, 5));

        add(Box.createVerticalStrut(11));
        sortingPanel.setAlignmentX(orientationLeftToRigth ? 0.0f : 1.0f);
        Mnemonics.setLocalizedText(sortButton, NbBundle.getMessage(ResultView.class, "TEXT_BUTTON_SORT"));     //NOI18N
        sortButton.setEnabled(false);
        sortButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sortButtonActionPerformed(evt);
            }
        });

        sortingPanel.add(sortButton);

        unsortButton.setSelected(true);
        Mnemonics.setLocalizedText(unsortButton, NbBundle.getMessage(ResultView.class, "TEXT_BUTTON_UNSORT"));     //NOI18N
        unsortButton.setEnabled(false);
        unsortButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sortButtonActionPerformed(evt);
            }
        });

        sortingPanel.add(unsortButton);

        add(sortingPanel);

    }//GEN-END:initComponents

    /**
     * Creates a dialog containing this result view.
     *
     * @return  created dialog
     */
    Dialog createDialog() {
        DialogDescriptor descriptor = new DialogDescriptor(
                this,                               //inner pane
                NbBundle.getMessage(ResultView.class,
                                    "TITLE_SEARCH_RESULTS"),            //NOI18N
                false,                              //modal?
                new Object[] {stopButton,
                              outputButton,
                              customizeButton,
                              closeButton},
                closeButton,                        //default button
                DialogDescriptor.DEFAULT_ALIGN,
                new HelpCtx(ResultView.class),
                new ButtonListener());
        descriptor.setClosingOptions(new Object[] {closeButton});
        
        Dialog dialog = DialogDisplayer.getDefault().createDialog(descriptor);
        dialog.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                stopSearching();
                if (resultModel != null) {
                    resultModel.removeChangeListener(ResultView.this);
                }
            }
        });
        
        return dialog;
    }

            
    private void sortButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sortButtonActionPerformed
        ExplorerManager explorerManager = null;
        Node[] selectedNodes = null;
        if ((explorerManager = explorerPanel.getExplorerManager()) != null) {
            selectedNodes = explorerManager.getSelectedNodes();
        }
        
        boolean sort = sortButton.isSelected();
        sortNodes(sort);

        if(selectedNodes!=null) {        
            try {
                explorerManager.setSelectedNodes(selectedNodes);
            } catch(PropertyVetoException pve) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                                                 pve);
                // OK it was vetoed.
            }
        }
    }//GEN-LAST:event_sortButtonActionPerformed
            
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private final javax.swing.JLabel resultLabel = new javax.swing.JLabel();
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

        if (resultModel.isSorted()) {
            sortButton.setSelected(true);
        } else {
            unsortButton.setSelected(true);
        }
        explorerPanel.getExplorerManager().setRootContext(root);
        resultModel.addChangeListener(this);
        initButtons();
    }
    
    /** Set visibility of buttons & others... */
    private void initButtons() {
        stopButton.setEnabled(!resultModel.isDone());
        sortButton.setEnabled(resultModel.isDone());
        unsortButton.setEnabled(resultModel.isDone());
        
        detailsPanel.showInfo(null);
    }
    
    /** Sorts nodes. */
    private void sortNodes(boolean sort) {
        Node root = resultModel.sortNodes(sort);
        explorerPanel.getExplorerManager().setRootContext(root);
        initButtons();
    }
    
    
    /* Implements interface ExplorerManager.Provider */
    public ExplorerManager getExplorerManager() {
        return explorerPanel.getExplorerManager();
    }
    
    /**
     * Displays information about the node currently selected in the tree
     * of matching objects. If there is no selected node or if there are
     * multiple nodes selected, clears the panel for displaying details.
     */
    private void nodeSelectionChanged() {
        Node[] nodes = explorerPanel.getExplorerManager()
                                    .getSelectedNodes();
        if (nodes != null && nodes.length == 1) {
            Node selectedNode = nodes[0];
            detailsPanel.showInfo(selectedNode);
            showDetails(selectedNode);
        } else {
            detailsPanel.showInfo(null);
            showDetails(null);
        }
    }
    
    /** (Re)open the dialog window for entering (new) search criteria. */
    private void customizeCriteria() {
        SearchPanel searchPanel = new SearchPanel(resultModel.getEnabledSearchTypes(), true);
        
        Node[] oldRoots = resultModel.getSearchGroup().getSearchRoots();
        
        searchPanel.showDialog();
        
        if (searchPanel.getReturnStatus() == SearchPanel.RET_OK) {
            //stop previous search
            resultModel.stop();
            
            // Start a new search.
            SearchEngine searchEngine = new SearchEngine();
            
            SearchGroup[] groups = SearchGroup.createSearchGroups(searchPanel.getCustomizedSearchTypes());

            SearchGroup searchGroup = null;

            if (groups.length > 0) {
                // PENDING Here should be solved cases when more groups were created,
                // if not only intersection result is necessary etc.
                searchGroup = groups[0];
            }
            
            ResultModel newResultModel = new ResultModel(resultModel.getEnabledSearchTypes(), searchGroup);

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
        Children children;
        
        if (node == null) {
            children = Children.LEAF;
        } else {
        
            children = new Children.Array();

            ArrayList listData = new ArrayList(20);

            SearchType[] searchTypes = resultModel.getSearchGroup()
                                                  .getSearchTypes();
            
            for (int i = 0; i < searchTypes.length; i++) {
                Node[] detailNodes = searchTypes[i].getDetails(node);

                if (detailNodes != null) {
                    children.add(detailNodes);
                }
            }
        }
        
        detailsPanel.setDetailsRoot(new AbstractNode(children));
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
            if (source == stopButton) {
                stopSearching();
            } else if (source == outputButton) {
                resultModel.fillOutput();
            } else if (source == customizeButton) {
                customizeCriteria();
            }
        }
    }
    
}
