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
 * File       : ErrorEvent.java
 * Created on : Oct 23, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework;

/**
 * @author Aztec
 */
public class ErrorEvent implements IErrorEvent
{
    String  m_Filename;
    String  m_ErrorMessage;
    int     m_ColumnNumber;
    int     m_LineNumber;

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IErrorEvent#getColumnNumber()
     */
    public int getColumnNumber()
    {
        return m_ColumnNumber;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IErrorEvent#getErrorMessage()
     */
    public String getErrorMessage()
    {
        return m_ErrorMessage;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IErrorEvent#getFilename()
     */
    public String getFilename()
    {
        return m_Filename;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IErrorEvent#getFormattedMessage()
     */
    public String getFormattedMessage()
    {
        StringBuffer errMsg = m_Filename != null? 
                                  new StringBuffer(m_Filename) :
                                  new StringBuffer();
        if(m_LineNumber != -1) 
        {
            errMsg.append("(line=");
            errMsg.append(Integer.toString(m_LineNumber));
            errMsg.append(", col=");
            errMsg.append(Integer.toString(m_ColumnNumber));
            errMsg.append(") : ");
        }
        else
            errMsg.append(":");
        errMsg.append(m_ErrorMessage);
        
        return errMsg.toString();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IErrorEvent#getLineNumber()
     */
    public int getLineNumber()
    {
        return m_LineNumber;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IErrorEvent#setColumnNumber(int)
     */
    public void setColumnNumber(int colNum)
    {
        m_ColumnNumber = colNum;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IErrorEvent#setErrorMessage(java.lang.String)
     */
    public void setErrorMessage(String message)
    {
        m_ErrorMessage = message;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IErrorEvent#setFilename(java.lang.String)
     */
    public void setFilename(String fileName)
    {
        m_Filename = fileName;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IErrorEvent#setLineNumber(int)
     */
    public void setLineNumber(int lnNum)
    {
        m_LineNumber = lnNum;
    }

}
