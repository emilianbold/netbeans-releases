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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package gui.actions;

import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.NewFileWizardOperator;
import org.netbeans.jellytools.actions.CloseAllDocumentsAction;

import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.junit.ide.ProjectSupport;
import java.io.File;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;

import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;


/**
 * Test Close Project Property
 *
 * @author  rashid@netbeans.org
 */
public class CloseProjectProperty extends org.netbeans.performance.test.utilities.PerformanceTestCase {

    
      private NbDialogOperator jdo ;
      private int index;
        
    private static String testProjectName = "MobileApplicationVisualMIDlet";  
    
    /**
     * Creates a new instance of CloseProjectProperty
     * @param testName the name of the test
     */
    public CloseProjectProperty(String testName) {
        super(testName);
        expectedTime = 10000;
        WAIT_AFTER_OPEN=4000;
    }
    
    /**
     * Creates a new instance of CloseProjectProperty
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public CloseProjectProperty(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = 10000;
        WAIT_AFTER_OPEN=4000;
    }
    
  
    
    public void initialize(){
                
        ProjectSupport.openProject(System.getProperty("xtest.tmpdir")+File.separator+testProjectName);
        new CloseAllDocumentsAction().performAPI();
       new EventTool().waitNoEvent(1000);

    }
    
    public void prepare(){
  
        Node pNode = new ProjectsTabOperator().getProjectRootNode(testProjectName);
        pNode.select();
        pNode.performPopupAction("Properties");
         
       jdo = new NbDialogOperator(testProjectName);
       JTreeOperator cattree = new JTreeOperator(jdo);       
       Node cNode = new Node(cattree,"Abilities") ;
       cNode.select();
        
       JButtonOperator addButton = new JButtonOperator(jdo,"Add");
       addButton.pushNoBlock();

       NbDialogOperator add_abil = new NbDialogOperator("Add Ability");
       JComboBoxOperator abilityCombo = new JComboBoxOperator(add_abil); 
       abilityCombo.clearText();
       abilityCombo.typeText("Ability_"+(index++));
       JButtonOperator abil_okButton = new JButtonOperator(add_abil,"OK");
       abil_okButton.push();
       
    }
    
    public ComponentOperator open(){
 
       JButtonOperator okButton = new JButtonOperator(jdo,"OK");
       okButton.push();
       return null;
    }
    
    public void close(){
        new CloseAllDocumentsAction().performAPI(); //avoid issue 68671 - editors are not closed after closing project by ProjectSupport
    }

    protected void shutdown() {
         ProjectSupport.closeProject(testProjectName);
//    new CloseAllDocumentsAction().performAPI(); //avoid issue 68671 - editors are not closed after closing project by ProjectSupport      
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(new CloseProjectProperty("measureTime"));
    }
}
