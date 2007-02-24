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
