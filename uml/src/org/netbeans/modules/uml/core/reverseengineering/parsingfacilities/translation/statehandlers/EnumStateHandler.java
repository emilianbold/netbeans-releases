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



package org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers;

import org.dom4j.Node;

import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.Identifier;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenDescriptor;
import org.netbeans.modules.uml.core.support.umlsupport.Log;

/**
 */
public class EnumStateHandler extends ClassStateHandler
{

	private Identifier m_Type = null;
	private boolean m_DiscoverType = false;

    public EnumStateHandler(String language, String packageName)
    {
        this(language, packageName, false);
    }
    
    public EnumStateHandler(String language, String packageName, boolean isInner)
    {
        super(language, packageName, isInner);
        m_DiscoverType = false;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers.StateHandler#initialize()
     */
    public void initialize()
    {
        Node pNode = getDOMNode();
        if(pNode == null)
        {
            createTopLevelNode("UML:Enumeration");
            setNodeAttribute("visibility", "public");
        }
        else
        {
            // The inner class token descriptor is really a 
            // sub node of the UML:Class XML node.  Therefore, it must
            // be a sub node of UML:Element.ownedElement.
            Node pClassifierFeature = ensureElementExists(pNode, 
                                         "UML:Element.ownedElement", 
                                         "UML:Element.ownedElement");

            if(pClassifierFeature != null)
            {
                Node pNewClass = createNamespaceElement(pClassifierFeature, 
                                "UML:Enumeration");
                setNodeAttribute("visibility", "public");

                if(pNewClass != null)
                {
                    setDOMNode(pNewClass);
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers.StateHandler#createSubStateHandler(java.lang.String, java.lang.String)
     */
    public StateHandler createSubStateHandler(String stateName, String language)
    {
        StateHandler retVal = null;

        if(stateName.equals("Enum Member"))
        {
            EnumMemberStateHandler enumMemberHandler 
                = new EnumMemberStateHandler(language, stateName);
            
            String type = "int";
            if((m_Type != null) && (m_Type.getLength() > 0))
            {
               type = m_Type.getIdentifierAsUML();
            }

            enumMemberHandler.setMemberType(type);

            retVal = enumMemberHandler;

            Node pEnumNode = getDOMNode();
            if(pEnumNode != null)
            {
                retVal.setDOMNode(pEnumNode);
            }
        }
        else if(stateName.equals("Type"))
        {
           m_DiscoverType = true;
           retVal = this;
        }
        else
        {
           retVal = super.createSubStateHandler(stateName, language);
        }
        return retVal;
    }

    public void processToken(ITokenDescriptor pToken, String language)
    {
    
    	try
		{  
    		if(pToken != null)
            {	
       	    	String tokenType = pToken.getType();
       	    	if( ((tokenType.equals("Identifier")) || 
                    (tokenType.equals("Primitive Type"))||
                    (tokenType.equals("Scope Operator"))) && 
                    (m_DiscoverType == true))
       	    	{
       	    		m_Type.addToken(pToken);
       	    	} 
		else if (tokenType.equals("Literal Section Terminator"))
		{
		    createTokenDescriptor("Literal Section Terminator", pToken);
		}
       	    	else
       	    	{
       	    		super.processToken(pToken, language);
       	    	}
            }    
		}
    	catch(Exception e)
		{
    		Log.stackTrace(e);
		}
    }

}
