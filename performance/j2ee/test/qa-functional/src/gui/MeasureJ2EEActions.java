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

package gui;


import gui.action.*;
import org.netbeans.junit.NbTestSuite;

/**
 * Measure UI-RESPONSIVENES and WINDOW_OPENING.
 *
 * @author  lmartinek@netbeans.org
 */
public class MeasureJ2EEActions  {
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        
	if (System.getProperty("onlyDeployment") != null && System.getProperty("onlyDeployment").equals("true")) {
		suite.addTest(new Deploy("measureTime", "Deploy Enterprise Application"));
	} else {

	        suite.addTest(new ExpandEJBNodesProjectsView("testExpandEjbProjectNode", "Expand EJB Project node"));
	        suite.addTest(new ExpandEJBNodesProjectsView("testExpandEjbNode", "Expand Enterprise Beans node"));
	
	        suite.addTest(new OpenJ2EEFiles("testOpeningJava", "Open Java file"));
	        suite.addTest(new OpenJ2EEFiles("testOpeningSessionBean", "Open Session Bean file"));
	        suite.addTest(new OpenJ2EEFiles("testOpeningEntityBean", "Open Entity Bean file"));
	        suite.addTest(new OpenJ2EEFiles("testOpeningEjbJarXml", "Open ejb-jar.xml file"));
	        suite.addTest(new OpenJ2EEFiles("testOpeningSunEjbJarXml", "Open sun-ejb-jar.xml file"));
	        suite.addTest(new OpenJ2EEFiles("testOpeningApplicationXml", "Open application.xml file"));
	        suite.addTest(new OpenJ2EEFiles("testOpeningSunApplicationXml", "Open sun-application.xml file"));
	        
	        suite.addTest(new OpenJ2EEFilesWithOpenedEditor("testOpeningSessionBean", "Open Session Bean file if Editor opened"));
	        suite.addTest(new OpenJ2EEFilesWithOpenedEditor("testOpeningEntityBean", "Open Entity Bean file if Editor opened"));
	        suite.addTest(new OpenJ2EEFilesWithOpenedEditor("testOpeningEjbJarXml", "Open ejb-jar.xml file if Editor opened"));
	        suite.addTest(new OpenJ2EEFilesWithOpenedEditor("testOpeningSunEjbJarXml", "Open sun-ejb-jar.xml file if Editor opened"));
	        suite.addTest(new OpenJ2EEFilesWithOpenedEditor("testOpeningApplicationXml", "Open application.xml file if Editor opened"));
	        suite.addTest(new OpenJ2EEFilesWithOpenedEditor("testOpeningSunApplicationXml", "Open sun-application.xml file if Editor opened"));
	        
	        suite.addTest(new MeasureSessionBeanAction("testAddBusinessMethod", "Add business method to SB"));
	        suite.addTest(new MeasureEntityBeanAction("testAddBusinessMethod", "Add business method to EB"));
	        
                suite.addTest(new MeasureEntityBeanAction("testAddFinderMethod", "Add finder method to EB"));
	        suite.addTest(new MeasureEntityBeanAction("testAddSelectMethod", "Add select method to EB"));
	
                
	        suite.addTest(new MeasureWebServiceAction("testAddOperation", "Add operation to WS"));
	        
                
	        suite.addTest(new MeasureCallEjbAction("measureTime", "Call EJB in session bean"));
	        
	        //suite.addTest(new Deploy("measureTime", "Deploy Enterprise Application"));
/* Unstable
	        suite.addTest(new CreateNewFile("testCreateNewSessionBean", "Create new session bean"));
	        suite.addTest(new CreateNewFile("testCreateNewEntityBean", "Create new entity bean"));
            
	        
	        suite.addTest(new CreateNewFile("testCreateNewWebService", "Create new web service"));
*/	
	        suite.addTest(new CreateJ2EEProject("testCreateEnterpriseApplicationProject", "Create Enterprise Application projects"));
	        suite.addTest(new CreateJ2EEProject("testCreateStandaloneEnterpriseApplicationProject", "Create standalone Enterprise Application project"));
	        suite.addTest(new CreateJ2EEProject("testCreateEJBModuleProject", "Create EJB Module project"));
        }
        
        return suite;
    }
    
}
