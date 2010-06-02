/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.parsing.impl.indexing;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Iterable over list of lists
 * @author Tomas Zezula
 */
//@NotThreadSafe
final class ProxyIterable<T> implements Iterable<T> {

    private final Collection<? extends Iterable<? extends T>> delegates;
    private final boolean allowDuplicates;
    private final AtomicReference<List<T>> cache;

    public ProxyIterable(Collection<? extends Iterable<? extends T>> delegates) {
        this(delegates, true, false);
    }

    public ProxyIterable(Collection<? extends Iterable<? extends T>> delegates, boolean allowDuplicates) {
        this(delegates, allowDuplicates, false);
    }

    public ProxyIterable(Collection<? extends Iterable<? extends T>> delegates, boolean allowDuplicates, boolean cacheValues) {
        assert delegates != null;
        this.delegates = delegates;
        this.allowDuplicates = allowDuplicates;
        this.cache = cacheValues ? new AtomicReference<List<T>>() : null;
    }

    public Iterator<T> iterator() {
        final List<T> _cache = cache == null ? null : cache.get();
        if (_cache != null) {
            return _cache.iterator();
        } else {
            return new ProxyIterator<T>(delegates.iterator(), allowDuplicates, cache);
        }
    }

    @Override
    public String toString() {
        return "ProxyIterable@" + Integer.toHexString(System.identityHashCode(this)) + " [" + delegates + "]"; //NOI18N
    }

    private static final class ProxyIterator<T> implements Iterator<T> {

        private final Iterator<? extends Iterable< ? extends T>> iterables;
        private final Set<T> seen;
        private final AtomicReference<List<T>> cacheRef;
        private final List<T> cache;
        private Iterator<? extends T> currentIterator;
        private T currentObject;

        public ProxyIterator(
                Iterator<? extends Iterable< ? extends T>> iterables,
                boolean allowDuplicates,
                final AtomicReference<List<T>> cacheRef) {
            assert iterables != null;
            this.iterables = iterables;
            this.seen = allowDuplicates ? null : new HashSet<T>();
            this.cacheRef = cacheRef;
            this.cache = this.cacheRef == null ? null : new LinkedList<T>();
        }

        @Override
        public boolean hasNext() {
            final boolean result = getCurrent(false) != null;
            if (!result && this.cacheRef != null) {
                this.cacheRef.compareAndSet(null, cache);
            }
            return result;
        }

        @Override
        public T next() {
            T curObj =  getCurrent(true);
            if (curObj == null) {
                throw new NoSuchElementException();
            } else if (cache != null) {
                cache.add(curObj);
            }
            return curObj;
        }

        private T getCurrent(boolean move) {
            if (currentObject != null && !move) {
                return currentObject;
            }
            
            T oldCurObj = currentObject;
            for(;;) {
                if (currentIterator != null) {
                    while(currentIterator.hasNext()) {
                        T o = currentIterator.next();
                        if (seen == null || seen.add(o)) {
                            currentObject = o;
                            return oldCurObj != null ? oldCurObj : currentObject;
                        }
                    }
                }

                if (iterables.hasNext()) {
                    currentIterator = iterables.next().iterator();
                } else {
                    currentIterator = null;
                    currentObject = null;
                    return oldCurObj;
                }
            } 
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

}
