/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.cnd.execution41.org.openide.execution;

import java.lang.reflect.Modifier;
import java.io.IOException;

import org.openide.ErrorManager;
import org.openide.execution.*;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.windows.InputOutput;

/** Executes a class in a thread in the current VM.
*
* @author Ales Novak
//* @deprecated Does not work well with Classpath API: there is no unambigous way to find the class name to load
//*             given only the data object, without using that API.
*/
public class ThreadExecutor extends Executor {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -7160546092135474445L;

    /** Create a new thread executor. */
    public ThreadExecutor() {
    }

    /*
    * @param ctx @see ExecutionEngine.Context
    * @param info an ExecInfo instance describing executed class
    */
    public ExecutorTask execute(ExecInfo info) throws IOException {
        TERunnable run = new TERunnable(info);
        ExecutorTask ret;
        InputOutput inout = (needsIO() ? null : InputOutput.NULL);

        synchronized (run) {
            ret = ExecutionEngine.getDefault().execute(info.getClassName(), run, inout);
            run.setInputOutput(ret.getInputOutput());
            try {
                run.wait();  // wait for arbitrary exceptions during executing run
                Throwable t = run.getException();
                if (t != null) {
                    if (! (t instanceof ThreadDeath)) {
                        if (t instanceof RuntimeException) {
                            throw (RuntimeException) t;
                        } else if (t instanceof Error) {
                            throw (Error) t;
                        } else if (t instanceof IOException) {
                            throw (IOException) t;
                        } else {
                            throw new IOException();
                        }
                    }
                }
            } catch (InterruptedException e) {
                IOException ioe = new IOException("Interupted: " + e); // NOI18N
                ErrorManager.getDefault().annotate(ioe, e);
                throw ioe;
            }
        }
        return ret;
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx (ThreadExecutor.class);
    }

    /** Subclasses of the executor can override this method
    * to check loaded class before its main method is invoked.
    *
    * @param clazz
    * @exception IOException
    */
    protected void checkClass(Class clazz) throws IOException {
        // find main (String[])
        final java.lang.reflect.Method method;
        try {
            method = clazz.getDeclaredMethod("main", new Class[] { String[].class }); // NOI18N
        } catch (NoSuchMethodException e) {
            IOException ioe = new IOException(e.toString());
            ErrorManager.getDefault().annotate(ioe,
                ErrorManager.USER, null,
                NbBundle.getMessage(ThreadExecutor.class, "EXC_NoSuchMethodException"),
                e, null);
            throw ioe;
        }
        if (!Modifier.isStatic(method.getModifiers()) ||
                !Modifier.isPublic(method.getModifiers()) ||
                method.getReturnType() != Void.TYPE) {
            IOException ioe = new IOException("wrong signature"); // NOI18N
            ErrorManager.getDefault().annotate(ioe, NbBundle.getMessage(ThreadExecutor.class, "EXC_not_public_static_void"));
            throw ioe;
        }
    }

    /** Invokes main method of the class with given parameters.
    *
    * @param clazz
    * @param params
    */
    protected void executeClass(Class clazz, String[] params) {
        try {
            final java.lang.reflect.Method method = clazz.getDeclaredMethod("main", new Class[] { params.getClass () }); // NOI18N
            method.setAccessible(true); // needs a permission
            method.invoke (null, new Object[] { params });
        } catch (java.lang.reflect.InvocationTargetException ex) {
            if (! (ex.getTargetException() instanceof ThreadDeath))
                ex.getTargetException().printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();  // is redirected since executed under EE
        }
    }
   
    /** Allows subclasses to provide its own classloader for loading
    * classes. Because the class loader is responsible for redirection
    * of input and output this method takes input output where the
    * I/O should be printed.
    * <P>
    * Default implementation creates <CODE>new NbClassLoader (io)</CODE>
    *
    * @param io the input/output to sent output of the classes to
    * @return the class loader to use
    */
    protected ClassLoader createClassLoader (InputOutput io) {
        throw new AssertionError("XXX broken, cannot work");
    }
    
    /* ThreadExecutor runnable
    * Its run method loads needed class, notifies waiting thread and executes main method of the class.
    */
    private class TERunnable implements Runnable {

        private Throwable exception;
        private ExecInfo info;
        private InputOutput io;

        TERunnable(ExecInfo info) {
            this.info = info;
        }

        public void run() {
            String className = info.getClassName();
            final String[] params  = info.getArguments();
            Class clazz = null;
            
            synchronized (this) {
                try {
                    ClassLoader loader = createClassLoader(io);
                    clazz = loader.loadClass(className);

                    if (clazz == null) {
                        throw new IOException(); // [PENDING]
                    }

                    if (clazz.getClassLoader() != loader) {
                        ErrorManager.getDefault().log(ErrorManager.WARNING, "The class " + clazz.getName() + " was loaded by an unexpected classloader: " + clazz.getClassLoader() + ". Usually this means you are trying to run a class from Filesystems via internal execution that is also in an enabled module JAR. The version in the JAR is being used."); // NOI18N
                    }

                    checkClass(clazz);

                } catch (Exception e) {
                    exception = e;
                    return;
                } catch (LinkageError e) {
                    exception = e;
                    return;
                } finally {
                    this.notifyAll();
                }
            }
            // out of sync block since it can take long time to complete...
            executeClass(clazz, params);
        }

        public Throwable getException() {
            return exception;
        }
        public void setInputOutput(InputOutput io) {
            this.io = io;
        }
    }
}
