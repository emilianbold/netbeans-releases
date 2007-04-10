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
import gui.window.*;

/**
 * Measure UI-RESPONSIVENES and WINDOW_OPENING.
 *
 * @author  mkhramov@netbeans.org
 */

public class VWPMeasureDialogs {
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();

        suite.addTest(new AddComponentLibraryDialog("measureTime","Add Component Library Dialog open"));
        suite.addTest(new PageStyleSheetDialog("measureTime","Page Stylesheet Dialog open"));
        suite.addTest(new PageFragmentBoxDialog("measureTime","Add Page Fragment Box Dialog open")); 
        
        suite.addTest(new VirtualFormsDialog("measureTime","Virtual Forms Dialog open"));
        suite.addTest(new TableLayoutOptionsDialog("measureTime","Table Layout Options Dialog open"));
        suite.addTest(new DataBindingDialog("measureTime","Data Binding Dialog open"));        
        suite.addTest(new ConfigureDefaultOptionsDialog("measureTime","List Default Options Dialog open"));
        suite.addTest(new PropertyBindingDialog("measureTime","Property Binding Dialog open"));
        suite.addTest(new ComponentStyleDialog("testButtonStyleDialog","Button Style Dialog Open test"));
        suite.addTest(new ComponentStyleDialog("testTableStyleDialog","Table Style Dialog Open test"));        
        suite.addTest(new ComponentStyleDialog("testLisbBoxStyleDialog","Listbox Style Dialog Open test"));        
        suite.addTest(new ManageComponentLibrariesDialog("measureTime","Manage Component Libraries Dialog open"));
        
       return suite; 
    }
    
}
