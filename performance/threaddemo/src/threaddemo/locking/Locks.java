/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

// XXX uses: FolderChildren, Children.MutexChildren, module system
// XXX EventSynch?

package threaddemo.locking;

/** Read-many/write-one lock.
* Allows control over resources that
* can be read by several readers at once but only written by one writer.
* <P>
* It is guaranteed that if you are a writer you can also enter the
* lock as a reader. But you cannot enter the write lock if you hold
* the read lock, since that can cause deadlocks.
* <P>
 * This implementation will probably not starve a writer or reader indefinitely,
 * but the exact behavior is at the mercy of {@link Object#wait()} and {@link Object#notifyAll()}.
* <P>
* Examples of use:
*
* <pre>
* Lock m = Locks.readWriteLock("foo", 0);
* 
* // Grant write access, compute an integer and return it:
* return (Integer)m.write(new LockAction() {
*     public Object run() {
*         return new Integer(1);
*     }
* });
* 
* // Obtain read access, do some computation, possibly throw an IOException:
* try {
*     m.read(new LockExceptionAction() {
*         public Object run() throws IOException {
*             if (...) throw new IOException();
*             return null;
*         }
*     });
* } catch (InvocationTargetException ex) {
*     throw (IOException)ex.getCause();
* }
* </pre>
 *
 * <p>Locks may have defined levels. A lock ordering is defined among
 * locks with levels (those without levels do not participate in this
 * ordering at all). While holding a lock with a high level, you may enter
 * a lock with a lower level. But you may not enter a lock with a high
 * level while holding a lock with a lower or equal level (unless you are
 * reentering the same lock). This scheme should prevent a class of deadlocks
 * resulting from two threads acquiring locks in opposite orders.
*
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
     * <li>{@link #read(LockAction)}, {@link #read(LockExceptionAction)},
     *     {@link #write(LockAction)}, {@link #write(LockExceptionAction)},
     *     {@link #read(Runnable)}, and {@link #write(Runnabe)} when called from the
     *     event thread run synchronously. Else they all block, like
     *     {@link EventQueue#invokeAndWait}.
     * <li>{@link #readLater(Runnable)} and {@link #writeLater(Runnable)} run asynchronously, like
     *     {@link EventQueue#invokeLater}.
     * <li>{@link #canRead} and {@link #canWrite} just test whether you are in the event
     *     thread, like {@link EventQueue#isDispatchThread}.
     * </ol>
     */
    public static Lock eventLock() {
        return EventLock.DEFAULT;
    }
    
    /**
     * XXX
     */
    public static synchronized Lock eventHybridLock() {
        return EventHybridLock.DEFAULT;
    }
    
    /**
     * XXX
     */
    public static Lock monitorLock(Object monitor) {
        return new MonitorLock(monitor);
    }
    
    /**
     * Create a read/write lock with a defined level.
     * @param name an identifying name to use when debugging
     * @param level an integer level other than {@link Integer.MAX_VALUE} (see class Javadoc for explanation)
     */
    public static Lock readWriteLock(String name, int level) {
        if (level == Integer.MAX_VALUE) throw new IllegalArgumentException();
        if (name == null) throw new IllegalArgumentException();
        return new ReadWriteLock(name, level);
    }
    
    /**
     * Create a lock with a privileged key and a defined level.
     * @param name an identifying name to use when debugging
     * @param privileged a key which may be used to call unbalanced entry/exit methods directly
     * @param level an integer level other than {@link Integer.MAX_VALUE} (see class Javadoc for explanation)
     */
    public static Lock readWriteLock(String name, PrivilegedLock privileged, int level) {
        ReadWriteLock l = (ReadWriteLock)readWriteLock(name, level);
        privileged.setParent(l);
        return l;
    }
    
}
