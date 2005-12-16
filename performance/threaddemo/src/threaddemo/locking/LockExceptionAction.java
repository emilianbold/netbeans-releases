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

package threaddemo.locking;

/** Action to be executed in a lock, possibly throwing checked exceptions.
 * May throw a checked exception.
 * Unchecked exceptions will be propagated to calling code without encapsulation.
 *
 */
public interface LockExceptionAction<T, E extends Exception> {
    
    /**
     * Execute the action.
     * Can throw an exception.
     * @return any object, then returned from {@link Lock#read(LockExceptionAction)} or {@link Lock#write(LockExceptionAction)}
     * @throws E any exception the body needs to throw
     */
    public T run() throws E;
    
}

