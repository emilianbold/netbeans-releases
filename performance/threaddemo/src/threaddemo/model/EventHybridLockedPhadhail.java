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

package threaddemo.model;

import java.io.*;
import java.lang.ref.*;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import threaddemo.locking.Lock;
import threaddemo.locking.LockAction;
import threaddemo.locking.LockExceptionAction;
import threaddemo.locking.Locks;

/**
 * Similar to LockedPhadhail but using the "event hybrid" lock.
 * @author Jesse Glick
 */
final class EventHybridLockedPhadhail extends AbstractPhadhail {
    
    private static final Factory FACTORY = new Factory() {
        public AbstractPhadhail create(File f) {
            return new EventHybridLockedPhadhail(f);
        }
    };
    
    public static Phadhail create(File f) {
        return forFile(f, FACTORY);
    }
    
    private EventHybridLockedPhadhail(File f) {
        super(f);
    }
    
    protected Factory factory() {
        return FACTORY;
    }
    
    public List getChildren() {
        return (List)Locks.eventHybridLock().read(new LockAction() {
            public Object run() {
                return EventHybridLockedPhadhail.super.getChildren();
            }
        });
    }
    
    public String getName() {
        return (String)Locks.eventHybridLock().read(new LockAction() {
            public Object run() {
                return EventHybridLockedPhadhail.super.getName();
            }
        });
    }
    
    public String getPath() {
        return (String)Locks.eventHybridLock().read(new LockAction() {
            public Object run() {
                return EventHybridLockedPhadhail.super.getPath();
            }
        });
    }
    
    public boolean hasChildren() {
        return ((Boolean)Locks.eventHybridLock().read(new LockAction() {
            public Object run() {
                return EventHybridLockedPhadhail.super.hasChildren() ? Boolean.TRUE : Boolean.FALSE;
            }
        })).booleanValue();
    }
    
    public void rename(final String nue) throws IOException {
        try {
            Locks.eventHybridLock().write(new LockExceptionAction() {
                public Object run() throws IOException {
                    EventHybridLockedPhadhail.super.rename(nue);
                    return null;
                }
            });
        } catch (InvocationTargetException e) {
            throw (IOException)e.getCause();
        }
    }
    
    public Phadhail createContainerPhadhail(final String name) throws IOException {
        try {
            return (Phadhail)Locks.eventHybridLock().write(new LockExceptionAction() {
                public Object run() throws IOException {
                    return EventHybridLockedPhadhail.super.createContainerPhadhail(name);
                }
            });
        } catch (InvocationTargetException e) {
            throw (IOException)e.getCause();
        }
    }
    
    public Phadhail createLeafPhadhail(final String name) throws IOException {
        try {
            return (Phadhail)Locks.eventHybridLock().write(new LockExceptionAction() {
                public Object run() throws IOException {
                    return EventHybridLockedPhadhail.super.createLeafPhadhail(name);
                }
            });
        } catch (InvocationTargetException e) {
            throw (IOException)e.getCause();
        }
    }
    
    public void delete() throws IOException {
        try {
            Locks.eventHybridLock().write(new LockExceptionAction() {
                public Object run() throws IOException {
                    EventHybridLockedPhadhail.super.delete();
                    return null;
                }
            });
        } catch (InvocationTargetException e) {
            throw (IOException)e.getCause();
        }
    }
    
    public InputStream getInputStream() throws IOException {
        try {
            return (InputStream)Locks.eventHybridLock().read(new LockExceptionAction() {
                public Object run() throws IOException {
                    return EventHybridLockedPhadhail.super.getInputStream();
                }
            });
        } catch (InvocationTargetException e) {
            throw (IOException)e.getCause();
        }
    }
    
    public OutputStream getOutputStream() throws IOException {
        // See comments in AbstractPhadhail.getOutputStream.
        try {
            return (OutputStream)Locks.eventHybridLock().read(new LockExceptionAction() {
                public Object run() throws IOException {
                    return EventHybridLockedPhadhail.super.getOutputStream();
                }
            });
        } catch (InvocationTargetException e) {
            throw (IOException)e.getCause();
        }
    }
    
    public Lock lock() {
        return Locks.eventHybridLock();
    }
    
}
