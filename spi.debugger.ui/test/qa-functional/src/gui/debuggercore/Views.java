/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * The Original Code is NetBeans. 
 * The Initial Developer of the Original Code is Sun Microsystems, Inc. 
 * Portions created by Sun Microsystems, Inc. are Copyright (C) 2003
 * All Rights Reserved.
 *
 * Contributor(s): Sun Microsystems, Inc.
 */

package gui.debuggercore;

import junit.textui.TestRunner;
import org.netbeans.jellytools.*;
import org.netbeans.junit.NbTestSuite;

public class Views extends JellyTestCase {
    
    public Views(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new Views("testViews"));
        return suite;
    }
    
    /** Use for execution inside IDE */
    public static void main(java.lang.String[] args) {
        TestRunner.run(suite());
    }
    
    /** setUp method  */
    public void setUp() {
        System.out.println("########  " + getName() + "  #######");
    }
    
    /** tearDown method */
    public void tearDown() {
    }
    
    /**
     *
     */
    public void testViews() {
        String [] actionItems = new String [] { Utilities.localVarsItem, Utilities.watchesItem, 
            Utilities.callStackItem, Utilities.classesItem, Utilities.breakpointsItem, 
            Utilities.sessionsItem, Utilities.threadsItem };
            
        String [] viewTitles = new String [] { Utilities.localVarsViewTitle, Utilities.watchesViewTitle, 
            Utilities.callStackViewTitle, Utilities.classesViewTitle, Utilities.breakpointsViewTitle,
            Utilities.sessionsViewTitle, Utilities.threadsViewTitle };
         
        TopComponentOperator [] viewsOpers = new TopComponentOperator[actionItems.length];
            
            for (int i = 0; i < actionItems.length; i++) {
                Utilities.showDebuggerView(actionItems[i]);
                TopComponentOperator top = new TopComponentOperator(viewTitles[i]);
                viewsOpers[i] = top;
            }
            
            for (int j = 0; j < viewsOpers.length; j++) {
                viewsOpers[j].close();
            }
    }
    
}
