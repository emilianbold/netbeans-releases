
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

package org.netbeans.lib.collab.util;

import java.util.*;

/**
 * This is PriorityQueue for JDK1.4. It offers
 * the same interface as jDK1.5 PriorityQueue.
 * It offers log(n) for offer and poll.
 *
 * @author Vijayakumar Palaniappan
 */
public class PriorityQueue {
    private ArrayList _list = null;
    private Comparator _comparator = null;
    public static void main(String s[]) {
        PriorityQueue pq = new PriorityQueue();
        Random r = new Random();
        for(int i = 10; i > 0; i--) {
            pq.offer(new Integer(r.nextInt(1000)));
        }
                
        for(int i = 0; i < 11; i++) {
            System.out.println(pq.poll());
        }
    }
        
    public PriorityQueue() {
        this(null);
    }
        
    public PriorityQueue(Comparator comp) {
        _list = new ArrayList();
        _comparator = comp;
    }
    public boolean offer(Object value) {
        if(_comparator == null) {
            if(!(value instanceof Comparable)) {
                throw new IllegalArgumentException(
                                                   "value should implement Comparable interface");
            }
        }
        _list.add(value);
        percolateUp(_list.size() - 1);
        return true;
    }
        
    public Object poll() {
        Object value = peek();
        if(value != null) {
            int size = _list.size();
            _list.set(0,_list.get(size - 1));
            _list.remove(size - 1);
            if(_list.size() > 1) {
                pushDownRoot(0);
            }
        }
        return value;
                
    }
        
    public Object peek() {
        if(_list.size() > 0) {
            return _list.get(0);
        } else {
            return null;
        }
    }
        
    public int size() {
        return _list.size();
    }
        
    public void clear() {
        _list.clear();
    }
        
    public Iterator iterator() {
        return _list.iterator();
    }
        
    private void pushDownRoot(int root) {
        int size = _list.size();
        Object value = _list.get(root);
        while(true) {
            int childPos = leftChildOf(root);
            if(childPos < size) {
                if(rightChildOf(root) < size &&
                   compare(_list.get(childPos + 1),_list.get(childPos)) < 0) {
                    childPos++;
                }
                        
                if(compare(_list.get(childPos),value) < 0) {
                    _list.set(root,_list.get(childPos));
                    root = childPos;
                } else {
                    _list.set(root,value);
                    return;
                }
                        
            } else {
                _list.set(root,value);
                return;
            }
        }
    }
        
    private void percolateUp(int leaf) {
        int parent = parentOf(leaf);
        Object value = _list.get(leaf);
        while(leaf > 0 && 
              compare(value,_list.get(parent)) < 0) {
            _list.set(leaf,_list.get(parent));
            leaf = parent;
            parent = parentOf(leaf);
        }
        _list.set(leaf,value);
    }
        
    private int compare(Object o1, Object o2) {
        if(_comparator == null) {
            return ((Comparable)o1).compareTo(o2);
        } else {
            return _comparator.compare(o1,o2);
        }
    }
        
    private static int parentOf(int index) {
        return (index-1)/2;
    }
        
    private static int leftChildOf(int index) {
        return 2*index+1;
    }
        
    private static int rightChildOf(int index) {
        return 2*(index+1);
    }
}
