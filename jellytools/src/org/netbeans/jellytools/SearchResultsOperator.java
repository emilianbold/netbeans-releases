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

package org.netbeans.jellytools;

import org.netbeans.jellytools.nodes.Node;

import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.Timeouts;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.Waiter;

import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.jemmy.operators.JRadioButtonOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.jemmy.operators.Operator;

/**
 * Provides access to the "Search Results" window. <br>
 * Timeouts used:<br>
 * SearchResultsOperator.SearchTime - maximum time for search to be performed.
 */
public class SearchResultsOperator extends TopComponentOperator {

    private static final long SEARCH_TIME = 600000;

    private static final String TITLE = Bundle.getString("org.netbeans.modules.search.Bundle",
                                                         "TEXT_TITLE_SEARCH_RESULTS");
    /** Used to temporary store default comparator */
    private static StringComparator oldComparator;
    private JButtonOperator _btStop;
    private JButtonOperator _btShowDetails;
    private JButtonOperator _btModifySearch;
    private JButtonOperator _btClose;
    private JButtonOperator _btShowInExplorer;
    private JButtonOperator _btHelp;
    private JTreeOperator _treeResult;
    private JListOperator _lstPositions;
    private JTextFieldOperator _txtFilesystem;
    private JTextFieldOperator _txtPath;
    private JRadioButtonOperator _rbSortByName;
    private JRadioButtonOperator _rbDoNotSort;

    /**
     * Waits for window opened.
     */
    public SearchResultsOperator() {
        super(TITLE);
    }

    static {
	Timeouts.initDefault("SearchResultsOperator.SearchTime", SEARCH_TIME);
    }

    //component access    
    /**
     * returns operator for "Stop Search" button.
     */
    public JButtonOperator btStopSearch() {
        if(_btStop == null) {
            _btStop = 
                new JButtonOperator(this, 
                                    Bundle.
                                    getStringTrimmed("org.netbeans.modules.search.Bundle",
                                                     "TEXT_BUTTON_STOP"));
        }
        return _btStop;
    }

    /**
     * Returns operator for "Show Details" button.
     */
    public JButtonOperator btShowDetails() {
        if(_btShowDetails == null) {
            _btShowDetails = 
                new JButtonOperator(this, 
                                    Bundle.
                                    getStringTrimmed("org.netbeans.modules.search.Bundle",
                                                     "TEXT_BUTTON_FILL"));
        }
        return _btShowDetails;
    }

    /**
     * Returns operator for "Modify Search" button.
     */
    public JButtonOperator btModifySearch() {
        if(_btModifySearch == null) {
            _btModifySearch = 
                new JButtonOperator(this, 
                                    Bundle.
                                    getStringTrimmed("org.netbeans.modules.search.Bundle",
                                                     "TEXT_BUTTON_CUSTOMIZE"));
        }
        return _btModifySearch;
    }

    /**
     * Returns operator for "Close" button.
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
     * Returns operator for "Help" button.
     */
    public JButtonOperator btHelp() {
        if(_btHelp == null) {
            _btHelp = 
                new JButtonOperator(this, 
                                    Bundle.
                                    getString("org.netbeans.core.actions.Bundle",
                                              "CTL_help_button"));
        }
        return _btHelp;
    }

    /**
     * Returns operator for search result tree.
     */
    public JTreeOperator treeResult() {
        if (_treeResult == null) {
            _treeResult = new JTreeOperator(this);
        }
        return _treeResult;
    }

    /**
     * Returns operator for position list.
     */
    public JListOperator lstPositions() {
        if (_lstPositions == null) {
            _lstPositions = new JListOperator(this);
        }
        return _lstPositions;
    }

    /**
     * Returns operator for "Show in Explorer" button.
     */
    public JButtonOperator btShowInExplorer() {
        if(_btShowInExplorer == null) {
            _btShowInExplorer = 
                new JButtonOperator(this, 
                                    Bundle.
                                    getStringTrimmed("org.netbeans.modules.search.Bundle",
                                                     "TEXT_BUTTON_SHOW"));
        }
        return _btShowInExplorer;
    }

    /**
     * Returns operator for filesystem text field.
     */
    public JTextFieldOperator txtFilesystem() {
        if (_txtFilesystem == null) {
            _txtFilesystem = new JTextFieldOperator(this, 0);
        }
        return _txtFilesystem;
    }

    /**
     * Returns operator for path text field.
     */
    public JTextFieldOperator txtPath() {
        if (_txtPath == null) {
            _txtPath = new JTextFieldOperator(this, 0);
        }
        return _txtPath;
    }

    /**
     * Returns operator for "Sort by Name" radio button.
     */
    public JRadioButtonOperator rbSortByName() {
        if(_rbSortByName == null) {
            _rbSortByName = 
                new JRadioButtonOperator(this, 
                                         Bundle.
                                         getString("org.netbeans.modules.search.Bundle",
                                                   "TEXT_BUTTON_SORT"));
        }
        return _rbSortByName;
    }

    /**
     * Returns operator for "Do Not Sort" radio button.
     */
    public JRadioButtonOperator rbDoNotSort() {
        if(_rbDoNotSort == null) {
            _rbDoNotSort = 
                new JRadioButtonOperator(this, 
                                         Bundle.
                                         getString("org.netbeans.modules.search.Bundle",
                                                   "TEXT_BUTTON_UNSORT"));
        }
        return _rbDoNotSort;
    }

    //shortcuts
    /**
     * Selects "Sort by Name" radio button.
     */
    public void sortByName() {
        rbSortByName().changeSelection(true);
    }

    /**
     * Selects "Do Not Sort" radio button.
     */
    public void doNotSort() {
        rbDoNotSort().changeSelection(true);
    }

    /**
     * Selects a path in the tree
     */
    public void selectResult(String treePath) {
        new Node(treeResult(), treePath).select();
    }

    /**
     * Pushes "Stop Search" button.
     */
    public void stopSearch() {
        btStopSearch().push();
    }

    /**
     * Pushes "Show Details" button and returns TermOperator from output window.
     */
    public TermOperator showDetails() {
        btShowDetails().push();
        return new TermOperator(TITLE);
    }

    /**
     * Pushes "Modify Search" button.
     */
    public SearchFilesystemsOperator modifySearch() {
        btModifySearch().pushNoBlock();
        return new SearchFilesystemsOperator();
    }

    /**
     * Pushes close button.
     */
    public void closeByButton() {
        btClose().push();
    }

    /**
     * Pushes help button.
     */
    public HelpOperator help() {
        btHelp().push();
        return(new HelpOperator());
    }

    /**
     * Pushes "Show in Explorer" button.
     */
    public void showInExplorer() {
        btShowInExplorer().push();
    }

    /**
     * Double clicks on position in list.
     */
    public EditorOperator showPosition(String position) {
        lstPositions().clickOnItem(position, 2);
        String selectedNode = treeResult().getSelectionPath().getLastPathComponent().toString();
        return new EditorOperator(selectedNode);
    }

    /**
     * Waits until search is finished.
     */
    public void waitEndOfSearch() {
        Waiter waiter = new Waiter(new Waitable() {
                public Object actionProduced(Object param) {
                    return(btStopSearch().isEnabled() ?
                           null : "");
                }
                public String getDescription() {
                    return "Wait for a search to be finished";
                }
            });
        waiter.getTimeouts().
            setTimeout("Waiter.WaitingTime", 
                       getTimeouts().
                       getTimeout("SearchResultsOperator.SearchTime"));
        try {
            waiter.waitAction(null);
        } catch(InterruptedException e) {
            throw(new JemmyException("Waiting has been interrupted", e));
        }
    }

    /** Performs verification by accessing all sub-components */    
    public void verify() {
        btStopSearch();
        btShowDetails();
        btModifySearch();
        btClose();
        btShowInExplorer();
        btHelp();
        treeResult();
        lstPositions();
        txtFilesystem();
        txtPath();
        rbSortByName();
        rbDoNotSort();
    }
}
