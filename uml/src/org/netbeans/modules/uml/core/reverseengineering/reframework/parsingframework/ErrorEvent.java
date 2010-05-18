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
