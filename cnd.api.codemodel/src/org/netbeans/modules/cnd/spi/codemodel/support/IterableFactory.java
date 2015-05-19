/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.spi.codemodel.support;

import java.util.Iterator;

/**
 *
 * @author akrasny
 */
public final class IterableFactory {

    public static <T> Iterable<T> create(final int size, final ElementProvider<T> elementProvider) {
        return new Iterable<T>() {
            @Override
            public Iterator<T> iterator() {
                return new Iterator<T>() {
                    private int index = 0;

                    @Override
                    public boolean hasNext() {
                        return index < size;
                    }

                    @Override
                    public T next() {
                        if (index >= size) {
                            throw new IndexOutOfBoundsException("Index: " + index); // NOI18N
                        }
                        return elementProvider.getAt(index++);
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        };
    }

    public interface ElementProvider<T> {

        public T getAt(int pos);
    }
    
    public static <K, V> Iterable<V> convert(final Iterable<? extends K> orig, final Converter<K, V> conv) {
        return new Iterable<V>() {
            @Override
            public Iterator<V> iterator() {
                return new Iterator<V>() {
                    
                    Iterator<? extends K> delegate = orig.iterator();

                    @Override
                    public boolean hasNext() {
                        return delegate.hasNext();
                    }

                    @Override
                    public V next() {
                        K next = delegate.next();
                        return conv.convert(next);
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        };
    }
    
    public interface Converter<K, V> {
        V convert(K in);
    }
    
    public static <T> Iterable<T> filter(final Iterable<T> source, final Filter<T> elementsFilter) {
        return new Iterable<T>() {
            
            @Override
            public Iterator<T> iterator() {
                return new FilteringIterator<>(source.iterator(), elementsFilter);
            }
            
        };
    }
    
    public static interface Filter<T> {        
        
        boolean accept(T element);
        
    }
    
    
    private static class FilteringIterator<T> implements Iterator<T> {
        
        private final Iterator<T> delegate;
        
        private final Filter<T> filter;
        
        private boolean hasNextElement;
        
        private T nextElement;
        

        public FilteringIterator(Iterator<T> delegate, Filter<T> filter) {
            this.delegate = delegate;
            this.filter = filter;
            computeNext();
        }

        @Override
        public boolean hasNext() {
            return hasNextElement;
        }

        @Override
        public T next() {
            T result = nextElement;
            computeNext();
            return result;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
        
        
        private void computeNext() {
            hasNextElement = false;
            
            while (delegate.hasNext()) {
                nextElement = delegate.next();
                
                if (filter.accept(nextElement)) {
                    hasNextElement = true;
                    break;
                }
            }
        }
    }
    
}
