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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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


package org.netbeans.modules.uml.project.ui.nodes;

import java.beans.PropertyChangeEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;

import org.openide.nodes.Node;
import org.openide.nodes.NodeEvent;
import org.openide.nodes.NodeListener;
import org.openide.nodes.NodeMemberEvent;
import org.openide.nodes.NodeReorderEvent;

import org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeItem;
import org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeDiagram;



public class UMLChildrenNodesCollection implements Collection<Node>, NodeListener
{
    
    TreeMap<Key, ArrayList<Node>> map = new TreeMap<Key, ArrayList<Node>>();
    Hashtable<Node, Key> hash = new Hashtable<Node, Key>();

    static int cnt = 0;
    int ind = cnt++;

    public int size() 
    {
        return hash.size();        
    }

    public boolean isEmpty() 
    {
        return hash.isEmpty();
    }

    public boolean contains(Object o) 
    {        
        Node n = null;
        if (o instanceof Node) 
        {
            n = (Node)o;
        } 
        else 
        {
            return false;
        }
        Key k = hash.get(n);
        if (k != null) 
        {
            ArrayList<Node> list = map.get(k);
            if (list != null) 
            {
                if (list.contains(n)) 
                {
                    return true;
                }
            }
        }
        Key k2 = calculateKey(n);
        if (! k2.equals(k)) 
        {
            ArrayList<Node> list = map.get(k2);
            if (list != null) 
            {
                return list.contains(n);
            }
        }
        return false;
    }

    public Iterator<Node> iterator() 
    {
        ArrayList<Node> list = asList();
        return list.iterator(); 
    }

    public <T> T[] toArray(T[] a) 
    {
        ArrayList<Node> list = asList();
        return list.toArray(a);
    }

    public Object[] toArray() 
    {
        ArrayList<Node> list = asList();
        return list.toArray();
    }

    public boolean add(Node n)
    {   
        boolean added = false;
        if (! hash.containsKey(n)) 
        {
            Key k = calculateKey(n);
            addToMap(n, k);
            hash.put(n, k);
            n.addNodeListener(this);
            added = true;
        }
        return added;
    }

    void addToMap(Node n, Key k) 
    {
        ArrayList<Node> list = map.get(k);
        if (list == null) 
        {
            list = new ArrayList<Node>(1);
            map.put(k, list);
        }
        list.add(n);
    }

    public boolean remove(Object o) 
    {
        Node n = null;
        if (o instanceof Node) 
        {
            n = (Node)o;
        } 
        else 
        {
            return false;
        }
        boolean removed = false;

        Key k = hash.get(n);
        removed = removeFromMap(n, k);
        Key k2 = calculateKey(n);
        if (! k2.equals(k)) 
        {
            removed |= removeFromMap(n, k2);
        }

        hash.remove(n);
        n.removeNodeListener(this);
        
        return removed;
    }

    boolean removeFromMap(Node n, Key k) 
    {
        boolean removed = false;
        if (k != null) 
        {
            ArrayList<Node> list = map.get(k);
            if (list != null) 
            {
                removed = list.remove(n);
                if (list.size() == 0) 
                {
                    map.remove(k);
                }
            }
        }
        return removed;
    }

    public boolean addAll(Collection<? extends Node> c) 
    {
        boolean changed = false;
        for(Node n : c) 
        {
            changed |= add(n);
        }
        return changed;
    }

    public boolean removeAll(Collection<?> c) 
    {
        boolean changed = false;
        for(Object n : c) 
        {
            changed |= remove(n);
        }
        return changed;       
    }
    
    public boolean containsAll(Collection<?> c)
    {
        boolean contains = true;
        for(Object n : c) 
        {
            contains &= contains(n);
        }
        return contains;        
    }

    public boolean retainAll(Collection<?> c) 
    {
        ArrayList<Node> list = asList();
        boolean changed = false;
        for(Node n : list) 
        {
            if (! c.contains(n)) 
            {
               changed |= remove(n);
            } 
        }
        return changed;        
    }

    public void clear() 
    {
        hash.clear();
        map.clear();
    }

    @Override
    public boolean equals(Object o) 
    {
        return (o == null ? false : hashCode() == o.hashCode());
    }
    
    @Override
    public int hashCode() 
    {
        int h = (hash == null ? 0 : hash.hashCode());
        int m = (map == null ? 0 : map.hashCode());
        return h ^ m;       
    }

    
    void dumpMap() 
    {
        ArrayList<Node> res = new ArrayList<Node>(hash.size());
        Set<Key> ks = map.keySet();
        for(Key k : ks) 
        {
            System.out.println("  key: "+k.name);            
            ArrayList<Node> list = map.get(k);            
            for(Node n : list) 
            {
                System.out.println("    n: "+n);                           
            }
        }
    }

    ArrayList<Node> asList() 
    {
        ArrayList<Node> res = new ArrayList<Node>(hash.size());
        Collection<ArrayList<Node>> c = map.values();
        for(ArrayList<Node> list : c) 
        {
            for(Node n : list) 
            {
                res.add(n);
            }
        }
        return res;
    }

    Key calculateKey(Node n) 
    {
        if(n instanceof ITreeItem)
        { 
            ITreeItem item = (ITreeItem)n;
            
            long priority = item.getSortPriority();
            String name = "";
            if (n instanceof ITreeDiagram) 
            {
                ITreeDiagram diagram = (ITreeDiagram)n;
                name = diagram.getData().getDescription();
            } 
            else 
            {
                name = item.getDisplayedName().toLowerCase();
            }
            return new Key(priority, name);
        }
        return new Key(0, "");
    }

    class Key implements Comparable<Key>
    {
        long priority;
        String name;
        
        Key(long priority, String name) 
        {
            this.priority = priority;
            this.name = name;
        }

        public int compareTo(Key other) 
        {
            if (this.priority > other.priority) 
            {
                return 1; 
            } 
            else if (this.priority < other.priority)
            {
                return -1;                
            }
            else 
            {
                if (name != null) 
                {
                    return name.compareTo(other.name);
                }
                else 
                {
                    return -1;
                }
            }
        }

        @Override
        public boolean equals(Object o) 
        {
            if (! (o instanceof Key)) 
            {
                return false;
            }
            Key other = (Key)o;
            if (this.name != null && this.name.equals(other.name)
                && this.priority == other.priority) 
            {
                return true;
            }
            return false;
        }
   
    }

    public void propertyChange (PropertyChangeEvent ev) 
    {
        Object src = ev.getSource();
        if (src instanceof Node) 
        {
            Node n = (Node) src;
            String propName = ev.getPropertyName();
            if (propName != null) 
            {
                if (propName.equals(Node.PROP_DISPLAY_NAME ) 
                    || propName.equals(Node.PROP_NAME ))
                { 
                    removeFromMap(n, hash.get(n));
                    addToMap(n, calculateKey(n));
                    hash.put(n, calculateKey(n));
                }
            }
        }
    }
    
    public void childrenAdded(NodeMemberEvent ev) {}

    public void childrenRemoved(NodeMemberEvent ev) {}

    public void childrenReordered(NodeReorderEvent ev) {}

    public void nodeDestroyed(NodeEvent ev) {}

}
