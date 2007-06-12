/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.test.xml.schema.lib.dom.parser;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeMap;
import org.w3c.dom.Node;

/**
 *
 * @author ca@netbeans.org
 */
public class NodeIterator {
    private Iterator m_iterator;
    private LinkedList<Node> m_trace = new LinkedList<Node>();
    
    /** Creates a new instance of NodeIterator */
    public NodeIterator(TreeMap<String, Node> map) {
        Collection c = map.values();
        m_iterator   = c.iterator();
    }
    
    public Node next() {
        boolean bSeekChildren = true;
        
        while (true) {
            if (m_trace.size() == 0) {
                Node node = null;
                if (m_iterator.hasNext()) {
                    node = (Node) m_iterator.next();
                    if (node != null) {
                        m_trace.add(node);
                    }
                }
                return node;
            } else {
                Node node = m_trace.getLast();
                Node nextNode = null;
                if (node.hasChildNodes() && bSeekChildren) {
                    nextNode = node.getFirstChild();
                    m_trace.add(nextNode);
                } else {
                    if (m_trace.size() > 1) {
                        nextNode = node.getNextSibling();
                    }
                    m_trace.removeLast();
                    if (nextNode != null) {
                        bSeekChildren = true;
                        m_trace.add(nextNode);
                    } else {
                        bSeekChildren = false;
                        continue;
                    }
                }
                
                return nextNode;
            }
        }
    }
}
