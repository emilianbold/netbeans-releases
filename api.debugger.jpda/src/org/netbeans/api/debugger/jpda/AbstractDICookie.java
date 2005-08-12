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

package org.netbeans.api.debugger.jpda;

import com.sun.jdi.VirtualMachine;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.jdi.connect.VMStartException;
import java.io.IOException;

/**
 * Abstract ancestor of all {@link org.netbeans.api.debugger.DebuggerInfo} 
 * Cookies. DebuggerInfo Cookie is responsible for creating of new JPDA 
 * VirtualMachine.
 *
 * <br><br>
 * <b>How to use it:</b>
 * <pre style="background-color: rgb(255, 255, 153);">
 *    DebuggerInfo di = DebuggerInfo.create (
 *        "My First Debugger Info", 
 *        new Object [] {
 *            abstractDICookieInstance
 *        }
 *    );
 *    DebuggerManager.getDebuggerManager ().startDebugging (di);</pre>
 *
 * @see AttachingDICookie
 * @see LaunchingDICookie
 * @see ListeningDICookie
 *
 * @author Jan Jancura
 */
public abstract class AbstractDICookie {
    
    /**
     * Creates a new instance of VirtualMachine for this DebuggerInfo Cookie.
     *
     * @return a new instance of VirtualMachine for this DebuggerInfo Cookie
     * @throws java.net.ConnectException When a connection is refused
     */
    public abstract VirtualMachine getVirtualMachine () throws IOException,
    IllegalConnectorArgumentsException, VMStartException;
}
