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


import java.awt.Component;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.Customizer;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.Mnemonics;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openidex.search.SearchType;


/**
 * Panel which shows all enabled search types for user allowing them to
 * select appropriate criteria for a new search.
 *
 * @author  Peter Zavadsky
 * @author  Marian Petras
 * @see SearchTypePanel
 */
public final class SearchPanel extends JPanel
                               implements PropertyChangeListener,
                                          FocusListener,
                                          ChangeListener,
                                          ActionListener {
    
    /** */
    public static final String PROP_DIALOG_TITLE
                               = "Find Files dialog title";             //NOI18N

    /** Return status code - returned if Cancel button has been pressed. */
    public static final int RET_CANCEL = 0;
    
    /** Return status code - returned if OK button has been pressed. */
    public static final int RET_OK = 1;

    /** Dialog descriptor. */
    private DialogDescriptor dialogDescriptor;

    /** OK button. */
    private final JButton okButton;
    
    /** Cancel button. */
    private final JButton cancelButton;

    /** Java equivalent. */
    private Dialog dialog;

    /** Return status. */
    private int returnStatus = RET_CANCEL;

    /** Ordered list of <code>SearchTypePanel</code>'s. */
    private List orderedSearchTypePanels;

    /** Whether some criterion is customized. */
    private boolean customized;
    
    
    /**
     * Creates a new <code>SearchPanel</code>.
     *
     * @param  searchTypeList  list of <code>SearchType</code>s to use
     */
    public SearchPanel(List searchTypeList) {
        this(searchTypeList, false);
    }
    
    /**
     * Creates a new <code>SearchPanel</code>.
     *
     * @param  searchTypeList  list of <code>SearchType</code>s to use 
     * @param  isCustomized  sets customized flag indicating there is at least
     *                       one from <code>SearchType</code>s already set and
     *                       search - okButton should be enabled
     */
    public SearchPanel(List searchTypeList, boolean isCustomized) {
        this.orderedSearchTypePanels = new ArrayList(searchTypeList.size());
        this.customized = isCustomized;

        // Default values of criteria.
        Iterator it;
        
        /* Create search type panels: */
        Map sortedCriteria;
        {
            SearchCriterion[] allCriteria = SearchProjectSettings.getInstance()
                                            .getSearchCriteria();
            sortedCriteria = Utils.sortCriteriaBySearchType(allCriteria);
        }
        Collection processedClassNames = new ArrayList();
        for (it = searchTypeList.iterator(); it.hasNext(); ) {
            SearchType searchType = (SearchType) it.next();
            String className = searchType.getClass().getName();
            if (processedClassNames.contains(className)) {
                continue;
            }
            processedClassNames.add(className);

            /*
             * isCustomized is <true> if and only if the constructor call
             * was initiated by the Modify Search action. We will leverage this
             * fact for decision whether to pre-fill the search pattern
             * (with the last entry in the history) or not.
             */
            final boolean initFromHistory =
                   !isCustomized
                   && FindDialogMemory.getDefault()
                      .wasSearchTypeUsed(searchType.getClass().getName());
            SearchTypePanel newPanel = new SearchTypePanel(searchType,
                                                           initFromHistory);
            Collection savedCriteria = (sortedCriteria == null)
                    ? null
                    : (Collection) sortedCriteria.get(className);
            
            int index = orderedSearchTypePanels.indexOf(newPanel);
            if (savedCriteria != null) {
                SearchTypePanel targetPanel = (index == -1)
                        ? newPanel
                        : (SearchTypePanel) orderedSearchTypePanels.get(index);
                targetPanel.addSavedCriteria(
                        Collections.unmodifiableCollection(savedCriteria));
            }
            if (index != -1) {
                continue;
            }
            orderedSearchTypePanels.add(newPanel);
            newPanel.addPropertyChangeListener(this);
        }
        
        initComponents();	

        // For each search type create one tab as its search type panel.
        for (it = orderedSearchTypePanels.iterator(); it.hasNext(); ) {
            tabbedPane.add((Component) it.next());
        }
        
        //prevents bug #43843 ("AIOOBE after push button Modify Search")
        tabbedPane.setSelectedIndex(0);

        setName(NbBundle.getBundle(SearchPanel.class)
                .getString("TEXT_TITLE_CUSTOMIZE"));                    //NOI18N

        okButton = new JButton(NbBundle.getBundle(SearchPanel.class)
                               .getString("TEXT_BUTTON_SEARCH"));       //NOI18N
        updateIsCustomized();

        Mnemonics.setLocalizedText(cancelButton = new JButton(),
                                   NbBundle.getBundle(SearchPanel.class)
                                   .getString("TEXT_BUTTON_CANCEL"));   //NOI18N

        Object options[] = new Object[] {okButton, cancelButton};

        initAccessibility();
        
        // Creates representing dialog descriptor.
        dialogDescriptor = new DialogDescriptor(
            this, 
            getName(), 
            true, 
            options, 
            options[0],
            DialogDescriptor.BOTTOM_ALIGN, 
            null,                                   //<null> HelpCtx - no help
            this);
    }
    
    /**
     * This method is called when the Search or Close button is pressed.
     * It closes the Find dialog, cleans up the individual panels
     * and sets the return status.
     *
     * @see  #getReturnStatus
     */
    public void actionPerformed(final ActionEvent evt) {
        doClose(evt.getSource() == okButton ? RET_OK : RET_CANCEL);
    }
    
    void setTitle(String title) {
        dialogDescriptor.setTitle(title);
    }

    private void initAccessibility() {
        this.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(SearchPanel.class).getString("ACS_SearchPanel")); // NOI18N         
        tabbedPane.getAccessibleContext().setAccessibleName(NbBundle.getBundle(SearchPanel.class).getString("ACSN_Tabs")); // NOI18N         
        tabbedPane.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(SearchPanel.class).getString("ACSD_Tabs")); // NOI18N         
        okButton.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(SearchPanel.class).getString("ACS_TEXT_BUTTON_SEARCH")); // NOI18N         
        cancelButton.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(SearchPanel.class).getString("ACS_TEXT_BUTTON_CANCEL")); // NOI18N         
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        tabbedPane = new javax.swing.JTabbedPane();

        setLayout(new java.awt.GridBagLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(tabbedPane, gridBagConstraints);

    }//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTabbedPane tabbedPane;
    // End of variables declaration//GEN-END:variables

    /** @return true if some criterion customized. */
    public boolean isCustomized() {
        return customized;
    }
    
    /**
     * Gets ordered criterion panels.
     *
     * @return iterator over properly ordered <code>SearchTypePanel</code>'s.
     */
    private List getOrderedSearchTypePanels() {
        return new ArrayList(orderedSearchTypePanels);
    }

    /** @return name of criterion at index is modified. */
    private String getTabText(int index) {
        try {
            return ((SearchTypePanel)getOrderedSearchTypePanels().get(index)).getName(); 
        } catch (ArrayIndexOutOfBoundsException ex) {
            return null;
        }
    }

    /**
     * Gets array of customized search types.
     *
     * @return current state of customized search types.
     */
    public SearchType[] getCustomizedSearchTypes() {
        
        List searchTypeList = new ArrayList(orderedSearchTypePanels.size());
        
        for (Iterator it = orderedSearchTypePanels.iterator(); it.hasNext(); ) {
            SearchTypePanel searchTypePanel = (SearchTypePanel) it.next(); 
            if (searchTypePanel.isCustomized()) {
                searchTypeList.add(searchTypePanel.getSearchType());
            }
        }
        
        return (SearchType[]) searchTypeList.toArray(
                new SearchType[searchTypeList.size()]);
    }
    
    /**
     * Getter for return status property.
     *
     * @return the return status of this dialog - one of RET_OK or RET_CANCEL
     */
    public int getReturnStatus () {
        return returnStatus;
    }

    /** Closes dialog. */
    private void doClose(int returnStatus) {

        Iterator it = orderedSearchTypePanels.iterator();
        while (it.hasNext()) {
            SearchTypePanel panel = (SearchTypePanel) it.next();
            panel.removePropertyChangeListener(this);
        }
        
        if (returnStatus == RET_OK) {
            FindDialogMemory.getDefault().clearSearchTypesUsed();
        }
        
        int selectedIndex = tabbedPane.getSelectedIndex();
        if (selectedIndex >= 0) {
            SearchTypePanel panel = getSearchTypePanel(selectedIndex);
            if (returnStatus == RET_OK) panel.onOk();
            else panel.onCancel();
        }
                          
        this.returnStatus = returnStatus;

        dialog.setVisible(false);
        dialog.dispose();
    }

    /** Shows dialog created from <code>DialogDescriptor</code> which wraps this instance. */
    public void showDialog()  {
        dialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);
        dialog.setModal(true);
        tabbedPane.addFocusListener(this);
        
        dialog.pack();
        dialog.setVisible(true);
    }
    
    /**
     * This method is called when the tabbed pane gets focus after the Find
     * dialog is displayed.
     * It lets the first tab to initialize focus.
     */
    public void focusGained(FocusEvent e) {
        tabbedPane.removeFocusListener(this);

        int selectedIndex = tabbedPane.getSelectedIndex();
        if (selectedIndex >= 0) {
            SearchTypePanel panel = getSearchTypePanel(selectedIndex);
            if ((panel != null) && (panel.customizerComponent != null)) {
                panel.customizerComponent.requestFocus();
            }
        }

        tabbedPane.addChangeListener(this);
    }
    
    /**
     * This method is called when the tabbed pane looses focus.
     * It does nothing and it is here just because this class declares that
     * it implements the <code>FocusListener</code> interface.
     *
     * @see  #focusGained(FocusEvent)
     */
    public void focusLost(FocusEvent e) {
        //does nothing
    }
            
    /**
     * This method is called when tab selection changes.
     * It initializes the customizer below the selected tab.
     */
    public void stateChanged(ChangeEvent e) {
        int selectedIndex = tabbedPane.getSelectedIndex();
        if (selectedIndex >= 0) {
            SearchTypePanel panel = getSearchTypePanel(selectedIndex);
            if (panel != null) {
                panel.initializeWithObject();
            }
        }
    }

    /** Implements <code>PropertyChangeListener</code> interface. */
    public void propertyChange(PropertyChangeEvent event) {
        if(SearchTypePanel.PROP_CUSTOMIZED.equals(event.getPropertyName())) {
            updateIsCustomized();
        }

        for(int i = 0; i < tabbedPane.getTabCount(); i++) {
            tabbedPane.setTitleAt(i, getTabText(i));
            tabbedPane.setIconAt(i, null);
        }
    }
    /**
     */
    private void updateIsCustomized() {
        customized = getCustomizedSearchTypes().length != 0;            

        okButton.setEnabled(isCustomized());
    }

   
    /**
     * Gets a <code>SearchTypePanel</code> for the given tab index.
     *
     * @param  index  index of the tab to get the panel from
     * @return  <code>SearchTypePanel</code> at the given tab;
     *          or <code>null</code> if there is none at the given tab index
     */
    private SearchTypePanel getSearchTypePanel(int index) {
        SearchTypePanel searchTypePanel = null; 
        
        Iterator it = getOrderedSearchTypePanels().iterator();
        while(index >= 0 && it.hasNext()) {
            searchTypePanel = (SearchTypePanel)it.next();
            index--;
        }
        
        return searchTypePanel;
    }
    
}
