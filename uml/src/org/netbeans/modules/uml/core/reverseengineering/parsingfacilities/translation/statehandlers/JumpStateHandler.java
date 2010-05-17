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
