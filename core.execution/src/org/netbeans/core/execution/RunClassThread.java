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

package org.netbeans.core.execution;

import org.openide.windows.InputOutput;

/** Simple class for executing tasks in extra threads */
final class RunClassThread extends Thread implements IOThreadIfc {

    /** InputOutput that is to be used */
    private InputOutput io;
    /** name */
    String allName; // used in innerclass Runnable
    /** reference to outer class */
    private final ExecutionEngine engine;
    /** ref to a Task */
    private final ExecutorTaskImpl task;
    /** Task to run */
    private /*final*/ Runnable run;

    /** generated names */
    static int number = 0;

    /** Created group */
    TaskThreadGroup mygroup;
    
    /** Is finalized? */
    boolean finalized;

    /**
    * @param base is a ThreadGroup we want to be in
    * @param m is a method to invoke
    * @param argv are params for the method
    */
    public RunClassThread(TaskThreadGroup base,
                    String name,
                    int number,
                    InputOutput io,
                    org.netbeans.core.execution.ExecutionEngine engine,
                    ExecutorTaskImpl task,
                    Runnable run) {
        super(base, "exec_" + name + "_" + number); // NOI18N
        mygroup = base;
        mygroup.setRunClassThread(this);
        this.allName = name;
        this.io = io;
        this.engine = engine;
        this.task = task;
        this.run = run;
        this.finalized = false;
        setDaemon(false);
        this.start();
    }

    /** runs the thread
    */
    public void run() {
        mygroup.setFinalizable(); // mark it finalizable - after the completetion of the current thread it will be finalized

        boolean fire = true;

        if (allName ==  null) {
            allName = generateName();
            fire = false;
        }

        String ioname = java.text.MessageFormat.format(
                            ProcessNode.getBundle().getString("CTL_ProgramIO"),
                            new Object[] { allName }
                        );

        // prepare environment (threads, In/Out, atd.)
        DefaultSysProcess def;
        if (io != null) {
            def = new DefaultSysProcess(this, mygroup, io, allName);
            TaskIO tIO = new TaskIO(io, ioname, true);
            io.select();
            engine.getTaskIOs ().put (io, tIO);
        } else {   // advance TaskIO for this process
            TaskIO tIO = null;
            tIO = engine.getTaskIOs().getTaskIO(ioname);
            if (tIO == null) { // executed for the first time
                io = org.openide.windows.IOProvider.getDefault().getIO(ioname, true);
                tIO = new TaskIO(io, ioname);
            } else {
                io = tIO.getInout();
            }
            io.select();
            io.setFocusTaken(true);
            engine.getTaskIOs().put(io, tIO);
            def = new DefaultSysProcess(this, mygroup, io, allName);
        }

        ExecutionEvent ev = null;
        try {

            ev = new ExecutionEvent(engine, def, isUserImportant(run));
            if (fire) {
                engine.fireExecutionStarted(ev);
            }

            synchronized (task.lock) {
                task.proc = def;
                task.lock.notifyAll();
            }

            // exec foreign Runnable
            run.run();
            // throw away user runnable
            run = null;

            int result = 2;
            try {
                result = def.result();
            } catch (ThreadDeath err) { // terminated while executing
            }
            task.result = result;

        } finally {
            Thread.interrupted(); // Clear the interruption status at first.
            if (ev != null) {
                if (fire) {
                    engine.fireExecutionFinished(ev);
                }
            }

            engine.closeGroup(mygroup); // free windows
            task.finished();
            engine.getTaskIOs().free(mygroup, io); // closes output

            mygroup = null;
            io = null;
            synchronized (this) {
                finalized = true;
                notifyAll();
            }
        }
    } // run method

    public InputOutput getInputOutput() {
        return io;
    }
    
    public synchronized boolean waitForEnd() throws InterruptedException {
        if (finalized) {
            return true;
        }
        
        this.wait(1000);
        return finalized;
    }
    
    /** @return false for inner processes like compilation, true otherwise */
    private boolean isUserImportant (Runnable run) {
        return !"org.openide.compiler.CompilerExecutor$CERunnable".equals(run.getClass().getName()); // NOI18N
    }

    static String generateName() {
        return java.text.MessageFormat.format(
                   ProcessNode.getBundle().getString("CTL_GeneratedName"),
                   new Object[] {new Integer(number++)}
               );
    }
}

