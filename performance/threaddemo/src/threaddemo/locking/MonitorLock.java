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

import java.lang.reflect.InvocationTargetException;

// XXX could track read vs. write state

/**
 * Simple lock that actually just uses a simple synchronization monitor.
 * @author Jesse Glick
 */
final class MonitorLock implements Lock {
    
    private final Object monitor;
    
    MonitorLock(Object monitor, int level) {
        this.monitor = monitor;
        // XXX handle level
    }
    
    public boolean canRead() {
        return Thread.holdsLock(monitor);
    }
    
    public Object read(LockExceptionAction action) throws InvocationTargetException {
        synchronized (monitor) {
            try {
                return action.run();
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new InvocationTargetException(e);
            }
        }
    }
    
    public Object read(LockAction action) {
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
        ReadWriteLock.LATER.post(new Runnable() {
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
    
    public Object write(LockAction action) {
        return read(action);
    }
    
    public Object write(LockExceptionAction action) throws InvocationTargetException {
        return read(action);
    }
    
    public void writeLater(Runnable action) {
        readLater(action);
    }
    
    public String toString() {
        return "MonitorLock<monitor=" + monitor + ">";
    }
    
}
