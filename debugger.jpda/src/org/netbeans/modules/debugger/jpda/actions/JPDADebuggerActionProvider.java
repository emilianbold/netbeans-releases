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

package org.netbeans.modules.debugger.jpda.actions;

import com.sun.jdi.ThreadReference;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.InvalidRequestStateException;
import com.sun.jdi.request.StepRequest;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.spi.debugger.ActionsProviderSupport;


/**
* Representation of a debugging session.
*
* @author   Jan Jancura
* @author  Marian Petras
*/
abstract class JPDADebuggerActionProvider extends ActionsProviderSupport 
implements PropertyChangeListener {
    
    private static boolean verbose = 
        System.getProperty ("netbeans.debugger.jdievents") != null;

    private JPDADebuggerImpl debugger;
    
    JPDADebuggerActionProvider (JPDADebuggerImpl debugger) {
        this.debugger = debugger;
        debugger.addPropertyChangeListener (debugger.PROP_STATE, this);
    }
    
    public void propertyChange (PropertyChangeEvent evt) {
        checkEnabled (debugger.getState ());
    }
    
    protected abstract void checkEnabled (int debuggerState);
    
    public boolean isEnabled (Object action) {
        checkEnabled (debugger.getState ());
        return super.isEnabled (action);
    }
    
    JPDADebuggerImpl getDebuggerImpl () {
        return debugger;
    }
    
    void removeStepRequests (ThreadReference tr) {
        //S ystem.out.println ("removeStepRequests");
        try {
            VirtualMachine vm = getDebuggerImpl ().getVirtualMachine ();
            if (vm == null) return;
            EventRequestManager erm = vm.eventRequestManager ();
            ArrayList l = new ArrayList (erm.stepRequests ());
            Iterator it = l.iterator ();
            while (it.hasNext ()) {
                StepRequest stepRequest = (StepRequest) it.next ();
                if (stepRequest.thread ().equals (tr)) {
                    //S ystem.out.println("  remove request " + stepRequest);
                    erm.deleteEventRequest (stepRequest);
                    break;
                }
                //S ystem.out.println("  do not remove " + stepRequest + " : " + stepRequest.thread ());
            }
        } catch (VMDisconnectedException e) {
        } catch (IllegalThreadStateException e) {
            e.printStackTrace();
        } catch (InvalidRequestStateException e) {
            e.printStackTrace();
        }
    }
}
