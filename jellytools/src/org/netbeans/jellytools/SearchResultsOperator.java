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

import org.netbeans.jellytools.nodes.Node;

import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.Timeouts;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.Waiter;

import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JRadioButtonOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.JTreeOperator;


/**
 * Provides access to the "Search Results" view. <p>
 * Usage:<br>
 * <pre>
 *      SearchResultsOperator sro = new SearchResultsOperator();
 *      sro.openResult("MyClass|myMethod");
 * </pre><p>
 * Timeouts used:<br>
 * SearchResultsOperator.SearchTime - maximum time for search to be performed.
 */
public class SearchResultsOperator extends TopComponentOperator {

    private static final long SEARCH_TIME = 600000;

    private static final String TITLE = Bundle.getString("org.netbeans.modules.search.Bundle",
                                                         "TITLE_SEARCH_RESULTS");
    /** Used to temporary store default comparator */
    private static StringComparator oldComparator;
    private JButtonOperator _btStop;
    private JButtonOperator _btShowDetails;
    private JButtonOperator _btModifySearch;
    private JTreeOperator _treeResult;
    private JRadioButtonOperator _rbSortByName;
    private JRadioButtonOperator _rbDoNotSort;

    /**
     * Waits for view opened.
     */
    public SearchResultsOperator() {
        // "Search Results"
        super(TITLE);
    }

    static {
	Timeouts.initDefault("SearchResultsOperator.SearchTime", SEARCH_TIME);
    }

    //component access    
    /**
     * returns operator for "Stop Search" button.
     * @return JButtonOperator instance
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
     * @return JButtonOperator instance
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
     * @return JButtonOperator instance
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
     * Returns operator for search result tree.
     * @return JTreeOperator instance
     */
    public JTreeOperator treeResult() {
        if (_treeResult == null) {
            _treeResult = new JTreeOperator(this);
        }
        return _treeResult;
    }

    /**
     * Returns operator for "Sort by Name" radio button.
     * @return JRadioButtonOperator instance
     */
    public JRadioButtonOperator rbSortByName() {
        if(_rbSortByName == null) {
            _rbSortByName = 
                new JRadioButtonOperator(this, 
                                         Bundle.
                                         getStringTrimmed("org.netbeans.modules.search.Bundle",
                                                          "TEXT_BUTTON_SORT"));
        }
        return _rbSortByName;
    }

    /**
     * Returns operator for "Do Not Sort" radio button.
     * @return JRadioButtonOperator instance
     */
    public JRadioButtonOperator rbDoNotSort() {
        if(_rbDoNotSort == null) {
            _rbDoNotSort = 
                new JRadioButtonOperator(this, 
                                         Bundle.
                                         getStringTrimmed("org.netbeans.modules.search.Bundle",
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
     * Selects a path in the results tree
     * @param path path to requested result (e.g. "MyClass|myMethod")
     */
    public void selectResult(String path) {
        new Node(treeResult(), path).select();
    }
    
    /**
     * Double clicks on the specified path in the results tree. It opens file
     * in editor.
     * @param path path to requested result (e.g. "MyClass|myMethod")
     */
    public void openResult(String path) {
        treeResult().clickOnPath(new Node(treeResult(), path).getTreePath(), 2);
    }

    /**
     * Pushes "Stop Search" button.
     */
    public void stopSearch() {
        btStopSearch().push();
    }

    /**
     * Pushes "Show Details" button and returns {@link OutputTabOperator} from output window.
     * @return OutputTabOperator instance
     */
    public OutputTabOperator showDetails() {
        btShowDetails().push();
        return new OutputTabOperator(TITLE);
    }

    /**
     * Pushes "Modify Search" button. and return {@link FindInFilesOperator}
     * @return FindInFilesOperator instance
     */
    public FindInFilesOperator modifySearch() {
        btModifySearch().pushNoBlock();
        return new FindInFilesOperator();
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
        treeResult();
        rbSortByName();
        rbDoNotSort();
    }
}
