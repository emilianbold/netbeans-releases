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

package org.netbeans.core.output2;

import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/** The action which shows standard IO component.
*
* @author Dafe Simonek
*/
public final class OutputWindowAction extends CallableSystemAction {

    public void performAction() {
        OutputWindow output = OutputWindow.findDefault();
        output.open();
        output.requestActive();
    }
    
    protected boolean asynchronous() {
        return false;
    }

    public String getName() {
        return NbBundle.getBundle(OutputWindowAction.class).getString("OutputWindow");
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx (OutputWindowAction.class);
    }

    protected String iconResource () {
        return "org/netbeans/core/resources/frames/output.gif"; // NOI18N
    }
}
