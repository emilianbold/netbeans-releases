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
import threaddemo.locking.Lock;
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
        Locks.readWriteLock("LP", PLOCK, 0);
    }
    
    private static final Factory FACTORY = new Factory() {
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
    
    public List getChildren() {
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
    
    public Lock lock() {
        return PLOCK.getLock();
    }
    
}
