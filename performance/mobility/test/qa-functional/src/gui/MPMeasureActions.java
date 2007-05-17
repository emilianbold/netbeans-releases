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

import gui.actions.*;
import org.netbeans.junit.NbTestSuite;

/**
 * Measure UI-RESPONSIVENES and WINDOW_OPENING.
 *
 * @author  mmirilovic@netbeans.org  , rashid@netbeans.org
 */
public class MPMeasureActions  {

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();

        // TODO add some test cases
        suite.addTest(new CreateMobilityProject("testCreateMobilityProject", "Create Mobile Application"));  
        suite.addTest(new CreateMobilityProject("testCreateMobilityLibrary", "Create Mobile Class Library"));
        suite.addTest(new OpenMIDletEditor("measureTime","Open a visual MIDlet"));
        suite.addTest(new MIDletViewsSwitch("testFlowToDesignSwitch","Flow To Design Switch"));
        suite.addTest(new MIDletViewsSwitch("testDesignToFlowSwitch","Design To Flow Switch"));
        suite.addTest(new MIDletViewsSwitch("testFlowToSourceSwitch","Flow To Source Switch"));
        suite.addTest(new MIDletViewsSwitch("testSourceToFlowSwitch","Source To Flow Switch"));
        suite.addTest(new CreateVisualMIDlet("measureTime","Create Visual MIDlet"));
        suite.addTest(new CreateMIDlet("measureTime","Create MIDlet"));
        suite.addTest(new SwitchConfiguration("measureTime","Switch Configuration"));
        suite.addTest(new OpenMobileproject("measureTime","Open Mobile CLDC project "));
        return suite;
    }
    
}
