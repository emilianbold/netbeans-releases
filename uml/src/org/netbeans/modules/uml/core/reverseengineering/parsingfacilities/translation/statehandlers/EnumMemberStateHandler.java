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
 * File       : EnumMemberStateHandler.java
 * Created on : Dec 11, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers;

import org.dom4j.Node;

/**
 * @author Aztec
 */
public class EnumMemberStateHandler extends AttributeStateHandler
{

    private String m_Type;

    public EnumMemberStateHandler(String language, String stateName)
    {
        super(language, stateName);
    }

    public String getFeatureName()
    {
        return "UML:EnumerationLiteral";
    }
    
    public String getFeatureOwnerName()
    {
       return "UML:Enumeration.literal";
    }
    
    /**
     * Retrieve the type of the enumeration member.
     *
     *
     * @param type [in] The type of the member.
     */
    public void setMemberType(String type)
    {
        m_Type = type;
    }

    /**
     * Retrieve the type of the enumeration member.
     *
     *
     * @return The type of the member.
     */
    public String getMemberType()
    {
        return m_Type;
    }
    
    /**
     * Initialize the state handler.  This is a one time initialization.
     */
    public void initialize()
    {
        super.initialize();
        setNodeAttribute("type", getMemberType());
    }
    
    /**
  * Creates and returns a new state handler for a sub-state.  If the sub-state
     * is not handled then null is returned.  The attribute state of interest is
     * <code>Initializer</code>
     *
     * @param stateName [in] The name of the new state.
     * @param language [in] The language of the state.
     *
     * @return The handler for the sub-state, null if the state is not handled.
     */
    public StateHandler createSubStateHandler(String stateName, String language)
    {
       StateHandler retVal = null;
       if(("Method Definition".equals(stateName)) ||
          ("Method Declaration".equals(stateName)) )
       {
          retVal = new OperationStateHandler(language,
                                             stateName,
                                             OperationStateHandler.OPERATION,
                                             false);
       }
       else if("Body".equals(stateName))
       {
          retVal = this;
       }
       else
       {
          retVal = super.createSubStateHandler(stateName, language);
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

}
