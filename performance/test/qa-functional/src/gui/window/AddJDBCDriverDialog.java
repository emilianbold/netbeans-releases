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

import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.RuntimeTabOperator;
import org.netbeans.jellytools.nodes.Node;

import org.netbeans.jemmy.operators.ComponentOperator;

/**
 * Test of Add JDBC Driver dialog
 *
 * @author  anebuzelsky@netbeans.org, mmirilovic@netbeans.org
 */
public class AddJDBCDriverDialog extends org.netbeans.performance.test.utilities.PerformanceTestCase {
    
    private String BUNDLE, MENU, TITLE;
    private Node thenode;
    
    /** Creates a new instance of AddJDBCDriverDialog */
    public AddJDBCDriverDialog(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
    }
    
    /** Creates a new instance of AddJDBCDriverDialog */
    public AddJDBCDriverDialog(String testName, String performanceDataName) {
        super(testName,performanceDataName);
        expectedTime = WINDOW_OPEN;
    }
    
    public void initialize() {
        BUNDLE = "org.netbeans.modules.db.resources.Bundle";
        MENU = org.netbeans.jellytools.Bundle.getStringTrimmed(BUNDLE, "AddNewDriver");
        TITLE = org.netbeans.jellytools.Bundle.getStringTrimmed(BUNDLE, "AddDriverDialogTitle");
        
        String path = org.netbeans.jellytools.Bundle.getStringTrimmed(BUNDLE, "NDN_Databases") + "|" + org.netbeans.jellytools.Bundle.getStringTrimmed(BUNDLE, "NDN_Drivers");
        // show Runtime tab and select Databases / Drivers node
        thenode = new Node (RuntimeTabOperator.invoke().getRootNode(), path);
        thenode.select();
    }
    
    public void prepare() {
        // do nothing
    }
    
    public ComponentOperator open() {
        // invoke Add Driver item from the popup
        thenode.callPopup().pushMenu(MENU);
        return new NbDialogOperator(TITLE);
    }
    
}
