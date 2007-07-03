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

package org.netbeans.modules.java.source.usages;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.openide.util.Exceptions;

/**
 *
 * @author Tomas Zezula
 */
public final class ClassIndexManager {
    
    private static final byte OP_ADD    = 1;
    private static final byte OP_REMOVE = 2;

    private static ClassIndexManager instance;
    private final Map<URL, ClassIndexImpl> instances = new HashMap<URL, ClassIndexImpl> ();
    private final ReadWriteLock lock;
    private final List<ClassIndexManagerListener> listeners = new CopyOnWriteArrayList<ClassIndexManagerListener> ();
    private boolean invalid;
    private Set<URL> added;
    private Set<URL> removed;
    private int depth = 0;
    
    
    
    private ClassIndexManager() {
        this.lock = new ReentrantReadWriteLock (false);
    }
    
    public void addClassIndexManagerListener (final ClassIndexManagerListener listener) {
        assert listener != null;
        this.listeners.add(listener);
    }
    
    public void removeClassIndexManagerListener (final ClassIndexManagerListener listener) {
        assert listener != null;
        this.listeners.remove(listener);
    }
    
    public <T> T writeLock (final ExceptionAction<T> r) throws IOException, InterruptedException {
        this.lock.writeLock().lock();
        try {
            depth++;
            try {
                if (depth == 1) {
                    this.added = new HashSet<URL>();
                    this.removed = new HashSet<URL>();
                }
                try {
                    return r.run();
                } finally {
                    if (depth == 1) {
                        if (!removed.isEmpty()) {
                            fire (removed, OP_REMOVE);
                            removed.clear();
                        }
                        if (!added.isEmpty()) {
                            fire (added, OP_ADD);
                            added.clear();
                        }                
                    }
                }
            } finally {
                depth--;
            }
        } finally {
            this.lock.writeLock().unlock();
        }
    }
    
    public <T> T readLock (final ExceptionAction<T> r) throws IOException, InterruptedException {
        this.lock.readLock().lock();
        try {
            return r.run();
        } finally {
            this.lock.readLock().unlock();
        }
    }
    
    public synchronized ClassIndexImpl getUsagesQuery (final URL root) throws IOException {
        assert root != null;
        if (invalid) {
            return null;
        }        
        return this.instances.get (root);
    }
    
    public synchronized ClassIndexImpl createUsagesQuery (final URL root, final boolean source) throws IOException {
        assert root != null;
        if (invalid) {
            return null;
        }        
        ClassIndexImpl qi = this.instances.get (root);
        if (qi == null) {  
            qi = PersistentClassIndex.create (root, Index.getDataFolder(root), source);
            this.instances.put(root,qi);
            if (added != null) {
                added.add (root);
            }
        }
        return qi;
    }
    
    synchronized void removeRoot (final URL root) throws IOException {
        ClassIndexImpl ci = this.instances.remove(root);                
        if (ci != null) {                
            ci.close();
            if (removed != null) {
                removed.add (root);
            }
        }
    }
    
    public synchronized  void close () {
        invalid = true;
        for (ClassIndexImpl ci : instances.values()) {
            try {
                ci.close();
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
        }
    }
    
    public static interface ExceptionAction<T> {
        public T run () throws IOException, InterruptedException;
    }
    
    private void fire (final Set<? extends URL> roots, final byte op) {
        if (!this.listeners.isEmpty()) {
            final ClassIndexManagerEvent event = new ClassIndexManagerEvent (this, roots);
            for (ClassIndexManagerListener listener : this.listeners) {
                if (op == OP_ADD) {
                    listener.classIndexAdded(event);
                }
                else if (op == OP_REMOVE) {
                    listener.classIndexRemoved(event);
                }
                else {
                    assert false : "Unknown op: " + op;     //NOI18N
                }
            }
        }
    }
    
    
    public static synchronized ClassIndexManager getDefault () {
        if (instance == null) {
            instance = new ClassIndexManager ();            
        }
        return instance;
    }        
}
