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
 * File       : TemplateParameterStateHandler.java
 * Created on : Dec 12, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers;

import org.dom4j.Node;

import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenDescriptor;

/**
 * @author Aztec
 */
public class TemplateParameterStateHandler extends StateHandler
{
    public StateHandler createSubStateHandler(String stateName, String language)
    {
        return super.createSubStateHandler(stateName, language);
    }
    
    public void initialize()
    {
        Node pCurNode = getDOMNode();

        if( pCurNode != null )
        {

            // The generalization token descriptor is really a 
            // sub node of the UML:Class XML node.  Therefore, I 
            // will create the TGeneralization under the passed
            // in node.  The TGeneralization node will then be
            // passed to StateHandler to use in the helper methods.
            Node pOwnedElement = ensureElementExists(pCurNode, 
                                         "UML:Element.ownedElement", 
                                         "UML:Element.ownedElement");

            if(pOwnedElement != null)
            {
                Node pParameter = createNode(pOwnedElement, 
                                            "UML:ParameterableElement");
                setDOMNode(pParameter);
            }
        }
    }
    
    public void stateComplete(String stateName) 
    {
        super.stateComplete(stateName);
    }
    
    public void processToken(ITokenDescriptor pToken, String lang)
    {
        if(pToken == null) return;
        
        String tokenType = pToken.getType();
        if("Name".equals(tokenType))
        {
           String nameString = pToken.getValue();

           long line = pToken.getLine();
           long col = pToken.getColumn();
           long pos = pToken.getPosition();
           long length = pToken.getLength();

           setNodeAttribute("name", nameString);
           createTokenDescriptor("Name", 
                                   line, 
                                   col, 
                                   pos, 
                                   nameString, 
                                   length);
        }
    }
}
