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

package org.netbeans.modules.cnd.modelimpl.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import org.netbeans.modules.cnd.modelimpl.trace.TraceUtils;

/**
 *
 */
public final class MapHierarchy<K, V> {
    
    private final LinkedList<Map<K, V>> maps = new LinkedList<>();

    public MapHierarchy() {
    }
    
    public MapHierarchy(Map<K, V> map) {
        push(map);
    }    
    
    public MapHierarchy(MapHierarchy<K, V> mapHierarchy) {
        for (Map<K, V> map : mapHierarchy.maps) {
            push(map);
        }
    }        
    
    public void push(Map<K, V> map) {
        maps.addLast(map);
    }
    
    public Map<K, V> pop() {
        return maps.removeLast();
    }
    
    public Map<K, V> peek() {
        return maps.peekLast();
    }
    
    public List<Map<K, V>> getMaps() {
        return getMaps(new AcceptingFilter());
    }
    
    public List<Map<K, V>> getMaps(Filter<Map<K, V>> filter) {
        List<Map<K, V>> result = new ArrayList<>();
        for (Map<K, V> map : maps) {
            if (filter.accept(map)) {
                result.add(map);
            }
        }
        return result;
    }
    
    public Map<K, V> getPlainMap() {
        Map<K, V> result = new HashMap<>();
        for (Map<K, V> map : maps) {
            result.putAll(map);
        }
        return result;
    }
    
    public boolean containsKey(K key) {
        ListIterator<Map<K, V>> stackIter = maps.listIterator(maps.size());
        while (stackIter.hasPrevious()) {
            Map<K, V> map = stackIter.previous();
            if (map.containsKey(key)) {
                return true;
            }
        }
        return false;
    }    
    
    public V get(K key) {
        ListIterator<Map<K, V>> stackIter = maps.listIterator(maps.size());
        while (stackIter.hasPrevious()) {
            Map<K, V> map = stackIter.previous();
            if (map.containsKey(key)) {
                return map.get(key);
            }
        }
        return null;
    }
    
    public int size() {
        int result = 0;
        for (Map<K, V> map : maps) {
            result += map.size();
        }
        return result;
    }
    
    public boolean isEmpty() {
        return size() == 0;
    }
    
    public Iterable<Map.Entry<K, V>> entries() {
        return new Iterable<Map.Entry<K, V>>() {

            @Override
            public Iterator<Map.Entry<K, V>> iterator() {
                return new EntrySetIterator<>(maps);
            }
            
        };        
    }
    
    public Iterable<K> keys() {
        return new Iterable<K>() {

            @Override
            public Iterator<K> iterator() {
                return new KeySetIterator<>(maps);
            }
            
        };
    }
    
    public Iterable<V> values() {
        return new Iterable<V>() {

            @Override
            public Iterator<V> iterator() {
                return new ValuesIterator<>(maps);
            }
            
        };        
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();
        traceMaps(out, 0);
        return out.toString();
    }
    
    private void traceMaps(StringBuilder out, int from) {
        if (from == maps.size()) {
            return;
        }
        TraceUtils.repeat(out, from * 2, ' '); // NOI18N
        out.append("MAPPING:\n"); // NOI18N
        traceMaps(out, from + 1);
        out.append(TraceUtils.traceMap(maps.get(from), from * 2));
        TraceUtils.repeat(out, from * 2, ' '); // NOI18N
        out.append("END OF MAPPING\n"); // NOI18N
    }
    
    /*
    ****************************************************************************
    *   Iterators
    ****************************************************************************
    */
  
    private abstract static class BaseMapIterator<K, V, I> implements Iterator<I> {
        
        private final LinkedList<Map<K, V>> maps;
        
        private final ListIterator<Map<K, V>> stackIter;
        
        private Iterator<I> mapIter;
        
        private I nextElem;
        
        
        public BaseMapIterator(LinkedList<Map<K, V>> maps) {
            this.maps = maps;
            this.stackIter = maps.listIterator(maps.size());
            this.mapIter = stackIter.hasPrevious() ? getMapIterator(stackIter.previous()) : null;
            this.nextElem = computeNext();
        }        
        
        protected abstract Iterator<I> getMapIterator(Map<K, V> map);
        
        @Override
        public boolean hasNext() {
            return nextElem != null;
        }

        @Override
        public I next() {
            I result = nextElem;
            nextElem = computeNext();
            return result;
        }
        
        @Override
        public void remove() {
            throw new UnsupportedOperationException("Not supported."); // NOI18N
        }
        
        private I computeNext() {
            while (mapIter != null) {
                if (mapIter.hasNext()) {
                    return mapIter.next();
                } else if (stackIter.hasPrevious()) {
                    mapIter = getMapIterator(stackIter.previous());
                } else {
                    mapIter = null;
                }
            }
            return null;            
        }        
    }    
    
    private static class EntrySetIterator<K, V> extends BaseMapIterator<K, V, Map.Entry<K, V>> {

        public EntrySetIterator(LinkedList<Map<K, V>> maps) {
            super(maps);
        }

        @Override
        protected Iterator<Map.Entry<K, V>> getMapIterator(Map<K, V> map) {
            return map.entrySet().iterator();
        }        
    }
    
    private static class KeySetIterator<K, V> extends BaseMapIterator<K, V, K> {

        public KeySetIterator(LinkedList<Map<K, V>> maps) {
            super(maps);
        }

        @Override
        protected Iterator<K> getMapIterator(Map<K, V> map) {
            return map.keySet().iterator();
        }        
    }
    
    private static class ValuesIterator<K, V> extends BaseMapIterator<K, V, V> {

        public ValuesIterator(LinkedList<Map<K, V>> maps) {
            super(maps);
        }

        @Override
        protected Iterator<V> getMapIterator(Map<K, V> map) {
            return map.values().iterator();
        }        
    }  
    
    /*
    ****************************************************************************
    *   Filters
    ****************************************************************************
    */    
    public static interface Filter<T> {
        
        boolean accept(T t);
        
    }
    
    public static class AcceptingFilter<K, V> implements Filter<Map<K, V>> {

        @Override
        public boolean accept(Map<K, V> t) {
            return true;
        }
        
    }
    
    public static class NonEmptyFilter<K, V> implements Filter<Map<K, V>> {

        @Override
        public boolean accept(Map t) {
            return t != null && !t.isEmpty();
        }
        
    }
}
