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
import java.util.*;
import org.openide.util.Mutex;
import org.openide.util.MutexException;

/**
 * Similar to LockedPhadhail but using the "event hybrid" mutex.
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
        return (List)Mutex.EVENT_HYBRID.readAccess(new Mutex.Action() {
            public Object run() {
                return EventHybridLockedPhadhail.super.getChildren();
            }
        });
    }
    
    public String getName() {
        return (String)Mutex.EVENT_HYBRID.readAccess(new Mutex.Action() {
            public Object run() {
                return EventHybridLockedPhadhail.super.getName();
            }
        });
    }
    
    public String getPath() {
        return (String)Mutex.EVENT_HYBRID.readAccess(new Mutex.Action() {
            public Object run() {
                return EventHybridLockedPhadhail.super.getPath();
            }
        });
    }
    
    public boolean hasChildren() {
        return ((Boolean)Mutex.EVENT_HYBRID.readAccess(new Mutex.Action() {
            public Object run() {
                return EventHybridLockedPhadhail.super.hasChildren() ? Boolean.TRUE : Boolean.FALSE;
            }
        })).booleanValue();
    }
    
    public void rename(final String nue) throws IOException {
        try {
            Mutex.EVENT_HYBRID.writeAccess(new Mutex.ExceptionAction() {
                public Object run() throws IOException {
                    EventHybridLockedPhadhail.super.rename(nue);
                    return null;
                }
            });
        } catch (MutexException e) {
            throw (IOException)e.getException();
        }
    }
    
    public Phadhail createContainerPhadhail(final String name) throws IOException {
        try {
            return (Phadhail)Mutex.EVENT_HYBRID.writeAccess(new Mutex.ExceptionAction() {
                public Object run() throws IOException {
                    return EventHybridLockedPhadhail.super.createContainerPhadhail(name);
                }
            });
        } catch (MutexException e) {
            throw (IOException)e.getException();
        }
    }
    
    public Phadhail createLeafPhadhail(final String name) throws IOException {
        try {
            return (Phadhail)Mutex.EVENT_HYBRID.writeAccess(new Mutex.ExceptionAction() {
                public Object run() throws IOException {
                    return EventHybridLockedPhadhail.super.createLeafPhadhail(name);
                }
            });
        } catch (MutexException e) {
            throw (IOException)e.getException();
        }
    }
    
    public void delete() throws IOException {
        try {
            Mutex.EVENT_HYBRID.writeAccess(new Mutex.ExceptionAction() {
                public Object run() throws IOException {
                    EventHybridLockedPhadhail.super.delete();
                    return null;
                }
            });
        } catch (MutexException e) {
            throw (IOException)e.getException();
        }
    }
    
    public InputStream getInputStream() throws IOException {
        try {
            return (InputStream)Mutex.EVENT_HYBRID.readAccess(new Mutex.ExceptionAction() {
                public Object run() throws IOException {
                    return EventHybridLockedPhadhail.super.getInputStream();
                }
            });
        } catch (MutexException e) {
            throw (IOException)e.getException();
        }
    }
    
    public OutputStream getOutputStream() throws IOException {
        // See comments in AbstractPhadhail.getOutputStream.
        try {
            return (OutputStream)Mutex.EVENT_HYBRID.readAccess(new Mutex.ExceptionAction() {
                public Object run() throws IOException {
                    return EventHybridLockedPhadhail.super.getOutputStream();
                }
            });
        } catch (MutexException e) {
            throw (IOException)e.getException();
        }
    }
    
    public Mutex mutex() {
        return Mutex.EVENT_HYBRID;
    }
    
}
