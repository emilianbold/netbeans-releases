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

import com.sun.jdi.Bootstrap;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.jdi.connect.ListeningConnector;
import com.sun.jdi.connect.Connector.Argument;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

/**
 * Listens on given port for some connection of remotely running JDK
 * and returns VirtualMachine for it.
 *
 * <br><br>
 * <b>How to use it:</b>
 * <pre style="background-color: rgb(255, 255, 153);">
 *    DebuggerInfo di = DebuggerInfo.create (
 *        "My First Listening Debugger Info",
 *        new Object [] {
 *            ListeningDICookie.create (
 *                1234
 *            )
 *        }
 *    );
 *    DebuggerManager.getDebuggerManager ().startDebugging (di);</pre>
 *
 * @author Jan Jancura
 */
public final class ListeningDICookie extends AbstractDICookie {

    /**
     * Public ID used for registration in Meta-inf/debugger.
     */
    public static final String ID = "netbeans-jpda-ListeningDICookie";

    private ListeningConnector listeningConnector;
    private Map args;

    private ListeningDICookie (
        ListeningConnector listeningConnector,
        Map args
    ) {
        this.listeningConnector = listeningConnector;
        this.args = args;
    }

    /**
     * Creates a new instance of ListeningDICookie for given parameters.
     *
     * @param listeningConnector a instance of ListeningConnector
     * @param args arguments to be used
     * @return a new instance of ListeningDICookie for given parameters
     */
    public static ListeningDICookie create (
        ListeningConnector listeningConnector,
        Map args
    ) {
        return new ListeningDICookie (
            listeningConnector,
            args
        );
    }

    /**
     * Creates a new instance of ListeningDICookie for given parameters.
     *
     * @param portNumber a number of port to listen on
     * @return a new instance of ListeningDICookie for given parameters
     */
    public static ListeningDICookie create (
        int portNumber
    ) {
        return new ListeningDICookie (
            findListeningConnector ("socket"),
            getArgs (
                findListeningConnector ("socket"),
                portNumber
            )
        );
    }

    /**
     * Creates a new instance of ListeningDICookie for given parameters.
     *
     * @param name a name of shared memory block to listen on
     * @return a new instance of ListeningDICookie for given parameters
     */
    public static ListeningDICookie create (
        String name
    ) {
        return new ListeningDICookie (
            findListeningConnector ("socket"),
            getArgs (
                findListeningConnector ("socket"),
                name
            )
        );
    }

    private static ListeningConnector findListeningConnector (String s) {
        Iterator iter = Bootstrap.virtualMachineManager ().
            listeningConnectors ().iterator ();
        while (iter.hasNext ()) {
            ListeningConnector ac = (ListeningConnector) iter.next ();
            if (ac.transport() != null && ac.transport ().name ().toLowerCase ().indexOf (s) > -1)
                return ac;
        }
        return null;
    }

    private static Map getArgs (
        ListeningConnector listeningConnector,
        int portNumber
    ) {
        Map args = listeningConnector.defaultArguments ();
        ((Argument) args.get ("port")).setValue ("" + portNumber);
        return args;
    }

    private static Map getArgs (
        ListeningConnector listeningConnector,
        String name
    ) {
        Map args = listeningConnector.defaultArguments ();
        ((Argument) args.get ("name")).setValue (name);
        return args;
    }

    /**
     * Returns instance of ListeningConnector.
     *
     * @return instance of ListeningConnector
     */
    public ListeningConnector getListeningConnector () {
        return listeningConnector;
    }

    /**
     * Returns map of arguments to be used.
     *
     * @return map of arguments to be used
     */
    public Map getArgs () {
        return args;
    }

    /**
     * Returns port number.
     *
     * @return port number
     */
    public int getPortNumber () {
        Argument a = (Argument) args.get ("port");
        if (a == null) return -1;
        String pn = a.value ();
        if (pn == null) return -1;
        try {
            return Integer.parseInt (pn);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * Returns shared memory block name.
     *
     * @return shared memory block name
     */
    public String getSharedMemoryName () {
        Argument a = (Argument) args.get ("name");
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
        try {
            try {
                listeningConnector.startListening(args); 
            } catch (Exception e) {
                // most probably already listening
            }
            return listeningConnector.accept (args);
        } finally {
            try {
                listeningConnector.stopListening(args);
            } catch (Exception e) {
                // most probably not listening anymore                
            }
        }
    }
}
