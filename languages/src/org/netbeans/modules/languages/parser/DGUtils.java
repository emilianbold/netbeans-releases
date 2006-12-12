//*
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.languages.parser;

import java.util.*;

/**
 *
 * @author Jan Jancura
 */
public class DGUtils {

    
    public static DG append (DG dg1, DG dg2) {
        DG ndg = DG.createDG ();
        Object what = dg2.getStartNode ();
        Set where = dg1.getEnds ();
        Set nn = new HashSet ();
        nn.add (dg1.getStartNode ());
        if (where.contains (dg1.getStartNode ()))
            nn.add (what);
        merge (dg1, dg2, nn, ndg, where, what, false, true);
        ndg.setStart (nn);
        return ndg;
    }
    
    public static DG plus (DG dg) {
        DG ndg = DG.createDG ();
        Object what = dg.getStartNode ();
        Set where = dg.getEnds ();
        Set nn = new HashSet ();
        nn.add (dg.getStartNode ());
        if (where.contains (dg.getStartNode ()))
            nn.add (what);
        merge (dg, dg, nn, ndg, where, what, true, true);
        ndg.setStart (nn);
        return ndg;
    }
    
    private static void merge (
        DG dg1,
        DG dg2,
        Set nn,
        DG ndg,
        Set where,
        Object what,
        boolean setEnds1,
        boolean setEnds2
    ) {
        if (ndg.containsNode (nn)) return;
        ndg.addNode (nn);

        Map edges = new HashMap ();
        Map properties = new HashMap ();
        Iterator it = nn.iterator ();
        while (it.hasNext ()) {
            Object n = it.next ();
            DG cdg = dg1.containsNode (n) ? dg1 : dg2;
            ndg.putAllProperties (nn, cdg.getProperties (n));
            if (setEnds1 && dg1.getEnds ().contains (n))
                ndg.addEnd (nn);
            if (setEnds2 && dg2.getEnds ().contains (n))
                ndg.addEnd (nn);
            Iterator it2 = cdg.getEdges (n).iterator ();
            while (it2.hasNext ()) {
                Object edge = it2.next ();
                Set ends = (Set) edges.get (edge);
                Map props = (Map) properties.get (edge);
                if (ends == null) {
                    ends = new HashSet ();
                    props = new HashMap ();
                    edges.put (edge, ends);
                    properties.put (edge, props);
                }
                Object en = cdg.getNode (n, edge);
                ends.add (en);
                props.putAll (cdg.getProperties (n, edge));
                if (where.contains (en))
                    ends.add (what);
            }
        }
        it = nn.iterator ();
        while (it.hasNext ()) {
            Object n = it.next ();
            DG cdg = dg1.containsNode (n) ? dg1 : dg2;
            Object en = cdg.getNode (n, Pattern.STAR);
            if (en == null) continue;
            Iterator it2 = edges.keySet ().iterator ();
            while (it2.hasNext ()) {
                Object e = it2.next ();
                if (cdg.getNode (n, e) != null) continue;
                ((Set) edges.get (e)).add (en);
                ((Map) properties.get (e)).putAll (cdg.getProperties (n, e));
                if (where.contains (en))
                    ((Set) edges.get (e)).add (what);
            }
        }
        
        it = edges.keySet ().iterator ();
        while (it.hasNext ()) {
            Object edge = it.next ();
            Set en = (Set) edges.get (edge);
            merge (dg1, dg2, en, ndg, where, what, setEnds1, setEnds2);
            ndg.addEdge (nn, en, edge);
            ndg.putAllProperties (nn, edge, (Map) properties.get (edge));
        }
    }
    
    
    
    
    
    
    public static DG merge (DG dg1, DG dg2) {
        DG ndg = DG.createDG ();
        Object startNode = merge (
            dg1, dg2, 
            dg1.getStartNode (), 
            dg2.getStartNode (), 
            ndg,
            true, true
        );
        ndg.setStart (startNode);
        return ndg;
    }
    
    private static Object merge (
        DG dg1,
        DG dg2,
        Object n1,
        Object n2,
        DG ndg,
        boolean addEnds1,
        boolean addEnds2
    ) {
        DNode dnode = new DNode (n1, n2);
        if (ndg.containsNode (dnode)) return dnode;
        ndg.addNode (dnode);
        ndg.putAllProperties (dnode, dg1.getProperties (n1));
        ndg.putAllProperties (dnode, dg2.getProperties (n2));
        if (addEnds1 && dg1.getEnds ().contains (n1))
            ndg.addEnd (dnode);
        if (addEnds2 && dg2.getEnds ().contains (n2))
            ndg.addEnd (dnode);
        
        Set edges2 = new HashSet (dg2.getEdges (n2));
        Iterator it = dg1.getEdges (n1).iterator ();
        while (it.hasNext ()) {
            Object edge = it.next ();
            Object nn1 = dg1.getNode (n1, edge);
            Object nn2 = dg2.getNode (n2, edge);
            Map properties = null;
            if ( !edge.equals (Pattern.STAR) && 
                 edges2.contains (Pattern.STAR) &&
                 nn2 == null
            ) {
                nn2 = dg2.getNode (n2, Pattern.STAR);
                properties = dg2.getProperties (n2, Pattern.STAR);
            } else
            if (nn2 != null)
                properties = dg2.getProperties (n2, edge);
            Object nnode = nn2 == null ?
                merge (dg1, nn1, ndg, addEnds1) :
                merge (dg1, dg2, nn1, nn2, ndg, addEnds1, addEnds2);
            ndg.addEdge (dnode, nnode, edge);
            ndg.putAllProperties (dnode, edge, dg1.getProperties (n1, edge));
            if (properties != null)
                ndg.putAllProperties (dnode, edge, properties);
            edges2.remove (edge);
        }
        it = edges2.iterator ();
        while (it.hasNext ()) {
            Object edge = it.next ();
            Object nn2 = dg2.getNode (n2, edge);
            Object nnode = null;
            Map properties = null;
            if ( !edge.equals (Pattern.STAR) && 
                 dg1.getEdges (n1).contains (Pattern.STAR)
            ) {
                nnode = merge (dg1, dg2, dg1.getNode (n1, Pattern.STAR), nn2, ndg, addEnds1, addEnds2);
                properties = dg1.getProperties (n1, Pattern.STAR);
            } else
                nnode = merge (dg2, nn2, ndg, addEnds2);
            ndg.addEdge (dnode, nnode, edge);
            ndg.putAllProperties (dnode, edge, dg2.getProperties (n2, edge));
            if (properties != null)
                ndg.putAllProperties (dnode, edge, properties);
        }
        return dnode;
    }
    
    private static Object merge (
        DG dg,
        Object n,
        DG ndg,
        boolean addEnds
    ) {
        if (ndg.containsNode (n)) return n;
        ndg.addNode (n);
        ndg.putAllProperties (n, dg.getProperties (n));
        if (addEnds && dg.getEnds ().contains (n))
            ndg.addEnd (n);
        
        Iterator it = dg.getEdges (n).iterator ();
        while (it.hasNext ()) {
            Object edge = it.next ();
            Object nn = dg.getNode (n, edge);
            Object endN = merge (dg, nn, ndg, addEnds);
            ndg.addEdge (n, endN, edge);
            ndg.putAllProperties (n, edge, dg.getProperties (n, edge));
        }
        return n;
    }
    
    private static class DNode {
        Object n1;
        Object n2;
        
        DNode (Object n1, Object n2) {
            this.n1 = n1;
            this.n2 = n2;
            if (n1 == null) throw new NullPointerException ();
            if (n2 == null) throw new NullPointerException ();
        }

        public int hashCode () {
            if (n1 != null)
                if (n2 != null) return n1.hashCode () * n2.hashCode ();
                else return n1.hashCode ();
            else
                return n2.hashCode ();
        }
        
        public boolean equals (Object obj) {
            return obj instanceof DNode &&
                n1 == ((DNode) obj).n1 &&
                n2 == ((DNode) obj).n2;
        }
        
        public String toString () {
            return "DN " + n1 + ":" + n2;
        }
    }
    
    static DG reduce (DG dg) {
        Map ends = new HashMap ();
        Set other = new HashSet ();
        Iterator it = dg.getNodes ().iterator ();
        while (it.hasNext ()) {
            Object node = it.next ();
            if (!dg.getEnds ().contains (node))
                other.add (node);
            else {
                Set e = (Set) ends.get (dg.getProperties (node));
                if (e == null) {
                    e = new HashSet ();
                    ends.put (dg.getProperties (node), e);
                }
                e.add (node);
            }
        }
        Set newNodes = new HashSet ();
        if (other.size () > 0) newNodes.add (other);
        newNodes.addAll (ends.values ());
        Map ng = reduce (dg, newNodes);

        DG ndg = DG.createDG ();
        it = ng.keySet ().iterator ();
        while (it.hasNext ()) {
            Set node = (Set) it.next ();
            if (!ndg.containsNode (node))
                ndg.addNode (node);
            Map edgeToNode = (Map) ng.get (node);
            Iterator it2 = edgeToNode.keySet ().iterator ();
            while (it2.hasNext ()) {
                Object edge = it2.next ();
                Set end = (Set) edgeToNode.get (edge);
                if (!ndg.containsNode (end))
                    ndg.addNode (end);
                ndg.addEdge (node, end, edge);
            }
        }
        ndg.setEnds (new HashSet ());
        it = ndg.getNodes ().iterator ();
        while (it.hasNext ()) {
            Set node = (Set) it.next ();
            Iterator it2 = node.iterator ();
            while (it2.hasNext ()) {
                Object n = it2.next ();
                if (dg.containsNode (n) && dg.getProperties (n) != null)
                    ndg.putAllProperties (node, dg.getProperties (n));
                Iterator it3 = ndg.getEdges (node).iterator ();
                while (it3.hasNext ()) {
                    Object edge = it3.next ();
                    if (dg.containsNode (n) && dg.getProperties (n, edge) != null)
                        ndg.putAllProperties (node, edge, dg.getProperties (n, edge));
                }
                if (dg.getEnds ().contains (n))
                    ndg.addEnd (node);
                if (dg.getStartNode ().equals (n))
                    ndg.setStart (node);
            }
        }
        return ndg;
    }
    
    private static Map reduce (DG dg, Set s) {
        Map m = new HashMap ();
        Iterator it = s.iterator ();
        while (it.hasNext ()) {
            Set nnode = (Set) it.next ();
            Iterator it2 = nnode.iterator ();
            while (it2.hasNext ()) {
                Object node = it2.next ();
                m.put (node, nnode);
            }
        }
        
        Map nnodes = new HashMap ();
        it = s.iterator ();
        while (it.hasNext ()) {
            Set nnode = (Set) it.next ();
            Map nodes = new HashMap ();
            Iterator it2 = nnode.iterator ();
            while (it2.hasNext ()) {
                Object node = it2.next ();
                Map edges = new HashMap ();
                Iterator it3 = dg.getEdges (node).iterator ();
                while (it3.hasNext ()) {
                    Object edge = it3.next ();
                    Object endNode = dg.getNode (node, edge);
                    edges.put (edge, m.get (endNode));
                }
                Set n = (Set) nodes.get (edges);
                if (n == null) {
                    n = new HashSet ();
                    nodes.put (edges, n);
                }
                n.add (node);
            }
            it2 = nodes.keySet ().iterator ();
            while (it2.hasNext ()) {
                Map edges = (Map) it2.next ();
                Set newState = (Set) nodes.get (edges);
                nnodes.put (newState, edges);
            }
        }
        if (nnodes.size () > s.size ())
            return reduce (dg, nnodes.keySet ());
        return nnodes;
    }

  
//      wrong: a*a  
//    static DG append (DG dg1, DG dg2) {
//        DG ndg = dg1.cloneDG ();
//        
//        Iterator it = dg2.getNodes ().iterator ();
//        while (it.hasNext ()) {
//            Object node = it.next ();
//            if (!ndg.containsNode (node)) {
//                ndg.addNode (node);
//                ndg.putAllProperties (node, dg2.getProperties (node));
//            }
//            Iterator it2 = dg2.getEdges (node).iterator ();
//            while (it2.hasNext ()) {
//                Object edge = it2.next ();
//                Object endN = dg2.getNode (node, edge);
//                if (!ndg.containsNode (endN)) {
//                    ndg.addNode (endN);
//                    ndg.putAllProperties (endN, dg2.getProperties (endN));
//                }
//                ndg.addEdge (node, endN, edge);
//                ndg.putAllProperties (node, edge, dg2.getProperties (node, edge));
//            }
//        }
//        
//        Set oldEnds = ndg.getEnds ();
//        ndg.setEnds (dg2.getEnds ());
//        
//        it = oldEnds.iterator ();
//        while (it.hasNext ()) {
//            Object n = it.next ();
//            append (ndg, n, dg2.getStartNode ());
//        }
//        it = ndg.getNodes ().iterator ();
//        while (it.hasNext ()) {
//            Object node = it.next ();
//            if ( node instanceof DNode &&
//                 dg2.getEnds ().contains (((DNode) node).n2)
//            )
//                ndg.addEnd (node);
//        }
//        return reduce (ndg);
//    }
//    
//    private static void append (DG dg, Object n1, Object n2) {
//        Iterator it = dg.getEdges (n2).iterator ();
//        while (it.hasNext ()) {
//            Object edge = it.next ();
//            Object nn1 = dg.getNode (n1, edge);
//            Object nn2 = dg.getNode (n2, edge);
//            if (nn1 != null)
//                append (dg, nn1, nn2);
//            else
//            if (dg.getNode (n1, Pattern.STAR) != null) {
//                Object en = merge (dg, dg, dg.getNode (n1, Pattern.STAR), nn2, dg);
//                dg.addEdge (n1, en, edge);
//            } else
//                dg.addEdge (n1, nn2, edge);
//        }
//        if (dg.getEnds ().contains (n2)) dg.addEnd (n1);
//    }
    
//    static DG merge (
//        DG dg1,
//        DG dg2,
//        Object where,
//        Object what
//    ) {
//        try {
//        // g, ng Map (node > Map (edge > Set (node)))
//        
//        // 1) dg1 -> g
//        Map g = new HashMap ();
//        Iterator it = dg1.getNodes ().iterator ();
//        while (it.hasNext ()) {
//            Object node = it.next ();
//            Map edgeToNodes = new HashMap ();
//            g.put (node, edgeToNodes);
//            
//            Iterator it2 = dg1.getEdges (node).iterator ();
//            while (it2.hasNext ()) {
//                Object edge = it2.next ();
//                Object end = dg1.getNode (node, edge);
//                Set ends = new HashSet ();
//                ends.add (end);
//                edgeToNodes.put (edge, ends);
//            }
//        }
//
//        // 2) dg2 what node to where node in g
//        Map edgeToNodes = (Map) g.get (where);
//        it = dg2.getEdges (what).iterator ();
//        while (it.hasNext ()) {
//            Object edge = it.next ();
//            Set ends = (Set) edgeToNodes.get (edge);
//            if (ends == null) {
//                ends = new HashSet ();
//                edgeToNodes.put (edge, ends);
//            }
//            Object end = dg2.getNode (what, edge);
//            if (end == what)
//                ends.add (where);
//            else
//                ends.add (end);
//        }
//        
//        // 3) merge rest of dg2 nodes to g
//        it = dg2.getNodes ().iterator ();
//        while (it.hasNext ()) {
//            Object node = it.next ();
//            if (node == what) continue;
//            edgeToNodes = new HashMap ();
//            g.put (node, edgeToNodes);
//            
//            Iterator it2 = dg2.getEdges (node).iterator ();
//            while (it2.hasNext ()) {
//                Object edge = it2.next ();
//                Object end = dg2.getNode (node, edge);
//                Set ends = new HashSet ();
//                if (end == what)
//                    ends.add (where);
//                else
//                    ends.add (end);
//                edgeToNodes.put (edge, ends);
//            }
//        }
//        
//        Map ng = new HashMap ();
//        Set newStart = new HashSet ();
//        newStart.add (dg1.getStartNode ());
//        merge (g, newStart, ng);
//        
//        DG dg = DG.createDG (newStart);
//        it = ng.keySet ().iterator ();
//        while (it.hasNext ()) {
//            Set node = (Set) it.next ();
//            if (!dg.containsNode (node))
//                dg.addNode (node);
//            Map edgeToNode = (Map) ng.get (node);
//            Iterator it2 = edgeToNode.keySet ().iterator ();
//            while (it2.hasNext ()) {
//                Object edge = it2.next ();
//                Set end = (Set) edgeToNode.get (edge);
//                if (!dg.containsNode (end))
//                    dg.addNode (end);
//                dg.addEdge (node, end, edge);
//            }
//        }
//        dg.setStart (newStart);
//        dg.setEnds (new HashSet ());
//        it = dg.getNodes ().iterator ();
//        while (it.hasNext ()) {
//            Set node = (Set) it.next ();
//            Iterator it2 = node.iterator ();
//            while (it2.hasNext ()) {
//                Object n = it2.next ();
//                if (dg1.containsNode (n) && dg1.getProperties (n) != null)
//                    dg.putAllProperties (node, dg1.getProperties (n));
//                if (dg2.containsNode (n) && dg2.getProperties (n) != null)
//                    dg.putAllProperties (node, dg2.getProperties (n));
//                Iterator it3 = dg.getEdges (node).iterator ();
//                while (it3.hasNext ()) {
//                    Object edge = it3.next ();
//                    if (dg1.containsNode (n) && dg1.getProperties (n, edge) != null)
//                        dg.putAllProperties (node, edge, dg1.getProperties (n, edge));
//                    if (dg2.containsNode (n) && dg2.getProperties (n, edge) != null)
//                        dg.putAllProperties (node, edge, dg2.getProperties (n, edge));
//                    if (n == where && dg2.getProperties (what, edge) != null)
//                        dg.putAllProperties (node, edge, dg2.getProperties (what, edge));
//                }
//                if (dg1.getEnds ().contains (n) ||
//                    dg2.getEnds ().contains (n)
//                ) {
//                    dg.addEnd (node);
//                    continue;
//                }
//            }
//        }
//        return dg;
//        
//        } catch (Exception e) {
//            e.printStackTrace ();
//            System.out.println("dg1:" + dg1);
//            System.out.println("dg2:" + dg2);
//            System.out.println("where:" + where);
//            return null;
//        }
//    }

//    static DG append (
//        DG dg1,
//        DG dg2
//    ) {
//        System.out.println("append");
//        System.out.println(dg1);
//        System.out.println();
//        System.out.println(dg2);
//        
//        try {
//        // g, ng Map (node > Map (edge > Set (node)))
//        
//        // 1) dg1 -> g
//        Map g = new HashMap ();
//        Iterator it = dg1.getNodes ().iterator ();
//        while (it.hasNext ()) {
//            Object node = it.next ();
//            Map edgeToNodes = new HashMap ();
//            g.put (node, edgeToNodes);
//            
//            Iterator it2 = dg1.getEdges (node).iterator ();
//            while (it2.hasNext ()) {
//                Object edge = it2.next ();
//                Object end = dg1.getNode (node, edge);
//                Set ends = new HashSet ();
//                ends.add (end);
//                edgeToNodes.put (edge, ends);
//            }
//        }
//
//        // 2) dg2 start append to all dg1 ends
//        it = dg1.getEnds ().iterator ();
//        while (it.hasNext ()) {
//            Object end = it.next ();
//            Map edgeToNodes = (Map) g.get (end);
//            Iterator it2 = dg2.getEdges (dg2.getStartNode ()).iterator ();
//            while (it2.hasNext ()) {
//                Object edge = it2.next ();
//                Set ends = (Set) edgeToNodes.get (edge);
//                if (ends == null) {
//                    ends = new HashSet ();
//                    edgeToNodes.put (edge, ends);
//                }
//                Object e = dg2.getNode (dg2.getStartNode (), edge);
//                if (e == dg2.getStartNode ())
//                    ends.add (end);
//                else
//                    ends.add (e);
//            }
//        }
//        
//        // 3) merge rest of dg2 nodes to g
//        it = dg2.getNodes ().iterator ();
//        while (it.hasNext ()) {
//            Object node = it.next ();
//            if (node == dg2.getStartNode ()) continue;
//            Map edgeToNodes = new HashMap ();
//            g.put (node, edgeToNodes);
//            
//            Iterator it2 = dg2.getEdges (node).iterator ();
//            while (it2.hasNext ()) {
//                Object edge = it2.next ();
//                Object end = dg2.getNode (node, edge);
//                Set ends = new HashSet ();
//                if (end == dg2.getStartNode ())
//                    ends.add (dg1.getEnds ().iterator ().next ());
//                else
//                    ends.add (end);
//                edgeToNodes.put (edge, ends);
//            }
//        }
//        
//        Map ng = new HashMap ();
//        Set newStart = new HashSet ();
//        newStart.add (dg1.getStartNode ());
//        merge (g, newStart, ng);
//        
//        DG dg = DG.createDG (newStart);
//        it = ng.keySet ().iterator ();
//        while (it.hasNext ()) {
//            Set node = (Set) it.next ();
//            if (!dg.containsNode (node))
//                dg.addNode (node);
//            Map edgeToNode = (Map) ng.get (node);
//            Iterator it2 = edgeToNode.keySet ().iterator ();
//            while (it2.hasNext ()) {
//                Object edge = it2.next ();
//                Set end = (Set) edgeToNode.get (edge);
//                if (!dg.containsNode (end))
//                    dg.addNode (end);
//                dg.addEdge (node, end, edge);
//            }
//        }
//        dg.setStart (newStart);
//        dg.setEnds (new HashSet ());
//        it = dg.getNodes ().iterator ();
//        while (it.hasNext ()) {
//            Set node = (Set) it.next ();
//            Iterator it2 = node.iterator ();
//            while (it2.hasNext ()) {
//                Object n = it2.next ();
//                if (dg1.containsNode (n) && dg1.getProperties (n) != null)
//                    dg.putAllProperties (node, dg1.getProperties (n));
//                if (dg2.containsNode (n) && dg2.getProperties (n) != null)
//                    dg.putAllProperties (node, dg2.getProperties (n));
//                Iterator it3 = dg.getEdges (node).iterator ();
//                while (it3.hasNext ()) {
//                    Object edge = it3.next ();
//                    if (dg1.containsNode (n) && dg1.getProperties (n, edge) != null)
//                        dg.putAllProperties (node, edge, dg1.getProperties (n, edge));
//                    if (dg2.containsNode (n) && dg2.getProperties (n, edge) != null)
//                        dg.putAllProperties (node, edge, dg2.getProperties (n, edge));
//                    if (dg1.getEnds ().contains (n) && dg2.getProperties (dg2.getStartNode (), edge) != null)
//                        dg.putAllProperties (node, edge, dg2.getProperties (dg2.getStartNode (), edge));
//                }
//                
//                if (dg2.getEnds ().contains (n)) {
//                    dg.addEnd (node);
//                    continue;
//                }
//                if (dg2.getEnds ().contains (dg2.getStartNode ()) &&
//                    dg1.getEnds ().contains (n)
//                ) {
//                    dg.addEnd (node);
//                    continue;
//                }
//            }
//        }
//        return reduce (dg);
//        
//        } catch (Exception e) {
//            e.printStackTrace ();
//            System.out.println("dg1:" + dg1);
//            System.out.println("dg2:" + dg2);
//            return null;
//        }
//    }

//    static DG star (
//        DG dg
//    ) {
//        try {
//        // g, ng Map (node > Map (edge > Set (node)))
//        
//        // 1) dg -> g
//        Map g = new HashMap ();
//        Iterator it = dg.getNodes ().iterator ();
//        while (it.hasNext ()) {
//            Object node = it.next ();
//            Map edgeToNodes = new HashMap ();
//            g.put (node, edgeToNodes);
//            
//            Iterator it2 = dg.getEdges (node).iterator ();
//            while (it2.hasNext ()) {
//                Object edge = it2.next ();
//                Object end = dg.getNode (node, edge);
//                Set ends = new HashSet ();
//                ends.add (end);
//                edgeToNodes.put (edge, ends);
//            }
//        }
//
//        // 2) merge start node to all end nodes
//        Object start = dg.getStartNode ();
//        it = dg.getEnds ().iterator ();
//        while (it.hasNext ()) {
//            Object end = it.next ();
//            if (end == start) continue;
//            Map edgeToNodes = (Map) g.get (end);
////            wrong:
////            if (edgeToNodes.isEmpty ()) {
////                Iterator it2 = g.values ().iterator ();
////                while (it2.hasNext ()) {
////                    Map m = (Map) it2.next ();
////                    Iterator it3 = m.values ().iterator ();
////                    while (it3.hasNext ()) {
////                        Set s = (Set) it3.next ();
////                        if (s.remove (end))
////                            s.add (start);
////                    }
////                }
////                continue;
////            }
//            Iterator it2 = dg.getEdges (start).iterator ();
//            while (it2.hasNext ()) {
//                Object edge = it2.next ();
//                Set ends = (Set) edgeToNodes.get (edge);
//                if (ends == null) {
//                    ends = new HashSet ();
//                    edgeToNodes.put (edge, ends);
//                }
//                Object e = dg.getNode (start, edge);
//                if (e == start)
//                    ends.add (end);
//                else
//                    ends.add (e);
//            }
//        }
//        
//        Map ng = new HashMap ();
//        Set newStart = new HashSet ();
//        newStart.add (dg.getStartNode ());
//        merge (g, newStart, ng);
//        
//        DG ndg = DG.createDG (newStart);
//        it = ng.keySet ().iterator ();
//        while (it.hasNext ()) {
//            Set node = (Set) it.next ();
//            if (!ndg.containsNode (node))
//                ndg.addNode (node);
//            Map edgeToNode = (Map) ng.get (node);
//            Iterator it2 = edgeToNode.keySet ().iterator ();
//            while (it2.hasNext ()) {
//                Object edge = it2.next ();
//                Set end = (Set) edgeToNode.get (edge);
//                if (!ndg.containsNode (end))
//                    ndg.addNode (end);
//                ndg.addEdge (node, end, edge);
//            }
//        }
//        ndg.setStart (newStart);
//        it = ndg.getNodes ().iterator ();
//        while (it.hasNext ()) {
//            Set node = (Set) it.next ();
//            Iterator it2 = node.iterator ();
//            while (it2.hasNext ()) {
//                Object n = it2.next ();
//                if (dg.getProperties (n) != null)
//                    ndg.putAllProperties (node, dg.getProperties (n));
//                Iterator it3 = ndg.getEdges (node).iterator ();
//                while (it3.hasNext ()) {
//                    Object edge = it3.next ();
//                    if (dg.getProperties (n, edge) != null)
//                        ndg.putAllProperties (node, edge, dg.getProperties (n, edge));
//                    if (dg.getEnds ().contains (n) && dg.getProperties (dg.getStartNode (), edge) != null)
//                        ndg.putAllProperties (node, edge, dg.getProperties (dg.getStartNode (), edge));
//                }
//                if (dg.getEnds ().contains (n)) {
//                    ndg.addEnd (node);
//                    continue;
//                }
//            }
//        }
//        return reduce (ndg);
//        
//        } catch (Exception e) {
//            e.printStackTrace ();
//            System.out.println("dg:" + dg);
//            return null;
//        }
//    }
//    
//    private static void merge (
//        Map g,
//        Set node,
//        Map ng
//    ) {
//        Map newEdgeToNodes = new HashMap ();
//        ng.put (node, newEdgeToNodes);
//        
//        Set dots = new HashSet ();
//        Iterator it = node.iterator ();
//        while (it.hasNext ()) {
//            Object oldNode = it.next ();
//            Map oldEdgeToNodes = (Map) g.get (oldNode);
//            Set nodes = (Set)oldEdgeToNodes.get (Pattern.STAR);
//            if (nodes == null) continue;
//            dots.addAll (nodes);
//        }
//        it = node.iterator ();
//        while (it.hasNext ()) {
//            Object oldNode = it.next ();
//            Map oldEdgeToNodes = (Map) g.get (oldNode);
//            Set ddots = null;
//            Iterator it2 = oldEdgeToNodes.keySet ().iterator ();
//            while (it2.hasNext ()) {
//                Object edge = it2.next ();
//                Set nodes = (Set) newEdgeToNodes.get (edge);
//                if (nodes == null) {
//                    nodes = new HashSet ();
//                    newEdgeToNodes.put (edge, nodes);
//                }
//                nodes.addAll ((Set) oldEdgeToNodes.get (edge));
//                if (edge.equals (Pattern.STAR) || dots.isEmpty ()) continue;
//                if (ddots == null) {
//                    ddots = new HashSet (dots);
//                    Set pom = (Set) oldEdgeToNodes.get (Pattern.STAR);
//                    if (pom != null)
//                        ddots.removeAll (pom);
//                }
//                nodes.addAll (ddots);
//            }
//        }
//        
//        it = newEdgeToNodes.values ().iterator ();
//        while (it.hasNext ()) {
//            Set n = (Set) it.next ();
//            if (ng.containsKey (n)) continue;
//            merge (g, n, ng);
//        }
//    }
}
