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
 * Test of New Database Connection dialog
 *
 * @author  anebuzelsky@netbeans.org
 */
public class NewDatabaseConnectionDialog extends testUtilities.PerformanceTestCase {
    
    /** Creates a new instance of NewDatabaseConnectionDialog */
    public NewDatabaseConnectionDialog(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
    }
    
    /** Creates a new instance of NewDatabaseConnectionDialog */
    public NewDatabaseConnectionDialog(String testName, String performanceDataName) {
        super(testName,performanceDataName);
        expectedTime = WINDOW_OPEN;
    }
    
    private Node thenode;
    
    protected void initialize() {
        // show Runtime tab and select Databases / Drivers /JDBC-ODBC Bridge node
        thenode = new Node (RuntimeTabOperator.invoke().getRootNode(), "Databases|Drivers|JDBC-ODBC Bridge");
        thenode.select();
    }
    
    public void prepare() {
        // do nothing
    }
        
    public ComponentOperator open() {
        // invoke Connect Using from the popup
        thenode.callPopup().pushMenu("Connect Using ...");
        return new DialogOperator("New Database Connection");
    }
    
}
