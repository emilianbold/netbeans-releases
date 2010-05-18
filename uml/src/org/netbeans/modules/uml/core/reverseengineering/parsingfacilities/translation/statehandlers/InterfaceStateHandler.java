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
