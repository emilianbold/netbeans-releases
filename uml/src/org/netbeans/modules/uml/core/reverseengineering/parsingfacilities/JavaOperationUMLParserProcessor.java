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

import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers.StateHandler;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers.StatementFactory;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IStateFilter;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IStateListener;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IStatePayload;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenDescriptor;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenFilter;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenProcessor;
import org.netbeans.modules.uml.core.support.umlsupport.Log;

/**
 */
public class JavaOperationUMLParserProcessor
        implements
            IJavaOperationUMLParserProcessor,
            IStateFilter,
            IStateListener,
            ITokenProcessor,
            ITokenFilter,
            IOperationParserOptionsHandler
{
    /**
     * ProcessState determines if a state is to be filtered or not.
     * 
     * @param stateName [in] The name of the state.
     * @param pVal [out] True if the state is to be processed, false otherwise.
     */ 
    public boolean processState(String stateName, String language)
    {
        return true;
    }

    /**
     * The OnBeginState event is fired when the state of the parser has changed.
     * A new state can begin while still in a state."
     * 
     * @param stateName [in] The name of the state.
     * @param payload [in] Extra data.
     */
    public void onBeginState(String stateName, String language,
            IStatePayload Payload)
    {
        addStateHandler(stateName, language);
    }

    /**
     * The OnEndState event will be fired when exiting a state.
     * 
     * @param stateName [in] The name of the state.
     */
    public void onEndState(String stateName)
    {
        removeStateHandler(stateName);
    }
    
    private static int indent = 0;
    private final boolean DEBUG = false ;
    
    //used only debugging parser tree, or reacting to it.
    private void echo (String s) {
        if (DEBUG)
            System.out.println (s);
    }
    
    //used only debugging parser tree, or reacting to it.
    private synchronized String getIndent(int n) {
        StringBuffer sb = new StringBuffer(n);
        for (int i=0; i<n; i++)
            sb.append("   ") ;
        return sb.toString() ;
    }
    
    //used only debugging parser tree, or reacting to it.
    private synchronized int addIndent() {
        return indent++;
    }
    //used only debugging parser tree, or reacting to it.
    private synchronized int removeIndent() {
        return --indent;
    }
    
    protected void addStateHandler(String stateName, String language)
    {
        OperationHandlerData data = new OperationHandlerData();
        data.stateName = stateName;
        if (m_StateHandlers.size() > 0)
        {
            OperationHandlerData handler = m_StateHandlers.peek();
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
            
            // I always want to add the state handler to the list.  Even
            // if it is NULL.  Pushing a NULL state handler on the stack
            // is a way to filter out an entire state (and all of the 
            // tokens in the state).
            
            //debugging statement - only effective if local DEBUG is true. Used to echo 
            //parsing structure.
            echo(getIndent(addIndent())+ "<"+ stateName +" handler="+ getName(data)+">" );
            m_StateHandlers.push( data );
        }
        else
        {
            data.handler = StatementFactory.retrieveStatementHandler(
                    stateName, language, getOptions(), m_SymbolTable);
            
            // I do not want to initialize the handler twice.  So, if the returned
            // handler is the same as the current handler (in other words 
            // CreateSubStateHandler returned the THIS pointe), then do not initialize
            // the handler.  The handler has already been initialized.
            if (data.handler != null)
            {    
                data.handler.initialize();
            
                // I only want to add NON-NULL state handlers to the 
                // list.
                
                //debugging statement - only effective if local DEBUG is true. Used to echo 
                //parsing structure.
                echo(getIndent(addIndent()) +"<"+ stateName +" handler="+ getName(data) +">" );
                m_StateHandlers.push(data);
            }
        }
    }
    
    /**
     * Removes the current state from the controlled list of states.
     *
     * @param stateName [in] The name of the state.
     */
    protected void removeStateHandler(String stateName)
    {
        if (m_StateHandlers.size() > 0)
        {
            // Remove the hander from the stack and notify the handler that
            // the state has ended.
            OperationHandlerData oldData = m_StateHandlers.pop();
            
            //debugging statement - only effective if local DEBUG is true. Used to echo 
            //parsing structure.
            echo(getIndent(removeIndent()) + "</"+ stateName +">" );

            if (oldData.handler != null)
                oldData.handler.stateComplete(stateName);
        }
    }
    
    private String getName(OperationHandlerData data)
    {
    	String res = data.handler == null ? "null" : data.handler.getClass().getName();
        int dot = res.lastIndexOf('.');
        return res.substring(dot + 1);
    }
    
    /**
     * Removes all controlled states.  The removed states will be 
     * notified.
     */
    protected void cleanUpStateHandlers()
    {
        // Cycle through the states that have not been completed yet.  
        // Act as if they have beed completed.  This wil force all
        // data to be sent to the listeners.  This should only occur
        // if something terrible has happened while processing
        // the source file.  Example Stack Overflow error.
        while (m_StateHandlers.size() > 0)
        {    
            OperationHandlerData data = m_StateHandlers.peek();
            removeStateHandler(data.stateName);
        }
    }
    
    /**
     * Process the token that has been discovered by the parser.
     * 
     * pToken [in] The token to be processed.
     */
    public void processToken(ITokenDescriptor token, String language)
    {
        
        //the "echo" calls are debugging statements - only effective if local DEBUG is true. Used to echo 
        //parsing structure.
        if (m_StateHandlers.size() > 0)
        {
            try
            {
                OperationHandlerData data = m_StateHandlers.peek();
                if (data.handler != null) {
                    data.handler.processToken(token, language);
                    echo(getIndent(indent) + "<token value="+token.getValue() +" handler="+ getName(data)+"/>" );
                } else
                    echo(getIndent(indent) + "<token value="+token.getValue() +" handler="+ getName(data)+"/>" );
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        } else
            echo(getIndent(indent) + "<token value="+token.getValue() +" handler=NO_STATE_HANDLER/>" );
    }

    /**
     * The token filter implementation.  Determines if the specified
     * token should be processed by the token listener.
     *
     * @param tokenType [in] The token type.
     * @param stateName [in] The name of the current state.
     * @param language [in] The language that is being processed.
     * @param pVal [in] The result.
     */
    public boolean isTokenValid(String tokenType, String stateName,
            String language)
    {
        return true;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IOperationParserOptionsHandler#setOptions(org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IOpParserOptions)
     */
    public void setOptions(IOpParserOptions options)
    {
        m_ParserOptions = options;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IOperationParserOptionsHandler#getOptions()
     */
    public IOpParserOptions getOptions()
    {
        return m_ParserOptions;
    }

    private static class OperationHandlerData
    {
        public StateHandler handler;
        public String       stateName;
    }
    
    private Stack<OperationHandlerData> m_StateHandlers = 
            new Stack<OperationHandlerData>();
    private IOpParserOptions            m_ParserOptions;
    private SymbolTable                 m_SymbolTable = new SymbolTable();
}
