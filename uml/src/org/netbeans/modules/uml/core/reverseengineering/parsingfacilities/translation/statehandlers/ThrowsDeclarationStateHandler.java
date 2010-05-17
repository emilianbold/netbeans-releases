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
 * File       : ThrowsDeclarationStateHandler.java
 * Created on : Dec 11, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers;

import org.dom4j.Element;
import org.dom4j.Node;

import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.Identifier;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenDescriptor;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;

/**
 * @author Aztec
 */
public class ThrowsDeclarationStateHandler extends StateHandler
{
    private int         m_IndentifierLevel;
    private Identifier  m_ExceptionName = new Identifier();
    private Node        m_pOwnerNode;
    private Node        m_pOperationNode;   
    private long        m_EndPosition = -1;
    private long        m_EndLine = -1;
    private long        m_EndColumn = -1;
    
    public StateHandler createSubStateHandler(String stateName, String language) 
    {
        StateHandler retVal = this;
        
        if("Identifier".equals(stateName))
        {
            // Check if we found a new identifier.  If the nesting level is greater
            // than zero we are current processing an identifier.
            if(m_IndentifierLevel <= 0)
            {
                m_ExceptionName.clear();
            }    

            m_IndentifierLevel++;

            retVal = this;
        }
        return retVal;
    }
    
    public void initialize() 
    {
        m_pOperationNode = getDOMNode();

        if(m_pOperationNode != null)
        {
            m_pOwnerNode = ensureElementExists(m_pOperationNode,
                                         "UML:Element.ownedElement", 
                                         "UML:Element.ownedElement");
        }
    }
    
    public void processToken(ITokenDescriptor pToken, String language) 
    {
        if(pToken == null) return;
        
        String tokenType = pToken.getType();
        
        if("Keyword".equals(tokenType))
        {         
            handleKeyword(pToken);

            long pos    = pToken.getPosition();
            long length = 0;
            
            m_EndLine = pToken.getLine();
            
            // ANTLR lines numbers are one based.  We need them to be zero based.
            m_EndLine--;
            
            m_EndColumn = pToken.getColumn();

            length = pToken.getLength();
            m_EndPosition = pos + length + 1;
        }
        else if("Identifier".equals(tokenType) ||
                 "Scope Operator".equals(tokenType))
        {
            m_ExceptionName.addToken(pToken);
        }
    }
    
    public void stateComplete(String stateName) 
    {
        if(m_IndentifierLevel > 0)   
        {
           m_IndentifierLevel--;

           // Now if I am not still in a nested state I must move back to the 
           // general state.
           if(m_IndentifierLevel == 0)
           {
                m_EndPosition = m_ExceptionName.getEndPosition();
                m_EndLine     = m_ExceptionName.getStartLine();
                m_EndColumn   = m_ExceptionName.getStartColumn();
                addException();
           }
        }
        else
        {
            if(m_pOperationNode != null)
            {
                updateEndPosition();
            }
        }
    }
    
    protected void addException() 
    {
        if(m_pOwnerNode != null && m_ExceptionName != null)
        {
            Node pException = createNamespaceElement(m_pOwnerNode,"UML:Exception");
         
           if(pException != null)
           {
                setNodeAttribute(pException, 
                                        "name", 
                                        m_ExceptionName.getIdentifierAsUML());
                createTokenDescriptor(pException,
                                        "Name", 
                                        m_ExceptionName.getStartLine(), 
                                        m_ExceptionName.getStartColumn(), 
                                        m_ExceptionName.getStartPosition(), 
                                        m_ExceptionName.getIdentifierAsSource(), 
                                        m_ExceptionName.getLength());
            }
        }
    }

    protected void updateEndPosition() 
    {
        if(m_pOperationNode != null)
        {
            Node pEndPos = XMLManip.selectSingleNode(m_pOperationNode, 
                            "TokenDescriptors/TDescriptor[@type=\"OpHeadEndPosition\"]");
         
            if(pEndPos != null)
            {
                Element pElement = (pEndPos instanceof Element)?
                                    (Element)pEndPos : null;
                if(pElement != null)
                {
                    XMLManip.setAttributeValue(pElement,"line", Long.toString(m_EndLine)); 
                    XMLManip.setAttributeValue(pElement,"column", Long.toString(m_EndColumn)); 
                    XMLManip.setAttributeValue(pElement,"position", Long.toString(m_EndPosition));  
                }
            }
            else
            {
                createTokenDescriptor(m_pOperationNode, 
                                        "OpHeadEndPosition", 
                                        m_EndLine,
                                        m_EndColumn,
                                        m_EndPosition,
                                        "", 
                                        0);
            }
        }
    }    
}
