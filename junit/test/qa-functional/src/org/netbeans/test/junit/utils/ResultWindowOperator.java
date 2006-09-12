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
package org.netbeans.test.junit.utils;

import java.awt.Component;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.modules.junit.actions.ResultWindowViewAction;
import org.netbeans.jemmy.ComponentChooser;

/**
 *
 * @author Max Sauer
 */
public class ResultWindowOperator extends TopComponentOperator {
    
    private static final Action invokeAction = new ResultWindowViewAction();
    
    /**
     * Creates a new instance of ResultWindowOperator
     */
    public ResultWindowOperator() {
        /* In IDE OutputWindow top component is singleton but in sense of
         jellytools, it is not singleton. It can be closed/hidden and
         again opened/shown, so it make sense to wait for OutputWindow
         top component again.
         */
        super(waitTopComponent(null, null, 0, resultsSubchooser));
    }
    
    /**
     * 
     * Opens JUnit Test Results from main menu Window|JUnit Test Results and
     * returns ResultWindowOperator.
     * 
     * @return instance of ResultsWindowOperatorOperator
     */
    public static ResultWindowOperator invoke() {
        invokeAction.perform();
        return new ResultWindowOperator();
    }
    
    
    /** 
     * SubChooser to determine ResultsWindow TopComponent
     * Used in constructor.
     */
    private static final ComponentChooser resultsSubchooser = new ComponentChooser() {
        public boolean checkComponent(Component comp) {
            return comp.getClass().getName().endsWith("ResultWindow"); //NOI18N
        }
        
        public String getDescription() {
            return "component instanceof org.netbeans.modules.junit.output.ResultWindow";// NOI18N
        }
    };
    
}
