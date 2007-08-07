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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.java.editor.semantic;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Set;

/**
 *
 * @author Jan Lahoda
 */
public enum ColoringAttributes {

    UNUSED,

    ABSTRACT,

    FIELD,
    LOCAL_VARIABLE,
    PARAMETER,
    METHOD,
    CONSTRUCTOR,
    CLASS,
    INTERFACE,
    ANNOTATION_TYPE,
    ENUM,
    DEPRECATED,
    STATIC,

    DECLARATION,
    
    PRIVATE,
    PACKAGE_PRIVATE,
    PROTECTED,
    PUBLIC,

    TYPE_PARAMETER_DECLARATION,
    TYPE_PARAMETER_USE,

    UNDEFINED,

    MARK_OCCURRENCES;
    
    public static Coloring empty() {
        return new Coloring();
    }
    
    public static Coloring add(Coloring c, ColoringAttributes a) {
        Coloring ci = new Coloring();
        
        ci.value = c.value | (1 << a.ordinal());
        
        return ci;
    }

    public static final class Coloring implements Collection<ColoringAttributes> {

        private int value;
        
        private Coloring() {}
        
        public int size() {
            return Integer.bitCount(value);
        }

        public boolean isEmpty() {
            return value == 0;
        }

        public boolean contains(Object o) {
            if (o instanceof ColoringAttributes) {
                return (value & (1 << ((ColoringAttributes) o).ordinal())) !=0;
            } else {
                return false;
            }
        }

        public Iterator<ColoringAttributes> iterator() {
            Set<ColoringAttributes> s = EnumSet.noneOf(ColoringAttributes.class);
            for (ColoringAttributes c : ColoringAttributes.values()) {
                if (contains(c))
                    s.add(c);
            }
            
            return s.iterator();
        }

        public Object[] toArray() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public <T> T[] toArray(T[] a) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public boolean add(ColoringAttributes o) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public boolean remove(Object o) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public boolean containsAll(Collection<?> c) {
            for (Object o : c) {
                if (!contains(o))
                    return false;
            }
            
            return true;
        }

        public boolean addAll(Collection<? extends ColoringAttributes> c) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public boolean removeAll(Collection<?> c) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public boolean retainAll(Collection<?> c) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void clear() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public int hashCode() {
            return value;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Coloring) {
                //XXX:
                return ((Coloring) obj).value == value;
            }
            
            return false;
        }
        
        
    }
}
