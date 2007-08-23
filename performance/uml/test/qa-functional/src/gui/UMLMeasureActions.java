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
 * @author  mmirilovic@netbeans.org
 */
public class UMLMeasureActions  {

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
             
        suite.addTest(new OpenUMLProject("measureTime", "Open UML Project"));
        suite.addTest(new OpenUMLDiagram("measureTime", "Open UML Diagram"));
        suite.addTest(new ScrollExpandedProject("measureTime", "Scroll Expanded Project"));
        suite.addTest(new SelectingMultipleNodes("measureTime", "Selecting Multiple Nodes"));
        suite.addTest(new CreateClassDiagramFromMultipleNodes("measureTime", "Create Class Diagram From Multiple Nodes")); 
        suite.addTest(new CreateSequenceDiagramFromMultipleNodes("measureTime", "Create Sequence Diagram From Multiple Nodes"));
        suite.addTest(new CreateEmptyDiagram("measureTime", "Create Empty UML Diagram"));        
        suite.addTest(new GenerateDependencyDiagram("measureTime", "Generate Dependency Diagram"));

        suite.addTest(new ReverseEngineering("measureTime", "Reverse Engineering"));

/* Stability issues, will be enabled later...
        suite.addTest(new GenerateModelReport("measureTime", "Generate Model Report"));
        suite.addTest(new CodeGenerationFromUMLProject("measureTime", "Code Generation From UML Project"));
        suite.addTest(new ApplyDesignPattern("measureTime", "Apply Design Pattern"));
*/

        return suite;
    }
    
}
