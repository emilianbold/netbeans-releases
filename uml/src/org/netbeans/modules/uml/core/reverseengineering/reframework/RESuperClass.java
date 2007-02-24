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

import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;

/**
 */
public class RESuperClass implements IRESuperClass
{
    private Node m_Data;

    /**
     * Get the XML DOM node that contains the interface information.
     * @param pVal [out] The XML DOM node that contains the data.
     */
    public Node getDOMNode()
    {
        return m_Data;
    }

    /**
     * Set the XML DOM node that contains the interface information.
     * @param pVal [out] The XML DOM node that contains the data.
     */
    public void setDOMNode(Node value)
    {
        m_Data = value;
    }

    /**
     * Retrieve the name of the implemented interface.
     * @param pVal [out] The name of the interface.
     */
    public String getName()
    {
        return XMLManip.getAttributeValue(m_Data, "value");
    }

    /** 
     * Retrieve the line number that contains the declaration.
     * @param pVal [out] The line number.
     */
    public int getLine()
    {
        return XMLManip.getAttributeIntValue(m_Data, "line");
    }

    /**
     * Retrieve the column that that contains the declaration.
     * @param pVal [out] The column.
     */
    public int getColumn()
    {
        return XMLManip.getAttributeIntValue(m_Data, "column");
    }

    /**
     * Retrieve the stream position that contains the declaration.
     * @param pVal [out] The stream position.
     */
    public int getPosition()
    {
        return XMLManip.getAttributeIntValue(m_Data, "position");
    }
}
