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

import org.netbeans.jemmy.operators.ComponentOperator;

/**
 * Measure J2SE Project Workflow Memory footprint
 *
 * @author  anebuzelsky@netbeans.org, mmirilovic@netbeans.org
 */
public class J2SEProjectWorkflow extends org.netbeans.performance.test.utilities.MemoryFootprintTestCase {

    private String j2seproject;

    /**
     * Creates a new instance of J2SEProjectWorkflow
     * @param testName the name of the test
     */
    public J2SEProjectWorkflow(String testName) {
        super(testName);
        prefix = "J2SE Project Workflow |";
    }
    
    /**
     * Creates a new instance of J2SEProjectWorkflow
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public J2SEProjectWorkflow(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        prefix = "J2SE Project Workflow |";
    }
    
    @Override
    public void setUp() {
        //do nothing
    }
    
    public void prepare() {
    }
    
    public void initialize() {
        super.initialize();
        FootprintUtilities.closeAllDocuments();
        FootprintUtilities.closeMemoryToolbar();
    }
    
    public ComponentOperator open(){
        // Create, edit, build and execute a sample J2SE project
        j2seproject = FootprintUtilities.createproject("Samples|Java", "Anagram Game", true);
        
        FootprintUtilities.openFile(j2seproject, "com.toy.anagrams.ui", "Anagrams.java", false);
        FootprintUtilities.editFile(j2seproject, "com.toy.anagrams.ui", "Anagrams.java");
        FootprintUtilities.buildproject(j2seproject);
        //runProject(j2seproject,true);
        //debugProject(j2seproject,true);
        //testProject(j2seproject);
        //collapseProject(j2seproject);
        
        return null;
    }
    
    public void close(){
        FootprintUtilities.deleteProject(j2seproject);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(new J2SEProjectWorkflow("measureMemoryFooprint"));
    }
    
}
