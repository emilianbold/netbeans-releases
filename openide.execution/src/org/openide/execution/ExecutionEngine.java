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

package org.openide.execution;

import java.security.AllPermission;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.security.Permissions;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.windows.IOProvider;
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

    /**
     * Users that want to link their classes with NetBeans module classes should do this through
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
    
    /**
     * Obtains default instance of the execution engine.
     * If default {@link Lookup} contains an instance of {@link ExecutionEngine},
     * that is used. Otherwise, a trivial basic implementation is returned with
     * the following behavior:
     * <ul>
     * <li>{@link #execute} just runs the runnable immediately and pretends to be done.
     * <li>{@link #createPermissions} just uses {@link AllPermission}. No I/O redirection
     *     or {@link System#exit} trapping is done.
     * <li>{@link #createLibraryPath} produces an empty path.
     * </ul>
     * This basic implementation is helpful in unit tests and perhaps in standalone usage
     * of other libraries.
     * @return some execution engine implementation (never null)
     * @since 2.16
     */
    public static ExecutionEngine getDefault() {
        ExecutionEngine ee = (ExecutionEngine) Lookup.getDefault().lookup(ExecutionEngine.class);
        if (ee == null) {
            ee = new Trivial();
        }
        return ee;
    }
    
    /**
     * Dummy fallback implementation, useful for unit tests.
     */
    static final class Trivial extends ExecutionEngine {
        
        public Trivial() {}

        protected NbClassPath createLibraryPath() {
            return new NbClassPath(new String[0]);
        }

        protected PermissionCollection createPermissions(CodeSource cs, InputOutput io) {
            PermissionCollection allPerms = new Permissions();
            allPerms.add(new AllPermission());
            allPerms.setReadOnly();
            return allPerms;
        }

        public ExecutorTask execute(String name, Runnable run, InputOutput io) {
            return new ET(run, name, io);
        }
        
        private static final class ET extends ExecutorTask {
            private RequestProcessor.Task task;
            private int resultValue;
            private final String name;
            private InputOutput io;
            
            public ET(Runnable run, String name, InputOutput io) {
                super(run);
                this.resultValue = resultValue;
                this.name = name;
                task = RequestProcessor.getDefault().post(this);
            }
            
            public void stop() {
                task.cancel();
            }
            
            public int result() {
                waitFinished();
                return resultValue;
            }
            
            public InputOutput getInputOutput() {
                return io;
            }
            
            public void run() {
                try {
                    super.run();
                } catch (RuntimeException x) {
                    x.printStackTrace();
                    resultValue = 1;
                }
            }
            
        }
        
    }

}
