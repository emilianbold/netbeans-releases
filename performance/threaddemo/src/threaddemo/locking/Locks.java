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

// XXX uses: FolderChildren, Children.MutexChildren, module system
// XXX lock wrapper for AbstractDocument (or Document + NbDocument.WriteLockable)

package threaddemo.locking;

/**
 * Factory for locks.
 * @author Ales Novak (old code), Jesse Glick (rewrite - #32439)
 */
public class Locks {

    private Locks() {}

    /**
     * Pseudo-lock that allows code to be synchronized with the AWT event dispatch thread.
     * This is handy in that you can define a constant of type Lock in some API, initially
     * set to a normal lock, and then switch everything to the event thread (or vice-versa).
     * <p>It behaves somewhat differently from a read-write lock.
     * <ol>
     * <li>There is no distinction between read and write access. There is only one
     *     access mode, which is exclusive, and runs on the AWT thread, not in the
     *     caller thread.
     * <li>There is no {@link PrivilegedLock}, so you cannot make entry or exit calls
     *     by themselves (which would make no sense).
     * <li>You cannot specify a level. The event lock is considered to be at a higher
     *     level than any ordinary lock with a defined level. This means that from the
     *     event thread, you can enter any lock (subject to other restrictions), but
     *     while holding any <em>ordered</em> lock you may not block on the event thread
     *     (using <code>Locks.eventLock</code> methods).
     * <li>{@link Lock#read(LockAction)}, {@link Lock#read(LockExceptionAction)},
     *     {@link Lock#write(LockAction)}, {@link Lock#write(LockExceptionAction)},
     *     {@link Lock#read(Runnable)}, and {@link Lock#write(Runnable)} when called from the
     *     event thread run synchronously. Else they all block, like
     *     {@link java.awt.EventQueue#invokeAndWait}.
     * <li>{@link Lock#readLater(Runnable)} and {@link Lock#writeLater(Runnable)} run asynchronously, like
     *     {@link java.awt.EventQueue#invokeLater}.
     * <li>{@link Lock#canRead} and {@link Lock#canWrite} just test whether you are in the event
     *     thread, like {@link java.awt.EventQueue#isDispatchThread}.
     * </ol>
     */
    public static RWLock event() {
        return EventLock.DEFAULT;
    }
    
    /**
     * XXX
     */
    public static synchronized RWLock eventHybrid() {
        return EventHybridLock.DEFAULT;
    }
    
    /**
     * XXX
     */
    public static RWLock monitor(Object monitor) {
        return new MonitorLock(monitor);
    }
    
    /**
     * Create a read/write lock.
     * Allows control over resources that
     * can be read by several readers at once but only written by one writer.
     * Wrapper for {@link java.util.concurrent.locks.ReentrantReadWriteLock}.
     */
    public static RWLock readWrite() {
        return new ReadWriteLockWrapper();
    }
    
    /**
     * Create a lock with a privileged key.
     * @param privileged a key which may be used to call unbalanced entry/exit methods directly
     * @see #readWrite()
     */
    public static RWLock readWrite(PrivilegedLock privileged) {
        DuplexLock l = (DuplexLock) readWrite();
        privileged.setParent(l);
        return l;
    }
    
}
