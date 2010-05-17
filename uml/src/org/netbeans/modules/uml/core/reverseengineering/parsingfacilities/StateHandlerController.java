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
