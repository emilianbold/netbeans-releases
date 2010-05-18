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
 * File       : AliasedTypedStateHandler.java
 * Created on : Dec 8, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers;

import org.dom4j.Node;

import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParserEventDispatcher;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.Identifier;
import org.netbeans.modules.uml.core.reverseengineering.reframework.ClassEvent;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IClassEvent;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenDescriptor;

/**
 * @author Aztec
 */
public class AliasedTypeStateHandler extends TopLevelStateHandler
{
    Identifier m_TypeIdentifier = new Identifier();

    public AliasedTypeStateHandler(String language)
    {
        super(language);
    }

    /**
     * Create a new state handler to be added to the state mechanism.  If the
     * state is not a state that is being processed then a new state handler is
     * not created.  The states of interest is <code>Expression List</code>
     *
     * @param stateName [in] The state name.
     * @param language [in] The langauge being processed.
     *
     * @return The hander for the new state.
     */
    public StateHandler createSubStateHandler(String stateName, String val)
    {
        StateHandler retVal = null;
        if("Type".equals(stateName))
        {
            // The attribute state handler will handle the type state itself.
            // So, I want to return this.
            retVal = this;
        }
        else if("Identifier".equals(stateName))
        {
            retVal = this;
        }
        return retVal;
    }

    /**
     * Initialize the state handler.  This is a one time initialization.
     */
    public void initialize()
    {
        Node pNode = getDOMNode();
        if(pNode == null)
        {
            super.createTopLevelNode("UML:AliasedType");
        }
        else
        {
            Node pNewAliasedType = createNamespaceElement(pNode, "UML:AliasedType");

            if(pNewAliasedType != null)
            {
                setDOMNode(pNewAliasedType);
            }
        }
    }



    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers.IAliasedTypedStateHandler#processToken(org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenDescriptor, java.lang.String)
     */
    public void processToken(ITokenDescriptor pToken, String language)
    {
        if(pToken == null) return;

        String tokenType = pToken.getType();

        if("Name".equals(tokenType))
        {
            String name = pToken.getValue();
            handleName(pToken);
        }
        else if("Type Decoration".equals(tokenType))
        {
            String name = pToken.getValue();
            handleTypeDecoration(pToken);
        }
        else if("Identifier".equals(tokenType) ||
                "Primitive Type".equals(tokenType)||
                "Scope Operator".equals(tokenType))
        {
            m_TypeIdentifier.addToken(pToken);

            if("Primitive Type".equals(tokenType))
            {
                createTokenDescriptor("IsPrimitive", -1, -1, -1, "true", -1);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers.IAliasedTypedStateHandler#stateComplete(java.lang.String)
     */
    public void stateComplete(String stateName)
    {
        if("Alias Declaration".equals(stateName))
        {
           writeStartToken();
           sendEvent();
        }
        if("Type".equals(stateName))
        {
           writeType();
        }
    }

    /**
     * Add the class name information to the XMI structure.
     *
     * @param pToken [in] The token that specifies the class name.
     */
    protected void handleName(ITokenDescriptor pToken)
    {
        if(pToken == null) return;

        String value = pToken.getValue();
        setNodeAttribute("aliasedName", value);

        long line = pToken.getLine();
        long col =  pToken.getColumn();
        long pos =  pToken.getPosition();
        long length = pToken.getLength();

        createTokenDescriptor("Name", line, col, pos, value, length);
    }

    /**
     * Add the Type Decoration information to the XMI structure.
     *
     * @param pToken [in] The token that specifies the class name.
     */
    protected void handleTypeDecoration(ITokenDescriptor pToken)
    {
        if(pToken == null) return;

        String value = pToken.getValue();

        setNodeAttribute("typeDecoration", value);

        long line = pToken.getLine();
        long col =  pToken.getColumn();
        long pos =  pToken.getPosition();
        long length = pToken.getLength();

        createTokenDescriptor("Decoration", line, col, pos, value, length);
    }

    /**
     * Sends the IClassEvent to all listeners.
     */
    protected void sendEvent()
    {
        IClassEvent pEvent = new ClassEvent();
        if(pEvent != null)
        {
            Node pNode = getDOMNode();

            if(pNode != null)
            {
                pEvent.setEventData(pNode);

                IUMLParserEventDispatcher pDispatcher = getEventDispatcher();

                if(pDispatcher != null)
                {
                    pDispatcher.fireClassFound("", pEvent, null);
                }
            }
        }
    }

    protected void writeType()
    {

        String value = m_TypeIdentifier.getIdentifierAsSource();

        String nameString = m_TypeIdentifier.getIdentifierAsUML();
        setNodeAttribute("actualType", nameString);

        long line = m_TypeIdentifier.getStartLine();
        long col = m_TypeIdentifier.getStartColumn();
        long pos = m_TypeIdentifier.getStartPosition();
        long length = m_TypeIdentifier.getLength();

        createTokenDescriptor("Type", line, col, pos, value, length);
    }
}
