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


//import java.io.File;
import javax.swing.tree.TreePath;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jellytools.RuntimeTabOperator;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.OutputOperator;
import org.netbeans.jellytools.OutputTabOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.CloseAllDocumentsAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.junit.ide.ProjectSupport;

/**
 * Measure UI-RESPONSIVENES and WINDOW_OPENING.
 *
 * @author rashid@netbeans.org
 *
 */
public class OpenComplexDiagram extends org.netbeans.performance.test.utilities.PerformanceTestCase {
    
   private String testProjectName ="TravelReservationService" ;
   private String testDiagramName ="TravelReservationService";
 

   /** Creates a new instance of OpenComplexDiagram */

    public OpenComplexDiagram(String testName) {
        super(testName);
        //TODO: Adjust expectedTime value        
        expectedTime = 10000;
        WAIT_AFTER_OPEN=4000;        
    }
    public OpenComplexDiagram(String testName, String  performanceDataName) {
        super(testName);
        //TODO: Adjust expectedTime value
        expectedTime = 10000;
        WAIT_AFTER_OPEN=4000;                
    }
    
    public void initialize(){
        log(":: initialize");
ProjectSupport.openProject(System.getProperty("xtest.tmpdir")+"/TravelReservationService/TravelReservationService");
    new CloseAllDocumentsAction().performAPI(); 
                
    }
   
    public void prepare() {
        log(":: prepare");
 
    }

    public ComponentOperator open() {
                log("::open");
          log(":: prepare");
        Node pNode = new ProjectsTabOperator().getProjectRootNode(testProjectName);
        Node doc = new Node(pNode,"Process Files"+"|"+testDiagramName+".bpel");
        doc.select();
        doc.performPopupAction("Open");   
    return new SchemaOperator(testDiagramName+".bpel");
    }
    
    protected void shutdown() {
        log("::shutdown");
        ProjectSupport.closeProject(testProjectName);
    }
//    public void testOpenComplexDiagram() {
//                doMeasurement();
//    }

  public void close(){
        log("::close");
       new CloseAllDocumentsAction().performAPI();
 
    } 
    
    
    
}