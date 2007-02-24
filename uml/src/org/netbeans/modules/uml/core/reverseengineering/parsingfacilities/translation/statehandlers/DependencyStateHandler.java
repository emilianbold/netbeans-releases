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
 * File       : DependencyStateHandler.java
 * Created on : Dec 10, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers;

import org.dom4j.Node;

import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParserEventDispatcher;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.Identifier;
import org.netbeans.modules.uml.core.reverseengineering.reframework.DependencyEvent;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IDependencyEvent;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenDescriptor;

/**
 * @author Aztec
 */
public class DependencyStateHandler extends TopLevelStateHandler
{
    Identifier m_DependencyName = new Identifier();
    Identifier m_FullName = new Identifier();
    boolean m_IsClassDependency = true;
    int m_NestedLevel;

    public DependencyStateHandler(String language)
    {
        super(language);
        m_IsClassDependency = true;
        m_NestedLevel = 0;
    }

    /**
     * Create a new state handler to be added to the state mechanism.  If the
     * state is not a state that is being processed then a new state handler is
     * not created.
     *
     * @param stateName [in] The state name.
     * @param language [in] The langauge being processed.
     *
     * @return The handler for the state.
     */
    public StateHandler createSubStateHandler(String stateName,
                                                String val)
    {
        StateHandler retVal = null;

        if("Identifier".equals(stateName))
        {
            m_NestedLevel++;
            retVal = this;
        }
        return retVal;
    }

    /**
     * Initializes the state handler.  The jump action XMI node is
     * initializes.
     */
    public void initialize()
    {
        createTopLevelNode("UML:Dependency");
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers.IDependencyStateHandler#isClassDependency()
     */
    public boolean isClassDependency()
    {
        return m_IsClassDependency;
    }

    /**
     * Process a new token.  The tokens that are processed are in the
     * context of an object creation.
     *
     * @param pToken [in] The token to be processed.
     */
    public void processToken(ITokenDescriptor pToken, String language)
    {
       if(pToken == null) return;

       String tokenType = pToken.getType();

        if("Keyword".equals(tokenType))
        {
            handleStartPosition(pToken);
            handleKeyword(pToken);
            handleFilename(pToken);
        }
        else if (("Identifier".equals(tokenType)) ||
                 ("Scope Operator".equals(tokenType)) )
        {
            // The FullName will also contain the OnDemand Operator
            // while the Dependency name will only contain the
            // name of the dependent package or class.
            m_DependencyName.addToken(pToken);
            m_FullName.addToken(pToken);
        }
        else if("OnDemand Operator".equals(tokenType))
        {
            setIsClassDependency(false);
            m_FullName.addToken(pToken);
        }
        else if("Statement Terminator".equals(tokenType))
        {
            handleTerminator(pToken);
            handleEndPostion(pToken);
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers.IDependencyStateHandler#setIsClassDependency(boolean)
     */
    public void setIsClassDependency(boolean newVal)
    {
        m_IsClassDependency = newVal;
    }

    /**
     * Notification that the a state has completed.
     *
     * @param stateName [in] The name of the state.
     */
    public void stateComplete(String stateName)
    {
        if(m_NestedLevel == 0)
        {
            IDependencyEvent pEvent = buildDependencyEvent();
            IUMLParserEventDispatcher pDispatcher = getEventDispatcher();

            if(pDispatcher != null)
            {
                pDispatcher.fireDependencyFound( "", pEvent, null);
            }
        }
        else
        {
            m_NestedLevel--;

            // Now if I am not still in a nested state I must move back to the
            // general state.
            if(m_NestedLevel == 0)
            {
                updateName();
            }
        }
    }

    protected IDependencyEvent buildDependencyEvent() {
        String value = isClassDependency() ? "true" : "false";
        createTokenDescriptor("Class Dependency", -1, -1, -1, value, 0);

        IDependencyEvent pEvent = new DependencyEvent();
        if(pEvent != null)
        {
            Node pNode = getDOMNode();

            if(pNode != null)
            {
                pEvent.setEventData(pNode);
                
            }
        }
        
        return pEvent;
    }

    /**
     * Add the dependency name information to the XMI structure.
     *
     * @param pToken [in] The token that specifies the class name.
     */
    protected void updateName()
    {
        String supplierName = m_DependencyName.getIdentifierAsUML();
        setNodeAttribute("name", supplierName);

        String value = m_FullName.getIdentifierAsSource();
        createTokenDescriptor("Name",
                                m_FullName.getStartLine(),
                                m_FullName.getStartColumn(),
                                m_FullName.getStartPosition(),
                                value,
                                m_FullName.getLength());

        setNodeAttribute("supplier", supplierName);
    }
}
