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
package org.netbeans.test.utilities.operators;

import java.awt.Component;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.operators.JToggleButtonOperator;
import org.netbeans.test.utilities.actions.SearchResultsViewAction;

/**
 * This Operator operates Search Results Window
 * @author Max Sauer
 */
public class SearchResultsOperator extends TopComponentOperator {
    
    private static final Action invokeAction = new SearchResultsViewAction();
    
    /**
     * Creates a new instance of SearchResultsOperator
     */
    public SearchResultsOperator() {
        /* In IDE ResultWindow top component is singleton but in sense of
         * jellytools, it is not singleton. It can be closed/hidden and
         * again opened/shown, so it make sense to wait for OutputWindow
         * top component again.
         */
        super(waitTopComponent(null, null, 0, resultsSubchooser));
    }
    
    /**
     *
     * Opens JUnit Test Results from main menu Window|Search Results and
     * returns SearchResultsOperator.
     *
     * @return instance of ResultsWindowOperatorOperator
     */
    public static SearchResultsOperator invoke() {
        invokeAction.perform();
        return new SearchResultsOperator();
    }
    
    /**
     * Returns operator of Statistics Panel
     * @return the operator of statistics panel 
     */ 
    public StatisticsPanelOperator getLeftPanelOperator() {
        return new StatisticsPanelOperator();
    }
    
    /**
     * Test whether the Results Filter toggle button is enabled
     * @return true if the button os enabled
     */ 
    public boolean isFilterButtonEnabled() {
        return (new JToggleButtonOperator(new StatisticsPanelOperator(), 0).isEnabled());
    }
    
    /**
     * Pushes Filter Button
     */
    public void pushFilterButton() {
        new JToggleButtonOperator(new StatisticsPanelOperator(), 0).push();
    }
    
    /**
     * SubChooser to determine ResultsWindow TopComponent
     * Used in constructor.
     */
    private static final ComponentChooser resultsSubchooser = new ComponentChooser() {
        public boolean checkComponent(Component comp) {
            return comp.getClass().getName().endsWith("ResultView"); //NOI18N
        }
        public String getDescription() {
            return "component instanceof org.netbeans.modules.search.ResultView";// NOI18N
        }
    };
}
