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
