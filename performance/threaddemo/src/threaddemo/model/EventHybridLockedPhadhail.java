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
final class EventHybridLockedPhadhail implements Phadhail, PhadhailListener {
    
    /**
     * add/removePhadhailListener must be called serially
     * though they may be called with only a read lock
     */
    private static final Object LISTENER_LOCK = new String("LP.LL");
    
    private static final Map instances = new WeakHashMap(); // Map<Phadhail,Reference<Phadhail>>
    
    /** factory */
    public static Phadhail forPhadhail(Phadhail _ph) {
        Reference r = (Reference)instances.get(_ph);
        Phadhail ph = (r != null) ? (Phadhail)r.get() : null;
        if (ph == null) {
            ph = BufferedPhadhail.forPhadhail(new EventHybridLockedPhadhail(_ph));
            instances.put(_ph, new WeakReference(ph));
        }
        return ph;
    }
    
    private final Phadhail ph;
    private List listeners = null; // List<PhadhailListener>
    
    private EventHybridLockedPhadhail(Phadhail ph) {
        this.ph = ph;
    }
    
    public List getChildren() {
        List phs; // List<Phadhail>
        phs = (List)Mutex.EVENT_HYBRID.readAccess(new Mutex.Action() {
            public Object run() {
                return ph.getChildren();
            }
        });
        return new LockedChildrenList(phs);
    }
    
    private static final class LockedChildrenList extends AbstractList {
        private final List orig; // List<Phadhail>
        private final Phadhail[] kids;
        public LockedChildrenList(List orig) {
            this.orig = orig;
            kids = new Phadhail[orig.size()];
        }
        public Object get(int i) {
            if (kids[i] == null) {
                kids[i] = forPhadhail((Phadhail)orig.get(i));
            }
             return kids[i];
        }
        public int size() {
            return kids.length;
        }
    }
    
    public String getName() {
        return (String)Mutex.EVENT_HYBRID.readAccess(new Mutex.Action() {
            public Object run() {
                return ph.getName();
            }
        });
    }
    
    public String getPath() {
        return (String)Mutex.EVENT_HYBRID.readAccess(new Mutex.Action() {
            public Object run() {
                return ph.getPath();
            }
        });
    }
    
    public boolean hasChildren() {
        return ((Boolean)Mutex.EVENT_HYBRID.readAccess(new Mutex.Action() {
            public Object run() {
                return ph.hasChildren() ? Boolean.TRUE : Boolean.FALSE;
            }
        })).booleanValue();
    }
    
    public void addPhadhailListener(final PhadhailListener l) {
        Mutex.EVENT_HYBRID.readAccess(new Runnable() {
            public void run() {
                synchronized (LISTENER_LOCK) {
                    if (listeners == null) {
                        ph.addPhadhailListener(EventHybridLockedPhadhail.this);
                        listeners = new ArrayList();
                    }
                    listeners.add(l);
                }
            }
        });
    }
    
    public void removePhadhailListener(final PhadhailListener l) {
        Mutex.EVENT_HYBRID.readAccess(new Runnable() {
            public void run() {
                synchronized (LISTENER_LOCK) {
                    if (listeners != null) {
                        listeners.remove(l);
                        if (listeners.isEmpty()) {
                            listeners = null;
                            ph.removePhadhailListener(EventHybridLockedPhadhail.this);
                        }
                    }
                }
            }
        });
    }
    
    public void rename(final String nue) throws IOException {
        try {
            Mutex.EVENT_HYBRID.writeAccess(new Mutex.ExceptionAction() {
                public Object run() throws IOException {
                    ph.rename(nue);
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
                    return forPhadhail(ph.createContainerPhadhail(name));
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
                    return forPhadhail(ph.createLeafPhadhail(name));
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
                    ph.delete();
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
                    return ph.getInputStream();
                }
            });
        } catch (MutexException e) {
            throw (IOException)e.getException();
        }
    }
    
    public OutputStream getOutputStream() throws IOException {
        // See comments in LockedPhadhail.getOutputStream.
        try {
            return (OutputStream)Mutex.EVENT_HYBRID.readAccess(new Mutex.ExceptionAction() {
                public Object run() throws IOException {
                    return ph.getOutputStream();
                }
            });
        } catch (MutexException e) {
            throw (IOException)e.getException();
        }
    }
    
    public String toString() {
        return "EventHybridLockedPhadhail<" + ph + ">";
    }
    
    public void childrenChanged(PhadhailEvent _ev) {
        Mutex.EVENT_HYBRID.readAccess(new Runnable() {
            public void run() {
                if (listeners != null) {
                    PhadhailEvent ev = PhadhailEvent.create(EventHybridLockedPhadhail.this);
                    Iterator it = listeners.iterator();
                    while (it.hasNext()) {
                        ((PhadhailListener)it.next()).childrenChanged(ev);
                    }
                }
            }
        });
    }
    
    public void nameChanged(final PhadhailNameEvent _ev) {
        Mutex.EVENT_HYBRID.readAccess(new Runnable() {
            public void run() {
                if (listeners != null) {
                    PhadhailNameEvent ev = PhadhailNameEvent.create(EventHybridLockedPhadhail.this, _ev.getOldName(), _ev.getNewName());
                    Iterator it = listeners.iterator();
                    while (it.hasNext()) {
                        ((PhadhailListener)it.next()).nameChanged(ev);
                    }
                }
            }
        });
    }
    
}
