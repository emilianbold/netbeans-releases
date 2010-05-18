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
 * File       : EnumMemberStateHandler.java
 * Created on : Dec 11, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers;

import org.dom4j.Node;

import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenDescriptor;

/**
 * @author Aztec
 */
public class EnumMemberStateHandler extends AttributeStateHandler
{

    private String m_Type;
    private ExpressionStateHandler argumentsHandler;

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
       else if("Expression List".equals(stateName))
       {
          argumentsHandler = new ExpressionStateHandler();
          retVal = argumentsHandler;
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

    public void processToken(ITokenDescriptor pToken, String language)
    {
	if(pToken == null) return;

        String tokenType = pToken.getType();
        if("Name".equals(tokenType))
	{
	    handleName(pToken);
            recordStartToken(pToken);
            handleComment(pToken);
        } 
	else if("Parameter End".equals(tokenType))
	{
            addLiteralArgumentsDescriptor();
	    createTokenDescriptor("Parameter End", pToken);
        } 
	else if("Body End".equals(tokenType))
	{
	    createTokenDescriptor("Body End", pToken);
        } 
	else if("Literal Separator".equals(tokenType))
	{
	    createTokenDescriptor("Literal Separator", pToken);
        } 
	else 
	{
	    super.processToken(pToken, language);
	}
    }

    private void addLiteralArgumentsDescriptor() {
        if (argumentsHandler != null) 
        {
            long startPos = argumentsHandler.getStartPosition();
            long length   = argumentsHandler.getEndPosition() - startPos;
            createTokenDescriptor("JavaEnumLiteralArguments", -1, -1,
                                  argumentsHandler.getStartPosition(),
                                  argumentsHandler.toString(),
                                  length);
        }
    }

}
