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

/**
 * Similar to DefaultPhadhail but all model methods are locked with a mutex.
 * This is not exactly realistic; a real API would expose the mutex
 * from the Phadhail interface, and the impl would not do the locking,
 * the caller would. But this is a rough approximation.
 * @author Jesse Glick
 */
final class LockedPhadhail implements Phadhail, PhadhailListener {
    
    private static final Mutex.Privileged PMUTEX = new Mutex.Privileged();
    private static final Mutex MUTEX = new Mutex(PMUTEX);
    
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
            ph = BufferedPhadhail.forPhadhail(new LockedPhadhail(_ph));
            instances.put(_ph, new WeakReference(ph));
        }
        return ph;
    }
    
    private final Phadhail ph;
    private List listeners = null; // List<PhadhailListener>
    
    private LockedPhadhail(Phadhail ph) {
        this.ph = ph;
    }
    
    public List getChildren() {
        List phs; // List<Phadhail>
        PMUTEX.enterReadAccess();
        try {
            phs = ph.getChildren();
        } finally {
            PMUTEX.exitReadAccess();
        }
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
        PMUTEX.enterReadAccess();
        try {
            return ph.getName();
        } finally {
            PMUTEX.exitReadAccess();
        }
    }
    
    public String getPath() {
        PMUTEX.enterReadAccess();
        try {
            return ph.getPath();
        } finally {
            PMUTEX.exitReadAccess();
        }
    }
    
    public boolean hasChildren() {
        PMUTEX.enterReadAccess();
        try {
            return ph.hasChildren();
        } finally {
            PMUTEX.exitReadAccess();
        }
    }
    
    public void addPhadhailListener(PhadhailListener l) {
        PMUTEX.enterReadAccess();
        try {
            synchronized (LISTENER_LOCK) {
                if (listeners == null) {
                    ph.addPhadhailListener(this);
                    listeners = new ArrayList();
                }
                listeners.add(l);
            }
        } finally {
            PMUTEX.exitReadAccess();
        }
    }
    
    public void removePhadhailListener(PhadhailListener l) {
        PMUTEX.enterReadAccess();
        try {
            synchronized (LISTENER_LOCK) {
                if (listeners != null) {
                    listeners.remove(l);
                    if (listeners.isEmpty()) {
                        listeners = null;
                        ph.removePhadhailListener(this);
                    }
                }
            }
        } finally {
            PMUTEX.exitReadAccess();
        }
    }
    
    public void rename(String nue) throws IOException {
        PMUTEX.enterWriteAccess();
        try {
            ph.rename(nue);
        } finally {
            PMUTEX.exitWriteAccess();
        }
    }
    
    public Phadhail createContainerPhadhail(String name) throws IOException {
        PMUTEX.enterWriteAccess();
        try {
            return forPhadhail(ph.createContainerPhadhail(name));
        } finally {
            PMUTEX.exitWriteAccess();
        }
    }
    
    public Phadhail createLeafPhadhail(String name) throws IOException {
        PMUTEX.enterWriteAccess();
        try {
            return forPhadhail(ph.createLeafPhadhail(name));
        } finally {
            PMUTEX.exitWriteAccess();
        }
    }
    
    public void delete() throws IOException {
        PMUTEX.enterWriteAccess();
        try {
            ph.delete();
        } finally {
            PMUTEX.exitWriteAccess();
        }
    }
    
    public InputStream getInputStream() throws IOException {
        PMUTEX.enterReadAccess();
        try {
            return ph.getInputStream();
        } finally {
            PMUTEX.exitReadAccess();
        }
    }
    
    public OutputStream getOutputStream() throws IOException {
        // Yes, read access - for the sake of the demo, currently Phadhail.getOutputStream
        // is not considered a mutator method (fires no changes); this would be different
        // if PhadhailListener included a content change event.
        // That would be trickier because then you would need to acquire the write mutex
        // when opening the stream but release it when closing the stream (*not* when
        // returning it to the caller).
        PMUTEX.enterReadAccess();
        try {
            return ph.getOutputStream();
        } finally {
            PMUTEX.exitReadAccess();
        }
    }
    
    public String toString() {
        return "LockedPhadhail<" + ph + ">";
    }
    
    public void childrenChanged(PhadhailEvent ev) {
        if (listeners != null) {
            ev = PhadhailEvent.create(this);
            Iterator it = listeners.iterator();
            while (it.hasNext()) {
                ((PhadhailListener)it.next()).childrenChanged(ev);
            }
        }
    }
    
    public void nameChanged(PhadhailNameEvent ev) {
        if (listeners != null) {
            ev = PhadhailNameEvent.create(this, ev.getOldName(), ev.getNewName());
            Iterator it = listeners.iterator();
            while (it.hasNext()) {
                ((PhadhailListener)it.next()).childrenChanged(ev);
            }
        }
    }
    
}
