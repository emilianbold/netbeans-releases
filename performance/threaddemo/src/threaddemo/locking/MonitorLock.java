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

// XXX need test

// XXX could track read vs. write state

/**
 * Simple lock that actually just uses a simple synchronization monitor.
 * @author Jesse Glick
 */
final class MonitorLock implements RWLock {

    private final Object monitor;

    MonitorLock(Object monitor) {
        this.monitor = monitor;
    }

    public boolean canRead() {
        return Thread.holdsLock(monitor);
    }

    public <T, E extends Exception> T read(LockExceptionAction<T,E> action) throws E {
        synchronized (monitor) {
            return action.run();
        }
    }
    
    public <T> T read(LockAction<T> action) {
        synchronized (monitor) {
            return action.run();
        }
    }
    
    public void read(Runnable action) {
        synchronized (monitor) {
            action.run();
        }
    }
    
    public void readLater(final Runnable action) {
        Worker.start(new Runnable() {
            public void run() {
                read(action);
            }
        });
    }
    
    public void write(Runnable action) {
        read(action);
    }
    
    public boolean canWrite() {
        return canRead();
    }
    
    public <T> T write(LockAction<T> action) {
        return read(action);
    }
    
    public <T, E extends Exception> T write(LockExceptionAction<T,E> action) throws E {
        return read(action);
    }
    
    public void writeLater(Runnable action) {
        readLater(action);
    }
    
    public String toString() {
        return "MonitorLock<monitor=" + monitor + ">";
    }
    
}
