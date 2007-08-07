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
 * File       : InterfaceStateHandler.java
 * Created on : Dec 11, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers;

import org.dom4j.Node;

/**
 * @author Aztec
 */
public class InterfaceStateHandler extends ClassStateHandler
{


    public InterfaceStateHandler(
        String language,
        String packageName,
        boolean isInner)
    {
        super(language, packageName, isInner);
        setForceAbstractMethods(true);
    }

    public InterfaceStateHandler(String language, String packageName)
    {
        this(language, packageName, false);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers.StateHandler#initialize()
     */
    public void initialize()
    {
        Node pNode = getDOMNode();
        if(pNode == null)
        {
            createTopLevelNode("UML:Interface");

           // These are the default values.
            setNodeAttribute("isAbstract", true) ;
            setNodeAttribute("isLeaf", false) ;
        }
        else
        {
            // The inner interface token descriptor is really a 
            // sub node of the UML:Class XML node.  Therefore, it must
            // be a sub node of UML:Element.ownedElement.
            Node pClassifierFeature  = ensureElementExists(pNode, 
                                         "UML:Element.ownedElement", 
                                         "UML:Element.ownedElement");

            if(pClassifierFeature != null)
            {
                Node pNewClass = createNamespaceElement(pClassifierFeature,
                                 "UML:Interface");

                if(pNewClass != null)
                {
                    setDOMNode(pNewClass);
                }
            }
        }
        
        setNodeAttribute("Stereotype", "interface"); //NO I18N
    }
    
    

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers.StateHandler#stateComplete(java.lang.String)
     */
    public void stateComplete(String stateName)
    {
        super.stateComplete(stateName);
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

        if("Variable Definition".equals(stateName))
        {
            AttributeStateHandler handler = new AttributeStateHandler(language, stateName);
            handler.setDefaultVisibility("public");
	    
	    // an interface attribute is static final in Java even in the absense 
	    // of explicit modifiers stating that, whereis static final 
	    // characteristics of the attribute in the model is rather the 
	    // characteristics of the attribute, not characterictics of 
	    // the source code it was REd from
	    if (language != null && language.equalsIgnoreCase("Java")) 
	    {
		handler.setDefaultStatic(true);
		handler.setDefaultFinal(true);
	    }
            retVal = handler;
        }
        else if(("Method Definition".equals(stateName)) ||           
               ("Method Declaration".equals(stateName)) )
        {
            OperationStateHandler handler = null;
            handler = new OperationStateHandler(language, 
                                                stateName, 
                                                OperationStateHandler.OPERATION, 
                                                isForceAbstractMethods());
            handler.setDefaultVisibility("public");
            retVal = handler;
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
