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

/*
 * W3cAttributeProxy.java
 *
 * Created on July 26, 2005, 1:29 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.netbeans.modules.uml.core.support.umlutils;

import com.sun.org.apache.xml.internal.dtm.DTMDOMException;
import com.sun.org.apache.xml.internal.dtm.ref.DTMNodeProxy;
import org.dom4j.Attribute;
import org.dom4j.Namespace;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;

/**
 *
 * @author Administrator
 */
public class W3CAttributeProxy extends W3CNodeProxy  implements Attr, Attribute
{    
    /** Creates a new instance of W3cAttributeProxy */
    public W3CAttributeProxy(DTMNodeProxy n)
    {
        super(n);
    }

    ///////////////////////////////////////////////////////////////////////////
    // W3C Attribute Implementation.
    /** Returns the code according to the type of node.
     * This makes processing nodes polymorphically much easier as the
     * switch statement can be used instead of multiple if (instanceof)
     * statements.
     *
     * @return a W3C DOM complient code for the node type such as
     * ELEMENT_NODE or ATTRIBUTE_NODE
     */
    public short getNodeType()
    {
        return Node.ATTRIBUTE_NODE;
    }
    
    public Element getOwnerElement()
    {
        Element retVal = getProxyNode().getOwnerElement();
        if(retVal instanceof DTMNodeProxy)
        {
            retVal = new W3CNodeProxy((DTMNodeProxy)retVal);
        }
        return retVal;
    }

    public boolean getSpecified()
    {
        return getProxyNode().getSpecified();
    }

    public String getValue()
    {
        return getProxyNode().getValue();
    }

    public boolean isId()
    {
        return getProxyNode().isId();
    }

    public void setValue(String str) throws org.w3c.dom.DOMException
    {
        getProxyNode().setValue(str);
    }
    
    ///////////////////////////////////////////////////////////////////////////
    // DOM4J Attribute Implemenation 
    
    /** <p>Sets the <code>Namespace</code> of this element or if this element
      * is read only then an <code>UnsupportedOperationException</code> 
      * is thrown.</p>
      *
      * @param namespace is the <code>Namespace</code> to associate with this 
      * element
      */
    public void setNamespace(Namespace namespace)
    {
        throw new DTMDOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR);
    }
    
    /** Accesses the data of this attribute which may implement data typing 
      * bindings such as XML Schema or 
      * Java Bean bindings or will return the same value as {@link #getText}
      */
    public Object getData()
    {
        return getValue();
    }
    
    /** Sets the data value of this attribute if this element supports data 
      * binding or calls {@link #setText} if it doesn't
      */
    public void setData(Object data)
    {
        throw new DTMDOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR);
    }
}
