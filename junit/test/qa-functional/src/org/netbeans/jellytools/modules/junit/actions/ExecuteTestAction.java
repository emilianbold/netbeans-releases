/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.jellytools.modules.junit.actions;

import org.netbeans.jellytools.actions.*;
import java.awt.event.KeyEvent;
import org.netbeans.jellytools.Bundle;

/** ExecuteTestAction Class
 * @author dave
 */
public class ExecuteTestAction extends Action {

    public static final String MENU = Bundle.getString("org.netbeans.core.Bundle", "Actions/Tools") + "|" + Bundle.getString("org.netbeans.modules.junit.Bundle", "LBL_Action_Tests") + "|" + Bundle.getString("org.netbeans.modules.junit.Bundle", "LBL_Action_RunTest");

    /** creates new ExecuteTestAction instance */
    public ExecuteTestAction() {
        super(MENU, MENU, new Action.Shortcut(KeyEvent.CTRL_MASK|KeyEvent.ALT_MASK, KeyEvent.VK_L));
    }
}
