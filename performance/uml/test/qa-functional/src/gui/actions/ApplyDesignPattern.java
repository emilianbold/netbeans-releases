/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package gui.actions;


import java.io.File;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.CloseAllDocumentsAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.junit.ide.ProjectSupport;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jellytools.NbDialogOperator;

import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
/**
 * Measure UI-RESPONSIVENES and WINDOW_OPENING.
 *
 * @author rashid@netbeans.org
 *
 */
public class ApplyDesignPattern extends org.netbeans.performance.test.utilities.PerformanceTestCase {
    
    private static String testProjectName = "jEdit-Model";
    private Node diag;  
    private NbDialogOperator d_wizard; 
   
    /** Creates a new instance of ApplyDesignPattern */
    public ApplyDesignPattern(String testName) {
        super(testName);
        //TODO: Adjust expectedTime value        
        expectedTime = 5000;
        WAIT_AFTER_OPEN=4000;        
    }
    public ApplyDesignPattern(String testName, String  performanceDataName) {
        super(testName);
        //TODO: Adjust expectedTime value
        expectedTime = 5000;
        WAIT_AFTER_OPEN=4000;                
    }
    
    public void initialize(){
        log(":: initialize");
        
        ProjectSupport.openProject(System.getProperty("xtest.tmpdir")+File.separator+testProjectName);
        new CloseAllDocumentsAction().performAPI();
        
    }
   
    public void prepare() {
        log(":: prepare");
        Node pNode = new ProjectsTabOperator().getProjectRootNode(testProjectName);
        diag = new Node(pNode,"Model");
        diag.select();
        diag.performPopupActionNoBlock("Apply Design Pattern");
        NbDialogOperator a_wizard = new NbDialogOperator("Design Pattern Apply Wizard");
        JButtonOperator a_wizard_next = new JButtonOperator(a_wizard,"Next");
        a_wizard_next.push();

        d_wizard = new NbDialogOperator("Design Pattern Wizard");
        JButtonOperator d_wizard_next = new JButtonOperator(d_wizard,"Next");
        JComboBoxOperator projectCombo = new JComboBoxOperator(d_wizard);
        projectCombo.selectItem("EJB2.0");

        d_wizard_next.push();
        new EventTool().waitNoEvent(1000);
        d_wizard_next.push();
        new EventTool().waitNoEvent(1000);
        d_wizard_next.push();
        JCheckBoxOperator d_wizardCheck = new JCheckBoxOperator(d_wizard,"Create class diagram");
        d_wizardCheck.doClick();
        d_wizard_next.push();
    }

    public ComponentOperator open() {
        log("::open");
       
        JButtonOperator d_wizard_finish = new JButtonOperator(d_wizard,"Finish");
        d_wizard_finish.push();

        return new TopComponentOperator("BeanManagedDiagram");

    }
    
    protected void shutdown() {
        log("::shutdown");
        ProjectSupport.closeProject(testProjectName);
    }
   

    public void close(){
        log("::close");
      new CloseAllDocumentsAction().performAPI();
 
    } 
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(new ApplyDesignPattern("measureTime"));
    }      


}