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
 * File       : JumpStateHandler.java
 * Created on : Dec 11, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers;

import org.dom4j.Node;

import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IOpParserOptions;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParserEventDispatcher;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IJumpEvent;
import org.netbeans.modules.uml.core.reverseengineering.reframework.JumpEvent;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenDescriptor;

/**
 * @author Aztec
 */
public class JumpStateHandler extends MethodDetailStateHandler
{
    private String m_JumpDestination;
    private int m_JumpType = IJumpEvent.JE_GOTO;


    /**
     * @param language
     */
    public JumpStateHandler(String language, String stateName)
    {
        super(language);
        if(stateName.equals("Break"))
        {
           m_JumpType = IJumpEvent.JE_BREAK;
        }
        else if(stateName.equals("Continue"))
        {
           m_JumpType = IJumpEvent.JE_CONTINUE;
        }
        else if(stateName.equals("Goto"))
        {
           m_JumpType = IJumpEvent.JE_GOTO;
        }
    }
    
    public StateHandler createSubStateHandler(String stateName, String language) 
    {
        IOpParserOptions pOptions = getOpParserOptions();

        return StatementFactory.retrieveStatementHandler(stateName, 
                                                          language,
                                                          pOptions, 
                                                          getSymbolTable());
    }
    
    public void initialize() 
    {
        Node pLoopNode = createNode("UML:JumpAction");   
        setDOMNode(pLoopNode);
    }
    
    public void processToken(ITokenDescriptor pToken, String language) 
    {
        if(pToken == null) return;
        
        String type = pToken.getType();
        
        String value = pToken.getValue();
        
        if("Destination".equals(type))
        {
            m_JumpDestination = value;
        }
    }
    
    public void stateComplete(String stateName) 
    {
        if((m_JumpType == IJumpEvent.JE_BREAK && "Break".equals(stateName))    || 
            (m_JumpType == IJumpEvent.JE_CONTINUE && "Continue".equals(stateName)) ||
            (m_JumpType == IJumpEvent.JE_GOTO && "Goto".equals(stateName)))
        {
            sendJumpEvent(stateName);
        }
    }

    protected void sendJumpEvent(String jumpType)
    {
        IJumpEvent pEvent = new JumpEvent();

        if(pEvent != null)
        {
            IUMLParserEventDispatcher pDispatcher = getEventDispatcher();

            if(m_JumpDestination != null)
            {
                Node pInputPin = createNode("UML:InputPin");
                if(pInputPin != null)
                {
                    setNodeAttribute(pInputPin, "value", m_JumpDestination);
                    setNodeAttribute(pInputPin, "kind", "Label");
                }
            }
            setNodeAttribute("type", jumpType);

            Node pNode = getDOMNode();

            pEvent.setEventData(pNode);
            pDispatcher.fireJumpEvent(pEvent, null);
        }
    }

}
