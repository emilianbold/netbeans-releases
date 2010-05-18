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


import java.awt.Component;
import java.awt.Dialog;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
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
    
    /** Return status code - returned if Cancel button has been pressed. */
    public static final int RET_CANCEL = 0;
    
    /** Return status code - returned if OK button has been pressed. */
    public static final int RET_OK = 1;
    
    /** */
    private final BasicSearchForm basicCriteriaPanel;

    /** */
    private final boolean projectWide;
    /** */
    private final boolean searchAndReplace;

    /** OK button. */
    private final JButton okButton;
    
    /** Cancel button. */
    private final JButton cancelButton;

    /** Java equivalent. */
    private Dialog dialog;

    /** Return status. */
    private int returnStatus = RET_CANCEL;

    /** Ordered list of <code>SearchTypePanel</code>'s. */
    private List<SearchTypePanel> orderedSearchTypePanels;
    
    
    /**
     * Creates a new {@code SearchPanel}.
     *
     * @param  basicSearchCriteria  basic search criteria,
     *                              or {@code null} if basic search criteria
     *                              should not be used
     * @param  preferredSearchScope  preferred search scope (may be {@code null})
     */
    SearchPanel(Map<SearchScope, Boolean> searchScopes,
                String preferredSearchScopeType,
                boolean searchAndReplace) {
        this(searchScopes,
             preferredSearchScopeType,
             null,
             Utils.cloneSearchTypes(Utils.getSearchTypes()),
             false,
             searchAndReplace);
    }

    /**
     * Creates a new {@code SearchPanel}.
     *
     * @param  basicSearchCriteria  basic search criteria,
     *                              or {@code null} if basic search criteria
     *                              should not be used
     * @param  extraSearchTypes  list of extra {@code SearchType}s to use
     */
    SearchPanel(Map<SearchScope, Boolean> searchScopes,
                String preferredSearchScopeType,
                BasicSearchCriteria basicSearchCriteria,
                Collection<? extends SearchType> extraSearchTypes) {
        this(searchScopes,
	     preferredSearchScopeType,
	     basicSearchCriteria,
	     extraSearchTypes,
	     true,
             basicSearchCriteria.isSearchAndReplace());
    }
    
    private SearchPanel(Map<SearchScope, Boolean> searchScopes,
                String preferredSearchScopeType,           //may be null
                BasicSearchCriteria basicSearchCriteria,    //may be null
		Collection<? extends SearchType> extraSearchTypes,
                boolean activateWithPreviousValues,
                boolean searchAndReplace) {
        assert (extraSearchTypes != null);
        if (extraSearchTypes == null) {
            extraSearchTypes = Collections.<SearchType>emptyList();
        }

        projectWide = SearchScopeRegistry.hasProjectSearchScopes(
                                                        searchScopes.keySet());
        this.searchAndReplace = searchAndReplace;
        
        /* Create panel for entering basic search criteria: */
        basicCriteriaPanel = new BasicSearchForm(searchScopes,
                                                 preferredSearchScopeType,
                                                 basicSearchCriteria,
                                                 searchAndReplace,
                                                 activateWithPreviousValues);
        basicCriteriaPanel.setUsabilityChangeListener(this);
        
        /* Create search type panels: */
        setLayout(new GridLayout(1, 1));
        if (!extraSearchTypes.isEmpty()) {
            
            orderedSearchTypePanels
                    = new ArrayList<SearchTypePanel>(extraSearchTypes.size());
            tabbedPane = new JTabbedPane();
            tabbedPane.add(basicCriteriaPanel);
            
            Set<String> processedClassNames = new HashSet<String>();
            for (SearchType searchType : extraSearchTypes) {
                String className = searchType.getClass().getName();
                if (!processedClassNames.add(className)) {
                    continue;
                }

                SearchTypePanel newPanel = new SearchTypePanel(searchType);
                int index = orderedSearchTypePanels.indexOf(newPanel);
                if (index != -1) {
                    continue;
                }
                
                orderedSearchTypePanels.add(newPanel);
                newPanel.addPropertyChangeListener(this);
                
                tabbedPane.add(newPanel);
            }
            
            add(tabbedPane);
            
            // initial selection
            int tabIndex = 0;              //prevents bug #43843 ("AIOOBE after push button Modify Search")       
            /*
             * we will use activateWithPreviousValues for the decision 
             * whether to pre-select the last selected tab 
             * (with the last used SearchType) or not.
             */        
            if (activateWithPreviousValues) {
                int searchTypeIndex = getIndexOfSearchType(
                            FindDialogMemory.getDefault().getLastSearchType());
                tabIndex = searchTypeIndex + 1;
                /* if searchTypeIndex is -1, then tabIndex is 0 */
            }
            tabbedPane.setSelectedIndex(tabIndex);
            updateFirstTabText();
            updateExtraTabsTexts();
        } else {
            orderedSearchTypePanels = null;
            tabbedPane = null;
            
            add(basicCriteriaPanel);
        }
        
        setName(NbBundle.getMessage(SearchPanel.class,
                                    "TEXT_TITLE_CUSTOMIZE"));           //NOI18N

        Mnemonics.setLocalizedText(okButton = new JButton(),
                                   NbBundle.getMessage(
                                            SearchPanel.class,
                                            "TEXT_BUTTON_SEARCH"));     //NOI18N
        updateIsCustomized();

        Mnemonics.setLocalizedText(cancelButton = new JButton(),
                                   NbBundle.getMessage(
                                            SearchPanel.class,
                                            "TEXT_BUTTON_CANCEL"));     //NOI18N

        initAccessibility();
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
    
    SearchScope getSearchScope() {
        return basicCriteriaPanel.getSelectedSearchScope();
    }
    
    /**
     * Returns basic criteria entered in the Find dialog.
     * 
     * @return  basic criteria specified in the Find dialog, or {@code null}
     *          if no basic criteria are specified or if the criteria
     *          are not valid (e.g. if an invalid search pattern is specified)
     */
    BasicSearchCriteria getBasicSearchCriteria() {
        BasicSearchCriteria basicCriteria
                = basicCriteriaPanel.getBasicSearchCriteria();
        return basicCriteria.isUsable() ? basicCriteria : null;
    }
    
    /**
     */
    List<SearchType> getSearchTypes() {
        List<SearchType> result;
        if (orderedSearchTypePanels == null) {
            result = Collections.<SearchType>emptyList();
        } else {
            result = new ArrayList<SearchType>(orderedSearchTypePanels.size());
            for (SearchTypePanel searchTypePanel : orderedSearchTypePanels) {
                result.add(searchTypePanel.getSearchType());
            }
        }
        return result;
    }

    private void initAccessibility() {
        this.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(SearchPanel.class).getString("ACS_SearchPanel")); // NOI18N         
        if (tabbedPane != null) {
            tabbedPane.getAccessibleContext().setAccessibleName(NbBundle.getBundle(SearchPanel.class).getString("ACSN_Tabs")); // NOI18N         
            tabbedPane.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(SearchPanel.class).getString("ACSD_Tabs")); // NOI18N         
        }
        okButton.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(SearchPanel.class).getString("ACS_TEXT_BUTTON_SEARCH")); // NOI18N         
        cancelButton.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(SearchPanel.class).getString("ACS_TEXT_BUTTON_CANCEL")); // NOI18N         
    }
    
    private JTabbedPane tabbedPane;

    /** @return name of criterion at index is modified. */
    private String getTabText(int index) {
        String text;
        if (index == 0) {
            text = NbBundle.getMessage(getClass(),
                                       "BasicSearchForm.tabText");      //NOI18N
            if (basicCriteriaPanel.getBasicSearchCriteria().isUsable()) {
                text = text + " *";                                     //NOI18N
            }
        } else {
            text = orderedSearchTypePanels.get(index - 1).getName(); 
        }
        return text;
    }

    /**
     * Gets array of customized search types.
     *
     * @return current state of customized search types.
     */
    List<SearchType> getCustomizedSearchTypes() {
        if (orderedSearchTypePanels == null) {
            return Collections.<SearchType>emptyList();
        }
        
        List<SearchType> searchTypeList
                = new ArrayList<SearchType>(orderedSearchTypePanels.size());
        for (SearchTypePanel searchTypePanel : orderedSearchTypePanels) {
            if (searchTypePanel.isCustomized()) {
                searchTypeList.add(searchTypePanel.getSearchType());
            }
        }
        return searchTypeList;
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

        if (orderedSearchTypePanels != null) {
            for (SearchTypePanel panel : orderedSearchTypePanels) {
                panel.removePropertyChangeListener(this);
            }
        }
        
        int selectedIndex = (tabbedPane == null) ? 0
                                                 : tabbedPane.getSelectedIndex();
        if (selectedIndex == 0) {
            if (returnStatus == RET_OK) {
                FindDialogMemory.getDefault().setLastUsedSearchType(null);
                basicCriteriaPanel.onOk();
            }
        } else if (selectedIndex > 0) {
            SearchTypePanel panel = getSearchTypePanel(selectedIndex);
            if (returnStatus == RET_OK){
                FindDialogMemory.getDefault().setLastUsedSearchType(panel.getSearchType());
                panel.onOk();                
            } else {
                panel.onCancel();
            }
        }
                          
        this.returnStatus = returnStatus;

        dialog.setVisible(false);
        dialog.dispose();
    }

    /**
     * Shows dialog created from {@code DialogDescriptor} which wraps this instance.
     */
    void showDialog()  {
	String titleMsgKey = projectWide
                             ? (searchAndReplace
                                          ? "LBL_ReplaceInProjects"     //NOI18N
                                          : "LBL_FindInProjects")       //NOI18N
                             : (searchAndReplace
                                          ? "LBL_ReplaceInFiles"        //NOI18N
                                          : "LBL_FindInFiles");         //NOI18N

        DialogDescriptor dialogDescriptor = new DialogDescriptor(
            this, 
            NbBundle.getMessage(getClass(), titleMsgKey),
            true, 
            new Object[] {okButton, cancelButton}, 
            okButton,
            DialogDescriptor.BOTTOM_ALIGN, 
            new HelpCtx(getClass()),
            this);
        dialogDescriptor.setTitle(NbBundle.getMessage(getClass(), titleMsgKey));

        dialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);
        dialog.setModal(true);
        if (tabbedPane != null) {
            tabbedPane.addFocusListener(this);
        }
        
        dialog.pack();
        dialog.setVisible(true);
    }

    /**
     * This method is called when the tabbed pane gets focus after the Find
     * dialog is displayed.
     * It lets the first tab to initialize focus.
     */
    public void focusGained(FocusEvent e) {
        assert tabbedPane != null;
        
        tabbedPane.removeFocusListener(this);

        Component defaultComp = null;
        int selectedIndex = tabbedPane.getSelectedIndex();
        if (selectedIndex == 0) {
            defaultComp = basicCriteriaPanel;
        } else if (selectedIndex > 0) {
            SearchTypePanel panel = getSearchTypePanel(selectedIndex);
            if (panel != null) {
                defaultComp = panel.customizerComponent;       //may be null
            }
        }
        if (defaultComp != null) {
            defaultComp.requestFocusInWindow();
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
     * This method is called when tab selection changes and when values entered
     * in the form for basic criteria become valid or invalid.
     * Depending on the trigger, it either updates state of the <em>Find</em>
     * button (enabled/disabled)
     * or initializes the customizer below the selected tab.
     * 
     * @see  #updateIsCustomized()
     * @see  #tabSelectionChanged()
     */
    public void stateChanged(ChangeEvent e) {
        if (e.getSource() == basicCriteriaPanel) {
            updateIsCustomized();
            if (tabbedPane != null) {
                updateFirstTabText();
            }
        } else {
            tabSelectionChanged();
        }
    }

    /**
     * Called when a different tab is selected or when a tab becomes customized
     * or uncustomized.
     */
    public void propertyChange(PropertyChangeEvent event) {
        if(SearchTypePanel.PROP_CUSTOMIZED.equals(event.getPropertyName())) {
            updateIsCustomized();
            if (tabbedPane != null) {
                updateExtraTabsTexts();
            }
        }
    }

    /**
     * Updates label of the first tab.
     * 
     * @see  #updateExtraTabsTexts
     */
    private void updateFirstTabText() {
        assert tabbedPane != null;
        tabbedPane.setTitleAt(0, getTabText(0));
    }

    /**
     * Updates labels of all tabs except the first one.
     * 
     * @see  #updateFirstTabText
     */
    private void updateExtraTabsTexts() {
        assert tabbedPane != null;
        int tabCount = tabbedPane.getTabCount();
        for (int i = 1; i < tabCount; i++) {
            tabbedPane.setTitleAt(i, getTabText(i));
        }
    }
    
    /**
     * Updates state of the <em>Find</em> dialog (enabled/disabled),
     * depending on values entered in the form.
     */
    private void updateIsCustomized() {
        okButton.setEnabled(checkIsCustomized());
    }
    
    /**
     * Checks whether valid criteria are entered in at least one criteria panel.
     * 
     * @return  {@code true} if some applicable criteria are entered,
     *          {@code false} otherwise
     */
    private boolean checkIsCustomized() {
        if (basicCriteriaPanel.isUsable()) {
            return true;
        }
        
        if ((orderedSearchTypePanels != null)
                && !orderedSearchTypePanels.isEmpty()) {
            for (SearchTypePanel searchTypePanel : orderedSearchTypePanels) {
                if (searchTypePanel.isCustomized()) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    private void tabSelectionChanged() {
        assert tabbedPane != null;

        int selectedIndex = tabbedPane.getSelectedIndex();
        if (selectedIndex == 0) {
            //basicCriteriaPanel.setCriteria(/*PENDING*/);
        } else if (selectedIndex > 0) {
            SearchTypePanel panel = getSearchTypePanel(selectedIndex);
            if (panel != null) {
                panel.initializeWithObject();
            }
        }
    }
   
    /**
     * Gets a <code>SearchTypePanel</code> for the given tab index.
     *
     * @param  index  index of the tab to get the panel from
     * @return  <code>SearchTypePanel</code> at the given tab;
     *          or <code>null</code> if there is none at the given tab index
     */
    private SearchTypePanel getSearchTypePanel(int index) {
        assert orderedSearchTypePanels != null;
        assert index >= 1;

        return (--index < orderedSearchTypePanels.size())
               ? orderedSearchTypePanels.get(index)
               : null;
    }
    
    /**
     * Gets the index for the the given {@code SearchType}
     *
     * @param searchTypeToFind {@code SearchType} to get the index for.        
     * @return  index of the given {@code SearchType}, or {@code -1}
     *          if the given search type is {@code null} or if it is not present
     *          in the list of used search types
     */
    private int getIndexOfSearchType(SearchType searchTypeToFind) {                        
        
        if(searchTypeToFind==null){
            return -1;
        }
        
        int index = -1;
        for (SearchTypePanel searchTypePanel : orderedSearchTypePanels) {
            index++;
            
            if(searchTypePanel.getSearchType().getClass() == searchTypeToFind.getClass()){
                return index;
            }            
        }
        
        return -1;
    }
    
}
