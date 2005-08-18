/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.jmx.test.helpers;

import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NewFileNameLocationStepOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.jellytools.EditorOperator;


/**
 *
 * @author an156382
 */
public class CloseAllDocuments extends JellyTestCase {
    
    /** Creates a new instance of CloseAllDocuments */
    public CloseAllDocuments(String name) {
        super(name);
    }
    public static NbTestSuite suite() {
        
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new CloseAllDocuments("closeAll"));
        return suite;
    }
    
    /** Use for execution inside IDE */
    public static void main(java.lang.String[] args) {
        // run whole suite
        junit.textui.TestRunner.run(suite());
    }
    
    public void setUp() {
        
    }
    
    public void tearDown() {
        
    }
    
    public void closeAll() {
        String editorName = "Main";
        EditorOperator eo = new EditorOperator(editorName);
        eo.closeAllDocuments();
    }
}
