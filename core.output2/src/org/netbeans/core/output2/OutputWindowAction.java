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
        ViewContainer output = ViewContainer.findDefault();
        if (!output.isDisplayable()) {
            output = findOutputComponent();
            if (output != null) {
                //OutputView should be defined in XML layer so default
                //code flow is here.
                output.open();
                output.requestActive();
            } else {
                //If OutputView is not defined in XML layer (or is not already present
                //in some mode get instance and dock it to default "output" mode.
                output = ViewContainer.findDefault();
                if (!output.isShowing()) {
                    Mode mode = WindowManager.getDefault().getCurrentWorkspace().findMode("output"); // NOI18N
                    if (mode == null) {
                        //Mode output not found, create it.
                        String displayName = NbBundle.getBundle(OutputWindowAction.class).getString("CTL_OutputWindow_OutputTab");
                        mode = WindowManager.getDefault().getCurrentWorkspace().createMode("output", displayName, null);
                    }
                    //Dock OutputView to "output" mode.
                    mode.dockInto(output);
                    output.open();
                }
            }
        }
        output.requestActive();
        
        //String name = NbBundle.getBundle(OutputWindowAction.class).getString("CTL_OutputWindow_OutputTab");
        //InputOutput io = output.getIO(name,false);
        //io.select();
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
    
    // PENDING When the output will be singleton (new UI spec), remove this method.
    /** Finds last selected component of output family */
    private static ViewContainer findOutputComponent() {
        Mode mode = WindowManager.getDefault().getCurrentWorkspace().findMode("output"); // NOI18N

        if (mode != null) {
            TopComponent[] tcs = mode.getTopComponents();
            for (int i = 0; i < tcs.length; i++) {
                if (tcs[i] instanceof ViewContainer) {
                    return (ViewContainer) tcs[i];
                }
            }
        }
        
        return null;
    }

}
