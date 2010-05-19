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

package org.netbeans.api.debugger;

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

import org.openide.util.Lookup.Item;
import org.openide.util.Lookup.Result;
import org.openide.util.LookupListener;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Martin Entlicher
 */
class PathLookup extends org.openide.util.Lookup {

    private org.openide.util.Lookup delegate;
    private String path;

    PathLookup(String path) {
        this.delegate = Lookups.forPath(path);
        this.path = path;
    }

    @Override
    public <T> T lookup(Class<T> clazz) {
        Item<T> item = lookupItem(new Template<T>(clazz));
        return (item == null) ? null : item.getInstance();
    }

    @Override
    public <T> Result<T> lookup(Template<T> template) {
        return resultJustForPath(delegate.lookup(template));
    }

    @Override
    public <T> Result<T> lookupResult(Class<T> clazz) {
        return resultJustForPath(delegate.lookupResult(clazz));
    }

    private <T> Result<T> resultJustForPath(Result<T> result) {
        int l = path.length() + 1;
        int count = 0;
        Collection<? extends Item<T>> allItems = result.allItems();
        for (Item<T> it : allItems) {
            String filePath = it.getId();
            assert filePath.startsWith(path) : "File path '"+filePath+"' does not start with searched path '"+path+"'";
            if (filePath.indexOf('/', l) > 0) {
                // This and further items are from a different folder
                // We have to return a restricted result
                break;
            }
            count++;
        }
        if (count < allItems.size()) {
            return new PathLookupResult<T>(result, count);
        }
        return result;
    }

    static class PathLookupResult<T> extends Result<T> {

        private Result<T> orig;
        private int n;

        PathLookupResult(Result<T> orig, int n) {
            this.orig = orig;
            this.n = n;
        }

        @Override
        public void addLookupListener(LookupListener l) {
            orig.addLookupListener(l);
        }

        @Override
        public void removeLookupListener(LookupListener l) {
            orig.removeLookupListener(l);
        }

        @Override
        public Collection<? extends T> allInstances() {
            return new PathLookupCollection(orig.allInstances(), n);
        }

        @Override
        public Set<Class<? extends T>> allClasses() {
            return new PathLookupSet(orig.allClasses(), n);
        }

        @Override
        public Collection<? extends Item<T>> allItems() {
            return new PathLookupCollection(orig.allItems(), n);
        }

        private static class PathLookupCollection<IT> implements Collection<IT> {

            private Collection<IT> delegate;
            private int n;

            PathLookupCollection(Collection<IT> delegate, int n) {
                this.delegate = delegate;
                this.n = n;
            }

            public int size() {
                return n;
            }

            public boolean isEmpty() {
                return n == 0;
            }

            public boolean contains(Object o) {
                Iterator it = iterator();
                for (int i = 0; i < n; i++) {
                    Object e = it.next();
                    if (o == e || o != null && o.equals(e)) {
                        return true;
                    }
                }
                return false;
            }

            public Iterator<IT> iterator() {
                return new PathLookupIterator(delegate.iterator(), n);
            }

            public Object[] toArray() {
                Object[] arr = new Object[n];
                Iterator it = iterator();
                for (int i = 0; i < n; i++) {
                    arr[i] = it.next();
                }
                return arr;
            }

            public <T> T[] toArray(T[] a) {
                if (a.length < n) {
                    a = (T[]) java.lang.reflect.Array.
                        newInstance(a.getClass().getComponentType(), n);
                }
                System.arraycopy(toArray(), 0, a, 0, n);
                if (a.length > n) {
                    a[n] = null;
                }
                return a;
            }

            public boolean add(IT o) {
                throw new UnsupportedOperationException("Not supported.");
            }

            public boolean remove(Object o) {
                throw new UnsupportedOperationException("Not supported.");
            }

            public boolean containsAll(Collection<?> c) {
                for (Iterator it = c.iterator(); it.hasNext(); ) {
                    if (!contains(it.next())) {
                        return false;
                    }
                }
                return true;
            }

            public boolean addAll(Collection<? extends IT> c) {
                throw new UnsupportedOperationException("Not supported.");
            }

            public boolean removeAll(Collection<?> c) {
                throw new UnsupportedOperationException("Not supported.");
            }

            public boolean retainAll(Collection<?> c) {
                throw new UnsupportedOperationException("Not supported.");
            }

            public void clear() {
                delegate.clear();
            }

        }

        private static class PathLookupSet<IT> extends PathLookupCollection<IT> implements Set<IT> {

            PathLookupSet(Set<IT> delegate, int n) {
                super(delegate, n);
            }

        }


        private static class PathLookupIterator<IIT> implements Iterator<IIT> {

            private int i;
            private Iterator<IIT> delegate;
            private int n;

            PathLookupIterator (Iterator<IIT> delegate, int n) {
                this.delegate = delegate;
                this.n = n;
                this.i = 0;
            }

            public boolean hasNext() {
                return i < n;
            }

            public IIT next() {
                if (i < n) {
                    i++;
                    return delegate.next();
                } else {
                    throw new NoSuchElementException("Index = "+i+", size = "+n);
                }
            }

            public void remove() {
                throw new UnsupportedOperationException("Not supported.");
            }

        }

    }

}
