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
    
    //used only debugging parser tree, or reacting to it.
    //there are System.out.println method calls commented out. To see the
    //tokens and States coming from the parser, uncomment these calls.
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
            
            //System.out.println(getIndent(addIndent())+ "<"+ stateName +" handler="+ getName(data)+">" );
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
                //System.out.println(getIndent(addIndent()) +"<"+ stateName +" handler="+ getName(data) +">" );
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
            //System.out.println(getIndent(removeIndent()) + "</"+ stateName +">" );

            if (oldData.handler != null)
                oldData.handler.stateComplete(stateName);
        }
    }
    
    private String getName(OperationHandlerData data)
    {
    	String res = data.handler == null? "null" : data.handler.getClass().getName();
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
        if (m_StateHandlers.size() > 0)
        {
            try
            {
                OperationHandlerData data = m_StateHandlers.peek();
                if (data.handler != null) {
                    data.handler.processToken(token, language);
                    //System.out.println(getIndent(indent) + "<token value="+token.getValue() +" handler="+ getName(data)+"/>" );
                } //else
                    //System.out.println(getIndent(indent) + "<token value="+token.getValue() +" handler="+ getName(data)+"/>" );
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        } //else
            //System.out.println(getIndent(indent) + "<token value="+token.getValue() +" handler=NO_STATE_HANDLER/>" );
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