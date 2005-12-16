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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import threaddemo.locking.RWLock;
import threaddemo.locking.Locks;

/**
 * A phadhail in which all model methods are locked with a simple monitor.
 * In this variant, the impl acquires locks automatically, though another
 * style would be to require the client to do this.
 * @author Jesse Glick
 */
final class MonitoredPhadhail extends AbstractPhadhail {
    
    private static final class LOCK {}
    private static final Object LOCK = new LOCK();
    private static final RWLock MLOCK = Locks.monitor(LOCK);
    
    private static final AbstractPhadhail.Factory FACTORY = new AbstractPhadhail.Factory() {
        public AbstractPhadhail create(File f) {
            return new MonitoredPhadhail(f);
        }
    };
    
    public static Phadhail create(File f) {
        return forFile(f, FACTORY);
    }
    
    private MonitoredPhadhail(File f) {
        super(f);
    }
    
    protected Factory factory() {
        return FACTORY;
    }
    
    public List<Phadhail> getChildren() {
        synchronized (LOCK) {
            return super.getChildren();
        }
    }
    
    public String getName() {
        synchronized (LOCK) {
            return super.getName();
        }
    }
    
    public String getPath() {
        synchronized (LOCK) {
            return super.getPath();
        }
    }
    
    public boolean hasChildren() {
        synchronized (LOCK) {
            return super.hasChildren();
        }
    }
    
    public void rename(String nue) throws IOException {
        synchronized (LOCK) {
            super.rename(nue);
        }
    }
    
    public Phadhail createContainerPhadhail(String name) throws IOException {
        synchronized (LOCK) {
            return super.createContainerPhadhail(name);
        }
    }
    
    public Phadhail createLeafPhadhail(String name) throws IOException {
        synchronized (LOCK) {
            return super.createLeafPhadhail(name);
        }
    }
    
    public void delete() throws IOException {
        synchronized (LOCK) {
            super.delete();
        }
    }
    
    public InputStream getInputStream() throws IOException {
        synchronized (LOCK) {
            return super.getInputStream();
        }
    }
    
    public OutputStream getOutputStream() throws IOException {
        synchronized (LOCK) {
            return super.getOutputStream();
        }
    }
    
    public RWLock lock() {
        return MLOCK;
    }
    
}
