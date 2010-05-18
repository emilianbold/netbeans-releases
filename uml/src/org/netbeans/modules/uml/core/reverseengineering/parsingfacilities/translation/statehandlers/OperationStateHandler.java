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
 * File       : OperationStateHandler.java
 * Created on : Dec 11, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers;

import org.dom4j.Node;

import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParserEventDispatcher;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IOperationEvent;
import org.netbeans.modules.uml.core.reverseengineering.reframework.OperationEvent;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenDescriptor;

/**
 * @author Aztec
 */
public class OperationStateHandler extends AttributeStateHandler
{
    private int m_Kind;
    private boolean m_ForceAbstract;
    
    public static final int CONSTRUCTOR     = 0;
    public static final int DESTRUCTOR      = 1;
    public static final int OPERATION       = 2;
    public static final int PROPERTY_GET    = 3;     
    public static final int PROPERTY_SET    = 4;

    /**
     * @param language
     * @param stateName
     */
    public OperationStateHandler(String language, 
                                    String stateName,
                                    int kind,
                                    boolean forceAbstract)
    {
       super(language, stateName);
       m_Kind = kind;
       m_ForceAbstract = forceAbstract;
    }
    
    public OperationStateHandler(String language, 
                                 String stateName,
                                 int kind)
    {
        this (language, stateName, kind, false);
    }
    
    public StateHandler createSubStateHandler(String stateName, String language) 
    {
        StateHandler retVal = null;    
        
        if( ("Parameters".equals(stateName)) || 
            ("Method Body".equals(stateName)) || 
            ("Constructor Body".equals(stateName)) )
        {
            retVal = this;
        }
        else if("Parameter".equals(stateName))
        {
            //retVal = new ParameterStateHandler("inout");
            retVal = new ParameterStateHandler("");
        }
        else if("Type".equals(stateName))
        {
            //retVal = new ParameterStateHandler("result", "Type");
            retVal = new OpReturnStateHandler(language, "Type");
            if(retVal != null)
            {
                ((TypeElementStateHandler)retVal).setTypeState(true);
            }
        }
        else if("Throws Declaration".equals(stateName))
        {
            retVal = new ThrowsDeclarationStateHandler();
        }
        else if("Class Declaration".equals(stateName))
        {
            //retVal = new ClassStateHandler(language);
        }

        if(retVal == null) 
        {      
            retVal = super.createSubStateHandler(stateName, language);
        }
        else
        {
            if((retVal != null) && (retVal != this))
            {
                Node pOperationNode = getDOMNode();

                if(pOperationNode != null)
                {
                    retVal.setDOMNode(pOperationNode);
                }
            }
        }        
        return retVal;
    }
    

    public String getFeatureName()
    {
        return "UML:Operation";
    }
    
    public void initialize() 
    {
        //TODO: Aztec
        //TypeElementStateHandler::Initialize()
        super.initialize();
        setNodeAttribute("isAbstract", isForceAbstract());
        if(getOperationKind() == CONSTRUCTOR)
        {
           setNodeAttribute("isConstructor", true) ;
        }
        else if(getOperationKind() == DESTRUCTOR)
        {
           setNodeAttribute("isDestructor", true) ;
        }
        else if(getOperationKind() == PROPERTY_GET)
        {
           setNodeAttribute("isProperty", true) ;
           setNodeAttribute("isQuery", true) ;
        }
        else if(getOperationKind() == PROPERTY_SET)
        {
           setNodeAttribute("isProperty", true) ;
           setNodeAttribute("isQuery", false) ;
        }
    }
    
    public boolean isForceAbstract()
    {
       return m_ForceAbstract;
    }
    
    public void processToken(ITokenDescriptor pToken, String language) 
    {
        if(pToken == null) return;
        
        String tokenType = pToken.getType();
        
        String value = pToken.getValue();
        
        if( ("Method Body End".equals(tokenType)) ||
           ("Statement Terminator".equals(tokenType)))
        {
            handleEndPostion(pToken);
        }
        if("Method Body Start".equals(tokenType))
        {
            handleBodyStart(pToken);
        }
        if("Parameter End".equals(tokenType))
        {
           long line = pToken.getLine();
           long col = pToken.getColumn();
           long pos = pToken.getPosition();
                  
           createTokenDescriptor("OpHeadEndPosition", line, col, pos + 1, "", 0);
        }
        if("Is Query".equals(tokenType))
        {
            setNodeAttribute("isQuery", value);
        }
        if(tokenType.equals("Keyword") && value.equals("Property"))
        {
        	setNodeAttribute("isProperty","true");
        }
        if("Is Virtual".equals(tokenType))
        {
            setNodeAttribute("isVirtual", value);
        }
        else
        {
            TypeElementStateHandler.processToken(this, pToken, language);
        }
    }

    public void stateComplete(String stateName) 
    {
        // If the name of the state that is complete matches the name 
        // of the state we were passed in the constructor, fire the 
        // operation found event.
        if( stateName.equals(getStateName()))
        {
            sendOnOperationFoundEvent();
        }

        super.stateComplete( stateName );
    }
    
    protected boolean isAbstractModifier(String value) 
    {
        return "abstract".equals(value);
    }

    protected int getOperationKind() 
    {
        return m_Kind;
    }

    protected void updateType() 
    {
        // No valid implementation in the C++ code base.
    }

    protected void sendOnOperationFoundEvent() 
    {
        IOperationEvent pEvent = new OperationEvent();
        if(pEvent != null)
        {
            Node pNode = getDOMNode();

            if(pNode != null)
            {
                pEvent.setEventData(pNode);

                IUMLParserEventDispatcher pDispatcher = getEventDispatcher();
        
                if(pDispatcher != null)
                {
                    pDispatcher.fireOperationFound(pEvent, null);
                }
            }
        }
    }



}
