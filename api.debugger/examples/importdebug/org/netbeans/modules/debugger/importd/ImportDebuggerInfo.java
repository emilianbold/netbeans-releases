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

package org.netbeans.modules.debugger.importd;

import org.netbeans.modules.debugger.*;

/**
* Contains information about a class to debug.
* Consists of these pieces of information:
* <UL>
* <LI>the class to run
* <LI>parameters for its main method
* <LI>a class name to stop execution in, if desired
* </UL>
* Uses Import debugger.
*
* @author Jan Jancura
*/
public class ImportDebuggerInfo extends AbstractDebuggerInfo {

    /**
    * Construct a new <code>DebuggerInfo</code> with the class to run and its parameters specified.
    * Sets class to stop in to be the class to run.
    *
    * @param className name of debugged class
    * @param argv command-line arguments used for debugging this class; may be empty but not <code>null</code>
    */
    public ImportDebuggerInfo (
        String className,
        String[] argv,
        String stopClassName
    ) {
        super (
            className,
            argv,
            stopClassName
        );
    }
    
    /**
     * Return display name of debugged process.
     *
     * @return display name of debugged process
     */
    public String getProcessName () {
        return getClassName ();
    }
    
    /**
     * Return display name of location where this process will run.
     *
     * @return display name of location where this process will run
     */
    public String getLocationName () {
        return "localhost";
    }
    
    /**
     * Return type of debugger which should be used to debug this DebuggerInfo.
     *
     * @return type of debugger which should be used to debug this DebuggerInfo
     */
    public DebuggerImpl getDebuggerImpl () {
        Thread.dumpStack();
        return ImportDebuggerModule.getDebuggerImpl ();
    }
}
