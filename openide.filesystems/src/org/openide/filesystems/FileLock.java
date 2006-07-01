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
package org.openide.filesystems;

import java.io.ByteArrayOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;


/** Represents an acquired lock on a <code>FileObject</code>.
* Typical usage includes locking the file in the editor on first
* modification, and then using this object to ensure exclusive access when
* overwriting the file (saving) by using {@link FileObject#getOutputStream}.
* Also used for renames, deletes, &amp;c.
* <p>Note that such locks are only used to protect against concurrent write accesses,
* and are not used for read operations (i.e. they are <em>not</em> write-one-read-many locks).
* Normally this is sufficient protection. If you really need an atomic read, you may
* simply lock the file, perform the read, and unlock it when done. The file will still
* be protected against writes, although the read operation did not request a lock.
*
* @see FileObject
*
* @author Petr Hamernik, Jaroslav Tulach, Ian Formanek
* @version 0.16, Jun 5, 1997
*
*/
public class FileLock extends Object {
    // ========================= NONE file lock =====================================

    /** Constant that can be used in filesystems that do not support locking.
     * Represents a lock which is never valid.
    */
    public static final FileLock NONE = new FileLock() {
            /** @return false always. */
            public boolean isValid() {
                return false;
            }
        };

    /** Determines if lock is locked or if it was released. */
    private boolean locked = true;
    private Throwable lockedBy;

    public FileLock() {
        assert (lockedBy = new Throwable()) != null;
    }

    // ===============================================================================
    //  This part of code could be used for monitoring of closing file streams.

    /*  public static java.util.HashMap locks = new java.util.HashMap();
      public FileLock() {
        locks.put(this, new Exception()); int size = locks.size();
        System.out.println ("locks:"+(size-1)+" => "+size);
      }
      public void releaseLock() {
        locked = false; locks.remove(this); int size = locks.size();
        System.out.println ("locks:"+(size+1)+" => "+size);
      } */

    //  End of the debug part
    // ============================================================================
    //  Begin of the original part

    /** Release this lock.
    * In typical usage this method will be called in a <code>finally</code> clause.
    */
    public void releaseLock() {
        locked = false;
    }

    //  End of the original part
    // ============================================================================

    /** Test whether this lock is still active, or released.
    * @return <code>true</code> if lock is still active
    */
    public boolean isValid() {
        return locked;
    }

    /** Finalize this object. Calls {@link #releaseLock} to release the lock if the program
    * for some reason failed to.
    */
    public void finalize() {
        assert (!isValid()) : assertMessageForInvalidLocks();
        releaseLock();
    }

    private String assertMessageForInvalidLocks() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        if (lockedBy != null) {
            Logger.global.log(Level.WARNING, null,
                              new Exception("Not released lock for file: " +
                                            toString() +
                                            " (traped in finalizer)").initCause(lockedBy));//NOI18N
        }

        releaseLock();
        return bos.toString();
    }
}
