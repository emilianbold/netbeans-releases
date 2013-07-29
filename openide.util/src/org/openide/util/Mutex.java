/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.openide.util;

import java.awt.EventQueue;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Read-many/write-one lock.
* Allows control over resources that
* can be read by several readers at once but only written by one writer.
* <P>
* It is guaranteed that if you are a writer you can also enter the
* mutex as a reader. Conversely, if you are the <em>only</em> reader you
* can enter the mutex as a writer, but you'll be warned because it is very
* deadlock prone (two readers trying to get write access concurently).
* <P>
* If the mutex is used only by one thread, the thread can repeatedly
* enter it as a writer or reader. So one thread can never deadlock itself,
* whichever order operations are performed in.
* <P>
* There is no strategy to prevent starvation.
* Even if there is a writer waiting to enter, another reader might enter
* the section instead.
* <P>
* Examples of use:
*
* <pre>
* Mutex m = new Mutex();
*
* // Grant write access, compute an integer and return it:
* return m.writeAccess(new Mutex.Action&lt;Integer>(){
*     public Integer run() {
*         return 1;
*     }
* });
*
* // Obtain read access, do some computation,
* // possibly throw an IOException:
* try {
*     m.readAccess(new Mutex.ExceptionAction&lt;Void>() {
*         public Void run() throws IOException {
*             if (...) throw new IOException();
*             return null;
*         }
*     });
* } catch (MutexException ex) {
*     throw (IOException) ex.getException();
* }
*
* // check whether you are already in read access
* if (m.isReadAccess()) {
*     // do your work
* }
* </pre>
*
* @author Ales Novak
*/
public final class Mutex extends Object {
    /** counter of created mutexes */
    private static int counter;

    /** logger for things that happen in mutex */
    private static final Logger LOG = Logger.getLogger(Mutex.class.getName());

    /** Mutex that allows code to be synchronized with the AWT event dispatch thread.
     * <P>
     * When the Mutex methods are invoked on this mutex, the methods' semantics 
     * change as follows:
     * <UL>
     * <LI>The {@link #isReadAccess} and {@link #isWriteAccess} methods
     *  return <code>true</code> if the current thread is the event dispatch thread
     *  and false otherwise.
     * <LI>The {@link #postReadRequest} and {@link #postWriteRequest} methods
     *  asynchronously execute the {@link java.lang.Runnable} passed in their 
     *  <code>run</code> parameter on the event dispatch thead.
     * <LI>The {@link #readAccess(java.lang.Runnable)} and 
     *  {@link #writeAccess(java.lang.Runnable)} methods asynchronously execute the 
     *  {@link java.lang.Runnable} passed in their <code>run</code> parameter 
     *  on the event dispatch thread, unless the current thread is 
     *  the event dispatch thread, in which case 
     *  <code>run.run()</code> is immediately executed.
     * <LI>The {@link #readAccess(Mutex.Action)},
     *  {@link #readAccess(Mutex.ExceptionAction action)},
     *  {@link #writeAccess(Mutex.Action action)} and
     *  {@link #writeAccess(Mutex.ExceptionAction action)} 
     *  methods synchronously execute the {@link Mutex.ExceptionAction}
     *  passed in their <code>action</code> parameter on the event dispatch thread,
     *  unless the current thread is the event dispatch thread, in which case
     *  <code>action.run()</code> is immediately executed.
     * </UL>
     */
    public static final Mutex EVENT = new Mutex();

    /** this is used from tests to prevent upgrade from readAccess to writeAccess
     * by strictly throwing exception. Otherwise we just notify that using ErrorManager.
     */
    static boolean beStrict;

    // lock mode constants

    /** Lock free */
    private static final int NONE = 0x0;

    /** Enqueue all requests */
    private static final int CHAIN = 0x1;

    /** eXclusive */
    private static final int X = 0x2;

    /** Shared */
    private static final int S = 0x3;

    /** number of modes */
    private static final int MODE_COUNT = 0x4;

    /** compatibility matrix */

    // [requested][granted]
    private static final boolean[][] cmatrix = {null,
        null, // NONE, CHAIN
        { true, false, false, false },{ true, false, false, true }
    };

    /** granted mode 
     * @GuaredBy("LOCK")
     */
    private int grantedMode = NONE;
    
    /** The mode the mutex was in before it started chaining 
     * @GuaredBy("LOCK")
     */
    private int origMode;

    /** protects internal data structures */
    private final Object LOCK;
    
    /** wrapper, if any */
    private final Executor wrapper;

    /** threads that - owns or waits for this mutex 
     * @GuaredBy("LOCK")
     */
    private final Map<Thread,ThreadInfo> registeredThreads = new HashMap<Thread,ThreadInfo>(7);

    /** number of threads that holds S mode (readersNo == "count of threads in registeredThreads that holds S") */

    // NOI18N
    private int readersNo = 0;

    /** a queue of waiting threads for this mutex */
    private List<QueueCell> waiters;

    /** identification of the mutex */
    private int cnt;

    /** Enhanced constructor that permits specifying an object to use as a lock.
    * The lock is used on entry and exit to {@link #readAccess} and during the
    * whole execution of {@link #writeAccess}. The ability to specify locks
    * allows several <code>Mutex</code>es to synchronize on one object or to synchronize
    * a mutex with another critical section.
    *
    * @param lock lock to use
    */
    public Mutex(Object lock) {
        this.LOCK = init(lock);
        this.wrapper = null;
    }

    /** Default constructor.
    */
    public Mutex() {
        this.LOCK = init(new InternalLock());
        this.wrapper = null;
    }

    /** @param privileged can enter privileged states of this Mutex
     * This helps avoid creating of custom Runnables.
     */
    public Mutex(Privileged privileged) {
        if (privileged == null) {
            throw new IllegalArgumentException("privileged == null"); //NOI18N
        } else {
            this.LOCK = init(new InternalLock());
            privileged.setParent(this);
        }
        this.wrapper = null;
    }

    /** Constructor for those who wish to do some custom additional tasks
     * whenever an action or runnable is executed in the {@link Mutex}. This
     * may be useful for wrapping all the actions with custom {@link ThreadLocal}
     * value, etc. Just implement the {@link Executor}'s <code>execute(Runnable)</code>
     * method and do pre and post initialization tasks before running the runnable.
     * <p>
     * The {@link Executor#execute} method shall return only when the passed in
     * {@link Runnable} is finished, otherwise methods like {@link Mutex#readAccess(Action)} and co.
     * might not return proper result.
     * 
     * @param privileged can enter privileged states of this Mutex
     *  @param executor allows to wrap the work of the mutex with a custom code
     * @since 7.12
     */
    public Mutex(Privileged privileged, Executor executor) {
        LOCK = new Mutex(privileged);
        this.wrapper = executor;
    }

    /** Initiates this Mutex */
    private Object init(Object lock) {
        this.waiters = new LinkedList<QueueCell>();
        this.cnt = counter++;
        if (LOG.isLoggable(Level.FINER)) {
            LOG.log(Level.FINER, "[" + cnt + "] created here", new Exception());
        }
        return lock;
    }

    /** Run an action only with read access.
    * See class description re. entering for write access within the dynamic scope.
    * @param action the action to perform
    * @return the object returned from {@link Mutex.Action#run}
    */
    public <T> T readAccess(final Action<T> action) {
        if (this == EVENT) {
            try {
                return doEventAccess(action);
            } catch (MutexException e) {
                throw (InternalError) new InternalError("Exception from non-Exception Action").initCause(e.getException()); // NOI18N
            }
        }
        if (wrapper != null) {
            try {
                return doWrapperAccess(action, null, true);
            } catch (MutexException e) {
                throw (InternalError) new InternalError("Exception from non-Exception Action").initCause(e.getException()); // NOI18N
            }
        }

        Thread t = Thread.currentThread();
        readEnter(t);

        try {
            return action.run();
        } finally {
            leave(t);
        }
    }

    /** Run an action with read access and possibly throw a checked exception.
    * The exception if thrown is then encapsulated
    * in a <code>MutexException</code> and thrown from this method. One is encouraged
    * to catch <code>MutexException</code>, obtain the inner exception, and rethrow it.
    * Here is an example:
    * <p><code><PRE>
    * try {
    *   mutex.readAccess (new ExceptionAction () {
    *     public void run () throws IOException {
    *       throw new IOException ();
    *     }
    *   });
    *  } catch (MutexException ex) {
    *    throw (IOException) ex.getException ();
    *  }
    * </PRE></code>
    * Note that <em>runtime exceptions</em> are always passed through, and neither
    * require this invocation style, nor are encapsulated.
    * @param action the action to execute
    * @return the object returned from {@link Mutex.ExceptionAction#run}
    * @exception MutexException encapsulates a user exception
    * @exception RuntimeException if any runtime exception is thrown from the run method
    * @see #readAccess(Mutex.Action)
    */
    public <T> T readAccess(final ExceptionAction<T> action) throws MutexException {
        if (this == EVENT) {
            return doEventAccess(action);
        }
        if (wrapper != null) {
            return doWrapperAccess(action, null, true);
        }

        Thread t = Thread.currentThread();
        readEnter(t);

        try {
            return action.run();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new MutexException(e);
        } finally {
            leave(t);
        }
    }

    /** Run an action with read access, returning no result.
    * It may be run asynchronously.
    *
    * @param action the action to perform
    * @see #readAccess(Mutex.Action)
    */
    public void readAccess(final Runnable action) {
        if (this == EVENT) {
            doEvent(action);

            return;
        }
        if (wrapper != null) {
            try {
                doWrapperAccess(null, action, true);
                return;
            } catch (MutexException ex) {
                throw new IllegalStateException(ex);
            }
        }

        Thread t = Thread.currentThread();
        readEnter(t);

        try {
            action.run();
        } finally {
            leave(t);
        }
    }

    /** Run an action with write access.
    * The same thread may meanwhile reenter the mutex; see the class description for details.
    *
    * @param action the action to perform
    * @return the result of {@link Mutex.Action#run}
    */
    public <T> T writeAccess(Action<T> action) {
        if (this == EVENT) {
            try {
                return doEventAccess(action);
            } catch (MutexException e) {
                throw (InternalError) new InternalError("Exception from non-Exception Action").initCause(e.getException()); // NOI18N
            }
        }
        if (wrapper != null) {
            try {
                return doWrapperAccess(action, null, false);
            } catch (MutexException e) {
                throw (InternalError) new InternalError("Exception from non-Exception Action").initCause(e.getException()); // NOI18N
            }
        }

        Thread t = Thread.currentThread();
        writeEnter(t);

        try {
            return action.run();
        } finally {
            leave(t);
        }
    }

    /** Run an action with write access and possibly throw an exception.
    * Here is an example:
    * <p><code><PRE>
    * try {
    *   mutex.writeAccess (new ExceptionAction () {
    *     public void run () throws IOException {
    *       throw new IOException ();
    *     }
    *   });
    *  } catch (MutexException ex) {
    *    throw (IOException) ex.getException ();
    *  }
    * </PRE></code>
    *
    * @param action the action to execute
    * @return the result of {@link Mutex.ExceptionAction#run}
    * @exception MutexException an encapsulated checked exception, if any
    * @exception RuntimeException if a runtime exception is thrown in the action
    * @see #writeAccess(Mutex.Action)
    * @see #readAccess(Mutex.ExceptionAction)
    */
    public <T> T writeAccess(ExceptionAction<T> action) throws MutexException {
        if (this == EVENT) {
            return doEventAccess(action);
        }
        if (wrapper != null) {
            return doWrapperAccess(action, null, false);
        }

        Thread t = Thread.currentThread();
        writeEnter(t);

        try {
            return action.run();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new MutexException(e);
        } finally {
            leave(t);
        }
    }

    /** Run an action with write access and return no result.
    * It may be run asynchronously.
    *
    * @param action the action to perform
    * @see #writeAccess(Mutex.Action)
    * @see #readAccess(Runnable)
    */
    public void writeAccess(final Runnable action) {
        if (this == EVENT) {
            doEvent(action);

            return;
        }
        if (wrapper != null) {
            try {
                doWrapperAccess(null, action, false);
            } catch (MutexException ex) {
                throw new IllegalStateException(ex);
            }
            return;
        }

        Thread t = Thread.currentThread();
        writeEnter(t);

        try {
            action.run();
        } finally {
            leave(t);
        }
    }

    /** Tests whether this thread has already entered the mutex in read access.
     * If it returns true, calling <code>readAccess</code>
     * will be executed immediatelly
     * without any blocking.
     * Calling <code>postWriteAccess</code> will delay the execution
     * of its <code>Runnable</code> until a readAccess section is over
     * and calling <code>writeAccess</code> is strongly prohibited and will
     * result in a warning as a deadlock prone behaviour.
     * <p><strong>Warning:</strong> since a thread with write access automatically
     * has effective read access as well (whether or not explicitly requested), if
     * you want to check whether a thread can read some data, you should check for
     * either kind of access, e.g.:
     * <pre>assert myMutex.isReadAccess() || myMutex.isWriteAccess();</pre>
     *
     * @return true if the thread is in read access section
     * @since 4.48
     */
    public boolean isReadAccess() {
        if (this == EVENT) {
            return javax.swing.SwingUtilities.isEventDispatchThread();
        }
        if (wrapper != null) {
            Mutex m = (Mutex)LOCK;
            return m.isReadAccess();
        }

        Thread t = Thread.currentThread();
        ThreadInfo info;

        synchronized (LOCK) {
            info = getThreadInfo(t);

            if (info != null) {
                if (info.counts[S] > 0) {
                    return true;
                }
            }
        }

        return false;
    }

    /** Tests whether this thread has already entered the mutex in write access.
     * If it returns true, calling <code>writeAccess</code> will be executed
     * immediatelly without any other blocking. <code>postReadAccess</code>
     * will be delayed until a write access runnable is over.
     *
     * @return true if the thread is in write access section
     * @since 4.48
     */
    public boolean isWriteAccess() {
        if (this == EVENT) {
            return javax.swing.SwingUtilities.isEventDispatchThread();
        }
        if (wrapper != null) {
            Mutex m = (Mutex)LOCK;
            return m.isWriteAccess();
        }

        Thread t = Thread.currentThread();
        ThreadInfo info;

        synchronized (LOCK) {
            info = getThreadInfo(t);

            if (info != null) {
                if (info.counts[X] > 0) {
                    return true;
                }
            }
        }

        return false;
    }

    /** Posts a read request. This request runs immediately iff
     * this Mutex is in the shared mode or this Mutex is not contended
     * at all.
     *
     * This request is delayed if this Mutex is in the exclusive
     * mode and is held by this thread, until the exclusive is left.
     *
     * Finally, this request blocks, if this Mutex is in the exclusive
     * mode and is held by another thread.
     *
     * <p><strong>Warning:</strong> this method blocks.</p>
     *
     * @param run runnable to run
     */
    public void postReadRequest(final Runnable run) {
        postRequest(S, run, null);
    }

    /** Posts a write request. This request runs immediately iff
     * this Mutex is in the "pure" exclusive mode, i.e. this Mutex
     * is not reentered in shared mode after the exclusive mode
     * was acquired. Otherwise it is delayed until all read requests
     * are executed.
     *
     * This request runs immediately if this Mutex is not contended at all.
     *
     * This request blocks if this Mutex is in the shared mode.
     *
     * <p><strong>Warning:</strong> this method blocks.</p>
     * @param run runnable to run
     */
    public void postWriteRequest(Runnable run) {
        postRequest(X, run, null);
    }

    /** toString */
    @Override
    public String toString() {
        if (this == EVENT) {
            return "Mutex.EVENT"; // NOI18N
        }

        String newline = System.getProperty("line.separator");
        StringBuilder sbuff = new StringBuilder(512);

        synchronized (LOCK) {
            sbuff.append("threads: ").append(getRegisteredThreads()).append(newline); // NOI18N
            sbuff.append("readersNo: ").append(readersNo).append(newline); // NOI18N
            sbuff.append("waiters: ").append(waiters).append(newline); // NOI18N
            sbuff.append("grantedMode: ").append(getGrantedMode(false)).append(newline); // NOI18N
        }

        return sbuff.toString();
    }

    // priv methods  -----------------------------------------

    /** enters this mutex for writing */
    final void writeEnter(Thread t) {
        enter(X, t, true);
    }

    /** enters this mutex for reading */
    final void readEnter(Thread t) {
        enter(S, t, true);
    }

    private void doLog(String action, Object ... params) {
        String tid = Integer.toHexString(Thread.currentThread().hashCode());
        LOG.log(Level.FINER, "[#" + cnt + "@" + tid + "] " + action, params);
    }
    
    /** enters this mutex with given mode
    * @param requested one of S, X
    * @param t
    */
    private boolean enter(int requested, Thread t, boolean block) {
        boolean log = LOG.isLoggable(Level.FINER);

        if (log) doLog("Entering {0}, {1}", requested, block); // NOI18N

        boolean ret = enterImpl(requested, t, block);

        if (log) doLog("Entering exit: {0}", ret); // NOI18N

        return ret;
    }

    private boolean enterImpl(int requested, Thread t, boolean block) {
        QueueCell cell = null;
        int loopc = 0;

        for (;;) {
            loopc++;
            synchronized (LOCK) {
                // does the thread reenter this mutex?
                ThreadInfo info = getThreadInfo(t);

                if (info != null) {
                    if (getGrantedMode(false) == NONE) {
                        // defensive
                        throw new IllegalStateException();
                    }
                    // reenters
                    // requested == S -> always succeeds
                    // info.mode == X -> always succeeds
                    if (((info.mode == S) && (getGrantedMode(false) == X)) ||
                        ((info.mode == X) && (getGrantedMode(false) == S))) {
                        // defensive
                        throw new IllegalStateException();
                    }
                    if ((info.mode == X) || (info.mode == requested)) {
                        if (info.forced) {
                            info.forced = false;
                        } else {
                            if ((requested == X) && (info.counts[S] > 0)) {
                                IllegalStateException e = new IllegalStateException("WARNING: Going from readAccess to writeAccess, see #10778: http://www.netbeans.org/issues/show_bug.cgi?id=10778 ");

                                if (beStrict) {
                                    throw e;
                                }
                                Exceptions.printStackTrace(e);
                            }
                            info.counts[requested]++;
                            if ((requested == S) &&
                                (info.counts[requested] == 1)) {
                                readersNo++;
                            }
                        }
                        return true;
                    } else if (canUpgrade(info.mode, requested)) {
                        IllegalStateException e = new IllegalStateException("WARNING: Going from readAccess to writeAccess, see #10778: http://www.netbeans.org/issues/show_bug.cgi?id=10778 ");

                        if (beStrict) {
                            throw e;
                        }
                        Exceptions.printStackTrace(e);
                        info.mode = X;
                        info.counts[requested]++;
                        info.rsnapshot = info.counts[S];
                        if (getGrantedMode(false) == S) {
                            setGrantedMode(X);
                        } else if (getGrantedMode(false) == X) {
                            // defensive
                            throw new IllegalStateException();
                        }
                        // else if grantedMode == CHAIN - let it be
                        return true;
                    } else {
                        IllegalStateException e = new IllegalStateException("WARNING: Going from readAccess to writeAccess through queue, see #10778: http://www.netbeans.org/issues/show_bug.cgi?id=10778 ");

                        if (beStrict) {
                            throw e;
                        }
                        Exceptions.printStackTrace(e);
                    }
                } else {
                    if (isCompatible(requested)) {
                        setGrantedMode(requested);
                        getRegisteredThreads().put(t,
                                              info = new ThreadInfo(t, requested));
                        if (requested == S) {
                            readersNo++;
                        }
                        return true;
                    }
                }
                if (!block) {
                    return false;
                }
                setGrantedMode(CHAIN);
                cell = chain(requested, t, 0);
            }
            // sync
            cell.sleep();
        }
         // for
    }
    
    /** privilegedEnter serves for processing posted requests */
    private boolean reenter(Thread t, int mode) {
        boolean log = LOG.isLoggable(Level.FINER);

        if (log) doLog("Re-Entering {0}", mode); // NOI18N

        boolean ret = reenterImpl(t, mode);

        if (log) doLog("Re-Entering exit: {0}", ret); // NOI18N

        return ret;
    }


    private boolean reenterImpl(Thread t, int mode) {
        // from leaveX -> grantedMode is NONE or S
        if (mode == S) {
            if ((getGrantedMode(false) != NONE) && (getGrantedMode(false) != S)) {
                throw new IllegalStateException(this.toString());
            }

            enter(mode, t, true);

            return false;
        }

        // assert (mode == X)
        ThreadInfo tinfo = getThreadInfo(t);
        boolean chainFromLeaveX = ((getGrantedMode(false) == CHAIN) && (tinfo != null) && (tinfo.counts[X] > 0));

        // process grantedMode == X or CHAIN from leaveX OR grantedMode == NONE from leaveS
        if ((getGrantedMode(false) == X) || (getGrantedMode(false) == NONE) || chainFromLeaveX) {
            enter(mode, t, true);

            return false;
        } else { // remains grantedMode == CHAIN or S from leaveS, so it will be CHAIN

            if (readersNo == 0) {
                throw new IllegalStateException(this.toString());
            }

            ThreadInfo info = new ThreadInfo(t, mode);
            getRegisteredThreads().put(t, info);

            // prevent from grantedMode == NONE (another thread - leaveS)
            readersNo += 2;

            // prevent from new readers
            setGrantedMode(CHAIN);

            return true;
        }
         // else X means ERROR!!!
    }

    /** @param t holds S (one entry) and wants X, grantedMode != NONE && grantedMode != X */
    private void privilegedEnter(Thread t, int mode) {
        boolean decrease = true;

        synchronized (LOCK) {
            getThreadInfo(t);
        }

        for (;;) {
            QueueCell cell;

            synchronized (LOCK) {
                if (decrease) {
                    decrease = false;
                    readersNo -= 2;
                }

                // always chain this thread
                // since there can be another one
                // in the queue with higher priority
                setGrantedMode(CHAIN);
                cell = chain(mode, t, Integer.MAX_VALUE);

                if (readersNo == 0) { // seems I may enter

                    // no one has higher prio?
                    if (waiters.get(0) == cell) {
                        waiters.remove(0);
                        
                        setGrantedMode(mode);

                        return;
                    } else {
                        setGrantedMode(NONE);
                        wakeUpOthers();
                    }
                }
            }
             // synchronized (LOCK)

            cell.sleep();

            // cell already removed from waiters here
        }
    }

    /** Leaves this mutex */
    final void leave(Thread t) {
        boolean log = LOG.isLoggable(Level.FINER);

        if (log) doLog("Leaving {0}", getGrantedMode(true)); // NOI18N

        leaveImpl(t);

        if (log) doLog("Leaving exit: {0}", getGrantedMode(true)); // NOI18N
    }

    private void leaveImpl(Thread t) {
        ThreadInfo info;
        int postedMode = NONE;
        boolean needLock = false;

        synchronized (LOCK) {
            info = getThreadInfo(t);

            switch (getGrantedMode(false)) {
            case NONE:
                throw new IllegalStateException();

            case CHAIN:

                if (info.counts[X] > 0) {
                    // it matters that X is handled first - see ThreadInfo.rsnapshot
                    postedMode = leaveX(info);
                } else if (info.counts[S] > 0) {
                    postedMode = leaveS(info);
                } else {
                    throw new IllegalStateException();
                }

                break;

            case X:
                postedMode = leaveX(info);

                break;

            case S:
                postedMode = leaveS(info);

                break;
            } // switch

            // do not give up LOCK until queued runnables are run
            if (postedMode != NONE) {
                int runsize = info.getRunnableCount(postedMode);

                if (runsize != 0) {
                    needLock = reenter(t, postedMode); // grab lock
                }
            }
        } // sync

        // check posted requests
        if ((postedMode != NONE) && (info.getRunnableCount(postedMode) > 0)) {
            doLog("Processing posted requests: {0}", postedMode); // NOI18N
            try {
                if (needLock) { // go from S to X or CHAIN
                    privilegedEnter(t, postedMode);
                }

                // holds postedMode lock here
                List runnables = info.dequeue(postedMode);
                final int size = runnables.size();

                for (int i = 0; i < size; i++) {
                    try {
                        Runnable r = (Runnable) runnables.get(i);

                        r.run();
                    }
                    catch (Exception e) {
                        Exceptions.printStackTrace(e);
                    }
                    catch (StackOverflowError e) {
                        // Try as hard as possible to get a real stack trace
                        e.printStackTrace();
                        Exceptions.printStackTrace(e);
                    }
                    catch (ThreadDeath td) {
                        throw td;
                    }
                    catch (Error e) {
                        Exceptions.printStackTrace(e);
                    }
                }
                 // for

                // help gc
                runnables = null;
            } finally {
                leave(t); // release lock grabbed - shared
            }
        }
         // mode
    }

    /** Leaves the lock supposing that info.counts[X] is greater than zero */
    private int leaveX(ThreadInfo info) {
        if ((info.counts[X] <= 0) || (info.rsnapshot > info.counts[S])) {
            // defensive
            throw new IllegalStateException();
        }

        if (info.rsnapshot == info.counts[S]) {
            info.counts[X]--;

            if (info.counts[X] == 0) {
                info.rsnapshot = 0;

                // downgrade the lock
                if (info.counts[S] > 0) {
                    info.mode = S;
                    setGrantedMode(S);
                } else {
                    info.mode = NONE;
                    setGrantedMode(NONE);
                    getRegisteredThreads().remove(info.t);
                }

                if (info.getRunnableCount(S) > 0) {
                    // wake up other readers of this mutex
                    wakeUpReaders();

                    return S;
                }

                // mode has changed
                wakeUpOthers();
            }
        } else {
            // rsnapshot < counts[S]
            if (info.counts[S] <= 0) {
                // defensive
                throw new IllegalStateException();
            }

            if (--info.counts[S] == 0) {
                if (readersNo <= 0) {
                    throw new IllegalStateException();
                }

                readersNo--;

                return X;
            }
        }

        return NONE;
    }

    /** Leaves the lock supposing that info.counts[S] is greater than zero */
    private int leaveS(ThreadInfo info) {
        if ((info.counts[S] <= 0) || (info.counts[X] > 0)) {
            // defensive
            throw new IllegalStateException();
        }

        info.counts[S]--;

        if (info.counts[S] == 0) {
            // remove the thread
            info.mode = NONE;
            getRegisteredThreads().remove(info.t);

            // downsize readersNo
            if (readersNo <= 0) {
                throw new IllegalStateException();
            }

            readersNo--;

            if (readersNo == 0) {
                // set grantedMode to NONE
                // and then wakeUp others - either immediately 
                // or in privelegedEnter()
                setGrantedMode(NONE);

                if (info.getRunnableCount(X) > 0) {
                    return X;
                }

                wakeUpOthers();
            } else if (info.getRunnableCount(X) > 0) {
                return X;
            } else if ((getGrantedMode(false) == CHAIN) && (readersNo == 1)) {
                // can be the mode advanced from CHAIN? Examine first item of waiters!
                for (int i = 0; i < waiters.size(); i++) {
                    QueueCell qc = waiters.get(i);

                    synchronized (qc) {
                        if (qc.isGotOut()) {
                            waiters.remove(i--);

                            continue;
                        }

                        ThreadInfo tinfo = getThreadInfo(qc.t);

                        if (tinfo != null) {
                            if (tinfo.mode == S) {
                                if (qc.mode != X) {
                                    // defensive
                                    throw new IllegalStateException();
                                }

                                if (waiters.size() == 1) {
                                    setGrantedMode(X);
                                }
                                 // else let CHAIN

                                tinfo.mode = X;
                                waiters.remove(i);
                                qc.wakeMeUp();
                            }
                        }
                         // else first request is a first X request of some thread

                        break;
                    }
                     // sync (qc)
                }
                 // for
            }
             // else
        }
         // count[S] == 0

        return NONE;
    }

    /** Adds this thread to the queue of waiting threads
    * @warning LOCK must be held
    */
    private QueueCell chain(final int requested, final Thread t, final int priority) {
        //long timeout = 0;

        /*
        if (killDeadlocksOn) {
            checkDeadlock(requested, t);
            timeout = (isDispatchThread() || checkAwtTreeLock() ? TIMEOUT : 0);
        }
        */
        QueueCell qc = new QueueCell(requested, t);

        //qc.timeout = timeout;
        qc.priority2 = priority;

        final int size = waiters.size();

        if (size == 0) {
            waiters.add(qc);
        } else if (qc.getPriority() == Integer.MAX_VALUE) {
            waiters.add(0, qc);
        } else {
            QueueCell cursor;
            int i = 0;

            do {
                cursor = waiters.get(i);

                if (cursor.getPriority() < qc.getPriority()) {
                    waiters.add(i, qc);

                    break;
                }

                i++;
            } while (i < size);

            if (i == size) {
                waiters.add(qc);
            }
        }

        return qc;
    }

    /** Scans through waiters and wakes up them */
    private void wakeUpOthers() {
        if ((getGrantedMode(false) == X) || (getGrantedMode(false) == CHAIN)) {
            // defensive
            throw new IllegalStateException();
        }

        if (waiters.isEmpty()) {
            return;
        }

        for (int i = 0; i < waiters.size(); i++) {
            QueueCell qc = waiters.get(i);

            synchronized (qc) {
                if (qc.isGotOut()) {
                    // bogus waiter
                    waiters.remove(i--);

                    continue;
                }

                if (isCompatible(qc.mode)) { // woken S -> should I wake X? -> no
                    waiters.remove(i--);
                    qc.wakeMeUp();
                    setGrantedMode(qc.mode);

                    if (getThreadInfo(qc.t) == null) {
                        // force to have a record since recorded threads
                        // do not use isCompatible call
                        ThreadInfo ti = new ThreadInfo(qc.t, qc.mode);
                        ti.forced = true;

                        if (qc.mode == S) {
                            readersNo++;
                        }

                        getRegisteredThreads().put(qc.t, ti);
                    }
                } else {
                    setGrantedMode(CHAIN);

                    break;
                }
            }
             // sync (qc)
        }
    }

    private void wakeUpReaders() {
        assert (getGrantedMode(false) == NONE) || (getGrantedMode(false) == S);

        if (waiters.isEmpty()) {
            return;
        }

        for (int i = 0; i < waiters.size(); i++) {
            QueueCell qc = waiters.get(i);

            synchronized (qc) {
                if (qc.isGotOut()) {
                    // bogus waiter
                    waiters.remove(i--);

                    continue;
                }

                if (qc.mode == S) { // readers only
                    waiters.remove(i--);
                    qc.wakeMeUp();
                    setGrantedMode(S);

                    if (getThreadInfo(qc.t) == null) {
                        // force to have a record since recorded threads
                        // do not use isCompatible call
                        ThreadInfo ti = new ThreadInfo(qc.t, qc.mode);
                        ti.forced = true;
                        readersNo++;
                        getRegisteredThreads().put(qc.t, ti);
                    }
                }
            }
             // sync (qc)
        }
    }

    /** Posts new request for current thread
    * @param mutexMode mutex mode for which the action is rquested
    * @param run the action
    */
    private void postRequest(final int mutexMode, final Runnable run, Executor exec) {
        if (this == EVENT) {
            doEventRequest(run);

            return;
        }
        if (wrapper != null) {
            Mutex m = (Mutex)LOCK;
            m.postRequest(mutexMode, run, wrapper);
            return;
        }

        final Thread t = Thread.currentThread();
        ThreadInfo info;

        synchronized (LOCK) {
            info = getThreadInfo(t);

            if (info != null) {
                // the same mode and mutex is not entered in the other mode
                // assert (mutexMode == S || mutexMode == X)
                if ((mutexMode == info.mode) && (info.counts[(S + X) - mutexMode] == 0)) {
                    enter(mutexMode, t, true);
                } else { // the mutex is held but can not be entered in X mode
                    info.enqueue(mutexMode, run);

                    return;
                }
            }
        }

        // this mutex is not held
        if (info == null) {
            if (exec != null) {
                class Exec implements Runnable {
                    @Override
                    public void run() {
                        enter(mutexMode, t, true);
                        try {
                            run.run();
                        } finally {
                            leave(t);
                        }
                    }
                }
                exec.execute(new Exec());
                return;
            }
            
            enter(mutexMode, t, true);
            try {
                run.run();
            } finally {
                leave(t);
            }

            return;
        }

        // run it immediately
        // info != null so enter(...) succeeded
        try {
            run.run();
        } finally {
            leave(t);
        }
    }

    /** @param requested is requested mode of locking
    * @return <tt>true</tt> if and only if current mode and requested mode are compatible
    */
    private boolean isCompatible(int requested) {
        // allow next reader in even in chained mode, if it was read access before
        if (requested == S && getGrantedMode(false) == CHAIN && getOrigMode() == S) return true;
        return cmatrix[requested][getGrantedMode(false)];
    }

    private ThreadInfo getThreadInfo(Thread t) {
        return getRegisteredThreads().get(t);
    }

    private boolean canUpgrade(int threadGranted, int requested) {
        return (threadGranted == S) && (requested == X) && (readersNo == 1);
    }
    
    // -------------------------------- WRAPPERS --------------------------------
    
    private <T> T doWrapperAccess(
        final ExceptionAction<T> action, final Runnable runnable, final boolean readOnly
    ) throws MutexException {
        class R implements Runnable {
            T ret;
            MutexException e;
            
            @Override
            public void run() {
                Mutex m = (Mutex)LOCK;
                try {
                    if (readOnly) {
                        if (action != null) {
                            ret = m.readAccess(action);
                        } else {
                            m.readAccess(runnable);
                        }
                    } else {
                        if (action != null) {
                            ret = m.writeAccess(action);
                        } else {
                            m.writeAccess(runnable);
                        }
                    }
                } catch (MutexException ex) {
                    e = ex;
                }
            }
        }
        R run = new R();
        Mutex m = (Mutex)LOCK;
        if (m.isWriteAccess() || m.isReadAccess()) {
            run.run();
        } else {
            wrapper.execute(run);
        }
        if (run.e != null) {
            throw run.e;
        }
        return run.ret;
    }

    // ------------------------------- EVENT METHODS ----------------------------

    /** Runs the runnable in event queue, either immediatelly,
    * or it posts it into the queue.
    */
    private static void doEvent(Runnable run) {
        if (EventQueue.isDispatchThread()) {
            run.run();
        } else {
            EventQueue.invokeLater(run);
        }
    }

    /** Methods for access to event queue.
    * @param run runabble to post later
    */
    private static void doEventRequest(Runnable run) {
        EventQueue.invokeLater(run);
    }

    /** Methods for access to event queue and waiting for result.
    * @param run runnable to post later
    */
    private static <T> T doEventAccess(final ExceptionAction<T> run)
    throws MutexException {
        if (isDispatchThread()) {
            try {
                return run.run();
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new MutexException(e);
            }
        }

        final AtomicReference<Union2<T,Throwable>> res = new AtomicReference<Union2<T,Throwable>>();
        final AtomicBoolean started = new AtomicBoolean(); // #210991
        final AtomicBoolean finished = new AtomicBoolean();
        final AtomicBoolean invoked = new AtomicBoolean();
        try {
            class AWTWorker implements Runnable {
                @Override
                public void run() {
                    started.set(true);
                    try {
                        res.set(Union2.<T,Throwable>createFirst(run.run()));
                    } catch (Exception e) {
                        res.set(Union2.<T,Throwable>createSecond(e));
                    } catch (LinkageError e) {
                        // #20467
                        res.set(Union2.<T,Throwable>createSecond(e));
                    } catch (StackOverflowError e) {
                        // #20467
                        res.set(Union2.<T,Throwable>createSecond(e));
                    }
                    finished.set(true);
                }
            }

            AWTWorker w = new AWTWorker();
            EventQueue.invokeAndWait(w);
            invoked.set(true);
        } catch (InterruptedException e) {
            res.set(Union2.<T,Throwable>createSecond(e));
        } catch (InvocationTargetException e) {
            res.set(Union2.<T,Throwable>createSecond(e));
        }

        Union2<T,Throwable> _res = res.get();
        if (_res == null) {
            throw new IllegalStateException("#210991: got neither a result nor an exception; started=" + started + " finished=" + finished + " invoked=" + invoked);
        } else if (_res.hasFirst()) {
            return _res.first();
        } else {
            Throwable e = _res.second();
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            } else {
                throw notifyException(e);
            }
        }
    }

    /** @return true iff current thread is EventDispatchThread */
    static boolean isDispatchThread() {
        boolean dispatch = EventQueue.isDispatchThread();

        if (!dispatch && (Utilities.getOperatingSystem() == Utilities.OS_SOLARIS)) {
            // on solaris the event queue is not always recognized correctly
            // => try to guess by name
            dispatch = (Thread.currentThread().getClass().getName().indexOf("EventDispatchThread") >= 0); // NOI18N
        }

        return dispatch;
    }

    /** Notify exception and returns new MutexException */
    private static MutexException notifyException(Throwable t) {
        if (t instanceof InvocationTargetException) {
            t = unfoldInvocationTargetException((InvocationTargetException) t);
        }

        if (t instanceof Error) {
            annotateEventStack(t);
            throw (Error) t;
        }

        if (t instanceof RuntimeException) {
            annotateEventStack(t);
            throw (RuntimeException) t;
        }

        MutexException exc = new MutexException((Exception) t);
        exc.initCause(t);

        return exc;
    }

    private static void annotateEventStack(Throwable t) {
        //ErrorManager.getDefault().annotate(t, new Exception("Caught here in mutex")); // NOI18N
    }

    private static Throwable unfoldInvocationTargetException(InvocationTargetException e) {
        Throwable ret;

        do {
            ret = e.getTargetException();

            if (ret instanceof InvocationTargetException) {
                e = (InvocationTargetException) ret;
            } else {
                e = null;
            }
        } while (e != null);

        return ret;
    }

    // --------------------------------------------- END OF EVENT METHODS ------------------------------

    /** Action to be executed in a mutex without throwing any checked exceptions.
    * Unchecked exceptions will be propagated to calling code.
     * @param T the type of object to return
    */
    public interface Action<T> extends ExceptionAction<T> {
        /** Execute the action.
        * @return any object, then returned from {@link Mutex#readAccess(Mutex.Action)} or {@link Mutex#writeAccess(Mutex.Action)}
        */
        @Override
        T run();
    }

    /** Action to be executed in a mutex, possibly throwing checked exceptions.
    * May throw a checked exception, in which case calling
    * code should catch the encapsulating exception and rethrow the
    * real one.
    * Unchecked exceptions will be propagated to calling code without encapsulation.
     * @param T the type of object to return
    */
    public interface ExceptionAction<T> {
        /** Execute the action.
        * Can throw an exception.
        * @return any object, then returned from {@link Mutex#readAccess(Mutex.ExceptionAction)} or {@link Mutex#writeAccess(Mutex.ExceptionAction)}
        * @exception Exception any exception the body needs to throw
        */
        T run() throws Exception;
    }

    private static final class ThreadInfo {
        /** t is forcibly sent from waiters to enter() by wakeUpOthers() */
        boolean forced;

        /** ThreadInfo for this Thread */
        final Thread t;

        /** granted mode */
        int mode;

        // 0 - NONE, 1 - CHAIN, 2 - X, 3 - S

        /** enter counter */
        int[] counts;

        /** queue of runnable rquests that are to be executed (in X mode) right after S mode is left
        * deadlock avoidance technique
        */
        List<Runnable>[] queues;

        /** value of counts[S] when the mode was upgraded
        * rsnapshot works as follows:
        * if a thread holds the mutex in the S mode and it reenters the mutex
        * and requests X and the mode can be granted (no other readers) then this
        * variable is set to counts[S]. This is used in the leave method in the X branch.
        * (X mode is granted by other words)
        * If rsnapshot is less than counts[S] then the counter is decremented etc. If the rsnapshot is
        * equal to count[S] then count[X] is decremented. If the X counter is zeroed then
        * rsnapshot is zeroed as well and current mode is downgraded to S mode.
        * rsnapshot gets less than counts[S] if current mode is X and the mutex is reentered
        * with S request.
        */
        int rsnapshot;

        @SuppressWarnings("unchecked")
        public ThreadInfo(Thread t, int mode) {
            this.t = t;
            this.mode = mode;
            this.counts = new int[MODE_COUNT];
            this.queues = (List<Runnable>[])new List[MODE_COUNT];
            counts[mode] = 1;
        }

        @Override
        public String toString() {
            return super.toString() + " thread: " + t + " mode: " + mode + " X: " + counts[2] + " S: " + counts[3]; // NOI18N
        }

        /** Adds the Runnable into the queue of waiting requests */
        public void enqueue(int mode, Runnable run) {
            if (queues[mode] == null) {
                queues[mode] = new ArrayList<Runnable>(13);
            }

            queues[mode].add(run);
        }

        /** @return a List of enqueued Runnables - may be null */
        public List dequeue(int mode) {
            List ret = queues[mode];
            queues[mode] = null;

            return ret;
        }

        public int getRunnableCount(int mode) {
            return ((queues[mode] == null) ? 0 : queues[mode].size());
        }
    }

    /** This class is defined only for better understanding of thread dumps where are informations like
    * java.lang.Object@xxxxxxxx owner thread_x
    *   wait for enter thread_y
    */
    private static final class InternalLock {
        InternalLock() {
        }
    }

    private static final class QueueCell {
        int mode;
        Thread t;
        boolean signal;
        boolean left;

        /** priority of the cell */
        int priority2;

        public QueueCell(int mode, Thread t) {
            this.mode = mode;
            this.t = t;
            this.left = false;
            this.priority2 = 0;
        }

        @Override
        public String toString() {
            return super.toString() + " mode: " + mode + " thread: " + t; // NOI18N
        }

        /** @return priority of this cell */
        public long getPriority() {
            return ((priority2 == 0) ? t.getPriority() : priority2);
        }

        /** @return true iff the thread left sleep */
        public boolean isGotOut() {
            return left;
        }

        /** current thread will sleep until wakeMeUp is called
        * if wakeMeUp was already called then the thread will not sleep
        */
        public synchronized void sleep() {
            boolean wasInterrupted = false;
            try {
                while (!signal) {
                    try {
                        long start = System.currentTimeMillis();
                        wait();
                        if (LOG.isLoggable(Level.FINE) && EventQueue.isDispatchThread() && (System.currentTimeMillis() - start) > 1000) {
                            LOG.log(Level.WARNING, toString(), new IllegalStateException("blocking on a mutex from EQ"));
                        }
                        return;
                    } catch (InterruptedException e) {
                        wasInterrupted = true;
                        LOG.log(Level.FINE, null, e);
                    }
                }
            } finally {
                left = true;
                if (wasInterrupted) { // #129003
                    Thread.currentThread().interrupt();
                }
            }
        }

        /** sends signal to a sleeper - to a thread that is in the sleep() */
        public void wakeMeUp() {
            signal = true;
            notifyAll();
        }
    }
    
    /** Provides access to Mutex's internal methods.
     *
     * This class can be used when one wants to avoid creating a
     * bunch of Runnables. Instead,
     * <pre>
     * try {
     *     enterXAccess ();
     *     yourCustomMethod ();
     * } finally {
     *     exitXAccess ();
     * }
     * </pre>
     * can be used.
     *
     * You must, however, control the related Mutex, i.e. you must be creator of
     * the Mutex.
     *
     * @since 1.17
     */
    public static final class Privileged {
        private Mutex parent;

        final void setParent(Mutex parent) {
            this.parent = parent;
        }

        public void enterReadAccess() {
            parent.readEnter(Thread.currentThread());
        }

        public void enterWriteAccess() {
            parent.writeEnter(Thread.currentThread());
        }

        public void exitReadAccess() {
            parent.leave(Thread.currentThread());
        }

        public void exitWriteAccess() {
            parent.leave(Thread.currentThread());
        }
    }

    private void setGrantedMode(int mode) {
        assert Thread.holdsLock(LOCK);
        if (grantedMode != CHAIN && mode == CHAIN) {
            this.origMode = grantedMode;
        }
        grantedMode = mode;
    }
    
    private int getGrantedMode(boolean skipCheck) {
        assert skipCheck || Thread.holdsLock(LOCK);
        return grantedMode;
    }

    private int getOrigMode() {
        assert Thread.holdsLock(LOCK);
        return origMode;
    }

    private Map<Thread,ThreadInfo> getRegisteredThreads() {
        assert Thread.holdsLock(LOCK);
        return registeredThreads;
    }
    
}
