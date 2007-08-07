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
 * File       : AttributeStateHandler.java
 * Created on : Dec 10, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers;

import org.dom4j.Node;

import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParserEventDispatcher;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.expression.Expression;
import org.netbeans.modules.uml.core.reverseengineering.reframework.AttributeEvent;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IAttributeEvent;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenDescriptor;

/**
 * @author Aztec
 */
public class AttributeStateHandler extends TypeElementStateHandler
{
    private Expression m_Expression = new Expression();
    private boolean m_InInitializer = false;
    private boolean m_PrimitiveType = false;
    private boolean defaultStatic = false;
    private boolean defaultFinal = false;

    public AttributeStateHandler(String language)
    {
        super(language, "Variable Definition");
        m_InInitializer = false;
        m_PrimitiveType = true;
    }
    
    public AttributeStateHandler(String language, String stateName)
    {
        super(language, stateName);
        m_InInitializer = false;
        m_PrimitiveType = true;
    }
    
    /**
     * Creates and returns a new state handler for a sub-state.  If the sub-state
     * is not handled then null is returned.  The attribute state of interest is
     * <code>Initializer</code>
     *
     * @param stateName [in] The name of the new state.
     * @param language [in] The language of the state.
     *
     * @return The handler for the sub-state, null if the state is not handled.
     */
    public StateHandler createSubStateHandler(String stateName, String language)
    {
        StateHandler retVal = null;

        if("Initializer".equals(stateName))
        {
            retVal = this;
            m_InInitializer = true;
        }
        else if(m_InInitializer)
        {
            m_Expression.addState(stateName, language);
            retVal = this;
        }
        else
        {
          retVal = super.createSubStateHandler(stateName, language);
        }

        return retVal;
    }

    /**
     * Used by TypeElementStateHandler to determine the type
     * of XMI fragment to create.  GetFeatureName returns the
     * XMI node name.
     *
     * @return The node name <code>UML:Attribute</code>
     */

    public String getFeatureName()
    {
        return "UML:Attribute";
    }

    /**
     * Initialize the state handler.  This is a one time initialization.
     */
    public void initialize()
    {
        super.initialize();

        setNodeAttribute("isLeaf", false);
        setNodeAttribute("ownerScope", "instance");

	if (defaultStatic) 
	{
	    setNodeAttribute("isStatic", true);
	}
	if (defaultFinal) 
	{
	    setNodeAttribute("isFinal", true);
	}
    }

    /**
     * The state handler is able to process the token.  Attribute tokens of
     * interest are <code>Statement Terminator</code> and
     * <code>Primitive Type</code>
     *
     * @param pToken [in] The token to process.
     */
    public void processToken(ITokenDescriptor pToken, String language)
    {
        if(pToken == null) return;

        if(m_InInitializer)
        {
            m_Expression.addToken(pToken, language);
        }
        else
        {
            String tokenType = pToken.getType();

            if("Statement Terminator".equals(tokenType))
            {
                handleEndPostion(pToken);
            }
            else if("Primitive Type".equals(tokenType)&&
                      getTypeState() == true)
            {
                m_PrimitiveType = true;
                super.processToken(pToken, language);
            }
            else
            {
                if(getTypeState())
                {
                    m_PrimitiveType = false;
                }
                super.processToken(pToken, language);
            }
        }
    }

    /**
     * Notification that the a state has completed.  All state clean up is
     * done this the StateComplete state.  The Attribute states of interest
     * is <code>Initializer</code> and <code>Type</code>.
     *
     * @param stateName [in] The name of the state.
     */
    public void stateComplete(String stateName)
    {
        if("Initializer".equals(stateName))
        {
            addInitializer();
            m_InInitializer = false;
        }
        else if(m_InInitializer)
        {
            m_Expression.endState(stateName);
        }
        else if("Type".equals(stateName))
        {
            if(isAggregation())
            {
                setNodeAttribute("AssociationType", "Aggregation");
            }
            else if(isComposition())
            {
                setNodeAttribute("AssociationType", "Composition");
            }
            super.stateComplete(stateName);
        }
        else if( "Variable Definition".equals(stateName))
        {
            // We've found the Attribute.
            sendOnAttributeFoundEvent();
            super.stateComplete(stateName);
        }
        else
        {
            super.stateComplete(stateName);
        }
    }



    /**
     * Processes the attributes initializer and adds the XMI
     * that represent the attributes initializer.
     */
    protected void addInitializer()
    {
        // Create the node that will contains the  initializer value.
        // The XMI Node structure is UML:Attribute.default/UML:Expression/UML:Expression.body
        Node pDefault = createNamespaceElement("UML:Attribute.default");
        if(pDefault != null)
        {
            Node pExpression = createNamespaceElement(pDefault, "UML:Expression");
            if(pExpression != null)
            {
                Node pExpressionBody = createNamespaceElement(pExpression, "UML:Expression.body");
                if(pExpressionBody != null)
                {
                    String value = m_Expression.toString();
                    pExpressionBody.setText(value);
                }
            }
        }

        // Now set the token descriptor.
        long startPos = m_Expression.getStartPosition();
        long length   = m_Expression.getEndPosition() - startPos;
        createTokenDescriptor("InitialValue", -1, -1,
                                   m_Expression.getStartPosition(),
                                   m_Expression.toString(),
                                   length);
    }

    /**
     * Specifies if the attribute is an aggregation association.  In java
     * all attributes that are of primitive data types are aggregation
     * relationships.
     *
     * @return true or false.
     */
    protected boolean isAggregation()
    {
        return !m_PrimitiveType;
    }

    /**
     * Specifies if the attribute is an composition association.  In java
     * there are no composition associations.
     *
     * @return true or false.
     */
    protected boolean isComposition()
    {
        return false;
    }

    /**
     * Notifies listeners (who are listening for UML atomic events) that
     * an Attribute has been found.
     */
    protected void sendOnAttributeFoundEvent()
    {

        IAttributeEvent pEvent = new AttributeEvent();
        if(pEvent != null)
        {
            Node pNode = getDOMNode();

            if(pNode != null)
            {
                pEvent.setEventData(pNode);

                IUMLParserEventDispatcher pDispatcher =
                    getEventDispatcher();

                if(pDispatcher != null)
                {
                    pDispatcher.fireAttributeFound(pEvent, null);
                }
            }
        }
    }


    void setDefaultStatic(boolean isStatic) 
    {
	defaultStatic = isStatic;
    }

    void setDefaultFinal(boolean isFinal) 
    {
	defaultFinal = isFinal;
    }


}
