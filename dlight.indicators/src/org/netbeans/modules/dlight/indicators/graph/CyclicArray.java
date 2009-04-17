/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.dlight.indicators.graph;

import java.lang.reflect.Array;

/**
 * Represents a cyclic array that stores a limited amount of T elements,
 * so that when a new element is added and a threashold (capasity)
 * is reached, the first element is dropped
 *
 * @author Vladimir Kvashin
 */
class CyclicArray<T> {

    /** array that stores elements */
    private T[] data;

    /** last element index */
    private int last;

    /** elements count */
    private int size;

    public CyclicArray() {
        this(10);
    }

    @SuppressWarnings("unchecked") // otherwise can't create T[] array
    public CyclicArray(int initialCapacity) {
        if (initialCapacity < 2) {
            throw new IllegalArgumentException("Invalid initial capacity " + initialCapacity); //NOI18N
        }
        data = (T[]) new Object[initialCapacity];
        last = -1;
        size = 0;
    }

    /**
     * Adds a new element to the array
     * @return true if the array was scrolled, otherwise false
     */
    public boolean add(T value) {
        if (last < data.length - 1) {
            data[++last] = value;
            if (size < data.length) {
                size++;
                return false;
            }            
        } else {
            data[last = 0] = value;            
        }
        return true;
    }

    /** Gets element by the given index */
    public T get(int index) {
        return data[trandformIndex(index)];
    }

    /** Sets element by the given index */
    public void set(int index, T value) {
        data[trandformIndex(index)] = value;
    }

    private final int trandformIndex(int index) {
        if (index < 0 || index >= size) {
            throw new IllegalArgumentException("Invalid index " + index + "; size: " + size); //NOI18N
        }
        if (size < data.length) {
            return index;
        } else {
            int first = last + 1;
            if (first + index < data.length) {
                return first + index;
            } else {
                return first + index - data.length;
            }
        }
    }

    /** gets element count */
    public int size() {
        return size;
    }


    /** returns the capacity of this cyclic array */
    public int capacity() {
        return data.length;
    }

    /** like realloc :) */
    public void setCapacity(int newCapacity) {
        @SuppressWarnings("unchecked") // otherwise can't create T[] array
        T[] newData = (T[]) new Object[newCapacity];
        int oldIndex = (newCapacity < size) ? size - newCapacity : 0;
        int newIndex = 0;
        while(oldIndex < size()) {
            newData[newIndex++] = get(oldIndex++);
        }

        data = newData;
        last = (size < data.length) ? size-1 : data.length-1;
        if (size > data.length) {
            size = data.length;
        }
    }

    /** like realloc :) */
    public void ensureCapacity(int capacity) {
        if (capacity > data.length) {
            setCapacity(capacity);
        }
    }

    public static final <T> boolean  areEqual(CyclicArray<T> a1, CyclicArray<T> a2) {
        if (a1 == null) {
            return a2 == null;
        } else if (a2 == null) {
            return a1 == null;
        } else {
            if (a1.size() != a2.size()) {
                return false;
            }
            for (int i = 0; i < a1.size(); i++) {
                T v1 = a1.get(i);
                T v2 = a2.get(i);
                if (v1 == null && v2 != null) {
                    return false;
                } else if(!v1.equals(v2)) {
                    return false;
                }
            }
            return true;
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("CyclicArray capacity=%d size=%d last=%d data=[", //NOI18N
                data.length, size, last));
        for (int i = 0; i < data.length; i++) {
            if (i > 0) {
                sb.append(", "); //NOI18N
            }
            sb.append(toString(data[i]));
        }
        sb.append(']'); //NOI18N
        return sb.toString();
    }

    private String toString(Object v) {
        if (v == null) {
            return "null"; // NOI18N
        } else if(v.getClass().isArray()) {
            int length = Array.getLength(v);
            StringBuilder sb = new StringBuilder("["); // NOI18N
            for (int i = 0; i < length; i++) {
                if (i > 0) {
                    sb.append(','); // NOI18N
                }
                sb.append(toString(Array.get(v, i)));
            }
            sb.append(']'); // NOI18N
            return sb.toString();
        } else {
            return v.toString();
        }
    }
    
}
