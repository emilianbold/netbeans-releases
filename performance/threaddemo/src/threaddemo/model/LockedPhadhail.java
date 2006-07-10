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
import threaddemo.locking.PrivilegedLock;

/**
 * A phadhail in which all model methods are locked with a plain lock.
 * In this variant, the impl acquires locks automatically, though another
 * style would be to require the client to do this.
 * @author Jesse Glick
 */
final class LockedPhadhail extends AbstractPhadhail {

    private static final PrivilegedLock PLOCK = new PrivilegedLock();
    static {
        Locks.readWrite(PLOCK);
    }
    
    private static final AbstractPhadhail.Factory FACTORY = new AbstractPhadhail.Factory() {
        public AbstractPhadhail create(File f) {
            return new LockedPhadhail(f);
        }
    };
    
    public static Phadhail create(File f) {
        return forFile(f, FACTORY);
    }
    
    private LockedPhadhail(File f) {
        super(f);
    }
    
    protected Factory factory() {
        return FACTORY;
    }
    
    public List<Phadhail> getChildren() {
        PLOCK.enterRead();
        try {
            return super.getChildren();
        } finally {
            PLOCK.exitRead();
        }
    }
    
    public String getName() {
        PLOCK.enterRead();
        try {
            return super.getName();
        } finally {
            PLOCK.exitRead();
        }
    }
    
    public String getPath() {
        PLOCK.enterRead();
        try {
            return super.getPath();
        } finally {
            PLOCK.exitRead();
        }
    }
    
    public boolean hasChildren() {
        PLOCK.enterRead();
        try {
            return super.hasChildren();
        } finally {
            PLOCK.exitRead();
        }
    }
    
    public void rename(String nue) throws IOException {
        PLOCK.enterWrite();
        try {
            super.rename(nue);
        } finally {
            PLOCK.exitWrite();
        }
    }
    
    public Phadhail createContainerPhadhail(String name) throws IOException {
        PLOCK.enterWrite();
        try {
            return super.createContainerPhadhail(name);
        } finally {
            PLOCK.exitWrite();
        }
    }
    
    public Phadhail createLeafPhadhail(String name) throws IOException {
        PLOCK.enterWrite();
        try {
            return super.createLeafPhadhail(name);
        } finally {
            PLOCK.exitWrite();
        }
    }
    
    public void delete() throws IOException {
        PLOCK.enterWrite();
        try {
            super.delete();
        } finally {
            PLOCK.exitWrite();
        }
    }
    
    public InputStream getInputStream() throws IOException {
        PLOCK.enterRead();
        try {
            return super.getInputStream();
        } finally {
            PLOCK.exitRead();
        }
    }
    
    public OutputStream getOutputStream() throws IOException {
        // See comment in AbstractPhadhail re. use of read access.
        PLOCK.enterRead();
        try {
            return super.getOutputStream();
        } finally {
            PLOCK.exitRead();
        }
    }
    
    public RWLock lock() {
        return PLOCK.getLock();
    }
    
}
