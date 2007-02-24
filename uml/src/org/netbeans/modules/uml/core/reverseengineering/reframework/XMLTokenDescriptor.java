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

import org.dom4j.Node;

import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.TokenDescriptor;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;

/**
 */
public class XMLTokenDescriptor extends TokenDescriptor
        implements IXMLTokenDescriptor
{
    private Node m_TokenDescriptorsNode;

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.IXMLTokenDescriptor#getTokenDescriptorNode()
     */
    public Node getTokenDescriptorNode()
    {
        return m_TokenDescriptorsNode;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.IXMLTokenDescriptor#setTokenDescriptorNode(org.dom4j.Node)
     */
    public void setTokenDescriptorNode(Node newVal)
    {
        m_TokenDescriptorsNode = newVal;
    }
    
    /**
     * Retrieves an int attribute from the XML node that represents the descriptor.
     * @param name [in] The attribute to retrieve.
     * @param pVal [out] The value.
     */
    protected int getIntAttribute(String name)
    {
        return XMLManip.getAttributeIntValue(m_TokenDescriptorsNode, name);
    }
    
    /**
     * Sets an int attribute to the XML node that represents the descriptor.
     * @param name [in] The attribute to set.
     * @param pVal [out] The value.
     */
    protected void setIntAttribute(String name, int val)
    {
        XMLManip.setAttributeValue(m_TokenDescriptorsNode, name, 
                String.valueOf(val));
    }
    
    protected String getAttribute(String name)
    {
        return XMLManip.getAttributeValue(m_TokenDescriptorsNode, name);
    }
    
    protected void setAttribute(String name, String value)
    {
        XMLManip.setAttributeValue(m_TokenDescriptorsNode, name, value);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenDescriptor#setLine(int)
     */
    public void setLine(int value)
    {
        setIntAttribute("line", value);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenDescriptor#getLine()
     */
    public int getLine()
    {
        return getIntAttribute("line");
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenDescriptor#setColumn(int)
     */
    public void setColumn(int value)
    {
        setIntAttribute("column", value);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenDescriptor#getColumn()
     */
    public int getColumn()
    {
        return getIntAttribute("column");
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenDescriptor#setPosition(int)
     */
    public void setPosition(int value)
    {
        setIntAttribute("position", value);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenDescriptor#getPosition()
     */
    public long getPosition()
    {
        return getIntAttribute("position");
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenDescriptor#setType(java.lang.String)
     */
    public void setType(String value)
    {
        setAttribute("type", value);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenDescriptor#getType()
     */
    public String getType()
    {
        return getAttribute("type");
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenDescriptor#setValue(java.lang.String)
     */
    public void setValue(String value)
    {
        setAttribute("value", value);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenDescriptor#getValue()
     */
    public String getValue()
    {
        return getAttribute("value");
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenDescriptor#setLength(int)
     */
    public void setLength(int value)
    {
        setIntAttribute("length", value);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenDescriptor#getLength()
     */
    public int getLength()
    {
        return getIntAttribute("length");
    }
}