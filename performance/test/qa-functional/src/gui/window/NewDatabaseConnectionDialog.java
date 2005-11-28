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

import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.RuntimeTabOperator;
import org.netbeans.jellytools.nodes.Node;

import org.netbeans.jemmy.operators.ComponentOperator;


/**
 * Test of New Database Connection dialog
 *
 * @author  anebuzelsky@netbeans.org, mmirilovic@netbeans.org
 */
public class NewDatabaseConnectionDialog extends org.netbeans.performance.test.utilities.PerformanceTestCase {
    
    private Node thenode;
    private String BUNDLE, MENU, TITLE;
    
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
    
    public void initialize() {
        BUNDLE = "org.netbeans.modules.db.resources.Bundle";
        MENU = Bundle.getStringTrimmed(BUNDLE,"ConnectUsing");
        TITLE = Bundle.getStringTrimmed(BUNDLE,"NewConnectionDialogTitle");
        
        String NODE = Bundle.getStringTrimmed(BUNDLE,"NDN_Databases") + "|" + Bundle.getStringTrimmed(BUNDLE,"NDN_Drivers") + "|" + "JDBC-ODBC Bridge"; //NOI18N impossible
        // show Runtime tab and select Databases / Drivers /JDBC-ODBC Bridge node
        thenode = new Node (RuntimeTabOperator.invoke().getRootNode(), NODE);
        thenode.select();
    }
    
    public void prepare() {
        // do nothing
    }
        
    public ComponentOperator open() {
        // invoke Connect Using from the popup
        thenode.callPopup().pushMenu(MENU);
        return new NbDialogOperator(TITLE);
    }
    
}
