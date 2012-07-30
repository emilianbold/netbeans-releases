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

import org.codeviation.commons.patterns.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

/** Convenience methods for working with collections.
 *
 * @author Petr Hrebejk
 */
public class CollectionsUtil {
        
    private CollectionsUtil() {}
    
    public static <T> HashSet<T> hashSet(T... params) {
        HashSet<T> s = new HashSet<T>(params.length);
        for (T p : params) {
            s.add(p);
        }
        return s;
    }
    
    public static <T> HashSet<T> hashSet(Collection<? extends T> original,  Filter<T> filter) {
        HashSet<T> s = new HashSet<T>(original.size());
        for (T p : original) {
            if ( filter.accept(p)) {
                s.add(p);
            }
        }
        return s;
    }
    
    public static <T,Q extends T> ArrayList<T> arrayList(Q... params) {
        ArrayList<T> s = new ArrayList<T>(params.length);
        for (T p : params) {
            s.add(p);
        }
        return s;
    }
    
    public static <T,Q extends T> ArrayList<T> arrayList(Collection<Q> original,  Filter<T> filter) {
        ArrayList<T> s = new ArrayList<T>(original.size());
        for (T p : original) {
            if ( filter.accept(p)) {
                s.add(p);
            }
        }
        return s;
    }
    
    
    public static <T, C extends Collection<T>, Q extends T> C add(C target, Q... params) {
        for (T p : params) {
            target.add(p);
        }
        
        return target;
    }
    
    public static <T, C extends Collection<T>, Q extends T> C add(C target, Iterator<Q> it) {
        while (it.hasNext()) {
            target.add(it.next());
        }
        
        return target;
    }

    public static <T, C extends Collection<T>, Q extends T> C add(C target, Iterable<Q> it) {
        return add(target, it.iterator());
    }
    
    public static <T, C extends Collection<T>, Q extends Collection<? extends T>> C add(C target, Q source, Filter<? super T> filter) {
        for (T t : source) {
            if (filter.accept(t)) {
                target.add(t);
            }
        }

        return target;
    }
        
    public static <T, Q extends T> void remove(Collection<T> target, Q... params) {
        for (T p : params) {
            target.remove(p);
        }
    }
    
    public static <T> void remove(Collection<T> target, Iterator<? extends T> it) {
        while (it.hasNext()) {
            target.remove(it.next());
        }
    }
    
    public static <T, Q extends T> Collection<Q> remove(Collection<Q> target, Filter<? super T> filter) {
        for (T t : target) {
            if (filter.accept(t)) {
                target.remove(t);
            }
        }

        return target;
    }

    
    public static <T, Q extends T> Collection<T> filter(Collection<T> target, Iterable<Q> it, Filter<Q> filter) {
        
        for (Q t : it) {
            if ( filter.accept(t)) {
                target.add(t);
            }
        }

        return target;
    }
        
}
