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
 * File       : AliasedTypedStateHandler.java
 * Created on : Dec 8, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers;

import org.dom4j.Node;

import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParserEventDispatcher;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.Identifier;
import org.netbeans.modules.uml.core.reverseengineering.reframework.ClassEvent;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IClassEvent;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenDescriptor;

/**
 * @author Aztec
 */
public class AliasedTypeStateHandler extends TopLevelStateHandler
{
    Identifier m_TypeIdentifier = new Identifier();

    public AliasedTypeStateHandler(String language)
    {
        super(language);
    }

    /**
     * Create a new state handler to be added to the state mechanism.  If the
     * state is not a state that is being processed then a new state handler is
     * not created.  The states of interest is <code>Expression List</code>
     *
     * @param stateName [in] The state name.
     * @param language [in] The langauge being processed.
     *
     * @return The hander for the new state.
     */
    public StateHandler createSubStateHandler(String stateName, String val)
    {
        StateHandler retVal = null;
        if("Type".equals(stateName))
        {
            // The attribute state handler will handle the type state itself.
            // So, I want to return this.
            retVal = this;
        }
        else if("Identifier".equals(stateName))
        {
            retVal = this;
        }
        return retVal;
    }

    /**
     * Initialize the state handler.  This is a one time initialization.
     */
    public void initialize()
    {
        Node pNode = getDOMNode();
        if(pNode == null)
        {
            super.createTopLevelNode("UML:AliasedType");
        }
        else
        {
            Node pNewAliasedType = createNamespaceElement(pNode, "UML:AliasedType");

            if(pNewAliasedType != null)
            {
                setDOMNode(pNewAliasedType);
            }
        }
    }



    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers.IAliasedTypedStateHandler#processToken(org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenDescriptor, java.lang.String)
     */
    public void processToken(ITokenDescriptor pToken, String language)
    {
        if(pToken == null) return;

        String tokenType = pToken.getType();

        if("Name".equals(tokenType))
        {
            String name = pToken.getValue();
            handleName(pToken);
        }
        else if("Type Decoration".equals(tokenType))
        {
            String name = pToken.getValue();
            handleTypeDecoration(pToken);
        }
        else if("Identifier".equals(tokenType) ||
                "Primitive Type".equals(tokenType)||
                "Scope Operator".equals(tokenType))
        {
            m_TypeIdentifier.addToken(pToken);

            if("Primitive Type".equals(tokenType))
            {
                createTokenDescriptor("IsPrimitive", -1, -1, -1, "true", -1);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers.IAliasedTypedStateHandler#stateComplete(java.lang.String)
     */
    public void stateComplete(String stateName)
    {
        if("Alias Declaration".equals(stateName))
        {
           writeStartToken();
           sendEvent();
        }
        if("Type".equals(stateName))
        {
           writeType();
        }
    }

    /**
     * Add the class name information to the XMI structure.
     *
     * @param pToken [in] The token that specifies the class name.
     */
    protected void handleName(ITokenDescriptor pToken)
    {
        if(pToken == null) return;

        String value = pToken.getValue();
        setNodeAttribute("aliasedName", value);

        long line = pToken.getLine();
        long col =  pToken.getColumn();
        long pos =  pToken.getPosition();
        long length = pToken.getLength();

        createTokenDescriptor("Name", line, col, pos, value, length);
    }

    /**
     * Add the Type Decoration information to the XMI structure.
     *
     * @param pToken [in] The token that specifies the class name.
     */
    protected void handleTypeDecoration(ITokenDescriptor pToken)
    {
        if(pToken == null) return;

        String value = pToken.getValue();

        setNodeAttribute("typeDecoration", value);

        long line = pToken.getLine();
        long col =  pToken.getColumn();
        long pos =  pToken.getPosition();
        long length = pToken.getLength();

        createTokenDescriptor("Decoration", line, col, pos, value, length);
    }

    /**
     * Sends the IClassEvent to all listeners.
     */
    protected void sendEvent()
    {
        IClassEvent pEvent = new ClassEvent();
        if(pEvent != null)
        {
            Node pNode = getDOMNode();

            if(pNode != null)
            {
                pEvent.setEventData(pNode);

                IUMLParserEventDispatcher pDispatcher = getEventDispatcher();

                if(pDispatcher != null)
                {
                    pDispatcher.fireClassFound("", pEvent, null);
                }
            }
        }
    }

    protected void writeType()
    {

        String value = m_TypeIdentifier.getIdentifierAsSource();

        String nameString = m_TypeIdentifier.getIdentifierAsUML();
        setNodeAttribute("actualType", nameString);

        long line = m_TypeIdentifier.getStartLine();
        long col = m_TypeIdentifier.getStartColumn();
        long pos = m_TypeIdentifier.getStartPosition();
        long length = m_TypeIdentifier.getLength();

        createTokenDescriptor("Type", line, col, pos, value, length);
    }
}
