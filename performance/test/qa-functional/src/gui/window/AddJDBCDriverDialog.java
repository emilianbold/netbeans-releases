/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package gui.window;

import org.netbeans.jellytools.RuntimeTabOperator;
import org.netbeans.jellytools.nodes.Node;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JMenuBarOperator;
import org.netbeans.jemmy.operators.DialogOperator;

/**
 * Test of Add JDBC Driver dialog
 *
 * @author  anebuzelsky@netbeans.org
 */
public class AddJDBCDriverDialog extends testUtilities.PerformanceTestCase {
    
    /** Creates a new instance of ValidateAddJDBCDriverDialog */
    public AddJDBCDriverDialog(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
    }
    
    /** Creates a new instance of ValidateAddJDBCDriverDialog */
    public AddJDBCDriverDialog(String testName, String performanceDataName) {
        super(testName,performanceDataName);
        expectedTime = WINDOW_OPEN;
    }
    
    private Node thenode;
    
    protected void initialize() {
        // show Runtime tab and select Databases / Drivers node
        thenode = new Node (RuntimeTabOperator.invoke().getRootNode(), "Databases|Drivers");
        thenode.select();
    }
    
    public void prepare() {
        // do nothing
    }
    
    public ComponentOperator open() {
        // invoke Add Driver item from the popup
        thenode.callPopup().pushMenu("Add Driver ...");
        return new DialogOperator("Add JDBC Driver");
    }
    
}
