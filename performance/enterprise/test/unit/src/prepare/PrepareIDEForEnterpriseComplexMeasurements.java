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

package prepare;

import java.io.File;
import java.util.ArrayList;

import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.MainWindowOperator;

import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.actions.EditAction;
import org.netbeans.jellytools.actions.CloseAllDocumentsAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.ProjectRootNode;

import org.netbeans.jemmy.operators.JMenuBarOperator;
import org.netbeans.jemmy.operators.JMenuItemOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.Operator;

import junit.framework.Test;

import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.ide.ProjectSupport;

import org.netbeans.performance.test.utilities.MeasureStartupTimeTestCase;


/**
 * Prepare user directory for complex measurements (startup time and memory consumption) of IDE with opened project and 10 files.
 * Open 10 java files and shut down ide.
 * Created user directory will be used to measure startup time and memory consumption of IDE with opened files.
 *
 * @author mmirilovic@netbeans.org
 */
public class PrepareIDEForEnterpriseComplexMeasurements extends JellyTestCase {
    
    /** Error output from the test. */
    protected static java.io.PrintStream err;
    
    /** Logging output from the test. */
    protected static java.io.PrintStream log;
    
    /** If true - at least one test failed */
    protected static boolean test_failed = false;
    
    /** Define testcase
     * @param testName name of the testcase
     */
    public PrepareIDEForEnterpriseComplexMeasurements(String testName) {
        super(testName);
    }
    
    /** Testsuite
     * @return testuite
     */
    public static Test suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new PrepareIDEForEnterpriseComplexMeasurements("closeWelcome"));
        suite.addTest(new PrepareIDEForEnterpriseComplexMeasurements("closeAllDocuments"));
        suite.addTest(new PrepareIDEForEnterpriseComplexMeasurements("closeMemoryToolbar"));
        suite.addTest(new PrepareIDEForEnterpriseComplexMeasurements("openProjects"));
        suite.addTest(new PrepareIDEForEnterpriseComplexMeasurements("openFiles"));
        suite.addTest(new PrepareIDEForEnterpriseComplexMeasurements("saveStatus"));
        return suite;
    }
    
    
    public void setUp() {
//        err = System.out;
        err = getLog();
        log = getRef();
    }
    
    /**
     * Close Welcome.
     */
    public void closeWelcome(){
        try {
            TopComponentOperator tComponent = new TopComponentOperator(Bundle.getStringTrimmed("org.netbeans.modules.welcome.Bundle","LBL_Tab_Title"));
            new JCheckBoxOperator(tComponent,Bundle.getStringTrimmed("org.netbeans.modules.welcome.resources.Bundle","LBL_ShowOnStartup")).changeSelection(false);
            tComponent.close();
        }catch(Exception exc){
            test_failed = true;
            fail(exc);
        }
    }
    
    /**
     * Close All Documents.
     */
    public void closeAllDocuments(){

	if ( new Action("Window|Close All Documents",null).isEnabled() )
	        try {
        	    new CloseAllDocumentsAction().perform();
	        }catch(Exception exc){
	            test_failed = true;
        	    fail(exc);
	        }

    }
    
    /**
     * Close Memory Toolbar.
     */
    public static void closeMemoryToolbar(){
        closeToolbar(Bundle.getStringTrimmed("org.netbeans.core.Bundle","Menu/View") + "|" +
                Bundle.getStringTrimmed("org.netbeans.core.windows.actions.Bundle","CTL_ToolbarsListAction") + "|" +
                Bundle.getStringTrimmed("org.netbeans.core.Bundle","Toolbars/Memory"));
    }

    
    private static void closeToolbar(String menu){
        MainWindowOperator mainWindow = MainWindowOperator.getDefault();
        JMenuBarOperator menuBar = new JMenuBarOperator(mainWindow.getJMenuBar());
        JMenuItemOperator menuItem = menuBar.showMenuItem(menu,"|");
        
        if(menuItem.isSelected())
            menuItem.push();
        else {
            menuItem.pushKey(java.awt.event.KeyEvent.VK_ESCAPE);
            mainWindow.pushKey(java.awt.event.KeyEvent.VK_ESCAPE);
        }
    }
      
    /**
     * Open Travel Reservation projects
     */
    public void openProjects() {
        try {
            String projectsLocation = System.getProperty("xtest.tmpdir") + File.separator + "TravelReservationService" + File.separator;
            ProjectSupport.openProject(projectsLocation + "ReservationPartnerServices");
            ProjectSupport.waitScanFinished();
            ProjectSupport.openProject(projectsLocation + "TravelReservationService");
            ProjectSupport.waitScanFinished();
            ProjectSupport.openProject(projectsLocation + "TravelReservationServiceApplication");
            ProjectSupport.waitScanFinished();
        }catch(Exception exc){
            test_failed = true;
            fail(exc);
        }
    }
    
    /**
     * Open 10 selected files from Travel Reservation projects
     */
    public void openFiles(){
        String OPEN = "Open";
        String EDIT = "Edit";
        
        try {
            String[][] nodes_path = {
                {"ReservationPartnerServices","Enterprise Beans","ReservationCallbackProviderMDB", "ReservationCallbackProviderBean.java", OPEN},
                {"ReservationPartnerServices","Source Packages","partnerservices|AirlineReservationPortType_Impl.java", "AirlineReservationPortType_Impl.java", OPEN},
                {"ReservationPartnerServices","Source Packages","partnerservices|HotelReservationPortType_Impl.java", "HotelReservationPortType_Impl.java", OPEN},
                {"ReservationPartnerServices","Source Packages","partnerservices|VehicleReservationPortType_Impl.java", "VehicleReservationPortType_Impl.java", OPEN},
                {"ReservationPartnerServices","Configuration Files","ejb-jar.xml", null, OPEN},
                {"ReservationPartnerServices","Configuration Files","sun-ejb-jar.xml", null, EDIT},
                {"TravelReservationService","Process Files","AirlineReservationService.wsdl", null, EDIT},
                {"TravelReservationService","Process Files","HotelReservationService.wsdl", null, EDIT},
                {"TravelReservationService","Process Files","OTA_TravelItinerary.xsd", null, OPEN},
                {"TravelReservationService","Process Files","TravelReservationService.bpel", null, OPEN}
            };
            
            ArrayList<Node> openFileNodes = new ArrayList<Node>();
            ArrayList<Node> editFileNodes = new ArrayList<Node>();
            Node node, fileNode;
            
            // create exactly (full match) and case sensitively comparing comparator
            Operator.DefaultStringComparator comparator = new Operator.DefaultStringComparator(true, true);
            
            for(int i=0; i<nodes_path.length; i++) {
                // try to workarround problems with tooltip on Win2K & WinXP - issue 56825
                ProjectRootNode projectNode = new ProjectsTabOperator().getProjectRootNode(nodes_path[i][0]);
                projectNode.expand();
                
                node = new Node(projectNode,nodes_path[i][1]);
                node.setComparator(comparator);
                node.expand();
                
                fileNode = new Node(node,nodes_path[i][2]);
                //try to avoid issue 56825
                fileNode.select();
                
                if(nodes_path[i][4].equals(OPEN)) {
                    openFileNodes.add(fileNode);
                } else if(nodes_path[i][4].equals(EDIT)) {
                    editFileNodes.add(fileNode);
                } else
                    throw new Exception("Not supported operation [" + nodes_path[i][4] + "] for node: " + fileNode.getPath());
                
                // open file one by one, opening all files at once causes never ending loop (java+mdr)
                //new OpenAction().performAPI(openFileNodes[i]);
            }
            
            // try to come back and open all files at-once, rises another problem with refactoring, if you do open file and next expand folder,
            // it doesn't finish in the real-time -> hard to reproduced by hand
            try {
                new OpenAction().performAPI(openFileNodes.toArray(new Node[0]));
                new EditAction().performAPI(editFileNodes.toArray(new Node[0]));
            }catch(Exception exc){
                err.println("---------------------------------------");
                err.println("issue 56825 : EXCEPTION catched during OpenAction");
                exc.printStackTrace(err);
                err.println("---------------------------------------");
                err.println("issue 56825 : Try it again");
                new OpenAction().performAPI(openFileNodes.toArray(new Node[0]));
                new EditAction().performAPI(editFileNodes.toArray(new Node[0]));
                err.println("issue 56825 : Success");
            }
            
            
            // check whether files are opened in editor
            for(int i=0; i<nodes_path.length; i++) {
                if(nodes_path[i][3]!=null)
                    new TopComponentOperator(nodes_path[i][3]);
                else
                    new TopComponentOperator(nodes_path[i][2]);
            }
//        new org.netbeans.jemmy.EventTool().waitNoEvent(60000);
            
        }catch(Exception exc){
            test_failed = true;
            fail(exc);
        }
    }
    
    /**
     * Save status, if one of the above defined test failed, this method creates
     * file in predefined path and it means the complex tests will not run.
     */
    public void saveStatus() throws java.io.IOException{
        if(test_failed)
            MeasureStartupTimeTestCase.createStatusFile();
    }
}
