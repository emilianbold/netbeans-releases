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
