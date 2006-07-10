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

package threaddemo.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import threaddemo.locking.RWLock;

/**
 * Wraps a plain Phadhail and buffers its list of children.
 * @author Jesse Glick
 */
final class BufferedPhadhail implements Phadhail, PhadhailListener {

    private static final Map<Phadhail, Reference<BufferedPhadhail>> instances = new WeakHashMap<Phadhail,Reference<BufferedPhadhail>>();
    
    public static Phadhail forPhadhail(Phadhail ph) {
        if (ph.hasChildren() && !(ph instanceof BufferedPhadhail)) {
            Reference<BufferedPhadhail> r = instances.get(ph);
            BufferedPhadhail bph = (r != null) ? r.get() : null;
            if (bph == null) {
                bph = new BufferedPhadhail(ph);
                instances.put(ph, new WeakReference<BufferedPhadhail>(bph));
            }
            return bph;
        } else {
            return ph;
        }
    }
    
    private final Phadhail ph;
    private Reference<List<Phadhail>> kids;
    private List<PhadhailListener> listeners = null;
    
    private BufferedPhadhail(Phadhail ph) {
        this.ph = ph;
    }
    
    public List<Phadhail> getChildren() {
        List<Phadhail> phs = null;
        if (kids != null) {
            phs = kids.get();
        }
        if (phs == null) {
            // Need to (re)calculate the children.
            phs = new BufferedChildrenList(ph.getChildren());
            kids = new WeakReference<List<Phadhail>>(phs);
        }
        return phs;
    }
    
    private static final class BufferedChildrenList extends AbstractList<Phadhail> {
        private final List<Phadhail> orig;
        private final Phadhail[] kids;
        public BufferedChildrenList(List<Phadhail> orig) {
            this.orig = orig;
            kids = new Phadhail[orig.size()];
        }
        public Phadhail get(int i) {
            if (kids[i] == null) {
                kids[i] = forPhadhail(orig.get(i));
            }
             return kids[i];
        }
        public int size() {
            return kids.length;
        }
    }
    
    public String getName() {
        return ph.getName();
    }
    
    public String getPath() {
        return ph.getPath();
    }
    
    public boolean hasChildren() {
        return ph.hasChildren();
    }
    
    public void addPhadhailListener(PhadhailListener l) {
        if (listeners == null) {
            ph.addPhadhailListener(this);
            listeners = new ArrayList<PhadhailListener>();
        }
        listeners.add(l);
    }
    
    public void removePhadhailListener(PhadhailListener l) {
        if (listeners != null) {
            listeners.remove(l);
            if (listeners.isEmpty()) {
                listeners = null;
                ph.removePhadhailListener(this);
            }
        }
    }
    
    public void rename(String nue) throws IOException {
        ph.rename(nue);
    }
    
    public Phadhail createContainerPhadhail(String name) throws IOException {
        return ph.createContainerPhadhail(name);
    }
    
    public Phadhail createLeafPhadhail(String name) throws IOException {
        return ph.createLeafPhadhail(name);
    }
    
    public void delete() throws IOException {
        ph.delete();
    }
    
    public InputStream getInputStream() throws IOException {
        return ph.getInputStream();
    }
    
    public OutputStream getOutputStream() throws IOException {
        return ph.getOutputStream();
    }
    
    public void childrenChanged(PhadhailEvent ev) {
        // clear cache
        kids = null;
        if (listeners != null) {
            PhadhailEvent ev2 = PhadhailEvent.create(this);
            for (PhadhailListener l : listeners) {
                l.childrenChanged(ev2);
            }
        }
    }
    
    public void nameChanged(PhadhailNameEvent ev) {
        if (listeners != null) {
            PhadhailNameEvent ev2 = PhadhailNameEvent.create(this, ev.getOldName(), ev.getNewName());
            for (PhadhailListener l : listeners) {
                l.nameChanged(ev2);
            }
        }
    }
    
    public String toString() {
        return "BufferedPhadhail<" + ph + ">@" + Integer.toHexString(System.identityHashCode(this));
    }
    
    public RWLock lock() {
        return ph.lock();
    }
    
}
