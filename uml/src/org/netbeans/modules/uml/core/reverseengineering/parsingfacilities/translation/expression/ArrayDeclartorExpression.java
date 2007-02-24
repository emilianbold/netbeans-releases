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
 * File       : ArrayDeclartorExpression.java
 * Created on : Dec 8, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.expression;

import org.dom4j.Node;

import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IREClassLoader;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParserEventDispatcher;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.InstanceInformation;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.ObjectInstanceInformation;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.SymbolTable;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers.ExpressionStateHandler;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers.StateHandler;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IREClass;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenDescriptor;

/**
 * @author avaneeshj
 *
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ArrayDeclartorExpression extends ExpressionStateHandler {
    private ITokenDescriptor  m_pPreIndexOperator = null;
    private ITokenDescriptor  m_pPostIndexOperator = null;
    
    public void clear() {
        m_pPreIndexOperator  = null;
        m_pPostIndexOperator = null;
    }
    
    /**
     * Create a new state handler to be added to the state mechanism.  If the
     * state is not a state that is being processed then a new state handler is
     * not created.
     *
     * @param stateName [in] The state name.
     * @param language [in] The langauge being processed.
     *
     * @return
     */
    public StateHandler createSubStateHandler(String stateName, String language) {
        return super.createSubStateHandler(stateName, language);
        
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.expression.IArrayDeclartorExpression#getEndPosition()
         */
    public long getEndPosition() {
        long retVal = -1;
        if(m_pPostIndexOperator != null) {
            retVal = m_pPostIndexOperator.getPosition()
            + m_pPostIndexOperator.getLength();
        }
        return retVal;
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.expression.IArrayDeclartorExpression#getStartLine()
         */
    public long getStartLine() {
        long retVal = -1;
        if(m_pPreIndexOperator != null) {
            retVal = m_pPreIndexOperator.getLine();
        }
        return retVal;
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.expression.IArrayDeclartorExpression#getStartPosition()
         */
    
    public long getStartPosition() {
        long retVal = -1;
        if(m_pPreIndexOperator != null) {
            retVal = m_pPreIndexOperator.getPosition();
        }
        return retVal;
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.expression.IArrayDeclartorExpression#initialize()
         */
    public void initialize() {
        // Not implemented In C++ Code...
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.expression.IArrayDeclartorExpression#processToken(org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenDescriptor, java.lang.String)
         */
    public void processToken(ITokenDescriptor pToken, String language) {
        
        if(pToken != null) {
            
            String type = pToken.getType();
            
            if("Array Start".equals(type)) {
                m_pPreIndexOperator = pToken;
            } else if("Array End".equals(type)) {
                m_pPostIndexOperator = pToken;
            } else {
                super.processToken(pToken, language);
            }
        }
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.expression.IArrayDeclartorExpression#writeAsXMI(org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.expression.InstanceRef, org.dom4j.Node, org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.expression.SymbolTable, org.netbeans.modules.uml.core.reverseengineering.reframework.IREClass, org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IREClassLoader)
         */
    public ETPairT<InstanceInformation, Node> writeAsXMI(InstanceInformation pInfo,
            Node pParentNode,
            SymbolTable symbolTable,
            IREClass pThisPtr,
            IREClassLoader pClassLoader) {
        ObjectInstanceInformation ref= null;
        if(pInfo == null) {
            ref = new ObjectInstanceInformation();
            ref.setInstanceOwner(pThisPtr);
            ref.setInstanceType(pThisPtr);
        }
        return new ETPairT<InstanceInformation, Node>(ref, null);
    }
    
    public String toString() {
        String retVal = "";
        int nextExpression = 0;
        
        if(getExpressionCount() > 1) {
            IExpressionProxy arrayDim = getExpression(nextExpression);
            if(arrayDim != null) {
                retVal = arrayDim.toString();
            }
            nextExpression++;
        }
        
        //for java this adds the '[' to the array decl.
        if(m_pPreIndexOperator != null) {
            String value =  m_pPreIndexOperator.getValue();
            if( value.length() > 0 ) {
                retVal += value;
            }
        }
        
        //if there are any sub expressions like in the case of a
        //multi-dimensioned array this appends, recursively, the
        //additional '[]' to the declaration.
        String suffix = null ;
        if(getExpressionCount() > nextExpression) {
            for(int index = nextExpression; index < getExpressionCount(); index++) {
                IExpressionProxy exp = getExpression(index);
                if(exp != null) {
                    String tmp = exp.toString();
                    if (tmp.contains("[]")) {
                        suffix = tmp ;
                    } else {
                        retVal += tmp ;
                    }
                }
            }
        }
        
        //for java this appends the ']' to the array decl.
        if(m_pPostIndexOperator != null) {
            String value = m_pPostIndexOperator.getValue();
            if( value.length() > 0) {
                retVal += value;
            }
        }
        
        if (suffix != null) {
            retVal += suffix ;
        }
        
        return retVal;
    }
    
    /**
     * Sends out the UMLParser operaiton details events that represent the
     * expression data.
     *
     * @param pInfo [in] The instance information context.
     * @param symbolTable [in] The symbol table to use for lookups.
     * @param pClassLoader [in] The classloader to use when searching for
     *                          class definitions.
     * @param pDispatcher [in] The event dispatcher used to send the events.
     *
     * @return The instance context.
     */
    
    public InstanceInformation sendOperationEvents(InstanceInformation  pInfo,
            IREClass             pThisPtr,
            SymbolTable          symbolTable,
            IREClassLoader        pClassLoader,
            IUMLParserEventDispatcher pDispatcher,
            Node               pParentNode) {
        
        InstanceInformation retVal = pInfo;
        // The first child expression is the array instance.
        IExpressionProxy proxy = getExpression(0);
        if((proxy != null) && (pDispatcher != null)) {
            //         InstanceRef arrayInstance = NULL;
            retVal = proxy.sendOperationEvents(pInfo,  pThisPtr,
                    symbolTable, pClassLoader,
                    pDispatcher, pParentNode);
            
//		   if(arrayInstance != NULL)
//		   {
//			  retVal = GetArrayInstance(arrayInstance, symbolTable, pParentNode);
//	 //          retVal->SetInstanceName("");
//	 //
//	 //          retVal->SetHasBeenReferenced(false);
//	 //          retVal->SendReference(pParentNode);
//		   }
        }
        
        // Send the events for the array index expression.
        int max = getExpressionCount();
        for(int index = 1; index < max; index++) {
            IExpressionProxy proxyExp = getExpression(index);
            if(proxyExp != null) {
                InstanceInformation curRef = null;
                if(pDispatcher != null) {
                    curRef = proxyExp.sendOperationEvents(pInfo,
                            pThisPtr,
                            symbolTable,
                            pClassLoader,
                            pDispatcher,
                            pParentNode);
                }
            }
        }
        return retVal;
    }
}

