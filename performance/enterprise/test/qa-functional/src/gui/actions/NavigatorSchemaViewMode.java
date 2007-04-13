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

import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.junit.ide.ProjectSupport;
import org.netbeans.jellytools.actions.CloseAllDocumentsAction;
import gui.EPUtilities;
import java.io.File;
/**
 *
 * @author  rashid@netbeans.org
 */
public class NavigatorSchemaViewMode  extends org.netbeans.performance.test.utilities.PerformanceTestCase {
    
  private SchemaOperator tco;
  private Node pNode; 
  private Node doc;
  private Node doc1;

    /** Creates a new instance of SchemaDesignView */
    public NavigatorSchemaViewMode(String testName) {
        super(testName);
        expectedTime = UI_RESPONSE;
     //   WAIT_AFTER_OPEN=4000; 
    }
    public NavigatorSchemaViewMode(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = UI_RESPONSE;
    }
//    public void testNavigatorSchemaViewMode() {
//        doMeasurement();
//    }

      protected void initialize() {
        log(":: initialize");
        ProjectSupport.openProject(System.getProperty("xtest.tmpdir")+File.separator+testProjectName);
        new CloseAllDocumentsAction().performAPI();
       Node pNode = new ProjectsTabOperator().getProjectRootNode(testProjectName);
        Node doc1 = new Node(pNode,"Process Files"+"|"+testSchemaName1+".xsd");
        doc1.select();
     
       SchemaOperator tco = new SchemaOperator(testSchemaName1+".xsd");
       JComboBoxOperator NavCombo = new JComboBoxOperator(tco,0); 
             NavCombo.selectItem("Schema View");
    }

    public void prepare() {
     log(":: prepare");
      
    }

    public ComponentOperator open() {
        log(":: open");
    Node pNode = new ProjectsTabOperator().getProjectRootNode(testProjectName);
        Node doc = new Node(pNode,"Process Files"+"|"+testSchemaName+".xsd");
        doc.select();
      
      return new SchemaOperator(testSchemaName+".xsd");

    }

   

    public void close() {
        log("::close");
       Node pNode = new ProjectsTabOperator().getProjectRootNode(testProjectName);
        Node doc1 = new Node(pNode,"Process Files"+"|"+testSchemaName1+".xsd");
        doc1.select();
 
    }
   

    protected void shutdown() {
        ProjectSupport.closeProject(testProjectName);

    }
    public static void main(String[] args) {
        junit.textui.TestRunner.run(new NavigatorSchemaViewMode("measureTime"));
    }
    

    private static String testProjectName = "SOATestProject";
    private static String testSchemaName = "fields";  
    private static String testSchemaName1 = "batch"; 
}



