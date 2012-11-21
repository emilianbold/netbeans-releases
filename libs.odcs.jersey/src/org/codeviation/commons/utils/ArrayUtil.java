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
package org.codeviation.commons.utils;

import java.util.Arrays;
import java.util.Iterator;

/**
 *
 * @author phrebejk
 */
public class ArrayUtil {
    
    private ArrayUtil() {}
    
    public static <T> T[] copy(T[] array) {     
        T[] a = newArray(array, array.length);
        System.arraycopy(array, 0, a, 0, array.length);
        return a;
    }
    
    public static <T> T[] union(T[] array, T... toAdd) {
        T[] a = newArray(array, array.length + toAdd.length);
        System.arraycopy(array, 0, a, 0, array.length);
        System.arraycopy(toAdd, 0, a, array.length, toAdd.length);        
        return a;
    }

    public static <T> boolean contains( T[] array, T... contained) {
        
        for (T t : contained) {
            boolean c = false;
            for (T a : array) {
                if ( a.equals(t) ) {
                    c = true;
                    break;
                }
            }
            if ( !c ) {
                return false;
            }

        }
            
        return true;    
    }
    
    public static <T> Iterator<T> iterator(T[] array) {
        return new ArrayIterator<T>(array);
    }
    
 
    // Private methods ---------------------------------------------------------
    
    private static <T> T[] newArray(T[] original, int lenght) {
        
        @SuppressWarnings("unchecked")
        T[] a = (T[])java.lang.reflect.Array.
		newInstance(original.getClass().getComponentType(), lenght);        
        return a;        
        
    }
    
    private static class ArrayIterator<T> implements Iterator<T> {

        private T[] array;
        private int i = 0;

        public ArrayIterator(T[] array) {
            this.array = array;
        }
        
        public boolean hasNext() {
            return array.length > i;
        }

        public T next() {
            return array[i++];
        }

        public void remove() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
    }
    

}
