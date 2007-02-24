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



package org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers;

import org.dom4j.Node;

import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.Identifier;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenDescriptor;
import org.netbeans.modules.uml.core.support.umlsupport.Log;

/**
 */
public class EnumStateHandler extends ClassStateHandler
{

	private Identifier m_Type = null;
	private boolean m_DiscoverType = false;

    public EnumStateHandler(String language, String packageName)
    {
        this(language, packageName, false);
    }
    
    public EnumStateHandler(String language, String packageName, boolean isInner)
    {
        super(language, packageName, isInner);
        m_DiscoverType = false;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers.StateHandler#initialize()
     */
    public void initialize()
    {
        Node pNode = getDOMNode();
        if(pNode == null)
        {
            createTopLevelNode("UML:Enumeration");
            setNodeAttribute("visibility", "public");
        }
        else
        {
            // The inner class token descriptor is really a 
            // sub node of the UML:Class XML node.  Therefore, it must
            // be a sub node of UML:Element.ownedElement.
            Node pClassifierFeature = ensureElementExists(pNode, 
                                         "UML:Element.ownedElement", 
                                         "UML:Element.ownedElement");

            if(pClassifierFeature != null)
            {
                Node pNewClass = createNamespaceElement(pClassifierFeature, 
                                "UML:Enumeration");
                setNodeAttribute("visibility", "public");

                if(pNewClass != null)
                {
                    setDOMNode(pNewClass);
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers.StateHandler#createSubStateHandler(java.lang.String, java.lang.String)
     */
    public StateHandler createSubStateHandler(String stateName, String language)
    {
        StateHandler retVal = null;

        if(stateName.equals("Enum Member"))
        {
            EnumMemberStateHandler enumMemberHandler 
                = new EnumMemberStateHandler(language, stateName);
            
            String type = "int";
            if((m_Type != null) && (m_Type.getLength() > 0))
            {
               type = m_Type.getIdentifierAsUML();
            }

            enumMemberHandler.setMemberType(type);

            retVal = enumMemberHandler;

            Node pEnumNode = getDOMNode();
            if(pEnumNode != null)
            {
                retVal.setDOMNode(pEnumNode);
            }
        }
        else if(stateName.equals("Type"))
        {
           m_DiscoverType = true;
           retVal = this;
        }
        else
        {
           retVal = super.createSubStateHandler(stateName, language);
        }
        return retVal;
    }

    public void processToken(ITokenDescriptor pToken, String language)
    {
    
    	try
		{  
    		if(pToken != null)
            {	
       	    	String tokenType = pToken.getType();
       	    	if( ((tokenType.equals("Identifier")) || 
                    (tokenType.equals("Primitive Type"))||
                    (tokenType.equals("Scope Operator"))) && 
                    (m_DiscoverType == true))
       	    	{
       	    		m_Type.addToken(pToken);
       	    	}
       	    	else
       	    	{
       	    		super.processToken(pToken, language);
       	    	}
            }    
		}
    	catch(Exception e)
		{
    		Log.stackTrace(e);
		}
    }

}
