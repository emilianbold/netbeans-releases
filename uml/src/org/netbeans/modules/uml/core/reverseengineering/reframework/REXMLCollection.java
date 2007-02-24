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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.uml.core.reverseengineering.reframework;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Node;

import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 * ETXMLCollection is a template used to retrieve the collection of XML tags.
 * @param DataType The collect element type.
 */
public class REXMLCollection<T extends IParserData>
        extends ETArrayList<T> implements ETList<T>
{
    private String m_XPath;
    private Node   m_Node;
    private Class  clazz;;

    public REXMLCollection(Class c, String xpath)
    {
        this.clazz  = c;
        m_XPath     = xpath;
    }
    
    public REXMLCollection(Class c, String xpath, Node node)
            throws InstantiationException, IllegalAccessException
    {
        this(c, xpath);
        setDOMNode(node);
    }
    
    public void setDOMNode(Node node) throws InstantiationException,
        IllegalAccessException
    {
        if ((m_Node = node) == null)
            return ;
        String name = node.getName();
        List nodes = node.selectNodes(m_XPath);
        for (int i = 0, nc = nodes.size(); i < nc; ++i)
            addElement((Node) nodes.get(i));
    }
    
    public Node getDOMNode()
    {
        return m_Node;
    }
    
    private void addElement(Node node) throws InstantiationException,
        IllegalAccessException
    {
        T type = (T) clazz.newInstance();
        type.setEventData(node);
        add(type);
    }
    
    public boolean find(T t)
    {
        return contains(t);
    }
    
    public int getCount()
    {
        return size();
    }
    
    public T item(int i)
    {
        return get(i);
    }
}