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
 * File       : VBMethodWithStatement.java
 * Created on : Dec 12, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers;

import org.dom4j.Node;

import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IOpParserOptions;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IREClassLoader;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParserEventDispatcher;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.InstanceInformation;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.expression.Expression;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IREClass;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenDescriptor;

/**
 * @author Aztec
 */
public class VBMethodWithStatement extends MethodCompositeStateHandler
{
    private InstanceInformation m_ReferenceInstance;
    private Expression mInstanceExpression = new Expression();
    private boolean m_IsInReferenceState;

    
    public VBMethodWithStatement(String language)
    {
        super(language);
        m_IsInReferenceState = false;
    }
    
    public StateHandler createSubStateHandler(String stateName, String language)
    {
        MethodDetailStateHandler retVal = null;
        
        if("With Reference".equals(stateName))
        {
            retVal = this;
            setIsInReferenceState(true);
        }
        else if("Body".equals(stateName))
        {
            retVal = this;
        }
        else if(isInReferenceState())
        {
            mInstanceExpression.addState(stateName, language);
            retVal = this;
        }
        else
        {
            IOpParserOptions pOptions = getOpParserOptions();

            retVal = StatementFactory.retrieveStatementHandler(stateName, 
                                                               language, 
                                                               pOptions,
                                                               getSymbolTable()); 

            Node pNode = getDOMNode();
            initializeHandler(retVal, pNode);
        }
        
        return retVal;
    }
    
    public void initialize()
    {
        // No valid implementation in the C++ code base.
    }
    
    public void stateComplete(String stateName)
    {
        if("With Reference".equals(stateName))
        {
            setIsInReferenceState(false);

            Node pNode = getDOMNode();

            IREClass pThisClass = getClassBeingProcessed();

            IUMLParserEventDispatcher pDispatcher = getEventDispatcher();
   
            IREClassLoader pLoader = getClassLoader();
            
            InstanceInformation ref = mInstanceExpression.sendOperationEvents(
                                                        getReferenceInstance(), 
                                                        pThisClass,
                                                        getSymbolTable(), 
                                                        pLoader, 
                                                        pDispatcher,
                                                        pNode);

            if(ref != null)
            {
                setReferenceInstance(ref);
            }
        }
    }
    
    public void processToken(ITokenDescriptor pToken, String language)
    {
        if(isInReferenceState() == true)
        {
            mInstanceExpression.addToken(pToken, language);
        }

    }
    
    protected boolean isInReferenceState() 
    {
        return m_IsInReferenceState;
    }

    protected void setIsInReferenceState(boolean value) 
    {
        m_IsInReferenceState = value;
    }

}
