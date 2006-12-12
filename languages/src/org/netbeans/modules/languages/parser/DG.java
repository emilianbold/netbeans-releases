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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.languages.parser;

import java.util.*;


/**
 * Directed Graph implementation.
 *
 * @author Jan Jancura
 */
public class DG {

    
    static DG createDG (Object node) {
        return new DG (node);
    }
    
    static DG createDG () {
        return new DG ();
    }

    
    private Map idToNode = new HashMap ();
    private Map nodeToId = new HashMap ();
    private Object start;
    private Set ends = new HashSet ();
    
    private DG () {
    }
    
    private DG (Object node) {
        start = node;
        Node n = new Node ();
        idToNode.put (node, n);
        nodeToId.put (n, node);
        ends.add (node);
    }
    
    Object getStartNode () {
        return start;
    }
    
    void setStart (Object node) {
        if (idToNode.get (node) == null) new NullPointerException ();
        start = node;
    }
    
    Set getEnds () {
        return Collections.unmodifiableSet (ends);
    }
    
    void setEnds (Set ends) {
        this.ends = new HashSet (ends);
    }
    
    void addEnd (Object end) {
        ends.add (end);
    }
    
    void removeEnd (Object end) {
        ends.remove (end);
    }
    
    void addNode (Object node) {
        if (idToNode.containsKey (node)) throw new IllegalArgumentException ();
        Node n = new Node ();
        idToNode.put (node, n);
        nodeToId.put (n, node);
    }
    
    void removeNode (Object node) {
        Node n = (Node) idToNode.remove (node);
        nodeToId.remove (n);
    }
    
    boolean containsNode (Object node) {
        return idToNode.containsKey (node);
    }
    
    Set getNodes () {
        return Collections.unmodifiableSet (idToNode.keySet ());
    }
    
    Object getNode (Object node, Object edge) {
        Node s = (Node) idToNode.get (node);
        Node e = s.getNode (edge);
        return nodeToId.get (e);
    }
    
    void addEdge (
        Object startNode,
        Object endNode,
        Object edge
    ) {
        Node s = (Node) idToNode.get (startNode);
        Node e = (Node) idToNode.get (endNode);
        s.addEdge (edge, e);
    }
    
    Set getEdges (Object node) {
        Node n = (Node) idToNode.get (node);
        return n.edges ();
    }
    
    Object getEdge (Object node, Object edge) {
        Node n = (Node) idToNode.get (node);
        return n.getEdge (edge);
    }

    Object getProperty (Object node, Object key) {
        Node n = (Node) idToNode.get (node);
        return n.getProperty (key);
    }
    
    Map getProperties (Object node) {
        Node n = (Node) idToNode.get (node);
        if (n.properties == null) return Collections.emptyMap ();
        return Collections.unmodifiableMap (n.properties);
    }
    
    void putAllProperties (Object node, Map properties) {
        if (properties.size () == 0) return;
        Node n = (Node) idToNode.get (node);
        if (n.properties == null) n.properties = new HashMap ();
        n.properties.putAll (properties);
    }

    void setProperty (Object node, Object key, Object value) {
        Node n = (Node) idToNode.get (node);
        n.setProperty (key, value);
    }

    Object getProperty (Object node, Object edge, Object key) {
        Node n = (Node) idToNode.get (node);
        return n.getEdgeProperty (edge, key);
    }

    Map getProperties (Object node, Object edge) {
        Node n = (Node) idToNode.get (node);
        if (n.idToProperties == null ||
            n.idToProperties.get (edge) == null
        ) return Collections.emptyMap ();
        return Collections.unmodifiableMap ((Map) n.idToProperties.get (edge));
    }

    void putAllProperties (Object node, Object edge, Map properties) {
        if (properties.size () == 0) return;
        Node n = (Node) idToNode.get (node);
        if (n.idToProperties == null) n.idToProperties = new HashMap ();
        if (n.idToProperties.get (edge) == null)
            n.idToProperties.put (edge, new HashMap ());
        ((Map) n.idToProperties.get (edge)).putAll (properties);
    }
    
    void setProperty (Object node, Object edge, Object key, Object value) {
        Node n = (Node) idToNode.get (node);
        n.setEdgeProperty (edge, key, value);
    }
    
    void changeKey (Object oldNode, Object newNode) {
        Node n = (Node) idToNode.get (oldNode);
        idToNode.remove (oldNode);
        idToNode.put (newNode, n);
        nodeToId.put (n, newNode);
    }
    
    public String toString () {
        StringBuffer sb = new StringBuffer ();
        Iterator it = getNodes ().iterator ();
        while (it.hasNext ()) {
            Object node = it.next ();
            sb.append (node).append ('(');
            Iterator it2 = getEdges (node).iterator ();
            while (it2.hasNext ()) {
                Object edge = it2.next ();
                Object end = getNode (node, edge);
                sb.append (convert (edge)).append (end);
                if (it2.hasNext ()) sb.append (',');
            }
            sb.append (')');
            if (it.hasNext ()) sb.append ('\n');
        }
        sb.append (" start: ").append (getStartNode ()).append (" end: ");
        it = getEnds ().iterator ();
        while (it.hasNext ()) {
            Object end = it.next ();
            sb.append (end);
            if (it.hasNext ()) sb.append (',');
        }
        sb.append ('\n');
        it = getNodes ().iterator ();
        while (it.hasNext ()) {
            Object node = it.next ();
            Node n = (Node) idToNode.get (node);
            sb.append ("  ").append (node).append (": ");
            if (n.properties != null)
                sb.append (n.properties);
            sb.append ('\n');
            if (n.idToProperties != null) {
                Iterator it2 = n.idToProperties.keySet ().iterator ();
                while (it2.hasNext ()) {
                    Object edge = it2.next ();
                    Map m = (Map) n.idToProperties.get (edge);
                    sb.append ("    ").append (convert (edge)).append (": ").append (m).append ('\n');
                }
            }
        }
        return sb.toString ();
    }
    
    private static Character NN = new Character ('\n');
    private static Character NR = new Character ('\n');
    private static Character NT = new Character ('\n');
    private static Character NS = new Character ('\n');
    
    private String convert (Object edge) {
        if (Pattern.STAR.equals (edge)) return ".";
        if (NN.equals (edge)) return "\\n";
        if (NR.equals (edge)) return "\\r";
        if (NT.equals (edge)) return "\\t";
        if (NS.equals (edge)) return "' '";
        return edge.toString ();
    }

    DG cloneDG (boolean cloneProperties) {
        DG dg = DG.createDG ();
        Iterator it = getNodes ().iterator ();
        while (it.hasNext ()) {
            Object node = it.next ();
            Set nnode = Collections.singleton (node);
            if (!dg.containsNode (nnode)) {
                dg.addNode (nnode);
                if (cloneProperties)
                    dg.putAllProperties (nnode, getProperties (node));
            }
            Iterator it2 = getEdges (node).iterator ();
            while (it2.hasNext ()) {
                Object edge = it2.next ();
                Object endN = getNode (node, edge);
                Object nEndN = Collections.singleton (endN);
                if (!dg.containsNode (nEndN)) {
                    dg.addNode (nEndN);
                    if (cloneProperties)
                        dg.putAllProperties (nEndN, getProperties (endN));
                }
                dg.addEdge (nnode, nEndN, edge);
                if (cloneProperties)
                    dg.putAllProperties (nnode, edge, getProperties (node, edge));
            }
            if (getEnds ().contains (node))
                dg.addEnd (nnode);
        }
        dg.setStart (Collections.singleton (getStartNode ()));
        return dg;
    }
    
    
    private static class Node {

        private Map properties;
        private Map idToProperties;
        private Map edgeToNode;
        private Map edges;


        Object getProperty (Object key) {
            if (properties == null) return null;
            return properties.get (key);
        }
        
        void setProperty (Object key, Object value) {
            if (properties == null) properties = new HashMap ();
            properties.put (key, value);
        }
        
        Node getNode (Object edge) {
            if (edgeToNode == null) return null;
            return (Node) edgeToNode.get (edge);
        }

        void addEdge (Object edge, Node node) {
            if (edgeToNode == null) edgeToNode = new HashMap ();
            if (edges == null) edges = new HashMap ();
            edgeToNode.put (edge, node);
            edges.put (edge, edge);
        }

        Object getEdge (Object edge) {
            if (edges == null) return null;
            return edges.get (edge);
        }

        Set edges () {
            if (edgeToNode == null) return Collections.EMPTY_SET;
            return edgeToNode.keySet ();
        }
        
        Object getEdgeProperty (Object edge, Object key) {
            if (idToProperties == null) return null;
            if (idToProperties.get (edge) == null) return null;
            return ((Map) idToProperties.get (edge)).get (key);
        }

        void setEdgeProperty (Object edge, Object key, Object value) {
            if (idToProperties == null) idToProperties = new HashMap ();
            Map m = (Map) idToProperties.get (edge);
            if (m == null) {
                m = new HashMap ();
                idToProperties.put (edge, m);
            }
            m.put (key, value);
        }
    }
}
