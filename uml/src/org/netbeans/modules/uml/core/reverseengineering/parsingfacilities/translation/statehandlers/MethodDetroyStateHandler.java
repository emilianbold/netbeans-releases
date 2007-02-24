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
 * File       : MethodDetroyStateHandler.java
 * Created on : Dec 10, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers;

import org.dom4j.Node;

import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParserEventDispatcher;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.Identifier;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.InstanceInformation;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.SymbolTable;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenDescriptor;

/**
 * @author Aztec
 */
public class MethodDetroyStateHandler extends MethodDetailStateHandler
{
    private Identifier  m_InstanceName = new Identifier();
    private boolean     m_FoundInstanceName;
    private int         m_IdentifierCount;

    public MethodDetroyStateHandler(String language)
    {
        super(language);
        m_FoundInstanceName = false;
        m_IdentifierCount = 0;
    }

    /**
     * Create a new state handler to be added to the state mechanism.  If the
     * state is not a state that is being processed then a new state handler is
     * not created.  The states of interest is <code>Expression List</code>
     *
     * @param stateName [in] The state name.
     * @param language [in] The langauge being processed.
     *
     * @return
     */
    public StateHandler createSubStateHandler(String stateName, String language)
    {
        StateHandler retVal = null;

        if("Identifier".equals(stateName))
        {
            if(!m_FoundInstanceName)
            {
                retVal = this;
                m_IdentifierCount++;
            }
        }
        return retVal;
    }

    /**
     * Initialize the state handler.  This is a one time initialization.
     */
    public void initialize()
    {
       // No valid implementation in the C++ code base.
    }

    /**
     * Process a new token.  The tokens that are processed are in the
     * context of an object creation.  The tokens of interest are
     * <code>Argument Start</code>, and <code>Argument End</code>
     *
     * @param pToken [in] The token to be processed.
     */
    public void processToken(ITokenDescriptor pToken, String language)
    {
        if(pToken == null) return;

        String type = pToken.getType();

        String value = pToken.getValue();

        if(!m_FoundInstanceName)
        {
            if("Identifier".equals(type) ||
                "Scope Operator".equals(type))
            {
                m_InstanceName.addToken(pToken);
            }
        }
    }

    /**
     * Notification that the a state has completed.
     *
     * @param stateName [in] The name of the state.
     */
    public void stateComplete(String stateName)
    {
        if("Identifier".equals(stateName))
        {
            m_IdentifierCount--;
            if(m_IdentifierCount <= 0)
            {
                InstanceInformation ref = retrieveInstance(m_InstanceName);

                Node pNode = getDOMNode();

                if(ref != null && pNode != null)
                {
                    if(!isWriteXMI())
                    {
                        sendEvent(ref, pNode);
                    }
                    else
                    {
                        writeXMI(ref, pNode);
                    }

                    getSymbolTable()
                        .removeInstance(m_InstanceName.getIdentifierAsSource());
                }
                m_FoundInstanceName = true;
            }
        }
    }

    /**
     * Retrieves the instance that is going to be the destroyed.
     *
     * @param identifier [in] The instance.
     *
     * @return
     */
    protected InstanceInformation retrieveInstance(Identifier identifier)
    {
        SymbolTable table = getSymbolTable();
        String name = identifier.getIdentifierAsSource();
        return table.findInstance(name);
    }

    /**
     * Sends a destroy event to all listeners.
     *
     * @param ref [in] The instance that is being destroyed.
     * @param pParent [in] The parent node that will recieve the data.
     */
    protected void sendEvent(InstanceInformation ref, Node pParent)
    {
        if(pParent != null)
        {
            IUMLParserEventDispatcher pDispatcher =
                getEventDispatcher();

            ref.sendDestroy(pParent, pDispatcher);
        }
    }

    /**
     * writes a destroy event data to the parent node.
     *
     * @param ref [in] The instance that is being destroyed.
     * @param pParent [in] The parent node that will recieve the data.
     */
    protected void writeXMI(InstanceInformation ref, Node pParent)
    {
        if(pParent != null)
        {
            Node pData = ref.generateDestroyXMI(pParent);
        }
    }
}
