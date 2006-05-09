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

package org.netbeans.core.execution;

import java.util.HashMap;
import java.util.Hashtable;

import org.openide.windows.InputOutput;
import org.openide.windows.TopComponent;

/** Tasks are supposed to obey following model: every task is a ThreadGroup
* and the ThreadGroup is under another ThreadGroup - called "base".
* Systems threads are not under group base.
* The table keeps couples ThreadGroup:TaskIO; each task is supposed
* to be encapsulate by a ThreadGroup; system's threads have special
* handling @see #systemIO
* Some tasks don't require io operations. For such tasks NullTaskIO is
* created (at ExecutionEngine.RunClass.run()); NullTaskIOs left reusing TaskIOs
*
* @author Ales Novak
*/
final class IOTable extends Hashtable<InputOutput,TaskIO> {
    /** generated Serialized Version UID */
    static final long serialVersionUID = 9096333712401558521L;

    /** ThreadGroup of all tasks */
    private ThreadGroup base;

    /** TaskIO of system's threads */
    private TaskIO systemIO;

    /** hashtable of free TaskIOs - name:TaskIO */
    private HashMap<String, TaskIO> freeTaskIOs;

    /**
    * @param base is a base ThreadGroup for tasks
    * @param systemIO is a TaskIO instance that is used for system threads
    */
    public IOTable(ThreadGroup base, TaskIO systemIO) {
        this.base = base;
        this.systemIO = systemIO;
        freeTaskIOs = new HashMap<String, TaskIO>(16);
    }

    /** finds top thread group of the calling thread
    * @return null iff the calling thread is not in any exec group
    * or exec group of calling thread
    */
    ThreadGroup findGroup () {
        ThreadGroup g = Thread.currentThread().getThreadGroup ();
        ThreadGroup old = null;
        while (g != null && g != base) {
            old = g;
            g = g.getParent ();
        }
        return (g == null) ? null : old;
    }
    
    private boolean searchingIO = false;

    /**
    * @return TaskIO specific for calling thread/threadgroup
    */
    private synchronized TaskIO getIO() {
        
        if (searchingIO) {
            return systemIO;
        }

        InputOutput inout = null;

        if (Thread.currentThread() instanceof IOThreadIfc) {
            inout = ((IOThreadIfc) Thread.currentThread()).getInputOutput();
        }

        IOPermissionCollection iopc = null;

        if (inout == null) {
            try {
                searchingIO = true;
                iopc = AccController.getIOPermissionCollection();
            } finally {
                searchingIO = false;
            }
            if (iopc == null) {
                return systemIO;
            }
            inout = iopc.getIO();
        }

        TaskIO io = (TaskIO) get(inout);

        // this piece of source is duplicated in exec engine
        // needed when classloader defines a class with an InpuOutput
        // but the classloader does not work on behalf of execution

        // following code is executed only if a task is dead but
        // some classes behave like Phoenix - they live again
        if (io == null) {
            if (inout instanceof TopComponent) {
                String allName = ((TopComponent) inout).getName();
                io = getTaskIO(allName);
                if (io == null) { // executed for the first time
                    inout = org.openide.windows.IOProvider.getDefault().getIO(allName, true);
                    io = new TaskIO(inout, allName);
                } else {
                    inout = io.getInout();
                }
                inout.select();
                inout.setFocusTaken(true);
                if ( iopc!= null) { // IOThreadIfc case
                    iopc.setIO(inout);
                }
                put(inout, io);
            } else {
                return new TaskIO(inout); // foreign inout - just return a TaskIO
            }
        }

        return io;
    }

    /**
    * @param name is a name of the tab
    * @return TaskIO
    */
    synchronized TaskIO getTaskIO(String name) {
        TaskIO ret;
        if (reuseTaskIO())
            if ((ret = getFreeTaskIO(name)) != null) return ret;
        return null;
    }

    /**
    * @return true iff TaskIO are to be reused
    */
    private boolean reuseTaskIO() {
        return true;
    }

    /**
    * @return true iff reused TaskIO should be reseted
    */
    private boolean clearTaskIO() {
        return true;
    }

    /**
    * @return free non-used TaskIO with given name or null
    */
    private TaskIO getFreeTaskIO(String name) {
        TaskIO t = (TaskIO) freeTaskIOs.get(name);
        if (t == null) {
            return null;
        }
        if (clearTaskIO()) {
            try {
                t.getInout().getOut().reset();
                t.getInout().getErr().reset();
            } catch (java.io.IOException e) {
            }
        }
        t.in = null;
        t.getInout().flushReader();
        freeTaskIOs.remove(name);
        return t;
    }

    /** frees resources binded to grp
    * @param grp is a ThreadGroup which TaskIO is to be released
    * @param io key for freed TaskIO
    */
    synchronized void free(ThreadGroup grp, InputOutput io) {
        TaskIO t = (TaskIO) get(io);
        if (t == null) {
            return; // nothing ??
        } else if (t.foreign) {
            return;
        }
        if (t.getName() != TaskIO.VOID) { // Null
            t = (TaskIO) freeTaskIOs.put(t.getName(), t); // free it
            if (t != null) {
                t.getInout().closeInputOutput();  // old one destroy
            }
        }
        remove(io);
    }

    /**
    * @return threadgroup specific Reader
    */
    public java.io.Reader getIn() {
        TaskIO io = getIO();
        if (io.in == null) {
            io.initIn();
        }
        return io.in;
    }

    /**
    * @return thread specific OutputWriter
    * Two calls in the same threadgroup will return the same PrintStream
    */
    public java.io.Writer getOut() {
        TaskIO io = getIO();
        if (io.out == null) {
            io.initOut();
        }
        return io.out;
    }

    /**
    * @return thread specific OutputWriter
    * Two calls in the same threadgroup will return the same PrintStream
    */
    public java.io.Writer getErr() {
        TaskIO io = getIO();
        if (io.err == null) {
            io.initErr();
        }
        return io.err;
    }
    
}
