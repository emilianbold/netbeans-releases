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
 * File       : ArrayDimensionStateHandler.java
 * Created on : Dec 9, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers;

import org.dom4j.Node;

import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenDescriptor;

/**
 * @author Aztec
 */
public class ArrayDimensionStateHandler extends StateHandler
{
    boolean     m_IsRange;
    boolean     m_IsLowerRange;
    Node        m_RangeNode;


    public ArrayDimensionStateHandler()
    {
        m_IsRange = false;
        m_IsLowerRange = false;
    }

    public StateHandler createSubStateHandler(String stateName, String language)
    {
        StateHandler retVal = null;

        if("Range".equals(stateName))
        {
            setIsInRange(true);
            setIsLowerRange(true);
            retVal = this;
        }
        return retVal;
    }

    public void initialize()
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
                        Node m_RangeNode = createNamespaceElement(rangeNode,
                                                "UML:MultiplicityRange");

                        if(m_RangeNode != null)
                        {
                            setNodeAttribute(m_RangeNode, "lower", "0");
                            setNodeAttribute(m_RangeNode, "upper", "*");
                        }
                    }
                }
            }
        }
    }

    public void processToken(ITokenDescriptor pToken, String language)
    {
        if(pToken == null) return;
        {
            if(!isInRange())
            {
                setUpperRange(pToken);
            }
            else
            {
                if(isLowerRange())
                {
                    setLowerRange(pToken);
                    setIsLowerRange(false);
                }
                else
                {
                    setUpperRange(pToken);
                }
            }
        }
    }



    //  *********************************************************************
    //  Data Access Members
    //  *********************************************************************

    protected void setIsInRange(boolean value)
    {
       m_IsRange = value;
    }

    protected boolean isInRange()
    {
       return m_IsRange;
    }

    protected  void setIsLowerRange(boolean value)
    {
       m_IsLowerRange = value;
    }

    protected  boolean isLowerRange()
    {
       return m_IsLowerRange;
    }

    //  *********************************************************************
    //  Helper Methods
    //  *********************************************************************

     protected void setLowerRange(ITokenDescriptor pToken)
     {
        if(pToken == null) return;

        if(m_RangeNode != null)
        {
            String lowerValue = pToken.getValue();

            if(lowerValue != null)
            {
                setNodeAttribute(m_RangeNode, "lower", lowerValue);
              }
        }
     }

     protected void setUpperRange(ITokenDescriptor pToken)
     {
        if(pToken == null) return;

        if(m_RangeNode != null)
        {
            String upperValue = pToken.getValue();

            if(upperValue != null)
            {
                setNodeAttribute(m_RangeNode, "upper", upperValue);
            }
        }
     }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers.StateHandler#stateComplete(java.lang.String)
     */
    public void stateComplete(String val)
    {
    }

}
