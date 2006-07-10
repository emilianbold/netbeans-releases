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

package threaddemo.locking;

// XXX add support for read/writeSoon(Runnable)?
// XXX why bother with InvocationTargetException?

/**
 * Some kind of a lock, with support for possible read and write modes.
 * <P>
 * Examples of use:
 *
 * <pre>
 * Lock m = Locks.readWriteLock("foo", 0);
 *
 * // Grant write access, compute an integer and return it:
 * return m.write(new LockAction<Integer>() {
 *     public Integer run() {
 *         return 1;
 *     }
 * });
 *
 * // Obtain read access, do some computation, possibly throw an IOException:
 * m.read(new LockExceptionAction<Void,IOException>() {
 *     public Void run() throws IOException {
 *         if (...) throw new IOException();
 *         return null;
 *     }
 * });
 * </pre>
 *
 * @see Locks
 * @author Jesse Glick
 */
public interface RWLock {
    
    /** Run an action only with read access.
    * @param action the action to perform
    * @return the object returned from {@link LockAction#run}
    */
    <T> T read(LockAction<T> action);

    /** Run an action with read access and possibly throw a checked exception.
    * Note that <em>runtime exceptions</em> are always passed through, and neither
    * require this invocation style, nor are encapsulated.
    * @param action the action to execute
    * @return the object returned from {@link LockExceptionAction#run}
    * @throws E a checked exception, if any
    * @throws RuntimeException if any runtime exception is thrown from the run method
    * @see #read(LockAction)
    */
    <T, E extends Exception> T read(LockExceptionAction<T, E> action) throws E;

    /** Run an action with read access, returning no result.
    * @param action the action to perform
    * @see #read(LockAction)
    */
    void read(Runnable action);
    
    /**
     * Run an action asynch with read access.
    * @param action the action to perform
    * @see #read(Runnable)
    */
    void readLater(Runnable action);

    /**
     * Run an action with write access.
     * <p><strong>May not be called while holding read access.</strong>
     * @param action the action to perform
     * @return the result of {@link LockAction#run}
     */
    <T> T write(LockAction<T> action);

    /**
     * Run an action with write access and possibly throw an exception.
     * <p><strong>May not be called while holding read access.</strong>
     * @param action the action to execute
     * @return the result of {@link LockExceptionAction#run}
     * @throws E a checked exception, if any
     * @throws RuntimeException if a runtime exception is thrown in the action
     * @see #write(LockAction)
     * @see #read(LockExceptionAction)
     */
    <T, E extends Exception> T write(LockExceptionAction<T, E> action) throws E;

    /**
     * Run an action with write access and return no result.
     * @param action the action to perform
     * @see #write(LockAction)
     * @see #read(Runnable)
     */
    void write(Runnable action);
    
    /**
     * Run an action asynchronously with write access.
     * @param action the action to perform
     * @see #write(LockAction)
     * @see #readLater(Runnable)
     */
    void writeLater(Runnable action);

    /**
     * Check if the current thread is holding a read or write lock.
     * @return true if either read or write access is available
     */
    boolean canRead();

    /**
     * Check if the current thread is holding the write lock.
     * Note that this will be false in case write access was entered and then
     * read access was entered inside of that, until the nested read access is
     * again exited.
     * @return true if write access is available
     */
    boolean canWrite();

}
