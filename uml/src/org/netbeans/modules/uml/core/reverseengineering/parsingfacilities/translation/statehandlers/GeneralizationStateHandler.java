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
 * File       : GeneralizationStateHandler.java
 * Created on : Dec 11, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers;

import org.dom4j.Element;
import org.dom4j.Node;

import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParserEventDispatcher;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.Identifier;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IREGeneralization;
import org.netbeans.modules.uml.core.reverseengineering.reframework.REGeneralization;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenDescriptor;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;

/**
 * @author Aztec
 */
public class GeneralizationStateHandler extends StateHandler
{
    private Identifier m_SuperClass = new Identifier();
    private int        m_NestedLevel = 0;
    private long       m_EndPosition = -1;
    private long       m_EndLine = -1;
    private long       m_EndColumn = -1;
    private Node       m_pClassNode;
    
    public StateHandler createSubStateHandler(String stateName, String language) 
    {
        StateHandler retVal = null;
        
        if("Identifier".equals(stateName))
        {
            // Check if we found a new identifier.  If the nesting level is greater
            // than zero we are current processing an identifier.
            if(m_NestedLevel <= 0)
            {
                m_SuperClass.clear();
            }

            m_NestedLevel++;

            retVal = this;
        }
        else if("Template Instantiation".equals(stateName))
        {
            retVal = new TemplateInstantiationStateHandler();

            Node pNode = getDOMNode();

            if(pNode != null)
            {
                 retVal.setDOMNode(pNode);
            }
        }
        return retVal;
    }
    
    public void initialize() 
    {
        m_pClassNode = getDOMNode();

        // The generalization token descriptor is really a 
        // sub node of the UML:Class XML node.  Therefore, I 
        // will create the TGeneralization under the passed
        // in node.  The TGeneralization node will then be
        // passed to StateHandler to use in the helper methods.
        Node pDescriptors = ensureElementExists("TokenDescriptors", 
                                                "TokenDescriptors");

        if(pDescriptors != null)
        {
            Node pRelationship = ensureElementExists(pDescriptors, 
                                   getRelationshipGroupTagName(), 
                                   getRelationshipGroupTagName());

            if(pRelationship != null)
            {
                setDOMNode(pRelationship);
            }
        }
    }
    
    public void processToken(ITokenDescriptor pToken, String language) 
    {
        if(pToken == null) return;
        
        String tokenType = pToken.getType();
        
        if("Keyword".equals(tokenType))
        {         
            handleRelationshipKeyword(pToken);
        }
        else if( ("Identifier".equals(tokenType)) ||
                 ("Scope Operator".equals(tokenType)) )
        {
             m_SuperClass.addToken(pToken);
        }
    }
    
    public void stateComplete(String stateName) 
    {
        if(m_NestedLevel > 0)   
        {
            m_NestedLevel--;

            // Now if I am not still in a nested state I must move back to the 
            // general state.
            if(m_NestedLevel == 0)
            {
                createRelationship(getRelationshipTagName(), m_SuperClass);
            }
        }
        else
        {
            updateEndPosition();
        }
    }
    
    public String getRelationshipTagName() 
    {
        return "SuperClass";
    }

    public String getRelationshipGroupTagName() 
    {
        return "TGeneralization";
    }

    public Identifier getIdentifier() 
    {
        return m_SuperClass;
    }
    
    protected void handleRelationshipKeyword(ITokenDescriptor pToken) 
    {
        if(pToken == null) return;
        
        long line = pToken.getLine();
        long column = pToken.getColumn();
        long position = pToken.getPosition();
        long length = pToken.getLength();
        String value = pToken.getValue();
        
        createDescriptor("Keyword", line, column, position, value, length);
    }

    protected void createRelationship(String tagName, Identifier generalization) 
    {
        Node pRelationshipNode = getDOMNode();
        if(pRelationshipNode != null)
        {
            Node pGeneralization = XMLManip.createElement((Element)pRelationshipNode, 
                                                            tagName);
      
            if(pGeneralization != null)
            {
                Element element = (pGeneralization instanceof Element)?
                                    (Element)pGeneralization : null;
                if(element != null)
                {
                    m_EndLine = generalization.getStartLine();
                    m_EndColumn = generalization.getStartColumn();
                    long pos = generalization.getStartPosition();
                    long length = generalization.getLength();
                    String value = generalization.getIdentifierAsUML();
            
                    XMLManip.setAttributeValue(element,"line", Long.toString(m_EndLine)); 
                    XMLManip.setAttributeValue(element,"column", Long.toString(m_EndColumn)); 
                    XMLManip.setAttributeValue(element,"position", Long.toString(pos));
                    XMLManip.setAttributeValue(element,"value", value); 
                    XMLManip.setAttributeValue(element,"length", Long.toString(length)); 

                    m_EndPosition = generalization.getEndPosition();
                }
            }
        }
        sendAtomicEvent(pRelationshipNode);
    }

    protected void updateEndPosition() 
    {
        if(m_pClassNode != null && 
           m_EndLine >= 0 && 
           m_EndColumn >= 0 && 
           m_EndPosition >= 0)
        {
            Node pEndPos = XMLManip.selectSingleNode(m_pClassNode, 
                "TokenDescriptors/TDescriptor[@type=\"ClassHeadEndPosition\"]");
         
            if(pEndPos != null)
            {
                Element pElement = (pEndPos instanceof Element)?
                                     (Element)pEndPos : null;
                if(pElement != null)
                {
                    XMLManip.setAttributeValue(pElement,"line", 
                                                Long.toString(m_EndLine)); 
                    XMLManip.setAttributeValue(pElement,"column", 
                                                Long.toString(m_EndColumn)); 
                    XMLManip.setAttributeValue(pElement,"position", 
                                                Long.toString(m_EndPosition));  
                }
            }
            else
            {
                createTokenDescriptor(m_pClassNode, 
                                            "ClassHeadEndPosition", 
                                            m_EndLine,
                                            m_EndColumn,
                                            m_EndPosition,
                                            "", 
                                            0);
            }
        }
    }

    protected void sendAtomicEvent(Node pRelationshipNode) 
    {
        if(pRelationshipNode == null) return;
        
        IREGeneralization pEvent = new REGeneralization();

        if(pEvent != null)
        {
            pEvent.setDOMNode(pRelationshipNode);
         
            IUMLParserEventDispatcher pDispatcher = getEventDispatcher();
        
            if(pDispatcher != null)
            {
                pDispatcher.fireGeneralizationFound(pEvent, null);
            }
        }
    }
}
