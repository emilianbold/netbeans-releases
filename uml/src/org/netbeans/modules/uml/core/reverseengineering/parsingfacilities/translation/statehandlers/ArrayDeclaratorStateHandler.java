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
 * File       : ArrayDeclartorStateHandler.java
 * Created on : Dec 9, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers;

import org.dom4j.Node;

import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.Identifier;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenDescriptor;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityRange;

/**
 * @author Aztec
 */
public class ArrayDeclaratorStateHandler extends StateHandler
{
    private Identifier     m_TypeIdentifier = new Identifier();
    private boolean         m_IsAggregation;
    private boolean         m_DimensionSpecifies;


    public ArrayDeclaratorStateHandler()
    {
        m_IsAggregation = false;
        m_DimensionSpecifies = false;
    }

    /**
     * Creates a new state handler.  The returned state handler may be a
     * pointer to <I>this</I> if the state handler also handles the new
     * state.  If the new state is not handle at all NULL is returned.
     *
     * @param stateName [in] The name of the state.
     * @param langauge [in] The language that is being processed.
     *
     * @return The new State Handler.
     */
    public StateHandler createSubStateHandler(String stateName, String val)
    {
        StateHandler retVal = null;

        if("Array Declarator".equals(stateName))
        {
            retVal = new ArrayDeclaratorStateHandler();
            m_IsAggregation      = false;
            m_DimensionSpecifies = false;
        }
        else if("Dimension".equals(stateName))
        {
            retVal = new ArrayDimensionStateHandler();
            m_IsAggregation      = false;
            m_DimensionSpecifies = true;
        }
        else if("Identifier".equals(stateName))
        {
            //m_NestedIdentifierLevel++;
            retVal = this;
        }

        if(retVal != null && (retVal != this))    
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
     * The expression handler will process the token.
     *
     * @param pToken [in] The token to be processed.
     */
    public void processToken(ITokenDescriptor pToken, String language)
    {
        String tokenType = pToken.getType();

        if("Primitive Type".equals(tokenType))
        {
            m_TypeIdentifier.addToken(pToken);
            m_IsAggregation = false;
        }
        else if("Identifier".equals(tokenType) ||
               "Scope Operator".equals(tokenType))
        {
            m_TypeIdentifier.addToken(pToken);
            m_IsAggregation = true;
        }
    }

    /**
     * Notifies the state handler that a state has completed.
     *
     * @param xstring& [in] The name of the completed state.
     */
    public void stateComplete(String stateName)
    {
        if("Array Declarator".equals(stateName))
        {
            if(m_DimensionSpecifies == false)
            {
                setDefaultRange();
            }

            if(m_TypeIdentifier != null)
            {
                setNodeAttribute("type", m_TypeIdentifier.getIdentifierAsUML());
            }


            if(m_TypeIdentifier != null)
            {
                createTokenDescriptor("Type",
                                       m_TypeIdentifier.getStartLine(),
                                       m_TypeIdentifier.getStartColumn(),
                                       m_TypeIdentifier.getStartPosition(),
                                       m_TypeIdentifier.getIdentifierAsSource(),
                                       m_TypeIdentifier.getLength());
            }
        }
    }


    /**
     * Checks if the association that represent the attribute should be
     * an aggregation.
     *
     * @return true if the association should be an aggregation.
     */
    protected boolean isAggregation()
    {
        return m_IsAggregation;
    }

    /**
     * Checks if the association that represent the attribute should be
     * an composition.
     *
     * @return true if the association should be an composition.
     */
    protected boolean isComposition()
    {
        return false;
    }

    /**
     * Sets the type of association should be created.  If the node is an
     * attribute and the type is not a primitve there should be a
     * asociation created.  SetAssociationType will call IsAggregation
     * to test if the association should be an aggregation, IsComposition
     * will be called to test if the association should be a composition.
     *
     * @param pNode [in] The node to test.
     * @see IsAggregation()
     * @see IsComposition()
     */
    protected void setAssociationType(Node pNode)
    {
        if(pNode != null)
        {
            // I only want to set the Assocition type if the node is a
            // UML:Attribute node.
            String nodeType = pNode.getName();
            if("UML:Attribute".equals(nodeType))
            {
                if(isAggregation())
                    setNodeAttribute("AssociationType", "Aggregation");
                else if(isComposition())
                    setNodeAttribute("AssociationType", "Composition");
            }
        }
    }

    protected void setDefaultRange()
    {

        Node pNode = getDOMNode();

        if(pNode != null)
        {
            Node structureNode = ensureElementExists(pNode,
                                           "UML:TypedElement.multiplicity",
                                           "UML:TypedElement.multiplicity");

            if(structureNode != null)
            {
                Node multiplictyNode = ensureElementExists(structureNode,
                                              "UML:Multiplicity",
                                              "UML:Multiplicity");
                if(multiplictyNode != null)
                {
                    Node rangeNode = ensureElementExists(multiplictyNode,
                                                 "UML:Multiplicity.range",
                                                 "UML:Multiplicity.range");
                    if(rangeNode != null)
                    {
                        Node dataNode = createNamespaceElement(rangeNode,
                                                "UML:MultiplicityRange");

                        if(dataNode != null)
                        {
                            setNodeAttribute(dataNode, "lower", "0");
                            setNodeAttribute(dataNode, "upper", "*");
                            setNodeAttribute(dataNode, "collectionType", IMultiplicityRange.AS_ARRAY);
                        }
                    }
                }
            }
            setAssociationType(pNode);
        }
    }


}
