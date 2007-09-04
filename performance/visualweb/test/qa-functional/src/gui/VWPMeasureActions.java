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

package gui;


import org.netbeans.junit.NbTestSuite;
import gui.action.*;

/**
 * Measure UI-RESPONSIVENES and WINDOW_OPENING.
 *
 * @author  mkhramov@netbeans.org
 */
public class VWPMeasureActions  {
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
	suite.addTest(new CreateWebPackProject("testCreateWebPackProject","Create Visual Web Project"));
//TODO do Open project through UI	suite.addTest(new OpenWebPackProject("measureTime","Open Small Web Project"));
//TODO do Open project through UI        suite.addTest(new OpenHugeWebPackProject("testOpenWebPackProject","Open Huge Web Project"));
        
        suite.addTest(new OpenProjectFirstPage("testOpenSmallProjectFirstPage","Open Small Project First Page"));
        suite.addTest(new OpenProjectFirstPage("testOpenLargeProjectFirstPage","Open Large Project First Page"));
        
        suite.addTest(new CSSRuleAddTest("measureTime","Measure time to add and modify CSS rule"));
        
        suite.addTest(new OpenBeanFiles("testApplicationBean","Open Application Bean"));
        suite.addTest(new OpenBeanFiles("testRequestBean","Open Request  Bean"));
        suite.addTest(new OpenBeanFiles("testSessionBean","Open Session Bean"));
        suite.addTest(new OpenNavigationPage("measureTime","Open Navigation Page"));
        

        suite.addTest(new CreateWebPackFiles("testCreateCSSTable","Create CSS table"));	
	suite.addTest(new CreateWebPackFiles("testCreateJSPFragment","Create JSP fragment for VWP project"));
        suite.addTest(new CreateWebPackFiles("testCreateJSPPage","Create JSP page for VWP project"));

        suite.addTest(new gui.window.DatabaseTableDrop("measureTime","Database table drop on Table time")); 
        //TODO Disabled because throws exception. See bugid #99202      
        suite.addTest(new ComponentAddTest("testAddTableComponent","Adding Table Component"));
	suite.addTest(new ComponentAddTest("testAddButtonComponent","Adding Button Component"));
	suite.addTest(new ComponentAddTest("testAddListboxComponent","Adding Listbox Component"));
        
        //suite.addTest(new CreateWebPackProjectSBS("testCreateWebPackProject","Create Visual Web Project SBS"));
        
        suite.addTest(new WebProjectDeployment("testDeploySmallProject","Deployment Small Project"));     
        suite.addTest(new WebProjectDeployment("testDeployLargeProject","Deployment Huge Project"));
        
        return suite;
    }
}

