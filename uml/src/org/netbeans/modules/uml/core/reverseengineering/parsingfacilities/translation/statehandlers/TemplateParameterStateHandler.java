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
