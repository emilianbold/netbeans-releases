/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.jellytools;

import org.netbeans.jellytools.nodes.Node;

import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.Timeouts;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.Waiter;
import org.netbeans.jemmy.operators.JButtonOperator;
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

    //shortcuts

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
    }
}
