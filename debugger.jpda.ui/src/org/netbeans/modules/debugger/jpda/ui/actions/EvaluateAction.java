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

package org.netbeans.modules.debugger.jpda.ui.actions;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.modules.debugger.jpda.ui.Evaluator;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;

/**
 * Invokes the expression evaluator GUI
 *
 * @author Martin Entlicher
 */
public class EvaluateAction extends CallableSystemAction {
    
    public String getName() {
        return NbBundle.getMessage(EvaluateAction.class, "CTL_Evaluate");
    }
    
    protected boolean asynchronous() {
        return false;
    }
    
    public boolean isEnabled() {
        DebuggerEngine de = DebuggerManager.getDebuggerManager().getCurrentEngine();
        if (de == null) return false;
        JPDADebugger debugger = (JPDADebugger) de.lookupFirst(null, JPDADebugger.class);
        if (debugger == null || debugger.getCurrentThread() == null) return false;
        return true;
    }
    
    public void performAction() {
        DebuggerEngine de = DebuggerManager.getDebuggerManager().getCurrentEngine();
        if (de == null) return ;
        JPDADebugger debugger = (JPDADebugger) de.lookupFirst(null, JPDADebugger.class);
        if (debugger == null) return ;
        Evaluator.open(debugger);
    }
    
    public HelpCtx getHelpCtx() {
        return null;
    }
    
}
