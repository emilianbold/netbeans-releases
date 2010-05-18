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
 * File       : MethodParameterStateHandler.java
 * Created on : Dec 11, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers;

import org.dom4j.Node;

import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.Identifier;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.InstanceInformation;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenDescriptor;

/**
 * @author Aztec
 */
public class MethodParameterStateHandler extends MethodDetailStateHandler
{
    private ITokenDescriptor m_InstanceName;
    private Identifier m_TypeIdentifier = new Identifier();
    private boolean m_PrimitiveType;
    private String m_StateName = "Parameter";
    private InstanceInformation m_Instance;
    private boolean m_IsTemplateInstantiation = false;
    private TemplateInstantiationStateHandler mTemplateHandler = null;


    public MethodParameterStateHandler(String language)
    {
        super(language);
    }
    
    public MethodParameterStateHandler(String language, String stateName)
    {
        super(language);
        m_StateName = stateName;
        m_PrimitiveType = false;
        m_InstanceName = null;
    }
    
    public StateHandler createSubStateHandler(String stateName, String language) 
    {
        StateHandler retVal = null;
        if("Type".equals(stateName))
        {
            retVal = this;
        }
        else if("Template Instantiation".equals(stateName))
        {
           mTemplateHandler = new TemplateInstantiationStateHandler(false);
           retVal = mTemplateHandler;           
        }
        else if("Array Declarator".equals(stateName))
        {
            //SetModifierState(false);
            //SetTypeState(false);
      
            // The attribute state handler will handle the type state itself.
            // So, I want to return this.
            //retVal = new ArrayDeclartorStateHandler();

            // TODO: I must figure out what to do with arrays.  For now, SQD does not
            // handle Arrays.
            retVal = this;
        }
        else if("Identifier".equals(stateName))
        {
            retVal = this;
        }

        if(retVal != null && retVal != this)
        {
            Node pClassNode = getDOMNode();

            if(pClassNode != null)
            {
                retVal.setDOMNode(pClassNode);
            }
        }
        return retVal;
    }
    
    /**
     * Initialize the state handler.  This is a one time initialization.
     *
     */
    public void initialize()
    {
        // No valid implementation in the C++ code base.
    }
    
    public void processToken(ITokenDescriptor pToken, String language) 
    {
        if(pToken == null) return;
        
        String tokenType = pToken.getType();
        
        if("Name".equals(tokenType))
        {
            setInstanceName(pToken);
        }
        else if("Identifier".equals(tokenType) ||                 
                  "Scope Operator".equals(tokenType))
        {
            m_TypeIdentifier.addToken(pToken);
        }
        else if ("Primitive Type".equals(tokenType))
        {
            m_TypeIdentifier.addToken(pToken);
            setIsPrimitive(true);
        }
    }
    
    public void stateComplete(String stateName) 
    {
       if(m_StateName.equals(stateName))
       {
          InstanceInformation pInfo = addInstanceToSymbolTable();
          
          if(pInfo != null)
          {
             // I only want to send out reference events for parameters.
             if("Parameter".equals(stateName))
             {
                Node pParentNode = getDOMNode();
                
                if(pParentNode != null)
                {
                   // EventExecutor::RefVariableDef data;
                   // data.VariableName = pInfo->GetInstanceName();
                   // data.VariableType = CComBSTR(pInfo->GetInstanceTypeName().c_str());
                   // data.DeclaringClassifier = CComBSTR(pInfo->GetInstanceOwnerName().c_str());
                   
                   // EventExecutor::RefVariableStack refStack;
                   // refStack.push(data);
                   // EventExecutor::SendVariableReference(refStack, pParentNode);
                   
                   pInfo.sendReference(pParentNode);
                }
             }
          }
          reportData();
       }
       else if("Template Instantiation".equals(stateName))
       {
          m_IsTemplateInstantiation = false;
       }
    }
    
    public void setInstanceName(ITokenDescriptor name) 
    {
        m_InstanceName = name;
    }

    public String getInstanceName()
    {
        String retVal = null;
        if(m_InstanceName != null)
        {
            retVal = m_InstanceName.getValue();
        }
        return retVal;
    }

    public ITokenDescriptor getInstanceNameToken() 
    {
        return m_InstanceName;
    }

    public long getLineNumber() 
    {
        long retVal = -1L;
        if(m_InstanceName != null)
        {
            retVal = m_InstanceName.getLine();
        }
        return retVal;
    }

    public boolean isPrimitive() 
    {
        return m_PrimitiveType;
    }

    public void setIsPrimitive(boolean value) 
    {
        m_PrimitiveType = value;
    }

    public String toString() 
    {
        String retVal = "";

        String name = getInstanceName();
        
        retVal = m_TypeIdentifier.getIdentifierAsSource() + " ";
        
        if(name != null)
        {
            retVal += name;
        }        
        return retVal;
    }
    
    protected InstanceInformation addInstanceToSymbolTable() 
    {
        InstanceInformation retVal = m_Instance;

        // If retVal is not NULL then the instance has already been
        // added to the symbol table.  Do not add the instance again.
        if(retVal == null)
        {
            String typeName = m_TypeIdentifier.getIdentifierAsUML();
            if(mTemplateHandler != null)
            {
               typeName = mTemplateHandler.getTypeNameAsUML();
            }
        
            String instanceName  = getInstanceName();
            
            if(instanceName != null && typeName != null)
            {
                if(isPrimitive())
                {
                    retVal = addPrimitiveInstance(instanceName, typeName);
                }
                else
                {
                    retVal = addObjectInstance(instanceName, typeName);
                }
            }
        }        
        return retVal;
    }

    protected void processNewInstance(InstanceInformation pInfo) 
    {
        // No valid implementation in the C++ code base.
    }

    protected void sendVariableReference(InstanceInformation pInfo) 
    {
        // No valid implementation in the C++ code base.
    }

    protected void addTypeToken(ITokenDescriptor pToken) 
    {
        m_TypeIdentifier.addToken(pToken);
    }

    protected Identifier getTypeIdentifier() 
    {
        return m_TypeIdentifier;
    }

}
