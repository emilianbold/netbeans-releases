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


import org.netbeans.junit.NbTestSuite;
import gui.window.*;
import org.netbeans.junit.NbTestCase;

/**
 * Measure UI-RESPONSIVENES and WINDOW_OPENING.
 *
 * @author  lmartinek@netbeans.org
 */
public class MeasureJ2EEDialogs {


    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();

        suite.addTest(new SelectJ2EEModuleDialog("measureTime", "Select J2EE Module Dialog open"));
        suite.addTest(new InvokeEJBAction("testAddBusinessMethodDialog", "Add Business method Dialog open"));
        suite.addTest(new InvokeEJBAction("testCallEJBDialog", "Call EJB Dialog open"));
        //Disabled because JAX-RPC issues in NB6.0 - enable again when fixed
        suite.addTest(new InvokeWSAction("testAddOperationDialog", "Add Operation Dialog open"));

        return suite;
    }
    
}
