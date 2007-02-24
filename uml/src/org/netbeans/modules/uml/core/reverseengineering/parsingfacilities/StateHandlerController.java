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


package org.netbeans.modules.uml.core.reverseengineering.parsingfacilities;

import java.util.Stack;

import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.expression.ExpressionFactory;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers.StateHandler;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenDescriptor;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 */
public class StateHandlerController
{
    public StateHandlerController()
    {
    }
    
    public StateHandlerController(boolean keepRoots)
    {
        m_KeepRoots = keepRoots;
    }
    
    public void addStateHandler(String stateName, String language)
    {
        HandlerData data = new HandlerData();
        data.stateName = stateName;
        data.language = language;
        if (m_StateHandlers.size() > 0)
        {
            HandlerData handler = m_StateHandlers.peek();
            if (handler.handler != null)
            {
                data.handler = 
                    handler.handler.createSubStateHandler(stateName, language);
                // I do not want to initialize the handler twice.  So, if the returned
                // handler is the same as the current handler (in other words 
                // CreateSubStateHandler returned the THIS pointe), then do not initialize
                // the handler.  The handler has already been initialized.
                if (data.handler != handler.handler && data.handler != null)
                    data.handler.initialize();
            }
            m_StateHandlers.push(data);
        }
        else
        {
            data.handler = 
                ExpressionFactory.getExpressionForState(stateName, language);
            if (data.handler != null)
            {    
                data.handler.initialize();
                m_StateHandlers.push(data);
            }
        }
        
        // If the controller is to manange the states then store them in a 
        // StateHandlerList.
        if (getKeepRoots() && data.handler != null)
            m_Roots.add(data.handler);
    }

    /**
     * Removes a state handler from the list of state handlers.
     *
     * @param stateName [in] The name of the state.
     */
    public void removeStateHandler(String statename)
    {
        if (m_StateHandlers.size() > 0)
        {
            // Remove the hander from the stack and notify the handler that
            // the state has ended.
            HandlerData oldData = m_StateHandlers.pop();
            if (oldData.handler != null)
                oldData.handler.stateComplete(statename);
        }
    }

    /**
     * Process the token that has been discovered by the parser.
     * 
     * pToken [in] The token to be processed.
     */
    public void processToken(ITokenDescriptor token, String language)
    {
        if (m_StateHandlers.size() > 0)
        {
            HandlerData data = m_StateHandlers.peek();
            if (data.handler != null)
                data.handler.processToken(token, language);
        }
    }

    /**
     * Retrieves the current state handler.  The current state
     * handler is responsible for all states and tokens.
     *
     * @return The current state handler, NULL may also be 
     *         returned when there is no active state handler.
     */
    public StateHandler getCurrentState()
    {
        return m_StateHandlers.size() > 0 ?
                            m_StateHandlers.peek().handler 
                          : null;
    }

    /**
     * Get the status of the KeepRoots state.  When in the KeepRoots
     * state the root state handler will not be removed.
     *
     * @return 
     */
    public boolean getKeepRoots()
    {
        return m_KeepRoots;
    }

    /**
     * Retrieve all of the root state handlers.
     *
     * @return A list of root state handlers.
     */
    public ETList<StateHandler> getRootStateHandlers()
    {
        return m_Roots;
    }

    /**
     * Retrieve the number of state handlers that are being controlled.
     *
     * @return The number of state handlers.
     */
    public int getNumberOfStateHandlers()
    {
        return m_StateHandlers.size();
    }
    
    private static class HandlerData
    {
        public StateHandler handler;
        public String       stateName;
        public String       language;
    }
    
    private Stack<HandlerData>   m_StateHandlers = new Stack<HandlerData>();
    private boolean             m_KeepRoots;
    private ETList<StateHandler> m_Roots         = 
                                                new ETArrayList<StateHandler>();
}