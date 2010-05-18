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
 * File       : ParameterStateHandler.java
 * Created on : Dec 11, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers;

import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenDescriptor;

/**
 * @author Aztec
 */
public class ParameterStateHandler extends TypeElementStateHandler
{
    private String m_Direction = null;

    public ParameterStateHandler(String language)
    {
        super(language, "Parameter");
    }

    public ParameterStateHandler(String language, String direction)
    {
        super(language, "Parameter");
        m_Direction = direction;
    }
    
    public ParameterStateHandler(String language, String direction, String stateName)
    {
        super(language, stateName);
        m_Direction = direction;
    }
    
    public void initialize() 
    {
        super.initialize();

        if(m_Direction != null)
        {
            setNodeAttribute("direction", m_Direction);
        }
        setNodeAttribute("isQuery", false);
    }
    
    public void processToken(ITokenDescriptor pToken, String language) 
    {
        if(pToken == null) return;
        
        String tokenType = pToken.getType();
        
        // If the parameter is a return type I do not care if the parameter is a
        // primitive.  It will always  be a return type.
        if("result".equals(m_Direction) == false)
        {
           // If the token type is primitive type then the parameter will
           // always be in.  Other wise use the default.  My need to be overridden
           // by a language specific implememtation.
            if("Primitive Type".equals(tokenType))
            {
                setNodeAttribute("direction", "in");
                createTokenDescriptor("IsPrimitive", -1, -1, -1, "true", -1);
            }
        }

        super.processToken(pToken, language);
    }
    
    /**
     * Retrieves the name of the feature that is being added.  This method 
     * is used by TypeElementStateHandler during processing.
     *
     *
     * @return 
     */
    public String getFeatureName()
    {
        return "UML:Parameter";
    }

    
    protected void handleModifier(ITokenDescriptor pToken, String language) 
    {
        if(pToken == null) return;
        
        long   line = pToken.getLine();
        long   col = pToken.getColumn();
        long   pos = pToken.getPosition();
        long   length = pToken.getLength();
        String value = pToken.getValue();

        createTokenDescriptor("Modifier", line, col, pos, value, length);

        // In some langauges certian modifiers specify that a class is abstract or 
        // constant.  So, I will query if the modifier should be handlec as  abstract
        // or leaf modifiers.  The query methods are virtual so they can be overriden
        // when specifing a new langauge.
        if(isLeafModifier(value, language))
        {
            setNodeAttribute("isQuery", true);
        }
    }

    protected boolean isAbstractModifier(String value) 
    {
        // No valid implementation in the C++ code base.
        return false;
    }

    protected boolean isOwnerScopeModifier(String value) 
    {
        // No valid implementation in the C++ code base.
        return false;
    }

}
