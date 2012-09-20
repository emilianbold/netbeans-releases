/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2011 Oracle and/or its affiliates. All rights reserved.
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

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.ItemSelectable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.JTextComponent;
import org.netbeans.api.search.SearchHistory;
import org.netbeans.api.search.SearchPattern;
import org.netbeans.api.search.provider.SearchInfo;
import org.netbeans.api.search.ui.ComponentUtils;
import org.netbeans.api.search.ui.FileNameController;
import org.netbeans.api.search.ui.ScopeController;
import org.netbeans.api.search.ui.ScopeOptionsController;
import org.netbeans.api.search.ui.SearchPatternController;
import org.netbeans.api.search.ui.SearchPatternController.Option;
import org.netbeans.modules.search.ui.CheckBoxWithButtonPanel;
import org.netbeans.modules.search.ui.FormLayoutHelper;
import org.netbeans.modules.search.ui.PatternChangeListener;
import org.netbeans.modules.search.ui.TextFieldFocusListener;
import org.netbeans.modules.search.ui.UiUtils;
import org.netbeans.spi.search.SearchScopeDefinition;
import org.openide.cookies.EditorCookie;
import org.openide.nodes.Node;
import org.openide.text.NbDocument;
import org.openide.util.WeakListeners;
import org.openide.windows.TopComponent;

/**
 *
 * @author  Marian Petras
 */
final class BasicSearchForm extends JPanel implements ChangeListener,
                                                      ItemListener {

    private final String preferredSearchScopeType;
    private ChangeListener usabilityChangeListener;
    private BasicSearchCriteria searchCriteria = new BasicSearchCriteria();
    private SearchScopeDefinition[] extraSearchScopes;
    private boolean searchInGeneratedSetAutomatically = false;
    private PropertyChangeListener topComponentRegistryListener;

    /** Creates new form BasicSearchForm */
    BasicSearchForm(String preferredSearchScopeType,
            boolean searchAndReplace, BasicSearchCriteria initialCriteria,
            SearchScopeDefinition... extraSearchScopes) {

        this.preferredSearchScopeType = preferredSearchScopeType;
        this.extraSearchScopes = extraSearchScopes;
        initComponents(searchAndReplace);
        initAccessibility(searchAndReplace);
        initHistory();
        if (searchAndReplace && (searchCriteria.getReplaceExpr() == null)) {
            /* We must set the initial replace string, otherwise it might not
             * be initialized at all if the user keeps the field "Replace With:"
             * empty. One of the side-effects would be that method
             * BasicSearchCriteria.isSearchAndReplace() would return 'false'. */
            searchCriteria.setReplaceExpr("");                        //NOI18N
        }
        initInteraction(searchAndReplace);
        setValuesOfComponents(initialCriteria, searchAndReplace);
        setContextAwareOptions();
    }

    /**
     * Set values of form components.
     *
     * Interaction must be already set up when we set values, otherwise state of
     * the dialog might not be corresponding to the values, e.g. the Find dialog
     * could be disabled although valid values are entered.
     */
    private void setValuesOfComponents(
            BasicSearchCriteria initialCriteria, boolean searchAndReplace) {
        
        if (initialCriteria != null) {
            initValuesFromCriteria(initialCriteria, searchAndReplace);
        } else {
            initValuesFromHistory(searchAndReplace);
        }
        if (searchAndReplace) {
            updateReplacePatternColor();
        }
        useCurrentlySelectedText();
        setSearchCriteriaValues();
    }

    /**
     * Set currently selected text (in editor) as "Text to find" value.
     */
    public void useCurrentlySelectedText() {
        Node[] arr = TopComponent.getRegistry().getActivatedNodes();
        if (arr.length > 0) {
            EditorCookie ec = arr[0].getLookup().lookup(EditorCookie.class);
            if (ec != null) {
                JEditorPane recentPane = NbDocument.findRecentEditorPane(ec);
                if (recentPane != null) {
                    String initSearchText = recentPane.getSelectedText();
                    if (initSearchText != null) {
                        cboxTextToFind.setSearchPattern(SearchPattern.create(
                                initSearchText, false, false, false));
                        searchCriteria.setTextPattern(initSearchText);
                        return;
                    }
                }
            }
        }
        searchCriteria.setTextPattern(
                cboxTextToFind.getSearchPattern().getSearchExpression());
    }
    
    /**
     * Set options that depend on current context, and listeners that ensure
     * they stay valid when the context changes.
     */
    private void setContextAwareOptions() {
        updateSearchInGeneratedForActiveTopComponent();
        topComponentRegistryListener = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals(
                        TopComponent.Registry.PROP_ACTIVATED)) {
                    updateSearchInGeneratedForActiveTopComponent();
                }
            }
        };
        TopComponent.getRegistry().addPropertyChangeListener(
                WeakListeners.propertyChange(topComponentRegistryListener,
                TopComponent.getRegistry()));
    }

    /**
     * Update searching in generated sources. If Files window is selected,
     * Search In Generated Sources option should be checked automatically.
     */
    private void updateSearchInGeneratedForActiveTopComponent() {
        TopComponent tc = TopComponent.getRegistry().getActivated();
        if (tc != null && tc.getHelpCtx() != null
                && "ProjectTab_Files".equals( //NOI18N
                tc.getHelpCtx().getHelpID())) {
            if (!scopeSettingsPanel.isSearchInGenerated()) {
                scopeSettingsPanel.setSearchInGenerated(true);
                searchInGeneratedSetAutomatically = true;
            }
        } else if (searchInGeneratedSetAutomatically) {
            scopeSettingsPanel.setSearchInGenerated(false);
            searchInGeneratedSetAutomatically = false;
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     * 
     * @param  searchAndReplace  {@code true} if components for
     *				 search &amp; replace should be created;
     *                           {@code false} otherwise
     */
    private void initComponents(final boolean searchAndReplace) {

        lblTextToFind = new JLabel();
        cboxTextToFind = ComponentUtils.adjustComboForSearchPattern(new JComboBox());
        lblTextToFind.setLabelFor(cboxTextToFind.getComponent());
        btnTestTextToFind = new JButton();

        if (searchAndReplace) {
            lblReplacement = new JLabel();
            cboxReplacement = new JComboBox();
            cboxReplacement.setEditable(true);
            lblReplacement.setLabelFor(cboxReplacement);
            chkPreserveCase = new JCheckBox();
        }

        lblScope = new JLabel();
        cboxScope = ComponentUtils.adjustComboForScope(new JComboBox(),
                preferredSearchScopeType, extraSearchScopes);
        lblScope.setLabelFor(cboxScope.getComponent());

        lblFileNamePattern = new JLabel();
        cboxFileNamePattern = ComponentUtils.adjustComboForFileName(
                new JComboBox());
        lblFileNamePattern.setLabelFor(cboxFileNamePattern.getComponent());
        
        chkWholeWords = new JCheckBox();
        chkCaseSensitive = new JCheckBox();
        chkRegexp = new JCheckBox();

        TextPatternCheckBoxGroup.bind(
                chkCaseSensitive, chkWholeWords, chkRegexp, chkPreserveCase);

        setMnemonics(searchAndReplace);
        
        initFormPanel(searchAndReplace);
        this.add(formPanel);

        /* find the editor components of combo-boxes: */
        Component cboxEditorComp;
        if (cboxReplacement != null) {
            cboxEditorComp = cboxReplacement.getEditor().getEditorComponent();
            replacementPatternEditor = (JTextComponent) cboxEditorComp;
        }
        this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
    }

    /**
     * Initialize form panel and add appropriate components to it.
     */
    protected void initFormPanel(boolean searchAndReplace) {

        formPanel = new SearchFormPanel();
        formPanel.addRow(lblTextToFind, cboxTextToFind.getComponent());
        initContainingTextOptionsRow(searchAndReplace);
        if (searchAndReplace) {
            formPanel.addRow(lblReplacement, cboxReplacement);
        }
        formPanel.addRow(lblScope, cboxScope.getComponent());
        formPanel.addRow(lblFileNamePattern,
                cboxFileNamePattern.getComponent());
        initScopeOptionsRow(searchAndReplace);
    }

    /**
     * Initialize panel for controls for text pattern options and add it to the
     * form panel.
     */
    private void initContainingTextOptionsRow(boolean searchAndReplace) {

        JPanel jp = new JPanel();
        if (searchAndReplace) {
            FormLayoutHelper flh = new FormLayoutHelper(jp,
                    FormLayoutHelper.DEFAULT_COLUMN,
                    FormLayoutHelper.DEFAULT_COLUMN);
            flh.addRow(chkCaseSensitive, chkPreserveCase);
            flh.addRow(chkWholeWords,
                    new CheckBoxWithButtonPanel(chkRegexp, btnTestTextToFind));
            jp.setMaximumSize(jp.getMinimumSize());

            formPanel.addRow(new JLabel(), jp);
        } else {
            jp.setLayout(new FlowLayout(FlowLayout.LEADING, 0, 0));
            jp.add(chkCaseSensitive);
            jp.add(chkWholeWords);
            jp.add(new CheckBoxWithButtonPanel(chkRegexp, btnTestTextToFind));
            jp.setMaximumSize(jp.getPreferredSize());
            formPanel.addRow(new JLabel(), jp);
        }
    }

    private void initScopeOptionsRow(boolean searchAndReplace) {
        this.scopeSettingsPanel = ComponentUtils.adjustPanelForOptions(
                new JPanel(), searchAndReplace, cboxFileNamePattern);
        formPanel.addRow(new JLabel(), scopeSettingsPanel.getComponent());
    }

    /**
     */
    private void initAccessibility(boolean searchAndReplace) {
        chkCaseSensitive.getAccessibleContext().setAccessibleDescription(
                UiUtils.getText(
                "BasicSearchForm.chkCaseSensitive."                     //NOI18N
                + "AccessibleDescription"));                            //NOI18N
        chkRegexp.getAccessibleContext().setAccessibleDescription(
                UiUtils.getText(
                "BasicSearchForm.chkRegexp."                            //NOI18N
                + "AccessibleDescription"));                            //NOI18N
        chkWholeWords.getAccessibleContext().setAccessibleDescription(
                UiUtils.getText(
                "BasicSearchForm.chkWholeWords."                        //NOI18N
                + "AccessibleDescription"));                            //NOI18N
        if (searchAndReplace) {
            cboxReplacement.getAccessibleContext().setAccessibleDescription(
                    UiUtils.getText(
                    "BasicSearchForm.cbox.Replacement."                 //NOI18N
                    + "AccessibleDescription"));                        //NOI18N
            chkPreserveCase.getAccessibleContext().setAccessibleDescription(
                    UiUtils.getText(
                    "BasicSearchForm.chkPreserveCase."                  //NOI18N
                    + "AccessibleDescription"));                        //NOI18N
        }
    }

    /**
     * Fills text and sets values of check-boxes according to the current
     * search criteria.
     */
    private void initValuesFromCriteria(BasicSearchCriteria initialCriteria,
            boolean searchAndReplace) {
        cboxTextToFind.setSearchPattern(initialCriteria.getSearchPattern());
        if (cboxReplacement != null) {
            cboxReplacement.setSelectedItem(initialCriteria.getReplaceExpr());
        }

        selectChk(chkPreserveCase, initialCriteria.isPreserveCase());
        scopeSettingsPanel.setFileNameRegexp(initialCriteria.isFileNameRegexp());
        scopeSettingsPanel.setUseIgnoreList(initialCriteria.isUseIgnoreList());
        cboxFileNamePattern.setRegularExpression(initialCriteria.isFileNameRegexp());
        cboxFileNamePattern.setFileNamePattern(initialCriteria.getFileNamePatternExpr());
        if (!searchAndReplace) {
            scopeSettingsPanel.setSearchInArchives(
                    initialCriteria.isSearchInArchives());
            scopeSettingsPanel.setSearchInGenerated(
                    initialCriteria.isSearchInGenerated());
        }
    }

    private static void selectChk(JCheckBox checkbox, boolean value) {
        if (checkbox != null) {
            checkbox.setSelected(value);
        }
    }
    
    /**
     */
    private void initInteraction(final boolean searchAndReplace) {
        /* set up updating of the validity status: */
        

        final TextFieldFocusListener focusListener = new TextFieldFocusListener();
        if (replacementPatternEditor != null) {
            replacementPatternEditor.addFocusListener(focusListener);
        }

        if (replacementPatternEditor != null) {
            replacementPatternEditor.getDocument().addDocumentListener(
                    new ReplacementPatternListener());
        }
        
        chkRegexp.addItemListener(this);
        cboxTextToFind.bind(Option.REGULAR_EXPRESSION, chkRegexp);
        cboxTextToFind.bind(Option.MATCH_CASE, chkCaseSensitive);
        cboxTextToFind.bind(Option.WHOLE_WORDS, chkWholeWords);

        boolean regexp = chkRegexp.isSelected();
        boolean caseSensitive = chkCaseSensitive.isSelected();
        chkWholeWords.setEnabled(!regexp);
        if (searchAndReplace) {
            chkPreserveCase.addItemListener(this);
            chkPreserveCase.setEnabled(!regexp && !caseSensitive);
        }
        searchCriteria.setUsabilityChangeListener(this);

        scopeSettingsPanel.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                searchCriteria.setSearchInArchives(
                        scopeSettingsPanel.isSearchInArchives());
                searchCriteria.setSearchInGenerated(
                        scopeSettingsPanel.isSearchInGenerated());
                searchCriteria.setUseIgnoreList(
                        scopeSettingsPanel.isUseIgnoreList());
            }
        });

        cboxFileNamePattern.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                searchCriteria.setFileNamePattern(
                        cboxFileNamePattern.getFileNamePattern());
                searchCriteria.setFileNameRegexp(
                        cboxFileNamePattern.isRegularExpression());
            }
        });

        cboxTextToFind.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                SearchPattern sp = cboxTextToFind.getSearchPattern();
                searchCriteria.setTextPattern(sp.getSearchExpression());
                searchCriteria.setRegexp(sp.isRegExp());
                searchCriteria.setWholeWords(sp.isWholeWords());
                searchCriteria.setCaseSensitive(sp.isMatchCase());
            }
        });
        initButtonInteraction();
    }

    private void initButtonInteraction() {
       
        btnTestTextToFind.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                openTextPatternSandbox();
            }
        });
    }

    private void openTextPatternSandbox() {

        SearchPattern sp = cboxTextToFind.getSearchPattern();
        String expr = sp.getSearchExpression() == null ? "" // NOI18N
                : sp.getSearchExpression();
        boolean matchCase = chkCaseSensitive.isSelected();

        PatternSandbox.openDialog(new PatternSandbox.TextPatternSandbox(
                expr, matchCase) {

            @Override
            protected void onApply(String newExpr, boolean newMatchCase) {
                cboxTextToFind.setSearchPattern(SearchPattern.create(
                        newExpr, false, newMatchCase, true));
            }
        }, btnTestTextToFind);
    }

    /**
     * Initializes pop-ups of combo-boxes with last entered patterns and
     * expressions. The combo-boxes' text-fields remain empty.
     */
    private void initHistory() {

        FindDialogMemory memory = FindDialogMemory.getDefault();
        List<String> entries;

        if (cboxReplacement != null) {
            entries = memory.getReplacementExpressions();
            if (!entries.isEmpty()) {
                cboxReplacement.setModel(new ListComboBoxModel(entries, true));
            }
        }
    }
    
    /**
     */
    private void initValuesFromHistory(final boolean searchAndReplace) {
        final FindDialogMemory memory = FindDialogMemory.getDefault();

        if (memory.isFileNamePatternSpecified()
                && cboxFileNamePattern.getComponent().getItemCount() != 0) {
            cboxFileNamePattern.getComponent().setSelectedIndex(0);
        }
        if (cboxReplacement != null && cboxReplacement.getItemCount() != 0) {
            cboxReplacement.setSelectedIndex(0);
        }

        chkWholeWords.setSelected(memory.isWholeWords());
        chkCaseSensitive.setSelected(memory.isCaseSensitive());
        chkRegexp.setSelected(memory.isRegularExpression());

        scopeSettingsPanel.setFileNameRegexp(memory.isFilePathRegex());
        scopeSettingsPanel.setUseIgnoreList(memory.IsUseIgnoreList());
        if (searchAndReplace) {
            chkPreserveCase.setSelected(memory.isPreserveCase());
        } else {
            scopeSettingsPanel.setSearchInArchives(memory.isSearchInArchives());
            scopeSettingsPanel.setSearchInGenerated(
                    memory.isSearchInGenerated());
        }
    }
    
    private void setSearchCriteriaValues() {
        searchCriteria.setWholeWords(chkWholeWords.isSelected());
        searchCriteria.setCaseSensitive(chkCaseSensitive.isSelected());
        searchCriteria.setRegexp(chkRegexp.isSelected());
        searchCriteria.setFileNameRegexp(scopeSettingsPanel.isFileNameRegExp());
        searchCriteria.setUseIgnoreList(scopeSettingsPanel.isUseIgnoreList());
        searchCriteria.setSearchInArchives(
                scopeSettingsPanel.isSearchInArchives());
        searchCriteria.setSearchInGenerated(
                scopeSettingsPanel.isSearchInGenerated());
        if (chkPreserveCase != null) {
            searchCriteria.setPreserveCase(chkPreserveCase.isSelected());
        }
    }

    @Override
    public boolean requestFocusInWindow() {
	return cboxTextToFind.getComponent().requestFocusInWindow();
    }

    /**
     * Sets proper color of replace pattern.
     */
    private void updateReplacePatternColor() {
        boolean wasInvalid = invalidReplacePattern;
        invalidReplacePattern = searchCriteria.isReplacePatternInvalid();
        if (invalidReplacePattern != wasInvalid) {
            if (defaultTextColor == null) {
                assert !wasInvalid;
                defaultTextColor = cboxReplacement.getForeground();
            }
            replacementPatternEditor.setForeground(
                    invalidReplacePattern ? getErrorTextColor()
                                       : defaultTextColor);
        }
    }

    private static boolean isBackrefSyntaxUsed(String text) {
        final int len = text.length();
        if (len < 2) {
            return false;
        }
        String textToSearch = text.substring(0, len - 1);
        int startIndex = 0;
        int index;
        while ((index = textToSearch.indexOf('\\', startIndex)) != -1) {
            char c = text.charAt(index + 1);
            if (c == '\\') {
                startIndex = index + 1;
            } else if ((c >= '0') && (c <= '9')) {
                return true;
            } else {
                startIndex = index + 2;
            }
        }
        return false;
    }
    
    private Color getErrorTextColor() {
        if (errorTextColor == null) {
            errorTextColor = UIManager.getDefaults()
                             .getColor("TextField.errorForeground");    //NOI18N
            if (errorTextColor == null) {
                errorTextColor = Color.RED;
            }
        }
        return errorTextColor;
    }
    
    void setUsabilityChangeListener(ChangeListener l) {
        usabilityChangeListener = l;
    }
    
    @Override
    public void stateChanged(ChangeEvent e) {
        if (usabilityChangeListener != null) {
            usabilityChangeListener.stateChanged(new ChangeEvent(this));
        }
    }
    
    /**
     * Called when some of the check-boxes is selected or deselected.
     * 
     * @param  e  event object holding information about the change
     */
    @Override
    public void itemStateChanged(ItemEvent e) {
        final ItemSelectable toggle = e.getItemSelectable();
        final boolean selected = (e.getStateChange() == ItemEvent.SELECTED);
        if (toggle == chkRegexp) {
            if (cboxReplacement != null){
                updateReplacePatternColor();
            }
            setTextToFindToolTip();
        } else if (toggle == chkPreserveCase) {
            searchCriteria.setPreserveCase(selected);
        } else {
            assert false;
        }
    }

    private void setTextToFindToolTip() {
        String t;
        if (searchCriteria.isRegexp()) {
            t = null;
        } else {
            t = UiUtils.getText(
                    "BasicSearchForm.cboxTextToFind.tooltip");          //NOI18N
        }
        cboxTextToFind.getComponent().setToolTipText(t);
    }

    /**
     * Called when the criteria in the Find dialog are confirmed by the user
     * and the search is about to be started.
     * This method just passes the message to the criteria object.
     */
    void onOk() {
        searchCriteria.onOk();

        final FindDialogMemory memory = FindDialogMemory.getDefault();

        if (searchCriteria.isTextPatternUsable()) {
            SearchHistory.getDefault().add(getCurrentSearchPattern());
            memory.setTextPatternSpecified(true);
        } else {
            memory.setTextPatternSpecified(false);
        }
        if (searchCriteria.isFileNamePatternUsable()) {
            memory.storeFileNamePattern(
                    searchCriteria.getFileNamePatternExpr());
            memory.setFileNamePatternSpecified(true);
        } else {
            memory.setFileNamePatternSpecified(false);
        }
        if (replacementPatternEditor != null) {
            memory.storeReplacementExpression(
                    replacementPatternEditor.getText());
        }
        memory.setWholeWords(chkWholeWords.isSelected());
        memory.setCaseSensitive(chkCaseSensitive.isSelected());
        memory.setRegularExpression(chkRegexp.isSelected());
        if (searchCriteria.isSearchAndReplace()) {
            memory.setPreserveCase(chkPreserveCase.isSelected());
        } else {
            memory.setSearchInArchives(scopeSettingsPanel.isSearchInArchives());
            if (!searchInGeneratedSetAutomatically) {
                memory.setSearchInGenerated(
                        scopeSettingsPanel.isSearchInGenerated());
            }
        }
        memory.setFilePathRegex(scopeSettingsPanel.isFileNameRegExp());
        memory.setUseIgnoreList(scopeSettingsPanel.isUseIgnoreList());
        if (cboxScope.getSelectedScopeId() != null
                && !SearchPanel.isOpenedForSelection()) {
            memory.setScopeTypeId(cboxScope.getSelectedScopeId());
        }
    }

    /**
     * Read current dialog contents as a SearchPattern.
     *
     * @return SearchPattern for the contents of the current dialog. Null if the
     * * search string is empty, meaning that the dialog is empty.
     */
    private SearchPattern getCurrentSearchPattern() {
        return cboxTextToFind.getSearchPattern();
    }

    /**
     */
    public SearchInfo getSearchInfo() {
        return cboxScope.getSearchInfo();
    }

    public String getSelectedScopeName() {
        return cboxScope.getSelectedScopeTitle();
    }

    /** */
    BasicSearchCriteria getBasicSearchCriteria() {
        return searchCriteria;
    }
    
    boolean isUsable() {
        return (cboxScope.getSearchInfo() != null)
               && searchCriteria.isUsable();
    }

    private void setMnemonics(boolean searchAndReplace) {

        lclz(lblTextToFind, "BasicSearchForm.lblTextToFind.text");      //NOI18N
        lclz(lblScope, "BasicSearchForm.lblScope.text");                //NOI18N
        lclz(lblFileNamePattern,
                "BasicSearchForm.lblFileNamePattern.text");             //NOI18N
        lclz(chkWholeWords, "BasicSearchForm.chkWholeWords.text");      //NOI18N
        lclz(chkCaseSensitive, "BasicSearchForm.chkCaseSensitive.text");//NOI18N
        lclz(chkRegexp, "BasicSearchForm.chkRegexp.text");              //NOI18N
        
        btnTestTextToFind.setText(UiUtils.getHtmlLink(
                "BasicSearchForm.btnTestTextToFind.text"));             //NOI18N
       

        if (searchAndReplace) {
            lclz(lblReplacement, "BasicSearchForm.lblReplacement.text");//NOI18N
            lclz(chkPreserveCase,
                    "BasicSearchForm.chkPreserveCase.text");            //NOI18N
        } else {
          
        }
        setTextToFindToolTip();
    }

    private void lclz(AbstractButton ab, String msg) {
        UiUtils.lclz(ab, msg);
    }

    private void lclz(JLabel l, String msg) {
        UiUtils.lclz(l, msg);
    }

    private static final Logger watcherLogger = Logger.getLogger(
            "org.netbeans.modules.search.BasicSearchForm.FileNamePatternWatcher");//NOI18N

    private SearchPatternController cboxTextToFind;
    private JComboBox cboxReplacement;
    private FileNameController cboxFileNamePattern;
    private JCheckBox chkWholeWords;
    private JCheckBox chkCaseSensitive;
    private JCheckBox chkRegexp;
    private JCheckBox chkPreserveCase;
    private JTextComponent replacementPatternEditor;
    protected SearchFormPanel formPanel;
    private JButton btnTestTextToFind;
    private JLabel lblTextToFind;
    private ScopeController cboxScope;
    private JLabel lblFileNamePattern;    
    private JLabel lblScope;
    private JLabel lblReplacement;
    private Color errorTextColor, defaultTextColor;
    private boolean invalidTextPattern = false;
    private boolean invalidReplacePattern = false;
    private ScopeOptionsController scopeSettingsPanel;

    /**
     * Form panel to which rows can be added.
     */
    private final class SearchFormPanel extends JPanel {

        private FormLayoutHelper flh;

        public SearchFormPanel() {
            super();
            this.flh = new FormLayoutHelper(this,
                    FormLayoutHelper.DEFAULT_COLUMN,
                    FormLayoutHelper.DEFAULT_COLUMN);
            flh.setAllGaps(true);
        }

        public void addRow(JComponent label, JComponent component) {

            flh.addRow(label, component);
        }
    }

    /**
     * Class for controlling which settings of text pattern depend on other
     * settings and how.
     */
    static class TextPatternCheckBoxGroup implements ItemListener {

        private JCheckBox matchCase;
        private JCheckBox wholeWords;
        private JCheckBox regexp;
        private JCheckBox preserveCase;
        private boolean lastPreserveCaseValue;
        private boolean lastWholeWordsValue;

        private TextPatternCheckBoxGroup(
                JCheckBox matchCase,
                JCheckBox wholeWords,
                JCheckBox regexp,
                JCheckBox preserveCase) {

            this.matchCase = matchCase;
            this.wholeWords = wholeWords;
            this.regexp = regexp;
            this.preserveCase = preserveCase;
        }

        private void initListeners() {
            this.matchCase.addItemListener(this);
            this.wholeWords.addItemListener(this);
            this.regexp.addItemListener(this);
            if (this.preserveCase != null) {
                this.preserveCase.addItemListener(this);
            }
        }

        private void matchCaseChanged() {
            updatePreserveCaseAllowed();
        }

        private void regexpChanged() {
            updateWholeWordsAllowed();
            updatePreserveCaseAllowed();
        }

        private void updateWholeWordsAllowed() {
            if (regexp.isSelected() == wholeWords.isEnabled()) {
                if (regexp.isSelected()) {
                    lastWholeWordsValue = wholeWords.isSelected();
                    wholeWords.setSelected(false);
                    wholeWords.setEnabled(false);
                } else {
                    wholeWords.setEnabled(true);
                    wholeWords.setSelected(lastWholeWordsValue);
                }
            }
        }

        private void updatePreserveCaseAllowed() {
            if (preserveCase == null) {
                return;
            }
            if (preserveCase.isEnabled()
                    == (regexp.isSelected() || matchCase.isSelected())) {
                if (preserveCase.isEnabled()) {
                    lastPreserveCaseValue = preserveCase.isSelected();
                    preserveCase.setSelected(false);
                    preserveCase.setEnabled(false);
                } else {
                    preserveCase.setEnabled(true);
                    preserveCase.setSelected(lastPreserveCaseValue);
                }
            }
        }

        @Override
        public void itemStateChanged(ItemEvent e) {
            ItemSelectable is = e.getItemSelectable();
            if (is == matchCase) {
                matchCaseChanged();
            } else if (is == regexp) {
                regexpChanged();
            }
        }

        static void bind(JCheckBox matchCase,
                JCheckBox wholeWords,
                JCheckBox regexp,
                JCheckBox preserveCase) {

            TextPatternCheckBoxGroup tpcbg = new TextPatternCheckBoxGroup(
                    matchCase, wholeWords, regexp, preserveCase);
            tpcbg.initListeners();
        }
    }

    private class ReplacementPatternListener extends PatternChangeListener {

        public ReplacementPatternListener() {
        }

        @Override
        public void handleComboBoxChange(String text) {
            searchCriteria.setReplaceExpr(text);
            if (cboxReplacement != null) {
                updateReplacePatternColor();
            }
        }
    }
}
