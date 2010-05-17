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
 * File       : TypeElementStateHandler.java
 * Created on : Dec 9, 2003
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
public class TypeElementStateHandler extends TopLevelStateHandler
{
    protected boolean    m_ModifiersState;
    protected boolean    m_TypeState;
    protected Identifier m_TypeIdentifier = new Identifier();
    protected int        m_TypeNestedLevel;
    protected String     m_ElementStateName;
    private TemplateInstantiationStateHandler mTemplateHandler = null;
    private String       m_DefaultVisibility = "package";

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers.ITypeElementStateHandler#getFeatureName()
     */
    public String getFeatureName()
    {
        // No valid implementation in the C++ code base.
        return null;
    }
 
    /**
     * Specifies the name of the tag that will own the feature node.
     */
    public String getFeatureOwnerName()
    {
       return "UML:Element.ownedElement";
    }
    
    /**
     * Initialize the state handler.
     *
     * @param stateName the name of the state that is being processed
     * @param language the name of the language that was parsed
     */
    public TypeElementStateHandler(String language, String stateName)
    {
        super(language);
        m_ModifiersState = false;
        m_TypeState = false;
        m_TypeNestedLevel = 0;
        m_ElementStateName = stateName;
    }

    /**
     * Initialize the state handler.
     *
     * @param stateName the name of the state that is being processed
     * @param language the name of the language that was parsed
     * @param defaultVis the visibility that should be set if the visibility is
     *                   not specified
     */
    public TypeElementStateHandler(String language, 
                                   String stateName, 
                                   String defaultVis)
    {
        this(language, stateName);
        setDefaultVisibility(defaultVis);
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

        if("Modifiers".equals(stateName))
        {
            // The attribute state handler will handle the modifiers state itself.
            // So, I want to return this.
            retVal = this;
            setModifierState(true);
            setTypeState(false);
        }
        else if("Type".equals(stateName))
        {
            // The attribute state handler will handle the type state itself.
            // So, I want to return this.
            retVal = this;
            setTypeState(true);
            setModifierState(false);
        }
        else if("Array Declarator".equals(stateName))
        {
            setModifierState(false);
            setTypeState(false);

            // The attribute state handler will handle the type state itself.
            // So, I want to return this.
            retVal = new ArrayDeclaratorStateHandler();
        }
        else if("Identifier".equals(stateName))
        {
            m_TypeNestedLevel++;
            retVal = this;
        }
        else if("Template Instantiation".equals(stateName))
        {
            mTemplateHandler = new TemplateInstantiationStateHandler(true);
            retVal = mTemplateHandler;  
        }
        else if("Alias Declaration".equals(stateName))
        {
            retVal = new AliasedTypeStateHandler("C++");
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

    protected Identifier getTypeIdentifier()
    {
        return m_TypeIdentifier;
    }

    /**
     * Add the type name information to the XMI structure.
     *
     * @param pToken [in] The token that specifies the class name.
     */
    protected void handleName(ITokenDescriptor pToken)
    {
        if(pToken == null) return;
        {
            String nameString = pToken.getValue();

            if (nameString != null)
            {
                setNodeAttribute("name", nameString);

                long line = pToken.getLine();
                long col = pToken.getColumn();
                long pos = pToken.getPosition();
                long length = pToken.getLength();

                createTokenDescriptor("Name", line, col, pos, nameString, length);
            }
        }
    }

    /**
     * Generates the visiblity XMI inforamtion
     *
     * @param value [out] The visibility type.
     *
     * @return true if the visiblity was handled.
     */
    protected boolean handleVisibilityModifier(String value)
    {
        boolean retVal = false;

        // I do not have to worry about package protected because the visibility
        // attribute set to package by default.
        String visValue = null;

        if("public".equals(value))
        {
            visValue = "public";
            retVal = true;
        }
        else if("private".equals(value))
        {
            visValue = "private";
            retVal = true;
        }
        else if("protected".equals(value))
        {
            visValue = "protected";
            retVal = true;
        }


        if(retVal == true)
        {
            setNodeAttribute("visibility", visValue);
        }
        return retVal;
    }

    /**
     * Generate the XMI data to represent the type information.
     */
    protected void updateType()
    {
       Identifier typeIdentifier = m_TypeIdentifier;
       if(mTemplateHandler != null)
       {
          typeIdentifier = mTemplateHandler.getTypeIdentifier();
       }
       
       if(typeIdentifier != null)
       {
          String value = typeIdentifier.getIdentifierAsUML();
          setNodeAttribute("type", value);
          createTokenDescriptor("Type",
                                typeIdentifier.getStartLine(),
                                typeIdentifier.getStartColumn(),
                                typeIdentifier.getStartPosition(),
                                typeIdentifier.getIdentifierAsSource(),
                                typeIdentifier.getLength());
       }
       
    }


    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers.ITypeElementStateHandler#getModifierState()
     */
    public boolean getModifierState()
    {
        return m_ModifiersState;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers.ITypeElementStateHandler#getStateName()
     */
    public String getStateName()
    {
        return m_ElementStateName;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers.ITypeElementStateHandler#getTypeState()
     */
    public boolean getTypeState()
    {
        return m_TypeState;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers.ITypeElementStateHandler#setModifierState(boolean)
     */
    public void setModifierState(boolean value)
    {
       m_ModifiersState = value;

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers.ITypeElementStateHandler#setTypeState(boolean)
     */
    public void setTypeState(boolean value)
    {
       m_TypeState = value;
    }

    /**
     * Initialize the state handler.  This is a one time initialization.
     */
    public void initialize()
    {
        Node pCurNode = getDOMNode();

        if( pCurNode != null)
        {
           String ownerName = getFeatureOwnerName();
             // The generalization token descriptor is really a
             // sub node of the UML:Class XML node.  Therefore, I
             // will create the TGeneralization under the passed
             // in node.  The TGeneralization node will then be
             // passed to StateHandler to use in the helper methods.
            Node pClassifierFeature = ensureElementExists(pCurNode,
                                                          ownerName,
                                                          ownerName);

            if(pClassifierFeature != null)
            {
                Node pFeature = createNode(pClassifierFeature,
                               getFeatureName());

                if(pFeature != null)
                {
                    setDOMNode(pFeature);
                    setNodeAttribute("visibility", getDefaultVisibility()) ;
                }
            }
        }
        else
        {
            createTopLevelNode("UML:Element.ownedElement");
        }
    }

    /**
     * Process a new token.  The tokens that are processed are in the
     * context of an object creation.
     *
     * @param pToken [in] The token to be processed.
     */
    public void processToken(ITokenDescriptor pToken, String language)
    {
        processToken(this, pToken, language);
    }
    
    public static void processToken(TypeElementStateHandler sh, 
            ITokenDescriptor pToken, String language)
    {
       if(pToken == null) return;

        String tokenType = pToken.getType();

        if("Name".equals(tokenType))
        {
             String name = pToken.getValue();
             sh.handleName(pToken);
             sh.recordStartToken(pToken);
        }
        else if("Modifier".equals(tokenType))
        {
            sh.handleModifier(pToken, language);
            sh.recordStartToken(pToken);
            sh.handleComment(pToken);
        }
        else if( (("Identifier".equals(tokenType)) ||
                    ("Primitive Type".equals(tokenType)) ||
                    ("Scope Operator".equals(tokenType))) &&
                    (sh.getTypeState() == true) )
        {
            sh.m_TypeIdentifier.addToken(pToken);
            sh.recordStartToken(pToken);
            sh.handleComment(pToken);

            if("Primitive Type".equals(tokenType))
            {
                sh.createTokenDescriptor("IsPrimitive", -1, -1, -1, "true", -1);
            }
        }
        else if( ("Identifier".equals(tokenType)) && (sh.getTypeState() == true) )
        {
            String name = pToken.getValue();

            sh.m_TypeIdentifier.addToken(pToken);
            sh.recordStartToken(pToken);
            sh.handleComment(pToken);
        }
    }

    /**
     * Notification that the a state has completed.
     *
     * @param stateName [in] The name of the state.
     */
    public void stateComplete(String stateName)
    {
        //if((GetTypeState() == false) && (GetModifierState() == false))
        if(stateName.equals(m_ElementStateName))
        {
            writeStartToken();
        }

        if(getTypeState())
        {
            if(m_TypeNestedLevel > 0)
            {
                m_TypeNestedLevel--;

                if(m_TypeNestedLevel == 0)
                {
                    updateType();
                    setTypeState(false);
                }
            }
            else
            {
                // Handles when the type was a primitive.
                updateType();
                setTypeState(false);
            }
        }
        else
        {
            m_TypeNestedLevel = 0;
        }

        setModifierState(false);
    }

   public String getDefaultVisibility()
   {
      return m_DefaultVisibility;
   }

   public void setDefaultVisibility(String value)
   {
      this.m_DefaultVisibility = value;
   }

}
