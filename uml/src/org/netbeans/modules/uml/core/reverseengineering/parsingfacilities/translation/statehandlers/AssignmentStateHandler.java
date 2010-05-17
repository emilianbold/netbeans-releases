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
 * File       : AssignmentStateHandler.java
 * Created on : Dec 12, 2003
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
public class AssignmentStateHandler extends MethodDetailStateHandler
{
    private Expression m_Expression = new Expression();
    private int m_NestedAssigments = 1;
    private String m_StateName;


    public AssignmentStateHandler(String stateName, String language)
    {
        super(language);
        m_StateName = stateName;
       m_Expression.addState(stateName, language);
    }
    
    public StateHandler createSubStateHandler(String stateName, String language)
    {
        if(m_StateName.equals(stateName))
        {
            m_NestedAssigments++;
        }
        if(m_Expression != null)
        m_Expression.addState(stateName, language);
        return this;
    }
    
    public void initialize()
    {
        // No valid implementation in the C++ code base.
    }
    
    public void processToken(ITokenDescriptor pToken, String language)
    {
        if(m_Expression != null)
        m_Expression.addToken(pToken, language);
    }
    
    public void stateComplete(String stateName)
    {
        if(m_StateName.equals(stateName))
        {
            --m_NestedAssigments;
        
            if(m_NestedAssigments == 0)
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
            if(m_Expression != null)
            m_Expression.endState(stateName);
        }
    }
    
    public long getStartPosition()
    {
        return m_Expression.getStartPosition();
    }

    public long getEndPosition()
    {
        return m_Expression.getEndPosition();
    }

    public long getStartLine()
    {
        return m_Expression.getStartLine();
    }
    
    public String toString()
    {
        return m_Expression.toString();
    }
    
    public InstanceInformation sendOperationEvents(
                                        InstanceInformation pInstance,
                                        IREClass pThisPtr,
                                        SymbolTable symbolTable,
                                        IREClassLoader pClassLoader,
                                        IUMLParserEventDispatcher pDispatcher,
                                        Node pParentNode)
    {
        if(m_Expression != null)
        return m_Expression.sendOperationEvents(pInstance, 
                                                   pThisPtr, 
                                                   symbolTable, 
                                                   pClassLoader, 
                                                   pDispatcher,
                                                   pParentNode);
        
        return null;
    }
    
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

}
