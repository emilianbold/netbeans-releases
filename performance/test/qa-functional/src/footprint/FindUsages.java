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

import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;

import org.netbeans.junit.ide.ProjectSupport;


/**
 * Measure Find Usages Memory footprint
 *
 * @author  anebuzelsky@netbeans.org, mmirilovic@netbeans.org
 */
public class FindUsages extends MemoryFootprintTestCase {
    
    /**
     * Creates a new instance of FindUsages
     * @param testName the name of the test
     */
    public FindUsages(String testName) {
        super(testName);
        prefix = "Find Usages |";
    }
    
    /**
     * Creates a new instance of FindUsages
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public FindUsages(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        prefix = "Find Usages |";
    }
    
    public void initialize() {
        super.initialize();
        FootprintUtilities.closeAllDocuments();
        FootprintUtilities.closeMemoryToolbar();
        FootprintUtilities.closeUIGesturesToolbar();
    }

    public void prepare() {
    }
    
    public ComponentOperator open(){
        // jEdit project
        log("Opening project jEdit");
        ProjectsTabOperator.invoke();
        ProjectSupport.openProject(System.getProperty("performance.footprint.jedit.location"));
        
        // invoke Find Usages
        Node filenode = new Node(new SourcePackagesNode("jEdit"), "org.gjt.sp.jedit" + "|" + "jEdit.java");
        filenode.callPopup().pushMenuNoBlock("Find Usages");
        
        NbDialogOperator findusagesdialog = new NbDialogOperator("Find Usages");
        new JCheckBoxOperator(findusagesdialog,"Search in Comments").setSelected(true);
        new JButtonOperator(findusagesdialog,"Find").push();
        
        return new TopComponentOperator("Usages");
    }
    
    public void close(){
        FootprintUtilities.closeProject("jEdit");
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(new FindUsages("measureMemoryFooprint"));
    }
    
}
