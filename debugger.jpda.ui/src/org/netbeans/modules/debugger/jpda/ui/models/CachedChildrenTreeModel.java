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

package org.netbeans.modules.debugger.jpda.ui.models;

import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;

/**
 * A TreeModel, which caches children objects and allow seamless update of children objects.
 * 
 * @author Martin Entlicher
 */
public abstract class CachedChildrenTreeModel extends Object implements TreeModel {

    private Map<Object, ChildrenTree> childrenCache = new WeakHashMap<Object, ChildrenTree>();
    
    public final Object[] getChildren (Object o, int from, int to)
    throws UnknownTypeException {
        Object[] ch;
        boolean cache = cacheChildrenOf(o);
        if (cache) {
            synchronized (childrenCache) {
                //ch = (List) childrenCache.get(o);
                ChildrenTree cht = childrenCache.get(o);
                if (cht != null) {
                    ch = cht.getChildren();
                } else {
                    ch = null;
                }
            }
        } else ch = null;
        if (ch == null) {
            ch = computeChildren(o);
            if (ch == null) {
                throw new UnknownTypeException (o);
            } else {
                if (cache) {
                    synchronized (childrenCache) {
                        ChildrenTree cht = new ChildrenTree(o);
                        cht.setChildren(ch);
                        childrenCache.put(o, cht);
                    }
                }
            }
        }
        ch = reorder(ch);
        int l = ch.length;
        from = Math.min(l, from);
        to = Math.min(l, to);
        if (from == 0 && to == l) {
            return ch;
        } else {
            Object[] ch1 = new Object[to - from];
            System.arraycopy(ch, from, ch1, 0, to - from);
            ch = ch1;
        }
        return ch;
    }
    
    protected abstract Object[] computeChildren(Object node) throws UnknownTypeException;
    
    protected boolean cacheChildrenOf(Object node) {
        return true;
    }
    
    protected Object[] reorder(Object[] nodes) {
        return nodes;
    }
    
    protected final void recomputeChildren() throws UnknownTypeException {
        synchronized (childrenCache) {
            recomputeChildren(getRoot());
        }
    }
    
    private void recomputeChildren(Object node) throws UnknownTypeException {
        ChildrenTree cht = childrenCache.get(node);
        if (cht != null) {
            Set keys = childrenCache.keySet();
            Object[] oldCh = cht.getChildren();
            Object[] newCh = computeChildren(node);
            cht.setChildren(newCh);
            for (int i = 0; i < newCh.length; i++) {
                if (keys.contains(newCh[i])) {
                    recomputeChildren(newCh[i]);
                }
            }
        }
    }
    
    private final static class ChildrenTree {
        
        private Object node;
        private Object[] ch;
        
        public ChildrenTree(Object node) {
            this.node = node;
        }
        
        public void setChildren(Object[] ch) {
            this.ch = ch;
        }
        
        public Object[] getChildren() {
            return ch;
        }
        
    }
    
}
