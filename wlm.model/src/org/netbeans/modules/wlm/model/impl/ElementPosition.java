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
 * Portions Copyrighted 1997-2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.wlm.model.impl;

import java.util.Collection;
import java.util.Iterator;
import org.netbeans.modules.wlm.model.api.WLMComponent;

/**
 *
 * @author anjeleevich
 */
public class ElementPosition implements 
        Collection<Class<? extends WLMComponent>> 
{
    private Class<? extends WLMComponent>[] data;
    
    public ElementPosition() {
        this((ElementPosition) null, null);
    }
    
    public ElementPosition(Class<? extends WLMComponent> elementType) {
        this((ElementPosition) null, elementType);
    }
    
    public ElementPosition(Class<? extends WLMComponent> firstElementType,
            Class<? extends WLMComponent> nextElementType)
    {
        this(new ElementPosition(firstElementType), nextElementType);
    }
    
    @SuppressWarnings("unchecked")    
    public ElementPosition(ElementPosition prevElementPosition, 
            Class<? extends WLMComponent> nextElementType)
    {
        if (prevElementPosition == null) {
            if (nextElementType != null) {
                this.data = new Class[] { nextElementType };
            }
        } else {
            int size = prevElementPosition.size();
            
            if (nextElementType != null) {
                size++;
            }
            
            this.data = new Class[size];
            
            prevElementPosition.toArray(this.data);
            
            if (nextElementType != null) {
                this.data[size - 1] = nextElementType;
            }
        }
    }
    
    public int size() {
        return (data == null) ? 0 : data.length;
    }

    public boolean isEmpty() {
        return (data == null) || (data.length == 0);
    }

    public boolean contains(Object o) {
        if (data == null) {
            return false;
        }
        
        for (int i = data.length - 1; i >= 0; i--) {
            if ((o == null) && (data[i] == null)) {
                return true;
            }
            
            if ((data[i] != null) && (o != null) && o.equals(data[i])) {
                return true;
            }
        }
        
        return false;
    }

    public Iterator<Class<? extends WLMComponent>> iterator() {
        return new ElementOrderIterator();
    }

    public boolean containsAll(Collection<?> c) {
        Iterator<?> i = c.iterator();
        
        while (i.hasNext()) {
            if (!contains(i.next())) {
                return false;
            }
        }
        
        return true;
    }

    public Object[] toArray() {
        int size = size();
        
	Object[] result = new Object[size];
        if (size > 0) {
            System.arraycopy(data, 0, result, 0, size);
        }
        
	return result;
    }

    @SuppressWarnings("unchecked")    
    public <T> T[] toArray(T[] a) {
        int size = size();
         
        if (a.length < size) {
            a = (T[]) java.lang.reflect.Array.newInstance(a.getClass()
                    .getComponentType(), size);
        }
        
        if (size > 0) {
            System.arraycopy(data, 0, a, 0, size);
        }
        
        for (int i = a.length - 1; i >= size; i--) {
            a[i] = null;
        }
            
        return a;    
    }    
    
    public boolean add(Class<? extends WLMComponent> o) {
        return rejectModification();
    }

    public boolean addAll(Collection<? extends Class<? extends WLMComponent>> c) {
        return rejectModification();
    }

    public boolean remove(Object o) {
        return rejectModification();
    }
    
    public boolean removeAll(Collection<?> c) {
        return rejectModification();
    }

    public boolean retainAll(Collection<?> c) {
        return rejectModification();
    }

    public void clear() {
        rejectModification();
    }
    
    private boolean rejectModification() {
        throw new UnsupportedOperationException("Immutable collection");
    }
    
    private class ElementOrderIterator implements 
            Iterator<Class<? extends WLMComponent>> 
    {
        private int index = 0;
        
        public boolean hasNext() {
            return (data != null) && (index < data.length);
        }

        public Class<? extends WLMComponent> next() {
            return data[index++];
        }

        public void remove() {
            rejectModification();
        }
    }
    
    public static final ElementPosition EMPTY = new ElementPosition();
}
