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
import javax.swing.JComponent;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.operators.JComponentOperator;

/**
 * This operates StatisticsPanel from inside JUnit Tests results window
 * @author Max Sauer
 */
public class StatisticsPanelOperator extends JComponentOperator {
    
    /** 
     * Waits for index-th output tab with given name.
     * It is activated by defalt.
     * @param name name of output tab to look for
     * @param index index of requested output tab with given name
     */
    public StatisticsPanelOperator() {
        super((JComponent) new ResultWindowOperator().waitSubComponent(statisticsSubchooser));
    }
    
    private static final ComponentChooser statisticsSubchooser = new ComponentChooser() {
        public boolean checkComponent(Component comp) {
            return comp.getClass().getName().endsWith("StatisticsPanel"); //NOI18N
        }
        
        public String getDescription() {
            return "component instanceof org.netbeans.modules.junit.output.StatisticsPanel";// NOI18N
        }
    };
    
}
