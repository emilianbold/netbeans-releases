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
