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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.jellytools;

import org.netbeans.jellytools.actions.FindInFilesAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;

/**
 * Provides access to the Find in Files/Projects dialog.
 */
public class FindInFilesOperator extends NbDialogOperator {

    private static final FindInFilesAction invokeAction = new FindInFilesAction();
    private JButtonOperator _btFind;

    /**
     * Waits for dialog displayed.
     */
    public FindInFilesOperator() {
        super(Bundle.getString("org.netbeans.modules.search.Bundle", "LBL_FindInProjects"));
    }

    /**
     * Invokes dialog by selecting a node and pushing menu.
     */
    public static FindInFilesOperator invoke(Node node) {
        invokeAction.perform(node);
        return new FindInFilesOperator();
    }

    /**
     * Returns operator for a Find button.
     * @return JButtonOperator instance
     */
    public JButtonOperator btFind() {
        if(_btFind == null) {
            _btFind = 
                new JButtonOperator(this, 
                                    Bundle.
                                    getStringTrimmed("org.netbeans.modules.search.Bundle",
                                              "TEXT_BUTTON_SEARCH"));
        }
        return _btFind;
    }

    /**
     * Returns operator for the "Whole Words" check box.
     * @return JCheckBoxOperator instance
     */
    public JCheckBoxOperator cbWholeWords() {
        String text = Bundle.getStringTrimmed("org.netbeans.modules.search.Bundle",
                                              "BasicSearchForm.chkWholeWords.text");
        return new JCheckBoxOperator(this, text);
    }

    /**
     * Returns operator for the "Case Sensitive" check box.
     * @return JCheckBoxOperator instance
     */
    public JCheckBoxOperator cbCase() {
        String text = Bundle.getStringTrimmed("org.netbeans.modules.search.Bundle",
                                              "BasicSearchForm.chkCaseSensitive.text");
        return new JCheckBoxOperator(this, text);
    }

    /**
     * Returns operator for the "Regular Expression" check box.
     * @return JCheckBoxOperator instance
     */
    public JCheckBoxOperator cbRegExpr() {
        String text = Bundle.getStringTrimmed("org.netbeans.modules.search.Bundle",
                                              "BasicSearchForm.chkRegexp.text");
        return new JCheckBoxOperator(this, text);
    }

    /**
     * Returns operator for the "Containing Text:" text field.
     * @return JTextFieldOperator instance
     */
    public JTextFieldOperator txtText() {
        return new JTextFieldOperator(this);
    }
    
    /**
     * Returns operator for the "File Name Patterns:" text field.
     * @return JTextFieldOperator instance
     */
    public JTextFieldOperator txtPatterns() {
        return new JTextFieldOperator(this, 1);
    }

    /**
     * Pushes Find button.
     * @return "Search Result" window.
     */
    public SearchResultsOperator find() {
        btFind().push();
        SearchResultsOperator results = new SearchResultsOperator();
        results.waitEndOfSearch();
        return results;
    }

    /** Performs verification by accessing all sub-components */    
    public void verify() {
        btFind();
        btClose();
        btHelp();
        cbRegExpr();
        cbWholeWords();
        cbCase();
        txtText();
        txtPatterns();
    }
}
