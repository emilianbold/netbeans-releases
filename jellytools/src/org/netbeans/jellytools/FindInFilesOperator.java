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

package org.netbeans.jellytools;

import org.netbeans.jellytools.actions.FindInFilesAction;
import org.netbeans.jellytools.nodes.Node;

import org.netbeans.jemmy.JemmyException;

import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.jemmy.operators.JRadioButtonOperator;
import org.netbeans.jemmy.operators.JTabbedPaneOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;

/**
 * Provides access to the Find in Files dialog.
 */
public class FindInFilesOperator extends NbDialogOperator {
    /** Full text page title */
    public static String FULL_TEXT_PAGE   = Bundle.getString("org.netbeans.modules.search.types.Bundle", 
                                                             "TEXT_FULLTEXT_CRITERION");
    /** Object name page title */
    public static String OBJECT_NAME_PAGE = Bundle.getString("org.netbeans.modules.search.types.Bundle", 
                                                             "TEXT_OBJECTNAME_CRITERION");
    /** Object type page title */
    public static String TYPE_PAGE        = Bundle.getString("org.netbeans.modules.search.types.Bundle",
                                                             "TEXT_OBJECTTYPE_CRITERION");
    /** Date page title */
    public static String DATE_PAGE        = Bundle.getString("org.netbeans.modules.search.types.Bundle",
                                                             "TEXT_DATE_CRITERION");

    private static final FindInFilesAction invokeAction = new FindInFilesAction();

    private JTabbedPaneOperator _tabbed;
    private JButtonOperator _btClose;
    private JButtonOperator _btSearch;

    /**
     * Waits for dialog displayed.
     */
    public FindInFilesOperator() {
        super(Bundle.getString("org.netbeans.modules.search.Bundle",
                               "TEXT_TITLE_CUSTOMIZE"));
    }

    /**
     * Invokes dialog by selecting a node and pushing menu.
     */
    public static FindInFilesOperator invoke(Node node) {
        invokeAction.perform(node);
        return new FindInFilesOperator();
    }

    //component access    
    /**
     * Returns an operator for a tabbed on the dialog.
     */
    public JTabbedPaneOperator tabbed() {
        if(_tabbed == null) {
            _tabbed = new JTabbedPaneOperator(this);
        }
        return(_tabbed);
    }

    /**
     * Returns operator for a close button.
     */
    public JButtonOperator btClose() {
        if(_btClose == null) {
            _btClose = 
                new JButtonOperator(this, 
                                    Bundle.
                                    getStringTrimmed("org.netbeans.modules.search.Bundle",
                                              "TEXT_BUTTON_CANCEL"));
        }
        return _btClose;
    }

    /**
     * Returns operator for a search button.
     */
    public JButtonOperator btSearch() {
        if(_btSearch == null) {
            _btSearch = 
                new JButtonOperator(this, 
                                    Bundle.
                                    getStringTrimmed("org.netbeans.modules.search.Bundle",
                                              "TEXT_BUTTON_SEARCH"));
        }
        return _btSearch;
    }

    /**
     * Returns operator for the "Use Criterion" check box.
     */
    public JCheckBoxOperator cbUseCriterion() {
        return new JCheckBoxOperator(this, 
                                     Bundle.getStringTrimmed("org.netbeans.modules.search.Bundle",
                                                      "TEXT_BUTTON_APPLY"));
    }

    /**
     * Returns operator for the "Save Settings as" button.
     */
    public JButtonOperator btSaveSetting() {
        return new JButtonOperator(this, 
                                   Bundle.getStringTrimmed("org.netbeans.modules.search.Bundle",
                                                           "TEXT_BUTTON_SAVE_AS"));
    }

    /**
     * Returns operator for the "Restore Saved" button.
     */
    public JButtonOperator btRestoreSaved() {
        return new JButtonOperator(this, 
                                   Bundle.getStringTrimmed("org.netbeans.modules.search.Bundle",
                                                           "TEXT_BUTTON_RESTORE"));
    }

    /**
     * Returns operator for the "Match Whole Words Only" check box.
     * @throws JemmyException when wrong page is selected
     */
    public JCheckBoxOperator cbWholeWords() {
        String text = Bundle.getStringTrimmed("org.netbeans.modules.search.types.Bundle",
                                       "TEXT_LABEL_WHOLE_WORDS");
        checkPage("\"" + text + "\" checkbox", new String[] {FULL_TEXT_PAGE, OBJECT_NAME_PAGE});
        return new JCheckBoxOperator(this, text);
    }

    /**
     * Returns operator for the "Match Case" check box.
     * @throws JemmyException when wrong page is selected
     */
    public JCheckBoxOperator cbCase() {
        String text = Bundle.getStringTrimmed("org.netbeans.modules.search.types.Bundle",
                                       "TEXT_LABEL_CASE_SENSITIVE");
        checkPage("\"" + text + "\" checkbox", new String[] {FULL_TEXT_PAGE, OBJECT_NAME_PAGE});
        return new JCheckBoxOperator(this, text);
    }

    /**
     * Returns operator for the "Regular Expression" check box.
     * @throws JemmyException when wrong page is selected
     */
    public JCheckBoxOperator cbRegExpr() {
        String text = Bundle.getStringTrimmed("org.netbeans.modules.search.types.Bundle",
                                              "TEXT_LABEL_RE");
        checkPage("\"" + text + "\" radio button", new String[] {FULL_TEXT_PAGE, OBJECT_NAME_PAGE});
        return new JCheckBoxOperator(this, text);
    }

    /**
     * Returns operator for the "Search for Text Containing:" text field.
     * @throws JemmyException when wrong page is selected
     */
    public JTextFieldOperator txtSearchText() {
        checkPage("Search text field", new String[] {FULL_TEXT_PAGE, OBJECT_NAME_PAGE});
        return new JTextFieldOperator(this);
    }

    /**
     * Returns operator for the types list.
     * @throws JemmyException when wrong page is selected
     */
    public JListOperator lstTypes() {
        checkPage("types list", new String[] {TYPE_PAGE});
        return new JListOperator(this);
    }

    /**
     * Returns operator for the "Within the Past" radio button.
     * @throws JemmyException when wrong page is selected
     */
    public JRadioButtonOperator rbPastDays() {
        String text = Bundle.getStringTrimmed("org.netbeans.modules.search.types.Bundle",
                                              "TEXT_BUTTON_PRECEDING");
        checkPage("\"" + text + "\" radio button", new String[] {DATE_PAGE});
        return new JRadioButtonOperator(this, text);
    }

    /**
     * Returns operator for the "Between" radio button.
     * @throws JemmyException when wrong page is selected
     */
    public JRadioButtonOperator rbBetween() {
        String text = Bundle.getStringTrimmed("org.netbeans.modules.search.types.Bundle",
                                              "TEXT_BUTTON_RANGE");
        checkPage("\"" + text + "\" radio button", new String[] {DATE_PAGE});
        return new JRadioButtonOperator(this, text);
    }

    /**
     * Returns operator for the "Within the Past" text field.
     * @throws JemmyException when wrong page is selected
     */
    public JTextFieldOperator txtPastDays() {
        checkPage("Past days text field", new String[] {DATE_PAGE});
        return new JTextFieldOperator(this);
    }

    /**
     * Returns operator for the first "Between" text field.
     * @throws JemmyException when wrong page is selected
     */
    public JTextFieldOperator txtBetweenStart() {
        checkPage("Between start text field", new String[] {DATE_PAGE});
        return new JTextFieldOperator(this, 1);
    }

    /**
     * Returns operator for the second "Between" text field.
     * @throws JemmyException when wrong page is selected
     */
    public JTextFieldOperator txtBetweenEnd() {
        checkPage("Between end text field", new String[] {DATE_PAGE});
        return new JTextFieldOperator(this, 2);
    }

    //shortcuts
    /**
     * Pushes close button.
     */
    public void close() {
        btClose().push();
    }

    /**
     * Pushes search button.
     * @return "Search Result" window.
     */
    public SearchResultsOperator search() {
        btSearch().push();
        SearchResultsOperator results = new SearchResultsOperator();
        results.waitEndOfSearch();
        return results;
    }

    //Low level
    /**
     * Selects one of the tabbed's pages.
     */
    public void selectPage(String pageTitle) {
        tabbed().selectPage(pageTitle);
    }

    /**
     * Changes "Use Cirterion" check box value.
     * @param use Value to be set.
     */
    public void useCriterion(boolean use) {
        cbUseCriterion().changeSelection(use);
    }

    /**
     * Saves settings under the <code>name</code> name on the active page.
     */
    public void saveSetting(String name) {
        useCriterion(true);
        btSaveSetting().pushNoBlock();
        JDialogOperator saveDialog = 
            new JDialogOperator(this, 
                                Bundle.getString("org.netbeans.modules.search.Bundle",
                                                 "TEXT_LABEL_SAVE_CRITERION"));
        JTextFieldOperator nameOper = new JTextFieldOperator(saveDialog);
        nameOper.clearText();
        nameOper.typeText(name);
        new JButtonOperator(saveDialog,
                            Bundle.getString("org.netbeans.core.windows.services.Bundle",
                                             "OK_OPTION_CAPTION")).
            push();
    }

    /**
     * Restores <code>name</code> settings for the active page.
     */
    public void restoreSaved(String name) {
        useCriterion(true);
        btRestoreSaved().pushNoBlock();
        JDialogOperator restoreDialog = 
            new JDialogOperator(this, 
                                Bundle.getString("org.netbeans.modules.search.Bundle",
                                                 "TEXT_LABEL_RESTORE_CRITERION"));
        new JComboBoxOperator(restoreDialog).selectItem(name);
        new JButtonOperator(restoreDialog,
                            Bundle.getString("org.netbeans.core.windows.services.Bundle",
                                             "OK_OPTION_CAPTION")).
            push();
    }

    /**
     * Sets substring search criterion on the active page.
     * @param substring Substring field value
     * @param wholeWords "Match Whole Words Only" check box value
     * @param caseSensitive "Match Case" check box value
     */
    public void setSubstring(String substring, boolean wholeWords, boolean caseSensitive) {
        checkPage("substring components", new String[] {FULL_TEXT_PAGE, OBJECT_NAME_PAGE});
        JTextFieldOperator field = txtSearchText();
        field.clearText();
        field.typeText(substring);
        cbWholeWords().changeSelection(wholeWords);
        cbCase().changeSelection(caseSensitive);
        useCriterion(true);
    }

    /**
     * Sets regexpr search criterion on the active page.
     * @param regexpr RegExpr field value
     */
    public void setRegExpr(String regexpr) {
        checkPage("regexpr components", new String[] {FULL_TEXT_PAGE, OBJECT_NAME_PAGE});
        cbRegExpr().changeSelection(true);
        JTextFieldOperator field = txtSearchText();
        field.clearText();
        field.typeText(regexpr);
        useCriterion(true);
    }

    //Full Text
    /**
     * Selects "Full Text" page.
     */
    public void selectFullTextPage() {
        selectPage(FULL_TEXT_PAGE);
    }

    /**
     * Sets full text substring search criterion.
     * @param substring Substring field value
     * @param wholeWords "Match Whole Words Only" check box value
     * @param caseSensitive "Match Case" check box value
     */
    public void setFullTextSubstring(String substring, boolean wholeWords, boolean caseSensitive) {
        selectFullTextPage();
        setSubstring(substring, wholeWords, caseSensitive);
    }

    /**
     * Sets full text regexpr search criterion.
     * @param regexpr RegExpr field value
     */
    public void setFullTextRegExpr(String regexpr) {
        selectFullTextPage();
        setRegExpr(regexpr);
    }

    /**
     * Restores <code>name</code> full text settings.
     */
    public void restoreFullText(String name) {
        selectFullTextPage();
        restoreSaved(name);
    }

    //Object Name
    /**
     * Selects "Object Name" page.
     */
    public void selectObjectNamePage() {
        selectPage(OBJECT_NAME_PAGE);
    }

    /**
     * Sets object name substring search criterion.
     * @param substring Substring field value
     * @param wholeWords "Match Whole Words Only" check box value
     * @param caseSensitive "Match Case" check box value
     */
    public void setObjectNameSubstring(String substring, boolean wholeWords, boolean caseSensitive) {
        selectObjectNamePage();
        setSubstring(substring, wholeWords, caseSensitive);
    }

    /**
     * Sets object name regexpr search criterion.
     * @param regexpr RegExpr field value
     */
    public void setObjectNameRegExpr(String regexpr) {
        selectObjectNamePage();
        setRegExpr(regexpr);
    }

    /**
     * Restores <code>name</code> object name settings.
     */
    public void restoreObjectName(String name) {
        selectObjectNamePage();
        restoreSaved(name);
    }

    //Type
    /**
     * Selects "Type" page.
     */
    public void selectTypePage() {
        selectPage(TYPE_PAGE);
    }

    /**
     * Select type in the list.
     */
    public void setType(String type) {
        selectTypePage();
        lstTypes().selectItem(type);
        useCriterion(true);
    }

    /**
     * Restores <code>name</code> type settings.
     */
    public void restoreType(String name) {
        selectTypePage();
        restoreSaved(name);
    }

    //Date
    /**
     * Selects "Date" page.
     */
    public void selectDatePage() {
        selectPage(DATE_PAGE);
    }

    /**
     * Sets criterion to search within past days.
     */
    public void setDatePastDays(int days) {
        selectDatePage();
        rbPastDays().changeSelection(true);
        JTextFieldOperator field = txtPastDays();
        field.clearText();
        field.typeText(Integer.toString(days));
        useCriterion(true);
    }

    /**
     * Sets criterion to search between dates.
     */
    public void setDateBetween(String startDate, String endDate) {
        selectDatePage();
        rbBetween().changeSelection(true);
        JTextFieldOperator field = txtBetweenStart();
        field.clearText();
        field.typeText(startDate);
        field = txtBetweenEnd();
        field.clearText();
        field.typeText(endDate);
        useCriterion(true);
    }

    /**
     * Restores <code>name</code> date settings.
     */
    public void restoreDate(String name) {
        selectDatePage();
        restoreSaved(name);
    }

    //High level
    /**
     * Unselect "Use Criterion" check box on all pages.
     */
    public void clearCriteria() {
        selectFullTextPage();
        useCriterion(false);
        selectObjectNamePage();
        useCriterion(false);
        selectTypePage();
        useCriterion(false);
        selectDatePage();
        useCriterion(false);
    }

    private void checkPage(String component, String[] pages) {
        String selected = tabbed().getTitleAt(tabbed().getSelectedIndex());
        for(int i = 0; i < pages.length; i++) {
            if(getComparator().equals(selected, pages[i])) {
                return;
            }
        }
        throw(new JemmyException("\"" + selected + "\" page does not have "+
                                 component + "!"));
    }

    /** Performs verification by accessing all sub-components */    
    public void verify() {
        btSearch();
        btClose();
        btHelp();
        selectFullTextPage();
        cbUseCriterion();
        btSaveSetting();
        btRestoreSaved();
        cbRegExpr();
        cbWholeWords();
        cbCase();
        txtSearchText();
        selectObjectNamePage();
        cbUseCriterion();
        btSaveSetting();
        btRestoreSaved();
        cbRegExpr();
        cbWholeWords();
        cbCase();
        txtSearchText();
        selectTypePage();
        cbUseCriterion();
        btSaveSetting();
        btRestoreSaved();
        lstTypes();
        selectDatePage();
        cbUseCriterion();
        btSaveSetting();
        btRestoreSaved();
        rbPastDays();
        rbBetween();
        txtPastDays();
        txtBetweenStart();
        txtBetweenEnd();
    }
}
