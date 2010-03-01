/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.LayoutStyle;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.ItemSelectable;
import java.awt.SystemColor;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.openide.ErrorManager;
import org.openide.awt.Mnemonics;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openidex.search.SearchHistory;
import org.openidex.search.SearchPattern;
import static java.awt.event.HierarchyEvent.DISPLAYABILITY_CHANGED;

/**
 *
 * @author  Marian Petras
 */
final class BasicSearchForm extends JPanel implements ChangeListener,
                                                      ItemListener {
    
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
        initAccessibility();
        initHistory();
        initInteraction();

        if (searchAndReplace && (searchCriteria.getReplaceExpr() == null)) {
            /* We must set the initial replace string, otherwise it might not
             * be initialized at all if the user keeps the field "Replace With:"
             * empty. One of the side-effects would be that method
             * BasicSearchCriteria.isSearchAndReplace() would return 'false'. */
            searchCriteria.setReplaceExpr("");                        //NOI18N
        }

        /*
         * Interaction must be already set up when we set values, otherwise
         * state of the dialog might not be corresponding to the values,
         * e.g. the Find dialog could be disabled although valid values
         * are entered.
         */
        if (usePreviousValues) {
            initPreviousValues();
        } else {
            initValuesFromHistory();
        }
        updateTextPatternColor();
        if (searchAndReplace){
            updateReplacePatternColor();
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
        JLabel lblTextToFind = new JLabel();
        cboxTextToFind = new JComboBox();
        lblTextToFind.setLabelFor(cboxTextToFind);
        lblHintTextToFind = new JLabel();
        lblHintTextToFind.setMinimumSize(new Dimension(0, 0));
        
        JLabel lblReplacement;
	if (searchAndReplace) {
            lblReplacement = new JLabel();
            cboxReplacement = new JComboBox();
            cboxReplacement.getAccessibleContext().setAccessibleDescription(getText("BasicSearchForm.cbox.Replacement.AccessibleDescription"));
            lblReplacement.setLabelFor(cboxReplacement);
	} else {
            lblReplacement = null;
            cboxReplacement = null;
        }
        
        JLabel lblFileNamePattern = new JLabel();
        cboxFileNamePattern = new JComboBox();
        lblFileNamePattern.setLabelFor(cboxFileNamePattern);
        JLabel lblHintFileNamePattern = new JLabel();
        
        chkWholeWords = new JCheckBox();
        chkCaseSensitive = new JCheckBox();
        chkRegexp = new JCheckBox();
        
        Mnemonics.setLocalizedText(
                lblTextToFind,
                getText("BasicSearchForm.lblTextToFind.text"));         //NOI18N
        lblHintTextToFind.setText(
                getText("BasicSearchForm.lblHintTextToFind.text"));     //NOI18N
        lblHintTextToFind.setForeground(SystemColor.textInactiveText);
        cboxTextToFind.setEditable(true);
        
        if (searchAndReplace) {
            Mnemonics.setLocalizedText(
                    lblReplacement,
                    getText("BasicSearchForm.lblReplacement.text"));        //NOI18N
            cboxReplacement.setEditable(true);
        }

        Mnemonics.setLocalizedText(
                lblFileNamePattern,
                getText("BasicSearchForm.lblFileNamePattern.text"));    //NOI18N
        lblHintFileNamePattern.setText(
                getText("BasicSearchForm.lblHintFileNamePattern.text"));//NOI18N
        lblHintFileNamePattern.setForeground(SystemColor.textInactiveText);
        cboxFileNamePattern.setEditable(true);
        
        Mnemonics.setLocalizedText(
                chkWholeWords,
                getText("BasicSearchForm.chkWholeWords.text"));         //NOI18N

        Mnemonics.setLocalizedText(
                chkCaseSensitive,
                getText("BasicSearchForm.chkCaseSensitive.text"));      //NOI18N

        Mnemonics.setLocalizedText(
                chkRegexp,
                getText("BasicSearchForm.chkRegexp.text"));             //NOI18N

        JComponent optionsPanel
                = createButtonsPanel("LBL_OptionsPanelTitle",           //NOI18N
                                     chkWholeWords,
                                     chkCaseSensitive,
                                     chkRegexp);
        JComponent scopePanel
                = createButtonsPanel("LBL_ScopePanelTitle",             //NOI18N
                                     createSearchScopeButtons());
        
        GridLayout lowerPanelLayout = new GridLayout(1, 0);
        JComponent lowerPanel = new JPanel(lowerPanelLayout);
        lowerPanel.add(optionsPanel);
        lowerPanel.add(scopePanel);
        lowerPanelLayout.setHgap(
                LayoutStyle.getInstance()
                .getPreferredGap(optionsPanel,  
                                 scopePanel,
                                 ComponentPlacement.UNRELATED,
                                 SwingConstants.EAST,
                                 null));

        GroupLayout criteriaPanelLayout = new GroupLayout(this);
        setLayout(criteriaPanelLayout);
        criteriaPanelLayout.setHonorsVisibility(false);
        criteriaPanelLayout.setHorizontalGroup(
            criteriaPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(criteriaPanelLayout.createParallelGroup(Alignment.LEADING)
                    .addGroup(criteriaPanelLayout.createSequentialGroup()
                        .addGroup(createParallelGroup(criteriaPanelLayout, Alignment.LEADING,
                                                 lblTextToFind,
                                                 lblReplacement,
                                                 lblFileNamePattern))
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addGroup(createParallelGroup(criteriaPanelLayout, Alignment.LEADING,
                                                 lblHintFileNamePattern,
                                                 lblHintTextToFind,
                                                 cboxTextToFind,
                                                 cboxReplacement,
                                                 cboxFileNamePattern)))
                    .addComponent(lowerPanel))
                .addContainerGap()
        );

        SequentialGroup seqGroup = criteriaPanelLayout.createSequentialGroup();
        seqGroup.addContainerGap()
                .addGroup(criteriaPanelLayout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(lblTextToFind)
                    .addComponent(cboxTextToFind, GroupLayout.PREFERRED_SIZE,
                                         GroupLayout.DEFAULT_SIZE,
                                         GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(lblHintTextToFind);
        if (cboxReplacement != null) {
            seqGroup.addPreferredGap(ComponentPlacement.UNRELATED)
                    .addGroup(criteriaPanelLayout.createParallelGroup(Alignment.BASELINE)
                        .addComponent(lblReplacement)
                        .addComponent(cboxReplacement, GroupLayout.PREFERRED_SIZE,
                                              GroupLayout.DEFAULT_SIZE,
                                              GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(ComponentPlacement.RELATED);
        }
        seqGroup.addPreferredGap(ComponentPlacement.UNRELATED)
                .addGroup(criteriaPanelLayout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(lblFileNamePattern)
                    .addComponent(cboxFileNamePattern, GroupLayout.PREFERRED_SIZE,
                                              GroupLayout.DEFAULT_SIZE,
                                              GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(lblHintFileNamePattern)
                .addPreferredGap(ComponentPlacement.UNRELATED)
                .addComponent(lowerPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addContainerGap();
        criteriaPanelLayout.setVerticalGroup(seqGroup);
        
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
    }

    /**
     * Creates a {@code ParallelGroup} and adds the given components to it,
     * skipping {@code null} components.
     * Calling
     * <blockquote><pre><code>createParallelGroup(groupLayout, alignment,
     *                     component1,
     *                     component2,
     *                     component3,
     *                     ...
     *                     component<i>n</i>)</code></pre></blockquote>
     * is equivalent to calling
     * <blockquote><pre><code>groupLayout.createParallelGroup(alignment)
     *      .addComponent(component1)
     *      .addComponent(component2)
     *      .addComponent(component3)
     *      ...
     *      .addComponent(component<i>n</i>)</code></pre></blockquote>
     * except that {@code null} components are skipped and {@code JComboBox}
     * components are automatically added with size constraints
     * {@code (0, 300, Short.MAX_VALUE)}.
     */
    private static ParallelGroup createParallelGroup(GroupLayout groupLayout,
                                                     Alignment alignment,
                                                     Component... components) {
        ParallelGroup group = groupLayout.createParallelGroup(alignment);
        for (Component c : components) {
            if (c == null) {
                continue;
            }

            if (c.getClass() == JComboBox.class) {
                group.addComponent(c, 0, 300, Short.MAX_VALUE);
            } else {
                group.addComponent(c);
            }
        }
        return group;
    }

    /**
     */
    private void initAccessibility() {
        chkCaseSensitive.getAccessibleContext().setAccessibleDescription(getText("BasicSearchForm.chkCaseSensitive.AccessibleDescription"));
        chkRegexp.getAccessibleContext().setAccessibleDescription(getText("BasicSearchForm.chkRegexp.AccessibleDescription"));
        chkWholeWords.getAccessibleContext().setAccessibleDescription(getText("BasicSearchForm.chkWholeWords.AccessibleDescription"));
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
    }
    
    /**
     */
    private void initInteraction() {
        /* set up updating of the validity status: */
        class PatternChangeListener implements DocumentListener {
            private final JComboBox sourceComboBox;
            PatternChangeListener(JComboBox srcCBox) {
                this.sourceComboBox = srcCBox;
            }
            public void insertUpdate(DocumentEvent e) {
                update(e);
            }
            public void removeUpdate(DocumentEvent e) {
                update(e);
            }
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
                
                if (sourceComboBox == cboxTextToFind) {
                    searchCriteria.setTextPattern(text);
                    updateTextPatternColor();
                    if (cboxReplacement != null){
                        updateReplacePatternColor();
                    }
                } else if (sourceComboBox == cboxFileNamePattern) {
                    searchCriteria.setFileNamePattern(text);
                } else {
                    assert sourceComboBox == cboxReplacement;
                    searchCriteria.setReplaceExpr(text);
                    if (cboxReplacement != null){
                        updateReplacePatternColor();
                    }
                }
            }
        }

        final TextFieldFocusListener focusListener = new TextFieldFocusListener();
        textToFindEditor.addFocusListener(focusListener);
        if (replacementPatternEditor != null) {
            replacementPatternEditor.addFocusListener(focusListener);
        }

        final FileNamePatternWatcher watcher = new FileNamePatternWatcher(fileNamePatternEditor);
        fileNamePatternEditor.addFocusListener(watcher);
        fileNamePatternEditor.addHierarchyListener(watcher);
        
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

        boolean regexp = chkRegexp.isSelected();
        chkWholeWords.setEnabled(!regexp);

        searchCriteria.setUsabilityChangeListener(this);
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
                String searchExpression = pattern.getSearchExpression();
                if (!itemsList.contains(searchExpression)) {
                    itemsList.add(searchExpression);
                }
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
    private void initValuesFromHistory() {
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
            if (defaultTextColor == null) {
                assert !wasInvalid;
                defaultTextColor = textToFindEditor.getForeground();
            }
            textToFindEditor.setForeground(
                    invalidTextPattern ? getErrorTextColor()
                                       : defaultTextColor);
        }
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
    public void itemStateChanged(ItemEvent e) {
        final ItemSelectable toggle = e.getItemSelectable();
        final boolean selected = (e.getStateChange() == ItemEvent.SELECTED);
        if (toggle == chkRegexp) {
            searchCriteria.setRegexp(selected);
            updateTextPatternColor();
            if (cboxReplacement != null){
                updateReplacePatternColor();
            }
            chkWholeWords.setEnabled(!selected);
            lblHintTextToFind.setVisible(!selected);
        } else if (toggle == chkCaseSensitive) {
            searchCriteria.setCaseSensitive(selected);
        } else if (toggle == chkWholeWords) {
            searchCriteria.setWholeWords(selected);
        } else {
            assert false;
        }
    }

    /**
     */
    private AbstractButton[] createSearchScopeButtons() {
        radioBtnGroup = new ButtonGroup();
        
        ItemListener buttonStateListener = new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    AbstractButton selectedButton = (AbstractButton) e.getSource();
                    Object storedObject = selectedButton.getClientProperty("searchScope");
                    selectedSearchScope = (SearchScope) storedObject;
                }
            }
        };

        AbstractButton[] result = new AbstractButton[searchScopes.size()];
        int index = 0;
        
        boolean preferredScopeSelected = false;
        int firstEnabled = -1;
        for (Map.Entry<SearchScope, Boolean> entry : orderSearchScopes()) {
            SearchScope searchScope = entry.getKey();
            boolean enabled = entry.getValue();
            String searchScopeInfo = enabled ? searchScope.getAdditionalInfo()
                                             : null;
            AbstractButton button = (searchScopeInfo == null)
                                    ? new JRadioButton()
                                    : new ButtonWithExtraInfo(searchScopeInfo);
            Mnemonics.setLocalizedText(button, searchScope.getDisplayName());
            button.getAccessibleContext().setAccessibleDescription(searchScope.getDisplayName());
            button.putClientProperty("searchScope", searchScope);
            button.addItemListener(buttonStateListener);

            button.setEnabled(enabled);
            if (enabled) {
                if (searchScope.getTypeId().equals(preferredSearchScopeType)) {
                    button.setSelected(true);
                    preferredScopeSelected = true;
                } else if (firstEnabled == -1) {
                    firstEnabled = index;
                }
            }
            result[index++] = button;
            
            radioBtnGroup.add(button);
        }
        
        if (!preferredScopeSelected && (firstEnabled != -1)) {
            result[firstEnabled].setSelected(true);
        }
        return result;
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
     */
    private JComponent createButtonsPanel(String borderTitleBundleKey,
                                          AbstractButton... buttons) {
        JComponent buttonsPanel = new JPanel();
        GroupLayout buttonsPanelLayout = new GroupLayout(buttonsPanel);
        buttonsPanel.setLayout(buttonsPanelLayout);
        
        ParallelGroup parallelGroup = buttonsPanelLayout.createParallelGroup();
        for (AbstractButton button : buttons) {
            if (button instanceof ButtonWithExtraInfo) {
                /*
                 * parallelGroup.addComponent(button) makes the button's maximum size
                 * equal to its preferred size. We need the button to expand
                 * horizontally so we set its maximum width to MAX_VALUE.
                 * If horizontal expanding is set on a button, it causes
                 * that the button passes the invalidate-validate cycle much
                 * more frequently - so we only set it on buttons which need it
                 * (i.e. on buttons with extra information available).
                 */
                parallelGroup.addComponent(button, GroupLayout.DEFAULT_SIZE,
                                          GroupLayout.PREFERRED_SIZE,
                                          Short.MAX_VALUE);
            } else {
                parallelGroup.addComponent(button);
            }
        }
        buttonsPanelLayout.setHorizontalGroup(parallelGroup);
        
        GroupLayout.SequentialGroup sequentialGroup = buttonsPanelLayout.createSequentialGroup();
        boolean first = true;
        for (AbstractButton button : buttons) {
            if (!first) {
                sequentialGroup.addPreferredGap(ComponentPlacement.RELATED);
            }
            sequentialGroup.addComponent(button);
            first = false;
        }
        buttonsPanelLayout.setVerticalGroup(sequentialGroup);
        
        buttonsPanelLayout.linkSize(buttons);
        
        buttonsPanel.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createTitledBorder(
                                getText(borderTitleBundleKey)),
                        BorderFactory.createEmptyBorder(3, 5, 5, 5)));
        
        return buttonsPanel;
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
    }

    /**
     * Read current dialog contents as a SearchPattern.
     * @return SearchPattern for the contents of the current dialog. Null if the     *         search string is empty, meaning that the dialog is empty.
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
    
    /**
     * Listener that selects all text in a text field when the text field
     * gains permanent focus.
     */
    private static class TextFieldFocusListener implements FocusListener {

        public void focusGained(FocusEvent e) {
            if (!e.isTemporary()) {
                JTextComponent textComp = (JTextComponent) e.getSource();
                if (textComp.getText().length() != 0) {
                    textComp.selectAll();
                }
            }
        }

        public void focusLost(FocusEvent e) {
            /* do nothing */
        }

    }

    /**
     * Extension of the {@code TextFieldFocusListener}
     * - besides selecting of all text upon focus gain,
     * it displays &quot;(no files)&quot; if no file name pattern is specified.
     * 
     * @author  Marian Petras
     */
    private final class FileNamePatternWatcher extends TextFieldFocusListener
                                               implements HierarchyListener {
        
        private final Logger watcherLogger = Logger.getLogger(
                "org.netbeans.modules.search.BasicSearchForm.FileNamePatternWatcher");//NOI18N
        
        private final JTextComponent txtComp;
        private final Document doc;
        
        private Color foregroundColor;
        private String infoText;
        private boolean infoDisplayed;
        
        private FileNamePatternWatcher(JTextComponent txtComp) {
            this.txtComp = txtComp;
            doc = txtComp.getDocument();
        }
        
        public void hierarchyChanged(HierarchyEvent e) {
            if ((e.getComponent() != txtComp)
                    || ((e.getChangeFlags() & DISPLAYABILITY_CHANGED) == 0)
                    || !txtComp.isDisplayable()) {
                return;
            }
            
            watcherLogger.finer("componentShown()");
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
            watcherLogger.finer("displayInfo()");
            
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
            watcherLogger.finer("hideInfo()");
            
            txtComp.setEnabled(true);
            try {
                ignoreFileNamePatternChanges = true;
                doc.remove(0, doc.getLength());
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
                        "BasicSearchForm.cboxFileNamePattern.allFiles");     //NOI18N
            }
            return infoText;
        }
        
    }
    
    private String getText(String bundleKey) {
        return NbBundle.getMessage(getClass(), bundleKey);
    }
    
    private ButtonGroup radioBtnGroup;
    private JComboBox cboxTextToFind;
    private JComboBox cboxReplacement;
    private JComboBox cboxFileNamePattern;
    private JCheckBox chkWholeWords;
    private JCheckBox chkCaseSensitive;
    private JCheckBox chkRegexp;
    private JTextComponent textToFindEditor;
    private JTextComponent fileNamePatternEditor;
    private JTextComponent replacementPatternEditor;
    private JLabel lblHintTextToFind;
    
    private Color errorTextColor, defaultTextColor;
    private boolean invalidTextPattern = false;
    private boolean invalidReplacePattern = false;
    
    /**
     * When set to {@link true}, changes of file name pattern are ignored.
     * This is needed when the text in the file name pattern is programatically
     * (i.e. not by the user) set to "(all files)" and when this text is
     * cleared (when the text field gets focus).
     */
    private boolean ignoreFileNamePatternChanges = false;

}
