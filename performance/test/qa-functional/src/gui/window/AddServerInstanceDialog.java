/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package gui.window;

import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.RuntimeTabOperator;
import org.netbeans.jellytools.nodes.Node;

import org.netbeans.jemmy.operators.ComponentOperator;

/**
 * Test of Add Server Instance dialog
 *
 * @author  mmirilovic@netbeans.org
 */
public class AddServerInstanceDialog extends org.netbeans.performance.test.utilities.PerformanceTestCase {
    
    private String BUNDLE, MENU, TITLE;
    private Node thenode;
    
    /** Creates a new instance of AddServerInstanceDialog */
    public AddServerInstanceDialog(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
    }
    
    /** Creates a new instance of AddServerInstanceDialog */
    public AddServerInstanceDialog(String testName, String performanceDataName) {
        super(testName,performanceDataName);
        expectedTime = WINDOW_OPEN;
    }
    
    public void initialize() {
        //TODO BUNDLE = "org.netbeans.modules.db.resources.Bundle";
        MENU = "Add Server..."; //TODO org.netbeans.jellytools.Bundle.getStringTrimmed(BUNDLE, "AddNewDriver");
        TITLE = "Add Server Instance"; //TODO org.netbeans.jellytools.Bundle.getStringTrimmed(BUNDLE, "AddDriverDialogTitle");
        
        String path = "Servers"; //TODO org.netbeans.jellytools.Bundle.getStringTrimmed(BUNDLE, "NDN_Databases") + "|" + org.netbeans.jellytools.Bundle.getStringTrimmed(BUNDLE, "NDN_Drivers");
        
        // show Runtime tab and select Servers
        thenode = new Node (RuntimeTabOperator.invoke().getRootNode(), path);
        thenode.select();
    }
    
    public void prepare() {
        // do nothing
    }
    
    public ComponentOperator open() {
        // invoke Add Servers item from the popup
        thenode.callPopup().pushMenu(MENU);
        return new NbDialogOperator(TITLE);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(new AddServerInstanceDialog("measureTime"));
    }
    
}
