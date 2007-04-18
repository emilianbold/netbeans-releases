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

package gui;


import org.netbeans.junit.NbTestSuite;
import gui.actions.*;

/**
 * Measure UI-RESPONSIVENES and WINDOW_OPENING.
 *
 * @author  mmirilovic@netbeans.org, rashid@netbeans.org
 */
public class EPMeasureActions  {

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
            
        suite.addTest(new CreateBPELmodule("measureTime", "Create BPEL module"));
        suite.addTest(new CreateCompositeApplication("measureTime", "Create Composite Application"));
        suite.addTest(new AddNewWSDLDocument("measureTime", "Add New WSDL Document"));
        suite.addTest(new AddNewXMLSchema("measureTime", "Add New XML Schema"));
        suite.addTest(new AddNewXMLDocument("measureTime", "Add New XML Document"));
        suite.addTest(new AddNewBpelProcess("measureTime", "Add New Bpel Process")); 

//TODO disbale temporary - there is huge memory leak!- IZ 98405        suite.addTest(new OpenSchemaView("testOpenSchemaView", "Open Schema View")); 
//TODO disbale temporary - there is huge memory leak!- IZ 98405        suite.addTest(new OpenSchemaView("testOpenComplexSchemaView", "Open Complex Schema View"));
//TODO it's the same as SwitchSchemaView, isn't it ?                                     suite.addTest(new SchemaViewSwitchTest("measureTime", "Schema View Switch"));
        
        suite.addTest(new BuildComplexProject("measureTime", "Build Complex Project"));
        
//TODO disbale temporary - there is huge memory leak!- IZ 98405        suite.addTest(new SwitchToDesignView("measureTime", "Schema | Switch to Design View"));
//TODO disbale temporary - there is huge memory leak!- IZ 98405        suite.addTest(new SwitchToSchemaView("measureTime", "Schema | Switch to Schema View"));
        suite.addTest(new SchemaNavigatorDesignView("measureTime", "Schema Navigator Design View"));
        suite.addTest(new SchemaNavigatorSchemaView("measureTime", "Schema Navigator Schema View"));
        suite.addTest(new NavigatorSchemaViewMode("measureTime","Schema Navigator Schema View mode"));
        
        suite.addTest(new ValidateSchema("measureTime","Validate Schema"));
//TODO there is an password dialog solve before enable to run again        suite.addTest(new DeployProject("measureTime","Deploy Project"));
         suite.addTest(new OpenComplexDiagram("measureTime","Open Complex Diagram"));         
         suite.addTest(new OpenBPELproject("measureTime","OpenBPELproject"));

        suite.addTest(new StartAppserver("measureTime","Start Appserver"));
        return suite;
    }
    
}
