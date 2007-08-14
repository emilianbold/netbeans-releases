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

package gui.window;

import java.awt.Component;
import javax.swing.JButton;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JButtonOperator;

/**
 *
 * @author mkhramov@netbeans.org
 */
public class AddProfilingPointWizard  extends org.netbeans.performance.test.utilities.PerformanceTestCase {

    private static final String menuPrefix = "Window|Profiling|"; //NOI18N
    
    private String commandName;
    private String windowName;
    private TopComponentOperator ppointsPane;
    private JButtonOperator addPointButton;
    private NbDialogOperator wizard;
    
    /**
     * @param testName 
     */
    public AddProfilingPointWizard(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;        
    }
    /**
     * @param testName 
     * @param performanceDataName
     */
    public AddProfilingPointWizard(String testName, String performanceDataName) {
        super(testName,performanceDataName);
        expectedTime = WINDOW_OPEN;        
    }
    public void initialize() {
        log(":: initialize");
        commandName = "Profiling Points"; //NOI18N
        windowName = "Profiling Points"; ////NOI18N
        new Action(menuPrefix+commandName,null).performMenu(); // NOI18N  
        ppointsPane = new TopComponentOperator(windowName);
        
        addPointButton = new JButtonOperator(ppointsPane,new ComponentChooser() {

            public boolean checkComponent(Component component) {

	try{
                if ( (((JButton)component).getToolTipText()).equals("Add Profiling Point") ) //NOI18N
                      return true; 
                else  return false;
	} catch (java.lang.NullPointerException npe) {}

             return false;
            }

            public String getDescription() {
                return "Selecting button by tooltip";
            }
        });

    }

    public void prepare() {
        log(":: prepare");        
        
    }

    public ComponentOperator open() {
        addPointButton.pushNoBlock();
        wizard =new NbDialogOperator("New Profiling Point"); //NOI18N
        return null;
    }
    public void close() {
        wizard.close();
    }
    
    public void shutdown() {
        ppointsPane.closeWindow();
    }

}
