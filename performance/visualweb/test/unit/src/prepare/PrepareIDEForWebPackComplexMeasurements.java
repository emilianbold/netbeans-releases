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

package prepare;


import junit.framework.Test;
import junit.framework.TestSuite;

import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.ProjectRootNode;

import org.netbeans.junit.NbTestSuite;

/**
 *
 * Prepare user directory for complex measurements (startup time and memory consumption) of IDE with opened VWP project.
 * Open Visual Web pack project (HugeApp) and shut down ide.
 * Created user directory will be used to measure startup time and memory consumption of IDE with opened files.*
 *
 * @author mkhramov@netbeans.org, mmirilovic@netbeans.org
 */
public class PrepareIDEForWebPackComplexMeasurements  extends PrepareIDEForComplexMeasurements {
    
    /**
     * Creates a new instance of PrepareIDEForWebPackComplexMeasurements
     */
    public PrepareIDEForWebPackComplexMeasurements(String testName) {
        super(testName);
    }
    
    /** Testsuite
     * @return testuite
     */
    public static Test suite() {
        TestSuite suite = new NbTestSuite();
        suite.addTest(new PrepareIDEForComplexMeasurements("closeWelcome"));
        suite.addTest(new PrepareIDEForComplexMeasurements("closeAllDocuments"));
        suite.addTest(new PrepareIDEForComplexMeasurements("closeMemoryToolbar"));
        suite.addTest(new PrepareIDEForComplexMeasurements("closeUIGesturesToolbar"));
        suite.addTest(new PrepareIDEForWebPackComplexMeasurements("openPages"));
        suite.addTest(new PrepareIDEForComplexMeasurements("saveStatus"));
        return suite;
    }
    
    public void openPages() {
        log("::open Pages");
        try {
            String PageName = "Page1.jsp";
            String WPN = org.netbeans.jellytools.Bundle.getString("org.netbeans.modules.visualweb.project.jsfproject.ui.Bundle", "LBL_Node_DocBase");

            // try to workarround problems with tooltip on Win2K & WinXP - issue 56825
            ProjectRootNode projectNode = new ProjectsTabOperator().getProjectRootNode("HugeApp");
            projectNode.expand();

            Node pagesNode = new Node(projectNode,WPN);
            pagesNode.expand();
            
	    Node webPage = new Node(projectNode,WPN+"|"+PageName);
	    webPage.select();
	            
            try { 
                new OpenAction().performAPI(webPage);
            } catch(Exception ex) {

		log("First Attempt in Open Action failed");
                new OpenAction().performAPI(webPage);
            }
            log(":: Open Action passed");
            
        } catch(Exception exc) {
	
            test_failed = true;
            fail(exc);            
        }
    }
    
}
