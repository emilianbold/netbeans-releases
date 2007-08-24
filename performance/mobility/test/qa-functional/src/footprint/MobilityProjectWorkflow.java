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

package footprint;

import org.netbeans.jellytools.TopComponentOperator;

import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JToggleButtonOperator;
import org.netbeans.jemmy.operators.Operator;

/**
 * Measure Mobility Project Workflow Memory footprint
 *
 * @author  mmirilovic@netbeans.org
 */
public class MobilityProjectWorkflow extends org.netbeans.performance.test.utilities.MemoryFootprintTestCase {
    
    private String projectName;
            
    /**
     * Creates a new instance of MobilityProjectWorkflow
     *
     * @param testName the name of the test
     */
    public MobilityProjectWorkflow(String testName) {
        super(testName);
        prefix = "Mobility Project Workflow |";
    }
    
    /**
     * Creates a new instance of MobilityProjectWorkflow
     *
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public MobilityProjectWorkflow(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        prefix = "Mobility Project Workflow |";
    }
    
    @Override
    public void setUp() {
        // do nothing
    }
    
    public void prepare() {
    }
    
    public void initialize() {
        super.initialize();
        MPFootprintUtilities.closeAllDocuments();
        MPFootprintUtilities.closeMemoryToolbar();
    }
    
    public ComponentOperator open(){
        projectName = MPFootprintUtilities.createproject("Mobility", "MIDP Application", true); //NOI18N
        log("Created project name: "+projectName);
        // get opened editor
        Operator.StringComparator defaultOperator = Operator.getDefaultStringComparator();
        Operator.setDefaultStringComparator(new Operator.DefaultStringComparator(true, true));
        TopComponentOperator midletEditor = new TopComponentOperator("HelloMIDlet.java");
        Operator.setDefaultStringComparator(defaultOperator);
            
        // switch to Screen Design
        new JToggleButtonOperator(midletEditor, "Screen").pushNoBlock(); //NOI18N
        new EventTool().waitNoEvent(1500);
        
        // switch to Source
        new JToggleButtonOperator(midletEditor, "Source").pushNoBlock(); //NOI18N
        new EventTool().waitNoEvent(3000);
        
        // switch to Screen Design
        new JToggleButtonOperator(midletEditor, "Flow").pushNoBlock(); //NOI18N
        new EventTool().waitNoEvent(1500);
        
        MPFootprintUtilities.buildproject(projectName);
        
        return null;
    }
    
    public void close(){
        log("Deleting project: "+projectName);
        MPFootprintUtilities.deleteProject(projectName);
        log("Deleted...");
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(new MobilityProjectWorkflow("measureMemoryFooprint"));
    }
    
}
