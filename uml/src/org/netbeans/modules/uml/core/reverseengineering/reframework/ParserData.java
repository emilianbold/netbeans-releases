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

import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;

import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenDescriptor;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 */
public class ParserData implements IParserData
{
    private Node m_EventData;

    /**
     * Retrieves the event data in a pure XMI format.  All TokenDescriptors
     * will be removed from the string.
     * @return The XMI data.
     */
    public String getXMIString()
    {
        // Commented out in C++ code.
        return null;
    }

    /**
     * The event data in its pure format.  All TokenDescriptors will 
     * still exist in the string.
     * @return The data stream.
     */
    public String getRawData()
    {
        Node node = getEventData();
        return node != null? node.asXML() : null;
    }

    /**
     * Retrieves the event data.  The data should only be set by the sender of the event.
     * @param pVal [OUT] The XML document that contains the event data.
     */
    public Node getEventData()
    {
        return m_EventData instanceof Document? 
                  ((Document) m_EventData).getRootElement()
                : m_EventData;
    }

    /**
     * Sets the event data.  The data should only be set by the sender of the event.
     * @param newValue [IN] The XML document that contains the event data.
     */
    public void setEventData(Node value)
    {
        m_EventData = value;
    }

    /**
     * Retrieves the token descriptors for the parser event.
     * @param pVal The token descriptors for the parser data.
     * @return S_OK if successful.
     */
    public ETList<ITokenDescriptor> getTokenDescriptors()
    {
        Node node = getEventData();
        return node != null? getTokenDescriptors(node) : null;
    }
    
    /**
     * Retrieves the token descriptors for the parser event.  The token descriptors will 
     * be contained by the specified node.
     * @param node The DOM node to search.
     * @param pVal The token descriptors for the parser data.
     * @return S_OK if successful.
     */
    private ETList<ITokenDescriptor> getTokenDescriptors(Node node)
    {
        Node descriptorNode = node.selectSingleNode("TokenDescriptors");
        if (descriptorNode != null)
        {
            ETList<ITokenDescriptor> descriptors = 
                    new ETArrayList<ITokenDescriptor>();
            List nodes = descriptorNode.selectNodes("TDescriptor");
            for (int i = 0, nc = nodes.size(); i < nc; ++i)
            {
                IXMLTokenDescriptor desc = new XMLTokenDescriptor();
                desc.setTokenDescriptorNode((Node) nodes.get(i));
                descriptors.add(desc);
            }
            return descriptors;
        }
        return null;
    }
    
    /**
     * Retrieves a node from the XML data.  The XML node contained by the
     * parser data instance will be searched.
     * @param query [in] The query used to find the node.
     * @param pVal [out] The XML node found.
     */
    protected Node getXMLNode(String query)
    {
        Node data = getEventData();
        return data != null? XMLManip.selectSingleNode(data, query) : null;
    }
    
    /**
     * Retrieves a token descriptor of a specified type.  
     * @param type The type of the descriptor.
     * @param pVal The descriptor of NULL if a token descriptor or the specified
     *             type is not found.
     */
    public ITokenDescriptor getTokenDescriptor(String type)
    {
        Node data = getEventData();
        return data != null? getTokenDescriptor(data, type) : null;
    }
    
    /**
     * Retrieves the value of a token descriptor of a specified type.  
     * @param type The type of the descriptor.
     * @return The value of the descriptor if found, else <code>null</code>.
     */
    protected String getDescriptorValue(String type)
    {
        ITokenDescriptor desc = getTokenDescriptor(type);
        return desc != null? desc.getValue() : null;
    }
    
    /**
     * Retrieves a token descriptor of a specified type.  
     * @param pNode The XML node to search.
     * @param type The type of the descriptor.
     * @param pVal The descriptor of NULL if a token descriptor or the specified
     *             type is not found.
     */
    private ITokenDescriptor getTokenDescriptor(Node node, String type)
    {
        ETList<ITokenDescriptor> descriptors = getTokenDescriptors(node);
        if (descriptors != null)
        {    
            int descCount = descriptors.size();
            for (int i = 0; i < descCount; ++i)
            {
                ITokenDescriptor desc = descriptors.get(i);
                if (desc == null) continue;
                
                String curType = desc.getType();
                if (curType != null && curType.equals(type))
                    return desc;
            }
        }
        return null;
    }

    /**
     * Retrieves the name of the file that contained the source code element.
     * @param pVal The file name.
     */
    public String getFilename()
    {
        return getFilename(getEventData());
    }

    /**
     * Retrieves the name of the file that contained the source code element.
     * The specified XML node is search for a token descriptor with the type 
     * of "Filename".  If the token descriptor is not found then the parent
     * XML node is searched.
     * @param pNode The XML node to search.
     * @param pVal The file name.
     */
    protected String getFilename(Node node)
    {
        if (node == null) return null;
        ITokenDescriptor desc = getTokenDescriptor(node, "Filename");
        if (desc != null)
            return desc.getValue();
        Node n = XMLManip.selectSingleNode(node, 
                        "UML:Element.ownedElement/UML:SourceFileArtifact");
        if (n != null)
            return XMLManip.getAttributeValue(n, "sourcefile");
        return getFilename(node.getParent());
    }
    
    /** 
     * Returns the value of the token descriptor whose type is @a type
     * 
     * @param type[in] the type of token descriptor
     * @param pVal[in] the value of the token descriptor
     */
    protected String getTokenDescriptorValue(String type)
    {
        ITokenDescriptor desc = getTokenDescriptor(type);
        return desc != null? desc.getValue() : null;
    }
}