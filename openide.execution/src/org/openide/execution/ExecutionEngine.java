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

package org.openide.execution;

import java.security.PermissionCollection;
import java.security.CodeSource;

import org.openide.windows.InputOutput;

/**
 * Engine providing the environment necessary to run long-lived processes.
 * May perform tasks such as setting up thread groups, etc.
 * Modules should not implement this class.
 * @author Jaroslav Tulach, Ales Novak
 */
public abstract class ExecutionEngine extends Object {

    /**
     * Run some task in the execution engine.
     * @param name a name of the new process
     * @param run a runnable to execute
     * @param io an I/O handle to automatically redirect system I/O streams in the dynamic scope of the task to,
     *           or null if no such redirection is required
     * @return an executor task that can control the execution
     */
    public abstract ExecutorTask execute(String name, Runnable run, InputOutput io);

    /** Trap accesses to
     * Users that want to link their classes with the IDE should do this through
     * internal execution. The {@link NbClassLoader} used in internal execution will assume that calling
     * this method and giving the permission collection to the class being defined will
     * trigger automatic redirection of system output, input, and error streams into the given I/O tab.
     * Implementations of the engine should bind the tab and returned permissions.
     * Since the permission collection is on the stack when calling methods on {@link System#out} etc.,
     * it is possible to find the appropriate tab for redirection.
     * @param cs code source to construct the permission collection for
     * @param io an I/O tab
     * @return a permission collection
     */
    protected abstract PermissionCollection createPermissions(CodeSource cs, InputOutput io);

    /** Method that allows implementor of the execution engine to provide
    * class path to all libraries that one could find useful for development
    * in the system.
    *
    * @return class path to libraries
     * @deprecated There are generally no excuses to be using this method as part of a normal module;
     * its exact meaning is vague, and probably not what you want.
    */
    protected abstract NbClassPath createLibraryPath ();
    
    /** Method to obtain default instance of the engine.
     * @return the engine
     * @since 2.16
     */
    public static ExecutionEngine getDefault () {
        return (ExecutionEngine)
            org.openide.util.Lookup.getDefault().lookup(ExecutionEngine.class);
    }

}
