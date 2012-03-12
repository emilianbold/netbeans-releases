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
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.ItemSelectable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.HierarchyEvent;
import static java.awt.event.HierarchyEvent.DISPLAYABILITY_CHANGED;
import java.awt.event.HierarchyListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.openide.ErrorManager;
import org.openide.awt.Mnemonics;
import org.openide.cookies.EditorCookie;
import org.openide.nodes.Node;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openidex.search.SearchHistory;
import org.openidex.search.SearchPattern;

/**
 *
 * @author  Marian Petras
 */
final class BasicSearchForm extends JPanel implements ChangeListener,
                                                      ItemListener {
    public static final String HTML_LINK_PREFIX =
            "<html><u><a href=\"#\">";                                  //NOI18N
    public static final String HTML_LINK_SUFFIX = "</a></u></html>";    //NOI18N
    
    private final BasicSearchCriteria searchCriteria;
    private final Map<SearchScope, Boolean> searchScopes;
    private final String preferredSearchScopeType;
    private SearchScope selectedSearchScope;
    private ChangeListener usabilityChangeListener;

    /** Creates new form BasicSearchForm */
    BasicSearchForm(Map<SearchScope, Boolean> searchScopes,
                    String preferredSearchScopeType,
		    BasicSearchCriteria criteria,
		    boolean searchAndReplace,
                    boolean usePreviousValues) {
        this.searchCriteria = (criteria != null)
                              ? criteria
                              : new BasicSearchCriteria();
        this.searchScopes = searchScopes;
        this.preferredSearchScopeType = preferredSearchScopeType;
        initComponents(searchAndReplace);
        initAccessibility(searchAndReplace);
        initHistory();
        initInteraction(searchAndReplace);
        if (searchAndReplace && (searchCriteria.getReplaceExpr() == null)) {
            /* We must set the initial replace string, otherwise it might not
             * be initialized at all if the user keeps the field "Replace With:"
             * empty. One of the side-effects would be that method
             * BasicSearchCriteria.isSearchAndReplace() would return 'false'. */
            searchCriteria.setReplaceExpr("");                        //NOI18N
        }
        setValuesOfComponents(usePreviousValues, searchAndReplace);
    }

    /**
     * Set values of form components.
     *
     * Interaction must be already set up when we set values, otherwise state of
     * the dialog might not be corresponding to the values, e.g. the Find dialog
     * could be disabled although valid values are entered.
     */
    private void setValuesOfComponents(
            boolean usePreviousValues, boolean searchAndReplace) {
        
        if (usePreviousValues) {
            initPreviousValues();
        } else {
            initValuesFromHistory(searchAndReplace);
        }
        updateTextPatternColor();
        updateFileNamePatternColor();
        if (searchAndReplace) {
            updateReplacePatternColor();
        }
        useCurrentlySelectedText();
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
                    String initSearchText = null;
                    if (org.netbeans.editor.Utilities.isSelectionShowing(
                            recentPane.getCaret())) {
                        initSearchText = recentPane.getSelectedText();
                    }
                    if (initSearchText != null) {
                        cboxTextToFind.setSelectedIndex(-1);
                        textToFindEditor.setText(initSearchText);
                        searchCriteria.setTextPattern(initSearchText);
                    }
                }
            }
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
        cboxTextToFind = new JComboBox();        
        cboxTextToFind.setEditable(true);
        lblTextToFind.setLabelFor(cboxTextToFind);
        btnTestTextToFind = new JButton();

        if (searchAndReplace) {
            lblReplacement = new JLabel();
            cboxReplacement = new JComboBox();
            cboxReplacement.setEditable(true);
            lblReplacement.setLabelFor(cboxReplacement);
            chkPreserveCase = new JCheckBox();
        }

        lblScope = new JLabel();
        cboxScope = new JComboBox();
        cboxScope.setEditable(false);
        lblScope.setLabelFor(cboxScope);

        lblFileNamePattern = new JLabel();
        cboxFileNamePattern = new JComboBox();
        cboxFileNamePattern.setEditable(true);
        lblFileNamePattern.setLabelFor(cboxFileNamePattern);        
        btnTestFileNamePattern = new JButton();

        chkFileNameRegex = new JCheckBox();
        chkWholeWords = new JCheckBox();
        chkCaseSensitive = new JCheckBox();
        chkRegexp = new JCheckBox();

        TextPatternCheckBoxGroup.bind(
                chkCaseSensitive, chkWholeWords, chkRegexp, chkPreserveCase);

        if (!searchAndReplace) {
            chkArchives = new JCheckBox();
            chkArchives.setEnabled(false); // not implemented yet
            chkGenerated = new JCheckBox();
            chkGenerated.setEnabled(false); // not implemented yet
        }
        chkUseIgnoreList = new JCheckBox();
        btnEditIgnoreList = new JButton();

        setMnemonics(searchAndReplace);
        initIgnoreListControlComponents();
        initFormPanel(searchAndReplace);
        this.add(formPanel);

        /* find the editor components of combo-boxes: */
        Component cboxEditorComp;
        cboxEditorComp = cboxTextToFind.getEditor().getEditorComponent();
        textToFindEditor = (JTextComponent) cboxEditorComp;
        cboxEditorComp = cboxFileNamePattern.getEditor().getEditorComponent();
        fileNamePatternEditor = (JTextComponent) cboxEditorComp;
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
        formPanel.addRow(lblTextToFind, cboxTextToFind);
        initContainingTextOptionsRow(searchAndReplace);
        if (searchAndReplace) {
            formPanel.addRow(lblReplacement, cboxReplacement);
        }
        initScopeRow();
        formPanel.addRow(lblScope, cboxScope);
        formPanel.addRow(lblFileNamePattern, cboxFileNamePattern);
        initScopeOptionsRow(searchAndReplace);
    }

    /**
     * Initialize ignoreListOptionPanel and related control components.
     */
    private void initIgnoreListControlComponents() {
        ignoreListOptionPanel = new CheckBoxWithButtonPanel(chkUseIgnoreList,
                btnEditIgnoreList);
    }

    /**
     * Add row with selection of scope to the form panel.
     */
    protected final void initScopeRow() {

        for (Map.Entry<SearchScope, Boolean> e : orderSearchScopes()) {
            if (e.getValue()) { // add only enabled search scopes
                SearchScope ss = e.getKey();
                ScopeItem si = new ScopeItem(ss);
                cboxScope.addItem(si);
                if (selectedSearchScope == null) {
                    if (ss.getTypeId().equals(preferredSearchScopeType)) {
                        selectedSearchScope = ss;
                        cboxScope.setSelectedItem(si);
                    }
                }
            }
        }
        if (selectedSearchScope == null) {
            ScopeItem si = (ScopeItem) cboxScope.getItemAt(0);
            selectedSearchScope = si.getSearchScope();
            cboxScope.setSelectedIndex(0);
        }
        cboxScope.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                ScopeItem item = (ScopeItem) cboxScope.getSelectedItem();
                selectedSearchScope = item.getSearchScope();
                stateChanged(null);
            }
        });
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

    /**
     * Initialize panel for controls for scope options and add it to the form
     * panel.
     */
    private void initScopeOptionsRow(boolean searchAndReplace) {

        JPanel jp = new JPanel();
        if (searchAndReplace) {
            jp.setLayout(new FlowLayout(FlowLayout.LEADING, 0, 0));
            jp.add(ignoreListOptionPanel);
            jp.add(chkFileNameRegex);
            jp.setMaximumSize(jp.getMinimumSize());
        } else {
            FormLayoutHelper flh = new FormLayoutHelper(jp,
                    FormLayoutHelper.DEFAULT_COLUMN,
                    FormLayoutHelper.DEFAULT_COLUMN);
            flh.addRow(chkArchives, chkGenerated);
            flh.addRow(ignoreListOptionPanel,
                    new CheckBoxWithButtonPanel(
                    chkFileNameRegex, btnTestFileNamePattern));
            jp.setMaximumSize(jp.getMinimumSize());
        }
        formPanel.addRow(new JLabel(), jp);
    }

    /**
     */
    private void initAccessibility(boolean searchAndReplace) {
        chkCaseSensitive.getAccessibleContext().setAccessibleDescription(
                getText(
                "BasicSearchForm.chkCaseSensitive."                     //NOI18N
                + "AccessibleDescription"));                            //NOI18N
        chkRegexp.getAccessibleContext().setAccessibleDescription(
                getText(
                "BasicSearchForm.chkRegexp."                            //NOI18N
                + "AccessibleDescription"));                            //NOI18N
        chkWholeWords.getAccessibleContext().setAccessibleDescription(
                getText(
                "BasicSearchForm.chkWholeWords."                        //NOI18N
                + "AccessibleDescription"));                            //NOI18N
        if (searchAndReplace) {
            cboxReplacement.getAccessibleContext().setAccessibleDescription(
                    getText(
                    "BasicSearchForm.cbox.Replacement."                 //NOI18N
                    + "AccessibleDescription"));                        //NOI18N
            chkPreserveCase.getAccessibleContext().setAccessibleDescription(
                    getText(
                    "BasicSearchForm.chkPreserveCase."                  //NOI18N
                    + "AccessibleDescription"));                        //NOI18N
        }
    }

    /**
     * Fills text and sets values of check-boxes according to the current
     * search criteria.
     */
    private void initPreviousValues() {
        cboxTextToFind.setSelectedItem(searchCriteria.getTextPatternExpr());
        cboxFileNamePattern.setSelectedItem(searchCriteria.getFileNamePatternExpr());
        if (cboxReplacement != null) {
            cboxReplacement.setSelectedItem(searchCriteria.getReplaceExpr());
        }

        chkWholeWords.setSelected(searchCriteria.isWholeWords());
        chkCaseSensitive.setSelected(searchCriteria.isCaseSensitive());
        chkRegexp.setSelected(searchCriteria.isRegexp());
        chkFileNameRegex.setSelected(searchCriteria.isFileNameRegexp());
        chkUseIgnoreList.setSelected(searchCriteria.isUseIgnoreList());

        selectChk(chkPreserveCase, searchCriteria.isPreserveCase());
        selectChk(chkArchives, searchCriteria.isSearchInArchives());
        selectChk(chkGenerated, searchCriteria.isSearchInGenerated());
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
        textToFindEditor.addFocusListener(focusListener);
        if (replacementPatternEditor != null) {
            replacementPatternEditor.addFocusListener(focusListener);
        }

        fileNamePatternWatcher = 
                new FileNamePatternWatcher(fileNamePatternEditor);        
        fileNamePatternEditor.addFocusListener(fileNamePatternWatcher);
        fileNamePatternEditor.addHierarchyListener(fileNamePatternWatcher);
        
        textToFindEditor.getDocument().addDocumentListener(
                new PatternChangeListener(cboxTextToFind));
        fileNamePatternEditor.getDocument().addDocumentListener(
                new PatternChangeListener(cboxFileNamePattern));
        if (replacementPatternEditor != null) {
            replacementPatternEditor.getDocument().addDocumentListener(
                    new PatternChangeListener(cboxReplacement));
        }
        
        chkRegexp.addItemListener(this);
        chkCaseSensitive.addItemListener(this);
        chkWholeWords.addItemListener(this);
        chkFileNameRegex.addItemListener(this);
        chkUseIgnoreList.addItemListener(this);

        boolean regexp = chkRegexp.isSelected();
        boolean caseSensitive = chkCaseSensitive.isSelected();
        chkWholeWords.setEnabled(!regexp);
        if (searchAndReplace) {
            chkPreserveCase.addItemListener(this);
            chkPreserveCase.setEnabled(!regexp && !caseSensitive);
        } else {
            chkArchives.addItemListener(this);
            chkGenerated.addItemListener(this);
        }
        searchCriteria.setUsabilityChangeListener(this);

        initButtonInteraction();
    }

    private void initButtonInteraction() {
        btnTestFileNamePattern.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                openPathPatternSandbox();
            }
        });
        btnTestTextToFind.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                openTextPatternSandbox();
            }
        });
        btnEditIgnoreList.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                IgnoreListPanel.openDialog(btnEditIgnoreList);
            }
        });
    }

    private void openPathPatternSandbox() {

        PatternSandbox.openDialog(new PatternSandbox.PathPatternSandbox(
                cboxFileNamePattern.getSelectedItem() == null
                ? "" : (String) cboxFileNamePattern.getSelectedItem()){ //NOI18N

            @Override
            protected void onApply(String pattern) {
                if (pattern.isEmpty()) {
                    if (!fileNamePatternWatcher.infoDisplayed) {
                        cboxFileNamePattern.setSelectedItem(pattern);
                        fileNamePatternWatcher.displayInfo();
                    }
                } else {
                    if (fileNamePatternWatcher.infoDisplayed) {
                        fileNamePatternWatcher.hideInfo();
                    }
                    cboxFileNamePattern.setSelectedItem(pattern);
                }
            }
        }, btnTestFileNamePattern);
    }

    private void openTextPatternSandbox() {

        String expr = cboxTextToFind.getSelectedItem() == null ? "" // NOI18N
                : cboxTextToFind.getSelectedItem().toString();
        boolean matchCase = chkCaseSensitive.isSelected();

        PatternSandbox.openDialog(new PatternSandbox.TextPatternSandbox(
                expr, matchCase) {

            @Override
            protected void onApply(String newExpr, boolean newMatchCase) {
                cboxTextToFind.setSelectedItem(newExpr);
                chkCaseSensitive.setSelected(newMatchCase);
            }
        }, btnTestTextToFind);
    }

    /**
     * Initializes pop-ups of combo-boxes with last entered patterns and
     * expressions. The combo-boxes' text-fields remain empty.
     */
    private void initHistory() {
        final List<SearchPattern> patterns
                = SearchHistory.getDefault().getSearchPatterns();
        if (!patterns.isEmpty()) {
            List<String> itemsList = new ArrayList<String>(patterns.size());
            for (SearchPattern pattern : patterns) {
                itemsList.add(pattern.getSearchExpression());
            }
            cboxTextToFind.setModel(new ListComboBoxModel(itemsList));
        }

        FindDialogMemory memory = FindDialogMemory.getDefault();
        List<String> entries;

        entries = memory.getFileNamePatterns();
        if (!entries.isEmpty()) {
            cboxFileNamePattern.setModel(new ListComboBoxModel(entries, true));
        }

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

        if (memory.isTextPatternSpecified()
                && (cboxTextToFind.getItemCount() != 0)) {
            cboxTextToFind.setSelectedIndex(0);
        }
        if (memory.isFileNamePatternSpecified()
                && cboxFileNamePattern.getItemCount() != 0) {
            cboxFileNamePattern.setSelectedIndex(0);
        }
        if (cboxReplacement != null && cboxReplacement.getItemCount() != 0) {
            cboxReplacement.setSelectedIndex(0);
        }

        chkWholeWords.setSelected(memory.isWholeWords());
        chkCaseSensitive.setSelected(memory.isCaseSensitive());
        chkRegexp.setSelected(memory.isRegularExpression());
        chkFileNameRegex.setSelected(memory.isFilePathRegex());
        chkUseIgnoreList.setSelected(memory.IsUseIgnoreList());
        if (searchAndReplace) {
            chkPreserveCase.setSelected(memory.isPreserveCase());
        } else {
            chkArchives.setSelected(memory.isSearchInArchives());
            chkGenerated.setSelected(memory.isSearchInGenerated());
        }
    }
    
    @Override
    public boolean requestFocusInWindow() {
        assert textToFindEditor != null;
        
	if (textToFindEditor.isFocusOwner()) {
            return true;
	}

        int textLength = textToFindEditor.getText().length();
        if (textLength > 0) {
            textToFindEditor.setCaretPosition(0);
            textToFindEditor.moveCaretPosition(textLength);
        }

        return textToFindEditor.requestFocusInWindow();
    }

    /**
     * Sets proper color of text pattern.
     */
    private void updateTextPatternColor() {
        boolean wasInvalid = invalidTextPattern;
        invalidTextPattern = searchCriteria.isTextPatternInvalid();
        if (invalidTextPattern != wasInvalid) {
            Color dfltColor = getDefaultTextColor(); // need to be here to init
            textToFindEditor.setForeground(
                    invalidTextPattern ? getErrorTextColor()
                    : dfltColor);
        }
    }

    /**
     * Sets proper color of file pattern.
     */
    private void updateFileNamePatternColor() {
        boolean wasInvalid = invalidFileNamePattern;
        invalidFileNamePattern = searchCriteria.isFileNamePatternInvalid();
        if (invalidFileNamePattern != wasInvalid) {
            Color dfltColor = getDefaultTextColor(); // need to be here to init
            fileNamePatternEditor.setForeground(
                    invalidFileNamePattern ? getErrorTextColor()
                    : dfltColor);
        }
    }

    private Color getDefaultTextColor() {
        if (defaultTextColor == null) {
            defaultTextColor = textToFindEditor.getForeground();
        }
        return defaultTextColor;
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
                defaultTextColor = textToFindEditor.getForeground();
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
            searchCriteria.setRegexp(selected);
            updateTextPatternColor();
            if (cboxReplacement != null){
                updateReplacePatternColor();
            }
            setTextToFindToolTip();
        } else if (toggle == chkCaseSensitive) {
            searchCriteria.setCaseSensitive(selected);
        } else if (toggle == chkWholeWords) {
            searchCriteria.setWholeWords(selected);
        } else if (toggle == chkPreserveCase) {
            searchCriteria.setPreserveCase(selected);
        } else if (toggle == chkArchives) {
            searchCriteria.setSearchInArchives(selected);
        } else if (toggle == chkGenerated) {
            searchCriteria.setSearchInGenerated(selected);
        } else if (toggle == chkFileNameRegex) {
            searchCriteria.setFileNameRegexp(selected);
            updateFileNamePatternColor();
            setFileNamePatternToolTip();
        } else if (toggle == chkUseIgnoreList) {
            searchCriteria.setUseIgnoreList(selected);
        } else {
            assert false;
        }
    }

    private void setTextToFindToolTip() {
        String t;
        if (searchCriteria.isRegexp()) {
            t = null;
        } else {
            t = getText("BasicSearchForm.cboxTextToFind.tooltip");      //NOI18N
        }
        cboxTextToFind.setToolTipText(t);
    }

    private void setFileNamePatternToolTip() {
        String t;
        if (searchCriteria.isFileNameRegexp()) {
            t = null;
        } else {
            t = getText("BasicSearchForm.cboxFileNamePattern.tooltip"); //NOI18N
        }
        cboxFileNamePattern.setToolTipText(t);
    }

    /**
     * Moves the node selection search scope to the last position.
     * The implementation assumes that the node selection search scope
     * is the first search scope among all registered search scopes.
     */
    private Collection<Map.Entry<SearchScope, Boolean>> orderSearchScopes() {
        Collection<Map.Entry<SearchScope, Boolean>> currentCollection
                = searchScopes.entrySet();
        
        if (currentCollection.isEmpty() || (currentCollection.size() == 1)) {
            return currentCollection;
        }
        
        Collection<Map.Entry<SearchScope, Boolean>> newCollection
                = new ArrayList<Map.Entry<SearchScope, Boolean>>(currentCollection.size());
        Map.Entry<SearchScope, Boolean> firstEntry = null;
        for (Map.Entry<SearchScope, Boolean> entry : currentCollection) {
            if (firstEntry == null) {
                firstEntry = entry;
            } else {
                newCollection.add(entry);
            }
        }
        newCollection.add(firstEntry);
        return newCollection;
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
            memory.storeFileNamePattern(fileNamePatternEditor.getText());
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
            memory.setSearchInArchives(chkArchives.isSelected());
            memory.setSearchInGenerated(chkGenerated.isSelected());
        }
        memory.setFilePathRegex(chkFileNameRegex.isSelected());
        memory.setUseIgnoreList(chkUseIgnoreList.isSelected());
    }

    /**
     * Read current dialog contents as a SearchPattern.
     *
     * @return SearchPattern for the contents of the current dialog. Null if the
     * * search string is empty, meaning that the dialog is empty.
     */
    private SearchPattern getCurrentSearchPattern() {
        return SearchPattern.create(textToFindEditor.getText(),
                                    chkWholeWords.isSelected(),
                                    chkCaseSensitive.isSelected(),
                                    chkRegexp.isSelected());
    }
    
    /**
     */
    SearchScope getSelectedSearchScope() {
        assert selectedSearchScope != null;
        return selectedSearchScope;
    }
    
    /** */
    BasicSearchCriteria getBasicSearchCriteria() {
        return searchCriteria;
    }
    
    boolean isUsable() {
        return (selectedSearchScope != null)
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
        lclz(chkFileNameRegex, "BasicSearchForm.chkFileNameRegex.text");//NOI18N
        btnTestTextToFind.setText(getHtmlLink(
                "BasicSearchForm.btnTestTextToFind.text"));             //NOI18N
        btnTestFileNamePattern.setText(getHtmlLink(
                "BasicSearchForm.btnTestFileNamePattern.text"));        //NOI18N
        btnEditIgnoreList.setText(
                getHtmlLink("BasicSearchForm.btnEditIgnoreList.text")); //NOI18N
        lclz(chkUseIgnoreList, "BasicSearchForm.chkUseIgnoreList.text");//NOI18N

        if (searchAndReplace) {
            lclz(lblReplacement, "BasicSearchForm.lblReplacement.text");//NOI18N
            lclz(chkPreserveCase,
                    "BasicSearchForm.chkPreserveCase.text");            //NOI18N
        } else {
            lclz(chkArchives, "BasicSearchForm.chkArchives.text");      //NOI18N
            lclz(chkGenerated, "BasicSearchForm.chkGenerated.text");    //NOI18N
        }
        setTextToFindToolTip();
        setFileNamePatternToolTip();
    }

    /**
     * Convenience method for setting localized text and mnemonics of buttons.
     */
    private void lclz(AbstractButton obj, String key) {
        Mnemonics.setLocalizedText(obj, getText(key));
    }

    /**
     * Convenience method for setting localized text and mnemonics of labels
     */
    private void lclz(JLabel obj, String key) {
        Mnemonics.setLocalizedText(obj, getText(key));
    }
    
    /**
     * Listener that selects all text in a text field when the text field
     * gains permanent focus.
     */
    private static class TextFieldFocusListener implements FocusListener {

        @Override
        public void focusGained(FocusEvent e) {
            if (!e.isTemporary()) {
                JTextComponent textComp = (JTextComponent) e.getSource();
                if (textComp.getText().length() != 0) {
                    textComp.selectAll();
                }
            }
        }

        @Override
        public void focusLost(FocusEvent e) {
            /* do nothing */
        }

    }

    private static final Logger watcherLogger = Logger.getLogger(
            "org.netbeans.modules.search.BasicSearchForm.FileNamePatternWatcher");//NOI18N
        
    /**
     * Extension of the {@code TextFieldFocusListener}
     * - besides selecting of all text upon focus gain,
     * it displays &quot;(no files)&quot; if no file name pattern is specified.
     * 
     * @author  Marian Petras
     */
    private final class FileNamePatternWatcher extends TextFieldFocusListener
                                               implements HierarchyListener {
        
        private final JTextComponent txtComp;
        private final Document doc;
        
        private Color foregroundColor;
        private String infoText;
        private boolean infoDisplayed;
        
        private FileNamePatternWatcher(JTextComponent txtComp) {
            this.txtComp = txtComp;
            doc = txtComp.getDocument();
        }
        
        @Override
        public void hierarchyChanged(HierarchyEvent e) {
            if ((e.getComponent() != txtComp)
                    || ((e.getChangeFlags() & DISPLAYABILITY_CHANGED) == 0)
                    || !txtComp.isDisplayable()) {
                return;
            }
            
            watcherLogger.finer("componentShown()");                    //NOI18N
            if (foregroundColor == null) {
                foregroundColor = txtComp.getForeground();
            }
            if ((doc.getLength() == 0) && !txtComp.isFocusOwner()) {
                displayInfo();
            }
        }
        
        @Override
        public void focusGained(FocusEvent e) {

            /*
             * Order of method calls hideInfo() and super.focusGained(e)
             * is important! See bug #113202.
             */

            if (infoDisplayed) {
                hideInfo();
            }
            super.focusGained(e);   //selects all text
        }

        @Override
        public void focusLost(FocusEvent e) {
            super.focusLost(e);     //does nothing
            if (isEmptyText()) {
                displayInfo();
            }
        }
        
        private boolean isEmptyText() {
            int length = doc.getLength();
            if (length == 0) {
                return true;
            }
            
            String text;
            try {
                text = doc.getText(0, length);
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
                text = null;
            }
            return (text != null) && (text.trim().length() == 0);
        }
        
        private void displayInfo() {
            assert ((doc.getLength() == 0) && !txtComp.isFocusOwner());
            watcherLogger.finer("displayInfo()");                       //NOI18N
            
            try {
                txtComp.setForeground(txtComp.getDisabledTextColor());
                
                ignoreFileNamePatternChanges = true;
                doc.insertString(0, getInfoText(), null);
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            } finally {
                ignoreFileNamePatternChanges = false;
                infoDisplayed = true;
            }
        }
        
        private void hideInfo() {
            watcherLogger.finer("hideInfo()");                          //NOI18N
            
            txtComp.setEnabled(true);
            try {
                ignoreFileNamePatternChanges = true;
                if (doc.getText(0, doc.getLength()).equals(getInfoText())) {
                    doc.remove(0, doc.getLength());
                }
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            } finally {
                ignoreFileNamePatternChanges = false;
                txtComp.setForeground(foregroundColor);
                infoDisplayed = false;
            }
        }
        
        private String getInfoText() {
            if (infoText == null) {
                infoText = NbBundle.getMessage(
                        getClass(),
                        "BasicSearchForm.cboxFileNamePattern.allFiles");//NOI18N
            }
            return infoText;
        }
        
    }
    
    private String getText(String bundleKey) {
        return NbBundle.getMessage(getClass(), bundleKey);
    }

    private String getHtmlLink(String key) {
        return HTML_LINK_PREFIX + getText(key) + HTML_LINK_SUFFIX;
    }

    private JComboBox cboxTextToFind;
    private JComboBox cboxReplacement;
    private JComboBox cboxFileNamePattern;
    private JCheckBox chkWholeWords;
    private JCheckBox chkCaseSensitive;
    private JCheckBox chkRegexp;
    private JCheckBox chkPreserveCase;
    private JTextComponent textToFindEditor;
    private JTextComponent fileNamePatternEditor;
    private JTextComponent replacementPatternEditor;
    protected SearchFormPanel formPanel;
    private JButton btnEditIgnoreList;
    protected JPanel ignoreListOptionPanel;
    protected JCheckBox chkUseIgnoreList;
    private JCheckBox chkFileNameRegex;
    private JButton btnTestTextToFind;
    private JButton btnTestFileNamePattern;
    private JLabel lblTextToFind;
    private JComboBox cboxScope;
    private JLabel lblFileNamePattern;
    private JCheckBox chkArchives;
    private JCheckBox chkGenerated;
    private JLabel lblScope;
    private JLabel lblReplacement;
    private FileNamePatternWatcher fileNamePatternWatcher;

    private Color errorTextColor, defaultTextColor;
    private boolean invalidTextPattern = false;
    private boolean invalidReplacePattern = false;
    private boolean invalidFileNamePattern = false;
    
    /**
     * When set to {@link true}, changes of file name pattern are ignored.
     * This is needed when the text in the file name pattern is programatically
     * (i.e. not by the user) set to "(all files)" and when this text is
     * cleared (when the text field gets focus).
     */
    private boolean ignoreFileNamePatternChanges = false;

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
     * Panel for a checkbox and a button that is enbled if and only if the
     * checkbox is selected.
     */
    private class CheckBoxWithButtonPanel extends JPanel
            implements ItemListener {

        private JCheckBox checkbox;
        private JButton button;
        private JLabel leftParenthesis;
        private JLabel rightParenthesis;
        private String enabledText;
        private String disabledText;

        /**
         * Constructor.
         *
         *  * The text of the button must be already set.
         *
         * @param checkbox
         * @param button
         */
        public CheckBoxWithButtonPanel(JCheckBox checkbox, JButton button) {
            this.checkbox = checkbox;
            this.button = button;
            initTexts();
            init();
        }

        /**
         * Init panel and helper elements.
         */
        private void init() {
            this.setLayout(new FlowLayout(
                    FlowLayout.LEADING, 0, 0));
            this.add(checkbox);
            setLinkLikeButton(button);
            leftParenthesis = new JLabel("(");                         // NOI18N
            rightParenthesis = new JLabel(")");                         //NOI18N
            add(leftParenthesis);
            add(button);
            add(rightParenthesis);
            MouseListener ml = createLabelMouseListener();
            leftParenthesis.addMouseListener(ml);
            rightParenthesis.addMouseListener(ml);
            button.setEnabled(false);

            this.setMaximumSize(
                    this.getMinimumSize());
            checkbox.addItemListener(this);
            if (checkbox.isSelected()) {
                enableButton();
            } else {
                disableButton();
            }
        }

        /**
         * Init values of enabled and disabled button texts.
         */
        private void initTexts() {
            enabledText = button.getText();
            if (enabledText.startsWith(HTML_LINK_PREFIX)
                    && enabledText.endsWith(HTML_LINK_SUFFIX)) {
                disabledText = enabledText.substring(HTML_LINK_PREFIX.length(),
                        enabledText.length() - HTML_LINK_SUFFIX.length());
            } else {
                disabledText = enabledText;
            }
        }

        /**
         * Create listener that delegates mouse clicks on parenthesis to the
         * button.
         */
        private MouseListener createLabelMouseListener() {
            return new MouseAdapter() {

                @Override
                public void mouseClicked(MouseEvent e) {
                    if (button.isEnabled()) {
                        for (ActionListener al : button.getActionListeners()) {
                            al.actionPerformed(null);
                        }
                    }
                }
            };
        }

        /**
         * Set button border and background to look like a label with link.
         */
        private void setLinkLikeButton(JButton button) {
            button.setBorderPainted(false);
            button.setContentAreaFilled(false);
            button.setBorder(new EmptyBorder(0, 0, 0, 0));
            button.setCursor(Cursor.getPredefinedCursor(
                    Cursor.HAND_CURSOR));
        }

        @Override
        public void itemStateChanged(ItemEvent e) {
            if (checkbox.isSelected()) {
                enableButton();
            } else {
                disableButton();
            }
        }

        /**
         * Enable button and parentheses around it.
         */
        private void enableButton() {
            button.setText(enabledText);
            button.setEnabled(true);
            leftParenthesis.setCursor(
                    Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            rightParenthesis.setCursor(
                    Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            leftParenthesis.setEnabled(true);
            rightParenthesis.setEnabled(true);
        }

        /**
         * Disable button and parentheses around it.
         */
        private void disableButton() {
            button.setText(disabledText);
            button.setEnabled(false);
            leftParenthesis.setCursor(Cursor.getDefaultCursor());
            rightParenthesis.setCursor(Cursor.getDefaultCursor());
            leftParenthesis.setEnabled(false);
            rightParenthesis.setEnabled(false);
        }
    }

    /**
     * Wrapper of scope to be used as JComboBox item.
     */
    private final class ScopeItem {

        private static final String START = "(";                       // NOI18N
        private static final String END = ")";                         // NOI18N
        private static final String SP = " ";                          // NOI18N
        private static final String ELLIPSIS = "...";                  // NOI18N
        private static final int MAX_EXTRA_INFO_LEN = 20;
        private SearchScope searchScope;

        public ScopeItem(SearchScope searchScope) {
            this.searchScope = searchScope;
        }

        public SearchScope getSearchScope() {
            return this.searchScope;
        }

        private boolean isAdditionaInfoAvailable() {
            return searchScope.getAdditionalInfo() != null
                    && searchScope.getAdditionalInfo().length() > 0;
        }

        private String getTextForLabel(String text) {
            String extraInfo = searchScope.getAdditionalInfo();
            String extraText = extraInfo;
            if (extraInfo.length() > MAX_EXTRA_INFO_LEN) {
                extraText = extraInfo.substring(0, MAX_EXTRA_INFO_LEN)
                        + ELLIPSIS;
                if (extraText.length() >= extraInfo.length()) {
                    extraText = extraInfo;
                }
            }
            return getFullText(text, extraText);
        }

        private String getFullText(String text, String extraText) {
            return text + SP + START + SP + extraText + SP + END;
        }

        @Override
        public String toString() {
            if (isAdditionaInfoAvailable()) {
                return getTextForLabel(clr(searchScope.getDisplayName()));
            } else {
                return clr(searchScope.getDisplayName());
            }
        }

        /**
         * Clear some legacy special characters from scope names.
         *
         * Some providers can still include ampresands that were used for
         * mnemonics in previous versions, but now are ignored.
         */
        private String clr(String s) {
            return s.replaceAll("\\&", "");                             //NOI18N
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

    /**
     * Listener to changes of pattern combo boxes.
     */
    private class PatternChangeListener implements DocumentListener {

        private final JComboBox sourceComboBox;

        PatternChangeListener(JComboBox srcCBox) {
            this.sourceComboBox = srcCBox;
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            update(e);
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            update(e);
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            update(e);
        }

        private void update(DocumentEvent e) {
            if ((sourceComboBox == cboxFileNamePattern)
                    && ignoreFileNamePatternChanges) {
                return;
            }

            final Document doc = e.getDocument();

            String text;
            try {
                text = doc.getText(0, doc.getLength());
            } catch (BadLocationException ex) {
                assert false;
                ErrorManager.getDefault().notify(ErrorManager.ERROR, ex);
                text = "";                                          //NOI18N
            }
            handleComboBoxChange(text);
        }

        private void handleComboBoxChange(String text) {
            if (sourceComboBox == cboxTextToFind) {
                searchCriteria.setTextPattern(text);
                updateTextPatternColor();
                if (cboxReplacement != null) {
                    updateReplacePatternColor();
                }
            } else if (sourceComboBox == cboxFileNamePattern) {
                searchCriteria.setFileNamePattern(text);
                updateFileNamePatternColor();
            } else {
                assert sourceComboBox == cboxReplacement;
                searchCriteria.setReplaceExpr(text);
                if (cboxReplacement != null) {
                    updateReplacePatternColor();
                }
            }
        }
    }
}
