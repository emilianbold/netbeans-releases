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
 * File       : TokenDescriptor.java
 * Created on : Oct 23, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework;

import java.util.HashMap;

/**
 * @author Aztec
 */
public class TokenDescriptor implements ITokenDescriptor
{
    private HashMap<String, String> m_Properties = new HashMap<String, String>();
    private int m_Length;
    private String m_Value;
    private String m_Type;
    private long m_Position;
    private int m_Column;
    private int m_Line;

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenDescriptor#addProperty(java.lang.String, java.lang.String)
     */
    public void addProperty(String name, String value)
    {
        if(name != null && value != null)
            m_Properties.put(name, value);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenDescriptor#getColumn()
     */
    public int getColumn()
    {
        return m_Column;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenDescriptor#getLength()
     */
    public int getLength()
    {
        return m_Length;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenDescriptor#getLine()
     */
    public int getLine()
    {
        return m_Line;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenDescriptor#getPosition()
     */
    public long getPosition()
    {
        return m_Position;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenDescriptor#getProperty(java.lang.String)
     */
    public String getProperty(String name)
    {
        if(name == null) return null;
        
        return m_Properties.get(name);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenDescriptor#getType()
     */
    public String getType()
    {
        return m_Type;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenDescriptor#getValue()
     */
    public String getValue()
    {
        return m_Value;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenDescriptor#setColumn(int)
     */
    public void setColumn(int value)
    {
        m_Column = value;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenDescriptor#setLength(int)
     */
    public void setLength(int value)
    {
        m_Length = value;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenDescriptor#setLine(int)
     */
    public void setLine(int value)
    {
        m_Line = value;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenDescriptor#setPosition(int)
     */
    public void setPosition(long value)
    {
        m_Position = value;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenDescriptor#setType(java.lang.String)
     */
    public void setType(String value)
    {
        m_Type = value;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenDescriptor#setValue(java.lang.String)
     */
    public void setValue(String value)
    {
        m_Value = value;
    }

}
