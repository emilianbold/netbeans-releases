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

import org.netbeans.jellytools.actions.MaximizeWindowAction;
import org.netbeans.jellytools.actions.RestoreWindowAction;

import org.netbeans.jellytools.nodes.Node;

import org.netbeans.jemmy.operators.Operator;
import org.netbeans.jemmy.operators.ComponentOperator;


/**
 * Test of Set Default Server Dialog
 *
 * @author  anebuzelsky@netbeans.org, mmirilovic@netbeans.org
 */
public class SetDefaultServerDialog extends testUtilities.PerformanceTestCase {
    
    private String MENU, TITLE;
    
    /** Creates a new instance of SetDefaultServerDialog */
    public SetDefaultServerDialog(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
    }
    
    /** Creates a new instance of SetDefaultServerDialog */
    public SetDefaultServerDialog(String testName, String performanceDataName) {
        super(testName,performanceDataName);
        expectedTime = WINDOW_OPEN;
    }
    
    private RuntimeTabOperator thetab;
    private Node thenode;
    
    public void initialize() {
        MENU = org.netbeans.jellytools.Bundle.getStringTrimmed("org.netbeans.modules.j2ee.deployment.impl.ui.actions.Bundle","LBL_SetDefaultServer");
        TITLE = org.netbeans.jellytools.Bundle.getStringTrimmed("org.netbeans.modules.j2ee.deployment.impl.ui.actions.Bundle","LBL_SetDefaultDlgTitle");
        
        // show Runtime tab, maximize it and select Server Registry node
        thetab = RuntimeTabOperator.invoke();
        new MaximizeWindowAction().performAPI(thetab);
        
        Operator.StringComparator thecomparator = new Operator.DefaultStringComparator(false, false);
        thenode = new Node (thetab.tree(), thetab.tree().findPath("Server Registry", thecomparator)); //NOI18N impossible
        thenode.setComparator(thecomparator);
        thenode.select();
    }
    
    public void prepare() {
        // do nothing
    }
    
    public ComponentOperator open() {
        // invoke Add new server instance using URL item from the popup
        thenode.callPopup().pushMenu(MENU);
        return new NbDialogOperator(TITLE);
    }
    
    public void shutdown() {
        // restore the original size of Runtime tab
        new RestoreWindowAction().performAPI(thetab);
    }
}
