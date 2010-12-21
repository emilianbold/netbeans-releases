/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.java.source.usages;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.java.source.classpath.AptCacheForSourceQuery;
import org.netbeans.modules.java.source.indexing.JavaIndex;
import org.netbeans.modules.parsing.lucene.support.IndexManager;
import org.netbeans.modules.parsing.lucene.support.IndexManager.Action;
import org.openide.util.Exceptions;

/**
 *
 * @author Tomas Zezula
 */
public final class ClassIndexManager {

    public static final String PROP_SOURCE_ROOT = "source";  //NOI18N
    
    private static final byte OP_ADD    = 1;
    private static final byte OP_REMOVE = 2;

    private static ClassIndexManager instance;
    private final Map<URL, ClassIndexImpl> instances = new HashMap<URL, ClassIndexImpl> ();
    private final Map<URL, ClassIndexImpl> transientInstances = new HashMap<URL, ClassIndexImpl> ();
    private final InternalLock internalLock;
    private final Map<ClassIndexManagerListener,Void> listeners = Collections.synchronizedMap(new IdentityHashMap<ClassIndexManagerListener, Void>());
    private boolean invalid;
    private Set<URL> added;
    private Set<URL> removed;
    private int depth = 0;

    private ClassIndexManager() {
        this.internalLock = new InternalLock();
    }

    public void addClassIndexManagerListener (final ClassIndexManagerListener listener) {
        assert listener != null;
        this.listeners.put(listener,null);
    }

    public void removeClassIndexManagerListener (final ClassIndexManagerListener listener) {
        assert listener != null;
        this.listeners.remove(listener);
    }

    @Deprecated
    public <T> T writeLock (final Action<T> r) throws IOException, InterruptedException {
        //Ugly, in scala much more cleaner.
        return prepareWriteLock(
            new Action<T>() {
                @Override
                public T run() throws IOException, InterruptedException {
                    return IndexManager.writeAccess(r);
                }
            });
    }

    public <T> T prepareWriteLock(final Action<T> r) throws IOException, InterruptedException {
        synchronized (internalLock) {
            depth++;
            if (depth == 1) {
                this.added = new HashSet<URL>();
                this.removed = new HashSet<URL>();
            }
        }
        try {
            try {
                return r.run();
            } finally {
                Set<URL> addedCp = null;
                Set<URL> removedCp = null;
                synchronized (internalLock) {
                    if (depth == 1) {
                        if (!removed.isEmpty()) {
                            removedCp = new HashSet<URL>(removed);
                            removed.clear();
                        }
                        if (!added.isEmpty()) {
                            addedCp = new HashSet<URL>(added);
                            added.clear();
                        }
                    }
                }
                if (removedCp != null) {
                    fire (removedCp, OP_REMOVE);
                }
                if (addedCp != null) {
                    fire (addedCp, OP_ADD);
                }
            }
        } finally {
            synchronized (internalLock) {
                depth--;
            }
        }
    }
          
    @CheckForNull
    public ClassIndexImpl getUsagesQuery (@NonNull final URL root, final boolean beforeCreateAllowed) {
        synchronized (internalLock) {
            assert root != null;
            if (invalid) {
                return null;
            }
            Pair<ClassIndexImpl,Boolean> pair = getClassIndex(root, beforeCreateAllowed, false);
            ClassIndexImpl index = pair.first;
            if (index != null) {
                return index;
            }
            URL translatedRoot = AptCacheForSourceQuery.getSourceFolder(root);
            if (translatedRoot != null) {
                pair = getClassIndex(translatedRoot, beforeCreateAllowed, false);
                index = pair.first;
                if (index != null) {
                    return index;
                }
            } else {
                translatedRoot = root;
            }
            if (beforeCreateAllowed) {
                String attr = null;
                try {
                    attr = JavaIndex.getAttribute(translatedRoot, PROP_SOURCE_ROOT, null);            
                    if (Boolean.TRUE.toString().equals(attr)) {
                        index = PersistentClassIndex.create (root, JavaIndex.getIndex(root), true);
                        this.transientInstances.put(root,index);
                    } else if (Boolean.FALSE.toString().equals(attr)) {
                        index = PersistentClassIndex.create (root, JavaIndex.getIndex(root), false);
                        this.transientInstances.put(root,index);
                    }
                } catch(IOException ioe) {/*Handled bellow by return null*/
                } catch(IllegalStateException ise) {
                  /* Required by some wrongly written tests
                   * which access ClassIndex without setting the cache dir
                   * Handled bellow by return null
                   */
                }
            }
            return index;
        }
    }

    public ClassIndexImpl createUsagesQuery (final URL root, final boolean source) throws IOException {
        assert root != null;
        synchronized (internalLock) {
            if (invalid) {
                return null;
            }
            Pair<ClassIndexImpl,Boolean> pair = getClassIndex (root, true, true);
            ClassIndexImpl qi = pair.first;
            if (qi == null) {
                qi = PersistentClassIndex.create (root, JavaIndex.getIndex(root), source);
                this.instances.put(root,qi);
                if (added != null) {
                    added.add (root);
                }
            } else if (source && !qi.isSource()){
                //Wrongly set up freeform project, which is common for it, prefer source
                qi.close ();
                qi = PersistentClassIndex.create (root, JavaIndex.getIndex(root), source);
                this.instances.put(root,qi);
                if (added != null) {
                    added.add (root);
                }
            } else if (pair.second) {
                if (added != null) {
                    added.add (root);
                }
            }
            return qi;
        }
    }
    
    public void removeRoot (final URL root) throws IOException {
        synchronized (internalLock) {
            ClassIndexImpl ci = this.instances.remove(root);
            if (ci != null) {
                ci.close();
                if (removed != null) {
                    removed.add (root);
                }
            }
        }
    }
    
    public void close () {
        synchronized (internalLock) {
            invalid = true;
            for (ClassIndexImpl ci : instances.values()) {
                try {
                    ci.close();
                } catch (IOException ioe) {
                    Exceptions.printStackTrace(ioe);
                }
            }
        }
    }
            
    private void fire (final Set<? extends URL> roots, final byte op) {
        if (!this.listeners.isEmpty()) {
            ClassIndexManagerListener[] _listeners;
            synchronized (this.listeners) {
                _listeners = this.listeners.keySet().toArray(new ClassIndexManagerListener[this.listeners.size()]);
            }
            final ClassIndexManagerEvent event = new ClassIndexManagerEvent (this, roots);
            for (ClassIndexManagerListener listener : _listeners) {
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
    
    @NonNull
    private Pair<ClassIndexImpl,Boolean> getClassIndex(
            final URL root,
            final boolean allowTransient,
            final boolean promote) {
        ClassIndexImpl index = this.instances.get (root);
        boolean promoted = false;
        if (index == null && allowTransient) {            
            if (promote) {
                index = this.transientInstances.remove(root);
                if (index != null) {
                    this.instances.put(root, index);
                    promoted = true;
                }
            } else {
                index = this.transientInstances.get(root);
            }
        }
        return Pair.<ClassIndexImpl,Boolean>of(index,promoted);
    }
    
    
    public static synchronized ClassIndexManager getDefault () {
        if (instance == null) {
            instance = new ClassIndexManager ();            
        }
        return instance;
    }

    private static final class InternalLock {
        
        private InternalLock(){}
    }
}
