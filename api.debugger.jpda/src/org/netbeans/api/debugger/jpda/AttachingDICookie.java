/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.api.debugger.jpda;

import com.sun.jdi.Bootstrap;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.connect.AttachingConnector;
import com.sun.jdi.connect.Connector.Argument;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;


/**
 * Attaches to some already running JDK and returns VirtualMachine for it.
 *
 * <br><br>
 * <b>How to use it:</b>
 * <pre style="background-color: rgb(255, 255, 153);">
 *    DebuggerInfo di = DebuggerInfo.create (
 *        "My Attaching First Debugger Info", 
 *        new Object [] {
 *            AttachingDICookie.create (
 *                "localhost",
 *                1234
 *            )
 *        }
 *    );
 *    DebuggerManager.getDebuggerManager ().startDebugging (di);</pre>
 *
 * @author Jan Jancura
 */
public final class AttachingDICookie extends AbstractDICookie {

    /**
     * Public ID used for registration in Meta-inf/debugger.
     */
    public static final String ID = "netbeans-jpda-AttachingDICookie";

    private AttachingConnector attachingConnector;
    private Map<String,? extends Argument> args;

    
    private AttachingDICookie (
        AttachingConnector attachingConnector,
        Map<String,? extends Argument> args
    ) {
        this.attachingConnector = attachingConnector;
        this.args = args;
    }

    /**
     * Creates a new instance of AttachingDICookie for given parameters.
     *
     * @param attachingConnector a connector to be used
     * @param args map of arguments
     * @return a new instance of AttachingDICookie for given parameters
     */
    public static AttachingDICookie create (
        AttachingConnector attachingConnector,
        Map<String,? extends Argument> args
    ) {
        return new AttachingDICookie (
            attachingConnector, 
            args
        );
    }

    /**
     * Creates a new instance of AttachingDICookie for given parameters.
     *
     * @param hostName a name of computer to attach to
     * @param portNumber a potr number
     * @return a new instance of AttachingDICookie for given parameters
     */
    public static AttachingDICookie create (
        String hostName,
        int portNumber
    ) {
        return new AttachingDICookie (
            findAttachingConnector ("socket"),
            getArgs (
                findAttachingConnector ("socket"), 
                hostName, 
                portNumber
            )
        );
    }

    /**
     * Creates a new instance of AttachingDICookie for given parameters.
     *
     * @param name a name of shared memory block
     * @return a new instance of AttachingDICookie for given parameters
     */
    public static AttachingDICookie create (
        String name
    ) {
        return new AttachingDICookie (
            findAttachingConnector ("shmem"),
            getArgs (
                findAttachingConnector ("shmem"), 
                name
            )
        );
    }

    /** 
     * Returns instance of AttachingDICookie.
     *
     * @return instance of AttachingDICookie
     */
    public AttachingConnector getAttachingConnector () {
        return attachingConnector;
    }

    /**
     * Returns map of arguments.
     *
     * @return map of arguments
     */
    public Map<String,? extends Argument> getArgs () {
        return args;
    }

    /**
     * Returns port number.
     *
     * @return port number
     */
    public int getPortNumber () {
        Argument a = args.get ("port");
        if (a == null) return -1;
        String pn = a.value ();
        if (pn == null) return -1;
        return Integer.parseInt (pn);
    }

    /**
     * Returns name of computer.
     *
     * @return name of computer
     */
    public String getHostName () {
        Argument a = args.get ("hostname");
        if (a == null) return null;
        return a.value ();
    }

    /**
     * Returns shared memory block name.
     *
     * @return shared memory block name
     */
    public String getSharedMemoryName () {
        Argument a = args.get ("name");
        if (a == null) return null;
        return a.value ();
    }

    /**
     * Creates a new instance of VirtualMachine for this DebuggerInfo Cookie.
     *
     * @return a new instance of VirtualMachine for this DebuggerInfo Cookie
     */
    public VirtualMachine getVirtualMachine () throws IOException,
    IllegalConnectorArgumentsException {
        return attachingConnector.attach (args);
    }
    
    
    // private helper methods ..................................................

    private static Map<String,? extends Argument> getArgs (
        AttachingConnector attachingConnector,
        String hostName,
        int portNumber
    ) {
        Map<String,? extends Argument> args = attachingConnector.defaultArguments ();
        args.get ("hostname").setValue (hostName);
        args.get ("port").setValue ("" + portNumber);
        return args;
    }

    private static Map<String,? extends Argument> getArgs (
        AttachingConnector attachingConnector,
        String name
    ) {
        Map<String,? extends Argument> args = attachingConnector.defaultArguments ();
        args.get ("name").setValue (name);
        return args;
    }
    
    private static AttachingConnector findAttachingConnector (String s) {
        Iterator<AttachingConnector> iter = Bootstrap.virtualMachineManager ().
            attachingConnectors ().iterator ();
        while (iter.hasNext ()) {
            AttachingConnector ac = iter.next ();
            if (ac.transport() != null && ac.transport ().name ().toLowerCase ().indexOf (s) > -1)
                return ac;
        }
        return null;
    }
}
