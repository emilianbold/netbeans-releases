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

package gui.window;

import org.netbeans.jellytools.NewFileWizardOperator;
import org.netbeans.jellytools.actions.NewFileAction;

import org.netbeans.jemmy.operators.ComponentOperator;

/**
 * Test of New File Dialog
 *
 * @author  mmirilovic@netbeans.org
 */
public class NewFileDialog extends org.netbeans.performance.test.utilities.PerformanceTestCase {

    /** Creates a new instance of NewFileDialog */
    public NewFileDialog(String testName) {
        super(testName);
        expectedTime = 1821; // 4.1 : 1614, N/A, 1821, 1752, 1182, 1297
    }
    
    /** Creates a new instance of NewFileDialog */
    public NewFileDialog(String testName, String performanceDataName) {
        super(testName,performanceDataName);
        expectedTime = 1821; // 4.1 : 1614, N/A, 1821, 1752, 1182, 1297
    }
    
    public void prepare() {
        // do nothing
        gui.Utilities.workarroundMainMenuRolledUp();
    }
    
    public ComponentOperator open() {
        // invoke File / Open File from the main menu
        new NewFileAction().performMenu();
        return new NewFileWizardOperator();
    }
    
}
