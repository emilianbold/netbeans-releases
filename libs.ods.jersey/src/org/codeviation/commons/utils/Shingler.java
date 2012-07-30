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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author phrebejk
 */
public class Shingler<T extends Enum> {

    private int size;
    private BigInteger p;
    private BigInteger m;
    
    private BigInteger precomputed[]; 
    
    public Shingler(Class<T> clazz, int size, int p, long m) {
        this.size = size;
        this.p = BigInteger.valueOf(p);
        this.m = BigInteger.valueOf(m);
        
        Enum constants[] = clazz.getEnumConstants();
        
        this.precomputed = new BigInteger[constants.length];
        for (int i = 0; i < constants.length; i++) {
            BigInteger ev = BigInteger.valueOf( constants[i].ordinal() + 1 );
            precomputed[i] = ev.multiply(this.p.pow( size -1 ));
             
            //precomputed[i] = ev * pow(p, size - 1);
        }
        
                
    }
    
    public List<Long> compute(List<T> list) {
        
        long first = computeFirst(list);
        
        if (first == Long.MIN_VALUE) {
            return Collections.<Long>emptyList();
        }
        
        List<Long> r = new ArrayList<Long>( list.size() - size + 1);
        r.add(first);
        
        for( int i = 0; i < list.size() - size; i++) {
            long next = computeNext(first, list.get(i), list.get(i + size));
            r.add(next);
            first = next;
        }
        
        return r;
    }
    
    long computeFirst(List<T> list) {
        
        
        if( list.size() < size ) {
            return Long.MIN_VALUE;
        }
        
        
        BigInteger s = BigInteger.valueOf(list.get(0).ordinal() + 1);
        
        for( int i = 1; i < size; i++) {
            s = s.multiply(p);
            s = s.add( BigInteger.valueOf(list.get(i).ordinal() + 1));
//            s *= p;
//            s += list.get(i).ordinal() + 1;
        }
        
        
        return s.mod(m).longValue();
        
//        return s % m;
    }
    
    long computeNext(long last, T toRemove, T toAdd ) {
        
        // int a = toAdd.ordinal() + 1;
        BigInteger a = BigInteger.valueOf(toAdd.ordinal() + 1);
        
        BigInteger r = BigInteger.valueOf(last);
        r = r.subtract(getPrecomputed(toRemove)).multiply(p).add(a);
        return r.mod(m).longValue();
        
        
        //return ( p * ( last - getPrecomuted(toRemove)) + a ) % m;
    }
    
//    private long pow( long a, long b ) {
//        return (long)Math.pow(a, b) ;
//    }
//    
    private BigInteger getPrecomputed( T value ) {
        return precomputed[value.ordinal()];
    }
    
}
