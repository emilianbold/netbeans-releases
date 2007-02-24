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
 * File       : MethodCallStateHandler.java
 * Created on : Dec 10, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers;

import org.dom4j.Node;

import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IREClassLoader;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParserEventDispatcher;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.InstanceInformation;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.SymbolTable;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.expression.Expression;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IREClass;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenDescriptor;

/**
 * @author Aztec
 */
public class MethodCallStateHandler
    extends MethodDetailStateHandler
{
    private Expression m_Expression = new Expression();
    private int        m_NestedMethodCalls;
    private String     m_StateName;
    
    public MethodCallStateHandler(String stateName, String language)    
    {
        super(language);
        m_NestedMethodCalls = 1;
        m_StateName = stateName;
        m_Expression.addState(stateName, getLanguage());
    }
    
    /**
     * Creates and returns a new state handler for a sub-state.  If the sub-state
     * is not handled then NULL is returned.  The variable state of interest is
     * <code>Initializer</code>
     *
     * @param stateName [in] The name of the new state.
     * @param language [in] The language of the state.
     *
     * @return The handler for the sub-state, NULL if the state is not handled.
     */
    public StateHandler createSubStateHandler(String stateName, String language)
    {
        if(m_StateName.equals(stateName))
        {
            m_NestedMethodCalls++;
        }
        m_Expression.addState(stateName, language);
        return this;
    }
    
    /**
     * Builds the XMI that will represent the expression.  The 
     * XML DOM Nodes that represent the expression will be added
     * as children to the specified DOM Node.
     *
     * @param pInfo [in] The instance that is the input pin.
     * @param pParentNode [in] The node that will contain the XMI node.
     * @param pVal [out] The data.
     */
    public ETPairT<InstanceInformation, Node> writeAsXMI(
                                                InstanceInformation pInfo,
                                                Node pParentNode,
                                                SymbolTable symbolTable,
                                                IREClass pThisPtr,
                                                IREClassLoader pClassLoader)
    {
        return m_Expression.writeAsXMI(pInfo, 
                                      pParentNode,
                                      symbolTable,
                                      pThisPtr, 
                                      pClassLoader);
    }

    /**
     * Sends out the UMLParser structure details events that represent the 
     * method call data.  
     *
     * @param pInfo [in] The instance information context.
     * @param symbolTable [in] The symbol table to use for lookups.
     * @param pClassLoader [in] The classloader to use when searching for 
     *                          class definitions.
     * @param pDispatcher [in] The event dispatcher used to send the events.
     * 
     * @return The instance context.
     */
    public InstanceInformation sendOperationEvents(
                                            InstanceInformation pInstance,
                                            IREClass pThisPtr,
                                            SymbolTable symbolTable,
                                            IREClassLoader pClassLoader,
                                            IUMLParserEventDispatcher pDispatcher,
                                            Node pParentNode)
    {
        return m_Expression.sendOperationEvents(pInstance, 
                                               pThisPtr, 
                                               symbolTable, 
                                               pClassLoader, 
                                               pDispatcher,
                                               pParentNode);
    }

    /**
     * Converts the expression data into a string representation.
     *
     * @return The string representation.
     */
    public String toString()
    {
        return m_Expression.toString();
    }

    /**
     * Retrieve the start position of the expression.  The start position
     * is the file position before the first character of the expression.
     *
     * @return The file position where the expression starts.
     */
    public long getStartPosition()
    {
        return m_Expression.getStartPosition();
    }

    /**
     * Retrieve the end position of the expression.  The end position
     * is the file position after the last character of the expression.
     *
     * @return The file position where the expression ends.
     */
    public long getEndPosition()
    {
        return m_Expression.getEndPosition();
    }

    /**
     * Retrieve the start position of the expression.  The start position
     * is the file position before the first character of the expression.
     *
     * @return The file position where the expression starts.
     */
    public long getStartLine()
    {
        return m_Expression.getStartLine();
    }
    
    public void initialize()
    {
        // No valid implementation in the C++ code base.
    }

    /**
     * Process a new token.  The tokens that are processed are in the
     * context of an object creation.
     *
     * @param pToken [in] The token to be processed.
     */
    public void processToken(ITokenDescriptor pToken, String language)
    {
        m_Expression.addToken(pToken, language);
    }
    
    /**
     * Notification that the a state has completed.
     *
     * @param stateName [in] The name of the state.
     */
    public void stateComplete(String stateName)
    {
        if(m_StateName.equals(stateName))
        {
            --m_NestedMethodCalls;

            if(m_NestedMethodCalls == 0)
            {
                reportData();
            }
            else
            {
                m_Expression.endState(stateName);
            }
        }
        else
        {
            m_Expression.endState(stateName);
        }
    }
}
