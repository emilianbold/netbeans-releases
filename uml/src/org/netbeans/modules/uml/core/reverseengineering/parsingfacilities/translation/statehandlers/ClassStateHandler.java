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
 * File       : ClassStateHandler.java
 * Created on : Dec 9, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers;

import org.dom4j.Node;

import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParserEventDispatcher;
import org.netbeans.modules.uml.core.reverseengineering.reframework.ClassEvent;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IClassEvent;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ILanguage;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenDescriptor;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;

/**
 * @author Aztec
 */
public class ClassStateHandler extends TopLevelStateHandler
{
    private boolean     m_ModifiersState = false;
    private boolean     m_IsInnerClass = false;
    private boolean     m_BodyState = false;
    private boolean     m_ForceAbstractOps = false;
    private String      m_PackageName = null;

    public ClassStateHandler(String language,
                             String packageName,
                             boolean isInner)
    {
        super(language);
        m_ModifiersState = false;
        m_IsInnerClass = isInner;
        m_BodyState = false;
        m_ForceAbstractOps = false;
        m_PackageName = packageName;
    }

    public ClassStateHandler(String language, String packageName)
    {
        this(language, packageName, false);
    }
    
    /**
     * The IsForceAbstractMethods is use to determine if all methods
     * should be abstract.  Generally, IsForceAbstractMethods will be
     * false.  Since all interface methods are required to be abstract
     * (Even when the abstract keyword is missing).
     *
     * @param value [in] The value of IsForceAbstractMethods.
     */
    public void setForceAbstractMethods(boolean value)
    {
        m_ForceAbstractOps = value;
    }

    /**
     * The IsForceAbstractMethods is use to determine if all methods
     * should be abstract.  Generally, IsForceAbstractMethods will be
     * false.  Since all interface methods are required to be abstract
     * (Even when the abstract keyword is missing).
     *
     * @return The value of IsForceAbstractMethods.
     */
    public boolean isForceAbstractMethods()
    {
        return m_ForceAbstractOps;
    }

    /**
     * Determines if the class declaration is an inner class or an
     * out class.
     *
     * @return true if the class is an inner class, false if the
     *              the class is an outer class.
     */
    public boolean isInnerClass()
    {
        return m_IsInnerClass;
    }

    protected String getPackageName()
    {
        return m_PackageName;
    }

    protected void setPackageName(String m_PackageName)
    {
        this.m_PackageName = m_PackageName;
    }
    
    /**
     * Add modifier information to the XMI structure.
     *
     * @param pToken [in] The token that specifies a modifier.
     */
    protected void handleModifier(ITokenDescriptor pToken, String language)
    {
        if(pToken == null) return;

        String value = pToken.getValue();

        super.handleModifier(pToken, language);

        if(isLeafModifier(value, language))
        {
            setNodeAttribute("isLeaf", true) ;
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

        String nameString = pToken.getValue();
        setNodeAttribute("name", nameString);

        long line = pToken.getLine();
        long col = pToken.getColumn();
        long pos = pToken.getPosition();
        long length = pToken.getLength();

        createTokenDescriptor("Name", line, col, pos, nameString, length);
    }
    
    /**
     * Create a new state handler to be added to the state mechanism.  If the
     * state is not a state that is being processed then a new state handler is 
     * not created.  The states of interest is <code>Expression List</code>
     *
     * @param stateName [in] The state name.
     * @param language [in] The langauge being processed.
     *
     * @return 
     */
    public StateHandler createSubStateHandler(String stateName, String language)
    {
 
        StateHandler retVal = null;

        m_ModifiersState = false;   
        if("Body".equals(stateName))
        {
            m_BodyState = true;
            retVal = this;    
        }
        else if("Generalization".equals(stateName))
        {
            retVal = new GeneralizationStateHandler(); 
        }
        else if("Realization".equals(stateName))
        {
            retVal = new RealizationStateHandler();    
        }
        else if("Variable Definition".equals(stateName))
        {
            retVal = new AttributeStateHandler(language, stateName);
        }
        else if(("Method Definition".equals(stateName)) ||           
               ("Method Declaration".equals(stateName)) )
        {
            retVal = new OperationStateHandler(language, 
                                                stateName, 
                                                OperationStateHandler.OPERATION, 
                                                isForceAbstractMethods());
        } 
        else if("Constructor Definition".equals(stateName))
        {
            retVal = new OperationStateHandler(language, 
                                                stateName, 
                                                OperationStateHandler.CONSTRUCTOR, 
                                                false);
        }
        else if("Destructor Definition".equals(stateName))
        {
            retVal = new OperationStateHandler(language, 
                                                stateName, 
                                                OperationStateHandler.DESTRUCTOR, 
                                                false);
        }
        else if("Class Declaration".equals(stateName))
        {
            retVal = new ClassStateHandler(language, m_PackageName, true);  
        }
        else if("Interface Declaration".equals(stateName))
        {
            retVal = new InterfaceStateHandler(language, null, true);
        }
        else if("Enumeration Declaration".equals(stateName))
        {
           retVal = new EnumStateHandler(language, null, true);
        }
        else if("Modifiers".equals(stateName))
        {
            // The class state handler will handle the modifiers state itself.
            // So, I want to return this.
            retVal = this;
            m_ModifiersState = true;
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

    /**
     * Initializes the state handler.  The class XMI node is
     * initializes.
     */
    public void initialize()
    {
        Node pNode = getDOMNode();
        if(pNode == null)
        {
            super.createTopLevelNode("UML:Class");

            // These are the default values.
            setNodeAttribute("isAbstract", false);
            setNodeAttribute("isLeaf", false);
            setNodeAttribute("visibility", "package");
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
                Node pNewClass = createNamespaceElement(
                                    pClassifierFeature,
                                    "UML:Class");

                if(pNewClass != null)
                {
                    setDOMNode(pNewClass);
                    
                    // These are the default values.
                    setNodeAttribute("isAbstract", false);
                    setNodeAttribute("isLeaf", false);
                    setNodeAttribute("visibility", "package");
                }
            }
        }
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


    public void processToken(ITokenDescriptor pToken, String language)
    {
        if(pToken == null) return;

        String tokenType = pToken.getType();
        if("Keyword".equals(tokenType))
        {
            handleKeyword(pToken);

            handleComment(pToken);
            recordStartToken(pToken);
        }
        else if("Name".equals(tokenType))
        {
            handleName(pToken);
            handleFilename(pToken);
            recordStartToken(pToken);

            // Because the name of the class can be the end of the class header
            // I must record it.  However the generalization or realization may
            // override the attribute value.
            long line = pToken.getLine();
            long col = pToken.getColumn();
            long pos = pToken.getPosition();
            long length = pToken.getLength();
            long endPos = pos + length;

            createTokenDescriptor("ClassHeadEndPosition", line, col, endPos, "", 0);
        }
        else if("Modifier".equals(tokenType))
        {
            handleModifier(pToken, language);

             handleComment(pToken);
             recordStartToken(pToken);
        }
        else if("Class Body End".equals(tokenType))
        {
            handleEndPostion(pToken);
        }
        else if("Class Body Start".equals(tokenType))
        {
            handleBodyStart(pToken);
        }
        else if("Stereotype".equals(tokenType))
        {
        	String value = pToken.getValue();
        	setNodeAttribute("Stereotype",value);
        }

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers.StateHandler#stateComplete(java.lang.String)
     */
    public void stateComplete(String stateName)
    {
        if("Modifiers".equals(stateName))
        {
           m_ModifiersState = false;
        }
        else if(!m_ModifiersState && !m_BodyState)
        {
           //WriteDocument(_T("C:\\Class_NEW.xml"));

            writeStartToken();

            if(m_PackageName != null && m_PackageName.trim().length() > 0)
            {
                createTokenDescriptor("Package", -1, -1, -1,
                                    m_PackageName,
                                    m_PackageName.length());
            }

            if (!isInnerClass())
                sendEvent();
        }

        if(m_BodyState)
        m_BodyState = false;
    }

}
