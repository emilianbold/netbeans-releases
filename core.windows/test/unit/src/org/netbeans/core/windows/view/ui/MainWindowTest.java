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

package org.netbeans.core.windows.view.ui;

import org.netbeans.junit.NbTestCase;
import org.openide.util.NbBundle;

/**
 * Some basic tests for MainWindow.
 */
public class MainWindowTest extends NbTestCase {

    public MainWindowTest(String testName) {
        super(testName);
    }

    protected boolean runInEQ () {
        return true;
    }

    protected void setUp() throws Exception {
    }

    public void testBrandingTokensExist() {
        // API support creates branding for these tokens to set application title
        assertNotNull("Main window title without projects exists", 
                NbBundle.getMessage(MainWindow.class, "CTL_MainWindow_Title_No_Project"));
        assertNotNull("Main window title without projects exists", 
                NbBundle.getMessage(MainWindow.class, "CTL_MainWindow_Title"));
    }
    
}
