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
 * A phadhail in which all model methods are locked with a plain mutex.
 * In this variant, the impl acquires locks automatically, though another
 * style would be to require the client to do this.
 * @author Jesse Glick
 */
final class LockedPhadhail extends AbstractPhadhail {
    
    private static final Mutex.Privileged PMUTEX = new Mutex.Privileged();
    static {
        new Mutex("LP", PMUTEX, 0);
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
        PMUTEX.enterReadAccess();
        try {
            return super.getChildren();
        } finally {
            PMUTEX.exitReadAccess();
        }
    }
    
    public String getName() {
        PMUTEX.enterReadAccess();
        try {
            return super.getName();
        } finally {
            PMUTEX.exitReadAccess();
        }
    }
    
    public String getPath() {
        PMUTEX.enterReadAccess();
        try {
            return super.getPath();
        } finally {
            PMUTEX.exitReadAccess();
        }
    }
    
    public boolean hasChildren() {
        PMUTEX.enterReadAccess();
        try {
            return super.hasChildren();
        } finally {
            PMUTEX.exitReadAccess();
        }
    }
    
    public void rename(String nue) throws IOException {
        PMUTEX.enterWriteAccess();
        try {
            super.rename(nue);
        } finally {
            PMUTEX.exitWriteAccess();
        }
    }
    
    public Phadhail createContainerPhadhail(String name) throws IOException {
        PMUTEX.enterWriteAccess();
        try {
            return super.createContainerPhadhail(name);
        } finally {
            PMUTEX.exitWriteAccess();
        }
    }
    
    public Phadhail createLeafPhadhail(String name) throws IOException {
        PMUTEX.enterWriteAccess();
        try {
            return super.createLeafPhadhail(name);
        } finally {
            PMUTEX.exitWriteAccess();
        }
    }
    
    public void delete() throws IOException {
        PMUTEX.enterWriteAccess();
        try {
            super.delete();
        } finally {
            PMUTEX.exitWriteAccess();
        }
    }
    
    public InputStream getInputStream() throws IOException {
        PMUTEX.enterReadAccess();
        try {
            return super.getInputStream();
        } finally {
            PMUTEX.exitReadAccess();
        }
    }
    
    public OutputStream getOutputStream() throws IOException {
        // See comment in AbstractPhadhail re. use of read access.
        PMUTEX.enterReadAccess();
        try {
            return super.getOutputStream();
        } finally {
            PMUTEX.exitReadAccess();
        }
    }
    
    public Mutex mutex() {
        return PMUTEX.getMutex();
    }
    
}
