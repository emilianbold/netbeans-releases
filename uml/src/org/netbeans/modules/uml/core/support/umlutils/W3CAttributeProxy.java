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
