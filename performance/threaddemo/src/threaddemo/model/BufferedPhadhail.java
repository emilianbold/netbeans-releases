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

/**
 * Wraps a plain Phadhail and buffers its list of children.
 * @author Jesse Glick
 */
final class BufferedPhadhail implements Phadhail, PhadhailListener {
    
    private static final Map instances = new WeakHashMap(); // Map<Phadhail,BufferedPhadhail>
    
    public static Phadhail forPhadhail(Phadhail ph) {
        if (ph.hasChildren() && !(ph instanceof BufferedPhadhail)) {
            BufferedPhadhail bph = (BufferedPhadhail)instances.get(ph);
            if (bph == null) {
                bph = new BufferedPhadhail(ph);
                instances.put(ph, bph);
            }
            return bph;
        } else {
            return ph;
        }
    }
    
    private final Phadhail ph;
    private Reference kids; // Reference<List<Phadhail>>
    private int listenerCount = 0;
    
    private BufferedPhadhail(Phadhail ph) {
        this.ph = ph;
    }
    
    public List getChildren() {
        List phs = null; // List<Phadhail>
        if (kids != null) {
            phs = (List)kids.get();
        }
        if (phs == null) {
            // Need to (re)calculate the children.
            phs = new BufferedChildrenList(ph.getChildren());
            kids = new WeakReference(phs);
        }
        return phs;
    }
    
    private static final class BufferedChildrenList extends AbstractList {
        private final List orig; // List<Phadhail>
        private final Phadhail[] kids;
        public BufferedChildrenList(List orig) {
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
        return ph.getName();
    }
    
    public String getPath() {
        return ph.getPath();
    }
    
    public boolean hasChildren() {
        return ph.hasChildren();
    }
    
    public void addPhadhailListener(PhadhailListener l) {
        // XXX actually this is not 100% correct because the fired events
        // will refer to an *unbuffered* phadhail
        ph.addPhadhailListener(l);
        if (listenerCount == 0) {
            ph.addPhadhailListener(this);
        }
        listenerCount++;
    }
    
    public void removePhadhailListener(PhadhailListener l) {
        ph.removePhadhailListener(l);
        listenerCount--;
        if (listenerCount == 0) {
            ph.removePhadhailListener(this);
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
    }
    
    public void nameChanged(PhadhailNameEvent ev) {
        // ignore
    }
    
    public String toString() {
        return "BufferedPhadhail<" + ph + ">@" + Integer.toHexString(System.identityHashCode(this));
    }
    
}
