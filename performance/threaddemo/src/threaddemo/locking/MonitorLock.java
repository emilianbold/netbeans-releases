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
